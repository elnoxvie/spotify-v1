package com.elnware.spotifystreamer.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.elnware.spotifystreamer.R;
import com.elnware.spotifystreamer.activity.compat.AppCompatPreferenceActivity;
import com.elnware.spotifystreamer.provider.MySuggestionProvider;

/**
 * Setting Preference Activity
 * Created by elnoxvie on 6/25/15.
 */
public class SettingsPreferenceActivity extends AppCompatPreferenceActivity {
   private  static final String PREF_CLEAR_HISTORY  = "clear_history";
   public static final String PREF_SPOTIFY_MARKET = "spotify_market";
   private Preference        mPrefSpotifyMarket;
   private SharedPreferences mSharedPreferences;
   private View              mParentLayout;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main_preference);
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      mParentLayout = findViewById(R.id.parent);
      setSupportActionBar(toolbar);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      addPreferencesFromResource(R.xml.settings);

      Preference mPrefClearHistory = findPreference(PREF_CLEAR_HISTORY);

      mPrefSpotifyMarket = findPreference(PREF_SPOTIFY_MARKET);

      mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
      String currentMarket = mSharedPreferences.getString(PREF_SPOTIFY_MARKET, getString(R.string.default_spotify_market));
      mPrefSpotifyMarket.setSummary(getString(R.string.current_market, currentMarket));

      mSharedPreferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);

      mPrefClearHistory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
         @Override
         public boolean onPreferenceClick(Preference preference) {
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(SettingsPreferenceActivity.this, MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.clearHistory();

            Snackbar
                    .make(mParentLayout, R.string.msg_search_history_cleared, Snackbar.LENGTH_LONG)
                    .show();
            return true;
         }
      });
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()){
         case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            break;
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
   }

   private final SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener =
           new SharedPreferences.OnSharedPreferenceChangeListener() {
              @Override
              public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                 if (key.equals(PREF_SPOTIFY_MARKET)) {
                    String currentMarket = sharedPreferences.getString(key, getString(R.string.default_spotify_market));
                    mPrefSpotifyMarket.setSummary(getString(R.string.current_market, currentMarket));
                 }

              }
           };

}
