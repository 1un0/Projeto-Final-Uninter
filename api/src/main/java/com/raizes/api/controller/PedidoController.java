package com.raizes.api.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.raizes.api.model.Estoque;
import com.raizes.api.model.ItemPedido;
import com.raizes.api.model.Pedido;
import com.raizes.api.model.Produto;
import com.raizes.api.model.enums.Perfil;
import com.raizes.api.model.enums.StatusPedido;
import com.raizes.api.security.UsuarioAutenticado;
import com.raizes.api.repository.EstoqueRepository;
import com.raizes.api.repository.PedidoRepository;
import com.raizes.api.repository.ProdutoRepository;
import com.raizes.api.repository.UnidadeRepository;
import com.raizes.api.repository.UsuarioRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

	private final PedidoRepository repository;
	private final UsuarioRepository usuarioRepository;
	private final UnidadeRepository unidadeRepository;
	private final ProdutoRepository produtoRepository;
	private final EstoqueRepository estoqueRepository;

	PedidoController(PedidoRepository repository, UsuarioRepository usuarioRepository,
			UnidadeRepository unidadeRepository, ProdutoRepository produtoRepository,
			EstoqueRepository estoqueRepository) {
		this.repository = repository;
		this.usuarioRepository = usuarioRepository;
		this.unidadeRepository = unidadeRepository;
		this.produtoRepository = produtoRepository;
		this.estoqueRepository = estoqueRepository;
	}

	@GetMapping
	public List<Pedido> findAll(@RequestParam(required = false) Long usuario,
			@RequestParam(required = false) Long unidade) {
		if (usuario != null) return repository.findByUsuarioId(usuario);
		if (unidade != null) return repository.findByUnidadeId(unidade);
		return repository.findAll();
	}

	@GetMapping("/meus")
	public List<Pedido> meusPedidos(@AuthenticationPrincipal UsuarioAutenticado logado) {
		return repository.findByUsuarioId(logado.id());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Pedido> findById(@PathVariable long id) {
		return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public Pedido create(@Valid @RequestBody Pedido pedido, @AuthenticationPrincipal UsuarioAutenticado logado) {
		if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O pedido precisa ter pelo menos um item");
		}

		Long idUsuario = logado.id();
		if (logado.perfil() != Perfil.CLIENTE && pedido.getUsuario() != null && pedido.getUsuario().getId() != null) {
			idUsuario = pedido.getUsuario().getId();
		}

		pedido.setId(null);
		pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
		pedido.setUsuario(usuarioRepository.findById(idUsuario)
				.filter(u -> Boolean.TRUE.equals(u.getContaAtiva()))
				.orElseThrow(() -> erro("Usuario nao encontrado ou inativo")));
		pedido.setUnidade(unidadeRepository.findById(pedido.getUnidade().getId())
				.filter(u -> Boolean.TRUE.equals(u.getAtivo()))
				.orElseThrow(() -> erro("Unidade nao encontrada ou inativa")));

		BigDecimal total = BigDecimal.ZERO;
		for (ItemPedido item : pedido.getItens()) {
			if (item.getQuantidade() == null || item.getQuantidade() < 1) {
				throw erro("Quantidade invalida");
			}
			Produto produto = produtoRepository.findById(item.getProduto().getId())
					.filter(p -> Boolean.TRUE.equals(p.getAtivo()))
					.orElseThrow(() -> erro("Produto " + item.getProduto().getId() + " nao encontrado ou inativo"));

			Estoque estoque = estoqueRepository
					.findByProdutoIdAndUnidadeIdAndAtivoTrue(produto.getId(), pedido.getUnidade().getId())
					.orElseThrow(() -> erro("Produto " + produto.getNomeProduto() + " sem estoque nesta unidade"));
			if (estoque.getQuantidadeEstoque() < item.getQuantidade()) {
				throw erro("Estoque insuficiente para " + produto.getNomeProduto()
						+ " (disponivel: " + estoque.getQuantidadeEstoque() + ")");
			}
			estoque.setQuantidadeEstoque(estoque.getQuantidadeEstoque() - item.getQuantidade());
			estoqueRepository.save(estoque);

			item.setId(null);
			item.setProduto(produto);
			item.setPrecoUnitario(produto.getValor());
			item.setPedido(pedido);
			total = total.add(produto.getValor().multiply(BigDecimal.valueOf(item.getQuantidade())));
		}
		pedido.setTotal(total);
		return repository.save(pedido);
	}

	@PatchMapping("/{id}/status")
	@Transactional
	public ResponseEntity<Pedido> atualizarStatus(@PathVariable long id, @RequestParam StatusPedido status) {
		return repository.findById(id).map(pedido -> {
			StatusPedido atual = pedido.getStatus();
			if (atual == StatusPedido.CANCELADO || atual == StatusPedido.ENTREGUE) {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"Pedido " + atual + " nao pode mudar de status");
			}
			if (status == StatusPedido.CANCELADO) {
				for (ItemPedido item : pedido.getItens()) {
					estoqueRepository.findByProdutoIdAndUnidadeIdAndAtivoTrue(item.getProduto().getId(),
							pedido.getUnidade().getId()).ifPresent(estoque -> {
								estoque.setQuantidadeEstoque(estoque.getQuantidadeEstoque() + item.getQuantidade());
								estoqueRepository.save(estoque);
							});
				}
			}
			pedido.setStatus(status);
			return ResponseEntity.ok(repository.save(pedido));
		}).orElse(ResponseEntity.notFound().build());
	}

	private static ResponseStatusException erro(String mensagem) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
	}
}
