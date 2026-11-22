package academia.valor;

import academia.excecoes.RegraDeNegocioException;

/** R08: CPF imutável; guarda só os 11 dígitos. */
public record Cpf(String numero) implements Comparable<Cpf> {
    public Cpf {
        if (numero == null) throw new RegraDeNegocioException("CPF é obrigatório.");
        numero = numero.replaceAll("\\D", "");
        if (numero.length() != 11) throw new RegraDeNegocioException("CPF deve ter 11 dígitos.");
    }

    @Override public int compareTo(Cpf outro) { return numero.compareTo(outro.numero); }
    @Override public String toString() { return numero.substring(0, 3) + "." + numero.substring(3, 6) + "." + numero.substring(6, 9) + "-" + numero.substring(9); }
}
