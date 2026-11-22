# D2 · HEALTHTECH Academia e Planos — versão simples (Java puro)

Java 17+. Sem Spring, sem banco, sem GUI, sem bibliotecas. Organizado em pastas (pacotes) por tipo de elemento, 27 arquivos `.java`.

## 1. Glossário (R01) e onde cada regra está (R02)

| Termo | Significado | No código |
|---|---|---|
| Aluno | Pessoa com CPF; no máximo uma matrícula | `Aluno` |
| Matrícula | Vínculo aluno–plano, com estado | `Matricula`, `EstadoMatricula` |
| Plano | Mensal, Trimestral ou Anual, com preço e regras | `Plano`, `PlanoMensal/Trimestral/Anual`, `TipoPlano` |
| Mensalidade | Cobrança mensal com vencimento | `Mensalidade` |
| Check-in | Presença do dia (1 por dia) | `fazerCheckin()` |
| Congelamento | Suspensão da matrícula | `congelar()`, `descongelar()` |
| Bloqueio | Suspensão por atraso | `bloquear()`, `desbloquear()` |
| Avaliação Física | Exame válido por 90 dias | `AvaliacaoFisica` |
| Ficha de Treino | Exercícios definidos pela avaliação | `FichaTreino`, `ObjetivoTreino` |
| Upgrade | Mudança de plano no próximo mês | `solicitarMudancaDePlano()` |
| Aula Coletiva / Lista de Espera | Aula com capacidade e fila | `AulaColetiva`, `ResultadoInscricao` |

| Regra | Onde é garantida |
|---|---|
| RN01 | `Matricula.abrir()` (plano único e avaliação); `Aluno.vincular()` (uma matrícula por aluno); `SecretariaDaAcademia.matricular()`; `TipoPlano` (só 3 planos) |
| RN02 | `Plano.diasMaximosDeCongelamentoPorAno()` (abstrato) + `PlanoAnual` (30); `Plano.permiteCongelamento()`; `Matricula.congelar()` |
| RN03 | `EstadoMatricula.permiteCheckin()`; `Matricula.fazerCheckin()` |
| RN04 | `Mensalidade.diasDeAtraso()`; `Matricula.atualizarSituacao()` (bloqueia com atraso > 10 dias); `Matricula.pagarMensalidade()` (reativa) |
| RN05 | `AulaColetiva.inscrever()` (capacidade + fila `ArrayDeque`) |
| RN06 | `EstadoMatricula` (`congelar/descongelar/bloquear/desbloquear/cancelar`); `Matricula.cancelar()` |
| RN07 | `Matricula.solicitarMudancaDePlano()`, `aplicarVigenciaDoPlano()`, `gerarMensalidade()` |
| RN08 | `Matricula.abrir()`; `AvaliacaoFisica` (90 dias, `gerarFichaDeTreino()`); `Matricula.atualizarSituacao()` (congela ao vencer), `descongelar()`, `renovarAvaliacao()` |

**Interpretação (RN02 × RN08):** o congelamento a pedido é só do Anual (cota de 30 dias/ano; termina quando o aluno descongela ou a cota acaba). O congelamento por avaliação vencida vale para qualquer plano e não consome a cota.

| Req. | Atendimento |
|---|---|
| R03 | 26 tipos próprios além da `Main` |
| R04 | `Plano` abstrata (3 métodos abstratos; `valorDoContrato()` e `permiteCongelamento()` os usam) + 3 subclasses |
| R05 | `Vencivel` (`Mensalidade` e `AvaliacaoFisica`) usada em `List<Vencivel>`; `RelogioAcademia` |
| R06 | `SecretariaDaAcademia(RelogioAcademia)` |
| R07 | `EstadoMatricula` |
| R08 | `Dinheiro`, `Cpf`, `FichaTreino` (records validados) |
| R09 | Construtores validam; sem setters públicos; `List.copyOf` |
| R10 | `RegraDeNegocioException` + 5 derivadas; `Main` captura sem encerrar |
| R11 | `Aluno implements Comparable`; `Matricula.POR_NOME` e `POR_ESTADO_E_NOME` |
| R12 | `Main`: 15 opções + dados iniciais |

## 2. Estrutura de pastas (`src/academia/`)
```
src/academia/
├── app/         Main                                   → ponto de entrada (menu)
├── servico/     SecretariaDaAcademia, RelogioSimulado  → casos de uso e relógio simulado
├── modelo/      Aluno, Matricula, Mensalidade,         → classes de domínio (entidades)
│                AvaliacaoFisica, AulaColetiva
├── plano/       Plano (abstract), PlanoMensal,         → hierarquia de planos (R04)
│                PlanoTrimestral, PlanoAnual
├── enums/       TipoPlano, EstadoMatricula,            → enumerações (R07)
│                ObjetivoTreino, ResultadoInscricao
├── valor/       Dinheiro, Cpf, FichaTreino             → records imutáveis (R08)
├── interfaces/  Vencivel, RelogioAcademia              → contratos (R05, R06)
└── excecoes/    RegraDeNegocioException,               → exceções de domínio (R10)
                 MatriculaDuplicadaException,
                 TransicaoDeEstadoInvalidaException,
                 CheckinNaoPermitidoException,
                 CongelamentoNaoPermitidoException,
                 AvaliacaoFisicaVencidaException
```
Os métodos ficam dentro das classes (o Java não separa métodos em pastas); a tabela da seção 1 diz em qual classe e método cada regra mora.

## 3. Compilar e executar
```bash
mkdir out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out academia.app.Main
```
Windows (PowerShell): `javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName`. Se os acentos saírem quebrados: `chcp 65001`. IntelliJ: `src` como *Sources Root* e rodar `academia.app.Main`.

### Roteiro de testes (CPFs só com números)
Ana `52998224725` (ANUAL) · Bruno `11144477735` (MENSAL) · Carla `39053344705` (TRIMESTRAL, avaliação vence em 5 dias) · Diego `16899535009` (MENSAL)

| # | Ação | Esperado |
|---|---|---|
| 1 | `3` Ana duas vezes | 1º ok; 2º recusado (RN03) |
| 2 | `6` Bruno | Recusado: plano sem congelamento (RN02) |
| 3 | `6` Ana, `7` Ana, `6` Ana; depois `6` de novo | Congela, reativa, congela; o 4º recusado (RN06) |
| 4 | `1` com CPF de Ana | Recusado (RN01) |
| 5 | `13` aula 1 com Carla, depois Bruno | Ambos vão para a espera, nessa ordem (RN05) |
| 6 | `14` aula 1 com Diego | Carla entra no lugar (FIFO) |
| 7 | `9` Diego → MENSAL; depois → TRIMESTRAL | Recusado (igual); agendado (RN07) |
| 8 | `15` 16 dias; `2` | Bruno BLOQUEADO (RN04); Carla CONGELADA por avaliação (RN08) |
| 9 | `3` Bruno; `5` Bruno; `3` Bruno | Recusado; paga e volta a ATIVA; check-in ok |
| 10 | `7` Carla; `10` Carla; `7` Carla | Recusado; renova; reativa (RN08) |
| 11 | `4` Diego (após o passo 8) | Nova mensalidade já com preço do TRIMESTRAL (RN07) |
| 12 | `11` Diego | Ficha de treino e vencimentos (`List<Vencivel>`) |
| 13 | `8` Diego; depois `3`, `8`, `6` | Cancela; todo o resto recusado: terminal (RN06) |
| 14 | Plano inválido ("SEMESTRAL") | Mensagem de erro; menu continua (R10) |
