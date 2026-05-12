package com.crudproject.service;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsável por gerar relatórios.
 *
 * Fluxo (para cada relatório):
 *   1. Carrega o .jrxml do classpath (src/main/resources/reports/)
 *   2. Compila o XML em memória → JasperReport
 *   3. Preenche com dados via Connection do banco → JasperPrint
 *   4. Exporta como PDF → byte[]
 *
 * O DataSource é fornecido automaticamente pelo Spring — é o mesmo
 * pool de conexões usado pela JPA. Pegamos uma Connection emprestada
 * só durante a geração do relatório.
 */
@Service
public class ReportService {

    @Autowired
    private DataSource dataSource;

    /**
     * Gera o PDF do relatório de lista de todos os clientes ativos.
     *
     * @return bytes do PDF (pronto para devolver ao cliente HTTP)
     */
    public byte[] gerarListaClientesPdf() throws Exception {

        // 1) Carrega o .jrxml do classpath
        InputStream jrxml = new ClassPathResource("reports/clientes-lista.jrxml")
                .getInputStream();

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
}
