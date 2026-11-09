# 🏗️ Gestão de Obras

> Sistema web para gerenciamento de obras, desenvolvido como projeto extensionista com foco na aplicação prática de desenvolvimento Full Stack, modelagem de banco de dados e integração entre frontend e backend.

<div align="center">

![Next.js](https://img.shields.io/badge/Next.js-black?style=for-the-badge&logo=next.js&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white)
![Vercel](https://img.shields.io/badge/Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white)

</div>

---

## 📌 Sobre o Projeto

O **Gestão de Obras** tem como objetivo centralizar informações essenciais no acompanhamento de canteiros de obras e projetos de construção civil, oferecendo uma base robusta para organização de dados operacionais e controle orçamentário-financeiro.

O projeto está sendo concebido no contexto acadêmico, exercitando na prática conceitos de Engenharia de Software, arquitetura de sistemas distribuídos, boas práticas de APIs RESTful e persistência relacional de alta confiabilidade.

---

## 📸 Demonstração

<!-- Dica: adicione imagens ou um gif animado da aplicação na pasta /assets do repositório -->
> 🖼️ *Pré-visualizações da interface web e do dashboard estarão disponíveis conforme a entrega das interfaces for finalizada.*

| Dashboard de Acompanhamento | Gestão de Despesas & Obras |
| :---: | :---: |
| *(Imagem da tela / Mockup)* | *(Imagem da tela / Mockup)* |

---

## 🎯 Objetivos

- **Centralização:** Consolidar dados de diferentes obras em uma interface única e acessível.
- **Acompanhamento dinâmico:** Facilitar o rastreio do cronograma e status de cada projeto.
- **Controle orçamentário:** Estruturar o fluxo de receitas, custos e previsões de despesas.
- **Excelência técnica:** Aplicar padrões de projeto (Clean Code, camadas bem definidas) no ecossistema Full Stack.
- **Interoperabilidade:** Estabelecer uma comunicação fluida entre interface web (Next.js), API REST (Spring Boot) e persistência (PostgreSQL).

---

## 🛠️ Tecnologias Utilizadas

### Frontend
- **[Next.js](https://nextjs.org/)** — Framework React para renderização ágil e roteamento moderno.
- **HTML5 & CSS3 / Tailwind CSS** — Estrutura e estilização das interfaces.

### Backend
- **Java** — Linguagem principal do ecossistema backend.
- **[Spring Boot](https://spring.io/projects/spring-boot)** — Fundação para a API REST modular e performática.
- **Spring Web** — Exposição dos controllers e endpoints HTTP.
- **Spring Data JPA** — Abstração de persistência orientada a repositórios.
- **Lombok** — Minimização de código boilerplate (getters, setters, construtores).
- **Bean Validation (Hibernate Validator)** — Validação declarativa dos payloads da API.
- **Flyway** — Controle de versão e migração programática do banco de dados.
- **Apache Maven** — Automação de compilação e gerenciamento de dependências.

### Banco de Dados & Infraestrutura
- **[PostgreSQL](https://www.postgresql.org/)** — Banco de dados relacional robusto para produção e desenvolvimento.
- **Git & GitHub** — Controle de versão distribuído e colaboração em equipe.
- **[Vercel](https://vercel.com/)** — Plataforma de deploy contínuo para o frontend.

---

## 🧩 Funcionalidades

As funcionalidades abaixo representam o escopo planejado (MVP e incrementos futuros):

- [x] Arquitetura base e modelagem relacional.
- [ ] Cadastro, consulta, edição e exclusão (CRUD) de obras.
- [ ] Visualização detalhada de fichas técnicas de cada obra.
- [ ] Registro e categorização de despesas e receitas.
- [ ] Indicadores de custos consolidados vs. orçado.
- [ ] Dashboard analítico com métricas operacionais.
- [ ] Validações de payload e tratamento centralizado de exceções (`@RestControllerAdvice`).
- [ ] Deploy contínuo (CI/CD) em ambiente de homologação e produção.

---

## 🏛️ Arquitetura

O sistema adota uma arquitetura em camadas desacopladas (Cliente-Servidor):

```text
┌────────────────────────────────┐
│      Next.js (Frontend)        │  Interface Web (Vercel)
└───────────────┬────────────────┘
                │ Requisições HTTP / JSON
┌───────────────▼────────────────┐
│   Spring Boot (Backend API)    │  Controllers -> Services -> Repositories
└───────────────┬────────────────┘
                │ JPA / JDBC & Migrations (Flyway)
┌───────────────▼────────────────┐
│      PostgreSQL (Database)     │  Armazenamento relacional
└────────────────────────────────┘
```

1. **Frontend:** Renderiza as views, lida com o estado local e consome a API REST.
2. **Backend:** Concentra regras de negócio, validações de domínio e segurança.
3. **Persistência:** O Flyway executa migrações estruturais automatizadas direto no PostgreSQL.

---

## 🚀 Como Executar o Projeto Localmente

### Pré-requisitos
Certifique-se de ter instalado em sua máquina:
- **Node.js** (versão 18+ recomendada) e **npm** / **yarn** / **pnpm**
- **Java JDK** (versão 17 ou 21, conforme configurado no `pom.xml`)
- **Maven** (opcional se utilizar o wrapper `./mvnw`)
- **PostgreSQL** ativo localmente ou via container Docker
- **Git**

---

### 1. Clonar o Repositório

```bash
git clone https://github.com/helenadntt/Projeto_Gestao-de-Obras.git
cd Projeto_Gestao-de-Obras
```

---

### 2. Configurar o Banco de Dados

Crie uma base de dados no PostgreSQL (por exemplo, `gestao_obras`). Em seguida, configure as credenciais no arquivo `backend/src/main/resources/application.properties` (ou através de variáveis de ambiente):

```env
DB_URL=jdbc:postgresql://localhost:5432/gestao_obras
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha
```

---

### 3. Executar o Backend

Navegue até a pasta do serviço backend e inicie o Spring Boot:

**Linux / macOS:**
```bash
cd backend
./mvnw spring-boot:run
```

**Windows (PowerShell / Prompt):**
```cmd
cd backend
.\mvnw.cmd spring-boot:run
```

> A API inicializará por padrão na porta `8080` (ex: `http://localhost:8080`).

---

### 4. Executar o Frontend

Em um novo terminal, entre no diretório do frontend, instale os pacotes e inicie o servidor de desenvolvimento:

```bash
cd frontend
npm install
npm run dev
```

> A interface estará acessível via navegador no endereço: `http://localhost:3000`.

---

## 👥 Equipe do Projeto

### 🎓 Discentes (Desenvolvedores)
- **Helena Trombetta** — [GitHub](https://github.com/helenadntt)
- **Vitor Ferraz** — [GitHub](https://github.com/vitorferraz) <!-- Atualize com o link real se houver -->

### 👨‍🏫 Docentes e Orientadores
- **Prof. Antônio Junio**
- **Prof. Jonas Bernardino**

**Contexto Acadêmico:** Projeto Extensionista Interdisciplinar / Desenvolvimento Backend & Full Stack.

---

## 📚 Documentação Complementar

Recursos adicionais previstos para o ciclo de desenvolvimento:
- [ ] Matriz de Requisitos Funcionais (RF) e Não Funcionais (RNF).
- [ ] Diagrama de Entidade-Relacionamento (DER).
- [ ] Especificação OpenAPI / Swagger para os endpoints REST.
- [ ] Relatório de evidências de testes de carga e unitários.

---

## 📈 Status do Projeto

![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-yellow?style=flat-square)

Projeto em constante evolução técnica e funcional, focado na resolução prática de problemas reais de gestão e na capacitação da equipe em padrões profissionais de desenvolvimento.