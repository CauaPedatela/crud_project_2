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

// ── Resposta paginada genérica vinda do backend ──
// Espelha o PageResponseDTO<T> do Java. Usada pelo endpoint /api/clientes/buscar.
// O componente lê `content` para a tabela e `totalElements` para o MatPaginator.
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

// ── Contadores agregados do header (espelha ContadoresDTO.java) ──
// Vem do endpoint /api/clientes/contadores — não traz nenhum cliente, só os números.
export interface Contadores {
  total: number;
  ativos: number;
}

// ── Estado brasileiro retornado pelo IBGE ──
// API: GET https://servicodados.ibge.gov.br/api/v1/localidades/estados
// Usamos só os 3 campos abaixo; o IBGE retorna outros (id, região, etc.)
export interface Estado {
  id: number;
  sigla: string; // UF de 2 letras, ex: "SP"
  nome: string;  // "São Paulo"
}

// ── Município brasileiro retornado pelo IBGE ──
// API: GET https://servicodados.ibge.gov.br/api/v1/localidades/estados/{UF}/municipios
export interface Cidade {
  id: number;
  nome: string;
}
