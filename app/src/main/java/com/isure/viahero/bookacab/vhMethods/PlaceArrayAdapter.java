package com.isure.viahero.bookacab.vhMethods;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;


/**
 * Created by nec on 8/30/2016.
 */
public class PlaceArrayAdapter extends ArrayAdapter<PlaceArrayAdapter.PlaceAutocomplete> implements Filterable {

    private GoogleApiClient mGoogleApiClient;
    private AutocompleteFilter mPlaceFilter;
    private LatLngBounds mBounds;
    private ArrayList<PlaceAutocomplete> mResultList;

    public PlaceArrayAdapter(Context context, int resource, LatLngBounds bounds, AutocompleteFilter filter) {
        super(context,resource);
        mBounds = bounds;
        mPlaceFilter = filter;
    }

    public void setmGoogleApiClient(GoogleApiClient googleApiClient){
        if(googleApiClient == null || !googleApiClient.isConnected()){
            mGoogleApiClient = null;
        }else {
            mGoogleApiClient = googleApiClient;
        }
    }

    @Override
    public int getCount(){
        return mResultList.size();
    }
    @Override
    public PlaceAutocomplete getItem(int position) {
        return mResultList.get(position);
    }

    private ArrayList<PlaceAutocomplete> getPredictions(CharSequence constraint){
        if(mGoogleApiClient != null){
            PendingResult<AutocompletePredictionBuffer> result = Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient,constraint.toString(),
                    mBounds,mPlaceFilter);
            AutocompletePredictionBuffer autocompletePredictions = result.await(60, TimeUnit.SECONDS);
            final Status status = autocompletePredictions.getStatus();
            if(!status.isSuccess()){
                Toast.makeText(getContext(),"Error: " + status.toString(),Toast.LENGTH_SHORT).show();
                autocompletePredictions.release();
                return null;
            }
            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount() + 1);

            while (iterator.hasNext()){
                AutocompletePrediction prediction = iterator.next();
                resultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                        prediction.getFullText(null).toString()));
            }
            autocompletePredictions.release();
            return resultList;
        }
        return null;
    }

    @Override
    public Filter getFilter(){

        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if(constraint != null){
                    mResultList = getPredictions(constraint);
                    if(mResultList != null){
                        results.values = mResultList;
                        results.count = mResultList.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results!=null && results.count>0){
                    notifyDataSetChanged();
                }else {
                    notifyDataSetInvalidated();
                }
            }
        };

        return filter;
    }

    public class PlaceAutocomplete {
        public CharSequence placeId;
        public CharSequence description;
        public PlaceAutocomplete(CharSequence placeId, CharSequence description) {
            this.placeId = placeId;
            this.description = description;
        }
        @Override
        public String toString(){
            return description.toString();
        }
    }
}
