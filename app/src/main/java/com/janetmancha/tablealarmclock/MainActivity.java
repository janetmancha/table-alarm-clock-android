package com.janetmancha.tablealarmclock;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {



    //Elementos UI
    public TextView textViewHour;
    private TextView textViewMinutes;
    private TextView textViewColon;
    private TextView textViewDate;
    private TextView textViewAMPM;
    private ImageView imageViewPlug;
    private ImageView imageViewPadlock;


    Timer timer = new Timer();
    Timer timerIcon = new Timer();
    SimpleDateFormat timeFormat;

    boolean points = false;
    boolean padlockClosed = true;
    int formatHour = 24;
    String am_pm;




//    public Handler timeHandler = new Handler() {
//        public void handleMessage(Message msg) {
//            final Date date = new Date();
//            final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
//            final String timeString = timeFormat.format(date);
//            textViewTime = (TextView) findViewById(R.id.textViewTime);
//            textViewTime.setText(timeString);
//        }
//    };

    public Handler timeHandler = new Handler() {
        public void handleMessage(Message msg) {

            final Date date = new Date();
            timeFormat = new SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy");
            //timeFormat = new SimpleDateFormat("HH mm ss");
           // timeFormat = new SimpleDateFormat("HH:mm:ss");

            //final String timeString = timeFormat.format(date);

            Calendar calendar = Calendar.getInstance();
            int hours;
            if (formatHour == 24) {
               hours= calendar.get(Calendar.HOUR_OF_DAY);

            } else {
                //calendar.set(Calendar.AM_PM, Calendar.PM);
               hours = calendar.get(Calendar.HOUR);
               if (hours == 00) {
                   hours = 12;
               }
            }

            //int hours = calendar.get(Calendar.HOUR_OF_DAY); // formato 24 horas, .HOUR seria formato 12 horas
            int minutes = calendar.get(Calendar.MINUTE);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int dayWeek= calendar.get(calendar.DAY_OF_WEEK);

            am_pm =  (Calendar.AM_PM == 9) ? "PM" :  "AM"; // si devuelve 9 es PM
//            if (Calendar.AM_PM == 9) { // si devuelve 9 es PM
//                am_pm ="PM";
//            }else {
//                am_pm = "AM";
//            }
//


//            if (points == false){
//                    timeFormat = new SimpleDateFormat("HH:mm:ss");
//                    points = true;
//                } else {
//                    timeFormat = new SimpleDateFormat("HH mm ss");
//                    points = false;
//            }
//            final String timeString = timeFormat.format(date);

//            textViewHour =(TextView) findViewById(R.id.textViewHour);
//            textViewMinutes = (TextView) findViewById(R.id.textViewMinutes);
//            textViewColon = (TextView) findViewById(R.id.textViewColon);
//            textViewDate = (TextView) findViewById(R.id.textViewDate);
//            imageViewPlug = (ImageView) findViewById(R.id.imageViewPlug);

//            textViewHour.setText(date.getHours()+ "");
//            textViewMinutes.setText(date.getMinutes()+ "");
//            textViewSeconds.setText(date.getSeconds()+ "");

            textViewHour.setText(FormatTwoDigits(hours));
            textViewMinutes.setText(FormatTwoDigits(minutes));
            textViewDate.setText(timeFormat.format(calendar.getTime()));
            textViewAMPM.setText(am_pm);

            if (points == false){
                textViewColon.setVisibility(TextView.VISIBLE);
                points = true;
            } else {
                textViewColon.setVisibility(TextView.INVISIBLE);
                points = false;
            }

            //textViewTime = (TextView) findViewById(R.id.textViewTime);
            //textViewTime.setText(timeString);
            //textViewTime.setText(date.getSeconds() + "");

        }
    };



    public Handler timeHandlerIcon = new Handler() {
        public void handleMessage(Message msg) {

            //saber si esta enchufado a la red por usb o un gargador de corriente alterna
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = getBaseContext().registerReceiver(null, ifilter);
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            if (usbCharge == false && acCharge == false){
                if (imageViewPlug.getVisibility() == View.VISIBLE ){
                    imageViewPlug.setVisibility(View.INVISIBLE);
                } else {
                    imageViewPlug.setVisibility(View.VISIBLE);
                }

            } else {
                imageViewPlug.setVisibility(View.VISIBLE);
            }
        }};


    public void onPause() {
        super.onPause();  // Always call the superclass method first
        timer.cancel();
        timerIcon.cancel();
    }

    public void onResume() {
        super.onResume();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeHandler.obtainMessage(1).sendToTarget();
            }
        }, 0, 1000);

        timerIcon = new Timer ();
        timerIcon.schedule(new TimerTask() {
            @Override
            public void run() {
                timeHandlerIcon.obtainMessage(1).sendToTarget();
            }
        }, 0, 500);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // orientacion horizontal
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        textViewHour =(TextView) findViewById(R.id.textViewHour);
        textViewMinutes = (TextView) findViewById(R.id.textViewMinutes);
        textViewColon = (TextView) findViewById(R.id.textViewColon);
        textViewDate = (TextView) findViewById(R.id.textViewDate);
        imageViewPlug = (ImageView) findViewById(R.id.imageViewPlug);
        imageViewPadlock = (ImageView) findViewById(R.id.imageViewPadlock);
        textViewAMPM = (TextView) findViewById(R.id.textViewAMPM);


        textViewHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (padlockClosed == false) { //candado esta abierto
                    if (formatHour == 12) {
                        formatHour = 24;
                        textViewAMPM.setVisibility(View.INVISIBLE);
                    } else {
                        formatHour = 12;
                        textViewAMPM.setVisibility(View.VISIBLE);
                    }
                }
                timeHandler.obtainMessage(1).sendToTarget();
            }
        });


        imageViewPadlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (padlockClosed == true) { //candato cerrado
                    imageViewPadlock.setImageResource(R.mipmap.ic_padlock_open_foreground);
                    padlockClosed = false;

                    textViewHour.setClickable(true);

                } else { //candado abierto
                    imageViewPadlock.setImageResource(R.mipmap.ic_padlock_block_foreground);
                    padlockClosed = true;

                    textViewHour.setClickable(false);
                }
            }
        });


    }


    //funcion para poner dos digitos en la hora, minutos
    public String FormatTwoDigits (int num) {
//        if (num >=0 && num<=9){
//            return "0"+num;
//        }
//        return num + "";

        return (num >=0 && num<=9) ? "0" + num : num + "";

    }

}
