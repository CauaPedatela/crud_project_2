/*
 * TabelaClientesPanel — exibe a tabela de clientes filtrada e paginada.
 * Lê o FiltroState para consultar apenas os clientes que correspondem à busca/filtros.
 */
package com.crudproject.wicket.page;

import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.model.TipoPessoa;
import com.crudproject.service.ClienteService;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;

public class TabelaClientesPanel extends Panel {

    @SpringBean
    private ClienteService clienteService;

    // CONSTRUTOR LIMPO: Não recebe mais o modalRelatorio, simplificando a orquestração
    public TabelaClientesPanel(String id, FiltroState filtros) {
        super(id);

        IModel<List<ClienteResponseDTO>> clientesFiltradosModel = new LoadableDetachableModel<List<ClienteResponseDTO>>() {
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

        PageableListView<ClienteResponseDTO> listView = new PageableListView<ClienteResponseDTO>("listaClientes", clientesFiltradosModel, 5) {
            @Override
            protected void populateItem(ListItem<ClienteResponseDTO> item) {
                ClienteResponseDTO cliente = item.getModelObject();

                item.add(new Label("numero", item.getIndex() + 1));

                PageParameters pp = new PageParameters();
                pp.add("id", cliente.getId());
                BookmarkablePageLink<Void> linkNome = new BookmarkablePageLink<>("linkNome", DetalhesClientePage.class, pp);
                linkNome.add(new Label("nomeTexto", cliente.getNome()));
                item.add(linkNome);

                boolean isPF = cliente.getTipoPessoa() == TipoPessoa.FISICA;
                Label tipoLabel = new Label("tipo", isPF ? "PF" : "PJ");
                tipoLabel.add(new AttributeAppender("class",
                        Model.of(isPF ? " text-bg-success" : " text-bg-primary"), " "));
                item.add(tipoLabel);

                item.add(new Label("documento", cliente.getCpfCnpj()));
                item.add(new Label("email",     cliente.getEmail()));

                boolean isAtivo = Boolean.TRUE.equals(cliente.getAtivo());
                Label ativoLabel = new Label("ativo", isAtivo ? "Sim" : "Não");
                ativoLabel.add(new AttributeAppender("class",
                        Model.of(isAtivo ? " text-bg-success" : " text-bg-danger"), " "));
                item.add(ativoLabel);

                WebMarkupContainer btnEditar = new WebMarkupContainer("btnEditar");
                btnEditar.add(AttributeModifier.replace("onclick",       "abrirModalEdicao(this)"));
                btnEditar.add(AttributeModifier.replace("data-id",       String.valueOf(cliente.getId())));
                btnEditar.add(AttributeModifier.replace("data-nome",     safe(cliente.getNome())));
                btnEditar.add(AttributeModifier.replace("data-email",    safe(cliente.getEmail())));
                btnEditar.add(AttributeModifier.replace("data-ativo",    String.valueOf(Boolean.TRUE.equals(cliente.getAtivo()))));
                btnEditar.add(AttributeModifier.replace("data-tipo",     cliente.getTipoPessoa() != null ? cliente.getTipoPessoa().name() : "FISICA"));
                btnEditar.add(AttributeModifier.replace("data-cpf-cnpj", safe(cliente.getCpfCnpj())));
                btnEditar.add(AttributeModifier.replace("data-rg-ie",    safe(cliente.getRgInscricaoEstadual())));
                item.add(btnEditar);

                WebMarkupContainer btnExcluir = new WebMarkupContainer("btnExcluir");
                btnExcluir.add(AttributeModifier.replace
                        ("onclick" , "abrirModalExclusao(" + cliente.getId() + ")"));
                item.add(btnExcluir);

                // BOTÃO RELATÓRIO CORRIGIDO: Retorna a ser um container simples e leve,
                // delegando para o JS a responsabilidade de passar o ID para o modal.
                WebMarkupContainer btnRelatorio = new WebMarkupContainer("btnRelatorio");
                btnRelatorio.add(AttributeModifier.replace("onclick", "abrirModalRelatorio(this)"));
                btnRelatorio.add(AttributeModifier.replace("data-id", String.valueOf(cliente.getId())));
                item.add(btnRelatorio);
            }
        };

        listView.setOutputMarkupId(true);
        add(listView);

        AjaxPagingNavigator paginacao = new AjaxPagingNavigator("paginacao", listView);
        paginacao.setMarkupId("wicketPaginacao");
        paginacao.setOutputMarkupId(true);
        add(paginacao);
    }

    private static String safe(String v) {
        return v != null ? v : "";
    }
}