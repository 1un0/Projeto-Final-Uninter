package com.raizes.api.model;

import java.util.Objects;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.raizes.api.model.enums.TipoFidelidade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "fidelidade")
public class Fidelidade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_pedido", nullable = false)
	private Pedido pedido;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_usuario", nullable = false)
	private Usuario usuario;

	@Column(nullable = false)
	private Integer pontos;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoFidelidade tipo;

	@CreationTimestamp
	@Column(name = "registro_data", updatable = false)
	private LocalDateTime registroData;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Integer getPontos() {
		return pontos;
	}

	public void setPontos(Integer pontos) {
		this.pontos = pontos;
	}

	public TipoFidelidade getTipo() {
		return tipo;
	}

	public void setTipo(TipoFidelidade tipo) {
		this.tipo = tipo;
	}

	public LocalDateTime getRegistroData() {
		return registroData;
	}

	public void setRegistroData(LocalDateTime registroData) {
		this.registroData = registroData;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Fidelidade other = (Fidelidade) obj;
		return id != null && Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
