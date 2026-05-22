package com.crudproject.service.reports;

import org.apache.poi.ss.usermodel.*;

// Fábrica de estilos POI reutilizados pelos builders Excel.
// Todos os métodos criam um novo CellStyle a partir do Workbook recebido —
// cada Workbook tem seu próprio pool de estilos, então eles não podem ser
// compartilhados entre workbooks diferentes.

public final class ExcelEstilos {

    private ExcelEstilos() {}

    // Cabeçalho de coluna: azul escuro, texto branco, negrito, centralizado.
    public static CellStyle header(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        return s;
    }

    // Título de seção (ex: "DADOS DO CLIENTE"): azul claro, negrito.
    public static CellStyle secao(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        return s;
    }

    // Rótulo de campo (ex: "Nome / Razão Social"): cinza claro, negrito.
    public static CellStyle label(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        return s;
    }

    // Linha alternada de dados: fundo azul pálido.
    public static CellStyle alt(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    // Cria uma célula na coluna `col` com o valor e estilo fornecidos.
    // style pode ser null — nenhum estilo será aplicado nesse caso.
    public static void celula(Row row, int col, String valor, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(valor != null ? valor : "");
        if (style != null) cell.setCellStyle(style);
    }

    // Retorna "—" para strings nulas ou vazias (evita células em branco no relatório).
    public static String nvl(String v) {
        return (v != null && !v.isBlank()) ? v : "—";
    }
}
