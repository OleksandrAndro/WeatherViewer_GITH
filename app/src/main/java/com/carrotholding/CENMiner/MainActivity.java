package com.carrotholding.CENMiner;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.chromium.mojo.system.AsyncWaiter;
import org.xwalk.core.XWalkView;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    private LocationManager locationManager;
    TextView textView3;
    Timer timer1;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    boolean doubleBackToExitPressedOnce = false;
    ProgressDialog dialog;
    Handler h;
    Call<ResponseBody> body;
    Context context = MainActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        textView3 = (TextView) findViewById(R.id.textView3);


        h = new Handler(Looper.myLooper()) {                              //it's handler for toast messages from separate -
            //- flow for TimerTask with returning last geoposition
            //and informing user about current action
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1)                                        //showing ProgressDialog with GET Response
                    showDialogGET();
                else if (msg.what == 2)
                    showToastNoCoord();                                      //showing toast about no awaliable and previous geoposition
                super.handleMessage(msg);
            }
        };

    }

    @Override
    protected void onPause() {
        super.onPause();
        freezing();
    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.imageButton3:                              //start button

                boolean providerStatus = (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) || (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

                if (!hasConnection(MainActivity.this)) {

                    Toast toast = Toast.makeText(getApplicationContext(), R.string.toastTurnOnWiFi, Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    checkStatusGeoposition();
                }
                break;

            case R.id.buttontest:                                  //debugging show button
                showDialogGET();
                Controller(0.0, 0.0);
                break;
        }
    }

    final LocationListener locationListenerGPS = new LocationListener() {    //listener for GPS
        @Override
        public void onLocationChanged(Location location) {
            timer1.cancel();                                                //stopping timer for waiting current geoposition
            dialog.dismiss();
            showDialogGET();
            Controller(location.getLatitude(), location.getLongitude());                                                  //go to REST client for requesting coordinates to server
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            //canceling all updates
            locationManager.removeUpdates(locationListenerGPS);
            locationManager.removeUpdates(locationListenerNetwork);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }
    };

    final LocationListener locationListenerNetwork = new LocationListener() {   //listener for NETWORK
        @Override
        public void onLocationChanged(Location location) {
            timer1.cancel();                                                    //stopping timer for waiting current geoposition
            //lat = location.getLatitude();
            //longit = location.getLongitude();
            dialog.dismiss();
            showDialogGET();
            Controller(location.getLatitude(), location.getLongitude());                                                       //go to REST client for requesting coordinates to server
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(locationListenerGPS);                //debugging show button
            locationManager.removeUpdates(locationListenerNetwork);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }
    };

    private void Controller(double lat, double longit) {                                        //REST client

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();                  //it's debuging line
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);     //-----------------

        final OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .followRedirects(true)                           //it's important line for current web-resource
                .addInterceptor(logging);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://weather-collector.dyndns.org:8080/api/")   //it's root web way
                .client(okHttpClient.build())
                .build();
        MyRetrofit myRetrofit = retrofit.create(MyRetrofit.class);
            body = myRetrofit.getData(lat, longit);
            body.enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String responce = response.body().string();
                        textView3.setText(responce);                    //debbuging line for hide textView
                        dialog.dismiss();                               //hide progressDialog waiting response from server
                        Intent openlinkIntent = new Intent(MainActivity.this, WebView.class);
                        openlinkIntent.putExtra("URL", responce);       //putting URL to intent for WebActivity
                        startActivity(openlinkIntent);                  //opening WebView activity
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast errorURL = Toast.makeText(getApplicationContext(), R.string.toastFailedResponse, Toast.LENGTH_LONG);
                    errorURL.show();
                    dialog.dismiss();
                    t.printStackTrace();
                }
            });

    }

    public void onClickLocationSettings() {                        //opening settings activity for turn on catching geoposition
        startActivity(new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    //check avaliable Internet-connection with some deprecated methods
    public static boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public void checkStatusGeoposition() {

        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        //don't start listeners if no provider is enabled
        if (!gps_enabled & !network_enabled) {
            Toast toast = Toast.makeText(getApplicationContext(), R.string.toastTurnOnLocationDetection, Toast.LENGTH_LONG);
            toast.show();
            onClickLocationSettings();
            return;
        }

        //starting UpdatePosition if GPS or NETWORK location provider turn on
        if (gps_enabled) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);

        }

        if (network_enabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGPS);
        }

        //starting timer for waiting determine time - 12 seconds for catching geoposition
        //if geoposition not catched for that determine time - go to method
        //GetLastLocation
        timer1 = new Timer();
        showDialogGPS();
        timer1.schedule(new GetLastLocation(), 12000);
    }

    @Override
    //if back button is pressed - cancel timer for catching geoposition and hide progress dialog
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.toastClickBackToExit, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);

    }

    class GetLastLocation extends TimerTask {

        @Override
        public void run() {
                //dialog.dismiss();
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.removeUpdates(locationListenerGPS);
                locationManager.removeUpdates(locationListenerNetwork);

                Location net_loc = null, gps_loc = null;
                if (gps_enabled)

                {
                    gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                }

                if (network_enabled)

                {
                    net_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                }

                //if there are both values use the latest one
                if (gps_loc != null && net_loc != null) {
                    if (gps_loc.getTime() > net_loc.getTime()) {
                        //lat = gps_loc.getLatitude();
                        //longit = gps_loc.getLongitude();
                        dialog.dismiss();
                        h.sendEmptyMessage(1);
                        Controller(gps_loc.getLatitude(), gps_loc.getLongitude());
                    } else {
                        //lat = net_loc.getLatitude();
                        //longit = net_loc.getLongitude();
                        dialog.dismiss();
                        h.sendEmptyMessage(1);
                        Controller(net_loc.getLatitude(), net_loc.getLongitude());
                    }
                    return;
                }

                if (gps_loc != null) {
                    //lat = gps_loc.getLatitude();
                    //longit = gps_loc.getLongitude();
                    dialog.dismiss();
                    h.sendEmptyMessage(1);
                    Controller(gps_loc.getLatitude(), gps_loc.getLongitude());
                    return;
                }
                if (net_loc != null) {
                    //lat = net_loc.getLatitude();
                    //longit = net_loc.getLongitude();
                    dialog.dismiss();
                    h.sendEmptyMessage(1);
                    Controller(net_loc.getLatitude(), net_loc.getLongitude());
                    return;
                }
                //mainActivity.showToastNoCoord();

                dialog.dismiss();
                h.sendEmptyMessage(1);
                Controller(0.0, 0.0);
                //h.sendEmptyMessage(2);
            }

        }


    public void showToastNoCoord () {
        Toast toast = Toast.makeText(getApplicationContext(), R.string.toastLocationNotAvaliable, Toast.LENGTH_LONG);
        toast.show();
    }

    private void showDialogGPS() {
        dialog = new ProgressDialog(this);
        dialog.setMessage(context.getResources().getString(R.string.progressDialogLocDetection));
        String f = String.valueOf(R.string.progressDialogLocDetection);
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                freezing();
            }
        });
        dialog.show();
    }

    private void showDialogGET() {
        dialog = new ProgressDialog(this);
        dialog.setMessage(context.getResources().getString(R.string.progressDialogSendRequest));
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (body != null)
                body.cancel();
            }
        });
        dialog.show();
    }

    private void freezing(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(locationListenerGPS);         //turning off updates geoposition
        locationManager.removeUpdates(locationListenerNetwork);     //turning off updates geoposition
        if (timer1 != null) {
            timer1.cancel();                                       //stopping timer
            dialog.dismiss();
        }
    }

}
