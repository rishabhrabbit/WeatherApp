package com.example.LiveWeather.repository;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.LiveWeather.Model.Example;
import com.example.LiveWeather.Request.ApiRequest;
import com.example.LiveWeather.Request.RetrofitRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {

    public ApiRequest apiRequest;

    public WeatherRepository() {
        apiRequest = RetrofitRequest.getRetrofitInstance().create(ApiRequest.class);
    }


    public LiveData<Example> getWeatherData(double lat, double lon) {
        final MutableLiveData<Example> data = new MutableLiveData<>();
        apiRequest.getWeatherData(lat, lon).enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {
                data.setValue(response.body());
                Log.d("XXXX",response.body().toString());
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {

            }
        });
        return data;
    }
}
