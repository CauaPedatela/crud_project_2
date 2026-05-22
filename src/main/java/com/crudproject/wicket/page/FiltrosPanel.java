package com.crudproject.wicket.page;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

public class FiltrosPanel extends Panel {

    private final Form<?> formFiltros;
    private AbstractDefaultAjaxBehavior refreshBehavior;

    public FiltrosPanel(String id, FiltroState filtros, TabelaClientesPanel tabelaParaAtualizar,
                        Component... componentesParaAtualizar) {
        super(id);

        formFiltros = new Form<Void>("formFiltros");
        formFiltros.setOutputMarkupId(true);

        RadioGroup<String> grupoAtivo = new RadioGroup<>("grupoFiltroAtivo", PropertyModel.<String>of(filtros, "filtroAtivo"));
        grupoAtivo.add(new Radio<>("radioAtivoTodos",   Model.of("todos")));
        grupoAtivo.add(new Radio<>("radioAtivoAtivo",   Model.of("ativo")));
        grupoAtivo.add(new Radio<>("radioAtivoInativo", Model.of("inativo")));
        formFiltros.add(grupoAtivo);

        RadioGroup<String> grupoTipo = new RadioGroup<>("grupoFiltroTipo", PropertyModel.<String>of(filtros, "filtroTipo"));
        grupoTipo.add(new Radio<>("radioTipoTodos", Model.of("todos")));
        grupoTipo.add(new Radio<>("radioTipoPF",    Model.of("PF")));
        grupoTipo.add(new Radio<>("radioTipoPJ",    Model.of("PJ")));
        formFiltros.add(grupoTipo);

        TextField<String> campoDataInicio = new TextField<>("dataCriacaoInicio", PropertyModel.<String>of(filtros, "dataCriacaoInicio"));
        campoDataInicio.add(AttributeModifier.replace("type", "date"));
        formFiltros.add(campoDataInicio);

        TextField<String> campoDataFim = new TextField<>("dataCriacaoFim", PropertyModel.<String>of(filtros, "dataCriacaoFim"));
        campoDataFim.add(AttributeModifier.replace("type", "date"));
        formFiltros.add(campoDataFim);

        // Limpar: reseta os filtros, re-renderiza form + tabela e fecha o modal.
        formFiltros.add(new AjaxButton("btnLimparFiltros", formFiltros) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                form.clearInput(); // <-- Limpa a memória de input do Wicket
                filtros.setFiltroAtivo("todos");
                filtros.setFiltroTipo("todos");
                filtros.setDataCriacaoInicio("");
                filtros.setDataCriacaoFim("");
                target.add(formFiltros, tabelaParaAtualizar);
                for (Component c : componentesParaAtualizar) {
                    target.add(c);
                }
                target.appendJavaScript(
                        "var m = bootstrap.Modal.getInstance(document.getElementById('modalFiltros'));" +
                                "if (m) m.hide();");
            }
        }.setDefaultFormProcessing(false));

        // Aplicar: re-renderiza form + tabela e fecha o modal via JS.
        formFiltros.add(new AjaxButton("btnAplicarFiltros", formFiltros) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(formFiltros, tabelaParaAtualizar);
                for (Component c : componentesParaAtualizar) {
                    target.add(c);
                }
                target.appendJavaScript(
                        "var m = bootstrap.Modal.getInstance(document.getElementById('modalFiltros'));" +
                                "if (m) m.hide();");
            }
        });

        // Behavior invisível: o listener show.bs.modal chama esta URL para
        // re-renderizar o form com o FiltroState atual sempre que o modal abre.
        refreshBehavior = new AbstractDefaultAjaxBehavior() {
            @Override
            protected void respond(AjaxRequestTarget target) {
                formFiltros.clearInput(); // <-- Força os campos a puxarem o valor atualizado do backend
                target.add(formFiltros);
            }
        };
        formFiltros.add(refreshBehavior);

        add(formFiltros);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        // Registra o listener uma vez (DOMContentLoaded). A cada abertura do modal,
        // um AJAX call silencioso ao Wicket re-renderiza o form com o estado real.
        response.render(OnDomReadyHeaderItem.forScript(
                "document.getElementById('modalFiltros').addEventListener('show.bs.modal', function() {" +
                        "  Wicket.Ajax.ajax({u:'" + refreshBehavior.getCallbackUrl() + "'});" +
                        "});"
        ));
    }
}