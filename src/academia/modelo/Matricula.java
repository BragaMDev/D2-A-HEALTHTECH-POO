package academia.modelo;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

import academia.desconto.PoliticaDeDesconto;
import academia.desconto.SemDesconto;
import academia.enums.EstadoMatricula;
import academia.enums.TipoPlano;
import academia.excecoes.AvaliacaoFisicaVencidaException;
import academia.excecoes.CheckinNaoPermitidoException;
import academia.excecoes.CongelamentoNaoPermitidoException;
import academia.excecoes.RegraDeNegocioException;
import academia.excecoes.TransicaoDeEstadoInvalidaException;
import academia.interfaces.Vencivel;
import academia.plano.Plano;
import academia.valor.FichaTreino;

/** Agregado central: plano, estado, check-in, mensalidades, congelamento, upgrade e avaliação física. */
public final class Matricula {
    public static final int LIMITE_DE_ATRASO_EM_DIAS = 10; // RN04

    // R11: ordens alternativas para listagens
    public static final Comparator<Matricula> POR_NOME = Comparator.comparing(Matricula::aluno); // usa Comparable<Aluno>
    public static final Comparator<Matricula> POR_ESTADO_E_NOME = Comparator.comparing(Matricula::estado).thenComparing(POR_NOME);

    private final int numero;
    private final Aluno aluno;
    private final LocalDate inicio;
    private Plano plano;                         // RN01: um único plano
    private Plano planoAgendado;                 // RN07
    private YearMonth vigenciaDoPlanoAgendado;   // RN07
    private AvaliacaoFisica avaliacao;           // RN08
    private EstadoMatricula estado = EstadoMatricula.ATIVA;
    private boolean congeladaPorAvaliacao;       // RN08 (administrativo) x RN02 (pedido do aluno)
    private LocalDate congeladaDesde;
    private LocalDate congeladaAte;
    private final Map<Integer, Integer> diasCongeladosPorAno = new HashMap<>();
    private final List<Mensalidade> mensalidades = new ArrayList<>();
    private final List<LocalDate> checkins = new ArrayList<>();

    private Matricula(int numero, Aluno aluno, Plano plano, AvaliacaoFisica avaliacao, LocalDate inicio) {
        this.numero = numero;
        this.aluno = aluno;
        this.plano = plano;
        this.avaliacao = avaliacao;
        this.inicio = inicio;
    }

    /** RN01 + RN08 + R09: única porta de criação; nasce completa e válida. */
    public static Matricula abrir(int numero, Aluno aluno, TipoPlano tipo, AvaliacaoFisica avaliacao, LocalDate hoje) {
        if (aluno == null || tipo == null || hoje == null) throw new RegraDeNegocioException("Matrícula exige aluno, plano e data.");
        if (avaliacao == null) throw new AvaliacaoFisicaVencidaException("RN08: é preciso ter avaliação física para se matricular.");
        if (avaliacao.estaVencido(hoje)) throw new AvaliacaoFisicaVencidaException("RN08: avaliação física vencida.");
        Matricula m = new Matricula(numero, aluno, tipo.criarPlano(), avaliacao, hoje);
        aluno.vincular(m); // RN01: recusa segunda matrícula
        return m;
    }

    // ---------- consultas (sem setters públicos, R09) ----------
    public int numero() { return numero; }
    public Aluno aluno() { return aluno; }
    public EstadoMatricula estado() { return estado; }
    public Plano plano() { return plano; }
    public AvaliacaoFisica avaliacao() { return avaliacao; }
    public FichaTreino fichaDeTreino() { return avaliacao.gerarFichaDeTreino(); } // RN08
    public List<Mensalidade> mensalidades() { return List.copyOf(mensalidades); }
    public List<LocalDate> checkins() { return List.copyOf(checkins); }
    public boolean temMensalidadePendente() { return mensalidades.stream().anyMatch(m -> !m.estaPaga()); }

    /** R05: lista polimórfica de tudo que vence (mensalidades pendentes + avaliação), em ordem de data. */
    public List<Vencivel> vencimentosEmAberto() {
        List<Vencivel> lista = new ArrayList<>();
        mensalidades.stream().filter(m -> !m.estaPaga()).forEach(lista::add);
        lista.add(avaliacao);
        lista.sort(Comparator.comparing(Vencivel::dataDeVencimento));
        return List.copyOf(lista);
    }

    // ---------- RN03: check-in ----------
    public void fazerCheckin(LocalDate hoje) {
        if (!estado.permiteCheckin())
            throw new CheckinNaoPermitidoException("RN03: check-in exige matrícula ATIVA (estado: " + estado + ").");
        if (checkins.contains(hoje))
            throw new CheckinNaoPermitidoException("RN03: já há check-in em " + hoje + " (máximo 1 por dia).");
        checkins.add(hoje);
    }

    // ---------- RN04: mensalidade, atraso e bloqueio ----------
    public Mensalidade gerarMensalidade(LocalDate vencimento, LocalDate hoje) {
        return gerarMensalidade(vencimento, hoje, new SemDesconto());
    }

    /** Desafio C: o valor cobrado é decidido pela política (Strategy) recebida. */
    public Mensalidade gerarMensalidade(LocalDate vencimento, LocalDate hoje, PoliticaDeDesconto politica) {
        if (politica == null) throw new RegraDeNegocioException("Política de desconto é obrigatória.");
        if (estado == EstadoMatricula.CANCELADA) throw new RegraDeNegocioException("Matrícula CANCELADA não gera mensalidades.");
        aplicarVigenciaDoPlano(hoje); // RN07: preço do plano vigente
        YearMonth competencia = mensalidades.stream().map(Mensalidade::competencia).max(Comparator.naturalOrder())
                .map(c -> c.plusMonths(1)).orElse(YearMonth.from(inicio));
        Mensalidade nova = new Mensalidade(competencia, politica.aplicar(this, plano.precoMensal()), vencimento);
        mensalidades.add(nova);
        return nova;
    }

    private boolean temAtrasoAcimaDoLimite(LocalDate hoje) {
        return mensalidades.stream().anyMatch(m -> m.diasDeAtraso(hoje) > LIMITE_DE_ATRASO_EM_DIAS);
    }

    /** Paga a mais antiga pendente; se estava BLOQUEADA e o atraso foi resolvido, volta a ATIVA (RN04/RN06). */
    public Mensalidade pagarMensalidade(LocalDate hoje) {
        Mensalidade pendente = mensalidades.stream().filter(m -> !m.estaPaga())
                .min(Comparator.comparing(Mensalidade::competencia))
                .orElseThrow(() -> new RegraDeNegocioException("Não há mensalidade pendente."));
        pendente.pagar();
        if (estado == EstadoMatricula.BLOQUEADA && !temAtrasoAcimaDoLimite(hoje)) estado = estado.desbloquear();
        atualizarSituacao(hoje); // RN08 continua valendo
        return pendente;
    }

    // ---------- RN02 / RN06: congelamento ----------
    /** Congela até o aluno descongelar ou até esgotar a cota anual do plano (30 dias no Anual). */
    public void congelar(LocalDate hoje) {
        EstadoMatricula novo = estado.congelar(); // RN06: bloqueada/cancelada/já congelada são recusadas
        if (!plano.permiteCongelamento())
            throw new CongelamentoNaoPermitidoException("RN02: o plano " + plano.tipo() + " não permite congelamento.");
        int restante = plano.diasMaximosDeCongelamentoPorAno() - diasCongeladosPorAno.getOrDefault(hoje.getYear(), 0);
        if (restante <= 0)
            throw new CongelamentoNaoPermitidoException("RN02: a cota de " + plano.diasMaximosDeCongelamentoPorAno() + " dias de " + hoje.getYear() + " acabou.");
        estado = novo;
        congeladaPorAvaliacao = false;
        congeladaDesde = hoje;
        congeladaAte = hoje.plusDays(restante);
    }

    /** RN06/RN08: reativa; exige avaliação física em dia. */
    public void descongelar(LocalDate hoje) {
        estado.descongelar(); // valida a transição
        if (avaliacao.estaVencido(hoje))
            throw new AvaliacaoFisicaVencidaException("RN08: renove a avaliação física antes de descongelar.");
        if (congeladaPorAvaliacao) { estado = estado.descongelar(); congeladaPorAvaliacao = false; }
        else encerrarCongelamentoVoluntario(hoje);
    }

    private void encerrarCongelamentoVoluntario(LocalDate fim) {
        long usados = Math.max(0, Math.min(ChronoUnit.DAYS.between(congeladaDesde, fim), ChronoUnit.DAYS.between(congeladaDesde, congeladaAte)));
        diasCongeladosPorAno.merge(congeladaDesde.getYear(), (int) usados, Integer::sum);
        estado = estado.descongelar();
        congeladaDesde = null;
        congeladaAte = null;
    }

    // ---------- RN08: renovação ----------
    public void renovarAvaliacao(AvaliacaoFisica nova, LocalDate hoje) {
        if (estado == EstadoMatricula.CANCELADA) throw new TransicaoDeEstadoInvalidaException("RN06: matrícula CANCELADA é terminal.");
        if (nova == null || !nova.realizadaEm().isAfter(avaliacao.realizadaEm()) || nova.estaVencido(hoje))
            throw new AvaliacaoFisicaVencidaException("RN08: a nova avaliação deve ser mais recente que a atual e estar em dia.");
        avaliacao = nova; // a ficha passa a ser definida pela nova avaliação
    }

    // ---------- RN07: upgrade ----------
    public void solicitarMudancaDePlano(TipoPlano novoTipo, LocalDate hoje) {
        if (estado != EstadoMatricula.ATIVA)
            throw new RegraDeNegocioException("RN07: mudança de plano exige matrícula ATIVA (estado: " + estado + ").");
        if (temMensalidadePendente())
            throw new RegraDeNegocioException("RN07: há mensalidade pendente; quite antes de mudar de plano.");
        if (novoTipo == null || novoTipo == plano.tipo())
            throw new RegraDeNegocioException("RN07: escolha um plano diferente do atual.");
        planoAgendado = novoTipo.criarPlano();
        vigenciaDoPlanoAgendado = YearMonth.from(hoje).plusMonths(1); // só no próximo mês
    }

    private void aplicarVigenciaDoPlano(LocalDate hoje) {
        if (planoAgendado != null && !YearMonth.from(hoje).isBefore(vigenciaDoPlanoAgendado)) {
            plano = planoAgendado;
            planoAgendado = null;
            vigenciaDoPlanoAgendado = null;
        }
    }

    // ---------- RN06: cancelamento ----------
    public void cancelar() {
        estado = estado.cancelar();
        planoAgendado = null;
        vigenciaDoPlanoAgendado = null;
    }

    /** Aplica as regras que dependem do tempo: RN07 (vigência), RN02 (fim da cota), RN04 (bloqueio), RN08 (avaliação). */
    public void atualizarSituacao(LocalDate hoje) {
        aplicarVigenciaDoPlano(hoje);
        if (estado == EstadoMatricula.CONGELADA && !congeladaPorAvaliacao && !hoje.isBefore(congeladaAte))
            encerrarCongelamentoVoluntario(congeladaAte); // cota acabou: reativa sozinha
        if (estado == EstadoMatricula.ATIVA && temAtrasoAcimaDoLimite(hoje)) estado = estado.bloquear(); // RN04
        if (estado == EstadoMatricula.ATIVA && avaliacao.estaVencido(hoje)) {                           // RN08
            estado = estado.congelar();
            congeladaPorAvaliacao = true;
        }
    }

    @Override public String toString() {
        long pendentes = mensalidades.stream().filter(m -> !m.estaPaga()).count();
        return String.format("MAT-%03d | %-14s | %s | %-10s | %-9s%s | check-ins: %d | pendentes: %d%s",
                numero, aluno.nome(), aluno.cpf(), plano.tipo(), estado, congeladaPorAvaliacao ? " (avaliação vencida)" : "",
                checkins.size(), pendentes,
                planoAgendado == null ? "" : " | muda p/ " + planoAgendado.tipo() + " em " + vigenciaDoPlanoAgendado);
    }
}
