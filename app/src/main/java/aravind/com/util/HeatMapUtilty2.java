package aravind.com.util;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import aravind.com.constants.FireBaseConstants;

public class HeatMapUtilty2 {
    public static ArrayList<LatLng> readItems(DataSnapshot childrenSnapShot) {
        ArrayList<LatLng> latLongList = new ArrayList<>();
        double latitude;
        double longitude;
        if (childrenSnapShot != null) {
            Log.i("INFO", "Count: " + childrenSnapShot.getChildrenCount());
            Iterator<DataSnapshot> it=childrenSnapShot.getChildren().iterator();
            DataSnapshot firstLevelChildSnapShot = it.next();

            if (childrenSnapShot.getChildren().iterator()!= null) {
                Log.i("INFO", "Children Count: " + firstLevelChildSnapShot.getChildrenCount());
                for (DataSnapshot secondLevelSnapShot : firstLevelChildSnapShot.getChildren()) {
                    latitude = Double.parseDouble((String) secondLevelSnapShot.child(FireBaseConstants.FIREBASE_KEY_LATITUDE).getValue());
                    longitude = Double.parseDouble((String) secondLevelSnapShot.child(FireBaseConstants.FIREBASE_KEY_LONGITUDE).getValue());

                    LatLng coordinates = new LatLng(latitude, longitude);
                    latLongList.add(coordinates);
                }
            }
        }
        return latLongList;
    }

    public static boolean isNullOrEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }
}
