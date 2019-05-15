package com.example.smarttrafficlightcontrol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private Button   btnSetTime;
    private Button   btnSetNow;
    private Button   btnBat;
    private Button   btnTat;
    private Button   btnCamBien;
    private WebView  wbv;
    private TextView txtvTimerSet;
    private TextView txtvTimerCurrent;
    private String   temp = "192.168.4.1";
    private String   ip;
    private Spinner  spHours;
    private Spinner  spMinutes;
    private String   hourSelected = "";
    private String   minuteSelected = "";
    private String   currentHour , currentMinute;
    private boolean  isStartedSet = false;
    private boolean  isStartedCurrent = false;
    private ThreadTimerSet   threadTimerSet;
    private ThreadTimerCurrent   threadTimerCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtvTimerSet     = (TextView) findViewById(R.id.txtvTimerSet);
        txtvTimerCurrent = (TextView) findViewById(R.id.txtvTimerCurrent);
        btnSetTime       = (Button)   findViewById(R.id.btnSetTime);
        btnSetNow        = (Button)   findViewById(R.id.btnSetNow);
        btnBat           = (Button)   findViewById(R.id.btnBat);
        btnTat           = (Button)   findViewById(R.id.btnTat);
        btnCamBien       = (Button)   findViewById(R.id.btnCamBien);
        wbv              = (WebView)  findViewById(R.id.wbv);
        spHours          = (Spinner)  findViewById(R.id.spHours);
        spMinutes        = (Spinner)  findViewById(R.id.spMinutes);

        timer();
        btnSetNow.post(new Runnable() {
            @Override
            public void run() {
                btnSetNow.performClick();
            }
        });

        btnSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ip = "http://" + temp + "/" + "2000" + "01" + "01" + hourSelected + minuteSelected + "timer";
                wbv.loadUrl(ip);

                //region THREAD TIMER

                txtvTimerCurrent.setVisibility(View.INVISIBLE);
                txtvTimerSet.setVisibility(View.VISIBLE);

                if(isStartedCurrent)
                    threadTimerCurrent.kill();
                if(isStartedSet)
                    threadTimerSet.kill();

                threadTimerSet = new ThreadTimerSet();
                threadTimerSet.start();

                //endregion
            }
        });

        btnSetNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                int day    = calendar.get(Calendar.DATE);
                int month  = calendar.get(Calendar.MONTH) + 1;
                int year   = calendar.get(Calendar.YEAR);
                int hour   = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                if(hour<10)  currentHour = "0" + String.valueOf(hour);
                else    currentHour = String.valueOf(hour);

                if(minute<10)  currentMinute = "0" + String.valueOf(minute);
                else    currentMinute = String.valueOf(minute);

                ip = "http://" + temp + "/" + "2019" + "05" + "12" + currentHour + currentMinute + "timer";
                wbv.loadUrl(ip);

                //region   THREAD TIMER

                txtvTimerCurrent.setVisibility(View.VISIBLE);
                txtvTimerSet.setVisibility(View.INVISIBLE);

                if(isStartedCurrent)
                    threadTimerCurrent.kill();
                if(isStartedSet)
                    threadTimerSet.kill();

                threadTimerCurrent = new ThreadTimerCurrent();
                threadTimerCurrent.start();


                //endregion
            }
        });

        btnBat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ip = "http://" + temp + "/201905121800timer";
                wbv.loadUrl(ip);
            }
        });

        btnTat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ip = "http://" + temp + "/201905120700timer";
                wbv.loadUrl(ip);
            }
        });

        btnCamBien.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ip = "http://" + temp + "/201905121710timer";
                wbv.loadUrl(ip);
            }
        });
    }

    public class ThreadTimerCurrent extends Thread{
        boolean running = true;

        public void kill(){
            running = false;
        }

        public void run(){
            isStartedCurrent = true;
            while(running){
                try{
                    Thread.sleep(1000);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                            int hour   = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.getInstance(TimeZone.getDefault()).get(Calendar.MINUTE);

                            if(hour<10 && minute<10)
                                txtvTimerCurrent.setText("0" + hour +":0"+ minute);
                            else
                            if(hour<10)
                                txtvTimerCurrent.setText("0" + hour + ":" + minute);
                            else
                            if(minute<10)
                                txtvTimerCurrent.setText(hour +":0"+ minute);
                            else
                                txtvTimerCurrent.setText(hour + ":" + minute);
                        }
                    });
                }catch (InterruptedException ex){

                }
            }
        }
    }

    public class ThreadTimerSet extends Thread{

        int second = 0;
        int hour   = Integer.parseInt(hourSelected);
        int minute = Integer.parseInt(minuteSelected);
        boolean running = true;

        public void kill(){
            running = false;
        }

        public void run(){
            isStartedSet = true;
            while(running){
                try{
                    Thread.sleep(1000);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            second++;
                            if(second==60){
                                second=0;
                                minute++;
                                if(minute==60){
                                    minute=0;
                                    hour++;
                                    if(hour==24)
                                        hour=0;
                                }
                            }

                            if(hour<10 && minute<10)
                                txtvTimerSet.setText("0" + hour +":0"+ minute);
                            else
                            if(hour<10)
                                txtvTimerSet.setText("0" + hour + ":" + minute);
                            else
                            if(minute<10)
                                txtvTimerSet.setText(hour +":0"+ minute);
                            else
                                txtvTimerSet.setText(hour + minute);
                        }
                    });
                }catch (InterruptedException ex){

                }
            }
        }
    }

    protected void timer(){

        //region KHỞI TẠO CÁC GIÁ TRỊ GIỜ, PHÚT

        String[] hours  = new String[]{"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12",
                                        "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"};
        String[] minutes= new String[]{"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14",
                                        "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
                                        "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44",
                                        "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"};


        List<String> listHours = new ArrayList<>();
        for(String hour: hours)
            listHours.add(hour);

        List<String> listMinutes = new ArrayList<>();
        for(String minute: minutes)
            listMinutes.add(minute);


        //endregion

        //region SPINNER GIỜ

        //  ADD GIÁ TRỊ VÀO SPINNER GIỜ
        ArrayAdapter<String> adapterHours = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listHours);
        adapterHours.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spHours.setAdapter(adapterHours);

        // BẮT SỰ KIỆN KHI CHỌN GIỜ
        spHours.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hourSelected = spHours.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //endregion

        //region SPINNER PHÚT

        //  ADD GIÁ TRỊ VÀO SPINNER PHÚT
        ArrayAdapter<String> adapterMinutes = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listMinutes);
        adapterMinutes.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spMinutes.setAdapter(adapterMinutes);

        // BẮT SỰ KIỆN KHI CHỌN PHÚT
        spMinutes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                minuteSelected = spMinutes.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //endregion

    }
}
