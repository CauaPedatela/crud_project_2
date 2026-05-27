package com.crudproject.controller;

import com.crudproject.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//   Expõe as rotas REST de geração de relatórios.
//
//   Mapa de rotas:
//   GET /api/relatorios/clientes/pdf   → lista de clientes em PDF (aceita filtros opcionais)
//   GET /api/relatorios/clientes/excel → lista de clientes em Excel (aceita filtros opcionais)
//   GET /api/relatorios/cliente/detalhes/pdf?id=   → detalhes de um cliente em PDF
//   GET /api/relatorios/cliente/detalhes/excel?id= → detalhes de um cliente em Excel

@RestController
@RequestMapping("/api/relatorios")
public class ReportController {

    @Autowired
    private ReportService reportService;

//     Gera e devolve o PDF com a lista de clientes.
//     O retorno é byte[] (binário do PDF) embrulhado em ResponseEntity
//     para que possamos controlar os cabeçalhos HTTP — principalmente:
//       Content-Type: application/pdf       → diz ao navegador/Apidog que é PDF
//       Content-Disposition: inline; ...    → "inline" abre direto; "attachment" força download

    @GetMapping("/clientes/pdf")
    public ResponseEntity<byte[]> gerarListaClientesPdf(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) String ativo,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim) throws Exception {

        // Se algum filtro foi enviado, usa o overload filtrado; caso contrário, lista geral.
        byte[] pdf = temFiltro(termo, ativo, tipo, dataInicio, dataFim)
                ? reportService.gerarListaClientesPdf(termo, ativo, tipo, dataInicio, dataFim)
                : reportService.gerarListaClientesPdf();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "clientes.pdf");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/cliente/detalhes/pdf")
    public ResponseEntity<byte[]> gerarRelatorioClienteDetalhes(@RequestParam Long id) throws Exception {
        // Chama o service passando o ID recebido na URL
        byte[] pdf = reportService.gerarRelatorioClienteEspecifico(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        // Nome do arquivo sugere que é um relatório individual
        headers.setContentDispositionFormData("inline", "cliente_detalhes_" + id + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/clientes/excel")
    public ResponseEntity<byte[]> gerarListaClientesExcel(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) String ativo,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim) throws Exception {

        // Mesma lógica do PDF: usa overload filtrado se houver filtro.
        byte[] excel = temFiltro(termo, ativo, tipo, dataInicio, dataFim)
                ? reportService.gerarListaClientesExcel(termo, ativo, tipo, dataInicio, dataFim)
                : reportService.gerarListaClientesExcel();

        HttpHeaders headers = new HttpHeaders();
        // MIME Type oficial para arquivos .xlsx
        headers.set(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        // Força o download do arquivo
        headers.setContentDispositionFormData("attachment", "lista_clientes.xlsx");

        return ResponseEntity.ok().headers(headers).body(excel);
    }

    @GetMapping("/cliente/detalhes/excel")
    public ResponseEntity<byte[]> gerarRelatorioClienteDetalhesExcel(@RequestParam Long id) throws Exception {
        byte[] excel = reportService.gerarRelatorioClienteEspecificoExcel(id);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        headers.setContentDispositionFormData("attachment", "cliente_detalhes_" + id + ".xlsx");

        return ResponseEntity.ok().headers(headers).body(excel);
    }

    // Retorna true se pelo menos um filtro foi enviado pelo cliente.
    // Mantemos esta lógica em um único lugar para evitar duplicação entre PDF e Excel.
    private boolean temFiltro(String termo, String ativo, String tipo,
                              String dataInicio, String dataFim) {
        return (termo      != null && !termo.isBlank())
            || (ativo      != null && !ativo.isBlank())
            || (tipo       != null && !tipo.isBlank())
            || (dataInicio != null && !dataInicio.isBlank())
            || (dataFim    != null && !dataFim.isBlank());
    }
}
