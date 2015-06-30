package com.elnware.spotifystreamer;

import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.elnware.spotifystreamer.activity.SettingsPreferenceActivity;
import com.elnware.spotifystreamer.fragment.SearchFragment;
import com.elnware.spotifystreamer.activity.base.SingleFragmentActivity;


public class MainActivity extends SingleFragmentActivity {


   @Override
   public Fragment createFragment() {
      return SearchFragment.newInstance();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.menu_main, menu);
      return true;
   }

   @Override
   public void onActivityReenter(int resultCode, Intent data) {
      super.onActivityReenter(resultCode, data);
      ActivityCompat.postponeEnterTransition(this);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      int id = item.getItemId();

      if (id == R.id.action_settings) {
         startActivity(new Intent(this, SettingsPreferenceActivity.class));
         return true;
      }

      return super.onOptionsItemSelected(item);
   }
}
