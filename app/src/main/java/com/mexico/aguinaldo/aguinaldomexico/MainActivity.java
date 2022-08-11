package com.mexico.aguinaldo.aguinaldomexico;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button calcular;
    private ImageView fechaInicio;
    private EditText editTextFechaInicio, editTextSueldo,editTextAguinaldo,editTextFechaFin;
    private TextView textView;
    private int day, month, year, dias;
    private int sueldo,aguinaldo;
    private Date fechaInicial, fechaFinal;
    private double aguinaldoAnual, aguinaldoProporcional;
    private AdView mAdView;
    private String regexp;
    private AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-9001957420901694~9538615966");

        /*MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");*/ // PRUEBAS


        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        fechaInicio=(ImageView) findViewById(R.id.imageView);
        calcular=(Button)findViewById(R.id.calcular);
        editTextFechaInicio=(EditText)findViewById(R.id.editTextFechaInicio);
        editTextFechaFin=(EditText)findViewById(R.id.editTextFechaFin);
        editTextSueldo=(EditText)findViewById(R.id.editTextSueldo);
        editTextAguinaldo=(EditText) findViewById(R.id.editTextAguinaldo);

        textView=(TextView) findViewById(R.id.textView);

        fechaInicio.setOnClickListener(this);
        calcular.setOnClickListener(this);

        fechaFinal=null;
        fechaInicial=null;


        regexp = "\\d{4}/\\d{1,2}/\\d{1,2}";


        final Calendar c= Calendar.getInstance();
        year=c.get(Calendar.YEAR);

        editTextFechaFin.setText(year+"/12/31");



    }

    @Override
    public void onClick(View view) {
        if (view==fechaInicio){
            final Calendar c= Calendar.getInstance();
            day=c.get(Calendar.DAY_OF_MONTH);
            month=c.get(Calendar.MONTH);
            year=c.get(Calendar.YEAR);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    String monthCalculo="";
                    String dayCalculo="";

                    monthCalculo = (i1+1)>=10 ? ""+(i1+1) : "0"+(i1+1);
                    dayCalculo = i2>=10 ? ""+i2 : "0"+i2;

                    editTextFechaInicio.setText(i+"/"+monthCalculo+"/"+dayCalculo);
                    fechaInicial = new Date((i-1900),i1,i2);
                    /*fechaFinal = new Date(String.valueOf(Calendar.getInstance().getTime()));*/
                    fechaFinal = new Date(editTextFechaFin.getText().toString());
                }
            }, day, month, year);
            datePickerDialog.show();
        }else if (view==calcular){

            /*if (fechaInicial==null || fechaFinal==null){*/
            if (!Pattern.matches(regexp, editTextFechaInicio.getText().toString())){
                Toast.makeText(MainActivity.this,"Favor de seleccionar fecha de inicio valida en formato AAAA/MM/DD", Toast.LENGTH_LONG).show();
                return;
            }else if (!Pattern.matches(regexp, editTextFechaFin.getText().toString())){
                Toast.makeText(MainActivity.this,"Favor de seleccionar fecha de fin valida en formato AAAA/MM/DD", Toast.LENGTH_LONG).show();
                return;
            }else{
                fechaInicial = new Date(editTextFechaInicio.getText().toString());
                fechaFinal = new Date(editTextFechaFin.getText().toString());
            }

            if ( fechaFinal.before(fechaInicial)){
                Toast.makeText(MainActivity.this,"Ingresar una fecha menor a la de hoy", Toast.LENGTH_LONG).show();
            }else if (editTextSueldo.getEditableText().toString().isEmpty()){
                Toast.makeText(MainActivity.this,"Favor de llenar el campo de sueldo", Toast.LENGTH_LONG).show();
            }else if (editTextAguinaldo.getEditableText().toString().isEmpty()){
                Toast.makeText(MainActivity.this,"Favor de llenar el campo de días de aguinaldo", Toast.LENGTH_LONG).show();
            }else {
                calcularAguinaldo();
            }

        }
    }


    public void calcularAguinaldo(){
         sueldo = Integer.parseInt(editTextSueldo.getEditableText().toString());
         aguinaldo = Integer.parseInt(editTextAguinaldo.getEditableText().toString());
         dias=(int) ((fechaFinal.getTime()-fechaInicial.getTime())/86400000);
         DecimalFormat formato = new DecimalFormat("#.00");

         if (dias >=365 ){
            aguinaldoAnual = ((sueldo/30)*(aguinaldo));
            textView.setText("Te corresponden $"+formato.format(aguinaldoAnual)+" de Aguinaldo por 365 dias de trabajo");
            valorame();

         }else{
            aguinaldoAnual = ((sueldo/30)*(aguinaldo));
            aguinaldoProporcional =((aguinaldoAnual/365)*dias);
            textView.setText("Te corresponden $"+formato.format(aguinaldoProporcional)+" de Aguinaldo por "+dias+" días de trabajo");
            valorame();
        }
    }


    public void valorame(){
        try {
            Thread.sleep(5000);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Valora nuestra App? Tu opinión nos importa: ¡Dinos lo que piensas!")
                    .setCancelable(false)
                    .setPositiveButton("Ok, ir a Google Play", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.mexico.aguinaldo.aguinaldomexico") ) );
                        }
                    })
                    .setNegativeButton("Ahora no", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            alert = builder.create();
            alert.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }



}


