package academia.app;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Scanner;

import academia.enums.ObjetivoTreino;
import academia.enums.TipoPlano;
import academia.excecoes.RegraDeNegocioException;
import academia.interfaces.Vencivel;
import academia.modelo.AvaliacaoFisica;
import academia.modelo.Matricula;
import academia.servico.RelogioSimulado;
import academia.servico.SecretariaDaAcademia;
import academia.valor.FichaTreino;

/** R12: apenas orquestra o menu; as regras vivem no domínio. */
public final class Main {
    private final Scanner entrada = new Scanner(System.in);
    private final RelogioSimulado relogio = new RelogioSimulado();
    private final SecretariaDaAcademia secretaria = new SecretariaDaAcademia(relogio);

    public static void main(String[] args) {
        Main app = new Main();
        app.carregarDadosIniciais();
        app.executar();
    }

    private void carregarDadosIniciais() {
        LocalDate hoje = relogio.hoje();
        secretaria.matricular("Ana Souza", "52998224725", TipoPlano.ANUAL,
                new AvaliacaoFisica(hoje.minusDays(10), 62, 1.65, ObjetivoTreino.CONDICIONAMENTO));
        secretaria.matricular("Bruno Lima", "11144477735", TipoPlano.MENSAL,
                new AvaliacaoFisica(hoje.minusDays(5), 95, 1.78, ObjetivoTreino.EMAGRECIMENTO));
        secretaria.matricular("Carla Mendes", "39053344705", TipoPlano.TRIMESTRAL,
                new AvaliacaoFisica(hoje.minusDays(85), 58, 1.60, ObjetivoTreino.HIPERTROFIA)); // vence em 5 dias
        secretaria.matricular("Diego Ramos", "16899535009", TipoPlano.MENSAL,
                new AvaliacaoFisica(hoje.minusDays(1), 80, 1.80, ObjetivoTreino.HIPERTROFIA));
        secretaria.pagarMensalidade("39053344705"); // Carla e Diego em dia
        secretaria.pagarMensalidade("16899535009");
        secretaria.cadastrarAula("Spinning", 2);
        secretaria.cadastrarAula("Yoga", 3);
        secretaria.inscreverEmAula(1, "52998224725"); // Spinning lotado: Ana e Diego
        secretaria.inscreverEmAula(1, "16899535009");
        System.out.println("Dados de exemplo: Ana 52998224725 | Bruno 11144477735 | Carla 39053344705 | Diego 16899535009");
    }

    private void executar() {
        while (true) {
            System.out.print("""

                    ===== HEALTHTECH ACADEMIA =====
                     1 - Matricular aluno         9 - Mudar de plano (upgrade)
                     2 - Listar matrículas       10 - Renovar avaliação física
                     3 - Check-in                11 - Ficha de treino e vencimentos
                     4 - Gerar mensalidade       12 - Listar aulas coletivas
                     5 - Pagar mensalidade       13 - Inscrever em aula
                     6 - Congelar                14 - Cancelar inscrição em aula
                     7 - Descongelar             15 - Avançar data simulada
                     8 - Cancelar matrícula       0 - Sair
                    Opção: """);
            if (!entrada.hasNextLine()) return;
            try {
                switch (entrada.nextLine().trim()) {
                    case "0" -> { System.out.println("Até logo!"); return; }
                    case "1" -> matricular();
                    case "2" -> listar();
                    case "3" -> { secretaria.fazerCheckin(ler("CPF")); System.out.println("✔ Check-in feito."); }
                    case "4" -> System.out.println("✔ Gerada: " + secretaria.gerarMensalidade(ler("CPF")));
                    case "5" -> System.out.println("✔ Paga: " + secretaria.pagarMensalidade(ler("CPF")));
                    case "6" -> { secretaria.congelar(ler("CPF")); System.out.println("✔ Matrícula congelada."); }
                    case "7" -> { secretaria.descongelar(ler("CPF")); System.out.println("✔ Matrícula reativada."); }
                    case "8" -> { secretaria.cancelar(ler("CPF")); System.out.println("✔ Matrícula cancelada (definitivo)."); }
                    case "9" -> { secretaria.solicitarMudancaDePlano(ler("CPF"), plano()); System.out.println("✔ Mudança agendada para o próximo mês."); }
                    case "10" -> { secretaria.renovarAvaliacao(ler("CPF"), novaAvaliacao()); System.out.println("✔ Avaliação renovada."); }
                    case "11" -> fichaEVencimentos();
                    case "12" -> secretaria.aulas().forEach(System.out::println);
                    case "13" -> System.out.println("✔ " + secretaria.inscreverEmAula(inteiro("Nº da aula"), ler("CPF")));
                    case "14" -> {
                        var promovido = secretaria.cancelarInscricao(inteiro("Nº da aula"), ler("CPF"));
                        System.out.println("✔ Inscrição cancelada." + promovido.map(m -> " Entrou da espera: " + m.aluno().nome()).orElse(""));
                    }
                    case "15" -> { relogio.avancarDias(inteiro("Dias a avançar")); System.out.println("✔ Data simulada: " + relogio.hoje()); }
                    default -> System.out.println("Opção inválida.");
                }
            } catch (RegraDeNegocioException e) {                      // R10: erro de domínio não derruba o programa
                System.out.println("✖ " + e.getMessage());
            } catch (IllegalArgumentException e) {                     // número/enum digitado errado
                System.out.println("✖ Entrada inválida: " + e.getMessage());
            }
        }
    }

    private void matricular() {
        String nome = ler("Nome");
        String cpf = ler("CPF");
        TipoPlano tipo = plano();
        System.out.println("✔ " + secretaria.matricular(nome, cpf, tipo, novaAvaliacao()));
    }

    private void listar() {
        Comparator<Matricula> ordem = ler("Ordem: 1=nome  2=estado e nome").equals("2") ? Matricula.POR_ESTADO_E_NOME : Matricula.POR_NOME;
        secretaria.listar(ordem).forEach(System.out::println);
    }

    private void fichaEVencimentos() {
        Matricula m = secretaria.localizar(ler("CPF"));
        FichaTreino ficha = m.fichaDeTreino();
        System.out.println("Ficha (" + ficha.objetivo() + "): " + ficha.exercicios());
        for (Vencivel v : m.vencimentosEmAberto()) // R05: List<Vencivel> com tipos sem herança em comum
            System.out.println("  " + v.dataDeVencimento() + " - " + v.descricao() + (v.estaVencido(relogio.hoje()) ? " [VENCIDO]" : " [em dia]"));
    }

    private TipoPlano plano() { return TipoPlano.valueOf(ler("Plano (MENSAL/TRIMESTRAL/ANUAL)").toUpperCase()); }

    private AvaliacaoFisica novaAvaliacao() {
        ObjetivoTreino objetivo = ObjetivoTreino.valueOf(ler("Objetivo (EMAGRECIMENTO/HIPERTROFIA/CONDICIONAMENTO)").toUpperCase());
        return new AvaliacaoFisica(relogio.hoje(), decimal("Peso (kg)"), decimal("Altura (m)"), objetivo);
    }

    private String ler(String rotulo) { System.out.print(rotulo + ": "); return entrada.nextLine().trim(); }
    private int inteiro(String rotulo) { return Integer.parseInt(ler(rotulo)); }
    private double decimal(String rotulo) { return Double.parseDouble(ler(rotulo).replace(',', '.')); }
}
