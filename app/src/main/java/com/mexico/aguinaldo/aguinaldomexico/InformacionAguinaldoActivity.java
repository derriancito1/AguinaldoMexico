package com.mexico.aguinaldo.aguinaldomexico;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Pantalla principal de la guía y base visual compartida por las páginas independientes.
 * Cada opción del menú abre su propia Activity: no se hace scroll hacia una sección.
 */
public class InformacionAguinaldoActivity extends AppCompatActivity {
    public static final String EXTRA_EJERCICIO = "ejercicio";

    private static final int VERDE = Color.rgb(46, 125, 50);
    private static final int VERDE_OSCURO = Color.rgb(27, 94, 32);
    private static final int TEXTO = Color.rgb(38, 54, 48);

    private LinearLayout contenedor;
    private ISRManager isrManager;
    private TextView tvValores;
    private int ejercicioSeleccionado;

    /** Las clases hijas reemplazan este identificador para construir una página propia. */
    protected String pantalla() { return "guia"; }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_informacion_aguinaldo);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat barras = WindowCompat.getInsetsController(
                getWindow(), getWindow().getDecorView());
        barras.setAppearanceLightStatusBars(true);
        barras.setAppearanceLightNavigationBars(true);
        View root = findViewById(R.id.infoRoot);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);

        contenedor = findViewById(R.id.contenidoInformacion);
        isrManager = new ISRManager(this);
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        int solicitado = getIntent().getIntExtra(EXTRA_EJERCICIO, anioActual);
        ejercicioSeleccionado = solicitado >= 2025 && solicitado <= 2100 ? solicitado : anioActual;

        ImageButton volver = findViewById(R.id.btnVolverInfo);
        volver.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        DrawerLayout drawer = findViewById(R.id.drawerLayout);
        MenuNavegacion.instalar(this, drawer, findViewById(R.id.navigationView));
        findViewById(R.id.btnAbrirNavegacion)
                .setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        TextView tituloPantalla = findViewById(R.id.tituloInformacion);
        tituloPantalla.setText(titulo());
        construirContenido();
    }

    /** Título propio de la actividad, también usado por accesibilidad. */
    private String titulo() {
        switch (pantalla()) {
            case "calculo": return "Cómo se calcula";
            case "derechos": return "Derechos laborales";
            case "fiscal": return "ISR y UMA";
            case "valores": return "Valores fiscales por año";
            case "fuentes": return "Fuentes oficiales";
            default: return "Guía del aguinaldo";
        }
    }

    private void construirContenido() {
        switch (pantalla()) {
            case "calculo":
                agregarTarjeta("Cálculo del aguinaldo",
                        "1. Salario diario = salario mensual bruto ÷ 30.\n\n" +
                        "2. Aguinaldo del año completo = salario diario × días de aguinaldo pactados (mínimo 15 para quienes se rigen por la LFT).\n\n" +
                        "3. Aguinaldo proporcional = aguinaldo del año completo × días trabajados dentro del año ÷ 365 (o 366 si es bisiesto).\n\n" +
                        "Ejemplo: salario mensual de $12,000, 15 días de aguinaldo y año completo:\n" +
                        "$12,000 ÷ 30 × 15 = $6,000 brutos.\n\n" +
                        "La calculadora cuenta desde la fecha de ingreso (o el 1 de enero, si ingresaste antes) hasta la fecha única de fin y pago, ambas inclusive. Para salarios variables u otros esquemas pueden aplicar reglas específicas.");
                agregarTarjeta("Interpretación del resultado",
                        "Aguinaldo bruto: importe antes de impuestos.\n\n" +
                        "Parte exenta: importe que no se considera para la retención estimada de ISR bajo los supuestos de la calculadora.\n\n" +
                        "Parte gravada: bruto menos exento.\n\n" +
                        "Aguinaldo neto estimado: bruto menos retención ISR estimada.");
                break;
            case "derechos":
                agregarTarjeta("Tus derechos laborales",
                        "• Para relaciones laborales sujetas a la Ley Federal del Trabajo, el aguinaldo mínimo es de 15 días de salario por un año completo de servicios.\n\n" +
                        "• Debe pagarse antes del 20 de diciembre. Si termina la relación laboral, corresponde la parte proporcional al terminarla.\n\n" +
                        "• Quien no trabajó todo el año también tiene derecho al pago proporcional.\n\n" +
                        "• Si no se paga o se entrega incompleto, puedes solicitar orientación gratuita a PROFEDET. El plazo para reclamar es generalmente un año desde que sea exigible.\n\n" +
                        "• El contrato y otras condiciones aplicables pueden otorgar más de 15 días.");
                agregarTarjeta("Preguntas frecuentes",
                        "¿Renuncié o terminó mi contrato: me toca aguinaldo?\nSí, corresponde la parte proporcional del tiempo trabajado, que debe incluirse en el finiquito.\n\n" +
                        "¿El pago puede ser en vales o mercancía?\nNo. Debe pagarse en dinero, conforme a la normativa aplicable.\n\n" +
                        "¿Mi empresa puede dar más de 15 días?\nSí. Revisa tu contrato o las prestaciones del centro de trabajo.\n\n" +
                        "¿Qué hago si no lo recibí?\nPuedes pedir asesoría a PROFEDET o acudir a la autoridad laboral competente.");
                break;
            case "fiscal":
                agregarTarjeta("ISR, parte exenta y aguinaldo neto",
                        "• El aguinaldo bruto es el importe antes de impuestos.\n\n" +
                        "• Como referencia general, la exención puede ser de hasta 30 UMA diarias vigentes a la fecha de pago; se deben cumplir los requisitos fiscales y considerar si la exención anual sigue disponible.\n\n" +
                        "• Parte exenta = el menor entre aguinaldo bruto y exención disponible.\n\n" +
                        "• Parte gravada = aguinaldo bruto − parte exenta.\n\n" +
                        "• ISR estimado = retención sobre la parte gravada, considerando el salario mensual y la tarifa del año de pago.\n\n" +
                        "• Aguinaldo neto estimado = aguinaldo bruto − ISR estimado.\n\n" +
                        "El cálculo utiliza un procedimiento de estimación para gratificaciones. La retención de nómina puede variar por el método utilizado, ajustes y otras percepciones.");
                agregarTarjeta("Fechas y vigencia de la UMA",
                        "El valor de la UMA suele actualizarse a partir del 1 de febrero. Por eso, en enero puede corresponder la UMA diaria anterior.\n\n" +
                        "La app utiliza la fecha única de fin y pago para determinar qué UMA y tarifa ISR aplicar, y descarga los parámetros por ejercicio desde Firebase cuando están disponibles.");
                break;
            case "valores":
                agregarSeccionValores(anioActual());
                break;
            case "fuentes":
                agregarSeccionFuentes();
                break;
            default:
                agregarIntroduccion("Conoce el aguinaldo, los cálculos, tus derechos y las cifras oficiales del ejercicio seleccionado. Selecciona un tema para abrir su propia pantalla.");
                agregarAcceso("Cómo se calcula", "Fórmulas, proporcionalidad y ejemplo práctico", ComoCalcularActivity.class);
                agregarAcceso("Derechos laborales", "Mínimos de ley, fechas y preguntas frecuentes", DerechosLaboralesActivity.class);
                agregarAcceso("ISR y UMA", "Exención, retención y fecha aplicable", IsrUmaActivity.class);
                agregarAcceso("Valores fiscales por año", "Consulta las referencias de UMA y salarios mínimos", ValoresFiscalesActivity.class);
                agregarAcceso("Fuentes oficiales", "Enlaces a PROFEDET, SAT, INEGI y CONASAMI", FuentesOficialesActivity.class);
                break;
        }
    }

    private int anioActual() { return Calendar.getInstance().get(Calendar.YEAR); }

    private void agregarIntroduccion(String descripcion) {
        TextView sub = nuevoTexto(descripcion, 15, TEXTO);
        sub.setPadding(dp(2), dp(6), dp(2), dp(14));
        contenedor.addView(sub);
    }

    private void agregarAcceso(String titulo, String descripcion, Class<? extends AppCompatActivity> destino) {
        LinearLayout card = crearTarjeta();
        TextView encabezado = nuevoTexto(titulo + "  ›", 18, VERDE_OSCURO);
        encabezado.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(encabezado);
        TextView cuerpo = nuevoTexto(descripcion, 14, TEXTO);
        cuerpo.setPadding(0, dp(8), 0, 0);
        card.addView(cuerpo);
        card.setClickable(true);
        card.setFocusable(true);
        card.setContentDescription(titulo + ". " + descripcion);
        card.setOnClickListener(v -> MenuNavegacion.abrirPantalla(this, destino, ejercicioSeleccionado));
        contenedor.addView(card);
    }

    private void agregarTarjeta(String titulo, String contenido) {
        LinearLayout card = crearTarjeta();
        TextView encabezado = nuevoTexto(titulo, 18, VERDE_OSCURO);
        encabezado.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(encabezado);
        TextView cuerpo = nuevoTexto(contenido, 14, TEXTO);
        cuerpo.setLineSpacing(dp(4), 1f);
        cuerpo.setPadding(0, dp(10), 0, 0);
        card.addView(cuerpo);
        contenedor.addView(card);
    }

    private void agregarSeccionValores(int anioActual) {
        LinearLayout card = crearTarjeta();
        TextView encabezado = nuevoTexto("Valores fiscales por año", 18, VERDE_OSCURO);
        encabezado.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(encabezado);
        TextView descripcion = nuevoTexto("Elige el año a consultar. La UMA suele actualizarse el 1 de febrero y el salario mínimo a partir del 1 de enero.", 14, TEXTO);
        descripcion.setPadding(0, dp(8), 0, dp(8));
        card.addView(descripcion);

        List<String> anios = new ArrayList<>();
        for (int y = Math.max(2025, Math.max(anioActual, ejercicioSeleccionado)); y >= 2025; y--) {
            anios.add(Integer.toString(y));
        }
        Spinner spinner = new Spinner(this);
        spinner.setContentDescription("Seleccionar año para consultar los valores fiscales");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, anios);
        spinner.setAdapter(adapter);
        spinner.setSelection(anios.indexOf(String.valueOf(ejercicioSeleccionado)));
        card.addView(spinner);

        tvValores = nuevoTexto("Consultando valores fiscales…", 15, TEXTO);
        tvValores.setLineSpacing(dp(5), 1f);
        tvValores.setPadding(0, dp(12), 0, dp(4));
        card.addView(tvValores);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                consultarEjercicio(Integer.parseInt(anios.get(position)));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        contenedor.addView(card);
    }

    private void consultarEjercicio(int ejercicio) {
        ejercicioSeleccionado = ejercicio;
        tvValores.setText("Consultando datos de " + ejercicio + "…");
        isrManager.precargar(ejercicio, new ISRManager.ISRCallback() {
            @Override public void onDatosObtenidos(DatosFiscales datos, String origen) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed() || ejercicioSeleccionado != datos.ejercicio) return;
                    double umaDiciembre = datos.umaParaFecha(datos.ejercicio + "-12-31");
                    double umaEnero = datos.umaParaFecha(datos.ejercicio + "-01-15");
                    tvValores.setText("Ejercicio " + datos.ejercicio + "\n\n"
                            + "UMA diaria (enero): $" + moneda(umaEnero) + "\n"
                            + "UMA diaria (febrero a diciembre): $" + moneda(umaDiciembre) + "\n"
                            + "Referencia de exención, 30 UMA (feb–dic): $" + moneda(umaDiciembre * 30) + "\n\n"
                            + "SALARIO MÍNIMO GENERAL\n"
                            + "Diario: $" + moneda(datos.salarioMinimoGeneralDiario) + "\n"
                            + "Mensual de referencia: $" + moneda(datos.salarioMinimoGeneralMensual) + "\n\n"
                            + "ZONA LIBRE DE LA FRONTERA NORTE\n"
                            + "Diario: $" + moneda(datos.salarioMinimoFronteraNorteDiario) + "\n"
                            + "Mensual de referencia: $" + moneda(datos.salarioMinimoFronteraNorteMensual) + "\n\n"
                            + "Los salarios mínimos son referencias generales; algunos oficios tienen mínimos profesionales.");
                });
            }
            @Override public void onError(String error) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed() || ejercicioSeleccionado != ejercicio) return;
                    tvValores.setText("Todavía no hay datos fiscales completos para " + ejercicio
                            + ". No se mostrarán valores estimados como oficiales.");
                });
            }
        });
    }

    private void agregarSeccionFuentes() {
        LinearLayout card = crearTarjeta();
        TextView encabezado = nuevoTexto("Fuentes oficiales y orientación", 18, VERDE_OSCURO);
        encabezado.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(encabezado);
        TextView intro = nuevoTexto("Consulta la información laboral y fiscal directamente con las autoridades.", 14, TEXTO);
        intro.setPadding(0, dp(8), 0, dp(8));
        card.addView(intro);
        agregarEnlace(card, "PROFEDET · Aguinaldo y asesoría", "https://www.profedet.gob.mx/micrositio/index.php/aguinaldo");
        agregarEnlace(card, "Ley Federal del Trabajo · Artículo 87", "https://www.diputados.gob.mx/LeyesBiblio/pdf/LFT.pdf");
        agregarEnlace(card, "INEGI · Valores de UMA", "https://www.inegi.org.mx/temas/uma/");
        agregarEnlace(card, "CONASAMI · Salarios mínimos", "https://www.gob.mx/conasami/documentos/consulta-de-salarios-minimos");
        agregarEnlace(card, "SAT · Beneficios fiscales del salario", "https://wwwmat.sat.gob.mx/consulta/02330/conoce-los-beneficios-del-regimen-de-salarios");
        TextView nota = nuevoTexto("Información general para relaciones laborales sujetas a la LFT. Para casos particulares consulta tu recibo de nómina o solicita asesoría a PROFEDET.", 13, TEXTO);
        nota.setPadding(0, dp(12), 0, 0);
        card.addView(nota);
        contenedor.addView(card);
    }

    private void agregarEnlace(LinearLayout destino, String titulo, String url) {
        TextView boton = nuevoTexto(titulo + "  ↗", 14, VERDE);
        boton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        boton.setPadding(dp(6), dp(12), dp(6), dp(12));
        boton.setBackgroundColor(Color.rgb(245, 250, 247));
        boton.setClickable(true);
        boton.setFocusable(true);
        boton.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (Exception ex) {
                Toast.makeText(this, "No hay una aplicación disponible para abrir el enlace", Toast.LENGTH_LONG).show();
            }
        });
        destino.addView(boton);
    }

    private LinearLayout crearTarjeta() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackgroundResource(R.drawable.info_card_background);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(12);
        card.setLayoutParams(lp);
        return card;
    }

    private TextView nuevoTexto(String valor, float tamano, int color) {
        TextView tv = new TextView(this);
        tv.setText(valor);
        tv.setTextSize(tamano);
        tv.setTextColor(color);
        tv.setGravity(Gravity.START);
        return tv;
    }

    private String moneda(double importe) {
        return String.format(new Locale("es", "MX"), "%,.2f", importe);
    }

    private int dp(float valor) {
        return Math.round(getResources().getDisplayMetrics().density * valor);
    }
}
