package br.com.agenday.controller;

import br.com.agenday.config.SecurityConfig;
import br.com.agenday.model.Agendamento;
import br.com.agenday.model.Cliente;
import br.com.agenday.model.Servico;
import br.com.agenday.model.StatusAgendamento;
import br.com.agenday.repository.AgendamentoRepository;
import br.com.agenday.service.AgendamentoService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminAgendaController.class)
@Import(SecurityConfig.class)
@WithMockUser(username = "admin", roles = {"ADMIN"})
class AdminAgendaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgendamentoRepository agendamentoRepository;

    @MockBean
    private AgendamentoService agendamentoService;

    @Test
    @DisplayName("GET /admin deve carregar página da agenda com métricas e lista de agendamentos")
    void testExibirAgenda() throws Exception {
        LocalDate hoje = LocalDate.now();
        Cliente cliente = new Cliente("Joana", "11999992222");
        Servico servico = new Servico("Drenagem", "Desc", 60, new BigDecimal("130.00"));
        Agendamento ag = new Agendamento(cliente, servico, hoje, LocalTime.of(10, 0), StatusAgendamento.AGENDADO);

        when(agendamentoRepository.findByDataComDetalhesOrderByHorarioAsc(hoje)).thenReturn(List.of(ag));

        mockMvc.perform(get("/admin").param("data", hoje.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/agenda"))
                .andExpect(model().attributeExists("agendamentos"))
                .andExpect(model().attribute("totalAtendimentos", 1L))
                .andExpect(model().attribute("faturamentoEstimado", new BigDecimal("130.00")));
    }

    @Test
    @DisplayName("POST /admin/agendamentos/{id}/concluir deve marcar como concluído e redirecionar")
    void testConcluirAgendamento() throws Exception {
        mockMvc.perform(post("/admin/agendamentos/5/concluir")
                        .with(csrf())
                        .param("data", "2026-11-20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?data=2026-11-20"));

        verify(agendamentoService).concluirAgendamento(5L);
    }

    @Test
    @DisplayName("POST /admin/agendamentos/{id}/cancelar deve cancelar agendamento e redirecionar")
    void testCancelarAgendamento() throws Exception {
        mockMvc.perform(post("/admin/agendamentos/5/cancelar")
                        .with(csrf())
                        .param("data", "2026-11-20")
                        .param("motivo", "Cliente desmarcou"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?data=2026-11-20"));

        verify(agendamentoService).cancelarAgendamento(5L, "Cliente desmarcou");
    }

    @Test
    @DisplayName("POST /admin/bloqueios deve criar bloqueio administrativo e redirecionar")
    void testCriarBloqueio() throws Exception {
        mockMvc.perform(post("/admin/bloqueios")
                        .with(csrf())
                        .param("data", "2026-11-20")
                        .param("horarioInicio", "14:00")
                        .param("duracaoMinutos", "60")
                        .param("motivo", "Consulta Médica"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?data=2026-11-20"));

        verify(agendamentoService).bloquearHorario(
                eq(LocalDate.of(2026, 11, 20)),
                eq(LocalTime.of(14, 0)),
                eq(60),
                eq("Consulta Médica")
        );
    }

    @Test
    @WithAnonymousUser
    @DisplayName("GET /admin sem autenticação deve redirecionar para a página de login")
    void testAcessoAdminSemAutenticacao() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
