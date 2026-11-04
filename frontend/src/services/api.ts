const API_BASE = "/api";

export async function fetchObras() {
  const res = await fetch(`${API_BASE}/obras`);
  if (!res.ok) throw new Error("Erro ao buscar obras");
  return res.json();
}

export async function fetchObraById(id: number) {
  const res = await fetch(`${API_BASE}/obras/${id}`);
  if (!res.ok) throw new Error("Obra não encontrada");
  return res.json();
}

export async function createObra(data: unknown) {
  const res = await fetch(`${API_BASE}/obras`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error("Erro ao criar obra");
  return res.json();
}

export async function updateObra(id: number, data: unknown) {
  const res = await fetch(`${API_BASE}/obras/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error("Erro ao atualizar obra");
  return res.json();
}

export async function deleteObra(id: number) {
  const res = await fetch(`${API_BASE}/obras/${id}`, { method: "DELETE" });
  if (!res.ok) throw new Error("Erro ao deletar obra");
}

export async function fetchFinanceiro(obraId: number) {
  const res = await fetch(`${API_BASE}/obras/${obraId}/financeiro`);
  if (!res.ok) throw new Error("Erro ao buscar lançamentos");
  return res.json();
}

export async function createFinanceiro(obraId: number, data: unknown) {
  const res = await fetch(`${API_BASE}/obras/${obraId}/financeiro`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error("Erro ao criar lançamento");
  return res.json();
}
