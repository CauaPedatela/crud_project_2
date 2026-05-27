// ================================================================
// cliente.service.ts — Serviço de comunicação com a API de Clientes
//
// Responsabilidade: fazer as chamadas HTTP ao backend Spring Boot.
// Todos os métodos retornam Observable — o componente que chamar
// precisa fazer `.subscribe()` para receber os dados.
//
// Conceito Angular: @Injectable({ providedIn: 'root' }) cria um
// serviço singleton — uma única instância compartilhada em todo app.
// ================================================================

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cliente, ClienteDTO, ImportacaoResultado } from '../models/cliente.model';

// URL base da API — o proxy.conf.json redireciona /api → localhost:8080
const BASE = '/api/clientes';

@Injectable({ providedIn: 'root' })
export class ClienteService {

  // O HttpClient é injetado pelo Angular automaticamente
  constructor(private http: HttpClient) {}

  // ── Lista todos os clientes cadastrados ──
  listar(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(BASE);
  }

  // ── Busca clientes com filtros opcionais ──
  // Parâmetros vazios ou nulos são ignorados pelo backend
  buscarComFiltros(params: {
    termo?: string;
    ativo?: string;      // "true", "false" ou ""
    tipo?: string;       // "FISICA", "JURIDICA" ou ""
    dataInicio?: string; // "yyyy-MM-dd"
    dataFim?: string;    // "yyyy-MM-dd"
  }): Observable<Cliente[]> {
    // HttpParams monta a query string automaticamente: ?termo=abc&ativo=true
    let httpParams = new HttpParams();
    if (params.termo)      httpParams = httpParams.set('termo',      params.termo);
    if (params.ativo)      httpParams = httpParams.set('ativo',      params.ativo);
    if (params.tipo)       httpParams = httpParams.set('tipo',       params.tipo);
    if (params.dataInicio) httpParams = httpParams.set('dataInicio', params.dataInicio);
    if (params.dataFim)    httpParams = httpParams.set('dataFim',    params.dataFim);
    return this.http.get<Cliente[]>(`${BASE}/buscar`, { params: httpParams });
  }

  // ── Busca um cliente pelo ID ──
  buscarPorId(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${BASE}/${id}`);
  }

  // ── Cadastra um novo cliente com seus endereços ──
  criar(dto: ClienteDTO): Observable<Cliente> {
    return this.http.post<Cliente>(BASE, dto);
  }

  // ── Atualiza os dados de um cliente existente ──
  // O backend ignora cpfCnpj e tipoPessoa no PUT (imutáveis)
  atualizar(id: number, dto: ClienteDTO): Observable<Cliente> {
    return this.http.put<Cliente>(`${BASE}/${id}`, dto);
  }

  // ── Exclui um cliente e todos os seus endereços ──
  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE}/${id}`);
  }

  // ── Importa clientes em lote a partir de um arquivo Excel ──
  // Usa FormData pois é um upload de arquivo (multipart/form-data)
  importar(arquivo: File): Observable<ImportacaoResultado> {
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    return this.http.post<ImportacaoResultado>(`${BASE}/importar`, formData);
  }
}
