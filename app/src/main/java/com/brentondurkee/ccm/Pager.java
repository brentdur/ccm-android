package com.brentondurkee.ccm;

import android.support.v7.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.graphics.Color;
import android.os.Bundle;

import android.support.design.widget.TabLayout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

import com.brentondurkee.ccm.events.Events;
import com.brentondurkee.ccm.inbox.Msgs;
import com.brentondurkee.ccm.talks.Talks;

import java.util.zip.Inflater;

/**
 * Created by brenton on 6/11/15.
 */
public class Pager extends FragmentActivity {
    CollectionPagerActivity mAdapter;
    ViewPager mPager;
    private final String TAG=getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_pager);
        mAdapter = new CollectionPagerActivity(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(1);
        mPager.setPageMargin(25);
        Log.v(TAG, "created");

        TabLayout tabLayout = (TabLayout) findViewById(R.id.frame);
//        tabLayout.setTabsFromPagerAdapter(mAdapter);
        tabLayout.setupWithViewPager(mPager);
        tabLayout.setTabTextColors(Color.WHITE, Color.BLACK);
//        for(int i = 0; i < tabLayout.getTabCount(); i++){
//            mAdapter.setTabLayout(i, tabLayout.getTabAt(i), LayoutInflater.from(this), mPager);
////            tabLayout.getTabAt(i).setCustomView(R.layout.tab);
//        }
        tabLayout.getTabAt(1).select();


    }

}

class CollectionPagerActivity extends FragmentPagerAdapter {
    private final String TAG=getClass().getSimpleName();

    public CollectionPagerActivity(FragmentManager fm) {
        super(fm);
    }

    public void setTabLayout(int index, TabLayout.Tab Tab, LayoutInflater inflater, ViewPager pager){
        View view = inflater.inflate(R.layout.tab, pager, false);
//        ((Button) view.findViewById(R.id.tab)).setText(getPageTitle(index));
        Tab.setCustomView(view);
    }

    @Override
    public CharSequence getPageTitle(int position) {
//        return super.getPageTitle(position);
        switch(position){
            case 1: return "Events";
            case 0: return "Messages";
            case 2: return "Talks";
            default: return "Default";
        }
    }

    @Override
    public Fragment getItem(int position) {
        Log.v(TAG, "get item: " + position);
        switch(position){
            case 1: return new Events.EventList();
            case 0: return new Msgs.MsgList();
            case 2: return new Talks.TalkList();
            default: Log.v(TAG, "Default"); return null;

        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
