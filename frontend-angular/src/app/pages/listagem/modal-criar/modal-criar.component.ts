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

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar }  from '@angular/material/snack-bar';
import { Observable, of } from 'rxjs';

import { ClienteService } from '../../../services/cliente.service';
import { ViaCepService }  from '../../../services/viacep.service';
import { IbgeService }    from '../../../services/ibge.service';
import {
  ClienteDTO, EnderecoDTO, TipoPessoa, TipoEndereco,
  Estado, Cidade
} from '../../../models/cliente.model';

@Component({
  selector: 'app-modal-criar',
  templateUrl: './modal-criar.component.html'
})
export class ModalCriarComponent implements OnInit {

  form: FormGroup;
  salvando = false;

  // Lista de estados (vem do IBGE — única e compartilhada)
  estados$: Observable<Estado[]> = of([]);

  // Cache de cidades por índice de endereço.
  // Como cada endereço pode ter um estado diferente, mantemos uma lista
  // separada por índice. A chave é o índice no FormArray de endereços.
  cidadesPorIndice = new Map<number, Observable<Cidade[]>>();

  // Observable estável para "lista vazia" — evita o async pipe re-subscrever
  // a cada change detection quando o índice ainda não tem cidades carregadas.
  private readonly EMPTY_CIDADES$: Observable<Cidade[]> = of([]);

  constructor(
    private fb:             FormBuilder,
    private dialogRef:      MatDialogRef<ModalCriarComponent>,
    private clienteService: ClienteService,
    private viaCepService:  ViaCepService,
    private ibgeService:    IbgeService,
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

  ngOnInit(): void {
    // Carrega estados uma vez quando o modal abre
    this.estados$ = this.ibgeService.listarEstados();
  }

  // ── Getter para o FormArray de endereços (tipagem correta) ──
  get enderecos(): FormArray {
    return this.form.get('enderecos') as FormArray;
  }

  /**
   * Retorna o observable de cidades para o endereço no índice i.
   * Se ainda não foi disparada nenhuma consulta para esse índice, devolve of([]).
   */
  cidades$(i: number): Observable<Cidade[]> {
    return this.cidadesPorIndice.get(i) ?? this.EMPTY_CIDADES$;
  }

  /**
   * Chamado quando o usuário troca o estado de um endereço.
   * Carrega as cidades correspondentes e limpa a cidade atual.
   */
  onEstadoChange(i: number, uf: string): void {
    if (!uf) {
      this.cidadesPorIndice.set(i, of([]));
      this.enderecos.at(i).get('cidade')?.setValue('');
      return;
    }
    this.cidadesPorIndice.set(i, this.ibgeService.listarCidades(uf));
    this.enderecos.at(i).get('cidade')?.setValue('');
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
  // Preenche logradouro/bairro/UF de imediato. Para a cidade, primeiro carrega
  // a lista do IBGE para o UF retornado e DEPOIS seleciona a cidade — caso
  // contrário o mat-select fica vazio porque não tem a option ainda.
  buscarCep(i: number): void {
    const cep = this.enderecos.at(i).get('cep')?.value ?? '';
    this.viaCepService.buscar(cep).subscribe(resp => {
      if (!resp || resp.erro) return; // CEP não encontrado — não faz nada
      const end = this.enderecos.at(i);
      end.patchValue({
        logradouro: resp.logradouro,
        bairro:     resp.bairro,
        estado:     resp.uf
      });

      // Carrega cidades do estado retornado e seleciona a cidade após popular
      if (resp.uf) {
        this.cidadesPorIndice.set(i, this.ibgeService.listarCidades(resp.uf));
        setTimeout(() => end.patchValue({ cidade: resp.localidade }), 50);
      }
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

    // Tenta montar o DTO — pode lançar erro se a data for inválida
    let dto: ClienteDTO;
    try {
      dto = this.montarDTO();
    } catch (e: any) {
      this.snackBar.open(e.message || 'Erro ao montar dados.', 'Fechar',
        { duration: 5000, panelClass: ['snack-erro'] });
      return;
    }

    this.salvando = true;
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

  // ── Converte "ddMMyyyy" para "yyyy-MM-dd" e valida se é uma data REAL ──
  // O ngx-mask aceita qualquer dígito; sem validação aqui, "99/99/9999" passaria
  // para o backend como "9999-99-99" e o Jackson dispararia exceção feia.
  // Agora levantamos um erro amigável que o caller transforma em mensagem.
  private converterData(raw: string): string | undefined {
    if (!raw) return undefined;
    const n = raw.replace(/\D/g, '');
    if (n.length !== 8) {
      throw new Error('Data incompleta. Use o formato dd/mm/aaaa.');
    }
    const dia = parseInt(n.substring(0, 2), 10);
    const mes = parseInt(n.substring(2, 4), 10);
    const ano = parseInt(n.substring(4), 10);

    // Validação básica de faixas
    if (mes < 1 || mes > 12 || dia < 1 || dia > 31 || ano < 1900 || ano > 2100) {
      throw new Error('Data inválida. Verifique dia, mês e ano.');
    }
    // Validação de "data realmente existe" (ex: 31/02 não existe)
    const d = new Date(ano, mes - 1, dia);
    if (d.getFullYear() !== ano || d.getMonth() !== mes - 1 || d.getDate() !== dia) {
      throw new Error('Data inválida. Esse dia não existe nesse mês.');
    }

    const dd = String(dia).padStart(2, '0');
    const mm = String(mes).padStart(2, '0');
    return `${ano}-${mm}-${dd}`;
  }

  cancelar(): void {
    this.dialogRef.close(); // fecha sem retornar nada → pai não recarrega
  }
}
