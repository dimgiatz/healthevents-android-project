package com.example.dimitris.unipismartalert;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

//Service για timer
public class MyService extends Service {

    private static MyService instance = null;

    public static final String COUNTDOWN_TI = "com.example.dimitris.unipismartalert.countdown_br";
    public static final String COUNTDOWN_BR = "com.example.dimitris.unipismartalert.countdown_br";
    public static final String COUNTDOWN_FI = "com.example.dimitris.unipismartalert.countdown_fi";

    Intent bi = new Intent(COUNTDOWN_BR);
    Intent ti=new Intent(COUNTDOWN_TI);
    Intent fi=new Intent(COUNTDOWN_FI);

    CountDownTimer cdt = null;
    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    Ringtone ring = RingtoneManager.getRingtone(this, alarmSound);

    //Ελεγχός αν τρέχει το service
    public static boolean isInstanceCreated() {
        return instance != null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance=this;

        //Broadcast για εναρξη ήχου διαρκειας 30 δευτερολέπτων
        bi.setAction("com.example.dimitris.unipismartalert.CUSTOM_INTENT");
        bi.putExtra("play",true);
        sendBroadcast(bi);

        //countdown timer
        cdt = new CountDownTimer(30000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                //broadcast για αλλαγη δευτερολεπτο στην οθόνη
                ti.putExtra("countdown", millisUntilFinished);
                sendBroadcast(ti);
            }

            @Override
            public void onFinish() {
                //broadcast για τέλος ήχου
                bi.putExtra("play",false);
                sendBroadcast(bi);
                //broadcast για αποστολή SMS
                sendBroadcast(fi);
                stopSelf();
            }
        };

        cdt.start();
    }

    //Οταν καταστρέφουμε το Service σταματαεί ήχος και timer
    @Override
    public void onDestroy() {
        instance=null;
        bi.putExtra("play",false);
        sendBroadcast(bi);
        cdt.cancel();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}

