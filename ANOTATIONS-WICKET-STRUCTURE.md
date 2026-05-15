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

## 9. Checklist rápido

Ao criar um novo componente, verifique:

- [ ] O `wicket:id` no HTML é **idêntico** ao id passado no Java?
- [ ] O componente Java foi adicionado com `.add()` ao componente pai correto?
- [ ] O `wicket:id` está dentro da tag do componente pai no HTML?
- [ ] Se é um Panel novo: existe um arquivo `.html` com o mesmo nome na mesma pasta?
- [ ] Se o Panel contém estrutura sensível (ex: modal): usar `setRenderBodyOnly(true)`?
