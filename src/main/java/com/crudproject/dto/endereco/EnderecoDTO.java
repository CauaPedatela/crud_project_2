package com.crudproject.dto.endereco;

import com.crudproject.model.TipoEndereco;

//  DTO unificado para Endereço — usado embedded no ClienteDTO.
//
//  Quando vem dentro do ClienteDTO numa requisição PUT:
//    - id preenchido     → atualizar esse endereço
//    - id null/ausente   → criar novo endereço
//    - sumiu da lista    → deletar (sync semantics)
//
// Quando vem num POST:
//    - id é sempre ignorado (o banco gera)

public class EnderecoDTO {

    private Long id;
    private TipoEndereco tipo;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private String pais;
    private Boolean principal;

    // ============================================================
    // Getters e Setters
    // ============================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoEndereco getTipo() { return tipo; }
    public void setTipo(TipoEndereco tipo) { this.tipo = tipo; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public Boolean getPrincipal() { return principal; }
    public void setPrincipal(Boolean principal) { this.principal = principal; }
}
