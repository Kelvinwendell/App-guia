# SPEC-010: Hospedar a API de graça (Render free tier)

## Status
BLOCKED (requer ação externa — conta no Render e autorização de acesso ao repositório)

## Prioridade
CRÍTICO

## Execução
**Não autônoma.** Requer conta do dono do projeto. Eu posso preparar o arquivo de
configuração (`render.yaml`) e revisar o Dockerfile, mas não posso criar a conta nem conectar
o repositório.

## Problema
Hoje o sistema só roda localmente via `docker compose`. Não há nenhum lugar público para a
API rodar.

## Por que resolve sem custo
Render.com tem um *free tier* de Web Service: builda o `Dockerfile` automaticamente a partir
do GitHub, dá uma URL HTTPS pública, 750h/mês gratuitas (suficiente pra um serviço só).
Limitação conhecida: o serviço "dorme" depois de 15 min sem tráfego no free tier, e demora
~1 min pra "acordar" na próxima requisição — aceitável pra um projeto estudantil, não para um
produto comercial real.

## Checklist de execução (manual, pelo dono do projeto)
- [ ] Criar conta em https://render.com (pode entrar com GitHub).
- [ ] "New Web Service" → conectar o repositório `Kelvinwendell/App-guia`.
- [ ] Root directory: `backend/` (o Render builda pelo `backend/Dockerfile`).
- [ ] Definir variáveis de ambiente no painel do Render (nunca no código):
      `MONGODB_URI` (da SPEC-009), `JWT_SECRET` (gerado com `openssl rand -base64 48`,
      **nunca** o valor-padrão do repositório — ver SPEC-001), `JWT_EXPIRATION_MINUTES`,
      `APP_SEED_ENABLED=false`, `APP_ALLOW_ROLE_SELECTION=false`, `AI_ENABLED`, `AI_PROVIDER`,
      `AI_API_KEY`, `AI_MODEL`.
- [ ] Deploy e validar: `curl https://<nome-do-servico>.onrender.com/actuator/health`.

## Resultado
(preencher quando o deploy for feito; posso ajudar revisando logs de build/erro se algo
falhar, e preparar o `render.yaml` antes, se quiser)
