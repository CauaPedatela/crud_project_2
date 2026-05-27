// ================================================================
// cliente.model.ts — Interfaces TypeScript que espelham os DTOs Java
//
// Estas interfaces descrevem o formato dos dados que trafegam entre
// o frontend Angular e o backend Spring Boot via API REST.
//
// Regras de negócio importantes (refletidas aqui):
//   - cpfCnpj e tipoPessoa são IMUTÁVEIS após o cadastro
//   - Cada cliente tem exatamente 1 endereço com principal=true
//   - dataCadastro é gerada no servidor, nunca pelo frontend
// ================================================================

// Tipo de pessoa: Física (CPF) ou Jurídica (CNPJ)
export type TipoPessoa = 'FISICA' | 'JURIDICA';

// Tipo de endereço: Residencial ou Comercial
export type TipoEndereco = 'RESIDENCIAL' | 'COMERCIAL';

// ── Endereço completo (resposta do servidor, sempre tem `id`) ──
export interface Endereco {
  id?: number;
  tipo: TipoEndereco;
  logradouro: string;
  numero?: string;
  complemento?: string;
  bairro: string;
  cidade: string;
  estado: string;       // UF com 2 caracteres, ex: "SP"
  cep: string;          // 8 dígitos sem formatação, ex: "01310100"
  pais: string;
  telefone?: string;    // opcional no endereço
  principal: boolean;   // exatamente um endereço deve ser true por cliente
}

// ── Cliente completo (resposta do servidor, usado nas telas de listagem e detalhes) ──
export interface Cliente {
  id: number;
  tipoPessoa: TipoPessoa;
  nome: string;
  cpfCnpj: string;              // 11 dígitos (CPF) ou 14 dígitos (CNPJ), sem formatação
  rgInscricaoEstadual?: string; // RG (PF) ou Inscrição Estadual (PJ)
  dataNascimento?: string;      // formato "yyyy-MM-dd" (ISO 8601)
  email: string;
  ativo: boolean;
  dataCadastro: string;         // formato "yyyy-MM-ddTHH:mm:ss" (gerado no servidor)
  enderecos: Endereco[];
}

// ── DTO de endereço: usado ao criar ou atualizar (enviado ao servidor) ──
// Diferença em relação a Endereco: id é opcional (novo endereço não tem id ainda)
export interface EnderecoDTO {
  id?: number;
  tipo: TipoEndereco;
  logradouro: string;
  numero?: string;
  complemento?: string;
  bairro: string;
  cidade: string;
  estado: string;
  cep: string;
  pais: string;
  telefone?: string;
  principal: boolean;
}

// ── DTO de cliente: usado ao criar ou atualizar (enviado ao servidor) ──
// Não contém `id`, `dataCadastro` (gerados no servidor).
// Ao atualizar, cpfCnpj e tipoPessoa são ignorados pelo backend (imutáveis).
export interface ClienteDTO {
  tipoPessoa: TipoPessoa;
  nome: string;
  cpfCnpj: string;
  rgInscricaoEstadual?: string;
  dataNascimento?: string;
  email: string;
  ativo: boolean;
  enderecos: EnderecoDTO[];
}

// ── Resultado da importação em lote via Excel ──
// Espelha o POJO ImportacaoResultado.java do backend
export interface ImportacaoResultado {
  sucessos: number;
  erros: number;
  mensagensErro: string[];
  temErros: boolean;
}

// ── Resposta da API ViaCEP ──
// Usada pelo ViaCepService para auto-preencher campos de endereço
export interface ViaCepResponse {
  logradouro: string;
  bairro: string;
  localidade: string; // cidade
  uf: string;         // estado (UF)
  erro?: boolean;     // true quando o CEP não existe na base dos Correios
}
