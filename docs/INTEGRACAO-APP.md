# Integração app ↔ API

O app não tem dados próprios: tudo o que aparece vem da API (sem mocks) e tudo o que o usuário faz vira uma chamada.

## Login, sessão e servidor

1. `POST /api/auth/login` → `accessToken` + dados do usuário. O app guarda o token (SharedPreferences, **fora do backup**
   do Android) e abre a Home do perfil (`operador`, `gestor` ou `liderança`).
2. `AuthInterceptor` coloca `Authorization: Bearer …` em toda chamada. Se a API responder **401**, a sessão é encerrada e o
   app volta ao login com uma mensagem.
3. O endereço da API é resolvido **a cada requisição** (`BaseUrlInterceptor`), então pode ser trocado na tela de login
   ("Servidor: …") sem recompilar: emulador `http://10.0.2.2:8080/`, aparelho físico `http://IP-DO-PC:8080/`.

## Tela → endpoints

| Tela | Perfil | Chamadas principais |
|---|---|---|
| Login / Cadastro | todos | `POST /api/auth/login`, `POST /api/auth/register` |
| Home | O · G · L | O: `/api/ideias` (totais) · G: `/api/ideias` + `/api/projetos` · L: `/api/dashboard/resumo` |
| Ideias (minhas / aprovadas da empresa) | O | `GET /api/ideias`, `GET /api/ideias/aprovadas` |
| Nova/Editar ideia | O | `POST /api/ideias`, `PUT /api/ideias/{id}`, `DELETE /api/ideias/{id}` (+ `GET /api/estrategias?status=VIGENTE` para vincular) |
| Ideias (triagem) | G | `GET /api/ideias`, `PATCH /api/ideias/{id}/avaliacao`, `POST /api/ia/ideias/avaliar-pendentes`, `POST /api/ia/ideias/{id}/avaliar` |
| Projetos | G · L | `GET /api/projetos`; G também `POST/PUT/PATCH/DELETE` |
| Detalhe do projeto | G · L | `GET /api/projetos/{id}`; G: `PATCH …/progresso`, `POST/DELETE …/resultados` |
| Assistente de IA | G | `GET /api/ia/status`, `POST /api/ia/chat` |
| Estratégias | todos leem · L edita | `GET /api/estrategias`; L: `POST/PUT/DELETE` |
| Histórico de estratégias | L | `GET /api/estrategias/historico?categoria=&campanha=` |
| Dashboard | L | `/api/dashboard/resumo`, `/estrategias`, `/evolucao` e `POST /api/ia/dashboard/insights` |

A barra inferior e os botões mudam conforme o perfil, mas **quem garante a regra é o servidor**: mesmo um app adulterado
receberia 403.

## Erros e estados de tela

`LeitorDeErroApi` traduz a resposta em mensagem legível (campo inválido, sem permissão, servidor fora do ar, sem rede…).
Cada tela mostra um destes estados: **carregando**, **erro** (com "Tentar novamente") ou **pronto**. Se uma atualização
falha com dados já na tela, eles permanecem e aparece só um aviso.

## Mantendo os dois lados em sincronia

- Os modelos do app (`domain/model`) espelham os DTOs de resposta da API pelo **nome dos campos**. Renomear um campo no
  backend sem acertar o app quebraria a leitura — por isso existe o **teste de contrato**
  (`ContratoComApiTest`), que roda os repositórios reais do app contra a API em execução e falha se algum JSON deixar de
  virar o modelo esperado (login, ideias, estratégias + histórico, projetos + resultados, dashboard e IA).
- O backend tem testes de integração por controller e por perfil (matriz de permissões) com um MongoDB real via
  Testcontainers; `scripts/smoke-test.ps1` repete o essencial contra o que estiver no ar.
- `CapturasDeTelaTest` abre o app de verdade na JVM (Robolectric), entra na API, navega clicando e salva as telas em PNG
  (`entregas/capturas/`). Serve como teste de interface de ponta a ponta e já apontou defeitos reais (lista que "pulava"
  ao priorizar com IA, rótulo colado no valor).

## Regerar as capturas de tela

Com a API **recém-populada** (o teste de contrato acrescenta registros):

```bash
docker compose down -v && docker compose up -d --build
cd mobile
AGUIA_API_URL=http://localhost:8080/ AGUIA_CAPTURAS_DIR=$PWD/build/capturas \
  ./gradlew testDebugUnitTest --tests '*CapturasDeTelaTest*'
```
