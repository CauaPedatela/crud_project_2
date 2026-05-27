// ================================================================
// modal-excluir-endereco.component.ts — Confirmação de exclusão
//
// Recebe via MAT_DIALOG_DATA: { cliente: Cliente, endereco: Endereco }
// Ao confirmar: remove o endereço da lista e chama clienteService.atualizar()
//
// Regra de negócio: se o endereço excluído era o principal e ainda
// existem outros, o primeiro da lista restante vira principal automaticamente.
// ================================================================

import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ClienteService } from '../../../services/cliente.service';
import { Cliente, Endereco, EnderecoDTO } from '../../../models/cliente.model';

@Component({
  selector: 'app-modal-excluir-endereco',
  templateUrl: './modal-excluir-endereco.component.html'
})
export class ModalExcluirEnderecoComponent {

  excluindo = false;

  constructor(
    private dialogRef:      MatDialogRef<ModalExcluirEnderecoComponent>,
    private clienteService: ClienteService,
    private snackBar:       MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: { cliente: Cliente; endereco: Endereco }
  ) {}

  confirmar(): void {
    this.excluindo = true;

    // Remove o endereço da lista, mantendo os outros
    let enderecosFiltrados: EnderecoDTO[] = this.data.cliente.enderecos
      .filter(e => e.id !== this.data.endereco.id)
      .map(e => ({
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
      }));

    // Se o endereço excluído era o principal, o primeiro da lista restante assume
    const eraOPrincipal = this.data.endereco.principal;
    if (eraOPrincipal && enderecosFiltrados.length > 0) {
      enderecosFiltrados[0] = { ...enderecosFiltrados[0], principal: true };
    }

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
          enderecos:           enderecosFiltrados
        };
        this.clienteService.atualizar(this.data.cliente.id, dto).subscribe({
          next: () => {
            this.excluindo = false;
            this.dialogRef.close(true);
          },
          error: (err) => {
            this.excluindo = false;
            const msg = err?.error?.message || 'Erro ao excluir endereço.';
            this.snackBar.open(msg, 'Fechar', { duration: 5000, panelClass: ['snack-erro'] });
          }
        });
      },
      error: () => {
        this.excluindo = false;
        this.snackBar.open('Erro ao carregar cliente.', 'Fechar', { duration: 4000 });
      }
    });
  }

  cancelar(): void {
    this.dialogRef.close();
  }
}
