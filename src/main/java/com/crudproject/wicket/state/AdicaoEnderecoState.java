/*
 * AdicaoEnderecoState — todos os campos do modal "Adicionar Endereço"
 * da página de detalhes. Carrega defaults (tipo = RESIDENCIAL, país = Brasil).
 */
package com.crudproject.wicket.state;

import com.crudproject.model.TipoEndereco;

import java.io.Serializable;

public class AdicaoEnderecoState implements Serializable {

    private static final long serialVersionUID = 1L;

    private TipoEndereco tipo        = TipoEndereco.RESIDENCIAL;
    private String       logradouro;
    private String       numero;
    private String       complemento;
    private String       bairro;
    private String       cidade;
    private String       estado;
    private String       cep;
    private String       pais        = "Brasil";
    private String       telefone;
    private Boolean      principal   = false;

    public static AdicaoEnderecoState inicial() {
        return new AdicaoEnderecoState();
    }

    public void resetar() {
        this.tipo        = TipoEndereco.RESIDENCIAL;
        this.logradouro  = null;
        this.numero      = null;
        this.complemento = null;
        this.bairro      = null;
        this.cidade      = null;
        this.estado      = null;
        this.cep         = null;
        this.pais        = "Brasil";
        this.telefone    = null;
        this.principal   = false;
    }

    public TipoEndereco getTipo()                 { return tipo; }
    public void         setTipo(TipoEndereco v)   { this.tipo = v; }

    public String       getLogradouro()           { return logradouro; }
    public void         setLogradouro(String v)   { this.logradouro = v; }

    public String       getNumero()               { return numero; }
    public void         setNumero(String v)       { this.numero = v; }

    public String       getComplemento()          { return complemento; }
    public void         setComplemento(String v)  { this.complemento = v; }

    public String       getBairro()               { return bairro; }
    public void         setBairro(String v)       { this.bairro = v; }

    public String       getCidade()               { return cidade; }
    public void         setCidade(String v)       { this.cidade = v; }

    public String       getEstado()               { return estado; }
    public void         setEstado(String v)       { this.estado = v; }

    public String       getCep()                  { return cep; }
    public void         setCep(String v)          { this.cep = v; }

    public String       getPais()                 { return pais; }
    public void         setPais(String v)         { this.pais = v; }

    public String       getTelefone()             { return telefone; }
    public void         setTelefone(String v)     { this.telefone = v; }

    public Boolean      getPrincipal()            { return principal; }
    public void         setPrincipal(Boolean v)   { this.principal = v; }
}
