# SPEC-RD-007 — Estratégias e histórico

**Prioridade:** P1 • **Dependências:** RD-002 • **Requisitos:** RF-07.

## Objetivo

Apresentar diretrizes e sua evolução com leitura consistente, mantendo as permissões e o contrato de auditoria atuais.

## Arquivos

Modificar `ui/estrategias/{EstrategiasScreen,EstrategiaFormScreen,HistoricoEstrategiasScreen,EstrategiasViewModels}.kt`. Componentes específicos permanecem nesse pacote. Criar `EstrategiasViewModelTest.kt`, `HistoricoEstrategiasViewModelTest.kt` e `EstrategiasConteudoTest.kt` no pacote correspondente em `src/test`.

## Conteúdo e contratos

Manter `DadosEstrategia`, `StatusEstrategia.VIGENTE/ENCERRADA`, `AcaoEstrategia.CRIADA/ATUALIZADA/REMOVIDA`, `EstrategiaRepository` e filtros do histórico por categoria/campanha. A aparência de linha do tempo é opcional; não pode sugerir eventos que não existem na API.

## Como implementar

- [ ] Cartão de estratégia dá prioridade a título e status, seguido de descrição e metadados. Rótulos longos quebram antes de apertar ações.
- [ ] Todos os perfis consultam estratégias/histórico; apenas liderança vê criar, editar e excluir.
- [ ] Formulário mantém título, descrição, categoria, campanha opcional e status. Erro não apaga a edição; sucesso volta à tela que abriu e permite atualizar a lista.
- [ ] Histórico exibe ação, título, data, categoria, campanha e autor conforme dados disponíveis. Manter a identificação parcial atual; não prometer ID completo se a UI não o oferece.
- [ ] Filtros categoria/campanha, hoje lado a lado, tornam-se empilhados em largura pequena/fonte ampliada. Manter ambos os valores ao tentar novamente após erro.
- [ ] Estados vazios distinguem lista sem diretrizes de histórico sem correspondência; limpar filtros não cria nem altera dados.
- [ ] Exclusão confirma o título do item. Não reescrever histórico para melhorar apresentação.

## Testes e aceite

- [ ] Consulta renderiza VIGENTE e ENCERRADA corretamente, sem duplicação de rótulos por cor.
- [ ] Gestor/operador não veem botões de escrita; liderança recebe os callbacks esperados.
- [ ] Histórico envia categoria/campanha ao repositório; retry usa os mesmos valores. Vazio não é erro.
- [ ] Campanha/autor nulos têm representação consistente, sem literal “null”.
- [ ] Nome longo, data e filtros permanecem legíveis em 360 dp/fonte 150%; formulário rola com IME.
- [ ] Capturar lista, formulário e histórico, incluindo resultados vazios. Rodar a regressão mobile.

**Fora do escopo:** versionamento novo de estratégia, edição retroativa de auditoria ou filtros adicionais na API.
