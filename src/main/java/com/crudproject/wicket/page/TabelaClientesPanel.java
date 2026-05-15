/*
 * TabelaClientesPanel — exibe a tabela de clientes filtrada e paginada.
 * Lê o FiltroState para consultar apenas os clientes que correspondem à busca/filtros.
 * A tabela é atualizada a cada submit do formulário de busca ou de filtros.
 */
package com.crudproject.wicket.page;

import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.model.TipoPessoa;
import com.crudproject.service.ClienteService;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;

public class TabelaClientesPanel extends Panel {

    @SpringBean
    private ClienteService clienteService;

    public TabelaClientesPanel(String id, FiltroState filtros) {
        super(id);

        IModel<List<ClienteResponseDTO>> clientesFiltradosModel =
                new LoadableDetachableModel<List<ClienteResponseDTO>>() {
                    @Override
                    protected List<ClienteResponseDTO> load() {
                        return clienteService.buscarComFiltros(
                                filtros.getTermoBusca(),
                                filtros.getFiltroAtivo(),
                                filtros.getFiltroTipo(),
                                filtros.getDataCriacaoInicio(),
                                filtros.getDataCriacaoFim());
                    }
                };

        add(new Label("contadorPagina", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return clientesFiltradosModel.getObject().size() + " clientes encontrados";
            }
        }));

        PageableListView<ClienteResponseDTO> listView =
                new PageableListView<ClienteResponseDTO>("listaClientes", clientesFiltradosModel, 5) {
                    @Override
                    protected void populateItem(ListItem<ClienteResponseDTO> item) {
                        ClienteResponseDTO cliente = item.getModelObject();

                        item.add(new Label("numero",    item.getIndex() + 1));
                        item.add(new Label("nome",      cliente.getNome()));

                        boolean isPF = cliente.getTipoPessoa() == TipoPessoa.FISICA;
                        Label tipoLabel = new Label("tipo", isPF ? "PF" : "PJ");
                        tipoLabel.add(new AttributeAppender("class", Model.of(
                                isPF ? " text-bg-success" : " text-bg-primary"), " "));
                        item.add(tipoLabel);

                        item.add(new Label("documento", cliente.getCpfCnpj()));
                        item.add(new Label("email",     cliente.getEmail()));

                        boolean isAtivo = Boolean.TRUE.equals(cliente.getAtivo());
                        Label ativoLabel = new Label("ativo", isAtivo ? "Sim" : "Não");
                        ativoLabel.add(new AttributeAppender("class", Model.of(
                                isAtivo ? " text-bg-success" : " text-bg-danger"), " "));
                        item.add(ativoLabel);
                    }
                };

        add(listView);
        add(new PagingNavigator("paginacao", listView));
    }
}
