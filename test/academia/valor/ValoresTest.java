package academia.valor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import academia.excecoes.RegraDeNegocioException;

class ValoresTest {
    @Test
    void cpfComTamanhoErradoLanca() {
        assertThrows(RegraDeNegocioException.class, () -> new Cpf("123"));
        assertThrows(RegraDeNegocioException.class, () -> new Cpf(null));
    }

    @Test
    void cpfIgnoraPontuacao() {
        assertEquals(new Cpf("52998224725"), new Cpf("529.982.247-25"));
    }

    @Test
    void dinheiroNegativoLanca() {
        assertThrows(RegraDeNegocioException.class, () -> Dinheiro.reais("-1"));
    }

    @Test
    void descontoPercentualArredondaEmDuasCasas() {
        assertEquals(Dinheiro.reais("89.91"), Dinheiro.reais("99.90").comDesconto(10));
        assertEquals(Dinheiro.zero(), Dinheiro.reais("99.90").comDesconto(100));
        assertThrows(RegraDeNegocioException.class, () -> Dinheiro.reais("10").comDesconto(101));
    }
}
