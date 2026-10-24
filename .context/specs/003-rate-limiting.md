# SPEC-003: Rate limiting em login/registro e endpoints de IA

## Status
DONE (com escopo reduzido — ver Resultado)

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
Implementado: `security/LimitadorDeTaxa` (janela fixa em memória, chave→contagem,
`ConcurrentHashMap`, sem dependência nova nem serviço externo — adequado para 1 instância).
Nova exceção `LimiteDeRequisicoesExcedidoException` (429), encaixa no handler existente sem
mudança nenhuma (prova o valor do `ApiException` polimórfico já elogiado na auditoria).
Nova propriedade `app.rate-limit.{login-por-minuto,ia-por-minuto}` (configurável via env var,
default 10/min em produção).

Aplicado em:
- `AuthService.login` — por e-mail normalizado, antes de checar a senha.
- `AssistenteIaService.conversar` e `InsightsIaService.gerar` — por id do usuário autenticado
  (threadeado do controller via `@AuthenticationPrincipal UsuarioLogado`, sem acoplar o
  serviço ao `SecurityContextHolder`).

RED confirmado em `LimitadorDeTaxaTest` (unitário, com um `Clock` controlável à mão — mesmo
padrão de `AvaliadorHeuristicoTest`) e `RateLimitingIT` (integração, 3 cenários) antes da
implementação. GREEN depois. Efeito colateral do novo campo em `AppProperties` (record):
4 pontos de construção manual em `Dados.java`/`SecurityConfigTest.java` precisaram do 5º
argumento — corrigidos antes da regressão fechar.

Suíte completa: 267/267 verdes (260 anteriores + 7 novos testes desta spec e da SPEC-001/002).

**Escopo reduzido conscientemente, documentado aqui para não ser esquecido:**
- `/api/auth/register` **não** tem limite — limitar por e-mail não impede spam de cadastro
  com e-mails diferentes, e não há IP disponível na camada de serviço sem mudar a assinatura
  do controller de forma mais invasiva. Mitigação recomendada: limitar por IP na camada de
  borda (proxy/hospedagem), não no código da aplicação — ver nota em SPEC-010.
- O limitador é por instância do processo (em memória). Se a API algum dia rodar em mais de
  uma instância, o limite deixa de ser global (cada instância conta separado). Para 1
  instância (cenário real deste projeto hoje), não é um problema.
