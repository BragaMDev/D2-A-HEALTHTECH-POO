package academia.plano;

import academia.enums.TipoPlano;
import academia.valor.Dinheiro;

/** Plano MENSAL. */
public final class PlanoMensal extends Plano {
    public PlanoMensal() { super(TipoPlano.MENSAL); }
    @Override public Dinheiro precoMensal() { return Dinheiro.reais("99.90"); }
    @Override public int mesesDeContrato() { return 1; }
    @Override public int diasMaximosDeCongelamentoPorAno() { return 0; }
}
