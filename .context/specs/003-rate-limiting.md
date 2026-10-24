# SPEC-003: Rate limiting em login/registro e endpoints de IA

## Status
PENDING

## Prioridade
CRÍTICO

## Execução
Autônoma (código local, backend)

## Problema
`/api/auth/login` e `/api/auth/register` são públicos e sem nenhuma proteção contra força
bruta. `/api/ia/chat` e `/api/ia/dashboard/insights` não têm limite de chamadas por
usuário — qualquer conta autenticada pode gerar custo real de API de IA sem limite (só a
avaliação em lote de ideias tem um teto de 20).

## Critério de aceite
- [ ] Mais de N tentativas de login por minuto do mesmo IP/e-mail retornam `429 Too Many
      Requests` em vez de processar a tentativa.
- [ ] Mais de M chamadas de IA (chat + insights) por usuário por minuto retornam `429`.
- [ ] Limites configuráveis via `application.yml` (não hardcoded), com defaults razoáveis
      para o tamanho do projeto (ex.: 10 tentativas de login/min por IP, 10 chamadas de
      IA/min por usuário).
- [ ] Não usa nenhum serviço externo pago — biblioteca em memória (Bucket4j), compatível
      com instância única (não é multi-instância ainda, então não precisa de Redis).

## Plano TDD
1. **RED**: teste de integração que faz N+1 requisições de login em sequência rápida e espera
   `429` na última; outro teste equivalente para um endpoint de IA. Confirmar que FALHA hoje
   (hoje nunca retorna 429).
2. **GREEN**: adicionar dependência `bucket4j` (ou implementação simples em memória com
   `ConcurrentHashMap` + janela deslizante, se preferir zero dependência nova) e um filtro/
   interceptor que aplica o limite nos paths relevantes.
3. **REGRESSÃO**: suíte completa verde, incluindo os testes de fluxo normal (poucas
   requisições) que não devem ser bloqueados.

## Resultado
(preencher ao final do ciclo TDD, com números reais de execução)
