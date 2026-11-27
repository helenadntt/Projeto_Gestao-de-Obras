"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import {
  AlertTriangle,
  Calendar,
  Eye,
  MapPin,
  Pencil,
  Plus,
  Search,
  Trash2,
  User,
} from "lucide-react";
import { ObraFormModal } from "@/components/obras/ObraFormModal";
import {
  ErrorBanner,
  Pagination,
  ProgressBar,
  StatusObraBadge,
} from "@/components/ui/Common";
import {
  ALL_STATUS_OBRA,
  formatCurrency,
  formatDate,
  STATUS_OBRA_LABELS,
} from "@/lib/format";
import { api, ApiClientError } from "@/services/api";
import type { ObraResponse, PageResponse, StatusObra } from "@/types/api";

export default function ObrasPage() {
  const [data, setData] = useState<PageResponse<ObraResponse> | null>(null);
  const [status, setStatus] = useState<StatusObra | "">("");
  const [busca, setBusca] = useState("");
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [modalOpen, setModalOpen] = useState(false);
  const [editingObra, setEditingObra] = useState<ObraResponse | null>(null);

  const carregar = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await api.obras.listar({
        status,
        busca: busca.trim() || undefined,
        page,
        size: 9,
      });
      setData(result);
    } catch {
      setError("Não foi possível carregar as obras. Verifique se a API está ativa.");
    } finally {
      setLoading(false);
    }
  }, [status, busca, page]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  async function handleDelete(obra: ObraResponse) {
    if (!window.confirm(`Deseja excluir a obra "${obra.nome}"?`)) {
      return;
    }
    try {
      await api.obras.excluir(obra.id);
      carregar();
    } catch (err) {
      if (err instanceof ApiClientError) {
        setError(err.message);
      } else {
        setError("Erro ao excluir a obra.");
      }
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Gestão de Obras
          </h1>
          <p className="mt-1 text-sm text-slate-500">
            Cadastre projetos, acompanhe prazos, progresso físico e orçamento.
          </p>
        </div>

        <button
          type="button"
          onClick={() => {
            setEditingObra(null);
            setModalOpen(true);
          }}
          className="btn-primary"
        >
          <Plus className="h-4 w-4" />
          Nova Obra
        </button>
      </div>

      {/* Filtros */}
      <div className="card p-4">
        <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
          <div className="relative md:col-span-2">
            <Search className="pointer-events-none absolute left-3 top-2.5 h-4 w-4 text-slate-400" />
            <input
              type="text"
              value={busca}
              onChange={(e) => {
                setBusca(e.target.value);
                setPage(0);
              }}
              placeholder="Buscar por nome da obra, cliente ou responsável..."
              className="input-field pl-9"
            />
          </div>

          <select
            value={status}
            onChange={(e) => {
              setStatus(e.target.value as StatusObra | "");
              setPage(0);
            }}
            className="input-field"
          >
            <option value="">Todos os status</option>
            {ALL_STATUS_OBRA.map((s) => (
              <option key={s} value={s}>
                {STATUS_OBRA_LABELS[s]}
              </option>
            ))}
          </select>
        </div>
      </div>

      {error && <ErrorBanner message={error} onRetry={carregar} />}

      {loading && !data ? (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="card h-56 animate-pulse bg-slate-100" />
          ))}
        </div>
      ) : data && data.content.length === 0 ? (
        <div className="card p-12 text-center">
          <p className="text-base font-medium text-slate-700">
            Nenhuma obra encontrada.
          </p>
          <p className="mt-1 text-sm text-slate-500">
            Ajuste os filtros ou cadastre sua primeira obra para começar.
          </p>
        </div>
      ) : data ? (
        <div className="space-y-4">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
            {data.content.map((obra) => (
              <div
                key={obra.id}
                className="card flex flex-col justify-between p-5 transition hover:border-brand-500/50 hover:shadow-md"
              >
                <div>
                  <div className="flex items-start justify-between gap-2">
                    <Link
                      href={`/obras/${obra.id}`}
                      className="text-base font-semibold text-slate-900 hover:text-brand-600"
                    >
                      {obra.nome}
                    </Link>
                    <StatusObraBadge status={obra.status} />
                  </div>

                  {obra.cliente && (
                    <p className="mt-1 text-xs font-medium text-slate-500">
                      Cliente: {obra.cliente}
                    </p>
                  )}

                  <div className="mt-3 space-y-1.5 text-xs text-slate-600">
                    <p className="flex items-center gap-1.5">
                      <User className="h-3.5 w-3.5 text-slate-400" />
                      {obra.responsavel}
                    </p>
                    <p className="flex items-center gap-1.5 truncate">
                      <MapPin className="h-3.5 w-3.5 shrink-0 text-slate-400" />
                      <span className="truncate">{obra.endereco}</span>
                    </p>
                    <p className="flex items-center gap-1.5">
                      <Calendar className="h-3.5 w-3.5 text-slate-400" />
                      {formatDate(obra.dataInicio)} → {formatDate(obra.previsaoFim)}
                    </p>
                  </div>

                  {obra.atrasada && (
                    <div className="mt-3 inline-flex items-center gap-1 rounded-md bg-rose-50 px-2 py-1 text-xs font-medium text-rose-700">
                      <AlertTriangle className="h-3.5 w-3.5" />
                      Prazo previsto vencido
                    </div>
                  )}

                  <div className="mt-4">
                    <div className="mb-1 flex items-center justify-between text-xs">
                      <span className="text-slate-500">Progresso físico</span>
                      <span className="font-semibold text-slate-800">
                        {obra.progressoPercentual}%
                      </span>
                    </div>
                    <ProgressBar value={obra.progressoPercentual} />
                  </div>
                </div>

                <div className="mt-5 flex items-center justify-between border-t border-slate-100 pt-3">
                  <div>
                    <p className="text-[11px] uppercase text-slate-400">Orçamento</p>
                    <p className="text-sm font-bold text-slate-900">
                      {formatCurrency(obra.orcamentoPrevisto)}
                    </p>
                  </div>

                  <div className="flex items-center gap-1.5">
                    <Link
                      href={`/obras/${obra.id}`}
                      className="rounded-lg border border-slate-200 p-2 text-slate-600 hover:bg-slate-100 hover:text-brand-600"
                      title="Ver detalhes"
                    >
                      <Eye className="h-4 w-4" />
                    </Link>
                    <button
                      type="button"
                      onClick={() => {
                        setEditingObra(obra);
                        setModalOpen(true);
                      }}
                      className="rounded-lg border border-slate-200 p-2 text-slate-600 hover:bg-slate-100 hover:text-slate-900"
                      title="Editar"
                    >
                      <Pencil className="h-4 w-4" />
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDelete(obra)}
                      className="rounded-lg border border-slate-200 p-2 text-rose-600 hover:bg-rose-50"
                      title="Excluir"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="card">
            <Pagination page={data.page} onPageChange={setPage} />
          </div>
        </div>
      ) : null}

      <ObraFormModal
        open={modalOpen}
        obra={editingObra}
        onClose={() => setModalOpen(false)}
        onSaved={carregar}
      />
    </div>
  );
}
