package com.example.xyzreader.ui;

import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.adapter.RecyclerViewAdapter;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, RecyclerViewAdapter.ViewHolderOnClickHandler {

    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private TextView mEmptyView;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_container);
        collapsingToolbar.setTitle(getResources().getString(R.string.welcome_xyz_reader));

        mEmptyView=(TextView)findViewById(R.id.recycler_view_empty);

        mImageView=(ImageView)findViewById(R.id.thumbnail);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);


        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
        }
    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
        registerReceiver(mConnectivityReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_NO_CONNECTIVITY));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
        unregisterReceiver(mConnectivityReceiver);
    }

    private boolean mIsRefreshing = false;

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);

                updateRefreshingUI();
            }
        }
    };


    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_NO_CONNECTIVITY.equals(intent.getAction())) {
                mIsRefreshing = false;
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        mAdapter=new RecyclerViewAdapter(this, this);
        mAdapter.setCursor(cursor);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);

        if(getResources().getBoolean(R.bool.muilti_columns)){// If it is a tablet
            int columnCount = getResources().getInteger(R.integer.list_column_count);
            StaggeredGridLayoutManager sglm =
                    new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(sglm);
        }
        else{// This is just a phone
            // Set the layout manager
            mRecyclerView.setLayoutManager(new LinearLayoutManager(ArticleListActivity.this));
        }

        if(mAdapter.getItemCount()==0){
            mEmptyView.setVisibility(View.VISIBLE);
        } else{
            mEmptyView.setVisibility(View.GONE);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }


    @Override
    public void onClick(long id, RecyclerViewAdapter.ViewHolder vh) {
        Intent intent = new Intent(Intent.ACTION_VIEW, ItemsContract.Items.buildItemUri(id));

        //Get the reference of the view to share
        View sharedView=vh.thumbnailView;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // shared element transition
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this, sharedView, sharedView.getTransitionName()).toBundle();
            startActivity(intent, bundle);
        } else {
            startActivity(intent);
        }
    }
}
