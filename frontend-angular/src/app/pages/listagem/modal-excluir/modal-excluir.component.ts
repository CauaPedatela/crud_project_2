// ================================================================
// modal-excluir.component.ts — Confirmação de exclusão de cliente
// ================================================================

import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ClienteService } from '../../../services/cliente.service';
import { Cliente } from '../../../models/cliente.model';

@Component({
  selector: 'app-modal-excluir',
  templateUrl: './modal-excluir.component.html'
})
export class ModalExcluirComponent {

  excluindo = false;

  constructor(
    private dialogRef:      MatDialogRef<ModalExcluirComponent>,
    private clienteService: ClienteService,
    private snackBar:       MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public cliente: Cliente
  ) {}

  confirmar(): void {
    this.excluindo = true;
    this.clienteService.excluir(this.cliente.id).subscribe({
      next: () => {
        this.excluindo = false;
        this.dialogRef.close(true); // avisa o pai que foi excluído
      },
      error: (err) => {
        this.excluindo = false;
        const msg = err?.error?.message || 'Erro ao excluir cliente.';
        this.snackBar.open(msg, 'Fechar', { duration: 5000, panelClass: ['snack-erro'] });
      }
    });
  }

  cancelar(): void {
    this.dialogRef.close();
  }
}
