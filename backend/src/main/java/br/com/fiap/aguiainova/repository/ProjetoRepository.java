package br.com.fiap.aguiainova.repository;

import br.com.fiap.aguiainova.model.Projeto;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjetoRepository extends MongoRepository<Projeto, String> {

    boolean existsByEstrategiaId(String estrategiaId);
}
