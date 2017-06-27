package com.example.android.gps_location;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    final static int M_LOCATION_PERMISSION = 10;

    TextView mTextView;
    LocationManager locationManager;
    LocationListener locationListener;


    SensorManager sensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    SensorEventListener sensorEventListener;

    Float azimut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.tv_location);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mTextView.setText("Latitude:" + location.getLatitude() + "\nLongitude:" + location.getLongitude());
                Log.d(this.getClass().toString(), "Latitude:" + location.getLatitude() + "\nLongitude:" + location.getLongitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        sensorEventListener = new SensorEventListener() {
            float[] mGravity;
            float[] mGeomagnetic;
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {


                if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                    mGravity = sensorEvent.values;
                if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                    mGeomagnetic = sensorEvent.values;
                if (mGravity != null && mGeomagnetic != null) {
                    float R[] = new float[9];
                    float I[] = new float[9];
                    boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                    if (success) {
                        float orientation[] = new float[3];
                        SensorManager.getOrientation(R, orientation);
                        azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                        Log.d(this.getClass().toString(), "Azimut: " + azimut*180/Math.PI + "\n Roll:" + orientation[2]*180/Math.PI);
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };


        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                }, M_LOCATION_PERMISSION);
            }
            else{
                getLocation();
            }
        }
        else{
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case M_LOCATION_PERMISSION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getLocation();
                }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
    }

    private void getLocation() {

        try {
            locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
    }
}
