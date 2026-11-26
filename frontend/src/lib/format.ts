import type { StatusObra, StatusPagamento, TipoLancamento } from "@/types/api";

const brlFormatter = new Intl.NumberFormat("pt-BR", {
  style: "currency",
  currency: "BRL",
});

export function formatCurrency(value: number | undefined | null): string {
  return brlFormatter.format(Number(value ?? 0));
}

export function formatDate(isoDate?: string | null): string {
  if (!isoDate) return "—";
  const parts = isoDate.split("-");
  if (parts.length !== 3) return isoDate;
  return `${parts[2]}/${parts[1]}/${parts[0]}`;
}

export function formatPercent(value: number | undefined | null): string {
  return `${Number(value ?? 0).toFixed(1)}%`;
}

export const STATUS_OBRA_LABELS: Record<StatusObra, string> = {
  PLANEJAMENTO: "Planejamento",
  EM_ANDAMENTO: "Em andamento",
  PARALISADA: "Paralisada",
  CONCLUIDA: "Concluída",
  CANCELADA: "Cancelada",
};

export const STATUS_OBRA_STYLES: Record<StatusObra, string> = {
  PLANEJAMENTO: "bg-sky-100 text-sky-800 border-sky-200",
  EM_ANDAMENTO: "bg-amber-100 text-amber-800 border-amber-200",
  PARALISADA: "bg-rose-100 text-rose-800 border-rose-200",
  CONCLUIDA: "bg-emerald-100 text-emerald-800 border-emerald-200",
  CANCELADA: "bg-slate-200 text-slate-700 border-slate-300",
};

export const STATUS_PAGAMENTO_LABELS: Record<StatusPagamento, string> = {
  PENDENTE: "Pendente",
  PAGO: "Pago",
  CANCELADO: "Cancelado",
};

export const STATUS_PAGAMENTO_STYLES: Record<StatusPagamento, string> = {
  PENDENTE: "bg-amber-100 text-amber-800 border-amber-200",
  PAGO: "bg-emerald-100 text-emerald-800 border-emerald-200",
  CANCELADO: "bg-slate-200 text-slate-600 border-slate-300",
};

export const TIPO_LANCAMENTO_LABELS: Record<TipoLancamento, string> = {
  RECEITA: "Receita",
  DESPESA: "Despesa",
};

export const ALL_STATUS_OBRA: StatusObra[] = [
  "PLANEJAMENTO",
  "EM_ANDAMENTO",
  "PARALISADA",
  "CONCLUIDA",
  "CANCELADA",
];
