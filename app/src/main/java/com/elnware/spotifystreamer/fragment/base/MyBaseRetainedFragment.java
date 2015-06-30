package com.elnware.spotifystreamer.fragment.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

/**
 * Base Fragment that implement retained fragment.
 * This will allow fragment with the need to request data
 * to automatically have it's data retained
 * Created by elnoxvie on 6/20/15.
 */
@SuppressWarnings("unchecked")
public abstract class MyBaseRetainedFragment<T> extends Fragment {
   protected RetainedFragment<T> mRetainedFragment;

   @Nullable
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      FragmentManager fragmentManager = getSupportFragmentManager();
      mRetainedFragment = (RetainedFragment<T>) fragmentManager.findFragmentByTag(getClass().getSimpleName());

      if (mRetainedFragment == null) {
         mRetainedFragment = new RetainedFragment<>();
         fragmentManager
                 .beginTransaction()
                 .add(mRetainedFragment, getClass().getSimpleName())
                 .commit();
      }

      return super.onCreateView(inflater, container, savedInstanceState);
   }

   protected ActionBar getCompatActionBar(){
      FragmentActivity activity = getActivity();
      if (activity != null){
         return ((AppCompatActivity) getActivity()).getSupportActionBar();
      }

      return null;
   }

   protected AppCompatActivity getCompatActivity(){
      FragmentActivity activity = getActivity();
      if (activity != null){
         return (AppCompatActivity) getActivity();
      }

      return null;
   }

   protected Window getWindow(){
     return getActivity().getWindow();
   }

   protected FragmentManager getSupportFragmentManager(){
      return getActivity().getSupportFragmentManager();
   }
}
