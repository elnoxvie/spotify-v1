package com.elnware.spotifystreamer.activity;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;

import com.elnware.spotifystreamer.fragment.TopTrackFragment;
import com.elnware.spotifystreamer.activity.base.SingleFragmentToolbarlessActivity;

/**
 * Activity to show Top Tracks
 * Created by elnoxvie on 6/21/15.
 */
public class TopTrackActivity extends SingleFragmentToolbarlessActivity{
   private String mArtistId;
   private String mArtistName;
   private String mArtistImage;

   @Override
   protected void onCreate(Bundle savedInstanceState) {

      if (savedInstanceState == null) {
         if (getIntent() != null) {
            mArtistId = getIntent().getStringExtra(TopTrackFragment.EXTRA_ARTIST_ID);
            mArtistName = getIntent().getStringExtra(TopTrackFragment.EXTRA_ARTIST_NAME);
            mArtistImage = getIntent().getStringExtra(TopTrackFragment.EXTRA_ARTIST_IMAGE);
         }
      } else {
         mArtistId = savedInstanceState.getString(TopTrackFragment.EXTRA_ARTIST_ID);
         mArtistName = savedInstanceState.getString(TopTrackFragment.EXTRA_ARTIST_NAME);
         mArtistImage = savedInstanceState.getString(TopTrackFragment.EXTRA_ARTIST_IMAGE);
      }

      super.onCreate(savedInstanceState);

      ActivityCompat.postponeEnterTransition(this);
   }

   @Override
   protected void onNavigationButtonClick() {
      NavUtils.navigateUpFromSameTask(this);
   }

   @Override
   protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putString(TopTrackFragment.EXTRA_ARTIST_ID, mArtistId);
      outState.putString(TopTrackFragment.EXTRA_ARTIST_NAME, mArtistName);
      outState.putString(TopTrackFragment.EXTRA_ARTIST_IMAGE, mArtistImage);
   }

   @Override
   public Fragment createFragment() {
      return TopTrackFragment.newInstance(mArtistId, mArtistName, mArtistImage);
   }
}
