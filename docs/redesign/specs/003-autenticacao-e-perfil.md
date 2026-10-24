# SPEC-RD-003 — Login, cadastro e perfil

**Prioridade:** P1 • **Dependências:** RD-002 • **Requisitos:** RF-03, RNF-03/04/06.

## Objetivo

Completar as telas de entrada e conta com leitura clara, formulário utilizável com teclado aberto e estados coerentes. A apresentação já adicionada ao login é ponto de partida, não requisito para ocupar uma altura fixa.

## Arquivos e contratos

Modificar `ui/auth/{LoginScreen,RegistroScreen,AuthViewModel}.kt`, `ui/perfil/PerfilScreen.kt`. Extrair conteúdo visual puro junto dessas funcionalidades se necessário aos testes. Não alterar `AuthRepository`, `GerenciadorSessao` nem o contrato `NovoUsuario` por estética.

Criar testes `ui/auth/AuthViewModelTest.kt`, `ui/auth/AutenticacaoConteudoTest.kt` e `ui/perfil/PerfilConteudoTest.kt` em `src/test`. Reutilizar os testes existentes em `core/session/` e `core/network/InterceptadoresTest.kt`.

## Como implementar

- [ ] Login: identidade compacta, mensagem de entrada e campos E-mail/Senha. O botão Entrar deve ser alcançável por rolagem com IME aberta; mensagem decorativa cede espaço antes dos campos essenciais.
- [ ] Cadastro: agrupar Nome/E-mail/Senha e dados profissionais Cargo/Departamento/Unidade. Manter seleção de perfil conforme contrato atual, com comunicação de que o servidor define o acesso concedido.
- [ ] Usar componentes de RD-002, campos mascarados e estados de erro. Validar sem converter senha com `trim` ou transformar credenciais.
- [ ] Preservar mínimo de seis caracteres do cadastro atual; não inventar nova política de senha. Erro de autenticação continua genérico.
- [ ] Manter `rememberSaveable` para edição onde já existe; não persistir credenciais em previews, capturas com dados reais ou arquivos de log.
- [ ] Perfil: nome/e-mail, papel de acesso e dados profissionais com quebra adequada. Ausência de cargo/departamento/unidade não produz “null”. Saída é ação secundária claramente nomeada e com contraste correto.
- [ ] Configuração de servidor permanece estritamente condicionada por `servidorEditavel`; validar release, não só debug.

## Testes e aceite

- [ ] VM com fake AuthRepository: login vazio não chama API; cadastro com senha curta não chama API; sucesso usa perfil retornado pelo servidor, não o selecionado no formulário.
- [ ] UI: loading bloqueia envio, dados permanecem após erro e todos os campos/CTA são alcançáveis em 360 dp, fonte 150% e IME.
- [ ] Perfil mostra campos opcionais sem espaços quebrados; Sair remove sessão e volta ao login sem possibilidade de retorno à tela autenticada.
- [ ] Testes existentes de servidor editável continuam passando. Release não mostra nem permite editar servidor e não libera HTTP sem TLS.
- [ ] Capturas de login/cadastro/perfil usam dados sintéticos. Testes e lint passam.

**Fora do escopo:** recuperar senha, biometria, login social, alteração de perfis pelo app ou mudanças de RBAC no backend.
