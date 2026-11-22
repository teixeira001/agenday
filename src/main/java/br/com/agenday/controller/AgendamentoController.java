package br.com.agenday.controller;

import br.com.agenday.dto.AgendamentoRequestDTO;
import br.com.agenday.model.Agendamento;
import br.com.agenday.repository.AgendamentoRepository;
import br.com.agenday.repository.ServicoRepository;
import br.com.agenday.service.AgendamentoService;
import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AgendamentoController {

    private final ServicoRepository servicoRepository;
    private final AgendamentoService agendamentoService;
    private final AgendamentoRepository agendamentoRepository;

    public AgendamentoController(ServicoRepository servicoRepository,
                                 AgendamentoService agendamentoService,
                                 AgendamentoRepository agendamentoRepository) {
        this.servicoRepository = servicoRepository;
        this.agendamentoService = agendamentoService;
        this.agendamentoRepository = agendamentoRepository;
    }

    /**
     * Exibe o formulário de agendamento mobile-first para o cliente.
     */
    @GetMapping("/agendamento")
    public String exibirFormularioAgendamento(Model model) {
        if (!model.containsAttribute("agendamentoDTO")) {
            model.addAttribute("agendamentoDTO", new AgendamentoRequestDTO());
        }
        model.addAttribute("servicos", servicoRepository.findAllByAtivoTrueOrderByNomeAsc());
        model.addAttribute("dataMinima", LocalDate.now());
        return "agendamento";
    }

    /**
     * Endpoint REST consumido pelo JavaScript da página para buscar horários livres em tempo real.
     */
    @GetMapping("/api/horarios-disponiveis")
    @ResponseBody
    public ResponseEntity<List<String>> consultarHorariosDisponiveis(
            @RequestParam("servicoId") Long servicoId,
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        try {
            List<LocalTime> horarios = agendamentoService.obterHorariosDisponiveis(data, servicoId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            List<String> formatados = horarios.stream().map(formatter::format).toList();
            return ResponseEntity.ok(formatados);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Processa a submissão do agendamento com validação de dados e concorrência (PRG Pattern).
     */
    @PostMapping("/agendamento")
    public String processarAgendamento(
            @Valid @ModelAttribute("agendamentoDTO") AgendamentoRequestDTO dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("servicos", servicoRepository.findAllByAtivoTrueOrderByNomeAsc());
            model.addAttribute("dataMinima", LocalDate.now());
            return "agendamento";
        }

        try {
            Agendamento agendamentoCriado = agendamentoService.criarAgendamento(dto);
            return "redirect:/agendamento/sucesso/" + agendamentoCriado.getId();
        } catch (IllegalStateException | IllegalArgumentException e) {
            model.addAttribute("servicos", servicoRepository.findAllByAtivoTrueOrderByNomeAsc());
            model.addAttribute("dataMinima", LocalDate.now());
            model.addAttribute("erroDisponibilidade", e.getMessage());
            return "agendamento";
        }
    }

    /**
     * Exibe a tela de sucesso com resumo da reserva e o botão de WhatsApp.
     */
    @GetMapping("/agendamento/sucesso/{id}")
    public String exibirSucesso(@PathVariable("id") Long id, Model model) {
        Agendamento agendamento = agendamentoRepository.findByIdComDetalhes(id)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado: " + id));

        model.addAttribute("agendamento", agendamento);

        // Formatação amigável para a mensagem de WhatsApp
        DateTimeFormatter dataFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter horaFmt = DateTimeFormatter.ofPattern("HH:mm");

        String mensagemWhatsApp = String.format(
                "Olá! Acabei de agendar uma sessão no Agenday:\n\n" +
                "💆 *Serviço:* %s\n" +
                "📅 *Data:* %s\n" +
                "⏰ *Horário:* %s\n" +
                "👤 *Nome:* %s\n\n" +
                "Poderia me confirmar o atendimento?",
                agendamento.getServico().getNome(),
                agendamento.getData().format(dataFmt),
                agendamento.getHorario().format(horaFmt),
                agendamento.getCliente().getNome()
        );

        String linkWhatsApp = "https://api.whatsapp.com/send?text=" +
                URLEncoder.encode(mensagemWhatsApp, StandardCharsets.UTF_8);

        model.addAttribute("linkWhatsApp", linkWhatsApp);
        return "sucesso";
    }
}
