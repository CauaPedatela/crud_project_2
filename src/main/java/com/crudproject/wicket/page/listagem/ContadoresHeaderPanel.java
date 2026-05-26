/*
 * ContadoresHeaderPanel — renderiza os contadores "Total de clientes" e
 * "Total de ativos" no cabeçalho da Listagem. Faz uma única leitura ao
 * ClienteService por requisição (AbstractReadOnlyModel) e expõe os dois
 * Labels via getters para que panels que mutam clientes (criar, editar,
 * excluir, importar) re-renderizem os contadores via target.add(...).
 */
package com.crudproject.wicket.page.listagem;

import com.crudproject.service.ClienteService;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class ContadoresHeaderPanel extends Panel {

    @SpringBean
    private ClienteService clienteService;

    private final Label totalClientesLabel;
    private final Label totalAtivosLabel;

    public ContadoresHeaderPanel(String id) {
        super(id);

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

    public Label getTotalClientesLabel() { return totalClientesLabel; }
    public Label getTotalAtivosLabel()   { return totalAtivosLabel; }
}
