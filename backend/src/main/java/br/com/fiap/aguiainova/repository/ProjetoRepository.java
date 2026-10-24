package br.com.fiap.aguiainova.repository;

import java.util.Collection;
import java.util.List;

import br.com.fiap.aguiainova.model.Projeto;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjetoRepository extends MongoRepository<Projeto, String> {

    boolean existsByEstrategiaId(String estrategiaId);

    List<Projeto> findByEstrategiaIdIn(Collection<String> estrategiaIds);

    /** Projetos criados a partir de alguma destas ideias (qualquer que seja a estrategia do projeto). */
    List<Projeto> findByIdeiaIdIn(Collection<String> ideiaIds);
}
