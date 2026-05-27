// ================================================================
// app.module.ts — Módulo raiz da aplicação Angular
//
// Aqui registramos TUDO que a aplicação usa:
//   - Módulos Angular (Browser, HTTP, Forms, Animations)
//   - Módulos Angular Material (UI components)
//   - Módulo de máscaras (ngx-mask)
//   - Todos os Components (páginas e modais)
// ================================================================

import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

// Biblioteca de máscaras (CPF, CNPJ, CEP, Telefone, Data)
import { NgxMaskModule } from 'ngx-mask';

// Roteamento
import { AppRoutingModule } from './app-routing.module';

// ── Angular Material ──────────────────────────────────────────────
import { MatToolbarModule }        from '@angular/material/toolbar';
import { MatIconModule }           from '@angular/material/icon';
import { MatButtonModule }         from '@angular/material/button';
import { MatTooltipModule }        from '@angular/material/tooltip';
import { MatCardModule }           from '@angular/material/card';
import { MatDividerModule }        from '@angular/material/divider';
import { MatTableModule }          from '@angular/material/table';
import { MatPaginatorModule }      from '@angular/material/paginator';
import { MatSortModule }           from '@angular/material/sort';
import { MatFormFieldModule }      from '@angular/material/form-field';
import { MatInputModule }          from '@angular/material/input';
import { MatSelectModule }         from '@angular/material/select';
import { MatSlideToggleModule }    from '@angular/material/slide-toggle';
import { MatRadioModule }          from '@angular/material/radio';
import { MatCheckboxModule }       from '@angular/material/checkbox';
import { MatDialogModule }         from '@angular/material/dialog';
import { MatSnackBarModule }       from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule }          from '@angular/material/chips';
import { MatListModule }           from '@angular/material/list';

// ── Components ────────────────────────────────────────────────────
import { AppComponent } from './app.component';

// Página de listagem de clientes + seus modais
import { ListagemComponent }       from './pages/listagem/listagem.component';
import { ModalCriarComponent }     from './pages/listagem/modal-criar/modal-criar.component';
import { ModalEditarComponent }    from './pages/listagem/modal-editar/modal-editar.component';
import { ModalExcluirComponent }   from './pages/listagem/modal-excluir/modal-excluir.component';
import { ModalFiltrosComponent }   from './pages/listagem/modal-filtros/modal-filtros.component';
import { ModalImportarComponent }  from './pages/listagem/modal-importar/modal-importar.component';
import { ModalRelatorioComponent } from './pages/listagem/modal-relatorio/modal-relatorio.component';

// Página de detalhes de cliente + seus modais de endereço
import { DetalhesComponent }              from './pages/detalhes/detalhes.component';
import { ModalAddEnderecoComponent }      from './pages/detalhes/modal-add-endereco/modal-add-endereco.component';
import { ModalEditarEnderecoComponent }   from './pages/detalhes/modal-editar-endereco/modal-editar-endereco.component';
import { ModalExcluirEnderecoComponent }  from './pages/detalhes/modal-excluir-endereco/modal-excluir-endereco.component';

@NgModule({
  declarations: [
    // ── Shell ──
    AppComponent,

    // ── Listagem ──
    ListagemComponent,
    ModalCriarComponent,
    ModalEditarComponent,
    ModalExcluirComponent,
    ModalFiltrosComponent,
    ModalImportarComponent,
    ModalRelatorioComponent,

    // ── Detalhes ──
    DetalhesComponent,
    ModalAddEnderecoComponent,
    ModalEditarEnderecoComponent,
    ModalExcluirEnderecoComponent
  ],

  imports: [
    // Angular core
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,   // necessário para [(ngModel)] na barra de busca da listagem

    // Máscaras de campo
    NgxMaskModule.forRoot(),

    // Roteamento
    AppRoutingModule,

    // Angular Material — UI components
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    MatCardModule,
    MatDividerModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatRadioModule,
    MatCheckboxModule,
    MatDialogModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatListModule
  ],

  providers: [],

  bootstrap: [AppComponent]
})
export class AppModule { }
