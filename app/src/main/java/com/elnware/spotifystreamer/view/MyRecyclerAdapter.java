package com.elnware.spotifystreamer.view;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Custom RecyclerViewAdapter that implements onClick and OnLongItemClick
 * and some common functions similar to those in ListView
 * Created by elnoxvie on 6/21/15.
 * @param <L> List Type
 * @param <M> Class that extends MyRecylerViewHolder
 * @param <T> Type of the class returned by getItem(int position)
 */
@SuppressWarnings("unchecked")
public class MyRecyclerAdapter<L extends List, M extends MyRecyclerViewHolder, T> extends RecyclerView.Adapter<M>{
   private L mData;
   private  MyRecyclerView.MyRecylerCallbacks mCallbacks;

   public MyRecyclerAdapter(L data){
      this.mData = data;
   }

   public void setRecyclerCallbacks(MyRecyclerView.MyRecylerCallbacks callbacks){
      this.mCallbacks = callbacks;
   }

   public MyRecyclerView.MyRecylerCallbacks getRecyclerCallbacks(){
      return mCallbacks;
   }

   public L getItems(){
      return mData;
   }

   public void changeData(L data){
      mData = data;
      notifyDataSetChanged();
   }

   @Override
   public M onCreateViewHolder(ViewGroup viewGroup, int i) {  return null; }

   @Override
   public void onBindViewHolder(M holder , int i) {
   }

   @Override
   public int getItemCount() {
      return mData.size();
   }

   @SuppressWarnings("unchecked")
   public T getItem(int position){
      //noinspection unchecked
      return (T) mData.get(position);
   }
}
