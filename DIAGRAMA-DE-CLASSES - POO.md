# Diagrama de Classes — Academia e Planos (D2 · HEALTHTECH)

```mermaid
classDiagram
    direction TB

    class Main {
        -Scanner entrada
        -RelogioSimulado relogio
        -SecretariaDaAcademia secretaria
        +main(String[])$ void
    }

    class RelogioAcademia {
        <<interface>>
        +hoje() LocalDate
    }

    class Vencivel {
        <<interface>>
        +dataDeVencimento() LocalDate
        +descricao() String
        +estaVencido(LocalDate) boolean
    }

    class SecretariaDaAcademia {
        -RelogioAcademia relogio
        -Map~Cpf, Matricula~ matriculas
        -List~AulaColetiva~ aulas
        +matricular(String, String, TipoPlano, AvaliacaoFisica) Matricula
        +localizar(String) Matricula
        +listar(Comparator) List~Matricula~
        +fazerCheckin(String) void
        +pagarMensalidade(String) Mensalidade
        +congelar(String) void
        +cancelar(String) void
        +solicitarMudancaDePlano(String, TipoPlano) void
        +renovarAvaliacao(String, AvaliacaoFisica) void
        +inscreverEmAula(int, String) ResultadoInscricao
    }

    class RelogioSimulado {
        -LocalDate hoje
        +avancarDias(int) void
        +hoje() LocalDate
    }

    class Aluno {
        -String nome
        -Cpf cpf
        -Matricula matricula
        +nome() String
        +cpf() Cpf
        +matricula() Optional~Matricula~
        +compareTo(Aluno) int
    }

    class Matricula {
        +int LIMITE_DE_ATRASO_EM_DIAS$
        +Comparator POR_NOME$
        +Comparator POR_ESTADO_E_NOME$
        -int numero
        -LocalDate inicio
        -Plano plano
        -Plano planoAgendado
        -AvaliacaoFisica avaliacao
        -EstadoMatricula estado
        -List~Mensalidade~ mensalidades
        -List~LocalDate~ checkins
        +abrir(int, Aluno, TipoPlano, AvaliacaoFisica, LocalDate)$ Matricula
        +fichaDeTreino() FichaTreino
        +vencimentosEmAberto() List~Vencivel~
        +fazerCheckin(LocalDate) void
        +gerarMensalidade(LocalDate, LocalDate) Mensalidade
        +pagarMensalidade(LocalDate) Mensalidade
        +congelar(LocalDate) void
        +descongelar(LocalDate) void
        +solicitarMudancaDePlano(TipoPlano, LocalDate) void
        +renovarAvaliacao(AvaliacaoFisica, LocalDate) void
        +cancelar() void
        +atualizarSituacao(LocalDate) void
    }

    class Mensalidade {
        -YearMonth competencia
        -Dinheiro valor
        -LocalDate vencimento
        -boolean paga
        +estaPaga() boolean
        +diasDeAtraso(LocalDate) long
        +pagar() void
        +dataDeVencimento() LocalDate
        +estaVencido(LocalDate) boolean
        +descricao() String
    }

    class AvaliacaoFisica {
        +int VALIDADE_EM_DIAS$
        -LocalDate realizadaEm
        -double pesoKg
        -double alturaMetros
        -ObjetivoTreino objetivo
        +gerarFichaDeTreino() FichaTreino
        +dataDeVencimento() LocalDate
        +descricao() String
    }

    class AulaColetiva {
        -int numero
        -String nome
        -int capacidadeMaxima
        -List~Matricula~ inscritos
        -Deque~Matricula~ listaDeEspera
        +inscrever(Matricula) ResultadoInscricao
        +cancelarInscricao(Matricula) Optional~Matricula~
    }

    class Plano {
        <<abstract>>
        -TipoPlano tipo
        +tipo() TipoPlano
        +precoMensal()* Dinheiro
        +mesesDeContrato()* int
        +diasMaximosDeCongelamentoPorAno()* int
        +valorDoContrato() Dinheiro
        +permiteCongelamento() boolean
    }
    class PlanoMensal {
        +precoMensal() Dinheiro
        +mesesDeContrato() int
        +diasMaximosDeCongelamentoPorAno() int
    }
    class PlanoTrimestral {
        +precoMensal() Dinheiro
        +mesesDeContrato() int
        +diasMaximosDeCongelamentoPorAno() int
    }
    class PlanoAnual {
        +precoMensal() Dinheiro
        +mesesDeContrato() int
        +diasMaximosDeCongelamentoPorAno() int
    }

    class TipoPlano {
        <<enumeration>>
        MENSAL
        TRIMESTRAL
        ANUAL
        +criarPlano() Plano
    }
    class EstadoMatricula {
        <<enumeration>>
        ATIVA
        CONGELADA
        BLOQUEADA
        CANCELADA
        +permiteCheckin() boolean
        +congelar() EstadoMatricula
        +descongelar() EstadoMatricula
        +bloquear() EstadoMatricula
        +desbloquear() EstadoMatricula
        +cancelar() EstadoMatricula
    }
    class ObjetivoTreino {
        <<enumeration>>
        EMAGRECIMENTO
        HIPERTROFIA
        CONDICIONAMENTO
        +exercicios() List~String~
    }
    class ResultadoInscricao {
        <<enumeration>>
        CONFIRMADA
        EM_LISTA_DE_ESPERA
    }

    class Cpf {
        <<record>>
        String numero
        +compareTo(Cpf) int
    }
    class Dinheiro {
        <<record>>
        BigDecimal valor
        +reais(String)$ Dinheiro
        +multiplicar(int) Dinheiro
    }
    class FichaTreino {
        <<record>>
        ObjetivoTreino objetivo
        List~String~ exercicios
    }

    %% Herança (extends)
    Plano <|-- PlanoMensal
    Plano <|-- PlanoTrimestral
    Plano <|-- PlanoAnual

    %% Realização (implements)
    RelogioAcademia <|.. RelogioSimulado
    Vencivel <|.. Mensalidade
    Vencivel <|.. AvaliacaoFisica

    %% Composição (tem-um)
    Matricula "1" *-- "0..*" Mensalidade
    Matricula "1" *-- "1" Plano : plano atual
    Matricula "1" *-- "1" AvaliacaoFisica

    %% Agregação / associação / dependência
    SecretariaDaAcademia "1" o-- "0..*" Matricula
    SecretariaDaAcademia "1" o-- "0..*" AulaColetiva
    AulaColetiva "1" o-- "0..*" Matricula : inscritos / espera
    Aluno "1" -- "0..1" Matricula
    Aluno --> Cpf
    Matricula --> EstadoMatricula
    Matricula ..> TipoPlano
    Matricula ..> FichaTreino
    Mensalidade --> Dinheiro
    AvaliacaoFisica --> ObjetivoTreino
    AvaliacaoFisica ..> FichaTreino
    FichaTreino --> ObjetivoTreino
    Plano --> TipoPlano
    Plano ..> Dinheiro
    TipoPlano ..> Plano : cria
    AulaColetiva ..> ResultadoInscricao
    SecretariaDaAcademia --> RelogioAcademia
    Main --> SecretariaDaAcademia
    Main --> RelogioSimulado
```

## Hierarquia de exceções

```mermaid
classDiagram
    direction TB
    class RuntimeException {
        <<java.lang>>
    }
    class RegraDeNegocioException
    class MatriculaDuplicadaException
    class CongelamentoNaoPermitidoException
    class CheckinNaoPermitidoException
    class TransicaoDeEstadoInvalidaException
    class AvaliacaoFisicaVencidaException

    RuntimeException <|-- RegraDeNegocioException
    RegraDeNegocioException <|-- MatriculaDuplicadaException
    RegraDeNegocioException <|-- CongelamentoNaoPermitidoException
    RegraDeNegocioException <|-- CheckinNaoPermitidoException
    RegraDeNegocioException <|-- TransicaoDeEstadoInvalidaException
    RegraDeNegocioException <|-- AvaliacaoFisicaVencidaException
```
