package br.com.fiap.aguiainova.repository;

import java.util.Collection;
import java.util.List;

import br.com.fiap.aguiainova.model.Ideia;
import br.com.fiap.aguiainova.model.enums.StatusIdeia;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IdeiaRepository extends MongoRepository<Ideia, String> {

    boolean existsByEstrategiaId(String estrategiaId);

    List<Ideia> findByEstrategiaIdIn(Collection<String> estrategiaIds);

    List<Ideia> findByStatus(StatusIdeia status, Sort sort);

    List<Ideia> findByStatus(StatusIdeia status, Pageable pageable);
}
