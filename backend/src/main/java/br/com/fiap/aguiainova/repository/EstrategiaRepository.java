package br.com.fiap.aguiainova.repository;

import java.util.List;

import br.com.fiap.aguiainova.model.Estrategia;
import br.com.fiap.aguiainova.model.enums.StatusEstrategia;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EstrategiaRepository extends MongoRepository<Estrategia, String> {

    List<Estrategia> findByStatus(StatusEstrategia status);
}
