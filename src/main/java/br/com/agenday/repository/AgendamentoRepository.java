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

    // Lista todos os agendamentos ativos de uma data (desconsiderando os cancelados para cálculo de horários livres)
    List<Agendamento> findByDataAndStatusNotOrderByHorarioAsc(LocalDate data, StatusAgendamento status);

    // Lista agendamentos entre um intervalo de datas (para visualização semanal da massoterapeuta)
    List<Agendamento> findByDataBetweenOrderByDataAscHorarioAsc(LocalDate dataInicio, LocalDate dataFim);
}
