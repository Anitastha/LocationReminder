package com.anita.locationreminder.activities;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.anita.locationreminder.R;
import com.anita.locationreminder.listeners.MyChildEventListener;
import com.anita.locationreminder.models.LongLat;
import com.anita.locationreminder.utils.FirebaseUtils;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener,
        com.google.android.gms.location.LocationListener{

        private DrawerLayout drawerLayout;
        private Location mLocation;
        private GoogleMap map;
        private GoogleApiClient googleApiClient;
        private LocationManager locationManager;
        private LocationRequest locationRequest;
        private LatLng latLng;
        private long maxid=0;
        private EditText etName, etTask;
        private TextView tvLatitude, tvLongitude;
        private LocationManager manager;
        private LongLat longLat;
        private boolean isPermission;
        private ImageView imgAddAlarm;
        private SearchView search_map;
        private View navHome, navTask, navSetting, navHelp, navAbout, navExit;
        private List<LongLat> longLatList = new ArrayList<>();
        private boolean vibration, sound, sateliteview;
        private int radius;
        private Marker marker;
        private SharedPreferences preferences;
        private boolean isDialogShowing = false;
        private DatabaseReference databaseReference;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_maps);
                drawerLayout = findViewById(R.id.drawerLayout);
                imgAddAlarm = findViewById(R.id.imgAddAlarm);
                navHome = findViewById(R.id.navHome);
                navTask = findViewById(R.id.navTask);
                navSetting = findViewById(R.id.navSetting);
                navHelp = findViewById(R.id.navHelp);
                navAbout = findViewById(R.id.navAbout);
                navExit = findViewById(R.id.navExit);
                search_map = findViewById(R.id.search_map);
                longLat = new LongLat();
                databaseReference = FirebaseUtils.database.child("LongLat");

         //for auto increment in firebase
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists())
                        maxid=(dataSnapshot.getChildrenCount());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

          //for Location permission
            if(requestSinglePermission()){

                }

                final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);

                //for search location
            search_map.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    String location = search_map.getQuery().toString();
                    List<Address> addressList = null;
                    if (location != null || !location.equals("")){
                        Geocoder geocoder = new Geocoder(MapsActivity.this);
                        try{
                            addressList = geocoder.getFromLocationName(location,1);
                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }
                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                        map.addMarker(new MarkerOptions().position(latLng).title(location));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }
            });

               //for click listener
                mapFragment.getMapAsync(this);
                imgAddAlarm.setOnClickListener(this);
                navHome.setOnClickListener(this);
                navTask.setOnClickListener(this);
                navSetting.setOnClickListener(this);
                navHelp.setOnClickListener(this);
                navAbout.setOnClickListener(this);
                navExit.setOnClickListener(this);

            preferences = getSharedPreferences("Locationreminder", MODE_PRIVATE);
            vibration = preferences.getBoolean("Vibration", false);
            sound = preferences.getBoolean("Sound", false);
            sateliteview = preferences.getBoolean("Sateliteview",false);
            radius = preferences.getInt("radius", 200);

                googleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
                locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                checkLocation();

        }

    private void getLonglat() {
        FirebaseRecyclerOptions<LongLat> option =
                new FirebaseRecyclerOptions.Builder<LongLat>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("LongLat"), LongLat.class)
                        .build();

                    for (LongLat longLat : longLatList) {
                        CircleOptions options = new CircleOptions();
                        options.radius(radius);
                        options.center(new LatLng(Double.parseDouble(longLat.getLatitude()), Double.parseDouble(longLat.getLongitude())));
                        options.strokeWidth(0);
                        options.fillColor(Color.parseColor("#500084d3"));
                        map.addCircle(options);
                    }
                }

     //for navigationbar drawer
    public void openDrawer(View view) {
        drawerLayout.openDrawer(Gravity.LEFT);
    }


    //for current location and marker
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        getLonglat();
        if(latLng!= null){
            marker = map.addMarker(new MarkerOptions().position(latLng).title("Me"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15F));
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
    }

    //for check location with dialogbox
        private boolean checkLocation() {
                if (!isLocationEnabled()){
                        showAlert();
                }
                return isLocationEnabled();
        }

        private void showAlert() {
          final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
          dialog.setTitle("Enable Location")
                  .setMessage("Your Location setting is set to 'Off'. \nPlease Enable Location to "+
                          "use this app")
                  .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialogInterface, int i) {
                                  Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                  startActivity(intent);
                          }
                  })
                  .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialogInterface, int i) {

                          }
                  });
          dialog.show();
        }

        //for location enable to find current location
        private boolean isLocationEnabled() {
            manager = (LocationManager)  getSystemService(Context.LOCATION_SERVICE);
                return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }

        //for onetime location permission to access the current location
        private boolean requestSinglePermission() {
                Dexter.withActivity(this)
                        .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        .withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse response) {
                                        isPermission = true;
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                     if (response.isPermanentlyDenied()){
                                             isPermission = false;
                                     }
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                                }
                        }).check();
                return isPermission;
        }

        @Override
        protected void onStart() {
                super.onStart();

                if (googleApiClient !=null){
                        googleApiClient.connect();
                }
        }

        @Override
        protected void onStop() {
                super.onStop();
                if(googleApiClient.isConnected()){
                        googleApiClient.disconnect();
                }
        }


        //for location permission
    @Override
        public void onConnected(@Nullable Bundle bundle) {
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
        return;
                }
                startLocationUpdates();
        mLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if(mLocation == null){
                startLocationUpdates();
        }
        else {
           Toast.makeText(this, "Location Detected", Toast.LENGTH_SHORT).show();
        }
        }

        private void startLocationUpdates() {
          locationRequest = LocationRequest.create()
          .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

          if (ActivityCompat.checkSelfPermission(this,
                   Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                 ActivityCompat.checkSelfPermission(this,
                         Manifest.permission.ACCESS_COARSE_LOCATION) !=
                         PackageManager.PERMISSION_GRANTED)
                {
                        return;
                }
          LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);
        }

//for onclick
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imgAddAlarm) {
            final LatLng latLng = new LatLng(map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude);
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.layout_add_alarm);
            etName = dialog.findViewById(R.id.etName);
            etTask = dialog.findViewById(R.id.etTask);
            tvLatitude = dialog.findViewById(R.id.tvLatitude);
            tvLongitude = dialog.findViewById(R.id.tvLongitude);
            Button btnAdd = dialog.findViewById(R.id.btnAdd);
            Button btnCancel = dialog.findViewById(R.id.btnCancel);
            tvLatitude.setText("Latitude : " + latLng.latitude);
            tvLongitude.setText("Longitude : " + latLng.longitude);

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!validate()) return;
                    longLat.setNames(etName.getText().toString().trim());
                    longLat.setTask(etTask.getText().toString().trim());
                    longLat.setLongitude(tvLongitude.getText().toString().trim());
                    longLat.setLatitude(tvLatitude.getText().toString().trim());

                    databaseReference.child(String.valueOf(maxid+1)).setValue(longLat);
                    Toast.makeText(MapsActivity.this, "Alarm Added Successfully.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            dialog.show();

        }
        else if (v.getId() == R.id.navHome){
            startActivity(new Intent(this,MapsActivity.class));
            finish();
        }
        else if (v.getId() ==R.id.navTask){
            startActivity(new Intent(this,LocationlistActivity.class));
        }
        else if (v.getId() ==R.id.navSetting){
            startActivity(new Intent(this,SettingActivity.class));
        }
        else if (v.getId() ==R.id.navHelp){
            startActivity(new Intent(this,HelpActivity.class));
        }
        else if (v.getId() ==R.id.navAbout){
            startActivity(new Intent(this,AboutActivity.class));
        }
        else if (v.getId() ==R.id.navExit){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure want to Exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }
    }

    private boolean validate() {
        if (TextUtils.isEmpty(etName.getText().toString().trim())) {
            etName.setError("Name is required!!!");
            etTask.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(etTask.getText().toString().trim())) {
            etTask.setError("Task is required!!!");
            etTask.requestFocus();
            return false;
        }
        return true;
    }

    //for change location
    @Override
    public void onLocationChanged(Location location) {
        String msg = "Current Location: " +
                Double.toString(location.getLongitude()) + "," +
                Double.toString(location.getLatitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LongLat foundLocation = null;
        for (LongLat longLatL : longLatList) {
            Location pointLocation = new Location("");
            pointLocation.setLatitude(Double.parseDouble(longLatL.getLatitude()));
            pointLocation.setLongitude(Double.parseDouble(longLatL.getLongitude()));

            if(marker == null)return;
            marker.remove();
            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
            marker = map.addMarker(new MarkerOptions().position(myLocation).title("Me"));

            if (location.distanceTo(pointLocation) < radius) {
                if (vibration) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        v.vibrate(2000);
                    }
                }
                final MediaPlayer mPlayer = MediaPlayer.create(MapsActivity.this, R.raw.song);
                if (sound) {
                    mPlayer.start();
                }
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.layout_dismiss);
                dialog.setCancelable(false);
                TextView tvText = dialog.findViewById(R.id.tvText);
                Button btnDismiss = dialog.findViewById(R.id.btnDismiss);
                tvText.setText("You have reached " + longLatL.getNames());
                showNotification("Near to " + longLatL.getNames(), longLatL.getTask());
                btnDismiss.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        mPlayer.stop();
                        isDialogShowing = false;
                    }
                });
                dialog.show();
                isDialogShowing = true;
                foundLocation = longLatL;
                continue;
            }
        }

        if (foundLocation != null) {


            FirebaseDatabase.getInstance().getReference()
                    .child("LongLat")
                    .child(databaseReference.getRef().getKey())
                    .removeValue()
//                   .setValue(null)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MapsActivity.this, "Alarm Deleted Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
//            Url.getEndPoints().deletelonglat(cookie, foundLocation.get_id()).enqueue(new Callback<Void>() {
//                @Override
//                public void onResponse(Call<Void> call, Response<Void> response) {
//                    if(!response.isSuccessful()){
//                        Toast.makeText(DashboardActivity.this, "Failed to delete.", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<Void> call, Throwable t) {
//                    Toast.makeText(DashboardActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
            map.clear();
//            getlonglat();
            longLatList.remove(foundLocation);
        }
    }


    @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }

    private void showNotification(String title, String desc) {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = "Channel1";
            String description = "This is channel 1";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Channel1", name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Channel1")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(desc)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(1, builder.build());

    }
}