package br.com.fiap.aguiainova.mapper;

import java.time.LocalDate;
import java.util.List;

import br.com.fiap.aguiainova.dto.request.ProjetoRequest;
import br.com.fiap.aguiainova.dto.request.ResultadoRequest;
import br.com.fiap.aguiainova.dto.response.ProjetoResponse;
import br.com.fiap.aguiainova.dto.response.ResultadoProjetoResponse;
import br.com.fiap.aguiainova.model.DadosProjeto;
import br.com.fiap.aguiainova.model.DadosResultado;
import br.com.fiap.aguiainova.model.Projeto;
import br.com.fiap.aguiainova.model.ProjetoMetricas;
import br.com.fiap.aguiainova.model.ResultadoProjeto;
import org.springframework.stereotype.Component;

@Component
public class ProjetoMapper {

    /** @param inicioPadrao data usada quando a requisicao nao informa o inicio do projeto */
    public DadosProjeto paraDados(ProjetoRequest req, LocalDate inicioPadrao) {
        LocalDate inicio = req.dataInicio() != null ? req.dataInicio() : inicioPadrao;
        return new DadosProjeto(req.nome(), req.descricao(), req.investimento(), inicio, req.prazo(),
                req.estrategiaId(), req.ideiaId(), req.status(), req.etapa());
    }

    /** @param dataPadrao data usada quando a requisicao nao informa a data do resultado */
    public DadosResultado paraDadosResultado(ResultadoRequest req, LocalDate dataPadrao) {
        LocalDate data = req.data() != null ? req.data() : dataPadrao;
        return new DadosResultado(data, req.descricao(), req.retornoFinanceiro(), req.ganhoProdutividade(),
                req.reducaoCustos());
    }

    public ProjetoResponse paraResponse(Projeto projeto, String estrategiaTitulo, String ideiaTitulo,
            LocalDate hoje) {
        ProjetoMetricas metricas = ProjetoMetricas.de(projeto);
        return new ProjetoResponse(projeto.getId(), projeto.getNome(), projeto.getDescricao(), projeto.getStatus(),
                projeto.getEtapa(), projeto.getInvestimento(), projeto.getDataInicio(), projeto.getPrazo(),
                projeto.getProgresso(), projeto.getEstrategiaId(), estrategiaTitulo, projeto.getIdeiaId(),
                ideiaTitulo, projeto.getAutor().usuarioId(), projeto.getAutor().nome(),
                metricas.retornoFinanceiro(), metricas.lucro(), metricas.roi(), metricas.ganhoProdutividade(),
                metricas.reducaoCustos(), projeto.estaAtrasado(hoje), projeto.diasParaPrazo(hoje),
                paraResponses(projeto.getResultados()), projeto.getCriadoEm(), projeto.getAtualizadoEm());
    }

    private static List<ResultadoProjetoResponse> paraResponses(List<ResultadoProjeto> resultados) {
        return resultados.stream()
                .map(r -> new ResultadoProjetoResponse(r.id(), r.data(), r.descricao(), r.retornoFinanceiro(),
                        r.ganhoProdutividade(), r.reducaoCustos(), r.registradoPor().nome()))
                .toList();
    }
}
