package academia.enums;

import academia.excecoes.TransicaoDeEstadoInvalidaException;

/** R07 / RN06: ciclo de vida. Só existem transições por métodos de negócio; as demais são recusadas. */
public enum EstadoMatricula {
    ATIVA, CONGELADA, BLOQUEADA, CANCELADA;

    public boolean permiteCheckin() { return this == ATIVA; }             // RN03

    public EstadoMatricula congelar() {                                    // RN06: bloqueada/cancelada não congelam
        if (this == ATIVA) return CONGELADA;
        throw recusar("congelar");
    }

    public EstadoMatricula descongelar() {
        if (this == CONGELADA) return ATIVA;
        throw recusar("descongelar");
    }

    public EstadoMatricula bloquear() {                                    // RN04
        if (this == ATIVA) return BLOQUEADA;
        throw recusar("bloquear");
    }

    public EstadoMatricula desbloquear() {                                 // RN04/RN06: só quando o problema for resolvido
        if (this == BLOQUEADA) return ATIVA;
        throw recusar("desbloquear");
    }

    public EstadoMatricula cancelar() {                                    // RN06: qualquer estado; CANCELADA é terminal
        if (this != CANCELADA) return CANCELADA;
        throw recusar("cancelar");
    }

    private TransicaoDeEstadoInvalidaException recusar(String acao) {
        return new TransicaoDeEstadoInvalidaException("RN06: não é possível " + acao + " uma matrícula " + this + ".");
    }
}
