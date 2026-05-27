// ================================================================
// viacep.service.ts — Integração com a API pública ViaCEP
//
// Responsabilidade: dado um CEP de 8 dígitos, consultar o ViaCEP
// e retornar os dados do endereço (logradouro, bairro, cidade, UF).
//
// Usado pelos modais de endereço: ao sair do campo CEP, o formulário
// é preenchido automaticamente com os dados retornados.
// ================================================================

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ViaCepResponse } from '../models/cliente.model';

@Injectable({ providedIn: 'root' })
export class ViaCepService {

  constructor(private http: HttpClient) {}

  // ── Consulta o CEP informado e retorna os dados do endereço ──
  // Retorna null se o CEP não tiver 8 dígitos ou se a API retornar erro
  buscar(cep: string): Observable<ViaCepResponse | null> {
    const cepLimpo = cep.replace(/\D/g, ''); // remove traço: "01310-100" → "01310100"

    if (cepLimpo.length !== 8) {
      return of(null); // CEP incompleto: não faz a requisição
    }

    return this.http
      .get<ViaCepResponse>(`https://viacep.com.br/ws/${cepLimpo}/json/`)
      .pipe(
        // Se a requisição falhar (rede, timeout), retorna null sem quebrar a tela
        catchError(() => of(null))
      );
  }
}
