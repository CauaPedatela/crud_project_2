package com.crudproject.service.reports;

import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.dto.endereco.EnderecoResponseDTO;
import com.crudproject.service.validation.MascaraUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Gera o arquivo .xlsx com os dados detalhados de um único cliente.
// Estrutura: seção "DADOS DO CLIENTE" (label | valor) seguida de
// seção "LISTA DE ENDEREÇOS" (tabela com cabeçalho + linhas).

public final class ClienteDetalhesExcelBuilder {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] COLUNAS_ENDERECOS = {
        "ID", "Logradouro", "Nº", "Complemento", "Bairro",
        "Cidade", "UF", "CEP", "Telefone", "Principal?", "Tipo"
    };

    private ClienteDetalhesExcelBuilder() {}

    public static byte[] gerar(ClienteResponseDTO c) throws Exception {
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Cliente");

            CellStyle secaoStyle  = ExcelEstilos.secao(wb);
            CellStyle labelStyle  = ExcelEstilos.label(wb);
            CellStyle headerStyle = ExcelEstilos.header(wb);
            CellStyle altStyle    = ExcelEstilos.alt(wb);

            int rowNum = 0;

            // ── Seção: Dados do Cliente ───────────────────────────────────────
            rowNum = secao(sheet, rowNum, "DADOS DO CLIENTE", secaoStyle, 1);
            rowNum = dado(sheet, rowNum, labelStyle, "Nome / Razão Social",     ExcelEstilos.nvl(c.getNome()));
            rowNum = dado(sheet, rowNum, labelStyle, "E-mail",                  ExcelEstilos.nvl(c.getEmail()));
            rowNum = dado(sheet, rowNum, labelStyle, "CPF / CNPJ",              ExcelEstilos.nvl(MascaraUtil.formatarCpfCnpj(c.getCpfCnpj())));
            rowNum = dado(sheet, rowNum, labelStyle, "RG / Inscrição Estadual", ExcelEstilos.nvl(c.getRgInscricaoEstadual()));
            rowNum = dado(sheet, rowNum, labelStyle, "Tipo de Pessoa",
                    c.getTipoPessoa() != null ? c.getTipoPessoa().name() : "");
            rowNum = dado(sheet, rowNum, labelStyle, "Nascimento / Fundação",
                    c.getDataNascimento() != null ? c.getDataNascimento().format(FMT) : "—");
            rowNum = dado(sheet, rowNum, labelStyle, "Ativo",
                    Boolean.TRUE.equals(c.getAtivo()) ? "Sim" : "Não");

            rowNum++; // linha vazia separando as seções

            // ── Seção: Endereços ──────────────────────────────────────────────
            rowNum = secao(sheet, rowNum, "LISTA DE ENDEREÇOS", secaoStyle, COLUNAS_ENDERECOS.length - 1);

            Row headerRow = sheet.createRow(rowNum++);
            for (int i = 0; i < COLUNAS_ENDERECOS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(COLUNAS_ENDERECOS[i]);
                cell.setCellStyle(headerStyle);
            }

            List<EnderecoResponseDTO> enderecos = c.getEnderecos();
            if (enderecos != null) {
                for (EnderecoResponseDTO e : enderecos) {
                    Row row      = sheet.createRow(rowNum);
                    CellStyle st = rowNum % 2 == 0 ? altStyle : null;

                    ExcelEstilos.celula(row, 0,  e.getId() != null ? String.valueOf(e.getId()) : "", st);
                    ExcelEstilos.celula(row, 1,  ExcelEstilos.nvl(e.getLogradouro()),  st);
                    ExcelEstilos.celula(row, 2,  ExcelEstilos.nvl(e.getNumero()),      st);
                    ExcelEstilos.celula(row, 3,  ExcelEstilos.nvl(e.getComplemento()), st);
                    ExcelEstilos.celula(row, 4,  ExcelEstilos.nvl(e.getBairro()),      st);
                    ExcelEstilos.celula(row, 5,  ExcelEstilos.nvl(e.getCidade()),      st);
                    ExcelEstilos.celula(row, 6,  ExcelEstilos.nvl(e.getEstado()),      st);
                    ExcelEstilos.celula(row, 7,  ExcelEstilos.nvl(MascaraUtil.formatarCep(e.getCep())),       st);
                    ExcelEstilos.celula(row, 8,  ExcelEstilos.nvl(MascaraUtil.formatarTelefone(e.getTelefone())), st);
                    ExcelEstilos.celula(row, 9,
                            Boolean.TRUE.equals(e.getPrincipal()) ? "Sim" : "Não", st);
                    ExcelEstilos.celula(row, 10,
                            e.getTipo() != null ? e.getTipo().name() : "", st);

                    rowNum++;
                }
            }

            for (int i = 0; i < COLUNAS_ENDERECOS.length; i++) sheet.autoSizeColumn(i);

            wb.write(out);
            return out.toByteArray();
        }
    }

    // ── Helpers de layout ─────────────────────────────────────────────────────

    // Cria a linha de cabeçalho de seção e mescla da coluna 0 até lastCol.
    private static int secao(Sheet sheet, int rowNum, String titulo, CellStyle estilo, int lastCol) {
        Row row  = sheet.createRow(rowNum);
        Cell c   = row.createCell(0);
        c.setCellValue(titulo);
        c.setCellStyle(estilo);
        if (lastCol > 0) {
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, lastCol));
        }
        return rowNum + 1;
    }

    // Cria a linha "label (col 0) | valor (col 1)".
    private static int dado(Sheet sheet, int rowNum, CellStyle labelStyle, String label, String valor) {
        Row row  = sheet.createRow(rowNum);
        Cell lbl = row.createCell(0);
        lbl.setCellValue(label);
        lbl.setCellStyle(labelStyle);
        row.createCell(1).setCellValue(valor);
        return rowNum + 1;
    }
}
