package com.example.LiveWeather.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.LiveWeather.MainActivity;
import com.example.LiveWeather.Model.Example;
import com.example.LiveWeather.repository.WeatherRepository;

public class WeatherViewModel extends AndroidViewModel {

    private WeatherRepository weatherRepository;
    private LiveData<Example> exampleLiveData;

    public WeatherViewModel(@NonNull  Application application) {
        super(application);
        String name = "delhi";
        double l,l1;
        l= MainActivity.getInstance().get_lat_lon("lat");
        l1= MainActivity.getInstance().get_lat_lon("lon");
        weatherRepository = new WeatherRepository();
        this.exampleLiveData = weatherRepository.getWeatherData(l, l1);
    }

    public LiveData<Example> getExampleLiveData(){
        return exampleLiveData;
    }

}
