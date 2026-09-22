package br.com.fiap.aguiainova.repository;

import java.util.Optional;

import br.com.fiap.aguiainova.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
