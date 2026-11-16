package br.com.agenday.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento com Cliente. Opcional (nullable = true) para permitir bloqueios administrativos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = true)
    private Cliente cliente;

    // Relacionamento com Serviço. Opcional para bloqueios administrativos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", nullable = true)
    private Servico servico;

    @NotNull(message = "A data é obrigatória.")
    @Column(nullable = false)
    private LocalDate data;

    @NotNull(message = "O horário é obrigatório.")
    @Column(nullable = false)
    private LocalTime horario;

    @NotNull(message = "O status do agendamento é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    @Size(max = 500, message = "A observação não pode ultrapassar 500 caracteres.")
    @Column(length = 500)
    private String observacao;

    // Duração customizada em minutos (utilizada para bloqueios administrativos quando não há serviço vinculado)
    @Column(name = "duracao_bloqueio_minutos")
    private Integer duracaoBloqueioMinutos;

    public Agendamento() {
    }

    public Agendamento(Cliente cliente, Servico servico, LocalDate data, LocalTime horario, StatusAgendamento status) {
        this.cliente = cliente;
        this.servico = servico;
        this.data = data;
        this.horario = horario;
        this.status = status;
    }

    // Construtor específico para Bloqueios Administrativos
    public static Agendamento criarBloqueio(LocalDate data, LocalTime horario, int duracaoMinutos, String motivo) {
        Agendamento bloqueio = new Agendamento();
        bloqueio.setData(data);
        bloqueio.setHorario(horario);
        bloqueio.setDuracaoBloqueioMinutos(duracaoMinutos);
        bloqueio.setStatus(StatusAgendamento.BLOQUEADO);
        bloqueio.setObservacao(motivo != null && !motivo.isBlank() ? motivo : "Bloqueio de Horário");
        return bloqueio;
    }

    // Método utilitário para calcular horário de término baseado na duração do serviço ou do bloqueio
    public LocalTime getHorarioTermino() {
        if (horario == null) {
            return null;
        }
        if (servico != null && servico.getDuracaoMinutos() != null) {
            return horario.plusMinutes(servico.getDuracaoMinutos());
        }
        if (duracaoBloqueioMinutos != null) {
            return horario.plusMinutes(duracaoBloqueioMinutos);
        }
        return horario;
    }

    public Integer getDuracaoBloqueioMinutos() {
        return duracaoBloqueioMinutos;
    }

    public void setDuracaoBloqueioMinutos(Integer duracaoBloqueioMinutos) {
        this.duracaoBloqueioMinutos = duracaoBloqueioMinutos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public void setHorario(LocalTime horario) {
        this.horario = horario;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agendamento that = (Agendamento) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Agendamento{" +
                "id=" + id +
                ", data=" + data +
                ", horario=" + horario +
                ", status=" + status +
                '}';
    }
}
