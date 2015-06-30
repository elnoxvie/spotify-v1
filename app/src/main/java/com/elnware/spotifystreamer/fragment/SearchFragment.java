package com.elnware.spotifystreamer.fragment;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.elnware.spotifystreamer.BuildConfig;
import com.elnware.spotifystreamer.util.Response;
import com.elnware.spotifystreamer.provider.MySuggestionProvider;
import com.elnware.spotifystreamer.R;
import com.elnware.spotifystreamer.activity.TopTrackActivity;
import com.elnware.spotifystreamer.fragment.base.MyBaseRetainedFragment;
import com.elnware.spotifystreamer.fragment.base.TaskFragment;
import com.elnware.spotifystreamer.util.ImageUtils;
import com.elnware.spotifystreamer.util.RetrofitErrorHandler;
import com.elnware.spotifystreamer.view.MyRecyclerAdapter;
import com.elnware.spotifystreamer.view.MyRecyclerView;
import com.elnware.spotifystreamer.view.MyRecyclerViewHolder;
import com.elnware.spotifystreamer.view.decorator.DividerItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.RetrofitError;


/**
 * A search fragment containing a search box on the toolbar
 * to search for artists and display the search results
 */
public class SearchFragment extends MyBaseRetainedFragment<List<Artist>>
        implements TaskFragment.TaskCallbacks<Response<List<Artist>>> {

   public static final int DURATION = 700;

   private static final String LOG_TAG               = MyBaseRetainedFragment.class.getSimpleName();
   private static final String EXTRA_SEARCH_TEXT     = "search_text";
   private static final String EXTRA_SEARCH_EXPANDED = "search_expanded";

   private SpotifyService mSpotify;

   private ArtistSearchResultAdapter mySearchResultAdapter;
   private SearchTaskFragment        fragmentTask;
   private LinearLayout              mProgressContainer;
   private MyRecyclerView            myRecyclerView;
   private View                      mEmptyView;

   private String mQueryString;
   private boolean mIsSearchExpanded = false;
   private TextView mEmptyText;

   public static SearchFragment newInstance() {
      return new SearchFragment();
   }

   public SearchFragment() {
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      SpotifyApi api = new SpotifyApi();
      mSpotify = api.getService();
      setHasOptionsMenu(true);
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putString(EXTRA_SEARCH_TEXT, mQueryString);
      outState.putBoolean(EXTRA_SEARCH_EXPANDED, mIsSearchExpanded);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      super.onCreateView(inflater, container, savedInstanceState);
      View view = inflater.inflate(R.layout.fragment_spotify_search, container, false);
      myRecyclerView = (MyRecyclerView) view.findViewById(R.id.recycler_view);
      mProgressContainer = (LinearLayout) view.findViewById(R.id.progress_container);
      mEmptyView = view.findViewById(android.R.id.empty);
      mEmptyText = (TextView) mEmptyView.findViewById(R.id.tv_empty);
      mEmptyText.setText(getString(R.string.msg_search_click));


      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
         setupWindowAnimations();
      }

      if (savedInstanceState != null) {
         String searchString = savedInstanceState.getString(EXTRA_SEARCH_TEXT);
         mIsSearchExpanded = savedInstanceState.getBoolean(EXTRA_SEARCH_EXPANDED, false);
         if (searchString == null) {
            searchString = "";
         }
         mQueryString = searchString;
         getCompatActivity().supportInvalidateOptionsMenu();
      }

      LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
      layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
      myRecyclerView.setLayoutManager(layoutManager);
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
          myRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), null));
      }
      myRecyclerView.setHasFixedSize(true);
      myRecyclerView.setEmptyView(mEmptyView);

      FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
      Fragment        fragment        = fragmentManager.findFragmentByTag(SearchTaskFragment.class.getSimpleName());

      if (fragment == null) {
         fragmentTask = new SearchTaskFragment();
         getSupportFragmentManager()
                 .beginTransaction()
                 .add(fragmentTask, fragmentTask.getClass().getSimpleName())
                 .commit();
      } else {
         fragmentTask = (SearchTaskFragment) fragment;
      }

      fragmentTask.setTargetFragment(this, 0);
      fragmentTask.setSpotifyService(mSpotify);

      if (mRetainedFragment.getData() == null) {
         mySearchResultAdapter = new ArtistSearchResultAdapter(new ArrayList<Artist>());
      } else {
         if (!fragmentTask.isTaskRunning()) {
            mySearchResultAdapter = new ArtistSearchResultAdapter(mRetainedFragment.getData());
         } else {
            mySearchResultAdapter = new ArtistSearchResultAdapter(new ArrayList<Artist>());
         }
      }

      if (fragmentTask.isTaskRunning()) {
         showProgressIndeterminate(true);
      } else {
         showProgressIndeterminate(false);
      }

      mySearchResultAdapter.setRecyclerCallbacks(new MySimpleRecyclerCallback());
      myRecyclerView.setAdapter(mySearchResultAdapter);

      return view;
   }

   @TargetApi(Build.VERSION_CODES.LOLLIPOP)
   private void setupWindowAnimations() {
      Explode explode = new Explode();
      explode.setDuration(DURATION);
      getWindow().setExitTransition(explode);

      Fade fade = new Fade();
      fade.setDuration(DURATION);
      getWindow().setReenterTransition(fade);
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.search_fragment_menu, menu);

      final MenuItem searchItem = menu.findItem(R.id.actionSearch);

      SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

      final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
      searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

      if (mIsSearchExpanded) {
         MenuItemCompat.expandActionView(searchItem);
         searchView.setIconified(false);
      }

      searchView.setQuery(mQueryString, false);

      searchView.setOnQueryTextListener(mQueryTextListener);
      searchView.setOnCloseListener(new SearchView.OnCloseListener() {
         @Override
         public boolean onClose() {
            mQueryString = "";
            mIsSearchExpanded = false;

            return false;
         }
      });

      searchView.setOnSearchClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            mIsSearchExpanded = true;
         }
      });

      searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
         @Override
         public boolean onSuggestionSelect(int position) {
            String suggestion = (String) searchView.getSuggestionsAdapter().getItem(position);
            searchView.setQuery(suggestion, true);
            return true;
         }

         @Override
         public boolean onSuggestionClick(int position) {
            Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
            searchView.setQuery(cursor.getString(SearchRecentSuggestions.QUERIES_PROJECTION_QUERY_INDEX), true);
            return true;
         }
      });
   }


   private final SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
         mQueryString = query;
         SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                 MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
         suggestions.saveRecentQuery(query, null);
         fragmentTask.start(query);

         return false;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
         return false;
      }
   };

   @Override
   public void onPreExecute() {
      showProgressIndeterminate(true);
   }

   @Override
   public void onPostExecute(Response<List<Artist>> response) {
      mySearchResultAdapter.changeData(response.getData());
      mRetainedFragment.setData(response.getData());

      if (!response.isError()) {
         mEmptyText.setText(getString(R.string.msg_no_artist_found));
      } else {
         String standardErrorMessage = RetrofitErrorHandler.getStandardErrorMessage(getActivity(),
                 response.getThrowable());
         mEmptyText.setText(standardErrorMessage);
      }

      showProgressIndeterminate(false);
   }

   private static void log(String tag) {
      if (BuildConfig.DEBUG) {
         Log.d(LOG_TAG, tag);
      }
   }

   private void showProgressIndeterminate(boolean isShowing) {
      if (isShowing) {
         mProgressContainer.setVisibility(View.VISIBLE);
         myRecyclerView.setVisibility(View.GONE);
         mEmptyView.setVisibility(View.GONE);
      } else {
         mProgressContainer.setVisibility(View.GONE);
         myRecyclerView.setVisibility(View.VISIBLE);
      }
   }

   private class MySimpleRecyclerCallback extends MyRecyclerView.SimpleRecyclerCallbacks {
      @Override
      public void OnItemClick(final View view, int position) {
         super.OnItemClick(view, position);
         final Artist artist = mySearchResultAdapter.getItems().get(position);
         final Image  image  = ImageUtils.getOptimumImage(artist.images, R.integer.default_cover_image_width);

         Intent i = new Intent(getActivity(), TopTrackActivity.class);

         View   sharedView     = view.findViewById(R.id.img_artist_icon);
         String transitionName = getString(R.string.default_shared_transition_name);

         i.putExtra(TopTrackFragment.EXTRA_ARTIST_ID, artist.id);
         i.putExtra(TopTrackFragment.EXTRA_ARTIST_NAME, artist.name);
         if (image != null) {
            i.putExtra(TopTrackFragment.EXTRA_ARTIST_IMAGE, image.url);
         }

         ActivityOptionsCompat transitionActivityOptions =
                 ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                         Pair.create(sharedView, transitionName));

         ActivityCompat.startActivity(getActivity(),
                 i,
                 transitionActivityOptions.toBundle());
      }
   }

   /**
    * A Retained Fragment that contains AsyncTask to search for artists
    */
   public static class SearchTaskFragment extends TaskFragment<Response<List<Artist>>> {

      private SpotifyService   mSpotifyService;
      private SearchArtistTask mTask;

      public SearchTaskFragment() {
      }

      public void setSpotifyService(SpotifyService spotifyService) {
         mSpotifyService = spotifyService;
      }

      public void start(String... artistName) {
         mTask = new SearchArtistTask();
         mTask.execute(artistName);
      }

      /**
       * Async Task for searching Artist
       */
      public class SearchArtistTask extends AsyncTask<String, Void, Response<List<Artist>>> {

         @Override
         protected void onPreExecute() {
            super.onPreExecute();
            if (mCallbacks != null) {
               mCallbacks.onPreExecute();
            }
         }

         @SuppressWarnings("unchecked")
         @Override
         protected Response<List<Artist>> doInBackground(String... artistName) {

            Response<List<Artist>> response = new Response();
            response.setData(Collections.EMPTY_LIST);

            ArtistsPager results;

            try {
               results = mSpotifyService.searchArtists(artistName[0]);
            } catch (RetrofitError e) {
               e.printStackTrace();
               response.setThrowable(RetrofitErrorHandler.handleError(e));
               return response;
            }

            if (results.artists != null && results.artists.items.size() > 0) {
               response.setData(results.artists.items);
               return response;
            }

            return response;
         }

         @Override
         protected void onPostExecute(Response<List<Artist>> artists) {
            super.onPostExecute(artists);
            if (mCallbacks != null) {
               mCallbacks.onPostExecute(artists);
            }
         }
      }
   }

   /**
    * Artist Result adapter
    */
   public class ArtistSearchResultAdapter extends
           MyRecyclerAdapter<List<Artist>, ArtistSearchResultAdapter.ViewHolder, Artist> {

      public ArtistSearchResultAdapter(List<Artist> data) {
         super(data);
      }

      @Override
      public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
         View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_artist, parent, false);
         return new ViewHolder(view);
      }

      @Override
      public void onBindViewHolder(ViewHolder holder, int position) {
         Artist artist = getItem(position);
         holder.tvArtistTitle.setText(artist.name);

         /**
          * we will use resource to store the width setting so that we can modify
          * them on larger device if necessary
          */
         Image image = ImageUtils.getOptimumImage(artist.images,
                 getResources().getInteger(R.integer.default_list_image_width));

         if (image != null) {
            Glide.with(SearchFragment.this)
                    .load(image.url)
                    .error(R.drawable.empty)
                    .into(holder.ivArtistIcon);
         } else {
            holder.ivArtistIcon.setImageResource(R.drawable.empty);
         }
      }

      public class ViewHolder extends MyRecyclerViewHolder {
         final TextView  tvArtistTitle;
         final ImageView ivArtistIcon;

         public ViewHolder(View view) {
            super(view);
            ivArtistIcon = (ImageView) view.findViewById(R.id.img_artist_icon);
            tvArtistTitle = (TextView) view.findViewById(R.id.tv_artist_name);
         }

         @Override
         public void onClick(View view) {
            if (getRecyclerCallbacks() != null) {
               getRecyclerCallbacks().OnItemClick(view, getAdapterPosition());
            }
         }
      }
   }

}
