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
import { MatPaginator, PageEvent } from '@angular/material/paginator';
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
  // MatTableDataSource é usado como container reativo da tabela.
  // Paginação é server-side agora — apenas o sort fica client-side dentro da página atual.
  dataSource = new MatTableDataSource<Cliente>();
  // Colunas que aparecem na tabela, na ordem da esquerda para a direita
  colunas: string[] = ['numero', 'nome', 'tipo', 'documento', 'email', 'status', 'acoes'];

  // Referências aos controles de paginação e ordenação declarados no HTML
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  // ── Estado da busca ──
  termoBusca = '';
  filtros: FiltrosState = {};
  carregando = false;

  // ── Estado da paginação server-side ──
  // O MatPaginator não pagina mais em memória — apenas dispara onPageChange,
  // que altera pageIndex/pageSize e chama carregar() pra pedir a página nova ao backend.
  pageIndex = 0;
  pageSize = 10;
  totalElements = 0;

  // ── Contadores do cabeçalho ──
  // Populados pelo endpoint /api/clientes/contadores (dois COUNT no banco).
  // Não carregamos mais a lista inteira de clientes só para contar.
  totalClientes = 0;
  totalAtivos = 0;

  constructor(
    private clienteService:   ClienteService,
    public  relatorioService: RelatorioService,
    private dialog:  MatDialog,
    private snackBar: MatSnackBar
  ) {}

  // Carrega a primeira página e os contadores ao abrir a página
  ngOnInit(): void {
    this.carregar();
    this.carregarContadores();
  }

  // Conecta apenas o sort ao dataSource — paginator agora é server-side.
  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
  }

  // ── Carrega a página atual do backend ──
  // Método único que sempre pede ao servidor a fatia correspondente a
  // (pageIndex, pageSize) + filtros atuais. Substitui o antigo carregarTodos().
  carregar(): void {
    this.carregando = true;
    this.clienteService.buscarComFiltros({
      termo:      this.termoBusca || undefined,
      ativo:      this.filtros.ativo,
      tipo:       this.filtros.tipo,
      dataInicio: this.filtros.dataInicio,
      dataFim:    this.filtros.dataFim,
      page:       this.pageIndex,
      size:       this.pageSize
    }).subscribe({
      next: response => {
        this.dataSource.data = response.content;
        this.totalElements = response.totalElements;
        this.carregando = false;
      },
      error: () => {
        this.mostrarErro('Não foi possível carregar a lista de clientes.');
        this.carregando = false;
      }
    });
  }

  // ── Carrega apenas os contadores do header ──
  // Endpoint dedicado /contadores devolve { total, ativos } sem trazer nenhum cliente.
  carregarContadores(): void {
    this.clienteService.contadores().subscribe({
      next: c => {
        this.totalClientes = c.total;
        this.totalAtivos = c.ativos;
      }
    });
  }

  // ── Disparado pelo MatPaginator quando muda página ou tamanho ──
  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.carregar();
  }

  // ── Executa busca com o termo e os filtros atuais ──
  // Reseta para a primeira página — caso contrário, se o usuário está na página 5
  // e filtra, pode acabar pedindo uma página que nem existe no resultado filtrado.
  buscar(): void {
    this.pageIndex = 0;
    this.carregar();
  }

  // ── Limpa a busca e volta a mostrar a primeira página ──
  limparBusca(): void {
    this.termoBusca = '';
    this.filtros = {};
    this.pageIndex = 0;
    this.carregar();
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
        this.carregar();
        this.carregarContadores();
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
        this.carregar();
        this.carregarContadores();  // status ativo/inativo pode ter mudado
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
        this.carregar();
        this.carregarContadores();
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
      if (houve) {
        this.carregar();           // recarrega a página atual
        this.carregarContadores(); // atualiza os totais do header
      }
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
