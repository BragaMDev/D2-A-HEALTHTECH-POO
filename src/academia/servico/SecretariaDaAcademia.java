package academia.servico;

import java.time.LocalDate;
import java.util.*;

import academia.desconto.PoliticaDeDesconto;
import academia.desconto.SemDesconto;
import academia.enums.ResultadoInscricao;
import academia.enums.TipoPlano;
import academia.excecoes.MatriculaDuplicadaException;
import academia.excecoes.RegraDeNegocioException;
import academia.interfaces.RelogioAcademia;
import academia.modelo.Aluno;
import academia.modelo.AulaColetiva;
import academia.modelo.AvaliacaoFisica;
import academia.modelo.Matricula;
import academia.modelo.Mensalidade;
import academia.valor.Cpf;

/**
 * Casos de uso. R06: recebe pelo construtor uma INTERFACE (RelogioAcademia).
 * Antes de cada operação sincroniza a matrícula com a data atual.
 */
public final class SecretariaDaAcademia {
    private final RelogioAcademia relogio;
    private final Map<Cpf, Matricula> matriculas = new LinkedHashMap<>();
    private final List<AulaColetiva> aulas = new ArrayList<>();
    private int proximaMatricula = 1;
    private PoliticaDeDesconto politica = new SemDesconto(); // Desafio C: estratégia atual

    public SecretariaDaAcademia(RelogioAcademia relogio) {
        if (relogio == null) throw new RegraDeNegocioException("Secretaria exige um relógio.");
        this.relogio = relogio;
    }

    /** Desafio C: troca a regra de desconto em tempo de execução; vale para as próximas mensalidades. */
    public void trocarPoliticaDeDesconto(PoliticaDeDesconto nova) {
        if (nova == null) throw new RegraDeNegocioException("Política de desconto é obrigatória.");
        this.politica = nova;
    }

    public PoliticaDeDesconto politicaDeDesconto() { return politica; }

    /** RN01 + RN08: cria aluno, matrícula e a primeira mensalidade (vence em 5 dias). */
    public Matricula matricular(String nome, String cpfTexto, TipoPlano tipo, AvaliacaoFisica avaliacao) {
        Cpf cpf = new Cpf(cpfTexto);
        if (matriculas.containsKey(cpf)) throw new MatriculaDuplicadaException("RN01: já existe matrícula para o CPF " + cpf + ".");
        Matricula m = Matricula.abrir(proximaMatricula++, new Aluno(nome, cpf), tipo, avaliacao, relogio.hoje());
        m.gerarMensalidade(relogio.hoje().plusDays(5), relogio.hoje(), politica);
        matriculas.put(cpf, m);
        return m;
    }

    public Matricula localizar(String cpfTexto) {
        Matricula m = matriculas.get(new Cpf(cpfTexto));
        if (m == null) throw new RegraDeNegocioException("Nenhuma matrícula para o CPF " + cpfTexto + ".");
        m.atualizarSituacao(relogio.hoje());
        return m;
    }

    public List<Matricula> listar(Comparator<Matricula> ordem) {
        matriculas.values().forEach(m -> m.atualizarSituacao(relogio.hoje()));
        List<Matricula> lista = new ArrayList<>(matriculas.values());
        lista.sort(ordem);
        return List.copyOf(lista);
    }

    public void fazerCheckin(String cpf) { localizar(cpf).fazerCheckin(relogio.hoje()); }
    public Mensalidade gerarMensalidade(String cpf) { LocalDate h = relogio.hoje(); return localizar(cpf).gerarMensalidade(h.plusDays(5), h, politica); }
    public Mensalidade pagarMensalidade(String cpf) { return localizar(cpf).pagarMensalidade(relogio.hoje()); }
    public void congelar(String cpf) { localizar(cpf).congelar(relogio.hoje()); }
    public void descongelar(String cpf) { localizar(cpf).descongelar(relogio.hoje()); }
    public void cancelar(String cpf) { localizar(cpf).cancelar(); }
    public void solicitarMudancaDePlano(String cpf, TipoPlano novo) { localizar(cpf).solicitarMudancaDePlano(novo, relogio.hoje()); }
    public void renovarAvaliacao(String cpf, AvaliacaoFisica nova) { localizar(cpf).renovarAvaliacao(nova, relogio.hoje()); }

    // ---- aulas coletivas (RN05) ----
    public void cadastrarAula(String nome, int capacidade) { aulas.add(new AulaColetiva(aulas.size() + 1, nome, capacidade)); }
    public List<AulaColetiva> aulas() { return List.copyOf(aulas); }

    private AulaColetiva aula(int numero) {
        return aulas.stream().filter(a -> a.numero() == numero).findFirst()
                .orElseThrow(() -> new RegraDeNegocioException("Aula #" + numero + " não existe."));
    }

    public ResultadoInscricao inscreverEmAula(int numero, String cpf) { return aula(numero).inscrever(localizar(cpf)); }
    public Optional<Matricula> cancelarInscricao(int numero, String cpf) { return aula(numero).cancelarInscricao(localizar(cpf)); }
}
