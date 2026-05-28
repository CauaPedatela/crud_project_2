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
import { Cliente, ClienteDTO, ImportacaoResultado, PageResponse, Contadores } from '../models/cliente.model';

// URL base da API — o proxy.conf.json redireciona /api → localhost:8080
const BASE = '/api/clientes';

@Injectable({ providedIn: 'root' })
export class ClienteService {

  // O HttpClient é injetado pelo Angular automaticamente
  constructor(private http: HttpClient) {}

  // ── Lista todos os clientes cadastrados (sem paginação) ──
  // Mantido para compatibilidade — não é usado pela listagem (que agora pagina).
  listar(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(BASE);
  }

  // ── Apenas os totais para o header (sem trazer nenhum cliente) ──
  // Evita carregar a lista inteira só para mostrar "X clientes / Y ativos".
  // Backend faz dois COUNT(*) no banco — sem instanciar entidades.
  contadores(): Observable<Contadores> {
    return this.http.get<Contadores>(`${BASE}/contadores`);
  }

  // ── Busca clientes com filtros opcionais + paginação no backend ──
  // Parâmetros vazios são ignorados; page e size vão sempre (defaults 0 e 10).
  // Retorna uma página com { content, totalElements, totalPages, page, size }.
  buscarComFiltros(params: {
    termo?: string;
    ativo?: string;      // "true", "false" ou ""
    tipo?: string;       // "FISICA", "JURIDICA" ou ""
    dataInicio?: string; // "yyyy-MM-dd"
    dataFim?: string;    // "yyyy-MM-dd"
    page?: number;       // 0-based
    size?: number;       // itens por página
  }): Observable<PageResponse<Cliente>> {
    // HttpParams monta a query string automaticamente: ?termo=abc&page=0&size=10
    let httpParams = new HttpParams();
    if (params.termo)      httpParams = httpParams.set('termo',      params.termo);
    if (params.ativo)      httpParams = httpParams.set('ativo',      params.ativo);
    if (params.tipo)       httpParams = httpParams.set('tipo',       params.tipo);
    if (params.dataInicio) httpParams = httpParams.set('dataInicio', params.dataInicio);
    if (params.dataFim)    httpParams = httpParams.set('dataFim',    params.dataFim);
    httpParams = httpParams.set('page', String(params.page ?? 0));
    httpParams = httpParams.set('size', String(params.size ?? 10));
    // Sort fixo: mais recentes primeiro (espelha o comportamento antigo do DAO).
    httpParams = httpParams.set('sort', 'dataCadastro,desc');

    return this.http.get<PageResponse<Cliente>>(`${BASE}/buscar`, { params: httpParams });
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
