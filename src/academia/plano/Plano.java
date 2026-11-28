package academia.plano;

import academia.enums.TipoPlano;
import academia.valor.Dinheiro;

/** R04 / RN02: todo plano tem preço e regras próprias. */
public abstract class Plano {
    private final TipoPlano tipo;

    protected Plano(TipoPlano tipo) { this.tipo = tipo; }

    public TipoPlano tipo() { return tipo; }

    public abstract Dinheiro precoMensal();
    public abstract int mesesDeContrato();
    /** RN02: 0 = sem congelamento (só o Anual devolve 30). */
    public abstract int diasMaximosDeCongelamentoPorAno();

    // métodos concretos que usam os abstratos
    public Dinheiro valorDoContrato() { return precoMensal().multiplicar(mesesDeContrato()); }
    public boolean permiteCongelamento() { return diasMaximosDeCongelamentoPorAno() > 0; }

    @Override public String toString() { return tipo + " (" + precoMensal() + "/mês)"; }
}
