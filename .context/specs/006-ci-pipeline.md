# SPEC-006: Pipeline de CI (GitHub Actions)

## Status
PENDING

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
(preencher ao final, com o link do run do Actions como evidência)
