/*
 * ListagemClientesPage — página principal de gestão de clientes.
 *
 * Após a refatoração, esta classe é APENAS orquestração: cria o FiltroState,
 * instancia os panels filhos e cuida da injeção de CSS/JS via PackageResourceReference.
 * Toda lógica de form, modal e CRUD vive nos panels (wicket.page.listagem.*
 * e wicket.page.shared.*).
 *
 * Hierarquia de panels filhos:
 *   ContadoresHeaderPanel    — contadores de topo
 *   BuscaPanel               — barra de busca AJAX
 *   FiltrosPanel             — modal de filtros AJAX
 *   TabelaClientesPanel      — tabela paginada
 *   RodapeAcoesPanel         — botões Adicionar/PDF/Excel/Importar + links de relatório
 *   ExcluirClienteModalPanel — modal de exclusão (Listagem-only)
 *   EditarClienteModalPanel  — modal de edição (compartilhado com Detalhes)
 *   CriarClienteModalPanel   — modal de criação com lista dinâmica de endereços
 *   ImportarExcelModalPanel  — modal de importação via planilha
 *   RelatorioClienteModalPanel — modal de relatório individual (compartilhado)
 */
package com.crudproject.wicket.page;

import com.crudproject.wicket.page.listagem.ContadoresHeaderPanel;
import com.crudproject.wicket.page.listagem.CriarClienteModalPanel;
import com.crudproject.wicket.page.listagem.ExcluirClienteModalPanel;
import com.crudproject.wicket.page.listagem.ImportarExcelModalPanel;
import com.crudproject.wicket.page.listagem.RodapeAcoesPanel;
import com.crudproject.wicket.page.shared.EditarClienteModalPanel;
import com.crudproject.wicket.page.shared.RelatorioClienteModalPanel;
import com.crudproject.wicket.resources.Resources;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.resource.PackageResourceReference;

public class ListagemClientesPage extends WebPage {

    // Path RELATIVO ao pacote da classe Resources (com.crudproject.wicket.resources).
    // Usar uma classe marcadora no mesmo pacote dos .css/.js evita o
    // SecurePackageResourceGuard bloquear o acesso por scope diferente.
    private static final PackageResourceReference CSS_REF =
            new PackageResourceReference(Resources.class, "clientes.css");
    private static final PackageResourceReference JS_REF =
            new PackageResourceReference(Resources.class, "clientes.js");

    public ListagemClientesPage() {
        FiltroState filtros = new FiltroState();

        FeedbackPanel feedbackPagina = new FeedbackPanel("feedback");
        feedbackPagina.setOutputMarkupId(true);
        add(feedbackPagina);

        // Contadores + Tabela + Rodapé (com Links de relatório re-renderizáveis)
        ContadoresHeaderPanel contadores = new ContadoresHeaderPanel("contadoresHeader");
        add(contadores);

        TabelaClientesPanel tabelaPanel = new TabelaClientesPanel("tabelaPanel", filtros);
        tabelaPanel.setOutputMarkupId(true);

        RodapeAcoesPanel rodape = new RodapeAcoesPanel("rodapeAcoes", filtros);

        // Busca/Filtros: precisam re-renderizar a tabela e os links de relatório
        // (que ficam dentro do RodapeAcoesPanel) quando o usuário aplica filtros.
        add(new BuscaPanel("buscaPanel", filtros, tabelaPanel,
                rodape.getLinkRelatorioPdf(), rodape.getLinkRelatorioExcel()));
        add(tabelaPanel);
        add(rodape);

        FiltrosPanel filtrosPanel = new FiltrosPanel("filtrosPanel", filtros, tabelaPanel,
                rodape.getLinkRelatorioPdf(), rodape.getLinkRelatorioExcel());
        filtrosPanel.setRenderBodyOnly(true);
        add(filtrosPanel);

        // Modais — cada um define quais componentes externos re-renderizar via varargs.
        add(new RelatorioClienteModalPanel("modalRelatorio"));

        add(new ExcluirClienteModalPanel("modalExcluirCliente",
                feedbackPagina, tabelaPanel,
                contadores.getTotalClientesLabel(),
                contadores.getTotalAtivosLabel()));

        add(new EditarClienteModalPanel("modalEditarCliente",
                feedbackPagina, tabelaPanel,
                contadores.getTotalAtivosLabel()));

        add(new CriarClienteModalPanel("modalCriarCliente",
                feedbackPagina, tabelaPanel,
                contadores.getTotalClientesLabel(),
                contadores.getTotalAtivosLabel()));

        add(new ImportarExcelModalPanel("modalImportarExcel",
                feedbackPagina, tabelaPanel,
                contadores.getTotalClientesLabel(),
                contadores.getTotalAtivosLabel()));
    }

    // Injeta o CSS/JS compartilhado (servido por PackageResourceReference).
    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(CSS_REF));
        response.render(JavaScriptHeaderItem.forReference(JS_REF));
    }
}
