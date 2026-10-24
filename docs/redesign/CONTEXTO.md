# Contexto consolidado

## Produto e intenção

Águia Inova conecta ideias de colaboradores, avaliação por gestores, projetos e estratégia empresarial. O operador registra ideias; o gestor decide e acompanha projetos; a liderança acompanha resultados e define estratégias. O backend já oferece esses recursos; o trabalho atual concentra-se na experiência Android.

O usuário pediu um design mais bonito, minimalista e moderno, aprovou a continuidade do tema escuro e do azul e autorizou consultar seu Figma. A direção aprovada é manter identidade e funcionalidades, diminuir o peso visual dos cartões, melhorar hierarquia e legibilidade e dar destaque à próxima ação relevante.

## Fontes e recorte de evidência

Snapshot: HEAD `ac827f3`, branch observada `main`, com alterações locais não commitadas em oito arquivos Kotlin mobile e artefatos em `docs/design/`. Durante a revisão documental também foi detectada alteração em `mobile/gradle.properties`, externa à escrita deste pacote. Preservá-la e inspecioná-la em RD-000; nenhuma dessas mudanças foi revertida. Revalidar esse estado ao iniciar execução; não sobrescrever mudanças posteriores do usuário.

Fontes locais lidas: README raiz, `.context/`, navegação, ViewModels, repositórios, telas Compose, testes, arquivos Gradle, POM, Dockerfile mobile e CI backend. Links antigos para `.context/specs/` e `.context/STATUS.md` aparecem no README, mas esses caminhos não estavam presentes no inventário desta sessão. Não tratá-los como specs acessíveis.

Referência visual efetivamente consultada:

- [Kit oficial Material 3](https://www.figma.com/community/file/1035203688168086460/material-3-design-kit).
- [Cópia criada na equipe do usuário](https://www.figma.com/design/93Zr4oJqgrTpHQTA6obBKh/Material-3-Design-Kit--Community-).
- Componentes inspecionados: cartão horizontal Filled `52350:28197`, campo Outlined Enabled `52798:24903`, página Navigation `55141:14251`; estilos Roboto do kit.
- Registro local detalhado: [material3-figma.md](../design/material3-figma.md).

A cópia do kit é uma biblioteca de referência; **não contém um redesign completo das telas do Águia Inova**. As imagens `previa-design.png` e `previa-material3.png` são simulações desenhadas no Windows, com dados ilustrativos; não são capturas do app, nem prova de fidelidade da fonte ou dos ícones Android.

## Stack observada no repositório

| Parte | Configuração observada |
|---|---|
| App | Kotlin, Jetpack Compose Material 3, MVVM, DI manual por AppContainer |
| Android | minSdk 26, compileSdk/targetSdk 36, versionCode 2, versionName 2.0 |
| Build mobile | Gradle wrapper 9.1.0; AGP 9.0.1; plugin Compose Kotlin 2.0.21; BOM Compose 2024.09.00 |
| Compilação Kotlin/Java | Toolchain e bytecode configurados em Java 17; compatibilidade do runtime Gradle precisa ser validada |
| Rede | Retrofit 2.11.0, OkHttp 4.12.0, Gson; modelos de domínio usados na desserialização |
| Backend | Java 25, Spring Boot 4.1.1 no POM, MongoDB, Spring Security/JWT |
| Testes | JUnit, coroutines-test, Robolectric, Compose UI Test, MockWebServer; backend com Testcontainers |
| CI | Apenas `.github/workflows/backend-ci.yml`; não há job mobile nesta baseline |

São versões declaradas localmente, não uma afirmação de compatibilidade já verificada. RD-000 deve validar a combinação antes de qualquer atualização de dependência.

## Estado técnico atual

Já foram editados `Color.kt`, `Theme.kt`, `Type.kt`, `Cartoes.kt`, `Campos.kt`, `Layout.kt`, `HomeScreens.kt` e `LoginScreen.kt`. Isso estabelece uma primeira direção, mas listas, formulários, gráficos, estados e contraste ainda precisam de revisão específica. O uso global da nova paleta pode afetar elementos que ainda têm texto branco fixo, como balões de chat e botões de erro.

`git diff --check` passou na sessão anterior. Build e testes mobile não chegaram a executar a compilação: `java.io.IOException: Unable to establish loopback connection`. O stack trace mostrou falha em `PipeImpl`/sockets locais do Java no Windows; trocar runtime e seletor não resolveu. A causa final do ambiente não foi confirmada. Não concluir que há erro no código, nem que o código compila.

O SDK foi localizado em `C:/Users/Administrador/AppData/Local/Android/Sdk` e há runtime do Android Studio instalado. São caminhos desta máquina, não configuração portátil a versionar.

A alteração observada em `mobile/gradle.properties` fixa `apiBaseUrl=https://app-guia.onrender.com/`. RD-000 deve registrar esse valor e os testes interativos devem sobrescrevê-lo no build para a API descartável; não testar criação/exclusão usando inadvertidamente a configuração de produção.

## Contexto que permanece válido

- Estados compartilhados: `Estado.Carregando`, `Estado.Erro`, `Estado.Pronto`; atualização com falha mantém dados anteriores em `BaseViewModel`.
- Rede retorna `Resultado<T>`; sessão expirada conduz ao login; autorização final permanece no servidor.
- IA é opcional. Avaliação e insights podem usar heurística identificada; chat sem provedor não inventa resposta.
- Há testes de contrato e capturas reais condicionados a variáveis de ambiente. Sem elas, esses testes são ignorados.
- A URL de produção descrita no README não teve disponibilidade revalidada para este PRD. Não usar produção para testes que criam ou removem dados.

## Limites desta iniciativa

Sem migração para web, alteração de banco, novas permissões, novo provedor de IA ou novas regras de cálculo. Não aproveitar o redesign para inserir Hilt, Room, multimódulos ou uma camada de abstração por tela. Mudanças funcionais necessárias à usabilidade devem ser pequenas, explícitas, testadas e pertencentes à spec do fluxo afetado.
