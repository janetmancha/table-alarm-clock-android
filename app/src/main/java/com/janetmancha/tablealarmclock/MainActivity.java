package com.janetmancha.tablealarmclock;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    //Elementos UI
    private TextView textViewDate, textViewHour, textViewMinutes, textViewColon;
    private TextView textViewAlarm1Hour, textViewAlarm1Minutes;
    private TextView textViewAlarm2Hour, textViewAlarm2Minutes;
    private TextView textViewAlarmEdit;
    private ImageView imageViewPlug, imageViewPadlock, imageViewSnooze;
    private ImageView imageViewIncrease, imageViewDecrease, imageViewOk;

    Timer timer = new Timer();
    Timer timerIcon = new Timer();
    SimpleDateFormat timeFormat;

    private SharedPreferences prefs;
    SharedPreferences.Editor editor;

    MediaPlayer player;
    Uri currentTone;

    boolean padlockClosed;

    public Handler timeHandler = new Handler() {
        public void handleMessage(Message msg) {
            final Date date = new Date();
            timeFormat = new SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy");
            Calendar calendar = Calendar.getInstance();
            int hours = calendar.get(Calendar.HOUR_OF_DAY); // formato 24 horas, .HOUR seria formato 12 horas
            int minutes = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            textViewHour.setText(FormatTwoDigits(hours));
            textViewMinutes.setText(FormatTwoDigits(minutes));
            textViewDate.setText(timeFormat.format(calendar.getTime()));
            textViewColon.setVisibility(textViewColon.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE ); //pintar los dos puntos cada segundo
            //comprobar en el segundo 0 de cada minuto si tiene que sonar la alarma
            if (second == 0 && (
                IsAlarmNow(textViewAlarm1Hour.getText().toString(),textViewAlarm1Minutes.getText().toString(), prefs.getBoolean("alarm1Activate", false)) ||
                IsAlarmNow(textViewAlarm2Hour.getText().toString(),textViewAlarm2Minutes.getText().toString(), prefs.getBoolean("alarm2Activate", false)) ||
                IsAlarmNow(prefs.getString("snoozeAlarmHour",null),prefs.getString("snoozeAlarmMinutes",null),prefs.getBoolean("snoozeActivate",false))
                )) {
                showAlertDialog();
            }
        }
    };

    public Handler timeHandlerIcon = new Handler() {
        public void handleMessage(Message msg) {
            //comprobacion si esta enchufado a la red por usb o un gargador de corriente alterna
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = getBaseContext().registerReceiver(null, ifilter);
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            if (!usbCharge && !acCharge) {
                imageViewPlug.setVisibility(imageViewPlug.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
            } else {
                imageViewPlug.setVisibility(View.VISIBLE);
            }
            //textview alarma modificandose
            if (textViewAlarmEdit != null) {
                textViewAlarmEdit.setVisibility(textViewAlarmEdit.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE );
            }
            //comprobar si esta activa la opcion snooze
            if (prefs.getBoolean("snoozeActivate",true)) {
                imageViewSnooze.setVisibility(imageViewSnooze.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE );
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //mantener la pantalla siempre encendida
        super.onCreate(savedInstanceState);
        //ocultar barra de notificaciones versiones 4.1 y superiores para inferiores cambiado style en manifest
        if (Build.VERSION.SDK_INT > 16) { getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); }
        prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        editor = prefs.edit();
        if (getIntent().getBooleanExtra("init", true)) {
            editor.putBoolean("snoozeActivate", false);
            editor.apply();
        }
        setTheme(prefs.getInt("theme",R.style.AppThemeNoBarBlack));
        setContentView(R.layout.activity_main);

        ImageView imageViewAlarm1 = (ImageView) findViewById(R.id.imageViewAlarm1);
        ImageView imageViewAlarm2 = (ImageView) findViewById(R.id.imageViewAlarm2);
        ImageView imageViewSound = (ImageView) findViewById(R.id.imageViewSound);
        ImageView imageViewEditTheme = (ImageView) findViewById(R.id.imageViewEditTheme);

        textViewHour =(TextView) findViewById(R.id.textViewHour);
        textViewMinutes = (TextView) findViewById(R.id.textViewMinutes);
        textViewColon = (TextView) findViewById(R.id.textViewColon);
        textViewDate = (TextView) findViewById(R.id.textViewDate);
        imageViewPlug = (ImageView) findViewById(R.id.imageViewPlug);
        imageViewPadlock = (ImageView) findViewById(R.id.imageViewPadlock);
        textViewAlarm1Hour = (TextView)findViewById(R.id.textViewAlarm1Hour);
        textViewAlarm1Minutes = (TextView) findViewById(R.id.textViewAlarm1Minutes);
        textViewAlarm2Hour = (TextView) findViewById(R.id.textViewAlarm2Hour);
        textViewAlarm2Minutes = (TextView) findViewById(R.id.textViewAlarm2Minutes);
        imageViewIncrease = (ImageView) findViewById(R.id.imageViewIncrease);
        imageViewDecrease = (ImageView) findViewById(R.id.imageViewDecrease);
        imageViewOk = (ImageView) findViewById(R.id.imageViewOk);
        imageViewSnooze = (ImageView) findViewById(R.id.imageViewSnooze);

        //Coger preferencias
        currentTone = Uri.parse(prefs.getString("currenTone", RingtoneManager.getActualDefaultRingtoneUri(MainActivity.this, RingtoneManager.TYPE_ALARM).toString()));
        textViewAlarm1Hour.setText(prefs.getString("hourAlarm1","00"));
        textViewAlarm2Hour.setText(prefs.getString("hourAlarm2","00"));
        textViewAlarm1Minutes.setText(prefs.getString("minutesAlarm1","00"));
        textViewAlarm2Minutes.setText(prefs.getString("minutesAlarm2","00"));
        showIcon("alarm1Activate", R.mipmap.ic_bell_on_foreground, R.mipmap.ic_bell_off_foreground,imageViewAlarm1);
        showIcon("alarm2Activate", R.mipmap.ic_bell_on_foreground, R.mipmap.ic_bell_off_foreground,imageViewAlarm2);
        padlockClosed = showIcon("padlockClosed", R.mipmap.ic_padlock_block_foreground, R.mipmap.ic_padlock_open_foreground,imageViewPadlock);

        //Onclick botones
        imageViewPadlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewPadlock.setImageResource(padlockClosed ? R.mipmap.ic_padlock_open_foreground : R.mipmap.ic_padlock_block_foreground);
                padlockClosed = !padlockClosed;
                editor.putBoolean("padlockClosed",padlockClosed);
                editor.apply();
                cancelEdit();
            }
        });
        iconSetOnClick(imageViewAlarm1, "alarm1Activate");
        iconSetOnClick(imageViewAlarm2, "alarm2Activate");
        textViewAlarmSetOnClick (textViewAlarm1Hour);
        textViewAlarmSetOnClick (textViewAlarm1Minutes);
        textViewAlarmSetOnClick (textViewAlarm2Hour);
        textViewAlarmSetOnClick (textViewAlarm2Minutes);
        imageViewOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelEdit();
            }
        });
        imageViewIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmIncrease();
            }
        });
        imageViewDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmDecrease();
            }
        });
        imageViewSnooze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player != null) player.stop();
                editor.putString("snoozeAlarmHour",null);
                editor.putString("snoozeAlarmMinutes",null);
                editor.putBoolean("snoozeActivate",false);
                imageViewSnooze.setVisibility(View.INVISIBLE);
                editor.apply();
            }
        });
        imageViewEditTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 if (!padlockClosed) { //candado esta abierto
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.putExtra("init", false);
                    switch (prefs.getInt("theme", R.style.AppThemeNoBarBlack)) {
                        case R.style.AppThemeNoBarBlack:
                            editor.putInt("theme", R.style.AppThemeNoBarGreen);
                            break;
                        case R.style.AppThemeNoBarGreen:
                            editor.putInt("theme", R.style.AppThemeNoBarRed);
                            break;
                        case R.style.AppThemeNoBarRed:
                            editor.putInt("theme", R.style.AppThemeNoBarWhite);
                            break;
                        case R.style.AppThemeNoBarWhite:
                            editor.putInt("theme", R.style.AppThemeNoBarBlack);
                            break;
                    }
                    editor.apply();
                    startActivity(intent);
                }
            }
        });
        imageViewSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!padlockClosed) { //candado esta abierto
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
        if (resultCode == -1) {
            currentTone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            editor.putString("currenTone", currentTone.toString());
        }
    }

    //funcion cancelar opcion editar alarmas
    public void cancelEdit () {
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
            editor.apply();
        }
    }

    //funcion para que parpadee el textview a modificar de las alarmas
    public void clickModifiyingAlarm (TextView textView) {
        if (!padlockClosed) { //candado esta abierto,
            if (textViewAlarmEdit == null) {
                textViewAlarmEdit = textView;
                imageViewIncrease.setVisibility(View.VISIBLE);
                imageViewDecrease.setVisibility(View.VISIBLE);
                imageViewOk.setVisibility(View.VISIBLE);
            } else if (textViewAlarmEdit == textView) {
                cancelEdit();
            }
        }
    }

    //funcion para poner dos digitos en la hora, minutos
    public String FormatTwoDigits (int num) {
        return (num >=0 && num<=9) ? "0" + num : num + "";
   }

   //funcion sumar textViews Alarmas
    public void alarmIncrease (){
        if (textViewAlarmEdit != null){
            int num = Integer.parseInt(textViewAlarmEdit.getText().toString());
            num = num + 1;
            if (textViewAlarmEdit == textViewAlarm1Hour || textViewAlarmEdit == textViewAlarm2Hour){
                if (num == 24) num = 0;
            }
            if (textViewAlarmEdit == textViewAlarm1Minutes || textViewAlarmEdit == textViewAlarm2Minutes){
                if (num > 59) num = 0;
            }
            String resul = FormatTwoDigits(num);
            textViewAlarmEdit.setText(resul);
        }
    }

    //funcion restar textViews Alarmas
    public void alarmDecrease (){
        if (textViewAlarmEdit != null){
            int num = Integer.parseInt(textViewAlarmEdit.getText().toString());
            num = num -1;
            if (textViewAlarmEdit == textViewAlarm1Hour || textViewAlarmEdit == textViewAlarm2Hour){
                if (num < 0) num = 23;
            }
            if (textViewAlarmEdit == textViewAlarm1Minutes || textViewAlarmEdit == textViewAlarm2Minutes){
                if (num <0)num =59;
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
        player = MediaPlayer.create(this, currentTone);
        player.start();
        wake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            player.stop();
            dialog.cancel();
            imageViewSnooze.setVisibility(View.INVISIBLE);
            editor.putBoolean("snoozeActivate",false);
            editor.apply();
            }
        });
        sleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MINUTE, 1); //minutosASumar es int.
            editor.putString("snoozeAlarmHour",FormatTwoDigits(calendar.get(Calendar.HOUR_OF_DAY)));
            editor.putString("snoozeAlarmMinutes",FormatTwoDigits(calendar.get(Calendar.MINUTE)));
            editor.putBoolean("snoozeActivate",true);
            editor.apply();
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

    public boolean showIcon(String key, int iconTrue, int iconFalse, ImageView imageView){
        boolean activate  = prefs.getBoolean(key,false);
        imageView.setImageResource(activate ? iconTrue: iconFalse);
        return activate;
    }

    public void iconSetOnClick(final ImageView i, final String key) {
        i.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!padlockClosed) { //candado esta abierto
                    boolean b = prefs.getBoolean(key, false);
                    i.setImageResource(b ? R.mipmap.ic_bell_off_foreground : R.mipmap.ic_bell_on_foreground);
                    editor.putBoolean(key, !b);
                    editor.apply();
                }
            }
        });
    }

    public void textViewAlarmSetOnClick (final TextView t) {
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickModifiyingAlarm(t);
            }
        });
    }
}
