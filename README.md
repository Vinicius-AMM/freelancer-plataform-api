# 📦 Plataforma de Gestão de Projetos Freelancers

## 📄 Descrição

API desenvolvida em Java com Spring Boot para uma **plataforma de gestão de projetos freelancers**. O sistema permite que usuários atuem como **Clientes** ou **Freelancers**.

- **Clientes** podem cadastrar projetos, receber propostas, negociar e aprovar a conclusão dos trabalhos.
- **Freelancers** podem buscar projetos, enviar suas propostas e gerenciar os projetos em que foram aceitos.

A API gerencia todo o ciclo de vida do projeto, desde a criação e negociação de propostas, passando pela execução, até a entrega e aprovação final.

---

## 🚀 Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **PostgreSQL**
- **Docker & Docker Compose**
- **Flyway** (para versionamento de banco de dados)
- **JWT** (para autenticação e autorização via token)
- **Swagger/OpenAPI** (para documentação dos endpoints)
- **Redis** (para gerenciamento de cache, como perfis de usuário)
- **Spring Security** (para controle de acesso baseado em papéis)
- **JUnit 5, Mockito & Testcontainers** (para testes unitários e de integração)
---

## ⚙️ Como Rodar o Projeto

### 1. Pré-requisitos
- [Java 21](https://www.oracle.com/br/java/technologies/downloads/#java21) ou superior
- [Maven](https://maven.apache.org/download.cgi)
- [Docker](https://www.docker.com/get-started/) e [Docker Compose](https://docs.docker.com/compose/install/)
---
### 2. Clonar o repositório

```
git clone https://github.com/Vinicius-AMM/freelancer-plataform-api.git
```
---
### 3. Gerando suas próprias chaves RSA

Para utilizar autenticação JWT com RSA, é necessário gerar um par de chaves: **privada** (para assinar tokens) e **pública** (para validá-los).
##### 3.1. Gerar a chave privada

```shell
$ openssl genrsa > app.key
```

Cria o arquivo `app.key`, contendo a chave privada.
##### 3.2. Gerar a chave pública correspondente

```shell
$ openssl rsa -in app.key -pubout -out app.pub
```

Cria o arquivo `app.pub`, contendo a chave pública.

##### 3.3. Estrutura final esperada

- `app.key`: chave privada (deve ser mantida em segurança, nunca exposta)
- `app.pub`: chave pública (pode ser usada pela aplicação para verificar tokens)

##### 3.4. Certifique-se de que os arquivos gerados estejam no diretório `src/main/resources/keys`, seguindo a estrutura do projeto.
---
### 4. Subir a aplicação com Docker Compose

O docker-compose.yml (se configurado) subirá o container da API, um banco de dados PostgreSQL e um servidor Redis.

```
docker-compose up --build
```
---
### 5. Acessar a API

A API estará disponível em:  
📍 http://localhost:8080

A documentação interativa com Swagger estará em:  
📘 http://localhost:8080/swagger-ui.html

---
### 6. Finalizar a aplicação

Para parar os containers:

```
docker-compose down
```
---
# 📚 Endpoints

## 🔐 Autenticação & Usuários (`/auth`, `/api/users`)

| Método     | Endpoint                         | Descrição                                                               |
| ---------- | -------------------------------- | ----------------------------------------------------------------------- |
| **POST**   | `/auth/register`                 | Cria um novo usuário.                                                   |
| **POST**   | `/auth/login`                    | Autentica e retorna um token JWT.                                       |
| **GET**    | `/api/users/{id}/profile`        | Retorna perfil público limitado ou completo (se for o próprio usuário). |
| **PATCH**  | `/api/users/{id}/updatePassword` | Altera a senha do usuário.                                              |
| **PATCH**  | `/api/users/{id}/updateFullName` | Altera o nome completo do usuário.                                      |
| **PATCH**  | `/api/users/{id}/updateEmail`    | Atualiza o e-mail do usuário.                                           |
| **PATCH**  | `/api/users/{id}/updateDocument` | Atualiza o documento do usuário.                                        |
| **PATCH**  | `/api/users/{id}/changeUserRole` | Altera papel atual entre `CLIENT` e `FREELANCER`.                       |
| **DELETE** | `/api/users/{id}`                | Exclui a própria conta (exige senha).                                   |
##### Exemplo - Registro

```
{
  "fullName": "Test User",
  "document": "12345678901",
  "email": "test@email.com",
  "password": "password",
  "mainUserRole": "FREELANCER",
  "currentUserRole": "CLIENT"
}
```

##### Exemplo - Login

```
{
  "email": "test@email.com",
  "password": "password"
}
```

---

## 📋 Projetos (`/api/project`)

| Método     | Endpoint                                        | Descrição                                                                                                       |
| ---------- | ----------------------------------------------- | --------------------------------------------------------------------------------------------------------------- |
| **POST**   | `/api/project/create`                           | Cria um projeto (CLIENT).                                                                                       |
| **GET**    | `/api/project/projects`                         | Lista projetos disponíveis (paginação).                                                                         |
| **GET**    | `/api/project/{id}`                             | Retorna um projeto pelo ID.                                                                                     |
| **PUT**    | `/api/project/update-project/{id}`              | Atualiza um projeto (somente dono).                                                                             |
| **DELETE** | `/api/project/delete-project/{id}`              | Deleta um projeto apenas se seu status for `OPEN` ou `NEGOTIATING`(Somente o `CLIENT` do projeto pode deletar). |
| **POST**   | `/api/project/{projectId}/freelancer-complete`  | Freelancer marca o projeto como concluído.                                                                      |
| **POST**   | `/api/project/{projectId}/client-approve`       | Cliente aprova conclusão (`COMPLETED_BY_FREELANCER`).                                                           |
| **POST**   | `/api/project/{projectId}/request-adjustments`  | Cliente solicita ajustes.                                                                                       |
| **GET**    | `/api/project/client/completed`                 | Lista projetos concluídos do CLIENT.                                                                            |
| **GET**    | `/api/project/freelancer/completed`             | Lista projetos concluídos do FREELANCER.                                                                        |
| **GET**    | `/api/project/client/pending-approval`          | Lista projetos aguardando aprovação do CLIENT.                                                                  |
| **GET**    | `/api/project/freelancer/active`                | Lista projetos ativos do FREELANCER.                                                                            |
| **GET**    | `/api/project/client/active`                    | Lista projetos ativos do CLIENT.                                                                                |
| **GET**    | `/api/project/freelancer/adjustments-requested` | Lista projetos com ajustes solicitados (FREELANCER).                                                            |
##### Exemplo - Criação de Projeto

```
{
  "title": "Desenvolvimento de API de E-commerce",
  "description": "API para gerenciar produtos, pedidos e clientes.",
  "startDate": "2025-01-10",
  "endDate": "2025-03-10",
  "estimatedBudget": 8000.00
}
```

---

## 💡 Propostas (`/api/proposal`)

| Método     | Endpoint                                   | Descrição                                                                                                                                                                                                                                                                                                                                                                          |
| ---------- | ------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **POST**   | `/api/proposal/project/{projectId}/create` | Envia proposta para um projeto (FREELANCER).                                                                                                                                                                                                                                                                                                                                       |
| **GET**    | `/api/proposal/project/{projectId}`        | Lista propostas de um projeto (somente CLIENT dono).                                                                                                                                                                                                                                                                                                                               |
| **GET**    | `/api/proposal/freelancer/{freelancerId}`  | Lista propostas enviadas por um freelancer.                                                                                                                                                                                                                                                                                                                                        |
| **GET**    | `/api/proposal/{proposalId}`               | Retorna uma proposta específica.                                                                                                                                                                                                                                                                                                                                                   |
| **PUT**    | `/api/proposal/{proposalId}/update`        | Atualiza uma proposta existente.                                                                                                                                                                                                                                                                                                                                                   |
| **POST**   | `/api/proposal/{proposalId}/decision`      | CLIENT aceita ou recusa proposta (exige senha).<br><br>**ACCEPT:** Muda o status do projeto para IN_PROGRESS, define o acceptedFreelancer, atualiza o orçamento do projeto com o valor da proposta e invalida o cache do projeto.<br>    <br>**DECLINE:** A proposta é deletada. Se for a última proposta e o projeto estiver em NEGOTIATING, o status do projeto volta para OPEN. |
| **DELETE** | `/api/proposal/{proposalId}/delete`        | Deleta uma proposta.                                                                                                                                                                                                                                                                                                                                                               |
##### Exemplo - Envio de Proposta
```
{
  "startDate": "2025-01-12",
  "endDate": "2025-03-05",
  "offeredValue": 7500.00
}
```
##### Exemplo - Decisão sobre Proposta
```
{
  "action": "ACCEPT",
  "rawPassword": "clientPassword"
}
```
---
## 📜 Regras de Negócio

- **Usuários:**
    
    - Um usuário não pode ser criado com e-mail ou documento já existente.
    - O usuário pode alternar seu papel (currentUserRole) entre CLIENT e FREELANCER a qualquer momento.
    
- **Projetos:**
    - Apenas usuários com o papel CLIENT podem criar projetos.
    - Um projeto só pode ser excluído se seu status for OPEN ou NEGOTIATING.
    - Ao receber a primeira proposta, um projeto com status OPEN muda para NEGOTIATING.
    - Ao aceitar uma proposta, o status do projeto muda para IN_PROGRESS, o freelancer da proposta é vinculado, e o orçamento do projeto é atualizado para o valor da proposta aceita.
    - O freelancer só pode marcar um projeto como concluído se o status for IN_PROGRESS.
    - O cliente só pode solicitar ajustes ou aprovar a conclusão de projetos com status COMPLETED_BY_FREELANCER.
    
- **Propostas:**
    - Apenas usuários com o papel FREELANCER podem enviar propostas.
    - Propostas só podem ser enviadas para projetos com status OPEN ou NEGOTIATING.
    - Apenas o dono do projeto (CLIENT) pode aceitar ou recusar propostas.
    - Ao recusar a última proposta de um projeto em NEGOTIATING, o status do projeto retorna para OPEN.
    
- **Segurança:**
    - Endpoints de criação, atualização e exclusão exigem autenticação e, em muitos casos, verificação de propriedade e senha.
    - O login retorna um token JWT válido por **2 horas**.
    - Senhas são criptografadas com **BCrypt**.

---
## ⚡Caching

A aplicação utiliza **Redis** para otimizar o desempenho de consultas frequentes.

- **Perfis de Usuário (userProfileCache):** Os dados de perfil são cacheados para reduzir acessos ao banco. O cache é invalidado (@CacheEvict) sempre que há uma atualização em qualquer dado do usuário (nome, e-mail, etc.).
- **Detalhes do Projeto (getProjectCache):** As informações de um projeto específico são cacheadas (@Cacheable). O cache é invalidado quando o projeto é atualizado, deletado, ou quando uma proposta é aceita para ele.
---
## 📌 Extras

- ✅ **Testes Automatizados:** Cobertura de testes com JUnit 5, Mockito e Testcontainers para testes de integração.
- 🔒 **Segurança Robusta:** Controle de acesso por papel (@PreAuthorize) e validação de propriedade dos recursos.
