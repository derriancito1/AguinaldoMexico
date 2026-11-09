package com.mexico.aguinaldo.aguinaldomexico;

import java.util.List;

/** Datos fiscales de un ejercicio obtenidos de Firebase o del respaldo local. */
public class DatosFiscales {
    public int ejercicio;
    public double umaDiaria;
    public double umaEneroDiaria;
    public String vigenteDesde;
    public List<TablaISR> tarifas;
    public double salarioMinimoGeneralDiario;
    public double salarioMinimoGeneralMensual;
    public double salarioMinimoFronteraNorteDiario;
    public double salarioMinimoFronteraNorteMensual;

    public double umaParaFecha(String fechaIso) {
        if (fechaIso == null || vigenteDesde == null) {
            throw new IllegalArgumentException("Fecha o vigencia UMA no disponible");
        }
        return fechaIso.compareTo(vigenteDesde) < 0 ? umaEneroDiaria : umaDiaria;
    }
}
