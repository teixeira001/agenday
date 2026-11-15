package br.com.agenday.repository;

import br.com.agenday.model.Cliente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Busca cliente pelo número de telefone para reaproveitar cadastro
    Optional<Cliente> findByTelefone(String telefone);

    // Verifica se já existe algum cliente com o telefone informado
    boolean existsByTelefone(String telefone);
}
