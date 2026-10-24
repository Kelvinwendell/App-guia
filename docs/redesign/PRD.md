# PRD — Redesign Android do Águia Inova

**Versão:** 1.0 • **Data:** 24/10/2026 • **Estado:** pronto para orientar implementação; aceite Android pendente.

## 1. Problema e resultado esperado

O app já oferece o fluxo de inovação integrado, mas sua apresentação tem cartões com peso semelhante, espaços pouco eficientes, hierarquia inconsistente e componentes que precisam se adaptar melhor a textos longos e telas pequenas. A primeira revisão visual foi aprovada pelo usuário, sem validação em um build Android.

Entregar uma experiência escura, minimalista e moderna, baseada nos componentes Material 3 consultados no Figma. O usuário deve reconhecer seu perfil, entender os indicadores e encontrar sua próxima ação com clareza, preservando dados, regras e navegação existentes.

## 2. Personas e permissões preservadas

| Capacidade | Operador | Gestor | Liderança |
|---|---|---|---|
| Ideias próprias | Criar; editar/excluir enquanto em análise | Consultar todas, avaliar e priorizar | Sem aba de gestão de ideias |
| Aprovadas da empresa | Consultar | Consultar | API permite consulta; não criar nova tela neste ciclo |
| Projetos | Sem acesso | Consultar, criar, atualizar andamento/resultados, excluir | Consultar |
| Estratégias e histórico | Consultar | Consultar | Consultar e gerenciar estratégias |
| Dashboard | Sem acesso | Sem acesso | Consultar e solicitar insights |
| Assistente e avaliação IA | Sem acesso | Usar | Sem chat; insights pelo dashboard |
| Perfil/sessão | Consultar perfil e sair | Consultar perfil e sair | Consultar perfil e sair |

A matriz resume fluxos existentes e não autoriza ampliá-los. `@PreAuthorize` e verificações de dono são a autoridade final. Gestão de usuários, embora exista na API para liderança, não entra no redesign.

## 3. Objetivos verificáveis

- O1: todas as telas do inventário abaixo usam os componentes e tokens compartilhados, sem uma segunda identidade visual.
- O2: há exatamente uma ação primária destacada na home de cada perfil; ações de gestão não aparecem para perfis de consulta.
- O3: telas funcionam em larguras de 360 e 411 dp, com fonte em 100% e 150%, sem sobreposição, corte de ação essencial ou rolagem horizontal da página.
- O4: texto comum alcança contraste mínimo de 4,5:1; texto grande e indicadores visuais essenciais, 3:1. Não depender só de cor para status ou seleção.
- O5: carregamento, vazio, erro inicial, falha de atualização e envio em andamento têm tratamento coerente e preservam a intenção do usuário.
- O6: build debug, testes mobile e lint passam em ambiente reproduzível; fluxos reais dos três perfis têm evidências antes da entrega.

Esses são critérios de aceite propostos para esta implementação, não métricas já medidas. Não adicionar analytics ou coleta de dados para verificá-los.

## 4. Inventário e rastreabilidade

| Requisito | Telas/experiência | Spec responsável |
|---|---|---|
| RF-01 | Tema, formas, tipografia, cores semânticas | RD-001 |
| RF-02 | Cabeçalhos, navegação, cartões, campos, estados e diálogos | RD-002 |
| RF-03 | Login, cadastro, perfil e saída de sessão | RD-003 |
| RF-04 | Home operador, gestor e liderança | RD-004 |
| RF-05 | Ideias, nova/editar ideia, filtros, aprovação/rejeição e ranking | RD-005 |
| RF-06 | Lista de projetos, criação, detalhe, andamento e resultados | RD-006 |
| RF-07 | Estratégias, criação/edição e histórico | RD-007 |
| RF-08 | Dashboard, KPIs, filtros, prazos e gráficos | RD-008 |
| RF-09 | Chat do gestor, avaliação IA e insights da liderança | RD-009 |
| RF-10 | Evidências reais, CI mobile e APK de revisão | RD-000, RD-010 |

## 5. Requisitos transversais

**RNF-01 — Identidade:** manter paleta azul escura da direção aprovada. Cartões preenchidos e superfícies tonais substituem bordas decorativas. Referências e exceções documentadas em RD-001.

**RNF-02 — Legibilidade:** fonte nativa Roboto/escala Material 3, tamanhos em `sp`, superfícies em `dp`; não diminuir texto para caber. Permitir quebra e layouts em coluna quando necessário. Valores financeiros essenciais devem continuar acessíveis integralmente.

**RNF-03 — Acessibilidade:** alvos interativos de pelo menos 48 dp; descrições em ações apenas com ícone; ícones decorativos sem descrição; seleção, erro e progresso também por texto/semântica. Suporte a TalkBack e teclado sem esconder envio/salvamento.

**RNF-04 — Estado:** não executar requisições por recomposição. Preservar campos em recriação quando suportado por estado salvável, sem persistir senha/token em arquivos de preview. Listas mantêm chaves estáveis; carregamento não exibe números ilustrativos.

**RNF-05 — Código limpo:** responsabilidades e interfaces de [ARQUITETURA.md](ARQUITETURA.md). Componentes visuais recebem dados e callbacks, não repositórios. Sem reescrever o backend para resolver composição visual.

**RNF-06 — Segurança:** preservar sessão criptografada, logout/401, troca de servidor exclusiva de debug, release HTTPS e regras de acesso. Não colocar credenciais de produção, API keys ou tokens no app, nas specs ou nas capturas.

**RNF-07 — Performance:** listas dinâmicas em componentes lazy com IDs; evitar listas grandes dentro de rolagem vertical aninhada. Não adicionar chamadas de IA, rede ou animações contínuas apenas para enriquecer visualmente a tela.

## 6. Regras funcionais de continuidade

- Valores e datas usam `Formatadores`; enums e suas permissões não são renomeados na API.
- Avaliação IA é sugestão, não aprovação automática. Heurística continua identificada como “Regras”.
- Insights são solicitados explicitamente; o endpoint atual não recebe filtro de estratégia. A UI deve informar quando a leitura é global, mesmo com gráfico filtrado.
- Cadastro mantém o contrato atual e não promete que escolher um perfil concede acesso: o servidor pode forçar Operador por configuração.
- A falha de refresh não apaga os dados já carregados. Erro inicial mostra recuperação; vazio não é exibido como erro.
- Confirmação destrutiva identifica o item. Cancelar não chama o repositório.

## 7. Não objetivos

Não criar tema claro, onboarding, notificações, busca global, sincronização offline, novo sistema de contas ou internacionalização completa. Não redesenhar um site. Não criar telas para endpoints ainda sem interface. Não alterar fórmulas de ROI, lucro, prazos ou a estratégia de autenticação. Não atualizar toda a stack sem diagnóstico de incompatibilidade.

## 8. Abordagem e decisões

| Alternativa considerada | Benefício | Custo/decisão |
|---|---|---|
| Ajustar apenas cores | Baixo esforço | Insuficiente para hierarquia, fluxos e acessibilidade |
| Refinar Compose com Material 3 | Reutiliza arquitetura e componentes já existentes | Escolhida como direção técnica para executar a intenção aprovada |
| Reescrever UI/arquitetura | Liberdade maior | Risco e escopo desnecessários para o objetivo |

O usuário aprovou a direção visual e pediu uso de componentes do Figma. Os detalhes de execução deste pacote são propostas técnicas do autor. Figma fornece referência de componentes; o app usa seus equivalentes nativos em Compose, não imagens de telas.

## 9. Riscos e mitigação

| Risco observado | Mitigação e responsável |
|---|---|
| Build Java local bloqueado | RD-000: diagnosticar e estabelecer ambiente reproduzível antes de aceitar código |
| Paleta nova com conteúdo branco fixo | RD-001 inventaria contrastes; RD-005/009 corrigem seus callsites |
| Chips/botões em Row não cabem | RD-002 define reflow; specs de domínio verificam textos reais e fonte ampliada |
| Dependência de API nas capturas | RD-002 cria conteúdo renderizável sem rede; RD-010 separa teste visual e integração real |
| Documentação histórica com links ausentes | Contexto deste pacote aponta fontes presentes; não reconstruir fatos ausentes como concluídos |
| Regressão de permissão durante refactor | Matriz por perfil em cada spec e teste integrado RD-010 |

## 10. Critério de entrega do produto

Todos os requisitos RF e RNF têm evidência na spec responsável; o ambiente não bloqueia build/testes; as capturas finais são Android reais; não há dados fictícios em runtime; há um APK novo identificado e instruções de instalação. Se houver limitação material, descrevê-la no relatório e manter o item correspondente aberto.

Este pedido é de documentação para iniciar execução. Implementação futura, publicação e merge são ações separadas deste PRD.
