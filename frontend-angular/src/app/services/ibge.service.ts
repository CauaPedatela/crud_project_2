// ================================================================
// ibge.service.ts — Integração com a API pública do IBGE
//
// Fornece duas listas:
//   - Estados (27 UFs do Brasil) — busca uma vez por sessão
//   - Cidades por UF (varia por estado) — busca sob demanda
//
// A API do IBGE não exige autenticação e tem CORS aberto, pode ser
// chamada direto do browser. Documentação:
//   https://servicodados.ibge.gov.br/api/docs/localidades
//
// Como os estados nunca mudam dentro da mesma sessão, cacheamos
// a lista no `estadosCache` para evitar requisições repetidas.
// ================================================================

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, shareReplay, tap } from 'rxjs/operators';
import { Estado, Cidade } from '../models/cliente.model';

@Injectable({ providedIn: 'root' })
export class IbgeService {

  private readonly BASE = 'https://servicodados.ibge.gov.br/api/v1/localidades';

  // Cache local: a lista de estados é fixa, evita repedir
  private estadosCache$?: Observable<Estado[]>;

  // Cache por UF: chave = sigla, valor = lista de cidades
  // Como a maioria dos usuários só edita 1 ou 2 endereços por sessão,
  // não vale a pena cache mais sofisticado.
  private cidadesCache = new Map<string, Observable<Cidade[]>>();

  constructor(private http: HttpClient) {}

  /**
   * Lista os 27 estados do Brasil ordenados por nome.
   * Usa shareReplay(1) para que múltiplos componentes que se inscrevam
   * compartilhem a mesma requisição (não faz 5 fetches se 5 modais abrirem).
   */
  listarEstados(): Observable<Estado[]> {
    if (!this.estadosCache$) {
      this.estadosCache$ = this.http
        .get<Estado[]>(`${this.BASE}/estados?orderBy=nome`)
        .pipe(
          // Se o IBGE estiver fora do ar, devolve lista vazia em vez de quebrar
          catchError(() => of([] as Estado[])),
          shareReplay(1)
        );
    }
    return this.estadosCache$;
  }

  /**
   * Lista as cidades de um estado (UF), ordenadas por nome.
   * Recebe a sigla (ex: "SP") — ou o ID do estado, ambos funcionam na API.
   */
  listarCidades(uf: string): Observable<Cidade[]> {
    if (!uf) return of([]);

    const chave = uf.toUpperCase();
    if (!this.cidadesCache.has(chave)) {
      const obs = this.http
        .get<Cidade[]>(`${this.BASE}/estados/${chave}/municipios?orderBy=nome`)
        .pipe(
          map(list => list.map(c => ({ id: c.id, nome: c.nome }))),
          catchError(() => of([] as Cidade[])),
          shareReplay(1)
        );
      this.cidadesCache.set(chave, obs);
    }
    return this.cidadesCache.get(chave)!;
  }
}
