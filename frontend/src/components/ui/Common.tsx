"use client";

import { AlertTriangle, ChevronLeft, ChevronRight, X } from "lucide-react";
import type { ReactNode } from "react";
import {
  STATUS_OBRA_LABELS,
  STATUS_OBRA_STYLES,
  STATUS_PAGAMENTO_LABELS,
  STATUS_PAGAMENTO_STYLES,
} from "@/lib/format";
import type { PageMeta, StatusObra, StatusPagamento } from "@/types/api";

export function StatusObraBadge({ status }: { status: StatusObra }) {
  return (
    <span
      className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium ${STATUS_OBRA_STYLES[status]}`}
    >
      {STATUS_OBRA_LABELS[status]}
    </span>
  );
}

export function StatusPagamentoBadge({ status }: { status: StatusPagamento }) {
  return (
    <span
      className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium ${STATUS_PAGAMENTO_STYLES[status]}`}
    >
      {STATUS_PAGAMENTO_LABELS[status]}
    </span>
  );
}

export function ProgressBar({
  value,
  danger = false,
}: {
  value: number;
  danger?: boolean;
}) {
  const clamped = Math.max(0, Math.min(100, value));
  return (
    <div className="h-2 w-full overflow-hidden rounded-full bg-slate-200">
      <div
        className={`h-full rounded-full transition-all ${
          danger ? "bg-rose-500" : clamped >= 100 ? "bg-emerald-500" : "bg-brand-500"
        }`}
        style={{ width: `${clamped}%` }}
      />
    </div>
  );
}

export function ErrorBanner({
  message,
  onRetry,
}: {
  message: string;
  onRetry?: () => void;
}) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">
      <div className="flex items-center gap-2.5">
        <AlertTriangle className="h-4 w-4 shrink-0 text-rose-600" />
        <span>{message}</span>
      </div>
      {onRetry && (
        <button
          type="button"
          onClick={onRetry}
          className="rounded-lg border border-rose-300 bg-white px-3 py-1 text-xs font-semibold text-rose-700 hover:bg-rose-100"
        >
          Tentar novamente
        </button>
      )}
    </div>
  );
}

export function Modal({
  open,
  title,
  onClose,
  children,
  maxWidth = "max-w-2xl",
}: {
  open: boolean;
  title: string;
  onClose: () => void;
  children: ReactNode;
  maxWidth?: string;
}) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-[1px]">
      <div
        className={`max-h-[90vh] w-full ${maxWidth} overflow-y-auto rounded-2xl bg-white shadow-xl`}
      >
        <div className="sticky top-0 flex items-center justify-between border-b border-slate-200 bg-white px-6 py-4">
          <h2 className="text-lg font-semibold text-slate-900">{title}</h2>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
            aria-label="Fechar modal"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="p-6">{children}</div>
      </div>
    </div>
  );
}

export function Pagination({
  page,
  onPageChange,
}: {
  page: PageMeta;
  onPageChange: (nextPage: number) => void;
}) {
  if (page.totalPages <= 1) return null;

  return (
    <div className="flex items-center justify-between border-t border-slate-200 px-4 py-3 text-sm text-slate-600">
      <span>
        Página <strong>{page.number + 1}</strong> de{" "}
        <strong>{page.totalPages}</strong> ({page.totalElements} registros)
      </span>
      <div className="flex items-center gap-2">
        <button
          type="button"
          disabled={page.number === 0}
          onClick={() => onPageChange(page.number - 1)}
          className="btn-secondary px-2.5 py-1.5 text-xs"
        >
          <ChevronLeft className="h-4 w-4" />
          Anterior
        </button>
        <button
          type="button"
          disabled={page.number + 1 >= page.totalPages}
          onClick={() => onPageChange(page.number + 1)}
          className="btn-secondary px-2.5 py-1.5 text-xs"
        >
          Próxima
          <ChevronRight className="h-4 w-4" />
        </button>
      </div>
    </div>
  );
}
