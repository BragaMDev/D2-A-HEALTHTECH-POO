package academia.valor;

import java.math.BigDecimal;
import java.math.RoundingMode;

import academia.excecoes.RegraDeNegocioException;

/** R08: objeto de valor imutável; nunca negativo, 2 casas decimais. */
public record Dinheiro(BigDecimal valor) {
    public Dinheiro {
        if (valor == null || valor.signum() < 0) throw new RegraDeNegocioException("Valor monetário inválido.");
        valor = valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static Dinheiro reais(String texto) { return new Dinheiro(new BigDecimal(texto)); }
    public Dinheiro multiplicar(int fator) { return new Dinheiro(valor.multiply(BigDecimal.valueOf(fator))); }

    @Override public String toString() { return "R$ " + valor.toPlainString().replace('.', ','); }
}
