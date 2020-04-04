package aravind.com.rougetwo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import aravind.com.constants.ErrorConstants;
import aravind.com.constants.FireBaseConstants;
import aravind.com.util.HeatMapUtility;
import aravind.com.util.HeatMapUtilty2;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity implements ValueEventListener {

    private final int SPLASH_DISPLAY_LENGTH = 100;
    public DatabaseReference dat1;
    public DatabaseReference dat2;
    private ArrayList<LatLng> coordinates;
    private ArrayList<LatLng> hospitals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dat1 = FirebaseDatabase.getInstance().getReference();
        dat1.addListenerForSingleValueEvent(this);
}

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        coordinates = HeatMapUtility.readItems(dataSnapshot);
        hospitals= HeatMapUtilty2.readItems(dataSnapshot);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this, MapsActivity.class);
              mainIntent.putExtra("patient",coordinates);
              mainIntent.putExtra("hospitals",hospitals);
                startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Toast.makeText(SplashActivity.this, ErrorConstants.ERROR_FIREBASE_MSG, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this, MapsActivity.class);
                startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

}
