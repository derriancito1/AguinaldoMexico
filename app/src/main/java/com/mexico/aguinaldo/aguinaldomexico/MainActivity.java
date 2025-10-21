package com.mexico.aguinaldo.aguinaldomexico;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.android.gms.ads.MobileAds;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest;

import android.os.Build;

import android.view.WindowInsets;

import android.graphics.Insets;
import android.graphics.Color;

import android.view.Window;









public class MainActivity extends AppCompatActivity {

    EditText etSalarioMensual, etDiasAguinaldo, etFechaIngreso, etFechaSalida;
    Button btnCalcular;
    TextView tvResultados;
    Calendar fechaIngresoCalendar;
    Calendar fechaSalidaCalendar;

    ISRManager isrManager;
    AdView adView;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_LAST_REVIEW = "lastReviewTime";
    private static final long REVIEW_INTERVAL = 7 * 24 * 60 * 60 * 1000L; // 7 días

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.setStatusBarColor(Color.BLACK); // Cambia el fondo de la barra de estado a negro
            }


            // Aquí insertas el bloque para manejar el padding inferior
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                getWindow().setDecorFitsSystemWindows(false);
                findViewById(R.id.main_layout).setOnApplyWindowInsetsListener((v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsets.Type.systemBars());
                    v.setPadding(0, systemBars.top, 0, systemBars.bottom); // Aplica padding superior e inferior
                    return insets;
                });
            }


            // Inicializa AdMob
            MobileAds.initialize(this, initializationStatus -> {});

            adView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);

            // Fin AdMob



            AndroidThreeTen.init(this);
            FirebaseApp.initializeApp(this);

            isrManager = new ISRManager(this);

            etSalarioMensual = findViewById(R.id.etSalarioMensual);
            etDiasAguinaldo = findViewById(R.id.etDiasAguinaldo);
            etFechaIngreso = findViewById(R.id.etFechaIngreso);
            etFechaSalida = findViewById(R.id.etFechaSalida);
            btnCalcular = findViewById(R.id.btnCalcular);
            tvResultados = findViewById(R.id.tvResultados);

            ImageButton btnAyudaSalario = findViewById(R.id.btnAyudaSalario);
            btnAyudaSalario.setOnClickListener(v -> {
                try {
                    new AlertDialog.Builder(this)
                            .setTitle("Salario mensual")
                            .setMessage("Ingresa tu salario mensual bruto, es decir, el total que percibes antes de cualquier deducción o impuesto. " +
                                    "Normalmente puedes encontrar este dato en tu recibo de nómina o contrato laboral.")
                            .setPositiveButton("OK", null)
                            .show();
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            });

            ImageButton btnAyudaDiasAguinaldo = findViewById(R.id.btnAyudaDiasAguinaldo);
            btnAyudaDiasAguinaldo.setOnClickListener(v -> {
                try {
                    new AlertDialog.Builder(this)
                            .setTitle("Días de aguinaldo")
                            .setMessage("Este campo representa la cantidad de días de aguinaldo que tu empresa otorga en el año. " +
                                    "Por ley en México, el mínimo es 15 días, pero algunas empresas pueden dar más. " +
                                    "Puedes consultar esta información en tu contrato o con recursos humanos.")
                            .setPositiveButton("OK", null)
                            .show();
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            });

            fechaIngresoCalendar = Calendar.getInstance();
            fechaSalidaCalendar = Calendar.getInstance();

            int year = Calendar.getInstance().get(Calendar.YEAR);
            fechaSalidaCalendar.set(year, Calendar.DECEMBER, 31);
            actualizarFechaSalidaEditText();

            etFechaIngreso.setOnClickListener(view -> {
                try {
                    mostrarDatePicker(fechaIngresoCalendar, etFechaIngreso);
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            });

            etFechaSalida.setOnClickListener(view -> {
                try {
                    mostrarDatePicker(fechaSalidaCalendar, etFechaSalida);
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            });

            btnCalcular.setOnClickListener(view -> {
                try {
                    calcularAguinaldo();
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            });

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


    @Override
    protected void onPause() {
        if (adView != null) adView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) adView.resume();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) adView.destroy();
        super.onDestroy();
    }
    private void mostrarDatePicker(Calendar calendar, EditText etFecha) {
        try {
            int año = calendar.get(Calendar.YEAR);
            int mes = calendar.get(Calendar.MONTH);
            int dia = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, añoSel, mesSel, diaSel) -> {
                        calendar.set(añoSel, mesSel, diaSel);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        etFecha.setText(sdf.format(calendar.getTime()));
                    },
                    año, mes, dia);
            datePickerDialog.show();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private void actualizarFechaSalidaEditText() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etFechaSalida.setText(sdf.format(fechaSalidaCalendar.getTime()));
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e); // <--- lo registras
        }
    }


    private void calcularAguinaldo() {

        try {


            double salarioMensual = Double.parseDouble(etSalarioMensual.getText().toString());
            int diasAguinaldo = Integer.parseInt(etDiasAguinaldo.getText().toString());
            double UMA_DIARIA = 113.14;

            String inputFechaIngreso = etFechaIngreso.getText().toString().trim();
            String inputFechaSalida = etFechaSalida.getText().toString().trim();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());

            LocalDate fechaIngreso = LocalDate.parse(inputFechaIngreso, formatter);
            LocalDate fechaSalida = LocalDate.parse(inputFechaSalida, formatter);

            int anioEnCurso = fechaSalida.getYear();

            LocalDate inicioAnio = LocalDate.of(anioEnCurso, 1, 1);
            LocalDate finAnio = LocalDate.of(anioEnCurso, 12, 31);

            if (fechaIngreso.isBefore(inicioAnio)) fechaIngreso = inicioAnio;
            if (fechaSalida.isAfter(finAnio)) fechaSalida = finAnio;

            if (fechaSalida.isBefore(fechaIngreso)) {
                Toast.makeText(this, "La fecha de salida no puede ser anterior a la de ingreso", Toast.LENGTH_LONG).show();
                return;
            }

            final long diasTrab = ChronoUnit.DAYS.between(fechaIngreso, fechaSalida.plusDays(1));
            final double salarioDiario = salarioMensual / 30.0;
            final double aguinaldoBruto = salarioDiario * diasAguinaldo * ((double) diasTrab / 365.0);
            final double parteExenta = UMA_DIARIA * 30 * ((double) diasTrab / 365.0);

            double tempParteGravada = aguinaldoBruto - parteExenta;
            if (tempParteGravada < 0) tempParteGravada = 0;
            final double parteGravada = tempParteGravada;

            // Ahora sí usamos variables finales dentro del callback
            isrManager.getTablaISRActual(new ISRManager.ISRCallback() {
                @Override
                public void onTablaObtenida(List<TablaISR> tablaISR) {
                    double isrSalarioMensual = calcularISR(tablaISR, salarioMensual);
                    double isrIngresoTotal = calcularISR(tablaISR, salarioMensual + parteGravada);
                    double isrAguinaldo = isrIngresoTotal - isrSalarioMensual;
                    if (isrAguinaldo < 0) isrAguinaldo = 0;

                    double aguinaldoNeto = aguinaldoBruto - isrAguinaldo;

                    String resultado =
                            "📅 Días trabajados: " + diasTrab + " días\n\n" +
                                    "💰 Aguinaldo bruto: $" + String.format(Locale.US, "%.2f", aguinaldoBruto) + "\n" +
                                    "🟩 Parte exenta (30 UMA): $" + String.format(Locale.US, "%.2f", parteExenta) + "\n" +
                                    "🟥 Parte gravada: $" + String.format(Locale.US, "%.2f", parteGravada) + "\n" +
                                    "💸 ISR retenido: $" + String.format(Locale.US, "%.2f", isrAguinaldo) + "\n\n" +
                                    "✅ Aguinaldo neto a recibir: $" + String.format(Locale.US, "%.2f", aguinaldoNeto);


                    runOnUiThread(() -> {
                        tvResultados.setText(resultado);
                        //diasTrabajados.setText("Días trabajados: " + diasTrab);
                    });
                }



                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error al cargar tabla ISR: " + error, Toast.LENGTH_LONG).show());
                }
            });

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);  // Reporta a Firebase
            Toast.makeText(this, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show();
        }
    }


    private double calcularISR(List<TablaISR> tablaISR, double ingreso) {
        try {
            if (tablaISR == null || tablaISR.isEmpty()) return 0;
            for (TablaISR rango : tablaISR) {
                if (ingreso >= rango.getMin() && ingreso <= rango.getMax()) {
                    return (ingreso - rango.getMin()) * rango.getTasa() + rango.getCuotaFija();
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e); // <--- lo registras
        }
        return 0;
    }

    




}
