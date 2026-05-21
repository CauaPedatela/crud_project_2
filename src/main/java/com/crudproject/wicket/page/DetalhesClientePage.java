/*
 * DetalhesClientePage — exibe os dados de um cliente e seus endereços, com CRUD completo de endereços.
 *
 * Operações de endereço disponíveis:
 *   - Excluir: bloqueado para o endereço principal e para o único endereço restante
 *   - Editar: número, complemento, telefone e status de principal
 *   - Adicionar: formulário completo com todos os campos do endereço
 *
 * Todas as mutações passam pelo ClienteService.atualizar(), reutilizando o EnderecoSincronizador.
 * O containerEnderecos (setOutputMarkupId) é atualizado via AJAX após cada operação.
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
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DetalhesClientePage extends WebPage {

    @SpringBean
    private ClienteService clienteService;

    private static final DateTimeFormatter FMT_DATA     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_CADASTRO = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    // ── Componentes da página ────────────────────────────────────────────────
    private Long                                        clienteId;
    private LoadableDetachableModel<ClienteResponseDTO> clienteModel;
    private FeedbackPanel                               feedbackPanel;
    private FeedbackPanel                               feedbackPanelEndereco;
    private FeedbackPanel                               feedbackPanelEditarCliente;
    private FeedbackPanel                               feedbackPanelEditarEndereco;
    private WebMarkupContainer                          cardCliente;
    private WebMarkupContainer                          btnEditarCliente;
    private WebMarkupContainer                          containerEnderecos;

    // ── Form: editar cliente ─────────────────────────────────────────────────
    private String  formEmail;
    private String  formRgIe;
    private Boolean formAtivo;

    // ── Form: excluir endereço ───────────────────────────────────────────────
    private Long idEnderecoParaExcluir;

    // ── Form: editar endereço ────────────────────────────────────────────────
    private Long    idEnderecoParaEditar;
    private String  editEndNumero;
    private String  editEndComplemento;
    private String  editEndTelefone;
    private Boolean editEndPrincipal;

    // ── Form: adicionar endereço ─────────────────────────────────────────────
    private TipoEndereco addEndTipo        = TipoEndereco.RESIDENCIAL;
    private String       addEndLogradouro;
    private String       addEndNumero;
    private String       addEndComplemento;
    private String       addEndBairro;
    private String       addEndCidade;
    private String       addEndEstado;
    private String       addEndCep;
    private String       addEndPais        = "Brasil";
    private String       addEndTelefone;
    private Boolean      addEndPrincipal   = false;

    // ════════════════════════════════════════════════════════════════════════
    // CONSTRUTOR
    // ════════════════════════════════════════════════════════════════════════

    public DetalhesClientePage(PageParameters params) {
        clienteId = params.get("id").toLong();

        clienteModel = new LoadableDetachableModel<ClienteResponseDTO>() {
            @Override
            protected ClienteResponseDTO load() {
                return clienteService.buscarPorId(clienteId);
            }
        };

        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        add(new BookmarkablePageLink<>("linkNavClientes", ListagemClientesPage.class));
        add(new BookmarkablePageLink<>("linkBreadcrumb",  ListagemClientesPage.class));
        add(new BookmarkablePageLink<>("linkVoltar",      ListagemClientesPage.class));

        // Botão "Editar" do topo — data-* gerados dinamicamente do clienteModel
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
                tag.put("data-ativo",    String.valueOf(Boolean.TRUE.equals(c.getAtivo())));
                tag.put("data-tipo",     c.getTipoPessoa() != null ? c.getTipoPessoa().name() : "FISICA");
                tag.put("data-cpf-cnpj", safe(c.getCpfCnpj()));
                tag.put("data-rg-ie",    safe(c.getRgInscricaoEstadual()));
            }
        });
        add(btnEditarCliente);

        WebMarkupContainer linkPdf = new WebMarkupContainer("linkRelatorioPdf");
        linkPdf.add(AttributeModifier.replace("href",
                "/api/relatorios/cliente/detalhes/pdf?id=" + clienteId));
        add(linkPdf);

        WebMarkupContainer linkExcel = new WebMarkupContainer("linkRelatorioExcel");
        linkExcel.add(AttributeModifier.replace("href",
                "/api/relatorios/cliente/detalhes/excel?id=" + clienteId));
        add(linkExcel);

        adicionarCardCliente();
        adicionarContainerEnderecos();
        adicionarFormEditarCliente();
        adicionarFormExcluirEndereco();
        adicionarFormEditarEndereco();
        adicionarFormAdicionarEndereco();
    }

    // ════════════════════════════════════════════════════════════════════════
    // CARD DO CLIENTE
    // ════════════════════════════════════════════════════════════════════════

    private void adicionarCardCliente() {
        cardCliente = new WebMarkupContainer("cardCliente");
        cardCliente.setOutputMarkupId(true);

        cardCliente.add(new Label("avatarIniciais", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return gerarIniciais(clienteModel.getObject().getNome());
            }
        }));
        cardCliente.add(new Label("nomeCliente", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() { return clienteModel.getObject().getNome(); }
        }));

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
        add(cardCliente);
    }

    // ════════════════════════════════════════════════════════════════════════
    // CONTAINER DE ENDEREÇOS (lista + botões por endereço)
    // ════════════════════════════════════════════════════════════════════════

    private void adicionarContainerEnderecos() {
        containerEnderecos = new WebMarkupContainer("containerEnderecos");
        containerEnderecos.setOutputMarkupId(true);

        ListView<EnderecoResponseDTO> listaEnderecos = new ListView<EnderecoResponseDTO>(
                "listaEnderecos",
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
                int     totalEnds   = clienteModel.getObject().getEnderecos().size();
                boolean podeDeletar = !isPrincipal && totalEnds > 1;

                Label tipoLabel = new Label("endTipo",
                        end.getTipo() != null ? end.getTipo().name() : "—");
                tipoLabel.add(new AttributeAppender("class", Model.of(
                        end.getTipo() == TipoEndereco.RESIDENCIAL
                                ? " text-bg-success-subtle text-success-emphasis"
                                : " text-bg-warning-subtle text-warning-emphasis"), " "));
                item.add(tipoLabel);

                Label principalLabel = new Label("endPrincipal", isPrincipal ? "Principal" : "Secundário");
                principalLabel.add(new AttributeAppender("class", Model.of(
                        isPrincipal ? " text-bg-primary-subtle text-primary-emphasis"
                                    : " text-bg-secondary-subtle text-secondary-emphasis"), " "));
                item.add(principalLabel);

                item.add(new Label("endLogradouro",     nvl(end.getLogradouro())));
                item.add(new Label("endNumeroEndereco", nvl(end.getNumero())));
                item.add(new Label("endComplemento",    nvl(end.getComplemento())));
                item.add(new Label("endBairro",         nvl(end.getBairro())));
                item.add(new Label("endCidade",         nvl(end.getCidade())));
                item.add(new Label("endEstado",         nvl(end.getEstado())));
                item.add(new Label("endCep",            nvl(end.getCep())));
                item.add(new Label("endPais",           nvl(end.getPais())));
                item.add(new Label("endTelefone",       nvl(end.getTelefone())));

                // Botão Editar — passa dados via data-* para o JS preencher o modal
                WebMarkupContainer btnEditar = new WebMarkupContainer("btnEditarEndereco");
                btnEditar.add(AttributeModifier.replace("onclick",          "abrirModalEditarEndereco(this)"));
                btnEditar.add(AttributeModifier.replace("data-id",          String.valueOf(end.getId())));
                btnEditar.add(AttributeModifier.replace("data-numero",      safe(end.getNumero())));
                btnEditar.add(AttributeModifier.replace("data-complemento", safe(end.getComplemento())));
                btnEditar.add(AttributeModifier.replace("data-telefone",    safe(end.getTelefone())));
                btnEditar.add(AttributeModifier.replace("data-principal",   String.valueOf(isPrincipal)));
                btnEditar.add(AttributeModifier.replace("data-logradouro",  safe(end.getLogradouro())));
                item.add(btnEditar);

                // Botão Excluir — visualmente desabilitado para principal ou único endereço
                WebMarkupContainer btnExcluir = new WebMarkupContainer("btnExcluirEndereco");
                if (podeDeletar) {
                    btnExcluir.add(AttributeModifier.replace("onclick",
                            "abrirModalExcluirEndereco(this)"));
                    btnExcluir.add(AttributeModifier.replace("data-id",
                            String.valueOf(end.getId())));
                    btnExcluir.add(AttributeModifier.replace("data-logradouro",
                            safe(end.getLogradouro())));
                } else {
                    String motivo = isPrincipal
                            ? "Este é o endereço principal. Para excluí-lo, primeiro defina outro endereço como principal."
                            : "O cliente deve ter pelo menos um endereço cadastrado.";
                    btnExcluir.add(AttributeModifier.replace("onclick", "mostrarAvisoNaoPodeDeletar(this)"));
                    btnExcluir.add(AttributeModifier.replace("data-motivo", motivo));
                    btnExcluir.add(new AttributeAppender("class", Model.of(" opacity-50"), ""));
                }
                item.add(btnExcluir);
            }
        };

        containerEnderecos.add(listaEnderecos);
        add(containerEnderecos);
    }

    // ════════════════════════════════════════════════════════════════════════
    // FORM: EDITAR CLIENTE
    // ════════════════════════════════════════════════════════════════════════

    private void adicionarFormEditarCliente() {
        Form<Void> form = new Form<>("formEditarCliente");

        feedbackPanelEditarCliente = new FeedbackPanel("feedbackEditarCliente",
                new ComponentFeedbackMessageFilter(form));
        feedbackPanelEditarCliente.setOutputMarkupId(true);
        form.add(feedbackPanelEditarCliente);

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
                    dto.setNome(current.getNome());
                    dto.setCpfCnpj(current.getCpfCnpj());
                    dto.setDataNascimento(current.getDataNascimento());
                    dto.setEmail(formEmail);
                    dto.setAtivo(formAtivo);
                    dto.setRgInscricaoEstadual(
                            current.getTipoPessoa() == TipoPessoa.JURIDICA
                                    ? formRgIe : current.getRgInscricaoEstadual());
                    dto.setEnderecos(toEnderecosDTOs(current.getEnderecos()));
                    clienteService.atualizar(clienteId, dto);
                    info("Cliente atualizado com sucesso.");
                    target.add(feedbackPanel, cardCliente, btnEditarCliente);
                    target.appendJavaScript(
                            "var m = bootstrap.Modal.getInstance(document.getElementById('modalEditarCliente'));" +
                            "if (m) m.hide();");
                } catch (Exception ex) {
                    form.error("Erro ao atualizar: " + ex.getMessage());
                    target.add(feedbackPanelEditarCliente);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> formClicked) {
                target.add(feedbackPanelEditarCliente);
            }
        });

        add(form);
    }

    // ════════════════════════════════════════════════════════════════════════
    // FORM: EXCLUIR ENDEREÇO
    // ════════════════════════════════════════════════════════════════════════

    private void adicionarFormExcluirEndereco() {
        Form<Void> form = new Form<>("formExcluirEndereco");

        HiddenField<Long> hiddenId = new HiddenField<>("hiddenExcluirEnderecoId",
                new PropertyModel<Long>(this, "idEnderecoParaExcluir"), Long.class);
        hiddenId.setMarkupId("idEnderecoParaExcluir");
        hiddenId.setOutputMarkupId(true);
        form.add(hiddenId);

        form.add(new AjaxButton("btnConfirmarExcluirEndereco", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                if (idEnderecoParaExcluir == null) return;
                try {
                    ClienteResponseDTO current = clienteService.buscarPorId(clienteId);
                    List<EnderecoResponseDTO> enderecos = current.getEnderecos();

                    EnderecoResponseDTO alvo = enderecos.stream()
                            .filter(e -> idEnderecoParaExcluir.equals(e.getId()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Endereço não encontrado."));

                    if (Boolean.TRUE.equals(alvo.getPrincipal())) {
                        throw new RuntimeException(
                                "Não é possível excluir o endereço principal. " +
                                "Defina outro endereço como principal antes de excluir este.");
                    }
                    if (enderecos.size() <= 1) {
                        throw new RuntimeException("O cliente deve ter pelo menos um endereço.");
                    }

                    List<EnderecoDTO> novosEnderecos = toEnderecosDTOs(enderecos).stream()
                            .filter(e -> !idEnderecoParaExcluir.equals(e.getId()))
                            .collect(Collectors.toList());

                    clienteService.atualizar(clienteId, clienteDtoComEnderecos(current, novosEnderecos));
                    clienteModel.detach();
                    info("Endereço excluído com sucesso.");
                    target.add(feedbackPanel, containerEnderecos);
                } catch (Exception ex) {
                    error("Erro ao excluir endereço: " + ex.getMessage());
                    target.add(feedbackPanel);
                }
                target.appendJavaScript(
                        "var m = bootstrap.Modal.getInstance(document.getElementById('modalConfirmarExcluirEndereco'));" +
                        "if (m) m.hide();");
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> formClicked) {
                target.add(feedbackPanel);
            }
        });

        add(form);
    }

    // ════════════════════════════════════════════════════════════════════════
    // FORM: EDITAR ENDEREÇO
    // ════════════════════════════════════════════════════════════════════════

    private void adicionarFormEditarEndereco() {
        Form<Void> form = new Form<>("formEditarEndereco");

        feedbackPanelEditarEndereco = new FeedbackPanel("feedbackEditarEndereco",
                new ComponentFeedbackMessageFilter(form));
        feedbackPanelEditarEndereco.setOutputMarkupId(true);
        form.add(feedbackPanelEditarEndereco);

        HiddenField<Long> hiddenId = new HiddenField<>("hiddenEditarEnderecoId",
                new PropertyModel<Long>(this, "idEnderecoParaEditar"), Long.class);
        hiddenId.setMarkupId("editEnderecoId");
        hiddenId.setOutputMarkupId(true);
        form.add(hiddenId);

        TextField<String> tfNumero = new TextField<>("editEndNumero",
                new PropertyModel<>(this, "editEndNumero"));
        tfNumero.setMarkupId("editEndNumero");
        tfNumero.setOutputMarkupId(true);
        form.add(tfNumero);

        TextField<String> tfComplemento = new TextField<>("editEndComplemento",
                new PropertyModel<>(this, "editEndComplemento"));
        tfComplemento.setMarkupId("editEndComplemento");
        tfComplemento.setOutputMarkupId(true);
        form.add(tfComplemento);

        TextField<String> tfTelefone = new TextField<>("editEndTelefone",
                new PropertyModel<>(this, "editEndTelefone"));
        tfTelefone.setMarkupId("editEndTelefone");
        tfTelefone.setOutputMarkupId(true);
        form.add(tfTelefone);

        CheckBox cbPrincipal = new CheckBox("editEndPrincipal",
                new PropertyModel<>(this, "editEndPrincipal"));
        cbPrincipal.setMarkupId("editEndPrincipal");
        cbPrincipal.setOutputMarkupId(true);
        form.add(cbPrincipal);

        form.add(new AjaxButton("btnSalvarEditarEndereco", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                if (idEnderecoParaEditar == null) return;
                try {
                    ClienteResponseDTO current = clienteService.buscarPorId(clienteId);
                    List<EnderecoDTO> novosEnderecos = toEnderecosDTOs(current.getEnderecos());

                    EnderecoDTO alvo = novosEnderecos.stream()
                            .filter(e -> idEnderecoParaEditar.equals(e.getId()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Endereço não encontrado."));

                    alvo.setNumero(editEndNumero);
                    alvo.setComplemento(editEndComplemento);
                    alvo.setTelefone(editEndTelefone);

                    if (Boolean.TRUE.equals(editEndPrincipal)) {
                        // Desmarca todos e marca o endereço editado como principal
                        novosEnderecos.forEach(e -> e.setPrincipal(false));
                        alvo.setPrincipal(true);
                    } else {
                        // Impede desmarcar o principal quando ele é o único
                        boolean temOutroPrincipal = novosEnderecos.stream()
                                .anyMatch(e -> !idEnderecoParaEditar.equals(e.getId())
                                        && Boolean.TRUE.equals(e.getPrincipal()));
                        if (!temOutroPrincipal && Boolean.TRUE.equals(alvo.getPrincipal())) {
                            throw new RuntimeException(
                                    "Não é possível desmarcar o único endereço principal. " +
                                    "Defina outro endereço como principal antes.");
                        }
                        alvo.setPrincipal(false);
                    }

                    clienteService.atualizar(clienteId, clienteDtoComEnderecos(current, novosEnderecos));
                    clienteModel.detach();
                    info("Endereço atualizado com sucesso.");
                    target.add(feedbackPanel, containerEnderecos);
                    target.appendJavaScript(
                            "var m = bootstrap.Modal.getInstance(document.getElementById('modalEditarEndereco'));" +
                            "if (m) m.hide();");
                } catch (Exception ex) {
                    form.error("Erro ao editar endereço: " + ex.getMessage());
                    target.add(feedbackPanelEditarEndereco);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> formClicked) {
                target.add(feedbackPanelEditarEndereco);
            }
        });

        add(form);
    }

    // ════════════════════════════════════════════════════════════════════════
    // FORM: ADICIONAR ENDEREÇO
    // ════════════════════════════════════════════════════════════════════════

    private void adicionarFormAdicionarEndereco() {
        Form<Void> form = new Form<>("formAdicionarEndereco");
        form.setOutputMarkupId(true);

        // FeedbackPanel scoped ao form — erros ficam dentro do modal sem fechá-lo
        feedbackPanelEndereco = new FeedbackPanel("feedbackEndereco",
                new ComponentFeedbackMessageFilter(form));
        feedbackPanelEndereco.setOutputMarkupId(true);
        form.add(feedbackPanelEndereco);

        ChoiceRenderer<TipoEndereco> renderer = new ChoiceRenderer<TipoEndereco>() {
            @Override public Object getDisplayValue(TipoEndereco t) {
                String n = t.name();
                return n.charAt(0) + n.substring(1).toLowerCase();
            }
            @Override public String getIdValue(TipoEndereco t, int idx) { return t.name(); }
        };

        form.add(new DropDownChoice<>("addEndTipo",
                new PropertyModel<>(this, "addEndTipo"),
                Arrays.asList(TipoEndereco.values()), renderer));
        form.add(new TextField<>("addEndLogradouro",  new PropertyModel<>(this, "addEndLogradouro")));
        form.add(new TextField<>("addEndNumero",      new PropertyModel<>(this, "addEndNumero")));
        form.add(new TextField<>("addEndComplemento", new PropertyModel<>(this, "addEndComplemento")));
        form.add(new TextField<>("addEndBairro",      new PropertyModel<>(this, "addEndBairro")));
        form.add(new TextField<>("addEndCidade",      new PropertyModel<>(this, "addEndCidade")));
        form.add(new TextField<>("addEndEstado",      new PropertyModel<>(this, "addEndEstado")));
        form.add(new TextField<>("addEndCep",         new PropertyModel<>(this, "addEndCep")));
        form.add(new TextField<>("addEndPais",        new PropertyModel<>(this, "addEndPais")));
        form.add(new TextField<>("addEndTelefone",    new PropertyModel<>(this, "addEndTelefone")));
        form.add(new CheckBox("addEndPrincipal",      new PropertyModel<>(this, "addEndPrincipal")));

        form.add(new AjaxButton("btnSalvarAdicionarEndereco", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                try {
                    ClienteResponseDTO current = clienteService.buscarPorId(clienteId);
                    List<EnderecoDTO> novosEnderecos = toEnderecosDTOs(current.getEnderecos());

                    EnderecoDTO novoEndereco = new EnderecoDTO();
                    novoEndereco.setTipo(addEndTipo);
                    novoEndereco.setLogradouro(addEndLogradouro);
                    novoEndereco.setNumero(addEndNumero);
                    novoEndereco.setComplemento(addEndComplemento);
                    novoEndereco.setBairro(addEndBairro);
                    novoEndereco.setCidade(addEndCidade);
                    novoEndereco.setEstado(addEndEstado);
                    novoEndereco.setCep(addEndCep);
                    novoEndereco.setPais(addEndPais != null && !addEndPais.isBlank() ? addEndPais : "Brasil");
                    novoEndereco.setTelefone(addEndTelefone);
                    novoEndereco.setPrincipal(Boolean.TRUE.equals(addEndPrincipal));

                    // Se o novo é principal, desmarca todos os existentes
                    if (Boolean.TRUE.equals(addEndPrincipal)) {
                        novosEnderecos.forEach(e -> e.setPrincipal(false));
                    }
                    novosEnderecos.add(novoEndereco);

                    clienteService.atualizar(clienteId, clienteDtoComEnderecos(current, novosEnderecos));
                    limparCamposAdicionarEndereco();
                    clienteModel.detach();
                    info("Endereço adicionado com sucesso.");
                    target.add(form, feedbackPanel, containerEnderecos);
                    target.appendJavaScript(
                            "var m = bootstrap.Modal.getInstance(document.getElementById('modalAdicionarEndereco'));" +
                            "if (m) m.hide();");
                } catch (Exception ex) {
                    form.error("Erro ao adicionar endereço: " + ex.getMessage());
                    target.add(feedbackPanelEndereco);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> formClicked) {
                target.add(feedbackPanelEndereco);
            }
        });

        add(form);
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════════════════

    @Override
    protected void onDetach() {
        if (clienteModel != null) clienteModel.detach();
        super.onDetach();
    }

    // Monta um ClienteDTO completo substituindo apenas a lista de endereços.
    // Usado pelos 3 forms de endereço para evitar duplicar a construção do DTO.
    private ClienteDTO clienteDtoComEnderecos(ClienteResponseDTO current, List<EnderecoDTO> enderecos) {
        ClienteDTO dto = new ClienteDTO();
        dto.setTipoPessoa(current.getTipoPessoa());
        dto.setNome(current.getNome());
        dto.setCpfCnpj(current.getCpfCnpj());
        dto.setDataNascimento(current.getDataNascimento());
        dto.setEmail(current.getEmail());
        dto.setAtivo(current.getAtivo());
        dto.setRgInscricaoEstadual(current.getRgInscricaoEstadual());
        dto.setEnderecos(enderecos);
        return dto;
    }

    private void limparCamposAdicionarEndereco() {
        addEndTipo        = TipoEndereco.RESIDENCIAL;
        addEndLogradouro  = null;
        addEndNumero      = null;
        addEndComplemento = null;
        addEndBairro      = null;
        addEndCidade      = null;
        addEndEstado      = null;
        addEndCep         = null;
        addEndPais        = "Brasil";
        addEndTelefone    = null;
        addEndPrincipal   = false;
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
            dto.setTelefone(e.getTelefone());
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
