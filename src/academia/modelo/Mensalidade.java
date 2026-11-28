package academia.modelo;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

import academia.excecoes.RegraDeNegocioException;
import academia.interfaces.Vencivel;
import academia.valor.Dinheiro;

/** RN04: mensalidade com vencimento. */
public final class Mensalidade implements Vencivel {
    private final YearMonth competencia;
    private final Dinheiro valor;
    private final LocalDate vencimento;
    private boolean paga;

    public Mensalidade(YearMonth competencia, Dinheiro valor, LocalDate vencimento) {
        if (competencia == null || valor == null || vencimento == null)
            throw new RegraDeNegocioException("Mensalidade incompleta.");
        this.competencia = competencia;
        this.valor = valor;
        this.vencimento = vencimento;
    }

    public YearMonth competencia() { return competencia; }
    public Dinheiro valor() { return valor; }
    public boolean estaPaga() { return paga; }

    /** RN04: dias corridos de atraso (0 se paga ou no prazo). */
    public long diasDeAtraso(LocalDate hoje) {
        return paga ? 0 : Math.max(0, ChronoUnit.DAYS.between(vencimento, hoje));
    }

    public void pagar() {
        if (paga) throw new RegraDeNegocioException("Mensalidade " + competencia + " já está paga.");
        paga = true;
    }

    @Override public LocalDate dataDeVencimento() { return vencimento; }
    @Override public boolean estaVencido(LocalDate hoje) { return !paga && Vencivel.super.estaVencido(hoje); }
    @Override public String descricao() { return "Mensalidade " + competencia + " (" + valor + ")"; }
    @Override public String toString() { return descricao() + " vence " + vencimento + (paga ? " [PAGA]" : " [PENDENTE]"); }
}
