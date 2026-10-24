# SPEC-RD-000 — Baseline e ambiente reproduzível

**Prioridade:** P0 • **Dependências:** nenhuma • **Requisitos:** RF-10, RNF-06.

## Objetivo e situação inicial

Conseguir compilar, testar e renderizar o código local antes de aceitar o redesign. Há mudanças não commitadas em oito arquivos mobile. O erro observado impede inicialização normal do Gradle no Windows e não demonstra incompatibilidade do código.

## Arquivos e limites

Ler `mobile/gradle/wrapper/gradle-wrapper.properties`, `mobile/gradle/libs.versions.toml`, `mobile/gradle.properties`, `mobile/app/build.gradle.kts`, `mobile/Dockerfile` e `mobile/settings.gradle.kts`. Alterar configuração versionada somente se houver incompatibilidade reproduzida e evidência da correção. Criar `entregas/redesign/RD-000/RELATORIO.md` na execução.

Não modificar segurança do Windows, antivírus, firewall, política de acesso, licenças ou arquivos fora do projeto para contornar o ambiente. Instalação/download de dependências segue as permissões da sessão. Não mudar toda a stack para tentar resolver uma falha local de sockets.

## Como executar

- [ ] Registrar `git status --short`, branch, HEAD e resumo do diff; manter alterações do usuário.
- [ ] Identificar Java realmente usado pelo wrapper e pelo daemon, versão instalada do SDK 36 e disponibilidade da toolchain Java 17 declarada no módulo.
- [ ] Registrar o `apiBaseUrl` efetivo: a configuração local aponta para produção. Usar override explícito para API descartável em APKs de teste e `AGUIA_API_URL` separado para testes JVM; nunca criar/remover dados na produção para validar o redesign.
- [ ] Rodar `gradlew --version` e uma tarefa simples com `--stacktrace`; guardar saída. Distinguir erro de processo/socket, download, toolchain, plugin e compilação Kotlin.
- [ ] Validar compatibilidade declarada de AGP/Gradle/runtime/plugin Compose nas fontes oficiais se for necessário alterar versões. Manter Java 25 do backend separado do runtime do build mobile.
- [ ] Se o Windows continuar bloqueado, usar um ambiente Android já autorizado e reproduzível, como runner CI ou contêiner existente. O Dockerfile atual também precisa ter sua compatibilidade validada; sua existência não prova build funcional.
- [ ] Rodar assembleDebug, testDebugUnitTest e lintDebug. Não usar `--offline` como padrão se dependências faltarem.
- [ ] Separar falhas preexistentes de regressões do redesign e registrar diagnóstico. Só fazer ajuste mínimo de configuração quando a causa estiver demonstrada.

## Contrato de saída

Um procedimento repetível com versões, comandos, resultados e uma tela Android renderizada. Se não houver ambiente funcional, produzir relatório do bloqueio e deixar esta spec aberta; specs posteriores podem ser refinadas documentalmente, mas não aceitas como prontas.

## Validação e aceite

- [ ] `:app:assembleDebug` termina com exit 0 e gera APK novo em `mobile/app/build/outputs/apk/debug/app-debug.apk`.
- [ ] `:app:testDebugUnitTest` termina com exit 0; relatório distingue testes executados e ignorados por ausência de API.
- [ ] `:app:lintDebug` termina com exit 0, ou erros preexistentes são corrigidos com escopo identificado antes do aceite.
- [ ] Nenhum token, caminho pessoal de SDK ou segredo foi adicionado ao Git.
- [ ] Registro identifica a baseline real. Não afirmar que telas passaram somente porque o Gradle iniciou.

**Fora do escopo:** redesign adicional, instalação de provedor IA, alteração da API e deploy. Não escrever testes que apenas verificam texto de arquivos de configuração.
