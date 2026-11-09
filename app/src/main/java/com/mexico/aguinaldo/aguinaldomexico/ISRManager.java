package com.mexico.aguinaldo.aguinaldomexico;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Tarifas mensuales ISR y UMA vigentes por año, con caché y actualización de Firebase. */
public class ISRManager {
    private static final String TAG = "ISRManager";
    private static final String PREFS_NAME = "ISR_DATOS_FISCALES_V2";
    private static final long TTL_MS = TimeUnit.HOURS.toMillis(24);
    private static final String NODE = "tablasISR";
    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<TablaISR>>() {}.getType();

    public interface ISRCallback {
        void onDatosObtenidos(DatosFiscales datos, String origen);
        void onError(String error);
    }

    public ISRManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Consultar un año concreto, no el año del reloj del móvil. */
    public void obtenerDatos(int ejercicio, ISRCallback callback) {
        obtenerDatos(ejercicio, false, callback);
    }

    private void obtenerDatos(int ejercicio, boolean forzarRed, ISRCallback callback) {
        if (ejercicio < 2025 || ejercicio > 2100) {
            callback.onError("No hay información fiscal compatible con ese ejercicio");
            return;
        }
        DatosFiscales cache = cargarCache(ejercicio);
        long last = prefs.getLong("ultima_" + ejercicio, 0);
        if (!forzarRed && cache != null && System.currentTimeMillis() - last < TTL_MS && last > 0) {
            callback.onDatosObtenidos(cache, "Datos fiscales locales recientes");
            return;
        }

        // Una lectura del nodo permite obtener la tarifa y su UMA en conjunto.
        // get() intenta consultar Firebase; si falla usamos los datos válidos locales.
        FirebaseDatabase.getInstance().getReference(NODE).get()
            .addOnSuccessListener(snapshot -> {
                try {
                    DatosFiscales datos = parseFirebase(snapshot, ejercicio);
                    if (datos != null && validar(datos)) {
                        guardarCache(datos);
                        Log.i(TAG, "Firebase Realtime Database: /tablasISR, ejercicio " + ejercicio + ", sincronización correcta");
                        callback.onDatosObtenidos(datos, "Firebase (lectura completada)");
                    } else {
                        usarRespaldo(ejercicio, cache, callback,
                            "Aún no existe una tabla ISR y UMA completa para " + ejercicio);
                    }
                } catch (Exception ex) {
                    Log.w(TAG, "Datos remotos inválidos para " + ejercicio, ex);
                    usarRespaldo(ejercicio, cache, callback, "Los datos fiscales de Firebase no son válidos");
                }
            })
            .addOnFailureListener(error -> {
                Log.w(TAG, "Firebase no disponible, modo sin conexión", error);
                usarRespaldo(ejercicio, cache, callback, "No hay conexión ni datos fiscales para " + ejercicio);
            });
    }

    /** Precarga durante la apertura, incluso sin que el usuario pulse Calcular. */
    public void precargar(int ejercicio) {
        precargar(ejercicio, null);
    }

    /** Fuerza lectura remota en cada apertura, incluso con una caché reciente. */
    public void precargar(int ejercicio, ISRCallback listener) {
        obtenerDatos(ejercicio, true, new ISRCallback() {
            @Override public void onDatosObtenidos(DatosFiscales datos, String origen) {
                Log.d(TAG, "Sincronización de datos fiscales " + ejercicio + ": " + origen);
                if (listener != null) listener.onDatosObtenidos(datos, origen);
            }
            @Override public void onError(String error) {
                Log.i(TAG, error);
                if (listener != null) listener.onError(error);
            }
        });
    }

    private void usarRespaldo(int ejercicio, DatosFiscales cache,
                              ISRCallback callback, String error) {
        if (cache != null && validar(cache)) {
            callback.onDatosObtenidos(cache, "Respaldo disponible sin conexión");
            return;
        }
        DatosFiscales base = cargarBaseIncluida(ejercicio);
        if (base != null && validar(base)) {
            // Se guarda SIN marcar fecha de actualización remota.
            prefs.edit().putString("datos_" + ejercicio, gson.toJson(base)).apply();
            callback.onDatosObtenidos(base, "Respaldo oficial incluido en la app");
            return;
        }
        callback.onError(error + ". No se estimará el ISR usando otro año o una tarifa vacía.");
    }

    private DatosFiscales parseFirebase(DataSnapshot snapshot, int ejercicio) {
        DataSnapshot lista = snapshot.child(String.valueOf(ejercicio));
        if (!lista.exists()) return null;
        ArrayList<TablaISR> tarifas = new ArrayList<>();
        for (DataSnapshot child : lista.getChildren()) {
            TablaISR rango = child.getValue(TablaISR.class);
            if (rango != null) tarifas.add(rango);
        }
        DataSnapshot p = snapshot.child("parametrosFiscales").child(String.valueOf(ejercicio));
        DatosFiscales datos = new DatosFiscales();
        datos.ejercicio = ejercicio;
        datos.tarifas = tarifas;
        if (p.exists()) {
            Double diaria = p.child("umaDiaria").getValue(Double.class);
            Double enero = p.child("umaEneroDiaria").getValue(Double.class);
            String vigente = p.child("vigenteDesde").getValue(String.class);
            if (diaria != null) datos.umaDiaria = diaria;
            if (enero != null) datos.umaEneroDiaria = enero;
            datos.vigenteDesde = vigente;
            Double smgDia = p.child("salarioMinimoGeneralDiario").getValue(Double.class);
            Double smgMes = p.child("salarioMinimoGeneralMensual").getValue(Double.class);
            Double zlfnDia = p.child("salarioMinimoFronteraNorteDiario").getValue(Double.class);
            Double zlfnMes = p.child("salarioMinimoFronteraNorteMensual").getValue(Double.class);
            if (smgDia != null) datos.salarioMinimoGeneralDiario = smgDia;
            if (smgMes != null) datos.salarioMinimoGeneralMensual = smgMes;
            if (zlfnDia != null) datos.salarioMinimoFronteraNorteDiario = zlfnDia;
            if (zlfnMes != null) datos.salarioMinimoFronteraNorteMensual = zlfnMes;
        } else {
            // Compatibilidad con nodos existentes, que únicamente contenían tarifas.
            DatosFiscales local = cargarBaseIncluida(ejercicio);
            if (local != null) {
                datos.umaDiaria = local.umaDiaria;
                datos.umaEneroDiaria = local.umaEneroDiaria;
                datos.vigenteDesde = local.vigenteDesde;
            }
        }
        // Compatibilidad: los datos fiscales de la BD pueden ser de una versión
        // antigua sin salarios mínimos. Se conserva la tabla remota y se usan
        // SOLO referencias oficiales locales para el salario del año conocido.
        if (datos.salarioMinimoGeneralMensual <= 0 || datos.salarioMinimoFronteraNorteMensual <= 0) {
            DatosFiscales local = cargarBaseIncluida(ejercicio);
            if (local != null) {
                datos.salarioMinimoGeneralDiario = local.salarioMinimoGeneralDiario;
                datos.salarioMinimoGeneralMensual = local.salarioMinimoGeneralMensual;
                datos.salarioMinimoFronteraNorteDiario = local.salarioMinimoFronteraNorteDiario;
                datos.salarioMinimoFronteraNorteMensual = local.salarioMinimoFronteraNorteMensual;
                Log.w(TAG, "Faltan salarios mínimos en Firebase para " + ejercicio + "; actualizar parametrosFiscales");
            }
        }
        return datos;
    }

    private DatosFiscales cargarBaseIncluida(int ejercicio) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                context.getAssets().open("tablasISR_base.json"), StandardCharsets.UTF_8))) {
            JsonObject raiz = JsonParser.parseReader(br).getAsJsonObject();
            JsonElement lista = raiz.get(String.valueOf(ejercicio));
            JsonObject parametros = raiz.getAsJsonObject("parametrosFiscales");
            if (lista == null || parametros == null || !parametros.has(String.valueOf(ejercicio))) return null;
            JsonObject p = parametros.getAsJsonObject(String.valueOf(ejercicio));
            DatosFiscales datos = new DatosFiscales();
            datos.ejercicio = ejercicio;
            datos.tarifas = gson.fromJson(lista, LIST_TYPE);
            datos.umaDiaria = p.get("umaDiaria").getAsDouble();
            datos.umaEneroDiaria = p.get("umaEneroDiaria").getAsDouble();
            datos.vigenteDesde = p.get("vigenteDesde").getAsString();
            datos.salarioMinimoGeneralDiario = p.get("salarioMinimoGeneralDiario").getAsDouble();
            datos.salarioMinimoGeneralMensual = p.get("salarioMinimoGeneralMensual").getAsDouble();
            datos.salarioMinimoFronteraNorteDiario = p.get("salarioMinimoFronteraNorteDiario").getAsDouble();
            datos.salarioMinimoFronteraNorteMensual = p.get("salarioMinimoFronteraNorteMensual").getAsDouble();
            return validar(datos) ? datos : null;
        } catch (Exception ex) {
            Log.e(TAG, "No se pudo abrir respaldo oficial", ex);
            return null;
        }
    }

    private DatosFiscales cargarCache(int ejercicio) {
        try {
            String json = prefs.getString("datos_" + ejercicio, null);
            if (json == null) return null;
            DatosFiscales datos = gson.fromJson(json, DatosFiscales.class);
            return validar(datos) && datos.ejercicio == ejercicio ? datos : null;
        } catch (Exception ex) {
            Log.w(TAG, "Caché inválida", ex);
            return null;
        }
    }

    private void guardarCache(DatosFiscales datos) {
        prefs.edit().putString("datos_" + datos.ejercicio, gson.toJson(datos))
                .putLong("ultima_" + datos.ejercicio, System.currentTimeMillis()).apply();
    }

    /** Nunca devolver ISR 0 por falta de un rango de tarifa. */
    private boolean validar(DatosFiscales d) {
        if (d == null || d.tarifas == null || d.tarifas.size() != 11
                || d.ejercicio < 2025 || (Double.isNaN(d.umaDiaria) || Double.isInfinite(d.umaDiaria)) || d.umaDiaria <= 0
                || (Double.isNaN(d.umaEneroDiaria) || Double.isInfinite(d.umaEneroDiaria)) || d.umaEneroDiaria <= 0
                || d.vigenteDesde == null || !d.vigenteDesde.matches("\\d{4}-\\d{2}-\\d{2}")
                || d.salarioMinimoGeneralDiario <= 0 || d.salarioMinimoGeneralMensual <= 0
                || d.salarioMinimoFronteraNorteDiario <= 0 || d.salarioMinimoFronteraNorteMensual <= 0
                || Double.isNaN(d.salarioMinimoGeneralMensual) || Double.isInfinite(d.salarioMinimoGeneralMensual)
                || Double.isNaN(d.salarioMinimoFronteraNorteMensual) || Double.isInfinite(d.salarioMinimoFronteraNorteMensual)) return false;
        double ultimoMax = 0;
        for (TablaISR r : d.tarifas) {
            if (r == null || (Double.isNaN(r.getMin()) || Double.isInfinite(r.getMin())) || (Double.isNaN(r.getMax()) || Double.isInfinite(r.getMax()))
                    || (Double.isNaN(r.getCuotaFija()) || Double.isInfinite(r.getCuotaFija())) || (Double.isNaN(r.getTasa()) || Double.isInfinite(r.getTasa()))
                    || r.getMin() < 0 || r.getMin() > r.getMax() || r.getCuotaFija() < 0 || r.getTasa() < 0 || r.getTasa() > .36)
                return false;
            if (Math.abs((r.getMin() - ultimoMax) - 0.01) > 0.011) return false;
            ultimoMax = r.getMax();
        }
        return ultimoMax > 1_000_000;
    }
}
