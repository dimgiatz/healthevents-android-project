package com.example.dimitris.unipismartalert;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Main2Activity extends AppCompatActivity {

    Spinner filterSpinner;
    Spinner sortSpinner;
    private TextView noConnection;
    private ListView list;
    private ListViewAdapter adapter;
    public static ArrayList<DEvents> dEventsArrayList;
    public static ArrayList<String> array_sort;
    public static String type;
    DEvents dEvents;
    Query query;
    public static int sposition;
    LinearLayout filters;
    //Comparator για σορταρισμα
    Comparator comp2=new Comparator<DEvents>() {
        @Override
        public int compare(DEvents o1, DEvents o2) {
            if (sposition == 0) {
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            } else {
                return -1 * o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        dEventsArrayList = new ArrayList<>();
        array_sort = new ArrayList<>();
        list = (ListView) findViewById(R.id.listView);
        noConnection = (TextView) findViewById(R.id.noConnection);
        filters=(LinearLayout)findViewById(R.id.filters);

        //ελεγχος συνδεσης με Internet
        if (!isNetworkAvailable()) {
            filters.setVisibility(View.GONE);
            list.setVisibility(View.GONE);
            noConnection.setVisibility(View.VISIBLE);
        }

        //Spinner για Φιλτρό Γεγονότος
        filterSpinner = (Spinner) findViewById(R.id.filterSpinner);
        ArrayAdapter<CharSequence> adapterSpinnerFilter = ArrayAdapter.createFromResource(this,
                R.array.filterSpinner, android.R.layout.simple_spinner_item);
        adapterSpinnerFilter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapterSpinnerFilter);

        //Listener στο spinner και αντιστοιχη αναζητηση στη firebase βαση φιλτρού
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                type = filterSpinner.getSelectedItem().toString();

                if (position == 0) {
                    query = FirebaseDatabase.getInstance().getReference()
                            .child("Events").orderByChild("time");
                } else {
                    query = FirebaseDatabase.getInstance().getReference()
                            .child("Events").orderByChild("type").equalTo(type);
                }

                    // αναζήτηση σε firebase και αποθηκευση σε dEvent κλαση για καθε γεγονος
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            dEventsArrayList.clear();
                            if (dataSnapshot.exists()) {
                                // dataSnapshot is the "issue" node with all children with id 0
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    dEvents = new DEvents(singleSnapshot.child("type").getValue().toString(),
                                            singleSnapshot.child("time").getValue().toString(),
                                            singleSnapshot.child("lat").getValue().toString(),
                                            singleSnapshot.child("lon").getValue().toString());

                                    dEventsArrayList.add(dEvents);
                                }
                            }
                            else{
                                dEventsArrayList.clear();
                            }
                            //σορταρισμα απο παλαιοτερο σε νεοτερο γεγονος αν είναι η αρχικη επιλογη
                            if (sortSpinner.getSelectedItemPosition() == 1) {
                                Collections.sort(dEventsArrayList,comp2);
                            }
                            adapter = new ListViewAdapter(getApplicationContext(), dEventsArrayList);
                            list.setAdapter(adapter);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        //Spinner σορταρίσματος χρόνου
        sortSpinner = (Spinner) findViewById(R.id.sortSpinner);
        ArrayAdapter<CharSequence> adapterSpinnerSort = ArrayAdapter.createFromResource(this,
                R.array.sortSpinner, android.R.layout.simple_spinner_item);
        adapterSpinnerSort.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapterSpinnerSort);

        //Spinner Listener και αντιστοιχο σορταρισμα βαση χρονου
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, final int position, long id) {
                sposition=position;
                Collections.sort(dEventsArrayList, comp2);
                adapter = new ListViewAdapter(getApplicationContext(), dEventsArrayList);
                list.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    //ελεγχος για συνδεση με Internet
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

}