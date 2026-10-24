# SPEC-011: Distribuir o APK sem pagar a taxa da Play Store

## Status
BLOCKED (requer ação externa — gerar keystore de assinatura e publicar um Release no GitHub)

## Prioridade
BOM TER

## Execução
**Não autônoma na parte de assinatura/publicação** (precisa de uma keystore pessoal, que não
devo gerar e guardar eu mesmo por segurança). Posso preparar o processo de build assinado.

## Problema
A Google Play Console cobra uma taxa única de US$25 para publicar. Para um projeto
estudantil isso é opcional e evitável.

## Checklist de execução (manual, pelo dono do projeto)
- [ ] Gerar uma keystore própria (`keytool -genkeypair ...`) e **guardar em local seguro, fora
      do repositório** (nunca commitar a keystore nem a senha).
- [ ] Gerar o APK assinado em release:
      `cd mobile && ./gradlew assembleRelease -PapiBaseUrl=https://<url-do-render>/`
- [ ] Criar um Release no GitHub (`Releases` → `Draft a new release`) e subir o APK assinado
      como anexo — distribuição gratuita, com histórico de versão.
- [ ] (Alternativa gratuita com atualização mais fácil: Firebase App Distribution — grátis,
      avisa testadores por e-mail quando sai uma versão nova.)

## Resultado
(preencher quando a primeira versão assinada for publicada)
