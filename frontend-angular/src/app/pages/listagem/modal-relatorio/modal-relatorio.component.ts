// ================================================================
// modal-relatorio.component.ts — Diálogo para gerar relatório
//
// Recebe o ID do cliente via MAT_DIALOG_DATA.
// Não chama API — apenas abre URLs via RelatorioService.
// ================================================================

import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { RelatorioService } from '../../../services/relatorio.service';

@Component({
  selector: 'app-modal-relatorio',
  templateUrl: './modal-relatorio.component.html'
})
export class ModalRelatorioComponent {

  constructor(
    private dialogRef:       MatDialogRef<ModalRelatorioComponent>,
    public  relatorioService: RelatorioService,
    @Inject(MAT_DIALOG_DATA) public clienteId: number
  ) {}

  fechar(): void {
    this.dialogRef.close();
  }
}
