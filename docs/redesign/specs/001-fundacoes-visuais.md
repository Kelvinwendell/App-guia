# SPEC-RD-001 — Fundações visuais e contraste

**Prioridade:** P0 • **Dependências:** RD-000 • **Requisitos:** RF-01, RNF-01/02/03.

## Objetivo

Consolidar os ajustes parciais em uma fundação Material 3 reutilizável, com pares de cor legíveis e espaçamentos claros. O Figma é referência de anatomia e escala; a paleta escura da marca substitui o tema lilás demonstrativo do kit.

## Arquivos

Modificar `ui/theme/Color.kt`, `Theme.kt`, `Type.kt`, `CoresDeDominio.kt`, relativos à raiz Kotlin indicada em ARQUITETURA.md. Criar `ui/theme/Espacamentos.kt` com `object AguiaEspacamentos` apenas para os valores compartilhados abaixo. Criar `ui/theme/TemaVisualTest.kt` no source set de teste para composição dos estados relevantes.

## Contrato visual

| Token/papel | Valor inicial decidido |
|---|---|
| Fundo / surfaceContainerLowest | `#0B1220` |
| Superficie / surfaceContainer | `#131F32` |
| SuperficieElevada / primaryContainer | `#1A2B43` |
| Borda | `#2A3C55` |
| Primaria / onPrimary | `#53C7F5` / `#0B1220` |
| Sucesso / Alerta / Erro | `#55D9B1` / `#FFCF70` / `#FF8892` |
| TextoSecundario | `#A7B7CC` |
| Formas extraSmall/small/medium/large/extraLarge | 4 / 8 / 12 / 16 / 28 dp |
| AguiaEspacamentos: Minimo, Pequeno, Medio, Conteudo, Tela, Secao | 4 / 8 / 12 / 16 / 20 / 24 dp |

Manter `Typography()` nativo; isso usa Roboto no Android e escala Material 3. Não baixar uma fonte alternativa por conveniência. Tokens de espaçamento são para relações repetidas; uma dimensão de gráfico não precisa virar token global.

## Como implementar

- [ ] Conferir `Theme.kt` contra a versão Material 3 realmente resolvida no build e remover APIs incompatíveis se necessário sem perder os papéis semânticos.
- [ ] Centralizar a paleta em `AguiaCores`, mapeando `onPrimary`, `onError`, `onSurface`, `onSurfaceVariant` e containers no tema.
- [ ] Inventariar `Color.White`, superfícies coloridas, transparências e cores fixas em toda `ui/`. Medir contraste sobre a cor final composta, não só sobre o hex original.
- [ ] Corrigir os pares compartilhados; registrar callsites específicos a tratar em RD-005, RD-008 e RD-009. O aceite global não pode manter contraste quebrado por conveniência.
- [ ] Fazer catálogo de composição em teste com título, corpo, botão, selo, erro, desabilitado e superfícies. Exibir a mesma amostra a 100% e 150% de fonte.

## Validação e aceite

- [ ] Texto normal ≥4,5:1; texto grande/elemento essencial ≥3:1 nos estados ativos. Desabilitados não parecem ações disponíveis.
- [ ] Status incluem rótulos; não depender somente de verde/vermelho.
- [ ] Tema e catálogo compilam, com Roboto/Material 3; sem download de fonte em runtime.
- [ ] Cores de domínio continuam mapeadas a enums existentes.
- [ ] Build/testes/lint da execução passam. Inspeção visual documenta eventuais exceções com solução e spec responsável, não um aceite global antecipado.

**Fora do escopo:** tema claro, dynamic color automático, renomear enums ou reproduzir fotografias/avatar de exemplo do Figma.
