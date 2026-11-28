package academia.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import academia.excecoes.TransicaoDeEstadoInvalidaException;

class EstadoMatriculaTest {
    @Test
    void transicoesValidas() {
        assertEquals(EstadoMatricula.CONGELADA, EstadoMatricula.ATIVA.congelar());
        assertEquals(EstadoMatricula.ATIVA, EstadoMatricula.CONGELADA.descongelar());
        assertEquals(EstadoMatricula.BLOQUEADA, EstadoMatricula.ATIVA.bloquear());
        assertEquals(EstadoMatricula.ATIVA, EstadoMatricula.BLOQUEADA.desbloquear());
    }

    @Test
    void transicoesInvalidasLancamExcecaoDeDominio() {
        assertThrows(TransicaoDeEstadoInvalidaException.class, () -> EstadoMatricula.BLOQUEADA.congelar());
        assertThrows(TransicaoDeEstadoInvalidaException.class, () -> EstadoMatricula.CONGELADA.congelar());
        assertThrows(TransicaoDeEstadoInvalidaException.class, () -> EstadoMatricula.ATIVA.descongelar());
        assertThrows(TransicaoDeEstadoInvalidaException.class, () -> EstadoMatricula.CANCELADA.cancelar());
        assertThrows(TransicaoDeEstadoInvalidaException.class, () -> EstadoMatricula.CANCELADA.desbloquear());
    }

    @Test
    void somenteAtivaPermiteCheckin() {
        assertTrue(EstadoMatricula.ATIVA.permiteCheckin());
        assertFalse(EstadoMatricula.CONGELADA.permiteCheckin());
        assertFalse(EstadoMatricula.BLOQUEADA.permiteCheckin());
        assertFalse(EstadoMatricula.CANCELADA.permiteCheckin());
    }
}
