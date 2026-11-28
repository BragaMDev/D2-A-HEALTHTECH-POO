package academia.servico;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import academia.enums.EstadoMatricula;
import academia.enums.ObjetivoTreino;
import academia.enums.TipoPlano;
import academia.modelo.AvaliacaoFisica;
import academia.modelo.Matricula;
import academia.valor.Dinheiro;

/** Desafio D: relatórios com Streams sobre dados conhecidos. */
class RelatoriosGerenciaisTest {
    private final RelogioSimulado relogio = new RelogioSimulado();
    private final SecretariaDaAcademia sec = new SecretariaDaAcademia(relogio);

    private void aluno(String nome, String cpf, TipoPlano tipo) {
        sec.matricular(nome, cpf, tipo, new AvaliacaoFisica(relogio.hoje(), 70, 1.7, ObjetivoTreino.CONDICIONAMENTO));
    }

    private List<Matricula> lista() { return sec.listar(Matricula.POR_NOME); }

    @Test
    void agrupaMatriculasPorEstadoEPendenciasPorPlano() {
        aluno("Ana", "52998224725", TipoPlano.ANUAL);
        aluno("Bruno", "11144477735", TipoPlano.MENSAL);
        sec.pagarMensalidade("52998224725");
        relogio.avancarDias(16); // Bruno passa de 10 dias de atraso

        assertEquals(Map.of(EstadoMatricula.ATIVA, 1L, EstadoMatricula.BLOQUEADA, 1L),
                RelatoriosGerenciais.matriculasPorEstado(lista()));
        Map<TipoPlano, Dinheiro> pendente = RelatoriosGerenciais.receitaPendentePorPlano(lista());
        assertEquals(Dinheiro.zero(), pendente.get(TipoPlano.ANUAL));
        assertEquals(Dinheiro.reais("99.90"), pendente.get(TipoPlano.MENSAL));
    }

    @Test
    void inadimplentesVemOrdenadosPeloMaiorAtraso() {
        aluno("Ana", "52998224725", TipoPlano.MENSAL);      // vence em 5 dias
        relogio.avancarDias(3);
        aluno("Bruno", "11144477735", TipoPlano.MENSAL);    // vence 3 dias depois da Ana
        relogio.avancarDias(9);                             // Ana: 7 dias de atraso; Bruno: 4

        var devedores = RelatoriosGerenciais.inadimplentes(lista(), relogio.hoje());
        assertEquals(List.of("Ana", "Bruno"), devedores.stream().map(d -> d.nome()).toList());
        assertEquals(7, devedores.get(0).diasDeAtraso());
        assertEquals(4, devedores.get(1).diasDeAtraso());
    }

    @Test
    void rankingDeFrequenciaOrdenaELimita() {
        aluno("Ana", "52998224725", TipoPlano.MENSAL);
        aluno("Bruno", "11144477735", TipoPlano.MENSAL);
        aluno("Carla", "39053344705", TipoPlano.MENSAL);
        sec.fazerCheckin("11144477735");
        relogio.avancarDias(1);
        sec.fazerCheckin("11144477735");
        sec.fazerCheckin("39053344705");

        var ranking = RelatoriosGerenciais.rankingDeFrequencia(lista(), 2);
        assertEquals(2, ranking.size());
        assertEquals("Bruno", ranking.get(0).nome());
        assertEquals(2, ranking.get(0).checkins());
        assertEquals("Carla", ranking.get(1).nome());
    }

    @Test
    void mediaDeCheckinsPorPlano() {
        aluno("Ana", "52998224725", TipoPlano.MENSAL);
        aluno("Bruno", "11144477735", TipoPlano.MENSAL);
        sec.fazerCheckin("52998224725");
        assertEquals(0.5, RelatoriosGerenciais.mediaDeCheckinsPorPlano(lista()).get(TipoPlano.MENSAL).doubleValue());
    }
}
