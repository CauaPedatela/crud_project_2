# Desafio Estágio — Requisitos e Status

> **PDF original:** [desafio-estagio.pdf](./desafio-estagio.pdf)
> **Última revisão:** 2026-05-19

---

## 1. Stack obrigatória

### Ferramentas
- JDK 17
- IntelliJ IDEA
- Jasper Studio 6.11 (para editar `.jrxml`)

### Bibliotecas / frameworks
- Java 17
- Hibernate **5**
- Wicket **7**
- Angular **14**
- Angular Material
- jQuery (máscaras, cálculos)
- Bootstrap
- JasperReports (PDF)
- Apache POI (Excel)
- SQL: **MySQL**
- Padrões: **SOLID** e **MVC**

---

## 2. Estrutura do projeto exigida

> "Utilize apenas um repositório. A estrutura deve conter um diretório para o projeto Angular, um para o projeto Wicket e um para o backend Java (Spring para integração com Angular). Padrão MVC, dividido em camadas."

> **Obs do desafio:** começar implementando tudo em Wicket, e **depois** migrar para Angular.

---

## 3. Modelo de dados

### Cliente
| Campo | Observação |
|---|---|
| Tipo de Pessoa | Física **ou** Jurídica |
| CPF **ou** CNPJ | dependendo do tipo |
| Nome | só para PF |
| RG | só para PF |
| Data de Nascimento | só para PF |
| Razão Social | só para PJ |
| Inscrição Estadual | só para PJ |
| Data de Criação | só para PJ |
| E-mail | obrigatório |
| Ativo | Sim / Não |
| Endereços | lista 1:N com FK |

### Endereço
| Campo |
|---|
| Logradouro |
| Número |
| CEP |
| Bairro |
| Telefone |
| Cidade |
| Estado |
| Endereço Principal (Sim / Não) |
| Complemento |

---

## 4. Objetivo geral

> CRUD completo para **Clientes** e **Endereços** com impressão de **relatórios PDF e Excel**.

### Boas práticas exigidas
- Código genérico e organizado, reutilizável
- Validação de campos obrigatórios **no service**
- Cada método com um único objetivo claro
- Nomes significativos
- Comentar o código quando ajudar
- **Commits diários** antes de encerrar o trabalho

---

## 5. Desafio geral (seção 6 do PDF)

### 6.1 Tecnologias
- Frameworks: **Wicket** e **Angular**
- Validação de campos obrigatórios
- Visibilidade de campos muda conforme ações do usuário (ex: PF vs PJ)
- **JavaScript/jQuery** para cálculos e máscaras

### 6.2 Arquitetura
- MVC com separação clara
- Componentes genéricos para reutilização

### 6.3 Organização do código
- **Todas as operações** (editar, excluir, incluir, listar) **na mesma página**
- Usar **ModalWindow** quando necessário
- Listagens com **paginação**

---

## 6. Funcionalidades dos CRUDs (seção 7 do PDF)

- [ ] **FeedbackPanel** no Wicket (e Notify.js no Angular) para mensagens de sucesso/erro
- [ ] **Confirmação** para ações irreversíveis (exclusão)
- [ ] Relatórios sobre Clientes em **PDF (Jasper)** e **Excel (POI)**
- [ ] **Importação Excel**: usuário coloca a planilha num local designado e o sistema cadastra ou explica os erros
- [ ] Validações: bloquear CPF/CNPJ duplicado, impedir alteração de dados sensíveis
- [ ] **Testes unitários JUnit** para todos os CRUDs
- [ ] Mensagens em local **flutuante** que **desaparecem automaticamente**

---

## 7. Desafios específicos (seção 8 do PDF)

### 8.1 Componentes
- Bootstrap + componentes Wicket: `Panel`, `TextField`, `NumberTextField`, `RadioGroup`, `CheckBox`, `ListView`, link, submit
- Usar **AjaxRequestTarget** para atualizar campos
- Demonstrar no submit que os valores do form vão automaticamente para o objeto Java

### 8.2 Jasper
- Estudar como gerar relatórios Jasper
- Botão de relatório **por linha** (1 cliente)
- Botão de relatório de **todos** os registros listados

### 8.3 Excel
- Exportação Excel (botão ao lado do PDF)
- **Importação** Excel

### 8.4 Persistência
- **Hibernate 5**
- MVC
- Foco em back-end genérico, fácil de refatorar
- CRUDs com classes **separadas para Serviço e DAO**
- Relacionamentos **OneToMany / ManyToOne**
- Consultas com `Search` (`com.googlecode.genericdao.search`), **HQL** e **SQL puro**

### 8.5 ListView e ModalWindow
- Tela de listagem **com filtros**
- Usar `ListView` / `PageableListView` / `DataView` / `DefaultView`
- Botão de inclusão abre **ModalWindow** com campos de cadastro
- Ao fechar/salvar, a listagem atualiza automaticamente
- Cada item com botões de editar e excluir (excluir pede confirmação)

---

# Status atual do projeto

## Backend (Spring Boot + Hibernate + MySQL)

| Item | Status |
|---|---|
| Spring Boot 2.7.18 + Hibernate 5.6.x (via SB) + MySQL | ✅ |
| Entidades JPA `Cliente` e `Endereco` | ✅ |
| Relacionamento `@OneToMany` / `@ManyToOne` | ✅ |
| DTOs + Mappers (entrada/saída separados) | ✅ |
| Repositories (Spring Data JPA) | ✅ |
| `ClienteDAO` com busca filtrada (JPA Specification API) | ✅ |
| Service com regras de negócio | ✅ |
| `ClienteValidator` + `DocumentoUtil` (CPF/CNPJ) | ✅ |
| `EnderecoSincronizador` (sync no PUT) | ✅ |
| Controllers REST | ✅ |
| Relatório PDF lista (`/api/relatorios/clientes/pdf`) | ✅ |
| Relatório PDF detalhes (`/api/relatorios/cliente/detalhes/pdf?id=`) | ✅ |
| Relatório Excel lista (`/api/relatorios/clientes/excel`) | ✅ |
| Relatório Excel detalhes (`/api/relatorios/cliente/detalhes/excel?id=`) | ✅ |
| **Importação Excel** (`POST /api/clientes/importar`) | ❌ |
| **Testes JUnit** dos CRUDs | ❌ |
| Consulta com **HQL puro** | ❌ |
| Consulta com **SQL nativo puro** | ❌ |

### Desvios do enunciado a discutir
- O desafio pede `com.googlecode.genericdao.search`. Usamos **JPA Criteria via Specification** (abordagem mais moderna, mesma capacidade).
- O desafio pede HQL e SQL puro explicitamente — hoje só temos JPQL via `JpaRepository`. Para cumprir, podemos adicionar uma ou duas queries em algum método (ex: relatório com SQL nativo).

---

## Frontend Wicket — Visualização e relatórios

| Item | Status |
|---|---|
| `ListagemClientesPage` | ✅ |
| `BuscaPanel` (busca por nome/doc/email) | ✅ |
| `FiltrosPanel` (modal com status/tipo/data) | ✅ |
| `TabelaClientesPanel` (PageableListView + paginação) | ✅ |
| `FiltroState` compartilhando estado entre panels | ✅ |
| `DetalhesClientePage` (dados + endereços) | ✅ |
| Modal de relatório (PDF / Excel) por cliente | ✅ |
| Botões de relatório global no rodapé da listagem | ✅ |
| **AJAX** em busca, filtros e paginação (`AjaxButton`, `AjaxPagingNavigator`) | ✅ |

## Frontend Wicket — CRUD (pendente, vai ser reconstruído passo a passo)

| Item | Status |
|---|---|
| Excluir cliente (lista e detalhes, com confirmação) | ❌ |
| Editar cliente (modal) | ❌ |
| Criar cliente com pelo menos 1 endereço (modal) | ❌ |
| Criar endereço (dentro de detalhes) | ❌ |
| Editar endereço (dentro de detalhes) | ❌ |
| Excluir endereço (dentro de detalhes) | ❌ |
| `FeedbackPanel` para sucesso/erro | ❌ |
| Mensagens flutuantes com auto-dismiss | ❌ |
| Importação Excel (UI) | ❌ |

---

## Frontend Angular

| Item | Status |
|---|---|
| Tudo | ❌ (fase futura — só começa depois do Wicket completo) |

---

## Estrutura do projeto (4 do PDF)

| Item | Status |
|---|---|
| Repositório único | ✅ |
| Pasta separada para Angular | ❌ (Angular ainda não começou) |
| Pasta separada para Wicket | ⚠ Wicket está dentro do mesmo módulo do backend |
| Pasta separada para backend | ⚠ Idem |

> A organização atual é **monomódulo Maven**. Quando o Angular começar, vale a pena considerar dividir em `backend/`, `frontend-wicket/`, `frontend-angular/`.

---

# Lacunas para fechar o desafio

Em ordem sugerida de execução:

1. **CRUD Wicket completo** (reconstrução passo a passo):
   - Excluir cliente → Editar cliente → Criar cliente com endereço → CRUD de endereços → FeedbackPanel
2. **Importação Excel** (endpoint + tela)
3. **Testes JUnit** dos CRUDs
4. **Consultas HQL e SQL puro** em pelo menos uma operação (para cumprir o critério explícito do enunciado)
5. **Frontend Angular 14** replicando o CRUD, consumindo os REST Controllers existentes
6. **Reorganização opcional** em multi-módulo quando o Angular entrar
