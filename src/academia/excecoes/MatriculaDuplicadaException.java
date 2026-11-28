package academia.excecoes;


/** RN01: aluno já possui matrícula. */
public class MatriculaDuplicadaException extends RegraDeNegocioException {
    private static final long serialVersionUID = 1L;
    public MatriculaDuplicadaException(String mensagem) { super(mensagem); }
}
