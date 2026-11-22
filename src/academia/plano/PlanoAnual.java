package academia.plano;

import academia.enums.TipoPlano;
import academia.valor.Dinheiro;

/** Plano ANUAL. */
public final class PlanoAnual extends Plano {
    public PlanoAnual() { super(TipoPlano.ANUAL); }
    @Override public Dinheiro precoMensal() { return Dinheiro.reais("79.90"); }
    @Override public int mesesDeContrato() { return 12; }
    @Override public int diasMaximosDeCongelamentoPorAno() { return 30; }
}
