package br.com.agenday.model;

public enum StatusAgendamento {
    AGENDADO("Agendado"),
    CANCELADO("Cancelado"),
    CONCLUIDO("Concluído"),
    BLOQUEADO("Bloqueado");

    private final String descricao;

    StatusAgendamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
