package br.com.fiap.aguiainova.mapper;

import br.com.fiap.aguiainova.dto.request.EstrategiaRequest;
import br.com.fiap.aguiainova.dto.response.EstrategiaHistoricoResponse;
import br.com.fiap.aguiainova.dto.response.EstrategiaResponse;
import br.com.fiap.aguiainova.dto.response.SaudeEstrategiaResponse;
import br.com.fiap.aguiainova.model.DadosEstrategia;
import br.com.fiap.aguiainova.model.DiagnosticoEstrategia;
import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.EstrategiaHistorico;
import br.com.fiap.aguiainova.model.IndicadoresEstrategia;
import br.com.fiap.aguiainova.model.SaudeEstrategia;
import org.springframework.stereotype.Component;

@Component
public class EstrategiaMapper {

    public DadosEstrategia paraDados(EstrategiaRequest req) {
        return new DadosEstrategia(req.titulo(), req.descricao(), req.categoria(), req.campanha(), req.status(),
                req.metas(), req.inicio(), req.fim());
    }

    public EstrategiaResponse paraResponse(Estrategia estrategia) {
        return new EstrategiaResponse(estrategia.getId(), estrategia.getTitulo(), estrategia.getDescricao(),
                estrategia.getCategoria(), estrategia.getCampanha(), estrategia.getStatus(), estrategia.getMetas(),
                estrategia.getInicio(), estrategia.getFim(), estrategia.getAutor().usuarioId(),
                estrategia.getAutor().nome(), estrategia.getCriadoEm(), estrategia.getAtualizadoEm());
    }

    public SaudeEstrategiaResponse paraResponse(DiagnosticoEstrategia diagnostico) {
        Estrategia estrategia = diagnostico.estrategia();
        IndicadoresEstrategia indicadores = diagnostico.indicadores();
        SaudeEstrategia saude = diagnostico.saude();
        return new SaudeEstrategiaResponse(estrategia.getId(), estrategia.getTitulo(), estrategia.getCategoria(),
                saude.nivel(), saude.motivos(), saude.proximaAcao(), indicadores.ideias(),
                indicadores.ideiasEmAnalise(), indicadores.aprovadasSemProjeto(), indicadores.projetos(),
                indicadores.projetosAtrasados(), indicadores.investimento(), indicadores.retorno(),
                indicadores.roiPercentual(), indicadores.ultimaAtividade());
    }

    public EstrategiaHistoricoResponse paraResponse(EstrategiaHistorico registro) {
        return new EstrategiaHistoricoResponse(registro.getId(), registro.getEstrategiaId(), registro.getData(),
                registro.getCategoria(), registro.getCampanha(), registro.getAcao(), registro.getTitulo(),
                registro.getDescricao(), registro.getStatus(), registro.getAutor().nome());
    }
}
