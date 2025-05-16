## Feature: `proposal-service`

Este módulo é responsável pela **gestão de propostas para projetos** dentro da plataforma. Ele permite que usuários `FREELANCER` autenticados criem, visualizem, atualizem e excluam suas propostas. Usuários `CLIENT` (donos dos projetos) podem visualizar as propostas recebidas para seus projetos e tomar decisões (aceitar/rejeitar) sobre elas. A implementação segue padrões RESTful, garantindo segurança, validação e organização das regras de negócio.

---

### Funcionalidades

- Criação de novas propostas por usuários `FREELANCER` para projetos `OPEN` ou `NEGOTIATING`.
- Listagem paginada e ordenada de propostas:
    - Por ID do projeto (para o `CLIENT` dono do projeto).
    - Por ID do freelancer (para o `FREELANCER` dono das propostas).
- Visualização de detalhes de uma proposta específica (pelo `FREELANCER` que a criou ou pelo `CLIENT` dono do projeto).
- Atualização de propostas existentes (apenas pelo `FREELANCER` criador).
- Exclusão de propostas (apenas pelo FREELANCER criador, com confirmação de senha).
- Processamento de decisão sobre uma proposta (Aceitar/Rejeitar) pelo `CLIENT` dono do projeto (com confirmação de senha).
    - Aceitar uma proposta muda o status do projeto para `IN_PROGRESS`.    
    - Rejeitar uma proposta a remove. Se for a última proposta e o projeto estiver `NEGOTIATING`, o status do projeto volta para `OPEN`.    
- Controle de acesso via JWT e roles.
- Documentação completa via Swagger.
- Testes unitários e de integração.

---

### Estrutura

#### Entidade `Proposal`
- Mapeada para a tabela `proposals`.
- Campos: `id`, `deadline`, `offeredValue`, `createdAt`, `project (associação ManyToOne com Project)`, `freelancer (associação ManyToOne com User)`.

#### Enum `ProposalDecisionAction`
- Representa as ações possíveis que um CLIENT pode tomar sobre uma proposta (ACCEPT, DECLINE).

#### Serviços
- Interface `IProposalService`
- Implementação `ProposalServiceImpl` com lógica de negócio (CRUD de propostas, processamento de decisões, validações de acesso e status do projeto).

#### Controllers
- `ProposalController`: expõe endpoints REST para operações com propostas.

#### Utilitários
- `ProposalAccessHelper`: valida se o usuário (freelancer) tem permissão para modificar/visualizar a proposta ou se o cliente é dono do projeto associado.
- `ProposalUpdateHelper`: centraliza a lógica de atualização parcial ou completa dos dados da proposta.
- `GetAllProposalsHelper`: auxilia na construção de respostas paginadas para listagem de propostas.

---

### Segurança

- Autenticação via JWT.
- Autorização com @PreAuthorize e validações adicionais nos serviços:
    - Criar proposta: requer role FREELANCER.
    - Listar propostas por projeto: requer role CLIENT (e ser dono do projeto).
    - Listar propostas por freelancer: requer role FREELANCER (e ser o dono das propostas).
    - Visualizar detalhes de uma proposta: requer autenticação (e ser o freelancer da proposta ou o cliente dono do projeto).
    - Atualizar/Excluir proposta: requer role FREELANCER (e ser o dono da proposta).
    - Processar decisão da proposta: requer role CLIENT (e ser dono do projeto).

---

### Banco de Dados

- Migração Flyway: `V3__create-table-proposals.sql`
  - Cria a tabela proposals com chaves estrangeiras para `projects` e `users`.
  - Adiciona índices para `project_id` e `user_id`.
-  Migração Flyway: `add-index-to-projects-user-id.sql`

---
### Testes

#### Unitários
- `ProposalServiceImplTest`: cobre regras de negócio, validações de status, permissões e manipulação de dados para criação, listagem, visualização, atualização, exclusão de propostas e processamento de decisões.
- `ProposalAccessHelperTest`: testa a lógica de validação de acesso às propostas.
- `ProposalUpdateHelperTest`: testa a lógica de atualização dos campos da proposta.
- `GetAllProposalsHelperTest`: testa a lógica de paginação para listagem de propostas.
#### Integração
- `ProposalControllerIntegrationTest`: cobre endpoints REST com autenticação, validação de DTOs, respostas HTTP e interações com o banco de dados, utilizando **Testcontainers**.

---

### Cache

- **Invalida** o cache de projetos (getProjectCache):
  - Na criação de uma proposta (createProposal), se o status do projeto mudar para NEGOTIATING.
  - No processamento de uma decisão (processProposalDecision), se o status do projeto mudar (para IN_PROGRESS ou OPEN).

---

### Endpoints

#### Protegidos (JWT)

| Método | Endpoint                                 | Descrição                                                                        | Permissão                                                                 |
| ------ | ---------------------------------------- | -------------------------------------------------------------------------------- | ------------------------------------------------------------------------- |
| POST   | /api/proposal/project/{projectId}/create | Criar nova proposta para um projeto                                              | FREELANCER                                                                |
| GET    | /api/proposal/project/{projectId}        | Listar propostas de um projeto específico                                        | CLIENT (dono do projeto)                                                  |
| GET    | /api/proposal/freelancer/{freelancerId}  | Listar propostas de um freelancer específico                                     | FREELANCER (dono das propostas)                                           |
| GET    | /api/proposal/proposal/{proposalId}      | Visualizar detalhes de uma proposta específica                                   | Autenticado (freelancer da proposta ou cliente dono do projeto associado) |
| PUT    | /api/proposal/{proposalId}/update        | Atualizar uma proposta existente                                                 | FREELANCER (dono da proposta)                                             |
| DELETE | /api/proposal/{proposalId}/delete        | Deletar uma proposta (requer senha do freelancer)                                | FREELANCER (dono da proposta)                                             |
| POST   | /api/proposal/{proposalId}/decision      | Processar decisão (Aceitar/Rejeitar) para uma proposta (requer senha do cliente) | CLIENT (dono do projeto associado à proposta)                             |
