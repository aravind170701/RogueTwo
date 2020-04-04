package aravind.com.rougetwo;

import android.location.Location;
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

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import aravind.com.constants.ErrorConstants;
import aravind.com.util.HeatMapUtility;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnSuccessListener<Location>, ValueEventListener {

    public GoogleMap mMap;
    public DatabaseReference dat;
    public List<LatLng> coordinates;
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;


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


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (lastKnownLocation == null) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this);
        }
    }

    private void addMarker(LatLng coordinate, String title) {
        mMap.addMarker(new MarkerOptions().position(coordinate).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 14));
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
    public void onSuccess(Location location) {
        // Got last known location. In some rare situations this can be null.
        if (location != null) {
            lastKnownLocation = location;
            /*Take the data from the OnDataChange Listener*/
            dat = FirebaseDatabase.getInstance().getReference();
            dat.addListenerForSingleValueEvent(this);

            //add marker at last known location
            addMarker(new LatLng(location.getLatitude(), location.getLongitude()), "User's Location");
        } else {
            Toast.makeText(MapsActivity.this, ErrorConstants.ERROR_LAST_LOCATION_MSG, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        coordinates = HeatMapUtility.readItems(dataSnapshot);

        //Calling the HeatMap Method to plot the details
        addHeatMap();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Toast.makeText(MapsActivity.this, ErrorConstants.ERROR_FIREBASE_MSG, Toast.LENGTH_SHORT).show();
    }
}

