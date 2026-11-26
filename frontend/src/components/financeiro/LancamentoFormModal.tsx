"use client";

import { useEffect, useState } from "react";
import { ErrorBanner, Modal } from "@/components/ui/Common";
import { api, ApiClientError } from "@/services/api";
import type {
  CentroCustoResponse,
  LancamentoRequest,
  LancamentoResponse,
  ObraResponse,
  StatusPagamento,
  TipoLancamento,
} from "@/types/api";

interface Props {
  open: boolean;
  lancamento?: LancamentoResponse | null;
  fixedObraId?: number;
  obras: ObraResponse[];
  centrosCusto: CentroCustoResponse[];
  onClose: () => void;
  onSaved: () => void;
}

const todayIso = () => new Date().toISOString().slice(0, 10);

export function LancamentoFormModal({
  open,
  lancamento,
  fixedObraId,
  obras,
  centrosCusto,
  onClose,
  onSaved,
}: Props) {
  const [obraId, setObraId] = useState<string>("");
  const [centroCustoId, setCentroCustoId] = useState<string>("");
  const [descricao, setDescricao] = useState("");
  const [tipo, setTipo] = useState<TipoLancamento>("DESPESA");
  const [valor, setValor] = useState("");
  const [dataCompetencia, setDataCompetencia] = useState(todayIso());
  const [statusPagamento, setStatusPagamento] = useState<StatusPagamento>("PENDENTE");
  const [dataPagamento, setDataPagamento] = useState("");
  const [comprovanteUrl, setComprovanteUrl] = useState("");
  const [observacoes, setObservacoes] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (!open) return;
    setError(null);
    setFieldErrors({});
    if (lancamento) {
      setObraId(String(lancamento.obraId));
      setCentroCustoId(lancamento.centroCustoId ? String(lancamento.centroCustoId) : "");
      setDescricao(lancamento.descricao);
      setTipo(lancamento.tipo);
      setValor(String(lancamento.valor));
      setDataCompetencia(lancamento.dataCompetencia);
      setStatusPagamento(lancamento.statusPagamento);
      setDataPagamento(lancamento.dataPagamento ?? "");
      setComprovanteUrl(lancamento.comprovanteUrl ?? "");
      setObservacoes(lancamento.observacoes ?? "");
    } else {
      setObraId(
        fixedObraId
          ? String(fixedObraId)
          : obras.length > 0
            ? String(obras[0].id)
            : ""
      );
      setCentroCustoId("");
      setDescricao("");
      setTipo("DESPESA");
      setValor("");
      setDataCompetencia(todayIso());
      setStatusPagamento("PENDENTE");
      setDataPagamento("");
      setComprovanteUrl("");
      setObservacoes("");
    }
  }, [open, lancamento, fixedObraId, obras]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setFieldErrors({});

    const payload: LancamentoRequest = {
      obraId: Number(obraId),
      centroCustoId: centroCustoId ? Number(centroCustoId) : null,
      descricao,
      tipo,
      valor: Number(valor),
      dataCompetencia,
      statusPagamento,
      dataPagamento: statusPagamento === "PAGO" ? dataPagamento || null : null,
      comprovanteUrl: comprovanteUrl || undefined,
      observacoes: observacoes || undefined,
    };

    try {
      if (lancamento) {
        await api.lancamentos.atualizar(lancamento.id, payload);
      } else {
        await api.lancamentos.criar(payload);
      }
      onSaved();
      onClose();
    } catch (err) {
      if (err instanceof ApiClientError) {
        setError(err.message);
        setFieldErrors(err.fieldErrors);
      } else {
        setError("Não foi possível salvar o lançamento.");
      }
    } finally {
      setSaving(false);
    }
  }

  const centrosDisponiveis = centrosCusto.filter(
    (c) => c.ativo || (lancamento && c.id === lancamento.centroCustoId)
  );

  return (
    <Modal
      open={open}
      title={lancamento ? "Editar Lançamento Financeiro" : "Novo Lançamento Financeiro"}
      onClose={onClose}
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <ErrorBanner message={error} />}

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {!fixedObraId && (
            <div className="sm:col-span-2">
              <label className="label-field">Obra *</label>
              <select
                required
                value={obraId}
                onChange={(e) => setObraId(e.target.value)}
                className="input-field"
              >
                <option value="">Selecione uma obra...</option>
                {obras.map((o) => (
                  <option key={o.id} value={o.id}>
                    {o.nome}
                  </option>
                ))}
              </select>
              {fieldErrors.obraId && (
                <p className="mt-1 text-xs text-rose-600">{fieldErrors.obraId}</p>
              )}
            </div>
          )}

          <div className="sm:col-span-2">
            <label className="label-field">Descrição *</label>
            <input
              type="text"
              required
              maxLength={200}
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              placeholder="Ex.: Compra de cimento CP-II ou 1ª medição do cliente"
              className="input-field"
            />
            {fieldErrors.descricao && (
              <p className="mt-1 text-xs text-rose-600">{fieldErrors.descricao}</p>
            )}
          </div>

          <div>
            <label className="label-field">Tipo *</label>
            <select
              value={tipo}
              onChange={(e) => setTipo(e.target.value as TipoLancamento)}
              className="input-field"
            >
              <option value="DESPESA">Despesa (Saída)</option>
              <option value="RECEITA">Receita (Entrada)</option>
            </select>
          </div>

          <div>
            <label className="label-field">Centro de Custo</label>
            <select
              value={centroCustoId}
              onChange={(e) => setCentroCustoId(e.target.value)}
              className="input-field"
            >
              <option value="">Sem centro de custo</option>
              {centrosDisponiveis.map((cc) => (
                <option key={cc.id} value={cc.id}>
                  {cc.nome}
                  {!cc.ativo ? " (Inativo)" : ""}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="label-field">Valor (R$) *</label>
            <input
              type="number"
              required
              min="0.01"
              step="0.01"
              value={valor}
              onChange={(e) => setValor(e.target.value)}
              placeholder="0.00"
              className="input-field"
            />
            {fieldErrors.valor && (
              <p className="mt-1 text-xs text-rose-600">{fieldErrors.valor}</p>
            )}
          </div>

          <div>
            <label className="label-field">Data de Competência *</label>
            <input
              type="date"
              required
              value={dataCompetencia}
              onChange={(e) => setDataCompetencia(e.target.value)}
              className="input-field"
            />
          </div>

          <div>
            <label className="label-field">Status de Pagamento</label>
            <select
              value={statusPagamento}
              onChange={(e) => setStatusPagamento(e.target.value as StatusPagamento)}
              className="input-field"
            >
              <option value="PENDENTE">Pendente</option>
              <option value="PAGO">Pago</option>
              <option value="CANCELADO">Cancelado</option>
            </select>
          </div>

          <div>
            <label className="label-field">Data de Pagamento</label>
            <input
              type="date"
              disabled={statusPagamento !== "PAGO"}
              value={statusPagamento === "PAGO" ? dataPagamento : ""}
              onChange={(e) => setDataPagamento(e.target.value)}
              className="input-field disabled:bg-slate-100 disabled:text-slate-400"
            />
          </div>

          <div className="sm:col-span-2">
            <label className="label-field">URL do Comprovante (opcional)</label>
            <input
              type="url"
              maxLength={500}
              value={comprovanteUrl}
              onChange={(e) => setComprovanteUrl(e.target.value)}
              placeholder="https://..."
              className="input-field"
            />
          </div>

          <div className="sm:col-span-2">
            <label className="label-field">Observações</label>
            <textarea
              rows={2}
              value={observacoes}
              onChange={(e) => setObservacoes(e.target.value)}
              placeholder="Número de NF, fornecedor ou informações adicionais..."
              className="input-field"
            />
          </div>
        </div>

        <div className="flex justify-end gap-3 border-t border-slate-200 pt-4">
          <button type="button" onClick={onClose} className="btn-secondary">
            Cancelar
          </button>
          <button type="submit" disabled={saving} className="btn-primary">
            {saving
              ? "Salvando..."
              : lancamento
                ? "Salvar Alterações"
                : "Registrar Lançamento"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
