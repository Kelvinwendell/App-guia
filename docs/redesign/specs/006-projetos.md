# SPEC-RD-006 — Projetos, andamento e resultados

**Prioridade:** P1 • **Dependências:** RD-002, RD-004 • **Requisitos:** RF-06, RNF-02/03/06.

## Objetivo

Melhorar a leitura de projeto e seus resultados sem misturar métricas, edição e ações destrutivas. Gestor edita; liderança consulta.

## Arquivos e contratos

Modificar `ui/projetos/{ProjetosScreen,ProjetoFormScreen,ProjetoDetalheScreen,ProjetosViewModels}.kt`. Manter classes atuais e `ProjetoFormViewModel` no local existente; não renomear modelos `DadosProjeto`, `AndamentoProjeto`, `DadosResultado`.

Separar os blocos de detalhe em `ui/projetos/ComponentesProjeto.kt` somente se necessário a clareza/teste. Criar `ProjetosViewModelTest.kt`, `ProjetoDetalheViewModelTest.kt` e `ProjetosConteudoTest.kt` em `src/test/.../ui/projetos/`.

## Como implementar

- [ ] Lista: nome, status/etapa, progresso, prazo e indicadores existentes, com detalhes secundários em menor ênfase; cada cartão navega pelo id real.
- [ ] Criação: nome/descrição, investimento/prazo e vínculos com estratégia/ideia, sem inventar obrigatoriedade para associações opcionais.
- [ ] Detalhe: resumo, indicadores, andamento, resultados e ações finais em seções. Gestor vê editor/registrar/remover; liderança não recebe controles de escrita.
- [ ] Manter conversão de datas/números em `Formatadores`; apresentar erro próximo ao campo quando possível. Não modificar parser ou aceitar datas antes rejeitadas sem teste específico e decisão de escopo.
- [ ] Progresso comunica porcentagem por texto e semântica; slider e seleção de etapa/status não salvam por recomposição. Salvar envia `AndamentoProjeto` pelo VM existente.
- [ ] Valores extensos e negativos quebram corretamente em linhas de informações. Status e prioridade não dependem só de cor.
- [ ] Exclusão de projeto e resultado usa confirmação com contexto do item; erro mantém os dados e permite nova tentativa.

## Testes e aceite

- [ ] VM recebe lista/obter/salvarAndamento/adicionarResultado/removerResultado/remover pelos contratos atuais e propaga sucesso/erro corretamente.
- [ ] Liderança consegue consultar lista/detalhe sem CTA de criar, editar ou excluir.
- [ ] Entrada `1.234,56` continua convertida para 1234.56 onde já aceita; data inválida e investimento negativo não disparam criação.
- [ ] Atualização bem-sucedida mostra estado devolvido pela API; erro de refresh não descarta conteúdo anterior.
- [ ] Detalhe com muitos resultados, nomes extensos e IME não perde a última ação. Testar fonte 150% e largura 360 dp.
- [ ] Capturas reais de lista, criação e detalhe; build/testes/lint aprovados.

**Fora do escopo:** novos cálculos financeiros, novo formulário de edição completa se não existe no app, anexos, cronograma ou integração externa.
