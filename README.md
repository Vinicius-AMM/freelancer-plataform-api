## Feature: `project-service`

Este módulo é responsável pela **gestão de projetos** dentro da plataforma. Ele permite que usuários autenticados com a role adequada criem, visualizem, atualizem e excluam projetos. A implementação segue padrões RESTful, garantindo segurança, validação e organização das regras de negócio.

---

### Funcionalidades

- Criação de novos projetos por usuários autenticados com role `CLIENT`.
- Listagem paginada e ordenada de projetos.
- Visualização de detalhes de um projeto.
- Atualização e exclusão de projetos (apenas pelo criador).
- Controle de acesso via JWT e roles.
- Utilização de cache para otimizar requisições.
- Documentação completa via Swagger.
- Testes unitários e de integração.

---

### Estrutura

#### Entidade `Project`
- Mapeada para a tabela `projects`.
- Campos: `id`, `title`, `description`,`deadline`, `estimatedBudget`, `status`, `createdAt`, `user`.

#### Enum `ProjectStatus`
- Representa os estados possíveis de um projeto (`OPEN`, `NEGOTIATING`, `IN_PROGRESS`, `FINISHED`, `CANCELLED`.).

#### Serviços
- Interface `IProjectService`
- Implementação `ProjectServiceImpl` com lógica de negócio (CRUD + validações).

#### Controllers
- `ProjectController`: expõe endpoints REST para operações com projetos.

#### Utilitários
- `ProjectAccessHelper`: valida se o usuário tem permissão para modificar o projeto.
- `ProjectUpdateHelper`: centraliza a lógica de atualização parcial ou completa dos dados.

---

### Segurança

- Autenticação via JWT.
- Autorização com `@PreAuthorize`:
  - Criar, atualizar e excluir: requer role `CLIENT`.
  - Visualizar: apenas autenticação.

---

### Banco de Dados

- Migração Flyway: `V2__create-table-projects.sql`.

---

### Testes

#### Unitários
- `ProjectServiceImplTest`: cobre regras de negócio, validações e manipulação de dados.

#### Integração
- `ProjectControllerIntegrationTest`: cobre endpoints REST com autenticação e validação de respostas, utilizando **Testcontainers**.

---

### Cache

- Ativado no método `getProjectById`.
- Invalidação automática em operações de atualização e exclusão.

---

### Endpoints

#### Protegidos (JWT)

| Método | Endpoint                                | Descrição                                | Permissão      |
|--------|-----------------------------------------|-------------------------------------------|----------------|
| POST   | `/api/project/create`                   | Criar novo projeto                         | CLIENT         |
| GET    | `/api/project/projects`                 | Listar todos os projetos                   | Qualquer usuário autenticado |
| GET    | `/api/project/projects/{id}`            | Visualizar detalhes de um projeto          | Qualquer usuário autenticado |
| PUT    | `/api/project/update-project/{id}`      | Atualizar um projeto                       | CLIENT (dono)  |
| DELETE | `/api/project/delete-project/{id}`      | Deletar um projeto com confirmação de senha| CLIENT (dono)  |
