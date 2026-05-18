/*
 * ListagemClientesPage — página principal do sistema.
 * Orquestra os 3 panels (Busca, Tabela, Filtros) e exibe contadores no header.
 * Apenas visualização — sem editar, excluir ou criar.
 */
package com.crudproject.wicket.page;

import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.service.ClienteService;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;

public class ListagemClientesPage extends WebPage {

    @SpringBean
    private ClienteService clienteService;

    private FiltroState filtros = new FiltroState();

    public ListagemClientesPage() {
        adicionarContadoresHeader();
        adicionarPanels();
    }

    // Contadores do topo direito — sempre sobre o total geral, ignorando filtros.
    private void adicionarContadoresHeader() {
        IModel<List<ClienteResponseDTO>> todosClientesModel =
                new LoadableDetachableModel<List<ClienteResponseDTO>>() {
                    @Override
                    protected List<ClienteResponseDTO> load() {
                        return clienteService.buscarTodos();
                    }
                };

        add(new Label("totalClientes", new AbstractReadOnlyModel<Integer>() {
            @Override
            public Integer getObject() {
                return todosClientesModel.getObject().size();
            }
        }));

        add(new Label("totalAtivos", new AbstractReadOnlyModel<Integer>() {
            @Override
            public Integer getObject() {
                return (int) todosClientesModel.getObject().stream()
                        .filter(c -> Boolean.TRUE.equals(c.getAtivo()))
                        .count();
            }
        }));
    }

    private void adicionarPanels() {
        add(new BuscaPanel("buscaPanel", filtros));
        add(new TabelaClientesPanel("tabelaPanel", filtros));

        FiltrosPanel filtrosPanel = new FiltrosPanel("filtrosPanel", filtros);
        filtrosPanel.setRenderBodyOnly(true);
        add(filtrosPanel);
    }
}
