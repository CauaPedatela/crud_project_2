// ================================================================
// modal-editar-endereco.component.ts — Editar endereço existente
//
// Recebe via MAT_DIALOG_DATA: { cliente: Cliente, endereco: Endereco }
//
// Todos os campos são editáveis. Estado e Cidade vêm da API do IBGE
// (dropdowns dinâmicos). Ao alterar o CEP, o ViaCEP preenche tudo
// e o dropdown de cidade é carregado automaticamente para o estado retornado.
// ================================================================

import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, of } from 'rxjs';

import { ClienteService } from '../../../services/cliente.service';
import { ViaCepService } from '../../../services/viacep.service';
import { IbgeService } from '../../../services/ibge.service';
import {
  Cliente, Endereco, EnderecoDTO,
  Estado, Cidade
} from '../../../models/cliente.model';

@Component({
  selector: 'app-modal-editar-endereco',
  templateUrl: './modal-editar-endereco.component.html'
})
export class ModalEditarEnderecoComponent implements OnInit {

  form: FormGroup;
  salvando = false;

  // Dropdowns dinâmicos
  estados$: Observable<Estado[]> = of([]);
  cidades$: Observable<Cidade[]> = of([]);

  constructor(
    private fb:             FormBuilder,
    private dialogRef:      MatDialogRef<ModalEditarEnderecoComponent>,
    private clienteService: ClienteService,
    private viaCepService:  ViaCepService,
    private ibgeService:    IbgeService,
    private snackBar:       MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: { cliente: Cliente; endereco: Endereco }
  ) {
    const end = data.endereco;

    this.form = this.fb.group({
      logradouro:  [end.logradouro || '', Validators.required],
      numero:      [end.numero     || '', Validators.required],
      complemento: [end.complemento || ''],
      bairro:      [end.bairro     || '', Validators.required],
      cidade:      [end.cidade     || '', Validators.required],
      estado:      [end.estado     || '', [Validators.required, Validators.maxLength(2)]],
      cep:         [end.cep        || '', Validators.required],
      pais:        [end.pais       || 'Brasil', Validators.required],
      telefone:    [end.telefone   || ''],
      principal:   [end.principal]
    });

    if (end.numero === 'SN') {
      this.form.get('numero')?.disable();
    }
  }

  ngOnInit(): void {
    // Carrega a lista de estados do IBGE assim que o modal abre
    this.estados$ = this.ibgeService.listarEstados();

    // Já carrega as cidades do estado atual (se houver) pra mostrar selecionado
    const ufAtual = this.form.get('estado')?.value;
    if (ufAtual) {
      this.cidades$ = this.ibgeService.listarCidades(ufAtual);
    }
  }

  /**
   * Disparado quando o usuário seleciona um estado no dropdown.
   * Carrega as cidades correspondentes e limpa a cidade selecionada
   * (a anterior pode não existir no novo estado).
   */
  onEstadoChange(uf: string): void {
    if (!uf) {
      this.cidades$ = of([]);
      this.form.get('cidade')?.setValue('');
      return;
    }
    this.cidades$ = this.ibgeService.listarCidades(uf);
    // Limpa a cidade se trocou de estado — usuário escolhe a nova
    if (uf !== this.data.endereco.estado) {
      this.form.get('cidade')?.setValue('');
    }
  }

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

  /**
   * Consulta o ViaCEP no blur do campo CEP. Quando vem resposta válida:
   *   - Preenche logradouro/bairro
   *   - Define o estado (UF)
   *   - Dispara onEstadoChange para carregar as cidades daquele estado
   *   - Define a cidade após as cidades estarem carregadas
   */
  buscarCep(): void {
    const cep = this.form.get('cep')?.value;
    if (!cep) return;

    this.viaCepService.buscar(cep).subscribe(resposta => {
      if (!resposta || resposta.erro) return;

      this.form.patchValue({
        logradouro: resposta.logradouro || this.form.get('logradouro')?.value,
        bairro:     resposta.bairro     || this.form.get('bairro')?.value,
        estado:     resposta.uf         || this.form.get('estado')?.value
      });

      // Carrega cidades do estado retornado e seleciona a cidade do ViaCEP
      if (resposta.uf) {
        this.cidades$ = this.ibgeService.listarCidades(resposta.uf);
        // Aguarda 1 tick para o dropdown popular antes de setar o valor
        // (mat-select precisa das options no DOM pra exibir a seleção).
        setTimeout(() => {
          this.form.patchValue({ cidade: resposta.localidade });
        }, 50);
      }
    });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.salvando = true;
    const v = this.form.getRawValue();
    const enderecoEditado = this.data.endereco;

    const enderecosMapeados: EnderecoDTO[] = this.data.cliente.enderecos.map(e => {
      if (e.id === enderecoEditado.id) {
        return {
          id:          e.id,
          tipo:        e.tipo,
          logradouro:  v.logradouro.trim(),
          numero:      v.numero || undefined,
          complemento: v.complemento || undefined,
          bairro:      v.bairro.trim(),
          cidade:      v.cidade.trim(),
          estado:      v.estado.toUpperCase(),
          cep:         v.cep.replace(/\D/g, ''),
          pais:        v.pais.trim(),
          telefone:    v.telefone ? v.telefone.replace(/\D/g, '') : undefined,
          principal:   v.principal
        };
      }
      return {
        id: e.id, tipo: e.tipo,
        logradouro: e.logradouro, numero: e.numero, complemento: e.complemento,
        bairro: e.bairro, cidade: e.cidade, estado: e.estado,
        cep: e.cep, pais: e.pais, telefone: e.telefone,
        principal: v.principal ? false : e.principal
      };
    });

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
