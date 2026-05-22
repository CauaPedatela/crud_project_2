/*
 * BuscaPanel — renderiza a barra de pesquisa por nome, CPF/CNPJ ou e-mail.
 * Ao clicar em "Buscar":
 *   - o termo digitado é gravado em FiltroState (via PropertyModel),
 *   - o AjaxButton chama target.add(...) re-renderizando a tabela e os
 *     componentes extras (links de relatório) — sem recarregar a página.
 *
 * Os componentesParaAtualizar (varargs) recebem os links de relatório: eles
 * precisam ser re-renderizados a cada busca para que sua URL aponte sempre
 * para o estado atual da página, refletindo o filtro recém-aplicado.
 */
package com.crudproject.wicket.page;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class BuscaPanel extends Panel {

    public BuscaPanel(String id, FiltroState filtros, TabelaClientesPanel tabelaParaAtualizar,
                      Component... componentesParaAtualizar) {
        super(id);

        Form<?> formBusca = new Form<Void>("formBusca");
        formBusca.add(new TextField<>("campoBusca", PropertyModel.<String>of(filtros, "termoBusca")));

        formBusca.add(new AjaxButton("btnBuscar", formBusca) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(tabelaParaAtualizar);
                for (Component c : componentesParaAtualizar) {
                    target.add(c);
                }
            }
        });

        add(formBusca);
    }
}
