package com.mexico.aguinaldo.aguinaldomexico;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;

import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

/** Un solo intento automático en este dispositivo; Google decide si muestra el cuadro. */
public final class SolicitudCalificacion {
    private static final String TAG = "CalificacionApp";
    private static final String PREFS = "calificacion_app_v1";
    private static final String FIRST = "primer_uso";
    private static final String COUNT = "calculos_exitosos";
    private static final String ATTEMPTED = "solicitud_ya_intentada";

    private SolicitudCalificacion() { }

    public static void registrarApertura(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!pref.contains(FIRST)) {
            pref.edit().putLong(FIRST, System.currentTimeMillis()).apply();
        }
    }

    /** Invocar únicamente después de presentar un cálculo válido al usuario. */
    public static void registrarCalculoExitoso(AppCompatActivity activity) {
        SharedPreferences pref = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (pref.getBoolean(ATTEMPTED, false)) return;
        int calculos = pref.getInt(COUNT, 0) + 1;
        long ahora = System.currentTimeMillis();
        long primerUso = pref.getLong(FIRST, 0L);
        if (primerUso <= 0L) primerUso = ahora;
        pref.edit().putInt(COUNT, calculos).putLong(FIRST, primerUso).apply();
        if (!ReviewPolicy.permitirIntento(ahora, primerUso, calculos, false)) return;

        // Permite leer primero los resultados; no mostrar una solicitud sobre otra pantalla.
        activity.getWindow().getDecorView().postDelayed(() -> {
            if (activity.isFinishing() || activity.isDestroyed()
                    || !activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                return; // Se volverá a evaluar tras otro cálculo exitoso.
            }
            if (pref.getBoolean(ATTEMPTED, false)) return;
            // Una sola solicitud automática: no se infiere ni almacena una reseña real.
            pref.edit().putBoolean(ATTEMPTED, true).apply();
            try {
                ReviewManager manager = ReviewManagerFactory.create(activity);
                manager.requestReviewFlow().addOnCompleteListener(activity, task -> {
                    if (!task.isSuccessful()) {
                        Log.i(TAG, "Solicitud de reseña no disponible en este dispositivo");
                        return;
                    }
                    if (activity.isFinishing() || activity.isDestroyed()
                            || !activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                        return;
                    }
                    manager.launchReviewFlow(activity, task.getResult())
                            .addOnCompleteListener(result ->
                                    Log.d(TAG, "Flujo de reseña cerrado; respuesta privada"));
                });
            } catch (RuntimeException ex) {
                // Ningún error de Play Store debe bloquear el uso de la calculadora.
                Log.w(TAG, "No fue posible solicitar reseña", ex);
            }
        }, 1800L);
    }
}
