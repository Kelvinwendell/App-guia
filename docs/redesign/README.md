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

| ID | Entrega | Dependências | Situação inicial |
|---|---|---|---|
| [RD-000](specs/000-baseline-e-ambiente.md) | Baseline, ambiente e evidências | — | A executar; build local bloqueado |
| [RD-001](specs/001-fundacoes-visuais.md) | Tokens, tipografia e contraste | RD-000 | Código parcial, sem validação Android |
| [RD-002](specs/002-componentes-e-navegacao.md) | Componentes, shell e estados | RD-001 | Código parcial, sem validação Android |
| [RD-003](specs/003-autenticacao-e-perfil.md) | Login, cadastro e perfil | RD-002 | Login parcial; demais telas pendentes |
| [RD-004](specs/004-homes-por-perfil.md) | Homes dos três perfis | RD-002 | Código parcial, sem validação Android |
| [RD-005](specs/005-ideias.md) | Ideias, filtros e decisões | RD-002, RD-004 | Revisão específica pendente |
| [RD-006](specs/006-projetos.md) | Projetos, andamento e resultados | RD-002, RD-004 | Revisão específica pendente |
| [RD-007](specs/007-estrategias-e-historico.md) | Estratégias e histórico | RD-002 | Revisão específica pendente |
| [RD-008](specs/008-dashboard.md) | Dashboard e gráficos | RD-002, RD-004 | Revisão específica pendente |
| [RD-009](specs/009-assistente-e-insights.md) | Assistente, avaliações e insights | RD-005, RD-008 | Revisão específica pendente |
| [RD-010](specs/010-validacao-ci-e-entrega.md) | Aceite integrado, CI e APK | RD-003 a RD-009 | A executar |

“Código parcial” significa que há alterações locais; não equivale a requisito concluído. Só atualizar a situação após reunir as evidências de aceite da spec.

## Organização e manutenção

- `docs/redesign/`: PRD, contexto desta iniciativa e specs de execução.
- `docs/design/`: referências Figma, prévias e seus renderizadores.
- `.context/`: conhecimento técnico durável já existente. Não copiar o backlog para lá.
- Relatórios de execução, logs e capturas reais: `entregas/redesign/<id-da-spec>/`, criados durante a implementação. Não versionar caches, segredos ou dados pessoais reais.

As specs são instruções de implementação, não evidência de que a implementação passou. Este pacote não inicia automaticamente alterações de produto, commits, publicação ou deploy.
