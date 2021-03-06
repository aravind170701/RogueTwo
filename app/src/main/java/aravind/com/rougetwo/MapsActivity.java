package aravind.com.rougetwo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import aravind.com.constants.ErrorConstants;
import aravind.com.constants.FireBaseConstants;
import aravind.com.util.HeatMapUtility;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnSuccessListener<Location>, ValueEventListener {

    public GoogleMap mMap;
    public DatabaseReference dat;
    public ArrayList<LatLng> coordinates;
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;
    private boolean loactionPermissionDenied = true;
    private int locationRequestCode = 1000;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    FloatingActionButton floatbtn;
    ArrayList<LatLng> hospitals=new ArrayList<>();
    ArrayList<LatLng> heatmapCoordinate=new ArrayList<>();
    int[] colorDark = {
            Color.rgb(102, 0, 0), // green
            Color.rgb(255, 0, 0)    // red
    };

    float[] startPoints = {
            0.2f, 1f
    };
    int[] colorLight = {
            Color.rgb(255, 153, 153), // green
            Color.rgb(0, 153, 0)    // red
    };
    ArrayList<WeightedLatLng> weighteHospitals=new ArrayList<>();
    Gradient gradientDefault = new Gradient(colorLight, startPoints);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        floatbtn = findViewById(R.id.floatbtn);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
   void form_weighted_coordinates()
   {
       for(LatLng hospital:hospitals)
       {
           weighteHospitals.add(new WeightedLatLng(hospital));
       }
   }
    private void addMarker(LatLng coordinate, String title) {
        mMap.addMarker(new MarkerOptions().position(coordinate).title(title));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 10));
    }

     public void addHeatMap(ArrayList<LatLng> patients,Gradient g) {
        if (!HeatMapUtility.isNullOrEmpty(patients)) {
            form_weighted_coordinates();
            HeatmapTileProvider h = new HeatmapTileProvider.Builder().data(patients).weightedData(weighteHospitals).gradient(g).build();

            mMap.addTileOverlay(new TileOverlayOptions().tileProvider(h));
        } else {
            Toast.makeText(MapsActivity.this, ErrorConstants.ERROR_HEATMAP_MSG, Toast.LENGTH_SHORT).show();
        }
    }
    public void addHeatMap(ArrayList<LatLng> patients) {
        if (!HeatMapUtility.isNullOrEmpty(patients)) {
            HeatmapTileProvider h = new HeatmapTileProvider.Builder().data(patients).build();
            mMap.addTileOverlay(new TileOverlayOptions().tileProvider(h));
        } else {
            Toast.makeText(MapsActivity.this, ErrorConstants.ERROR_HEATMAP_MSG, Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loactionPermissionDenied = false;
                    fusedLocationClient.getLastLocation().addOnSuccessListener(this);
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    //point to default coordinates
                    dat = FirebaseDatabase.getInstance().getReference().child("default").child("0");
                    dat.addListenerForSingleValueEvent(this);
                }
                break;
            }
        }
    }

    @Override
    public void onSuccess(Location location) {
        // Got last known location. In some rare situations this can be null.
        if (location == null) {
            Toast.makeText(MapsActivity.this, ErrorConstants.ERROR_LAST_LOCATION_MSG, Toast.LENGTH_SHORT).show();
            location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(0.0);
            location.setLongitude(0.0);
        }

        getListFromIntent();
        if (!HeatMapUtility.isNullOrEmpty(coordinates)) {
           find_nearby_patients();
            //add marker at last known location
            addMarker(new LatLng(location.getLatitude(), location.getLongitude()), "User's Location");
        } else {
            lastKnownLocation = location;

            /*Take the data from the OnDataChange Listener*/
            dat = FirebaseDatabase.getInstance().getReference();
            dat.addListenerForSingleValueEvent(this);

            //add marker at last known location
            addMarker(new LatLng(location.getLatitude(), location.getLongitude()), "User's Location");
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (lastKnownLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        locationRequestCode);

            } else {
                fusedLocationClient.getLastLocation().addOnSuccessListener(this);
            }
        }
    }

    private void getListFromIntent() {
        if (getIntent().getExtras() != null) {
            coordinates=(ArrayList<LatLng>) getIntent().getExtras().get("patient");
            hospitals=(ArrayList<LatLng>) getIntent().getExtras().get("hospitals");
        }
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        LatLng defaultCoordinates = null;
        if (loactionPermissionDenied) {
            double latitude = Double.parseDouble(dataSnapshot.child(FireBaseConstants.FIREBASE_KEY_LATITUDE).getValue().toString());
            double longitude = Double.parseDouble(dataSnapshot.child(FireBaseConstants.FIREBASE_KEY_LONGITUDE).getValue().toString());
            defaultCoordinates = new LatLng(latitude, longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultCoordinates, 6));
            coordinates = HeatMapUtility.readItems(dataSnapshot);
        } else {
            coordinates = HeatMapUtility.readItems(dataSnapshot);
        }
    }
    public double getDistance(LatLng LatLng1, LatLng LatLng2) {
        double distance = 0;
        Location locationA = new Location("A");
        locationA.setLatitude(LatLng1.latitude);
        locationA.setLongitude(LatLng1.longitude);
        Location locationB = new Location("B");
        locationB.setLatitude(LatLng2.latitude);
        locationB.setLongitude(LatLng2.longitude);
        distance = locationA.distanceTo(locationB);

        return distance;
    }
    // finding patients nearby a hospital
       void find_nearby_patients()
    {
        hospitals.add(new LatLng(19.2001756,72.9664046));
        for(LatLng hospital:hospitals) {
            addMarker(hospital,"Hospital");
            heatmapCoordinate.clear();
            if(coordinates!=null) {
                for (LatLng coordinate : coordinates) {
                    if (getDistance(hospital, coordinate) < 5000) {
                        heatmapCoordinate.add(coordinate);
                    }
                }
                if (heatmapCoordinate.size() >= 120) {
                    Gradient gradient1 = new Gradient(colorDark, startPoints);

                    addHeatMap(heatmapCoordinate, gradient1);
                } else {
                    Gradient gradient2 = new Gradient(colorLight, startPoints);

                    addHeatMap(heatmapCoordinate, gradient2);
                }
            }
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        addMarker(new LatLng(0.0, 0.0), "User's Location");
        Toast.makeText(MapsActivity.this, ErrorConstants.ERROR_FIREBASE_MSG, Toast.LENGTH_SHORT).show();
    }

    public void getlatestlocation(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);

        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this);
        }
    }
}
