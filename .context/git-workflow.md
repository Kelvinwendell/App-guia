# Fluxo de Git — Águia Inova

## Branch e commit

- Trabalho em uma branch nomeada por tarefa, PR para `main`, **squash merge** (um commit
  final por PR na `main`, histórico detalhado fica só na descrição do PR).
- Mensagem de commit: título curto no imperativo, corpo explicando o porquê (não o quê — o
  diff já mostra o quê). Prefixo de tipo quando ajuda (`fix:`, `feat:`, `docs:`, `perf:`,
  `refactor:`, `ci:`), mas não é obrigatório nem fiscalizado.
- CI (`.github/workflows/backend-ci.yml`) roda em todo push/PR. Não mescle com CI vermelho.

## Squash merge muda o grafo de commits — cuidado ao reabrir uma branch de trabalho

**Armadilha real, já encontrada neste projeto:** depois que uma branch é squash-mergeada na
`main`, o commit resultante na `main` **não tem a branch original como ancestral** — só o
commit anterior da `main`. Se você continuar commitando na mesma branch de trabalho e abrir
um PR novo dela, o Git enxerga todos os commits antigos da branch como "não mesclados" de
novo (mesmo com conteúdo idêntico ao que já está na `main`), e o diff do PR novo vem enorme
— com tudo que já foi mesclado antes, de novo.

**Como resolver:** antes de abrir o segundo PR, traga a `main` atual para dentro da branch de
trabalho:

```bash
git fetch origin main
git merge origin/main
```

Isso gera conflitos do tipo *add/add* nos arquivos que foram tocados dos dois lados (a versão
pré-squash da branch vs. a versão pós-squash da `main`) — resolva pegando a versão da sua
branch, que é a mais atualizada:

```bash
git checkout --ours <arquivo-em-conflito>
git add <arquivo-em-conflito>
git commit
```

Depois disso o PR volta a mostrar só o diff real (os arquivos que mudaram de fato desde o
último squash), não o histórico inteiro de novo.

## O que nunca fazer aqui

- **Nunca force-push na `main`.** Mesmo que pareça inofensivo (projeto solo, sem
  colaboradores), é uma regra sem exceção. Reescrita de histórico (ex.: remover um autor de
  commit do passado) é uma operação que o dono do repositório roda no próprio terminal, não
  algo que se automatiza numa sessão de trabalho.
- **Nunca apagar branch remota automaticamente** sem o dono pedir explicitamente — mesmo uma
  branch já mesclada pode ser removida manualmente pelo dono com um clique na própria tela do
  Pull Request, ou por `git push origin --delete <branch>`.
