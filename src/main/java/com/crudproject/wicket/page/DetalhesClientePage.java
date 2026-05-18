/*
 * DetalhesClientePage — exibe os dados de um cliente e seus endereços.
 * Apenas visualização — sem editar, excluir ou criar.
 *
 * Conceitos Wicket usados:
 *   - PageParameters: passa o id do cliente pela URL (ex: /detalhes?id=1)
 *   - Label: exibe um valor Java no HTML via wicket:id
 *   - WebMarkupContainer: bloco controlado pelo Java (atributos, visibilidade)
 *   - BookmarkablePageLink: link para outra página Wicket
 *   - ListView: itera uma lista renderizando um card por item
 *   - AttributeAppender / AttributeModifier: manipula atributos HTML
 */
package com.crudproject.wicket.page;

import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.dto.endereco.EnderecoResponseDTO;
import com.crudproject.model.TipoEndereco;
import com.crudproject.model.TipoPessoa;
import com.crudproject.service.ClienteService;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.time.format.DateTimeFormatter;

public class DetalhesClientePage extends WebPage {

    @SpringBean
    private ClienteService clienteService;

    private static final DateTimeFormatter FMT_DATA     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_CADASTRO = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    public DetalhesClientePage(PageParameters params) {
        Long clienteId = params.get("id").toLong();
        ClienteResponseDTO cliente = clienteService.buscarPorId(clienteId);

        boolean isPF    = cliente.getTipoPessoa() == TipoPessoa.FISICA;
        boolean isAtivo = Boolean.TRUE.equals(cliente.getAtivo());

        // ----- Links de navegação -----
        add(new BookmarkablePageLink<>("linkNavClientes", ListagemClientesPage.class));
        add(new BookmarkablePageLink<>("linkBreadcrumb",  ListagemClientesPage.class));
        add(new BookmarkablePageLink<>("linkVoltar",      ListagemClientesPage.class));

        // ----- Cabeçalho do card: avatar, nome, badges -----
        add(new Label("avatarIniciais", gerarIniciais(cliente.getNome())));
        add(new Label("nomeCliente",    cliente.getNome()));

        Label badgeTipo = new Label("badgeTipo", isPF ? "PF" : "PJ");
        badgeTipo.add(new AttributeAppender("class", Model.of(
                isPF ? " text-bg-info-subtle text-info-emphasis"
                     : " text-bg-warning-subtle text-warning-emphasis"), " "));
        add(badgeTipo);

        Label badgeAtivo = new Label("badgeAtivo", isAtivo ? "Ativo" : "Inativo");
        badgeAtivo.add(new AttributeAppender("class", Model.of(
                isAtivo ? " text-bg-success-subtle text-success-emphasis"
                        : " text-bg-danger-subtle text-danger-emphasis"), " "));
        add(badgeAtivo);

        String dataCadastro = cliente.getDataCadastro() != null
                ? cliente.getDataCadastro().format(FMT_CADASTRO) : "—";
        add(new Label("infoCliente", "Cliente #" + cliente.getId() + " · cadastrado em " + dataCadastro));

        // ----- Campos de dados (bloco unificado PF/PJ) -----
        add(new Label("labelNome",          isPF ? "Nome"               : "Razão Social"));
        add(new Label("valorNome",          nvl(cliente.getNome())));
        add(new Label("valorEmail",         nvl(cliente.getEmail())));
        add(new Label("valorAtivo",         isAtivo ? "Sim" : "Não"));
        add(new Label("labelDocumento",     isPF ? "CPF"                : "CNPJ"));
        add(new Label("valorDocumento",     nvl(cliente.getCpfCnpj())));
        add(new Label("labelIdentificacao", isPF ? "RG"                 : "Inscrição Estadual"));
        add(new Label("valorIdentificacao", nvl(cliente.getRgInscricaoEstadual())));
        add(new Label("labelData",          isPF ? "Data de Nascimento" : "Data de Criação"));
        add(new Label("valorData",          cliente.getDataNascimento() != null
                ? cliente.getDataNascimento().format(FMT_DATA) : "—"));
        add(new Label("valorTelefone",      nvl(cliente.getTelefone())));

        // ----- Links de relatório (href com o id do cliente) -----
        WebMarkupContainer linkPdf = new WebMarkupContainer("linkRelatorioPdf");
        linkPdf.add(AttributeModifier.replace("href",
                "/api/relatorios/cliente/detalhes/pdf?id=" + clienteId));
        add(linkPdf);

        WebMarkupContainer linkExcel = new WebMarkupContainer("linkRelatorioExcel");
        linkExcel.add(AttributeModifier.replace("href",
                "/api/relatorios/cliente/detalhes/excel?id=" + clienteId));
        add(linkExcel);

        // ----- Lista de endereços (apenas visualização) -----
        add(new ListView<EnderecoResponseDTO>("listaEnderecos", cliente.getEnderecos()) {
            @Override
            protected void populateItem(ListItem<EnderecoResponseDTO> item) {
                EnderecoResponseDTO end = item.getModelObject();
                boolean isPrincipal = Boolean.TRUE.equals(end.getPrincipal());

                String tipoTexto = end.getTipo() != null ? end.getTipo().name() : "—";
                Label tipoLabel = new Label("endTipo", tipoTexto);
                tipoLabel.add(new AttributeAppender("class", Model.of(
                        end.getTipo() == TipoEndereco.RESIDENCIAL
                                ? " text-bg-success-subtle text-success-emphasis"
                                : " text-bg-warning-subtle text-warning-emphasis"), " "));
                item.add(tipoLabel);

                Label principal = new Label("endPrincipal", isPrincipal ? "Principal" : "Secundário");
                principal.add(new AttributeAppender("class", Model.of(
                        isPrincipal ? " text-bg-primary-subtle text-primary-emphasis"
                                    : " text-bg-secondary-subtle text-secondary-emphasis"), " "));
                item.add(principal);

                item.add(new Label("endLogradouro",      nvl(end.getLogradouro())));
                item.add(new Label("endNumeroEndereco",  nvl(end.getNumero())));
                item.add(new Label("endComplemento",     nvl(end.getComplemento())));
                item.add(new Label("endBairro",          nvl(end.getBairro())));
                item.add(new Label("endCidade",          nvl(end.getCidade())));
                item.add(new Label("endEstado",          nvl(end.getEstado())));
                item.add(new Label("endCep",             nvl(end.getCep())));
                item.add(new Label("endPais",            nvl(end.getPais())));
            }
        });
    }

    private String gerarIniciais(String nome) {
        if (nome == null || nome.isBlank()) return "?";
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        return ("" + partes[0].charAt(0) + partes[partes.length - 1].charAt(0)).toUpperCase();
    }

    private String nvl(String v) {
        return (v != null && !v.isBlank()) ? v : "—";
    }
}
