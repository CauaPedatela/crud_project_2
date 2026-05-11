package com.crudproject.dto.endereco;

/**
 * DTO de entrada para cadastro/atualização de Endereço.
 *
 * Não tem id (o banco gera no INSERT, e no UPDATE o id
 * vem como path param na URL — ex: PUT /api/enderecos/{id}).
 *
 * O campo clienteId substitui o objeto Cliente completo:
 * quem chama a API só precisa informar o ID do cliente dono,
 * sem montar o objeto Cliente inteiro no JSON.
 */
public class EnderecoCadastroDTO {

    // ID do cliente dono desse endereço (FK).
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
