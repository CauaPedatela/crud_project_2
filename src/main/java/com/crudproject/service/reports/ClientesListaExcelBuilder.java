package com.crudproject.service.reports;

import com.crudproject.dto.cliente.ClienteResponseDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.List;

// Gera o arquivo .xlsx com a lista geral de clientes ativos.
// Recebe a lista já filtrada e retorna os bytes do arquivo pronto para download.

public final class ClientesListaExcelBuilder {

    private static final String[] COLUNAS = {
        "ID", "Nome / Razão Social", "E-mail", "CPF / CNPJ", "Tipo Pessoa", "Ativo"
    };

    private ClientesListaExcelBuilder() {}

    public static byte[] gerar(List<ClienteResponseDTO> clientes) throws Exception {
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Clientes");

            CellStyle headerStyle = ExcelEstilos.header(wb);
            CellStyle altStyle    = ExcelEstilos.alt(wb);

            // ── Cabeçalho ────────────────────────────────────────────────────
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < COLUNAS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(COLUNAS[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Dados ─────────────────────────────────────────────────────────
            int rowNum = 1;
            for (ClienteResponseDTO c : clientes) {
                Row row      = sheet.createRow(rowNum);
                CellStyle st = rowNum % 2 == 0 ? altStyle : null;

                ExcelEstilos.celula(row, 0, c.getId() != null ? String.valueOf(c.getId()) : "", st);
                ExcelEstilos.celula(row, 1, ExcelEstilos.nvl(c.getNome()),    st);
                ExcelEstilos.celula(row, 2, ExcelEstilos.nvl(c.getEmail()),   st);
                ExcelEstilos.celula(row, 3, ExcelEstilos.nvl(c.getCpfCnpj()), st);
                ExcelEstilos.celula(row, 4,
                        c.getTipoPessoa() != null ? c.getTipoPessoa().name() : "", st);
                ExcelEstilos.celula(row, 5,
                        Boolean.TRUE.equals(c.getAtivo()) ? "Sim" : "Não", st);

                rowNum++;
            }

            for (int i = 0; i < COLUNAS.length; i++) sheet.autoSizeColumn(i);

            wb.write(out);
            return out.toByteArray();
        }
    }
}
