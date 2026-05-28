# Desafio de Estágio — CRUD de Clientes

Sistema CRUD de **Clientes** (Pessoa Física e Jurídica) com seus **Endereços** em relação 1:N,
geração de relatórios em PDF e Excel, importação por planilha e dois frontends
(Wicket 7 servindo HTML server-side + Angular 14 SPA consumindo a mesma API REST).

---

## Pré-requisitos

| Ferramenta | Versão | Para quê |
|---|---|---|
| **Docker Desktop** | última | Subir o MySQL |
| **JDK** | 17 | Compilar e rodar o Spring Boot |
| **Node.js** | 16+ | Rodar o frontend Angular |
| **IntelliJ IDEA** | 2024+ | Rodar os run configurations já versionados |
| **Git** | — | Clonar o repositório |

> Maven **não** precisa instalar — o projeto roda via IntelliJ ou pela classe principal.
> Apenas o **banco** é dockerizado; backend e frontend rodam localmente.

---

## Como rodar (passo a passo)

### 1. Clonar e entrar na pasta

```bash
git clone <url-do-repo>
cd crud_project_2
```

### 2. Configurar o `application.properties`

O `application.properties` está no `.gitignore` porque tem senha do banco. Crie a partir do exemplo:

```bash
# Copie o modelo
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

E ajuste a senha para `UnikaDevDB` (que é a definida no `docker-compose.yml`).

### 3. Subir o banco (Docker)

```bash
docker compose up -d
```

Aguarde alguns segundos até o MySQL ficar "healthy". Para conferir:

```bash
docker compose ps
```

### 4. Rodar o backend Spring Boot

**Opção A — pelo IntelliJ (recomendado):** abra o projeto, escolha o run config **`RODAR WICKET`** (ou **`RODAR TUDO`** para subir Wicket + Angular juntos) e clique em Run.

**Opção B — pela linha de comando:** abra o `CrudProjectApplication.java` e rode pela IDE,
ou compile o jar manualmente.

A aplicação sobe em `http://localhost:8080`.

> **VM Option necessária:** `--add-opens java.base/java.lang=ALL-UNNAMED`
> Já está configurada no run config `RODAR WICKET`. Necessária para Wicket/POI funcionarem em Java 17 (sistema de módulos do JPMS).

### 5. Rodar o frontend Angular

Em outro terminal:

```bash
cd frontend-angular
npm install        # apenas na primeira vez
npm start
```

Acesse em `http://localhost:4200`.

> Pelo IntelliJ existe o run config `RODAR ANGULAR` que faz isso automaticamente.

### 6. Pronto — pontos de acesso

| URL | O que é |
|---|---|
| `http://localhost:8080/` | Frontend **Wicket** (homepage redireciona para listagem de clientes) |
| `http://localhost:4200/` | Frontend **Angular** (SPA) |
| `http://localhost:8080/api/clientes` | API REST (lista todos) |

---

## Arquitetura

```
┌─────────────────────────────────┐     ┌─────────────────────────────────┐
│  WICKET 7 (server-side)         │     │  ANGULAR 14 (SPA)               │
│  http://localhost:8080          │     │  http://localhost:4200          │
│  - Conecta direto ao Service    │     │  - Consome a API REST           │
└──────────────┬──────────────────┘     └──────────────┬──────────────────┘
               │                                       │ HTTP/JSON
               │                                       ▼
               │                        ┌──────────────────────────────┐
               │                        │  REST Controllers            │
               │                        │  /api/clientes, /api/relat.  │
               │                        └──────────────┬───────────────┘
               │                                       │
               └───────────────────┬───────────────────┘
                                   ▼
                  ┌────────────────────────────────────┐
                  │  Service Layer (regras de negócio) │
                  │  ClienteService, ReportService,    │
                  │  ClienteValidator, etc.            │
                  └────────────────┬───────────────────┘
                                   │
                  ┌────────────────▼───────────────────┐
                  │  Repository / DAO (Spring Data)    │
                  └────────────────┬───────────────────┘
                                   │ Hibernate / JPA
                                   ▼
                  ┌────────────────────────────────────┐
                  │  MySQL 8 (rodando no Docker)       │
                  │  banco: crud_project_db            │
                  └────────────────────────────────────┘
```

**Pontos importantes:**
- O **Wicket** chama o `Service` diretamente em Java (mesma JVM) — não passa pelo Controller REST.
- O **Angular** consome só a API REST (`/api/...`).
- A camada **Service** é o ponto de encontro: regras de negócio centralizadas servem os dois frontends.

---

## Funcionalidades implementadas

### Backend
- ✅ CRUD completo de Cliente (POST, GET, GET/{id}, PUT, DELETE)
- ✅ Sincronização de endereços no PUT (id existe = update, sem id = create, sumiu = delete)
- ✅ Validação CPF/CNPJ (Caelum Stella — matematicamente válido)
- ✅ Validação de e-mail e telefone (regex + comprimento)
- ✅ CPF/CNPJ, TipoPessoa e dataCadastro **imutáveis após cadastro**
- ✅ Exatamente 1 endereço principal por cliente (validado)
- ✅ Busca filtrada paginada (Specification API)
- ✅ Contadores agregados (total / ativos) sem carregar dados
- ✅ Geração de relatórios PDF (Jasper) — lista geral e detalhes individuais
- ✅ Geração de relatórios Excel (Apache POI) — lista e detalhes
- ✅ Importação de clientes via planilha Excel
- ✅ Tratamento global de exceções (datas inválidas → mensagem amigável)

### Frontend Wicket
- ✅ Listagem com paginação, busca, filtros (modal AJAX)
- ✅ Modal de **criar cliente** com múltiplos endereços
- ✅ Modal de **editar cliente** (nome, e-mail, IE, ativo — CPF/CNPJ imutável)
- ✅ Modal de **excluir cliente** com confirmação
- ✅ Página de **detalhes** com CRUD de endereços (adicionar, editar, excluir)
- ✅ Modal de **importar Excel**
- ✅ Modal de **relatórios** (PDF/Excel) por cliente e geral
- ✅ Integração **ViaCEP** (CEP → preenche logradouro/bairro/cidade/UF)
- ✅ Integração **API IBGE** (UF e Cidade como dropdowns dinâmicos)
- ✅ Máscaras de CPF/CNPJ, CEP, telefone e data

### Frontend Angular
- ✅ Listagem com filtros, paginação e contadores
- ✅ Modal de criar / editar / excluir cliente
- ✅ Página de detalhes com CRUD de endereços
- ✅ Integração ViaCEP + IBGE (paridade com o Wicket)
- ✅ Validação local antes do submit (com mensagens amigáveis)
- ✅ Importação de Excel
- ✅ Download de relatórios PDF/Excel

---

## Modelos de dados

### Cliente (`tb_cliente`)

| Campo | Tipo | Observação |
|---|---|---|
| `id` | Long | PK |
| `tipoPessoa` | Enum | `FISICA` ou `JURIDICA` — imutável |
| `nome` | String | Nome civil (PF) ou Razão Social (PJ) |
| `cpfCnpj` | String | 11 dígitos (CPF) ou 14 (CNPJ) — imutável, unique |
| `rgInscricaoEstadual` | String | RG (PF) ou Inscrição Estadual (PJ) |
| `dataNascimento` | LocalDate | Nascimento (PF) ou Fundação (PJ) |
| `email` | String | E-mail do cliente |
| `telefone` | String | Telefone principal (opcional) |
| `ativo` | Boolean | Status |
| `dataCadastro` | LocalDateTime | Gerada no servidor — imutável |
| `enderecos` | List\<Endereco\> | 1:N, cascade ALL, orphanRemoval |

### Endereço (`tb_endereco`)

| Campo | Tipo | Observação |
|---|---|---|
| `id` | Long | PK |
| `tipo` | Enum | `RESIDENCIAL` ou `COMERCIAL` |
| `logradouro` | String | Obrigatório |
| `numero` | String | Aceita `"SN"` para sem-número |
| `complemento` | String | Opcional |
| `bairro` | String | Obrigatório |
| `cidade` | String | Obrigatório |
| `estado` | String | UF (2 letras) |
| `cep` | String | 8 dígitos (sem máscara no banco) |
| `pais` | String | Padrão "Brasil" |
| `telefone` | String | Opcional (do endereço) |
| `principal` | Boolean | Exatamente um endereço deve ser `true` |
| `cliente_id` | Long | FK para `tb_cliente` |

---

## Rotas da API REST

```
POST   /api/clientes                              criar cliente
GET    /api/clientes                              listar todos (sem paginação)
GET    /api/clientes/buscar                       listar paginado com filtros
GET    /api/clientes/contadores                   total e ativos
GET    /api/clientes/{id}                         buscar por id
PUT    /api/clientes/{id}                         atualizar (sync de endereços)
DELETE /api/clientes/{id}                         excluir
POST   /api/clientes/importar                     importar Excel
GET    /api/clientes/modelo-planilha              baixar modelo de importação

GET    /api/relatorios/clientes/pdf               lista em PDF
GET    /api/relatorios/cliente/detalhes/pdf?id=   detalhes em PDF
GET    /api/relatorios/clientes/excel             lista em Excel
GET    /api/relatorios/cliente/detalhes/excel?id= detalhes em Excel
```

---

## Stack

| Tecnologia | Versão | Função |
|---|---|---|
| Java | 17 | Linguagem do backend |
| Spring Boot | 2.7.18 | Framework Web + DI |
| Hibernate / JPA | (via Spring Boot) | ORM |
| MySQL | 8.0 (Docker) | Banco de dados |
| Apache Wicket | 7.18.0 | Frontend server-side (Fase 1) |
| Angular | 14.2 | Frontend SPA (Fase 2) |
| Angular Material | 14.2 | Components do Angular |
| JasperReports | 6.20.0 | Geração de PDF |
| Apache POI | 5.2.3 | Geração e leitura de Excel |
| Caelum Stella | 2.1.6 | Validação matemática de CPF/CNPJ |
| jQuery Mask Plugin | 1.14.16 | Máscaras de input (Wicket) |
| ngx-mask | 13.2 | Máscaras de input (Angular) |
| Bootstrap | 5.3 | CSS do Wicket |

**APIs externas consumidas:**
- **ViaCEP** (`https://viacep.com.br`) — busca de endereço por CEP
- **IBGE Localidades** (`https://servicodados.ibge.gov.br/api/v1/localidades`) — UFs e municípios

---

## Estrutura de pastas

```
crud_project_2/
├── docker-compose.yml          ← MySQL 8 (único serviço dockerizado)
├── pom.xml                     ← Configuração Maven do backend
├── src/main/
│   ├── java/com/crudproject/
│   │   ├── config/             ← GlobalExceptionHandler
│   │   ├── controller/         ← REST controllers
│   │   ├── dao/                ← Busca filtrada via Specification
│   │   ├── dto/                ← DTOs de entrada e saída
│   │   │   ├── cliente/
│   │   │   └── endereco/
│   │   ├── mapper/             ← Conversão DTO ↔ Entity
│   │   ├── model/              ← Entidades JPA
│   │   ├── repository/         ← Spring Data JPA
│   │   ├── service/            ← Regras de negócio
│   │   │   ├── reports/        ← Builders de Excel
│   │   │   └── validation/     ← ClienteValidator, DocumentoUtil
│   │   ├── wicket/             ← Frontend Wicket
│   │   │   ├── page/           ← Pages e Panels
│   │   │   ├── resources/      ← CSS, JS (incluindo IBGE/ViaCEP)
│   │   │   └── state/          ← POJOs Serializable de form state
│   │   └── CrudProjectApplication.java
│   └── resources/
│       ├── application.properties.example
│       └── reports/            ← Templates .jrxml do Jasper
└── frontend-angular/
    ├── src/app/
    │   ├── models/             ← Interfaces TS (Cliente, Endereco, Estado…)
    │   ├── pages/
    │   │   ├── listagem/       ← Tela principal + modais
    │   │   └── detalhes/       ← Tela de detalhes + modais
    │   ├── services/           ← ClienteService, ViaCepService, IbgeService
    │   └── app.module.ts
    └── package.json
```

---

## Troubleshooting

### "Conexão recusada na porta 3306"
O Docker não terminou de subir o MySQL. Aguarde uns 10 segundos e tente de novo.
Para verificar: `docker compose ps` deve mostrar STATUS = `healthy`.

### "Cannot deserialize value of type LocalDate"
Backend já trata isso com o `GlobalExceptionHandler` — retorna a mensagem amigável "Data inválida…".

### Angular não carrega cidades depois de selecionar UF
Verificar console: a API do IBGE pode estar fora do ar. O frontend tem fallback (lista vazia).

### Wicket exibe "InaccessibleObjectException"
Falta a VM Option `--add-opens java.base/java.lang=ALL-UNNAMED`.
Use o run config **`RODAR WICKET`** (já configurado).

### Banco vem vazio após `docker compose up`
Comportamento esperado. O Hibernate cria as tabelas na primeira inicialização do Spring Boot.
Para cadastrar dados de teste, use a tela do Wicket ou as rotas REST.

### Para zerar tudo (banco + dados)
```bash
docker compose down -v   # ⚠️ apaga todos os dados!
docker compose up -d
```

---

## Run Configurations (IntelliJ)

Estão versionadas em `.idea/runConfigurations/`:

| Config | O que faz |
|---|---|
| `RODAR WICKET` | Sobe o Spring Boot (Wicket + REST + Angular consumidor) com VM Options corretas |
| `RODAR ANGULAR` | Roda `npm start` na pasta `frontend-angular` |
| `RODAR TUDO` | Compound — sobe os dois ao mesmo tempo |
