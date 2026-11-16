package br.com.agenday.repository;

import br.com.agenday.model.Agendamento;
import br.com.agenday.model.StatusAgendamento;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    // Lista todos os agendamentos de uma determinada data ordenados por horário
    List<Agendamento> findByDataOrderByHorarioAsc(LocalDate data);

    // Busca agendamentos do dia trazendo Cliente e Servico em um único SELECT (evita LazyInitializationException com open-in-view=false)
    @Query("SELECT a FROM Agendamento a " +
           "LEFT JOIN FETCH a.cliente " +
           "LEFT JOIN FETCH a.servico " +
           "WHERE a.data = :data " +
           "ORDER BY a.horario ASC")
    List<Agendamento> findByDataComDetalhesOrderByHorarioAsc(@Param("data") LocalDate data);

    // Busca agendamento por ID trazendo Cliente e Servico em um único SELECT para a tela de confirmação/sucesso
    @Query("SELECT a FROM Agendamento a " +
           "LEFT JOIN FETCH a.cliente " +
           "LEFT JOIN FETCH a.servico " +
           "WHERE a.id = :id")
    Optional<Agendamento> findByIdComDetalhes(@Param("id") Long id);

    // Lista todos os agendamentos ativos de uma data trazendo servico para calculo de horario de termino
    @Query("SELECT a FROM Agendamento a " +
           "LEFT JOIN FETCH a.servico " +
           "WHERE a.data = :data AND a.status != :status " +
           "ORDER BY a.horario ASC")
    List<Agendamento> findByDataAndStatusNotOrderByHorarioAsc(@Param("data") LocalDate data, @Param("status") StatusAgendamento status);

    // Lista agendamentos entre um intervalo de datas (para visualização semanal da massoterapeuta)
    List<Agendamento> findByDataBetweenOrderByDataAscHorarioAsc(LocalDate dataInicio, LocalDate dataFim);
}
