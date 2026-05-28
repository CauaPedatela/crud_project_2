package com.crudproject.service;

import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.service.reports.ClienteDetalhesExcelBuilder;
import com.crudproject.service.validation.MascaraUtil;
import com.crudproject.service.reports.ClientesListaExcelBuilder;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Orquestra a geração de relatórios.
// PDFs  → JasperReports compilado em memória a partir dos .jrxml em classpath:/reports/.
// Excel → builders dedicados em com.crudproject.service.reports (Apache POI puro).

@Service
public class ReportService {

    @Autowired
    private DataSource dataSource;

    // @Lazy evita dependência circular potencial ao adicionar ClienteService aqui.
    @Lazy
    @Autowired
    private ClienteService clienteService;

    // ────────────────────────────────────────────────────────────────────────
    // PDF — JasperReports
    // ────────────────────────────────────────────────────────────────────────

    // Lista geral (todos os clientes, inclusive inativos) — usada pelo controller REST.
    // buscarComFiltros com todos os parâmetros nulos retorna a lista completa do banco
    // (equivale ao antigo buscarTodos, que foi removido por ser redundante).
    public byte[] gerarListaClientesPdf() throws Exception {
        return gerarPdfDaLista(clienteService.buscarComFiltros(null, null, null, null, null));
    }

    // Lista filtrada — usada pela tela Wicket, recebe os filtros da tela.
    public byte[] gerarListaClientesPdf(String termo, String filtroAtivo, String filtroTipo,
                                        String dataInicio, String dataFim) throws Exception {
        return gerarPdfDaLista(
                clienteService.buscarComFiltros(termo, filtroAtivo, filtroTipo, dataInicio, dataFim));
    }

    // Gera o PDF da lista a partir de uma lista de clientes já resolvida (sem tocar
    // no banco aqui). A lista vira um JRMapCollectionDataSource: o Jasper itera os
    // mapas e busca cada field (nome, cpf_cnpj, tipo_pessoa) pela chave do mapa.
    private byte[] gerarPdfDaLista(List<ClienteResponseDTO> clientes) throws Exception {
        InputStream jrxml = new ClassPathResource("reports/clientes-lista.jrxml").getInputStream();
        JasperReport report = JasperCompileManager.compileReport(jrxml);

        List<Map<String, ?>> linhas = new ArrayList<>();
        for (ClienteResponseDTO c : clientes) {
            Map<String, Object> linha = new HashMap<>();
            linha.put("id",         c.getId() != null ? String.valueOf(c.getId()) : "");
            linha.put("nome",       c.getNome() != null ? c.getNome() : "");
            linha.put("email",      c.getEmail() != null ? c.getEmail() : "");
            linha.put("cpf_cnpj",   MascaraUtil.formatarCpfCnpj(c.getCpfCnpj()));
            linha.put("tipo_pessoa", c.getTipoPessoa() != null ? c.getTipoPessoa().name() : "");
            linha.put("ativo",      Boolean.TRUE.equals(c.getAtivo()) ? "Sim" : "Não");
            linhas.add(linha);
        }

        JasperPrint print = JasperFillManager.fillReport(
                report, new HashMap<String, Object>(), new JRMapCollectionDataSource(linhas));
        return JasperExportManager.exportReportToPdf(print);
    }

    public byte[] gerarRelatorioClienteEspecifico(Long id) throws Exception {
        InputStream jrxml = new ClassPathResource("reports/cliente-detalhes.jrxml").getInputStream();
        JasperReport report = JasperCompileManager.compileReport(jrxml);

        try (Connection conn = dataSource.getConnection()) {
            Map<String, Object> params = new HashMap<>();
            params.put("ID_CLIENTE", id);
            JasperPrint print = JasperFillManager.fillReport(report, params, conn);
            return JasperExportManager.exportReportToPdf(print);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Excel — Apache POI (lógica de célula encapsulada nos builders)
    // ────────────────────────────────────────────────────────────────────────

    // Lista geral (todos os clientes, inclusive inativos) — usada pelo controller REST.
    // Mesma justificativa do PDF acima: buscarComFiltros(null,...) = todos do banco.
    public byte[] gerarListaClientesExcel() throws Exception {
        return ClientesListaExcelBuilder.gerar(
                clienteService.buscarComFiltros(null, null, null, null, null));
    }

    public byte[] gerarListaClientesExcel(String termo, String filtroAtivo, String filtroTipo,
                                          String dataInicio, String dataFim) throws Exception {
        List<ClienteResponseDTO> clientes = clienteService.buscarComFiltros(
                termo, filtroAtivo, filtroTipo, dataInicio, dataFim);
        return ClientesListaExcelBuilder.gerar(clientes);
    }

    public byte[] gerarRelatorioClienteEspecificoExcel(Long id) throws Exception {
        ClienteResponseDTO cliente = clienteService.buscarPorId(id);
        return ClienteDetalhesExcelBuilder.gerar(cliente);
    }
}
