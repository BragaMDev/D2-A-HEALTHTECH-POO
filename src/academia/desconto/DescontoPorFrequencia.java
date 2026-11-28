package academia.desconto;

import academia.modelo.Matricula;
import academia.valor.Dinheiro;

/** Estratégia de fidelidade: 10% para quem já fez 12 ou mais check-ins. */
public final class DescontoPorFrequencia implements PoliticaDeDesconto {
    public static final int MINIMO_DE_CHECKINS = 12;
    public static final int PERCENTUAL = 10;

    @Override public Dinheiro aplicar(Matricula matricula, Dinheiro valorBase) {
        return matricula.checkins().size() >= MINIMO_DE_CHECKINS ? valorBase.comDesconto(PERCENTUAL) : valorBase;
    }

    @Override public String descricao() {
        return "Fidelidade: " + PERCENTUAL + "% a partir de " + MINIMO_DE_CHECKINS + " check-ins";
    }
}
