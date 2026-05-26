/*
 * EditarEnderecoModalPanel — modal de edição de endereço.
 *
 * Campos editáveis: número, complemento, telefone e status "principal".
 * Os demais (logradouro, bairro, cidade, estado, CEP, país, tipo) são
 * exibidos só como referência (logradouro tem display read-only no HTML).
 *
 * O JS abrirModalEditarEndereco(btn) pré-preenche os campos editáveis via
 * data-* attributes. No submit, busca o cliente, atualiza o endereço alvo,
 * aplica a regra de "principal único" e chama clienteService.atualizar().
 */
package com.crudproject.wicket.page.detalhes;

import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.dto.endereco.EnderecoDTO;
import com.crudproject.service.ClienteService;
import com.crudproject.wicket.state.ClienteDtoBuilder;
import com.crudproject.wicket.state.EdicaoEnderecoState;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;

public class EditarEnderecoModalPanel extends Panel {

    @SpringBean
    private ClienteService clienteService;

    private final EdicaoEnderecoState state = new EdicaoEnderecoState();

    public EditarEnderecoModalPanel(String id,
                                    final IModel<ClienteResponseDTO> clienteModel,
                                    final FeedbackPanel feedbackPagina,
                                    final Component... componentesParaAtualizar) {
        super(id);

        Form<Void> form = new Form<>("formEditarEndereco");

        final FeedbackPanel feedbackModal = new FeedbackPanel("feedbackEditarEndereco",
                new ComponentFeedbackMessageFilter(form));
        feedbackModal.setOutputMarkupId(true);
        form.add(feedbackModal);

        HiddenField<Long> hiddenId = new HiddenField<>("hiddenEditarEnderecoId",
                new PropertyModel<Long>(state, "idEndereco"), Long.class);
        hiddenId.setMarkupId("editEnderecoId");
        hiddenId.setOutputMarkupId(true);
        form.add(hiddenId);

        TextField<String> tfNumero = new TextField<>("editEndNumero",
                new PropertyModel<>(state, "numero"));
        tfNumero.setMarkupId("editEndNumero");
        tfNumero.setOutputMarkupId(true);
        form.add(tfNumero);

        TextField<String> tfComplemento = new TextField<>("editEndComplemento",
                new PropertyModel<>(state, "complemento"));
        tfComplemento.setMarkupId("editEndComplemento");
        tfComplemento.setOutputMarkupId(true);
        form.add(tfComplemento);

        TextField<String> tfTelefone = new TextField<>("editEndTelefone",
                new PropertyModel<>(state, "telefone"));
        tfTelefone.setMarkupId("editEndTelefone");
        tfTelefone.setOutputMarkupId(true);
        form.add(tfTelefone);

        CheckBox cbPrincipal = new CheckBox("editEndPrincipal",
                new PropertyModel<>(state, "principal"));
        cbPrincipal.setMarkupId("editEndPrincipal");
        cbPrincipal.setOutputMarkupId(true);
        form.add(cbPrincipal);

        form.add(new AjaxButton("btnSalvarEditarEndereco", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                if (state.getIdEndereco() == null) return;
                try {
                    ClienteResponseDTO current = clienteModel.getObject();
                    List<EnderecoDTO> novosEnderecos = ClienteDtoBuilder.toEnderecosDTOs(current.getEnderecos());

                    EnderecoDTO alvo = novosEnderecos.stream()
                            .filter(e -> state.getIdEndereco().equals(e.getId()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Endereço não encontrado."));

                    alvo.setNumero(state.getNumero());
                    alvo.setComplemento(state.getComplemento());
                    alvo.setTelefone(state.getTelefone());

                    if (Boolean.TRUE.equals(state.getPrincipal())) {
                        novosEnderecos.forEach(e -> e.setPrincipal(false));
                        alvo.setPrincipal(true);
                    } else {
                        boolean temOutroPrincipal = novosEnderecos.stream()
                                .anyMatch(e -> !state.getIdEndereco().equals(e.getId())
                                        && Boolean.TRUE.equals(e.getPrincipal()));
                        if (!temOutroPrincipal && Boolean.TRUE.equals(alvo.getPrincipal())) {
                            throw new RuntimeException(
                                    "Não é possível desmarcar o único endereço principal. " +
                                            "Defina outro endereço como principal antes.");
                        }
                        alvo.setPrincipal(false);
                    }

                    clienteService.atualizar(current.getId(),
                            ClienteDtoBuilder.comEnderecos(current, novosEnderecos));
                    clienteModel.detach();

                    EditarEnderecoModalPanel.this.getPage().info("Endereço atualizado com sucesso.");
                    target.add(feedbackPagina);
                    for (Component c : componentesParaAtualizar) {
                        target.add(c);
                    }
                    target.appendJavaScript(
                            "var m = bootstrap.Modal.getInstance(document.getElementById('modalEditarEndereco'));" +
                                    "if (m) m.hide();");
                } catch (Exception ex) {
                    form.error("Erro ao editar endereço: " + ex.getMessage());
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
}
