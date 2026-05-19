/*
 * BuscaPanel — renderiza a barra de pesquisa por nome, CPF/CNPJ ou e-mail.
 * Ao clicar em "Buscar":
 *   - o termo digitado é gravado em FiltroState (via PropertyModel),
 *   - o AjaxButton chama target.add(tabelaParaAtualizar) — só o painel da tabela
 *     é re-renderizado, sem recarregar a página inteira.
 */
package com.crudproject.wicket.page;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class BuscaPanel extends Panel {

    public BuscaPanel(String id, FiltroState filtros, TabelaClientesPanel tabelaParaAtualizar) {
        super(id);

        Form<?> formBusca = new Form<Void>("formBusca");
        formBusca.add(new TextField<>("campoBusca", PropertyModel.<String>of(filtros, "termoBusca")));

        formBusca.add(new AjaxButton("btnBuscar", formBusca) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(tabelaParaAtualizar);
            }
        });

        add(formBusca);
    }
}
