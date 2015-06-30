package com.elnware.spotifystreamer.fragment.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * A Fragment that will survive orientation changed
 * and retained the data of choice.
 *
 * Created by Andrew Chen on 6/20/15.
 */
public  class RetainedFragment<T> extends Fragment{

   private T mData;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
   }

   public void setData(T data){
      this.mData = data;
   }

   public T getData(){
      return mData;
   }
}
