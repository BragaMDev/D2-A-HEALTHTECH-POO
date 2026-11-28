package academia.enums;

import java.util.List;

/** RN08: o objetivo da avaliação física define a ficha de treino. */
public enum ObjetivoTreino {
    EMAGRECIMENTO(List.of("Esteira 30 min", "Bicicleta 20 min", "Circuito funcional")),
    HIPERTROFIA(List.of("Supino reto", "Agachamento livre", "Remada curvada")),
    CONDICIONAMENTO(List.of("Elíptico 25 min", "Corrida intervalada", "Prancha"));

    private final List<String> exercicios;

    ObjetivoTreino(List<String> exercicios) { this.exercicios = exercicios; }

    public List<String> exercicios() { return exercicios; }
}
