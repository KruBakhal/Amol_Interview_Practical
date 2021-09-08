package com.amol.interview_practical;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.amol.interview_practical.Adapter.HomeAdapter;
import com.amol.interview_practical.Model.ProgressUIType;
import com.amol.interview_practical.Model.api.Datum;
import com.amol.interview_practical.Model.api.MainData;
import com.amol.interview_practical.ViewModel.FetchDataRepository;
import com.amol.interview_practical.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FetchDataRepository fetchLiveData;
    private ActivityMainBinding viewBinding;
    private HomeAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);
        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        setUp();

    }

    private void setUp() {
        fetchLiveData = new ViewModelProvider(this).get(FetchDataRepository.class);

        setAdapter();

        fetchLiveData.setInterNetConnectivity_Context(this);
        // Internet Connectivity
        fetchLiveData.getInternetConnectivity().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean t) {
                if (t) {
                    viewBinding.btnStatus.setImageResource(R.drawable.outline_wifi_white_24);

                } else {
                    viewBinding.btnStatus.setImageResource(R.drawable.outline_wifi_off_white_24);
                    if (fetchLiveData != null && (fetchLiveData.showProgressBar.
                            getValue() == ProgressUIType.CANCEL)
                    ) {
                        viewBinding.retry.layRetry.setVisibility(View.VISIBLE);
                        viewBinding.pg.layPg.setVisibility(View.GONE);
                        viewBinding.recyclerViewMain.setVisibility(View.GONE);
                    }
                }
            }
        });
        // Progress Layout Display
        fetchLiveData.getShowProgressStatus().observe(
                this, new Observer<ProgressUIType>() {
                    @Override
                    public void onChanged(ProgressUIType t) {
                        if (t == ProgressUIType.SHOW) {
                            viewBinding.recyclerViewMain.setVisibility(View.GONE);
                            viewBinding.retry.layRetry.setVisibility(View.GONE);
                            viewBinding.pg.layPg.setVisibility(View.VISIBLE);

                        } else if (t == ProgressUIType.DIMISS) {
                            viewBinding.pg.layPg.setVisibility(View.GONE);
                            viewBinding.retry.layRetry.setVisibility(View.GONE);
                            viewBinding.recyclerViewMain.setVisibility(View.VISIBLE);
                            viewBinding.swipeContainer.setRefreshing(false);
                        } else if (t == ProgressUIType.DATA_LOAD) {
                            if (adapter != null) {
                                adapter.list.add(null);
                                adapter.notifyItemInserted(adapter.list.size() - 1);
                            }
                        } else if (t == ProgressUIType.DATA_CANCEL) {
                            if (adapter != null && adapter.list != null && adapter.list.size() > 1) {
                                adapter.list.remove(adapter.list.size() - 1);
                                adapter.notifyItemRemoved(adapter.list.size() - 1);
                            }
                        } else {
                            viewBinding.pg.layPg.setVisibility(View.GONE);
                            viewBinding.retry.layRetry.setVisibility(View.VISIBLE);
                            viewBinding.recyclerViewMain.setVisibility(View.GONE);
                        }
                    }
                });

        // UI Data
        fetchLiveData.getMainResponse(this).observe(this, callbackResponse);
        fetchLiveData.getUserData().observe(this, new Observer<List<Datum>>() {
            @Override
            public void onChanged(List<Datum> data) {
                adapter.addItem(data);
            }
        });
        // click event and init UI
        viewBinding.retry.img12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchLiveData.retryCall();

            }
        });
        viewBinding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        viewBinding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (checkConnection(MainActivity.this)) {
                    if (fetchLiveData != null) {
                        viewBinding.swipeContainer.setRefreshing(false);
                        fetchLiveData.retryCall();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                    viewBinding.swipeContainer.setRefreshing(false);
                }
            }
        });

    }

    private void setAdapter() {
        layoutManager = viewBinding.recyclerViewMain.getLayoutManager();
        adapter = new HomeAdapter();
        viewBinding.recyclerViewMain.setAdapter(adapter);
        viewBinding.recyclerViewMain.addOnScrollListener(recyclerViewOnScrollListener);
    }

    private void loadMoreItems() {
        fetchLiveData.loading = true;
        fetchLiveData.getMainResponsePaginations();
    }

    private Observer<? super MainData> callbackResponse = new Observer<MainData>() {
        @Override
        public void onChanged(MainData mainResponse) {
            if (mainResponse.getData() != null && mainResponse.getData().size() > 0) {
                adapter.addItem(mainResponse.getData());
            }
        }
    };

    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();

            if (!fetchLiveData.loading && !fetchLiveData.isLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= fetchLiveData.offset) {
                    loadMoreItems();
                }
            }
        }
    };

    public static boolean checkConnection(Context context) {
        boolean result = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network networkCapabilities = connectivityManager.getActiveNetwork();
            if (networkCapabilities == null)
                result = false;
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(networkCapabilities);
            if (actNw == null)
                result = false;
            else if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                result = true;
            } else if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                result = true;
            } else if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                result = true;
            }
        } else {
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info == null)
                result = false;
            else if (ConnectivityManager.TYPE_WIFI == info.getType())
                result = true;
            else if (ConnectivityManager.TYPE_ETHERNET == info.getType())
                result = true;
            else if (ConnectivityManager.TYPE_MOBILE == info.getType())
                result = true;
        }
        return result;
    }

}
