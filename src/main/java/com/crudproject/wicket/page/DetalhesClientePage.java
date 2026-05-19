/*
 * DetalhesClientePage — exibe os dados de um cliente e seus endereços, com edição AJAX.
 *
 * Conceitos Wicket usados:
 *   - LoadableDetachableModel: carrega o cliente do banco sob demanda, com cache por requisição
 *   - WebMarkupContainer ("cardCliente"): bloco re-renderizável via target.add(...)
 *   - AbstractReadOnlyModel: cada Label deriva seu valor do clienteModel
 *   - Behavior + onComponentTag: define data-* dinâmicos no botão de editar
 *   - Form + HiddenField + TextField + CheckBox + AjaxButton: padrão de edição modal
 *   - FeedbackPanel: exibe mensagens de sucesso/erro alimentadas por info()/error()
 */
package com.crudproject.wicket.page;

import com.crudproject.dto.cliente.ClienteDTO;
import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.dto.endereco.EnderecoDTO;
import com.crudproject.dto.endereco.EnderecoResponseDTO;
import com.crudproject.model.TipoEndereco;
import com.crudproject.model.TipoPessoa;
import com.crudproject.service.ClienteService;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DetalhesClientePage extends WebPage {

    @SpringBean
    private ClienteService clienteService;

    private static final DateTimeFormatter FMT_DATA     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_CADASTRO = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    // Campos da página
    private Long                                        clienteId;
    private LoadableDetachableModel<ClienteResponseDTO> clienteModel;
    private FeedbackPanel                               feedbackPanel;
    private WebMarkupContainer                          cardCliente;
    private WebMarkupContainer                          btnEditarCliente;

    // Campos do form de edição
    private String  formEmail;
    private String  formTelefone;
    private String  formRgIe;
    private Boolean formAtivo;

    public DetalhesClientePage(PageParameters params) {
        clienteId = params.get("id").toLong();

        // Model único compartilhado por todos os labels do cardCliente.
        // O onDetach() abaixo limpa o cache no fim de cada requisição.
        clienteModel = new LoadableDetachableModel<ClienteResponseDTO>() {
            @Override
            protected ClienteResponseDTO load() {
                return clienteService.buscarPorId(clienteId);
            }
        };

        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        // ----- Links de navegação -----
        add(new BookmarkablePageLink<>("linkNavClientes", ListagemClientesPage.class));
        add(new BookmarkablePageLink<>("linkBreadcrumb",  ListagemClientesPage.class));
        add(new BookmarkablePageLink<>("linkVoltar",      ListagemClientesPage.class));

        // ----- Botão "Editar" (action bar do topo) -----
        // Os data-* são gerados dinamicamente do clienteModel em cada renderização (Behavior).
        // Quando target.add(btnEditarCliente) roda após salvar, os data-* refletem os dados novos.
        btnEditarCliente = new WebMarkupContainer("btnEditarCliente");
        btnEditarCliente.setOutputMarkupId(true);
        btnEditarCliente.add(new Behavior() {
            @Override
            public void onComponentTag(Component component, ComponentTag tag) {
                ClienteResponseDTO c = clienteModel.getObject();
                tag.put("onclick",       "abrirModalEdicao(this)");
                tag.put("data-id",       String.valueOf(c.getId()));
                tag.put("data-nome",     safe(c.getNome()));
                tag.put("data-email",    safe(c.getEmail()));
                tag.put("data-telefone", safe(c.getTelefone()));
                tag.put("data-ativo",    String.valueOf(Boolean.TRUE.equals(c.getAtivo())));
                tag.put("data-tipo",     c.getTipoPessoa() != null ? c.getTipoPessoa().name() : "FISICA");
                tag.put("data-cpf-cnpj", safe(c.getCpfCnpj()));
                tag.put("data-rg-ie",    safe(c.getRgInscricaoEstadual()));
            }
        });
        add(btnEditarCliente);

        // ----- Links de relatório -----
        WebMarkupContainer linkPdf = new WebMarkupContainer("linkRelatorioPdf");
        linkPdf.add(AttributeModifier.replace("href",
                "/api/relatorios/cliente/detalhes/pdf?id=" + clienteId));
        add(linkPdf);

        WebMarkupContainer linkExcel = new WebMarkupContainer("linkRelatorioExcel");
        linkExcel.add(AttributeModifier.replace("href",
                "/api/relatorios/cliente/detalhes/excel?id=" + clienteId));
        add(linkExcel);

        // ----- Card do cliente (re-renderizável via AJAX) -----
        adicionarCardCliente();

        // ----- Lista de endereços (apenas visualização) -----
        add(new ListView<EnderecoResponseDTO>("listaEnderecos",
                new AbstractReadOnlyModel<List<EnderecoResponseDTO>>() {
                    @Override
                    public List<EnderecoResponseDTO> getObject() {
                        return clienteModel.getObject().getEnderecos();
                    }
                }) {
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

        // ----- Form de edição -----
        adicionarFormEditar();
    }

    // cardCliente contém todos os labels de dados do cliente, ligados ao clienteModel.
    // Quando target.add(cardCliente) roda após salvar, todos os labels re-leem do model
    // (que recarrega do banco porque o detach() limpa o cache).
    private void adicionarCardCliente() {
        cardCliente = new WebMarkupContainer("cardCliente");
        cardCliente.setOutputMarkupId(true);

        cardCliente.add(new Label("avatarIniciais", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return gerarIniciais(clienteModel.getObject().getNome());
            }
        }));

        cardCliente.add(new Label("nomeCliente", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return clienteModel.getObject().getNome();
            }
        }));

        // badgeTipo: tipoPessoa é imutável, mas mantemos com model por consistência
        Label badgeTipo = new Label("badgeTipo", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return clienteModel.getObject().getTipoPessoa() == TipoPessoa.FISICA ? "PF" : "PJ";
            }
        });
        badgeTipo.add(new AttributeAppender("class", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return clienteModel.getObject().getTipoPessoa() == TipoPessoa.FISICA
                        ? " text-bg-info-subtle text-info-emphasis"
                        : " text-bg-warning-subtle text-warning-emphasis";
            }
        }, " "));
        cardCliente.add(badgeTipo);

        // badgeAtivo: pode mudar via edição
        Label badgeAtivo = new Label("badgeAtivo", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return Boolean.TRUE.equals(clienteModel.getObject().getAtivo()) ? "Ativo" : "Inativo";
            }
        });
        badgeAtivo.add(new AttributeAppender("class", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return Boolean.TRUE.equals(clienteModel.getObject().getAtivo())
                        ? " text-bg-success-subtle text-success-emphasis"
                        : " text-bg-danger-subtle text-danger-emphasis";
            }
        }, " "));
        cardCliente.add(badgeAtivo);

        cardCliente.add(new Label("infoCliente", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                ClienteResponseDTO c = clienteModel.getObject();
                String dt = c.getDataCadastro() != null ? c.getDataCadastro().format(FMT_CADASTRO) : "—";
                return "Cliente #" + c.getId() + " · cadastrado em " + dt;
            }
        }));

        cardCliente.add(new Label("labelNome", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return clienteModel.getObject().getTipoPessoa() == TipoPessoa.FISICA ? "Nome" : "Razão Social";
            }
        }));
        cardCliente.add(new Label("valorNome", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() { return nvl(clienteModel.getObject().getNome()); }
        }));
        cardCliente.add(new Label("valorEmail", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() { return nvl(clienteModel.getObject().getEmail()); }
        }));
        cardCliente.add(new Label("valorAtivo", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return Boolean.TRUE.equals(clienteModel.getObject().getAtivo()) ? "Sim" : "Não";
            }
        }));
        cardCliente.add(new Label("labelDocumento", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return clienteModel.getObject().getTipoPessoa() == TipoPessoa.FISICA ? "CPF" : "CNPJ";
            }
        }));
        cardCliente.add(new Label("valorDocumento", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() { return nvl(clienteModel.getObject().getCpfCnpj()); }
        }));
        cardCliente.add(new Label("labelIdentificacao", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return clienteModel.getObject().getTipoPessoa() == TipoPessoa.FISICA ? "RG" : "Inscrição Estadual";
            }
        }));
        cardCliente.add(new Label("valorIdentificacao", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() { return nvl(clienteModel.getObject().getRgInscricaoEstadual()); }
        }));
        cardCliente.add(new Label("labelData", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return clienteModel.getObject().getTipoPessoa() == TipoPessoa.FISICA
                        ? "Data de Nascimento" : "Data de Criação";
            }
        }));
        cardCliente.add(new Label("valorData", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                java.time.LocalDate d = clienteModel.getObject().getDataNascimento();
                return d != null ? d.format(FMT_DATA) : "—";
            }
        }));
        cardCliente.add(new Label("valorTelefone", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() { return nvl(clienteModel.getObject().getTelefone()); }
        }));

        add(cardCliente);
    }

    // Form de edição — espelha o da listagem (mesmo modal, mesmos campos editáveis).
    private void adicionarFormEditar() {
        Form<Void> form = new Form<>("formEditarCliente");

        HiddenField<Long> hiddenId = new HiddenField<>("hiddenEditClienteId",
                new PropertyModel<Long>(this, "clienteId"), Long.class);
        hiddenId.setMarkupId("editClienteId");
        hiddenId.setOutputMarkupId(true);
        form.add(hiddenId);

        TextField<String> tfEmail = new TextField<>("formEmail",
                new PropertyModel<>(this, "formEmail"));
        tfEmail.setMarkupId("editEmail");
        tfEmail.setOutputMarkupId(true);
        form.add(tfEmail);

        TextField<String> tfTelefone = new TextField<>("formTelefone",
                new PropertyModel<>(this, "formTelefone"));
        tfTelefone.setMarkupId("editTelefone");
        tfTelefone.setOutputMarkupId(true);
        form.add(tfTelefone);

        TextField<String> tfRgIe = new TextField<>("formRgIe",
                new PropertyModel<>(this, "formRgIe"));
        tfRgIe.setMarkupId("editRgIe");
        tfRgIe.setOutputMarkupId(true);
        form.add(tfRgIe);

        CheckBox cbAtivo = new CheckBox("formAtivo",
                new PropertyModel<>(this, "formAtivo"));
        cbAtivo.setMarkupId("editAtivo");
        cbAtivo.setOutputMarkupId(true);
        form.add(cbAtivo);

        form.add(new AjaxButton("btnSalvarEdicao", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                try {
                    ClienteResponseDTO current = clienteService.buscarPorId(clienteId);

                    ClienteDTO dto = new ClienteDTO();
                    dto.setTipoPessoa(current.getTipoPessoa());
                    dto.setNome(current.getNome());                     // imutável
                    dto.setCpfCnpj(current.getCpfCnpj());                 // imutável
                    dto.setDataNascimento(current.getDataNascimento());   // imutável
                    dto.setEmail(formEmail);
                    dto.setTelefone(formTelefone);
                    dto.setAtivo(formAtivo);
                    // IE: para PJ usa o form; para PF preserva o valor atual (campo fica oculto pelo JS)
                    dto.setRgInscricaoEstadual(
                            current.getTipoPessoa() == TipoPessoa.JURIDICA
                                    ? formRgIe
                                    : current.getRgInscricaoEstadual());
                    dto.setEnderecos(toEnderecosDTOs(current.getEnderecos()));

                    clienteService.atualizar(clienteId, dto);
                    info("Cliente atualizado com sucesso.");
                    // cardCliente: re-renderiza labels com valores novos do banco
                    // btnEditarCliente: re-renderiza data-* com valores novos
                    target.add(feedbackPanel, cardCliente, btnEditarCliente);
                } catch (Exception ex) {
                    error("Erro ao atualizar: " + ex.getMessage());
                    target.add(feedbackPanel);
                }
                target.appendJavaScript(
                        "var m = bootstrap.Modal.getInstance(document.getElementById('modalEditarCliente'));" +
                        "if (m) m.hide();");
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> formClicked) {
                target.add(feedbackPanel);
            }
        });

        add(form);
    }

    // O Wicket chama detach() automaticamente nos models de componentes,
    // mas o clienteModel é um campo da página — precisamos detachá-lo manualmente
    // para que o cache do LoadableDetachableModel seja limpo entre requisições.
    @Override
    protected void onDetach() {
        if (clienteModel != null) clienteModel.detach();
        super.onDetach();
    }

    private static List<EnderecoDTO> toEnderecosDTOs(List<EnderecoResponseDTO> lista) {
        if (lista == null) return new ArrayList<>();
        return lista.stream().map(e -> {
            EnderecoDTO dto = new EnderecoDTO();
            dto.setId(e.getId());
            dto.setTipo(e.getTipo());
            dto.setLogradouro(e.getLogradouro());
            dto.setNumero(e.getNumero());
            dto.setComplemento(e.getComplemento());
            dto.setBairro(e.getBairro());
            dto.setCidade(e.getCidade());
            dto.setEstado(e.getEstado());
            dto.setCep(e.getCep());
            dto.setPais(e.getPais());
            dto.setPrincipal(e.getPrincipal());
            return dto;
        }).collect(Collectors.toList());
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

    private static String safe(String v) {
        return v != null ? v : "";
    }
}
