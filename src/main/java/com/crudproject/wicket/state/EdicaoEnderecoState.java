/*
 * EdicaoEnderecoState — campos editáveis no modal "Editar Endereço".
 * A partir desta versão, TODOS os campos do endereço são editáveis
 * (logradouro, bairro, cidade, estado, CEP, país), além do número,
 * complemento, telefone e status principal que já eram editáveis.
 *
 * Apenas o "tipo" (RESIDENCIAL/COMERCIAL) continua somente no add — para
 * mudá-lo, basta excluir e adicionar de novo (caso de uso raro).
 */
package com.crudproject.wicket.state;

import java.io.Serializable;

public class EdicaoEnderecoState implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long    idEndereco;

    // Campos que agora também são editáveis
    private String  logradouro;
    private String  bairro;
    private String  cidade;
    private String  estado;
    private String  cep;
    private String  pais;

    // Campos que já eram editáveis
    private String  numero;
    private String  complemento;
    private String  telefone;
    private Boolean principal;

    public Long    getIdEndereco()         { return idEndereco; }
    public void    setIdEndereco(Long v)   { this.idEndereco = v; }

    public String  getLogradouro()         { return logradouro; }
    public void    setLogradouro(String v) { this.logradouro = v; }

    public String  getBairro()             { return bairro; }
    public void    setBairro(String v)     { this.bairro = v; }

    public String  getCidade()             { return cidade; }
    public void    setCidade(String v)     { this.cidade = v; }

    public String  getEstado()             { return estado; }
    public void    setEstado(String v)     { this.estado = v; }

    public String  getCep()                { return cep; }
    public void    setCep(String v)        { this.cep = v; }

    public String  getPais()               { return pais; }
    public void    setPais(String v)       { this.pais = v; }

    public String  getNumero()             { return numero; }
    public void    setNumero(String v)     { this.numero = v; }

    public String  getComplemento()        { return complemento; }
    public void    setComplemento(String v){ this.complemento = v; }

    public String  getTelefone()           { return telefone; }
    public void    setTelefone(String v)   { this.telefone = v; }

    public Boolean getPrincipal()          { return principal; }
    public void    setPrincipal(Boolean v) { this.principal = v; }
}
