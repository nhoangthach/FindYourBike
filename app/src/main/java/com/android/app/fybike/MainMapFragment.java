package com.android.app.fybike;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import com.android.app.fybike.map.GPSTracker;
import com.android.app.fybike.map.MapHandler;
import com.android.app.fybike.map.MapType;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MainMapFragment extends Fragment implements OnMapReadyCallback {

    private SupportMapFragment m_map;
    private GoogleMap mMap;
    private double mLatitude;
    private double mLongitude;
    private AutoCompleteTextView atvPlaces;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*EditText search = (EditText) getActivity().findViewById(R.id.txtSearch);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    return true;
                }
                return false;
            }
        });*/

        atvPlaces = (AutoCompleteTextView) getActivity().findViewById(R.id.atv_places);
        atvPlaces.setThreshold(1);

        atvPlaces.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MapHandler mapHandler = new MapHandler(getActivity(),atvPlaces,s);
                mapHandler.showAutoCompletePlaces();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        m_map = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        m_map.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        GPSTracker gps = new GPSTracker(this.getActivity());
        mLatitude= gps.getLatitude();
        mLongitude = gps.getLongitude();

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mLatitude, mLongitude), 12));
        /*googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_place)).anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                .position(new LatLng(10.7956526, 106.6772461)));*/
        try {
            googleMap.setMyLocationEnabled(true);

        }catch(SecurityException se) {
            //show something
        }

        MapHandler mapHandler = new MapHandler(mLatitude,mLongitude, MapType.CAR_REPAIR.toString(),googleMap);
        mapHandler.showNearPlaces();
        // mapHandler.getLocalLocation();

    }

}
