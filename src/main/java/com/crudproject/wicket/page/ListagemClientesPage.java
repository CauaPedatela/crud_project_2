package com.crudproject.wicket.page;

import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.model.TipoPessoa;
import com.crudproject.service.ClienteService;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;

public class ListagemClientesPage extends WebPage {

    @SpringBean
    private ClienteService clienteService;

    private String termoBusca        = "";
    private String filtroAtivo       = "todos";
    private String filtroTipo        = "todos";
    private String dataCriacaoInicio = "";
    private String dataCriacaoFim    = "";

    private IModel<List<ClienteResponseDTO>> todosClientesModel;
    private IModel<List<ClienteResponseDTO>> clientesFiltradosModel;

    public ListagemClientesPage() {
        criarModelos();
        adicionarContadoresHeader();
        adicionarFormBusca();
        adicionarFormFiltros();
        adicionarTabela();
    }

    private void criarModelos() {
        todosClientesModel = new LoadableDetachableModel<List<ClienteResponseDTO>>() {
            @Override
            protected List<ClienteResponseDTO> load() {
                return clienteService.buscarTodos();
            }
        };

        clientesFiltradosModel = new LoadableDetachableModel<List<ClienteResponseDTO>>() {
            @Override
            protected List<ClienteResponseDTO> load() {
                return clienteService.buscarComFiltros(
                        termoBusca, filtroAtivo, filtroTipo,
                        dataCriacaoInicio, dataCriacaoFim);
            }
        };
    }

    private void adicionarContadoresHeader() {
        add(new Label("totalClientes", new AbstractReadOnlyModel<Integer>() {
            @Override public Integer getObject() {
                return todosClientesModel.getObject().size();
            }
        }));

        add(new Label("totalAtivos", new AbstractReadOnlyModel<Integer>() {
            @Override public Integer getObject() {
                return (int) todosClientesModel.getObject().stream().filter(c -> Boolean.TRUE.equals(c.getAtivo())).count();
            }
        }));
    }

    private void adicionarFormBusca() {
        Form<?> formBusca = new Form<Void>("formBusca");
        formBusca.add(new TextField<>("campoBusca",
                PropertyModel.<String>of(this, "termoBusca")));
        add(formBusca);
    }

    private void adicionarFormFiltros() {
        Form<?> formFiltros = new Form<Void>("formFiltros");

        RadioGroup<String> grupoAtivo = new RadioGroup<>("grupoFiltroAtivo",
                PropertyModel.<String>of(this, "filtroAtivo"));
        grupoAtivo.add(new Radio<>("radioAtivoTodos",   Model.of("todos")));
        grupoAtivo.add(new Radio<>("radioAtivoAtivo",   Model.of("ativo")));
        grupoAtivo.add(new Radio<>("radioAtivoInativo", Model.of("inativo")));
        formFiltros.add(grupoAtivo);

        RadioGroup<String> grupoTipo = new RadioGroup<>("grupoFiltroTipo",
                PropertyModel.<String>of(this, "filtroTipo"));
        grupoTipo.add(new Radio<>("radioTipoTodos", Model.of("todos")));
        grupoTipo.add(new Radio<>("radioTipoPF",    Model.of("PF")));
        grupoTipo.add(new Radio<>("radioTipoPJ",    Model.of("PJ")));
        formFiltros.add(grupoTipo);

        TextField<String> campoDataInicio = new TextField<>("dataCriacaoInicio",
                PropertyModel.<String>of(this, "dataCriacaoInicio"));
        campoDataInicio.add(AttributeModifier.replace("type", "date"));
        formFiltros.add(campoDataInicio);

        TextField<String> campoDataFim = new TextField<>("dataCriacaoFim",
                PropertyModel.<String>of(this, "dataCriacaoFim"));
        campoDataFim.add(AttributeModifier.replace("type", "date"));
        formFiltros.add(campoDataFim);

        formFiltros.add(new Button("btnLimparFiltros") {
            @Override
            public void onSubmit() {
                filtroAtivo       = "todos";
                filtroTipo        = "todos";
                dataCriacaoInicio = "";
                dataCriacaoFim    = "";
            }
        }.setDefaultFormProcessing(false));

        add(formFiltros);
    }

    private void adicionarTabela() {
        add(new Label("contadorPagina", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
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
