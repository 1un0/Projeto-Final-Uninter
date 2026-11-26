package com.raizes.api.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.raizes.api.dto.ResgateRequest;
import com.raizes.api.model.Fidelidade;
import com.raizes.api.model.Pedido;
import com.raizes.api.model.enums.AcaoAuditoria;
import com.raizes.api.model.enums.StatusPedido;
import com.raizes.api.model.enums.TipoFidelidade;
import com.raizes.api.repository.FidelidadeRepository;
import com.raizes.api.repository.PedidoRepository;
import com.raizes.api.security.UsuarioAutenticado;
import com.raizes.api.service.AuditoriaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/fidelidade")
public class FidelidadeController {

	private final FidelidadeRepository repository;
	private final PedidoRepository pedidoRepository;
	private final AuditoriaService auditoria;

	FidelidadeController(FidelidadeRepository repository, PedidoRepository pedidoRepository,
			AuditoriaService auditoria) {
		this.repository = repository;
		this.pedidoRepository = pedidoRepository;
		this.auditoria = auditoria;
	}

	@GetMapping("/saldo")
	public Map<String, Integer> saldo(@AuthenticationPrincipal UsuarioAutenticado logado) {
		return Map.of("saldo", calcularSaldo(logado.id()));
	}
	@GetMapping("/extrato")
	public List<Fidelidade> extrato(@AuthenticationPrincipal UsuarioAutenticado logado) {
		return repository.findByUsuarioId(logado.id());
	}

	@PostMapping("/resgate")
	@Transactional
	public Pedido resgatar(@Valid @RequestBody ResgateRequest req,
			@AuthenticationPrincipal UsuarioAutenticado logado) {
		Pedido pedido = pedidoRepository.findById(req.idPedido())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido nao encontrado"));
		if (!pedido.getUsuario().getId().equals(logado.id())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Os pontos só podem ser utilizados nos pedidos do usuario");
		}
		if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "So é possigvel utilizar os pontos depois de pago");
		}
		int saldo = calcularSaldo(logado.id());
		if (req.pontos() > saldo) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Saldo insuficiente (saldo atual: " + saldo + ")");
		}
		BigDecimal desconto = BigDecimal.valueOf(req.pontos(), 1).setScale(2, RoundingMode.DOWN); // 10 pts = R$1
		if (desconto.compareTo(pedido.getTotal()) > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Desconto (R$ " + desconto + ") maior que o total do pedido (R$ " + pedido.getTotal() + ")");
		}

		pedido.setTotal(pedido.getTotal().subtract(desconto));
		pedidoRepository.save(pedido);

		Fidelidade resgate = new Fidelidade();
		resgate.setPedido(pedido);
		resgate.setUsuario(pedido.getUsuario());
		resgate.setPontos(req.pontos());
		resgate.setTipo(TipoFidelidade.RESGATE);
		repository.save(resgate);

		auditoria.registrar(logado.id(), AcaoAuditoria.ATUALIZACAO, "Pedido", pedido.getId(),
				"Resgate de " + req.pontos() + " pontos (desconto R$ " + desconto + ")");
		return pedido;
	}

	private int calcularSaldo(Long idUsuario) {
		int saldo = 0;
		for (Fidelidade f : repository.findByUsuarioId(idUsuario)) {
			saldo += f.getTipo() == TipoFidelidade.ACUMULO ? f.getPontos() : -f.getPontos();
		}
		return saldo;
	}
}
