# 🌐 Referência Rápida da API REST

- **Base URL local:** `http://localhost:8080/api`
- **Swagger UI (Interativo):** `http://localhost:8080/api/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/api/v3/api-docs`
- **Healthcheck:** `http://localhost:8080/api/actuator/health`

---

## 1. Obras (`/api/obras`)

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/api/obras` | Lista obras paginadas (`status`, `busca`, `page`, `size`, `sort`) | `200 OK` |
| `GET` | `/api/obras/{id}` | Retorna detalhes da obra + dias restantes + resumo financeiro | `200 OK` |
| `GET` | `/api/obras/{id}/resumo-financeiro` | Retorna totalizadores financeiros e gastos por centro de custo da obra | `200 OK` |
| `GET` | `/api/obras/{id}/lancamentos` | Lista lançamentos financeiros vinculados à obra (`tipo`, `statusPagamento`) | `200 OK` |
| `POST` | `/api/obras` | Cadastra nova obra | `201 Created` |
| `PUT` | `/api/obras/{id}` | Atualiza todos os dados da obra | `200 OK` |
| `PATCH` | `/api/obras/{id}/status` | Atualiza status, progresso percentual e data de conclusão | `200 OK` |
| `DELETE` | `/api/obras/{id}` | Exclui a obra e seus lançamentos em cascata | `204 No Content` |

### Exemplo — `POST /api/obras`
```json
{
  "nome": "Residencial Parque das Flores",
  "descricao": "Edifício residencial com 12 unidades",
  "cliente": "Construtora Horizonte Ltda",
  "endereco": "Av. Paulista, 1200 - São Paulo/SP",
  "responsavel": "Eng. Carlos Silva",
  "orcamentoPrevisto": 450000.00,
  "dataInicio": "2026-01-15",
  "previsaoFim": "2026-12-20",
  "status": "EM_ANDAMENTO",
  "progressoPercentual": 25
}
```

---

## 2. Centros de Custo (`/api/centros-custo`)

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/api/centros-custo` | Lista centros de custo (`apenasAtivos=true\|false`) | `200 OK` |
| `GET` | `/api/centros-custo/{id}` | Busca centro de custo por ID | `200 OK` |
| `POST` | `/api/centros-custo` | Cria novo centro de custo | `201 Created` |
| `PUT` | `/api/centros-custo/{id}` | Atualiza nome, descrição ou status ativo | `200 OK` |
| `DELETE` | `/api/centros-custo/{id}` | Exclui centro de custo (preserva lançamentos) | `204 No Content` |

---

## 3. Lançamentos Financeiros (`/api/lancamentos`)

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/api/lancamentos` | Lista paginada (`obraId`, `centroCustoId`, `tipo`, `statusPagamento`, `dataInicio`, `dataFim`) | `200 OK` |
| `GET` | `/api/lancamentos/{id}` | Busca lançamento por ID | `200 OK` |
| `POST` | `/api/lancamentos` | Registra nova receita ou despesa | `201 Created` |
| `PUT` | `/api/lancamentos/{id}` | Atualiza lançamento completo | `200 OK` |
| `PATCH` | `/api/lancamentos/{id}/pagamento` | Atualiza status (`PENDENTE`, `PAGO`, `CANCELADO`) e data de pagamento | `200 OK` |
| `DELETE` | `/api/lancamentos/{id}` | Exclui lançamento | `204 No Content` |

### Exemplo — `POST /api/lancamentos`
```json
{
  "obraId": 1,
  "centroCustoId": 1,
  "descricao": "Compra de 50 sacos de cimento CP-II",
  "tipo": "DESPESA",
  "valor": 2450.50,
  "dataCompetencia": "2026-03-10",
  "dataPagamento": "2026-03-12",
  "statusPagamento": "PAGO",
  "comprovanteUrl": "https://storage.exemplo.com/nf-1042.pdf",
  "observacoes": "Fornecedor ConstruMais"
}
```

---

## 4. Dashboard (`/api/dashboard`)

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/api/dashboard/resumo` | Indicadores consolidados: obras por status, progresso médio, receitas, despesas, saldos, gastos por centro de custo e alertas | `200 OK` |

---

## 5. Padrão de Erros

Todas as respostas de erro (`400`, `404`, `409`, `422`, `500`) seguem o contrato `ApiErrorResponse`:

```json
{
  "timestamp": "2026-11-25T23:55:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Erro de validação nos dados enviados.",
  "path": "/api/obras",
  "fieldErrors": [
    {
      "field": "nome",
      "message": "O nome da obra é obrigatório."
    }
  ]
}
```
