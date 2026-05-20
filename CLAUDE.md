# CLAUDE.md — Contexto do Projeto

## Visão Geral
Desafio de estágio: sistema CRUD de Clientes (Pessoa Física e Jurídica) com seus Endereços
(relação 1:N), geração de relatórios PDF e Excel, e dois frontends (Wicket → Angular).

**Usuário:** iniciante em programação. Guiar passo a passo, explicar o conceito ANTES de codar.

---

## Stack e Versões
| Tecnologia | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 2.7.18 |
| Hibernate / JPA | via Spring Boot |
| MySQL | (local, configurado em application.properties) |
| Apache Wicket | 7.18.0 |
| JasperReports | 6.20.0 |
| Apache POI (Excel) | 5.2.3 |
| Caelum Stella | 2.1.6 (validação matemática CPF/CNPJ) |
| jQuery Mask Plugin | 1.14.16 (máscaras de campo no frontend) |
| Angular | 14 (fase futura) |
| Bootstrap | 5.3 (nos HTMLs Wicket) |

---

## Estrutura de Pacotes

```
com.crudproject
├── model/
│   ├── Cliente.java           — entidade JPA unificada PF+PJ
│   ├── Endereco.java          — entidade JPA, FK cliente_id
│   ├── TipoPessoa.java        — enum: FISICA, JURIDICA
│   └── TipoEndereco.java      — enum de tipos de endereço
│
├── dto/
│   ├── cliente/
│   │   ├── ClienteDTO.java          — entrada (POST/PUT)
│   │   └── ClienteResponseDTO.java  — saída (GET), implements Serializable (Wicket)
│   └── endereco/
│       ├── EnderecoDTO.java
│       └── EnderecoResponseDTO.java  — implements Serializable (Wicket)
│
├── mapper/
│   ├── ClienteMapper.java     — DTO ↔ Entity
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
│   ├── ClienteService.java        — orquestrador principal; delega ao DAO para filtros
│   ├── EnderecoSincronizador.java — lógica de sync no PUT
│   ├── ReportService.java         — geração PDF e Excel
│   └── validation/
│       ├── ClienteValidator.java  — regras de negócio
│       └── DocumentoUtil.java     — validação CPF/CNPJ
│
├── controller/                — REST (usado para testes e futura integração Angular)
│   ├── ClienteController.java
│   └── ReportController.java
│
└── wicket/
    ├── WicketApplication.java — homepage = ListagemClientesPage; registra SpringComponentInjector
    ├── WicketConfig.java      — registra WicketFilter no Spring Boot para /*
    └── page/
        ├── FiltroState.java           — POJO Serializable com termo, ativo, tipo, datas
        ├── BuscaPanel.java    + .html  — barra de pesquisa (AjaxButton → atualiza tabela)
        ├── FiltrosPanel.java  + .html  — modal de filtros (AjaxButton Aplicar/Limpar)
        ├── TabelaClientesPanel.java + .html — tabela (PageableListView + AjaxPagingNavigator)
        ├── ListagemClientesPage.java  + .html  — ✅ orquestra panels + CRUD de cliente (criar/editar/excluir)
        └── DetalhesClientePage.java   + .html  — ✅ visualiza + edita cliente (cardCliente AJAX-atualizável)
```

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
| numero | numero | opcional |
| complemento | complemento | opcional |
| bairro | bairro | opcional |
| cidade | cidade | obrigatório |
| estado | estado | UF, 2 chars |
| cep | cep | obrigatório |
| pais | pais | obrigatório |
| principal | principal | boolean |
| cliente | cliente_id | FK para Cliente (@ManyToOne) |

---

## Rotas REST Existentes (para testes / Angular futuro)

```
POST   /api/clientes                          → cadastrar
GET    /api/clientes                          → listar todos
GET    /api/clientes/{id}                     → buscar por id
PUT    /api/clientes/{id}                     → atualizar (sync de endereços)
DELETE /api/clientes/{id}                     → excluir

GET    /api/relatorios/clientes/pdf           → lista em PDF
GET    /api/relatorios/cliente/detalhes/pdf?id=  → detalhes em PDF
GET    /api/relatorios/clientes/excel         → lista em Excel
GET    /api/relatorios/cliente/detalhes/excel?id= → detalhes em Excel
```

---

## Arquitetura Wicket (regra importante)

> **As páginas Wicket se conectam DIRETAMENTE ao Service, sem passar pelo Controller.**
> `Page → @SpringBean → ClienteService → Repository → MySQL`

O Controller REST existe em paralelo e ficará disponível para a futura migração Angular.

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
  - Campos editáveis: email, telefone, IE (PJ), ativo
  - Nome e CPF/CNPJ exibidos `disabled` (imutáveis após cadastro)
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

### Pendente Wicket ⏳
1. **CRUD de endereços** dentro de `DetalhesClientePage` (adicionar/editar/excluir endereço de cliente já existente)
2. **Importação via Excel** (UI)
3. **Mensagens flutuantes com auto-dismiss** (toast) — requisito do desafio
4. Máscaras de CPF/CNPJ, CEP e telefone no frontend (jQuery Mask — data já feita)

### Pendente Backend ⏳
- Endpoint `POST /api/clientes/importar` (upload Excel)
- Testes JUnit dos CRUDs
- Pelo menos uma consulta HQL pura e uma SQL nativa (requisito explícito do desafio em 8.4)

### Fase futura — Angular ⏳
- Replicar tudo em Angular 14 + Angular Material, consumindo os REST Controllers

---

## Convenções do Projeto
- Comentários em português
- Cada método com um único objetivo claro
- Validações de negócio ficam em `ClienteValidator`
- Sincronização de endereços fica em `EnderecoSincronizador`
- CPF/CNPJ são **imutáveis** após o cadastro (validado no service)
- TipoPessoa (PF/PJ) é **imutável** após o cadastro
- Exatamente **um** endereço deve ser `principal = true` por cliente
