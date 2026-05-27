// ================================================================
// relatorio.service.ts — Serviço para download de relatórios
//
// Responsabilidade: abrir os endpoints de relatório do backend
// em nova aba do navegador, forçando o download do arquivo.
//
// Não usa Observable porque não há dado a receber — apenas abre a URL.
// ================================================================

import { Injectable } from '@angular/core';

// Filtros opcionais que podem ser repassados ao relatório de lista.
// Quando preenchidos, o backend gera o relatório apenas com os clientes
// que casam com os filtros (mesma lógica do ClienteDAO.buscarComFiltros).
export interface FiltrosRelatorio {
  termo?:      string;
  ativo?:      string; // "ativo" | "inativo"
  tipo?:       string; // "PF" | "PJ"
  dataInicio?: string; // "yyyy-MM-dd"
  dataFim?:    string; // "yyyy-MM-dd"
}

@Injectable({ providedIn: 'root' })
export class RelatorioService {

  // ── Relatório PDF da lista de clientes (com filtros opcionais) ──
  pdfLista(filtros?: FiltrosRelatorio): void {
    const qs = this.montarQueryString(filtros);
    window.open(`/api/relatorios/clientes/pdf${qs}`, '_blank');
  }

  // ── Planilha Excel da lista de clientes (com filtros opcionais) ──
  excelLista(filtros?: FiltrosRelatorio): void {
    const qs = this.montarQueryString(filtros);
    window.open(`/api/relatorios/clientes/excel${qs}`, '_blank');
  }

  // ── Relatório PDF de um cliente específico ──
  pdfCliente(id: number): void {
    window.open(`/api/relatorios/cliente/detalhes/pdf?id=${id}`, '_blank');
  }

  // ── Planilha Excel de um cliente específico ──
  excelCliente(id: number): void {
    window.open(`/api/relatorios/cliente/detalhes/excel?id=${id}`, '_blank');
  }

  // ── Download da planilha modelo para importação em lote ──
  modeloPlanilha(): void {
    window.open('/api/clientes/modelo-planilha', '_blank');
  }

  // ── Helper: monta "?termo=abc&ativo=ativo" a partir dos filtros ──
  // Ignora campos vazios/undefined para não poluir a URL.
  private montarQueryString(filtros?: FiltrosRelatorio): string {
    if (!filtros) return '';
    const partes: string[] = [];
    if (filtros.termo)      partes.push(`termo=${encodeURIComponent(filtros.termo)}`);
    if (filtros.ativo)      partes.push(`ativo=${filtros.ativo}`);
    if (filtros.tipo)       partes.push(`tipo=${filtros.tipo}`);
    if (filtros.dataInicio) partes.push(`dataInicio=${filtros.dataInicio}`);
    if (filtros.dataFim)    partes.push(`dataFim=${filtros.dataFim}`);
    return partes.length === 0 ? '' : `?${partes.join('&')}`;
  }
}
