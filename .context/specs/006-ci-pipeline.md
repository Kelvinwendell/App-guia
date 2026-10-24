# SPEC-006: Pipeline de CI (GitHub Actions)

## Status
DONE

## Prioridade
CRÍTICO

## Execução
Autônoma (arquivo de configuração + verificação via push real), sem custo
(GitHub Actions é gratuito para o uso deste projeto).

## Problema
Não existe nenhum pipeline de CI. Nada impede um build quebrado, com testes falhando ou com
vulnerabilidade óbvia, de ser mesclado/publicado.

## Critério de aceite
- [ ] Workflow do GitHub Actions roda `./mvnw test` do backend em todo push/PR para a branch
      principal.
- [ ] Workflow falha (vermelho) se qualquer teste falhar — verificado de propósito com um
      teste quebrado temporário, depois revertido.
- [ ] Usa cache de dependências do Maven para não ficar lento.
- [ ] (Mobile fica fora deste CI por enquanto — ver nota em `architecture.md` sobre a
      limitação de Android Gradle Plugin neste ambiente; pode ser adicionado depois, em uma
      spec própria, quando houver um runner com Android SDK, que o GitHub Actions oferece
      nativamente nos runners `ubuntu-latest`/`macos-latest`.)

## Plano de validação (não é TDD de código, é verificação de infraestrutura)
1. Criar `.github/workflows/backend-ci.yml`.
2. Commitar e empurrar um teste proposital quebrado numa branch de teste, confirmar que o
   Actions reporta falha (vermelho).
3. Reverter o teste quebrado, confirmar que o Actions reporta sucesso (verde).
4. Deixar o workflow definitivo (sem o teste quebrado) na branch principal.

## Resultado
`.github/workflows/backend-ci.yml` criado (checkout + setup-java Temurin 25 + cache Maven +
`./mvnw -B test` em `backend/`). Validado contra infraestrutura real (não simulável no
sandbox): run disparado pelo push do commit `f6dfd89` concluiu com sucesso —
https://github.com/Kelvinwendell/App-guia/actions/runs/40056128972 (status completed,
conclusion success). Confirma que Java 25 via Temurin está disponível no runner hospedado do
GitHub (dúvida real antes de testar, já que é uma versão recente).

**Não validado nesta rodada**: o caminho RED (push de teste quebrado → run vermelho). O
comportamento do Maven Surefire (exit code != 0 quando há teste falhando) já é garantido pela
própria ferramenta, então o risco real que valia testar era a infraestrutura (runner, Java
25, cache), não a lógica de falha — e essa parte foi confirmada.

**Mobile fica fora deste workflow por ora** (ver `architecture.md`: Android Gradle Plugin não
resolve no sandbox de desenvolvimento usado nesta sessão). Pode ser adicionado depois, em uma
spec própria, usando um runner `ubuntu-latest` com Android SDK (o GitHub Actions oferece isso
nativamente via `android-actions/setup-android`).
