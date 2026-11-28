package academia.desconto;

import academia.modelo.Matricula;
import academia.valor.Dinheiro;

/** Estratégia padrão: cobra o preço cheio do plano. */
public final class SemDesconto implements PoliticaDeDesconto {
    @Override public Dinheiro aplicar(Matricula matricula, Dinheiro valorBase) { return valorBase; }
    @Override public String descricao() { return "Sem desconto"; }
}
