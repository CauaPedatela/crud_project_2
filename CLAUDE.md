# CLAUDE.md — Contexto do Projeto

## Visão Geral
Desafio de estágio: sistema CRUD de Clientes (Pessoa Física e Jurídica) com seus Endereços
(relação 1:N), geração de relatórios PDF e Excel, e dois frontends (Wicket → Angular).

**Usuário:** iniciante em programação. Guiar passo a passo, explicar o conceito ANTES de codar.

---

## ⚠️ Regras Invioláveis (escopo do desafio)

Estas regras valem para QUALQUER conversa, mesmo após `/clear`:

1. **Stack congelada.** Não introduzir libs, frameworks ou padrões fora dos listados em "Stack e Versões". Se achar que falta algo, **pergunte primeiro**.
2. **Sem refatorações amplas espontâneas.** Limpeza, renomeação em massa, extração de abstrações novas, troca de bibliotecas — só com pedido explícito do usuário.
3. **REST Controllers são intocáveis.** Eles atendem ao Angular em paralelo ao Wicket. **NUNCA remova endpoints**, mesmo que pareçam não usados — o Angular consome todos eles.
4. **Schema do banco é congelado.** Não altere `@Entity`, `@Column`, `@OneToMany`, `nullable`, `unique`, tipos de colunas ou nomes de tabelas sem confirmação. Hibernate gera DDL automaticamente — qualquer mudança altera o banco real.
5. **CPF/CNPJ e TipoPessoa são IMUTÁVEIS após cadastro.** Não permitir edição em nenhum lugar do código.
6. **Endereço principal: exatamente UM por cliente.** Regra forte do validator — nunca relaxar.
7. **DTOs DEVEM `implements Serializable`.** Wicket serializa modelos entre requisições — remover quebra a sessão.
8. **Pipeline de salvar/atualizar:** `normalizarDados()` → `ClienteValidator` → persistência. **Não inverter a ordem** (Caelum Stella falha se o CPF chegar formatado).

---

## 🤝 Comportamento Esperado do Assistente

1. **Mais de 1 arquivo a tocar → entrar em plan mode automaticamente** (`ExitPlanMode` só após aprovação).
2. **Antes de criar abstração nova** (helper, util, classe genérica): perguntar se vale a complexidade. Triplicar 3 linhas é melhor que abstrair cedo.
3. **Antes de tocar em arquivo fora do diretório da tarefa atual:** confirmar com o usuário.
4. **Código "esquisito" ≠ código errado.** Investigar a intenção antes de "limpar" — pode ser workaround intencional documentado em outro lugar.
5. **Cada subdiretório tem seu próprio `CLAUDE.md`** com regras específicas. Leia-o antes de editar arquivos lá dentro.
6. **Em caso de dúvida sobre escopo:** pergunte antes, não depois.

---

## 📝 Estilo de Comentários

O usuário é iniciante e usa os comentários para aprender. Seguir o PDF (seção 5.1) à risca:

- **Comentar liberalmente em pt-BR.** 1-2 linhas curtas explicando o "porquê" de cada bloco lógico.
- **Em código novo escrito pelo Claude: sempre comentar.** Tanto o "o quê" quanto o "porquê", de forma didática.
- **Não remover comentários existentes** ao refatorar — exceto se o comentário ficou objetivamente errado.
- **JavaDoc curto** em métodos públicos do Service e do Controller.
- **Nomes significativos** em pt-BR para variáveis, métodos e classes (já é a convenção do projeto).

---

## 📋 Requisitos Formais do Desafio (PDF)

Checklist sintetizado das seções 4-8 do PDF "Desafio Estágio":

| Seção | Requisito | Status |
|---|---|---|
| 4.1 | Modelo `Cliente` (PF/PJ unificado) com `Endereco` 1:N | ✅ |
| 5.1 | Validações no **serviço**, não no controller | ✅ |
| 5.1 | Comentar em pt-BR, métodos com objetivo único | ✅ |
| 6.1 | Wicket + Angular | ✅ Ambos prontos |
| 6.1 | Validações de campos obrigatórios | ✅ |
| 6.1 | Visibilidade de campos por ação do usuário (PF↔PJ) | ✅ |
| 6.1 | jQuery para máscaras | ✅ data, CPF/CNPJ, CEP, telefone |
| 6.3 | CRUD numa mesma página + Modal Windows | ✅ |
| 6.3 | Paginação | ✅ |
| 7 | `FeedbackPanel` no Wicket | ✅ |
| 7 | Confirmação para ações irreversíveis (excluir) | ✅ |
| 7 | Relatórios PDF (Jasper) e Excel (POI) | ✅ |
| 7 | Importação por planilha Excel | ✅ |
| 7 | Evitar CPF/CNPJ duplicado | ✅ |
| 7 | Impedir alteração de dados sensíveis | ✅ (CPF/CNPJ/TipoPessoa) |
| 7 | **Testes JUnit em TODOS os CRUDs** | ⏳ **Pendente** (`src/test/` não existe) |
| 7 | **Mensagens flutuantes com auto-dismiss** | ⏳ **Pendente** |
| 8.1 | Componentes Wicket exigidos: `Panel`, `TextField`, `NumberTextField`, `RadioGroup`, `CheckBox`, `ListView`, Link, Submit, `AjaxRequestTarget` | ✅ |
| 8.2 | Botão de relatório por linha + botão de todos | ✅ |
| 8.3 | Exportação Excel (linha + todos) | ✅ |
| 8.3 | Importação Excel | ✅ |
| 8.4 | Hibernate 5 (via Spring Boot 2.7) | ✅ |
| 8.4 | Classes separadas Serviço e DAO | ✅ |
| 8.4 | OneToMany / ManyToOne | ✅ |
| 8.4 | **Search (`com.googlecode.genericdao.search`)** | ⏳ **Pendente** (hoje usa Specification API, é diferente) |
| 8.4 | **HQL puro** (pelo menos uma consulta) | ⏳ **Pendente** |
| 8.4 | **SQL nativo** (pelo menos uma consulta) | ⏳ **Pendente** |
| 8.5 | Tela com filtros + paginação + Modal de inclusão | ✅ |

---

## Stack e Versões
| Tecnologia | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 2.7.18 |
| Hibernate / JPA | via Spring Boot |
| MySQL | 8.0 (Docker — único serviço dockerizado) |
| Apache Wicket | 7.18.0 |
| JasperReports | 6.20.0 |
| Apache POI (Excel) | 5.2.3 |
| Caelum Stella | 2.1.6 (validação matemática CPF/CNPJ) |
| jQuery Mask Plugin | 1.14.16 (máscaras no Wicket) |
| Angular | 14.2 |
| Angular Material | 14.2 |
| ngx-mask | 13.2 (máscaras no Angular) |
| Bootstrap | 5.3 (nos HTMLs Wicket) |

**APIs externas consumidas:**
- **ViaCEP** — busca de endereço por CEP
- **IBGE Localidades** — lista de UFs e municípios (dropdowns dinâmicos)

---

## Estrutura de Pacotes

```
com.crudproject
├── config/
│   └── GlobalExceptionHandler.java   — @ControllerAdvice (data inválida, regras de negócio)
│
├── model/
│   ├── Cliente.java           — entidade JPA unificada PF+PJ
│   ├── Endereco.java          — entidade JPA, FK cliente_id
│   ├── TipoPessoa.java        — enum: FISICA, JURIDICA
│   └── TipoEndereco.java      — enum: RESIDENCIAL, COMERCIAL
│
├── dto/
│   ├── cliente/
│   │   ├── ClienteDTO.java          — entrada (POST/PUT), implements Serializable
│   │   └── ClienteResponseDTO.java  — saída (GET), implements Serializable
│   └── endereco/
│       ├── EnderecoDTO.java         — implements Serializable
│       └── EnderecoResponseDTO.java — implements Serializable
│
├── mapper/
│   ├── ClienteMapper.java     — DTO ↔ Entity (updateEntity ignora campos imutáveis)
│   └── EnderecoMapper.java
│
├── repository/
│   ├── ClienteRepository.java  — JpaRepository + JpaSpecificationExecutor
│   └── EnderecoRepository.java
│
├── dao/
│   └── ClienteDAO.java        — busca filtrada via JPA Specification API (WHERE dinâmico)
│
├── service/
│   ├── ClienteService.java         — orquestrador principal (salvar/atualizar/buscar)
│   ├── ClienteImportacaoService.java — importação por planilha Excel
│   ├── EnderecoSincronizador.java  — lógica de sync no PUT (id existe→update, sumiu→delete)
│   ├── ReportService.java          — geração PDF (Jasper) e Excel (POI builders)
│   ├── reports/                    — builders Excel internos
│   └── validation/
│       ├── ClienteValidator.java   — regras de negócio
│       ├── DocumentoUtil.java      — validação CPF/CNPJ (Caelum Stella) + limpeza
│       └── MascaraUtil.java        — formatação para exibição
│
├── controller/                — REST (consumido pelo Angular)
│   ├── ClienteController.java
│   └── ReportController.java
│
└── wicket/
    ├── WicketApplication.java — homepage = ListagemClientesPage; registra SpringComponentInjector
    ├── WicketConfig.java      — registra WicketFilter no Spring Boot para /*
    ├── resources/
    │   ├── clientes.css       — estilos compartilhados
    │   └── clientes.js        — máscaras, ViaCEP, IBGE, autoHideFeedback
    ├── state/                 — POJOs Serializable (CriacaoClienteState, EdicaoClienteState, etc)
    └── page/
        ├── ListagemClientesPage  — orquestra panels (busca, filtros, tabela, rodapé)
        ├── DetalhesClientePage   — exibe cliente + CRUD de endereços
        ├── listagem/             — modais CRUD de cliente + importação
        ├── detalhes/             — modais CRUD de endereço + card do cliente
        └── shared/               — modais reutilizados (Editar Cliente, Relatório)
```

> **Frontend Angular** vive em `frontend-angular/` na raiz. Estrutura:
> `src/app/{models,services,pages/listagem,pages/detalhes}` — espelha
> a organização do Wicket (com `services/ibge.service.ts` para a integração IBGE).

---

## Modelo de Dados

### Cliente (`tb_cliente`)
| Campo Java | Coluna | Observação |
|---|---|---|
| id | id | PK, auto-increment |
| tipoPessoa | tipo_pessoa | enum: FISICA / JURIDICA |
| nome | nome | nome civil (PF) ou razão social (PJ) |
| cpfCnpj | cpf_cnpj | unique, 11 dígitos (CPF) ou 14 (CNPJ) |
| rgInscricaoEstadual | rg_inscricao_estadual | RG (PF) ou Inscrição Estadual (PJ) |
| dataNascimento | data_nascimento | nascimento (PF) ou fundação (PJ) |
| email | email | obrigatório |
| telefone | telefone | opcional |
| ativo | ativo | boolean |
| dataCadastro | data_cadastro | gerado no servidor (LocalDateTime.now()) |
| enderecos | — | @OneToMany, cascade ALL, orphanRemoval |

### Endereco (`tb_endereco`)
| Campo Java | Coluna | Observação |
|---|---|---|
| id | id | PK |
| tipo | tipo | enum TipoEndereco |
| logradouro | logradouro | obrigatório |
| numero | numero | obrigatório (aceita "SN" para sem-número) |
| complemento | complemento | opcional |
| bairro | bairro | obrigatório (validado no service) |
| cidade | cidade | obrigatório |
| estado | estado | UF, 2 chars |
| cep | cep | obrigatório |
| pais | pais | obrigatório |
| principal | principal | boolean |
| cliente | cliente_id | FK para Cliente (@ManyToOne) |

---

## Rotas REST Existentes (consumidas pelo Angular)

```
POST   /api/clientes                              → cadastrar
GET    /api/clientes                              → listar todos (sem paginação)
GET    /api/clientes/buscar                       → listar paginado com filtros
GET    /api/clientes/contadores                   → total e ativos (sem trazer dados)
GET    /api/clientes/{id}                         → buscar por id
PUT    /api/clientes/{id}                         → atualizar (sync de endereços)
DELETE /api/clientes/{id}                         → excluir
POST   /api/clientes/importar                     → importação via Excel
GET    /api/clientes/modelo-planilha              → modelo de planilha para download

GET    /api/relatorios/clientes/pdf               → lista em PDF
GET    /api/relatorios/cliente/detalhes/pdf?id=   → detalhes em PDF
GET    /api/relatorios/clientes/excel             → lista em Excel
GET    /api/relatorios/cliente/detalhes/excel?id= → detalhes em Excel
```

---

## Arquitetura Wicket (regra importante)

> **As páginas Wicket se conectam DIRETAMENTE ao Service, sem passar pelo Controller.**
> `Page → @SpringBean → ClienteService → Repository → MySQL`

O Controller REST existe em paralelo e é o que o frontend Angular consome.

### Como o Wicket funciona (resumo para contexto)
- Cada página = 1 `.java` (lógica) + 1 `.html` (template), mesmo nome, mesmo pacote
- A ponte entre eles é o atributo `wicket:id="algumId"` no HTML
- No Java: `add(new AlgumComponente("algumId"))` — o ID deve ser **idêntico**
- O HTML precisa do namespace: `<html xmlns:wicket="http://wicket.apache.org/dtds.data/wicket-xhtml1.4-strict.dtd">`
- Para injetar um Service: `@SpringBean private ClienteService clienteService;` (requer `wicket-spring` no futuro ou ApplicationContext manual)
- O build copia HTMLs de `src/main/java` graças ao bloco `<resources>` no `pom.xml`

### Componentes Wicket principais a usar neste projeto
| Componente | Para que serve |
|---|---|
| `Label` | Exibir texto de uma variável Java no HTML |
| `ListView` | Iterar uma lista e renderizar linhas de tabela |
| `PageableListView` | ListView com paginação |
| `WebMarkupContainer` | Div/bloco que pode ser mostrado/ocultado (ex: bloco PF vs PJ) |
| `Form` | Formulário com binding automático para objeto Java |
| `TextField` | Campo de texto vinculado a uma propriedade |
| `RadioGroup` / `Radio` | Seleção PF/PJ |
| `CheckBox` | Campo "Ativo" |
| `AjaxButton` | Botão que submete sem recarregar a página |
| `ModalWindow` | Janela modal do próprio Wicket |
| `FeedbackPanel` | Exibe mensagens de erro/sucesso |
| `AjaxRequestTarget` | Atualiza componentes específicos via AJAX |

---

## Estado Atual e Próximos Passos

### Feito ✅ — Backend
- Banco de dados (entidades, migrations implícitas via Hibernate)
- Camada completa: Model → DTO → Mapper → Validator → Service → Repository
- REST Controllers (para testes e futura integração Angular)
- Relatórios: PDF e Excel (lista + detalhes individuais) via Jasper
- `ClienteDAO` com busca filtrada via JPA Specification API
- `ClienteDAO.buscarComFiltros` ordena por `dataCadastro DESC` (cliente novo aparece no topo)
- **Blindagem de validações (fase atual):**
  - `ClienteDTO` e `EnderecoDTO` implementam `Serializable` (exigência do Wicket para serialização de sessão)
  - `DocumentoUtil.limparFormatacao()` remove máscaras (pontos, traços, barras) de CPF/CNPJ, telefone e CEP antes de qualquer validação
  - `ClienteService.normalizarDados()` chama o `DocumentoUtil` no início de `salvar()` e `atualizar()` — garante que o validator sempre recebe dados limpos
  - `ClienteValidator` usa **Caelum Stella** (`CPFValidator`/`CNPJValidator`) para validação matemática (dígito verificador) de CPF e CNPJ
  - `ClienteValidator` valida e-mail via regex e telefone por comprimento (10 dígitos = fixo, 11 = celular, ambos com DDD)
  - `ClienteValidator.validarEnderecos` exige bairro como campo obrigatório

### Feito ✅ — Frontend Wicket (visualização e relatórios)
- Integração Wicket+Spring Boot (`WicketApplication` + `WicketConfig`)
- `ListagemClientesPage` — orquestra 3 panels + contadores no header
  - `BuscaPanel` — barra de busca AJAX (`AjaxButton`)
  - `TabelaClientesPanel` — `PageableListView` + `AjaxPagingNavigator`
  - `FiltrosPanel` — modal de filtros AJAX (Aplicar/Limpar via `AjaxButton`)
  - `FiltroState` — POJO `Serializable` compartilhando estado entre panels
  - Rodapé com botão **Adicionar Cliente** + **Relatório de Todos (PDF/Excel)**
- `DetalhesClientePage` — exibe dados do cliente (via `LoadableDetachableModel` + `cardCliente` re-renderizável) e endereços (apenas leitura)
  - Clique no nome da lista → navega via `BookmarkablePageLink`
  - Labels dinâmicos PF/PJ via `AbstractReadOnlyModel`
  - Modal de **Relatório** com links `PDF` / `Excel`
- Cada linha da tabela tem botão de **Relatório por cliente** (modal via JS)

### Feito ✅ — Frontend Wicket (CRUD de cliente, tudo AJAX)
- **Excluir cliente** a partir da listagem (botão lixeira na linha) — modal de confirmação + `AjaxButton` + `target.add(tabelaPanel, contadores)`
- **Editar cliente** a partir da listagem (botão lápis) **e** da página de detalhes (botão "Editar"):
  - Modal pré-preenchido via `data-*` attributes + JS (`abrirModalEdicao(btn)`)
  - Campos editáveis: nome, email, IE (PJ), ativo
  - **CPF/CNPJ e tipoPessoa permanecem imutáveis** (regra forte do desafio)
  - `onSubmit` recarrega o cliente do banco pra preservar campos imutáveis + endereços
  - Após salvar: re-renderiza `tabelaPanel`/`cardCliente`/`contadores`/`btnEditarCliente` via `target.add(...)`
- **Criar cliente** com 1 ou mais endereços (modal "Cadastrar Novo Cliente"):
  - Estrutura `<div class="modal">` envolvendo `<form wicket:id>` (necessário pra `target.add(form)` não quebrar o Bootstrap)
  - `<select wicket:id="criarTipoPessoa">` com `ChoiceRenderer` customizado (display "Pessoa Física"/"Pessoa Jurídica", value `name()`)
  - JS `alternarLabelsCriacao(select)` troca labels PF/PJ ao mudar o tipo
  - **Lista dinâmica de endereços** via `ListView` + `containerEnderecos.setOutputMarkupId(true)`:
    - "Adicionar outro endereço" (`AjaxButton` com `formnovalidate`)
    - "Tornar Principal" por item — desmarca todos e marca o clicado
    - "Remover" por item — desabilitado quando há só 1 endereço
  - Cliente novo aparece no topo da tabela (graças ao sort do DAO)
- **`FeedbackPanel` em dois níveis:**
  - **Page-level** (`#feedback` no topo da página) — para sucesso/erro de excluir e editar
  - **Modal-scoped** (`#feedbackCriar` dentro do modal de criar) com `ComponentFeedbackMessageFilter(form)` — erros aparecem dentro do modal sem fechá-lo, usuário corrige e tenta de novo
  - CSS no `<style>` da página deixa os panels com cara de alert Bootstrap (`.feedbackPanelERROR`/`INFO`/`WARNING`)
- **AJAX em tudo:** busca, filtros, paginação, excluir, editar, criar
- **Data de nascimento** no modal de criar: campo com máscara `dd/mm/aaaa` via **jQuery Mask Plugin**; o Java parseia via `DateTimeFormatter.ofPattern("dd/MM/yyyy")`
- **CSS fix para scroll do modal de criar:** `form { display: contents }` faz o `<form>` sumir do layout flex do Bootstrap, restaurando o scroll do `.modal-body` sem quebrar o submit

### Feito ✅ — Frontend Wicket (CRUD de endereços + importação + IBGE)
- **CRUD de endereços** em `DetalhesClientePage`:
  - `AdicionarEnderecoModalPanel` — modal de criar endereço
  - `EditarEnderecoModalPanel` — modal de editar (TODOS os campos editáveis)
  - `ExcluirEnderecoModalPanel` — exclusão com confirmação + regra de "não excluir principal/único"
- **Importação Excel** via `ImportarExcelModalPanel` (UI + endpoint backend)
- **Máscaras jQuery** completas: data, CPF/CNPJ, CEP, telefone (em `clientes.js`)
- **Auto-dismiss de feedback** (`autoHideFeedback()` no JS — desaparece após 5s)
- **Integração API IBGE** para UF e Cidade (dropdowns dinâmicos via `<select>` puro + `<input type="hidden" wicket:id>`):
  - JS popula opções; hidden mantém o valor que o Wicket lê na submissão
  - Funciona em `CriarClienteModalPanel`, `AdicionarEnderecoModalPanel` e `EditarEnderecoModalPanel`
  - Integrado com ViaCEP: ao buscar CEP, seleciona UF e carrega cidades do estado

### Feito ✅ — Frontend Angular (paridade completa com Wicket)
- Listagem com filtros, busca, paginação e contadores
- CRUD de cliente (criar / editar / excluir) com modais Material
- Página de detalhes com CRUD de endereços
- Integração **ViaCEP** + **API IBGE** (dropdowns dinâmicos de UF/Cidade)
- Importação por planilha Excel
- Download de relatórios PDF / Excel
- Validação local com mensagens amigáveis (incluindo data inválida)
- `ibge.service.ts` com cache local (estados uma vez, cidades por UF)

### Feito ✅ — Tratamento de erros
- `GlobalExceptionHandler` (`@ControllerAdvice`):
  - `HttpMessageNotReadableException` → detecta `LocalDate` inválido vindo do Jackson e retorna mensagem amigável "Data inválida. Use o formato dd/mm/aaaa com dia/mês/ano válidos."
  - `DateTimeParseException` → mesma mensagem amigável
  - `RuntimeException` e `IllegalArgumentException` → propagam `getMessage()` ao frontend como 400

### Pendente Backend ⏳
- **Testes JUnit dos CRUDs** (requisito explícito 7 — `src/test/` ainda não existe)
- **Consultas via Search `com.googlecode.genericdao.search`** (requisito explícito 8.4 — hoje usa Specification API, é diferente)
- **Pelo menos uma consulta HQL pura e uma SQL nativa** (requisito explícito 8.4)

---

## Convenções do Projeto
- Comentários em português
- Cada método com um único objetivo claro
- Validações de negócio ficam em `ClienteValidator`
- Sincronização de endereços fica em `EnderecoSincronizador`
- CPF/CNPJ são **imutáveis** após o cadastro (validado no service)
- TipoPessoa (PF/PJ) é **imutável** após o cadastro
- Exatamente **um** endereço deve ser `principal = true` por cliente
