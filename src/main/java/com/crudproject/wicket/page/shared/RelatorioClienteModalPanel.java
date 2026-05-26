/*
 * RelatorioClienteModalPanel — modal "Emitir Relatório" usado tanto na linha
 * da Listagem (botão por cliente) quanto na página de Detalhes (botão do topo).
 *
 * O markup é praticamente estático: 2 links (PDF/Excel) cujos hrefs são
 * preenchidos via JS abrirModalRelatorio(id) usando os ids "linkRelatorioPdfLista"
 * e "linkRelatorioExcelLista". Como não há binding Wicket, o Panel só existe
 * para isolar o HTML e facilitar a migração para Angular (vira <app-relatorio-cliente-modal>).
 */
package com.crudproject.wicket.page.shared;

import org.apache.wicket.markup.html.panel.Panel;

public class RelatorioClienteModalPanel extends Panel {

    public RelatorioClienteModalPanel(String id) {
        super(id);
    }
}
