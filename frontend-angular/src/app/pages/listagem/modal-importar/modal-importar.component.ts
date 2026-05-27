// ================================================================
// modal-importar.component.ts — Importação em lote via Excel
//
// Fluxo:
//  1. Usuário baixa a planilha modelo (botão)
//  2. Preenche os dados e faz upload do .xlsx
//  3. Backend processa e retorna ImportacaoResultado
//  4. Modal exibe quantos foram importados e lista de erros
// ================================================================

import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar }  from '@angular/material/snack-bar';

import { ClienteService }      from '../../../services/cliente.service';
import { RelatorioService }    from '../../../services/relatorio.service';
import { ImportacaoResultado } from '../../../models/cliente.model';

@Component({
  selector: 'app-modal-importar',
  templateUrl: './modal-importar.component.html'
})
export class ModalImportarComponent {

  arquivoSelecionado: File | null = null;
  importando = false;
  resultado: ImportacaoResultado | null = null;

  constructor(
    private dialogRef:      MatDialogRef<ModalImportarComponent>,
    private clienteService: ClienteService,
    public  relatorioService: RelatorioService,
    private snackBar:       MatSnackBar
  ) {}

  // ── Captura o arquivo quando o usuário seleciona no input ──
  onArquivoSelecionado(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.arquivoSelecionado = input.files?.[0] ?? null;
    this.resultado = null; // limpa resultado anterior ao trocar arquivo
  }

  // ── Envia o arquivo para o backend e exibe o resultado ──
  importar(): void {
    if (!this.arquivoSelecionado) return;

    this.importando = true;
    this.clienteService.importar(this.arquivoSelecionado).subscribe({
      next: (res) => {
        this.importando = false;
        this.resultado = res;
      },
      error: (err) => {
        this.importando = false;
        const msg = err?.error?.message || 'Erro ao importar o arquivo.';
        this.snackBar.open(msg, 'Fechar', { duration: 5000, panelClass: ['snack-erro'] });
      }
    });
  }

  // ── Fecha o modal; retorna true se algum cliente foi importado ──
  fechar(): void {
    const houve = this.resultado != null && this.resultado.sucessos > 0;
    this.dialogRef.close(houve);
  }
}
