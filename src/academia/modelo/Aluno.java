package academia.modelo;

import java.util.Optional;

import academia.excecoes.MatriculaDuplicadaException;
import academia.excecoes.RegraDeNegocioException;
import academia.valor.Cpf;

/** Aluno. R11: ordem natural por nome. RN01: no máximo uma matrícula. */
public final class Aluno implements Comparable<Aluno> {
    private final String nome;
    private final Cpf cpf;
    private Matricula matricula;

    public Aluno(String nome, Cpf cpf) {
        if (nome == null || nome.isBlank() || cpf == null) throw new RegraDeNegocioException("Aluno exige nome e CPF.");
        this.nome = nome.trim();
        this.cpf = cpf;
    }

    public String nome() { return nome; }
    public Cpf cpf() { return cpf; }
    public Optional<Matricula> matricula() { return Optional.ofNullable(matricula); }

    /** RN01: sem setter público; só Matricula.abrir() vincula. */
    void vincular(Matricula nova) {
        if (matricula != null) throw new MatriculaDuplicadaException("RN01: " + nome + " já possui matrícula.");
        matricula = nova;
    }

    @Override public int compareTo(Aluno outro) {
        int porNome = nome.compareToIgnoreCase(outro.nome);
        return porNome != 0 ? porNome : cpf.compareTo(outro.cpf);
    }

    @Override public String toString() { return nome + " (" + cpf + ")"; }
}
