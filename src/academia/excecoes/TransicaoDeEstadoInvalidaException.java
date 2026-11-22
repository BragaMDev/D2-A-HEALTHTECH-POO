package academia.excecoes;


/** RN06: transição de estado recusada. */
public class TransicaoDeEstadoInvalidaException extends RegraDeNegocioException {
    private static final long serialVersionUID = 1L;
    public TransicaoDeEstadoInvalidaException(String mensagem) { super(mensagem); }
}
