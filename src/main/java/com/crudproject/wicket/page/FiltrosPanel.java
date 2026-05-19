/*
 * FiltrosPanel — renderiza o modal de filtros (status, tipo de pessoa, datas).
 *
 * Botão "Aplicar Filtros" (AjaxButton):
 *   - Wicket grava os valores do form em FiltroState (via PropertyModel),
 *   - target.add(tabelaParaAtualizar) re-renderiza só a tabela,
 *   - target.appendJavaScript(...) fecha o modal sem recarregar a página.
 *
 * Botão "Limpar" (AjaxButton com setDefaultFormProcessing(false)):
 *   - Não grava os campos do form (descarta o input pendente),
 *   - Reseta FiltroState manualmente,
 *   - Re-renderiza form (para os campos mostrarem os valores limpos) + tabela.
 */
package com.crudproject.wicket.page;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

public class FiltrosPanel extends Panel {

    public FiltrosPanel(String id, FiltroState filtros, TabelaClientesPanel tabelaParaAtualizar) {
        super(id);

        Form<?> formFiltros = new Form<Void>("formFiltros");
        // O setOutputMarkupId(true) é necessário porque o botão "Limpar" chama
        // target.add(formFiltros) para reexibir os campos com os valores resetados.
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

        // Limpar: reseta os filtros e re-renderiza form + tabela (modal continua aberto).
        formFiltros.add(new AjaxButton("btnLimparFiltros", formFiltros) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                filtros.setFiltroAtivo("todos");
                filtros.setFiltroTipo("todos");
                filtros.setDataCriacaoInicio("");
                filtros.setDataCriacaoFim("");
                target.add(formFiltros, tabelaParaAtualizar);
            }
        }.setDefaultFormProcessing(false));

        // Aplicar: re-renderiza tabela e fecha o modal via JS.
        formFiltros.add(new AjaxButton("btnAplicarFiltros", formFiltros) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(tabelaParaAtualizar);
                target.appendJavaScript(
                        "var m = bootstrap.Modal.getInstance(document.getElementById('modalFiltros'));" +
                        "if (m) m.hide();");
            }
        });

        add(formFiltros);
    }
}
