package com.crudproject.controller;

import com.crudproject.dto.cliente.ClienteAtualizacaoDTO;
import com.crudproject.dto.cliente.ClienteCadastroDTO;
import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expõe as rotas REST de Cliente.
 *
 * Todas as requisições passam por aqui antes de chegar no Service.
 * O Controller não tem regra de negócio — ele só traduz HTTP em
 * chamadas Java e devolve os DTOs como JSON.
 *
 * Mapa de rotas:
 *   POST   /api/clientes        → cadastrar
 *   GET    /api/clientes        → listar todos
 *   GET    /api/clientes/{id}   → buscar por id
 *   PUT    /api/clientes/{id}   → atualizar
 *   DELETE /api/clientes/{id}   → excluir
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    /**
     * Cadastra um novo cliente com seus endereços.
     *
     * Rota: POST /api/clientes
     * Body: ClienteCadastroDTO (com pelo menos 1 endereço embedded)
     * Retorno: ClienteResponseDTO com id gerado
     */
    @PostMapping
    public ClienteResponseDTO cadastrar(@RequestBody ClienteCadastroDTO dto) {
        return clienteService.salvar(dto);
    }

    /**
     * Lista todos os clientes cadastrados.
     *
     * Rota: GET /api/clientes
     * Retorno: lista de ClienteResponseDTO (com endereços)
     */
    @GetMapping
    public List<ClienteResponseDTO> listarTodos() {
        return clienteService.buscarTodos();
    }

    /**
     * Busca um cliente pelo id.
     *
     * Rota: GET /api/clientes/{id}
     * Retorno: ClienteResponseDTO ou erro 500 (futuramente 404) se não existir.
     */
    @GetMapping("/{id}")
    public ClienteResponseDTO buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id);
    }

    /**
     * Atualiza dados do cliente.
     *
     * Rota: PUT /api/clientes/{id}
     * Body: ClienteAtualizacaoDTO (sem tipoPessoa, sem enderecos)
     *
     * Endereços são gerenciados pelo EnderecoController.
     * tipoPessoa é imutável após o cadastro.
     */
    @PutMapping("/{id}")
    public ClienteResponseDTO atualizar(@PathVariable Long id,
                                        @RequestBody ClienteAtualizacaoDTO dto) {
        return clienteService.atualizar(id, dto);
    }

    /**
     * Exclui um cliente.
     *
     * Rota: DELETE /api/clientes/{id}
     * Retorno: 200 OK com corpo vazio (futuramente 204 No Content).
     */
    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        clienteService.excluir(id);
    }
}
