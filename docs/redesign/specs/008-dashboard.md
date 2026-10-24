# SPEC-RD-008 — Dashboard executivo e gráficos

**Prioridade:** P1 • **Dependências:** RD-002, RD-004 • **Requisitos:** RF-08, RNF-02/03.

## Objetivo

Organizar KPIs, comparações, evolução e prazos em uma leitura executiva clara, sem alterar métricas calculadas pelo backend. Evitar que gráficos bonitos escondam valores negativos, zero ou dados ausentes.

## Arquivos e contratos

Modificar `ui/dashboard/{DashboardScreen,DashboardViewModel}.kt`, `ui/components/Graficos.kt`; extrair `ui/dashboard/ComponentesDashboard.kt` quando ajudar a separar seções. Preservar `DadosDashboard`, `ResumoDashboard`, `ItemGrafico`, assinaturas públicas de `GraficoBarras`/`GraficoColunas` e filtros existentes.

Criar `ui/dashboard/DashboardViewModelTest.kt`, `ui/dashboard/DashboardConteudoTest.kt` e `ui/components/GraficosTest.kt` no source set de teste.

## Como implementar

- [ ] Agrupar KPIs em resultado financeiro, eficiência e carteira/prazos, mantendo as métricas existentes. Usar cartões responsivos; valor nulo permanece “—”, não vira zero.
- [ ] Filtro de estratégia atualiza resumo/evolução pelo VM; o comparativo `porEstrategia()` continua global e deve ter rótulo explícito.
- [ ] Gráficos conservam rótulos e apresentam valores textuais acessíveis; meses numerosos podem rolar horizontalmente dentro do gráfico, sem rolagem horizontal da página inteira.
- [ ] Corrigir o caso observado em `Graficos.kt`: a fração mínima atual desenha barra para zero. Zero deve ter valor textual e nenhuma magnitude positiva aparente.
- [ ] Negativos mantêm sinal numérico e representação distinguível; se usar magnitude absoluta para o comprimento, explicitar a convenção e não apresentar todos os valores como ganhos. Não alterar o dado original.
- [ ] Listas vazias mostram explicação contextual; próximo prazo expõe atraso em texto, data, progresso e status.
- [ ] Reservar a seção de insights para RD-009. Não disparar IA ao abrir/atualizar dashboard.

## Testes e aceite

- [ ] VM: filtro encaminha estrategiaId a resumo/evolução; comparativo global mantém seu contrato; erro não zera métricas anteriores.
- [ ] `GraficosTest`: dados `[0, 100, -50]` mantêm rótulos/sinais, zero não produz barra positiva e todos os valores são acessíveis pela semântica/texto.
- [ ] Período vazio, um único mês e 24 meses não colidem com rótulos. Não escrever teste que apenas repete `maxOf`/fração da implementação.
- [ ] Moeda extensa e ROI indisponível são legíveis em largura 360 dp/fonte 150%.
- [ ] Dashboard permanece exclusivo da liderança. Gráficos recebem dados, não chamam repositórios.
- [ ] Validar visualmente KPIs, comparações, evolução e próximos prazos com dados determinísticos e integração real descartável.

**Fora do escopo:** biblioteca de BI, exportação, novas fórmulas, paginação de API e promessa de totais completos acima do teto do backend.
