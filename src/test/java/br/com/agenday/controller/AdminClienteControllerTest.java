package br.com.agenday.controller;

import br.com.agenday.config.SecurityConfig;
import br.com.agenday.model.Agendamento;
import br.com.agenday.model.Cliente;
import br.com.agenday.model.Servico;
import br.com.agenday.model.StatusAgendamento;
import br.com.agenday.repository.AgendamentoRepository;
import br.com.agenday.repository.ClienteRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminClienteController.class)
@Import(SecurityConfig.class)
class AdminClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteRepository clienteRepository;

    @MockBean
    private AgendamentoRepository agendamentoRepository;

    @Test
    @DisplayName("GET /admin/clientes deve exigir autenticação")
    void testAcessoAnonimoRedirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/admin/clientes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /admin/clientes sem busca deve listar todos os clientes em ordem alfabética")
    void testListarClientesSemBusca() throws Exception {
        Cliente c1 = new Cliente("Ana Paula", "11999990001");
        c1.setId(1L);
        when(clienteRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(c1));

        mockMvc.perform(get("/admin/clientes"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/clientes"))
                .andExpect(model().attributeExists("clientes"))
                .andExpect(model().attribute("paginaAtiva", "clientes"));

        verify(clienteRepository).findAllByOrderByNomeAsc();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /admin/clientes com parâmetro de busca deve filtrar clientes")
    void testListarClientesComBusca() throws Exception {
        Cliente c1 = new Cliente("Beatriz Lima", "11999990002");
        c1.setId(2L);
        when(clienteRepository.buscarPorNomeOuTelefone("Beatriz")).thenReturn(List.of(c1));

        mockMvc.perform(get("/admin/clientes").param("busca", "Beatriz"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/clientes"))
                .andExpect(model().attribute("busca", "Beatriz"))
                .andExpect(model().attributeExists("clientes"));

        verify(clienteRepository).buscarPorNomeOuTelefone("Beatriz");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /admin/clientes/{id} deve exibir detalhes, histórico e ficha técnica")
    void testExibirDetalhesCliente() throws Exception {
        Cliente cliente = new Cliente("Carlos Andrade", "11999990003");
        cliente.setId(3L);
        cliente.setObservacoes("Paciente com queixa de tensão nos ombros.");

        Servico servico = new Servico("Massagem Terapêutica", "Desc", 60, new BigDecimal("150.00"));
        Agendamento agendamento = new Agendamento(cliente, servico, LocalDate.now().minusDays(5), LocalTime.of(14, 0), StatusAgendamento.CONCLUIDO);

        when(clienteRepository.findById(3L)).thenReturn(Optional.of(cliente));
        when(agendamentoRepository.findByClienteIdComServicoOrderByDataDesc(3L)).thenReturn(List.of(agendamento));

        mockMvc.perform(get("/admin/clientes/3"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/cliente-detalhes"))
                .andExpect(model().attribute("cliente", cliente))
                .andExpect(model().attribute("atendimentosRealizados", 1L))
                .andExpect(model().attribute("valorTotalInvestido", new BigDecimal("150.00")))
                .andExpect(model().attributeExists("historico"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /admin/clientes/{id}/observacoes deve atualizar ficha técnica e redirecionar")
    void testSalvarObservacoes() throws Exception {
        Cliente cliente = new Cliente("Carlos Andrade", "11999990003");
        cliente.setId(3L);

        when(clienteRepository.findById(3L)).thenReturn(Optional.of(cliente));

        mockMvc.perform(post("/admin/clientes/3/observacoes")
                        .with(csrf())
                        .param("observacoes", "Nova observação técnica anotada."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/clientes/3"))
                .andExpect(flash().attributeExists("mensagemSucesso"));

        verify(clienteRepository).save(any(Cliente.class));
    }
}
