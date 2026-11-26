package com.raizes.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.raizes.api.dto.PagamentoRequest;
import com.raizes.api.model.Fidelidade;
import com.raizes.api.model.Pagamento;
import com.raizes.api.model.Pedido;
import com.raizes.api.model.Usuario;
import com.raizes.api.model.enums.AcaoAuditoria;
import com.raizes.api.model.enums.Perfil;
import com.raizes.api.model.enums.StatusPagamento;
import com.raizes.api.model.enums.StatusPedido;
import com.raizes.api.model.enums.TipoFidelidade;
import com.raizes.api.repository.FidelidadeRepository;
import com.raizes.api.repository.PagamentoRepository;
import com.raizes.api.repository.PedidoRepository;
import com.raizes.api.security.UsuarioAutenticado;
import com.raizes.api.service.AuditoriaService;
import com.raizes.api.service.GatewayPagamentoSimulado;
import com.raizes.api.service.NotificacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

	private final PagamentoRepository repository;
	private final PedidoRepository pedidoRepository;
	private final FidelidadeRepository fidelidadeRepository;
	private final GatewayPagamentoSimulado gateway;
	private final NotificacaoService notificacao;
	private final AuditoriaService auditoria;

	PagamentoController(PagamentoRepository repository, PedidoRepository pedidoRepository,
			FidelidadeRepository fidelidadeRepository, GatewayPagamentoSimulado gateway,
			NotificacaoService notificacao, AuditoriaService auditoria) {
		this.repository = repository;
		this.pedidoRepository = pedidoRepository;
		this.fidelidadeRepository = fidelidadeRepository;
		this.gateway = gateway;
		this.notificacao = notificacao;
		this.auditoria = auditoria;
	}

	@GetMapping
	public List<Pagamento> findAll(@RequestParam(required = false) Long pedido) {
		return pedido == null ? repository.findAll() : repository.findByPedidoId(pedido);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Pagamento> findById(@PathVariable long id) {
		return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public Pagamento pagar(@Valid @RequestBody PagamentoRequest req,
			@AuthenticationPrincipal UsuarioAutenticado logado) {
		Pedido pedido = pedidoRepository.findById(req.idPedido())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido nao encontrado"));
		if (logado.perfil() == Perfil.CLIENTE && !pedido.getUsuario().getId().equals(logado.id())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este pedido e de outro usuario");
		}
		if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Pedido nao esta aguardando pagamento (status: " + pedido.getStatus() + ")");
		}

		Pagamento pagamento = repository.save(gateway.processar(pedido, Boolean.TRUE.equals(req.simularRecusa())));

		if (pagamento.getStatus() == StatusPagamento.APROVADO) {
			pedido.setStatus(StatusPedido.PAGO);
			pedidoRepository.save(pedido);
			Usuario cliente = pedido.getUsuario();
			acumularPontos(pedido, cliente);
			notificacao.enviarEmail(cliente.getEmail(), "Pagamento aprovado",
					"Seu pedido #" + pedido.getId() + " no valor de R$ " + pedido.getTotal() + " foi pago.");
		}

		auditoria.registrar(logado.id(), AcaoAuditoria.CRIACAO, "Pagamento", pagamento.getId(),
				"Pagamento " + pagamento.getStatus() + " do pedido " + pedido.getId());
		return pagamento;
	}

	private void acumularPontos(Pedido pedido, Usuario cliente) {
		int pontos = pedido.getTotal().intValue();
		if (!Boolean.TRUE.equals(cliente.getAceitaFidelidade()) || pontos <= 0) {
			return;
		}
		Fidelidade fidelidade = new Fidelidade();
		fidelidade.setPedido(pedido);
		fidelidade.setUsuario(cliente);
		fidelidade.setPontos(pontos);
		fidelidade.setTipo(TipoFidelidade.ACUMULO);
		fidelidadeRepository.save(fidelidade);
	}
}
