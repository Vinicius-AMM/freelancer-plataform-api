## Feature: `user-service`

É responsável pelo **cadastro, autenticação e gerenciamento de usuários** na plataforma. Ele utiliza **Spring Boot**, **Spring Security** com **JWT (chaves RSA)**, e possui cobertura com testes unitários e de integração.

---

### Funcionalidades

- Registro e login de usuários com autenticação baseada em JWT (chave pública/privada RSA).
- Consulta de perfil de usuário autenticado ou de terceiros(com dados limitados).
- Atualização de informações do usuário (nome, email, documento, senha).
- Alteração da role atual.
- Exclusão de conta.
- Proteção dos endpoints com base nas permissões do usuário.
- Tratamento de exceções personalizadas para regras de negócio.

---

### Estrutura

#### Autenticação (JWT)
- Tokens JWT assinados com chave privada RSA e validados com chave pública.
- Endpoints públicos para autenticação:
  - `POST /auth/register`: Registro de novo usuário.
  - `POST /auth/login`: Login com retorno do token JWT.

#### Entidade `User`
- Mapeada para a tabela `users`.
- Campos: `id`, `fullName`, `document`, `email`, `password`, `mainUserRole`, `currentUserRole`.

#### Serviços
- `AuthenticationService`: Gerencia registro e login.
- `UserService`: Opera ações de gerenciamento de perfil:
  - Buscar perfil
  - Atualizar dados
  - Alterar role atual
  - Deletar conta

---

### Testes

#### Unitários
- Serviços e validadores foram testados individualmente para garantir comportamento isolado.

#### Integração
- Estrutura de testes utilizando **Testcontainers**, com containers PostgreSQL e Redis para simulação de ambiente real.
- Testes cobrindo os principais endpoints de autenticação e gerenciamento de usuários.

---

### Dependências utilizadas

- Spring Boot Starter Web  
- Spring Boot Starter Security  
- Spring Boot Starter Data JPA  
- PostgreSQL Driver  
- Flyway (migrações)  
- Lombok  
- Redis  
- Testcontainers (PostgreSQL, Redis)  
- Spring Boot Starter Validation  
- Springdoc OpenAPI (Swagger)

---

### Endpoints

#### Públicos
| Método | Endpoint            | Descrição           |
|--------|---------------------|---------------------|
| POST   | `/auth/register`    | Registro de usuário |
| POST   | `/auth/login`       | Login do usuário    |

#### Protegidos (JWT)
| Método | Endpoint                                | Descrição                            |
|--------|-----------------------------------------|--------------------------------------|
| GET    | `/api/user/{id}/profile`                | Obter perfil de usuário              |
| PATCH  | `/api/user/{id}/changeUserRole`         | Alterar role atual do usuário        |
| PATCH  | `/api/user/{id}/updateDocument`         | Atualizar documento do usuário       |
| PATCH  | `/api/user/{id}/updateEmail`            | Atualizar email                      |
| PATCH  | `/api/user/{id}/updateFullName`         | Atualizar nome completo              |
| PATCH  | `/api/user/{id}/updatePassword`         | Atualizar senha                      |
| DELETE | `/api/user/{id}`                        | Deletar conta                        |

---

### Gerando suas próprias chaves RSA

Para utilizar autenticação JWT com RSA, é necessário gerar um par de chaves: **privada** (para assinar tokens) e **pública** (para validá-los).

#### 1. Gerar a chave privada

```bash
$ openssl genrsa > app.key
```

Cria o arquivo `app.key`, contendo a chave privada.

#### 2. Gerar a chave pública correspondente

```bash
$ openssl rsa -in app.key -pubout -out app.pub
```

Cria o arquivo `app.pub`, contendo a chave pública.

#### 3. Estrutura final esperada

- `app.key`: chave privada (deve ser mantida em segurança, nunca exposta)
- `app.pub`: chave pública (pode ser usada pela aplicação para verificar tokens)

---

Coloque os arquivos gerados dentro do diretório `src/main/resources/keys` conforme a estrutura do projeto.
