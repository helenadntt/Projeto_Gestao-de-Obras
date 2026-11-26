# 🏗️ Gestão de Obras

Sistema web Full Stack para gerenciamento operacional e controle financeiro de obras de construção civil, desenvolvido como projeto extensionista com foco em Engenharia de Software, APIs RESTful, modelagem relacional e integração entre frontend e backend.

---

### 🛠️ Tecnologias & Ferramentas

**Frontend**  
![Next.js](https://img.shields.io/badge/Next.js_15-000000?style=for-the-badge&logo=nextdotjs&logoColor=white)
![React](https://img.shields.io/badge/React_19-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)

**Backend**  
![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0202?style=for-the-badge&logo=flyway&logoColor=white)
![Swagger](https://img.shields.io/badge/OpenAPI_Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![Maven](https://img.shields.io/badge/Apache_Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

**Banco de Dados & DevOps**  
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker_Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)

---

## 📌 Sobre o Projeto

O **Gestão de Obras** centraliza informações essenciais no acompanhamento de projetos de construção civil, fornecendo controle de prazos, progresso físico e organização financeira estruturada por centros de custo.

Principais pilares arquiteturais aplicados:
- Separação clara de responsabilidades no backend (`controller`, `service`, `repository`, `domain`, `dto`, `mapper`, `exception`, `config`);
- API RESTful paginada com filtros dinâmicos via JPA Specifications;
- Interface web responsiva em Next.js App Router + TypeScript + Tailwind CSS integrada à API;
- Persistência relacional com integridade garantida no PostgreSQL e versionamento contínuo via **Flyway** (`ddl-auto=validate`);
- Documentação viva via **Swagger / OpenAPI 3.0**, testes de integração automatizados e CI via GitHub Actions.

---

## 🧩 Funcionalidades

- [x] **Gestão de Obras:** Cadastro, listagem paginada com busca/filtros, edição completa, atualização rápida de status/progresso e exclusão (CRUD completo).
- [x] **Visão Detalhada da Obra:** Dados cadastrais, cálculo de dias restantes/atraso, controle de andamento, resumo financeiro consolidado e extrato da obra.
- [x] **Centros de Custo:** Seed inicial automático e CRUD completo para categorização de despesas e receitas.
- [x] **Gestão Financeira:** Registro de receitas e despesas com controle de competência, pagamento, status (`PENDENTE`, `PAGO`, `CANCELADO`), baixa rápida e filtros combináveis.
- [x] **Indicadores & Dashboard:** Totalizadores de obras por status, progresso médio, receitas/despesas realizadas e pendentes, saldos, gastos por centro de custo e alertas de atraso/orçamento excedido.
- [x] **Validação & Tratamento de Exceções:** Bean Validation no backend, contrato padronizado de erros (`GlobalExceptionHandler`) e feedback visual por campo no frontend.
- [x] **Containers, Seed Demo & CI:** Docker Compose (PostgreSQL + Backend + Frontend), perfil opcional `demo` com dados de exemplo e pipeline GitHub Actions.
- [ ] **Deploy Contínuo em Nuvem:** Publicação em produção (Vercel + hospedagem backend/PostgreSQL).

---

## 🏛️ Estrutura do Repositório

```text
Projeto_Gestao-de-Obras/
├── backend/                     # API RESTful em Spring Boot 3.4 (Java 21)
│   ├── src/main/java/br/com/gestaoobras/api/
│   │   ├── config/              # CORS, OpenAPI/Swagger e DemoDataSeeder (@Profile("demo"))
│   │   ├── controller/          # Endpoints REST (Obras, Centros de Custo, Lançamentos, Dashboard)
│   │   ├── domain/              # Entidades JPA e Enums
│   │   ├── dto/                 # Records de entrada/saída com Bean Validation
│   │   ├── exception/           # Exceções de negócio e GlobalExceptionHandler
│   │   ├── mapper/              # Conversores Entity <-> DTO
│   │   ├── repository/          # Repositórios Spring Data JPA e queries agregadas
│   │   └── service/             # Regras de negócio e cálculos financeiros
│   ├── src/main/resources/
│   │   ├── db/migration/        # Migrations Flyway (V1__create_initial_schema.sql)
│   │   └── application*.yml     # Perfis base, dev e prod
│   ├── src/test/                # Testes automatizados de integração (MockMvc + H2/Flyway)
│   ├── Dockerfile               # Build multi-stage JDK 21 -> JRE 21 Alpine
│   └── pom.xml
├── frontend/                    # Aplicação Web em Next.js 15 + TypeScript + Tailwind CSS
│   ├── src/
│   │   ├── app/                 # Rotas: /, /obras, /obras/[id], /financeiro, /centros-custo
│   │   ├── components/          # Layout (AppShell), modais de formulário e componentes UI
│   │   ├── lib/                 # Formatadores BRL, datas e rótulos de status
│   │   ├── services/            # Cliente HTTP tipado (api.ts)
│   │   └── types/               # Contratos TypeScript espelhados da API
│   ├── Dockerfile               # Build standalone Next.js
│   └── package.json
├── docs/                        # Documentação técnica do projeto
│   ├── REQUISITOS.md            # Requisitos funcionais, não funcionais e regras de negócio
│   ├── DER.md                   # Diagrama de Entidade-Relacionamento e fluxo Flyway
│   └── API.md                   # Referência rápida dos endpoints REST
├── .github/workflows/ci.yml     # Pipeline CI (testes backend + lint/build frontend)
├── docker-compose.yml           # Orquestração PostgreSQL 16 + Backend + Frontend
├── .env.example                 # Modelo de variáveis de ambiente
└── README.md
```

---

## 🚀 Como Executar o Projeto

### Opção 1: Com Docker Compose via WSL (Recomendado — sobe tudo em 1 comando)

Pré-requisito: **WSL 2** (Ubuntu ou outra distro Linux) com **Docker** e **Docker Compose** instalados (seja Docker Engine nativo no WSL ou integração WSL 2 ativada no Docker Desktop).

```bash
# 1. Abra o terminal do WSL e verifique/inicie o serviço do Docker (caso use Docker Engine nativo no WSL)
sudo service docker start

# 2. Acesse a pasta do projeto (se estiver no disco C: do Windows, ou use o caminho onde clonou no WSL)
cd /mnt/c/Users/SEU_USUARIO/Documents/ProjetoEXT/Projeto_Gestao-de-Obras

# 3. (Opcional) Copie o arquivo de variáveis de ambiente
cp .env.example .env

# 4. Suba PostgreSQL + Backend + Frontend
docker compose up --build
```

> **Dicas para WSL 2:**
> - Para melhor desempenho de build/I/O, recomenda-se clonar o repositório dentro do filesystem nativo do Linux no WSL (ex.: `~/projetos/Projeto_Gestao-de-Obras`) em vez de `/mnt/c/...`.
> - Caso apareça erro de permissão no socket do Docker (`/var/run/docker.sock`), execute `sudo usermod -aG docker $USER` e reabra o terminal do WSL (ou use `sudo docker compose up --build`).
> - Para encerrar os containers: `docker compose down` (ou `docker compose down -v` para limpar também o volume do banco).
> - Por padrão no Docker Compose o perfil `dev,demo` vem ativo, populando automaticamente **4 obras e 12 lançamentos de demonstração** caso o banco esteja vazio. Para iniciar com o banco limpo (apenas os 6 centros de custo padrão), defina `SPRING_PROFILES_ACTIVE=dev` no `.env`.

Endereços após subir (acessíveis normalmente pelo navegador do Windows via `localhost`):
- **Frontend Web:** `http://localhost:3000`
- **API Base URL:** `http://localhost:8080/api`
- **Swagger UI:** `http://localhost:8080/api/swagger-ui.html`
- **Healthcheck:** `http://localhost:8080/api/actuator/health`

---

### Opção 2: Executando Localmente em Desenvolvimento

#### 1. Banco de dados (PostgreSQL)
Suba apenas o PostgreSQL via Docker (`docker compose up -d postgres`) ou crie a base `gestao_obras` no seu PostgreSQL local.

#### 2. Backend
```bash
cd backend

# Windows (com dados de demonstração)
$env:SPRING_PROFILES_ACTIVE="dev,demo"; .\mvnw.cmd spring-boot:run

# Linux / macOS
SPRING_PROFILES_ACTIVE=dev,demo ./mvnw spring-boot:run
```

#### 3. Frontend
Em outro terminal:
```bash
cd frontend
npm install
npm run dev
```

Acesse `http://localhost:3000`.

---

### 🧪 Testes e Validação

**Backend** (usa H2 em modo PostgreSQL com Flyway real — não precisa de Docker/Postgres ativo):
```bash
cd backend
.\mvnw.cmd test   # Windows
./mvnw test       # Linux / macOS
```

**Frontend** (lint + typecheck + build de produção):
```bash
cd frontend
npm run lint
npm run build
```

---

## 📚 Documentação Complementar

- [x] [Levantamento de Requisitos e Regras de Negócio](docs/REQUISITOS.md)
- [x] [Diagrama de Entidade-Relacionamento (DER) e Fluxo Flyway](docs/DER.md)
- [x] [Referência Rápida de Endpoints da API](docs/API.md)
- [x] Documentação interativa **Swagger / OpenAPI** disponível em `/api/swagger-ui.html`

---

## 👥 Equipe

| Papel | Nome |
|---|---|
| **Discente** | Helena Trombetta |
| **Discente** | Vitor Ferraz |
| **Docente Orientador** | Antônio Junio |
| **Docente Orientador** | Jonas Bernardino |

**Contexto:** Projeto Extensionista + Desenvolvimento Full Stack.
