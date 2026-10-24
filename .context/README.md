# .context — conhecimento durável do Águia Inova

Esta pasta é **engenharia de contexto**, não documentação de produto nem rastreamento de
tarefas: um conjunto pequeno e de alto sinal de fatos duráveis sobre este projeto, para que
qualquer sessão (humana ou de IA) consiga trabalhar nele corretamente sem precisar
redescobrir tudo do zero lendo o código inteiro.

## O que entra aqui (e o que não entra)

**Entra:** arquitetura e padrões de projeto realmente usados, convenções de código e de teste
que o repositório já segue, invariantes de segurança que não podem regredir, como o fluxo de
Git deste projeto funciona (incluindo as armadilhas já encontradas), e como o sistema está
implantado em produção. Tudo isso muda devagar e vale a pena manter atualizado.

**Não entra:** listas de tarefas, status de "o que falta fazer", specs de trabalho em
andamento. Isso é estado transitório — pertence a issues, PRs e ao histórico do Git, não a um
arquivo de contexto de longa duração que acumula lixo conforme o trabalho avança.

## Arquivos

- [`architecture.md`](architecture.md) — camadas, padrões de projeto, onde cada coisa mora
  (backend e mobile), como rodar os testes.
- [`conventions.md`](conventions.md) — convenções de código e teste que o projeto já segue;
  siga o mesmo padrão ao adicionar algo novo.
- [`security-invariants.md`](security-invariants.md) — regras de segurança que não podem
  regredir, com o porquê de cada uma e onde são aplicadas no código.
- [`git-workflow.md`](git-workflow.md) — branch, commits, PR, merge, CI — e as armadilhas já
  encontradas nesse fluxo.
- [`deployment.md`](deployment.md) — como o sistema está implantado em produção hoje
  (MongoDB Atlas + Render), variáveis de ambiente e problemas já resolvidos no caminho.

## Regra de manutenção

Atualize o arquivo relevante quando um fato durável mudar de verdade (uma nova convenção
pega, uma regra de segurança nova, a arquitetura muda). Não registre aqui o "estou fazendo
X agora" — isso é para o commit, a PR ou a conversa, não para este diretório.
