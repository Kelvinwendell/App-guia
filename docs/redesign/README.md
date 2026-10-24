# Redesign do Águia Inova

Pacote de produto e execução, preparado em **24/10/2026** a partir do código local e das decisões desta conversa. O escopo é concluir o redesign Android com Material 3 e preservar o sistema integrado existente.

## Comece aqui

1. Leia [CONTEXTO.md](CONTEXTO.md): produto, arquitetura real e limites da evidência atual.
2. Leia [PRD.md](PRD.md): requisitos, permissões, prioridades e aceite do produto.
3. Consulte [ARQUITETURA.md](ARQUITETURA.md): responsabilidades, contratos e regras de código limpo.
4. Execute [EXECUCAO.md](EXECUCAO.md): sequência, comandos e definição de pronto.
5. Implemente uma spec por vez, começando por [SPEC-RD-000](specs/000-baseline-e-ambiente.md).

## Specs

Os IDs `RD` pertencem apenas a este redesign; não reutilizam a numeração histórica de hardening do backend.

| ID | Entrega | Dependências | Situação (24/10/2026) |
|---|---|---|---|
| [RD-000](specs/000-baseline-e-ambiente.md) | Baseline, ambiente e evidências | — | A executar; build local bloqueado |
| [RD-001](specs/001-fundacoes-visuais.md) | Tokens, tipografia e contraste | RD-000 | Mesclado (#7) |
| [RD-002](specs/002-componentes-e-navegacao.md) | Componentes, shell e estados | RD-001 | Mesclado (#8); ajuste da barra inferior no #17 |
| [RD-003](specs/003-autenticacao-e-perfil.md) | Login, cadastro e perfil | RD-002 | Mesclado (#10) |
| [RD-004](specs/004-homes-por-perfil.md) | Homes dos três perfis | RD-002 | PR #13 aberto, CI verde |
| [RD-005](specs/005-ideias.md) | Ideias, filtros e decisões | RD-002, RD-004 | PR #14 aberto, CI verde; `IdeiaFormConteudoTest` não criado |
| [RD-006](specs/006-projetos.md) | Projetos, andamento e resultados | RD-002, RD-004 | Mesclado (#11) |
| [RD-007](specs/007-estrategias-e-historico.md) | Estratégias e histórico | RD-002 | PR #17 aberto, CI verde, validado no emulador |
| [RD-008](specs/008-dashboard.md) | Dashboard e gráficos | RD-002, RD-004 | PR #15 aberto, CI verde, validado no emulador |
| [RD-009](specs/009-assistente-e-insights.md) | Assistente, avaliações e insights | RD-005, RD-008 | PR #16 aberto (empilhado no #15), validado no emulador sem IA real |
| [RD-010](specs/010-validacao-ci-e-entrega.md) | Aceite integrado, CI e APK | RD-003 a RD-009 | Parcial: [relatório](../../entregas/redesign/RD-010/RELATORIO.md); matriz completa pendente |

“PR aberto” não é aceite: o aceite final depende da matriz do RD-010. Validação visual real foi feita em emulador só para RD-007, RD-008 e RD-009 (ver relatório); as demais specs foram verificadas por testes Robolectric e leitura de código.

## Organização e manutenção

- `docs/redesign/`: PRD, contexto desta iniciativa e specs de execução.
- `docs/design/`: referências Figma, prévias e seus renderizadores.
- `.context/`: conhecimento técnico durável já existente. Não copiar o backlog para lá.
- Relatórios de execução, logs e capturas reais: `entregas/redesign/<id-da-spec>/`, criados durante a implementação. Não versionar caches, segredos ou dados pessoais reais.

As specs são instruções de implementação, não evidência de que a implementação passou. Este pacote não inicia automaticamente alterações de produto, commits, publicação ou deploy.
