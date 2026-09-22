<#
.SYNOPSIS
    Teste rapido de ponta a ponta da API Aguia Inova (login, permissoes por perfil, fluxo ideia -> projeto -> dashboard, IA).

.DESCRIPTION
    Precisa da API no ar com os dados de exemplo (docker compose up). Cria uma ideia e um projeto de teste; o projeto e
    removido no final, mas a ideia aprovada permanece (a API so deixa excluir ideias em analise). Rode em ambiente de
    desenvolvimento. Termina com codigo 1 se qualquer verificacao falhar.

.EXAMPLE
    pwsh scripts/smoke-test.ps1 -Api http://localhost:8080
#>
param(
    [string]$Api = "http://localhost:8080",
    [string]$Senha = "123456"
)

$ErrorActionPreference = "Stop"
$script:falhas = 0
$script:total = 0

function Chamar {
    param([string]$Metodo, [string]$Caminho, [string]$Token, $Corpo)
    $cabecalhos = @{}
    if ($Token) { $cabecalhos["Authorization"] = "Bearer $Token" }
    $parametros = @{ Uri = "$Api$Caminho"; Method = $Metodo; Headers = $cabecalhos; UseBasicParsing = $true }
    if ($null -ne $Corpo) {
        $json = $Corpo | ConvertTo-Json -Depth 6
        $parametros["Body"] = [System.Text.Encoding]::UTF8.GetBytes($json)
        $parametros["ContentType"] = "application/json; charset=utf-8"
    }
    try {
        $resposta = Invoke-WebRequest @parametros
        $status = [int]$resposta.StatusCode
        # Le os bytes e decodifica como UTF-8: o Windows PowerShell 5.1 devolve byte[] (ou ISO-8859-1) para alguns tipos.
        $texto = [System.Text.Encoding]::UTF8.GetString($resposta.RawContentStream.ToArray())
        $dados = if ($texto) { $texto | ConvertFrom-Json } else { $null }
    } catch {
        if ($null -eq $_.Exception.Response) { throw "Sem resposta de $Api ($($_.Exception.Message)). A API esta no ar?" }
        $status = [int]$_.Exception.Response.StatusCode
        $dados = $null
    }
    [pscustomobject]@{ Status = $status; Dados = $dados }
}

function Conferir {
    param([string]$Descricao, [bool]$Ok, [string]$Detalhe = "")
    $script:total++
    if ($Ok) {
        Write-Host ("  [ok]    " + $Descricao) -ForegroundColor Green
    } else {
        $script:falhas++
        Write-Host ("  [FALHA] " + $Descricao + $(if ($Detalhe) { "  -> " + $Detalhe } else { "" })) -ForegroundColor Red
    }
}

function ConferirStatus {
    param([string]$Descricao, $Resposta, [int]$Esperado)
    Conferir "$Descricao (HTTP $Esperado)" ($Resposta.Status -eq $Esperado) "recebido HTTP $($Resposta.Status)"
}

function ConferirSucesso {
    param([string]$Descricao, $Resposta)
    Conferir "$Descricao (HTTP 2xx)" ($Resposta.Status -ge 200 -and $Resposta.Status -lt 300) "recebido HTTP $($Resposta.Status)"
}

function Entrar([string]$Email) {
    $r = Chamar "POST" "/api/auth/login" $null @{ email = $Email; senha = $Senha }
    if ($r.Status -ne 200) { throw "Login de $Email falhou (HTTP $($r.Status)). O banco tem os dados de exemplo?" }
    $r.Dados.accessToken
}

Write-Host "`nAguia Inova - teste rapido em $Api`n" -ForegroundColor Cyan

Write-Host "Saude e autenticacao"
$saude = Chamar "GET" "/actuator/health"
Conferir "API saudavel" ($saude.Status -eq 200 -and $saude.Dados.status -eq "UP") "HTTP $($saude.Status)"
$op = Entrar "vinicius@gmail.com"
$gestor = Entrar "anna@gmail.com"
$lider = Entrar "gabriela@gmail.com"
Conferir "login dos tres perfis devolve token" ($op -and $gestor -and $lider)
$eu = Chamar "GET" "/api/auth/me" $lider
Conferir "/me devolve o perfil LIDERANCA" ($eu.Dados.perfil -eq "LIDERANCA") "perfil=$($eu.Dados.perfil)"
$semSenha = Chamar "POST" "/api/auth/login" $null @{ email = "anna@gmail.com"; senha = "senha-errada" }
ConferirStatus "senha errada e recusada" $semSenha 401
ConferirStatus "rota protegida sem token" (Chamar "GET" "/api/ideias") 401

Write-Host "`nPermissoes por perfil"
$estrategiaNova = @{ titulo = "Estrategia de teste"; descricao = "Nao deve ser criada"; categoria = "Teste" }
ConferirStatus "operador nao cria estrategia" (Chamar "POST" "/api/estrategias" $op $estrategiaNova) 403
ConferirStatus "gestor nao cria estrategia" (Chamar "POST" "/api/estrategias" $gestor $estrategiaNova) 403
ConferirStatus "operador nao ve projetos" (Chamar "GET" "/api/projetos" $op) 403
ConferirStatus "operador nao ve o dashboard" (Chamar "GET" "/api/dashboard/resumo" $op) 403
ConferirStatus "gestor nao ve o dashboard" (Chamar "GET" "/api/dashboard/resumo" $gestor) 403
ConferirStatus "lideranca nao lista ideias" (Chamar "GET" "/api/ideias" $lider) 403
ConferirStatus "operador nao usa a IA do gestor" (Chamar "POST" "/api/ia/ideias/avaliar-pendentes" $op) 403
ConferirStatus "todos leem estrategias (operador)" (Chamar "GET" "/api/estrategias" $op) 200
ConferirStatus "lideranca le o historico de estrategias" (Chamar "GET" "/api/estrategias/historico" $lider) 200
ConferirStatus "lideranca le projetos" (Chamar "GET" "/api/projetos" $lider) 200

Write-Host "`nFluxo: ideia (operador) -> aprovacao (gestor) -> projeto (gestor) -> dashboard (lideranca)"
$estrategias = Chamar "GET" "/api/estrategias?status=VIGENTE" $op
$estrategiaId = if ($estrategias.Dados.Count -gt 0) { $estrategias.Dados[0].id } else { $null }
$sufixo = Get-Date -Format "yyyyMMdd-HHmmss"
$ideiaNova = Chamar "POST" "/api/ideias" $op @{
    titulo = "Ideia de teste $sufixo"; descricao = "Criada pelo smoke-test para validar o fluxo completo."
    categoria = "Teste"; estrategiaId = $estrategiaId
}
ConferirSucesso "operador cria ideia" $ideiaNova
$ideiaId = $ideiaNova.Dados.id
Conferir "ideia nasce EM_ANALISE" ($ideiaNova.Dados.status -eq "EM_ANALISE") "status=$($ideiaNova.Dados.status)"
Conferir "operador nao ve a nota da IA" ($null -eq $ideiaNova.Dados.avaliacaoIa)

$aprovada = Chamar "PATCH" "/api/ideias/$ideiaId/avaliacao" $gestor @{ status = "APROVADA"; prioridade = "ALTA"; parecer = "Aprovada no smoke-test" }
ConferirSucesso "gestor aprova a ideia" $aprovada
Conferir "status APROVADA" ($aprovada.Dados.status -eq "APROVADA") "status=$($aprovada.Dados.status)"
ConferirStatus "operador nao aprova" (Chamar "PATCH" "/api/ideias/$ideiaId/avaliacao" $op @{ status = "APROVADA"; prioridade = "BAIXA" }) 403

$prazo = (Get-Date).AddDays(60).ToString("yyyy-MM-dd")
$projeto = Chamar "POST" "/api/projetos" $gestor @{
    nome = "Projeto de teste $sufixo"; descricao = "Projeto criado pelo smoke-test."; investimento = 10000
    prazo = $prazo; estrategiaId = $estrategiaId; ideiaId = $ideiaId
}
ConferirSucesso "gestor cria projeto a partir da ideia aprovada" $projeto
$projetoId = $projeto.Dados.id
ConferirSucesso "gestor atualiza o andamento" (Chamar "PATCH" "/api/projetos/$projetoId/progresso" $gestor @{ status = "EM_ANDAMENTO"; etapa = "EXECUCAO"; progresso = 40 })
$resultado = Chamar "POST" "/api/projetos/$projetoId/resultados" $gestor @{
    descricao = "Resultado de teste"; retornoFinanceiro = 15000; ganhoProdutividade = 5; reducaoCustos = 2
}
ConferirSucesso "gestor registra resultado (retorno financeiro)" $resultado
ConferirStatus "operador nao registra resultado" (Chamar "POST" "/api/projetos/$projetoId/resultados" $op @{ descricao = "x"; retornoFinanceiro = 1 }) 403

$resumo = Chamar "GET" "/api/dashboard/resumo" $lider
ConferirStatus "lideranca abre o dashboard" $resumo 200
Conferir "dashboard traz ROI e projetos" ($null -ne $resumo.Dados.roiPercentual -and $resumo.Dados.totalProjetos -ge 1) "totalProjetos=$($resumo.Dados.totalProjetos)"
ConferirStatus "relatorio por estrategia (grafico)" (Chamar "GET" "/api/dashboard/estrategias" $lider) 200
ConferirStatus "relatorio de evolucao mensal (grafico)" (Chamar "GET" "/api/dashboard/evolucao" $lider) 200

Write-Host "`nIA (com chave configurada usa o modelo; sem chave usa regras locais)"
$status = Chamar "GET" "/api/ia/status" $gestor
ConferirStatus "status da IA" $status 200
Write-Host ("        provedor disponivel: " + $status.Dados.disponivel) -ForegroundColor DarkGray
$ranking = Chamar "POST" "/api/ia/ideias/avaliar-pendentes" $gestor
ConferirStatus "gestor prioriza ideias pendentes" $ranking 200
$insights = Chamar "POST" "/api/ia/dashboard/insights" $lider
ConferirStatus "lideranca gera insights do dashboard" $insights 200

Write-Host "`nLimpeza"
ConferirSucesso "gestor remove o projeto de teste" (Chamar "DELETE" "/api/projetos/$projetoId" $gestor)

Write-Host ""
if ($script:falhas -eq 0) {
    Write-Host "Tudo certo: $($script:total) verificacoes passaram." -ForegroundColor Green
    exit 0
}
Write-Host "$($script:falhas) de $($script:total) verificacoes falharam." -ForegroundColor Red
exit 1
