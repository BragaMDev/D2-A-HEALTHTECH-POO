package academia.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import academia.enums.ObjetivoTreino;
import academia.enums.ResultadoInscricao;
import academia.enums.TipoPlano;
import academia.excecoes.RegraDeNegocioException;
import academia.valor.Cpf;

class AulaColetivaTest {
    private static Matricula matricula(int n, String cpf) {
        LocalDate hoje = LocalDate.of(2026, 3, 10);
        return Matricula.abrir(n, new Aluno("Aluno " + n, new Cpf(cpf)), TipoPlano.MENSAL,
                new AvaliacaoFisica(hoje, 70, 1.7, ObjetivoTreino.CONDICIONAMENTO), hoje);
    }

    @Test
    void listaDeEsperaSegueOrdemDeInscricao() {
        Matricula a = matricula(1, "52998224725"), b = matricula(2, "11144477735"), c = matricula(3, "39053344705");
        AulaColetiva aula = new AulaColetiva(1, "Spinning", 1);

        assertEquals(ResultadoInscricao.CONFIRMADA, aula.inscrever(a));
        assertEquals(ResultadoInscricao.EM_LISTA_DE_ESPERA, aula.inscrever(b));
        assertEquals(ResultadoInscricao.EM_LISTA_DE_ESPERA, aula.inscrever(c));

        Optional<Matricula> promovido = aula.cancelarInscricao(a);
        assertEquals(Optional.of(b), promovido); // FIFO: b entrou antes de c
        assertEquals(1, aula.listaDeEspera().size());
    }

    @Test
    void inscricaoDuplicadaLanca() {
        Matricula a = matricula(1, "52998224725");
        AulaColetiva aula = new AulaColetiva(1, "Yoga", 5);
        aula.inscrever(a);
        assertThrows(RegraDeNegocioException.class, () -> aula.inscrever(a));
    }
}
