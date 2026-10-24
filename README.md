# Águia Inova Sprint 2 (Backend + App integrados)

[![Backend CI](https://github.com/Kelvinwendell/App-guia/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/Kelvinwendell/App-guia/actions/workflows/backend-ci.yml)

Gestão da inovação empresarial: operadores registram **ideias**, gestores as avaliam e transformam em **projetos**, e a
liderança define **estratégias** e acompanha o **dashboard** (ROI, lucro, prazo, investimento, produtividade).
O app Android da Sprint 1 agora conversa com uma API real (sem mocks, sem Firebase/Room) e há IA generativa opcional.

## Em produção

API rodando de graça (MongoDB Atlas + Render): **<https://app-guia.onrender.com>**
(Swagger: `/swagger-ui/index.html` · saúde: `/actuator/health`). O plano gratuito do Render
"dorme" após 15 min sem tráfego — a primeira chamada depois disso demora ~1 min para responder.

```
.
├── backend/               API REST — Java 25, Spring Boot 4, Spring Security (JWT), Spring Data MongoDB
├── mobile/                App Android — Kotlin, Jetpack Compose, MVVM, Retrofit
├── docker-compose.yml     Sobe MongoDB + API com um comando
├── .env.example           Variáveis de ambiente (copie para .env)
├── .github/workflows/     CI do backend (GitHub Actions)
├── .context/              Mapeamento de arquitetura e specs de hardening para produção (ver abaixo)
├── docs/                  Arquitetura, contrato da API e integração app ↔ API
├── scripts/smoke-test.ps1 Teste rápido de ponta a ponta contra a API em execução
└── entregas/              APK de depuração e capturas de tela geradas do app real
```

## Como rodar

### 1. Backend + banco (Docker)

```bash
cp .env.example .env        # opcional: tudo tem valor padrão de desenvolvimento
docker compose up --build
```

- API: <http://localhost:8080> · documentação interativa (Swagger): <http://localhost:8080/swagger-ui.html>
- Saúde: <http://localhost:8080/actuator/health>
- Na primeira subida, com o banco vazio, são criados os usuários de teste e dados de exemplo (`APP_SEED_ENABLED=true`).

| Perfil    | E-mail               | Senha    |
|-----------|----------------------|----------|
| Operador  | `vinicius@gmail.com` | `123456` |
| Gestor    | `anna@gmail.com`     | `123456` |
| Liderança | `gabriela@gmail.com` | `123456` |

Para recomeçar do zero (apaga o banco): `docker compose down -v && docker compose up --build`.

### 2. App Android

O app precisa saber o endereço da API. Ele já vem com um padrão para o emulador (`http://10.0.2.2:8080/`, que é o
computador que hospeda o emulador) e, **em build de debug**, o endereço pode ser trocado dentro do app, na tela de
login ("Servidor: …"), sem recompilar — útil para aparelho físico (`http://IP-DO-SEU-PC:8080/`, mesma rede Wi-Fi). Em
build de release essa opção fica escondida (trocar de servidor em produção seria um vetor de phishing) — o endereço
é fixado no momento do build via `-PapiBaseUrl=...`.

- **Android Studio:** abra a pasta `mobile/` e execute (▶). Para fixar outro endereço no build:
  `./gradlew installDebug -PapiBaseUrl=http://192.168.0.10:8080/`
- **APK pronto:** `entregas/aguia-inova-debug.apk` (instale com `adb install -r`).
- **Sem Android Studio (Docker):** `mobile/Dockerfile` compila, roda os testes unitários e exporta o APK. Ele só aceita
  os [termos do Android SDK](https://developer.android.com/studio/terms) se **você** passar
  `ACCEPT_ANDROID_SDK_LICENSES=yes`:

  ```bash
  cd mobile
  docker build --build-arg ACCEPT_ANDROID_SDK_LICENSES=yes \
      --build-arg API_BASE_URL=http://192.168.0.10:8080/ \
      --target artefatos --output type=local,dest=./saida .
  # -> ./saida/aguia-inova-debug.apk  e  ./saida/relatorio-testes
  ```
- HTTP sem TLS só é permitido nos builds de *debug* (`app/src/debug/AndroidManifest.xml`). Em produção use HTTPS.

#### Publicar um APK assinado (sem Play Store)

A Google Play Console cobra uma taxa única de US$25 — evitável para distribuição direta via GitHub Releases.

1. Gere sua própria keystore (guarde o `.jks` e a senha **fora do repositório**, nunca commitados):
   ```bash
   keytool -genkeypair -v -keystore aguia-inova-release.jks -alias aguia-inova \
     -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Builde o APK assinado, apontando para a API real:
   ```bash
   cd mobile
   export RELEASE_KEYSTORE_PATH="/caminho/para/aguia-inova-release.jks"
   export KEYSTORE_PASSWORD="sua-senha-da-keystore"
   export KEY_ALIAS="aguia-inova"
   export KEY_PASSWORD="sua-senha-da-keystore"
   ./gradlew assembleRelease -PapiBaseUrl=https://app-guia.onrender.com/
   # -> app/build/outputs/apk/release/app-release.apk
   ```
   (sem essas variáveis definidas, o build de release continua funcionando, só sai sem assinatura)
3. GitHub → `Releases` → `Draft a new release` → anexe o `.apk` → publique. Alternativa com
   atualização mais fácil para testadores: [Firebase App Distribution](https://firebase.google.com/docs/app-distribution) (grátis).

### 3. Testar

```bash
# Backend (Testcontainers sobe um MongoDB descartável; precisa do Docker rodando)
cd backend && ./mvnw verify

# Ponta a ponta contra a API em execução (login dos 3 perfis, permissões, CRUDs, dashboard, IA)
pwsh scripts/smoke-test.ps1 -Api http://localhost:8080

# App: testes unitários (regras, sessão, ViewModels) e de contrato com a API real
cd mobile && AGUIA_API_URL=http://localhost:8080/ ./gradlew testDebugUnitTest
```

> O teste de contrato do app cadastra dados de verdade; rode-o contra um ambiente descartável
> (`docker compose down -v && docker compose up`). Sem `AGUIA_API_URL`, ele é ignorado.

## Perfis e permissões

| Recurso | Operador | Gestor | Liderança |
|---|---|---|---|
| Cadastro / login / `me` | ✔ | ✔ | ✔ |
| **Estratégias** — consultar e ver histórico (id, data, categoria, campanha) | ✔ | ✔ | ✔ |
| Estratégias — criar / editar / excluir (com metas e período) | ✘ | ✘ | ✔ |
| Estratégias — **saúde** (em dia, em risco ou sem tração) | ✘ | ✔ | ✔ |
| **Ideias** — listar | só as próprias | todas | ✘ |
| Ideias ‑ aprovadas da empresa | ✔ | ✔ | ✔ |
| Ideias — criar / editar / excluir (enquanto *em análise*) | ✔ (só as suas) | ✘ | ✘ |
| Ideias — aprovar / rejeitar / priorizar | ✘ | ✔ | ✘ |
| **Projetos** — consultar (etapa, status, investimento, prazo, retorno) | ✘ | ✔ | ✔ |
| Projetos — criar / editar / progresso / resultados / excluir | ✘ | ✔ | ✘ |
| **Dashboard** e relatórios para gráficos | ✘ | ✘ | ✔ |
| Usuários — listar e mudar perfil | ✘ | ✘ | ✔ |
| **IA** — avaliar ideias, ranking, assistente (chat) | ✘ | ✔ | ✘ |
| IA — insights do dashboard | ✘ | ✘ | ✔ |
| IA — rascunho e crítica de estratégias | ✘ | ✘ | ✔ |
| IA — leitura da saúde de uma estratégia | ✘ | ✔ | ✔ |

As regras são aplicadas **no servidor** (`@PreAuthorize` + verificações de dono no serviço); o app apenas esconde o que o
perfil não pode usar. O perfil é relido do banco a cada requisição: rebaixar ou excluir um usuário vale na hora, sem
esperar o token expirar.

## IA (Sprint 2 — Inovação)

Seis recursos, todos com o **gestor/liderança como decisor** (a IA só sugere):

1. **Pontuação e priorização de ideias** — nota 0–100, alinhamento com a estratégia vigente, prioridade sugerida,
   pontos fortes e riscos. Sem IA disponível, cai automaticamente para uma **avaliação por regras** (marcada como
   `HEURISTICA`/"Regras" na tela), então o fluxo nunca fica parado.
2. **Assistente do gestor (chat)** — ajuda a escolher ideias e montar projetos com base nos dados reais. Sem IA
   configurada, o app avisa "IA não configurada no servidor" (não há resposta inventada).
3. **Insights do dashboard** — leitura executiva dos números; sem IA, gera uma leitura por regras.
4. **Rascunho de estratégia** (liderança) — a partir de um objetivo escrito livremente, a IA propõe título, descrição,
   categoria, metas mensuráveis e período; a liderança revisa antes de salvar. Não há substituto por regras: sem IA, o
   app avisa que ela está indisponível.
5. **Crítica de estratégia** (liderança) — antes de salvar, nota de 0 a 100 de maturidade (clareza, metas mensuráveis,
   período), melhorias e **sobreposição** com as demais estratégias vigentes; sem IA, o mesmo resultado sai por regras.
6. **Saúde da estratégia** (gestor e liderança) — cada estratégia vigente é classificada **por regras** (*em dia*,
   *em risco* ou *sem tração*) com os motivos e a próxima ação; a IA, sob demanda, explica o diagnóstico e recomenda o
   que fazer. A IA nunca altera o nível calculado.

Em produção (Render) as variáveis vão no painel do serviço: o passo a passo está em [`.context/deployment.md`](.context/deployment.md#ligando-a-ia-em-produção-passo-a-passo). Localmente, configure em `.env` (a chave fica **só no servidor**, nunca no app):

| Provedor | `AI_PROVIDER` | `AI_BASE_URL` | Chave |
|---|---|---|---|
| Google Gemini (gratuito) | `gemini` | *(vazio)* | <https://aistudio.google.com/apikey> |
| OpenRouter (modelos `:free`) | `openai` | `https://openrouter.ai/api/v1` | chave do OpenRouter |
| GitHub Models | `openai` | `https://models.github.ai/inference` | token do GitHub |

`AI_MODEL` muda com frequência entre provedores — confira a lista atual. Textos de usuários enviados à IA são tratados
como **dados**, não como instruções (delimitados e higienizados), e a saída do modelo é validada e limitada antes de ser
gravada ou mostrada.

## Segurança em resumo

JWT HS256 assinado com segredo do ambiente (mínimo de 32 bytes **e diferente de qualquer valor-padrão do repositório** —
a API se recusa a subir caso contrário), emissor validado, senhas com BCrypt (com *hash* fictício para não vazar por
tempo se o e-mail existe), erros de autenticação/autorização em JSON padronizado (401/403), sem *stack trace* nas
respostas, rate limiting em login e nos endpoints de IA, sessão do app criptografada (`EncryptedSharedPreferences`) e
fora do backup do Android, troca de servidor da API só disponível em build de debug, e desconexão automática quando a
API responde 401.

**Decisões que você deve conhecer**

- `APP_ALLOW_ROLE_SELECTION=true` (padrão) permite escolher o perfil no cadastro, como no app da Sprint 1 — conveniente
  para a demonstração, mas qualquer pessoa vira Liderança. Em produção use `false`: todo cadastro vira Operador e só a
  Liderança promove usuários (`PATCH /api/usuarios/{id}/perfil`).
- `APP_SEED_ENABLED` já é `false` por padrão na aplicação (fail-safe) — só semeia se você definir explicitamente
  `true` (é o que o `docker-compose.yml` de desenvolvimento faz). Nunca defina `true` em produção: os usuários de
  teste têm senha conhecida (`123456`).
- `RATE_LIMIT_LOGIN_POR_MINUTO`/`RATE_LIMIT_IA_POR_MINUTO` (padrão 10/min cada) limitam tentativas de login por
  e-mail e chamadas de IA por usuário — protegem contra força bruta e contra custo descontrolado de API de IA.
- `DASHBOARD_LIMITE_REGISTROS` (padrão 5000) é um teto de segurança nas agregações do dashboard, para nunca carregar
  uma coleção inteira na memória por requisição.
- O MongoDB do Compose não tem senha e só escuta em `127.0.0.1` — uso local. Em produção, use um MongoDB gerenciado
  (este projeto usa MongoDB Atlas — ver [`.context/specs/009-mongodb-atlas-migration.md`](.context/specs/009-mongodb-atlas-migration.md)).

Mapeamento completo da arquitetura, decisões e todo o trabalho de hardening para produção (com o que foi testado,
riscos conhecidos e o que ainda falta) está em [`.context/`](.context/STATUS.md) — útil tanto para humano quanto para
qualquer sessão de IA que for continuar o trabalho depois.

## Onde está cada pilar de POO

| Pilar | Onde ver |
|---|---|
| **Encapsulamento** | Entidades (`model/`) sem *setters*: o estado só muda por métodos com regra (`Ideia.avaliar`, `Projeto.registrarResultado`, …); objetos de parâmetro `Dados*` normalizam e validam antes de criar/alterar. |
| **Herança** | `DocumentoBase` (id e auditoria) → `Usuario`, `Estrategia`, `Ideia`, `Projeto`; `ApiException` → `NaoEncontradoException`, `ProibidoException`, `ConflitoException`, … (cada uma define o seu status HTTP); `BaseViewModel` no app. |
| **Polimorfismo** | `AvaliadorDeIdeias` e `GeradorDeInsights` têm implementação por IA e por regras, intercambiáveis; `LlmClient` tem adaptadores Gemini/OpenAI-compatível e um nulo; no app, os repositórios são interfaces (`IdeiaRepository`) com implementação remota e falsa nos testes. |
| **Abstração** | Interfaces de domínio (`LlmClient`, `AvaliadorDeIdeias`, `*Repository` do app) escondem o provedor, o banco e a rede; DTOs de entrada/saída (`dto/`) separam o contrato da API do modelo interno. |

Detalhes e diagramas em [`docs/ARQUITETURA.md`](docs/ARQUITETURA.md); contrato da API em [`docs/API.md`](docs/API.md);
como o app e a API se encaixam em [`docs/INTEGRACAO-APP.md`](docs/INTEGRACAO-APP.md).

## Observação sobre a stack

O enunciado cita "JPA/Hibernate" **e** MongoDB. JPA/Hibernate é para bancos relacionais e não funciona com MongoDB;
como o banco exigido é o MongoDB, a persistência usa **Spring Data MongoDB** (o equivalente no mundo de documentos:
repositórios, anotações de mapeamento e auditoria). Se for exigido JPA de fato, será preciso um banco relacional e
retrabalho em `model/` (mapeamento), `repository/` e nas consultas agregadas do `DashboardService`, que usam
`MongoTemplate`; controllers, DTOs e o app não mudam.
