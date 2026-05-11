package com.crudproject.dto.endereco;

/**
 * DTO de saída de qualquer operação com Endereço.
 *
 * Exporta clienteId (não o objeto Cliente completo) para evitar
 * ciclos infinitos na serialização JSON e não vazar dados do cliente.
 */
public class EnderecoResponseDTO {

    private Long id;
    private Long clienteId;

    private String logradouro;
    private String numero;
    private String cep;
    private String bairro;
    private String telefone;
    private String cidade;
    private String estado;
    private Boolean enderecoPrincipal;
    private String complemento;

    // Getters e setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Boolean getEnderecoPrincipal() { return enderecoPrincipal; }
    public void setEnderecoPrincipal(Boolean enderecoPrincipal) { this.enderecoPrincipal = enderecoPrincipal; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }
}
