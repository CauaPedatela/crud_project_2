// ================================================================
// modal-filtros.component.ts — Diálogo de filtros avançados
//
// Não chama nenhuma API — apenas coleta os filtros e retorna
// o objeto para o ListagemComponent via dialogRef.close(filtros).
// ================================================================

import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-modal-filtros',
  templateUrl: './modal-filtros.component.html'
})
export class ModalFiltrosComponent {

  form: FormGroup;

  constructor(
    private fb:        FormBuilder,
    private dialogRef: MatDialogRef<ModalFiltrosComponent>,
    @Inject(MAT_DIALOG_DATA) filtrosAtuais: any
  ) {
    // Pré-preenche com os filtros que já estavam ativos.
    // As datas vêm armazenadas em ISO (yyyy-MM-dd) — convertemos pra BR (dd/MM/yyyy)
    // antes de passar para o input com máscara, pra que o campo mostre certo ao reabrir.
    this.form = this.fb.group({
      ativo:       [filtrosAtuais?.ativo ?? ''],
      tipo:        [filtrosAtuais?.tipo ?? ''],
      dataInicio:  [this.brData(filtrosAtuais?.dataInicio)],
      dataFim:     [this.brData(filtrosAtuais?.dataFim)]
    });
  }

  // ── Converte ISO "yyyy-MM-dd" de volta para "ddMMyyyy" (formato que o ngx-mask exibe como dd/MM/yyyy) ──
  // Quando o usuário aplicou um filtro e reabre o modal, este método garante que
  // o campo continue exibindo a data em formato brasileiro.
  private brData(iso: string | undefined | null): string {
    if (!iso) return '';
    const partes = iso.split('-');
    if (partes.length !== 3) return '';
    const [ano, mes, dia] = partes;
    return `${dia}${mes}${ano}`; // ngx-mask formata automaticamente como "dd/MM/yyyy"
  }

  // ── Aplica os filtros: retorna o objeto para o pai ──
  aplicar(): void {
    const v = this.form.value;
    this.dialogRef.close({
      ativo:      v.ativo      || undefined,
      tipo:       v.tipo       || undefined,
      dataInicio: v.dataInicio ? this.isoData(v.dataInicio) : undefined,
      dataFim:    v.dataFim    ? this.isoData(v.dataFim)    : undefined
    });
  }

  // ── Limpa todos os filtros e retorna objeto vazio ──
  limpar(): void {
    this.dialogRef.close({});
  }

  cancelar(): void {
    this.dialogRef.close(undefined); // undefined = pai não altera os filtros atuais
  }

  // ── Converte "ddMMyyyy" (máscara) para "yyyy-MM-dd" (ISO) ──
  private isoData(raw: string): string {
    const n = raw.replace(/\D/g, '');
    if (n.length !== 8) return '';
    return `${n.substring(4)}-${n.substring(2, 4)}-${n.substring(0, 2)}`;
  }
}
