# SPEC-RD-002 — Componentes, shell e estados comuns

**Prioridade:** P0 • **Dependências:** RD-001 • **Requisitos:** RF-02, RNF-02/03/04/05.

## Objetivo

Entregar componentes consistentes sem sacrificar reflow, acessibilidade e recuperação de estado. Preservar a navegação de cada perfil e o comportamento atual de sessão.

## Arquivos

Modificar `ui/components/{Cartoes,Campos,Layout,Dialogos}.kt` e, somente se necessário para corrigir encaixe de destinos, `navigation/{Rotas,NavGraph}.kt`. Testes novos: `ui/components/ComponentesTest.kt`, `ui/components/EstadosTest.kt`, `navigation/NavegacaoPorPerfilTest.kt`, `ui/support/FixturesVisuais.kt`, todos em `src/test/java/br/com/fiap/aguiainova/`.

Não dividir arquivos apenas para ter um componente por arquivo. Se extrair o cabeçalho/barra para arquivo próprio, manter funções e imports existentes compatíveis.

## Contratos

- `CartaoAguia`: filled tonal, raio `shapes.medium`, sem borda decorativa.
- `CartaoAcao`: ícone 24 dentro de suporte circular 40, padding/gap 16, título 16/24 e apoio 14/20; destaque apenas quando explicitamente solicitado.
- `CartaoIndicador`: rótulo secundário, valor principal e marcador de categoria; altura cresce para conteúdo acessível.
- `CampoTexto`: acrescentar `erro: String? = null` ao fim da assinatura; `erro` aciona `isError` e substitui dica quando presente.
- `BotaoPrimario`: preservar assinatura; altura mínima 52 com `heightIn`, sem impedir crescimento de texto; loading comunica estado e desabilita reenvio pela UI.
- `MensagemVazia`: parâmetros opcionais finais `acaoTexto` e `aoAcao`; ação só aparece se ambos estiverem presentes.
- `TelaBase` e `ConteudoDoEstado`: preservar assinaturas e papéis; filhos recebem área disponível. Não introduzir uma segunda máquina de estados.

## Como implementar

- [ ] Manter o `Scaffold` como dono dos insets e conferir o efeito do teclado; evitar padding duplo no conteúdo.
- [ ] Aplicar papéis do tema em lugar de cores fixas no conteúdo de botões e superfícies.
- [ ] Ajustar cabeçalhos para título longo/voltar/ações sem disputa por largura. Ícones interativos têm alvo de 48 dp e descrição; setas decorativas não duplicam texto no TalkBack.
- [ ] Na barra inferior, manter `irParaAba`, `saveState`, `restoreState` e `launchSingleTop`. Verificar as três listas de abas existentes, incluindo cinco itens para gestor/liderança.
- [ ] Permitir quebra/rolagem de chips; não cortar status ou diminuir fonte para acomodar rótulos.
- [ ] Criar testes de conteúdo puro que compõem loading, erro e vazio sem rede, usando callbacks contadores. Fixtures somente em source set de teste.

## Testes e aceite

- [ ] `EstadosTest`: erro inicial mostra mensagem e retry; tocar retry chama uma única vez; vazio sem ação não cria botão.
- [ ] `ComponentesTest`: campo com erro expõe semântica e mantém valor; botão carregando não chama callback; botão de duas linhas cresce e continua acessível com fonte 150%.
- [ ] `NavegacaoPorPerfilTest`: abas correspondem ao perfil; trocar aba repetidamente não acumula destinos; logout/401 não deixa conteúdo protegido visível.
- [ ] Validar largura 360/411 dp e fonte 100/150%, inclusive valores financeiros extensos e título de três linhas.
- [ ] Rodar regressão mobile e inspecionar os três estados. Não testar cor comparando apenas constantes Kotlin.

**Fora do escopo:** nova navegação por drawer, novas permissões ou lib de design adicional.
