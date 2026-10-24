# Águia Inova — refinamento com Material 3

## Referência consultada no Figma

Biblioteca oficial: [Material 3 Design Kit, por Material Design](https://www.figma.com/community/file/1035203688168086460/material-3-design-kit).

Cópia de trabalho na equipe do usuário: [Material 3 Design Kit (Community)](https://www.figma.com/design/93Zr4oJqgrTpHQTA6obBKh/Material-3-Design-Kit--Community-).

Componentes e estilos inspecionados pelo conector Figma:

- Horizontal card / Filled (`52350:28197`): raio de 12, padding de 16, ícone/avatar de 40, título 16/24 e texto de apoio 14/20.
- Text field / Outlined / Enabled (`52798:24903`): componente nativo de campo com estados de foco, erro e desabilitado.
- Navigation (`55141:14251`): estrutura de item com ícone, rótulo e indicador tonal para seleção.
- Estilos M3: Roboto e escala tipográfica padrão do Material 3.

## Aplicação no Android

Os componentes do Figma foram adaptados para seus equivalentes nativos em Jetpack Compose Material 3; não são imagens de interface ou componentes web incorporados.

- Cartões compartilhados: superfícies tonais sem contorno decorativo, raio 12.
- Ações rápidas: ícone de contorno de 24 em suporte circular de 40, espaçamento 16 e texto de apoio 14/20. Apenas a ação principal recebe destaque azul.
- Tipografia: escala Material 3 nativa, com Roboto padrão do Android.
- Navegação: ícones de contorno e rótulos pequenos, preservando as abas e rotas existentes.
- Tema: papéis de surface-container e formas centralizados no MaterialTheme.
- Campos: mantidos OutlinedTextField nativos, com raio 12 da identidade do app; botões principais em cápsula.

A paleta azul escura e os textos do app foram preservados. As imagens de exemplo e o avatar do cartão de demonstração do kit não são conteúdo do Águia Inova e não foram incorporados.

## Validação

- `git diff --check`: sem erros de whitespace.
- Revisão dos locais de chamada de CartaoAcao: compatíveis com a assinatura atual.
- `:app:assembleDebug :app:testDebugUnitTest`: bloqueados antes da compilação por `java.io.IOException: Unable to establish loopback connection`, inclusive com o Java do Android Studio e seletor alternativo.
- A imagem `previa-material3.png` é uma simulação visual, não uma captura do Android em execução. Os ícones e a fonte são aproximações no renderizador Windows.
