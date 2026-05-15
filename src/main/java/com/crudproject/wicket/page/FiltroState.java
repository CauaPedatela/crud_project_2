/*
 * FiltroState — representa o estado atual dos filtros e da busca na tela de listagem.
 * É criado uma vez pela página principal e passado para os panels que precisam
 * ler ou gravar critérios de pesquisa. Implementa Serializable pois o Wicket
 * precisa salvar o estado da página na sessão HTTP.
 */
package com.crudproject.wicket.page;

import java.io.Serializable;

public class FiltroState implements Serializable {

    private static final long serialVersionUID = 1L;

    private String termoBusca        = "";
    private String filtroAtivo       = "todos";
    private String filtroTipo        = "todos";
    private String dataCriacaoInicio = "";
    private String dataCriacaoFim    = "";

    public String getTermoBusca()                    { return termoBusca; }
    public void   setTermoBusca(String v)            { this.termoBusca = v; }

    public String getFiltroAtivo()                   { return filtroAtivo; }
    public void   setFiltroAtivo(String v)           { this.filtroAtivo = v; }

    public String getFiltroTipo()                    { return filtroTipo; }
    public void   setFiltroTipo(String v)            { this.filtroTipo = v; }

    public String getDataCriacaoInicio()             { return dataCriacaoInicio; }
    public void   setDataCriacaoInicio(String v)     { this.dataCriacaoInicio = v; }

    public String getDataCriacaoFim()                { return dataCriacaoFim; }
    public void   setDataCriacaoFim(String v)        { this.dataCriacaoFim = v; }
}
