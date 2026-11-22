package br.com.agenday.repository;

import br.com.agenday.model.Cliente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Busca cliente pelo número de telefone para reaproveitar cadastro
    Optional<Cliente> findByTelefone(String telefone);

    // Verifica se já existe algum cliente com o telefone informado
    boolean existsByTelefone(String telefone);

    // Lista todos os clientes em ordem alfabética
    List<Cliente> findAllByOrderByNomeAsc();

    // Busca clientes por nome ou telefone
    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR c.telefone LIKE CONCAT('%', :termo, '%') ORDER BY c.nome ASC")
    List<Cliente> buscarPorNomeOuTelefone(@Param("termo") String termo);
}
