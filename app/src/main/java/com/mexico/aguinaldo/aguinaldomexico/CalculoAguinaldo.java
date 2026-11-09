package com.mexico.aguinaldo.aguinaldomexico;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Cálculo estimativo; la retención real puede variar según nómina/subsidio. */
public final class CalculoAguinaldo {
    private CalculoAguinaldo() {}

    public static class Resultado {
        public final double bruto, exento, gravado, isr, neto;
        Resultado(double bruto, double exento, double gravado, double isr, double neto) {
            this.bruto=bruto; this.exento=exento; this.gravado=gravado;
            this.isr=isr; this.neto=neto;
        }
    }

    public static Resultado calcular(double salarioMensual, int diasAguinaldo,
                                      long diasTrabajados, int diasEnEjercicio,
                                      double umaDiaria, List<TablaISR> tabla) {
        if ((Double.isNaN(salarioMensual) || Double.isInfinite(salarioMensual)) || salarioMensual <= 0 || diasAguinaldo < 1
                || diasTrabajados <= 0 || diasTrabajados > diasEnEjercicio
                || (diasEnEjercicio != 365 && diasEnEjercicio != 366)
                || (Double.isNaN(umaDiaria) || Double.isInfinite(umaDiaria)) || umaDiaria <= 0
                || tabla == null || tabla.size() != 11) {
            throw new IllegalArgumentException("Datos incompletos o valores fuera de rango");
        }
        double bruto = moneda(salarioMensual / 30.0 * diasAguinaldo * diasTrabajados / diasEnEjercicio);
        // Exención ANUAL de 30 UMA, NO proporcional a los días trabajados.
        // Supone disponible la exención completa y que se cumple la generalidad.
        double exento = moneda(Math.min(bruto, 30.0 * umaDiaria));
        double gravado = moneda(bruto - exento);
        // Procedimiento único de estimación del ISR de gratificación anual,
        // basado en el artículo 174 del Reglamento de la LISR. No se presenta
        // como retención exacta del patrón (nómina puede aplicar otro método).
        double isr = 0;
        if (gravado > 0) {
            double impuestoOrdinario = impuestoMensual(tabla, salarioMensual);
            double promedioMensual = gravado * 30.4 / 365.0;
            double impuestoConGratificacion = impuestoMensual(tabla, salarioMensual + promedioMensual);
            double diferencia = Math.max(0, impuestoConGratificacion - impuestoOrdinario);
            isr = moneda(Math.min(gravado, Math.max(0, (diferencia / promedioMensual) * gravado)));
        }
        return new Resultado(bruto, exento, gravado, isr, moneda(bruto - isr));
    }

    public static double impuestoMensual(List<TablaISR> rangos, double ingreso) {
        if (ingreso < 0 || (Double.isNaN(ingreso) || Double.isInfinite(ingreso)) || rangos == null || rangos.isEmpty()) {
            throw new IllegalArgumentException("Ingreso o tarifa ISR inválidos");
        }
        if (ingreso == 0) return 0;
        for (TablaISR r : rangos) {
            if (ingreso >= r.getMin() - 0.00001 && ingreso <= r.getMax() + 0.00001) {
                return moneda(Math.max(0, (ingreso - r.getMin()) * r.getTasa() + r.getCuotaFija()));
            }
        }
        throw new IllegalStateException("No hay rango ISR para el ingreso solicitado");
    }

    private static double moneda(double n) {
        return BigDecimal.valueOf(n).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
