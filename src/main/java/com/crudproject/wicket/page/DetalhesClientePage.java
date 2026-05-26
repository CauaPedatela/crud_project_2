/*
 * DetalhesClientePage — exibe os dados de um cliente e seus endereços, com
 * CRUD completo de endereços.
 *
 * Após a refatoração, esta classe é APENAS orquestração: carrega o cliente
 * via LoadableDetachableModel, instancia os panels filhos e configura os
 * BookmarkablePageLink de navegação. Toda lógica de form vive nos panels
 * (wicket.page.detalhes.* e wicket.page.shared.*).
 *
 * Hierarquia de panels filhos:
 *   CardClientePanel              — card de dados do cliente
 *   EnderecosListaPanel           — lista de endereços com botões editar/excluir
 *   EditarClienteModalPanel       — modal de edição (compartilhado com Listagem)
 *   ExcluirEnderecoModalPanel     — modal de confirmação de exclusão
 *   EditarEnderecoModalPanel      — modal de edição de endereço
 *   AdicionarEnderecoModalPanel   — modal de criação de endereço (com ViaCEP)
 *   RelatorioClienteModalPanel    — modal de relatório individual (compartilhado)
 */
package com.crudproject.wicket.page;

import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.service.ClienteService;
import com.crudproject.wicket.page.detalhes.AdicionarEnderecoModalPanel;
import com.crudproject.wicket.page.detalhes.CardClientePanel;
import com.crudproject.wicket.page.detalhes.EditarEnderecoModalPanel;
import com.crudproject.wicket.page.detalhes.EnderecosListaPanel;
import com.crudproject.wicket.page.detalhes.ExcluirEnderecoModalPanel;
import com.crudproject.wicket.page.shared.EditarClienteModalPanel;
import com.crudproject.wicket.page.shared.RelatorioClienteModalPanel;
import com.crudproject.wicket.resources.Resources;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class DetalhesClientePage extends WebPage {

    // Path RELATIVO à classe marcadora Resources, no mesmo pacote dos .css/.js.
    // Evita problemas de scope com o SecurePackageResourceGuard do Wicket.
    private static final PackageResourceReference CSS_REF =
            new PackageResourceReference(Resources.class, "clientes.css");
    private static final PackageResourceReference JS_REF =
            new PackageResourceReference(Resources.class, "clientes.js");

    @SpringBean
    private ClienteService clienteService;

    private LoadableDetachableModel<ClienteResponseDTO> clienteModel;

    public DetalhesClientePage(PageParameters params) {
        final Long clienteId = params.get("id").toLong();

        clienteModel = new LoadableDetachableModel<ClienteResponseDTO>() {
            @Override
            protected ClienteResponseDTO load() {
                return clienteService.buscarPorId(clienteId);
            }
        };

        FeedbackPanel feedbackPagina = new FeedbackPanel("feedback");
        feedbackPagina.setOutputMarkupId(true);
        add(feedbackPagina);

        add(new BookmarkablePageLink<>("linkNavClientes", ListagemClientesPage.class));
        add(new BookmarkablePageLink<>("linkBreadcrumb",  ListagemClientesPage.class));
        add(new BookmarkablePageLink<>("linkVoltar",      ListagemClientesPage.class));

        // Botão "Editar" do topo — data-* gerados dinamicamente do clienteModel
        WebMarkupContainer btnEditarCliente = new WebMarkupContainer("btnEditarCliente");
        btnEditarCliente.setOutputMarkupId(true);
        btnEditarCliente.add(new Behavior() {
            @Override
            public void onComponentTag(Component component, ComponentTag tag) {
                ClienteResponseDTO c = clienteModel.getObject();
                tag.put("onclick",       "abrirModalEdicao(this)");
                tag.put("data-id",       String.valueOf(c.getId()));
                tag.put("data-nome",     safe(c.getNome()));
                tag.put("data-email",    safe(c.getEmail()));
                tag.put("data-ativo",    String.valueOf(Boolean.TRUE.equals(c.getAtivo())));
                tag.put("data-tipo",     c.getTipoPessoa() != null ? c.getTipoPessoa().name() : "FISICA");
                tag.put("data-cpf-cnpj", safe(c.getCpfCnpj()));
                tag.put("data-rg-ie",    safe(c.getRgInscricaoEstadual()));
            }
        });
        add(btnEditarCliente);

        // Botão "Relatório" do topo — usa o modal compartilhado (RelatorioClienteModalPanel),
        // disparando abrirModalRelatorio(id) que preenche os hrefs e abre o modal.
        WebMarkupContainer btnRelatorio = new WebMarkupContainer("btnRelatorio");
        btnRelatorio.add(new Behavior() {
            @Override
            public void onComponentTag(Component component, ComponentTag tag) {
                tag.put("onclick", "abrirModalRelatorio(" + clienteId + ")");
            }
        });
        add(btnRelatorio);

        // Panels visuais + modais. O cardCliente e a listaEnderecos são re-renderizados
        // pelos modais via varargs (constructor injection — mesmo padrão dos BuscaPanel/FiltrosPanel).
        CardClientePanel cardCliente = new CardClientePanel("cardCliente", clienteModel);
        add(cardCliente);

        EnderecosListaPanel enderecosLista = new EnderecosListaPanel("enderecosLista", clienteModel);
        add(enderecosLista);

        add(new RelatorioClienteModalPanel("modalRelatorio"));

        add(new EditarClienteModalPanel("modalEditarCliente",
                feedbackPagina, cardCliente, btnEditarCliente));

        add(new ExcluirEnderecoModalPanel("modalExcluirEndereco",
                clienteModel, feedbackPagina, enderecosLista));

        add(new EditarEnderecoModalPanel("modalEditarEndereco",
                clienteModel, feedbackPagina, enderecosLista));

        add(new AdicionarEnderecoModalPanel("modalAdicionarEndereco",
                clienteModel, feedbackPagina, enderecosLista));
    }

    @Override
    protected void onDetach() {
        if (clienteModel != null) clienteModel.detach();
        super.onDetach();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(CSS_REF));
        response.render(JavaScriptHeaderItem.forReference(JS_REF));
    }

    private static String safe(String v) {
        return v != null ? v : "";
    }
}
