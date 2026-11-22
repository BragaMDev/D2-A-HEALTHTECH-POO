package academia.modelo;

import java.time.LocalDate;

import academia.enums.ObjetivoTreino;
import academia.excecoes.RegraDeNegocioException;
import academia.interfaces.Vencivel;
import academia.valor.FichaTreino;

/** RN08: avaliação física imutável; vale 90 dias e define a ficha de treino. */
public final class AvaliacaoFisica implements Vencivel {
    public static final int VALIDADE_EM_DIAS = 90;

    private final LocalDate realizadaEm;
    private final double pesoKg;
    private final double alturaMetros;
    private final ObjetivoTreino objetivo;

    public AvaliacaoFisica(LocalDate realizadaEm, double pesoKg, double alturaMetros, ObjetivoTreino objetivo) {
        if (realizadaEm == null || objetivo == null) throw new RegraDeNegocioException("Avaliação exige data e objetivo.");
        if (pesoKg <= 0 || alturaMetros <= 0) throw new RegraDeNegocioException("Peso e altura devem ser positivos.");
        this.realizadaEm = realizadaEm;
        this.pesoKg = pesoKg;
        this.alturaMetros = alturaMetros;
        this.objetivo = objetivo;
    }

    public LocalDate realizadaEm() { return realizadaEm; }
    public FichaTreino gerarFichaDeTreino() { return new FichaTreino(objetivo, objetivo.exercicios()); }

    @Override public LocalDate dataDeVencimento() { return realizadaEm.plusDays(VALIDADE_EM_DIAS); }
    @Override public String descricao() { return "Renovação da avaliação física"; }

    @Override public String toString() {
        return String.format("Avaliação de %s (%.0f kg, %.2f m, objetivo %s)", realizadaEm, pesoKg, alturaMetros, objetivo);
    }
}
