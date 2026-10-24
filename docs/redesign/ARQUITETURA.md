# Arquitetura e código limpo do redesign

## Estrutura preservada

Raiz Kotlin: `mobile/app/src/main/java/br/com/fiap/aguiainova/`.

```text
navigation/          rotas e composição dos destinos
core/di/             AppContainer e montagem de dependências
core/network/        cliente, interceptadores e Resultado<T>
core/session/        sessão e configuração debug do servidor
domain/model/        entidades, comandos e enums existentes
domain/repository/   contratos de acesso a dados
data/remote/api/     endpoints Retrofit
data/repository/     implementações remotas
ui/theme/           tokens de cor, forma, tipografia e semântica de domínio
ui/components/      componentes visuais reutilizáveis entre funcionalidades
ui/common/          estado, ciclo de vida e obtenção de ViewModel
ui/<funcionalidade>/ telas, conteúdo visual e componentes específicos
util/               formatadores já compartilhados
```

Manter módulo único e DI manual. Não criar diretórios `helpers`, `managers` ou `utils` novos para depositar responsabilidades sem dono. Componentes de uma funcionalidade ficam perto dela; só promover a `ui/components` quando houver uso real entre funcionalidades.

## Separação de responsabilidades

| Unidade | Deve fazer | Não deve fazer |
|---|---|---|
| `XScreen` | Obter VM, coletar StateFlow com lifecycle, ligar efeitos e navegação | Calcular ROI, chamar Retrofit, criar fixtures |
| `XConteudo` | Renderizar estado e emitir callbacks | Obter AppContainer, NavController ou VM |
| ViewModel | Carregar/executar via interfaces, validar ações e expor estado | Decidir padding, cor, fonte ou construir árvore Compose |
| Repository | Implementar contrato remoto e Resultado | Conhecer ícones, strings de layout ou NavController |
| Componente comum | UI acessível parametrizada por dados/ações | Entender permissões de todos os domínios num grande switch |

Preservar nomes públicos usados por `NavGraph`. A extração é incremental: primeiro retirar conteúdo renderizável das telas que serão alteradas, sem mover toda a árvore de arquivos de uma vez.

## Interfaces alvo

As assinaturas abaixo são decisões propostas, não APIs já implementadas. Raiz e nomes existentes continuam válidos.

```kotlin
@Composable
fun HomeConteudo(
    perfil: Perfil,
    resumo: ResumoHome,
    aoAbrirIdeias: () -> Unit,
    aoAbrirEstrategias: () -> Unit,
    aoAbrirProjetos: () -> Unit,
    aoCriarProjeto: () -> Unit,
    aoAbrirAssistente: () -> Unit,
    aoAbrirDashboard: () -> Unit,
    aoAbrirHistorico: () -> Unit,
    modifier: Modifier = Modifier
)
```

`HomeScreens.kt` continua como adaptação de navegação/carregamento; `HomeConteudo.kt` concentra a composição dos três perfis e usa funções privadas para seções distintas. Não multiplicar componentes de KPI por perfil.

Extensões compatíveis dos componentes existentes, em seus arquivos atuais:

```kotlin
// Novos parâmetros opcionais vão ao fim; manter chamadas atuais válidas.
// CampoTexto: erro: String? = null
// BotaoPrimario: trocar altura fixa por heightIn(min = 52.dp)
// MensagemVazia: acrescentar acaoTexto: String? = null e aoAcao: (() -> Unit)? = null
```

Em `CampoTexto`, `erro` prevalece sobre dica, aciona `isError` e semântica de erro; a validação que produz esse texto pertence ao fluxo/VM. Os campos de senha continuam mascarados. Não criar wrappers duplicados para cada campo de negócio.

Para outras telas, extrair `XConteudo` somente quando necessário a preview/teste ou à legibilidade. Os parâmetros devem usar os modelos de estado existentes e callbacks tipados; nenhum `Map<String, Any>`, mapa de ações por string ou objeto “universal” de configuração de tela.

## Regras práticas

- Nomes de domínio em português; seguir o idioma técnico existente. Não renomear enums serializados, campos Gson ou rotas por estética.
- `Modifier` recebido é aplicado ao contêiner raiz da UI. Componentes folha não impõem largura/rolagem do pai sem necessidade.
- Um dono para cada estado: negócio no VM, estado transitório de UI local; `rememberSaveable` quando deve sobreviver à recriação. Não reinicializar formulário a cada refresh.
- `Estado<T>` e `Resultado<T>` continuam sendo os contratos. Não introduzir uma segunda hierarquia de loading/error por funcionalidade.
- Efeitos em `LaunchedEffect`/`AoRetomar` com chave estável; nenhuma ação de escrita no corpo de um composable.
- Extrair funções por responsabilidade, não para cumprir limite arbitrário de linhas. Se arquivo agrupar diálogos/cartões independentes, separar junto à funcionalidade, mantendo imports e visibilidade mínimos.
- Usar `MaterialTheme.colorScheme`, `typography`, `shapes` e tokens de espaçamento compartilhados. `CoresDeDominio.kt` continua responsável por status/prioridade.
- Evitar `Color.White` em superfícies de destaque: usar `onPrimary`, `onError`, `onSurface` ou pares semânticos próprios testados.
- Não adicionar dependências para resolver alinhamento, padding ou um gráfico já implementado. Mudança de dependência deve ter justificativa e validação na spec.
- Strings tocadas podem ser extraídas para `strings.xml` quando forem repetidas ou usadas em acessibilidade; não fazer uma migração de i18n do repositório inteiro.

## Testabilidade e fixtures

Testes de UI determinísticos em `mobile/app/src/test/java/br/com/fiap/aguiainova/ui/`, usando Robolectric/Compose existentes. Novos dados de demonstração ficam em `ui/support/FixturesVisuais.kt` no **source set de teste**, sem import por `src/main`.

Um pequeno teste por comportamento relevante: conteúdo de erro recupera; callback não dispara sozinho; permissão esconde ação; fonte grande permite rolar até salvar. Não testar se uma constante é igual ao valor que acabou de ser escrito. Alteração só visual é validada por composição e inspeção; mudança de comportamento exige teste que falha antes da implementação.

## Backend e contratos

Backend permanece em camadas e IA mantém ports/adapters existentes. Nenhuma spec de redesign altera endpoints por padrão. Se uma necessidade visual revelar incompatibilidade de contrato, registrar a mudança separadamente, revisar `docs/API.md` e validar suíte backend com `./mvnw verify`; não adaptar DTO silenciosamente na tela.

Não converter o esquema MongoDB para JPA, nem adicionar persistência local ao mobile. Permissões de UI nunca substituem autorização do servidor.
