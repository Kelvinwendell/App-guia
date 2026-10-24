# Execução das specs

## Ordem sugerida

Executar sequencialmente nesta iniciativa: RD-000 → RD-001 → RD-002 → RD-003 → RD-004 → RD-005 → RD-006 → RD-007 → RD-008 → RD-009 → RD-010. As dependências mínimas estão no índice; não é necessário trabalho simultâneo para concluir o pacote.

Antes de cada spec, ler seu arquivo, o PRD e as convenções de `.context/`. Verificar `git status` e o diff atual. A baseline foi observada em `main` com trabalho local: preservar essas alterações e criar uma branch `codex/redesign-<id>` quando a execução for iniciada. Não limpar a árvore ou fazer reset para obter isolamento.

## Ciclo de trabalho

1. Confirmar a entrada exigida pela spec anterior e registrar os arquivos que já estavam modificados.
2. Reproduzir os casos de aceite e escrever primeiro o teste de qualquer mudança funcional necessária.
3. Implementar apenas a entrega daquela spec, reutilizando o que já existe.
4. Executar os testes focados; depois, build/testes mobile de regressão para a entrega da spec.
5. Inspecionar tela real/preview Compose conforme a matriz. Simulação externa não substitui validação Android.
6. Revisar diff, imports, permissões e dados fictícios. Registrar comandos, resultados, capturas e limitações.
7. Marcar a spec como concluída apenas com todos os critérios obrigatórios atendidos. Criar commits/PRs somente no fluxo autorizado pelo usuário.

## Comandos de referência

PowerShell, a partir de `mobile/`, após RD-000 estabelecer o ambiente:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain
```

Com JDK e SDK locais válidos, sem versionar caminhos pessoais. Não assumir que `--offline` funcionará sem dependências em cache.

Unix/CI, a partir de `mobile/`:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain
```

Testes de contrato e capturas existentes dependem de API **descartável**, com seed conhecido. `AGUIA_API_URL` é a URL do host que executa os testes JVM, diferente de `10.0.2.2` usado pelo emulador:

O `apiBaseUrl` local foi encontrado apontando para produção. Para instalar um APK de teste no emulador, passar explicitamente `-PapiBaseUrl=http://10.0.2.2:8080/` no build contra a API descartável; em aparelho físico, usar o endereço local autorizado do host. Definir `AGUIA_API_URL` afeta testes JVM e não altera automaticamente a URL compilada no APK.

```powershell
$env:AGUIA_API_URL = 'http://localhost:8080/'
$env:AGUIA_CAPTURAS_DIR = Join-Path $PWD '../entregas/redesign/RD-010/capturas'
.\gradlew.bat :app:testDebugUnitTest --tests '*CapturasDeTelaTest' --console=plain
```

Não executar contrato e capturas concorrentemente no mesmo banco: o teste de contrato cria dados e pode invalidar as expectativas do teste de captura. Preparar um banco descartável limpo por rodada; não rodar comandos que apagam volumes sem conferir o destino e a autorização.

Se tocar backend, executar `./mvnw verify` em `backend/`, com Docker disponível para Testcontainers. O workflow atual usa `test`; o POM inclui explicitamente `**/*Test.java` e `**/*IT.java` no Surefire, portanto esse comando também seleciona os testes de integração existentes. Conferir os relatórios e não inferir aprovação sem executá-los.

## Registro de execução

Criar `entregas/redesign/<RD-ID>/RELATORIO.md` ao executar a spec, com:

- Base e escopo efetivo do diff.
- Critérios atendidos e links para evidências.
- Comandos e códigos de saída; distinguir falhou, passou e ignorado.
- Dispositivo/qualifier, tamanho de fonte e dados usados nas capturas.
- Limitações ou defeitos preexistentes identificados.

Nenhum relatório, APK ou screenshot novo existe por força deste plano. Não copiar o APK antigo de `entregas/` e apresentá-lo como redesign compilado.

## Pronto para revisão

Uma spec está pronta quando seu comportamento e layout passam nos casos descritos, o diff permanece focado e não há bloqueio oculto. Build bloqueado significa validação pendente. Para a entrega global, aplicar integralmente RD-010.

## Prompt para iniciar

> Execute apenas a SPEC-RD-000 em docs/redesign/specs/000-baseline-e-ambiente.md. Leia docs/redesign/README.md, CONTEXTO.md, PRD.md, ARQUITETURA.md e as convenções de .context/. Preserve as alterações locais. Estabeleça uma baseline reproduzível e registre evidências; não avance para as outras specs nem publique ou faça merge automaticamente.
