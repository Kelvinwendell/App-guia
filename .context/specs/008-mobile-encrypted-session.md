# SPEC-008: Sessão (JWT) criptografada no armazenamento do app

## Status
PENDING

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
(preencher ao final do ciclo TDD, deixando explícito que a REGRESSÃO precisa ser confirmada
fora deste sandbox)
