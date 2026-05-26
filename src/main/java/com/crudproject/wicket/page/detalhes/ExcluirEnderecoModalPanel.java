/*
 * ExcluirEnderecoModalPanel — modal de confirmação de exclusão de endereço.
 *
 * O JS abrirModalExcluirEndereco(btn) preenche o HiddenField com o id do
 * endereço a excluir. No submit, valida regras de negócio (não pode excluir
 * o principal, deve restar pelo menos 1) e dispara clienteService.atualizar()
 * com a nova lista de endereços.
 */
package com.crudproject.wicket.page.detalhes;

import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.dto.endereco.EnderecoDTO;
import com.crudproject.dto.endereco.EnderecoResponseDTO;
import com.crudproject.service.ClienteService;
import com.crudproject.wicket.state.ClienteDtoBuilder;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;
import java.util.stream.Collectors;

public class ExcluirEnderecoModalPanel extends Panel {

    @SpringBean
    private ClienteService clienteService;

    private Long idEnderecoParaExcluir;

    public ExcluirEnderecoModalPanel(String id,
                                     final IModel<ClienteResponseDTO> clienteModel,
                                     final FeedbackPanel feedbackPagina,
                                     final Component... componentesParaAtualizar) {
        super(id);

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
                    ClienteResponseDTO current = clienteModel.getObject();
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

                    List<EnderecoDTO> novosEnderecos = ClienteDtoBuilder.toEnderecosDTOs(enderecos)
                            .stream()
                            .filter(e -> !idEnderecoParaExcluir.equals(e.getId()))
                            .collect(Collectors.toList());

                    clienteService.atualizar(current.getId(),
                            ClienteDtoBuilder.comEnderecos(current, novosEnderecos));
                    clienteModel.detach();

                    ExcluirEnderecoModalPanel.this.getPage().info("Endereço excluído com sucesso.");
                    target.add(feedbackPagina);
                    for (Component c : componentesParaAtualizar) {
                        target.add(c);
                    }
                } catch (Exception ex) {
                    ExcluirEnderecoModalPanel.this.getPage().error("Erro ao excluir endereço: " + ex.getMessage());
                    target.add(feedbackPagina);
                }
                target.appendJavaScript(
                        "var m = bootstrap.Modal.getInstance(document.getElementById('modalConfirmarExcluirEndereco'));" +
                                "if (m) m.hide();");
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> formClicked) {
                target.add(feedbackPagina);
            }
        });

        add(form);
    }

    public Long getIdEnderecoParaExcluir()        { return idEnderecoParaExcluir; }
    public void setIdEnderecoParaExcluir(Long id) { this.idEnderecoParaExcluir = id; }
}
