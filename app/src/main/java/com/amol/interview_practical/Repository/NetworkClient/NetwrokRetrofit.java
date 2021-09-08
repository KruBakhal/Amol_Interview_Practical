package com.amol.interview_practical.Repository.NetworkClient;


import com.amol.interview_practical.Model.api.MainData;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;


public class NetwrokRetrofit {

    String BASE_URL = "https://reqres.in/api/";

    public ApiInterface createNetworkInstance() {

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        return retrofit.create(ApiInterface.class);
    }

    public interface ApiInterface {
        @GET("users")
        Call<MainData> getDataPage(@Query("page") String page_number, @Query("per_page") String size);

        @GET("users")
        Call<MainData> getData();
    }
}