package academia.excecoes;


/** R10: raiz das exceções de domínio; o menu a captura sem encerrar o programa. */
public class RegraDeNegocioException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public RegraDeNegocioException(String mensagem) { super(mensagem); }
}
