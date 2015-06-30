package com.elnware.spotifystreamer.fragment.base;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * A Retained Fragment that contains AsyncTask
 * that could survived config changes
 *
 * Created by elnoxvie on 6/21/15.
 */
public abstract class TaskFragment<T> extends Fragment{

   /**
    * Callbacks for common AsyncTask Functions
    * @param <T> Type of the data returned
    */
   public interface TaskCallbacks<T> {
      void onPreExecute();
      void onPostExecute(T data);
   }

   protected TaskCallbacks<T> mCallbacks;
   protected AsyncTask mTask;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);
      if (activity instanceof TaskCallbacks){
         mCallbacks = (TaskCallbacks) activity;
      }else if (getTargetFragment() != null && getTargetFragment() instanceof TaskCallbacks){
         mCallbacks = (TaskCallbacks) getTargetFragment();
      }
   }

   @Override
   public void onDetach() {
      super.onDetach();
      mCallbacks = null;
   }

   /**
    * Check if task is still running
    * this will be useful when orientation changes
    * and we need to set the progress bar view etc.
    * @return true or false
    */
   public boolean isTaskRunning(){
      return ((mTask != null) && (mTask.getStatus() == AsyncTask.Status.RUNNING));
   }


}
