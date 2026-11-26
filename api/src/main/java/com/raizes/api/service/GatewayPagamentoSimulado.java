package com.raizes.api.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.raizes.api.model.Pagamento;
import com.raizes.api.model.Pedido;
import com.raizes.api.model.enums.StatusPagamento;

@Service
public class GatewayPagamentoSimulado {

	private static final Logger log = LoggerFactory.getLogger(GatewayPagamentoSimulado.class);

	public Pagamento processar(Pedido pedido, boolean simularRecusa) {
		Pagamento pagamento = new Pagamento();
		pagamento.setPedido(pedido);
		pagamento.setValorPedido(pedido.getTotal());
		pagamento.setStatus(simularRecusa ? StatusPagamento.RECUSADO : StatusPagamento.APROVADO);
		pagamento.setCodigoTransacao("SIM-" + UUID.randomUUID());

		log.info("[PAGAMENTO SIMULADO] Pedido {} | R$ {} | {} | {}", pedido.getId(), pedido.getTotal(),
				pagamento.getStatus(), pagamento.getCodigoTransacao());
		return pagamento;
	}
}
