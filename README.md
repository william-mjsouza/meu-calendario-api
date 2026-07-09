# Meu Calendário — API

> ## 🚀 Aplicação online — acesse agora
>
> ### **[https://meu-calendario-cg5xbtk6e-williamportfolionetlifyapp.vercel.app/index.html](https://meu-calendario-cg5xbtk6e-williamportfolionetlifyapp.vercel.app/index.html)**
>
> **API pública**: `https://meu-calendario-api.onrender.com`
> **Repositório do frontend**: [meu-calendario](https://github.com/william-mjsouza/meu-calendario)
>
> ⏱️ *A primeira requisição pode demorar ~50 segundos (cold start do free tier do Render — a instância dorme após 15 minutos de inatividade). As requisições seguintes são instantâneas.*

---

Backend REST em **Spring Boot 3.5 + Java 21** que serve o aplicativo [Meu Calendário](https://github.com/william-mjsouza/meu-calendario), com autenticação JWT stateless, PostgreSQL (Neon) como banco gerenciado, e deploy containerizado (Docker) no Render.

---

## Visão geral

API multi-tenant onde cada usuário só enxerga e manipula seus próprios eventos. A stack foi escolhida priorizando **confiabilidade em produção grátis** (Neon Postgres + Render Docker), **segurança pronta para portfólio** (BCrypt + JWT HS256 + CORS restrito + segredos em `.env`) e **arquitetura em camadas explícita**, com separação clara entre controller, service, repository, entity, DTO, config, exception e security.

Toda a lógica de recorrência de eventos (diária, semanal, quinzenal, mensal, anual) foi projetada para conversar com o frontend via DTOs em `snake_case`, minimizando adaptação entre camadas.

---

## Stack técnica

| Camada | Tecnologia |
|---|---|
| Linguagem | **Java 21** (records, pattern matching, virtual threads-ready) |
| Framework | **Spring Boot 3.5** (Web, Data JPA, Security, Validation) |
| Persistência | **PostgreSQL 16** (Neon serverless em produção; local em dev) |
| ORM | **Hibernate 6** |
| Segurança | **Spring Security 6** + **JJWT 0.12** (HS256, stateless) |
| Build | **Maven 3.9** (via wrapper `mvnw`) |
| Container | **Docker** multi-stage (build + runtime Alpine) |
| Config | **spring-dotenv 4.0** (carrega `.env` local) |
| Deploy | **Render** (auto-deploy do `main` no push) |
| CI/CD | GitHub → Render (auto-build via Docker) |

---

## Arquitetura em camadas

```
src/main/java/com/william/meu_calendario_api/
├── controller/          # Endpoints REST (@RestController)
│   ├── AuthController.java       # POST /api/auth/{register,login}
│   └── EventController.java      # CRUD /api/events (autenticado)
│
├── service/             # Regras de negócio
│   ├── AuthService.java          # Registro + login + geração de JWT
│   └── EventService.java         # CRUD sempre escopado ao userId
│
├── repository/          # Spring Data JPA
│   ├── UserRepository.java       # findByEmail, existsByEmail
│   └── EventRepository.java      # findByUserId, findByIdAndUserId
│
├── entity/              # Modelo de domínio (JPA)
│   ├── User.java                 # E-mail único, senha em BCrypt
│   ├── Event.java                # FK user_id — dono do evento
│   └── Frequency.java            # Enum de recorrência
│
├── dto/                 # Contratos de entrada/saída
│   ├── auth/                     # RegisterRequest, LoginRequest, AuthResponse
│   └── event/                    # EventRequest, EventResponse (snake_case)
│
├── config/              # Configurações Spring
│   └── SecurityConfig.java       # SecurityFilterChain + CORS + BCrypt bean
│
├── exception/           # Tratamento centralizado
│   ├── ResourceNotFoundException.java
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java  # @RestControllerAdvice
│                                    # Traduz exceções em 400/401/404/405/415/500
│
└── security/            # Infraestrutura de autenticação
    ├── CustomUserDetails.java
    ├── CustomUserDetailsService.java
    ├── JwtService.java              # Geração/validação HS256 via JJWT
    └── JwtAuthenticationFilter.java # OncePerRequestFilter
```

Cada camada tem responsabilidade única. **Controllers só orquestram** — recebem DTO, delegam ao service, retornam DTO. **Services** implementam as regras e transacionam via `@Transactional`. **Repositories** são interfaces do Spring Data — zero SQL escrito manualmente. **DTOs** protegem as entidades JPA de vazarem detalhes de persistência para a API pública.

---

## Segurança

- **Senhas em BCrypt** — nunca armazenadas em texto puro; hash com salt aleatório automático.
- **JWT stateless HS256** — o servidor não guarda sessão; toda requisição autenticada carrega o token no header `Authorization: Bearer <token>`. Segredo mínimo de 256 bits, expiração de 24h configurável.
- **Filtro dedicado** (`JwtAuthenticationFilter extends OncePerRequestFilter`) que popula o `SecurityContext` a cada requisição, permitindo que os controllers usem `@AuthenticationPrincipal CustomUserDetails` para obter o dono da ação sem buscar no banco.
- **Isolamento multi-tenant garantido no repository**: os métodos `findByUserId` e `findByIdAndUserId` tornam impossível vazar ou modificar eventos alheios — a segurança é enforçada no acesso a dados, não só na camada de controller.
- **CORS restritivo por padrão** (`http://localhost:*`), com produção liberada via env `FRONTEND_URL` — nunca `*` no `Access-Control-Allow-Origin`.
- **Segredos fora do Git**: `application.properties` usa placeholders `${DB_URL}`, `${DB_PASSWORD}`, `${JWT_SECRET}` resolvidos por `spring-dotenv` (local) ou pelas Environment Variables do Render (produção). Um `.env.example` documenta o formato sem expor valores reais.
- **Tratamento explícito de exceções** com códigos HTTP corretos: 401 para credenciais inválidas, 400 para validação de campos, 404 para recurso inexistente, 405 para método não suportado etc. — em vez do 500 genérico padrão.

---

## Endpoints

### Autenticação (públicos)

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| `POST` | `/api/auth/register` | `{ name, email, password }` | `{ token, userId, name, email }` |
| `POST` | `/api/auth/login` | `{ email, password }` | `{ token, userId, name, email }` |

### Eventos (autenticados — enviam `Authorization: Bearer <token>`)

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/events` | Lista todos os eventos do usuário autenticado |
| `POST` | `/api/events` | Cria um novo evento |
| `PUT` | `/api/events/{id}` | Atualiza um evento (404 se não for do usuário) |
| `DELETE` | `/api/events/{id}` | Remove um evento (404 se não for do usuário) |

### Formato do `EventRequest` / `EventResponse` (snake_case por compatibilidade com o frontend)

```json
{
  "id": 42,
  "title": "Reunião de sprint",
  "description": "Alinhar entregas",
  "start_date": "2026-07-15",
  "end_event_date": "2026-07-15",
  "start_hour": "09:00",
  "end_hour": "10:00",
  "all_day": false,
  "frequency": "WEEKLY",
  "end_date": "2026-12-31",
  "color": "rgb(122, 154, 197)",
  "formGroupColor": "var(--blue-form-group-bg-color)",
  "concluded": false,
  "originalFrequency": null
}
```

Frequência aceita: `ONCE`, `DAILY`, `WEEKLY`, `BIWEEKLY`, `MONTHLY`, `YEARLY`.

---

## Setup local

### Pré-requisitos
- Java 21
- Maven 3.9+ (ou use o wrapper `./mvnw`)
- PostgreSQL 14+ rodando localmente OU credenciais de um Postgres remoto (Neon, Supabase etc.)

### Passos

**1. Clone e configure o `.env`:**

```bash
git clone https://github.com/william-mjsouza/meu-calendario-api.git
cd meu-calendario-api
cp .env.example .env
# edite .env com suas credenciais reais
```

Conteúdo mínimo do `.env`:
```env
DB_URL=jdbc:postgresql://localhost:5432/meu_calendario
DB_USERNAME=root
DB_PASSWORD=sua_senha_do_postgres
JWT_SECRET=cole_um_secret_base64_de_32_bytes_ou_mais
```

Para gerar um `JWT_SECRET` novo:
- **Linux/macOS**: `openssl rand -base64 48`
- **PowerShell**: `[Convert]::ToBase64String([byte[]](1..48 | ForEach-Object { Get-Random -Maximum 256 }))`

**2. Crie o database (o Hibernate cria as tabelas automaticamente):**

```sql
CREATE DATABASE meu_calendario;
CREATE ROLE root WITH LOGIN SUPERUSER PASSWORD 'sua_senha_do_postgres';
```

**3. Rode a aplicação:**

```bash
./mvnw spring-boot:run    # Linux/macOS
mvnw.cmd spring-boot:run  # Windows
```

A API sobe em `http://localhost:8080`.

**4. Teste rápido:**

```bash
# Cadastro
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Fulano","email":"fulano@ex.com","password":"secreta123"}'

# Login (guarda o token)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"fulano@ex.com","password":"secreta123"}'

# Listar eventos (usa o token retornado)
curl http://localhost:8080/api/events \
  -H "Authorization: Bearer <token_aqui>"
```

---

## Deploy em produção (Docker + Render + Neon)

O deploy é 100% gratuito e o pipeline é `git push → Docker build no Render → serviço no ar`. Aqui está o passo a passo aplicado neste projeto:

### 1. Banco de dados: Neon (Postgres serverless)

- Conta gratuita em [neon.tech](https://neon.tech) → **Create Project**.
- Escolhida a região **AWS US East (Ohio)** para latência baixa com o Render.
- Neon fornece uma **connection string** pronta com SSL habilitado. Anotados: host, database, username, password.
- Free tier: **0.5 GB storage**, autosleep na branch main (retomada em <1s), sem cartão de crédito.

### 2. Backend: Render (Docker Web Service)

**a) Dockerfile multi-stage** ([Dockerfile](./Dockerfile)):

```dockerfile
# Stage 1: build do JAR com Maven
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# Stage 2: runtime enxuto com Alpine
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

Multi-stage garante que a imagem final não carrega Maven nem código-fonte — só o JAR + JRE Alpine (~180 MB).

**b) Porta dinâmica**: o Render injeta a variável `PORT` no container. O `application.properties` lê `server.port=${PORT:8080}` — usa a porta do Render em produção e cai para 8080 localmente.

**c) Setup no dashboard Render:**

- New → Web Service → conectar repositório `meu-calendario-api`.
- Runtime: **Docker** (detecta o Dockerfile automaticamente).
- Region: **Ohio** (mesma do Neon).
- Instance Type: **Free** (750h/mês, autosleep após 15min inativo).
- Environment Variables:
  - `DB_URL` = `jdbc:postgresql://<host-neon>/neondb?sslmode=require`
  - `DB_USERNAME` = usuário do Neon
  - `DB_PASSWORD` = senha do Neon
  - `JWT_SECRET` = segredo Base64 gerado com `openssl`
  - `FRONTEND_URL` = URL do frontend (para o CORS)

**d) CORS de produção**: o `application.properties` usa `setAllowedOriginPatterns` (com curinga) em vez de `setAllowedOrigins`, permitindo padrões tipo `https://meu-calendario-*.vercel.app` que cobrem todos os deployments do Vercel de uma vez.

**e) Auto-deploy contínuo**: cada `git push` no `main` dispara build + deploy automáticos no Render. O log de build fica visível no dashboard.

### 3. Domínio HTTPS grátis

O Render fornece automaticamente um subdomínio `.onrender.com` com certificado TLS válido — nenhuma configuração extra necessária.

### 4. Custo total

Zero. **Free tier permanente** para o tamanho do projeto: Neon (0.5 GB, sem expiração), Render (750h/mês, cold start de ~50s após 15min inativo).

---

## Estrutura de pastas

```
meu-calendario-api/
├── Dockerfile                        # Build multi-stage para deploy
├── pom.xml                           # Dependências Maven
├── mvnw / mvnw.cmd                   # Wrappers do Maven
├── .env.example                      # Template de env vars (versionado)
├── .env                              # Segredos locais (ignorado pelo Git)
├── .gitignore                        # Inclui .env e target/
├── src/
│   ├── main/
│   │   ├── java/com/william/meu_calendario_api/
│   │   │   ├── MeuCalendarioApiApplication.java
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   ├── dto/
│   │   │   ├── config/
│   │   │   ├── exception/
│   │   │   └── security/
│   │   └── resources/
│   │       └── application.properties
│   └── test/                         # Escopo futuro
└── README.md
```

---

## Decisões técnicas notáveis

- **Records em vez de classes para DTOs** (Java 21): menos boilerplate, imutabilidade natural, `equals`/`hashCode` gerados. Ex.: `RegisterRequest`, `LoginRequest`, `EventRequest`.
- **Lombok apenas nas entidades** (`@Getter`/`@Setter`/`@Builder`): DTOs não precisam — records resolvem melhor.
- **`@AuthenticationPrincipal CustomUserDetails`** nos controllers em vez de `Authentication` genérica: type-safe, o `userId` sai diretamente sem cast.
- **`orphanRemoval = true`** no `OneToMany` do User → Events: garantia de que remover um usuário limpa os eventos dele automaticamente (se um dia for necessário).
- **`@JsonFormat` explícito nos DTOs** para `LocalDate` (`yyyy-MM-dd`) e `LocalTime` (`HH:mm`): serialização estável, sem depender de configuração global do Jackson.
- **Tratamento de exceções centralizado** com handlers explícitos para as exceções mais comuns do Spring MVC (`HttpRequestMethodNotSupportedException`, `HttpMediaTypeNotSupportedException`, `NoResourceFoundException`), retornando os HTTP status codes semanticamente corretos.
