package academia.servico;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import academia.enums.EstadoMatricula;
import academia.enums.TipoPlano;
import academia.modelo.Matricula;
import academia.modelo.Mensalidade;
import academia.valor.Dinheiro;

/**
 * Desafio D: relatórios gerenciais com Streams e lambdas.
 * Só calcula e devolve dados; quem imprime é a camada app (Main).
 */
public final class RelatoriosGerenciais {
    public record Inadimplente(String nome, long diasDeAtraso, Dinheiro valorEmAberto) { }
    public record Frequencia(String nome, int checkins) { }

    private RelatoriosGerenciais() { }

    /** Relatório 1 (groupingBy + counting): quantas matrículas há em cada estado. */
    public static Map<EstadoMatricula, Long> matriculasPorEstado(List<Matricula> matriculas) {
        return matriculas.stream()
                .collect(Collectors.groupingBy(Matricula::estado, TreeMap::new, Collectors.counting()));
    }

    /** Relatório 2 (groupingBy + flatMapping + reducing): valor das mensalidades pendentes por plano atual. */
    public static Map<TipoPlano, Dinheiro> receitaPendentePorPlano(List<Matricula> matriculas) {
        return matriculas.stream()
                .collect(Collectors.groupingBy(m -> m.plano().tipo(), TreeMap::new,
                        Collectors.flatMapping(m -> m.mensalidades().stream().filter(x -> !x.estaPaga()),
                                Collectors.reducing(Dinheiro.zero(), Mensalidade::valor, Dinheiro::somar))));
    }

    /** Relatório 3 (filter + map + sorted): inadimplentes, do maior atraso para o menor. */
    public static List<Inadimplente> inadimplentes(List<Matricula> matriculas, LocalDate hoje) {
        return matriculas.stream()
                .filter(m -> m.mensalidades().stream().anyMatch(x -> x.diasDeAtraso(hoje) > 0))
                .map(m -> new Inadimplente(
                        m.aluno().nome(),
                        m.mensalidades().stream().mapToLong(x -> x.diasDeAtraso(hoje)).max().orElse(0),
                        m.mensalidades().stream().filter(x -> !x.estaPaga()).map(Mensalidade::valor)
                                .reduce(Dinheiro.zero(), Dinheiro::somar)))
                .sorted(Comparator.comparingLong(Inadimplente::diasDeAtraso).reversed()
                        .thenComparing(Inadimplente::nome))
                .toList();
    }

    /** Relatório 4 (map + sorted + limit): ranking dos alunos mais frequentes. */
    public static List<Frequencia> rankingDeFrequencia(List<Matricula> matriculas, int limite) {
        return matriculas.stream()
                .map(m -> new Frequencia(m.aluno().nome(), m.checkins().size()))
                .sorted(Comparator.comparingInt(Frequencia::checkins).reversed().thenComparing(Frequencia::nome))
                .limit(limite)
                .toList();
    }

    /** Relatório 5 (groupingBy + averagingInt): média de check-ins por plano. */
    public static Map<TipoPlano, Double> mediaDeCheckinsPorPlano(List<Matricula> matriculas) {
        return matriculas.stream()
                .collect(Collectors.groupingBy(m -> m.plano().tipo(), TreeMap::new,
                        Collectors.averagingInt(m -> m.checkins().size())));
    }
}
