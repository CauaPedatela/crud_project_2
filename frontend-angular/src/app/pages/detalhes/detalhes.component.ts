// ================================================================
// detalhes.component.ts — Página de detalhes de um cliente
//
// Responsabilidades:
//   - Carregar os dados do cliente pelo ID da URL
//   - Exibir card com dados pessoais e lista de endereços
//   - Abrir modais para: editar cliente, relatório,
//     adicionar/editar/excluir endereço
// ================================================================

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog }   from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ClienteService }   from '../../services/cliente.service';
import { RelatorioService } from '../../services/relatorio.service';
import { Cliente, Endereco } from '../../models/cliente.model';

import { ModalEditarComponent }          from '../listagem/modal-editar/modal-editar.component';
import { ModalRelatorioComponent }       from '../listagem/modal-relatorio/modal-relatorio.component';
import { ModalAddEnderecoComponent }     from './modal-add-endereco/modal-add-endereco.component';
import { ModalEditarEnderecoComponent }  from './modal-editar-endereco/modal-editar-endereco.component';
import { ModalExcluirEnderecoComponent } from './modal-excluir-endereco/modal-excluir-endereco.component';

@Component({
  selector: 'app-detalhes',
  templateUrl: './detalhes.component.html',
  styleUrls: ['./detalhes.component.scss']
})
export class DetalhesComponent implements OnInit {

  cliente: Cliente | null = null;
  carregando = true;

  constructor(
    private route:          ActivatedRoute,
    private router:         Router,
    private clienteService: ClienteService,
    public  relatorioService: RelatorioService,
    private dialog:   MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    // Lê o parâmetro :id da URL e carrega o cliente
    this.route.params.subscribe(params => {
      const id = Number(params['id']);
      this.carregarCliente(id);
    });
  }

  // ── Busca os dados do cliente no backend ──
  carregarCliente(id: number): void {
    this.carregando = true;
    this.clienteService.buscarPorId(id).subscribe({
      next: c => {
        this.cliente = c;
        this.carregando = false;
      },
      error: () => {
        this.carregando = false;
        this.snackBar.open('Cliente não encontrado.', 'Fechar', {
          duration: 4000, panelClass: ['snack-erro']
        });
        this.router.navigate(['/clientes']);
      }
    });
  }

  // ── Indica se o cliente é Pessoa Jurídica ──
  get isPJ(): boolean {
    return this.cliente?.tipoPessoa === 'JURIDICA';
  }

  // ── Gera as iniciais do nome para o avatar ──
  getIniciais(nome: string): string {
    const partes = nome.trim().split(' ');
    if (partes.length === 1) return partes[0].substring(0, 2).toUpperCase();
    return (partes[0][0] + partes[partes.length - 1][0]).toUpperCase();
  }

  // ── Formata data ISO "yyyy-MM-dd" para "dd/MM/yyyy" ──
  formatarData(iso: string | undefined): string {
    if (!iso) return '—';
    const [ano, mes, dia] = iso.split('T')[0].split('-');
    return `${dia}/${mes}/${ano}`;
  }

  // ── Formata CPF ou CNPJ para exibição ──
  formatarDocumento(cpfCnpj: string): string {
    const n = cpfCnpj.replace(/\D/g, '');
    if (n.length === 11) return n.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    if (n.length === 14) return n.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
    return cpfCnpj;
  }

  // ── Formata CEP "01310100" → "01310-100" ──
  formatarCep(cep: string): string {
    const n = cep.replace(/\D/g, '');
    return n.length === 8 ? `${n.substring(0,5)}-${n.substring(5)}` : cep;
  }

  // ── Abre modal de edição de dados do cliente ──
  abrirModalEditar(): void {
    if (!this.cliente) return;
    this.dialog.open(ModalEditarComponent, {
      width: '600px',
      data: this.cliente
    }).afterClosed().subscribe((editado: boolean) => {
      if (editado) {
        this.carregarCliente(this.cliente!.id);
        this.mostrarSucesso('Cliente atualizado com sucesso!');
      }
    });
  }

  // ── Abre modal de relatório ──
  abrirModalRelatorio(): void {
    if (!this.cliente) return;
    this.dialog.open(ModalRelatorioComponent, {
      width: '380px',
      data: this.cliente.id
    });
  }

  // ── Abre modal para adicionar novo endereço ──
  abrirModalAddEndereco(): void {
    if (!this.cliente) return;
    this.dialog.open(ModalAddEnderecoComponent, {
      width: '720px',
      maxHeight: '90vh',
      data: { clienteId: this.cliente.id, enderecos: this.cliente.enderecos }
    }).afterClosed().subscribe((adicionado: boolean) => {
      if (adicionado) {
        this.carregarCliente(this.cliente!.id);
        this.mostrarSucesso('Endereço adicionado com sucesso!');
      }
    });
  }

  // ── Abre modal para editar um endereço existente ──
  abrirModalEditarEndereco(endereco: Endereco): void {
    if (!this.cliente) return;
    this.dialog.open(ModalEditarEnderecoComponent, {
      width: '560px',
      data: { cliente: this.cliente, endereco }
    }).afterClosed().subscribe((editado: boolean) => {
      if (editado) {
        this.carregarCliente(this.cliente!.id);
        this.mostrarSucesso('Endereço atualizado com sucesso!');
      }
    });
  }

  // ── Abre modal para excluir um endereço ──
  abrirModalExcluirEndereco(endereco: Endereco): void {
    if (!this.cliente) return;

    // Regra: não pode excluir o único endereço
    if (this.cliente.enderecos.length === 1) {
      this.snackBar.open(
        'O cliente deve ter pelo menos um endereço.',
        'Entendi', { duration: 4000 }
      );
      return;
    }

    // Regra: não pode excluir o endereço principal sem antes promover outro.
    // O usuário precisa ir em "Editar" de outro endereço e marcá-lo como principal.
    if (endereco.principal) {
      this.snackBar.open(
        'Não é possível excluir o endereço principal. Defina outro endereço como principal antes de excluir este.',
        'Entendi', { duration: 5000 }
      );
      return;
    }

    this.dialog.open(ModalExcluirEnderecoComponent, {
      width: '450px',
      data: { cliente: this.cliente, endereco }
    }).afterClosed().subscribe((excluido: boolean) => {
      if (excluido) {
        this.carregarCliente(this.cliente!.id);
        this.mostrarSucesso('Endereço removido com sucesso!');
      }
    });
  }

  private mostrarSucesso(msg: string): void {
    this.snackBar.open(msg, 'Fechar', {
      duration: 3000, panelClass: ['snack-sucesso']
    });
  }

  voltar(): void {
    this.router.navigate(['/clientes']);
  }
}
