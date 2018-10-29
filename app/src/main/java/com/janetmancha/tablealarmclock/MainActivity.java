package com.janetmancha.tablealarmclock;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {



    //Elementos UI
    private ConstraintLayout constraintLayout;
    private TextView textViewHour;
    private TextView textViewMinutes;
    private TextView textViewColon;
    private TextView textViewDate;
    private TextView textViewAMPM;
    private TextView textViewAlarm1Hour;
    private TextView textViewAlarm2Hour;
    private TextView textViewAlarm1Minutes;
    private TextView textViewAlarm2Minutes;
    private TextView textViewAlarm1Format;
    private TextView textViewAlarm2Format;

    private ImageView imageViewPlug;
    private ImageView imageViewPadlock;
    private ImageView imageViewAlarm1;
    private ImageView imageViewAlarm2;
    private ImageView imageViewIncrease;
    private ImageView imageViewDecrease;
    private ImageView imageViewOk;



    Timer timer = new Timer();
    Timer timerIcon = new Timer();
    SimpleDateFormat timeFormat;

    boolean points = false;
    boolean padlockClosed = true;
    boolean alarm1Activate;
    boolean alarm2Activate;

    int formatHour = 24;
    int am_pm;


    TextView textViewAlarmEdit;
    TextView textViewAlarmFormatEdit;

    private SharedPreferences prefs;


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
            //int hours;
//            hours= calendar.get(Calendar.HOUR_OF_DAY);

//            if (formatHour == 24) {
//               hours= calendar.get(Calendar.HOUR_OF_DAY);
//
//            } else {
//                //calendar.set(Calendar.AM_PM, Calendar.PM);
//                hours = calendar.get(Calendar.HOUR);
//               if (hours == 00) {
//                   hours = 12;
//               }
//            }


            int hours = calendar.get(Calendar.HOUR_OF_DAY); // formato 24 horas, .HOUR seria formato 12 horas
            int minutes = calendar.get(Calendar.MINUTE);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int dayWeek= calendar.get(calendar.DAY_OF_WEEK);
            am_pm = calendar.get(Calendar.AM_PM);



//            if (points == false){
//                    timeFormat = new SimpleDateFormat("HH:mm:ss");
//                    points = true;
//                } else {
//                    timeFormat = new SimpleDateFormat("HH mm ss");
//                    points = false;
//            }
//            final String timeString = timeFormat.format(date);


//            textViewHour.setText(date.getHours()+ "");
//            textViewMinutes.setText(date.getMinutes()+ "");
//            textViewSeconds.setText(date.getSeconds()+ "");


            if (formatHour == 24){
                textViewHour.setText(FormatTwoDigits(hours));
            }

            textViewMinutes.setText(FormatTwoDigits(minutes));
            textViewDate.setText(timeFormat.format(calendar.getTime()));
            if (am_pm == Calendar.AM) {
                textViewAMPM.setText("AM");
            }
            else{
                textViewAMPM.setText("PM");
            }

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


            Alarm();

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


            if (textViewAlarmEdit != null){
                if (textViewAlarmEdit.getVisibility() == View.VISIBLE){
                    textViewAlarmEdit.setVisibility(View.INVISIBLE);
                } else {
                    textViewAlarmEdit.setVisibility(View.VISIBLE);
                }
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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE); // la pantalla gira con el dispositivo pero solo a orientaciones horizontales.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewHour =(TextView) findViewById(R.id.textViewHour);
        textViewMinutes = (TextView) findViewById(R.id.textViewMinutes);
        textViewColon = (TextView) findViewById(R.id.textViewColon);
        textViewDate = (TextView) findViewById(R.id.textViewDate);
        imageViewPlug = (ImageView) findViewById(R.id.imageViewPlug);
        imageViewPadlock = (ImageView) findViewById(R.id.imageViewPadlock);
        textViewAMPM = (TextView) findViewById(R.id.textViewAMPM);
        imageViewAlarm1 = (ImageView) findViewById(R.id.imageViewAlarm1);
        imageViewAlarm2 = (ImageView) findViewById(R.id.imageViewAlarm2);
        textViewAlarm1Hour = (TextView)findViewById(R.id.textViewAlarm1Hour);
        textViewAlarm1Minutes = (TextView) findViewById(R.id.textViewAlarm1Minutes);
        textViewAlarm2Hour = (TextView) findViewById(R.id.textViewAlarm2Hour);
        textViewAlarm2Minutes = (TextView) findViewById(R.id.textViewAlarm2Minutes);
        imageViewIncrease = (ImageView) findViewById(R.id.imageViewIncrease);
        imageViewDecrease = (ImageView) findViewById(R.id.imageViewDecrease);
        imageViewOk = (ImageView) findViewById(R.id.imageViewOk);
        textViewAlarm1Format = (TextView) findViewById(R.id.textViewAlarm1Format);
        textViewAlarm2Format = (TextView) findViewById(R.id.textViewAlarm2Format);
        constraintLayout = (ConstraintLayout) findViewById(R.id.constrainLayoutClock);


        prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        textViewAlarm1Hour.setText(prefs.getString("hourAlarm1","00"));
        textViewAlarm2Hour.setText(prefs.getString("hourAlarm2","00"));
        textViewAlarm1Minutes.setText(prefs.getString("minutesAlarm1","00"));
        textViewAlarm2Minutes.setText(prefs.getString("minutesAlarm2","00"));
        textViewAlarm1Format.setText(prefs.getString("formatAlarm1",textViewAlarm1Format.getText().toString()));
        textViewAlarm2Format.setText(prefs.getString("formatAlarm2",textViewAlarm2Format.getText().toString()));

        alarm1Activate = prefs.getBoolean("alarm1Activate",false);
        if (alarm1Activate == true) {
            imageViewAlarm1.setImageResource(R.mipmap.ic_bell_on_foreground);
        } else {
            imageViewAlarm1.setImageResource(R.mipmap.ic_bell_off_foreground);
        }

        alarm2Activate = prefs.getBoolean("alarm2Activate",false);
        if (alarm2Activate == true) {
            imageViewAlarm2.setImageResource(R.mipmap.ic_bell_on_foreground);
        } else {
            imageViewAlarm2.setImageResource(R.mipmap.ic_bell_off_foreground);
        }



        textViewHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (padlockClosed == false) { //candado esta abierto
                    if (formatHour == 12) {
                        formatHour = 24;
                        //textViewAMPM.setVisibility(View.INVISIBLE);
                    } else {
                        formatHour = 12;
                        //textViewAMPM.setVisibility(View.VISIBLE);
                    }
                    ChangeFormatHours (textViewHour,textViewAMPM);
                    ChangeFormatHours (textViewAlarm1Hour,textViewAlarm1Format);
                    ChangeFormatHours (textViewAlarm2Hour,textViewAlarm2Format);
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
                } else { //candado abierto
                    imageViewPadlock.setImageResource(R.mipmap.ic_padlock_block_foreground);
                    padlockClosed = true;
                    CancelEdit();
                }
            }
        });

        imageViewAlarm1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (padlockClosed == false) { //candado esta abierto
                    if (alarm1Activate == true) {
                        imageViewAlarm1.setImageResource(R.mipmap.ic_bell_off_foreground);
                        alarm1Activate = false;
                    } else {
                        imageViewAlarm1.setImageResource(R.mipmap.ic_bell_on_foreground);
                        alarm1Activate = true;
                    }
                    editor.putBoolean("alarm1Activate",alarm1Activate);
                    editor.commit();
                }

            }
        });

        imageViewAlarm2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (padlockClosed == false) { //candado esta abierto
                    if (alarm2Activate == true) {
                        imageViewAlarm2.setImageResource(R.mipmap.ic_bell_off_foreground);
                        alarm2Activate = false;
                    } else {
                        imageViewAlarm2.setImageResource(R.mipmap.ic_bell_on_foreground);
                        alarm2Activate = true;
                    }
                    editor.putBoolean("alarm2Activate",alarm2Activate);
                    editor.commit();
                }
            }
        });

        textViewAlarm1Hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickModifiyingAlarm(textViewAlarm1Hour,textViewAlarm1Format);
            }
        });

        textViewAlarm1Minutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickModifiyingAlarm(textViewAlarm1Minutes,textViewAlarm1Format);
            }
        });

        textViewAlarm2Hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickModifiyingAlarm(textViewAlarm2Hour,textViewAlarm2Format);
            }
        });

        textViewAlarm2Minutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickModifiyingAlarm(textViewAlarm2Minutes,textViewAlarm2Format);
            }
        });

        imageViewOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editor.putString("hourAlarm1", textViewAlarm1Hour.getText().toString());
                editor.putString("minutesAlarm1",textViewAlarm1Minutes.getText().toString());
                editor.putString("hourAlarm2",textViewAlarm2Hour.getText().toString());
                editor.putString("minutesAlarm2",textViewAlarm2Minutes.getText().toString());
                editor.putString("formatAlarm1",textViewAlarm1Format.getText().toString());
                editor.putString("formatAlarm2",textViewAlarm2Format.getText().toString());
                editor.commit();
                //editor.apply();
                CancelEdit();
            }
        });

        imageViewIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmIncrease();
            }
        });

        imageViewDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmDecrease();
            }
        });

//        constraintLayout.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//            }
//        });

    }

    //funcion cancelar opcion editar alarmas
    public void CancelEdit () {
        if (textViewAlarmEdit !=null) {
            textViewAlarmEdit.setVisibility(View.VISIBLE);
            textViewAlarmEdit = null;
            imageViewIncrease.setVisibility(View.INVISIBLE);
            imageViewDecrease.setVisibility(View.INVISIBLE);
            imageViewOk.setVisibility(View.INVISIBLE);
        }
    }

    //funcion para que parpadee el textview a modificar de las alarmas
    public void ClickModifiyingAlarm (TextView textView,TextView textViewFormat) {
        if (padlockClosed == false) { //candado esta abierto,
            if (textViewAlarmEdit == null) {
                textViewAlarmEdit = textView;
                textViewAlarmFormatEdit = textViewFormat;
                imageViewIncrease.setVisibility(View.VISIBLE);
                imageViewDecrease.setVisibility(View.VISIBLE);
                imageViewOk.setVisibility(View.VISIBLE);
            } else if (textViewAlarmEdit == textView) {
                CancelEdit();
            }
        }
    }

    //funcion para poner dos digitos en la hora, minutos
    public String FormatTwoDigits (int num) {
        return (num >=0 && num<=9) ? "0" + num : num + "";
//        if (num >=0 && num<=9){
//            return "0"+num;
//        }
//        return num + "";
   }

   //funcion sumar textViews Alarmas
    public void AlarmIncrease (){
        if (textViewAlarmEdit != null){
            int num = Integer.parseInt(textViewAlarmEdit.getText().toString());
            num = num + 1;

            if (textViewAlarmEdit == textViewAlarm1Hour || textViewAlarmEdit == textViewAlarm2Hour){
                if (formatHour == 24 && num == 24){
                    num = 0;
                } else if (formatHour == 12) {
                    if (num ==13){
                        num = 1;
                    }

                    if (num == 12) {
                        if (textViewAlarmFormatEdit.getText().toString() == "PM") {
                            textViewAlarmFormatEdit.setText("AM");
                        } else {
                            textViewAlarmFormatEdit.setText("PM");
                        }
                    }
                }
            }

            if (textViewAlarmEdit == textViewAlarm1Minutes || textViewAlarmEdit == textViewAlarm2Minutes){
                if (num > 59){
                    num = 0;
                }
            }

            String resul = FormatTwoDigits(num);
            textViewAlarmEdit.setText(resul);
        }
    }


    //funcion restar textViews Alarmas
    public void AlarmDecrease (){
        if (textViewAlarmEdit != null){
            int num = Integer.parseInt(textViewAlarmEdit.getText().toString());
            num = num -1;

            if (textViewAlarmEdit == textViewAlarm1Hour || textViewAlarmEdit == textViewAlarm2Hour){
                if (formatHour == 24 && num < 0){
                    num = 23;
                } else if (formatHour == 12) {
                    if (num ==0) {
                        num = 12;
                    }
                    if (num ==11) {
                        if (textViewAlarmFormatEdit.getText().toString() == "PM" && num == 11){
                            textViewAlarmFormatEdit.setText("AM");
                        }
                        else {
                            textViewAlarmFormatEdit.setText("PM");
                        }

                    }

                }
            }

            if (textViewAlarmEdit == textViewAlarm1Minutes || textViewAlarmEdit == textViewAlarm2Minutes){
                if (num > 59){
                    num = 0;
                }
            }


            String resul = FormatTwoDigits(num);
            textViewAlarmEdit.setText(resul);
        }
    }

    //funcion cambiar formato 24 horas o 12 horas
    public void ChangeFormatHours (TextView textViewHour, TextView textViewFormat){

        int hour=0;
        String hourFinal = textViewHour.getText().toString();

        try {
            hour = Integer.parseInt(textViewHour.getText().toString());
        } catch(NumberFormatException nfe) {

        }

        if (formatHour == 24){


            if (textViewFormat.getText().toString() == "PM") {
                if (hour == 1){ hourFinal = "13";}
                if (hour == 2){ hourFinal = "14";}
                if (hour == 3){ hourFinal = "15";}
                if (hour == 4){ hourFinal = "16";}
                if (hour == 5){ hourFinal = "17";}
                if (hour == 6){ hourFinal = "18";}
                if (hour == 7){ hourFinal = "19";}
                if (hour == 8){ hourFinal = "20";}
                if (hour == 9){ hourFinal = "21";}
                if (hour == 10){ hourFinal = "22";}
                if (hour == 11){ hourFinal = "23";}
                if (hour == 12){ hourFinal = "00";}
            }
            if (hour == 12 && textViewFormat.getText().toString() == "AM") {hourFinal = "00"; }

            textViewFormat.setVisibility(View.INVISIBLE);

        } else { // formato 12 horas

            if (hour == 0){ hourFinal = "12";textViewFormat.setText("AM");}
            if (hour == 13){ hourFinal = "01";textViewFormat.setText("PM");}
            if (hour == 14){ hourFinal = "02";textViewFormat.setText("PM");}
            if (hour == 15){ hourFinal = "03";textViewFormat.setText("PM");}
            if (hour == 16){ hourFinal = "04";textViewFormat.setText("PM");}
            if (hour == 17){ hourFinal = "05";textViewFormat.setText("PM");}
            if (hour == 18){ hourFinal = "06";textViewFormat.setText("PM");}
            if (hour == 19){ hourFinal = "07";textViewFormat.setText("PM");}
            if (hour == 20){ hourFinal = "08";textViewFormat.setText("PM");}
            if (hour == 21){ hourFinal = "09";textViewFormat.setText("PM");}
            if (hour == 22){ hourFinal = "10";textViewFormat.setText("PM");}
            if (hour == 23){ hourFinal = "11";textViewFormat.setText("PM");}

            textViewFormat.setVisibility(View.VISIBLE);
        }
        textViewHour.setText(hourFinal);

    }

    public void Alarm () {

        if (textViewAlarm1Hour.getText().toString().equals(textViewHour.getText().toString())
                && textViewAlarm1Minutes.getText().toString().equals(textViewMinutes.getText().toString())
                && alarm1Activate == true) {
            Toast.makeText(getBaseContext(),"Sonando alarma 1", Toast.LENGTH_SHORT).show();
        }

        if (textViewAlarm2Hour.getText().toString().equals(textViewHour.getText().toString())
                && textViewAlarm2Minutes.getText().toString().equals(textViewMinutes.getText().toString())
                && alarm2Activate == true) {
            Toast.makeText(getBaseContext(),"Sonando alarma 2", Toast.LENGTH_SHORT).show();
        }

    }
}
