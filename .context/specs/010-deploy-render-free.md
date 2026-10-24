# SPEC-010: Hospedar a API de graça (Render free tier)

## Status
DONE

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
- [x] Criar conta em https://render.com (entrou com GitHub).
- [x] "New Web Service" → conectado o repositório `Kelvinwendell/App-guia`.
- [x] Root directory: `backend`.
- [x] Variáveis de ambiente definidas no painel do Render.
- [x] Deploy e validar.

## Resultado
No ar em **https://app-guia.onrender.com**. `curl .../actuator/health` →
`{"groups":["liveness","readiness"],"status":"UP"}`, confirmado direto do terminal do dono do
projeto (este sandbox não alcança `onrender.com`, mesma restrição de rede que já bloqueou o
teste direto do Atlas — não impediu o deploy, só o teste automatizado daqui).

**Dois obstáculos reais encontrados e resolvidos no caminho** (documentados aqui porque
alguém repetindo esse processo vai tropeçar nos dois):
1. Primeiro deploy falhou (`Exited with status 1`, "no open ports detected") — causa: o
   MongoDB Atlas, por padrão, só libera o IP de quem criou o cluster na "IP Access List". O
   Render não tem IP fixo no plano gratuito, então a conexão era recusada antes mesmo da API
   conseguir abrir a porta. Corrigido adicionando `0.0.0.0/0` na IP Access List do Atlas
   (Database & Network Access → Network Access → IP Access List → Add IP Address → Allow
   Access from Anywhere).
2. Essa entrada `0.0.0.0/0` foi criada como **temporária** (expira em algumas horas/dias por
   padrão) — precisou editar a entrada e desligar a opção de expiração, senão o acesso cairia
   sozinho depois.

PR #2 (`claude/finalize-system-repo-7t57wz` → `main`) mesclado antes do deploy, então a
`main` já carrega todas as correções de produção (SPEC-001 a 006, 012).
