package com.mexico.aguinaldo.aguinaldomexico;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class CalculoAguinaldoTest {
    private List<TablaISR> tabla2026() {
        double[][] datos = {
            {0.01, 844.59, 0, .0192}, {844.60,7168.51,16.22,.064},
            {7168.52,12598.02,420.95,.1088}, {12598.03,14644.64,1011.68,.16},
            {14644.65,17533.64,1339.14,.1792}, {17533.65,35362.83,1856.84,.2136},
            {35362.84,55736.68,5665.16,.2352}, {55736.69,106410.50,10457.09,.30},
            {106410.51,141880.66,25659.23,.32}, {141880.67,425641.99,37009.69,.34},
            {425642.00,9999999999d,133488.54,.35}
        };
        List<TablaISR> out = new ArrayList<>();
        for (double[] r : datos) {
            TablaISR t = new TablaISR();
            t.setMin(r[0]); t.setMax(r[1]); t.setCuotaFija(r[2]); t.setTasa(r[3]);
            out.add(t);
        }
        return out;
    }

    @Test public void proporcionalExentoNoSeProrratea() {
        CalculoAguinaldo.Resultado r = CalculoAguinaldo.calcular(8480, 15, 92, 365, 117.31, tabla2026());
        assertEquals(1068.71, r.bruto, .001);
        assertEquals(1068.71, r.exento, .001);
        assertEquals(0, r.gravado, .001);
        assertEquals(0, r.isr, .001);
    }

    @Test public void anualRetencionUnica() {
        CalculoAguinaldo.Resultado r=CalculoAguinaldo.calcular(15000,15,365,365,117.31,tabla2026());
        assertEquals(7500, r.bruto, .001);
        assertEquals(3519.30, r.exento, .001);
        assertEquals(3980.70, r.gravado, .001);
        assertTrue(r.isr > 0);
        assertEquals(r.bruto-r.isr,r.neto,.01);
    }

    @Test public void bisiestoNoInflaAguinaldoAnual() {
        CalculoAguinaldo.Resultado r=CalculoAguinaldo.calcular(10000,15,366,366,117.31,tabla2026());
        assertEquals(5000, r.bruto, .001);
    }

    @Test(expected = IllegalArgumentException.class) public void noCalcularConTablaVacia() {
        CalculoAguinaldo.calcular(15000,15,365,365,117.31,new ArrayList<>());
    }
}
