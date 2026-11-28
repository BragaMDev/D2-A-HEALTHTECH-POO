package academia.interfaces;

import java.time.LocalDate;

/** R05: implementada por Mensalidade e AvaliacaoFisica (sem herança em comum); usada em List<Vencivel>. */
public interface Vencivel {
    LocalDate dataDeVencimento();
    String descricao();

    default boolean estaVencido(LocalDate hoje) { return hoje.isAfter(dataDeVencimento()); }
}
