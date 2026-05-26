# controller/ — REST API

> Leia também o `CLAUDE.md` da raiz antes de qualquer alteração aqui.

## Responsabilidade
Camada REST. Existe em **paralelo** ao Wicket — **não foi descontinuada nem está "morta"**. Ela será o ponto de integração da fase Angular futura (PDF seção 4 e 6.1).

```
Angular (futuro)  ─┐
                   ├─→ @RestController → ClienteService → ...
REST tests         ─┘
```

## ⚠️ Regra forte: Controllers são FINOS

```java
@PostMapping
public ResponseEntity<ClienteResponseDTO> criar(@RequestBody ClienteDTO dto) {
    // ✅ delegação direta ao service
    return ResponseEntity.ok(clienteService.salvar(dto));
}
```

**Não colocar validação no controller** (PDF seção 5.1 — "validações no serviço"). Só recebe DTO, chama Service, devolve `ResponseEntity`. Tratamento de erro vai num `@ControllerAdvice` (caso seja criado), não no método.

## Pode alterar sem perguntar
- **Adicionar endpoints novos** (ex: `POST /api/clientes/importar` ainda falta).
- Ajustar status codes para refletir melhor a semântica HTTP.
- Adicionar headers de resposta úteis (Content-Disposition em relatórios, etc).
- Adicionar `@CrossOrigin` configurado corretamente para o Angular.

## ⛔ EXIGE confirmação antes de alterar
- **REMOVER QUALQUER ENDPOINT EXISTENTE.** Mesmo que pareça não usado pelo Wicket. O Angular vai consumir todos.
- Mudar path de endpoint existente (ex: `/api/clientes` → `/clientes/v2`). Quebra contrato.
- Mudar verbo HTTP de um endpoint (POST → PUT).
- Mudar formato do JSON de saída.
- Adicionar autenticação/autorização — fora do escopo do desafio.

## Rotas atuais (não remover sem confirmação)

```
POST   /api/clientes
GET    /api/clientes
GET    /api/clientes/{id}
PUT    /api/clientes/{id}
DELETE /api/clientes/{id}
GET    /api/relatorios/clientes/pdf
GET    /api/relatorios/cliente/detalhes/pdf?id=
GET    /api/relatorios/clientes/excel
GET    /api/relatorios/cliente/detalhes/excel?id=
```

## Convenções
- `@RestController` + `@RequestMapping("/api/...")`.
- Métodos com nome em pt-BR (`criar`, `listar`, `buscarPorId`, `atualizar`, `excluir`).
- Comentário em pt-BR acima de cada endpoint explicando o que faz.
