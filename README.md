# 🏗️ Gestão de Obras

Sistema web para gerenciamento de obras, desenvolvido como projeto extensionista com foco na aplicação prática de desenvolvimento Full Stack, modelagem de banco de dados e integração entre frontend e backend.

## 📌 Sobre o projeto

O **Gestão de Obras** tem como objetivo centralizar informações relacionadas ao acompanhamento de obras, oferecendo uma base para organização de dados e controle financeiro.

O projeto está sendo desenvolvido no contexto acadêmico, aplicando conceitos de Engenharia de Software, desenvolvimento web, persistência de dados e APIs REST.

## 🎯 Objetivos

- Centralizar informações das obras.
- Facilitar o acompanhamento e a organização dos projetos.
- Estruturar o controle de receitas e despesas.
- Aplicar boas práticas de desenvolvimento Full Stack.
- Integrar uma interface web, uma API REST e um banco de dados relacional.

## 🛠️ Tecnologias utilizadas

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
- Git e GitHub.
- Vercel — deploy do frontend.

## 🧩 Funcionalidades

As funcionalidades abaixo representam o escopo planejado e devem ser atualizadas conforme a implementação evoluir.

- [ ] Cadastro, consulta, edição e exclusão de obras.
- [ ] Visualização de detalhes das obras.
- [ ] Registro e consulta de receitas e despesas.
- [ ] Cálculo de indicadores financeiros.
- [ ] Dashboard de acompanhamento.
- [ ] Validação de dados e tratamento de erros.
- [ ] Integração entre frontend, backend e banco de dados.
- [ ] Publicação da aplicação em ambiente de produção.

## 🏛️ Arquitetura

A aplicação utiliza uma arquitetura cliente-servidor:

1. **Frontend:** interface desenvolvida com Next.js.
2. **Backend:** API REST desenvolvida com Spring Boot.
3. **Banco de dados:** PostgreSQL para persistência das informações.
4. **Migrations:** Flyway para versionamento das alterações no esquema do banco.

O frontend consome os endpoints da API, enquanto o backend concentra as regras de negócio e o acesso aos dados.

## 🚀 Como executar o projeto

### Pré-requisitos

- Node.js e npm.
- Java em versão compatível com a configuração do projeto.
- Maven, ou Maven Wrapper caso esteja disponível no repositório.
- PostgreSQL.
- Git.

### 1. Clonar o repositório

```bash
git clone https://github.com/helenadntt/Projeto_Gestao-de-Obras.git
cd Projeto_Gestao-de-Obras
```

> Se o nome ou a URL do repositório for diferente, ajuste o comando conforme o endereço real do GitHub.

### 2. Configurar o banco de dados

Crie um banco de dados PostgreSQL para a aplicação e configure as credenciais por meio das variáveis de ambiente ou do arquivo de configuração utilizado pelo projeto.

Exemplo de variáveis:

```env
DB_URL=jdbc:postgresql://localhost:5432/gestao_obras
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha
```

Os nomes das variáveis devem corresponder à configuração efetiva do Spring Boot.

### 3. Executar o backend

Entre na pasta do backend e execute:

```bash
./mvnw spring-boot:run
```

No Windows, caso o Maven Wrapper esteja disponível:

```powershell
.\mvnw.cmd spring-boot:run
```

Se o projeto não possuir Maven Wrapper, utilize o Maven instalado e configurado no ambiente.

### 4. Executar o frontend

Entre na pasta do frontend e instale as dependências:

```bash
npm install
npm run dev
```

Acesse o endereço local indicado pelo Next.js no terminal.

> A estrutura de pastas, as variáveis de ambiente e os comandos devem ser ajustados à organização real do repositório.

## 👥 Equipe

**Discentes**
- Helena Trombetta
- Vitor Ferraz

**Docentes**
- Antônio Junio
- Jonas Bernardino

**Disciplina/Contexto:** Projeto Extensionista + Backend.

## 🔗 Links

- **Repositório:** [Projeto Gestão de Obras no GitHub](https://github.com/helenadntt/Projeto_Gestao-de-Obras)
- **Deploy:** a URL de produção será adicionada após a publicação e validação da aplicação.

## 📚 Documentação

A documentação complementar poderá incluir:

- Requisitos funcionais e não funcionais.
- Diagramas de casos de uso e arquitetura.
- Modelagem conceitual, lógica e física do banco de dados.
- Documentação dos endpoints da API.
- Evidências de testes.
- Instruções de implantação.

## 📈 Status do projeto

**Em desenvolvimento.**

Este projeto tem caráter acadêmico e extensionista, com foco no aprendizado prático, na colaboração e na construção incremental de uma aplicação Full Stack.

---

Desenvolvido por **Helena Trombetta e Vitor Ferraz**.
