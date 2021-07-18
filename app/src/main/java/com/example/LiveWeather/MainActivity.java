package com.example.LiveWeather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.LiveWeather.ViewModel.WeatherViewModel;
import com.example.weatherresponsecheck.R;
import com.example.LiveWeather.repository.WeatherRepository;

import com.example.weatherresponsecheck.databinding.ActivityMainBinding;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.google.gson.reflect.TypeToken.get;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding activityMainBinding;
    private WeatherViewModel viewModel;
    private TextView temp, desc, icon, feels_like, min, max, sunset, sunrise;
    private Button btn;
    String city_name;
    private Menu menu;
    String lat, lon;
    final int REQUESTCODE = 1;
    LocationCallback locationCallback;
    TextView error;
    FusedLocationProviderClient fusedLocationClient;
    private int REQUEST_CHECK_SETTINGS = 123;

    private static MainActivity instance;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = activityMainBinding.getRoot();
        setContentView(view);
        instance=this;
        Initialisations();
        getPermission();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                lat = String.valueOf(locationResult.getLastLocation().getLatitude());
                lon = String.valueOf(locationResult.getLastLocation().getLongitude());
                fusedLocationClient.removeLocationUpdates(locationCallback);
                save_lat_lon();

            }

        };
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callNow();
            }
        });
    }
    public static MainActivity getInstance() {
        return instance;
    }

    public void Initialisations(){
        temp = activityMainBinding.temp;
        desc = activityMainBinding.WD;
        icon = activityMainBinding.WeatherIcon;
        feels_like = activityMainBinding.feelsLikeCur;
        min = activityMainBinding.minTempCur;
        max = activityMainBinding.maxTempCur;
        sunset = activityMainBinding.sunset;
        sunrise = activityMainBinding.sunrise;
        btn = activityMainBinding.button;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableGPS();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUESTCODE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUESTCODE && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            enableGPS();
        }
        else{
            Toast.makeText(this, "Location Permission is Required", Toast.LENGTH_SHORT).show();
            getPermission();
        }
    }
    private void enableGPS() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Toast.makeText(MainActivity.this, "Hello", Toast.LENGTH_SHORT).show();
                setUpLocationListener();
            }
        });


        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                    }
                }
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode== RESULT_OK){
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            setUpLocationListener();
        }
        else{
            Toast.makeText(this, "Gps is Required", Toast.LENGTH_SHORT).show();
            enableGPS();
        }
    }

    @SuppressLint("MissingPermission")
    private void setUpLocationListener() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000)
                .setFastestInterval(2000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void save_lat_lon() {
        SharedPreferences sharedPreferences = getSharedPreferences(sfName.sharedPreferencename, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lat", lat);
        editor.putString("lon", lon);
        editor.apply();
    }

    public double get_lat_lon(String lat){
        SharedPreferences sharedPreferences = getSharedPreferences(sfName.sharedPreferencename,MODE_PRIVATE);
        String la = sharedPreferences.getString("lat","");
        String lo = sharedPreferences.getString("lon", "");
        Double l = Double.parseDouble(la);
        Double l1 = Double.parseDouble(lo);
        if(lat=="lat")return l;
        return l1;
    }

    public void callNow(){
        viewModel = new ViewModelProvider(this, new ViewModelProvider
                .AndroidViewModelFactory(getApplication())).get(WeatherViewModel .class);

        viewModel.getExampleLiveData().observe( this, example -> {
            if (example!=null && example.getMain()!=null){
                temp.setText(example.getMain().getTemp().toString());
                desc.setText((CharSequence) example.weather.get(0).getDescription().toUpperCase());
                icon.setText((CharSequence) example.weather.get(0).getIcon());
                feels_like.setText(example.getMain().getFeelsLike().toString());
                min.setText(example.getMain().getTempMin().toString());
                max.setText(example.getMain().getTempMax().toString());
                Long sunriseT = example.getSys().getSunrise();
                Date dt = new Date (sunriseT * 1000);
                SimpleDateFormat sfd = new SimpleDateFormat("HH:mm:ss");
                sunrise.setText(sfd.format(dt));
                Long sunsetT = example.getSys().getSunset();
                Date dt1 = new Date (sunsetT * 1000);
                SimpleDateFormat sfd1 = new SimpleDateFormat("HH:mm:ss");
                sunset.setText(sfd1.format(dt1));
                city_name = example.getName();
                menu.getItem(0).setTitle(city_name);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.city:
                return true;
            default:
                // Do nothing
        }
        return super.onOptionsItemSelected(item);
    }

}