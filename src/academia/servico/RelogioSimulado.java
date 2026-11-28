package academia.servico;

import java.time.LocalDate;

import academia.excecoes.RegraDeNegocioException;
import academia.interfaces.RelogioAcademia;

/** Relógio que pode avançar, para testar atraso, vencimento da avaliação e upgrade. */
public final class RelogioSimulado implements RelogioAcademia {
    private LocalDate hoje = LocalDate.now();

    public void avancarDias(int dias) {
        if (dias <= 0) throw new RegraDeNegocioException("Informe uma quantidade positiva de dias.");
        hoje = hoje.plusDays(dias);
    }

    @Override public LocalDate hoje() { return hoje; }
}
