package com.mexico.aguinaldo.aguinaldomexico;

import java.util.concurrent.TimeUnit;

/** Reglas puras que se pueden probar sin Android ni Google Play. */
public final class ReviewPolicy {
    public static final long MIN_ANTIGUEDAD_MS = TimeUnit.DAYS.toMillis(7);
    public static final int MIN_CALCULOS = 3;

    private ReviewPolicy() { }

    public static boolean permitirIntento(long ahora, long primerUso, int calculos, boolean yaIntentado) {
        return !yaIntentado && primerUso > 0 && ahora >= primerUso
                && ahora - primerUso >= MIN_ANTIGUEDAD_MS && calculos >= MIN_CALCULOS;
    }
}
