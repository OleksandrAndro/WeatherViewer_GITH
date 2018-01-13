package com.carrotholding.CENMiner;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Admin on 29.11.2017.
 */

public interface MyRetrofit {
    @GET("giveNearestDeviceOnline")
    Call<ResponseBody> getData(@Query("Lat") Double Lat, @Query("Lon") Double Lon, @Query("ttlTime") int ttlTime);
    //Call<List<POJOUmora>> getData ();
}
