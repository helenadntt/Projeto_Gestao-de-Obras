"use client";

import { useEffect, useState } from "react";
import { ErrorBanner, Modal } from "@/components/ui/Common";
import { ALL_STATUS_OBRA, STATUS_OBRA_LABELS } from "@/lib/format";
import { api, ApiClientError } from "@/services/api";
import type { ObraRequest, ObraResponse, StatusObra } from "@/types/api";

interface Props {
  open: boolean;
  obra?: ObraResponse | null;
  onClose: () => void;
  onSaved: () => void;
}

const todayIso = () => new Date().toISOString().slice(0, 10);

export function ObraFormModal({ open, obra, onClose, onSaved }: Props) {
  const [nome, setNome] = useState("");
  const [descricao, setDescricao] = useState("");
  const [cliente, setCliente] = useState("");
  const [endereco, setEndereco] = useState("");
  const [responsavel, setResponsavel] = useState("");
  const [orcamentoPrevisto, setOrcamentoPrevisto] = useState("0");
  const [dataInicio, setDataInicio] = useState(todayIso());
  const [previsaoFim, setPrevisaoFim] = useState(todayIso());
  const [dataConclusao, setDataConclusao] = useState("");
  const [status, setStatus] = useState<StatusObra>("PLANEJAMENTO");
  const [progressoPercentual, setProgressoPercentual] = useState("0");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (!open) return;
    setError(null);
    setFieldErrors({});
    if (obra) {
      setNome(obra.nome);
      setDescricao(obra.descricao ?? "");
      setCliente(obra.cliente ?? "");
      setEndereco(obra.endereco);
      setResponsavel(obra.responsavel);
      setOrcamentoPrevisto(String(obra.orcamentoPrevisto));
      setDataInicio(obra.dataInicio);
      setPrevisaoFim(obra.previsaoFim);
      setDataConclusao(obra.dataConclusao ?? "");
      setStatus(obra.status);
      setProgressoPercentual(String(obra.progressoPercentual));
    } else {
      const start = todayIso();
      setNome("");
      setDescricao("");
      setCliente("");
      setEndereco("");
      setResponsavel("");
      setOrcamentoPrevisto("");
      setDataInicio(start);
      setPrevisaoFim(start);
      setDataConclusao("");
      setStatus("PLANEJAMENTO");
      setProgressoPercentual("0");
    }
  }, [open, obra]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setFieldErrors({});

    const payload: ObraRequest = {
      nome,
      descricao: descricao || undefined,
      cliente: cliente || undefined,
      endereco,
      responsavel,
      orcamentoPrevisto: Number(orcamentoPrevisto || 0),
      dataInicio,
      previsaoFim,
      dataConclusao: dataConclusao || null,
      status,
      progressoPercentual: Number(progressoPercentual || 0),
    };

    try {
      if (obra) {
        await api.obras.atualizar(obra.id, payload);
      } else {
        await api.obras.criar(payload);
      }
      onSaved();
      onClose();
    } catch (err) {
      if (err instanceof ApiClientError) {
        setError(err.message);
        setFieldErrors(err.fieldErrors);
      } else {
        setError("Não foi possível salvar a obra. Verifique se a API está ativa.");
      }
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal
      open={open}
      title={obra ? `Editar Obra: ${obra.nome}` : "Cadastrar Nova Obra"}
      onClose={onClose}
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <ErrorBanner message={error} />}

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div className="sm:col-span-2">
            <label className="label-field">Nome da Obra *</label>
            <input
              type="text"
              required
              maxLength={150}
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              placeholder="Ex.: Residencial Parque das Flores"
              className="input-field"
            />
            {fieldErrors.nome && (
              <p className="mt-1 text-xs text-rose-600">{fieldErrors.nome}</p>
            )}
          </div>

          <div>
            <label className="label-field">Cliente / Contratante</label>
            <input
              type="text"
              maxLength={150}
              value={cliente}
              onChange={(e) => setCliente(e.target.value)}
              placeholder="Ex.: Construtora Horizonte Ltda"
              className="input-field"
            />
          </div>

          <div>
            <label className="label-field">Responsável Técnico *</label>
            <input
              type="text"
              required
              maxLength={100}
              value={responsavel}
              onChange={(e) => setResponsavel(e.target.value)}
              placeholder="Ex.: Eng. Mariana Oliveira"
              className="input-field"
            />
            {fieldErrors.responsavel && (
              <p className="mt-1 text-xs text-rose-600">{fieldErrors.responsavel}</p>
            )}
          </div>

          <div className="sm:col-span-2">
            <label className="label-field">Endereço *</label>
            <input
              type="text"
              required
              maxLength={255}
              value={endereco}
              onChange={(e) => setEndereco(e.target.value)}
              placeholder="Rua, número, bairro e cidade"
              className="input-field"
            />
            {fieldErrors.endereco && (
              <p className="mt-1 text-xs text-rose-600">{fieldErrors.endereco}</p>
            )}
          </div>

          <div>
            <label className="label-field">Orçamento Previsto (R$) *</label>
            <input
              type="number"
              required
              min="0"
              step="0.01"
              value={orcamentoPrevisto}
              onChange={(e) => setOrcamentoPrevisto(e.target.value)}
              placeholder="0.00"
              className="input-field"
            />
            {fieldErrors.orcamentoPrevisto && (
              <p className="mt-1 text-xs text-rose-600">
                {fieldErrors.orcamentoPrevisto}
              </p>
            )}
          </div>

          <div>
            <label className="label-field">Status</label>
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value as StatusObra)}
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
            <label className="label-field">Data de Início *</label>
            <input
              type="date"
              required
              value={dataInicio}
              onChange={(e) => setDataInicio(e.target.value)}
              className="input-field"
            />
          </div>

          <div>
            <label className="label-field">Previsão de Término *</label>
            <input
              type="date"
              required
              value={previsaoFim}
              onChange={(e) => setPrevisaoFim(e.target.value)}
              className="input-field"
            />
          </div>

          <div>
            <label className="label-field">Progresso Físico (%)</label>
            <input
              type="number"
              min="0"
              max="100"
              value={progressoPercentual}
              onChange={(e) => setProgressoPercentual(e.target.value)}
              className="input-field"
            />
          </div>

          <div>
            <label className="label-field">Data de Conclusão (opcional)</label>
            <input
              type="date"
              value={dataConclusao}
              onChange={(e) => setDataConclusao(e.target.value)}
              className="input-field"
            />
          </div>

          <div className="sm:col-span-2">
            <label className="label-field">Descrição</label>
            <textarea
              rows={3}
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              placeholder="Escopo, observações gerais e etapas principais..."
              className="input-field"
            />
          </div>
        </div>

        <div className="flex justify-end gap-3 border-t border-slate-200 pt-4">
          <button type="button" onClick={onClose} className="btn-secondary">
            Cancelar
          </button>
          <button type="submit" disabled={saving} className="btn-primary">
            {saving ? "Salvando..." : obra ? "Salvar Alterações" : "Cadastrar Obra"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
