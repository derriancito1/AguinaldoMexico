package com.mexico.aguinaldo.aguinaldomexico;

import org.junit.Test;
import static org.junit.Assert.*;

public class ReviewPolicyTest {
    private static final long T0 = 1_700_000_000_000L;

    @Test public void noAntesDeSieteDias() {
        assertFalse(ReviewPolicy.permitirIntento(T0 + ReviewPolicy.MIN_ANTIGUEDAD_MS - 1, T0, 4, false));
    }
    @Test public void requiereTresCalculos() {
        assertFalse(ReviewPolicy.permitirIntento(T0 + ReviewPolicy.MIN_ANTIGUEDAD_MS, T0, 2, false));
    }
    @Test public void puedeIntentarUnaVez() {
        assertTrue(ReviewPolicy.permitirIntento(T0 + ReviewPolicy.MIN_ANTIGUEDAD_MS, T0, 3, false));
        assertFalse(ReviewPolicy.permitirIntento(T0 + ReviewPolicy.MIN_ANTIGUEDAD_MS, T0, 3, true));
    }
    @Test public void protegeContraRelojRetrocedidoYFechaAusente() {
        assertFalse(ReviewPolicy.permitirIntento(T0, T0 + 1, 9, false));
        assertFalse(ReviewPolicy.permitirIntento(T0, 0, 9, false));
    }
}
