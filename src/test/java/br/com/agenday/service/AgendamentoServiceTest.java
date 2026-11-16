package br.com.agenday.service;

import br.com.agenday.dto.AgendamentoRequestDTO;
import br.com.agenday.model.Agendamento;
import br.com.agenday.model.Cliente;
import br.com.agenday.model.Servico;
import br.com.agenday.model.StatusAgendamento;
import br.com.agenday.repository.AgendamentoRepository;
import br.com.agenday.repository.ClienteRepository;
import br.com.agenday.repository.ServicoRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private Servico massagem60min;

    @BeforeEach
    void setUp() {
        massagem60min = new Servico("Massagem Relaxante", "Relaxamento", 60, new BigDecimal("120.00"));
        massagem60min.setId(1L);
    }

    @Test
    @DisplayName("Não deve retornar nenhum horário disponível aos domingos")
    void testDomingoNaoTemAtendimento() {
        LocalDate proximoDomingo = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(massagem60min));

        List<LocalTime> horarios = agendamentoService.obterHorariosDisponiveis(proximoDomingo, 1L);

        assertTrue(horarios.isEmpty());
    }

    @Test
    @DisplayName("Deve bloquear horários conflitantes considerando a duração do serviço e 15 min de intervalo técnico")
    void testConflitoComIntervaloTecnico() {
        // Próxima segunda-feira
        LocalDate segundaFeira = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(massagem60min));

        // Simula um agendamento existente de 60 min às 09:00 (ocupa até 10:00 + 15 min de limpeza = 10:15)
        Agendamento agExistente = new Agendamento(
                new Cliente("Maria", "11999991111"),
                massagem60min,
                segundaFeira,
                LocalTime.of(9, 0),
                StatusAgendamento.AGENDADO
        );

        when(agendamentoRepository.findByDataAndStatusNotOrderByHorarioAsc(segundaFeira, StatusAgendamento.CANCELADO))
                .thenReturn(List.of(agExistente));

        List<LocalTime> horariosLivres = agendamentoService.obterHorariosDisponiveis(segundaFeira, 1L);

        // 08:00 é permitido (vai até 09:00, com 15 min precisa terminar antes de 09:00? 08:00 + 60m + 15m = 09:15 colide com 09:00!)
        // 09:00 colide
        // 09:30 colide
        // 10:00 colide (pois a limpeza do anterior vai até 10:15)
        // 10:30 está livre! (10:30 + 60min = 11:30 <= 12:00)
        assertFalse(horariosLivres.contains(LocalTime.of(9, 0)));
        assertFalse(horariosLivres.contains(LocalTime.of(9, 30)));
        assertFalse(horariosLivres.contains(LocalTime.of(10, 0)));
        assertTrue(horariosLivres.contains(LocalTime.of(10, 30)));
    }

    @Test
    @DisplayName("Deve reaproveitar cliente existente com o mesmo telefone")
    void testReaproveitarClienteExistente() {
        LocalDate segundaFeira = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalTime horario = LocalTime.of(10, 30);

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(massagem60min));
        when(agendamentoRepository.findByDataAndStatusNotOrderByHorarioAsc(segundaFeira, StatusAgendamento.CANCELADO))
                .thenReturn(List.of());

        Cliente clienteExistente = new Cliente("Carlos Antigo", "11988887777");
        clienteExistente.setId(42L);

        when(clienteRepository.findByTelefone("11988887777")).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgendamentoRequestDTO dto = new AgendamentoRequestDTO(
                "Carlos Novo Nome",
                "11988887777",
                1L,
                segundaFeira,
                horario
        );

        Agendamento agendamentoCriado = agendamentoService.criarAgendamento(dto);

        assertNotNull(agendamentoCriado);
        assertEquals(42L, agendamentoCriado.getCliente().getId());
        assertEquals("Carlos Novo Nome", agendamentoCriado.getCliente().getNome());
        verify(clienteRepository).save(clienteExistente);
    }

    @Test
    @DisplayName("Deve concluir agendamento com sucesso")
    void testConcluirAgendamento() {
        Agendamento ag = new Agendamento(new Cliente("Ana", "11999993333"), massagem60min, LocalDate.now(), LocalTime.of(10, 0), StatusAgendamento.AGENDADO);
        ag.setId(10L);

        when(agendamentoRepository.findById(10L)).thenReturn(Optional.of(ag));

        agendamentoService.concluirAgendamento(10L);

        assertEquals(StatusAgendamento.CONCLUIDO, ag.getStatus());
        verify(agendamentoRepository).save(ag);
    }

    @Test
    @DisplayName("Deve salvar bloqueio administrativo corretamente")
    void testBloquearHorario() {
        LocalDate data = LocalDate.now().plusDays(2);
        LocalTime horario = LocalTime.of(14, 0);

        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Agendamento bloqueio = agendamentoService.bloquearHorario(data, horario, 120, "Médico");

        assertNotNull(bloqueio);
        assertEquals(StatusAgendamento.BLOQUEADO, bloqueio.getStatus());
        assertEquals(Integer.valueOf(120), bloqueio.getDuracaoBloqueioMinutos());
        assertEquals("Médico", bloqueio.getObservacao());
        assertEquals(LocalTime.of(16, 0), bloqueio.getHorarioTermino());
    }
}
