# 🏗️ Gestão de Obras

Sistema web para gerenciamento de obras, desenvolvido como projeto extensionista com foco na aplicação prática de desenvolvimento Full Stack, modelagem de banco de dados e integração entre frontend e backend.

📌 ## Sobre o projeto

O **Gestão de Obras** tem como objetivo centralizar informações relacionadas ao acompanhamento de obras, oferecendo uma base para organização de dados e controle financeiro.

O projeto está sendo desenvolvido no contexto acadêmico, aplicando conceitos de Engenharia de Software, desenvolvimento web, persistência de dados e APIs REST.

🎯 ## Objetivos

- Centralizar informações das obras.
- Facilitar o acompanhamento e a organização dos projetos.
- Estruturar o controle de receitas e despesas.
- Aplicar boas práticas de desenvolvimento Full Stack.
- Integrar uma interface web, uma API REST e um banco de dados relacional.

🛠️ ## Tecnologias utilizadas

### Frontend
- **Next.js** — framework para desenvolvimento da aplicação web.

### Backend
- **Java** — linguagem de programação.
- **Spring Boot** — construção da API REST.
- **Spring Web** — desenvolvimento dos endpoints.
- **Spring Data JPA** — persistência e acesso aos dados.
- **Lombok** — redução de código repetitivo.
- **Bean Validation** — validação de dados.
- **Flyway** — gerenciamento de migrations do banco.
- **Maven** — gerenciamento de dependências e build.

### Banco de dados
- **PostgreSQL** — banco de dados relacional.

### Versionamento e deploy
- **Git e GitHub**
- **Vercel** — deploy do frontend.

🧩 ## Funcionalidades

As funcionalidades abaixo representam o escopo planejado e devem ser atualizadas conforme a implementação evoluir:

- 📋 Cadastro, consulta, edição e exclusão de obras.
- 🔍 Visualização de detalhes das obras.
- 💰 Registro e consulta de receitas e despesas.
- 📊 Cálculo de indicadores financeiros.
- 📈 Dashboard de acompanhamento.
- ✅ Validação de dados e tratamento de erros.
- 🔌 Integração entre frontend, backend e banco de dados.
- 🚀 Publicação da aplicação em ambiente de produção.

🏛️ ## Arquitetura

A aplicação utiliza uma arquitetura cliente-servidor:

1. **Frontend:** interface desenvolvida com Next.js.
2. **Backend:** API REST desenvolvida com Spring Boot.
3. **Banco de dados:** PostgreSQL para persistência das informações.
4. **Migrations:** Flyway para versionamento das alterações no esquema do banco.

O frontend consome os endpoints da API, enquanto o backend concentra as regras de negócio e o acesso aos dados.

🚀 ## Como executar o projeto

### Pré-requisitos
- Node.js e npm.
- Java em versão compatível com a configuração do projeto.
- Maven, ou Maven Wrapper caso esteja disponível no repositório.
- PostgreSQL.
- Git.

### 1. Clonar o repositório
```bash
git clone [https://github.com/helenadntt/Projeto_Gestao-de-Obras.git](https://github.com/helenadntt/Projeto_Gestao-de-Obras.git)
cd Projeto_Gestao-de-Obras
