package com.mexico.aguinaldo.aguinaldomexico;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import com.google.android.material.navigation.NavigationView;

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

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.EditorInfo;
import android.content.Context;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.datepicker.MaterialDatePicker;
import java.util.TimeZone;












public class MainActivity extends AppCompatActivity {

    EditText etSalarioMensual, etDiasAguinaldo, etFechaIngreso, etFechaSalida;
    Button btnCalcular;
    TextView tvResultados, tvResumenDatos, tvSugerenciaSalario;
    View formContainer, summaryContainer, resultadosContainer;
    LinearLayout linearContent;
    ScrollView scrollView;
    Button btnEditarDatos;
    private boolean mostrandoResultados = false;
    private boolean tecladoVisible = false;
    private double salarioMinimoSugerido = 0;
    private static final String KEY_MOSTRANDO_RESULTADOS = "mostrandoResultados";
    Calendar fechaIngresoCalendar;
    Calendar fechaSalidaCalendar;

    ISRManager isrManager;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {

            // Android 15/16 dibuja la barra de estado sobre el fondo de la actividad.
            // El fondo es claro, así que los indicadores del reloj, señal y batería
            // deben ser OSCUROS para que tengan contraste.
            Window window = getWindow();
            WindowCompat.setDecorFitsSystemWindows(window, false);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
            WindowInsetsControllerCompat barController = WindowCompat.getInsetsController(
                    window, window.getDecorView());
            barController.setAppearanceLightStatusBars(true);
            barController.setAppearanceLightNavigationBars(true);

            // Android 15/16: adjustResize por sí solo no desplaza el contenido
            // cuando usamos edge-to-edge (decorFitsSystemWindows=false).
            // La zona útil debe excluir explícitamente el teclado (IME).
            // Además, ocultamos el banner mientras se escribe para dar
            // prioridad a los campos y evitar que el anuncio quede encima.
            View root = findViewById(R.id.main_layout);
            View banner = findViewById(R.id.adView);
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
                boolean ahoraVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
                int espacioInferior = Math.max(bars.bottom,
                        ahoraVisible ? keyboard.bottom : 0);

                if (v.getPaddingLeft() != bars.left || v.getPaddingTop() != bars.top
                        || v.getPaddingRight() != bars.right
                        || v.getPaddingBottom() != espacioInferior) {
                    v.setPadding(bars.left, bars.top, bars.right, espacioInferior);
                }
                int visibilidadBanner = ahoraVisible ? View.GONE : View.VISIBLE;
                if (banner.getVisibility() != visibilidadBanner) {
                    banner.setVisibility(visibilidadBanner);
                }
                if (ahoraVisible != tecladoVisible) {
                    tecladoVisible = ahoraVisible;
                    if (ahoraVisible) {
                        // Reposicionamos tras la adaptación al alto del teclado.
                        v.postDelayed(this::mantenerCampoActivoVisible, 120);
                    }
                }
                return insets;
            });
            ViewCompat.requestApplyInsets(root);

            // Inicializa AdMob
            MobileAds.initialize(this, initializationStatus -> {});

            adView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);

            // Fin AdMob



            AndroidThreeTen.init(this);
            FirebaseApp.initializeApp(this);

            isrManager = new ISRManager(this);
            SolicitudCalificacion.registrarApertura(this);
            DrawerLayout drawer = findViewById(R.id.drawerLayout);
            MenuNavegacion.instalar(this, drawer, findViewById(R.id.navigationView));
            ImageButton botonMenu = findViewById(R.id.btnAbrirNavegacion);
            botonMenu.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

            etSalarioMensual = findViewById(R.id.etSalarioMensual);
            etDiasAguinaldo = findViewById(R.id.etDiasAguinaldo);
            etFechaIngreso = findViewById(R.id.etFechaIngreso);
            etFechaSalida = findViewById(R.id.etFechaSalida);
            btnCalcular = findViewById(R.id.btnCalcular);
            tvResultados = findViewById(R.id.tvResultados);
            tvResumenDatos = findViewById(R.id.tvResumenDatos);
            tvSugerenciaSalario = findViewById(R.id.tvSugerenciaSalario);
            tvSugerenciaSalario.setOnClickListener(v -> {
                if (salarioMinimoSugerido > 0) etSalarioMensual.setText(dinero(salarioMinimoSugerido).replace(",", ""));
            });
            // Igual que la sugerencia del sueldo: un toque coloca el mínimo legal.
            // La persona conserva la posibilidad de editarlo (por ejemplo, 20 o 30 días).
            TextView tvSugerenciaDiasAguinaldo = findViewById(R.id.tvSugerenciaDiasAguinaldo);
            tvSugerenciaDiasAguinaldo.setOnClickListener(v -> {
                etDiasAguinaldo.setText("15");
                etDiasAguinaldo.setSelection(etDiasAguinaldo.getText().length());
            });
            formContainer = findViewById(R.id.formContainer);
            summaryContainer = findViewById(R.id.summaryContainer);
            resultadosContainer = findViewById(R.id.resultadosContainer);
            linearContent = findViewById(R.id.linearContent);
            scrollView = findViewById(R.id.scrollView);
            // Cambiar de sueldo a días con el teclado abierto también
            // debe desplazar el ScrollView hacia el nuevo campo.
            etSalarioMensual.setOnFocusChangeListener((v, tieneFoco) -> {
                if (tieneFoco) v.postDelayed(() -> mantenerCampoVisible(etSalarioMensual), 180);
            });
            etDiasAguinaldo.setOnFocusChangeListener((v, tieneFoco) -> {
                if (tieneFoco) v.postDelayed(() -> mantenerCampoVisible(etDiasAguinaldo), 180);
            });
            etDiasAguinaldo.setOnEditorActionListener((campo, accion, evento) -> {
                if (accion == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(campo.getWindowToken(), 0);
                    campo.clearFocus();
                    return true;
                }
                return false;
            });
            btnEditarDatos = findViewById(R.id.btnEditarDatos);
            btnEditarDatos.setOnClickListener(v -> mostrarFormulario());
            if (savedInstanceState != null && savedInstanceState.getBoolean(KEY_MOSTRANDO_RESULTADOS, false)) {
                // Restauramos la vista tras rotar la pantalla.
                mostrandoResultados = true;
                formContainer.setVisibility(View.GONE);
                summaryContainer.setVisibility(View.VISIBLE);
                resultadosContainer.setVisibility(View.VISIBLE);
                tvResumenDatos.setText(savedInstanceState.getString("resumen", ""));
                tvResultados.setText(savedInstanceState.getString("resultado", ""));
            }

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
            // Al iniciar, comprobar Firebase aunque exista una caché reciente.
            isrManager.precargar(year, new ISRManager.ISRCallback() {
                @Override public void onDatosObtenidos(DatosFiscales datos, String origen) {
                    runOnUiThread(() -> {
                        if (fechaSalidaCalendar.get(Calendar.YEAR) == datos.ejercicio)
                            mostrarSugerenciaSalario(datos);
                    });
                }
                @Override public void onError(String error) {
                    runOnUiThread(() -> tvSugerenciaSalario.setText("Introduce tu salario mensual bruto"));
                }
            });

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


    public int obtenerEjercicioSeleccionado() {
        return fechaSalidaCalendar != null ? fechaSalidaCalendar.get(Calendar.YEAR)
                : Calendar.getInstance().get(Calendar.YEAR);
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
    /** MaterialDatePicker permite alternar entre calendario y escritura de fecha. */
    private void mostrarDatePicker(Calendar calendar, EditText etFecha) {
        try {
            // MaterialDatePicker requiere una selección en UTC para evitar saltos
            // de día debidos a la zona horaria del teléfono.
            Calendar fechaUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            fechaUtc.clear();
            fechaUtc.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            String titulo = etFecha.getId() == R.id.etFechaIngreso
                    ? "Selecciona la fecha de ingreso" : "Selecciona la fecha de fin y pago";

            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(titulo)
                    .setSelection(fechaUtc.getTimeInMillis())
                    .build();

            picker.addOnPositiveButtonClickListener(fechaSeleccionadaUtc -> {
                if (fechaSeleccionadaUtc == null) return;
                Calendar seleccionUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                seleccionUtc.setTimeInMillis(fechaSeleccionadaUtc);
                calendar.set(seleccionUtc.get(Calendar.YEAR), seleccionUtc.get(Calendar.MONTH),
                        seleccionUtc.get(Calendar.DAY_OF_MONTH));
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "MX"));
                etFecha.setText(sdf.format(calendar.getTime()));
                if (etFecha.getId() == R.id.etFechaSalida) {
                    actualizarSugerenciaSalario(calendar.get(Calendar.YEAR));
                }
            });
            picker.show(getSupportFragmentManager(), "fecha_" + etFecha.getId());
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(this, "No se pudo abrir el calendario", Toast.LENGTH_SHORT).show();
        }
    }

    /** Mantiene legible el campo que se está editando cuando aparece el teclado. */
    private void mantenerCampoActivoVisible() {
        if (etDiasAguinaldo != null && etDiasAguinaldo.hasFocus()) {
            mantenerCampoVisible(etDiasAguinaldo);
        } else if (etSalarioMensual != null && etSalarioMensual.hasFocus()) {
            mantenerCampoVisible(etSalarioMensual);
        }
    }

    private void mantenerCampoVisible(EditText campo) {
        if (scrollView == null || campo == null || !campo.hasFocus()
                || formContainer == null || formContainer.getVisibility() != View.VISIBLE) return;
        scrollView.post(() -> {
            if (!campo.hasFocus() || !campo.isShown()) return;
            // requestRectangleOnScreen hace que el ScrollView suba el campo
            // incluso cuando la pantalla tiene poco espacio vertical.
            Rect rect = new Rect();
            campo.getDrawingRect(rect);
            int margen = Math.round(16 * getResources().getDisplayMetrics().density);
            rect.bottom += margen;
            campo.requestRectangleOnScreen(rect, false);
        });
    }

    private void mostrarResultados(String resumen) {
        mostrandoResultados = true;
        tvResumenDatos.setText(resumen);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(etSalarioMensual.getWindowToken(), 0);
        getCurrentFocusClear();
        TransitionManager.beginDelayedTransition(linearContent, new AutoTransition().setDuration(200));
        formContainer.setVisibility(View.GONE);
        summaryContainer.setVisibility(View.VISIBLE);
        resultadosContainer.setVisibility(View.VISIBLE);
        scrollView.post(() -> scrollView.smoothScrollTo(0, 0));
    }

    private void mostrarFormulario() {
        mostrandoResultados = false;
        TransitionManager.beginDelayedTransition(linearContent, new AutoTransition().setDuration(200));
        formContainer.setVisibility(View.VISIBLE);
        summaryContainer.setVisibility(View.GONE);
        // Evita que queden visibles resultados anteriores al editar los datos.
        resultadosContainer.setVisibility(View.GONE);
        scrollView.post(() -> scrollView.smoothScrollTo(0, 0));
    }

    private void getCurrentFocusClear() {
        View focus = getCurrentFocus();
        if (focus != null) focus.clearFocus();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_MOSTRANDO_RESULTADOS, mostrandoResultados);
        if (tvResumenDatos != null) outState.putString("resumen", tvResumenDatos.getText().toString());
        if (tvResultados != null) outState.putString("resultado", tvResultados.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Los EditText restauran sus textos automáticamente; sincronizamos
        // los Calendar con esos textos para que reabrir un picker sea preciso.
        sincronizarCalendarConCampo(fechaIngresoCalendar, etFechaIngreso);
        sincronizarCalendarConCampo(fechaSalidaCalendar, etFechaSalida);
        actualizarSugerenciaSalario(fechaSalidaCalendar.get(Calendar.YEAR));
    }

    private void sincronizarCalendarConCampo(Calendar calendar, EditText campo) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "MX"));
            sdf.setLenient(false);
            calendar.setTime(sdf.parse(campo.getText().toString()));
        } catch (Exception ignored) {
            // Si el campo está vacío, conservamos la selección inicial.
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


    private void mostrarSugerenciaSalario(DatosFiscales datos) {
        // La sugerencia NO rellena el campo ni invalida salarios superiores o inferiores.
        salarioMinimoSugerido = datos.salarioMinimoGeneralMensual;
        tvSugerenciaSalario.setText("Salario mínimo general " + datos.ejercicio + ": $"
                + dinero(salarioMinimoSugerido) + " mensuales · Toca para usarlo");
    }

    private void actualizarSugerenciaSalario(int ejercicio) {
        salarioMinimoSugerido = 0;
        tvSugerenciaSalario.setText("Consultando salario mínimo de " + ejercicio + "…");
        isrManager.obtenerDatos(ejercicio, new ISRManager.ISRCallback() {
            @Override public void onDatosObtenidos(DatosFiscales datos, String origen) {
                runOnUiThread(() -> {
                    if (fechaSalidaCalendar.get(Calendar.YEAR) == datos.ejercicio) mostrarSugerenciaSalario(datos);
                });
            }
            @Override public void onError(String error) {
                runOnUiThread(() -> {
                    if (fechaSalidaCalendar.get(Calendar.YEAR) == ejercicio)
                        tvSugerenciaSalario.setText("Sin referencia oficial para " + ejercicio + "; introduce tu salario mensual");
                });
            }
        });
    }

    private void calcularAguinaldo() {
        try {
            double salarioMensual = Double.parseDouble(etSalarioMensual.getText().toString().trim().replace(',', '.'));
            int diasAguinaldo = Integer.parseInt(etDiasAguinaldo.getText().toString().trim());
            if ((Double.isNaN(salarioMensual) || Double.isInfinite(salarioMensual)) || salarioMensual <= 0 || diasAguinaldo < 1 || diasAguinaldo > 365) {
                Toast.makeText(this, "Indica salario positivo y días de aguinaldo válidos", Toast.LENGTH_LONG).show();
                return;
            }
            String ingresoInput = etFechaIngreso.getText().toString().trim();
            String salidaInput = etFechaSalida.getText().toString().trim();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("es", "MX"));
            LocalDate ingresoReal = LocalDate.parse(ingresoInput, fmt);
            LocalDate salidaReal = LocalDate.parse(salidaInput, fmt);
            LocalDate fechaPago = salidaReal; // Fecha única de fin y pago, según lo solicitado.
            if (salidaReal.isBefore(ingresoReal)) {
                Toast.makeText(this, "La fecha de salida no puede ser anterior al ingreso", Toast.LENGTH_LONG).show();
                return;
            }
            // El año laboral lo determina el corte; la UMA y tarifa ISR corresponden al año del pago.
            int anioLaboral = salidaReal.getYear();
            LocalDate inicio = ingresoReal.isBefore(LocalDate.of(anioLaboral, 1, 1))
                    ? LocalDate.of(anioLaboral, 1, 1) : ingresoReal;
            long diasTrab = ChronoUnit.DAYS.between(inicio, salidaReal.plusDays(1));
            int diasEnAnio = LocalDate.of(anioLaboral, 1, 1).lengthOfYear();
            if (diasTrab < 1 || diasTrab > diasEnAnio) {
                Toast.makeText(this, "Revisa las fechas del período laboral", Toast.LENGTH_LONG).show();
                return;
            }
            String resumen = "Ingreso: " + ingresoInput + "  ·  Fin y pago: " + salidaInput
                    + "\nSalario mensual: $" + String.format(Locale.US, "%,.2f", salarioMensual)
                    + "  ·  Días de aguinaldo: " + diasAguinaldo;
            final int ejercicioFiscal = fechaPago.getYear();
            final String fechaPagoIso = fechaPago.toString();
            btnCalcular.setEnabled(false);
            btnCalcular.setText("Calculando...");
            isrManager.obtenerDatos(ejercicioFiscal, new ISRManager.ISRCallback() {
                @Override
                public void onDatosObtenidos(DatosFiscales datos, String origen) {
                    try {
                        double umaUsada = datos.umaParaFecha(fechaPagoIso);
                        CalculoAguinaldo.Resultado r = CalculoAguinaldo.calcular(
                                salarioMensual, diasAguinaldo, diasTrab, diasEnAnio, umaUsada, datos.tarifas);
                        String resultado =
                                "📅 Días trabajados: " + diasTrab + " de " + diasEnAnio + "\n\n"
                                + "💰 Aguinaldo bruto: $" + dinero(r.bruto) + "\n"
                                + "🟩 Parte exenta: $" + dinero(r.exento) + "\n"
                                + "🟥 Parte gravada: $" + dinero(r.gravado) + "\n"
                                + "ISR estimado a retener: $" + dinero(r.isr) + "\n\n"
                                + "✅ Aguinaldo neto estimado: $" + dinero(r.neto) + "\n\n"
                                + "UMA diaria aplicada: $" + dinero(umaUsada)
                                + "  ·  Ejercicio ISR: " + ejercicioFiscal;
                        runOnUiThread(() -> {
                            tvResultados.setText(resultado);
                            restaurarBotonCalcular();
                            mostrarResultados(resumen);
                            // Se cuenta el cálculo ahora; la API se intenta después,
                            // únicamente si la calculadora sigue en primer plano.
                            SolicitudCalificacion.registrarCalculoExitoso(MainActivity.this);
                        });
                    } catch (Exception ex) {
                        FirebaseCrashlytics.getInstance().recordException(ex);
                        runOnUiThread(() -> {
                            restaurarBotonCalcular();
                            Toast.makeText(MainActivity.this, "Datos fiscales inválidos: no se calculó ISR", Toast.LENGTH_LONG).show();
                        });
                    }
                }
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        restaurarBotonCalcular();
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Tarifa fiscal no disponible")
                                .setMessage(error)
                                .setPositiveButton("Entendido", null).show();
                    });
                }
            });
        } catch (Exception ex) {
            restaurarBotonCalcular();
            FirebaseCrashlytics.getInstance().recordException(ex);
            Toast.makeText(this, "Completa correctamente las fechas, salario y días", Toast.LENGTH_LONG).show();
        }
    }

    private String dinero(double valor) {
        return String.format(new Locale("es", "MX"), "%,.2f", valor);
    }

    private void restaurarBotonCalcular() {
        btnCalcular.setEnabled(true);
        btnCalcular.setText("Calcular Aguinaldo");
    }
}
