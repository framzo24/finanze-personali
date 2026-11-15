package it.francescozoni.finanzepersonali.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class QuoteIrene {

    public static final int PERSONE = 3;
    /** Ordine delle persone: Victoria (0), Irene cioè tu (1), Anita (2). */
    public static final int INDICE_TU = 1;

    private QuoteIrene() {
    }

    /** Tre parti uguali: i centesimi che avanzano vanno in ordine a Victoria, Irene e Anita. */
    public static List<BigDecimal> dividi(BigDecimal importo) {
        long centesimi = importo.movePointRight(2).setScale(0, RoundingMode.HALF_UP).abs().longValueExact();
        long base = centesimi / PERSONE;
        long resto = centesimi % PERSONE;
        int segno = importo.signum() < 0 ? -1 : 1;

        List<BigDecimal> parti = new ArrayList<>();
        for (int i = 0; i < PERSONE; i++) {
            long parte = base + (i < resto ? 1 : 0);
            parti.add(BigDecimal.valueOf(segno * parte, 2));
        }
        return parti;
    }

    public static BigDecimal quota(BigDecimal importo, int indice) {
        return dividi(importo).get(indice);
    }
}