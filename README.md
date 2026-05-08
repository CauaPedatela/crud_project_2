# Desafio de Estágio — CRUD de Clientes

## O que é esse projeto?

Sistema para cadastrar e gerenciar **Clientes e Endereços**, desenvolvido como desafio de estágio.

O usuário pode: **criar, editar, excluir e listar** clientes, além de gerar relatórios em PDF e Excel.

---

## Arquitetura do sistema

```
FRONTEND (o que o usuário vê)
  ├── Wicket 7   → Fase 1 (implementar primeiro)
  └── Angular 14 → Fase 2 (migrar depois)
        ↕  HTTP (chamadas REST)
BACKEND (Spring Boot - Java 17)
  ├── Controller → recebe as requisições HTTP
  ├── DTO        → contrato de entrada/saída (JSON)
  ├── Mapper     → converte DTO ↔ Entity
  ├── Service    → aplica as regras de negócio
  ├── Repository → acessa o banco (Spring Data JPA)
  └── DAO        → queries avançadas (HQL, SQL puro, Search) — Fase futura
        ↕  Hibernate (JPA)
BANCO DE DADOS (MySQL)
```

**Padrão MVC:**
- **M** (Model): classes Java que representam os dados (`Cliente`, `Endereco`)
- **V** (View): telas do usuário (Wicket ou Angular)
- **C** (Controller): código que recebe as requisições e decide o que fazer

---

## Estado atual do projeto

> Última atualização: rota de cadastro com DTOs em construção

### ✅ Concluído

- **Setup do ambiente** — Java 17, Maven, MySQL, IntelliJ
- **`pom.xml`** — Spring Boot 2.7, Hibernate, MySQL, Wicket 7, JasperReports, Apache POI
- **Banco de dados** — MySQL com banco `crud_project_db` criado via DBeaver
- **`application.properties`** — configurado (fora do Git, há `.example` no repo)
- **`CrudProjectApplication.java`** — entrypoint do Spring Boot
- **Modelos**
  - `Cliente.java` — entidade JPA com PF + PJ na mesma tabela
  - `Endereco.java` — entidade JPA com FK pra Cliente
  - `TipoPessoa.java` — enum FISICA / JURIDICA
- **Repositories**
  - `ClienteRepository.java` — `findByCpf`, `findByCnpj`
  - `EnderecoRepository.java` — `findByClienteId`
- **Services**
  - `ClienteService.java` — todos os métodos (`salvar`, `buscarTodos`, `buscarPorId`, `atualizar`, `excluir`) + validações de CPF/CNPJ
  - `EnderecoService.java` — só estrutura base, métodos públicos vazios
- **DTOs de Cliente** (Rota 1 — cadastro)
  - `ClienteCadastroDTO.java` — entrada do POST
  - `ClienteResponseDTO.java` — saída de qualquer operação
- **`ClienteMapper.java`** — converte DTO ↔ Entity em ambos os sentidos
- **`ClienteService.salvar()`** refatorado para receber DTO e retornar DTO

### ⏳ Em andamento

- **Rota 1: `POST /api/clientes`** (cadastro de cliente)
  - [x] DTO de entrada
  - [x] DTO de saída
  - [x] Mapper
  - [x] Service adaptado
  - [ ] **`ClienteController` ← PRÓXIMO PASSO**
  - [ ] Testar no Apidog

### 📋 A fazer

**Backend — Restante das rotas de Cliente**

- Rota 2: `GET /api/clientes` (listar todos)
- Rota 3: `GET /api/clientes/{id}` (buscar por id)
- Rota 4: `PUT /api/clientes/{id}` (atualizar)
- Rota 5: `DELETE /api/clientes/{id}` (excluir)

**Backend — Endereços**

- DTOs de Endereço (`EnderecoCadastroDTO`, `EnderecoResponseDTO`)
- `EnderecoMapper`
- Implementar métodos do `EnderecoService` (vazios atualmente)
- `EnderecoController` com rotas REST

**Backend — DAO (Desafio 8.4)**

- `dao/` com `GenericDAO`, `ClienteDAO`, `EnderecoDAO`
- Queries com `com.googlecode.genericdao.search`
- Queries com HQL e SQL puro

**Frontend Fase 1 — Wicket**

- Página de listagem de clientes (com filtros e paginação)
- Modal de cadastro/edição
- Modal de gestão de endereços
- FeedbackPanel para mensagens
- Confirmação para exclusões

**Relatórios**

- PDF com JasperReports (relatório de cliente individual + lista)
- Excel com Apache POI (exportação)
- Importação de Excel para cadastro em massa

**Testes**

- JUnit nos Services

**Frontend Fase 2 — Angular**

- Projeto Angular 14 separado
- Consumir as rotas REST já existentes
- Bootstrap + Angular Material + Notify.js

---

## Próximo passo concreto

> Quando voltar ao projeto, retome aqui:

**Implementar `ClienteController.java`** para expor o método `salvar()` do Service como rota HTTP `POST /api/clientes`.

O Service e o Mapper já estão prontos. Falta só:
1. Adicionar as anotações `@RestController` e `@RequestMapping("/api/clientes")` na classe
2. Injetar o `ClienteService` via `@Autowired`
3. Criar um método `cadastrar(@RequestBody ClienteCadastroDTO dto)` anotado com `@PostMapping`
4. Esse método só repassa a chamada: `return clienteService.salvar(dto);`

Depois disso, testar no Apidog enviando um POST com JSON de PF e outro de PJ.

---

## Fluxo do cadastro — do Apidog ao banco

Quando uma rota REST é chamada (ex: pelo Apidog, Angular ou Postman), o dado atravessa todas as camadas. O exemplo abaixo é o fluxo de **cadastro de cliente** (POST `/api/clientes`).

```
┌─────────────────────────────────────────┐
│ 1. APIDOG (cliente HTTP)                │
│                                         │
│  POST http://localhost:8080/api/clientes│
│                                         │
│  Body (JSON):                           │
│  {                                      │
│    "tipoPessoa": "FISICA",              │
│    "cpf": "123.456.789-00",             │
│    "nome": "João",                      │
│    "email": "joao@email.com",           │
│    "ativo": true                        │
│  }                                      │
└──────────────────┬──────────────────────┘
                   │ envia o JSON via HTTP
                   ▼
┌─────────────────────────────────────────┐
│ 2. SPRING (recebe a requisição)         │
│                                         │
│  Spring vê o POST /api/clientes,        │
│  procura quem responde por essa rota.   │
│  Encontra ClienteController.cadastrar() │
│                                         │
│  Antes de chamar, converte o JSON em    │
│  ClienteCadastroDTO automaticamente.    │
└──────────────────┬──────────────────────┘
                   │ ClienteCadastroDTO
                   ▼
┌─────────────────────────────────────────┐
│ 3. CONTROLLER — ClienteController       │
│                                         │
│  cadastrar(ClienteCadastroDTO dto) {    │
│      return clienteService.salvar(dto); │
│  }                                      │
│                                         │
│  Não tem regra. Só recebe e repassa.    │
└──────────────────┬──────────────────────┘
                   │ ClienteCadastroDTO
                   ▼
┌─────────────────────────────────────────┐
│ 4. SERVICE — ClienteService             │
│                                         │
│  ① Pede ao Mapper para converter        │
│     o DTO em Cliente (entidade)         │
│                                         │
│  ② Aplica as regras de negócio:         │
│     - Tipo de pessoa preenchido?        │
│     - CPF/CNPJ já cadastrado?           │
│     - Campos obrigatórios?              │
│                                         │
│  ③ Manda o Repository salvar            │
└──────────────────┬──────────────────────┘
                   │ Cliente (entidade)
                   ▼
┌─────────────────────────────────────────┐
│ 5. MAPPER — ClienteMapper               │
│                                         │
│  toEntity(dto)                          │
│    Cliente c = new Cliente()            │
│    c.setNome(dto.getNome())             │
│    c.setCpf(dto.getCpf())               │
│    ... (copia campo por campo)          │
│    return c                             │
└──────────────────┬──────────────────────┘
                   │ Cliente sem ID ainda
                   ▼
┌─────────────────────────────────────────┐
│ 6. REPOSITORY — ClienteRepository       │
│                                         │
│  clienteRepository.save(cliente)        │
│                                         │
│  Hibernate vê que não tem ID → INSERT   │
└──────────────────┬──────────────────────┘
                   │ SQL: INSERT INTO tb_cliente...
                   ▼
┌─────────────────────────────────────────┐
│ 7. MYSQL                                │
│                                         │
│  Salva o cliente, gera o ID (ex: 7)     │
│  Devolve o ID ao Hibernate              │
└──────────────────┬──────────────────────┘
                   │ Cliente com ID = 7
                   ▼
┌─────────────────────────────────────────┐
│ 8. SERVICE (caminho de volta)           │
│                                         │
│  Recebe o Cliente salvo (com ID)        │
│  Pede ao Mapper para converter de       │
│  volta em ClienteResponseDTO            │
└──────────────────┬──────────────────────┘
                   │ ClienteResponseDTO
                   ▼
┌─────────────────────────────────────────┐
│ 9. CONTROLLER → SPRING                  │
│                                         │
│  Spring converte o DTO em JSON          │
│  e devolve com status 201 Created       │
└──────────────────┬──────────────────────┘
                   │ HTTP Response com JSON
                   ▼
┌─────────────────────────────────────────┐
│ 10. APIDOG (recebe a resposta)          │
│                                         │
│  Status: 201 Created                    │
│  Body:                                  │
│  {                                      │
│    "id": 7,                             │
│    "tipoPessoa": "FISICA",              │
│    "nome": "João",                      │
│    "cpf": "123.456.789-00",             │
│    "email": "joao@email.com",           │
│    "ativo": true                        │
│  }                                      │
└─────────────────────────────────────────┘
```

**Resumo em uma frase:** o **DTO atravessa Controller e Service**, vira **Entity no Mapper**, é salvo pelo **Repository**, e volta o caminho inverso até o **JSON** que o Apidog recebe.

A entidade `Cliente` nunca é exposta ao mundo externo — quem sai e entra é sempre DTO.

### Por que cada camada existe nesse fluxo

| Camada | Por que precisa existir |
|---|---|
| **Controller** | Tradutor HTTP ↔ Java. Sem ele, ninguém atende a requisição |
| **DTO** | Embalagem segura. Não expõe a entidade real |
| **Service** | Lugar das regras. Sem ele, regras vazariam pro Controller |
| **Mapper** | Centraliza a conversão. Sem ele, viraria gambiarra no Service |
| **Repository** | Fala com o banco. Sem ele, teríamos SQL na mão em todo lugar |
| **Entity** | Mapeia a tabela. Sem ela, Hibernate não sabe o que persistir |

### E o Wicket? Como se encaixa nesse fluxo?

O Wicket **não usa HTTP entre frontend e backend** — ele roda no mesmo servidor que o Spring Boot. Ou seja, a página do Wicket chama o `Service` **diretamente em Java**, sem passar por Controller nem JSON.

```
WICKET                        APIDOG/ANGULAR
──────                        ──────────────
ClientePage.java              Apidog envia JSON
   │                                │
   │ chama em Java                  │ HTTP POST
   ▼                                ▼
[Mapper page→DTO]              ClienteController
   │                                │
   ▼                                │ chama em Java
ClienteService.salvar(dto)  ◄───────┘
   │
   ▼
ClienteMapper.toEntity(dto)
   │
   ▼
ClienteRepository.save(entity)
   │
   ▼
MySQL
```

**A camada Service é o ponto de encontro.** Tudo que está abaixo dela (Mapper, Repository, Entity, banco) é reutilizado por ambos os frontends. Por isso vale a pena construir o backend primeiro com a API REST — quando o Wicket chegar, ele só consome o que já existe.

---

## Tecnologias

| Tecnologia | Para que serve |
|---|---|
| Java 17 | Linguagem do backend |
| Spring Boot 2.7 | Framework que facilita criar APIs em Java |
| Hibernate 5 | Converte objetos Java em tabelas do banco |
| MySQL 8 | Banco de dados |
| Apache Wicket 7 | Framework para criar telas web em Java (Fase 1) |
| Angular 14 | Framework frontend moderno (Fase 2) |
| JasperReports | Geração de relatórios em PDF |
| Apache POI | Geração e leitura de planilhas Excel |
| JUnit | Testes automatizados |

> **Observação importante:** Spring Boot 2.7 (não 3.x) por compatibilidade com Wicket 7. Migrar para Spring Boot 3 só faz sentido após substituir o Wicket.

---

## Modelos de dados

### Cliente
| Campo | Tipo | Observação |
|---|---|---|
| tipoPessoa | Enum | FISICA ou JURIDICA — **imutável após cadastro** |
| cpf | String | Só para Pessoa Física |
| nome | String | Só para Pessoa Física |
| rg | String | Só para Pessoa Física |
| dataNascimento | LocalDate | Só para Pessoa Física |
| cnpj | String | Só para Pessoa Jurídica |
| razaoSocial | String | Só para Pessoa Jurídica |
| inscricaoEstadual | String | Só para Pessoa Jurídica |
| dataCriacao | LocalDate | Só para Pessoa Jurídica |
| email | String | Todos |
| ativo | Boolean | Sim ou Não |
| enderecos | List\<Endereco\> | Relação 1:N (um cliente, vários endereços) |

> **Decisão arquitetural:** uma única tabela `tb_cliente` com colunas opcionais para PF e PJ. Quando é PF, os campos PJ ficam null e vice-versa. As validações cruzadas ficam no Service.

### Endereço
| Campo | Tipo |
|---|---|
| logradouro | String |
| numero | String |
| cep | String |
| bairro | String |
| telefone | String |
| cidade | String |
| estado | String |
| enderecoPrincipal | Boolean |
| complemento | String |

---

## Estrutura de pastas atual

```
src/main/java/com/crudproject/
├── CrudProjectApplication.java     ← entrypoint Spring Boot
├── model/
│   ├── Cliente.java
│   ├── Endereco.java
│   └── TipoPessoa.java
├── dto/
│   └── cliente/
│       ├── ClienteCadastroDTO.java     ← entrada do POST
│       └── ClienteResponseDTO.java     ← saída
├── mapper/
│   └── ClienteMapper.java              ← DTO ↔ Entity
├── repository/
│   ├── ClienteRepository.java
│   └── EnderecoRepository.java
├── service/
│   ├── ClienteService.java             ← salvar() já recebe DTO
│   └── EnderecoService.java            ← métodos vazios ainda
├── controller/
│   └── ClienteController.java          ← VAZIO — próximo passo
└── wicket/page/                        ← vazio (Fase 1 - Wicket)
```

---

## Configuração local

O `application.properties` está no `.gitignore` para proteger a senha do banco. No repositório existe um `application.properties.example` como modelo.

**Quando clonar o projeto em outro PC:**

1. Copie `src/main/resources/application.properties.example` para `application.properties`
2. Edite `application.properties` e substitua `SUA_SENHA_AQUI` pela senha do MySQL local
3. Crie o banco no MySQL: `CREATE DATABASE crud_project_db;`

---

## Git — Comandos essenciais

```bash
# Ver o estado atual do repositório
git status

# Ver o histórico de commits
git log --oneline

# Adicionar arquivos para o próximo commit
git add nome-do-arquivo
# ou adicionar tudo:
git add .

# Criar um commit (snapshot do que foi feito)
git commit -m "mensagem descrevendo o que foi feito"

# Enviar para o repositório online (GitHub/GitLab)
git push origin main

# Baixar atualizações do repositório online
git pull origin main

# Voltar pro último push (descarta mudanças locais)
git reset --hard origin/main
git clean -fd
```

**Boas práticas de commit:**
- Faça commits diariamente antes de encerrar o trabalho
- Mensagens no imperativo ("Adiciona modelo Cliente"), prefixos: `feat:`, `chore:`, `docs:`, `refactor:`
- Não commite arquivos com senhas — o `.gitignore` já cuida disso

---

## Como continuar no computador de casa

1. **Clone o repositório** (se ainda não tiver):
   ```bash
   git clone https://github.com/CauaPedatela/crud_project_2.git
   cd crud-project
   ```

2. **Ou, se já tiver o repositório clonado**, baixe as atualizações:
   ```bash
   git pull origin main
   ```

3. **Configure o `application.properties`** (veja seção "Configuração local")

4. Abra o projeto no **IntelliJ IDEA**:
   - File → Open → selecione a pasta `crud-project`
   - O IntelliJ vai detectar o `pom.xml` e baixar as dependências automaticamente

5. **Continue do passo onde parou** olhando a seção "Estado atual do projeto" acima

---

## Sessões com o Claude Code

Ao abrir o Claude Code no computador de casa e navegar até a pasta do projeto, basta dizer:

> "Continuando o desafio de estágio — leia o README e me diga onde paramos."

O Claude vai ler a seção "Estado atual" e "Próximo passo concreto" e retomar exatamente de onde foi deixado.

---

## Dúvidas frequentes

**Por que Spring Boot e não apenas Spring?**
O Spring Boot já vem configurado para funcionar com o mínimo de esforço. O Spring puro exigiria muito mais configuração manual.

**O que é Maven?**
É o gerenciador de dependências do Java. O arquivo `pom.xml` lista todas as bibliotecas que o projeto usa.

**O que é Hibernate?**
É um ORM (Object-Relational Mapping) — ele traduz objetos Java para tabelas no banco de dados. Em vez de escrever SQL na mão para tudo, você escreve classes Java e o Hibernate cuida do resto.

**Por que Wicket antes de Angular?**
O desafio pede explicitamente isso. Wicket é um framework mais antigo onde você escreve as telas diretamente em Java. Angular é moderno e separado do backend. Começar pelo Wicket ajuda a entender o fluxo completo antes de separar as responsabilidades.

**Por que DTOs se a Entity já existe?**
Para não expor a entidade JPA diretamente ao mundo externo. O DTO é a "embalagem" segura dos dados — sem anotações JPA, sem ciclos infinitos de serialização, sem vazar campos sensíveis.

**Repository vs DAO?**
Repository (Spring Data JPA) é usado para CRUD simples (`save`, `findById`, `findByCpf`). DAO entra quando o desafio pede queries com Search dinâmico, HQL ou SQL puro — recursos que o Repository não cobre tão facilmente.
