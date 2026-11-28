package academia.modelo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import academia.enums.ResultadoInscricao;
import academia.excecoes.RegraDeNegocioException;

/** RN05: capacidade máxima + lista de espera FIFO (ordem de inscrição). */
public final class AulaColetiva {
    private final int numero;
    private final String nome;
    private final int capacidadeMaxima;
    private final List<Matricula> inscritos = new ArrayList<>();
    private final Deque<Matricula> listaDeEspera = new ArrayDeque<>();

    public AulaColetiva(int numero, String nome, int capacidadeMaxima) {
        if (nome == null || nome.isBlank() || capacidadeMaxima < 1) throw new RegraDeNegocioException("Aula inválida.");
        this.numero = numero;
        this.nome = nome.trim();
        this.capacidadeMaxima = capacidadeMaxima;
    }

    public int numero() { return numero; }
    public List<Matricula> inscritos() { return List.copyOf(inscritos); }          // R09
    public List<Matricula> listaDeEspera() { return List.copyOf(listaDeEspera); }  // R09

    /** RN05: lotou, vai para o fim da fila. */
    public ResultadoInscricao inscrever(Matricula matricula) {
        if (inscritos.contains(matricula) || listaDeEspera.contains(matricula))
            throw new RegraDeNegocioException("Aluno já inscrito (ou na espera) nesta aula.");
        if (inscritos.size() < capacidadeMaxima) {
            inscritos.add(matricula);
            return ResultadoInscricao.CONFIRMADA;
        }
        listaDeEspera.addLast(matricula);
        return ResultadoInscricao.EM_LISTA_DE_ESPERA;
    }

    /** Ao liberar uma vaga, o primeiro da fila entra (ordem de inscrição). */
    public Optional<Matricula> cancelarInscricao(Matricula matricula) {
        if (listaDeEspera.remove(matricula)) return Optional.empty();
        if (!inscritos.remove(matricula)) throw new RegraDeNegocioException("Aluno não está inscrito nesta aula.");
        Matricula proximo = listaDeEspera.pollFirst();
        if (proximo != null) inscritos.add(proximo);
        return Optional.ofNullable(proximo);
    }

    @Override public String toString() {
        return String.format("#%d %s | vagas %d/%d | espera: %d", numero, nome, inscritos.size(), capacidadeMaxima, listaDeEspera.size());
    }
}
