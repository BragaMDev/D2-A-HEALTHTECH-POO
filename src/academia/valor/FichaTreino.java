package academia.valor;

import java.util.List;

import academia.enums.ObjetivoTreino;
import academia.excecoes.RegraDeNegocioException;

/** RN08: ficha de treino definida pela avaliação física. Imutável (R08/R09). */
public record FichaTreino(ObjetivoTreino objetivo, List<String> exercicios) {
    public FichaTreino {
        if (objetivo == null || exercicios == null || exercicios.isEmpty())
            throw new RegraDeNegocioException("Ficha de treino incompleta.");
        exercicios = List.copyOf(exercicios);
    }
}
