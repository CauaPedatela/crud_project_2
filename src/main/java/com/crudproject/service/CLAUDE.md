# service/ — Orquestração e Regras de Negócio

> Leia também o `CLAUDE.md` da raiz antes de qualquer alteração aqui.

## Responsabilidade
Camada que **orquestra** a aplicação. Recebe DTOs, valida, normaliza, persiste, devolve.

- **`ClienteService`** — fluxo principal de CRUD de cliente.
- **`EnderecoSincronizador`** — lógica do PUT (sincroniza endereços recebidos com os do banco).
- **`ReportService`** — geração de PDF (Jasper) e Excel (POI).
- **`ClienteImportacaoService`** — importação por planilha Excel.
- **`service/validation/`** — validações puras (tem seu próprio CLAUDE.md).
- **`service/reports/`** — builders de Excel (delegação interna do ReportService).

> **Validações de negócio ficam AQUI, nunca no controller** (PDF seção 5.1).

## ⚠️ Pipeline obrigatório de `salvar()` e `atualizar()`

```
1. ClienteService.normalizarDados(dto)   ← remove máscaras (CPF, telefone, CEP)
2. ClienteValidator.validar(dto)         ← regras de negócio (Caelum Stella matemático)
3. ClienteRepository.save(entity)        ← persistência
```

**Nunca inverter esta ordem.** Se o validator receber CPF formatado (com pontos/traço), o Caelum Stella falha. Se persistir antes de validar, dado inconsistente entra no banco.

## ⚠️ Imutabilidade no UPDATE

`ClienteMapper.updateEntity()` **ignora propositalmente** `cpfCnpj`, `tipoPessoa` e `dataCadastro` ao atualizar uma entidade existente. Não mexer nisso — é a aplicação da regra "dados sensíveis imutáveis" (PDF seção 7).

## Pode alterar sem perguntar
- Adicionar métodos novos (com comentário em pt-BR explicando intenção).
- Ajustar lógica interna privada de métodos existentes.
- Melhorar mensagens de erro/log.
- Adicionar logs com SLF4J em pontos relevantes.

## ⛔ EXIGE confirmação antes de alterar
- **Assinatura pública de qualquer método de `ClienteService`** — controller REST e páginas Wicket dependem.
- Trocar o mecanismo de busca filtrada (`ClienteDAO` com Specification API hoje; o PDF pede também `com.googlecode.genericdao.search`, HQL puro e SQL puro).
- Mudar o tipo/severidade de exceções lançadas (hoje: `RuntimeException` / `IllegalArgumentException`).
- Alterar a ordem do pipeline normalizar → validar → persistir.
- Mexer em `ClienteMapper.updateEntity` (regra de imutabilidade).
- Trocar engine de PDF (Jasper) ou Excel (Apache POI) — são exigências do desafio.

## Convenções
- `@Service`, `@Transactional` nos métodos que escrevem.
- Comentário em pt-BR no topo de cada método público explicando o quê e o porquê.
- Métodos privados pequenos com nome significativo (PDF 5.1).
