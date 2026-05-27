// ================================================================
// app-routing.module.ts — Mapa de URLs do frontend Angular
//
// Define quais componentes são exibidos para cada URL.
// O Angular Router monitora a URL do navegador e renderiza
// o componente correspondente dentro do <router-outlet>.
//
// Rotas:
//   /            → redireciona para /clientes
//   /clientes    → ListagemComponent (tabela de clientes)
//   /clientes/42 → DetalhesComponent (detalhes do cliente 42)
// ================================================================

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListagemComponent } from './pages/listagem/listagem.component';
import { DetalhesComponent } from './pages/detalhes/detalhes.component';

const routes: Routes = [
  // Rota raiz: redireciona sempre para /clientes
  { path: '', redirectTo: '/clientes', pathMatch: 'full' },

  // Listagem de todos os clientes
  { path: 'clientes', component: ListagemComponent },

  // Detalhes de um cliente específico — :id é o ID do banco
  { path: 'clientes/:id', component: DetalhesComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
