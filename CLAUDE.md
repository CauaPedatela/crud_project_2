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
│   │   ├── ClienteDTO.java        — entrada (POST/PUT)
│   │   └── ClienteResponseDTO.java — saída (GET)
│   └── endereco/
│       ├── EnderecoDTO.java
│       └── EnderecoResponseDTO.java
│
├── mapper/
│   ├── ClienteMapper.java     — DTO ↔ Entity
│   └── EnderecoMapper.java
│
├── repository/
│   ├── ClienteRepository.java
│   └── EnderecoRepository.java
│
├── service/
│   ├── ClienteService.java        — orquestrador principal
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
    ├── WicketApplication.java — classe principal do Wicket (homepage = ListagemClientesPage)
    ├── WicketConfig.java      — registra WicketFilter no Spring Boot para /*
    └── page/
        ├── ListagemClientesPage.java  + .html  ← EM DESENVOLVIMENTO
        └── DetalhesClientePage.java   + .html  ← EM DESENVOLVIMENTO
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

### Feito ✅
- Banco de dados (entidades, migrations implícitas via Hibernate)
- Camada completa: Model → DTO → Mapper → Validator → Service → Repository
- REST Controllers (para testes e futura integração Angular)
- Relatórios: PDF e Excel (lista + detalhes individuais) via Jasper
- Integração Wicket+Spring Boot (WicketApplication + WicketConfig)
- HTML mockups das duas páginas (ListagemClientesPage, DetalhesClientePage)
- Classes Java das páginas criadas (vazias)

### Pendente Wicket ⏳
1. `ListagemClientesPage` — tabela dinâmica com dados reais do banco
2. `ListagemClientesPage` — busca/filtro
3. `ListagemClientesPage` — modal "Novo Cliente" funcional
4. `ListagemClientesPage` — paginação real
5. `DetalhesClientePage` — exibir dados do cliente
6. `DetalhesClientePage` — modal "Editar Cliente"
7. `DetalhesClientePage` — CRUD de endereços
8. Botões de relatório (PDF/Excel) nas páginas
9. Importação via Excel (endpoint + tela)
10. `FeedbackPanel` para mensagens de sucesso/erro
11. Confirmação de exclusão (JS confirm ou ModalWindow)

### Pendente Backend ⏳
- Endpoint `POST /api/clientes/importar` (upload Excel)

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
