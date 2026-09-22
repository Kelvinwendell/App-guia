package br.com.fiap.aguiainova.repository;

import br.com.fiap.aguiainova.model.EstrategiaHistorico;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EstrategiaHistoricoRepository extends MongoRepository<EstrategiaHistorico, String> {
}
