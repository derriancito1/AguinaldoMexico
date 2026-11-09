package com.mexico.aguinaldo.aguinaldomexico;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

/** Un menú lateral compartido; cada opción abre su propia Activity. */
public final class MenuNavegacion {
    private MenuNavegacion() { }

    public static void instalar(AppCompatActivity activity, DrawerLayout drawer, NavigationView navigation) {
        final OnBackPressedCallback cerrarPrimeroMenu = new OnBackPressedCallback(false) {
            @Override public void handleOnBackPressed() {
                drawer.closeDrawer(GravityCompat.START);
            }
        };
        activity.getOnBackPressedDispatcher().addCallback(activity, cerrarPrimeroMenu);
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override public void onDrawerOpened(android.view.View drawerView) {
                cerrarPrimeroMenu.setEnabled(true);
            }
            @Override public void onDrawerClosed(android.view.View drawerView) {
                cerrarPrimeroMenu.setEnabled(false);
            }
        });

        navigation.setNavigationItemSelectedListener(item -> {
            drawer.closeDrawer(GravityCompat.START);
            int id = item.getItemId();
            if (id == R.id.nav_calificar) {
                abrirGooglePlay(activity);
                return true;
            }
            if (id == R.id.nav_calculadora) {
                if (!(activity instanceof MainActivity)) {
                    Intent volver = new Intent(activity, MainActivity.class);
                    volver.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(volver);
                    activity.finish();
                }
                return true;
            }

            Class<? extends AppCompatActivity> destino = null;
            if (id == R.id.nav_guia) destino = InformacionAguinaldoActivity.class;
            else if (id == R.id.nav_como_calcular) destino = ComoCalcularActivity.class;
            else if (id == R.id.nav_derechos) destino = DerechosLaboralesActivity.class;
            else if (id == R.id.nav_isr) destino = IsrUmaActivity.class;
            else if (id == R.id.nav_valores) destino = ValoresFiscalesActivity.class;
            else if (id == R.id.nav_fuentes) destino = FuentesOficialesActivity.class;

            if (destino != null) {
                int ejercicio = activity instanceof MainActivity
                        ? ((MainActivity) activity).obtenerEjercicioSeleccionado()
                        : activity.getIntent().getIntExtra(InformacionAguinaldoActivity.EXTRA_EJERCICIO,
                        java.util.Calendar.getInstance().get(java.util.Calendar.YEAR));
                abrirPantalla(activity, destino, ejercicio);
                return true;
            }
            return false;
        });
    }

    /** Navegación entre páginas informativas sin acumular actividades duplicadas. */
    public static void abrirPantalla(AppCompatActivity activity,
                                    Class<? extends AppCompatActivity> destino, int ejercicio) {
        if (activity.getClass().equals(destino)) return;
        Intent intent = new Intent(activity, destino);
        intent.putExtra(InformacionAguinaldoActivity.EXTRA_EJERCICIO, ejercicio);
        activity.startActivity(intent);
        // Desde una página informativa, la nueva sustituye a la anterior en la pila.
        // Atrás regresa a la calculadora; el menú permanece disponible en todas.
        if (activity instanceof InformacionAguinaldoActivity) activity.finish();
    }

    /** La opción manual no consume el intento automático de Google Play In-App Review. */
    private static void abrirGooglePlay(AppCompatActivity activity) {
        String paquete = activity.getPackageName();
        Intent market = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + paquete));
        market.setPackage("com.android.vending");
        try {
            activity.startActivity(market);
        } catch (ActivityNotFoundException | SecurityException error) {
            try {
                activity.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + paquete)));
            } catch (ActivityNotFoundException | SecurityException ex) {
                Toast.makeText(activity, "No se pudo abrir Google Play", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
