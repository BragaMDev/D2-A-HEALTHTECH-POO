package academia.desconto;

import academia.modelo.Matricula;
import academia.valor.Dinheiro;

/** Estratégia de campanha: o mesmo percentual para todos (0 a 100). */
public final class DescontoPercentual implements PoliticaDeDesconto {
    private final int percentual;

    public DescontoPercentual(int percentual) {
        Dinheiro.reais("1").comDesconto(percentual); // reaproveita a validação 0..100 do Dinheiro
        this.percentual = percentual;
    }

    @Override public Dinheiro aplicar(Matricula matricula, Dinheiro valorBase) { return valorBase.comDesconto(percentual); }
    @Override public String descricao() { return "Desconto de campanha de " + percentual + "%"; }
}
