/*
 * EdicaoClienteState — campos editáveis do modal "Editar Cliente".
 * Nome agora também é editável (a partir desta versão).
 * CPF/CNPJ, tipo e dataCadastro permanecem imutáveis após o cadastro
 * (validado no service), portanto não fazem parte deste state.
 */
package com.crudproject.wicket.state;

import java.io.Serializable;

public class EdicaoClienteState implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long    idCliente;
    private String  nome;       // ← agora editável
    private String  email;
    private String  rgIe;
    private Boolean ativo;

    public Long    getIdCliente()        { return idCliente; }
    public void    setIdCliente(Long v)  { this.idCliente = v; }

    public String  getNome()             { return nome; }
    public void    setNome(String v)     { this.nome = v; }

    public String  getEmail()            { return email; }
    public void    setEmail(String v)    { this.email = v; }

    public String  getRgIe()             { return rgIe; }
    public void    setRgIe(String v)     { this.rgIe = v; }

    public Boolean getAtivo()            { return ativo; }
    public void    setAtivo(Boolean v)   { this.ativo = v; }
}
