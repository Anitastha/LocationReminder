package com.anita.locationreminder.activities;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.anita.locationreminder.R;
import com.anita.locationreminder.adapters.LocationAdapter;
import com.anita.locationreminder.models.LongLat;
import com.anita.locationreminder.utils.FirebaseUtils;
import com.anita.locationreminder.utils.ShakeDetector;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class LocationlistActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SensorManager manager;
    private Sensor mAccelerometer;
    private ShakeDetector shakeDetector;
    private List<LongLat> longLatList;
    private LocationAdapter adapter;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locationlist);

       databaseReference = FirebaseUtils.database.child("LongLat");
        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeDetector = new ShakeDetector();
        shakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake(int count) {
                Toast.makeText(LocationlistActivity.this, "Shake Detected!!!", Toast.LENGTH_SHORT).show();
//                getLongLat();
            }
        });
//        longLatList = new ArrayList<>();
//        adapter = new LocationAdapter(this,longLatList);
        recyclerView = findViewById(R.id.recyclerView);
//        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        getLongLat();

//        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<LongLat> options =
                new FirebaseRecyclerOptions.Builder<LongLat>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("LongLat"), LongLat.class)
                        .build();

        adapter = new LocationAdapter(options,this);
        recyclerView.setAdapter(adapter);

    }



//    private void getLongLat() {
//           longLatList.clear();
//        databaseReference.addChildEventListener(new MyChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                super.onChildAdded(dataSnapshot, s);
//             LongLat longLat = dataSnapshot.getValue(LongLat.class);
//                longLatList.add(longLat);
//                adapter.notifyDataSetChanged();
//
//            }
//        });
//    }

    public void goBack(View view) {
        super.onBackPressed();
    }

//    public void refreshList(){
//        getLongLat();
//    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.registerListener(shakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onStart() {
        super.onStart();
        manager.registerListener(shakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        adapter.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(shakeDetector);
    }

    @Override
    protected void onStop() {
        super.onStop();
        manager.unregisterListener(shakeDetector);
        adapter.stopListening();
    }
}
