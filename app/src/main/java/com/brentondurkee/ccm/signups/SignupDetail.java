package com.brentondurkee.ccm.signups;

import android.content.ContentResolver;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toolbar;

import com.brentondurkee.ccm.R;
import com.brentondurkee.ccm.Utils;
import com.brentondurkee.ccm.Log;
import com.brentondurkee.ccm.admin.AdminUtil;
import com.brentondurkee.ccm.provider.DataContract;
import com.brentondurkee.ccm.provider.SyncPosts;
import com.brentondurkee.ccm.provider.SyncUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


public class SignupDetail extends FragmentActivity{

    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.primaryCCM));
        toolbar.setTitleTextColor(getResources().getColor(R.color.abc_primary_text_material_dark));
        setActionBar(toolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SignupDetailFragment())
                    .commit();
        }



    }


    /**
     * A fragment to show the event details
     */
    public static class SignupDetailFragment extends Fragment implements OnMapReadyCallback {

        Cursor cursor;
        ContentResolver mResolver;
        private String location;
        private String address;

        private Button addButton;
        private int memberCount;
        private TextView memberCountView;
        private boolean isMember;
        private String id;

        private boolean mapDisabled = false;

        private final String TAG = getClass().getSimpleName();

        private GoogleMap mMap; // Might be null if Google Play services APK is not available.
        private SupportMapFragment m;
        private boolean open = false;
        private TextView openButton;

        final String[] PROJECTION = new String[]{
                DataContract.Signup.COLUMN_NAME_NAME,
                DataContract.Signup.COLUMN_NAME_DATE_INFO,
                DataContract.Signup.COLUMN_NAME_LOCATION,
                DataContract.Signup.COLUMN_NAME_MEMBER_COUNT,
                DataContract.Signup.COLUMN_NAME_MEMBER_OF,
                DataContract.Signup.COLUMN_NAME_ADDRESS,
                DataContract.Signup.COLUMN_NAME_DESCRIPTION,
                DataContract.Signup.COLUMN_NAME_ENTRY_ID
        };

        final String selection = DataContract.Signup._ID + " = '";

        public SignupDetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            Log.v(TAG, "create view");

            Bundle extras = getActivity().getIntent().getExtras();
            String id = extras.getString("id");
            mResolver = getActivity().getContentResolver();
            cursor = mResolver.query(DataContract.Signup.CONTENT_URI, PROJECTION, selection + id + "'", null, null);
            Log.v("Singup Detail", ""+cursor.getCount());
            cursor.moveToFirst();
            String name = cursor.getString(0);
            String dateInfo = cursor.getString(1);
            String location = cursor.getString(2);
            this.location = location;
            this.memberCount = cursor.getInt(3);
            this.isMember = cursor.getInt(4) == 1;
            this.address = cursor.getString(5);
            String description = cursor.getString(6);
            this.id = cursor.getString(7);
            View rootView = inflater.inflate(R.layout.fragment_signup_detail, container, false);
            cursor.close();

            ((TextView) rootView.findViewById(R.id.signupDetailTitle)).setText(name);
            ((TextView) rootView.findViewById(R.id.signupDetailLocation)).setText(location);
            rootView.findViewById(R.id.signupDetailLocation).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMaps();
                }
            });
            ((TextView) rootView.findViewById(R.id.signupDetailDate)).setText(dateInfo);
            ((TextView) rootView.findViewById(R.id.signupDetailDesc)).setText(description);
            this.memberCountView = (TextView) rootView.findViewById(R.id.signupDetailCount);
            this.memberCountView.setText("" + this.memberCount);
            this.addButton = (Button)rootView.findViewById(R.id.addButton);
            if (this.isMember){
                Log.v(TAG, "is already signued up");
                this.addButton.setText("Already signed up");
                this.addButton.setEnabled(false);
            }
            this.addButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    addUser();
                }
            });

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
            Geocoder geo = new Geocoder(this.getActivity());
            try{
                List<Address> list = geo.getFromLocationName(this.address, 2);
                LatLng coords = new LatLng(list.get(0).getLatitude(), list.get(0).getLongitude());
                mMap.addMarker(new MarkerOptions().position(coords).title(location));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coords, 15));
            }
            catch (Exception e){
                mapDisabled = true;
                openButton.setVisibility(View.INVISIBLE);
            }
        }

        public void openMaps() {
            if(mapDisabled){
                return;
            }
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in,
                    android.R.anim.fade_out);

            if (open) {
                openButton.setText(">");
                ft.hide(m);
            } else {
                openButton.setText("V");
                ft.show(m);
            }
            ft.commit();
            open = !open;
        }

        public void incrementMemberCount(){
            this.memberCount += 1;
            this.memberCountView.setText("" +this.memberCount);
        }

        public void addUser() {
            if(!isMember) {
                //run the network io on a different thread
                Bundle data = new Bundle();
                data.putString(SyncPosts.PUT_USER_SIGNUP, id);

                new AsyncTask<Bundle, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Bundle... data) {
                        return SyncPosts.putUserToSignup(data[0], SyncUtil.getAccount(), getActivity());
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        super.onPostExecute(aBoolean);
                        if (aBoolean) {
                            AdminUtil.toast(getActivity(), "Sign Up Successful");
                            addButton.setEnabled(false);
                            addButton.setText("You have signed up");
                            isMember = true;
                            incrementMemberCount();
                            SyncUtil.TriggerSelectiveRefresh(SyncUtil.SELECTIVE_SIGNUP);
                        } else {
                            AdminUtil.toast(getActivity(), "Failed to Sign Up");
                        }

                    }
                }.execute(data);
            }
        }
    }
}
