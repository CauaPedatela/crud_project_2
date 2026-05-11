package com.crudproject.dto.cliente;

import java.time.LocalDate;

/**
 * DTO de entrada para ATUALIZAÇÃO de Cliente (PUT /api/clientes/{id}).
 *
 * Diferenças intencionais para ClienteCadastroDTO:
 *
 * - SEM tipoPessoa
 *     → tipoPessoa é imutável após o cadastro. Como o cliente já existe
 *       no banco com seu tipo definido, não faz sentido aceitar esse
 *       campo aqui. O Service usa o tipo do banco para decidir quais
 *       validações aplicar (PF ou PJ).
 *
 * - SEM enderecos
 *     → Endereços são gerenciados em rotas próprias do EnderecoController
 *       (POST/PUT/DELETE /api/enderecos). PUT em cliente não toca neles.
 *
 * O id do cliente vai como path param na URL, não no body.
 */
public class ClienteAtualizacaoDTO {

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
}
