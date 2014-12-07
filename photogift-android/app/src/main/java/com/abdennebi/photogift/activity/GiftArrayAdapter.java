package com.abdennebi.photogift.activity;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.abdennebi.photogift.R;
import com.abdennebi.photogift.api.ApiCallbacks;
import com.abdennebi.photogift.api.ApiClient;
import com.abdennebi.photogift.application.PhotoGift;
import com.abdennebi.photogift.domain.Gift;
import com.abdennebi.photogift.domain.User;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GiftArrayAdapter extends ArrayAdapter<Gift> {

    public GiftArrayAdapter(Context context, int resource, List<Gift> objects) {
        super(context, resource, objects);
    }

    public GiftArrayAdapter(Context context, int resource) {
        this(context, resource, new ArrayList<Gift>());
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Gift gift = getItem(position);

        LayoutInflater inflater = LayoutInflater.from(getContext());

        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.gift_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.gift_title);
            holder.text = (TextView) convertView.findViewById(R.id.gift_text);
            holder.photo = (ImageView) convertView.findViewById(R.id.gift_photo);

            holder.touchedCount = (TextView) convertView.findViewById(R.id.gift_touched_count);
            holder.touchedImage = (ImageView) convertView.findViewById(R.id.gift_touched_button);
            holder.publicationDate = (TextView) convertView.findViewById(R.id.gift_publication_date);
            holder.authorPhoto = (ImageView) convertView.findViewById(R.id.gift_author_photo);
            holder.authorName = (TextView) convertView.findViewById(R.id.gift_author_name);
            holder.signalInappropriate = (ImageView) convertView.findViewById(R.id.gift_inappropriate_button);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        final PhotoGift app = ((PhotoGift) getContext().getApplicationContext());
        final User user = app.getApplicationSession().getUser();
        final ApiClient apiClient = app.getApiClient();

        holder.signalInappropriate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiClient.signalInappropiate(gift.giftId, new ApiCallbacks.Callback<Gift>() {
                    @Override
                    public void onSuccess(Gift result) {
                        Toast.makeText(getContext(),"Thank you for signaling", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception ex) {
                        Toast.makeText(getContext(),"There were a problem", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Picasso.with(getContext()).load(R.drawable.ic_favorite_grey600_48dp).into(holder.touchedImage);
        holder.title.setText(gift.title);
        holder.text.setText(gift.text);
        Picasso.with(getContext()).load(gift.imageUrl).into(holder.photo);
        holder.touchedCount.setText(String.valueOf(gift.touchedCount));
        holder.authorName.setText(gift.ownerDisplatName);

        if (gift.ownerProfilePhoto != null) {
            Picasso.with(getContext()).load(gift.ownerProfilePhoto).into(holder.authorPhoto);
        } else {
            Picasso.with(getContext()).load(R.drawable.ic_person_black_48dp).into(holder.authorPhoto);
        }

        Date date = gift.creationDate;
        if (date != null) {
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
            holder.publicationDate.setText("Created: " + dateFormat.format(date));
        }

        if (user != null) {
            Picasso.with(getContext()).load(R.drawable.ic_favorite_outline_black_48dp).into(holder.touchedImage);
            if (user.touchingGifts.contains(gift.giftId)) {
                // user has already been touched by this gift
                Picasso.with(getContext()).load(R.drawable.ic_favorite_black_48dp).into(holder.touchedImage);
                holder.touchedImage.setOnClickListener(null);
            } else {
                // user can click on touched icon
                holder.touchedImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        apiClient.setTouched(gift.giftId, new ApiCallbacks.Callback<Gift>() {
                            @Override
                            public void onSuccess(Gift result) {
                                Picasso.with(getContext()).load(R.drawable.ic_favorite_black_48dp).into(holder.touchedImage);
                                // The touched image is no more clickable
                                holder.touchedImage.setOnClickListener(null);
                                user.touchingGifts.add(gift.giftId);
                                Gift item = getItem(position);
                                item.touchedCount = result.touchedCount;
                                GiftArrayAdapter.this.notifyDataSetChanged();
                            }

                            @Override
                            public void onFailure(Exception ex) {
                                Toast.makeText(getContext(), "There were problem...", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }

        else { // the user is null
            holder.touchedImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "Please sign in to tell us how much you're touched by this gift !", Toast.LENGTH_LONG).show();
                }
            });
        }

        convertView.setTag(holder);
        return convertView;
    }

    class ViewHolder {
        TextView title;
        TextView text;
        ImageView photo;
        TextView touchedCount;
        ImageView touchedImage;
        TextView publicationDate;
        ImageView authorPhoto;
        TextView authorName;
        ImageView signalInappropriate;
    }
}
