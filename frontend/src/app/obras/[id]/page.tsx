"use client";

import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import {
  AlertTriangle,
  ArrowLeft,
  Calendar,
  CheckCircle2,
  MapPin,
  Pencil,
  Plus,
  Trash2,
  User,
} from "lucide-react";
import { LancamentoFormModal } from "@/components/financeiro/LancamentoFormModal";
import { ObraFormModal } from "@/components/obras/ObraFormModal";
import {
  ErrorBanner,
  ProgressBar,
  StatusObraBadge,
  StatusPagamentoBadge,
} from "@/components/ui/Common";
import {
  ALL_STATUS_OBRA,
  formatCurrency,
  formatDate,
  formatPercent,
  STATUS_OBRA_LABELS,
} from "@/lib/format";
import { api } from "@/services/api";
import type {
  CentroCustoResponse,
  LancamentoResponse,
  ObraDetalheResponse,
  StatusObra,
} from "@/types/api";

export default function ObraDetalhePage() {
  const params = useParams();
  const router = useRouter();
  const obraId = Number(params.id);

  const [detalhe, setDetalhe] = useState<ObraDetalheResponse | null>(null);
  const [lancamentos, setLancamentos] = useState<LancamentoResponse[]>([]);
  const [centrosCusto, setCentrosCusto] = useState<CentroCustoResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Controle rápido de status/progresso
  const [statusEdit, setStatusEdit] = useState<StatusObra>("PLANEJAMENTO");
  const [progressoEdit, setProgressoEdit] = useState(0);
  const [savingStatus, setSavingStatus] = useState(false);

  // Modais
  const [editObraOpen, setEditObraOpen] = useState(false);
  const [lancamentoModalOpen, setLancamentoModalOpen] = useState(false);
  const [editingLancamento, setEditingLancamento] =
    useState<LancamentoResponse | null>(null);

  const carregar = useCallback(async () => {
    if (!obraId || Number.isNaN(obraId)) return;
    setLoading(true);
    setError(null);
    try {
      const [det, lancPage, centros] = await Promise.all([
        api.obras.buscarPorId(obraId),
        api.obras.listarLancamentos(obraId, { size: 50 }),
        api.centrosCusto.listar(false),
      ]);
      setDetalhe(det);
      setStatusEdit(det.obra.status);
      setProgressoEdit(det.obra.progressoPercentual);
      setLancamentos(lancPage.content);
      setCentrosCusto(centros);
    } catch {
      setError("Não foi possível carregar os detalhes da obra.");
    } finally {
      setLoading(false);
    }
  }, [obraId]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  async function handleAtualizarStatus(e: React.FormEvent) {
    e.preventDefault();
    setSavingStatus(true);
    try {
      await api.obras.atualizarStatus(obraId, {
        status: statusEdit,
        progressoPercentual: progressoEdit,
      });
      await carregar();
    } catch {
      setError("Erro ao atualizar status e progresso da obra.");
    } finally {
      setSavingStatus(false);
    }
  }

  async function handleMarcarPago(id: number) {
    try {
      await api.lancamentos.atualizarPagamento(id, { statusPagamento: "PAGO" });
      carregar();
    } catch {
      setError("Erro ao registrar pagamento.");
    }
  }

  async function handleExcluirLancamento(id: number) {
    if (!window.confirm("Excluir este lançamento financeiro?")) return;
    try {
      await api.lancamentos.excluir(id);
      carregar();
    } catch {
      setError("Erro ao excluir lançamento.");
    }
  }

  async function handleExcluirObra() {
    if (!detalhe) return;
    if (
      !window.confirm(
        `Excluir a obra "${detalhe.obra.nome}" e todos os seus lançamentos?`
      )
    ) {
      return;
    }
    try {
      await api.obras.excluir(obraId);
      router.push("/obras");
    } catch {
      setError("Erro ao excluir obra.");
    }
  }

  if (loading && !detalhe) {
    return <div className="card h-64 animate-pulse bg-slate-100" />;
  }

  if (!detalhe) {
    return (
      <div className="space-y-4">
        <Link href="/obras" className="btn-secondary">
          <ArrowLeft className="h-4 w-4" />
          Voltar para Obras
        </Link>
        {error && <ErrorBanner message={error} onRetry={carregar} />}
      </div>
    );
  }

  const { obra, diasRestantes, resumoFinanceiro } = detalhe;

  return (
    <div className="space-y-6">
      {/* Cabeçalho */}
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div className="space-y-2">
          <Link
            href="/obras"
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-brand-600"
          >
            <ArrowLeft className="h-3.5 w-3.5" />
            Voltar para Obras
          </Link>
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-2xl font-bold text-slate-900">{obra.nome}</h1>
            <StatusObraBadge status={obra.status} />
            {obra.atrasada && (
              <span className="inline-flex items-center gap-1 rounded-full bg-rose-100 px-2.5 py-0.5 text-xs font-semibold text-rose-700">
                <AlertTriangle className="h-3.5 w-3.5" />
                Atrasada ({Math.abs(diasRestantes)}d)
              </span>
            )}
          </div>
          {obra.descricao && (
            <p className="max-w-3xl text-sm text-slate-600">{obra.descricao}</p>
          )}
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => setEditObraOpen(true)}
            className="btn-secondary"
          >
            <Pencil className="h-4 w-4" />
            Editar Obra
          </button>
          <button
            type="button"
            onClick={handleExcluirObra}
            className="btn-danger"
          >
            <Trash2 className="h-4 w-4" />
            Excluir
          </button>
        </div>
      </div>

      {error && <ErrorBanner message={error} onRetry={carregar} />}

      {/* Dados cadastrais + Atualização rápida de status/progresso */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="card space-y-3 p-6 lg:col-span-2">
          <h2 className="text-base font-semibold text-slate-900">
            Dados Cadastrais e Cronograma
          </h2>
          <div className="grid grid-cols-1 gap-4 text-sm sm:grid-cols-2">
            <div>
              <p className="text-xs font-semibold uppercase text-slate-400">
                Cliente
              </p>
              <p className="mt-0.5 font-medium text-slate-800">
                {obra.cliente || "Não informado"}
              </p>
            </div>
            <div>
              <p className="text-xs font-semibold uppercase text-slate-400">
                Responsável Técnico
              </p>
              <p className="mt-0.5 flex items-center gap-1.5 font-medium text-slate-800">
                <User className="h-4 w-4 text-slate-400" />
                {obra.responsavel}
              </p>
            </div>
            <div className="sm:col-span-2">
              <p className="text-xs font-semibold uppercase text-slate-400">
                Endereço
              </p>
              <p className="mt-0.5 flex items-center gap-1.5 font-medium text-slate-800">
                <MapPin className="h-4 w-4 text-slate-400" />
                {obra.endereco}
              </p>
            </div>
            <div>
              <p className="text-xs font-semibold uppercase text-slate-400">
                Período Previsto
              </p>
              <p className="mt-0.5 flex items-center gap-1.5 font-medium text-slate-800">
                <Calendar className="h-4 w-4 text-slate-400" />
                {formatDate(obra.dataInicio)} até {formatDate(obra.previsaoFim)}
              </p>
            </div>
            <div>
              <p className="text-xs font-semibold uppercase text-slate-400">
                Prazo / Conclusão
              </p>
              <p className="mt-0.5 font-medium text-slate-800">
                {obra.dataConclusao
                  ? `Concluída em ${formatDate(obra.dataConclusao)}`
                  : diasRestantes >= 0
                    ? `${diasRestantes} dias restantes`
                    : `${Math.abs(diasRestantes)} dias de atraso`}
              </p>
            </div>
          </div>
        </div>

        <form onSubmit={handleAtualizarStatus} className="card space-y-4 p-6">
          <h2 className="text-base font-semibold text-slate-900">
            Atualizar Andamento
          </h2>
          <div>
            <label className="label-field">Status</label>
            <select
              value={statusEdit}
              onChange={(e) => setStatusEdit(e.target.value as StatusObra)}
              className="input-field"
            >
              {ALL_STATUS_OBRA.map((s) => (
                <option key={s} value={s}>
                  {STATUS_OBRA_LABELS[s]}
                </option>
              ))}
            </select>
          </div>
          <div>
            <div className="mb-1 flex items-center justify-between">
              <label className="label-field mb-0">Progresso Físico</label>
              <span className="text-sm font-bold text-brand-600">
                {progressoEdit}%
              </span>
            </div>
            <input
              type="range"
              min={0}
              max={100}
              value={progressoEdit}
              onChange={(e) => setProgressoEdit(Number(e.target.value))}
              className="w-full accent-brand-600"
            />
          </div>
          <button
            type="submit"
            disabled={savingStatus}
            className="btn-primary w-full"
          >
            {savingStatus ? "Atualizando..." : "Salvar Andamento"}
          </button>
        </form>
      </div>

      {/* Resumo Financeiro da Obra */}
      <div className="card space-y-5 p-6">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <h2 className="text-base font-semibold text-slate-900">
            Resumo Financeiro da Obra
          </h2>
          {resumoFinanceiro.orcamentoExcedido && (
            <span className="inline-flex items-center gap-1 rounded-full bg-rose-100 px-3 py-1 text-xs font-semibold text-rose-700">
              <AlertTriangle className="h-3.5 w-3.5" />
              Orçamento excedido (
              {formatPercent(resumoFinanceiro.percentualOrcamentoUtilizado)})
            </span>
          )}
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <div className="rounded-lg border border-slate-200 bg-slate-50 p-4">
            <p className="text-xs font-semibold uppercase text-slate-500">
              Orçamento Previsto
            </p>
            <p className="mt-1 text-xl font-bold text-slate-900">
              {formatCurrency(resumoFinanceiro.orcamentoPrevisto)}
            </p>
            <p className="mt-1 text-xs text-slate-500">
              Disponível:{" "}
              <strong
                className={
                  resumoFinanceiro.orcamentoDisponivel < 0
                    ? "text-rose-600"
                    : "text-emerald-700"
                }
              >
                {formatCurrency(resumoFinanceiro.orcamentoDisponivel)}
              </strong>
            </p>
          </div>

          <div className="rounded-lg border border-slate-200 bg-slate-50 p-4">
            <p className="text-xs font-semibold uppercase text-slate-500">
              Receitas
            </p>
            <p className="mt-1 text-xl font-bold text-emerald-700">
              {formatCurrency(resumoFinanceiro.totalReceitasPagas)}
            </p>
            <p className="mt-1 text-xs text-slate-500">
              Pendentes: {formatCurrency(resumoFinanceiro.totalReceitasPendentes)}
            </p>
          </div>

          <div className="rounded-lg border border-slate-200 bg-slate-50 p-4">
            <p className="text-xs font-semibold uppercase text-slate-500">
              Despesas
            </p>
            <p className="mt-1 text-xl font-bold text-rose-700">
              {formatCurrency(resumoFinanceiro.totalDespesasPagas)}
            </p>
            <p className="mt-1 text-xs text-slate-500">
              Pendentes: {formatCurrency(resumoFinanceiro.totalDespesasPendentes)}
            </p>
          </div>

          <div className="rounded-lg border border-slate-200 bg-slate-50 p-4">
            <p className="text-xs font-semibold uppercase text-slate-500">
              Saldo Realizado
            </p>
            <p
              className={`mt-1 text-xl font-bold ${
                resumoFinanceiro.saldoRealizado >= 0
                  ? "text-emerald-700"
                  : "text-rose-700"
              }`}
            >
              {formatCurrency(resumoFinanceiro.saldoRealizado)}
            </p>
            <p className="mt-1 text-xs text-slate-500">
              Projetado: {formatCurrency(resumoFinanceiro.saldoProjetado)}
            </p>
          </div>
        </div>

        <div>
          <div className="mb-1 flex items-center justify-between text-xs">
            <span className="font-medium text-slate-600">
              Consumo do Orçamento Previsto
            </span>
            <span className="font-semibold text-slate-900">
              {formatPercent(resumoFinanceiro.percentualOrcamentoUtilizado)}
            </span>
          </div>
          <ProgressBar
            value={resumoFinanceiro.percentualOrcamentoUtilizado}
            danger={resumoFinanceiro.orcamentoExcedido}
          />
        </div>

        {resumoFinanceiro.despesasPorCentroCusto.length > 0 && (
          <div className="border-t border-slate-200 pt-4">
            <h3 className="mb-3 text-xs font-semibold uppercase tracking-wide text-slate-500">
              Despesas por Centro de Custo
            </h3>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {resumoFinanceiro.despesasPorCentroCusto.map((item, idx) => (
                <div
                  key={item.centroCustoId ?? idx}
                  className="flex items-center justify-between rounded-lg border border-slate-200 px-3.5 py-2.5 text-sm"
                >
                  <span className="truncate font-medium text-slate-700">
                    {item.centroCustoNome}
                  </span>
                  <span className="ml-2 shrink-0 font-bold text-slate-900">
                    {formatCurrency(item.totalValor)}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Extrato da Obra */}
      <div className="card overflow-hidden">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 px-6 py-4">
          <h2 className="text-base font-semibold text-slate-900">
            Lançamentos Financeiros da Obra ({lancamentos.length})
          </h2>
          <button
            type="button"
            onClick={() => {
              setEditingLancamento(null);
              setLancamentoModalOpen(true);
            }}
            className="btn-primary text-xs"
          >
            <Plus className="h-4 w-4" />
            Novo Lançamento
          </button>
        </div>

        {lancamentos.length === 0 ? (
          <div className="px-6 py-10 text-center text-sm text-slate-500">
            Nenhum lançamento financeiro registrado para esta obra.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200 text-sm">
              <thead className="bg-slate-50 text-left text-xs font-semibold uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3">Descrição</th>
                  <th className="px-4 py-3">Centro de Custo</th>
                  <th className="px-4 py-3">Competência</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3 text-right">Valor</th>
                  <th className="px-4 py-3 text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {lancamentos.map((l) => (
                  <tr key={l.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3 font-medium text-slate-900">
                      {l.descricao}
                    </td>
                    <td className="px-4 py-3 text-slate-600">
                      {l.centroCustoNome || "—"}
                    </td>
                    <td className="px-4 py-3 text-slate-600">
                      {formatDate(l.dataCompetencia)}
                    </td>
                    <td className="px-4 py-3">
                      <StatusPagamentoBadge status={l.statusPagamento} />
                    </td>
                    <td
                      className={`px-4 py-3 text-right font-semibold ${
                        l.tipo === "RECEITA"
                          ? "text-emerald-700"
                          : "text-rose-700"
                      }`}
                    >
                      {l.tipo === "RECEITA" ? "+ " : "- "}
                      {formatCurrency(l.valor)}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-1.5">
                        {l.statusPagamento === "PENDENTE" && (
                          <button
                            type="button"
                            onClick={() => handleMarcarPago(l.id)}
                            title="Marcar como pago"
                            className="rounded-lg border border-emerald-200 bg-emerald-50 p-1.5 text-emerald-700 hover:bg-emerald-100"
                          >
                            <CheckCircle2 className="h-4 w-4" />
                          </button>
                        )}
                        <button
                          type="button"
                          onClick={() => {
                            setEditingLancamento(l);
                            setLancamentoModalOpen(true);
                          }}
                          title="Editar"
                          className="rounded-lg border border-slate-200 p-1.5 text-slate-600 hover:bg-slate-100"
                        >
                          <Pencil className="h-4 w-4" />
                        </button>
                        <button
                          type="button"
                          onClick={() => handleExcluirLancamento(l.id)}
                          title="Excluir"
                          className="rounded-lg border border-slate-200 p-1.5 text-rose-600 hover:bg-rose-50"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <ObraFormModal
        open={editObraOpen}
        obra={obra}
        onClose={() => setEditObraOpen(false)}
        onSaved={carregar}
      />

      <LancamentoFormModal
        open={lancamentoModalOpen}
        lancamento={editingLancamento}
        fixedObraId={obraId}
        obras={[obra]}
        centrosCusto={centrosCusto}
        onClose={() => setLancamentoModalOpen(false)}
        onSaved={carregar}
      />
    </div>
  );
}
