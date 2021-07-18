package com.example.LiveWeather.Request;

import com.example.LiveWeather.Model.Example;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiRequest {
    @GET("weather?appid=e60ca6ecb804babbd7e4e97fb213110e&units=metric")
    Call<Example>getWeatherData(@Query("lat") double lat, @Query("lon") double lon);
}
