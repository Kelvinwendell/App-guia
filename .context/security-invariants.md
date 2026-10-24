# Invariantes de segurança — Águia Inova

Regras que não podem regredir. Se uma mudança futura tocar nelas, trate como incidente, não
como refactor normal.

## Backend

| Regra | Onde é aplicada | Por quê |
|---|---|---|
| `JWT_SECRET` precisa ter ≥32 bytes **e** ser diferente dos valores-padrão conhecidos do repositório | `SecurityConfig.chaveJwt()` | Um deploy que esquece de definir o segredo não pode subir assinando tokens reais com um segredo público no GitHub. |
| `APP_SEED_ENABLED` tem default `false` na aplicação | `application.yml` | Esquecer essa variável em produção não pode, sozinho, criar contas com senha conhecida (`123456`) no banco. |
| Login é limitado por e-mail (`RATE_LIMIT_LOGIN_POR_MINUTO`, default 10/min) | `AuthService.login`, `security/LimitadorDeTaxa` | Proteção básica contra força bruta. **Conhecido e aceito:** não cobre `/api/auth/register` nem é por IP — ver `git-workflow.md`/histórico se for endurecer isso. |
| Chamadas de IA (`/api/ia/chat`, `/api/ia/dashboard/insights`) são limitadas por usuário (`RATE_LIMIT_IA_POR_MINUTO`, default 10/min) | `AssistenteIaService`, `InsightsIaService` | Cada chamada pode custar dinheiro num provedor de IA pago; sem limite, um usuário autenticado sozinho pode gerar custo sem teto. |
| Agregações do dashboard têm teto de leitura (`DASHBOARD_LIMITE_REGISTROS`, default 5000) | `DashboardService.todosComLimiteDeSeguranca` | Nunca carregar uma coleção inteira na memória por requisição; acima do teto, loga aviso explícito de que os totais não refletem 100% dos dados. |
| Texto de usuário nunca entra cru num prompt de IA | `util/Textos.paraPrompt`, `service/ia/PromptsIa` | Mitiga prompt injection — texto é delimitado em tags e sanitizado antes de ir para o LLM. |
| Erros de autenticação têm mensagem genérica, idêntica para e-mail inexistente ou senha errada | `AuthService.login` | Evita enumeração de conta por tempo de resposta ou mensagem. |
| Stack trace nunca vai para o cliente | `application.yml` (`server.error.include-*: never`), `ApiExceptionHandler` | Erro interno não pode vazar detalhe de implementação. |

## Mobile

| Regra | Onde é aplicada | Por quê |
|---|---|---|
| Troca de servidor da API só é visível/editável em build de debug | `ConfiguracaoServidor.editavel()`, gate em `LoginScreen.kt` | Em build de release, permitir apontar o app para um servidor arbitrário é um vetor de phishing de credenciais pela própria tela de login legítima do app. |
| Sessão (token + usuário) é gravada com `EncryptedSharedPreferences`, não `SharedPreferences` comum | `core/session/ArmazenamentoSessaoPrefs` | Token não pode ficar legível em texto plano num aparelho rooteado/com backup habilitado. |
| HTTP sem TLS só é permitido em build de debug | `app/src/debug/AndroidManifest.xml` (sem equivalente em `main`) | Build de release depende do padrão do Android (bloqueia texto plano) — não adicione uma exceção de `cleartextTraffic` fora do source set de debug. |

## O que ainda não está coberto (conhecido, não é descuido)

- Rate limit é por instância do processo (em memória) — se a API algum dia rodar em mais de
  uma instância, o limite deixa de ser global. Resolver com um contador compartilhado (Redis)
  quando/se isso acontecer.
- `/api/auth/register` não tem rate limit — mitigação recomendada é limitar por IP na borda
  (proxy/hospedagem), não na aplicação, já que não há IP disponível na camada de serviço sem
  uma mudança mais invasiva no controller.
- O usuário do banco no MongoDB Atlas de produção tem papel administrativo mais amplo do que
  a API precisa (criado automaticamente pelo fluxo de setup do Atlas). Troca por um usuário
  com papel restrito (`readWrite` só no banco da aplicação) é uma pendência de baixo risco,
  não bloqueante.
