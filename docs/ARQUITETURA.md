# Arquitetura

## Visão geral

```
 ┌────────────────────┐  HTTPS/JSON + JWT   ┌──────────────────────────────┐        ┌───────────┐
 │  App Android       │ ──────────────────▶ │  API Spring Boot             │ ─────▶ │ MongoDB   │
 │  Compose · MVVM    │ ◀────────────────── │  camadas + Spring Security   │ ◀───── │           │
 └────────────────────┘                     └───────────────┬──────────────┘        └───────────┘
                                                            │ HTTPS (chave só no servidor)
                                                            ▼
                                             Gemini / OpenRouter / GitHub Models  (opcional)
```

Escolhi **arquitetura em camadas** no backend (o padrão que melhor cabe num CRUD com regras de negócio, papéis e
integrações) e **MVVM + Repository** no app (o padrão recomendado para Compose). Cada camada só conhece a de baixo,
sempre por interfaces quando há mais de uma implementação possível.

## Backend (`backend/src/main/java/br/com/fiap/aguiainova`)

| Pacote | Responsabilidade |
|---|---|
| `controller/` | HTTP: valida a entrada (`@Valid`), aplica o papel (`@PreAuthorize`) e delega. Sem regra de negócio |
| `service/` | Casos de uso e regras que envolvem mais de uma entidade (quem pode ver o quê, vínculos, histórico) |
| `service/ia/` | IA: avaliação de ideias e insights, com implementação por LLM, por regras e o *fallback* entre elas |
| `model/` | Entidades **ricas**: guardam o estado e as regras da própria entidade; sem *setters* |
| `repository/` | Acesso ao MongoDB (Spring Data) |
| `dto/request`, `dto/response` | Contrato da API, separado do modelo interno (records imutáveis) |
| `mapper/` | Entidade ⇄ DTO |
| `security/`, `config/` | JWT, filtros, propriedades, beans |
| `integration/ai/` | Clientes HTTP dos provedores de IA (adaptadores) |
| `exception/` | Hierarquia de erros de negócio → resposta HTTP padronizada |
| `bootstrap/` | Dados de exemplo (desligável) |

Fluxo de uma requisição: `Filtro JWT → controller → service → model/repository → mapper → DTO`. O filtro valida o token,
**carrega o usuário do banco** e monta a autoridade `ROLE_<PERFIL>`; `@PreAuthorize` barra o que o perfil não pode.
Regras de posse ("só a própria ideia") ficam no serviço, porque dependem do dado, não só do papel.

### Modelo de dados (coleções)

| Coleção | Conteúdo | Relações |
|---|---|---|
| `usuarios` | nome, e-mail único, senha (BCrypt), perfil, cargo, departamento, unidade | — |
| `estrategias` | título, descrição, categoria, campanha, status (vigente/encerrada), **metas** (até 8), **início/fim** do período | — |
| `estrategias_historico` | **registro imutável** a cada criação/alteração/remoção: id, data, categoria, campanha, ação, autor | `estrategiaId` |
| `ideias` | título, descrição, categoria, status, prioridade, parecer, `autor` (embutido), `avaliacaoIa` (embutida) | `estrategiaId` |
| `projetos` | nome, investimento, prazo, etapa, status, progresso, lista de `resultados` (embutida) | `estrategiaId`, `ideiaId` |

Valores monetários são `BigDecimal` gravado como `Decimal128`; datas simples, como texto ISO (`yyyy-MM-dd`); auditoria
(`criadoEm`/`atualizadoEm`) automática. ROI, lucro, dias para o prazo e atraso são **calculados** pelo modelo
(`ProjetoMetricas`), nunca gravados — não há como ficarem desatualizados.

### Padrões usados (e por quê)

| Padrão | Onde | Motivo |
|---|---|---|
| Camadas / Repository | todo o backend | separar HTTP, regra e dados |
| Objeto de parâmetro (`Dados*`) | `DadosIdeia`, `DadosProjeto`… | normalizar e validar entrada em um só lugar, antes de tocar a entidade |
| Strategy | `AvaliadorDeIdeias`, `GeradorDeInsights`, `AnalisadorDeEstrategias`, `LeitorDeSaude` | trocar "IA" por "regras" sem mudar quem usa |
| Decorator (contingência) | `AvaliadorComContingencia`, `GeradorInsightsComContingencia`, `AnalisadorEstrategiasComContingencia`, `LeitorSaudeComContingencia` | se a IA falhar, cai nas regras e marca a origem (`HEURISTICA`) |
| Adapter | `GeminiLlmClient`, `OpenAiCompatibleLlmClient` atrás de `LlmClient` | um contrato, vários provedores |
| Template Method | `ClienteLlmHttp<R>` | o esqueleto (chamada, timeout, erros) é comum; só o formato muda |
| Null Object | `LlmDesabilitadoClient` | sem chave configurada, o resto do código não precisa de `if` |
| Herança | `DocumentoBase`, `ApiException` (cada erro define o seu status) | reaproveitar identidade/auditoria e o mapeamento erro → HTTP |

### Segurança

- JWT HS256 (Nimbus / Spring Security resource server), segredo ≥ 32 bytes (a API **não sobe** com menos), emissor validado.
- Senhas com BCrypt; no login, o custo é pago mesmo se o e-mail não existe (sem vazar por tempo).
- 401/403 em JSON padronizado; sem *stack trace*; CSRF desligado por ser API *stateless* com Bearer.
- Papéis lidos do banco a cada requisição (rebaixar/excluir vale na hora).
- **Prompt injection**: texto de usuário vai para a IA como dado delimitado e higienizado; a resposta é validada
  (faixas de nota, enums, tamanho) antes de gravar; a chave do provedor só viaja em cabeçalho HTTP, nunca na URL.
- Em produção: `APP_SEED_ENABLED=false`, `APP_ALLOW_ROLE_SELECTION=false`, `JWT_SECRET` próprio, HTTPS na frente, MongoDB com senha.

### Como a IA se encaixa

```
IdeiaController/IaController ─▶ AvaliacaoIaService ─▶ AvaliadorDeIdeias  (@Primary = AvaliadorComContingencia)
                                                        ├─ AvaliadorLlm ─▶ LlmClient ─▶ Gemini | OpenAI-compat | (desligado)
                                                        └─ AvaliadorHeuristico  (regras locais, sempre disponível)
```

A nota da IA é **triagem para o gestor** (visível só a ele); quem aprova é sempre uma pessoa.

#### IA nas estratégias

```
EstrategiaIaService ─┬─ rascunho  ─▶ LlmClient (sem regras: 503 se não há IA)
                     ├─ crítica   ─▶ AnalisadorDeEstrategias (@Primary = …ComContingencia: Llm | Heurístico)
                     └─ leitura   ─▶ LeitorDeSaude          (@Primary = …ComContingencia: Llm | Heurístico)
                                         ▲
EstrategiaSaudeService ─▶ SaudeEstrategia.avaliar(IndicadoresEstrategia)   ← regras, sem IA, testáveis
```

A **saúde** é uma decisão do sistema, não da IA: `SaudeEstrategia` (em `model/`, como `ProjetoMetricas`) aplica as regras
sobre `IndicadoresEstrategia` e devolve nível, motivos e próxima ação. A IA só **explica** esse resultado
(`PromptsIa.leituraDeSaude` o envia junto e manda não contradizê-lo). Assim o número que a pessoa vê e o que o modelo lê
são os mesmos, e o painel funciona sem chave de API. O rascunho respeita os limites de `EstrategiaRequest` (o texto do
modelo é truncado em `InterpretadorRespostaIa`), e ids de sobreposição inventados pelo modelo são descartados.

## App Android (`mobile/app/src/main/java/br/com/fiap/aguiainova`)

| Pacote | Responsabilidade |
|---|---|
| `ui/*` | Telas Compose por funcionalidade (`auth, home, ideias, estrategias, projetos, dashboard, ia, perfil`), `components/` (peças reutilizáveis) e `theme/` |
| `ui/*/…ViewModel` | Estado da tela (`Estado`: carregando · erro · pronto), avisos e ações; herdam de `BaseViewModel` |
| `domain/model`, `domain/repository` | Modelos e **interfaces** de repositório — a tela só conhece isto |
| `data/remote/api`, `data/repository` | Interfaces Retrofit e implementações remotas (`…RepositoryRemoto`) |
| `core/network` | `ApiClient`, `AuthInterceptor` (Bearer), `BaseUrlInterceptor` (servidor trocável em tempo de execução), `LeitorDeErroApi` (HTTP → mensagem), `Resultado` |
| `core/session` | Sessão (token + usuário) e endereço do servidor, fora do backup do Android |
| `core/di` | `AppContainer`: injeção manual, sem framework, num único lugar |
| `navigation` | `NavGraph` e rotas; a barra inferior muda por perfil |
| `ui/components/Abas.kt` | `AbasDaTela`: abas que dividem **uma** tela (a barra inferior troca de tela; as abas, não) |

**Navegação por perfil.** Barra inferior — Operador: Início · Ideias · Estratégia · Perfil; Gestor: Início · Ideias ·
Projetos · **Direção** · Perfil; Liderança: Início · Estratégia · Painel · Projetos · Perfil. A tela de estratégias é a mesma
(`estrategias?aba=`): abas *Vigentes · Encerradas · Histórico* para todos e, para o gestor, a aba *Assistente* (chat de IA)
na frente, com título "Direção". O Painel da liderança tem as abas *Resumo · Estratégias · Projetos · Insights IA*, e as
Ideias do operador, *Minhas ideias · Aprovadas da empresa*.

Decisões: `Resultado` (sucesso/falha) em vez de exceções nas fronteiras; 401 em qualquer chamada encerra a sessão e volta
ao login; ao atualizar uma lista que já está na tela, uma falha **não apaga** o que o usuário via — só avisa.
Removi o Firebase/Room da Sprint 1: a fonte da verdade agora é a API.

## Infraestrutura

`docker-compose.yml` sobe `mongo:7.0` (com *healthcheck*) e a API (imagem multi-estágio, JRE sem root, *healthcheck* em
`/actuator/health`). Configuração 100% por variáveis de ambiente (`.env.example`). O app tem seu próprio `Dockerfile`
para compilar/testar/exportar o APK sem Android Studio.
