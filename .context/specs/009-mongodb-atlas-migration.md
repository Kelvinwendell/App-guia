# SPEC-009: Migrar o banco para MongoDB Atlas (camada gratuita)

## Status
DONE

## Prioridade
CRÍTICO

## Execução
**Não autônoma.** Requer que o dono do projeto crie uma conta e um cluster. Eu não posso
criar contas de terceiros em nome de ninguém.

## Problema
`docker-compose.yml` sobe o Mongo sem autenticação, só acessível em `127.0.0.1`. Não existe
hoje nenhum caminho de produção real para o banco (sem TLS, sem usuário/senha, sem backup
gerenciado).

## Por que resolve sem custo
MongoDB Atlas tem uma camada **M0 gratuita para sempre** (512MB, suficiente para um projeto
estudantil), já vem com autenticação (SCRAM) e TLS obrigatórios por padrão — resolve o furo
de segurança sem precisar configurar nada manualmente de criptografia.

## Checklist de execução (manual, pelo dono do projeto)
- [x] Criar conta em https://www.mongodb.com/cloud/atlas/register
- [x] Criar um cluster M0 (gratuito).
- [x] Usuário de banco criado (fluxo automático do Atlas: `kelvinwendellbarboza_db_user`,
      papel `atlasAdmin` — mais amplo que o necessário, ver nota abaixo).
- [x] Acesso de rede liberado: `0.0.0.0/0` na IP Access List (necessário porque o Render,
      no plano gratuito, não tem IP de saída fixo).
- [x] Connection string configurada como `MONGODB_URI` no Render (nunca commitada).
- [x] Conexão confirmada: `curl https://app-guia.onrender.com/actuator/health` →
      `{"status":"UP"}`.

## Resultado
Cluster `Cluster0`, host `cluster0.6dtkj0d.mongodb.net`, banco `aguiainova`. Conexão validada
de ponta a ponta através do Render em produção.

**Obstáculo real encontrado**: a entrada de IP `0.0.0.0/0` foi criada pelo Atlas como
temporária por padrão (expirava em algumas horas) — precisou editar e desligar a expiração,
senão o deploy voltaria a falhar sozinho depois.

**Pendência de segurança de baixo risco, não bloqueante**: o usuário do banco tem papel
`atlasAdmin` (acesso total ao projeto Atlas), mais do que a API precisa (só `readWrite` no
banco `aguiainova`). Troca opcional e de baixo risco para depois: criar um usuário novo em
Database Access com papel restrito e atualizar `MONGODB_URI` no Render.
