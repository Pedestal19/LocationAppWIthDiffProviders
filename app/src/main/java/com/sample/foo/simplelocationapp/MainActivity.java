package com.sample.foo.simplelocationapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    double longitudeBest, latitudeBest;
    double longitudeGPS, latitudeGPS;
    double longitudeNetwork, latitudeNetwork;
    TextView longitudeValueBest, latitudeValueBest;
    TextView longitudeValueGPS, latitudeValueGPS;
    TextView longitudeValueNetwork, latitudeValueNetwork;

    double oldLongitudeBest=0;
    double oldLatitudeBest=0;


    private static final String TAG = "MainActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        longitudeValueBest = (TextView) findViewById(R.id.longitudeValueBest);
        latitudeValueBest = (TextView) findViewById(R.id.latitudeValueBest);
        longitudeValueGPS = (TextView) findViewById(R.id.longitudeValueGPS);
        latitudeValueGPS = (TextView) findViewById(R.id.latitudeValueGPS);
        longitudeValueNetwork = (TextView) findViewById(R.id.longitudeValueNetwork);
        latitudeValueNetwork = (TextView) findViewById(R.id.latitudeValueNetwork);

       /* String locationProvider = LocationManager.NETWORK_PROVIDER;
// Or use LocationManager.GPS_PROVIDER

        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
*/
    }

    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void toggleGPSUpdates(View view) {
        if(!checkLocation())
            return;
        Button button = (Button) view;
        if(button.getText().equals(getResources().getString(R.string.pause))) {
            locationManager.removeUpdates(locationListenerGPS);
            button.setText(R.string.resume);
        }
        else {
            locationManager.requestLocationUpdates(
//                    LocationManager.GPS_PROVIDER, 30 * 60 * 1000, 5, locationListenerGPS);
                    LocationManager.GPS_PROVIDER, 0, 0, locationListenerGPS);

            button.setText(R.string.pause);
        }
    }

    public void toggleBestUpdates(View view) {
        if(!checkLocation())
            return;
        Button button = (Button) view;
        if(button.getText().equals(getResources().getString(R.string.pause))) {
            locationManager.removeUpdates(locationListenerBest);
            button.setText(R.string.resume);
        }
        else {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_HIGH);
            String provider = locationManager.getBestProvider(criteria, true);
            if(provider != null) {
//                locationManager.requestLocationUpdates(provider, 2 * 60 * 1000, 10, locationListenerBest);
                locationManager.requestLocationUpdates(provider, 0, 0, locationListenerBest);

                button.setText(R.string.pause);
                Toast.makeText(this, "Best Provider is " + provider, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void toggleNetworkUpdates(View view) {
        if(!checkLocation())
            return;
        Button button = (Button) view;
        if(button.getText().equals(getResources().getString(R.string.pause))) {
            locationManager.removeUpdates(locationListenerNetwork);
            button.setText(R.string.resume);
        }
        else {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
            Toast.makeText(this, "Network provider started running", Toast.LENGTH_LONG).show();
            button.setText(R.string.pause);
        }
    }

    private final LocationListener locationListenerBest = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeBest = location.getLongitude();
            latitudeBest = location.getLatitude();

            Log.w(TAG, "==current longitude is "+longitudeBest);
            Log.w(TAG, "==current latitude is "+latitudeBest);

            Log.w(TAG, "***old longitude is "+oldLongitudeBest);
            Log.w(TAG, "***old latitude is "+oldLatitudeBest);

            if(oldLatitudeBest==0 && oldLongitudeBest==0){
                oldLatitudeBest = latitudeBest;
                oldLongitudeBest = longitudeBest;
                Log.e(TAG, "intial 1st point is null, initialising with gps");
                Log.e(TAG, "longitude is "+oldLongitudeBest);
                Log.e(TAG, "latitude is "+oldLatitudeBest);


            }

            float theDistance = distance(oldLatitudeBest,oldLongitudeBest,latitudeBest, longitudeBest);
            Log.e(TAG, "current calc of the distance is "+theDistance);

            /***/

            float[] results = new float[1];
            Location.distanceBetween(
                    latitudeBest,longitudeBest,
                    oldLatitudeBest, oldLongitudeBest, results);

            Log.w(TAG, "### Distance is: " + results[0]);

            /***/


            if(theDistance > 10){
                oldLatitudeBest = latitudeBest;
                oldLongitudeBest = longitudeBest;
                Log.e(TAG, "distance is greater than 10m"+theDistance);
                Toast.makeText(MainActivity.this, "distance is greater than 10m", Toast.LENGTH_SHORT).show();
//                addNotification("distance is greater than 10m", "");
            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    longitudeValueBest.setText(longitudeBest + "");
                    latitudeValueBest.setText(latitudeBest + "");
                    Toast.makeText(MainActivity.this, "Best Provider update", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private final LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeNetwork = location.getLongitude();
            latitudeNetwork = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    longitudeValueNetwork.setText(longitudeNetwork + "");
                    latitudeValueNetwork.setText(latitudeNetwork + "");
                    Toast.makeText(MainActivity.this, "Network Provider update", Toast.LENGTH_SHORT).show();

                   /* NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("GPS Location Update")
                            .setContentText(longitudeValueGPS +" : "+ latitudeValueGPS)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    String x = longitudeValueNetwork +" : "+ latitudeValueNetwork;
                    sendAuthorizationNotification(longitudeNetwork +" : "+ latitudeNetwork);*/

                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private final LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();

            Log.w(TAG, "==current longitude is "+longitudeGPS);
            Log.w(TAG, "==current latitude is "+latitudeGPS);

            Log.w(TAG, "***old longitude is "+oldLongitudeBest);
            Log.w(TAG, "***old latitude is "+oldLatitudeBest);

            if(oldLatitudeBest==0 && oldLongitudeBest==0){
                oldLatitudeBest = latitudeGPS;
                oldLongitudeBest = longitudeGPS;
                Log.e(TAG, "intial 1st point is null, initialising with gps");
                Log.e(TAG, "longitude is "+oldLongitudeBest);
                Log.e(TAG, "latitude is "+oldLatitudeBest);


            }

            float theDistance = distance(oldLatitudeBest,oldLongitudeBest,latitudeGPS, longitudeGPS);
            Log.e(TAG, "current calc of the distance is "+theDistance);

            /***/

            float[] results = new float[1];
            Location.distanceBetween(
                    latitudeGPS,longitudeGPS,
                    oldLatitudeBest, oldLongitudeBest, results);

            Log.w(TAG, "### Distance is: " + results[0]);

            /***/


            if(theDistance > 10){
                oldLatitudeBest = latitudeGPS;
                oldLongitudeBest = longitudeGPS;
                Log.e(TAG, "distance is greater than 10m"+theDistance);
                Toast.makeText(MainActivity.this, "distance is greater than 10m", Toast.LENGTH_SHORT).show();
                addNotification("distance is greater than 10m", "");
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    longitudeValueGPS.setText(longitudeGPS + "");
                    latitudeValueGPS.setText(latitudeGPS + "");
                    Toast.makeText(MainActivity.this, "GPS Provider update", Toast.LENGTH_SHORT).show();

                   /* NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("GPS Location Update")
                            .setContentText(longitudeValueGPS +" : "+ latitudeValueGPS)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);*/

                    addNotification(longitudeGPS +" : "+ latitudeGPS, "GPS Coords []");

                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


    public void sendAuthorizationNotification(String message) {


        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;
        Notification n = new Notification.Builder(getApplicationContext())
                .setContentTitle(message)
                .setContentText(message)
                .setTicker("User Authorized!")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setDefaults(defaults)
                .setAutoCancel(true).build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);



    }

    private void addNotification(String msg, String title) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(msg);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }


    private float distance(double latA, double lngA, double latB, double lngB){
        Location locationA = new Location("point A");

        locationA.setLatitude(latA);
        locationA.setLongitude(lngA);

        Location locationB = new Location("point B");

        locationB.setLatitude(latB);
        locationB.setLongitude(lngB);

        float distance = locationA.distanceTo(locationB);

        return distance;
    }



}
