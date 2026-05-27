// ================================================================
// listagem.component.ts — Página principal de listagem de clientes
//
// Responsabilidades:
//   - Carregar e exibir a tabela de clientes (MatTable)
//   - Gerenciar busca por texto e filtros
//   - Abrir os modais (criar, editar, excluir, importar, relatório)
//   - Exibir notificações de sucesso/erro via MatSnackBar
// ================================================================

import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ClienteService }    from '../../services/cliente.service';
import { RelatorioService }  from '../../services/relatorio.service';
import { Cliente }           from '../../models/cliente.model';

import { ModalCriarComponent }     from './modal-criar/modal-criar.component';
import { ModalEditarComponent }    from './modal-editar/modal-editar.component';
import { ModalExcluirComponent }   from './modal-excluir/modal-excluir.component';
import { ModalFiltrosComponent }   from './modal-filtros/modal-filtros.component';
import { ModalImportarComponent }  from './modal-importar/modal-importar.component';
import { ModalRelatorioComponent } from './modal-relatorio/modal-relatorio.component';

// Interface que representa o estado dos filtros avançados
interface FiltrosState {
  ativo?: string;       // "true", "false" ou ""
  tipo?: string;        // "FISICA", "JURIDICA" ou ""
  dataInicio?: string;  // "yyyy-MM-dd"
  dataFim?: string;     // "yyyy-MM-dd"
}

@Component({
  selector: 'app-listagem',
  templateUrl: './listagem.component.html',
  styleUrls: ['./listagem.component.scss']
})
export class ListagemComponent implements OnInit, AfterViewInit {

  // ── Tabela ──
  // MatTableDataSource gerencia filtragem, ordenação e paginação automaticamente
  dataSource = new MatTableDataSource<Cliente>();
  // Colunas que aparecem na tabela, na ordem da esquerda para a direita
  colunas: string[] = ['nome', 'tipo', 'documento', 'email', 'status', 'acoes'];

  // Referências aos controles de paginação e ordenação declarados no HTML
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  // ── Estado da busca ──
  termoBusca = '';
  filtros: FiltrosState = {};
  carregando = false;

  // ── Getters calculados para os contadores no cabeçalho ──
  get totalClientes(): number { return this.dataSource.data.length; }
  get totalAtivos(): number   { return this.dataSource.data.filter(c => c.ativo).length; }

  constructor(
    private clienteService:   ClienteService,
    public  relatorioService: RelatorioService,
    private dialog:  MatDialog,
    private snackBar: MatSnackBar
  ) {}

  // Carrega a lista inicial ao abrir a página
  ngOnInit(): void {
    this.carregarTodos();
  }

  // Conecta paginator e sort ao dataSource depois que o HTML foi renderizado
  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  // ── Carrega todos os clientes sem filtros ──
  carregarTodos(): void {
    this.carregando = true;
    this.clienteService.listar().subscribe({
      next: clientes => {
        this.dataSource.data = clientes;
        this.carregando = false;
      },
      error: () => {
        this.mostrarErro('Não foi possível carregar a lista de clientes.');
        this.carregando = false;
      }
    });
  }

  // ── Executa busca com o termo e os filtros atuais ──
  buscar(): void {
    this.carregando = true;
    this.clienteService.buscarComFiltros({
      termo: this.termoBusca,
      ...this.filtros
    }).subscribe({
      next: clientes => {
        this.dataSource.data = clientes;
        this.carregando = false;
      },
      error: () => {
        this.mostrarErro('Erro ao buscar clientes.');
        this.carregando = false;
      }
    });
  }

  // ── Limpa a busca e volta a mostrar todos ──
  limparBusca(): void {
    this.termoBusca = '';
    this.filtros = {};
    this.carregarTodos();
  }

  // ── Abre o diálogo de filtros avançados ──
  abrirFiltros(): void {
    const ref = this.dialog.open(ModalFiltrosComponent, {
      width: '500px',
      data: { ...this.filtros } // passa uma cópia dos filtros atuais
    });

    // afterClosed emite o valor passado para dialogRef.close(...)
    ref.afterClosed().subscribe((novosFiltros: FiltrosState | undefined) => {
      if (novosFiltros !== undefined) {
        this.filtros = novosFiltros;
        this.buscar();
      }
    });
  }

  // ── Abre o modal de cadastro de novo cliente ──
  abrirModalCriar(): void {
    this.dialog.open(ModalCriarComponent, {
      width: '960px',
      maxHeight: '90vh',
      disableClose: true // impede fechar clicando fora (formulário longo)
    }).afterClosed().subscribe((criado: boolean) => {
      if (criado) {
        this.carregarTodos();
        this.mostrarSucesso('Cliente cadastrado com sucesso!');
      }
    });
  }

  // ── Abre o modal de edição de cliente ──
  abrirModalEditar(cliente: Cliente): void {
    this.dialog.open(ModalEditarComponent, {
      width: '600px',
      data: cliente
    }).afterClosed().subscribe((editado: boolean) => {
      if (editado) {
        this.carregarTodos();
        this.mostrarSucesso('Cliente atualizado com sucesso!');
      }
    });
  }

  // ── Abre o modal de confirmação de exclusão ──
  abrirModalExcluir(cliente: Cliente): void {
    this.dialog.open(ModalExcluirComponent, {
      width: '450px',
      data: cliente
    }).afterClosed().subscribe((excluido: boolean) => {
      if (excluido) {
        this.carregarTodos();
        this.mostrarSucesso('Cliente excluído com sucesso!');
      }
    });
  }

  // ── Abre o modal de relatório individual ──
  abrirModalRelatorio(cliente: Cliente): void {
    this.dialog.open(ModalRelatorioComponent, {
      width: '380px',
      data: cliente.id
    });
  }

  // ── Gera o relatório PDF da lista respeitando a busca e os filtros atuais ──
  gerarRelatorioPdf(): void {
    this.relatorioService.pdfLista({
      termo:      this.termoBusca || undefined,
      ativo:      this.filtros.ativo,
      tipo:       this.filtros.tipo,
      dataInicio: this.filtros.dataInicio,
      dataFim:    this.filtros.dataFim
    });
  }

  // ── Gera o relatório Excel da lista respeitando a busca e os filtros atuais ──
  gerarRelatorioExcel(): void {
    this.relatorioService.excelLista({
      termo:      this.termoBusca || undefined,
      ativo:      this.filtros.ativo,
      tipo:       this.filtros.tipo,
      dataInicio: this.filtros.dataInicio,
      dataFim:    this.filtros.dataFim
    });
  }

  // ── Abre o modal de importação via Excel ──
  abrirModalImportar(): void {
    this.dialog.open(ModalImportarComponent, {
      width: '640px',
      disableClose: true
    }).afterClosed().subscribe((houve: boolean) => {
      if (houve) this.carregarTodos(); // recarrega se importou algum cliente
    });
  }

  // ── Formata CPF (11 dígitos) ou CNPJ (14 dígitos) para exibição ──
  formatarDocumento(cpfCnpj: string): string {
    const n = cpfCnpj.replace(/\D/g, '');
    if (n.length === 11) return n.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    if (n.length === 14) return n.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
    return cpfCnpj;
  }

  // ── Mostra notificação verde de sucesso ──
  private mostrarSucesso(msg: string): void {
    this.snackBar.open(msg, 'Fechar', {
      duration: 3000,
      panelClass: ['snack-sucesso']
    });
  }

  // ── Mostra notificação vermelha de erro ──
  private mostrarErro(msg: string): void {
    this.snackBar.open(msg, 'Fechar', {
      duration: 5000,
      panelClass: ['snack-erro']
    });
  }
}
