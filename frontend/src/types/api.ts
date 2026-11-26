export type StatusObra =
  | "PLANEJAMENTO"
  | "EM_ANDAMENTO"
  | "PARALISADA"
  | "CONCLUIDA"
  | "CANCELADA";

export type TipoLancamento = "RECEITA" | "DESPESA";

export type StatusPagamento = "PENDENTE" | "PAGO" | "CANCELADO";

export interface PageMeta {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
}

export interface PageResponse<T> {
  content: T[];
  page: PageMeta;
}

export interface FieldErrorItem {
  field: string;
  message: string;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: FieldErrorItem[];
}

export interface ObraResponse {
  id: number;
  nome: string;
  descricao?: string;
  cliente?: string;
  endereco: string;
  responsavel: string;
  orcamentoPrevisto: number;
  dataInicio: string;
  previsaoFim: string;
  dataConclusao?: string;
  status: StatusObra;
  progressoPercentual: number;
  atrasada: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ObraRequest {
  nome: string;
  descricao?: string;
  cliente?: string;
  endereco: string;
  responsavel: string;
  orcamentoPrevisto: number;
  dataInicio: string;
  previsaoFim: string;
  dataConclusao?: string | null;
  status?: StatusObra;
  progressoPercentual?: number;
}

export interface ObraStatusUpdateRequest {
  status: StatusObra;
  progressoPercentual?: number;
  dataConclusao?: string | null;
}

export interface GastoPorCentroCusto {
  centroCustoId: number | null;
  centroCustoNome: string;
  totalValor: number;
  quantidadeLancamentos: number;
}

export interface ResumoFinanceiroObra {
  obraId: number;
  nomeObra: string;
  orcamentoPrevisto: number;
  totalReceitasPagas: number;
  totalReceitasPendentes: number;
  totalDespesasPagas: number;
  totalDespesasPendentes: number;
  saldoRealizado: number;
  saldoProjetado: number;
  orcamentoDisponivel: number;
  percentualOrcamentoUtilizado: number;
  orcamentoExcedido: boolean;
  quantidadeLancamentos: number;
  despesasPorCentroCusto: GastoPorCentroCusto[];
}

export interface ObraDetalheResponse {
  obra: ObraResponse;
  diasRestantes: number;
  resumoFinanceiro: ResumoFinanceiroObra;
}

export interface CentroCustoResponse {
  id: number;
  nome: string;
  descricao?: string;
  ativo: boolean;
  createdAt: string;
}

export interface CentroCustoRequest {
  nome: string;
  descricao?: string;
  ativo?: boolean;
}

export interface LancamentoResponse {
  id: number;
  obraId: number;
  obraNome: string;
  centroCustoId?: number;
  centroCustoNome?: string;
  descricao: string;
  tipo: TipoLancamento;
  valor: number;
  dataCompetencia: string;
  dataPagamento?: string;
  statusPagamento: StatusPagamento;
  comprovanteUrl?: string;
  observacoes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LancamentoRequest {
  obraId: number;
  centroCustoId?: number | null;
  descricao: string;
  tipo: TipoLancamento;
  valor: number;
  dataCompetencia: string;
  dataPagamento?: string | null;
  statusPagamento?: StatusPagamento;
  comprovanteUrl?: string;
  observacoes?: string;
}

export interface LancamentoPagamentoUpdateRequest {
  statusPagamento: StatusPagamento;
  dataPagamento?: string | null;
}

export interface AlertaObra {
  obraId: number;
  obraNome: string;
  status: StatusObra;
  atrasada: boolean;
  orcamentoExcedido: boolean;
  percentualOrcamentoUtilizado: number;
  previsaoFim: string;
}

export interface DashboardResumoResponse {
  totalObras: number;
  obrasPorStatus: Record<StatusObra, number>;
  progressoMedioObras: number;
  orcamentoTotalPrevisto: number;
  totalReceitasPagas: number;
  totalReceitasPendentes: number;
  totalDespesasPagas: number;
  totalDespesasPendentes: number;
  saldoRealizadoGeral: number;
  saldoProjetadoGeral: number;
  despesasPorCentroCusto: GastoPorCentroCusto[];
  alertasObras: AlertaObra[];
}
