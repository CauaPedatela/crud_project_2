// ================================================================
// app.component.ts — Componente raiz do aplicativo
//
// É o primeiro componente carregado, referenciado no index.html
// pela tag <app-root>. Contém a barra de navegação (MatToolbar)
// e o <router-outlet> onde as páginas são renderizadas.
// ================================================================

import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  titulo = 'Gestão de Clientes';
}
