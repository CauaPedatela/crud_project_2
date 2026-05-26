/*
 * ExcluirClienteModalPanel — modal de confirmação de exclusão de cliente.
 * O JS abrirModalExclusao(id) preenche o HiddenField "idClienteParaExcluir"
 * antes de abrir o modal. No submit, o panel chama clienteService.excluir(id),
 * fecha o modal via JS e dispara target.add(...) nos componentes externos
 * (tabela, contadores e FeedbackPanel da página).
 */
package com.crudproject.wicket.page.listagem;

import com.crudproject.service.ClienteService;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class ExcluirClienteModalPanel extends Panel {

    @SpringBean
    private ClienteService clienteService;

    private Long idParaExcluir;

    public ExcluirClienteModalPanel(String id, FeedbackPanel feedbackPagina,
                                    Component... componentesParaAtualizar) {
        super(id);

        Form<Void> form = new Form<>("formExcluirCliente");

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
                    ExcluirClienteModalPanel.this.getPage().info("Cliente excluído com sucesso.");
                    target.add(feedbackPagina);
                    for (Component c : componentesParaAtualizar) {
                        target.add(c);
                    }
                } catch (Exception ex) {
                    ExcluirClienteModalPanel.this.getPage().error("Erro ao excluir: " + ex.getMessage());
                    target.add(feedbackPagina);
                }
                target.appendJavaScript(
                        "var m = bootstrap.Modal.getInstance(document.getElementById('modalConfirmarExclusao'));" +
                                "if (m) m.hide();");
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> formClicked) {
                target.add(feedbackPagina);
            }
        });

        add(form);
    }

    public Long getIdParaExcluir()        { return idParaExcluir; }
    public void setIdParaExcluir(Long id) { this.idParaExcluir = id; }
}
