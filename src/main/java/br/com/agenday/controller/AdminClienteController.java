package br.com.agenday.controller;

import br.com.agenday.model.Agendamento;
import br.com.agenday.model.Cliente;
import br.com.agenday.model.StatusAgendamento;
import br.com.agenday.repository.AgendamentoRepository;
import br.com.agenday.repository.ClienteRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador administrativo responsável pela gestão de clientes, histórico de sessões
 * e gerenciamento da ficha técnica / anamnese / observações personalizadas (RF11).
 */
@Controller
@RequestMapping("/admin/clientes")
public class AdminClienteController {

    private final ClienteRepository clienteRepository;
    private final AgendamentoRepository agendamentoRepository;

    public AdminClienteController(ClienteRepository clienteRepository,
                                  AgendamentoRepository agendamentoRepository) {
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    /**
     * Lista todos os clientes cadastrados com opção de busca por nome ou telefone.
     */
    @GetMapping
    public String listarClientes(
            @RequestParam(value = "busca", required = false) String busca,
            Model model) {

        List<Cliente> clientes;
        if (busca != null && !busca.trim().isEmpty()) {
            clientes = clienteRepository.buscarPorNomeOuTelefone(busca.trim());
        } else {
            clientes = clienteRepository.findAllByOrderByNomeAsc();
        }

        model.addAttribute("clientes", clientes);
        model.addAttribute("busca", busca);
        model.addAttribute("paginaAtiva", "clientes");
        return "admin/clientes";
    }

    /**
     * Exibe o perfil detalhado do cliente: dados de contato, ficha técnica/anamnese
     * e histórico completo de atendimentos passados e futuros.
     */
    @GetMapping("/{id}")
    public String exibirDetalhesCliente(@PathVariable("id") Long id, Model model) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com ID: " + id));

        List<Agendamento> historico = agendamentoRepository.findByClienteIdComServicoOrderByDataDesc(id);

        long atendimentosRealizados = historico.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.CONCLUIDO)
                .count();

        BigDecimal valorTotalInvestido = historico.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.CONCLUIDO && a.getServico() != null)
                .map(a -> a.getServico().getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cliente", cliente);
        model.addAttribute("historico", historico);
        model.addAttribute("atendimentosRealizados", atendimentosRealizados);
        model.addAttribute("valorTotalInvestido", valorTotalInvestido);
        model.addAttribute("paginaAtiva", "clientes");

        return "admin/cliente-detalhes";
    }

    /**
     * Salva as anotações da ficha técnica (anamnese de massoterapia, histórico químico de cabeleireira, etc).
     */
    @PostMapping("/{id}/observacoes")
    public String salvarObservacoes(
            @PathVariable("id") Long id,
            @RequestParam(value = "observacoes", required = false) String observacoes,
            RedirectAttributes redirectAttributes) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com ID: " + id));

        cliente.setObservacoes(observacoes);
        clienteRepository.save(cliente);

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Ficha técnica e anotações atualizadas com sucesso!");
        return "redirect:/admin/clientes/" + id;
    }
}
