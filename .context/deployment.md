# Deployment — Águia Inova

Como o backend está implantado em produção hoje, de graça.

## Topologia

```
App mobile → HTTPS → Render (Web Service, Docker, free tier) → TLS → MongoDB Atlas (M0, free tier)
```

- **API:** `https://app-guia.onrender.com` — builda `backend/Dockerfile`. Plano gratuito
  "dorme" após 15 min sem tráfego (primeira requisição depois disso demora ~1 min).
- **Banco:** MongoDB Atlas, cluster `Cluster0`, banco `aguiainova`. Autenticação SCRAM + TLS
  obrigatórios (padrão do Atlas, não precisou de configuração extra). Índices são criados/
  atualizados sozinhos a cada subida da API (`auto-index-creation: true` em
  `application.yml`) — não existe passo de migração manual para o Mongo.

## Deploy: só depois que o CI passa

O job `deploy` em `.github/workflows/backend-ci.yml` roda só em push na `main` e só depois que
o job `test` passa (`needs: test`). Ele chama o **Deploy Hook** do Render (dispara o build
remotamente) e depois faz polling em `/actuator/health` até a API responder `UP` (confirma
também que a conexão com o Mongo está saudável, já que o health check do Spring agrega o
indicador do Mongo).

**Configuração manual necessária (uma vez só, feita por quem tem acesso aos paineis):**

1. No Render: **Settings → Deploy Hook** → copiar a URL.
2. No GitHub: **Settings → Secrets and variables → Actions → New repository secret** →
   nome `RENDER_DEPLOY_HOOK_URL`, valor a URL copiada.
3. No Render: **Settings → Auto-Deploy** → desligar. Sem isso, o Render continua
   re-deployando sozinho a cada push (inclusive um com teste quebrado) em paralelo ao job
   do GitHub Actions — os dois dispararem juntos não quebra nada, mas é build duplicado e
   anula o propósito de só subir depois que o CI passa.

## Variáveis de ambiente de produção

Definidas no painel do Render (nunca no código, nunca versionadas):

| Variável | Valor |
|---|---|
| `MONGODB_URI` | connection string do Atlas, formato `mongodb+srv://usuario:senha@cluster.../aguiainova?retryWrites=true&w=majority` |
| `JWT_SECRET` | gerado com `openssl rand -base64 48` — nunca um valor-padrão do repositório |
| `JWT_EXPIRATION_MINUTES` | `480` |
| `APP_SEED_ENABLED` | `false` (ou omitida — esse já é o default da aplicação) |
| `APP_ALLOW_ROLE_SELECTION` | `false` em produção |
| `AI_ENABLED`, `AI_PROVIDER`, `AI_API_KEY`, `AI_MODEL` | conforme o provedor de IA escolhido (ver README) |

## Ligando a IA em produção (passo a passo)

Sem `AI_API_KEY` a API **não chama nenhum provedor**: notas, insights, crítica e saúde de estratégias caem nas regras
locais (marcadas "Regras") e o chat e o rascunho de estratégia respondem 503. A chave fica só no Render.

1. Criar a chave grátis do Gemini em <https://aistudio.google.com/apikey> (os limites do plano gratuito aparecem em
   <https://aistudio.google.com/rate-limit>; valem por projeto, não por chave).
2. No Render: serviço `app-guia` → **Environment** → definir `AI_ENABLED=true`, `AI_PROVIDER=gemini`,
   `AI_API_KEY=<a chave>` e, opcionalmente, `AI_MODEL=gemini-3.6-flash` (já é o padrão; `gemini-3.8-flash` também
   existe) → salvar. O Render reinicia o serviço sozinho.
3. Esperar o serviço voltar e conferir `/actuator/health` (`UP`).
4. No app, entrar como **Gestor** → **Direção › Assistente**: o selo verde "IA conectada · gemini (...)" confirma. Como
   **Liderança**, **Estratégia › Nova** → "Gerar rascunho" também só funciona com a IA ligada.
5. Se o selo disser "IA não configurada no servidor", a chave não chegou (variável com nome errado, vazia ou serviço
   não reiniciado). Se o selo for verde mas as notas aparecerem como "Regras", o provedor está recusando as chamadas
   (chave inválida, modelo inexistente ou cota esgotada): veja os logs do Render (`IA indisponível ...`).

Nunca coloque a chave no repositório, em issue/PR ou em conversa: só no painel do Render (e, para testes locais, num
`.env` que o `.gitignore` já ignora).

## Problemas reais já encontrados e resolvidos (para quem repetir esse processo)

1. **MongoDB Atlas só libera por padrão o IP de quem criou o cluster** na "IP Access List".
   O Render, no plano gratuito, não tem IP de saída fixo — a conexão era recusada antes
   mesmo da API conseguir abrir a porta (sintoma: deploy falha com "Exited with status 1" /
   "no open ports detected", sem erro óbvio). Resolvido liberando `0.0.0.0/0` em
   **Database & Network Access → Network Access → IP Access List** no Atlas. Como a conexão
   já exige usuário/senha + TLS, isso não é o mesmo risco de um banco sem senha aberto.
2. **Essa entrada `0.0.0.0/0` é criada como temporária por padrão** no fluxo de setup do
   Atlas (expira em algumas horas/dias). Se expirar sozinha, o deploy volta a falhar sem
   aviso. Edite a entrada e desligue a expiração.
3. **Sandbox de desenvolvimento sem acesso de rede a `mongodb.net`/`onrender.com`:** se uma
   sessão de IA estiver rodando num ambiente com rede restrita (lista de domínios liberados),
   ela não consegue validar a conexão diretamente — precisa pedir para o dono do projeto
   rodar o `curl` e colar o resultado. Isso não indica problema real no deploy, só uma
   limitação do ambiente de quem está ajudando a configurar.

## Teste rápido de que está tudo no ar

```bash
curl https://app-guia.onrender.com/actuator/health
# esperado: {"groups":["liveness","readiness"],"status":"UP"}
```
