/*
 * AdicionarEnderecoModalPanel — modal "Adicionar Endereço" da página de detalhes.
 *
 * Todo o estado fica em AdicaoEnderecoState (DTO Serializable). O fechamento
 * do modal dispara o resetBehavior (AbstractDefaultAjaxBehavior) que zera o
 * state — assim cada abertura começa limpa.
 *
 * O campo CEP aciona ViaCEP via onblur="buscarCepNoBloco(this)" (clientes.js).
 */
package com.crudproject.wicket.page.detalhes;

import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.dto.endereco.EnderecoDTO;
import com.crudproject.model.TipoEndereco;
import com.crudproject.service.ClienteService;
import com.crudproject.wicket.state.AdicaoEnderecoState;
import com.crudproject.wicket.state.ClienteDtoBuilder;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.Arrays;
import java.util.List;

public class AdicionarEnderecoModalPanel extends Panel {

    // Lista estática de UFs removida — agora dropdowns estado/cidade são
    // populados via API do IBGE (ver clientes.js → ibgePopularSelectEstado).

    @SpringBean
    private ClienteService clienteService;

    private final AdicaoEnderecoState state = AdicaoEnderecoState.inicial();
    private AbstractDefaultAjaxBehavior resetBehavior;

    public AdicionarEnderecoModalPanel(String id,
                                       final IModel<ClienteResponseDTO> clienteModel,
                                       final FeedbackPanel feedbackPagina,
                                       final Component... componentesParaAtualizar) {
        super(id);

        final Form<Void> form = new Form<>("formAdicionarEndereco");
        form.setOutputMarkupId(true);

        resetBehavior = new AbstractDefaultAjaxBehavior() {
            @Override
            protected void respond(AjaxRequestTarget target) {
                state.resetar();
                form.clearInput();
                target.add(form);
            }
        };
        form.add(resetBehavior);

        final FeedbackPanel feedbackModal = new FeedbackPanel("feedbackEndereco",
                new ComponentFeedbackMessageFilter(form));
        feedbackModal.setOutputMarkupId(true);
        form.add(feedbackModal);

        ChoiceRenderer<TipoEndereco> renderer = new ChoiceRenderer<TipoEndereco>() {
            @Override public Object getDisplayValue(TipoEndereco t) {
                String n = t.name();
                return n.charAt(0) + n.substring(1).toLowerCase();
            }
            @Override public String getIdValue(TipoEndereco t, int idx) { return t.name(); }
        };

        form.add(new DropDownChoice<>("addEndTipo",
                new PropertyModel<>(state, "tipo"),
                Arrays.asList(TipoEndereco.values()), renderer));
        form.add(new TextField<>("addEndLogradouro",  new PropertyModel<>(state, "logradouro")));
        form.add(new TextField<>("addEndNumero",      new PropertyModel<>(state, "numero")));
        form.add(new TextField<>("addEndComplemento", new PropertyModel<>(state, "complemento")));
        form.add(new TextField<>("addEndBairro",      new PropertyModel<>(state, "bairro")));
        // Estado e Cidade são dropdowns IBGE → HiddenField recebe o valor do JS
        form.add(new HiddenField<>("addEndCidade",    new PropertyModel<>(state, "cidade")));
        form.add(new HiddenField<>("addEndEstado",    new PropertyModel<>(state, "estado")));
        form.add(new TextField<>("addEndCep",         new PropertyModel<>(state, "cep")));
        form.add(new TextField<>("addEndPais",        new PropertyModel<>(state, "pais")));
        form.add(new TextField<>("addEndTelefone",    new PropertyModel<>(state, "telefone")));
        form.add(new CheckBox("addEndPrincipal",      new PropertyModel<>(state, "principal")));

        form.add(new AjaxButton("btnSalvarAdicionarEndereco", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                try {
                    ClienteResponseDTO current = clienteModel.getObject();
                    List<EnderecoDTO> novosEnderecos = ClienteDtoBuilder.toEnderecosDTOs(current.getEnderecos());

                    EnderecoDTO novoEndereco = new EnderecoDTO();
                    novoEndereco.setTipo(state.getTipo());
                    novoEndereco.setLogradouro(state.getLogradouro());
                    novoEndereco.setNumero(state.getNumero());
                    novoEndereco.setComplemento(state.getComplemento());
                    novoEndereco.setBairro(state.getBairro());
                    novoEndereco.setCidade(state.getCidade());
                    novoEndereco.setEstado(state.getEstado());
                    novoEndereco.setCep(state.getCep());
                    novoEndereco.setPais(state.getPais() != null && !state.getPais().isBlank() ? state.getPais() : "Brasil");
                    novoEndereco.setTelefone(state.getTelefone());
                    novoEndereco.setPrincipal(Boolean.TRUE.equals(state.getPrincipal()));

                    if (Boolean.TRUE.equals(state.getPrincipal())) {
                        novosEnderecos.forEach(e -> e.setPrincipal(false));
                    }
                    novosEnderecos.add(novoEndereco);

                    clienteService.atualizar(current.getId(),
                            ClienteDtoBuilder.comEnderecos(current, novosEnderecos));
                    state.resetar();
                    clienteModel.detach();

                    AdicionarEnderecoModalPanel.this.getPage().info("Endereço adicionado com sucesso.");
                    target.add(form, feedbackPagina);
                    for (Component c : componentesParaAtualizar) {
                        target.add(c);
                    }
                    target.appendJavaScript(
                            "var m = bootstrap.Modal.getInstance(document.getElementById('modalAdicionarEndereco'));" +
                                    "if (m) m.hide();");
                } catch (Exception ex) {
                    form.error("Erro ao adicionar endereço: " + ex.getMessage());
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

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        if (resetBehavior != null) {
            response.render(OnDomReadyHeaderItem.forScript(
                    "document.getElementById('modalAdicionarEndereco').addEventListener('hidden.bs.modal', function() {" +
                            "  Wicket.Ajax.ajax({u:'" + resetBehavior.getCallbackUrl() + "'});" +
                            "});"
            ));
        }
    }
}
