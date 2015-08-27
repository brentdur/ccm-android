package com.brentondurkee.ccm.events;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brentondurkee.ccm.Log;
import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.Utils;
import com.brentondurkee.ccm.provider.DataContract;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class EventDetail extends AppCompatActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.primaryCCM));
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new EventDetailFragment())
                    .commit();
        }



    }

    /**
     * A fragment to show the event details
     */
    public static class EventDetailFragment extends Fragment implements OnMapReadyCallback {

        Cursor cursor;
        ContentResolver mResolver;
        Thread t;
        private String location;

        private final String TAG = getClass().getSimpleName();

        private GoogleMap mMap; // Might be null if Google Play services APK is not available.
        private SupportMapFragment m;
        private double[] latLng;
        private boolean open = false;
        private TextView openButton;

        final String[] PROJECTION = new String[]{
                DataContract.Event.COLUMN_NAME_TITLE,
                DataContract.Event.COLUMN_NAME_LOCATION,
                DataContract.Event.COLUMN_NAME_DATE,
                DataContract.Event.COLUMN_NAME_DESCRIPTION,
                DataContract.Event.COLUMN_NAME_LAT,
                DataContract.Event.COLUMN_NAME_LNG
        };

        final String selection = DataContract.Event._ID + " = '";

        public EventDetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            Log.v(TAG, "create view");

            latLng = new double[2];

            Bundle extras = getActivity().getIntent().getExtras();
            String id = extras.getString("id");
            mResolver = getActivity().getContentResolver();
            cursor = mResolver.query(DataContract.Event.CONTENT_URI, PROJECTION, selection + id + "'", null, null);
            Log.v("Event Detail", ""+cursor.getCount());
            cursor.moveToFirst();
            String title = cursor.getString(0);
            String location = cursor.getString(1);
            this.location = location;
            String date = cursor.getString(2);
            View rootView = inflater.inflate(R.layout.fragment_event_detail, container, false);
            t = Utils.timer(date, (TextView) rootView.findViewById(R.id.eventDetailDate), this.getActivity());
            t.start();
            date = Utils.dateTo(date);
            String description = cursor.getString(3);
            latLng[0] = cursor.getDouble(4);
            latLng[1] = cursor.getDouble(5);
            cursor.close();

            ((TextView) rootView.findViewById(R.id.eventDetailTitle)).setText(title);
            ((TextView) rootView.findViewById(R.id.eventDetailLocation)).setText(location);
            rootView.findViewById(R.id.eventDetailLocation).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMaps();
                }
            });
            ((TextView) rootView.findViewById(R.id.eventDetailDate)).setText(date);
            ((TextView) rootView.findViewById(R.id.eventDetailDesc)).setText(description);

            openButton = (TextView) rootView.findViewById(R.id.openMaps);
            openButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    openMaps();
                }
            });


            m = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map1));
            getChildFragmentManager().beginTransaction().hide(m).commit();
            m.getMapAsync(this);

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            Log.v(TAG, "activity");
            super.onActivityCreated(savedInstanceState);

        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            setUpMap();
        }

        /**
         * This is where we can add markers or lines, add listeners or move the camera. In this case, we
         * just add a marker near Africa.
         * <p/>
         * This should only be called once and when we are sure that {@link #mMap} is not null.
         */
        private void setUpMap() {
            mMap.addMarker(new MarkerOptions().position(new LatLng(latLng[0], latLng[1])).title(location));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng[0], latLng[1]), 15));
        }

        public void openMaps(){
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in,
                    android.R.anim.fade_out);

            if(open) {
                openButton.setText(">");
                ft.hide(m);
            } else {
                openButton.setText("V");
                ft.show(m);
            }
            ft.commit();
            open = !open;
        }

        @Override
        public void onPause() {
            super.onPause();
            t.interrupt();
        }
    }
}
