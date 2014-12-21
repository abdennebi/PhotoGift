package com.abdennebi.photogift.activity;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abdennebi.photogift.R;
import com.abdennebi.photogift.domain.GiftChain;
import com.abdennebi.photogift.utils.DateUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GiftChainAdapter extends RecyclerView.Adapter<GiftChainAdapter.ViewHolder> {

    private static final String LOG_TAG = GiftChainAdapter.class.getCanonicalName();

    private final GiftChainListFragment.OnArticleSelectedListener listener;

    int resource;

    List<GiftChain> items;

    Context context;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View giftChainItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.giftchain_item, parent, false);
        return new ViewHolder(giftChainItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GiftChain giftChain = items.get(position);
        String title = giftChain.title;
        long creationTime = giftChain.creationDate.getTime();

        holder.titleTextView.setText("" + title);
        holder.creationTimeTextView.setText("" + DateUtils.FORMAT.format(creationTime));
        Picasso.with(context).load(giftChain.thumbnailUrl).into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView creationTimeTextView;
        ImageView thumbnail;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.giftchain_item_title);
            creationTimeTextView = (TextView) itemView.findViewById(R.id.giftchain_item_title_creation_time);
            thumbnail = (ImageView) itemView.findViewById(R.id.giftchain_item_photo_thumnail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GiftChain giftChain = items.get(getPosition());
                    listener.onArticleSelected(giftChain.giftChainId);
                }
            });
        }
    }

    public void setItems(List<GiftChain> items) {
        this.items = items;
    }

    public GiftChainAdapter(Context _context,  List<GiftChain> _items, GiftChainListFragment.OnArticleSelectedListener _listener) {
        items = _items;
        context = _context;
        listener = _listener;
    }

    public GiftChainAdapter(Context context, GiftChainListFragment.OnArticleSelectedListener listener) {
        this(context, new ArrayList<GiftChain>(), listener);
    }
}
