# resources/ — Configuração e Templates de Relatórios

> Leia também o `CLAUDE.md` da raiz antes de qualquer alteração aqui.

## Responsabilidade
- **`application.properties`** — configuração do Spring Boot (DB, porta, JPA).
- **`reports/cliente-detalhes.jrxml`** — template Jasper para relatório de detalhes de cliente.
- **`reports/clientes-lista.jrxml`** — template Jasper para lista de clientes.

## ℹ️ Sobre o `application.properties`

O arquivo está **versionado no repositório** porque a senha do banco já está
pública no `docker-compose.yml` (que também é versionado). Esconder a credencial
em um lugar e expor no outro seria teatro — então mantemos coerência.

**Se um dia o projeto for para produção real:**
- Use Spring Profiles (`application-prod.properties`) ou variáveis de ambiente.
- Volte a gitignorar o `.properties` e use `${SPRING_DATASOURCE_PASSWORD}` para ler do ambiente.
- Documente o setup novo no README.

## ⚠️ Templates Jasper (.jrxml)

- Editados visualmente no **Jasper Studio 6.11** (instalação obrigatória pelo PDF seção 1).
- Edição direta no XML é aceitável apenas para:
  - Ajuste de caminho de subreport.
  - Mudança de `fontSize`, cor, label literal pequena.
  - Correção de typo.
- **Mudanças estruturais** (adicionar campo, mudar layout, agrupar) → fazer no Studio, não no XML cru.
- Conferir que o `.jrxml` continua válido após edição (Spring Boot falha no startup se Jasper não compilar).

## Pode alterar sem perguntar
- Adicionar `.properties` novos auxiliares (mensagens, i18n).
- Editar `.jrxml` para correções pequenas listadas acima.

## ⛔ EXIGE confirmação antes de alterar
- **Mudar string de conexão JDBC, porta do servidor, `spring.jpa.hibernate.ddl-auto`** no `application.properties` — pode quebrar o ambiente do supervisor.
- Mudar layout estrutural de relatórios Jasper sem usar o Studio.
- Adicionar/remover templates `.jrxml`.
- Mover arquivos para fora desta pasta.
- Voltar a gitignorar o `application.properties` sem antes configurar um esquema de Profiles/env vars.

## Convenções
- Comentários no `.properties` em pt-BR, com `#`.
- Nomes de templates Jasper em kebab-case (`cliente-detalhes.jrxml`).
