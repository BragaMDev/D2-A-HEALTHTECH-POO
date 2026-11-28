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
    public static Dinheiro zero() { return new Dinheiro(BigDecimal.ZERO); }
    public Dinheiro somar(Dinheiro outro) { return new Dinheiro(valor.add(outro.valor)); }

    /** Aplica desconto percentual (0 a 100); arredonda em 2 casas. */
    public Dinheiro comDesconto(int percentual) {
        if (percentual < 0 || percentual > 100) throw new RegraDeNegocioException("Percentual de desconto deve estar entre 0 e 100.");
        return new Dinheiro(valor.multiply(BigDecimal.valueOf(100 - percentual)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
    }

    public Dinheiro multiplicar(int fator) { return new Dinheiro(valor.multiply(BigDecimal.valueOf(fator))); }

    @Override public String toString() { return "R$ " + valor.toPlainString().replace('.', ','); }
}
