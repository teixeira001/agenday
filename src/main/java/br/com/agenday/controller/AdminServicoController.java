package br.com.agenday.controller;

import br.com.agenday.model.Servico;
import br.com.agenday.repository.ServicoRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador administrativo para gerenciamento de serviços (CRUD) (RF12).
 * Permite que profissionais de qualquer segmento (massoterapia, salão de beleza/cabeleireira,
 * estética, etc.) criem, alterem e ativem/desativem serviços sem necessidade de intervenção técnica.
 */
@Controller
@RequestMapping("/admin/servicos")
public class AdminServicoController {

    private final ServicoRepository servicoRepository;

    public AdminServicoController(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    /**
     * Exibe a lista completa de serviços (ativos e inativos) com ações de gestão.
     */
    @GetMapping
    public String listarServicos(Model model) {
        List<Servico> servicos = servicoRepository.findAllByOrderByNomeAsc();
        model.addAttribute("servicos", servicos);
        if (!model.containsAttribute("servicoNovo")) {
            model.addAttribute("servicoNovo", new Servico());
        }
        model.addAttribute("paginaAtiva", "servicos");
        return "admin/servicos";
    }

    /**
     * Cadastra um novo serviço no catálogo do profissional.
     */
    @PostMapping
    public String cadastrarServico(
            @Valid @ModelAttribute("servicoNovo") Servico servicoNovo,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("servicos", servicoRepository.findAllByOrderByNomeAsc());
            model.addAttribute("abrirModalCadastro", true);
            model.addAttribute("paginaAtiva", "servicos");
            return "admin/servicos";
        }

        servicoNovo.setAtivo(true);
        servicoRepository.save(servicoNovo);

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Serviço '" + servicoNovo.getNome() + "' cadastrado com sucesso!");
        return "redirect:/admin/servicos";
    }

    /**
     * Atualiza os dados de um serviço existente (nome, descrição, duração e preço).
     */
    @PostMapping("/{id}")
    public String atualizarServico(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("servicoEdicao") Servico servicoForm,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        Servico servicoExistente = servicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado com ID: " + id));

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Dados inválidos ao tentar atualizar o serviço.");
            return "redirect:/admin/servicos";
        }

        servicoExistente.setNome(servicoForm.getNome());
        servicoExistente.setDescricao(servicoForm.getDescricao());
        servicoExistente.setDuracaoMinutos(servicoForm.getDuracaoMinutos());
        servicoExistente.setValor(servicoForm.getValor());

        servicoRepository.save(servicoExistente);

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Serviço '" + servicoExistente.getNome() + "' atualizado com sucesso!");
        return "redirect:/admin/servicos";
    }

    /**
     * Alterna o status do serviço entre Ativo e Inativo (Soft Toggle).
     * Serviços inativos continuam no histórico da agenda, mas não aparecem para novos agendamentos públicos.
     */
    @PostMapping("/{id}/toggle-status")
    public String alternarStatusServico(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado com ID: " + id));

        boolean novoStatus = !servico.isAtivo();
        servico.setAtivo(novoStatus);
        servicoRepository.save(servico);

        String statusTexto = novoStatus ? "ativado" : "desativado";
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Serviço '" + servico.getNome() + "' foi " + statusTexto + " com sucesso!");
        return "redirect:/admin/servicos";
    }
}
