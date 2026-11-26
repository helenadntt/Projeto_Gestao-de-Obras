"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import {
  AlertTriangle,
  ArrowDownCircle,
  ArrowRight,
  ArrowUpCircle,
  Building2,
  RefreshCw,
  TrendingUp,
  Wallet,
} from "lucide-react";
import {
  ErrorBanner,
  ProgressBar,
  StatusObraBadge,
} from "@/components/ui/Common";
import {
  ALL_STATUS_OBRA,
  formatCurrency,
  formatDate,
  formatPercent,
  STATUS_OBRA_LABELS,
} from "@/lib/format";
import { api } from "@/services/api";
import type { DashboardResumoResponse } from "@/types/api";

export default function DashboardPage() {
  const [resumo, setResumo] = useState<DashboardResumoResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const carregar = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.dashboard.resumo();
      setResumo(data);
    } catch {
      setError(
        "Não foi possível carregar os indicadores do dashboard. Verifique se o backend está rodando em http://localhost:8080/api."
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const maxGastoCentro =
    resumo?.despesasPorCentroCusto.reduce(
      (max, item) => Math.max(max, Number(item.totalValor)),
      0
    ) || 1;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Dashboard Gerencial
          </h1>
          <p className="mt-1 text-sm text-slate-500">
            Visão consolidada do portfólio de obras, fluxo financeiro e alertas.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={carregar}
            disabled={loading}
            className="btn-secondary"
          >
            <RefreshCw className={`h-4 w-4 ${loading ? "animate-spin" : ""}`} />
            Atualizar
          </button>
          <Link href="/obras" className="btn-primary">
            Gerenciar Obras
            <ArrowRight className="h-4 w-4" />
          </Link>
        </div>
      </div>

      {error && <ErrorBanner message={error} onRetry={carregar} />}

      {loading && !resumo ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="card h-28 animate-pulse bg-slate-100" />
          ))}
        </div>
      ) : resumo ? (
        <>
          {/* KPIs principais */}
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <div className="card p-5">
              <div className="flex items-center justify-between">
                <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                  Total de Obras
                </p>
                <div className="rounded-lg bg-brand-50 p-2 text-brand-600">
                  <Building2 className="h-4 w-4" />
                </div>
              </div>
              <p className="mt-3 text-3xl font-bold text-slate-900">
                {resumo.totalObras}
              </p>
              <p className="mt-1 text-xs text-slate-500">
                Progresso médio:{" "}
                <strong className="text-slate-700">
                  {formatPercent(resumo.progressoMedioObras)}
                </strong>
              </p>
            </div>

            <div className="card p-5">
              <div className="flex items-center justify-between">
                <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                  Saldo Realizado
                </p>
                <div className="rounded-lg bg-indigo-50 p-2 text-indigo-600">
                  <Wallet className="h-4 w-4" />
                </div>
              </div>
              <p
                className={`mt-3 text-2xl font-bold ${
                  resumo.saldoRealizadoGeral >= 0
                    ? "text-emerald-600"
                    : "text-rose-600"
                }`}
              >
                {formatCurrency(resumo.saldoRealizadoGeral)}
              </p>
              <p className="mt-1 text-xs text-slate-500">
                Projetado:{" "}
                <strong className="text-slate-700">
                  {formatCurrency(resumo.saldoProjetadoGeral)}
                </strong>
              </p>
            </div>

            <div className="card p-5">
              <div className="flex items-center justify-between">
                <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                  Receitas Pagas
                </p>
                <div className="rounded-lg bg-emerald-50 p-2 text-emerald-600">
                  <ArrowUpCircle className="h-4 w-4" />
                </div>
              </div>
              <p className="mt-3 text-2xl font-bold text-slate-900">
                {formatCurrency(resumo.totalReceitasPagas)}
              </p>
              <p className="mt-1 text-xs text-slate-500">
                A receber:{" "}
                <strong className="text-emerald-700">
                  {formatCurrency(resumo.totalReceitasPendentes)}
                </strong>
              </p>
            </div>

            <div className="card p-5">
              <div className="flex items-center justify-between">
                <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                  Despesas Pagas
                </p>
                <div className="rounded-lg bg-rose-50 p-2 text-rose-600">
                  <ArrowDownCircle className="h-4 w-4" />
                </div>
              </div>
              <p className="mt-3 text-2xl font-bold text-slate-900">
                {formatCurrency(resumo.totalDespesasPagas)}
              </p>
              <p className="mt-1 text-xs text-slate-500">
                Pendentes:{" "}
                <strong className="text-amber-700">
                  {formatCurrency(resumo.totalDespesasPendentes)}
                </strong>
              </p>
            </div>
          </div>

          {/* Orçamento & Distribuição */}
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
            {/* Obras por status */}
            <div className="card p-6">
              <div className="flex items-center justify-between">
                <h2 className="text-base font-semibold text-slate-900">
                  Obras por Status
                </h2>
                <TrendingUp className="h-4 w-4 text-slate-400" />
              </div>
              <p className="mt-1 text-xs text-slate-500">
                Orçamento previsto somado:{" "}
                <strong className="text-slate-800">
                  {formatCurrency(resumo.orcamentoTotalPrevisto)}
                </strong>
              </p>

              <div className="mt-5 space-y-3">
                {ALL_STATUS_OBRA.map((st) => {
                  const count = resumo.obrasPorStatus[st] ?? 0;
                  const pct =
                    resumo.totalObras > 0
                      ? (count / resumo.totalObras) * 100
                      : 0;
                  return (
                    <div key={st}>
                      <div className="mb-1 flex items-center justify-between text-xs">
                        <span className="font-medium text-slate-700">
                          {STATUS_OBRA_LABELS[st]}
                        </span>
                        <span className="font-semibold text-slate-900">
                          {count} ({pct.toFixed(0)}%)
                        </span>
                      </div>
                      <ProgressBar value={pct} />
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Despesas por centro de custo */}
            <div className="card p-6 lg:col-span-2">
              <div className="flex items-center justify-between">
                <div>
                  <h2 className="text-base font-semibold text-slate-900">
                    Despesas por Centro de Custo
                  </h2>
                  <p className="mt-0.5 text-xs text-slate-500">
                    Total comprometido (pagas + pendentes, excluindo canceladas)
                  </p>
                </div>
                <Link
                  href="/financeiro"
                  className="text-xs font-semibold text-brand-600 hover:text-brand-700"
                >
                  Ver extrato →
                </Link>
              </div>

              {resumo.despesasPorCentroCusto.length === 0 ? (
                <div className="mt-8 rounded-lg border border-dashed border-slate-200 p-8 text-center text-sm text-slate-500">
                  Nenhuma despesa registrada até o momento.
                </div>
              ) : (
                <div className="mt-5 space-y-4">
                  {resumo.despesasPorCentroCusto.map((item, index) => {
                    const pct = (Number(item.totalValor) / maxGastoCentro) * 100;
                    return (
                      <div key={item.centroCustoId ?? `sem-${index}`}>
                        <div className="mb-1 flex flex-wrap items-center justify-between gap-2 text-sm">
                          <span className="font-medium text-slate-800">
                            {item.centroCustoNome}
                          </span>
                          <span className="text-xs text-slate-500">
                            {item.quantidadeLancamentos} lanç. •{" "}
                            <strong className="text-sm text-slate-900">
                              {formatCurrency(item.totalValor)}
                            </strong>
                          </span>
                        </div>
                        <ProgressBar value={pct} />
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>

          {/* Alertas Operacionais e Financeiros */}
          <div className="card overflow-hidden">
            <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
              <div className="flex items-center gap-2">
                <AlertTriangle className="h-5 w-5 text-amber-500" />
                <h2 className="text-base font-semibold text-slate-900">
                  Alertas de Prazo e Orçamento ({resumo.alertasObras.length})
                </h2>
              </div>
            </div>

            {resumo.alertasObras.length === 0 ? (
              <div className="px-6 py-8 text-center text-sm text-slate-500">
                Nenhuma obra com atraso ou orçamento excedido no momento.
              </div>
            ) : (
              <div className="divide-y divide-slate-200">
                {resumo.alertasObras.map((alerta) => (
                  <div
                    key={alerta.obraId}
                    className="flex flex-wrap items-center justify-between gap-4 px-6 py-4 hover:bg-slate-50"
                  >
                    <div className="space-y-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <Link
                          href={`/obras/${alerta.obraId}`}
                          className="font-semibold text-slate-900 hover:text-brand-600"
                        >
                          {alerta.obraNome}
                        </Link>
                        <StatusObraBadge status={alerta.status} />
                      </div>
                      <div className="flex flex-wrap gap-2 text-xs">
                        {alerta.atrasada && (
                          <span className="rounded-md bg-rose-100 px-2 py-0.5 font-medium text-rose-800">
                            Prazo vencido ({formatDate(alerta.previsaoFim)})
                          </span>
                        )}
                        {alerta.orcamentoExcedido && (
                          <span className="rounded-md bg-amber-100 px-2 py-0.5 font-medium text-amber-800">
                            Orçamento excedido (
                            {formatPercent(alerta.percentualOrcamentoUtilizado)})
                          </span>
                        )}
                      </div>
                    </div>

                    <Link
                      href={`/obras/${alerta.obraId}`}
                      className="btn-secondary text-xs"
                    >
                      Abrir detalhes
                      <ArrowRight className="h-3.5 w-3.5" />
                    </Link>
                  </div>
                ))}
              </div>
            )}
          </div>
        </>
      ) : null}
    </div>
  );
}
