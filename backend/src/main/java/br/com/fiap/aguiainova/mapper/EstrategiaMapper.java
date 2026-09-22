package br.com.fiap.aguiainova.mapper;

import br.com.fiap.aguiainova.dto.request.EstrategiaRequest;
import br.com.fiap.aguiainova.dto.response.EstrategiaHistoricoResponse;
import br.com.fiap.aguiainova.dto.response.EstrategiaResponse;
import br.com.fiap.aguiainova.model.DadosEstrategia;
import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.EstrategiaHistorico;
import org.springframework.stereotype.Component;

@Component
public class EstrategiaMapper {

    public DadosEstrategia paraDados(EstrategiaRequest req) {
        return new DadosEstrategia(req.titulo(), req.descricao(), req.categoria(), req.campanha(), req.status());
    }

    public EstrategiaResponse paraResponse(Estrategia estrategia) {
        return new EstrategiaResponse(estrategia.getId(), estrategia.getTitulo(), estrategia.getDescricao(),
                estrategia.getCategoria(), estrategia.getCampanha(), estrategia.getStatus(),
                estrategia.getAutor().usuarioId(), estrategia.getAutor().nome(), estrategia.getCriadoEm(),
                estrategia.getAtualizadoEm());
    }

    public EstrategiaHistoricoResponse paraResponse(EstrategiaHistorico registro) {
        return new EstrategiaHistoricoResponse(registro.getId(), registro.getEstrategiaId(), registro.getData(),
                registro.getCategoria(), registro.getCampanha(), registro.getAcao(), registro.getTitulo(),
                registro.getDescricao(), registro.getStatus(), registro.getAutor().nome());
    }
}
