package com.izheer.proxyretrofitdemo.customretrofits;

import com.izheer.proxyretrofitdemo.customretrofits.annotions.Field;
import com.izheer.proxyretrofitdemo.customretrofits.annotions.GET;
import com.izheer.proxyretrofitdemo.customretrofits.annotions.POST;
import com.izheer.proxyretrofitdemo.customretrofits.annotions.Query;

import okhttp3.Call;

public interface CustomServiceApi {

    @POST("/v3/weather/weatherInfo")
    Call postWeather(@Field("city") String city, @Field("key") String key);

    @GET("/v3/weather/weatherInfo")
    Call getWeather(@Query("city") String city, @Query("key") String key);

}
