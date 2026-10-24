Add-Type -AssemblyName System.Drawing
$bitmap = [System.Drawing.Bitmap]::new(1040,1100)
$g = [System.Drawing.Graphics]::FromImage($bitmap)
$g.SmoothingMode = 'AntiAlias'
$g.TextRenderingHint = 'AntiAliasGridFit'
function C($hex) { [System.Drawing.ColorTranslator]::FromHtml($hex) }
function Rect($x,$y,$w,$h,$color,$r=16) {
    $p = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $d = $r*2
    $p.AddArc($x,$y,$d,$d,180,90); $p.AddArc(($x+$w-$d),$y,$d,$d,270,90)
    $p.AddArc(($x+$w-$d),($y+$h-$d),$d,$d,0,90); $p.AddArc($x,($y+$h-$d),$d,$d,90,90); $p.CloseFigure()
    $b = [System.Drawing.SolidBrush]::new((C $color)); $g.FillPath($b,$p); $b.Dispose(); $p.Dispose()
}
function Txt($t,$x,$y,$size=14,$color='#FFFFFF',$bold=$false,$w=380,$h=80) {
    $weight = if($bold){[System.Drawing.FontStyle]::Bold}else{[System.Drawing.FontStyle]::Regular}
    $font = [System.Drawing.Font]::new('Segoe UI',$size,$weight,[System.Drawing.GraphicsUnit]::Pixel)
    $b = [System.Drawing.SolidBrush]::new((C $color))
    $g.DrawString($t,$font,$b,[System.Drawing.RectangleF]::new($x,$y,$w,$h)); $font.Dispose(); $b.Dispose()
}
function Card($x,$y,$w,$h,$fill='#131F32',$edge='#131F32') {
    Rect $x $y $w $h $edge 12
    Rect ($x+1) ($y+1) ($w-2) ($h-2) $fill 11
}
function Metric($x,$y,$w,$label,$value,$dot) {
    Card $x $y $w 98
    Rect ($x+16) ($y+20) 6 6 $dot 3
    Txt $label ($x+30) ($y+15) 12 '#A7B7CC' $false ($w-40) 30
    Txt $value ($x+16) ($y+44) 24 '#FFFFFF' $true ($w-28) 42
}
function Action($x,$y,$title,$sub,$icon,$featured=$false) {
    $fill=if($featured){'#1A2B43'}else{'#131F32'}
    $edge=if($featured){'#356981'}else{'#131F32'}
    Card $x $y 371 86 $fill $edge
    $tile=if($featured){'#53C7F5'}else{'#1B3349'}
    $ink=if($featured){'#0B1220'}else{'#53C7F5'}
    Rect ($x+16) ($y+21) 40 40 $tile 20
    Txt $icon ($x+28) ($y+30) 22 $ink $true 30 34
    Txt $title ($x+72) ($y+13) 16 '#FFFFFF' $false 257 25
    Txt $sub ($x+72) ($y+42) 14 '#A7B7CC' $false 244 38
    Txt '›' ($x+342) ($y+29) 25 '#53C7F5' $false 24 35
}
$g.Clear((C '#060C16'))
Txt 'ÁGUIA INOVA' 54 30 14 '#53C7F5' $true
Txt 'Uma interface mais clara. Mais espaço para inovar.' 54 58 26 '#FFFFFF' $true 940 50
Txt 'PRÉVIA VISUAL • baseada no código atualizado • dados ilustrativos' 54 104 12 '#A7B7CC' $false 940 30
$left=66; $right=563; $top=160
foreach($x in @($left,$right)) {
    Rect ($x-7) ($top-7) 425 905 '#26364D' 30
    Rect $x $top 411 891 '#0B1220' 24
    Txt '9:41' ($x+22) ($top+14) 12 '#FFFFFF' $true
    Txt '●  ▰' ($x+347) ($top+14) 12 '#A7B7CC' $false 60 22
}
$x=$left+20; $y=$top+54
Txt 'Águia Inova' $x $y 24 '#FFFFFF' $true
Txt 'Gestão da inovação empresarial' $x ($y+38) 14 '#A7B7CC'
Rect $x ($y+100) 64 64 '#142C3D' 20
Txt '✦' ($x+18) ($y+113) 32 '#53C7F5' $true 46 48
Txt 'Grandes mudanças começam com uma ideia.' $x ($y+180) 28 '#FFFFFF' $true 371 112
Txt 'Entre para conectar ideias, acompanhar projetos e transformar resultados.' $x ($y+297) 14 '#A7B7CC' $false 371 60
Card $x ($y+378) 371 56 '#0B1220' '#597086'
Txt 'E-mail' ($x+16) ($y+396) 16 '#A7B7CC'
Card $x ($y+446) 371 56 '#0B1220' '#597086'
Txt 'Senha' ($x+16) ($y+464) 16 '#A7B7CC'
Rect $x ($y+522) 371 52 '#53C7F5' 26
Txt 'Entrar' ($x+163) ($y+537) 14 '#0B1220' $true 80 28
Txt 'Criar conta' ($x+148) ($y+599) 14 '#53C7F5' $true
$x=$right+20; $y=$top+54
Txt 'Painel do Gestor' $x $y 24 '#FFFFFF' $true
Txt 'Gerencie ideias, projetos e acompanhe resultados.' $x ($y+38) 14 '#A7B7CC' $false 371 50
Metric $x ($y+102) 179 'Ideias pendentes' '2' '#53C7F5'
Metric ($x+191) ($y+102) 180 'Projetos ativos' '2' '#55D9B1'
Metric $x ($y+212) 371 'Retorno parcial' 'R$ 298.000,00' '#FFCF70'
Txt 'Ações rápidas' $x ($y+335) 16 '#FFFFFF' $true
Action $x ($y+375) 'Aprovar ideias' 'Analise, priorize e aprove ideias enviadas' '✦' $true
Action $x ($y+473) 'Assistente de IA' 'Converse para escolher ideias e montar projetos' '✧'
Action $x ($y+571) 'Criar projeto' 'Cadastre novas iniciativas' '▣'
Action $x ($y+669) 'Atualizar progresso' 'Acompanhe indicadores e entregas' '✓'
Rect $right ($top+805) 411 86 '#0B1220' 10
$labels=@('Home','Ideias','Projetos','Estratégias','Perfil'); $icons=@('⌂','✦','▣','↗','●')
for($i=0;$i -lt 5;$i++) {
    $nx=$right+5+$i*81
    if($i -eq 0){Rect ($nx+10) ($top+811) 60 30 '#142C3D' 15}
    $color=if($i -eq 0){'#53C7F5'}else{'#A7B7CC'}
    Txt $icons[$i] ($nx+31) ($top+813) 22 $color $true 40 33
    Txt $labels[$i] ($nx+8) ($top+850) 11 $color $false 81 24
}
Txt 'LOGIN' 66 1066 12 '#A7B7CC' $true
Txt 'PAINEL DO GESTOR' 563 1066 12 '#A7B7CC' $true
$bitmap.Save((Join-Path $PSScriptRoot 'previa-material3.png'),[System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose(); $bitmap.Dispose()
