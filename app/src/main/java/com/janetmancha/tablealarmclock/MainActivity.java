package com.janetmancha.tablealarmclock;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private TextView textViewHour;
    private TextView textViewMinutes;
    private TextView textViewColon;
    private TextView textViewDate;

    Timer timer = new Timer();
    SimpleDateFormat timeFormat;

    boolean points = false;
    boolean activatedAlarm2 = false;




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
            int hours = calendar.get(Calendar.HOUR_OF_DAY); // formato 24 horas, .HOUR seria formato 12 horas
            int minutes = calendar.get(Calendar.MINUTE);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int dayWeek= calendar.get(calendar.DAY_OF_WEEK);



//            if (points == false){
//                    timeFormat = new SimpleDateFormat("HH:mm:ss");
//                    points = true;
//                } else {
//                    timeFormat = new SimpleDateFormat("HH mm ss");
//                    points = false;
//            }
//            final String timeString = timeFormat.format(date);

            textViewHour =(TextView) findViewById(R.id.textViewHour);
            textViewMinutes = (TextView) findViewById(R.id.textViewMinutes);
            textViewColon = (TextView) findViewById(R.id.textViewColon);
            textViewDate = (TextView) findViewById(R.id.textViewDate);

//            textViewHour.setText(date.getHours()+ "");
//            textViewMinutes.setText(date.getMinutes()+ "");
//            textViewSeconds.setText(date.getSeconds()+ "");

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

            //textViewTime = (TextView) findViewById(R.id.textViewTime);
            //textViewTime.setText(timeString);
            //textViewTime.setText(date.getSeconds() + "");
        }
    };



    public void onPause() {
        super.onPause();  // Always call the superclass method first
        timer.cancel();
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
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // orientacion horizontal
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
