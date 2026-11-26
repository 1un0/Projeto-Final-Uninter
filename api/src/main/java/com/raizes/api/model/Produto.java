package com.raizes.api.model;

import java.util.Objects;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "produto")
public class Produto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(name = "nome_produto", nullable = false, unique = true)
	private String nomeProduto;

	@NotNull
	@DecimalMin("0.00")
	@Column(precision = 10, scale = 2, nullable = false)
	private BigDecimal valor;

	@CreationTimestamp
	@Column(name = "registro_data", updatable = false)
	private LocalDateTime registroData;

	private Boolean sazonal = false;

	private Boolean ativo = true;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNomeProduto() {
		return nomeProduto;
	}

	public void setNomeProduto(String nomeProduto) {
		this.nomeProduto = nomeProduto;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public LocalDateTime getRegistroData() {
		return registroData;
	}

	public void setRegistroData(LocalDateTime registroData) {
		this.registroData = registroData;
	}

	public Boolean getSazonal() {
		return sazonal;
	}

	public void setSazonal(Boolean sazonal) {
		this.sazonal = sazonal;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Produto other = (Produto) obj;
		return id != null && Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
