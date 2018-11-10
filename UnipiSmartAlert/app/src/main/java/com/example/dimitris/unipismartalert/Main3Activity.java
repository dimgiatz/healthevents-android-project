package com.example.dimitris.unipismartalert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class Main3Activity extends AppCompatActivity {

    TextView userView,passView,textView6;
    String username,password,msg,phone;
    int index=1;
    SharedPreferences sharedPref;
    Query query;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        userView=(TextView)findViewById(R.id.userText);
        passView=(TextView)findViewById(R.id.passText);
        textView6=(TextView)findViewById(R.id.textView6);

        sharedPref = this.getSharedPreferences("com.example.dimitris.unipismartalert.SharedPref", Context.MODE_PRIVATE);

    }

    //Ελεγχος απο firebase για login χρηστη
    public void signIn(View view){
        username=userView.getText().toString();
        password=passView.getText().toString();

        query = FirebaseDatabase.getInstance().getReference()
                .child("Users").orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        //Επιτυχημένο login στέλνει μήνυμα abort και σταματάει το timer
                        if (singleSnapshot.child("password").getValue().toString().equals(password)) {
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                            stopService(new Intent(getApplicationContext(), MyService.class));
                            if(!MyService.isInstanceCreated()) {
                                sendSOS();
                            }
                        } else {
                            textView6.setText("Wrong Password. Try Again!");
                        }
                    }
                }
                else{
                        textView6.setText("Wrong Username. Try Again!");
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //Αποστολή SMS
    public void sendSOS(){

        msg="Άκυρος ο συναγερμός. Όλα καλά.";

        SmsManager smsManager = SmsManager.getDefault();
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
    }
}
