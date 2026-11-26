import type {
  ApiErrorResponse,
  CentroCustoRequest,
  CentroCustoResponse,
  DashboardResumoResponse,
  LancamentoPagamentoUpdateRequest,
  LancamentoRequest,
  LancamentoResponse,
  ObraDetalheResponse,
  ObraRequest,
  ObraResponse,
  ObraStatusUpdateRequest,
  PageResponse,
  StatusObra,
  StatusPagamento,
  TipoLancamento,
} from "@/types/api";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") ||
  "http://localhost:8080/api";

export class ApiClientError extends Error {
  status: number;
  fieldErrors: Record<string, string>;

  constructor(message: string, status: number, fieldErrors: Record<string, string> = {}) {
    super(message);
    this.name = "ApiClientError";
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

function buildQuery(params: Record<string, string | number | boolean | undefined | null>): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      search.set(key, String(value));
    }
  });
  const qs = search.toString();
  return qs ? `?${qs}` : "";
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {}),
    },
    cache: "no-store",
  });

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const err = data as ApiErrorResponse | null;
    const fieldMap: Record<string, string> = {};
    err?.fieldErrors?.forEach((item) => {
      fieldMap[item.field] = item.message;
    });
    throw new ApiClientError(
      err?.message || `Erro HTTP ${response.status} ao comunicar com a API.`,
      response.status,
      fieldMap
    );
  }

  return data as T;
}

export const api = {
  dashboard: {
    resumo: () => request<DashboardResumoResponse>("/dashboard/resumo"),
  },

  obras: {
    listar: (params?: {
      status?: StatusObra | "";
      busca?: string;
      page?: number;
      size?: number;
    }) =>
      request<PageResponse<ObraResponse>>(
        `/obras${buildQuery({
          status: params?.status || undefined,
          busca: params?.busca,
          page: params?.page ?? 0,
          size: params?.size ?? 10,
        })}`
      ),

    buscarPorId: (id: number) => request<ObraDetalheResponse>(`/obras/${id}`),

    criar: (payload: ObraRequest) =>
      request<ObraResponse>("/obras", {
        method: "POST",
        body: JSON.stringify(payload),
      }),

    atualizar: (id: number, payload: ObraRequest) =>
      request<ObraResponse>(`/obras/${id}`, {
        method: "PUT",
        body: JSON.stringify(payload),
      }),

    atualizarStatus: (id: number, payload: ObraStatusUpdateRequest) =>
      request<ObraResponse>(`/obras/${id}/status`, {
        method: "PATCH",
        body: JSON.stringify(payload),
      }),

    excluir: (id: number) =>
      request<void>(`/obras/${id}`, {
        method: "DELETE",
      }),

    listarLancamentos: (
      id: number,
      params?: {
        tipo?: TipoLancamento | "";
        statusPagamento?: StatusPagamento | "";
        page?: number;
        size?: number;
      }
    ) =>
      request<PageResponse<LancamentoResponse>>(
        `/obras/${id}/lancamentos${buildQuery({
          tipo: params?.tipo || undefined,
          statusPagamento: params?.statusPagamento || undefined,
          page: params?.page ?? 0,
          size: params?.size ?? 20,
        })}`
      ),
  },

  centrosCusto: {
    listar: (apenasAtivos = false) =>
      request<CentroCustoResponse[]>(
        `/centros-custo${buildQuery({ apenasAtivos })}`
      ),

    criar: (payload: CentroCustoRequest) =>
      request<CentroCustoResponse>("/centros-custo", {
        method: "POST",
        body: JSON.stringify(payload),
      }),

    atualizar: (id: number, payload: CentroCustoRequest) =>
      request<CentroCustoResponse>(`/centros-custo/${id}`, {
        method: "PUT",
        body: JSON.stringify(payload),
      }),

    excluir: (id: number) =>
      request<void>(`/centros-custo/${id}`, {
        method: "DELETE",
      }),
  },

  lancamentos: {
    listar: (params?: {
      obraId?: number | "";
      centroCustoId?: number | "";
      tipo?: TipoLancamento | "";
      statusPagamento?: StatusPagamento | "";
      dataInicio?: string;
      dataFim?: string;
      page?: number;
      size?: number;
    }) =>
      request<PageResponse<LancamentoResponse>>(
        `/lancamentos${buildQuery({
          obraId: params?.obraId || undefined,
          centroCustoId: params?.centroCustoId || undefined,
          tipo: params?.tipo || undefined,
          statusPagamento: params?.statusPagamento || undefined,
          dataInicio: params?.dataInicio || undefined,
          dataFim: params?.dataFim || undefined,
          page: params?.page ?? 0,
          size: params?.size ?? 15,
        })}`
      ),

    criar: (payload: LancamentoRequest) =>
      request<LancamentoResponse>("/lancamentos", {
        method: "POST",
        body: JSON.stringify(payload),
      }),

    atualizar: (id: number, payload: LancamentoRequest) =>
      request<LancamentoResponse>(`/lancamentos/${id}`, {
        method: "PUT",
        body: JSON.stringify(payload),
      }),

    atualizarPagamento: (id: number, payload: LancamentoPagamentoUpdateRequest) =>
      request<LancamentoResponse>(`/lancamentos/${id}/pagamento`, {
        method: "PATCH",
        body: JSON.stringify(payload),
      }),

    excluir: (id: number) =>
      request<void>(`/lancamentos/${id}`, {
        method: "DELETE",
      }),
  },
};
