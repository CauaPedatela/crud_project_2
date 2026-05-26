# dto/ — Data Transfer Objects

> Leia também o `CLAUDE.md` da raiz antes de qualquer alteração aqui.

## Responsabilidade
DTOs de transferência entre camadas:
- **`ClienteDTO`, `EnderecoDTO`** → entrada (POST/PUT — vindo do controller REST ou do form Wicket).
- **`ClienteResponseDTO`, `EnderecoResponseDTO`** → saída (GET — devolvido pelo controller REST e usado pelas páginas Wicket).

Conversão DTO ↔ Entity é feita em `mapper/`, **nunca aqui** dentro do DTO.

## ⚠️ Regra inviolável

**TODOS os DTOs DEVEM `implements Serializable`** — sem exceção.

Motivo: o Wicket guarda modelos na sessão e serializa entre requisições. Um DTO não-serializável quebra a sessão silenciosamente (NotSerializableException em runtime, geralmente bem depois do bug ser introduzido).

```java
public class ClienteDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    // campos...
}
```

Se adicionar um campo de tipo complexo, esse tipo também precisa ser `Serializable`. Em dúvida, prefira tipos primitivos / `String` / `LocalDateTime`.

## Pode alterar sem perguntar
- Adicionar campos novos (mantendo `Serializable`).
- Ajustar/adicionar anotações de validação (`@NotBlank`, `@Email`, `@Pattern`) — desde que a validação efetiva continue no `ClienteValidator`.
- Adicionar construtores, getters/setters, `toString()`.

## ⛔ EXIGE confirmação antes de alterar
- **Mudar tipo de um campo existente** — quebra o contrato REST e o binding do form Wicket.
- **Remover campos** — pode quebrar serialização de objetos antigos em sessão.
- Renomear campos (mudaria o JSON da API REST).
- Adicionar lógica de negócio dentro do DTO (não é lugar dela).
- Remover `implements Serializable` de qualquer classe.

## Convenções
- Pacotes separados por entidade: `dto/cliente/`, `dto/endereco/`.
- Comentário em pt-BR acima de cada campo.
- `serialVersionUID` explícito.
