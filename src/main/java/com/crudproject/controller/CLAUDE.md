# controller/ — REST API

> Leia também o `CLAUDE.md` da raiz antes de qualquer alteração aqui.

## Responsabilidade
Camada REST. Existe em **paralelo** ao Wicket — **não foi descontinuada nem está "morta"**. É o que o **frontend Angular consome em produção** (PDF seção 4 e 6.1).

```
Angular (frontend SPA) ─┐
                        ├─→ @RestController → ClienteService → ...
Testes REST / Apidog   ─┘
```

## ⚠️ Regra forte: Controllers são FINOS

```java
@PostMapping
public ResponseEntity<ClienteResponseDTO> criar(@RequestBody ClienteDTO dto) {
    // ✅ delegação direta ao service
    return ResponseEntity.ok(clienteService.salvar(dto));
}
```

**Não colocar validação no controller** (PDF seção 5.1 — "validações no serviço"). Só recebe DTO, chama Service, devolve `ResponseEntity`. O tratamento global de erro vive em `config/GlobalExceptionHandler.java` (`@ControllerAdvice`) — não no método.

## Pode alterar sem perguntar
- **Adicionar endpoints novos** seguindo o padrão existente.
- Ajustar status codes para refletir melhor a semântica HTTP.
- Adicionar headers de resposta úteis (Content-Disposition em relatórios, etc).
- Adicionar `@CrossOrigin` configurado corretamente para o Angular.

## ⛔ EXIGE confirmação antes de alterar
- **REMOVER QUALQUER ENDPOINT EXISTENTE.** Mesmo que pareça não usado pelo Wicket. O Angular vai consumir todos.
- Mudar path de endpoint existente (ex: `/api/clientes` → `/clientes/v2`). Quebra contrato.
- Mudar verbo HTTP de um endpoint (POST → PUT).
- Mudar formato do JSON de saída.
- Adicionar autenticação/autorização — fora do escopo do desafio.

## Rotas atuais (não remover sem confirmação — o Angular depende de todas)

```
POST   /api/clientes                              criar
GET    /api/clientes                              listar todos
GET    /api/clientes/buscar                       listar paginado + filtros
GET    /api/clientes/contadores                   total e ativos (só count)
GET    /api/clientes/{id}                         buscar por id
PUT    /api/clientes/{id}                         atualizar (sync de endereços)
DELETE /api/clientes/{id}                         excluir
POST   /api/clientes/importar                     upload de planilha Excel
GET    /api/clientes/modelo-planilha              modelo de planilha para download

GET    /api/relatorios/clientes/pdf               lista em PDF
GET    /api/relatorios/cliente/detalhes/pdf?id=   detalhes em PDF
GET    /api/relatorios/clientes/excel             lista em Excel
GET    /api/relatorios/cliente/detalhes/excel?id= detalhes em Excel
```

## Convenções
- `@RestController` + `@RequestMapping("/api/...")`.
- Métodos com nome em pt-BR (`criar`, `listar`, `buscarPorId`, `atualizar`, `excluir`).
- Comentário em pt-BR acima de cada endpoint explicando o que faz.
