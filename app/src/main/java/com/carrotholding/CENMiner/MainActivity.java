package com.carrotholding.CENMiner;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
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

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;



public class MainActivity extends AppCompatActivity{
    private LocationManager locationManager;
    TextView textView3;
    TextView textView4;
    public static MyRetrofit myRetrofit;
    public Retrofit retrofit;
    HttpLoggingInterceptor logging;
    private boolean providerStatus;
    Double lat;
    Double longit;
    String finalCoord = null;
    Timer timer1;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    ProgressDialog dialog;
    Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        textView3 = (TextView) findViewById(R.id.textView3);
        textView4 = (TextView) findViewById(R.id.textView4);

        logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        final OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
                .followRedirects(true)
                .addInterceptor(logging);


        retrofit = new Retrofit.Builder()
                //.baseUrl("http://www.umori.li/api/")
                .baseUrl("http://weather-collector.dyndns.org:8080/api/")
                //.baseUrl("https://private-anon-5e528e661b-samplebookssetrestapi.apiary-mock.com/v1/books/")
                .client(okHttpClient.build())
                .build();

        h = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1)
                showDialogGET();
                else if(msg.what == 2)
                showToastNoCoord();
                super.handleMessage(msg);
            }
        };

    }

    /*@Override
    protected void onResume() {
        super.onResume();
        updateLocation();
    }*/

    @Override
    protected void onPause() {
        super.onPause();
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
        locationManager.removeUpdates(locationListenerGPS);
        locationManager.removeUpdates(locationListenerNetwork);
    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.imageButton3:

                providerStatus = (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) || (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

                /*if (!providerStatus)
                    onClickLocationSettings();*/  //check performing in updateLocation

               if (!hasConnection(MainActivity.this)) {

                Toast toast = Toast.makeText(getApplicationContext(), "Please, turn on WIFI or Mobile data", Toast.LENGTH_LONG);
                toast.show();
            } else {
                updateLocation();


            }
               /* retrofit = new Retrofit.Builder()
                        //.baseUrl("http://www.umori.li/api/")
                        .baseUrl("http://weather-collector.dyndns.org:8080/api/giveNearestDevice/")
                        //.baseUrl("https://private-anon-5e528e661b-samplebookssetrestapi.apiary-mock.com/v1/books/")
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .build();*/


                // Controller();

                /*Uri address = Uri.parse("http://statistics.carrot.network/carrot-miner-25-dashboard.html");
                Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
                startActivity(openlinkIntent);*/
                break;

            case R.id.buttontest:
                lat = 0.0;
                longit = 0.0;
                showDialogGET();
                Controller();
        }
    }

    final LocationListener locationListenerGPS = new LocationListener() {    //listener for GPS
        @Override
        public void onLocationChanged(Location location) {
            // showLocation(location);
            timer1.cancel();
            lat = location.getLatitude();
            longit = location.getLongitude();
            dialog.dismiss();
            showDialogGET();
            Controller();
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
            timer1.cancel();
            lat = location.getLatitude();
            longit = location.getLongitude();
            dialog.dismiss();
            showDialogGET();
            Controller();
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

    private void showLocation(Location location) {
        if (location == null)

        {
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


        } else if (location.getProvider().equals(LocationManager.GPS_PROVIDER))

        {
            finalCoord = formatLocation(location);
            lat = location.getLatitude();
            longit = location.getLongitude();
            textView4.setText(finalCoord + "gps");
        } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER))

        {
            finalCoord = formatLocation(location);
            lat = location.getLatitude();
            longit = location.getLongitude();
            textView4.setText(finalCoord + "net");
        }


    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()));
    }

    private void Controller() {

        myRetrofit = retrofit.create(MyRetrofit.class);
        Call<ResponseBody> body = myRetrofit.getData(lat, longit);
        //final frag2 mFrag2 = new frag2();
        body.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String responce = response.body().string();
                    textView3.setText(responce);
                    Uri address = Uri.parse(responce);
                    dialog.dismiss();
                    Intent openlinkIntent = new Intent(MainActivity.this, WebView.class);
                    openlinkIntent.putExtra("URL", responce);
                    startActivity(openlinkIntent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

    public void onClickLocationSettings() {
        startActivity(new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

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

    public void updateLocation() {

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
            Toast toast = Toast.makeText(getApplicationContext(), "Please, turn on location detection", Toast.LENGTH_LONG);
            toast.show();
            onClickLocationSettings();
            return;
        }

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

        if(network_enabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGPS);
        }
        timer1=new Timer();
        showDialogGPS();
        timer1.schedule(new GetLastLocation(), 12000);
    }


    public void showToastNoCoord () {
        Toast toast = Toast.makeText(getApplicationContext(), "Current location is not available", Toast.LENGTH_LONG);
        toast.show();
    }

    private void showDialogGPS() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait, location detection...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.show();
    }

    private void showDialogGET() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait, sending a request to the server...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.show();
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

            Location net_loc=null, gps_loc=null;
            if(gps_enabled)

            {
                gps_loc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            }

            if(network_enabled)

            {
                net_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            }

            //if there are both values use the latest one
            if(gps_loc!=null && net_loc!=null){
                if(gps_loc.getTime()>net_loc.getTime()) {
                    lat = gps_loc.getLatitude();
                    longit = gps_loc.getLongitude();
                   dialog.dismiss();
                    h.sendEmptyMessage(1);
                    Controller();
                }
                else
                {
                    lat = net_loc.getLatitude();
                    longit = net_loc.getLongitude();
                    dialog.dismiss();
                    h.sendEmptyMessage(1);
                    Controller();
                }
                return;
            }

            if(gps_loc!=null){
                lat = gps_loc.getLatitude();
                longit = gps_loc.getLongitude();
                dialog.dismiss();
                h.sendEmptyMessage(1);
               Controller();
                return;
            }
            if(net_loc!=null){
                lat = net_loc.getLatitude();
                longit = net_loc.getLongitude();
                dialog.dismiss();
                h.sendEmptyMessage(1);
               Controller();
                return;
            }
            //mainActivity.showToastNoCoord();

            lat = 0.0;
            longit = 0.0;
            dialog.dismiss();
            h.sendEmptyMessage(1);
            Controller();
            //h.sendEmptyMessage(2);
        }
    }



}
