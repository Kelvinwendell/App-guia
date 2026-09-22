package br.com.fiap.aguiainova.mapper;

import br.com.fiap.aguiainova.dto.request.IdeiaRequest;
import br.com.fiap.aguiainova.dto.response.IdeiaResponse;
import br.com.fiap.aguiainova.model.Autor;
import br.com.fiap.aguiainova.model.DadosIdeia;
import br.com.fiap.aguiainova.model.Ideia;
import org.springframework.stereotype.Component;

@Component
public class IdeiaMapper {

    public DadosIdeia paraDados(IdeiaRequest req) {
        return new DadosIdeia(req.titulo(), req.descricao(), req.categoria(), req.estrategiaId());
    }

    public IdeiaResponse paraResponse(Ideia ideia, String estrategiaTitulo, boolean incluirAvaliacaoIa) {
        Autor autor = ideia.getAutor();
        Autor avaliador = ideia.getAvaliador();
        return new IdeiaResponse(ideia.getId(), ideia.getTitulo(), ideia.getDescricao(), ideia.getCategoria(),
                ideia.getStatus(), ideia.getPrioridade(), autor.usuarioId(), autor.nome(), ideia.getEstrategiaId(),
                estrategiaTitulo, ideia.getParecer(), avaliador == null ? null : avaliador.nome(),
                ideia.getAvaliadaEm(), incluirAvaliacaoIa ? ideia.getAvaliacaoIa() : null, ideia.getCriadoEm(),
                ideia.getAtualizadoEm());
    }
}
