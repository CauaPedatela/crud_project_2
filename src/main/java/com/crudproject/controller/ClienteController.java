package com.crudproject.controller;

import com.crudproject.dto.cliente.ClienteDTO;
import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expõe as rotas REST de Cliente.
 *
 * Como o modelo é unificado (cliente + endereços numa única transação),
 * todas as operações de Endereço também passam por aqui — via PUT do cliente,
 * que faz a sincronização da lista de endereços.
 *
 * Mapa de rotas:
 *   POST   /api/clientes        → cadastrar (cliente + endereços)
 *   GET    /api/clientes        → listar todos
 *   GET    /api/clientes/{id}   → buscar por id
 *   PUT    /api/clientes/{id}   → atualizar (com sync de endereços)
 *   DELETE /api/clientes/{id}   → excluir
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    /**
     * Cadastra um novo cliente com seus endereços iniciais.
     *
     * Body: ClienteDTO (com pelo menos 1 endereço na lista enderecos).
     * Retorno: ClienteResponseDTO com id gerado, dataCadastro e endereços com ids.
     */
    @PostMapping
    public ClienteResponseDTO cadastrar(@RequestBody ClienteDTO dto) {
        return clienteService.salvar(dto);
    }

    /**
     * Lista todos os clientes cadastrados.
     */
    @GetMapping
    public List<ClienteResponseDTO> listarTodos() {
        return clienteService.buscarTodos();
    }

    /**
     * Busca um cliente pelo id.
     */
    @GetMapping("/{id}")
    public ClienteResponseDTO buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id);
    }

    /**
     * Atualiza o cliente e sincroniza a lista de endereços.
     *
     * Body: ClienteDTO (mesma estrutura do POST).
     * Lógica de sincronização dos endereços:
     *   endereço com id existente  →  atualiza
     *   endereço sem id            →  cria novo
     *   endereço que sumiu         →  deleta
     */
    @PutMapping("/{id}")
    public ClienteResponseDTO atualizar(@PathVariable Long id,
                                        @RequestBody ClienteDTO dto) {
        return clienteService.atualizar(id, dto);
    }

    /**
     * Exclui um cliente (e seus endereços, via cascade).
     */
    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        clienteService.excluir(id);
    }
}
