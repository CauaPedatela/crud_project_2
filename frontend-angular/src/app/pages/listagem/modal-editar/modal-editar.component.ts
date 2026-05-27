// ================================================================
// modal-editar.component.ts — Diálogo para editar cliente
//
// Recebe o Cliente completo via MAT_DIALOG_DATA.
// Apenas email, rgInscricaoEstadual e ativo são editáveis.
// CPF/CNPJ, tipoPessoa e nome são exibidos como readonly
// (imutáveis após o cadastro — regra de negócio da seção 7 do PDF).
// ================================================================

import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ClienteService } from '../../../services/cliente.service';
import { Cliente, ClienteDTO } from '../../../models/cliente.model';

@Component({
  selector: 'app-modal-editar',
  templateUrl: './modal-editar.component.html'
})
export class ModalEditarComponent {

  form: FormGroup;
  salvando = false;

  constructor(
    private fb:             FormBuilder,
    private dialogRef:      MatDialogRef<ModalEditarComponent>,
    private clienteService: ClienteService,
    private snackBar:       MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public cliente: Cliente  // dados do cliente recebidos do pai
  ) {
    // Inicializa o form com os valores atuais do cliente
    this.form = this.fb.group({
      email:               [cliente.email,               [Validators.required, Validators.email]],
      rgInscricaoEstadual: [cliente.rgInscricaoEstadual ?? ''],
      ativo:               [cliente.ativo]
    });
  }

  // ── Indica se o cliente é Pessoa Jurídica (mostra campo IE) ──
  get isPJ(): boolean {
    return this.cliente.tipoPessoa === 'JURIDICA';
  }

  // ── Formata CPF ou CNPJ para exibição no campo readonly ──
  get documentoFormatado(): string {
    const n = this.cliente.cpfCnpj.replace(/\D/g, '');
    if (n.length === 11) return n.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    if (n.length === 14) return n.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
    return this.cliente.cpfCnpj;
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.salvando = true;
    const v = this.form.value;

    // Monta o DTO preservando os campos imutáveis e os endereços existentes
    const dto: ClienteDTO = {
      tipoPessoa:          this.cliente.tipoPessoa,     // imutável — backend ignora, mas enviamos
      nome:                this.cliente.nome,           // imutável
      cpfCnpj:             this.cliente.cpfCnpj,        // imutável
      rgInscricaoEstadual: v.rgInscricaoEstadual || undefined,
      dataNascimento:      this.cliente.dataNascimento,
      email:               v.email.trim(),
      ativo:               v.ativo,
      enderecos:           this.cliente.enderecos.map(e => ({
        id:          e.id,
        tipo:        e.tipo,
        logradouro:  e.logradouro,
        numero:      e.numero,
        complemento: e.complemento,
        bairro:      e.bairro,
        cidade:      e.cidade,
        estado:      e.estado,
        cep:         e.cep,
        pais:        e.pais,
        telefone:    e.telefone,
        principal:   e.principal
      }))
    };

    this.clienteService.atualizar(this.cliente.id, dto).subscribe({
      next: () => {
        this.salvando = false;
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.salvando = false;
        const msg = err?.error?.message || 'Erro ao salvar alterações.';
        this.snackBar.open(msg, 'Fechar', { duration: 5000, panelClass: ['snack-erro'] });
      }
    });
  }

  cancelar(): void {
    this.dialogRef.close();
  }
}
