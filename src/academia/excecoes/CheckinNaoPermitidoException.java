package academia.excecoes;


/** RN03: matrícula não ativa ou check-in já feito no dia. */
public class CheckinNaoPermitidoException extends RegraDeNegocioException {
    private static final long serialVersionUID = 1L;
    public CheckinNaoPermitidoException(String mensagem) { super(mensagem); }
}
