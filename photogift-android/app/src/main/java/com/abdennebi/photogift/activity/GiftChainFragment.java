package com.abdennebi.photogift.activity;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;

import com.abdennebi.photogift.R;
import com.abdennebi.photogift.api.ApiClient;
import com.abdennebi.photogift.application.Listener;
import com.abdennebi.photogift.application.PhotoGift;
import com.abdennebi.photogift.domain.Gift;
import com.abdennebi.photogift.domain.GiftChain;

public class GiftChainFragment extends ListFragment implements Listener.GiftChainListener {

    private GiftChain giftChain;

    private Long giftChainId;

    private GiftAdapter adapter;

    private ApiClient apiClient;

    private PhotoGift application;

    private ProgressDialog progressDialog;

    public GiftChainFragment() {
    }

    public void loadGiftChain(Long giftChainId) {
        this.giftChainId = giftChainId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GiftAdapter(getActivity(), R.layout.gift_item);
        application = ((PhotoGift) (getActivity().getApplication()));
        apiClient = application.getApiClient();
        this.setListAdapter(adapter);

        // This Fragment will add items to the ActionBar
        setHasOptionsMenu(true);

        // Retain this Fragment across Activity Reconfigurations
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshData();
    }

    private void refreshData() {
        progressDialog = ProgressDialog.show(getActivity(), "Please wait ...", "Refreshing data ...", true);
        progressDialog.setCancelable(true);
        apiClient.getGiftChain(giftChainId, this);
    }


    @Override
    public void onReceived(GiftChain giftChain) {
        this.giftChain = giftChain;
        adapter.clear();
        adapter.addAll(giftChain.giftList);
        progressDialog.dismiss();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        boolean authenticated = ((PhotoGift) getActivity().getApplication()).isAuthenticated();
        inflater.inflate(R.menu.gift_fragment_menu, menu);
    }

    /**
     * Called by the parent Activity to refresh its Data.
     */
    public void refresh() {
        refreshData();
    }

    public Long getGiftChainId() {
        return giftChain.giftChainId;
    }

    public void addGift(Gift gift) {
        adapter.clear();
        adapter.add(gift);
        adapter.addAll(giftChain.giftList);
        adapter.notifyDataSetChanged();
    }
}
