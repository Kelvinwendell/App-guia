# .context — engenharia de contexto do Águia Inova

Esta pasta existe para que qualquer sessão (humana ou agente) consiga retomar o trabalho de
preparar o sistema para produção sem precisar re-derivar tudo do zero. Ela não é documentação
de produto — é o **estado vivo do trabalho de hardening/produção**.

## Estrutura

- `architecture.md` — mapa condensado da arquitetura (backend + mobile), padrões de projeto
  usados, e avaliação de aderência a clean code. Resultado de uma auditoria de código completa.
- `STATUS.md` — tabela mestre de todas as specs, com status atual. **Sempre olhe aqui primeiro.**
- `specs/NNN-titulo.md` — uma spec por item de trabalho, cada uma com critério de aceite e
  plano de TDD próprio.

## Regra do loop de desenvolvimento

Cada spec executável localmente segue este ciclo, sem pular etapa:

1. **RED** — escrever/ajustar um teste que expressa o comportamento desejado e confirmar que
   ele falha (prova que o teste testa algo real, não um teste que passa por acidente).
2. **GREEN** — implementar o mínimo necessário para o teste passar.
3. **REGRESSÃO** — rodar a suíte inteira (`./mvnw test` no backend). Se algo quebrou, isso é
   sinal de efeito colateral: diagnosticar e corrigir antes de seguir, nunca comentar/pular teste.
4. Se RED não virou GREEN de primeira: diagnosticar a causa real do erro, ajustar a implementação
   (não o teste, a menos que o teste estivesse errado) e repetir 2-3 até fechar.
5. Só quando REGRESSÃO está 100% verde: marcar a spec como `DONE` em `STATUS.md`, commitar com
   mensagem clara, e seguir para a próxima spec pela ordem de prioridade.

Specs marcadas `BLOCKED (requer ação externa)` não entram nesse loop — exigem uma conta,
credencial ou decisão que só o dono do projeto pode fazer (ex.: criar conta no MongoDB Atlas).
Para essas, a spec serve como checklist de execução manual.

## Prioridade

`CRÍTICO` → `IMPORTANTE` → `BOM TER`, sempre nessa ordem, dentro de cada grupo (autônomo vs.
bloqueado) — ver `STATUS.md` para a lista ordenada.
