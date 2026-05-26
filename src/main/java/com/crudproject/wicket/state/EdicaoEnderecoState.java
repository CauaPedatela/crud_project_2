/*
 * EdicaoEnderecoState — campos editáveis no modal "Editar Endereço".
 * Logradouro, bairro, cidade, CEP, estado, país e tipo permanecem imutáveis;
 * só número, complemento, telefone e status principal são editáveis.
 */
package com.crudproject.wicket.state;

import java.io.Serializable;

public class EdicaoEnderecoState implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long    idEndereco;
    private String  numero;
    private String  complemento;
    private String  telefone;
    private Boolean principal;

    public Long    getIdEndereco()        { return idEndereco; }
    public void    setIdEndereco(Long v)  { this.idEndereco = v; }

    public String  getNumero()            { return numero; }
    public void    setNumero(String v)    { this.numero = v; }

    public String  getComplemento()       { return complemento; }
    public void    setComplemento(String v) { this.complemento = v; }

    public String  getTelefone()          { return telefone; }
    public void    setTelefone(String v)  { this.telefone = v; }

    public Boolean getPrincipal()         { return principal; }
    public void    setPrincipal(Boolean v) { this.principal = v; }
}
