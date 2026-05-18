/*
 * BuscaPanel — renderiza a barra de pesquisa por nome, CPF/CNPJ ou e-mail.
 * Quando o usuário clica em "Buscar", o termo digitado é gravado em FiltroState
 * e a tabela (TabelaClientesPanel) atualiza automaticamente na re-renderização.
 */
package com.crudproject.wicket.page;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class BuscaPanel extends Panel {

    public BuscaPanel(String id, FiltroState filtros) {
        super(id);

        Form<?> formBusca = new Form<Void>("formBusca");
        formBusca.add(new TextField<>("campoBusca", PropertyModel.<String>of(filtros, "termoBusca")));
        add(formBusca);
    }
}

