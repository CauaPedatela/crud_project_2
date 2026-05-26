/*
 * ImportarExcelModalPanel — modal de importação de clientes via planilha.
 *
 * Fluxo:
 *   - Link "Baixar Planilha Modelo" → download da planilha de template
 *   - Upload de .xlsx + clique em "Importar" → service processa e devolve
 *     ImportacaoResultado (sucessos, erros, mensagens). Painel de resultados
 *     fica oculto até a primeira importação ser processada.
 *   - Após sucesso, a tabela e os contadores da página-pai são re-renderizados
 *     via componentesParaAtualizar.
 */
package com.crudproject.wicket.page.listagem;

import com.crudproject.service.ClienteImportacaoService;
import com.crudproject.service.ImportacaoResultado;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;

import java.util.ArrayList;
import java.util.List;

public class ImportarExcelModalPanel extends Panel {

    @SpringBean
    private ClienteImportacaoService importacaoService;

    private ImportacaoResultado resultadoImportacao;
    private FileUploadField     arquivoExcel;

    public ImportarExcelModalPanel(String id, FeedbackPanel feedbackPagina,
                                   Component... componentesParaAtualizar) {
        super(id);

        final Form<Void> form = new Form<>("formImportar");
        form.setMultiPart(true);
        form.setOutputMarkupId(true);

        final FeedbackPanel feedbackModal = new FeedbackPanel("feedbackImportar",
                new ComponentFeedbackMessageFilter(form));
        feedbackModal.setOutputMarkupId(true);
        form.add(feedbackModal);

        arquivoExcel = new FileUploadField("arquivoExcel");
        form.add(arquivoExcel);

        form.add(new Link<Void>("linkDownloadModelo") {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("target", "_blank");
            }
            @Override
            public void onClick() {
                try {
                    byte[] bytes = importacaoService.gerarPlanilhaModelo();
                    baixarArquivo(bytes,
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "modelo-importacao-clientes.xlsx");
                } catch (Exception e) {
                    error("Erro ao gerar planilha modelo: " + e.getMessage());
                }
            }
        });

        final WebMarkupContainer painelResultado = new WebMarkupContainer("painelResultado");
        painelResultado.setOutputMarkupId(true);
        painelResultado.setOutputMarkupPlaceholderTag(true);
        painelResultado.setVisible(false);

        painelResultado.add(new Label("resultadoSucessos", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return resultadoImportacao != null
                        ? String.valueOf(resultadoImportacao.getSucessos()) : "0";
            }
        }));
        painelResultado.add(new Label("resultadoErros", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return resultadoImportacao != null
                        ? String.valueOf(resultadoImportacao.getErros()) : "0";
            }
        }));

        WebMarkupContainer containerListaErros = new WebMarkupContainer("containerListaErros") {
            @Override public boolean isVisible() {
                return resultadoImportacao != null && resultadoImportacao.temErros();
            }
        };
        containerListaErros.setOutputMarkupId(true);
        containerListaErros.setOutputMarkupPlaceholderTag(true);

        containerListaErros.add(new ListView<String>("listaErros",
                new AbstractReadOnlyModel<List<String>>() {
                    @Override public List<String> getObject() {
                        if (resultadoImportacao == null) return new ArrayList<>();
                        return resultadoImportacao.getMensagensErro();
                    }
                }) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("mensagemErro", item.getModel()));
            }
        });

        painelResultado.add(containerListaErros);
        form.add(painelResultado);

        form.add(new AjaxButton("btnImportar", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                FileUpload upload = arquivoExcel.getFileUpload();
                if (upload == null || upload.getSize() == 0) {
                    form.error("Selecione um arquivo .xlsx para importar.");
                    target.add(feedbackModal);
                    return;
                }
                try {
                    resultadoImportacao = importacaoService.importar(upload.getInputStream());
                    painelResultado.setVisible(true);
                    target.add(painelResultado, feedbackModal);
                    for (Component c : componentesParaAtualizar) {
                        target.add(c);
                    }
                    if (resultadoImportacao.getSucessos() > 0) {
                        ImportarExcelModalPanel.this.getPage().info(
                                resultadoImportacao.getSucessos() + " cliente(s) importado(s) com sucesso.");
                        target.add(feedbackPagina);
                    }
                } catch (Exception e) {
                    form.error("Erro ao processar o arquivo: " + e.getMessage());
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

    private void baixarArquivo(final byte[] bytes, final String contentType, String fileName) {
        AbstractResourceStreamWriter stream = new AbstractResourceStreamWriter() {
            @Override
            public void write(java.io.OutputStream output) throws java.io.IOException {
                output.write(bytes);
            }
            @Override
            public String getContentType() {
                return contentType;
            }
        };
        ResourceStreamRequestHandler handler =
                new ResourceStreamRequestHandler(stream, fileName);
        handler.setContentDisposition(ContentDisposition.ATTACHMENT);
        getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
    }
}
