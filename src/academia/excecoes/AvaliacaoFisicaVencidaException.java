package academia.excecoes;


/** RN08: avaliação física ausente ou vencida. */
public class AvaliacaoFisicaVencidaException extends RegraDeNegocioException {
    private static final long serialVersionUID = 1L;
    public AvaliacaoFisicaVencidaException(String mensagem) { super(mensagem); }
}
