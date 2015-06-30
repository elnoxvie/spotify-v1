package com.elnware.spotifystreamer.activity.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.elnware.spotifystreamer.R;

/**
 * Convenient Class that wrap fragment inside Activity
 * Created by elnoxvie on 6/20/15.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity {

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
      getSupportActionBar().setIcon(R.mipmap.ic_launcher);

      if (savedInstanceState == null){
         getSupportFragmentManager()
                 .beginTransaction()
                 .add(R.id.fragment_container, createFragment())
                 .commit();
      }
   }


   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()){
         case android.R.id.home:
            onNavigationButtonClick();
            break;
      }

      return super.onOptionsItemSelected(item);
   }

   protected void onNavigationButtonClick(){ }

   protected abstract Fragment createFragment();
}
