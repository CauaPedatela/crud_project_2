// ================================================================
// modal-criar.component.ts — Diálogo para cadastrar novo cliente
//
// Abre via MatDialog. Fecha com `true` se o cadastro foi feito,
// ou `undefined` se o usuário cancelou.
//
// Formulário:
//   - FormGroup principal (tipoPessoa, nome, cpfCnpj, etc.)
//   - FormArray `enderecos` — permite múltiplos endereços
//   - Exatamente 1 endereço deve ter principal=true
// ================================================================

import { Component } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar }  from '@angular/material/snack-bar';

import { ClienteService } from '../../../services/cliente.service';
import { ViaCepService }  from '../../../services/viacep.service';
import { ClienteDTO, EnderecoDTO, TipoPessoa, TipoEndereco } from '../../../models/cliente.model';

// Lista dos estados brasileiros para o select de UF
export const UF_LIST = [
  'AC','AL','AP','AM','BA','CE','DF','ES','GO','MA',
  'MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN',
  'RS','RO','RR','SC','SP','SE','TO'
];

@Component({
  selector: 'app-modal-criar',
  templateUrl: './modal-criar.component.html'
})
export class ModalCriarComponent {

  form: FormGroup;
  salvando = false;
  ufOptions = UF_LIST;

  constructor(
    private fb:             FormBuilder,
    private dialogRef:      MatDialogRef<ModalCriarComponent>,
    private clienteService: ClienteService,
    private viaCepService:  ViaCepService,
    private snackBar:       MatSnackBar
  ) {
    // Monta o formulário reativo com validações.
    // Campos obrigatórios refletem as regras do ClienteValidator do backend
    // — assim o usuário recebe feedback antes do submit chegar ao servidor.
    this.form = this.fb.group({
      tipoPessoa:           ['FISICA', Validators.required],
      nome:                 ['', Validators.required],
      cpfCnpj:              ['', Validators.required],
      rgInscricaoEstadual:  ['', Validators.required], // RG (PF) ou Inscrição Estadual (PJ)
      dataNascimento:       ['', Validators.required], // nascimento (PF) ou fundação (PJ)
      email:                ['', [Validators.required, Validators.email]],
      ativo:                [true],
      enderecos:            this.fb.array([this.criarFormEndereco(true)]) // 1 endereço inicial = principal
    });
  }

  // ── Getter para o FormArray de endereços (tipagem correta) ──
  get enderecos(): FormArray {
    return this.form.get('enderecos') as FormArray;
  }

  // ── Getter para saber se o tipo é PJ (muda labels) ──
  get isPJ(): boolean {
    return this.form.get('tipoPessoa')?.value === 'JURIDICA';
  }

  // ── Máscara dinâmica de CPF/CNPJ ──
  get maskDocumento(): string {
    return this.isPJ ? '00.000.000/0000-00' : '000.000.000-00';
  }

  // ── Cria um FormGroup para um único endereço ──
  // CEP e número agora são obrigatórios (o número aceita "SN" para sem-número).
  private criarFormEndereco(principal: boolean = false): FormGroup {
    return this.fb.group({
      tipo:        ['RESIDENCIAL' as TipoEndereco, Validators.required],
      cep:         ['', Validators.required],
      logradouro:  ['', Validators.required],
      numero:      ['', Validators.required],
      complemento: [''],
      bairro:      ['', Validators.required],
      cidade:      ['', Validators.required],
      estado:      ['', Validators.required],
      pais:        ['Brasil', Validators.required],
      telefone:    [''],
      principal:   [principal]
    });
  }

  // ── Adiciona um novo endereço vazio ao FormArray ──
  adicionarEndereco(): void {
    this.enderecos.push(this.criarFormEndereco(false));
  }

  // ── Remove o endereço no índice i ──
  removerEndereco(i: number): void {
    if (this.enderecos.length === 1) return; // mínimo 1 endereço
    const eraPrincipal = this.enderecos.at(i).get('principal')?.value;
    this.enderecos.removeAt(i);
    // Se removeu o principal, o primeiro vira o principal
    if (eraPrincipal) {
      this.enderecos.at(0).get('principal')?.setValue(true);
    }
  }

  // ── Marca o endereço i como principal (desmarca todos os outros) ──
  tornarPrincipal(i: number): void {
    this.enderecos.controls.forEach((ctrl, idx) => {
      ctrl.get('principal')?.setValue(idx === i);
    });
  }

  // ── Alterna o número entre digitável e "SN" ──
  toggleSemNumero(i: number): void {
    const ctrl = this.enderecos.at(i).get('numero');
    if (!ctrl) return;
    if (ctrl.value === 'SN') {
      ctrl.setValue('');
      ctrl.enable();
    } else {
      ctrl.setValue('SN');
      ctrl.disable();
    }
  }

  // ── Consulta o ViaCEP quando o usuário sai do campo CEP ──
  buscarCep(i: number): void {
    const cep = this.enderecos.at(i).get('cep')?.value ?? '';
    this.viaCepService.buscar(cep).subscribe(resp => {
      if (!resp || resp.erro) return; // CEP não encontrado — não faz nada
      const end = this.enderecos.at(i);
      end.patchValue({
        logradouro: resp.logradouro,
        bairro:     resp.bairro,
        cidade:     resp.localidade,
        estado:     resp.uf
      });
    });
  }

  // ── Submete o formulário ──
  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched(); // destaca os campos inválidos
      return;
    }
    if (!this.enderecos.controls.some(c => c.get('principal')?.value)) {
      this.snackBar.open('Marque um endereço como principal.', 'OK', { duration: 4000 });
      return;
    }

    this.salvando = true;
    const dto = this.montarDTO();

    this.clienteService.criar(dto).subscribe({
      next: () => {
        this.salvando = false;
        this.dialogRef.close(true); // avisa o pai que algo foi criado
      },
      error: (err) => {
        this.salvando = false;
        const msg = err?.error?.message || 'Erro ao cadastrar cliente.';
        this.snackBar.open(msg, 'Fechar', { duration: 5000, panelClass: ['snack-erro'] });
      }
    });
  }

  // ── Monta o ClienteDTO a partir dos valores do formulário ──
  private montarDTO(): ClienteDTO {
    const v = this.form.getRawValue(); // getRawValue inclui campos disabled (ex: numero SN)

    return {
      tipoPessoa:          v.tipoPessoa as TipoPessoa,
      nome:                v.nome.trim(),
      cpfCnpj:             v.cpfCnpj.replace(/\D/g, ''), // remove máscara
      rgInscricaoEstadual: v.rgInscricaoEstadual || undefined,
      dataNascimento:      this.converterData(v.dataNascimento),
      email:               v.email.trim(),
      ativo:               v.ativo,
      enderecos: v.enderecos.map((e: any): EnderecoDTO => ({
        tipo:        e.tipo,
        logradouro:  e.logradouro.trim(),
        numero:      e.numero || undefined,
        complemento: e.complemento || undefined,
        bairro:      e.bairro.trim(),
        cidade:      e.cidade.trim(),
        estado:      e.estado,
        cep:         e.cep.replace(/\D/g, ''), // remove traço do CEP
        pais:        e.pais || 'Brasil',
        telefone:    e.telefone ? e.telefone.replace(/\D/g, '') : undefined,
        principal:   e.principal
      }))
    };
  }

  // ── Converte "ddMMyyyy" (saída do ngx-mask) para "yyyy-MM-dd" (ISO para o backend) ──
  private converterData(raw: string): string | undefined {
    if (!raw || raw.replace(/\D/g, '').length !== 8) return undefined;
    const n = raw.replace(/\D/g, '');
    return `${n.substring(4)}-${n.substring(2, 4)}-${n.substring(0, 2)}`;
  }

  cancelar(): void {
    this.dialogRef.close(); // fecha sem retornar nada → pai não recarrega
  }
}
