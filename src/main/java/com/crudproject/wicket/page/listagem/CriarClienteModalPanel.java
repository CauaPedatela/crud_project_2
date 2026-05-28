/*
 * CriarClienteModalPanel — modal "Cadastrar Novo Cliente" com lista dinâmica
 * de endereços. Todo o estado do formulário fica no CriacaoClienteState (DTO
 * Serializable) — substitui as ~7 propriedades soltas + a lista que existiam
 * na ListagemClientesPage. Esse DTO é o mesmo payload JSON que o Angular vai
 * enviar via POST /api/clientes no futuro.
 *
 * Fluxo:
 *   - Submit do form → monta ClienteDTO a partir do state → clienteService.salvar()
 *   - Sucesso: limpa state, fecha modal, re-renderiza tabela/contadores/feedback
 *   - Erro:   error() no form, mantém modal aberto, mostra feedback dentro do modal
 *   - Fechar modal: AbstractDefaultAjaxBehavior dispara resetar() pra limpar o state
 */
package com.crudproject.wicket.page.listagem;

import com.crudproject.dto.endereco.EnderecoDTO;
import com.crudproject.model.TipoEndereco;
import com.crudproject.model.TipoPessoa;
import com.crudproject.service.ClienteService;
import com.crudproject.wicket.state.CriacaoClienteState;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.Arrays;
import java.util.List;

public class CriarClienteModalPanel extends Panel {

    // Lista estática de UFs foi removida — agora os estados (e cidades) vêm
    // dinamicamente da API do IBGE via JS (ver clientes.js).

    @SpringBean
    private ClienteService clienteService;

    private final CriacaoClienteState state = CriacaoClienteState.inicial();
    private AbstractDefaultAjaxBehavior resetBehavior;

    public CriarClienteModalPanel(String id, FeedbackPanel feedbackPagina,
                                  Component... componentesParaAtualizar) {
        super(id);

        final Form<Void> form = new Form<>("formCriarCliente");
        form.setOutputMarkupId(true);

        // Limpa o state e o "raw input" do form quando o modal é fechado
        resetBehavior = new AbstractDefaultAjaxBehavior() {
            @Override
            protected void respond(AjaxRequestTarget target) {
                state.resetar();
                form.clearInput();
                target.add(form);
            }
        };
        form.add(resetBehavior);

        final FeedbackPanel feedbackModal = new FeedbackPanel("feedbackCriar",
                new ComponentFeedbackMessageFilter(form));
        feedbackModal.setOutputMarkupId(true);
        form.add(feedbackModal);

        ChoiceRenderer<TipoPessoa> rendererPessoa = new ChoiceRenderer<TipoPessoa>() {
            @Override public Object getDisplayValue(TipoPessoa t) {
                return t == TipoPessoa.FISICA ? "Pessoa Física" : "Pessoa Jurídica";
            }
            @Override public String getIdValue(TipoPessoa t, int idx) { return t.name(); }
        };
        form.add(new DropDownChoice<>("criarTipoPessoa",
                new PropertyModel<>(state, "tipoPessoa"),
                Arrays.asList(TipoPessoa.values()), rendererPessoa));

        form.add(new TextField<>("criarNome",    new PropertyModel<>(state, "nome")));
        form.add(new TextField<>("criarCpfCnpj", new PropertyModel<>(state, "cpfCnpj")));
        form.add(new TextField<>("criarRgIe",    new PropertyModel<>(state, "rgIe")));
        form.add(new TextField<>("criarData",    new PropertyModel<>(state, "data")));
        form.add(new CheckBox  ("criarAtivo",   new PropertyModel<>(state, "ativo")));
        form.add(new TextField<>("criarEmail",   new PropertyModel<>(state, "email")));

        final WebMarkupContainer containerEnderecos = new WebMarkupContainer("containerEnderecos");
        containerEnderecos.setOutputMarkupId(true);
        containerEnderecos.add(criarListaEnderecos(containerEnderecos));
        form.add(containerEnderecos);

        // "Adicionar outro endereço" — append no state.getEnderecos() e re-render
        form.add(new AjaxButton("btnAdicionarEndereco") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                EnderecoDTO novo = new EnderecoDTO();
                novo.setTipo(TipoEndereco.RESIDENCIAL);
                novo.setPais("Brasil");
                novo.setPrincipal(false);
                state.getEnderecos().add(novo);
                target.add(containerEnderecos);
            }
        });

        form.add(new AjaxButton("btnSalvarNovoCliente", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                try {
                    clienteService.salvar(state.toDto());

                    CriarClienteModalPanel.this.getPage().info("Cliente cadastrado com sucesso.");
                    state.resetar();
                    target.add(form, feedbackPagina);
                    for (Component c : componentesParaAtualizar) {
                        target.add(c);
                    }
                    target.appendJavaScript(
                            "var m = bootstrap.Modal.getInstance(document.getElementById('modalCriarCliente'));" +
                                    "if (m) m.hide();");
                } catch (Exception ex) {
                    form.error("Erro ao cadastrar cliente: " + ex.getMessage());
                    target.add(feedbackModal);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> formClicked) {
                target.add(feedbackModal);
            }
        });

        add(form);
    }

    // ListView dos endereços do novo cliente. Extraída em método para manter
    // o construtor enxuto e isolar a renderização de cada item.
    private ListView<EnderecoDTO> criarListaEnderecos(final WebMarkupContainer containerEnderecos) {
        final ChoiceRenderer<TipoEndereco> rendererEnd = new ChoiceRenderer<TipoEndereco>() {
            @Override public Object getDisplayValue(TipoEndereco t) {
                String n = t.name();
                return n.charAt(0) + n.substring(1).toLowerCase();
            }
            @Override public String getIdValue(TipoEndereco t, int idx) { return t.name(); }
        };

        ListView<EnderecoDTO> lista = new ListView<EnderecoDTO>("listaEnderecosCriar",
                new PropertyModel<>(state, "enderecos")) {
            @Override
            protected void populateItem(ListItem<EnderecoDTO> item) {
                final EnderecoDTO endereco = item.getModelObject();
                final int idx              = item.getIndex();

                item.add(new Label("numeroEndereco", "Endereço " + (idx + 1)));

                Label badge = new Label("badgePrincipal", "Principal");
                badge.setVisible(Boolean.TRUE.equals(endereco.getPrincipal()));
                item.add(badge);

                AjaxButton btnTornarPrincipal = new AjaxButton("btnTornarPrincipal") {
                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                        for (EnderecoDTO e : state.getEnderecos()) e.setPrincipal(false);
                        endereco.setPrincipal(true);
                        target.add(containerEnderecos);
                    }
                };
                btnTornarPrincipal.setVisible(!Boolean.TRUE.equals(endereco.getPrincipal()));
                item.add(btnTornarPrincipal);

                AjaxButton btnRemover = new AjaxButton("btnRemoverEndereco") {
                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                        List<EnderecoDTO> enderecos = state.getEnderecos();
                        if (enderecos.size() <= 1) return;
                        boolean eraPrincipal = Boolean.TRUE.equals(endereco.getPrincipal());
                        enderecos.remove(endereco);
                        if (eraPrincipal && !enderecos.isEmpty()) {
                            enderecos.get(0).setPrincipal(true);
                        }
                        target.add(containerEnderecos);
                    }
                };
                btnRemover.setEnabled(state.getEnderecos().size() > 1);
                item.add(btnRemover);

                item.add(new DropDownChoice<>("endTipo",
                        new PropertyModel<>(endereco, "tipo"),
                        Arrays.asList(TipoEndereco.values()), rendererEnd));
                item.add(new TextField<>("endCep",         new PropertyModel<>(endereco, "cep")));
                item.add(new TextField<>("endLogradouro",  new PropertyModel<>(endereco, "logradouro")));
                item.add(new TextField<>("endNumero",      new PropertyModel<>(endereco, "numero")));
                item.add(new TextField<>("endComplemento", new PropertyModel<>(endereco, "complemento")));
                item.add(new TextField<>("endBairro",      new PropertyModel<>(endereco, "bairro")));
                // Estado e Cidade são HiddenFields — o JS popula <select>s puros
                // e mantém estes hidden em sincronia. Permite lista dinâmica do IBGE
                // sem brigar com a validação interna do DropDownChoice.
                item.add(new HiddenField<>("endCidade",    new PropertyModel<>(endereco, "cidade")));
                item.add(new HiddenField<>("endEstado",    new PropertyModel<>(endereco, "estado")));
                item.add(new TextField<>("endPais",        new PropertyModel<>(endereco, "pais")));
                item.add(new TextField<>("endTelefone",    new PropertyModel<>(endereco, "telefone")));
            }
        };
        lista.setReuseItems(false);   // sempre recria os itens — necessário com AjaxButtons internos
        return lista;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        if (resetBehavior != null) {
            response.render(OnDomReadyHeaderItem.forScript(
                    "document.getElementById('modalCriarCliente').addEventListener('hidden.bs.modal', function() {" +
                            "  Wicket.Ajax.ajax({u:'" + resetBehavior.getCallbackUrl() + "'});" +
                            "});"
            ));
        }
    }
}
