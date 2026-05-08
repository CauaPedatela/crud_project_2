package com.crudproject.dto.cliente;

import com.crudproject.model.TipoPessoa;

import java.time.LocalDate;

public class ClienteCadastroDTO {

    private TipoPessoa tipoPessoa;

    // Campos de PF
    private String cpf;
    private String nome;
    private String rg;
    private LocalDate dataNascimento;

    // Campos de PJ
    private String cnpj;
    private String razaoSocial;
    private String inscricaoEstadual;
    private LocalDate dataCriacao;

    // Comuns
    private String email;
    private Boolean ativo;

    // Getters e setters

    public TipoPessoa getTipoPessoa() { return tipoPessoa; }
    public void setTipoPessoa(TipoPessoa tipoPessoa) { this.tipoPessoa = tipoPessoa; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getInscricaoEstadual() { return inscricaoEstadual; }
    public void setInscricaoEstadual(String inscricaoEstadual) { this.inscricaoEstadual = inscricaoEstadual; }

    public LocalDate getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDate dataCriacao) { this.dataCriacao = dataCriacao; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    // Helpers — derivam informação do próprio estado, sem regras de negócio

    public boolean isPessoaFisica() { return tipoPessoa == TipoPessoa.FISICA; }
    public boolean isPessoaJuridica() { return tipoPessoa == TipoPessoa.JURIDICA; }
}
