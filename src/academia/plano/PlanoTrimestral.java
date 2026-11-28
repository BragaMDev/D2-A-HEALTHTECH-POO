package academia.plano;

import academia.enums.TipoPlano;
import academia.valor.Dinheiro;

/** Plano TRIMESTRAL. */
public final class PlanoTrimestral extends Plano {
    public PlanoTrimestral() { super(TipoPlano.TRIMESTRAL); }
    @Override public Dinheiro precoMensal() { return Dinheiro.reais("89.90"); }
    @Override public int mesesDeContrato() { return 3; }
    @Override public int diasMaximosDeCongelamentoPorAno() { return 0; }
}
