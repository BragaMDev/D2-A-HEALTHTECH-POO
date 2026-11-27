## Matriz de rastreabilidade

| Regra | Descrição | Onde é garantida | Exceção | Teste |
|---|---|---|---|---|
| RN01 | Cada aluno tem no máximo uma matrícula, e só existem os planos Mensal, Trimestral e Anual | `Matricula.abrir()`<br>`Aluno.vincular()`<br>`SecretariaDaAcademia.matricular()`<br>`TipoPlano` | `MatriculaDuplicadaException` | `MatriculaTest.segundaMatriculaDoMesmoAlunoLanca` |
| RN02 | Só o plano Anual congela a pedido, com cota de 30 dias por ano | `Plano.permiteCongelamento()`<br>`PlanoAnual`<br>`Matricula.congelar()` | `CongelamentoNaoPermitidoException` | `MatriculaTest.planoMensalNaoCongela`<br>`MatriculaTest.cotaDeCongelamentoEsgotada` |
| RN03 | Check-in só com matrícula ATIVA e no máximo 1 por dia | `EstadoMatricula.permiteCheckin()`<br>`Matricula.fazerCheckin()` | `CheckinNaoPermitidoException` | `MatriculaTest.doisCheckinsNoMesmoDiaLancam`<br>`MatriculaTest.checkinComMatriculaCongeladaLanca`<br>`EstadoMatriculaTest.somenteAtivaPermiteCheckin` |
| RN04 | Atraso maior que 10 dias bloqueia a matrícula, e pagar o atraso a reativa | `Mensalidade.diasDeAtraso()`<br>`Matricula.atualizarSituacao()`<br>`Matricula.pagarMensalidade()` | Bloqueio automático, sem exceção própria. O check-in bloqueado lança `CheckinNaoPermitidoException` | `MatriculaTest.bloqueioSoComAtrasoMaiorQue10Dias`<br>`MatriculaTest.pagamentoDesbloqueia` |
| RN05 | Aula coletiva tem capacidade máxima e lista de espera na ordem de inscrição (FIFO) | `AulaColetiva.inscrever()`<br>`AulaColetiva.cancelarInscricao()` | `RegraDeNegocioException` | `AulaColetivaTest.listaDeEsperaSegueOrdemDeInscricao`<br>`AulaColetivaTest.inscricaoDuplicadaLanca` |
| RN06 | O estado só muda por transições válidas, e CANCELADA é terminal | `EstadoMatricula` (`congelar`, `descongelar`, `bloquear`, `desbloquear`, `cancelar`)<br>`Matricula.cancelar()` | `TransicaoDeEstadoInvalidaException` | `EstadoMatriculaTest.transicoesValidas`<br>`EstadoMatriculaTest.transicoesInvalidasLancamExcecaoDeDominio`<br>`MatriculaTest.canceladaEhTerminal` |
| RN07* | Mudança de plano só vale no próximo mês, e exige matrícula ATIVA, sem mensalidade pendente e com plano diferente do atual | `Matricula.solicitarMudancaDePlano()`<br>`Matricula.aplicarVigenciaDoPlano()`<br>`Matricula.gerarMensalidade()` | `RegraDeNegocioException` | `MatriculaTest.upgradeValeSoNoProximoMes` |
| RN08* | A avaliação física vale 90 dias. Vencida, ela impede a matrícula, congela a matrícula ativa e bloqueia o descongelamento até ser renovada | `AvaliacaoFisica`<br>`Matricula.abrir()`<br>`Matricula.atualizarSituacao()`<br>`Matricula.descongelar()`<br>`Matricula.renovarAvaliacao()` | `AvaliacaoFisicaVencidaException` | `MatriculaTest.avaliacaoVencidaImpedeMatricula`<br>`MatriculaTest.avaliacaoVencidaCongelaERenovacaoLibera` |

\* Regras autorais do grupo.

Todas as exceções herdam de `RegraDeNegocioException`, que o menu captura sem encerrar o programa.
