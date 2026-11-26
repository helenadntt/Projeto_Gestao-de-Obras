"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import {
  CheckCircle2,
  Pencil,
  Plus,
  RotateCcw,
  Trash2,
} from "lucide-react";
import { LancamentoFormModal } from "@/components/financeiro/LancamentoFormModal";
import {
  ErrorBanner,
  Pagination,
  StatusPagamentoBadge,
} from "@/components/ui/Common";
import { formatCurrency, formatDate } from "@/lib/format";
import { api } from "@/services/api";
import type {
  CentroCustoResponse,
  LancamentoResponse,
  ObraResponse,
  PageResponse,
  StatusPagamento,
  TipoLancamento,
} from "@/types/api";

export default function FinanceiroPage() {
  const [data, setData] = useState<PageResponse<LancamentoResponse> | null>(null);
  const [obras, setObras] = useState<ObraResponse[]>([]);
  const [centrosCusto, setCentrosCusto] = useState<CentroCustoResponse[]>([]);

  // Filtros
  const [obraId, setObraId] = useState<number | "">("");
  const [centroCustoId, setCentroCustoId] = useState<number | "">("");
  const [tipo, setTipo] = useState<TipoLancamento | "">("");
  const [statusPagamento, setStatusPagamento] = useState<StatusPagamento | "">("");
  const [dataInicio, setDataInicio] = useState("");
  const [dataFim, setDataFim] = useState("");
  const [page, setPage] = useState(0);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<LancamentoResponse | null>(null);

  const carregarApoio = useCallback(async () => {
    try {
      const [obrasPage, centros] = await Promise.all([
        api.obras.listar({ size: 100 }),
        api.centrosCusto.listar(false),
      ]);
      setObras(obrasPage.content);
      setCentrosCusto(centros);
    } catch {
      // Erro principal será tratado no carregamento da tabela
    }
  }, []);

  const carregarLancamentos = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await api.lancamentos.listar({
        obraId,
        centroCustoId,
        tipo,
        statusPagamento,
        dataInicio,
        dataFim,
        page,
        size: 15,
      });
      setData(result);
    } catch {
      setError("Não foi possível carregar os lançamentos financeiros.");
    } finally {
      setLoading(false);
    }
  }, [obraId, centroCustoId, tipo, statusPagamento, dataInicio, dataFim, page]);

  useEffect(() => {
    carregarApoio();
  }, [carregarApoio]);

  useEffect(() => {
    carregarLancamentos();
  }, [carregarLancamentos]);

  function limparFiltros() {
    setObraId("");
    setCentroCustoId("");
    setTipo("");
    setStatusPagamento("");
    setDataInicio("");
    setDataFim("");
    setPage(0);
  }

  async function handleMarcarPago(id: number) {
    try {
      await api.lancamentos.atualizarPagamento(id, { statusPagamento: "PAGO" });
      carregarLancamentos();
    } catch {
      setError("Erro ao confirmar pagamento.");
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm("Deseja realmente excluir este lançamento financeiro?")) {
      return;
    }
    try {
      await api.lancamentos.excluir(id);
      carregarLancamentos();
    } catch {
      setError("Erro ao excluir lançamento.");
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Gestão Financeira
          </h1>
          <p className="mt-1 text-sm text-slate-500">
            Controle receitas, despesas, pagamentos e fluxo de caixa por obra e centro de custo.
          </p>
        </div>

        <button
          type="button"
          onClick={() => {
            setEditing(null);
            setModalOpen(true);
          }}
          disabled={obras.length === 0}
          className="btn-primary"
          title={
            obras.length === 0
              ? "Cadastre ao menos uma obra antes de registrar lançamentos"
              : undefined
          }
        >
          <Plus className="h-4 w-4" />
          Novo Lançamento
        </button>
      </div>

      {/* Filtros */}
      <div className="card space-y-3 p-4">
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-6">
          <select
            value={obraId}
            onChange={(e) => {
              setObraId(e.target.value ? Number(e.target.value) : "");
              setPage(0);
            }}
            className="input-field"
          >
            <option value="">Todas as obras</option>
            {obras.map((o) => (
              <option key={o.id} value={o.id}>
                {o.nome}
              </option>
            ))}
          </select>

          <select
            value={centroCustoId}
            onChange={(e) => {
              setCentroCustoId(e.target.value ? Number(e.target.value) : "");
              setPage(0);
            }}
            className="input-field"
          >
            <option value="">Todos centros de custo</option>
            {centrosCusto.map((cc) => (
              <option key={cc.id} value={cc.id}>
                {cc.nome}
              </option>
            ))}
          </select>

          <select
            value={tipo}
            onChange={(e) => {
              setTipo(e.target.value as TipoLancamento | "");
              setPage(0);
            }}
            className="input-field"
          >
            <option value="">Receitas e Despesas</option>
            <option value="RECEITA">Receitas</option>
            <option value="DESPESA">Despesas</option>
          </select>

          <select
            value={statusPagamento}
            onChange={(e) => {
              setStatusPagamento(e.target.value as StatusPagamento | "");
              setPage(0);
            }}
            className="input-field"
          >
            <option value="">Todos status</option>
            <option value="PENDENTE">Pendente</option>
            <option value="PAGO">Pago</option>
            <option value="CANCELADO">Cancelado</option>
          </select>

          <input
            type="date"
            value={dataInicio}
            onChange={(e) => {
              setDataInicio(e.target.value);
              setPage(0);
            }}
            className="input-field"
            title="Competência a partir de"
          />

          <input
            type="date"
            value={dataFim}
            onChange={(e) => {
              setDataFim(e.target.value);
              setPage(0);
            }}
            className="input-field"
            title="Competência até"
          />
        </div>

        <div className="flex justify-end">
          <button
            type="button"
            onClick={limparFiltros}
            className="inline-flex items-center gap-1.5 text-xs font-medium text-slate-500 hover:text-slate-800"
          >
            <RotateCcw className="h-3.5 w-3.5" />
            Limpar filtros
          </button>
        </div>
      </div>

      {error && <ErrorBanner message={error} onRetry={carregarLancamentos} />}

      <div className="card overflow-hidden">
        {loading && !data ? (
          <div className="h-56 animate-pulse bg-slate-100" />
        ) : data && data.content.length === 0 ? (
          <div className="p-12 text-center text-sm text-slate-500">
            Nenhum lançamento financeiro encontrado para os filtros aplicados.
          </div>
        ) : data ? (
          <>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200 text-sm">
                <thead className="bg-slate-50 text-left text-xs font-semibold uppercase text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Descrição</th>
                    <th className="px-4 py-3">Obra</th>
                    <th className="px-4 py-3">Centro de Custo</th>
                    <th className="px-4 py-3">Competência</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3 text-right">Valor</th>
                    <th className="px-4 py-3 text-right">Ações</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {data.content.map((l) => (
                    <tr key={l.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-medium text-slate-900">
                        {l.descricao}
                      </td>
                      <td className="px-4 py-3">
                        <Link
                          href={`/obras/${l.obraId}`}
                          className="font-medium text-brand-600 hover:underline"
                        >
                          {l.obraNome}
                        </Link>
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
                              setEditing(l);
                              setModalOpen(true);
                            }}
                            title="Editar"
                            className="rounded-lg border border-slate-200 p-1.5 text-slate-600 hover:bg-slate-100"
                          >
                            <Pencil className="h-4 w-4" />
                          </button>
                          <button
                            type="button"
                            onClick={() => handleDelete(l.id)}
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
            <Pagination page={data.page} onPageChange={setPage} />
          </>
        ) : null}
      </div>

      <LancamentoFormModal
        open={modalOpen}
        lancamento={editing}
        obras={obras}
        centrosCusto={centrosCusto}
        onClose={() => setModalOpen(false)}
        onSaved={carregarLancamentos}
      />
    </div>
  );
}
