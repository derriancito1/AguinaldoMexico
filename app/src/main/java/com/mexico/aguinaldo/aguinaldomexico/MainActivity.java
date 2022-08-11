package com.mexico.aguinaldo.aguinaldomexico;

import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button fechaInicio, calcular;
    private EditText editTextFechaInicio, editTextSueldo,editTextAguinaldo;
    private TextView textView;
    private int day, month, year, dias;
    private int sueldo,aguinaldo;
    private Date fechaInicial, fechaFinal;
    private double aguinaldoAnual, aguinaldoProporcional;
    private AdView mAdView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-9001957420901694~2109582463");




        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        fechaInicio=(Button)findViewById(R.id.fechaInicio);
        calcular=(Button)findViewById(R.id.calcular);
        editTextFechaInicio=(EditText)findViewById(R.id.editTextFechaInicio);
        editTextSueldo=(EditText)findViewById(R.id.editTextSueldo);
        editTextAguinaldo=(EditText) findViewById(R.id.editTextAguinaldo);

        textView=(TextView) findViewById(R.id.textView);

        fechaInicio.setOnClickListener(this);
        calcular.setOnClickListener(this);

        fechaFinal=null;
        fechaInicial=null;


        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
            }
        });



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
                    fechaFinal = new Date(String.valueOf(Calendar.getInstance().getTime()));
                }
            }, day, month, year);
            datePickerDialog.show();
        }else if (view==calcular){
            if (fechaInicial==null || fechaFinal==null){
                Toast.makeText(MainActivity.this,"Favor de seleccionar fecha", Toast.LENGTH_LONG).show();
            }else if ( fechaFinal.before(fechaInicial)){
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

         }else{
            aguinaldoAnual = ((sueldo/30)*(aguinaldo));
            aguinaldoProporcional =((aguinaldoAnual/365)*dias);
            textView.setText("Te corresponden $"+formato.format(aguinaldoProporcional)+" de Aguinaldo por "+dias+" días de trabajo");
        }
    }



}


