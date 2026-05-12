package com.crudproject.controller;

import com.crudproject.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expõe as rotas REST de geração de relatórios.
 *
 * Mapa de rotas:
 *   GET /api/relatorios/clientes/pdf   → lista de todos os clientes ativos em PDF
 */
@RestController
@RequestMapping("/api/relatorios")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Gera e devolve o PDF com a lista de clientes.
     *
     * O retorno é byte[] (binário do PDF) embrulhado em ResponseEntity
     * para que possamos controlar os cabeçalhos HTTP — principalmente:
     *
     *   Content-Type: application/pdf       → diz ao navegador/Apidog que é PDF
     *   Content-Disposition: inline; ...    → "inline" abre direto; "attachment" força download
     */
    @GetMapping("/clientes/pdf")
    public ResponseEntity<byte[]> gerarListaClientesPdf() throws Exception {
        byte[] pdf = reportService.gerarListaClientesPdf();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "clientes.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}
