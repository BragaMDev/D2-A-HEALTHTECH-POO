package academia.excecoes;


/** RN02: plano sem congelamento ou limite anual esgotado. */
public class CongelamentoNaoPermitidoException extends RegraDeNegocioException {
    private static final long serialVersionUID = 1L;
    public CongelamentoNaoPermitidoException(String mensagem) { super(mensagem); }
}
