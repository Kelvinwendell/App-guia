# SPEC-009: Migrar o banco para MongoDB Atlas (camada gratuita)

## Status
BLOCKED (requer ação externa — conta no MongoDB Atlas)

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
- [ ] Criar conta em https://www.mongodb.com/cloud/atlas/register
- [ ] Criar um cluster M0 (gratuito) — escolher a região mais próxima do servidor onde a API
      vai rodar (SPEC-010).
- [ ] Criar um usuário de banco com senha forte (não reaproveitar senha de outra conta).
- [ ] Liberar acesso de rede: para o servidor da SPEC-010 (ou `0.0.0.0/0` temporariamente,
      trocando depois pelo IP fixo do serviço de hospedagem, se ele tiver um).
- [ ] Copiar a connection string (`mongodb+srv://usuario:senha@cluster.../aguiainova`) e
      configurar como variável de ambiente `MONGODB_URI` no serviço de hospedagem (nunca no
      código, nunca no `.env` commitado).
- [ ] Confirmar a conexão: subir a API apontando pra essa URI e rodar
      `curl $URL_DA_API/actuator/health` esperando `{"status":"UP"}`.

## Resultado
(preencher quando o checklist acima for executado pelo dono do projeto; eu posso ajudar a
validar a conexão quando a connection string estiver disponível, sem ver a senha em claro se
preferir passá-la só como variável de ambiente no serviço de hospedagem)
