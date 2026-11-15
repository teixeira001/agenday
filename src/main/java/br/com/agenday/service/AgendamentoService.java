package br.com.agenday.service;

import br.com.agenday.dto.AgendamentoRequestDTO;
import br.com.agenday.model.Agendamento;
import br.com.agenday.model.Cliente;
import br.com.agenday.model.Servico;
import br.com.agenday.model.StatusAgendamento;
import br.com.agenday.repository.AgendamentoRepository;
import br.com.agenday.repository.ClienteRepository;
import br.com.agenday.repository.ServicoRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendamentoService {

    // Intervalo técnico entre atendimentos (para higienização da sala e maca)
    public static final int INTERVALO_TECNICO_MINUTOS = 15;

    // Intervalo de espaçamento da grade de opções (a cada 30 minutos)
    public static final int PASSO_GRADE_MINUTOS = 30;

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository,
                              ClienteRepository clienteRepository,
                              ServicoRepository servicoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
    }

    /**
     * Calcula dinamicamente quais horários estão livres para um serviço em determinada data.
     */
    @Transactional(readOnly = true)
    public List<LocalTime> obterHorariosDisponiveis(LocalDate data, Long servicoId) {
        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado: " + servicoId));

        int duracaoMinutos = servico.getDuracaoMinutos();

        // 1. Domingo não há atendimento
        if (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return List.of();
        }

        // 2. Busca agendamentos ativos ou bloqueios existentes na data (ignora cancelados)
        List<Agendamento> agendamentosAtivos = agendamentoRepository
                .findByDataAndStatusNotOrderByHorarioAsc(data, StatusAgendamento.CANCELADO);

        // 3. Define a grade de expediente conforme o dia da semana (RN07)
        List<LocalTime> slotsCandidatos = gerarGradeExpediente(data.getDayOfWeek(), duracaoMinutos);

        // 4. Filtra apenas os horários sem colisão e que não sejam passados (se for hoje)
        boolean ehHoje = data.equals(LocalDate.now());
        LocalTime agora = LocalTime.now();

        List<LocalTime> horariosLivres = new ArrayList<>();

        for (LocalTime inicioSlot : slotsCandidatos) {
            // Se for hoje, não permite agendar horários passados
            if (ehHoje && !inicioSlot.isAfter(agora)) {
                continue;
            }

            LocalTime fimSlot = inicioSlot.plusMinutes(duracaoMinutos);

            // Verifica colisão com atendimentos ou bloqueios existentes
            boolean haConflito = agendamentosAtivos.stream().anyMatch(ag ->
                    temSobreposicaoComIntervaloTecnico(inicioSlot, fimSlot, ag)
            );

            if (!haConflito) {
                horariosLivres.add(inicioSlot);
            }
        }

        return horariosLivres;
    }

    /**
     * Cria e persiste um agendamento garantindo integridade e reaproveitando cliente existente (RN08 e RN05).
     */
    @Transactional
    public Agendamento criarAgendamento(AgendamentoRequestDTO dto) {
        Servico servico = servicoRepository.findById(dto.getServicoId())
                .orElseThrow(() -> new IllegalArgumentException("Serviço inválido."));

        // Validação final de concorrência antes de persistir
        List<LocalTime> livres = obterHorariosDisponiveis(dto.getData(), dto.getServicoId());
        if (!livres.contains(dto.getHorario())) {
            throw new IllegalStateException("O horário selecionado não está mais disponível.");
        }

        // RN08: Reaproveita cliente existente por telefone ou cadastra novo
        Cliente cliente = clienteRepository.findByTelefone(dto.getTelefoneCliente().trim())
                .map(c -> {
                    // Atualiza o nome se tiver mudado
                    if (!c.getNome().equalsIgnoreCase(dto.getNomeCliente().trim())) {
                        c.setNome(dto.getNomeCliente().trim());
                        return clienteRepository.save(c);
                    }
                    return c;
                })
                .orElseGet(() -> clienteRepository.save(new Cliente(dto.getNomeCliente().trim(), dto.getTelefoneCliente().trim())));

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setData(dto.getData());
        agendamento.setHorario(dto.getHorario());
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamento.setObservacao(dto.getObservacao());

        return agendamentoRepository.save(agendamento);
    }

    /**
     * Cancela um agendamento (RN03).
     */
    @Transactional
    public void cancelarAgendamento(Long agendamentoId, String motivo) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado: " + agendamentoId));

        agendamento.setStatus(StatusAgendamento.CANCELADO);
        if (motivo != null && !motivo.isBlank()) {
            String obsAtual = agendamento.getObservacao() != null ? agendamento.getObservacao() + " | " : "";
            agendamento.setObservacao(obsAtual + "Cancelado: " + motivo);
        }
        agendamentoRepository.save(agendamento);
    }

    /**
     * Gera todos os slots de início possíveis dentro do expediente que acomodam a duração do serviço.
     */
    private List<LocalTime> gerarGradeExpediente(DayOfWeek diaSemana, int duracaoMinutos) {
        List<LocalTime> slots = new ArrayList<>();

        if (diaSemana == DayOfWeek.SATURDAY) {
            // Sábado: 08:00 às 13:00 direto
            gerarIntervalo(slots, LocalTime.of(8, 0), LocalTime.of(13, 0), duracaoMinutos);
        } else {
            // Segunda a Sexta: Manhã (08:00 às 12:00) e Tarde (13:30 às 18:00)
            gerarIntervalo(slots, LocalTime.of(8, 0), LocalTime.of(12, 0), duracaoMinutos);
            gerarIntervalo(slots, LocalTime.of(13, 30), LocalTime.of(18, 0), duracaoMinutos);
        }

        return slots;
    }

    private void gerarIntervalo(List<LocalTime> lista, LocalTime abertura, LocalTime fechamento, int duracaoMinutos) {
        LocalTime atual = abertura;
        while (!atual.plusMinutes(duracaoMinutos).isAfter(fechamento)) {
            lista.add(atual);
            atual = atual.plusMinutes(PASSO_GRADE_MINUTOS);
        }
    }

    /**
     * Verifica sobreposição considerando o intervalo técnico de 15 minutos entre atendimentos (RN01).
     */
    private boolean temSobreposicaoComIntervaloTecnico(LocalTime novoInicio, LocalTime novoFim, Agendamento agExistente) {
        LocalTime existInicio = agExistente.getHorario();
        LocalTime existFimComIntervalo = agExistente.getHorarioTermino().plusMinutes(INTERVALO_TECNICO_MINUTOS);

        // O novo atendimento também necessita de 15 minutos ao término
        LocalTime novoFimComIntervalo = novoFim.plusMinutes(INTERVALO_TECNICO_MINUTOS);

        // Duas faixas de tempo [A1, A2] e [B1, B2] se sobrepõem se A1 < B2 e A2 > B1
        return novoInicio.isBefore(existFimComIntervalo) && novoFimComIntervalo.isAfter(existInicio);
    }
}
