package com.crudproject.controller;

import com.crudproject.dto.endereco.EnderecoAtualizacaoDTO;
import com.crudproject.dto.endereco.EnderecoCadastroDTO;
import com.crudproject.dto.endereco.EnderecoResponseDTO;
import com.crudproject.service.EnderecoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expõe as rotas REST de Endereço.
 *
 * Endereços de um cliente existente são criados/editados aqui.
 * Os endereços iniciais (no momento de cadastrar o cliente) vão
 * embedded no POST /api/clientes — não usam essas rotas.
 *
 * Mapa de rotas:
 *   POST   /api/enderecos                       → cadastrar (vinculando a um cliente)
 *   GET    /api/enderecos/{id}                  → buscar por id
 *   GET    /api/enderecos/cliente/{clienteId}   → listar endereços de um cliente
 *   PUT    /api/enderecos/{id}                  → atualizar
 *   DELETE /api/enderecos/{id}                  → excluir
 *   PUT    /api/enderecos/{id}/principal        → definir como principal
 */
@RestController
@RequestMapping("/api/enderecos")
public class EnderecoController {

    @Autowired
    private EnderecoService enderecoService;

    /**
     * Cadastra um novo endereço vinculado a um cliente existente.
     *
     * Body: EnderecoCadastroDTO com clienteId preenchido.
     */
    @PostMapping
    public EnderecoResponseDTO cadastrar(@RequestBody EnderecoCadastroDTO dto) {
        return enderecoService.salvar(dto);
    }

    /**
     * Busca um endereço pelo id.
     */
    @GetMapping("/{id}")
    public EnderecoResponseDTO buscarPorId(@PathVariable Long id) {
        return enderecoService.buscarPorId(id);
    }

    /**
     * Lista todos os endereços de um cliente específico.
     */
    @GetMapping("/cliente/{clienteId}")
    public List<EnderecoResponseDTO> listarPorCliente(@PathVariable Long clienteId) {
        return enderecoService.buscarPorCliente(clienteId);
    }

    /**
     * Atualiza os dados de um endereço existente.
     *
     * Body: EnderecoAtualizacaoDTO (sem clienteId).
     * Não é possível mover o endereço para outro cliente — o DTO em si
     * já não permite, deixando o contrato explícito.
     */
    @PutMapping("/{id}")
    public EnderecoResponseDTO atualizar(@PathVariable Long id,
                                         @RequestBody EnderecoAtualizacaoDTO dto) {
        return enderecoService.atualizar(id, dto);
    }

    /**
     * Exclui um endereço.
     */
    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        enderecoService.excluir(id);
    }

    /**
     * Promove esse endereço a "principal" do cliente, desmarcando os outros.
     *
     * Rota: PUT /api/enderecos/{id}/principal
     * (sem body — o id na URL é tudo que o Service precisa)
     */
    @PutMapping("/{id}/principal")
    public void definirComoPrincipal(@PathVariable Long id) {
        enderecoService.definirComoPrincipal(id);
    }
}
