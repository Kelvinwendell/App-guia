# Convenções — Águia Inova

Padrões que o código já segue de verdade (não aspiracionais). Ao adicionar código novo, siga
o mesmo padrão em vez de introduzir um estilo diferente.

## Backend (Java)

- **Nomes de domínio em português** (`Ideia`, `Projeto`, `Estrategia`, `avaliar`, `criar`,
  `listar`), nomes técnicos de infraestrutura em inglês quando for termo padrão da stack
  (`Repository`, `Service`, `Controller`, `DTO`). Siga o idioma que já está no arquivo que
  você está editando.
- **Sem comentário de javadoc genérico.** Comentário só quando explica um porquê não óbvio
  (uma decisão, uma armadilha, uma regra de negócio escondida) — nunca repete o que o nome do
  método/classe já diz.
- **Construtor com todas as dependências explícitas**, nunca `@Autowired` em campo. Isso é o
  que torna todo serviço testável sem subir o contexto do Spring.
- **Filtro dinâmico opcional em consulta:** use `util/Consultas.seIgual`/`seInformado`/
  `seInformadoIgnorandoCaixa` em vez de escrever `if (x != null) addCriteria(...)` de novo.
- **Propriedade de configuração nova:** adicione como um record aninhado em
  `config/AppProperties.java` (siga o padrão de `RateLimit`/`Dashboard`), nunca
  `@Value` solto num campo. Lembre-se: `AppProperties` é um record posicional — adicionar um
  campo quebra toda construção manual direta (`new AppProperties(...)`) em teste; os pontos
  atuais são `support/Dados.java` e `config/SecurityConfigTest.java`. Rode a suíte completa
  depois, não só o teste que você escreveu.
- **Erro esperado da aplicação:** crie uma subclasse de `exception/ApiException`, nunca lance
  `RuntimeException` crua nem adicione `instanceof` em `ApiExceptionHandler` — o handler já
  despacha por `status()` polimórfico.
- **Teste de integração:** estenda `support/AbstractIntegrationTest` (contexto completo +
  Testcontainers + banco limpo a cada teste) e use os fixtures de `support/Dados.java`
  (objetos de domínio sem banco) e `support/Cenarios.java` (dados criados pelos próprios
  serviços da aplicação, não inseridos direto no banco — mantém regra de negócio e histórico
  consistentes).
- **`backend/src/test/resources/application.yml` existe e precisa ficar completo.** O Spring
  Boot carrega só o primeiro `classpath:/application.yml` que encontra — não funde main e
  test. Se adicionar uma chave nova em `src/main/resources/application.yml`, replique em
  `src/test/resources/application.yml` também, ou os testes de integração quebram em massa
  por falta de bind de propriedade.

## Mobile (Kotlin)

- **Um domínio novo segue a forma dos 6 existentes:** `data/remote/api/XApi.kt` →
  `data/repository/XRepositoryRemoto.kt` (implementa `domain/repository/XRepository`) →
  `ui/x/XViewModel.kt` (herda `BaseViewModel`) → tela Compose em `ui/x/XScreen.kt`. Não
  invente uma variação.
- **Toda chamada de rede retorna `Resultado<T>`** (`core/network/Resultado.kt`), nunca deixe
  uma exceção de rede crua subir até a UI.
- **Teste de classe que depende de `Context` do Android:** use Robolectric
  (`@RunWith(RobolectricTestRunner::class)`), não tente mockar `Context` na mão. Veja
  `CapturasDeTelaTest.kt` e `ConfiguracaoServidorEditavelTest.kt` como referência.
- **Segredo/configuração sensível de build** (assinatura de release, por exemplo): leia só de
  variável de ambiente no `build.gradle.kts`, nunca hardcoded nem em arquivo versionado — veja
  o padrão de `signingConfigs` condicional já existente.

## Desenvolvimento orientado a teste (TDD)

Disciplina usada para construir as proteções de produção deste projeto e que vale manter:

1. **RED** — escreva/ajuste um teste que expressa o comportamento desejado e confirme que ele
   falha antes de implementar (prova que o teste testa algo real).
2. **GREEN** — implemente o mínimo necessário para o teste passar.
3. **REGRESSÃO** — rode a suíte inteira, não só o teste novo. Mudança de assinatura/contrato
   (como adicionar um campo a um record de propriedades) costuma quebrar coisa em lugar
   nenhum óbvio — é a regressão completa que pega isso.
4. Se RED não virou GREEN de primeira, corrija a implementação (não o teste, a menos que o
   teste estivesse errado) e repita 2–3 até fechar.

Refactor que não muda comportamento (como extrair um helper comum) não precisa de teste novo
— a suíte existente já é a prova; rode-a entre cada substituição, não só no final.
