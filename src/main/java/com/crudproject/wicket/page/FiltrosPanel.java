/*
 * FiltrosPanel — renderiza o modal de filtros (status, tipo de pessoa, datas).
 * Quando o usuário clica em "Aplicar", os valores são gravados em FiltroState
 * e a tabela reflete os novos critérios. O botão "Limpar" reseta tudo para "todos".
 */
package com.crudproject.wicket.page;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

public class FiltrosPanel extends Panel {

    public FiltrosPanel(String id, FiltroState filtros) {
        super(id);

        Form<?> formFiltros = new Form<Void>("formFiltros");

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

        formFiltros.add(new Button("btnLimparFiltros") {
            @Override
            public void onSubmit() {
                filtros.setFiltroAtivo("todos");
                filtros.setFiltroTipo("todos");
                filtros.setDataCriacaoInicio("");
                filtros.setDataCriacaoFim("");
            }
        }.setDefaultFormProcessing(false));

        add(formFiltros);
    }
}
