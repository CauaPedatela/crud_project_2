package com.crudproject.controller;

import com.crudproject.dto.cliente.ClienteDTO;
import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.service.ClienteImportacaoService;
import com.crudproject.service.ClienteService;
import com.crudproject.service.ImportacaoResultado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

//    Como o modelo é unificado (cliente + endereços numa única transação),
//    todas as operações de Endereço também passam por aqui
//    Mapa de rotas:
//      POST   /api/clientes        → cadastrar (cliente + endereços)
//      GET    /api/clientes        → listar todos
//      GET    /api/clientes/{id}   → buscar por id
//      PUT    /api/clientes/{id}   → atualizar (com sync de endereços)
//      DELETE /api/clientes/{id}   → excluir

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteImportacaoService clienteImportacaoService;

//     Cadastra um novo cliente com seus endereços iniciais.
//     Body: ClienteDTO (com pelo menos 1 endereço na lista enderecos).
//     Retorno: ClienteResponseDTO com id gerado, dataCadastro e endereços com ids.

    @PostMapping
    public ClienteResponseDTO cadastrar(@RequestBody ClienteDTO dto) {
        return clienteService.salvar(dto);
    }

//     Lista todos os clientes cadastrados.

    @GetMapping
    public List<ClienteResponseDTO> listarTodos() {
        return clienteService.buscarTodos();
    }

//     Busca um cliente pelo id.

    @GetMapping("/{id}")
    public ClienteResponseDTO buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id);
    }

//     Atualiza o cliente e sincroniza a lista de endereços.
//
//     Body: ClienteDTO (mesma estrutura do POST).
//     Lógica de sincronização dos endereços:
//       endereço com id existente  →  atualiza
//       endereço sem id            →  cria novo
//       endereço que sumiu         →  deleta

    @PutMapping("/{id}")
    public ClienteResponseDTO atualizar(@PathVariable Long id, @RequestBody ClienteDTO dto) {
        return clienteService.atualizar(id, dto);
    }

//     Exclui um cliente (e seus endereços, via cascade).

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        clienteService.excluir(id);
    }

//     Busca clientes com filtros opcionais — usado pelo Angular para a barra de filtros.
//     Todos os parâmetros são opcionais: enviar apenas os que o usuário preencheu.
//       termo      → busca por nome, CPF/CNPJ ou e-mail (like %termo%)
//       ativo      → "true", "false" ou vazio (todos)
//       tipo       → "FISICA", "JURIDICA" ou vazio (todos)
//       dataInicio → data de cadastro inicial, formato yyyy-MM-dd
//       dataFim    → data de cadastro final, formato yyyy-MM-dd

    @GetMapping("/buscar")
    public List<ClienteResponseDTO> buscarComFiltros(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) String ativo,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim) {
        return clienteService.buscarComFiltros(termo, ativo, tipo, dataInicio, dataFim);
    }

//     Faz upload de um arquivo .xlsx e importa os clientes em lote.
//     Retorna um resumo: quantos foram importados com sucesso e quais linhas tiveram erro.
//     O body da requisição deve ser multipart/form-data com o campo "arquivo".

    @PostMapping("/importar")
    public ResponseEntity<ImportacaoResultado> importar(
            @RequestParam("arquivo") MultipartFile arquivo) throws Exception {
        ImportacaoResultado resultado = clienteImportacaoService.importar(arquivo.getInputStream());
        return ResponseEntity.ok(resultado);
    }

//     Gera e devolve a planilha modelo (.xlsx) para o usuário preencher e depois importar.
//     O Angular pode oferecer um botão "Baixar modelo" que chama este endpoint.

    @GetMapping("/modelo-planilha")
    public ResponseEntity<byte[]> downloadModeloPlanilha() throws Exception {
        byte[] bytes = clienteImportacaoService.gerarPlanilhaModelo();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        headers.setContentDispositionFormData("attachment", "modelo_importacao.xlsx");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
