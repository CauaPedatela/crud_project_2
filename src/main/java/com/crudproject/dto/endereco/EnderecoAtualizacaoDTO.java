package com.crudproject.dto.endereco;

/**
 * DTO de entrada para ATUALIZAÇÃO de Endereço (PUT /api/enderecos/{id}).
 *
 * Diferença intencional para EnderecoCadastroDTO:
 *
 * - SEM clienteId
 *     → Um endereço já criado não pode ser "movido" para outro cliente.
 *       O vínculo é imutável. Por isso o DTO de atualização não aceita
 *       esse campo — fica explícito que a operação não tem como mudar
 *       o dono do endereço.
 *
 * O id do endereço vai como path param na URL, não no body.
 *
 * Para mudar o flag "principal", há a rota dedicada
 * PUT /api/enderecos/{id}/principal.
 */
public class EnderecoAtualizacaoDTO {

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
