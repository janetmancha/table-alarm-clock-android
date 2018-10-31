package com.janetmancha.tablealarmclock;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
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
    private TextView textViewAlarm1Hour;
    private TextView textViewAlarm2Hour;
    private TextView textViewAlarm1Minutes;
    private TextView textViewAlarm2Minutes;

    private ImageView imageViewPlug;
    private ImageView imageViewPadlock;
    private ImageView imageViewAlarm1;
    private ImageView imageViewAlarm2;
    private ImageView imageViewIncrease;
    private ImageView imageViewDecrease;
    private ImageView imageViewOk;
    private ImageView imageViewSnooze;
    private ImageView imageViewSound;


    Timer timer = new Timer();
    Timer timerIcon = new Timer();
    SimpleDateFormat timeFormat;

    boolean points = false;
    boolean padlockClosed = true;
    boolean alarm1Activate;
    boolean alarm2Activate;

    Boolean alarm = false;

    TextView textViewAlarmEdit;

    MediaPlayer player;

    String snoozeAlarmHour;
    String snoozeAlarmMinutes;

    private SharedPreferences prefs;
    SharedPreferences.Editor editor;

    Uri currentTone;



//            = getSharedPreferences("Preferences", Context.MODE_PRIVATE);;
//    final SharedPreferences.Editor editor = prefs.edit();



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
            Calendar calendar = Calendar.getInstance();


            int hours = calendar.get(Calendar.HOUR_OF_DAY); // formato 24 horas, .HOUR seria formato 12 horas
            int minutes = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int dayWeek= calendar.get(calendar.DAY_OF_WEEK);


            textViewHour.setText(FormatTwoDigits(hours));
            textViewMinutes.setText(FormatTwoDigits(minutes));
            textViewDate.setText(timeFormat.format(calendar.getTime()));

            if (points == false){
                textViewColon.setVisibility(TextView.VISIBLE);
                points = true;
            } else {
                textViewColon.setVisibility(TextView.INVISIBLE);
                points = false;
            }


            if (second == 0 && (
                IsAlarmNow(textViewAlarm1Hour.getText().toString(),textViewAlarm1Minutes.getText().toString(),alarm1Activate) ||
                IsAlarmNow(textViewAlarm2Hour.getText().toString(),textViewAlarm2Minutes.getText().toString(),alarm2Activate) ||
                IsAlarmNow(snoozeAlarmHour,snoozeAlarmMinutes,true)
                )) {
                showAlertDialog();
            }

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //mantener la pantalla siempre encendida

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
        //ocultar barra de notificaciones versiones 4.1 y superiores para inferiores cambiado style en manifest
        if (Build.VERSION.SDK_INT > 16) { getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); }
        setContentView(R.layout.activity_main);

        textViewHour =(TextView) findViewById(R.id.textViewHour);
        textViewMinutes = (TextView) findViewById(R.id.textViewMinutes);
        textViewColon = (TextView) findViewById(R.id.textViewColon);
        textViewDate = (TextView) findViewById(R.id.textViewDate);
        imageViewPlug = (ImageView) findViewById(R.id.imageViewPlug);
        imageViewPadlock = (ImageView) findViewById(R.id.imageViewPadlock);
        imageViewAlarm1 = (ImageView) findViewById(R.id.imageViewAlarm1);
        imageViewAlarm2 = (ImageView) findViewById(R.id.imageViewAlarm2);
        textViewAlarm1Hour = (TextView)findViewById(R.id.textViewAlarm1Hour);
        textViewAlarm1Minutes = (TextView) findViewById(R.id.textViewAlarm1Minutes);
        textViewAlarm2Hour = (TextView) findViewById(R.id.textViewAlarm2Hour);
        textViewAlarm2Minutes = (TextView) findViewById(R.id.textViewAlarm2Minutes);
        imageViewIncrease = (ImageView) findViewById(R.id.imageViewIncrease);
        imageViewDecrease = (ImageView) findViewById(R.id.imageViewDecrease);
        imageViewOk = (ImageView) findViewById(R.id.imageViewOk);
        imageViewSnooze = (ImageView) findViewById(R.id.imageViewSnooze);
        imageViewSound = (ImageView) findViewById(R.id.imageViewSound);

        constraintLayout = (ConstraintLayout) findViewById(R.id.constrainLayoutClock);


//        prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
//        final SharedPreferences.Editor editor = prefs.edit();

        prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        editor = prefs.edit();

        currentTone = RingtoneManager.getActualDefaultRingtoneUri(MainActivity.this, RingtoneManager.TYPE_ALARM);
        Log.d("german0",currentTone.toString());

        textViewAlarm1Hour.setText(prefs.getString("hourAlarm1","00"));
        textViewAlarm2Hour.setText(prefs.getString("hourAlarm2","00"));
        textViewAlarm1Minutes.setText(prefs.getString("minutesAlarm1","00"));
        textViewAlarm2Minutes.setText(prefs.getString("minutesAlarm2","00"));

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
                ClickModifiyingAlarm(textViewAlarm1Hour);
            }
        });

        textViewAlarm1Minutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickModifiyingAlarm(textViewAlarm1Minutes);
            }
        });

        textViewAlarm2Hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickModifiyingAlarm(textViewAlarm2Hour);
            }
        });

        textViewAlarm2Minutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickModifiyingAlarm(textViewAlarm2Minutes);
            }
        });

        imageViewOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                editor.putString("hourAlarm1", textViewAlarm1Hour.getText().toString());
//                editor.putString("minutesAlarm1",textViewAlarm1Minutes.getText().toString());
//                editor.putString("hourAlarm2",textViewAlarm2Hour.getText().toString());
//                editor.putString("minutesAlarm2",textViewAlarm2Minutes.getText().toString());
//                editor.commit();
//                //editor.apply();
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

        imageViewSnooze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.stop();
                snoozeAlarmHour = null;
                snoozeAlarmMinutes = null;
                imageViewSnooze.setVisibility(View.INVISIBLE);
            }
        });

        imageViewSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (padlockClosed == false) { //candado esta abierto
                    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                    startActivityForResult(intent, 0);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        currentTone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
    }

    //*****************************FUNCIONES******************************************

    //funcion cancelar opcion editar alarmas
    public void CancelEdit () {
        if (textViewAlarmEdit !=null) {
            textViewAlarmEdit.setVisibility(View.VISIBLE);
            textViewAlarmEdit = null;
            imageViewIncrease.setVisibility(View.INVISIBLE);
            imageViewDecrease.setVisibility(View.INVISIBLE);
            imageViewOk.setVisibility(View.INVISIBLE);


            editor.putString("hourAlarm1", textViewAlarm1Hour.getText().toString());
            editor.putString("minutesAlarm1",textViewAlarm1Minutes.getText().toString());
            editor.putString("hourAlarm2",textViewAlarm2Hour.getText().toString());
            editor.putString("minutesAlarm2",textViewAlarm2Minutes.getText().toString());
            editor.commit();
            //editor.apply();
        }
    }

    //funcion para que parpadee el textview a modificar de las alarmas
    public void ClickModifiyingAlarm (TextView textView) {
        if (padlockClosed == false) { //candado esta abierto,
            if (textViewAlarmEdit == null) {
                textViewAlarmEdit = textView;
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
                if (num == 24) {
                    num = 0;
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
                if (num < 0){
                    num = 23;
                }
            }

            if (textViewAlarmEdit == textViewAlarm1Minutes || textViewAlarmEdit == textViewAlarm2Minutes){
                if (num <0){
                    num =59;
                }
            }
            String resul = FormatTwoDigits(num);
            textViewAlarmEdit.setText(resul);
        }
    }


    public void showAlertDialog () {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_alarm, null);
        builder.setView(viewInflated);

        ImageView sleep = (ImageView) viewInflated.findViewById(R.id.imageViewSleep);
        ImageView wake = (ImageView) viewInflated.findViewById(R.id.imageViewWake);
        TextView hour = (TextView) viewInflated.findViewById(R.id.textViewHourDialog);
        TextView minutes = (TextView) viewInflated.findViewById(R.id.textViewMinutesDialog);
        TextView colon = (TextView) viewInflated.findViewById(R.id.textViewColonDialog);

        hour.setText(textViewHour.getText());
        minutes.setText(textViewMinutes.getText());

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);


        //player = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
        player = MediaPlayer.create(this, currentTone);
        player.start();

        wake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.stop();
                dialog.cancel();
                imageViewSnooze.setVisibility(View.INVISIBLE);
            }
        });

        sleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Date date = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date); //tuFechaBase es un Date;
                calendar.add(Calendar.MINUTE, 1); //minutosASumar es int.
                Date fechaSalida = calendar.getTime();

                snoozeAlarmHour = FormatTwoDigits(calendar.get(Calendar.HOUR_OF_DAY));
                snoozeAlarmMinutes = FormatTwoDigits(calendar.get(Calendar.MINUTE));

                imageViewSnooze.setVisibility(View.VISIBLE);

                player.stop();
                dialog.cancel();

            }
        });

    }

    public Boolean IsAlarmNow (String hour, String minutes, boolean alarmActivate){
        return textViewHour.getText().equals(hour) &&
               textViewMinutes.getText().equals(minutes) &&
               alarmActivate;
    }
}
