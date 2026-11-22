package academia.enums;

import academia.plano.Plano;
import academia.plano.PlanoAnual;
import academia.plano.PlanoMensal;
import academia.plano.PlanoTrimestral;

/** RN01: únicos planos permitidos. */
public enum TipoPlano {
    MENSAL, TRIMESTRAL, ANUAL;

    public Plano criarPlano() {
        return switch (this) {
            case MENSAL -> new PlanoMensal();
            case TRIMESTRAL -> new PlanoTrimestral();
            case ANUAL -> new PlanoAnual();
        };
    }
}
