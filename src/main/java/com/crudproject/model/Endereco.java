package com.crudproject.model;

import javax.persistence.*;

/**
 * Representa um Endereço vinculado a um Cliente.
 *
 * Relação: muitos Endereços pertencem a um Cliente (N:1).
 * No banco, a coluna "cliente_id" guarda o ID do cliente dono
 * desse endereço — é a Foreign Key (chave estrangeira).
 */

@Entity
@Table(name = "tb_endereco") // define o nome da tabela no banco
public class Endereco {

    /**
     * Chave primária da tabela.
     * Gerada automaticamente pelo banco (AUTO INCREMENT).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "logradouro", nullable = false, length = 200)
    private String logradouro;

    @Column(name = "numero", length = 10)
    private String numero;

    @Column(name = "cep", nullable = false, length = 9)
    private String cep;

    @Column(name = "bairro", length = 100)
    private String bairro;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "cidade", nullable = false, length = 100)
    private String cidade;

    @Column(name = "estado", nullable = false, length = 2)
    private String estado;

    @Column(name = "endereco_principal", nullable = false)
    private Boolean enderecoPrincipal;

    @Column(name = "complemento", length = 100)
    private String complemento;

    /**
     * Referência ao Cliente dono desse endereço.
     * @ManyToOne → muitos Endereços pertencem a um Cliente
     * @JoinColumn → cria a coluna "cliente_id" nessa tabela (Foreign Key)
     * No Java você enxerga um objeto Cliente.
     * No banco, o Hibernate salva apenas o ID do cliente.
     */

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // -------------------------
    // Getters e Setters
    // -------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Boolean getEnderecoPrincipal() {
        return enderecoPrincipal;
    }

    public void setEnderecoPrincipal(Boolean enderecoPrincipal) {
        this.enderecoPrincipal = enderecoPrincipal;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
}
