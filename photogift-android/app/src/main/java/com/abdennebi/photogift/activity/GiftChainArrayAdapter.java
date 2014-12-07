package com.abdennebi.photogift.activity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.abdennebi.photogift.R;
import com.abdennebi.photogift.domain.GiftChain;
import com.abdennebi.photogift.utils.DateUtils;
import com.squareup.picasso.Picasso;

public class GiftChainArrayAdapter extends ArrayAdapter<GiftChain> {

    private static final String LOG_TAG = GiftChainArrayAdapter.class.getCanonicalName();

    int resource;

    public GiftChainArrayAdapter(Context _context, int _resource, List<GiftChain> _items) {
        super(_context, _resource, _items);
        resource = _resource;
    }

    public GiftChainArrayAdapter(Context context, int resource) {
        this(context, resource, new ArrayList<GiftChain>());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout giftChainItem = null;
        try {
            GiftChain giftChain = getItem(position);

            String title = giftChain.title;
            long creationTime = giftChain.creationDate.getTime();

            if (convertView == null) {
                giftChainItem = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(inflater);
                layoutInflater.inflate(resource, giftChainItem, true);
            } else {
                giftChainItem = (LinearLayout) convertView;
            }

            TextView titleTextView = (TextView) giftChainItem.findViewById(R.id.giftchain_item_title);
            TextView creationTimeTextView = (TextView) giftChainItem.findViewById(R.id.giftchain_item_title_creation_time);
            ImageView thumbnail = (ImageView) giftChainItem.findViewById(R.id.giftchain_item_photo_thumnail);

            titleTextView.setText("" + title);
            creationTimeTextView.setText("" + DateUtils.FORMAT.format(creationTime));
            Picasso.with(getContext()).load(giftChain.thumbnailUrl).into(thumbnail);

        } catch (Exception e) {
            Toast.makeText(getContext(), "exception in ArrayAdpter: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return giftChainItem;
    }

}
