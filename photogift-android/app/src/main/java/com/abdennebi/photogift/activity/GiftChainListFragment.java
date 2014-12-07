package com.abdennebi.photogift.activity;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.List;

import com.abdennebi.photogift.R;
import com.abdennebi.photogift.api.ApiClient;
import com.abdennebi.photogift.application.Listener;
import com.abdennebi.photogift.application.PhotoGift;
import com.abdennebi.photogift.domain.GiftChain;

public class GiftChainListFragment extends ListFragment implements Listener.GiftChainListListener {

    private static final String LOG_TAG = GiftChainListFragment.class.getCanonicalName();

    private GiftChainArrayAdapter adapter;

    private OnArticleSelectedListener mListener;

    private ProgressDialog progressDialog;

    public interface OnArticleSelectedListener {

        public void onArticleSelected(Long giftChainId);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnArticleSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GiftChainArrayAdapter(getActivity(), R.layout.giftchain_item);
        this.setListAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListView listView = getListView();

        // Enable filtering when the user types in the virtual keyboard
        listView.setTextFilterEnabled(true);

        // Set an setOnItemClickListener on the ListView
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                GiftChain giftChain = adapter.getItem(position);
                mListener.onArticleSelected(giftChain.giftChainId);
            }
        });

        refreshData();
    }

    private void refreshData() {
        progressDialog = ProgressDialog.show(getActivity(), "Please wait ...", "Refreshing data ...", true);
        progressDialog.setCancelable(true);
        ApiClient apiClient = ((PhotoGift)getActivity().getApplication()).getApiClient();
        apiClient.getGiftChainList(this);
    }

    @Override
    public void onReceived(List<GiftChain> giftChainList) {
        adapter.clear();
        adapter.addAll(giftChainList);
        progressDialog.dismiss();
    }

    public void refresh() {
        refreshData();
    }

}