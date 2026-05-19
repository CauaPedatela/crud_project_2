# Anotações — Como o Wicket estrutura páginas e panels

## 1. A regra fundamental: Java + HTML andam juntos

Toda página e todo panel do Wicket é formado por **dois arquivos com o mesmo nome**,
na mesma pasta:

```
src/main/java/com/crudproject/wicket/page/
│
├── ListagemClientesPage.java    ← lógica
├── ListagemClientesPage.html    ← visual
│
├── BuscaPanel.java
├── BuscaPanel.html
│
├── TabelaClientesPanel.java
├── TabelaClientesPanel.html
│
├── FiltrosPanel.java
└── FiltrosPanel.html
```

O Wicket encontra o HTML automaticamente pelo nome — você não precisa declarar
esse vínculo em lugar nenhum.

---

## 2. A ponte entre Java e HTML: o `wicket:id`

No HTML, qualquer tag pode receber um atributo `wicket:id`:

```html
<div wicket:id="totalClientes">0</div>
```

No Java, você registra um componente com o **mesmo id**:

```java
add(new Label("totalClientes", ...));
//            ↑ deve ser idêntico ao wicket:id
```

O Wicket substitui o conteúdo da tag pelo valor do componente.
O `0` dentro da tag é só um placeholder visual para o editor — o usuário nunca o vê.

**Se o id existir no HTML mas não tiver um `add()` correspondente no Java → exceção.**
**Se tiver um `add()` no Java mas não tiver `wicket:id` no HTML → exceção.**

---

## 3. O método `.add()` — montando a árvore

`.add()` no Wicket **não é** o mesmo que `List.add()`.
Ele encaixa um componente dentro de outro, montando uma hierarquia (árvore).

```java
// Radio dentro de RadioGroup
grupoAtivo.add(new Radio<>("radioAtivoTodos", Model.of("todos")));
grupoAtivo.add(new Radio<>("radioAtivoAtivo", Model.of("ativo")));

// RadioGroup dentro de Form
formFiltros.add(grupoAtivo);

// Form dentro do Panel
add(formFiltros);
```

Essa hierarquia Java precisa espelhar a hierarquia do HTML:

```html
<form wicket:id="formFiltros">
    <div wicket:id="grupoFiltroAtivo">
        <input type="radio" wicket:id="radioAtivoTodos">
        <input type="radio" wicket:id="radioAtivoAtivo">
    </div>
</form>
```

Um componente filho **sempre** procura seu `wicket:id` dentro da tag do componente pai.

---

## 4. Panels — fragmentos reutilizáveis de página

Um Panel é um pedaço de página com seu próprio Java e HTML.
Ele é adicionado à página mãe como qualquer outro componente:

```java
// ListagemClientesPage.java
add(new BuscaPanel("buscaPanel", filtros));
```

```html
<!-- ListagemClientesPage.html -->
<div wicket:id="buscaPanel"></div>
```

O Wicket abre o `BuscaPanel.html` e insere seu conteúdo dentro da `<div>`.
A partir desse ponto, todos os componentes do `BuscaPanel` procuram seus
`wicket:id` **dentro do `BuscaPanel.html`**, não mais na página mãe.

---

## 5. A tag `<wicket:panel>`

O arquivo HTML de um Panel precisa marcar qual trecho é o conteúdo do panel.
Isso é feito com `<wicket:panel>`:

```html
<html>           <!-- ignorado pelo Wicket -->
<body>           <!-- ignorado pelo Wicket -->
<wicket:panel>

  <!-- tudo aqui dentro é o conteúdo do panel -->
  <div class="page-card">
    <form wicket:id="formBusca">
      ...
    </form>
  </div>

</wicket:panel>
</body>           <!-- ignorado pelo Wicket -->
</html>           <!-- ignorado pelo Wicket -->
```

O `<html>` e `<body>` existem só para o arquivo ser um HTML válido no editor.
O Wicket ignora tudo fora de `<wicket:panel>`.

---

## 6. O que o navegador recebe após a renderização

Antes da renderização (os arquivos):

```
ListagemClientesPage.html          BuscaPanel.html
─────────────────────────          ───────────────────────────────
<main>                             <wicket:panel>
  <div wicket:id="buscaPanel">       <div class="page-card">
  </div>                               <form wicket:id="formBusca">
</main>                                </form>
                                     </div>
                                   </wicket:panel>
```

Depois da renderização (o que o navegador recebe):

```html
<main>
  <div>                          ← a <div> da página mãe é mantida como wrapper
    <div class="page-card">      ← conteúdo do BuscaPanel.html
      <form ...>
      </form>
    </div>
  </div>
</main>
```

---

## 7. `setRenderBodyOnly(true)` — removendo o wrapper

Por padrão, a tag da página mãe que tem o `wicket:id` vira um wrapper ao redor
do conteúdo do panel. Às vezes isso quebra a estrutura HTML — por exemplo,
um `<span>` em volta de um `<div class="modal">` quebraria o Bootstrap.

```java
FiltrosPanel filtrosPanel = new FiltrosPanel("filtrosPanel", filtros);
filtrosPanel.setRenderBodyOnly(true);   // ← remove o wrapper
add(filtrosPanel);
```

```html
<!-- página mãe -->
<span wicket:id="filtrosPanel"></span>
```

**Sem** `setRenderBodyOnly(true)`:
```html
<span>                                     ← o span é mantido como wrapper
  <div class="modal fade" id="modalFiltros">
  </div>
</span>
```

**Com** `setRenderBodyOnly(true)`:
```html
<div class="modal fade" id="modalFiltros"> ← o span some, só o conteúdo aparece
</div>
```

Use quando o wrapper extra prejudicar a estrutura do HTML.

---

## 8. A árvore completa do sistema

```
JAVA                                           HTML
═══════════════════════════════════════════════════════════════════════

ListagemClientesPage.java                      ListagemClientesPage.html
│
├── add(Label("totalClientes"))      ────────► wicket:id="totalClientes"
├── add(Label("totalAtivos"))        ────────► wicket:id="totalAtivos"
│
├── add(BuscaPanel("buscaPanel"))    ────────► wicket:id="buscaPanel"
│     │                                               ↓ abre BuscaPanel.html
│     └── add(Form("formBusca"))     ────────────────► wicket:id="formBusca"
│               │
│               └── add(TextField("campoBusca")) ────► wicket:id="campoBusca"
│
├── add(TabelaClientesPanel("tabelaPanel")) ──► wicket:id="tabelaPanel"
│     │                                               ↓ abre TabelaClientesPanel.html
│     ├── add(Label("contadorPagina"))  ─────────────► wicket:id="contadorPagina"
│     ├── add(PageableListView("listaClientes")) ────► wicket:id="listaClientes"
│     │         │
│     │         ├── item.add(Label("numero"))    ────► wicket:id="numero"
│     │         ├── item.add(Label("nome"))      ────► wicket:id="nome"
│     │         ├── item.add(Label("tipo"))      ────► wicket:id="tipo"
│     │         ├── item.add(Label("documento")) ────► wicket:id="documento"
│     │         ├── item.add(Label("email"))     ────► wicket:id="email"
│     │         └── item.add(Label("ativo"))     ────► wicket:id="ativo"
│     │
│     └── add(PagingNavigator("paginacao"))  ────────► wicket:id="paginacao"
│
└── add(FiltrosPanel("filtrosPanel"))  ────────────► wicket:id="filtrosPanel"
      │   (setRenderBodyOnly = true)                      ↓ abre FiltrosPanel.html
      └── add(Form("formFiltros"))  ──────────────────► wicket:id="formFiltros"
                │
                ├── add(RadioGroup("grupoFiltroAtivo")) ─► wicket:id="grupoFiltroAtivo"
                │         ├── add(Radio("radioAtivoTodos"))   wicket:id="radioAtivoTodos"
                │         ├── add(Radio("radioAtivoAtivo"))   wicket:id="radioAtivoAtivo"
                │         └── add(Radio("radioAtivoInativo")) wicket:id="radioAtivoInativo"
                │
                ├── add(RadioGroup("grupoFiltroTipo")) ──► wicket:id="grupoFiltroTipo"
                │         ├── add(Radio("radioTipoTodos"))    wicket:id="radioTipoTodos"
                │         ├── add(Radio("radioTipoPF"))       wicket:id="radioTipoPF"
                │         └── add(Radio("radioTipoPJ"))       wicket:id="radioTipoPJ"
                │
                ├── add(TextField("dataCriacaoInicio")) ──► wicket:id="dataCriacaoInicio"
                ├── add(TextField("dataCriacaoFim"))    ──► wicket:id="dataCriacaoFim"
                └── add(Button("btnLimparFiltros"))     ──► wicket:id="btnLimparFiltros"
```

---

## 9. Checklist rápido (estrutura)

Ao criar um novo componente, verifique:

- [ ] O `wicket:id` no HTML é **idêntico** ao id passado no Java?
- [ ] O componente Java foi adicionado com `.add()` ao componente pai correto?
- [ ] O `wicket:id` está dentro da tag do componente pai no HTML?
- [ ] Se é um Panel novo: existe um arquivo `.html` com o mesmo nome na mesma pasta?
- [ ] Se o Panel contém estrutura sensível (ex: modal): usar `setRenderBodyOnly(true)`?

---

# PARTE 2 — Como os dados chegam até a tela

## 10. Model: o que carrega os dados até o componente

Você já viu que `add(new Label("totalClientes", ...))` liga o Java ao HTML.
Mas **de onde vem o valor** que o Label vai exibir?

A resposta é: de um **Model** (um objeto com método `getObject()`).
Quando o Wicket vai renderizar o componente, ele chama `model.getObject()`.
O que vier desse método é o que aparece na tela.

Três formas de passar valor para um Label:

```java
// (1) Valor literal — fixo, calculado já no construtor
add(new Label("nome", "João"));

// (2) Valor dentro de um Model<String> — fixo também
add(new Label("nome", new Model<String>("João")));

// (3) Valor calculado sob demanda (Model anônimo)
add(new Label("nome", new AbstractReadOnlyModel<String>() {
    @Override
    public String getObject() {
        return "João";
    }
}));
```

As três produzem o mesmo HTML. A diferença é **quando** o valor é calculado:

| Versão | Quando o valor é calculado |
|---|---|
| (1) e (2) | No momento do `add()` — uma vez só |
| (3) | A cada renderização — o `getObject()` é chamado pelo Wicket |

A versão (3) é o que permite buscar dados frescos do banco a cada exibição.

---

## 11. LoadableDetachableModel — load sob demanda

Quando o dado vem do banco, queremos:

1. Buscar só **quando** a página for realmente renderizada.
2. **Reusar** dentro da mesma requisição (não chamar o service várias vezes).
3. **Esquecer** o resultado ao fim da requisição (não inflar a sessão Wicket).

O `LoadableDetachableModel` faz exatamente isso:

```java
IModel<List<ClienteResponseDTO>> todosClientesModel =
    new LoadableDetachableModel<List<ClienteResponseDTO>>() {
        @Override
        protected List<ClienteResponseDTO> load() {
            return clienteService.buscarTodos();   // só roda quando precisa
        }
    };
```

Comportamento na renderização:

- **1ª** chamada de `todosClientesModel.getObject()` → executa `load()`,
  guarda o resultado em cache interno, retorna.
- **Demais** chamadas dentro da mesma requisição → retorna do cache, **não**
  chama `load()` de novo.
- **Final** da requisição → Wicket descarta o cache (`detach()`),
  então a próxima requisição vai re-executar `load()` e buscar dados novos.

É por isso que `clienteService.buscarTodos()` aparece **dentro** do
`LoadableDetachableModel`, e não como um campo de instância da página.

---

## 12. AbstractReadOnlyModel — valores derivados

Quando o valor a exibir é *derivado* de outro Model (ex: contar elementos
de uma lista), usamos `AbstractReadOnlyModel`:

```java
add(new Label("totalClientes", new AbstractReadOnlyModel<Integer>() {
    @Override
    public Integer getObject() {
        return todosClientesModel.getObject().size();
    }
}));
```

O Label "totalClientes" não conhece a lista — conhece apenas a função
"pegar o tamanho da lista". O `AbstractReadOnlyModel` é a versão
"calcula toda vez" do `IModel` (sem cache próprio — o cache, se houver,
vem do Model que está sendo consumido).

---

## 13. Rota completa: do banco até a tela

Vamos rastrear, passo a passo, como o número `5` aparece dentro de
`<div wicket:id="totalClientes">` na tela.

### Cenário

- Banco MySQL tem 5 clientes cadastrados.
- Browser acessa `http://localhost:8080/`.

### Fase 1 — Construção (constructor da página)

**ListagemClientesPage.java:**
```java
public ListagemClientesPage() {
    adicionarContadoresHeader();   // entramos aqui primeiro
    adicionarPanels();
}

private void adicionarContadoresHeader() {

    // PASSO A: criamos o Model "como buscar todos os clientes"
    IModel<List<ClienteResponseDTO>> todosClientesModel =
        new LoadableDetachableModel<List<ClienteResponseDTO>>() {
            @Override
            protected List<ClienteResponseDTO> load() {
                return clienteService.buscarTodos();
            }
        };
    // ⚠ NÃO executou clienteService.buscarTodos() aqui.
    //   Só guardou a função em uma variável.

    // PASSO B: criamos o Label que sabe contar essa lista
    add(new Label("totalClientes", new AbstractReadOnlyModel<Integer>() {
        @Override
        public Integer getObject() {
            return todosClientesModel.getObject().size();
        }
    }));
    // ⚠ Também não rodou nenhum Model agora.
    //   Só registrou o Label na árvore de componentes da página.
}
```

Estado da memória ao final da Fase 1:

```
ListagemClientesPage (instância nova)
│
└── Label "totalClientes"
      └── Model: AbstractReadOnlyModel<Integer>
                  └── getObject() ──► todosClientesModel.getObject().size()
```

O banco **ainda não foi consultado**.

### Fase 2 — Renderização (Wicket gera o HTML final)

Wicket abre `ListagemClientesPage.html` e percorre tag por tag.
Quando chega em:

```html
<div class="fw-bold fs-4 text-primary" wicket:id="totalClientes">0</div>
```

A cadeia de chamadas é esta (de cima para baixo):

```
1. Wicket vê wicket:id="totalClientes"
   └→ procura o componente com esse id na árvore Java
       └→ encontra o Label que adicionamos

2. Label precisa do texto a exibir → chama model.getObject()
   (model = AbstractReadOnlyModel<Integer>)

3. AbstractReadOnlyModel.getObject() executa:
       return todosClientesModel.getObject().size();

4. todosClientesModel.getObject() é chamado pela 1ª vez no request
   LoadableDetachableModel não tem cache → executa load()

5. load() executa:
       return clienteService.buscarTodos();

6. clienteService.buscarTodos():
   ── query SQL no MySQL via Repository
   ── recebe entidades JPA Cliente
   ── converte para List<ClienteResponseDTO> via Mapper
   ── retorna a lista (5 elementos)

7. Lista de 5 elementos sobe a pilha:
   load() → LoadableDetachableModel (que guarda em cache)
        → todosClientesModel.getObject()

8. .size() é chamado sobre a lista → retorna 5

9. AbstractReadOnlyModel.getObject() devolve 5

10. Label recebe 5 e converte para a string "5"

11. Wicket substitui o conteúdo da <div>:
    <div class="fw-bold fs-4 text-primary">5</div>
    (o atributo wicket:id é removido do HTML final)
```

### Tabela de chamadas em ordem

| # | Arquivo | Método/expressão | Resultado |
|:-:|---|---|---|
| 1 | (Wicket) | percorre o HTML | acha `wicket:id="totalClientes"` |
| 2 | (Wicket) | `Label.onRender()` | precisa do texto, chama o model |
| 3 | ListagemClientesPage.java | `AbstractReadOnlyModel.getObject()` | executa `todosClientesModel.getObject().size()` |
| 4 | ListagemClientesPage.java | `todosClientesModel.getObject()` | dispara `load()` |
| 5 | ListagemClientesPage.java | `LoadableDetachableModel.load()` | chama o service |
| 6 | ClienteService.java | `buscarTodos()` | query no banco, retorna lista de 5 |
| 7 | — | lista sobe a pilha | até `getObject()` do AbstractReadOnlyModel |
| 8 | ListagemClientesPage.java | `.size()` | resultado: `5` |
| 9 | (Wicket) | escreve no HTML | substitui o conteúdo da `<div>` por `5` |

---

## 14. O mesmo padrão para listas (ListView / PageableListView)

Para uma tabela paginada como em `TabelaClientesPanel.java`:

```java
IModel<List<ClienteResponseDTO>> clientesFiltradosModel =
    new LoadableDetachableModel<List<ClienteResponseDTO>>() {
        @Override
        protected List<ClienteResponseDTO> load() {
            return clienteService.buscarComFiltros(...);
        }
    };

PageableListView<ClienteResponseDTO> listView =
    new PageableListView<>("listaClientes", clientesFiltradosModel, 5) {
        @Override
        protected void populateItem(ListItem<ClienteResponseDTO> item) {
            ClienteResponseDTO cliente = item.getModelObject();
            item.add(new Label("documento", cliente.getCpfCnpj()));
            item.add(new Label("email", cliente.getEmail()));
            // ... outros Labels da linha
        }
    };
```

**TabelaClientesPanel.html:**
```html
<tr wicket:id="listaClientes">
  <td wicket:id="documento">000.000.000-00</td>
  <td wicket:id="email">email@email.com</td>
</tr>
```

Na renderização:

1. Wicket vê `<tr wicket:id="listaClientes">` → procura `PageableListView` na árvore.
2. `PageableListView` chama `clientesFiltradosModel.getObject()`
   (mesma mecânica: `LoadableDetachableModel` → `load()` → service → banco).
3. Recebe uma `List<ClienteResponseDTO>`.
4. Para cada item da lista, chama `populateItem(item)`.
5. Dentro de `populateItem` criamos os Labels filhos (`documento`, `email`, etc.)
   passando como valor o campo daquela linha (`cliente.getCpfCnpj()`).
6. Wicket repete a `<tr>` no HTML **uma vez para cada item**, substituindo
   os `wicket:id` filhos pelos valores que `populateItem` produziu.

A diferença para o exemplo do contador é que aqui o "consumidor da lista"
é o próprio `PageableListView` (que itera), em vez de um `.size()`.

---

## 15. Por que tudo isso, ao invés de só `new Label("id", valor)`?

Porque **o valor não existe quando o constructor roda** — ele está no banco.
Se escrevêssemos:

```java
// ❌ ruim — chama o banco no construtor
int total = clienteService.buscarTodos().size();
add(new Label("totalClientes", total));
```

- O banco seria consultado **mesmo se o componente não for renderizado**.
- Se outro componente precisasse da mesma lista, o banco seria consultado
  **duas vezes** no mesmo request.
- O valor (`total`) ficaria **congelado** na instância da página, que vive
  na sessão Wicket — qualquer dado novo no banco não apareceria sem
  reconstruir a página inteira.

Com Models:

- O banco é consultado **uma única vez por request** (cache do `LoadableDetachableModel`).
- O banco é consultado **só se o componente for renderizado**.
- A página armazena **funções** (Models), não dados — ela fica leve na sessão.

---

## 16. Checklist sobre Models

- [ ] O componente recebe um **Model** (não valor literal) quando o dado vem do banco?
- [ ] A chamada ao service está **dentro** de `LoadableDetachableModel.load()`?
- [ ] Valores derivados (`.size()`, transformações) estão em `AbstractReadOnlyModel`?
- [ ] O Model é definido **dentro do método de construção**, não como campo de
      instância? (Evita serializar o service na sessão)
- [ ] Listas (`ListView`, `PageableListView`) recebem o Model da lista, e
      `populateItem` é onde os Labels filhos são criados a partir de cada item?

---

## 17. Quando o `load()` roda? Renderização e ciclo do cache

**Renderização** = o Wicket gera o HTML final a partir da árvore de componentes
+ template. Acontece **uma vez por requisição**:

| Ação | O que é renderizado |
|---|---|
| Acesso inicial, F5, voltar/avançar, link | Página inteira |
| Submit de form sem AJAX | Página inteira |
| `AjaxButton`, `AjaxLink`, `AjaxPagingNavigator` | **Parcial** — só os componentes em `target.add(...)` |

O cache do `LoadableDetachableModel` vive apenas durante **uma** requisição:
abre no primeiro `getObject()`, é reusado pelos outros consumidores dentro da
mesma requisição, e é **descartado** (`detach()`) ao final. A próxima
requisição busca dados novos do banco.

Sem AJAX, todo clique recarrega a página inteira → todos os `load()` rodam de
novo. Com AJAX, só os componentes incluídos em `target.add(...)` são
re-renderizados → só os `load()` desses componentes rodam.

**Exemplo prático** (em `BuscaPanel.java`):

```java
formBusca.add(new AjaxButton("btnBuscar", formBusca) {
    @Override
    protected void onSubmit(AjaxRequestTarget target) {
        target.add(tabelaParaAtualizar);   // só o painel da tabela re-renderiza
    }
});
```

Para `target.add(componente)` funcionar, o componente alvo precisa de
`setOutputMarkupId(true)` — assim o Wicket gera um `id="..."` no HTML
e o JavaScript do Wicket sabe qual elemento substituir.
