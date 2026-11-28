package academia.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import academia.enums.EstadoMatricula;
import academia.enums.ObjetivoTreino;
import academia.enums.TipoPlano;
import academia.excecoes.AvaliacaoFisicaVencidaException;
import academia.excecoes.CheckinNaoPermitidoException;
import academia.excecoes.CongelamentoNaoPermitidoException;
import academia.excecoes.MatriculaDuplicadaException;
import academia.excecoes.RegraDeNegocioException;
import academia.excecoes.TransicaoDeEstadoInvalidaException;
import academia.valor.Cpf;
import academia.valor.Dinheiro;

class MatriculaTest {
    private static final LocalDate HOJE = LocalDate.of(2026, 3, 10);

    private Matricula mensal;
    private Matricula anual;

    private static AvaliacaoFisica avaliacao(LocalDate data) {
        return new AvaliacaoFisica(data, 70, 1.75, ObjetivoTreino.HIPERTROFIA);
    }

    @BeforeEach
    void preparar() {
        mensal = Matricula.abrir(1, new Aluno("Bruno", new Cpf("11144477735")), TipoPlano.MENSAL, avaliacao(HOJE.minusDays(1)), HOJE);
        anual = Matricula.abrir(2, new Aluno("Ana", new Cpf("52998224725")), TipoPlano.ANUAL, avaliacao(HOJE.minusDays(1)), HOJE);
    }

    // ---------- RN01 ----------
    @Test
    @DisplayName("RN01: o mesmo aluno não pode ter duas matrículas")
    void segundaMatriculaDoMesmoAlunoLanca() {
        Aluno aluno = new Aluno("Carla", new Cpf("39053344705"));
        Matricula.abrir(3, aluno, TipoPlano.MENSAL, avaliacao(HOJE), HOJE);
        assertThrows(MatriculaDuplicadaException.class,
                () -> Matricula.abrir(4, aluno, TipoPlano.ANUAL, avaliacao(HOJE), HOJE));
    }

    // ---------- RN08 ----------
    @Test
    @DisplayName("RN08: não matricula com avaliação física vencida")
    void avaliacaoVencidaImpedeMatricula() {
        AvaliacaoFisica velha = avaliacao(HOJE.minusDays(91));
        Aluno aluno = new Aluno("Diego", new Cpf("16899535009"));
        assertThrows(AvaliacaoFisicaVencidaException.class,
                () -> Matricula.abrir(5, aluno, TipoPlano.MENSAL, velha, HOJE));
    }

    // ---------- RN03 ----------
    @Test
    @DisplayName("RN03: só 1 check-in por dia")
    void doisCheckinsNoMesmoDiaLancam() {
        mensal.fazerCheckin(HOJE);
        assertThrows(CheckinNaoPermitidoException.class, () -> mensal.fazerCheckin(HOJE));
        mensal.fazerCheckin(HOJE.plusDays(1)); // no dia seguinte pode
        assertEquals(2, mensal.checkins().size());
    }

    @Test
    @DisplayName("RN03: matrícula congelada não faz check-in")
    void checkinComMatriculaCongeladaLanca() {
        anual.congelar(HOJE);
        assertThrows(CheckinNaoPermitidoException.class, () -> anual.fazerCheckin(HOJE));
    }

    // ---------- RN02 ----------
    @Test
    @DisplayName("RN02: só o plano anual congela")
    void planoMensalNaoCongela() {
        assertThrows(CongelamentoNaoPermitidoException.class, () -> mensal.congelar(HOJE));
        assertEquals(EstadoMatricula.ATIVA, mensal.estado()); // estado não mudou
    }

    @Test
    @DisplayName("RN02: cota de 30 dias/ano esgotada impede novo congelamento")
    void cotaDeCongelamentoEsgotada() {
        anual.congelar(HOJE);
        anual.descongelar(HOJE.plusDays(30)); // usa os 30 dias
        assertThrows(CongelamentoNaoPermitidoException.class, () -> anual.congelar(HOJE.plusDays(31)));
    }

    // ---------- RN04 ----------
    @Test
    @DisplayName("RN04: atraso de exatamente 10 dias não bloqueia; 11 bloqueia")
    void bloqueioSoComAtrasoMaiorQue10Dias() {
        LocalDate vencimento = HOJE.plusDays(5);
        mensal.gerarMensalidade(vencimento, HOJE);
        mensal.atualizarSituacao(vencimento.plusDays(10));
        assertEquals(EstadoMatricula.ATIVA, mensal.estado());
        mensal.atualizarSituacao(vencimento.plusDays(11));
        assertEquals(EstadoMatricula.BLOQUEADA, mensal.estado());
    }

    @Test
    @DisplayName("RN04/RN06: pagar o atraso reativa a matrícula bloqueada")
    void pagamentoDesbloqueia() {
        LocalDate vencimento = HOJE.plusDays(5);
        mensal.gerarMensalidade(vencimento, HOJE);
        LocalDate depois = vencimento.plusDays(11);
        mensal.atualizarSituacao(depois);
        assertThrows(CheckinNaoPermitidoException.class, () -> mensal.fazerCheckin(depois));
        mensal.pagarMensalidade(depois);
        assertEquals(EstadoMatricula.ATIVA, mensal.estado());
    }

    // ---------- RN08 (transição automática) ----------
    @Test
    @DisplayName("RN08: avaliação vencida congela; só descongela após renovar")
    void avaliacaoVencidaCongelaERenovacaoLibera() {
        mensal.gerarMensalidade(HOJE.plusDays(5), HOJE);
        mensal.pagarMensalidade(HOJE);                 // sem atraso para isolar a regra RN08
        LocalDate depois = HOJE.plusDays(95);
        mensal.atualizarSituacao(depois);
        assertEquals(EstadoMatricula.CONGELADA, mensal.estado());
        assertThrows(AvaliacaoFisicaVencidaException.class, () -> mensal.descongelar(depois));
        mensal.renovarAvaliacao(avaliacao(depois), depois);
        mensal.descongelar(depois);
        assertEquals(EstadoMatricula.ATIVA, mensal.estado());
    }

    // ---------- RN06 ----------
    @Test
    @DisplayName("RN06: cancelada é terminal")
    void canceladaEhTerminal() {
        mensal.cancelar();
        assertThrows(TransicaoDeEstadoInvalidaException.class, () -> mensal.cancelar());
        assertThrows(CheckinNaoPermitidoException.class, () -> mensal.fazerCheckin(HOJE));
        assertThrows(RegraDeNegocioException.class, () -> mensal.gerarMensalidade(HOJE.plusDays(5), HOJE));
    }

    // ---------- RN07 ----------
    @Test
    @DisplayName("RN07: upgrade só vale no próximo mês e não aceita pendência nem plano igual")
    void upgradeValeSoNoProximoMes() {
        mensal.gerarMensalidade(HOJE.plusDays(5), HOJE);
        assertThrows(RegraDeNegocioException.class, () -> mensal.solicitarMudancaDePlano(TipoPlano.TRIMESTRAL, HOJE)); // pendente
        mensal.pagarMensalidade(HOJE);
        assertThrows(RegraDeNegocioException.class, () -> mensal.solicitarMudancaDePlano(TipoPlano.MENSAL, HOJE));     // igual
        mensal.solicitarMudancaDePlano(TipoPlano.TRIMESTRAL, HOJE);

        assertEquals(TipoPlano.MENSAL, mensal.plano().tipo());                           // ainda no mês atual
        Mensalidade proxima = mensal.gerarMensalidade(HOJE.plusDays(5), HOJE.plusMonths(1));
        assertEquals(TipoPlano.TRIMESTRAL, mensal.plano().tipo());                       // próximo mês: novo plano
        assertEquals(Dinheiro.reais("89.90"), proxima.valor());
    }

    @Test
    @DisplayName("Competências das mensalidades são sequenciais")
    void competenciasSequenciais() {
        Mensalidade a = mensal.gerarMensalidade(HOJE.plusDays(5), HOJE);
        Mensalidade b = mensal.gerarMensalidade(HOJE.plusDays(5), HOJE);
        assertEquals(YearMonth.of(2026, 3), a.competencia());
        assertTrue(b.competencia().isAfter(a.competencia()));
    }
}
