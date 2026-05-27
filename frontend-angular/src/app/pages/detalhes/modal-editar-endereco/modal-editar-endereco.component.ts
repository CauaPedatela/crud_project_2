// ================================================================
// modal-editar-endereco.component.ts — Editar endereço existente
//
// Recebe via MAT_DIALOG_DATA: { cliente: Cliente, endereco: Endereco }
// Campos editáveis: numero, complemento, telefone, principal
// Campos somente leitura: logradouro, bairro, cidade, estado, cep
// (logradouro não muda sem trocar o endereço inteiro)
// ================================================================

import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ClienteService } from '../../../services/cliente.service';
import { Cliente, Endereco, EnderecoDTO } from '../../../models/cliente.model';

@Component({
  selector: 'app-modal-editar-endereco',
  templateUrl: './modal-editar-endereco.component.html'
})
export class ModalEditarEnderecoComponent {

  form: FormGroup;
  salvando = false;

  constructor(
    private fb:             FormBuilder,
    private dialogRef:      MatDialogRef<ModalEditarEnderecoComponent>,
    private clienteService: ClienteService,
    private snackBar:       MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: { cliente: Cliente; endereco: Endereco }
  ) {
    const end = data.endereco;

    // Campos readonly inicializados mas desabilitados — usamos getRawValue() ao salvar
    this.form = this.fb.group({
      // ── Somente leitura (referência visual) ──
      logradouro: [{ value: end.logradouro, disabled: true }],
      bairro:     [{ value: end.bairro,     disabled: true }],
      cidade:     [{ value: end.cidade,     disabled: true }],
      estado:     [{ value: end.estado,     disabled: true }],
      cep:        [{ value: end.cep,        disabled: true }],

      // ── Editáveis ── (número agora é obrigatório — aceita "SN")
      numero:      [end.numero     || '', Validators.required],
      complemento: [end.complemento || ''],
      telefone:    [end.telefone   || ''],
      principal:   [end.principal]
    });

    // Se o número for SN, desabilita o campo de texto
    if (end.numero === 'SN') {
      this.form.get('numero')?.disable();
    }
  }

  // ── Alterna o número entre campo livre e "SN" ──
  toggleSemNumero(): void {
    const ctrl = this.form.get('numero');
    if (!ctrl) return;
    if (ctrl.value === 'SN') {
      ctrl.setValue('');
      ctrl.enable();
    } else {
      ctrl.setValue('SN');
      ctrl.disable();
    }
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.salvando = true;
    const v = this.form.getRawValue(); // inclui os campos disabled

    const enderecoEditado = this.data.endereco;

    // Se este endereço virar principal, desmarca todos os outros
    const enderecosMapeados: EnderecoDTO[] = this.data.cliente.enderecos.map(e => {
      if (e.id === enderecoEditado.id) {
        // Substitui com os valores editados
        return {
          id:          e.id,
          tipo:        e.tipo,
          logradouro:  e.logradouro,
          numero:      v.numero || undefined,
          complemento: v.complemento || undefined,
          bairro:      e.bairro,
          cidade:      e.cidade,
          estado:      e.estado,
          cep:         e.cep,
          pais:        e.pais,
          telefone:    v.telefone ? v.telefone.replace(/\D/g, '') : undefined,
          principal:   v.principal
        };
      }
      return {
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
        // Se o editado virou principal, desmarca os demais
        principal: v.principal ? false : e.principal
      };
    });

    // Busca o cliente completo para montar o DTO com todos os campos imutáveis
    this.clienteService.buscarPorId(this.data.cliente.id).subscribe({
      next: cliente => {
        const dto = {
          tipoPessoa:          cliente.tipoPessoa,
          nome:                cliente.nome,
          cpfCnpj:             cliente.cpfCnpj,
          rgInscricaoEstadual: cliente.rgInscricaoEstadual,
          dataNascimento:      cliente.dataNascimento,
          email:               cliente.email,
          ativo:               cliente.ativo,
          enderecos:           enderecosMapeados
        };
        this.clienteService.atualizar(this.data.cliente.id, dto).subscribe({
          next: () => {
            this.salvando = false;
            this.dialogRef.close(true);
          },
          error: (err) => {
            this.salvando = false;
            const msg = err?.error?.message || 'Erro ao atualizar endereço.';
            this.snackBar.open(msg, 'Fechar', { duration: 5000, panelClass: ['snack-erro'] });
          }
        });
      },
      error: () => {
        this.salvando = false;
        this.snackBar.open('Erro ao carregar cliente.', 'Fechar', { duration: 4000 });
      }
    });
  }

  cancelar(): void {
    this.dialogRef.close();
  }
}
