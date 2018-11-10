package com.example.dimitris.unipismartalert;

import android.*;
import android.Manifest;
import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements SensorEventListener,LocationListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    SmsManager smsManager;
    LocationManager lm;
    String[] PERMISSIONS = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.SEND_SMS};
    SharedPreferences sharedPref;
    TextView textView,textView2,textView5;
    int index=1;
    String msg;
    String phone;
    float x,y,z,l;
    double rootSquare;
    static double Lat=0.0,Lon=0.0;
    double speed;
    TextToSpeech tts;
    Thread t;
    NotificationCompat.Builder mBuilder;
    NotificationManager notificationManager,lnotificationManager,
            fnotificationManager,vnotificationManager;
    Notification lnotification,fnotification,vnotification;

    //Ορισμος Thread για Text-To-Speech
    public class Speak implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 5; ++i) {
                tts.speak("Help me!", TextToSpeech.QUEUE_ADD, null);
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lm=(LocationManager) getSystemService(LOCATION_SERVICE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Κώδικας για Notifications με ήχο,δονηση,φως και Intent
        Intent tap = new Intent(this, MainActivity.class);
        tap.setAction(Intent.ACTION_MAIN);
        tap.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingTap = PendingIntent.getActivity(this, 0,
                tap, 0);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setSound(alarmSound)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingTap);
        notificationManager =(NotificationManager) this.getSystemService(
                Context.NOTIFICATION_SERVICE);
        lnotificationManager =(NotificationManager) this.getSystemService(
                Context.NOTIFICATION_SERVICE);
        vnotificationManager =(NotificationManager) this.getSystemService(
                Context.NOTIFICATION_SERVICE);
        fnotificationManager =(NotificationManager) this.getSystemService(
                Context.NOTIFICATION_SERVICE);

        //Αρχικοποίηση UI
        textView=(TextView)findViewById(R.id.textView);
        textView.setText("0"+" khm/h");
        textView2=(TextView)findViewById(R.id.textView2);
        textView5=(TextView)findViewById(R.id.textView5);

        //Real-Time έλεγχος για requests
        if(!checkPermissions(this, PERMISSIONS)){
                ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }

        //Έναρξή GPS
        startLoc();
        //Αποθήκευση τηλεφώνων σε sharedPreferences
        savePhones();

        //Αρχικοποίηση TTS
        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                }
            }
        });

    }


    //Αποθήκευση τηλεφώνων
    public void savePhones(){

        sharedPref = this.getSharedPreferences("com.example.dimitris.unipismartalert.SharedPref",
                Context.MODE_PRIVATE);

        sharedPref.edit().clear().commit();

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("phone1",getString(R.string.phone1));
//        editor.putString("phone2","6948743540");
        editor.commit();

    }

    //Μετάβαση σε Activity Στατιστικών
    public void statsActivity(View view){
        Intent i = new Intent(this, Main2Activity.class);
        startActivity(i);
    }

    //Έλεγχος αποτελέσματος Request Permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1) {
            //αν κάποιο permission δεν εγκρίθηκε εμφανίζεται μήνυμα για μετάβαση σε Settings
            if(!checkPermissions(this, PERMISSIONS)){
                goSettings();
            }else {
                //Ενεργοποιήση GPS την πρώτη φορά
                startLoc();
            }
        }
    }

    //Μήνυμα λόγω έλλειψης request
    public void goSettings(){
        Snackbar snackbar = Snackbar.make(this.findViewById(android.R.id.content),
                "App Needs Permissions To Function Properly",
                Snackbar.LENGTH_LONG);
        snackbar.setAction("Settings", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        snackbar.show();
    }

    //Ελεγχος permission
    public static boolean checkPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        return false;
                }
            }
        }
        return true;
    }

    //Ενάρξη GPS
    public void startLoc(){
        try{
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            Toast.makeText(getApplicationContext(),"GPS Enabled",Toast.LENGTH_SHORT).show();
            //ελεγχος αν είναι ενεργοποιημένο το GPS
            changeLocSettings();
        }
        catch(SecurityException s){

        }
    }

    //ελεγχός και μήνυμα ενεργοποιήσης για GPS
    public void changeLocSettings() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());


        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                0x1);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }


    //Sensor Listener και ελεγχος φωτεινοτητας και πτωσης
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            freeFalling(event);
        }else if(event.sensor.getType() == Sensor.TYPE_LIGHT){
            lumChecker(event);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);

        registerReceiver(br, new IntentFilter(MyService.COUNTDOWN_TI));
        registerReceiver(br2, new IntentFilter(MyService.COUNTDOWN_FI));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    //Απενεργοποίηση εφαρμογής
    @Override
    protected void onDestroy() {
        //Ελεγχος αν προκειται για αλλαγη orientation
        if(!isChangingConfigurations()){
            stopService(new Intent(getApplicationContext(), MyService.class));
            mSensorManager.unregisterListener(this);
            if(tts !=null){
                tts.stop();
                tts.shutdown();
            }
        }
        super.onDestroy();
    }


    @Override
    public void onLocationChanged(Location location) {

        Lat = location.getLatitude();
        Lon = location.getLongitude();
        speed=(location.getSpeed()*3600)/1000;

        textView.setText(String.valueOf((float)Math.round(speed * 100) / 100)+" khm/h");

        StatusBarNotification[] notifications = vnotificationManager.getActiveNotifications();

        //Ελεγχός ταχύτητας
        if (speed>80){

            //Ελεγχός αν υπάρχει ήδη notification
            if (notifications.length==0) {
                writeDB("High Speed");
            }

            mBuilder.setContentTitle("High Speed")
                    .setLights(Color.RED, 1000, 1000)
                    .setContentText("Slow Down Please");
            vnotification=mBuilder.build();
            vnotification.flags=Notification.FLAG_ONLY_ALERT_ONCE|Notification.FLAG_NO_CLEAR|
                    Notification.FLAG_AUTO_CANCEL|Notification.FLAG_SHOW_LIGHTS;
            vnotificationManager.notify(3, vnotification);
        }

    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        changeLocSettings();
    }

    //ελεγχός πτώσης
    public void freeFalling(SensorEvent event) {
        x= event.values[0];
        y= event.values[1];
        z= event.values[2];

        rootSquare = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        if (rootSquare < 1.9) {
                mBuilder.setContentTitle("Fall")
                        .setSound(null)
                        .setLights(Color.WHITE, 1000, 1000)
                        .setContentText("Did you fell down?");
                fnotification = mBuilder.build();
                fnotification.flags = Notification.FLAG_NO_CLEAR |
                        Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
                notificationManager.notify(1, fnotification);

                //εναρξή timer
                startService(new Intent(this, MyService.class));
        }

    }

    //ελεγχός φωτεινότητας
    public void lumChecker(SensorEvent event) {
        l= event.values[0];
        textView5.setText(String.valueOf(l)+" lux");

        StatusBarNotification[] notifications = lnotificationManager.getActiveNotifications();

        if((l>100000)) {

            if (notifications.length==0) {
                writeDB("Solar Radiation");
            }

            mBuilder.setContentTitle("Sunlight")
                    .setLights(Color.YELLOW, 1000, 1000)
                    .setContentText("High Solar Radiation");
            lnotification = mBuilder.build();
            lnotification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_NO_CLEAR |
                    Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
            lnotificationManager.notify(2, lnotification);

        }

    }


    //αποστολή SMS και καταγραφή σε βάση
    public void sendSOS(){

        msg="Βρίσκομαι στην τοποθεσία με γεωγραφικό μήκος:"+" "+String.valueOf(Lon)+" "+
                " και γεωγραφικό πλάτος:" +" "+ String.valueOf(Lat) +" "+ "και χρειάζομαι βοήθεια.";

        smsManager = SmsManager.getDefault();
        ArrayList<String> parts = smsManager.divideMessage(msg);

        while (!sharedPref.getString("phone"+String.valueOf(index),"Default value").equals("Default value")) {
            phone = sharedPref.getString("phone" + String.valueOf(index), "Default value");
            smsManager = SmsManager.getDefault();

            smsManager.sendMultipartTextMessage(phone, null, parts, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.",
                    Toast.LENGTH_SHORT).show();
            index++;
        }
        index=1;
        writeDB("SOS");

    }

    //κουμπί SOS
    public void buttonSOS(View view) {
        if(checkPermissions(this, PERMISSIONS)){
            if((Lon!=0.0)&&(Lat!=0.0)) {
                sendSOS();
            }
        }
        else{
            goSettings();
        }
        t = new Thread(new Speak());
        t.start();
    }

    //κουμπί Abort
    public void abortSOS(View view) {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
            Intent i=new Intent(this,Main3Activity.class);
            startActivity(i);
        }
        else{
            goSettings();
        }
    }

    //Εγραφή σε βάση με έλεγχο για GPS
    public void writeDB(String type){

        DatabaseReference reference;

        if ((Lon!=0.0)&&(Lat!=0.0)) {
            Long tsLong = System.currentTimeMillis() / 1000;
            String ts = tsLong.toString();

            reference = FirebaseDatabase.getInstance().getReference()
                    .child("Events/" + ts);
            reference.child("time").setValue(ts);
            reference.child("type").setValue(type);
            reference.child("lon").setValue(String.valueOf(Lon));
            reference.child("lat").setValue(String.valueOf(Lat));
        }
    }

    //BroadcastReceiver για καθε χτυπο του timer
    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long millisUntilFinished = intent.getLongExtra("countdown", -1);
            textView2.setText(String.valueOf(millisUntilFinished/1000) );
        }
    };

    //BroadcastReceiver για λήξη timer
    private BroadcastReceiver br2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(checkPermissions(context, PERMISSIONS)&&(Lon!=0.0)&&(Lat!=0.0)){
                sendSOS();
            }
            else{
                goSettings();
            }
            textView2.setText("");
            writeDB("Fall");
        }
    };

}
