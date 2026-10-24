# SPEC-007: Travar a troca de servidor fora de build de debug

## Status
DONE (código) / NÃO VERIFICADO (sem execução de teste — ver Resultado)

## Prioridade
CRÍTICO

## Execução
Autônoma (código local, mobile) — **mas não executável em teste automatizado neste sandbox**
(ver limitação de Android Gradle Plugin em `architecture.md`). Implementação + teste serão
escritos seguindo TDD, mas a validação "GREEN" precisa rodar em CI ou na máquina do dono do
projeto, não aqui.

## Problema
A tela de login (`ui/auth/LoginScreen.kt`) permite digitar um servidor de API diferente, e
isso está disponível também no build de produção (`release`), não só em `debug`. É um vetor
de phishing: um app malicioso ou um dispositivo comprometido pode reapontar o app para um
backend falso e capturar credenciais pela tela de login legítima do próprio app.

## Critério de aceite
- [ ] Em build `release`, o campo de troca de servidor não aparece (ou aparece mas é
      somente leitura, mostrando o servidor fixo de produção).
- [ ] Em build `debug`, continua funcionando como hoje (conveniência para desenvolvimento).
- [ ] `ConfiguracaoServidorPrefs` continua existindo para debug, mas não é alcançável pela UI
      em release.

## Plano TDD
1. **RED**: ajustar/adicionar teste em `ConfiguracaoServidorTest.kt` que verifica que, quando
   `BuildConfig.DEBUG == false`, o componente de configuração de servidor não permite
   alteração (ex.: `definirServidor()` é ignorado, ou a função correspondente na UI não é
   exposta). Confirmar que o teste falha com o código atual.
2. **GREEN**: em `LoginScreen.kt`, envolver o campo de troca de servidor em
   `if (BuildConfig.DEBUG) { ... }`; garantir que `ConfiguracaoServidor` em release sempre
   resolve para a URL de produção fixa (via `BuildConfig` ou `local.properties` no momento do
   build, como já existe para `apiBaseUrl` segundo o README).
3. **REGRESSÃO**: rodar `./gradlew testDebugUnitTest` — **não disponível neste sandbox**
   (plugin Android não resolve). Marcar a spec como `DONE (verificação pendente em CI/local)`
   em vez de `DONE`, até que alguém com ambiente Android confirme.

## Resultado
Implementado: `ConfiguracaoServidor.editavel(): Boolean` (novo método na interface);
`ConfiguracaoServidorPrefs` ganhou parâmetro de construtor `editavel: Boolean = BuildConfig.DEBUG`
(default preserva compatibilidade com `AppContainer`, que não precisou mudar).
`AuthViewModel.servidorEditavel` expõe isso; `LoginScreen.kt` envolve o botão "Servidor: ..." e
o diálogo de edição em `if (vm.servidorEditavel)`.

Teste escrito seguindo o precedente já usado no projeto (`CapturasDeTelaTest` usa
`@RunWith(RobolectricTestRunner::class)` + `ApplicationProvider.getApplicationContext()` para
instanciar classes que dependem de `Context` em JVM pura): `ConfiguracaoServidorEditavelTest`,
2 casos (constrói com `editavel=true`/`false` explícito, sem depender de `BuildConfig.DEBUG`
na asserção, para o teste não depender de qual variante foi compilada).

**Não consegui confirmar RED nem GREEN nesta sessão**: tentei de novo `./gradlew
compileDebugKotlin` depois do Docker ter sido religado (rede mudou de porta) e o Android
Gradle Plugin continua não resolvendo neste sandbox (mesmo erro de antes, ver
`architecture.md`). Revisei o diff manualmente por sintaxe/consistência (chaves balanceadas,
import novo usado, assinatura do construtor compatível com a chamada existente em
`AppContainer`), mas isso não substitui compilar de verdade. Precisa ser confirmado em CI
(quando o job mobile existir) ou localmente por quem tiver Android SDK.
