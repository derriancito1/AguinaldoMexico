package com.mexico.aguinaldo.aguinaldomexico;

public class TablaISR {
    private double min;
    private double max;
    private double cuotaFija;
    private double tasa;

    public TablaISR() {
        // Constructor vacío necesario para Firebase
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getCuotaFija() {
        return cuotaFija;
    }

    public double getTasa() {
        return tasa;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public void setCuotaFija(double cuotaFija) {
        this.cuotaFija = cuotaFija;
    }

    public void setTasa(double tasa) {
        this.tasa = tasa;
    }
}
