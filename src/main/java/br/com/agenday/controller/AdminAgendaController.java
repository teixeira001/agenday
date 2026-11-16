package br.com.agenday.controller;

import br.com.agenday.model.Agendamento;
import br.com.agenday.model.StatusAgendamento;
import br.com.agenday.repository.AgendamentoRepository;
import br.com.agenday.service.AgendamentoService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminAgendaController {

    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoService agendamentoService;

    public AdminAgendaController(AgendamentoRepository agendamentoRepository,
                                 AgendamentoService agendamentoService) {
        this.agendamentoRepository = agendamentoRepository;
        this.agendamentoService = agendamentoService;
    }

    /**
     * Exibe a agenda diária administrativa da massoterapeuta (RF05).
     */
    @GetMapping
    public String exibirAgenda(
            @RequestParam(value = "data", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            Model model) {

        LocalDate dataConsulta = (data != null) ? data : LocalDate.now();

        List<Agendamento> agendamentos = agendamentoRepository.findByDataOrderByHorarioAsc(dataConsulta);

        // Estatísticas do dia
        long totalAgendadosOuConcluidos = agendamentos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.AGENDADO || a.getStatus() == StatusAgendamento.CONCLUIDO)
                .count();

        long totalConcluidos = agendamentos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.CONCLUIDO)
                .count();

        BigDecimal faturamentoEstimado = agendamentos.stream()
                .filter(a -> (a.getStatus() == StatusAgendamento.AGENDADO || a.getStatus() == StatusAgendamento.CONCLUIDO) && a.getServico() != null)
                .map(a -> a.getServico().getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("agendamentos", agendamentos);
        model.addAttribute("dataSelecionada", dataConsulta);
        model.addAttribute("dataAnterior", dataConsulta.minusDays(1));
        model.addAttribute("dataProxima", dataConsulta.plusDays(1));
        model.addAttribute("ehHoje", dataConsulta.equals(LocalDate.now()));

        model.addAttribute("totalAtendimentos", totalAgendadosOuConcluidos);
        model.addAttribute("totalConcluidos", totalConcluidos);
        model.addAttribute("faturamentoEstimado", faturamentoEstimado);

        return "admin/agenda";
    }

    /**
     * Marca uma sessão como CONCLUÍDA.
     */
    @PostMapping("/agendamentos/{id}/concluir")
    public String concluirAgendamento(
            @PathVariable("id") Long id,
            @RequestParam("data") String dataStr) {
        agendamentoService.concluirAgendamento(id);
        return "redirect:/admin?data=" + dataStr;
    }

    /**
     * Cancela um agendamento com motivo opcional (RF06).
     */
    @PostMapping("/agendamentos/{id}/cancelar")
    public String cancelarAgendamento(
            @PathVariable("id") Long id,
            @RequestParam("data") String dataStr,
            @RequestParam(value = "motivo", required = false) String motivo) {
        agendamentoService.cancelarAgendamento(id, motivo);
        return "redirect:/admin?data=" + dataStr;
    }

    /**
     * Cria um bloqueio de horário na agenda (UC04 / RF07).
     */
    @PostMapping("/bloqueios")
    public String criarBloqueio(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam("horarioInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horarioInicio,
            @RequestParam("duracaoMinutos") int duracaoMinutos,
            @RequestParam(value = "motivo", required = false) String motivo) {

        agendamentoService.bloquearHorario(data, horarioInicio, duracaoMinutos, motivo);
        return "redirect:/admin?data=" + data;
    }

    /**
     * Remove um bloqueio administrativo.
     */
    @PostMapping("/bloqueios/{id}/remover")
    public String removerBloqueio(
            @PathVariable("id") Long id,
            @RequestParam("data") String dataStr) {
        agendamentoService.removerBloqueio(id);
        return "redirect:/admin?data=" + dataStr;
    }
}
