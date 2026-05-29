package com.crudproject.service;

import com.crudproject.dto.cliente.ClienteDTO;
import com.crudproject.dto.endereco.EnderecoDTO;
import com.crudproject.model.TipoEndereco;
import com.crudproject.model.TipoPessoa;
import com.crudproject.service.reports.ExcelEstilos;
import com.crudproject.service.validation.DocumentoUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Responsável por duas operações relacionadas à importação em lote:
//   gerarPlanilhaModelo() → retorna os bytes de um .xlsx de exemplo para o usuário baixar e preencher
//   importar(InputStream) → lê o .xlsx, valida e salva cada linha; acumula erros por linha

@Service
public class ClienteImportacaoService {

    // @Lazy evita dependência circular (ClienteService → via ReportService → nada, mas por convenção do projeto)
    @Lazy
    @Autowired
    private ClienteService clienteService;

    // ─── Índices de colunas do modelo (A=0, B=1, …) ──────────────────────────
    private static final int COL_TIPO_PESSOA  = 0;
    private static final int COL_NOME         = 1;
    private static final int COL_CPF_CNPJ     = 2;
    private static final int COL_RG_IE        = 3;
    private static final int COL_DATA         = 4;
    private static final int COL_EMAIL        = 5;
    private static final int COL_ATIVO        = 6;
    private static final int COL_CEP          = 7;
    private static final int COL_TIPO_END     = 8;
    private static final int COL_LOGRADOURO   = 9;
    private static final int COL_NUMERO       = 10;
    private static final int COL_COMPLEMENTO  = 11;
    private static final int COL_BAIRRO       = 12;
    private static final int COL_CIDADE       = 13;
    private static final int COL_UF           = 14;
    private static final int COL_PAIS         = 15;
    private static final int COL_TELEFONE     = 16;
    private static final int TOTAL_COLS       = 17;

    private static final String[] CABECALHOS = {
        "Tipo Pessoa (FISICA/JURIDICA)",
        "Nome / Razão Social",
        "CPF / CNPJ",
        "RG / Inscrição Estadual",
        "Data Nascimento/Fundação (dd/MM/yyyy)",
        "E-mail",
        "Ativo (SIM/NAO)",
        "CEP",
        "Tipo Endereço (RESIDENCIAL/COMERCIAL)",
        "Logradouro",
        "Número (ou SN)",
        "Complemento",
        "Bairro",
        "Cidade",
        "UF",
        "País",
        "Telefone (opcional)"
    };

    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ────────────────────────────────────────────────────────────────────────
    // Planilha Modelo
    // ────────────────────────────────────────────────────────────────────────

    public byte[] gerarPlanilhaModelo() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Modelo - Importação");

            // Colunas CPF/CNPJ e CEP formatadas como texto para não perder zeros à esquerda
            DataFormat fmt = wb.createDataFormat();
            CellStyle colTexto = wb.createCellStyle();
            colTexto.setDataFormat(fmt.getFormat("@"));
            sheet.setDefaultColumnStyle(COL_CPF_CNPJ, colTexto);
            sheet.setDefaultColumnStyle(COL_CEP, colTexto);

            CellStyle headerStyle    = ExcelEstilos.header(wb);
            CellStyle exemploStyle   = estiloExemplo(wb);
            CellStyle instrucaoStyle = estiloInstrucao(wb);

            // Linha 0 — instrução geral (célula mesclada abrangendo todas as colunas)
            Row linhaInstrucao = sheet.createRow(0);
            linhaInstrucao.setHeightInPoints(40);
            Cell cInstrucao = linhaInstrucao.createCell(0);
            cInstrucao.setCellValue(
                "PLANILHA MODELO — Preencha a partir da linha 3. " +
                "A linha 2 (em cinza) é apenas um exemplo e pode ser apagada. " +
                "Mantenha CPF/CNPJ e CEP formatados como Texto no Excel para não perder zeros à esquerda."
            );
            cInstrucao.setCellStyle(instrucaoStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, TOTAL_COLS - 1));

            // Linha 1 — cabeçalho de colunas
            Row linhaHeader = sheet.createRow(1);
            for (int i = 0; i < CABECALHOS.length; i++) {
                Cell c = linhaHeader.createCell(i);
                c.setCellValue(CABECALHOS[i]);
                c.setCellStyle(headerStyle);
            }

            // Linha 2 — exemplo preenchido (CPF fictício matematicamente válido para fins de demonstração)
            String[] exemplo = {
                "FISICA", "João da Silva", "529.982.247-25", "1234567", "15/06/1985",
                "joao.silva@email.com", "SIM",
                "01001-000", "RESIDENCIAL", "Praça da Sé", "1", "Apto 101",
                "Centro", "São Paulo", "SP", "Brasil", "(11) 98765-4321"
            };
            Row linhaExemplo = sheet.createRow(2);
            for (int i = 0; i < exemplo.length; i++) {
                Cell c = linhaExemplo.createCell(i);
                c.setCellValue(exemplo[i]);
                c.setCellStyle(exemploStyle);
            }

            // Auto-size com padding extra para legibilidade
            for (int i = 0; i < TOTAL_COLS; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1024);
            }

            wb.write(out);
            return out.toByteArray();
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Importação
    // ────────────────────────────────────────────────────────────────────────

    // Importa o .xlsx em modo "tudo ou nada":
    //   Passo 1 — lê e valida TODAS as linhas sem persistir nada; acumula erros.
    //   Passo 2 — só executa se o passo 1 não encontrou nenhum erro; salva todos os DTOs.
    // Se qualquer linha falhar na validação, nenhum registro é salvo e os erros são retornados.
    public ImportacaoResultado importar(InputStream inputStream) throws Exception {
        List<ClienteDTO> dtosValidos = new ArrayList<>();
        List<String> erros = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(inputStream)) {
            Sheet sheet = wb.getSheetAt(0);
            int primeiraLinhaDados = detectarPrimeiraLinhaDados(sheet);

            // Rastreia CPF/CNPJ já vistos na planilha (normalizado, sem máscara) → número da linha.
            // A checagem acontece ANTES de qualquer validação de negócio para garantir que
            // uma duplicata interna seja sinalizada com mensagem clara ("já aparece na linha X"),
            // e não mascarada por uma mensagem genérica de unicidade do banco.
            Map<String, Integer> cpfsVistos = new HashMap<>();

            // Passo 1: parseia e valida TODAS as linhas sem salvar nada
            for (int i = primeiraLinhaDados; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || linhaVazia(row)) continue;

                try {
                    ClienteDTO dto = parsearLinha(row);

                    // 1a) Duplicata interna — verifica ANTES das regras de negócio
                    // DocumentoUtil.limparFormatacao remove pontos/traços/barras para comparar
                    // o documento bruto, independente de como o usuário digitou na planilha
                    String cpfNormalizado = DocumentoUtil.limparFormatacao(dto.getCpfCnpj());
                    if (cpfsVistos.containsKey(cpfNormalizado)) {
                        throw new RuntimeException(
                            "CPF/CNPJ já aparece na linha " + cpfsVistos.get(cpfNormalizado) + " desta planilha.");
                    }
                    cpfsVistos.put(cpfNormalizado, i + 1);

                    // 1b) Validação de negócio: normalização completa + regras (Caelum Stella, unicidade no banco, etc.)
                    clienteService.validarParaImportacao(dto);

                    dtosValidos.add(dto);
                } catch (Exception ex) {
                    erros.add("Linha " + (i + 1) + ": " + ex.getMessage());
                }
            }

            int totalLinhas = dtosValidos.size() + erros.size();

            // Se houver qualquer erro, aborta sem salvar nada
            if (!erros.isEmpty()) {
                return new ImportacaoResultado(0, erros.size(), erros, totalLinhas);
            }

            // Passo 2: todas as linhas são válidas — persiste
            int sucessos = 0;
            for (ClienteDTO dto : dtosValidos) {
                clienteService.salvar(dto);
                sucessos++;
            }

            return new ImportacaoResultado(sucessos, 0, Collections.emptyList(), totalLinhas);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Helpers privados
    // ────────────────────────────────────────────────────────────────────────

    // Se a planilha veio do modelo (célula A1 começa com "PLANILHA MODELO"),
    // os dados reais começam na linha 2 (linha 0 = instrução, linha 1 = cabeçalho).
    // Caso contrário, assume cabeçalho na linha 0 e dados a partir da linha 1.
    private int detectarPrimeiraLinhaDados(Sheet sheet) {
        Row row0 = sheet.getRow(0);
        if (row0 != null && getCellValue(row0, 0).startsWith("PLANILHA MODELO")) {
            return 2;
        }
        return 1;
    }

    private ClienteDTO parsearLinha(Row row) {
        // — Tipo Pessoa —
        TipoPessoa tipoPessoa = parsearEnum(TipoPessoa.class,
            getCellValue(row, COL_TIPO_PESSOA), "Tipo de Pessoa (use FISICA ou JURIDICA)");

        // — Campos do cliente —
        String nome     = getCellValue(row, COL_NOME);
        String cpfCnpj  = getCellValue(row, COL_CPF_CNPJ);
        String rgIe     = getCellValue(row, COL_RG_IE);
        String dataStr  = getCellValue(row, COL_DATA);
        String email    = getCellValue(row, COL_EMAIL);
        String ativoStr = getCellValue(row, COL_ATIVO).trim().toUpperCase();
        Boolean ativo   = "SIM".equals(ativoStr) || "S".equals(ativoStr) || "TRUE".equals(ativoStr);

        LocalDate dataNascimento = null;
        if (!dataStr.isBlank()) {
            try {
                dataNascimento = LocalDate.parse(dataStr.trim(), FMT_DATA);
            } catch (Exception e) {
                throw new RuntimeException("Data inválida '" + dataStr + "'. Use o formato dd/MM/yyyy.");
            }
        }

        // — Campos do endereço —
        String cep        = getCellValue(row, COL_CEP);
        String tipoEndStr = getCellValue(row, COL_TIPO_END);
        String logradouro = getCellValue(row, COL_LOGRADOURO);
        String numero     = getCellValue(row, COL_NUMERO);
        String compl      = getCellValue(row, COL_COMPLEMENTO);
        String bairro     = getCellValue(row, COL_BAIRRO);
        String cidade     = getCellValue(row, COL_CIDADE);
        String uf         = getCellValue(row, COL_UF);
        String pais       = getCellValue(row, COL_PAIS);
        String telefone   = getCellValue(row, COL_TELEFONE);

        if (pais.isBlank()) pais = "Brasil";

        TipoEndereco tipoEndereco = parsearEnum(TipoEndereco.class,
            tipoEndStr.isBlank() ? "RESIDENCIAL" : tipoEndStr,
            "Tipo de Endereço (use RESIDENCIAL ou COMERCIAL)");

        EnderecoDTO endereco = new EnderecoDTO();
        endereco.setTipo(tipoEndereco);
        endereco.setCep(cep);
        endereco.setLogradouro(logradouro);
        endereco.setNumero(numero.isBlank() ? "SN" : numero);
        endereco.setComplemento(compl.isBlank() ? null : compl);
        endereco.setBairro(bairro);
        endereco.setCidade(cidade);
        endereco.setEstado(uf);
        endereco.setPais(pais);
        endereco.setTelefone(telefone.isBlank() ? null : telefone);
        endereco.setPrincipal(true);

        ClienteDTO dto = new ClienteDTO();
        dto.setTipoPessoa(tipoPessoa);
        dto.setNome(nome);
        dto.setCpfCnpj(cpfCnpj);
        dto.setRgInscricaoEstadual(rgIe);
        dto.setDataNascimento(dataNascimento);
        dto.setEmail(email);
        dto.setAtivo(ativo);
        dto.setEnderecos(Collections.singletonList(endereco));

        return dto;
    }

    private <T extends Enum<T>> T parsearEnum(Class<T> enumClass, String valor, String nomeCampo) {
        try {
            return Enum.valueOf(enumClass, valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(nomeCampo + " inválido: '" + valor + "'.");
        }
    }

    // Extrai o valor de uma célula como String, independente do tipo POI.
    // Células de data são convertidas para o formato dd/MM/yyyy.
    // Números inteiros têm o .0 removido (ex: 12345678901.0 → "12345678901").
    private String getCellValue(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return FMT_DATA.format(cell.getLocalDateTimeCellValue().toLocalDate());
                }
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            case BOOLEAN:
                return cell.getBooleanCellValue() ? "SIM" : "NAO";
            case FORMULA:
                try { return cell.getStringCellValue().trim(); }
                catch (Exception e1) {
                    try { return String.valueOf((long) cell.getNumericCellValue()); }
                    catch (Exception e2) { return ""; }
                }
            default:
                return "";
        }
    }

    private boolean linhaVazia(Row row) {
        for (int i = 0; i < TOTAL_COLS; i++) {
            if (!getCellValue(row, i).isBlank()) return false;
        }
        return true;
    }

    private CellStyle estiloExemplo(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font f = wb.createFont();
        f.setItalic(true);
        s.setFont(f);
        return s;
    }

    private CellStyle estiloInstrucao(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setWrapText(true);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        return s;
    }
}
