package com.crudproject.dto.cliente;

import com.crudproject.dto.endereco.EnderecoResponseDTO;
import com.crudproject.model.TipoPessoa;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// DTO de saída para Cliente.
// Inclui o id e a dataCadastro (gerados pelo banco/servidor) e
// a lista completa de endereços já com seus ids.
// Implementa Serializable para que o Wicket consiga salvar o estado
// da página na sessão HTTP (necessário para a paginação funcionar).

public class ClienteResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private TipoPessoa tipoPessoa;
    private String nome;
    private String cpfCnpj;
    private String rgInscricaoEstadual;
    private LocalDate dataNascimento;
    private String email;
    private Boolean ativo;
    private LocalDateTime dataCadastro;
    private List<EnderecoResponseDTO> enderecos;

    // ============================================================
    // Getters e Setters
    // ============================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public List<EnderecoResponseDTO> getEnderecos() { return enderecos; }
    public void setEnderecos(List<EnderecoResponseDTO> enderecos) { this.enderecos = enderecos; }
}
