package com.amol.interview_practical.ViewModel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.amol.interview_practical.MainActivity;
import com.amol.interview_practical.Model.ConnectionModel;
import com.amol.interview_practical.Model.ProgressUIType;
import com.amol.interview_practical.Model.User;
import com.amol.interview_practical.Model.api.Datum;
import com.amol.interview_practical.Model.api.MainData;
import com.amol.interview_practical.Repository.Database.AppDatabase;
import com.amol.interview_practical.Repository.Database.AppExecutors;
import com.amol.interview_practical.Repository.Helper.APIFetchService;
import com.amol.interview_practical.Repository.Helper.InternetConnectivityLiveData;
import com.amol.interview_practical.Repository.Callback.Network_Retrofit_CallBack_Interface;

import java.util.ArrayList;
import java.util.List;

import static com.amol.interview_practical.MainActivity.checkConnection;

public final class FetchDataRepository extends ViewModel implements Network_Retrofit_CallBack_Interface {

    private AppDatabase mDb;
    public MutableLiveData<MainData> currentFecthData = null;
    public boolean loading = false;
    MutableLiveData<Boolean> internetConnectivity;
    InternetConnectivityLiveData connecLiveData;
    public MutableLiveData<ProgressUIType> showProgressBar = new MutableLiveData<>(ProgressUIType.DIMISS);
    private APIFetchService apiFetchService;
    MutableLiveData<List<Datum>> listUserData = new MutableLiveData<>();
    public int page = 1;
    public int offset = 5;
    public int totalPage = 1;
    public boolean isLastPage = false;
    Context mainActivity;

    public LiveData<MainData> getMainResponse(Context mainActivity) {
        this.mainActivity = mainActivity;
        if (currentFecthData == null || currentFecthData.getValue() == null || currentFecthData.getValue().getData() == null) {
            currentFecthData = new MutableLiveData<MainData>();

            if (checkConnection(mainActivity)) {
                apiFetchService = new APIFetchService();
                apiFetchService.getFetchRepositoryInstance(this);
                apiFetchService.fecthDataFromNetwork();

            } else {
                checkForDatabase_to_Load();
            }
        }
        return currentFecthData;
    }

    private void checkForDatabase_to_Load() {
        if (mDb == null)
            mDb = AppDatabase.getInstance(mainActivity);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<User> listSmaple = mDb.personDao().loadAllPersons();
                if (listSmaple != null && listSmaple.size() > 0) {
                    ArrayList<Datum> samplelIst = new ArrayList<Datum>();
                    for (User datum : listSmaple) {
                        samplelIst.add(new Datum(datum.getId(), datum.getFirstName(), datum.getLastName(), datum.getEmail(), datum.getAvatar()));
                    }
                    AppExecutors.getInstance().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            listUserData.setValue(samplelIst);
                            showProgressBar.setValue(ProgressUIType.DIMISS);
                        }
                    });
                } else {

                    AppExecutors.getInstance().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            showProgressBar.setValue(ProgressUIType.CANCEL);

                        }
                    });
                }
            }
        });

    }

    public void getMainResponsePaginations() {
        if (checkConnection(mainActivity)) {
            if (apiFetchService == null) {
                apiFetchService = new APIFetchService();
                apiFetchService.getFetchRepositoryInstance(this);

            }
            if (page != totalPage) {
                page++;
            } else if (page == totalPage) {
                isLastPage = true;
            }

            if (page > totalPage) {
                isLastPage = true;
                return;
            }
            loading = true;
            apiFetchService.fecthDataFromNetworkPage(page, offset);

        } else {
//            showProgressBar.setValue(ProgressUIType.CANCEL);
        }
    }

    public MutableLiveData<List<Datum>> getUserData() {
        return listUserData;
    }

    public LiveData<Boolean> getInternetConnectivity() {
        if (internetConnectivity == null) {
            internetConnectivity = new MutableLiveData<Boolean>(true);
        }
        return internetConnectivity;
    }

    public LiveData<ProgressUIType> getShowProgressStatus() {
        if (showProgressBar == null)
            showProgressBar = new MutableLiveData<ProgressUIType>(ProgressUIType.CANCEL);
        return showProgressBar;
    }


    public void setInterNetConnectivity_Context(MainActivity context) {
        connecLiveData = new InternetConnectivityLiveData(context);
        connecLiveData.observe(context, new Observer<ConnectionModel>() {

            @Override
            public void onChanged(ConnectionModel connection) {
                if (connection.getIsConnected()) {
                    switch (connection.getType()) {
                        case 0: {
                            internetConnectivity.setValue(true);
                        }
                        case 1: {
                            internetConnectivity.setValue(true);
                        }
                    }
                } else {
                    internetConnectivity.setValue(false);
                }
            }
        });
    }

    public void retryCall() {
        showProgressBar.setValue(ProgressUIType.SHOW);
        if (checkConnection(mainActivity)) {
            if (apiFetchService == null) {
                apiFetchService = new APIFetchService();
                apiFetchService.getFetchRepositoryInstance(this);
            }
            apiFetchService.fecthDataFromNetwork();
        } else {
            showProgressBar.setValue(ProgressUIType.CANCEL);
        }

    }

    @Override
    public void onCallbackMainReponnse(MutableLiveData<MainData> list) {
        loading = false;

        if (list != null && list.getValue() != null) {
            page = list.getValue().getPage();
            totalPage = list.getValue().getTotalPages();
            currentFecthData.setValue(list.getValue());

            if (page == totalPage) {
                isLastPage = true;
            }

            if (list.getValue().getData() != null && list.getValue().getData().size() > 0) {
                if (listUserData == null)
                    listUserData = new MutableLiveData<>();
                listUserData.setValue(list.getValue().getData());
            }
            insertInDatabase(false);

        }

    }

    @Override
    public void onCallbackMainReponnsePaginations(MutableLiveData<MainData> list) {
        loading = false;
        onDatA_Dismiss_Progress();
        if (list != null && list.getValue() != null) {
            page = list.getValue().getPage();
            totalPage = list.getValue().getTotalPages();
            currentFecthData.setValue(list.getValue());

            if (page == totalPage) {
                isLastPage = true;
            }

            if (list.getValue().getData() != null && list.getValue().getData().size() > 0) {
                if (listUserData == null)
                    listUserData = new MutableLiveData<>();
                listUserData.setValue(list.getValue().getData());
            }
            insertInDatabase(true);

        }

    }

    private void insertInDatabase(boolean paginations) {
        if (mDb == null)
            mDb = AppDatabase.getInstance(mainActivity);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                ArrayList<User> samplelIst = new ArrayList<User>();
                for (Datum datum : listUserData.getValue()) {
                    samplelIst.add(new User(datum.getId(), datum.getFirstName(), datum.getLastName(), datum.getEmail(), datum.getAvatar()));
                }
                if (!paginations)
                    mDb.personDao().deleteAllUsers();
                mDb.personDao().insertPerson(samplelIst);
            }
        });
    }

    @Override
    public void onShowProgress() {
        showProgressBar.setValue(ProgressUIType.SHOW);
    }

    @Override
    public void onDatA_Load_Progress() {
        showProgressBar.setValue(ProgressUIType.DATA_LOAD);
    }

    @Override
    public void onDatA_Dismiss_Progress() {

        showProgressBar.setValue(ProgressUIType.DATA_CANCEL);
    }

    @Override
    public void onDimissProgress() {
        showProgressBar.setValue(ProgressUIType.DIMISS);
    }

    @Override
    public void onCanceledProgress(String message) {


        showProgressBar.setValue(ProgressUIType.DATA_CANCEL);
        showProgressBar.setValue(ProgressUIType.CANCEL);

    }

    @Override
    public void onCanceledPaginations(String message) {
        loading = false;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

    }
}
