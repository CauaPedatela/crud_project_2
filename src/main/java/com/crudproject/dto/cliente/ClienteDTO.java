package com.crudproject.dto.cliente;

import com.crudproject.dto.endereco.EnderecoDTO;
import com.crudproject.model.TipoPessoa;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

// DTO unificado para Cliente — usado em POST e PUT.
//
// O mesmo formato vale para criar e atualizar. A diferença está
// apenas em quais campos vêm preenchidos:

//  POST (criar)        → lista de endereços sem ids
//  PUT  (atualizar)    → id do cliente vem na URL (não no body);
//                        endereços com id existente = update,
//                        endereços sem id = novos,
//                        endereços que sumiram da lista = deletados
//
// Já o campo "id" dos endereços é FUNCIONAL — dirige a lógica de sync.]
//
// Implements Serializable: usado como campo intermediário no form de criação
// da ListagemClientesPage; o Wicket serializa a página inteira na sessão.

public class ClienteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private TipoPessoa tipoPessoa;
    private String nome;
    private String cpfCnpj;
    private String rgInscricaoEstadual;
    private LocalDate dataNascimento;
    private String email;
    private Boolean ativo;
    private List<EnderecoDTO> enderecos;

    // ============================================================
    // Getters e Setters
    // ============================================================

    public TipoPessoa getTipoPessoa() { return tipoPessoa; }
    public void setTipoPessoa(TipoPessoa tipoPessoa) { this.tipoPessoa = tipoPessoa; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }

    public String getRgInscricaoEstadual() { return rgInscricaoEstadual; }
    public void setRgInscricaoEstadual(String rgInscricaoEstadual) { this.rgInscricaoEstadual = rgInscricaoEstadual; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public List<EnderecoDTO> getEnderecos() { return enderecos; }
    public void setEnderecos(List<EnderecoDTO> enderecos) { this.enderecos = enderecos; }

    // ============================================================
    // Helpers
    // ============================================================

    public boolean isPessoaFisica() { return tipoPessoa == TipoPessoa.FISICA; }
    public boolean isPessoaJuridica() { return tipoPessoa == TipoPessoa.JURIDICA; }
}
