/*
 * RodapeAcoesPanel — barra inferior da Listagem com os botões de ação:
 * - "Adicionar Cliente" e "Importar via Excel": só abrem os modais Bootstrap
 * via data-bs-toggle (sem submit Wicket; o trabalho fica nos panels dos modais)
 * - "Relatório (PDF)" e "Exportar Excel": Links Wicket que codificam o
 * FiltroState atual na URL e disparam o download
 */
package com.crudproject.wicket.page.listagem;

import com.crudproject.service.ReportService;
import com.crudproject.wicket.page.FiltroState;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.apache.wicket.util.time.Duration; // IMPORT NOVO

public class RodapeAcoesPanel extends Panel {

    @SpringBean
    private ReportService reportService;

    private final Link<Void> linkRelatorioPdf;
    private final Link<Void> linkRelatorioExcel;

    public RodapeAcoesPanel(String id, FiltroState filtros) {
        super(id);

        linkRelatorioPdf = new Link<Void>("linkRelatorioPdf") {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                String href = tag.getAttribute("href");
                if (href != null) {
                    tag.put("href", href + querystring(filtros));
                }
                // MÁGICA 1: Injeta um timestamp no momento do clique, burlando o cache do navegador!
                tag.put("onclick", "this.href = this.href + '&_cb=' + new Date().getTime();");
            }
            @Override
            public void onClick() {
                IRequestParameters p = RequestCycle.get().getRequest().getQueryParameters();
                try {
                    byte[] bytes = reportService.gerarListaClientesPdf(
                            p.getParameterValue("termo").toString(""),
                            p.getParameterValue("ativo").toString(""),
                            p.getParameterValue("tipo").toString(""),
                            p.getParameterValue("de").toString(""),
                            p.getParameterValue("ate").toString(""));
                    baixarArquivo(bytes, "application/pdf", "relatorio-clientes.pdf");
                } catch (Exception e) {
                    error("Erro ao gerar PDF: " + e.getMessage());
                }
            }
        };
        linkRelatorioPdf.setOutputMarkupId(true);
        add(linkRelatorioPdf);

        linkRelatorioExcel = new Link<Void>("linkRelatorioExcel") {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                String href = tag.getAttribute("href");
                if (href != null) {
                    tag.put("href", href + querystring(filtros));
                }
                // MÁGICA 1: Injeta o timestamp aqui também
                tag.put("onclick", "this.href = this.href + '&_cb=' + new Date().getTime();");
            }
            @Override
            public void onClick() {
                IRequestParameters p = RequestCycle.get().getRequest().getQueryParameters();
                try {
                    byte[] bytes = reportService.gerarListaClientesExcel(
                            p.getParameterValue("termo").toString(""),
                            p.getParameterValue("ativo").toString(""),
                            p.getParameterValue("tipo").toString(""),
                            p.getParameterValue("de").toString(""),
                            p.getParameterValue("ate").toString(""));
                    baixarArquivo(bytes,
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "relatorio-clientes.xlsx");
                } catch (Exception e) {
                    error("Erro ao gerar Excel: " + e.getMessage());
                }
            }
        };
        linkRelatorioExcel.setOutputMarkupId(true);
        add(linkRelatorioExcel);
    }

    public Link<Void> getLinkRelatorioPdf()   { return linkRelatorioPdf; }
    public Link<Void> getLinkRelatorioExcel() { return linkRelatorioExcel; }

    private static String querystring(FiltroState f) {
        return "&termo=" + enc(f.getTermoBusca())
                + "&ativo=" + enc(f.getFiltroAtivo())
                + "&tipo="  + enc(f.getFiltroTipo())
                + "&de="    + enc(f.getDataCriacaoInicio())
                + "&ate="   + enc(f.getDataCriacaoFim());
    }

    private static String enc(String v) {
        if (v == null || v.isEmpty()) return "";
        try {
            return java.net.URLEncoder.encode(v, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return v;
        }
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

        // MÁGICA 2: Diz explicitamente para o navegador NUNCA fazer cache deste arquivo
        handler.setCacheDuration(Duration.NONE);

        getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
    }
}