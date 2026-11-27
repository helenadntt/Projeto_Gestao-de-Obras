"use client";

import { useCallback, useEffect, useState } from "react";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { ErrorBanner, Modal } from "@/components/ui/Common";
import { api, ApiClientError } from "@/services/api";
import type { CentroCustoResponse } from "@/types/api";

export default function CentrosCustoPage() {
  const [centros, setCentros] = useState<CentroCustoResponse[]>([]);
  const [apenasAtivos, setApenasAtivos] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Modal state
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<CentroCustoResponse | null>(null);
  const [nome, setNome] = useState("");
  const [descricao, setDescricao] = useState("");
  const [ativo, setAtivo] = useState(true);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const carregar = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const list = await api.centrosCusto.listar(apenasAtivos);
      setCentros(list);
    } catch {
      setError("Não foi possível carregar os centros de custo.");
    } finally {
      setLoading(false);
    }
  }, [apenasAtivos]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  function openCreate() {
    setEditing(null);
    setNome("");
    setDescricao("");
    setAtivo(true);
    setFormError(null);
    setModalOpen(true);
  }

  function openEdit(cc: CentroCustoResponse) {
    setEditing(cc);
    setNome(cc.nome);
    setDescricao(cc.descricao ?? "");
    setAtivo(cc.ativo);
    setFormError(null);
    setModalOpen(true);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setFormError(null);
    try {
      const payload = {
        nome,
        descricao: descricao || undefined,
        ativo,
      };
      if (editing) {
        await api.centrosCusto.atualizar(editing.id, payload);
      } else {
        await api.centrosCusto.criar(payload);
      }
      setModalOpen(false);
      carregar();
    } catch (err) {
      if (err instanceof ApiClientError) {
        setFormError(err.message);
      } else {
        setFormError("Erro ao salvar centro de custo.");
      }
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(cc: CentroCustoResponse) {
    if (!window.confirm(`Excluir o centro de custo "${cc.nome}"?`)) {
      return;
    }
    try {
      await api.centrosCusto.excluir(cc.id);
      carregar();
    } catch (err) {
      if (err instanceof ApiClientError) {
        setError(err.message);
      } else {
        setError("Erro ao excluir centro de custo.");
      }
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Centros de Custo
          </h1>
          <p className="mt-1 text-sm text-slate-500">
            Categorize receitas e despesas para análises precisas no dashboard.
          </p>
        </div>

        <button type="button" onClick={openCreate} className="btn-primary">
          <Plus className="h-4 w-4" />
          Novo Centro de Custo
        </button>
      </div>

      <div className="card flex items-center justify-between px-4 py-3">
        <label className="inline-flex cursor-pointer items-center gap-2 text-sm text-slate-700">
          <input
            type="checkbox"
            checked={apenasAtivos}
            onChange={(e) => setApenasAtivos(e.target.checked)}
            className="h-4 w-4 rounded border-slate-300 accent-brand-600"
          />
          Mostrar apenas centros de custo ativos
        </label>
        <span className="text-xs font-medium text-slate-500">
          {centros.length} cadastrados
        </span>
      </div>

      {error && <ErrorBanner message={error} onRetry={carregar} />}

      <div className="card overflow-hidden">
        {loading ? (
          <div className="h-48 animate-pulse bg-slate-100" />
        ) : centros.length === 0 ? (
          <div className="p-10 text-center text-sm text-slate-500">
            Nenhum centro de custo encontrado.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200 text-sm">
              <thead className="bg-slate-50 text-left text-xs font-semibold uppercase text-slate-500">
                <tr>
                  <th className="px-5 py-3">Nome</th>
                  <th className="px-5 py-3">Descrição</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3 text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {centros.map((cc) => (
                  <tr key={cc.id} className="hover:bg-slate-50">
                    <td className="px-5 py-3.5 font-semibold text-slate-900">
                      {cc.nome}
                    </td>
                    <td className="px-5 py-3.5 text-slate-600">
                      {cc.descricao || "—"}
                    </td>
                    <td className="px-5 py-3.5">
                      <span
                        className={`inline-flex rounded-full border px-2.5 py-0.5 text-xs font-medium ${
                          cc.ativo
                            ? "border-emerald-200 bg-emerald-100 text-emerald-800"
                            : "border-slate-300 bg-slate-200 text-slate-600"
                        }`}
                      >
                        {cc.ativo ? "Ativo" : "Inativo"}
                      </span>
                    </td>
                    <td className="px-5 py-3.5">
                      <div className="flex justify-end gap-1.5">
                        <button
                          type="button"
                          onClick={() => openEdit(cc)}
                          className="rounded-lg border border-slate-200 p-1.5 text-slate-600 hover:bg-slate-100"
                          title="Editar"
                        >
                          <Pencil className="h-4 w-4" />
                        </button>
                        <button
                          type="button"
                          onClick={() => handleDelete(cc)}
                          className="rounded-lg border border-slate-200 p-1.5 text-rose-600 hover:bg-rose-50"
                          title="Excluir"
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

      <Modal
        open={modalOpen}
        title={editing ? "Editar Centro de Custo" : "Novo Centro de Custo"}
        onClose={() => setModalOpen(false)}
        maxWidth="max-w-lg"
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {formError && <ErrorBanner message={formError} />}

          <div>
            <label className="label-field">Nome *</label>
            <input
              type="text"
              required
              maxLength={100}
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              placeholder="Ex.: Instalações Elétricas"
              className="input-field"
            />
          </div>

          <div>
            <label className="label-field">Descrição</label>
            <textarea
              rows={3}
              maxLength={255}
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              placeholder="Detalhe quais custos pertencem a esta categoria..."
              className="input-field"
            />
          </div>

          <label className="inline-flex cursor-pointer items-center gap-2 text-sm text-slate-700">
            <input
              type="checkbox"
              checked={ativo}
              onChange={(e) => setAtivo(e.target.checked)}
              className="h-4 w-4 rounded border-slate-300 accent-brand-600"
            />
            Centro de custo ativo (disponível para novos lançamentos)
          </label>

          <div className="flex justify-end gap-3 border-t border-slate-200 pt-4">
            <button
              type="button"
              onClick={() => setModalOpen(false)}
              className="btn-secondary"
            >
              Cancelar
            </button>
            <button type="submit" disabled={saving} className="btn-primary">
              {saving ? "Salvando..." : "Salvar"}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
