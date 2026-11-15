package br.com.agenday.repository;

import br.com.agenday.model.Servico;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    // Método customizado do Spring Data JPA para listar serviços ordenados por nome
    List<Servico> findAllByOrderByNomeAsc();
}
