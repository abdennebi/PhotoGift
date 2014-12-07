package com.abdennebi.photogift.activity;


import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.abdennebi.photogift.R;
import com.abdennebi.photogift.api.ApiCallbacks;
import com.abdennebi.photogift.api.ApiClient;
import com.abdennebi.photogift.application.PhotoGift;
import com.abdennebi.photogift.domain.Gift;
import com.abdennebi.photogift.domain.TopGiver;

import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

public class TopGiversActivity extends ListActivity {

    private ProgressDialog progressDialog;

    private PhotoGift application;

    private ApiClient apiClient;

    private TopGiversArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new TopGiversArrayAdapter(this, R.id.menu_item_top_givers );
        setListAdapter(adapter);
        application = ((PhotoGift) (getApplication()));
        apiClient = application.getApiClient();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();

    }

    private void refreshData() {
        progressDialog = ProgressDialog.show(this, "Please wait ...", "Refreshing data ...", true);
        progressDialog.setCancelable(true);
        apiClient.getTopGivers(new ApiCallbacks.Callback<List<TopGiver>>() {
            @Override
            public void onSuccess(List<TopGiver> result) {
                adapter.clear();
                adapter.addAll(result);
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Exception ex) {
                Toast.makeText(TopGiversActivity.this, "There were a problem when retreiving Top Givers :-(", LENGTH_LONG).show();
            }
        });
    }
}
