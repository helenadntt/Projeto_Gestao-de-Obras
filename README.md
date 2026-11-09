# 🏗️ Gestão de Obras

Sistema web para gerenciamento de obras, desenvolvido como projeto extensionista com foco na aplicação prática de desenvolvimento Full Stack, modelagem de banco de dados relacional e integração entre frontend e backend.

---

### 🛠️ Tecnologias & Ferramentas

**Frontend**  
![Next.js](https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=nextdotjs&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Node.js](https://img.shields.io/badge/Node.js-339933?style=for-the-badge&logo=nodedotjs&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)

**Backend**  
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0202?style=for-the-badge&logo=flyway&logoColor=white)
![Maven](https://img.shields.io/badge/Apache_Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

**Banco de Dados**  
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)

**DevOps & Versionamento**  
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
![Vercel](https://img.shields.io/badge/Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white)

---

## 📌 Sobre o Projeto

O **Gestão de Obras** tem como objetivo centralizar informações essenciais no acompanhamento de projetos de construção civil, fornecendo controle operacional e organização financeira estruturada.

O sistema é construído em ambiente acadêmico/extensionista, aplicando conceitos consolidados de Engenharia de Software:
- Separação clara de responsabilidades (SoC);
- Arquitetura baseada em APIs RESTful;
- Persistência e integridade em banco relacional;
- Versionamento contínuo de esquemas com migrations.

---

## 🎯 Objetivos

- **Centralização:** Consolidar os dados operacionais das obras em um único ponto de acesso.
- **Controle Financeiro:** Estruturar lançamentos de receitas, despesas e fluxo de caixa por obra.
- **Produtividade:** Reduzir o atrito na conferência e atualização de status de cada projeto.
- **Prática Profissional:** Aplicar padrões de mercado em desenvolvimento Full Stack integrado.

---

## 🧩 Funcionalidades

- [ ] **Gestão de Obras:** Cadastro, listagem, atualização e exclusão (CRUD completo).
- [ ] **Visão Detalhada:** Acompanhamento pontual de dados cadastrais, prazos e métricas de cada obra.
- [ ] **Gestão Financeira:** Registro de despesas e receitas vinculadas a centros de custos específicos.
- [ ] **Indicadores & Dashboard:** Totalizadores de custos, saldos e progresso das obras.
- [ ] **Validação & Tratamento de Exceções:** Feedback visual padronizado e validação rigorosa de payloads via Bean Validation.
- [ ] **Deploy Contínuo:** Aplicação publicada e acessível em nuvem.

---

## 🏛️ Arquitetura do Sistema

```text
┌─────────────────┐       JSON / HTTP REST       ┌──────────────────────┐
│  Frontend Web   │ ◄──────────────────────────► │   Backend Service    │
│    (Next.js)    │                              │ (Spring Boot / Java) │
└─────────────────┘                              └──────────┬───────────┘
                                                            │
                                                     JDBC / JPA Hibernate
                                                            │
                                                 ┌──────────▼───────────┐
                                                 │   Banco Relacional   │
                                                 │     (PostgreSQL)     │
                                                 │   [Flyway Migrations]│
                                                 └──────────────────────┘
```

1. **Frontend (Next.js):** Renderização, estado visual, roteamento e consumo assíncrono dos endpoints REST.
2. **Backend (Spring Boot):** Camada de controle, regras de negócio em serviços, DTOs de transporte, validações e mapeamento ORM via Spring Data JPA.
3. **Persistência (PostgreSQL + Flyway):** Armazenamento transacional e versionamento incremental de DDL via scripts SQL de migração.

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
Certifique-se de ter instalado em seu ambiente:
- **Node.js** (v18+) e gerenciador de pacotes (`npm`, `yarn` ou `pnpm`)
- **Java JDK** (versão 17 ou 21 recomendada)
- **PostgreSQL** em execução local ou container Docker
- **Git**

---

### 1. Clonar o Repositório

```bash
git clone https://github.com/helenadntt/Projeto_Gestao-de-Obras.git
cd Projeto_Gestao-de-Obras
```

---

### 2. Configurar o Banco de Dados

Crie a base de dados no PostgreSQL:

```sql
CREATE DATABASE gestao_obras;
```

Ajuste as propriedades de conexão no `application.properties` (ou `application.yml`) ou configure via variáveis de ambiente:

```env
DB_URL=jdbc:postgresql://localhost:5432/gestao_obras
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha
```

---

### 3. Executar o Backend

Navegue até o diretório do backend:

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows (CMD / PowerShell)
.\mvnw.cmd spring-boot:run
```

*(Caso não utilize o Maven Wrapper, execute diretamente `mvn spring-boot:run`).*  
As tabelas serão criadas/atualizadas automaticamente pelo **Flyway**.

---

### 4. Executar o Frontend

Em um novo terminal, entre na pasta do frontend:

```bash
# Instalar dependências
npm install

# Iniciar servidor de desenvolvimento
npm run dev
```

Abra [http://localhost:3000](http://localhost:3000) no seu navegador para visualizar a interface.

---

## 👥 Equipe

| Papel | Nome |
|---|---|
| **Discente** | Helena Trombetta |
| **Discente** | Vitor Ferraz |
| **Docente Orientador** | Antônio Junio |
| **Docente Orientador** | Jonas Bernardino |

**Contexto:** Projeto Extensionista + Desenvolvimento Backend.

---

## 🔗 Links Úteis

- **Repositório Oficial:** [github.com/helenadntt/Projeto_Gestao-de-Obras](https://github.com/helenadntt/Projeto_Gestao-de-Obras)
- **Deploy Frontend:** *Link em definição pós-publicação na Vercel*

---

## 📚 Documentação Complementar

- [ ] Levantamento de requisitos funcionais e não funcionais
- [ ] Diagrama de Entidade-Relacionamento (DER)
- [ ] Documentação de endpoints da API (Swagger / OpenAPI)
- [ ] Especificação dos fluxos de migrations (Flyway)

---

## 📈 Status do Projeto

> 🚧 **Em desenvolvimento ativo.**  
> Foco no ciclo iterativo entre modelagem, entrega dos endpoints de negócio e integração de interface.
