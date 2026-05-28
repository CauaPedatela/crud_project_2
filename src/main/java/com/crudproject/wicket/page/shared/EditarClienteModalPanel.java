/*
 * EditarClienteModalPanel — modal de edição de cliente compartilhado pela
 * Listagem e por Detalhes. Os campos editáveis são nome, e-mail, RG/IE
 * (visível só para PJ) e ativo. CPF/CNPJ e tipo permanecem imutáveis após
 * o cadastro (regra de negócio do ClienteService).
 *
 * O JS abrirModalEdicao(btn) pré-preenche os campos via data-* attributes
 * antes de abrir o modal. No submit, o panel recarrega o cliente do banco
 * para preservar os campos imutáveis e a lista de endereços, sobrescreve
 * apenas o que mudou e chama clienteService.atualizar().
 */
package com.crudproject.wicket.page.shared;

import com.crudproject.dto.cliente.ClienteDTO;
import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.model.TipoPessoa;
import com.crudproject.service.ClienteService;
import com.crudproject.wicket.state.ClienteDtoBuilder;
import com.crudproject.wicket.state.EdicaoClienteState;
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
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class EditarClienteModalPanel extends Panel {

    @SpringBean
    private ClienteService clienteService;

    private final EdicaoClienteState state = new EdicaoClienteState();

    public EditarClienteModalPanel(String id, FeedbackPanel feedbackPagina,
                                   Component... componentesParaAtualizar) {
        super(id);

        Form<Void> form = new Form<>("formEditarCliente");

        FeedbackPanel feedbackModal = new FeedbackPanel("feedbackEditar",
                new ComponentFeedbackMessageFilter(form));
        feedbackModal.setOutputMarkupId(true);
        form.add(feedbackModal);

        HiddenField<Long> hiddenId = new HiddenField<>("hiddenEditClienteId",
                new PropertyModel<Long>(state, "idCliente"), Long.class);
        hiddenId.setMarkupId("editClienteId");
        hiddenId.setOutputMarkupId(true);
        form.add(hiddenId);

        // Nome agora é editável (anteriormente era apenas exibição read-only).
        // O JS preenche este campo com data-nome ao abrir o modal.
        TextField<String> tfNome = new TextField<>("formNome",
                new PropertyModel<>(state, "nome"));
        tfNome.setMarkupId("editNome");
        tfNome.setOutputMarkupId(true);
        form.add(tfNome);

        TextField<String> tfEmail = new TextField<>("formEmail",
                new PropertyModel<>(state, "email"));
        tfEmail.setMarkupId("editEmail");
        tfEmail.setOutputMarkupId(true);
        form.add(tfEmail);

        TextField<String> tfRgIe = new TextField<>("formRgIe",
                new PropertyModel<>(state, "rgIe"));
        tfRgIe.setMarkupId("editRgIe");
        tfRgIe.setOutputMarkupId(true);
        form.add(tfRgIe);

        CheckBox cbAtivo = new CheckBox("formAtivo",
                new PropertyModel<>(state, "ativo"));
        cbAtivo.setMarkupId("editAtivo");
        cbAtivo.setOutputMarkupId(true);
        form.add(cbAtivo);

        form.add(new AjaxButton("btnSalvarEdicao", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                if (state.getIdCliente() == null) return;
                try {
                    ClienteResponseDTO current = clienteService.buscarPorId(state.getIdCliente());

                    ClienteDTO dto = new ClienteDTO();
                    dto.setTipoPessoa(current.getTipoPessoa());
                    // Nome agora vem do state (editável). Se vier vazio, o validator do
                    // service vai recusar com mensagem clara — não bloqueamos aqui.
                    dto.setNome(state.getNome());
                    dto.setCpfCnpj(current.getCpfCnpj());
                    dto.setDataNascimento(current.getDataNascimento());
                    dto.setEmail(state.getEmail());
                    dto.setAtivo(state.getAtivo());
                    dto.setRgInscricaoEstadual(
                            current.getTipoPessoa() == TipoPessoa.JURIDICA
                                    ? state.getRgIe()
                                    : current.getRgInscricaoEstadual());
                    dto.setEnderecos(ClienteDtoBuilder.toEnderecosDTOs(current.getEnderecos()));

                    clienteService.atualizar(state.getIdCliente(), dto);

                    EditarClienteModalPanel.this.getPage().info("Cliente atualizado com sucesso.");
                    target.add(feedbackPagina);
                    for (Component c : componentesParaAtualizar) {
                        target.add(c);
                    }
                    target.appendJavaScript(
                            "var m = bootstrap.Modal.getInstance(document.getElementById('modalEditarCliente'));" +
                                    "if (m) m.hide();");
                } catch (Exception ex) {
                    form.error("Erro ao atualizar: " + ex.getMessage());
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
