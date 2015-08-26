/*
 * Copyright (c) 2015. This work has been created by Brenton Durkee. Designed for use by RUF CCM
 */

package com.brentondurkee.ccm.admin;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import com.brentondurkee.ccm.R;

/**
 * Created by brenton on 7/20/15.
 */
public class AdminActivity extends FragmentActivity {
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.primaryCCM));
        toolbar.setTitleTextColor(getResources().getColor(R.color.abc_primary_text_material_dark));
        setActionBar(toolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        String type = getIntent().getStringExtra(AdminUtil.ADD_TYPE);

        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if(type.equals(AdminUtil.TYPE_EVENT)){
                ft.add(R.id.container, new EventAddFragment());
            }
            else if(type.equals(AdminUtil.TYPE_MSG)){
                ft.add(R.id.container, new MsgAddFragment());
            }
            else if(type.equals(AdminUtil.TYPE_TALK)) {
                ft.add(R.id.container, new TalkAddFragment());
            }
            else if(type.equals(AdminUtil.TYPE_SIGNUP)) {
                ft.add(R.id.container, new SignupAddFragment());
            }
            ft.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
