# 📋 Levantamento de Requisitos — Gestão de Obras

Este documento especifica os requisitos funcionais, não funcionais e regras de negócio implementados na API RESTful do sistema **Gestão de Obras**.

---

## 1. Requisitos Funcionais (RF)

| Código | Módulo | Descrição |
|---|---|---|
| **RF01** | Obras | Permitir o cadastro de novas obras informando nome, descrição, cliente, endereço, responsável técnico, orçamento previsto, datas de início/previsão de término, status e progresso percentual. |
| **RF02** | Obras | Permitir a listagem paginada e ordenável de obras, com filtros opcionais por `status` e busca textual (`nome`, `cliente` ou `responsavel`). |
| **RF03** | Obras | Permitir a consulta detalhada de uma obra, apresentando dados cadastrais, indicador de atraso, dias restantes até a previsão de término e resumo financeiro completo. |
| **RF04** | Obras | Permitir a atualização completa dos dados cadastrais de uma obra (`PUT /api/obras/{id}`). |
| **RF05** | Obras | Permitir a atualização rápida de status, progresso percentual e data de conclusão da obra (`PATCH /api/obras/{id}/status`). |
| **RF06** | Obras | Permitir a exclusão de obras, removendo automaticamente seus lançamentos financeiros associados. |
| **RF07** | Centros de Custo | Disponibilizar carga inicial (seed via Flyway) com os centros de custo padrão da construção civil. |
| **RF08** | Centros de Custo | Permitir o cadastro, listagem (todos ou apenas ativos), atualização e exclusão de centros de custo. |
| **RF09** | Financeiro | Permitir o registro de lançamentos financeiros (`RECEITA` ou `DESPESA`) vinculados a uma obra e opcionalmente a um centro de custo, com controle de valor, competência, pagamento, status, comprovante e observações. |
| **RF10** | Financeiro | Permitir a listagem paginada de lançamentos com filtros combináveis por obra, centro de custo, tipo, status de pagamento e intervalo de datas de competência. |
| **RF11** | Financeiro | Permitir a listagem direta dos lançamentos de uma obra específica (`GET /api/obras/{id}/lancamentos`). |
| **RF12** | Financeiro | Permitir a atualização completa de um lançamento e a atualização rápida de pagamento (`PATCH /api/lancamentos/{id}/pagamento`). |
| **RF13** | Financeiro | Permitir a exclusão de lançamentos financeiros. |
| **RF14** | Indicadores | Fornecer o resumo financeiro individual de cada obra, contendo receitas pagas/pendentes, despesas pagas/pendentes, saldo realizado, saldo projetado, orçamento disponível, percentual utilizado e gastos por centro de custo. |
| **RF15** | Dashboard | Fornecer endpoint consolidado de dashboard (`/api/dashboard/resumo`) com total de obras por status, progresso médio, orçamento total, saldos gerais, distribuição de despesas por centro de custo e alertas de obras atrasadas ou com orçamento excedido. |

---

## 2. Regras de Negócio (RN)

| Código | Regra |
|---|---|
| **RN01** | A data de previsão de término (`previsaoFim`) da obra não pode ser anterior à data de início (`dataInicio`). |
| **RN02** | A data de conclusão (`dataConclusao`), quando informada, não pode ser anterior à data de início (`dataInicio`). |
| **RN03** | Ao marcar uma obra como `CONCLUIDA`, caso a `dataConclusao` não tenha sido informada ela é preenchida automaticamente com a data atual (ou `dataInicio` se esta for futura) e o `progressoPercentual` é ajustado para `100%`. |
| **RN04** | Uma obra é considerada **atrasada** quando seu status é diferente de `CONCLUIDA` e `CANCELADA` e sua `previsaoFim` é anterior à data atual. |
| **RN05** | O nome de cada centro de custo deve ser único (validação case-insensitive retornando HTTP `409 Conflict`). |
| **RN06** | Não é permitido criar novos lançamentos vinculados a um centro de custo inativo (`ativo = false`). Ao excluir um centro de custo, o vínculo nos lançamentos existentes é definido como `NULL` para preservar o histórico financeiro. |
| **RN07** | Lançamentos com `statusPagamento = PAGO` sem `dataPagamento` informada recebem automaticamente a data atual. Lançamentos `PENDENTE` ou `CANCELADO` têm `dataPagamento` limpa (`null`). |
| **RN08** | Lançamentos com `statusPagamento = CANCELADO` são desconsiderados nos cálculos de receitas, despesas, saldos e consumo de orçamento. |

---

## 3. Requisitos Não Funcionais (RNF)

| Código | Categoria | Descrição |
|---|---|---|
| **RNF01** | Arquitetura | API RESTful estruturada em camadas (Controller, Service, Repository, Domain, DTO, Mapper, Exception). |
| **RNF02** | Persistência | Banco de dados relacional PostgreSQL com versionamento de schema gerenciado exclusivamente via Flyway (`ddl-auto: validate`). |
| **RNF03** | Validação | Validação rigorosa de entrada via Bean Validation (Jakarta Validation) e respostas de erro padronizadas (`ApiErrorResponse`). |
| **RNF04** | Documentação | Especificação OpenAPI 3.0 e interface interativa Swagger UI em `/api/swagger-ui.html`. |
| **RNF05** | Integração | Suporte a CORS configurável via variável de ambiente (`CORS_ALLOWED_ORIGINS`) para integração direta com frontend Next.js / Vercel. |
| **RNF06** | Portabilidade | Empacotamento com Docker (multi-stage build) e orquestração local via `docker-compose.yml` com PostgreSQL 16 e healthchecks. |
| **RNF07** | Qualidade | Suíte de testes automatizados de integração executáveis sem dependências externas (H2 em modo PostgreSQL com execução real das migrations Flyway). |
| **RNF08** | Segurança de Config | Separação de perfis (`dev`, `prod`, `test`) garantindo que credenciais reais sejam fornecidas apenas por variáveis de ambiente. |
