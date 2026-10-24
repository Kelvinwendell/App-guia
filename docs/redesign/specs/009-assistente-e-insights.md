# SPEC-RD-009 — Assistente, avaliação e insights

**Prioridade:** P1 • **Dependências:** RD-005, RD-008 • **Requisitos:** RF-09, RNF-03/04/06.

## Objetivo

Dar clareza à origem e aos limites das sugestões de IA, com chat legível e tratamento honesto de indisponibilidade. Preservar a decisão humana e a arquitetura do backend.

## Arquivos e contratos

Modificar `ui/ia/{AssistenteIaScreen,AssistenteViewModel}.kt`, bloco de insights em `ui/dashboard/` e apresentação da avaliação em `ui/ideias/`. Não alterar `IaRepository`, `MensagemChat`, `RespostaChat`, `InsightsIa` ou endpoints sem spec separada.

Criar `ui/ia/AssistenteViewModelTest.kt`, `ui/ia/AssistenteConteudoTest.kt` e `ui/dashboard/InsightsConteudoTest.kt` no source set de teste. Fakes devem retornar respostas determinísticas; nenhum teste exige chave real.

## Como implementar

- [ ] Balão do usuário usa par `primary/onPrimary`; assistente usa `surfaceContainer/onSurface`. Hoje o texto do balão pode herdar branco sobre o azul claro e precisa de correção.
- [ ] Manter remetente distinguível por alinhamento e semântica, não só cor. Mensagem longa permite leitura/rolagem; não limitar altura nem cortar resposta com ellipsis.
- [ ] Composer permanece visível com IME; Enviar desabilitado para branco, ocupado e indisponibilidade confirmada. Status ainda não carregado não deve ser apresentado como “conectado”.
- [ ] Implementar guarda de envio duplicado no VM se os testes mostrarem duas chamadas enquanto `ocupado`; a UI sozinha não é contrato de exclusão mútua. Não criar fila de mensagens nesta entrega.
- [ ] Falha de envio mostra erro e mantém a pergunta visível/recuperável; não inserir uma resposta artificial. Preservar limite de 20 mensagens de histórico enviado já existente.
- [ ] Avaliação/insights com fonte HEURISTICA mostram “Regras”/leitura por regras; sugestão de prioridade não muda status da ideia.
- [ ] Insights são gerados apenas por ação explícita; bloquear repetição enquanto carrega. O contrato `insights()` não recebe filtro: rotular leitura global mesmo quando dashboard está filtrado.
- [ ] Nunca colocar chave, modelo configurável ou configuração de servidor dentro do fluxo normal de uso do chat.

## Testes e aceite

- [ ] VM: mensagem vazia não chama conversar; envio usa pergunta e histórico anteriores corretos, limitado a 20; falha não gera mensagem do assistente.
- [ ] Duplo envio/duplo pedido de insights durante execução produz somente uma chamada; testar antes da correção funcional.
- [ ] Origem heurística aparece corretamente e IA não configurada não aparenta estar conectada.
- [ ] Gerar insights não ocorre por composição, navegação ou refresh do dashboard.
- [ ] Chat/insights mantêm contraste e conteúdo completo em fonte 150%, 360 dp e com teclado.
- [ ] Nenhuma avaliação aprova ideia automaticamente; regressão de RD-005 continua passando.

**Fora do escopo:** streaming, histórico persistido no servidor, troca de provedor, chamadas reais pagas ou execução automática de projetos pela IA.
