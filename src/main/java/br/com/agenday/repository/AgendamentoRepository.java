package br.com.agenday.repository;

import br.com.agenday.model.Agendamento;
import br.com.agenday.model.StatusAgendamento;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    // Lista todos os agendamentos de uma determinada data ordenados por horário
    List<Agendamento> findByDataOrderByHorarioAsc(LocalDate data);

    // Busca agendamentos do dia trazendo Cliente e Servico em um único SELECT (evita LazyInitializationException com open-in-view=false)
    @org.springframework.data.jpa.repository.Query("SELECT a FROM Agendamento a " +
           "LEFT JOIN FETCH a.cliente " +
           "LEFT JOIN FETCH a.servico " +
           "WHERE a.data = :data " +
           "ORDER BY a.horario ASC")
    List<Agendamento> findByDataComDetalhesOrderByHorarioAsc(@org.springframework.data.repository.query.Param("data") LocalDate data);

    // Lista todos os agendamentos ativos de uma data (desconsiderando os cancelados para cálculo de horários livres)
    List<Agendamento> findByDataAndStatusNotOrderByHorarioAsc(LocalDate data, StatusAgendamento status);

    // Lista agendamentos entre um intervalo de datas (para visualização semanal da massoterapeuta)
    List<Agendamento> findByDataBetweenOrderByDataAscHorarioAsc(LocalDate dataInicio, LocalDate dataFim);
}
