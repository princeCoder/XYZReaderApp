package com.example.xyzreader.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * Created by Prinzly Ngotoum on 2/23/16.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private Cursor mCursor;
    private Context mContext;
    final ViewHolderOnClickHandler mCallback;

    public RecyclerViewAdapter(Context c, ViewHolderOnClickHandler vh) {
        mContext = c;
        mCallback=vh;
    }


    //View holder class which help to recycle row view elements
    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;
        public CardView mContainer;

        public ViewHolder(View view) {
            super(view);
            thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
            mContainer=(CardView)view.findViewById(R.id.main_container);
            view.setClickable(true);

        }

    }


    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(parent instanceof RecyclerView){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_article, parent, false);
            view.setFocusable(true);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onClick(getItemId(vh.getAdapterPosition()),vh);
                }
            });
            return vh;
        }
        else {
            throw new RuntimeException(mContext.getString(R.string.recycler_binding_error));
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        holder.subtitleView.setText(
                DateUtils.getRelativeTimeSpanString(
                        mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL).toString()
                        + " by "
                        + mCursor.getString(ArticleLoader.Query.AUTHOR));

        Glide.with(holder.thumbnailView.getContext())
                .load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                .fitCenter()
                .into(holder.thumbnailView);
    }


    @Override
    public int getItemCount() {
        return mCursor!=null?mCursor.getCount():0;
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public void setCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public interface ViewHolderOnClickHandler{
        void onClick(long id,ViewHolder vh);
    }
}
