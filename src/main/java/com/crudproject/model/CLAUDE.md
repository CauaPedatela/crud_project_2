# model/ — Entidades JPA

> Leia também o `CLAUDE.md` da raiz antes de qualquer alteração aqui.

## Responsabilidade
Entidades JPA mapeadas para o schema MySQL. `Cliente` é **unificado** (PF+PJ via enum `TipoPessoa`) — não dividir em duas classes. `Endereco` tem `@ManyToOne` para `Cliente` (FK `cliente_id`).

Esta camada é **PURA persistência**. Sem regra de negócio, sem validação, sem chamadas a service/repository.

## Pode alterar sem perguntar
- Adicionar getters/setters faltantes.
- Adicionar métodos auxiliares de leitura que não tocam o banco (`isPessoaFisica()`, `getDocumentoFormatado()` etc) — comentando em pt-BR.
- Adicionar campos novos **opcionais** (com `nullable = true` ou default seguro) que não quebrem registros já existentes no banco.
- Ajustar `toString()`, `equals()`, `hashCode()`.

## ⛔ EXIGE confirmação antes de alterar
- **Mudar nome de tabela** (`@Table(name = ...)`) ou de coluna (`@Column(name = ...)`).
- **Adicionar ou remover relacionamentos** (`@OneToMany`, `@ManyToOne`, `@ManyToMany`).
- Alterar `nullable`, `unique`, `length` ou tipo de coluna em campo já existente.
- Renomear valores de enum (`TipoPessoa`, `TipoEndereco`) — quebra dados já salvos.
- Mudar estratégia de `@Id` / `@GeneratedValue`.
- Remover campos.

## ⚠️ Por que isso importa
O Hibernate roda com `ddl-auto` (provavelmente `update` ou `create-update`). **Qualquer mudança aqui altera o banco real automaticamente na próxima inicialização do Spring Boot.** Em produção isso seria uma migração; aqui é um efeito colateral silencioso.

## Convenções específicas
- Anotações JPA acima do campo, não acima do getter.
- `@Column(nullable = false)` para campos obrigatórios — não delegar isso só ao validator.
- `cascade = CascadeType.ALL, orphanRemoval = true` em `Cliente.enderecos` (já está assim — não mudar).
- Comentário em pt-BR acima de cada campo explicando para que serve.
