package academia.interfaces;

import java.time.LocalDate;

/** R06: a Secretaria depende desta interface (recebida no construtor), não de uma implementação. */
public interface RelogioAcademia {
    LocalDate hoje();
}
