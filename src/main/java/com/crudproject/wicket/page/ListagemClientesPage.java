/*
 * ListagemClientesPage — página principal do sistema.
 * Orquestra os 3 panels (Busca, Tabela, Filtros) e exibe contadores no header.
 * Apenas visualização — sem editar, excluir ou criar.
 */
package com.crudproject.wicket.page;

import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.service.ClienteService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;

public class ListagemClientesPage extends WebPage {

    @SpringBean
    private ClienteService clienteService;

    private FiltroState filtros = new FiltroState();

    private FeedbackPanel feedbackPanel;
    private TabelaClientesPanel tabelaPanel;
    public Long idParaExcluir;
    private Label totalClientesLabel;
    private Label totalAtivosLabel;

    public ListagemClientesPage() {
        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        adicionarContadoresHeader();
        adicionarPanels();
        adicionarFormExcluir();
    }

    // Contadores do topo direito — sempre sobre o total geral, ignorando filtros.
    private void adicionarContadoresHeader() {
        totalClientesLabel = new Label("totalClientes", new AbstractReadOnlyModel<Integer>() {
            @Override
            public Integer getObject() {
                return clienteService.buscarTodos().size();
            }
        });
        totalClientesLabel.setOutputMarkupId(true);
        add(totalClientesLabel);

        totalAtivosLabel = new Label("totalAtivos", new AbstractReadOnlyModel<Integer>() {
            @Override
            public Integer getObject() {
                return (int) clienteService.buscarTodos().stream()
                        .filter(c -> Boolean.TRUE.equals(c.getAtivo()))
                        .count();
            }
        });
        totalAtivosLabel.setOutputMarkupId(true);
        add(totalAtivosLabel);
    }

    private void adicionarPanels() {
        // Criamos o tabelaPanel antes para passar a referência aos panels que vão atualizá-lo via AJAX.
        // O setOutputMarkupId(true) garante que ele tenha um id="..." no HTML para o Wicket localizar via JS.
        tabelaPanel = new TabelaClientesPanel("tabelaPanel", filtros);
        tabelaPanel.setOutputMarkupId(true);

        add(new BuscaPanel("buscaPanel", filtros, tabelaPanel));
        add(tabelaPanel);

        FiltrosPanel filtrosPanel = new FiltrosPanel("filtrosPanel", filtros, tabelaPanel);
        filtrosPanel.setRenderBodyOnly(true);
        add(filtrosPanel);
    }

    private void adicionarFormExcluir() {
        Form<Void> form = new Form<>("formExcluirCliente");

        // HiddenField: PropertyModel liga o input ao campo idParaExcluir;
        // setMarkupId("idClienteParaExcluir") força um id HTML fixo pro JS achar.
        HiddenField<Long> hiddenId = new HiddenField<>(
                "hiddenClienteId",
                new PropertyModel<Long>(this, "idParaExcluir"), Long.class);
        hiddenId.setMarkupId("idClienteParaExcluir");
        hiddenId.setOutputMarkupId(true);
        form.add(hiddenId);

        form.add(new AjaxButton("btnConfirmarExclusao", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                if (idParaExcluir == null) return;
                try {
                    clienteService.excluir(idParaExcluir);
                    info("Cliente excluído com sucesso.");
                    target.add(feedbackPanel, tabelaPanel, totalClientesLabel, totalAtivosLabel);   // atualiza msg + tabela
                } catch (Exception ex) {
                    error("Erro ao excluir: " + ex.getMessage());
                    target.add(feedbackPanel);
                }
                // Fecha o modal em qualquer caso (sucesso ou erro)
                target.appendJavaScript(
                        "var m = bootstrap.Modal.getInstance(document.getElementById('modalConfirmarExclusao'));" +
                                "if (m) m.hide();");
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> formClicked) {
                target.add(feedbackPanel);
            }
        });

        add(form);
    }

}
