package com.abdennebi.photogift.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.abdennebi.photogift.R;
import com.abdennebi.photogift.domain.TopGiver;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class TopGiversArrayAdapter extends ArrayAdapter<TopGiver> {

    public TopGiversArrayAdapter(Context context, int resource, List<TopGiver> objects) {
        super(context, resource, objects);
    }

    public TopGiversArrayAdapter(Context context, int resource) {
        this(context, resource, new ArrayList<TopGiver>());
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final TopGiver topGiver = getItem(position);

        LayoutInflater inflater = LayoutInflater.from(getContext());

        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.top_givers_item, null);

            holder = new ViewHolder();
            holder.touchedCount = (TextView) convertView.findViewById(R.id.top_givers_item_touched_count);
            holder.authorPhoto = (ImageView) convertView.findViewById(R.id.top_givers_item_photo);
            holder.authorName = (TextView) convertView.findViewById(R.id.top_givers_item_display_name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Picasso.with(getContext()).load(topGiver.photoUrl).into(holder.authorPhoto);
        holder.touchedCount.setText(String.valueOf(topGiver.count));
        holder.authorName.setText(topGiver.displayName);
        convertView.setTag(holder);
        return convertView;
    }




    class ViewHolder {
        TextView authorName;
        TextView touchedCount;
        ImageView authorPhoto;
    }


}
