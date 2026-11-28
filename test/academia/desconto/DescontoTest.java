package academia.desconto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import academia.enums.ObjetivoTreino;
import academia.enums.TipoPlano;
import academia.excecoes.RegraDeNegocioException;
import academia.modelo.AvaliacaoFisica;
import academia.servico.RelogioSimulado;
import academia.servico.SecretariaDaAcademia;
import academia.valor.Dinheiro;

/** Desafio C: a mesma Secretaria cobra valores diferentes conforme a estratégia em uso. */
class DescontoTest {
    private static final String CPF = "11144477735";
    private final RelogioSimulado relogio = new RelogioSimulado();
    private final SecretariaDaAcademia secretaria = new SecretariaDaAcademia(relogio);

    private void matricular() {
        secretaria.matricular("Bruno", CPF, TipoPlano.MENSAL,
                new AvaliacaoFisica(relogio.hoje(), 80, 1.8, ObjetivoTreino.HIPERTROFIA));
    }

    @Test
    void estrategiaPodeSerTrocadaEmTempoDeExecucao() {
        matricular();
        assertEquals(Dinheiro.reais("99.90"), secretaria.gerarMensalidade(CPF).valor());

        secretaria.trocarPoliticaDeDesconto(new DescontoPercentual(50));
        assertEquals(Dinheiro.reais("49.95"), secretaria.gerarMensalidade(CPF).valor());

        secretaria.trocarPoliticaDeDesconto(new SemDesconto());
        assertEquals(Dinheiro.reais("99.90"), secretaria.gerarMensalidade(CPF).valor());
    }

    @Test
    void descontoPorFrequenciaSoApareceComCheckinsSuficientes() {
        matricular();
        secretaria.trocarPoliticaDeDesconto(new DescontoPorFrequencia());
        assertEquals(Dinheiro.reais("99.90"), secretaria.gerarMensalidade(CPF).valor()); // 0 check-ins

        for (int i = 0; i < DescontoPorFrequencia.MINIMO_DE_CHECKINS; i++) {
            secretaria.fazerCheckin(CPF);
            relogio.avancarDias(1);
            secretaria.pagarMensalidade(CPF);   // mantém a matrícula em dia
            secretaria.gerarMensalidade(CPF);
        }
        assertEquals(Dinheiro.reais("89.91"), secretaria.gerarMensalidade(CPF).valor()); // 10% off
    }

    @Test
    void politicaInvalidaELancada() {
        assertThrows(RegraDeNegocioException.class, () -> new DescontoPercentual(150));
        assertThrows(RegraDeNegocioException.class, () -> secretaria.trocarPoliticaDeDesconto(null));
    }
}
