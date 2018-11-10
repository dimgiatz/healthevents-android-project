package com.example.dimitris.unipismartalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.Toast;

//Broadcast Receiver για ήχο σε countdown timer
public class MyReceiver extends BroadcastReceiver {

    public static MediaPlayer AlarmMusic;
    @Override
    public void onReceive(Context context, Intent intent) {

        //Παιζει αρχείο ήχου διάρκειας 30 δευτερολέπτων με ήχο σε διαστήματος ενός δευτερολέπτου
        if (intent.getExtras().getBoolean("play")==true) {
            AlarmMusic = MediaPlayer.create(context, R.raw.alarm);
            AlarmMusic.setLooping(true);
            AlarmMusic.start();
        }
        //Σταματάει τον ήχο
        else if(intent.getExtras().getBoolean("play")==false){
            AlarmMusic.stop();
        }
    }
}
