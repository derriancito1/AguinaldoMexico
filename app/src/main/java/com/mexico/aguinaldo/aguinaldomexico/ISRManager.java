package com.mexico.aguinaldo.aguinaldomexico;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ISRManager {
    private static final String PREFS_NAME = "ISR_PREFS";
    private static final String KEY_YEAR = "ISR_YEAR";
    private static final String KEY_DATA = "ISR_DATA";

    private Context context;
    private SharedPreferences prefs;

    public ISRManager(Context context) {
        this.context = context;
        try {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    public void getTablaISRActual(ISRCallback callback) {
        try {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            int savedYear = prefs.getInt(KEY_YEAR, 0);

            if (currentYear != savedYear) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("tablasISR").child(String.valueOf(currentYear));
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        try {
                            if (snapshot.exists()) {
                                List<TablaISR> tabla = new ArrayList<>();
                                for (DataSnapshot child : snapshot.getChildren()) {
                                    TablaISR item = child.getValue(TablaISR.class);
                                    if (item != null) {
                                        tabla.add(item);
                                    }
                                }
                                guardarLocal(currentYear, tabla);
                                callback.onTablaObtenida(tabla);
                            } else {
                                callback.onTablaObtenida(cargarLocal());
                            }
                        } catch (Exception e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            callback.onError("Error al procesar datos de Firebase");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        FirebaseCrashlytics.getInstance().recordException(error.toException());
                        callback.onError("Error de Firebase: " + error.getMessage());
                    }
                });
            } else {
                callback.onTablaObtenida(cargarLocal());
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            callback.onError("Error inesperado al obtener tabla ISR");
        }
    }

    private void guardarLocal(int year, List<TablaISR> tabla) {
        try {
            prefs.edit()
                    .putInt(KEY_YEAR, year)
                    .putString(KEY_DATA, new Gson().toJson(tabla))
                    .apply();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private List<TablaISR> cargarLocal() {
        try {
            String json = prefs.getString(KEY_DATA, null);
            if (json != null) {
                return new Gson().fromJson(json, getType());
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        return new ArrayList<>();
    }

    private Type getType() {
        return new TypeToken<List<TablaISR>>() {}.getType();
    }

    public interface ISRCallback {
        void onTablaObtenida(List<TablaISR> tabla);
        void onError(String error);
    }

    public void imprimirDatosLocales() {
        try {
            String year = String.valueOf(prefs.getInt(KEY_YEAR, 0));
            String json = prefs.getString(KEY_DATA, "[]");
            Log.d("ISRManager", "Año guardado: " + year);
            Log.d("ISRManager", "Tabla ISR JSON: " + json);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }
}
