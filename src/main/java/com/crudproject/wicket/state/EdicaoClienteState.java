/*
 * EdicaoClienteState — campos editáveis do modal "Editar Cliente".
 * Nome, CPF/CNPJ, tipo e data permanecem imutáveis após o cadastro
 * (validado no service), portanto não fazem parte deste state.
 */
package com.crudproject.wicket.state;

import java.io.Serializable;

public class EdicaoClienteState implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long    idCliente;
    private String  email;
    private String  rgIe;
    private Boolean ativo;

    public Long    getIdCliente()        { return idCliente; }
    public void    setIdCliente(Long v)  { this.idCliente = v; }

    public String  getEmail()            { return email; }
    public void    setEmail(String v)    { this.email = v; }

    public String  getRgIe()             { return rgIe; }
    public void    setRgIe(String v)     { this.rgIe = v; }

    public Boolean getAtivo()            { return ativo; }
    public void    setAtivo(Boolean v)   { this.ativo = v; }
}
