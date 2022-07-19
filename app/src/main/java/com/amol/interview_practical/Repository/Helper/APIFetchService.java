package com.amol.interview_practical.Repository.Helper;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.amol.interview_practical.Model.api.MainData;
import com.amol.interview_practical.Repository.NetworkClient.NetwrokRetrofit;
import com.amol.interview_practical.Repository.Callback.Network_Retrofit_CallBack_Interface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class APIFetchService {

    Network_Retrofit_CallBack_Interface networkInterface;
    int cout = 0;
    APIFetchService fetchRepository = null;

    public APIFetchService getFetchRepositoryInstance(Network_Retrofit_CallBack_Interface param) {
        if (fetchRepository == null) {
            fetchRepository = new APIFetchService();
            networkInterface = param;
            cout++;
            Log.d("TAG", "getMainResponse: $cout");
        }
        return fetchRepository;
    }


    public void fecthDataFromNetworkPage(int page, int offset) {
        networkInterface.onDatA_Load_Progress();


        Call mainData = new NetwrokRetrofit().createNetworkInstance()
                .getDataPage("" + page, "" + offset);

        mainData.enqueue(new Callback<MainData>() {

            @Override
            public void onResponse(Call<MainData> call, Response<MainData> response) {
                MutableLiveData<MainData> list = new MutableLiveData<MainData>();
                list.setValue(response.body());
                networkInterface.onCallbackMainReponnsePaginations(list);

            }

            @Override
            public void onFailure(Call<MainData> call, Throwable t) {
                Log.d("TAG", "onFailure: " + t.getMessage());
//                networkInterface.onCanceledProgress(t.getMessage());

                networkInterface.onCanceledPaginations("");
            }
        });

    }

    public void fecthDataFromNetwork() {
        networkInterface.onShowProgress();

        Call mainData = new NetwrokRetrofit().createNetworkInstance()
                .getData();

        mainData.enqueue(new Callback<MainData>() {

            @Override
            public void onResponse(Call<MainData> call, Response<MainData> response) {
                MutableLiveData<MainData> list = new MutableLiveData<MainData>();
                list.setValue(response.body());
                networkInterface.onDimissProgress();
                networkInterface.onCallbackMainReponnse(list);

            }

            @Override
            public void onFailure(Call<MainData> call, Throwable t) {
                Log.d("TAG", "onFailure: " + t.getMessage());
                networkInterface.onCanceledProgress(t.getMessage());

            }
        });

    }


}