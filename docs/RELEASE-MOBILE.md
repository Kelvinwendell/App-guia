# Release do app Android (APK de produção)

O APK de produção precisa ser **assinado com a keystore de release** (`aguia-inova-release.jks`). A keystore e as
senhas nunca vão para o repositório. Há dois caminhos:

| Caminho | Precisa de | Quando usar |
|---|---|---|
| **A. GitHub Actions** (recomendado) | Cadastrar os secrets **uma vez**, num computador | Depois disso dá para gerar do celular, pelo app do GitHub |
| **B. Manual** | Um computador com o repositório, a keystore e o Android SDK | Se os secrets ainda não foram cadastrados |

> **Use sempre a mesma keystore.** Se ela for perdida ou trocada, quem tem o app instalado precisa desinstalar antes
> de atualizar. Guarde o `.jks` e as senhas num gerenciador de senhas.

## A. Pelo GitHub Actions

### A1. Cadastrar os secrets (uma vez, num computador)

1. Gere o conteúdo da keystore em base64. No PowerShell, na pasta onde está o `.jks`:
   ```powershell
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("aguia-inova-release.jks")) | Set-Clipboard
   ```
   (O conteúdo vai para a área de transferência.)
2. No GitHub, abra o repositório → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**
   e crie os quatro:

   | Nome | Valor |
   |---|---|
   | `ANDROID_KEYSTORE_BASE64` | o texto copiado no passo 1 |
   | `KEYSTORE_PASSWORD` | senha da keystore |
   | `KEY_ALIAS` | alias da chave (no README: `aguia-inova`) |
   | `KEY_PASSWORD` | senha da chave (muitas vezes igual à da keystore) |

### A2. Gerar e publicar (dá para fazer pelo celular)

1. GitHub (site ou app) → repositório → **Actions** → **Mobile Release** → **Run workflow**.
2. Preencha:
   - **Tag**: uma tag nova, ex. `v2.2-mobile`.
   - **Pré-release**: marque se for só para teste (não vira o "Latest").
3. O workflow roda os testes, gera o APK assinado apontando para `https://app-guia.onrender.com/`, confere a
   assinatura e publica em **Releases** com o SHA-256.
4. No celular: abra o release, baixe o `aguia-inova-vX.Y.apk` e instale.

Se faltar algum secret, o workflow para logo no primeiro passo dizendo qual.

## B. Manual (num computador)

```powershell
cd C:\Users\Administrador\App-guia\mobile
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:RELEASE_KEYSTORE_PATH = "C:\Users\Administrador\App-guia\aguia-inova-release.jks"
$env:KEYSTORE_PASSWORD = "<senha da keystore>"
$env:KEY_ALIAS = "aguia-inova"
$env:KEY_PASSWORD = "<senha da chave>"
# Nesta máquina o Gradle falha com "Unable to establish loopback connection" sem isto:
$env:TEMP = "C:\gtmp"; $env:TMP = "C:\gtmp"
.\gradlew.bat assembleRelease -PapiBaseUrl=https://app-guia.onrender.com/
```

- Saída: `mobile\app\build\outputs\apk\release\app-release.apk`. Se sair `app-release-unsigned.apk`, alguma
  variável ficou vazia.
- Publicar: `gh release create v2.2-mobile mobile/app/build/outputs/apk/release/app-release.apk --title "Águia Inova — APK v2.2"`
  ou pelo site (Releases → Draft a new release).
- Instalar direto num celular ligado por USB (depuração USB ativa):
  `adb install -r mobile\app\build\outputs\apk\release\app-release.apk`

## Antes de cada release

- Suba `versionCode` (sempre +1) e `versionName` em `mobile/app/build.gradle.kts`, senão o Android recusa instalar
  por cima da versão anterior.
- O build de release só aceita HTTPS (HTTP sem criptografia é liberado só no debug) e não usa minificação.
- O backend vai para o Render sozinho quando o Backend CI passa na `main`.

## Rodar tudo localmente (apresentação ou teste sem internet)

Na máquina de desenvolvimento há um script, fora do repositório, que sobe MongoDB, API (código da `main`),
emulador e app de debug apontando para a API local:

```powershell
powershell -ExecutionPolicy Bypass -File C:\ferramentas\rodar-aguia-local.ps1
```

Contas de demonstração: `helena.duarte@aguiainova.demo` (Liderança), `marcos.oliveira@aguiainova.demo` (Gestor),
`juliana.costa@aguiainova.demo` (Operadora), com a senha definida em `APP_SEED_DEMO_SENHA` no script.
Em outra máquina, siga o README (seção do `docker compose`) para subir API e banco.
