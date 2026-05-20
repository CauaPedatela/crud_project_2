package com.crudproject.wicket.page;

import com.crudproject.dto.cliente.ClienteDTO;
import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.dto.endereco.EnderecoDTO;
import com.crudproject.dto.endereco.EnderecoResponseDTO;
import com.crudproject.model.TipoPessoa;
import com.crudproject.model.TipoEndereco;
import com.crudproject.service.ClienteService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ListagemClientesPage extends WebPage {

    @SpringBean
    private ClienteService clienteService;

    private FiltroState filtros = new FiltroState();

    private FeedbackPanel feedbackPanel;
    private TabelaClientesPanel tabelaPanel;
    public Long idParaExcluir;
    private Label totalClientesLabel;
    private Label totalAtivosLabel;

    // Campos do form de edição — preenchidos pelo JS antes do submit (via PropertyModel)
    private Long    idParaEditar;
    private String  formEmail;
    private String  formTelefone;
    private String  formRgIe;
    private Boolean formAtivo;

    // Campos do form de criação (Adicionar Cliente)
    private TipoPessoa        criarTipoPessoa = TipoPessoa.FISICA;
    private String            criarNome;
    private String            criarCpfCnpj;
    private String            criarRgIe;
    private String            criarData; // String formato yyyy-MM-dd (parseada para LocalDate no submit)
    private Boolean           criarAtivo = true;
    private String            criarEmail;
    private String            criarTelefone;

    // Lista dinâmica de endereços do novo cliente. Sempre começa com 1 endereço (principal).
    // Pode crescer/encolher via os botões "Adicionar/Remover" do modal.
    private List<EnderecoDTO> enderecosCriacao = novaListaEnderecos();

    // Componentes do modal de criar que precisam ser referenciados em outros métodos
    private FeedbackPanel       feedbackPanelCriar;
    private WebMarkupContainer  containerEnderecos;

    public ListagemClientesPage() {
        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        adicionarContadoresHeader();
        adicionarPanels();
        adicionarFormExcluir();
        adicionarFormEditar();
        adicionarFormCriar();
    }

    // Contadores do topo direito — sempre sobre o total geral, ignorando filtros.
    private void adicionarContadoresHeader() {
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

    private void adicionarPanels() {
        // Criamos o tabelaPanel antes para passar a referência aos panels que vão atualizá-lo via AJAX.
        // O setOutputMarkupId(true) garante que ele tenha um id="..." no HTML para o Wicket localizar via JS.
        tabelaPanel = new TabelaClientesPanel("tabelaPanel", filtros);
        tabelaPanel.setOutputMarkupId(true);

        add(new BuscaPanel("buscaPanel", filtros, tabelaPanel));
        add(tabelaPanel);

        FiltrosPanel filtrosPanel = new FiltrosPanel("filtrosPanel", filtros, tabelaPanel);
        filtrosPanel.setRenderBodyOnly(true);
        add(filtrosPanel);
    }

    private void adicionarFormExcluir() {
        Form<Void> form = new Form<>("formExcluirCliente");

        // HiddenField: PropertyModel liga o input ao campo idParaExcluir;
        // setMarkupId("idClienteParaExcluir") força um id HTML fixo pro JS achar.
        HiddenField<Long> hiddenId = new HiddenField<>(
                "hiddenClienteId",
                new PropertyModel<Long>(this, "idParaExcluir"), Long.class);
        hiddenId.setMarkupId("idClienteParaExcluir");
        hiddenId.setOutputMarkupId(true);
        form.add(hiddenId);

        form.add(new AjaxButton("btnConfirmarExclusao", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                if (idParaExcluir == null) return;
                try {
                    clienteService.excluir(idParaExcluir);
                    info("Cliente excluído com sucesso.");
                    target.add(feedbackPanel, tabelaPanel, totalClientesLabel, totalAtivosLabel);   // atualiza msg + tabela
                } catch (Exception ex) {
                    error("Erro ao excluir: " + ex.getMessage());
                    target.add(feedbackPanel);
                }
                // Fecha o modal em qualquer caso (sucesso ou erro)
                target.appendJavaScript(
                        "var m = bootstrap.Modal.getInstance(document.getElementById('modalConfirmarExclusao'));" +
                                "if (m) m.hide();");
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> formClicked) {
                target.add(feedbackPanel);
            }
        });

        add(form);
    }

    // Form de edição de cliente. Os campos editáveis (email, telefone, RG/IE, ativo)
    // são pré-preenchidos pelo JS antes do submit; nome e CPF/CNPJ são imutáveis
    // (não chegam ao servidor — preservamos os valores atuais via clienteService.buscarPorId).
    private void adicionarFormEditar() {
        Form<Void> form = new Form<>("formEditarCliente");

        HiddenField<Long> hiddenId = new HiddenField<>("hiddenEditClienteId",
                new PropertyModel<Long>(this, "idParaEditar"), Long.class);
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
                if (idParaEditar == null) return;
                try {
                    ClienteResponseDTO current = clienteService.buscarPorId(idParaEditar);

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

                    clienteService.atualizar(idParaEditar, dto);
                    info("Cliente atualizado com sucesso.");
                    // tabela: re-renderiza com novo email/telefone/etc
                    // totalAtivosLabel: pode mudar se o usuário alterou "ativo"
                    target.add(feedbackPanel, tabelaPanel, totalAtivosLabel);
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

    // Form de Criação de Cliente (com lista dinâmica de endereços e feedback dentro do modal).
    private void adicionarFormCriar() {
        final Form<Void> form = new Form<>("formCriarCliente");
        form.setOutputMarkupId(true); // permite target.add(form) para limpar o conteúdo do modal

        // FeedbackPanel local ao modal — filtra apenas mensagens reportadas no form,
        // pra mostrar erros sem fechar o modal. Mensagens de info() seguem aparecendo no FeedbackPanel da página.
        feedbackPanelCriar = new FeedbackPanel("feedbackCriar", new ComponentFeedbackMessageFilter(form));
        feedbackPanelCriar.setOutputMarkupId(true);
        form.add(feedbackPanelCriar);

        // ChoiceRenderers customizados — exibem texto amigável mas mantêm name() como value
        // (que é o que o JS de troca de labels PF/PJ espera).
        ChoiceRenderer<TipoPessoa> rendererPessoa = new ChoiceRenderer<TipoPessoa>() {
            @Override public Object getDisplayValue(TipoPessoa t) {
                return t == TipoPessoa.FISICA ? "Pessoa Física" : "Pessoa Jurídica";
            }
            @Override public String getIdValue(TipoPessoa t, int idx) { return t.name(); }
        };
        form.add(new DropDownChoice<>("criarTipoPessoa",
                new PropertyModel<>(this, "criarTipoPessoa"),
                Arrays.asList(TipoPessoa.values()), rendererPessoa));

        final ChoiceRenderer<TipoEndereco> rendererEnd = new ChoiceRenderer<TipoEndereco>() {
            @Override public Object getDisplayValue(TipoEndereco t) {
                String n = t.name();
                return n.charAt(0) + n.substring(1).toLowerCase();   // "RESIDENCIAL" -> "Residencial"
            }
            @Override public String getIdValue(TipoEndereco t, int idx) { return t.name(); }
        };

        // ===== Campos do Cliente =====
        form.add(new TextField<>("criarNome",     new PropertyModel<>(this, "criarNome")));
        form.add(new TextField<>("criarCpfCnpj",  new PropertyModel<>(this, "criarCpfCnpj")));
        form.add(new TextField<>("criarRgIe",     new PropertyModel<>(this, "criarRgIe")));
        form.add(new TextField<>("criarData",     new PropertyModel<>(this, "criarData")));
        form.add(new CheckBox  ("criarAtivo",    new PropertyModel<>(this, "criarAtivo")));
        form.add(new TextField<>("criarEmail",    new PropertyModel<>(this, "criarEmail")));
        form.add(new TextField<>("criarTelefone", new PropertyModel<>(this, "criarTelefone")));

        // ===== Endereços (lista dinâmica via ListView) =====
        containerEnderecos = new WebMarkupContainer("containerEnderecos");
        containerEnderecos.setOutputMarkupId(true);   // alvo do target.add quando add/remove/principal mudar

        ListView<EnderecoDTO> listaEnderecos = new ListView<EnderecoDTO>("listaEnderecosCriar",
                new PropertyModel<>(this, "enderecosCriacao")) {
            @Override
            protected void populateItem(ListItem<EnderecoDTO> item) {
                final EnderecoDTO endereco = item.getModelObject();
                final int idx              = item.getIndex();

                item.add(new Label("numeroEndereco", "Endereço " + (idx + 1)));

                Label badge = new Label("badgePrincipal", "Principal");
                badge.setVisible(Boolean.TRUE.equals(endereco.getPrincipal()));
                item.add(badge);

                // "Tornar Principal": desmarca todos e marca este. Botão some no que já é principal.
                AjaxButton btnTornarPrincipal = new AjaxButton("btnTornarPrincipal") {
                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                        for (EnderecoDTO e : enderecosCriacao) e.setPrincipal(false);
                        endereco.setPrincipal(true);
                        target.add(containerEnderecos);
                    }
                };
                btnTornarPrincipal.setVisible(!Boolean.TRUE.equals(endereco.getPrincipal()));
                item.add(btnTornarPrincipal);

                // "Remover": só fica habilitado quando há mais de 1 endereço.
                // Se o removido era o principal, o primeiro restante vira principal.
                AjaxButton btnRemover = new AjaxButton("btnRemoverEndereco") {
                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                        if (enderecosCriacao.size() <= 1) return;
                        boolean eraPrincipal = Boolean.TRUE.equals(endereco.getPrincipal());
                        enderecosCriacao.remove(endereco);
                        if (eraPrincipal && !enderecosCriacao.isEmpty()) {
                            enderecosCriacao.get(0).setPrincipal(true);
                        }
                        target.add(containerEnderecos);
                    }
                };
                btnRemover.setEnabled(enderecosCriacao.size() > 1);
                item.add(btnRemover);

                // Campos do endereço — bound aos atributos do próprio EnderecoDTO via PropertyModel
                item.add(new DropDownChoice<>("endTipo",
                        new PropertyModel<>(endereco, "tipo"),
                        Arrays.asList(TipoEndereco.values()), rendererEnd));
                item.add(new TextField<>("endCep",         new PropertyModel<>(endereco, "cep")));
                item.add(new TextField<>("endLogradouro",  new PropertyModel<>(endereco, "logradouro")));
                item.add(new TextField<>("endNumero",      new PropertyModel<>(endereco, "numero")));
                item.add(new TextField<>("endComplemento", new PropertyModel<>(endereco, "complemento")));
                item.add(new TextField<>("endBairro",      new PropertyModel<>(endereco, "bairro")));
                item.add(new TextField<>("endCidade",      new PropertyModel<>(endereco, "cidade")));
                item.add(new TextField<>("endEstado",      new PropertyModel<>(endereco, "estado")));
                item.add(new TextField<>("endPais",        new PropertyModel<>(endereco, "pais")));
            }
        };
        listaEnderecos.setReuseItems(false);   // sempre recria os itens — necessário com AjaxButtons internos
        containerEnderecos.add(listaEnderecos);
        form.add(containerEnderecos);

        // Botão "Adicionar outro endereço" — adiciona um EnderecoDTO vazio à lista e re-renderiza
        form.add(new AjaxButton("btnAdicionarEndereco") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                EnderecoDTO novo = new EnderecoDTO();
                novo.setTipo(TipoEndereco.RESIDENCIAL);
                novo.setPais("Brasil");
                novo.setPrincipal(false);
                enderecosCriacao.add(novo);
                target.add(containerEnderecos);
            }
        });

        // ===== Botão principal: "Cadastrar Cliente" =====
        form.add(new AjaxButton("btnSalvarNovoCliente", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                try {
                    ClienteDTO dto = new ClienteDTO();
                    dto.setTipoPessoa(criarTipoPessoa);
                    dto.setNome(criarNome);
                    dto.setCpfCnpj(criarCpfCnpj);
                    dto.setRgInscricaoEstadual(criarRgIe);

                    if (criarData != null && !criarData.trim().isEmpty()) {
                        java.time.format.DateTimeFormatter formatterBr = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        dto.setDataNascimento(java.time.LocalDate.parse(criarData, formatterBr));
                    }

                    dto.setAtivo(criarAtivo != null ? criarAtivo : false);
                    dto.setEmail(criarEmail);
                    dto.setTelefone(criarTelefone);

                    // Garante país preenchido em todos os endereços (default Brasil)
                    for (EnderecoDTO e : enderecosCriacao) {
                        if (e.getPais() == null || e.getPais().isBlank()) e.setPais("Brasil");
                    }
                    // Passa uma cópia da lista pro DTO (evita compartilhar referência mutável)
                    dto.setEnderecos(new ArrayList<>(enderecosCriacao));

                    clienteService.salvar(dto);

                    // Sucesso → info na página + limpa Java + re-renderiza tudo + fecha modal
                    info("Cliente cadastrado com sucesso.");
                    limparCamposCriacao();
                    target.add(form, feedbackPanel, tabelaPanel, totalClientesLabel, totalAtivosLabel);
                    target.appendJavaScript(
                            "var m = bootstrap.Modal.getInstance(document.getElementById('modalCriarCliente'));" +
                            "if (m) m.hide();");

                } catch (Exception ex) {
                    // Erro → reporta no form (filtro do feedbackPanelCriar captura) e mantém modal aberto
                    form.error("Erro ao cadastrar cliente: " + ex.getMessage());
                    target.add(feedbackPanelCriar);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> formClicked) {
                // Validação HTML5/Wicket falhou — mostra mensagem dentro do modal
                target.add(feedbackPanelCriar);
            }
        });

        add(form);
    }

    // Reseta os campos do form de criação (chamado após salvar com sucesso)
    private void limparCamposCriacao() {
        criarTipoPessoa = TipoPessoa.FISICA;
        criarNome = null;
        criarCpfCnpj = null;
        criarRgIe = null;
        criarData = null;
        criarAtivo = true;
        criarEmail = null;
        criarTelefone = null;
        enderecosCriacao = novaListaEnderecos();
    }

    // Lista inicial: um único endereço vazio, marcado como principal
    private static List<EnderecoDTO> novaListaEnderecos() {
        List<EnderecoDTO> lista = new ArrayList<>();
        EnderecoDTO primeiro = new EnderecoDTO();
        primeiro.setTipo(TipoEndereco.RESIDENCIAL);
        primeiro.setPais("Brasil");
        primeiro.setPrincipal(true);
        lista.add(primeiro);
        return lista;
    }

    // Converte EnderecoResponseDTO → EnderecoDTO preservando IDs.
    // Necessário no atualizar() para o EnderecoSincronizador entender que são
    // os mesmos endereços (com ID = update, sem ID = insert, ausente = delete).
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
}