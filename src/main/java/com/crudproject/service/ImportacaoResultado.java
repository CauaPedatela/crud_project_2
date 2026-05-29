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
    // Total de linhas de dados encontradas na planilha (exclui cabeçalho e linhas vazias).
    // Permite à UI exibir "0 de N clientes importados" quando a operação é cancelada.
    private final int totalLinhasEncontradas;

    public ImportacaoResultado(int sucessos, int erros, List<String> mensagensErro, int totalLinhasEncontradas) {
        this.sucessos = sucessos;
        this.erros = erros;
        this.mensagensErro = mensagensErro;
        this.totalLinhasEncontradas = totalLinhasEncontradas;
    }

    public int getSucessos()               { return sucessos; }
    public int getErros()                  { return erros; }
    public List<String> getMensagensErro() { return mensagensErro; }
    public int getTotalLinhasEncontradas() { return totalLinhasEncontradas; }
    public boolean temErros()              { return erros > 0; }
}
