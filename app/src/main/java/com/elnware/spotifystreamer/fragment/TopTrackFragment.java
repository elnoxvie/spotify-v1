package com.elnware.spotifystreamer.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.elnware.spotifystreamer.R;
import com.elnware.spotifystreamer.util.Response;
import com.elnware.spotifystreamer.activity.SettingsPreferenceActivity;
import com.elnware.spotifystreamer.fragment.base.MyBaseRetainedFragment;
import com.elnware.spotifystreamer.fragment.base.TaskFragment;
import com.elnware.spotifystreamer.util.ImageUtils;
import com.elnware.spotifystreamer.util.RetrofitErrorHandler;
import com.elnware.spotifystreamer.util.TransitionUtils;
import com.elnware.spotifystreamer.view.MyRecyclerAdapter;
import com.elnware.spotifystreamer.view.MyRecyclerView;
import com.elnware.spotifystreamer.view.MyRecyclerViewHolder;
import com.elnware.spotifystreamer.view.decorator.DividerItemDecoration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * Fragment that show the top 10 tracks of the artist
 * Created by elnoxvie on 6/20/15.
 */
public class TopTrackFragment extends MyBaseRetainedFragment<List<Track>>
        implements TaskFragment.TaskCallbacks<Response<List<Track>>> {
   public static final  String EXTRA_ARTIST_ID      = "artist_id";
   public static final  String EXTRA_ARTIST_NAME    = "artist_name";
   public static final  String EXTRA_ARTIST_IMAGE   = "artist_image_url";
   private static final long   ANIM_DURATION        = 1000;
   private static final long   START_POSTPONE_DELAY = 300;

   private TopTrackAdapter mTopTrackAdapter;
   private SpotifyService  mSpotifyService;
   private String          mArtistId;
   private String          mArtistName;

   private TopTrackTaskFragment mTopTrackRetainedFragment;

   private MyRecyclerView        mRecyclerView;
   private LinearLayout          mProgressContainer;
   private View                  mEmptyView;
   private TextView              mEmptyText;
   private String                mArtistImage;
   private ImageView             mImageView;
   private TextView              mTvTitle;
   private DividerItemDecoration mDividerItemDecoration;

   public TopTrackFragment() {
   }

   public static TopTrackFragment newInstance(String artistId, String artistName, String artistUrl) {
      TopTrackFragment fragment = new TopTrackFragment();

      Bundle bundle = new Bundle();
      bundle.putString(EXTRA_ARTIST_ID, artistId);
      bundle.putString(EXTRA_ARTIST_NAME, artistName);
      bundle.putString(EXTRA_ARTIST_IMAGE, artistUrl);

      fragment.setArguments(bundle);
      return fragment;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      SpotifyApi spotify = new SpotifyApi();
      mSpotifyService = spotify.getService();

      Bundle bundle;
      if (savedInstanceState != null) {
         bundle = savedInstanceState;
      } else {
         bundle = getArguments();
      }

      mArtistId = bundle.getString(EXTRA_ARTIST_ID);
      mArtistName = bundle.getString(EXTRA_ARTIST_NAME);
      mArtistImage = bundle.getString(EXTRA_ARTIST_IMAGE);
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putString(EXTRA_ARTIST_ID, mArtistId);
      outState.putString(EXTRA_ARTIST_NAME, mArtistName);
      outState.putString(EXTRA_ARTIST_IMAGE, mArtistImage);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      super.onCreateView(inflater, container, savedInstanceState);
      View    view    = inflater.inflate(R.layout.fragment_top_songs, container, false);
      Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
      getCompatActivity().setSupportActionBar(toolbar);

      getCompatActionBar().setDisplayHomeAsUpEnabled(true);
      getCompatActionBar().setIcon(null);
      getCompatActionBar().setTitle(R.string.top_10_tracks);

      final CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
      toolbarLayout.setTitle(mArtistName);
      mTvTitle = (TextView) view.findViewById(R.id.tv_title);

      mImageView = (ImageView) view.findViewById(R.id.backdrop);

      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
         setupEnterAnimations();
         setupExitAnimations();
      }


      if (mArtistImage != null) {
         Glide.with(this)
                 .load(mArtistImage)
                 .asBitmap()
                 .dontAnimate()
                 .into(new BitmapImageViewTarget(mImageView) {

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                       super.onLoadFailed(e, errorDrawable);
                       /**
                        * When, we will also start the transition
                        */
                       ActivityCompat.startPostponedEnterTransition(getActivity());
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                       super.onResourceReady(resource, glideAnimation);

                       Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                          public void onGenerated(Palette p) {
                             Palette.Swatch swatch = p.getVibrantSwatch();
                             if (swatch != null) {
                                int color = swatch.getTitleTextColor();
                                mTvTitle.setBackgroundColor(swatch.getRgb());
                                mTvTitle.setTextColor(color);
                                toolbarLayout.setContentScrimColor(swatch.getRgb());
                                toolbarLayout.setTitle(mArtistName);
                             }
                          }
                       });

                       TransitionUtils.scheduleStartPostponedTransition(getActivity(), mImageView);
                    }
                 });
      } else {
         ActivityCompat.startPostponedEnterTransition(getActivity());
      }

      mRecyclerView = (MyRecyclerView) view.findViewById(R.id.recycler_view);
      mEmptyView = view.findViewById(android.R.id.empty);
      mProgressContainer = (LinearLayout) view.findViewById(R.id.progress_container);
      mEmptyText = (TextView) mEmptyView.findViewById(R.id.tv_empty);
      mEmptyText.setText(R.string.msg_no_top_tracks);

      LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
      layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
      mRecyclerView.setLayoutManager(layoutManager);
      mRecyclerView.setHasFixedSize(true);
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
         mDividerItemDecoration = new DividerItemDecoration(getActivity(), null);
         mRecyclerView.addItemDecoration(mDividerItemDecoration);
      }
      mRecyclerView.setEmptyView(mEmptyView);

      FragmentManager fragmentManager = getSupportFragmentManager();
      Fragment        fragment        =
              fragmentManager.findFragmentByTag(TopTrackTaskFragment.class.getSimpleName());

      if (fragment == null) {
         mTopTrackRetainedFragment = new TopTrackTaskFragment();
         getSupportFragmentManager()
                 .beginTransaction()
                 .add(mTopTrackRetainedFragment, mTopTrackRetainedFragment.getClass().getSimpleName())
                 .commit();
      } else {
         mTopTrackRetainedFragment = (TopTrackTaskFragment) fragment;
      }

      mTopTrackRetainedFragment.setTargetFragment(this, 0);
      mTopTrackRetainedFragment.setSpotiyService(mSpotifyService);

      /**
       * Check to see if there is an existing data
       * if none, then we will start to grab data
       */
      if (mRetainedFragment.getData() == null) {
         mTopTrackAdapter = new TopTrackAdapter(Collections.EMPTY_LIST);
         mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
               mTopTrackRetainedFragment.start(mArtistId);
            }
         });
      } else {
         mTopTrackAdapter = new TopTrackAdapter(mRetainedFragment.getData());
      }

      //If the task is still running we will display the progress bar
      if (mTopTrackRetainedFragment.isTaskRunning()) {
         showProgressIndeterminate(true);
      } else {
         showProgressIndeterminate(false);
      }

      mRecyclerView.setAdapter(mTopTrackAdapter);

      return view;
   }

   @Override
   public void onPreExecute() {
      showProgressIndeterminate(true);
   }

   @Override
   public void onPostExecute(Response<List<Track>> response) {
      mTopTrackAdapter.changeData(response.getData());
      mRetainedFragment.setData(response.getData());
      if (response.isError()) {
         String standardErrorMessage =
                 RetrofitErrorHandler.getStandardErrorMessage(getActivity(), response.getThrowable());
         mEmptyText.setText(standardErrorMessage);
      } else {
         mEmptyText.setText(getString(R.string.msg_no_top_tracks));
      }

      showProgressIndeterminate(false);
   }


   private void showProgressIndeterminate(boolean isShowing) {
      if (isShowing) {
         mProgressContainer.setVisibility(View.VISIBLE);
         mEmptyView.setVisibility(View.GONE);
         mRecyclerView.setVisibility(View.GONE);
      } else {
         mRecyclerView.setVisibility(View.VISIBLE);
         mProgressContainer.setVisibility(View.GONE);
      }

   }

   /**
    * Retained Fragment That contain AsyncTask to get Top Track
    */
   public static class TopTrackTaskFragment extends TaskFragment<Response<List<Track>>> {
      private SpotifyService mSpotifyService;
      private String         mSpotifyMarket;

      public void setSpotiyService(SpotifyService spotifyService) {
         mSpotifyService = spotifyService;
      }

      public void start(String... strings) {
         mTask = new TopTrackTask();
         mTask.execute(strings);
      }

      /**
       * Async task to get Top Track
       */
      public class TopTrackTask extends AsyncTask<String, Void, Response<List<Track>>> {

         public TopTrackTask() {}

         @Override
         protected void onPreExecute() {
            super.onPreExecute();
            if (mCallbacks != null) {
               mCallbacks.onPreExecute();
            }
         }

         @Override
         protected Response<List<Track>> doInBackground(String... strings) {

            Response<List<Track>> response = new Response<>();
            response.setData(Collections.EMPTY_LIST);

            Tracks tracks;
            try {
               SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
               mSpotifyMarket = mSharedPreference.getString(SettingsPreferenceActivity.PREF_SPOTIFY_MARKET,
                       getString(R.string.default_spotify_market));
               Map<String, Object> maps = new HashMap<>();
               maps.put(SpotifyService.COUNTRY, mSpotifyMarket);
               tracks = mSpotifyService.getArtistTopTrack(strings[0], maps);
            } catch (RetrofitError cause) {
               cause.printStackTrace();
               response.setThrowable(RetrofitErrorHandler.handleError(cause));
               return response;
            }

            if (tracks != null && tracks.tracks != null) {
               response.setData(tracks.tracks);
               return response;
            }

            return response;
         }

         @Override
         protected void onPostExecute(Response<List<Track>> response) {
            super.onPostExecute(response);
            if (mCallbacks != null) {
               mCallbacks.onPostExecute(response);
            }
         }
      }
   }

   /**
    * Top Track adapter
    */
   public class TopTrackAdapter extends  MyRecyclerAdapter<List<Track>,
           TopTrackAdapter.ViewHolder, Track> {

      public TopTrackAdapter(List<Track> data) {
         super(data);
      }

      @Override
      public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
         View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_track,
                 parent, false);
         return new ViewHolder(v);
      }

      @Override
      public void onBindViewHolder(ViewHolder holder, int position) {
         Track track = getItem(position);

         /**
          * we will use resource to store the width setting so that we can modify
          * them on larger device if necessary
          */
         Image image = ImageUtils.getOptimumImage(track.album.images,
                 getResources().getInteger(R.integer.default_list_image_width));

         if (image != null) {
            Glide.with(TopTrackFragment.this)
                    .load(image.url)
                    .error(R.drawable.empty)
                    .into(holder.ivTrackThumbnail);
         }else{
            holder.ivTrackThumbnail.setImageResource(R.drawable.empty);
         }
         holder.tvAlbumName.setText(track.album.name);
         holder.tvTrackName.setText(track.name);
         holder.tvPosition.setText(String.valueOf(++position));
      }

      public class ViewHolder extends MyRecyclerViewHolder {
         final TextView  tvPosition;
         final TextView  tvTrackName;
         final TextView  tvAlbumName;
         final ImageView ivTrackThumbnail;

         public ViewHolder(View v) {
            super(v);
            tvPosition = (TextView) v.findViewById(R.id.tv_position);
            tvTrackName = (TextView) v.findViewById(R.id.tv_track_name);
            tvAlbumName = (TextView) v.findViewById(R.id.tv_album_name);
            ivTrackThumbnail = (ImageView) v.findViewById(R.id.img_track_thumbnail);
         }
      }
   }

   @TargetApi(Build.VERSION_CODES.LOLLIPOP)
   private void setupEnterAnimations() {
      Transition enterTransition = getWindow().getSharedElementEnterTransition();
      enterTransition.addListener(new Transition.TransitionListener() {
         @Override
         public void onTransitionStart(Transition transition) {}

         @Override
         public void onTransitionEnd(Transition transition) {
            animateRevealShow(mImageView);
         }

         @Override
         public void onTransitionCancel(Transition transition) {}

         @Override
         public void onTransitionPause(Transition transition) {}

         @Override
         public void onTransitionResume(Transition transition) {}
      });
   }

   @TargetApi(Build.VERSION_CODES.LOLLIPOP)
   private void setupExitAnimations() {
      Transition sharedElementReturnTransition =
              getWindow().getSharedElementReturnTransition();
      sharedElementReturnTransition.setStartDelay(ANIM_DURATION);

      Transition returnTransition = getWindow().getReturnTransition();
      returnTransition.setDuration(ANIM_DURATION);
      returnTransition.addListener(new Transition.TransitionListener() {
         @Override
         public void onTransitionStart(Transition transition) {
            mRecyclerView.setVisibility(View.GONE);
            animateRevealHide(mImageView);
         }

         @Override
         public void onTransitionEnd(Transition transition) {}

         @Override
         public void onTransitionCancel(Transition transition) {}

         @Override
         public void onTransitionPause(Transition transition) {}

         @Override
         public void onTransitionResume(Transition transition) {}
      });
   }

   @TargetApi(Build.VERSION_CODES.LOLLIPOP)
   private void animateRevealShow(View viewRoot) {
      int cx          = (viewRoot.getLeft() + viewRoot.getRight()) / 2;
      int cy          = (viewRoot.getTop() + viewRoot.getBottom()) / 2;
      int finalRadius = Math.max(viewRoot.getWidth(), viewRoot.getHeight());

      Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, 0, finalRadius);

      viewRoot.setVisibility(View.VISIBLE);
      mRecyclerView.setVisibility(View.VISIBLE);
      anim.setDuration(ANIM_DURATION);
      anim.start();
   }

   @TargetApi(Build.VERSION_CODES.LOLLIPOP)
   private void animateRevealHide(final View viewRoot) {
      int cx = (viewRoot.getLeft() + viewRoot.getRight()) / 2;
      int cy            = (viewRoot.getTop() + viewRoot.getBottom()) / 2;
      int initialRadius = viewRoot.getWidth();

      Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, initialRadius, 0);
      anim.addListener(new AnimatorListenerAdapter() {
         @Override
         public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            viewRoot.setVisibility(View.INVISIBLE);
         }
      });
      anim.setDuration(ANIM_DURATION);
      anim.start();
   }
}
