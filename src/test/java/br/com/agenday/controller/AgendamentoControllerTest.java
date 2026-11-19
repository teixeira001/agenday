package br.com.agenday.controller;

import br.com.agenday.config.SecurityConfig;
import br.com.agenday.model.Agendamento;
import br.com.agenday.model.Cliente;
import br.com.agenday.model.Servico;
import br.com.agenday.model.StatusAgendamento;
import br.com.agenday.repository.AgendamentoRepository;
import br.com.agenday.repository.ServicoRepository;
import br.com.agenday.service.AgendamentoService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AgendamentoController.class)
@Import(SecurityConfig.class)
class AgendamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServicoRepository servicoRepository;

    @MockBean
    private AgendamentoService agendamentoService;

    @MockBean
    private AgendamentoRepository agendamentoRepository;

    @Test
    @DisplayName("GET /agendamento deve carregar página com lista de serviços")
    void testExibirFormulario() throws Exception {
        Servico s1 = new Servico("Relaxante", "Desc", 60, new BigDecimal("120.00"));
        when(servicoRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(s1));

        mockMvc.perform(get("/agendamento"))
                .andExpect(status().isOk())
                .andExpect(view().name("agendamento"))
                .andExpect(model().attributeExists("servicos"))
                .andExpect(model().attributeExists("agendamentoDTO"));
    }

    @Test
    @DisplayName("GET /api/horarios-disponiveis deve retornar JSON com horários formatados")
    void testConsultarHorariosJson() throws Exception {
        LocalDate amanha = LocalDate.now().plusDays(1);
        when(agendamentoService.obterHorariosDisponiveis(amanha, 1L))
                .thenReturn(List.of(LocalTime.of(9, 0), LocalTime.of(10, 30)));

        mockMvc.perform(get("/api/horarios-disponiveis")
                        .param("servicoId", "1")
                        .param("data", amanha.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("09:00"))
                .andExpect(jsonPath("$[1]").value("10:30"));
    }

    @Test
    @DisplayName("POST /agendamento com dados válidos deve redirecionar para tela de sucesso")
    void testProcessarAgendamentoComSucesso() throws Exception {
        LocalDate amanha = LocalDate.now().plusDays(1);
        Cliente cliente = new Cliente("Maria Silva", "11999998888");
        Servico servico = new Servico("Relaxante", "Desc", 60, new BigDecimal("120.00"));
        Agendamento agendamentoSalvo = new Agendamento(cliente, servico, amanha, LocalTime.of(9, 0), StatusAgendamento.AGENDADO);
        agendamentoSalvo.setId(10L);

        when(agendamentoService.criarAgendamento(any())).thenReturn(agendamentoSalvo);

        mockMvc.perform(post("/agendamento")
                        .with(csrf())
                        .param("nomeCliente", "Maria Silva")
                        .param("telefoneCliente", "11999998888")
                        .param("servicoId", "1")
                        .param("data", amanha.toString())
                        .param("horario", "09:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agendamento/sucesso/10"));
    }

    @Test
    @DisplayName("GET /agendamento/sucesso/{id} deve exibir detalhes e link do WhatsApp")
    void testExibirSucesso() throws Exception {
        LocalDate amanha = LocalDate.now().plusDays(1);
        Cliente cliente = new Cliente("Maria Silva", "11999998888");
        Servico servico = new Servico("Relaxante", "Desc", 60, new BigDecimal("120.00"));
        Agendamento agendamento = new Agendamento(cliente, servico, amanha, LocalTime.of(9, 0), StatusAgendamento.AGENDADO);
        agendamento.setId(10L);

        when(agendamentoRepository.findByIdComDetalhes(10L)).thenReturn(Optional.of(agendamento));

        mockMvc.perform(get("/agendamento/sucesso/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("sucesso"))
                .andExpect(model().attributeExists("agendamento"))
                .andExpect(model().attributeExists("linkWhatsApp"));
    }
}
