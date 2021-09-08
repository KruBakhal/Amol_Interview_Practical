package com.amol.interview_practical.Repository.Callback;

import androidx.lifecycle.MutableLiveData;

import com.amol.interview_practical.Model.api.MainData;

public interface Network_Retrofit_CallBack_Interface {

    public void onCallbackMainReponnse(MutableLiveData<MainData> list);

    public void onCallbackMainReponnsePaginations(MutableLiveData<MainData> list);

    public void onShowProgress();

    public void onDatA_Load_Progress();

    public void onDatA_Dismiss_Progress();

    public void onDimissProgress();

    void onCanceledProgress(String message);

    void onCanceledPaginations(String message);


}