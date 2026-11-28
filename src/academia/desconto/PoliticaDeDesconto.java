package academia.desconto;

import academia.modelo.Matricula;
import academia.valor.Dinheiro;

/** Desafio C (Strategy): regra de desconto da mensalidade, trocável em tempo de execução. */
public interface PoliticaDeDesconto {
    /** Devolve o valor final da mensalidade a partir do valor base do plano. */
    Dinheiro aplicar(Matricula matricula, Dinheiro valorBase);

    String descricao();
}
