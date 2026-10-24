# SPEC-011: Distribuir o APK sem pagar a taxa da Play Store

## Status
PARCIAL — código pronto (não verificado), falta a parte manual (keystore + build + publicação)

## Prioridade
BOM TER

## Execução
**Não autônoma na parte de assinatura/publicação** (precisa de uma keystore pessoal, que não
devo gerar e guardar eu mesmo por segurança). Posso preparar o processo de build assinado.

## Problema
A Google Play Console cobra uma taxa única de US$25 para publicar. Para um projeto
estudantil isso é opcional e evitável.

## Checklist de execução

### Parte feita (código)
- [x] `mobile/app/build.gradle.kts` ganhou `signingConfigs` condicional, lido só de variáveis
      de ambiente (`RELEASE_KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) —
      nunca commitado. Sem essas variáveis definidas, o release builda normalmente mas sem
      assinatura (não quebra nada hoje). **Não verificado** nesta sessão: mesma limitação de
      AGP não resolver no sandbox das specs mobile anteriores (007/008) — revisão só manual
      da sintaxe.

### Parte manual (pelo dono do projeto)
- [ ] Gerar uma keystore própria:
      ```bash
      keytool -genkeypair -v -keystore aguia-inova-release.jks -alias aguia-inova \
        -keyalg RSA -keysize 2048 -validity 10000
      ```
      **Guardar o `.jks` e a senha em local seguro, fora do repositório** (nunca commitar).
- [ ] Gerar o APK assinado em release, apontando para a API real em produção
      (https://app-guia.onrender.com, da SPEC-010):
      ```bash
      cd mobile
      export RELEASE_KEYSTORE_PATH="/caminho/para/aguia-inova-release.jks"
      export KEYSTORE_PASSWORD="sua-senha-da-keystore"
      export KEY_ALIAS="aguia-inova"
      export KEY_PASSWORD="sua-senha-da-keystore"
      ./gradlew assembleRelease -PapiBaseUrl=https://app-guia.onrender.com/
      ```
      APK sai em `mobile/app/build/outputs/apk/release/app-release.apk`.
- [ ] Criar um Release no GitHub (`Releases` → `Draft a new release`) e subir o APK assinado
      como anexo — distribuição gratuita, com histórico de versão.
- [ ] (Alternativa gratuita com atualização mais fácil: Firebase App Distribution — grátis,
      avisa testadores por e-mail quando sai uma versão nova.)

## Resultado
(preencher quando a primeira versão assinada for publicada)
