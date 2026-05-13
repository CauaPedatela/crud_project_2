package com.crudproject.service;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;


//    Service responsável por gerar relatórios.
//    Fluxo (para cada relatório):
//      1. Carrega o .jrxml do classpath (src/main/resources/reports/)
//      2. Compila o XML em memória → JasperReport
//      3. Preenche com dados via Connection do banco → JasperPrint
//      4. Exporta como PDF → byte[]
//    O DataSource é fornecido automaticamente pelo Spring — é o mesmo
//    pool de conexões usado pela JPA. Pegamos uma Connection emprestada
//    só durante a geração do relatório.

@Service
public class ReportService {

    @Autowired
    private DataSource dataSource;

//     Gera o PDF do relatório de lista de todos os clientes ativos.
//     @return bytes do PDF (pronto para devolver ao cliente HTTP)

    public byte[] gerarListaClientesPdf() throws Exception {

        // 1) Carrega o .jrxml do classpath
        InputStream jrxml = new ClassPathResource("reports/clientes-lista.jrxml").getInputStream();

        // 2) Compila o XML em objeto JasperReport
        JasperReport report = JasperCompileManager.compileReport(jrxml);

        // 3) Pega uma conexão do pool e preenche o relatório.
        //    try-with-resources garante que a conexão é devolvida ao pool
        //    mesmo se algo der errado no meio.
        try (Connection conn = dataSource.getConnection()) {

            // Parâmetros: relatórios podem receber valores externos.
            // Por enquanto nenhum — passamos um Map vazio.
            Map<String, Object> parametros = new HashMap<>();

            // Executa o SQL do .jrxml e produz o JasperPrint
            JasperPrint print = JasperFillManager.fillReport(report, parametros, conn);

            // 4) Exporta o JasperPrint como bytes de PDF
            return JasperExportManager.exportReportToPdf(print);
        }
    }

    public byte[] gerarRelatorioClienteEspecifico(Long id) throws Exception {
        // 1) Carrega o novo .jrxml de detalhes
        InputStream jrxml = new ClassPathResource("reports/cliente-detalhes.jrxml")
                .getInputStream();

        JasperReport report = JasperCompileManager.compileReport(jrxml);

        try (Connection conn = dataSource.getConnection()) {
            // 2) AGORA passamos o ID para o Map de parâmetros
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("ID_CLIENTE", id); // O nome "ID_CLIENTE" deve ser igual ao do Jasper

            // 3) O Jasper usará esse ID para filtrar o SQL automaticamente
            JasperPrint print = JasperFillManager.fillReport(report, parametros, conn);

            return JasperExportManager.exportReportToPdf(print);
        }
    }

    // 1. Método para o Excel da Lista Geral
    public byte[] gerarListaClientesExcel() throws Exception {
        InputStream jrxml = new ClassPathResource("reports/clientes-lista-excel.jrxml").getInputStream();
        JasperReport report = JasperCompileManager.compileReport(jrxml);

        try (Connection conn = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(report, new HashMap<>(), conn);
            return exportarParaXlsx(print);
        }
    }

    // 2. Método para o Excel do Cliente Específico
    public byte[] gerarRelatorioClienteEspecificoExcel(Long id) throws Exception {
        // Vamos criar um jrxml focado em Excel para evitar células mescladas
        InputStream jrxml = new ClassPathResource("reports/cliente-detalhes-excel.jrxml").getInputStream();
        JasperReport report = JasperCompileManager.compileReport(jrxml);

        try (Connection conn = dataSource.getConnection()) {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("ID_CLIENTE", id);

            JasperPrint print = JasperFillManager.fillReport(report, parametros, conn);
            return exportarParaXlsx(print);
        }
    }

    // 3. Método auxiliar que faz a mágica do Excel
    private byte[] exportarParaXlsx(JasperPrint print) throws Exception {
        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));

        // Configurações para deixar a planilha com cara de Excel real (não um desenho)
        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setDetectCellType(true); // Reconhece números e datas
        configuration.setRemoveEmptySpaceBetweenRows(true);
        configuration.setRemoveEmptySpaceBetweenColumns(true);
        configuration.setWhitePageBackground(false);
        configuration.setIgnorePageMargins(true); // Remove margens brancas

        exporter.setConfiguration(configuration);
        exporter.exportReport();

        return out.toByteArray();
    }
}
