package com.abdennebi.photogift.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abdennebi.photogift.R;
import com.abdennebi.photogift.api.ApiClient;
import com.abdennebi.photogift.application.Listener;
import com.abdennebi.photogift.application.PhotoGift;
import com.abdennebi.photogift.domain.GiftChain;

import java.util.List;

public class GiftChainListFragment extends Fragment implements Listener.GiftChainListListener {

    private static final String LOG_TAG = GiftChainListFragment.class.getCanonicalName();

    private static final String TAG = "GiftChainListFragment";

    private RecyclerView mRecyclerView;

    private GiftChainAdapter mAdapter;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.giftchain_list, container, false);
        rootView.setTag(TAG);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new GiftChainAdapter(getActivity(), mListener);

        // Set CustomAdapter as the mAdapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshData();
    }

    private void refreshData() {
        progressDialog = ProgressDialog.show(getActivity(), "Please wait ...", "Refreshing data ...", true);
        progressDialog.setCancelable(true);
        ApiClient apiClient = ((PhotoGift) getActivity().getApplication()).getApiClient();
        apiClient.getGiftChainList(this);
    }

    @Override
    public void onReceived(List<GiftChain> giftChainList) {
        mAdapter.setItems(giftChainList);
        mAdapter.notifyDataSetChanged();
        progressDialog.dismiss();
    }

    public void refresh() {
        refreshData();
    }

}