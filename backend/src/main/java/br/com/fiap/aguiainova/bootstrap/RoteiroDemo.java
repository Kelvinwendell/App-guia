package br.com.fiap.aguiainova.bootstrap;

import java.util.List;

import br.com.fiap.aguiainova.model.enums.EtapaProjeto;
import br.com.fiap.aguiainova.model.enums.Perfil;
import br.com.fiap.aguiainova.model.enums.Prioridade;
import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;
import br.com.fiap.aguiainova.model.enums.StatusProjeto;

/**
 * O que existe na base de demonstracao, so como dados: uma industria ficticia com cinco contas, seis estrategias,
 * doze ideias e cinco projetos. Datas sao dias relativos a "hoje", para a demonstracao nunca envelhecer. Foi montada
 * para mostrar de uma vez as tres saudes de estrategia (duas em dia, duas em risco e uma sem tracao), o painel com
 * retorno ao longo dos meses e ideias em todos os estados.
 */
final class RoteiroDemo {

    /** Dominio ficticio: nenhum e-mail real recebe nada, e as contas de demonstracao ficam faceis de reconhecer. */
    static final String EMAIL_DA_LIDERANCA = "helena.duarte@aguiainova.demo";

    private RoteiroDemo() {
    }

    record UsuarioDemo(String chave, String nome, String email, Perfil perfil, String cargo, String departamento,
            String unidade) {
    }

    /**
     * @param inicioEmDias        inicio do periodo, em dias a partir de hoje (negativo = no passado)
     * @param retrodatarEmDias    se informado, a estrategia passa a constar como criada ha tantos dias
     * @param descricaoRevisada   se informada, a estrategia e atualizada logo apos criada (gera uma entrada no historico)
     */
    record EstrategiaDemo(String chave, String titulo, String descricao, String categoria, String campanha,
            StatusEstrategia status, List<String> metas, int inicioEmDias, int fimEmDias, Integer retrodatarEmDias,
            String descricaoRevisada) {
    }

    /** @param estrategia chave da estrategia vinculada, ou nulo; @param decisao EM_ANALISE deixa a ideia sem decisao */
    record IdeiaDemo(String chave, String autor, String titulo, String descricao, String categoria, String estrategia,
            StatusIdeia decisao, Prioridade prioridade, String parecer) {
    }

    /** @param diasAtras quantos dias antes de hoje o resultado foi registrado */
    record ResultadoDemo(int diasAtras, String descricao, String retorno, Double ganhoProdutividade,
            Double reducaoCustos) {
    }

    /** @param estrategia chave da estrategia; @param ideia chave da ideia aprovada que originou o projeto */
    record ProjetoDemo(String nome, String descricao, String investimento, int inicioEmDias, int prazoEmDias,
            String estrategia, String ideia, StatusProjeto status, EtapaProjeto etapa, Integer progresso,
            List<ResultadoDemo> resultados) {
    }

    static List<UsuarioDemo> usuarios() {
        return List.of(
                new UsuarioDemo("lideranca", "Helena Duarte", EMAIL_DA_LIDERANCA, Perfil.LIDERANCA,
                        "Diretora de Operações", "Diretoria", "Matriz"),
                new UsuarioDemo("gestor", "Marcos Oliveira", "marcos.oliveira@aguiainova.demo", Perfil.GESTOR,
                        "Gerente de Inovação", "Inovação", "Unidade São Paulo"),
                new UsuarioDemo("juliana", "Juliana Costa", "juliana.costa@aguiainova.demo", Perfil.OPERADOR,
                        "Técnica de Manutenção", "Manutenção", "Unidade São Paulo"),
                new UsuarioDemo("rafael", "Rafael Lima", "rafael.lima@aguiainova.demo", Perfil.OPERADOR,
                        "Operador de Produção", "Produção", "Unidade Campinas"),
                new UsuarioDemo("tatiane", "Tatiane Alves", "tatiane.alves@aguiainova.demo", Perfil.OPERADOR,
                        "Analista de Qualidade", "Qualidade", "Unidade São Paulo"));
    }

    static List<EstrategiaDemo> estrategias() {
        return List.of(
                new EstrategiaDemo("desperdicio", "Redução de desperdício na produção",
                        "Reduzir perdas de material e retrabalho nas linhas de produção, medindo o desperdício por turno.",
                        "Sustentabilidade", "Zero Desperdício 2026", StatusEstrategia.VIGENTE,
                        List.of("Reduzir o desperdício de material em 15% até o fim do período",
                                "Reaproveitar pelo menos 30% das sobras de corte"),
                        -240, 120, null, null),
                new EstrategiaDemo("digital", "Transformação digital do chão de fábrica",
                        "Digitalizar processos e monitorar máquinas em tempo real.", "Tecnologia", "Fábrica 4.0",
                        StatusEstrategia.VIGENTE,
                        List.of("Monitorar 100% das máquinas da linha 3 em tempo real",
                                "Reduzir paradas não planejadas em 20%"),
                        -180, 180, null,
                        "Digitalizar processos, monitorar máquinas em tempo real e usar dados para decidir."),
                new EstrategiaDemo("seguranca", "Segurança e bem-estar do colaborador",
                        "Reduzir acidentes e afastamentos com ergonomia e prevenção.", "Pessoas", "Cuidar Sempre",
                        StatusEstrategia.VIGENTE, List.of("Reduzir os afastamentos por lesão em 25% em 12 meses"),
                        -180, 180, null, null),
                new EstrategiaDemo("energia", "Eficiência energética nas linhas",
                        "Reduzir o consumo de energia por unidade produzida, com medição e troca de equipamentos.",
                        "Sustentabilidade", "Energia Inteligente", StatusEstrategia.VIGENTE,
                        List.of("Reduzir o consumo de energia em 12% até dezembro",
                                "Medir o consumo por máquina em 100% da fábrica"),
                        -90, 270, null, null),
                new EstrategiaDemo("qualidade", "Qualidade e satisfação do cliente",
                        "Elevar a satisfação dos clientes com entregas no prazo e menos devoluções.", "Qualidade",
                        "Cliente em Primeiro Lugar", StatusEstrategia.VIGENTE, List.of(), -45, 300, 45, null),
                new EstrategiaDemo("online", "Expansão dos canais de venda online",
                        "Ampliar as vendas por canais digitais.", "Comercial", "E-commerce 2025",
                        StatusEstrategia.ENCERRADA, List.of("Atingir 15% das vendas pelo canal online"),
                        -540, -90, null, null));
    }

    static List<IdeiaDemo> ideias() {
        return List.of(
                new IdeiaDemo("sobras", "rafael", "Reaproveitar sobras de material na produção",
                        "Separar as sobras de corte e reutilizá-las na fabricação de peças menores, diminuindo a "
                                + "compra de matéria-prima e o descarte.",
                        "Sustentabilidade", "desperdicio", StatusIdeia.APROVADA, Prioridade.ALTA,
                        "Aprovada para virar projeto."),
                new IdeiaDemo("coleta", "juliana", "Coleta seletiva inteligente nas estações",
                        "Instalar lixeiras de coleta seletiva com sensor de nível em cada estação de trabalho, para "
                                + "aumentar a reciclagem e avisar a equipe de limpeza só quando precisa esvaziar.",
                        "Sustentabilidade", "desperdicio", StatusIdeia.APROVADA, Prioridade.MEDIA,
                        "Boa relação entre custo e impacto."),
                new IdeiaDemo("painelDesperdicio", "tatiane", "Painel de desperdício por turno",
                        "Painel simples com o desperdício apurado por turno, para que as equipes acompanhem a meta "
                                + "semana a semana e comparem os resultados entre as linhas.",
                        "Sustentabilidade", "desperdicio", StatusIdeia.EM_ANALISE, null, null),
                new IdeiaDemo("sensores", "juliana", "Sensores de vibração nas máquinas da linha 3",
                        "Instalar sensores IoT de vibração e temperatura para prever falhas e reduzir paradas não "
                                + "planejadas, enviando os dados para um painel em tempo real.",
                        "Tecnologia", "digital", StatusIdeia.APROVADA, Prioridade.ALTA,
                        "Prioridade da diretoria: aprovada."),
                new IdeiaDemo("checklist", "rafael", "Checklist digital de início de turno",
                        "Trocar o checklist em papel por um formulário no celular, com fotos e alertas automáticos "
                                + "para a manutenção.",
                        "Tecnologia", "digital", StatusIdeia.EM_ANALISE, null, null),
                new IdeiaDemo("painelFabrica", "tatiane", "Painel único de indicadores da fábrica",
                        "Reunir em uma só tela os indicadores de produção, qualidade e manutenção da fábrica, "
                                + "atualizados em tempo real.",
                        "Tecnologia", "digital", StatusIdeia.APROVADA, Prioridade.MEDIA,
                        "Aprovada para o próximo ciclo."),
                new IdeiaDemo("rodizio", "rafael", "Rodízio de tarefas para reduzir lesões",
                        "Alternar as tarefas repetitivas entre os operadores a cada duas horas, para diminuir "
                                + "lesões por esforço repetitivo.",
                        "Pessoas", "seguranca", StatusIdeia.APROVADA, Prioridade.MEDIA,
                        "Aprovada para virar projeto."),
                new IdeiaDemo("pausas", "tatiane", "Pausas ativas guiadas por vídeo",
                        "Exibir, nos monitores das estações, vídeos curtos de alongamento nos horários de pausa.",
                        "Pessoas", "seguranca", StatusIdeia.EM_ANALISE, null, null),
                new IdeiaDemo("consumo", "juliana", "Medição de consumo de energia por máquina",
                        "Instalar medidores de energia em cada máquina para descobrir onde está o maior consumo e "
                                + "priorizar as trocas de equipamento.",
                        "Tecnologia", "energia", StatusIdeia.APROVADA, Prioridade.ALTA,
                        "Base para as demais ações de energia."),
                new IdeiaDemo("led", "rafael", "Troca da iluminação por LED nas linhas",
                        "Substituir as lâmpadas fluorescentes das linhas de produção por iluminação LED.",
                        "Sustentabilidade", "energia", StatusIdeia.APROVADA, Prioridade.MEDIA,
                        "Retorno rápido; aprovada."),
                new IdeiaDemo("cafe", "tatiane", "Máquina de café premium no refeitório",
                        "Comprar uma máquina de café expresso para o refeitório.", "Outros", null,
                        StatusIdeia.REJEITADA, Prioridade.BAIXA, "Fora do escopo das estratégias vigentes."),
                new IdeiaDemo("caronas", "juliana", "Aplicativo de caronas entre turnos",
                        "Criar um aplicativo interno para organizar caronas entre os colaboradores dos três turnos.",
                        "Pessoas", null, StatusIdeia.EM_ANALISE, null, null));
    }

    static List<ProjetoDemo> projetos() {
        return List.of(
                new ProjetoDemo("Sensores IoT na linha 3",
                        "Implantação de sensores de vibração e painel de monitoramento na linha 3.", "120000", -150,
                        90, "digital", "sensores", StatusProjeto.EM_ANDAMENTO, EtapaProjeto.EXECUCAO, 55,
                        List.of(new ResultadoDemo(120, "Redução de paradas no primeiro trimestre", "15000", 4.0, 2.0),
                                new ResultadoDemo(90, "Menos manutenção corretiva", "22000", 6.0, 3.0),
                                new ResultadoDemo(60, "Previsão de falhas em produção", "31000", 9.0, 5.0),
                                new ResultadoDemo(30, "Ganho de disponibilidade das máquinas", "28000", 10.0, 6.0))),
                new ProjetoDemo("Painel de indicadores em tempo real",
                        "Painel único com os indicadores da fábrica.", "30000", 0, 150, "digital", "painelFabrica",
                        StatusProjeto.PLANEJAMENTO, EtapaProjeto.INICIACAO, null, List.of()),
                new ProjetoDemo("Reaproveitamento de sobras de material",
                        "Linha de peças pequenas fabricadas a partir das sobras de corte.", "60000", -240, -30,
                        "desperdicio", "sobras", StatusProjeto.CONCLUIDO, EtapaProjeto.ENCERRAMENTO, null,
                        List.of(new ResultadoDemo(200, "Economia de matéria-prima", "60000", 3.0, 8.0),
                                new ResultadoDemo(150, "Venda das peças reaproveitadas", "80000", 4.0, 10.0),
                                new ResultadoDemo(100, "Redução do descarte", "50000", null, 12.0))),
                new ProjetoDemo("Coleta seletiva inteligente",
                        "Lixeiras com sensor de nível nas estações das linhas 1 e 2.", "25000", -90, 60,
                        "desperdicio", "coleta", StatusProjeto.EM_ANDAMENTO, EtapaProjeto.EXECUCAO, 40,
                        List.of(new ResultadoDemo(30, "Menos descarte misturado", "9000", null, 4.0))),
                new ProjetoDemo("Programa de ergonomia nas estações",
                        "Adequação das estações de trabalho e rodízio de tarefas.", "45000", -180, -10, "seguranca",
                        "rodizio", StatusProjeto.EM_ANDAMENTO, EtapaProjeto.MONITORAMENTO, 80,
                        List.of(new ResultadoDemo(60, "Queda nos afastamentos por lesão", "12000", 5.0, null))));
    }

    static List<String> emails() {
        return usuarios().stream().map(UsuarioDemo::email).toList();
    }
}
