package com.crudproject.service;

import java.io.Serializable;
import java.util.List;

// Resultado de uma importação em lote via planilha Excel.
// Serializable porque é guardado como campo de página Wicket (serializada na sessão).

public class ImportacaoResultado implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int sucessos;
    private final int erros;
    private final List<String> mensagensErro;

    public ImportacaoResultado(int sucessos, int erros, List<String> mensagensErro) {
        this.sucessos = sucessos;
        this.erros = erros;
        this.mensagensErro = mensagensErro;
    }

    public int getSucessos()              { return sucessos; }
    public int getErros()                 { return erros; }
    public List<String> getMensagensErro() { return mensagensErro; }
    public boolean temErros()             { return erros > 0; }
}
