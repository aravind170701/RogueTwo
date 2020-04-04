package aravind.com.rougetwo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

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
    public List<LatLng> coordinates;
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;
    private boolean loactionPermissionDenied = true;
    private int locationRequestCode = 1000;
    private double wayLatitude = 0.0, wayLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void addMarker(LatLng coordinate, String title) {
        mMap.addMarker(new MarkerOptions().position(coordinate).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 6));
    }

    public void addHeatMap() {
        if (!HeatMapUtility.isNullOrEmpty(coordinates)) {
            HeatmapTileProvider h = new HeatmapTileProvider.Builder().data(coordinates).build();
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

        coordinates = getListFromIntent();
        if (!HeatMapUtility.isNullOrEmpty(coordinates)) {

            addHeatMap();

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

    private ArrayList<LatLng> getListFromIntent() {
        if (getIntent().getExtras() != null) {
            return (ArrayList<LatLng>) getIntent().getExtras().get(FireBaseConstants.FIREBASE_DATA);
        }
        return null;
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        LatLng defaultCoordinates = null;
        if (loactionPermissionDenied) {
            double latitude = Double.parseDouble(dataSnapshot.child(FireBaseConstants.FIREBASE_KEY_LATITUDE).getValue().toString());
            double longitude = Double.parseDouble(dataSnapshot.child(FireBaseConstants.FIREBASE_KEY_LONGITUDE).getValue().toString());
            defaultCoordinates = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultCoordinates, 6));
            coordinates = HeatMapUtility.readItems(dataSnapshot);
            addHeatMap();
        } else {
            coordinates = HeatMapUtility.readItems(dataSnapshot);

            //Calling the HeatMap Method to plot the details
            addHeatMap();
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        addMarker(new LatLng(0.0, 0.0), "User's Location");
        Toast.makeText(MapsActivity.this, ErrorConstants.ERROR_FIREBASE_MSG, Toast.LENGTH_SHORT).show();
    }
}
