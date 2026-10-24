# SPEC-RD-010 — Validação integrada, CI e entrega

**Prioridade:** P0 para aceite final • **Dependências:** RD-003 a RD-009 concluídas • **Requisitos:** RF-10 e todos os RNF.

## Objetivo

Transformar alterações de código em uma entrega verificável: testes, telas Android reais, pipeline mobile e APK identificado. Não aceitar prévias simuladas como evidência final.

## Arquivos

Criar `.github/workflows/mobile-ci.yml`; preservar `backend-ci.yml`. Adaptar `mobile/app/src/test/java/br/com/fiap/aguiainova/ui/CapturasDeTelaTest.kt` somente para mudanças legítimas de UI, sem afrouxar permissões ou esperar conteúdo inexistente. Criar `entregas/redesign/RD-010/RELATORIO.md` e capturas na execução; atualizar links pertinentes do README raiz e este índice.

APK novo de revisão: `entregas/redesign/aguia-inova-redesign-debug.apk`, acompanhado de data, commit/base e hash SHA-256. Não sobrescrever o APK histórico sem decisão explícita.

## Como implementar

- [ ] Criar workflow mobile para push/PR com checkout, JDK compatível validado em RD-000, SDK plataforma 36 e toolchain de compilação 17 disponível. Fixar versões das actions conforme a política do repositório e conferir documentação oficial ao implementar.
- [ ] Executar `assembleDebug`, `testDebugUnitTest`, `lintDebug`; publicar relatórios e APK como artefatos do CI. Não adicionar credenciais de assinatura a esse job.
- [ ] Testes visuais determinísticos não dependem da API. Testes condicionais de contrato/captura continuam explicitamente separados e identificados quando ignorados.
- [ ] Em ambiente descartável integrado, testar operador → criar ideia; gestor → decidir ideia → criar projeto → atualizar andamento/resultado; liderança → consultar dashboard/estratégias; testar consulta dos demais perfis e logout/401.
- [ ] Executar capturas reais em rodada com dados conhecidos, separada do teste de contrato que adiciona registros.
- [ ] Fazer inspeção de acessibilidade com TalkBack, toque, fonte e teclado. Registrar resultados por requisito; corrigir na spec dona e repetir só os checks afetados antes da regressão final.

## Matriz obrigatória

| Dimensão | Casos |
|---|---|
| Tela | Login, cadastro, perfil, três homes, ideias/form/decisão, projetos/form/detalhe/resultados, estratégias/form/histórico, dashboard e chat |
| Estado | Carregando, pronto, vazio, erro inicial, falha de refresh, envio em andamento |
| Geometria | 360 e 411 dp; fonte 100% e 150%; IME aberta nos formulários/chat |
| Dados | Título de 120 caracteres, moeda extensa, zero, negativo, campo opcional ausente, texto IA longo |
| Permissão | Três perfis, sessão expirada e acesso negado pelo servidor |

Combinar casos de risco sem exigir o produto cartesiano completo; cada linha deve ter evidência rastreável. Ao menos uma execução em dispositivo/emulador real complementa a renderização Robolectric.

## Aceite final

- [ ] CI mobile verde e backend existente sem regressão. Nenhuma falha ou teste ignorado é omitido no relatório.
- [ ] Nenhum callback chama rede por recomposição; nenhuma chamada IA adicional foi introduzida como efeito visual.
- [ ] Contraste, alvos de toque, labels e reflow atendem PRD. Cancelar exclusão não modifica dados.
- [ ] APK novo instala e abre; perfil/servidor/versão de build estão identificados. Debug não é apresentado como release de produção.
- [ ] Capturas finais são Android reais e distinguíveis das simulações antigas de `docs/design/`.
- [ ] Todos os RF/RNF têm referência no relatório. Somente então atualizar status das specs e preparar PR focado.

**Fora do escopo:** publicar release, assinar APK com chave nova, enviar à Play Store, fazer deploy de backend ou merge automático. A entrega desta spec é material revisável e validado.
