/*
 * ContadoresHeaderPanel — renderiza os contadores "Total de clientes" e
 * "Total de ativos" no cabeçalho da Listagem.
 *
 * Usa clienteRepository.count() e countByAtivo(true) DIRETAMENTE — SQL gera
 * apenas SELECT count(*), sem trazer entidades para a memória do servidor.
 * Os Labels são expostos via getters para que panels que mutam clientes
 * (criar, editar, excluir, importar) os re-renderizem via target.add(...).
 */
package com.crudproject.wicket.page.listagem;

import com.crudproject.repository.ClienteRepository;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class ContadoresHeaderPanel extends Panel {

    // Injetado direto — não passamos pelo Service porque é uma consulta agregada simples,
    // sem regras de negócio. Service só faria delegação cega ao mesmo repository.
    @SpringBean
    private ClienteRepository clienteRepository;

    private final Label totalClientesLabel;
    private final Label totalAtivosLabel;

    public ContadoresHeaderPanel(String id) {
        super(id);

        // Total geral — uma única query: SELECT count(*) FROM tb_cliente
        totalClientesLabel = new Label("totalClientes", new AbstractReadOnlyModel<Long>() {
            @Override
            public Long getObject() {
                return clienteRepository.count();
            }
        });
        totalClientesLabel.setOutputMarkupId(true);
        add(totalClientesLabel);

        // Total de ativos — uma única query: SELECT count(*) FROM tb_cliente WHERE ativo = true
        totalAtivosLabel = new Label("totalAtivos", new AbstractReadOnlyModel<Long>() {
            @Override
            public Long getObject() {
                return clienteRepository.countByAtivo(true);
            }
        });
        totalAtivosLabel.setOutputMarkupId(true);
        add(totalAtivosLabel);
    }

    public Label getTotalClientesLabel() { return totalClientesLabel; }
    public Label getTotalAtivosLabel()   { return totalAtivosLabel; }
}
