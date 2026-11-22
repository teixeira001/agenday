package br.com.agenday.controller;

import br.com.agenday.config.SecurityConfig;
import br.com.agenday.model.Servico;
import br.com.agenday.repository.ServicoRepository;
import java.math.BigDecimal;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminServicoController.class)
@Import(SecurityConfig.class)
class AdminServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServicoRepository servicoRepository;

    @Test
    @DisplayName("GET /admin/servicos deve exigir login administrativo")
    void testAcessoAnonimoRedirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/admin/servicos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /admin/servicos deve exibir catálogo com lista de serviços")
    void testListarServicos() throws Exception {
        Servico s1 = new Servico("Drenagem Linfática", "Desc", 60, new BigDecimal("130.00"));
        s1.setId(1L);
        when(servicoRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(s1));

        mockMvc.perform(get("/admin/servicos"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/servicos"))
                .andExpect(model().attributeExists("servicos"))
                .andExpect(model().attributeExists("servicoNovo"))
                .andExpect(model().attribute("paginaAtiva", "servicos"));

        verify(servicoRepository).findAllByOrderByNomeAsc();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /admin/servicos com dados válidos deve cadastrar novo serviço")
    void testCadastrarServicoComSucesso() throws Exception {
        mockMvc.perform(post("/admin/servicos")
                        .with(csrf())
                        .param("nome", "Corte e Escova")
                        .param("duracaoMinutos", "45")
                        .param("valor", "90.00")
                        .param("descricao", "Lavagem e finalização"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/servicos"))
                .andExpect(flash().attributeExists("mensagemSucesso"));

        verify(servicoRepository).save(any(Servico.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /admin/servicos com dados inválidos deve reexibir tela com erros de validação")
    void testCadastrarServicoComErroValidacao() throws Exception {
        mockMvc.perform(post("/admin/servicos")
                        .with(csrf())
                        .param("nome", "") // Inválido: NotBlank
                        .param("duracaoMinutos", "-10") // Inválido: Positive
                        .param("valor", "0.00")) // Inválido: Positive
                .andExpect(status().isOk())
                .andExpect(view().name("admin/servicos"))
                .andExpect(model().attribute("abrirModalCadastro", true))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /admin/servicos/{id} deve atualizar serviço existente")
    void testAtualizarServicoComSucesso() throws Exception {
        Servico servico = new Servico("Massagem", "Desc", 60, new BigDecimal("100.00"));
        servico.setId(1L);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        mockMvc.perform(post("/admin/servicos/1")
                        .with(csrf())
                        .param("nome", "Massagem Atualizada")
                        .param("duracaoMinutos", "75")
                        .param("valor", "160.00")
                        .param("descricao", "Nova descrição"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/servicos"))
                .andExpect(flash().attributeExists("mensagemSucesso"));

        verify(servicoRepository).save(servico);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /admin/servicos/{id}/toggle-status deve alternar disponibilidade do serviço")
    void testAlternarStatusServico() throws Exception {
        Servico servico = new Servico("Massagem", "Desc", 60, new BigDecimal("100.00"));
        servico.setId(1L);
        servico.setAtivo(true);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        mockMvc.perform(post("/admin/servicos/1/toggle-status")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/servicos"))
                .andExpect(flash().attributeExists("mensagemSucesso"));

        assertFalse(servico.isAtivo());
        verify(servicoRepository).save(servico);
    }
}
