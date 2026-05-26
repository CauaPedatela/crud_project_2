package com.crudproject.wicket.page.shared;

import com.crudproject.service.ReportService;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.apache.wicket.model.PropertyModel;

public class RelatorioClienteModalPanel extends Panel {

    @SpringBean
    private ReportService reportService;

    private Long idClienteSelecionado;

    public RelatorioClienteModalPanel(String id) {
        super(id);

        Form<Void> form = new Form<>("formRelatorio");

        HiddenField<Long> hiddenId = new HiddenField<>("hiddenClienteId",
                new PropertyModel<Long>(this, "idClienteSelecionado"), Long.class);
        hiddenId.setMarkupId("reportClienteId");
        hiddenId.setOutputMarkupId(true);
        form.add(hiddenId);

        form.add(new Button("btnPdf") {
            @Override
            public void onSubmit() {
                if (idClienteSelecionado == null) return;
                try {
                    byte[] bytes = reportService.gerarRelatorioClienteEspecifico(idClienteSelecionado);
                    baixarArquivo(bytes, "application/pdf", "detalhes-cliente-" + idClienteSelecionado + ".pdf");
                } catch (Exception e) {
                    error("Erro ao gerar PDF: " + e.getMessage());
                }
            }
        });

        form.add(new Button("btnExcel") {
            @Override
            public void onSubmit() {
                if (idClienteSelecionado == null) return;
                try {
                    byte[] bytes = reportService.gerarRelatorioClienteEspecificoExcel(idClienteSelecionado);
                    baixarArquivo(bytes, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "detalhes-cliente-" + idClienteSelecionado + ".xlsx");
                } catch (Exception e) {
                    error("Erro ao gerar Excel: " + e.getMessage());
                }
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
            public String getContentType() { return contentType; }
        };
        ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(stream, fileName);
        handler.setContentDisposition(ContentDisposition.ATTACHMENT);
        getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
    }
}