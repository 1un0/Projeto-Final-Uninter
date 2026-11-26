package com.raizes.api.model;

import java.util.Objects;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.raizes.api.model.enums.Perfil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "usuarios")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	private String nome;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Perfil perfil = Perfil.CLIENTE;

	@Size(min = 11, max = 14)
	private String cpf;

	@NotBlank
	@Email
	@Column(nullable = false, unique = true)
	private String email;

// TESTAR BCRYPT
	@NotBlank
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(length = 60, nullable = false)
	private String senha;

	@Column(name = "aceita_dados")
	private Boolean aceitaDados = false;

	@Column(name = "aceita_newsletter")
	private Boolean aceitaNewsletter = false;

	@Column(name = "aceita_fidelidade")
	private Boolean aceitaFidelidade = false;

	@Column(name = "consentimento_data")
	private LocalDateTime consentimentoData;

	@CreationTimestamp
	@Column(name = "registro_data", updatable = false)
	private LocalDateTime registroData;

	@Column(name = "conta_ativa")
	private Boolean contaAtiva = true;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Perfil getPerfil() {
		return perfil;
	}

	public void setPerfil(Perfil perfil) {
		this.perfil = perfil;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public Boolean getAceitaDados() {
		return aceitaDados;
	}

	public void setAceitaDados(Boolean aceitaDados) {
		this.aceitaDados = aceitaDados;
	}

	public Boolean getAceitaNewsletter() {
		return aceitaNewsletter;
	}

	public void setAceitaNewsletter(Boolean aceitaNewsletter) {
		this.aceitaNewsletter = aceitaNewsletter;
	}

	public Boolean getAceitaFidelidade() {
		return aceitaFidelidade;
	}

	public void setAceitaFidelidade(Boolean aceitaFidelidade) {
		this.aceitaFidelidade = aceitaFidelidade;
	}

	public LocalDateTime getConsentimentoData() {
		return consentimentoData;
	}

	public void setConsentimentoData(LocalDateTime consentimentoData) {
		this.consentimentoData = consentimentoData;
	}

	public LocalDateTime getRegistroData() {
		return registroData;
	}

	public void setRegistroData(LocalDateTime registroData) {
		this.registroData = registroData;
	}

	public Boolean getContaAtiva() {
		return contaAtiva;
	}

	public void setContaAtiva(Boolean contaAtiva) {
		this.contaAtiva = contaAtiva;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		return id != null && Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
