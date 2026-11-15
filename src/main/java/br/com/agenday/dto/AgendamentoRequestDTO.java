package br.com.agenday.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;

public class AgendamentoRequestDTO {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
    private String nomeCliente;

    @NotBlank(message = "O telefone / WhatsApp é obrigatório.")
    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
    private String telefoneCliente;

    @NotNull(message = "Selecione um serviço.")
    private Long servicoId;

    @NotNull(message = "A data é obrigatória.")
    @FutureOrPresent(message = "A data não pode ser no passado.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate data;

    @NotNull(message = "O horário é obrigatório.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime horario;

    private String observacao;

    public AgendamentoRequestDTO() {
    }

    public AgendamentoRequestDTO(String nomeCliente, String telefoneCliente, Long servicoId, LocalDate data, LocalTime horario) {
        this.nomeCliente = nomeCliente;
        this.telefoneCliente = telefoneCliente;
        this.servicoId = servicoId;
        this.data = data;
        this.horario = horario;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getTelefoneCliente() {
        return telefoneCliente;
    }

    public void setTelefoneCliente(String telefoneCliente) {
        this.telefoneCliente = telefoneCliente;
    }

    public Long getServicoId() {
        return servicoId;
    }

    public void setServicoId(Long servicoId) {
        this.servicoId = servicoId;
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

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
