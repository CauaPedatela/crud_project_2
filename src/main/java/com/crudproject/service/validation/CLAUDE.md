# service/validation/ — Validadores e Utilitários de Validação

> Leia também o `CLAUDE.md` da raiz e o de `service/` antes de qualquer alteração aqui.

## Responsabilidade
Regras de negócio **puras**. Sem repositório, sem efeitos colaterais, sem log de auditoria. Apenas:
- Recebe DTO → lança exceção se inválido, retorna se ok.

Componentes:
- **`ClienteValidator`** — regras de negócio sobre Cliente e Endereços.
- **`DocumentoUtil`** — utilitário (CPF/CNPJ válido, limpeza de máscaras).
- **`MascaraUtil`** — formatação para exibição.

## Regras já implementadas (não relaxar sem confirmação)

| Regra | Onde |
|---|---|
| CPF/CNPJ matematicamente válido (Caelum Stella) | `DocumentoUtil.validar()` |
| Email via regex | `ClienteValidator.validarEmail()` |
| Telefone com 10 (fixo) ou 11 (celular) dígitos com DDD | `ClienteValidator.validarTelefone()` |
| Bairro obrigatório no endereço | `ClienteValidator.validarEnderecos()` |
| Exatamente **um** endereço com `principal=true` | `ClienteValidator.validarEnderecos()` |
| Cliente não pode ter CPF/CNPJ duplicado no banco | `ClienteService` (consulta `ClienteRepository`) |

## ⚠️ Ponto único de normalização

**`DocumentoUtil.limparFormatacao()` é o ÚNICO lugar que remove máscaras** (pontos, traços, barras, parênteses, espaços) de CPF/CNPJ, telefone e CEP. Se precisar limpar formatação em outro lugar, **chame esse método** — não duplique a lógica.

## Pode alterar sem perguntar
- Adicionar novas validações de negócio (com testes JUnit, idealmente).
- Melhorar mensagens de erro — torná-las mais didáticas para o usuário final.
- Adicionar overloads em utils.

## ⛔ EXIGE confirmação antes de alterar
- **Relaxar uma regra existente** (ex: aceitar email sem `@`, aceitar telefone com 9 dígitos, permitir mais de um endereço principal).
- Mudar o tipo de exceção lançada (`RuntimeException` / `IllegalArgumentException` hoje) — código consumidor depende.
- Substituir Caelum Stella por outra lib de CPF/CNPJ.
- Adicionar dependência de repositório/banco aqui dentro — validators devem ser puros.

## Convenções
- Métodos com nome `validar<Coisa>(...)`, retornando `void` e lançando exceção em falha.
- Mensagens de erro em pt-BR, completas o suficiente para o `FeedbackPanel` exibir direto.
- Comentário em pt-BR explicando cada regra (didático para o usuário iniciante).
