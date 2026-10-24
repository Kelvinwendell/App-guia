# SPEC-008: Sessão (JWT) criptografada no armazenamento do app

## Status
DONE (código) / NÃO VERIFICADO — risco maior que a SPEC-007, ver Resultado

## Prioridade
IMPORTANTE

## Execução
Autônoma (código local, mobile) — mesma limitação de validação da SPEC-007.

## Problema
`ArmazenamentoSessaoPrefs` guarda o token JWT em `SharedPreferences` comum — legível em texto
puro num dispositivo rooteado/debugável, apesar de já estar fora do backup do Android.

## Critério de aceite
- [ ] Token e dados do usuário passam a ser gravados via `EncryptedSharedPreferences`
      (Jetpack Security / `androidx.security:security-crypto`).
- [ ] Comportamento funcional idêntico (login, leitura, limpar sessão) do ponto de vista dos
      testes existentes de `GerenciadorSessaoTest`.

## Plano TDD
1. **RED**: os testes de `GerenciadorSessaoTest`/`ArmazenamentoSessaoEmMemoria` continuam
   sendo o contrato — eles usam uma implementação fake, então não mudam. O teste novo é de
   integração mínima confirmando que o arquivo de preferências gravado no disco não contém o
   token em texto plano (requer um teste instrumentado Android, não unitário puro — anotar
   essa limitação na spec).
2. **GREEN**: troca de `context.getSharedPreferences(...)` por
   `EncryptedSharedPreferences.create(...)` em `ArmazenamentoSessaoPrefs`.
3. **REGRESSÃO**: suíte unitária mobile (não executável neste sandbox, ver SPEC-007).

## Resultado
Implementado: `ArmazenamentoSessaoPrefs` passou de `context.getSharedPreferences(...)` comum
para `EncryptedSharedPreferences.create(...)` (Jetpack Security — `MasterKey` com
`AES256_GCM`, chave+valor com `AES256_SIV`/`AES256_GCM`). Adicionada a dependência
`androidx.security:security-crypto:1.1.0` em `libs.versions.toml`/`build.gradle.kts`.
Nenhum outro ponto do código instancia `ArmazenamentoSessaoPrefs` além de `AppContainer`
(assinatura do construtor não mudou), e os testes existentes (`GerenciadorSessaoTest`,
contrato) usam o fake em memória, não essa classe — continuam intactos.

**Risco maior que a SPEC-007, registrado para quem for validar:**
1. Não consegui confirmar que `androidx.security:security-crypto:1.1.0` resolve de verdade
   (mesma limitação de rede/AGP das specs mobile anteriores) — a versão foi escrita de
   memória, não verificada contra o repositório Maven.
2. **Não escrevi teste automatizado para isso.** `EncryptedSharedPreferences` depende do
   Android Keystore, que o Robolectric historicamente não simula por completo sem
   configuração extra (shadows de `android.security.keystore`) — um teste malfeito aqui
   poderia parecer cobertura e na verdade nunca conseguir passar por limitação do próprio
   Robolectric, o que seria pior que não ter teste. Preferi ser honesto sobre a lacuna em vez
   de fabricar uma prova que eu não consigo confirmar que funciona.

**Verificação necessária antes de confiar nisso**: compilar com Android Studio/SDK real,
rodar o app num emulador, fazer login, e confirmar manualmente (ex.: `adb shell cat` no
arquivo de preferências do app, ou um teste instrumentado de verdade em aparelho/emulador,
não Robolectric) que o conteúdo não está mais em texto plano.
