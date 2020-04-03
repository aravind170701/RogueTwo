package aravind.com.util;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.firebase.database.DataSnapshot;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;

import aravind.com.constants.FireBaseConstants;

public class HeatMapUtility {

    public static ArrayList<LatLng> readItems(DataSnapshot childrenSnapShot) {
        ArrayList<LatLng> latLongList = new ArrayList<>();
        double latitude;
        double longitude;
        if (childrenSnapShot != null) {
            for (DataSnapshot firstLevelChildSnapShot : childrenSnapShot.getChildren()) {
                if (firstLevelChildSnapShot != null) {
                    for (DataSnapshot secondLevelSnapShot : firstLevelChildSnapShot.getChildren()) {
                        latitude = (double) secondLevelSnapShot.child(FireBaseConstants.FIREBASE_KEY_LATITUDE).getValue();
                        longitude = (double) secondLevelSnapShot.child(FireBaseConstants.FIREBASE_KEY_LONGITUDE).getValue();

                        LatLng coordinates = new LatLng(latitude, longitude);
                        latLongList.add(coordinates);
                    }
                }
            }
        }
        return latLongList;
    }

    public static boolean isNullOrEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }
}
