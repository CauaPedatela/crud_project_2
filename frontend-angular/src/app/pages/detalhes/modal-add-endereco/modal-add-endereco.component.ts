// ================================================================
// modal-add-endereco.component.ts — Adicionar endereço a cliente
//
// Recebe via MAT_DIALOG_DATA: { clienteId, enderecos[] }
// Ao salvar: envia todos os endereços existentes + o novo
// via clienteService.atualizar() (o backend sincroniza a lista).
// ================================================================

import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, of } from 'rxjs';

import { ClienteService } from '../../../services/cliente.service';
import { ViaCepService }  from '../../../services/viacep.service';
import { IbgeService }    from '../../../services/ibge.service';
import {
  Endereco, EnderecoDTO, TipoEndereco,
  Estado, Cidade
} from '../../../models/cliente.model';

@Component({
  selector: 'app-modal-add-endereco',
  templateUrl: './modal-add-endereco.component.html'
})
export class ModalAddEnderecoComponent implements OnInit {

  form: FormGroup;
  salvando = false;

  // Dropdowns dinâmicos (IBGE)
  estados$: Observable<Estado[]> = of([]);
  cidades$: Observable<Cidade[]> = of([]);

  constructor(
    private fb:             FormBuilder,
    private dialogRef:      MatDialogRef<ModalAddEnderecoComponent>,
    private clienteService: ClienteService,
    private viaCepService:  ViaCepService,
    private ibgeService:    IbgeService,
    private snackBar:       MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: { clienteId: number; enderecos: Endereco[] }
  ) {
    // Novo endereço começa como não-principal (já existe um principal).
    // CEP e número agora são obrigatórios (número aceita "SN").
    const jaPossuiPrincipal = data.enderecos.some(e => e.principal);
    this.form = this.fb.group({
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
      principal:   [!jaPossuiPrincipal] // principal só se não houver nenhum ainda
    });
  }

  ngOnInit(): void {
    // Carrega estados do IBGE ao abrir o modal
    this.estados$ = this.ibgeService.listarEstados();
  }

  /**
   * Disparado quando o usuário troca o estado no dropdown.
   * Carrega as cidades correspondentes e zera a cidade selecionada.
   */
  onEstadoChange(uf: string): void {
    if (!uf) {
      this.cidades$ = of([]);
      this.form.get('cidade')?.setValue('');
      return;
    }
    this.cidades$ = this.ibgeService.listarCidades(uf);
    this.form.get('cidade')?.setValue('');
  }

  // ── Busca o CEP e preenche os campos automaticamente ──
  buscarCep(): void {
    this.viaCepService.buscar(this.form.get('cep')?.value ?? '').subscribe(resp => {
      if (!resp || resp.erro) return;
      this.form.patchValue({
        logradouro: resp.logradouro,
        bairro:     resp.bairro,
        estado:     resp.uf
      });

      // Após receber UF, carrega cidades e seleciona a cidade do ViaCEP
      if (resp.uf) {
        this.cidades$ = this.ibgeService.listarCidades(resp.uf);
        setTimeout(() => this.form.patchValue({ cidade: resp.localidade }), 50);
      }
    });
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
    const v = this.form.getRawValue();

    // Novo endereço montado
    const novoEndereco: EnderecoDTO = {
      tipo:        v.tipo,
      logradouro:  v.logradouro.trim(),
      numero:      v.numero || undefined,
      complemento: v.complemento || undefined,
      bairro:      v.bairro.trim(),
      cidade:      v.cidade.trim(),
      estado:      v.estado,
      cep:         v.cep.replace(/\D/g, ''),
      pais:        v.pais || 'Brasil',
      telefone:    v.telefone ? v.telefone.replace(/\D/g, '') : undefined,
      principal:   v.principal
    };

    // Se o novo é principal, desmarca o anterior
    const enderecosMapeados: EnderecoDTO[] = this.data.enderecos.map(e => ({
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
      principal:   novoEndereco.principal ? false : e.principal
    }));

    const clienteDto = {
      tipoPessoa:   undefined as any,  // backend ignora campos imutáveis no PUT
      nome:         undefined as any,
      cpfCnpj:      undefined as any,
      email:        undefined as any,
      ativo:        undefined as any,
      enderecos:    [...enderecosMapeados, novoEndereco]
    } as any;

    // Busca o cliente completo para montar o DTO corretamente
    this.clienteService.buscarPorId(this.data.clienteId).subscribe({
      next: cliente => {
        const dto = {
          tipoPessoa:          cliente.tipoPessoa,
          nome:                cliente.nome,
          cpfCnpj:             cliente.cpfCnpj,
          rgInscricaoEstadual: cliente.rgInscricaoEstadual,
          dataNascimento:      cliente.dataNascimento,
          email:               cliente.email,
          ativo:               cliente.ativo,
          enderecos:           [...enderecosMapeados, novoEndereco]
        };
        this.clienteService.atualizar(this.data.clienteId, dto).subscribe({
          next: () => {
            this.salvando = false;
            this.dialogRef.close(true);
          },
          error: (err) => {
            this.salvando = false;
            const msg = err?.error?.message || 'Erro ao adicionar endereço.';
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
