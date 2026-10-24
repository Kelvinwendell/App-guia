# Deployment — Águia Inova

Como o backend está implantado em produção hoje, de graça.

## Topologia

```
App mobile → HTTPS → Render (Web Service, Docker, free tier) → TLS → MongoDB Atlas (M0, free tier)
```

- **API:** `https://app-guia.onrender.com` — builda `backend/Dockerfile` automaticamente a
  cada push na `main`. Plano gratuito "dorme" após 15 min sem tráfego (primeira requisição
  depois disso demora ~1 min).
- **Banco:** MongoDB Atlas, cluster `Cluster0`, banco `aguiainova`. Autenticação SCRAM + TLS
  obrigatórios (padrão do Atlas, não precisou de configuração extra).

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
