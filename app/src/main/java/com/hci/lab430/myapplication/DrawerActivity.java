package com.hci.lab430.myapplication;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.hci.lab430.myapplication.fragment.ItemFragment;
import com.hci.lab430.myapplication.fragment.LogFragment;
import com.hci.lab430.myapplication.fragment.PokemonListFragment;
import com.hci.lab430.myapplication.fragment.PokemonMapFragment;
import com.hci.lab430.myapplication.fragment.PokemonWebFragment;
import com.hci.lab430.myapplication.model.ItemFragmentManager;
import com.hci.lab430.myapplication.model.Utils;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Created by lab430 on 16/8/2.
 */
public class DrawerActivity extends CustomizedActivity implements ItemFragmentManager.OnBackStackChangedListener, ImageLoadingListener {

    private Drawer naviDrawer;
    private AccountHeader headerResult = null;
    private IProfile profile;
    private ItemFragmentManager itemFragmentManager;
    private ItemFragment[] fragments;

    private int defaultSelectedDrawerIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        //prepare fragments

        fragments = new ItemFragment[3];
        fragments[0] = PokemonListFragment.newInstance();
        ((LogFragment) fragments[0]).actualName = "f0";
        fragments[1] = PokemonWebFragment.newInstance();
        ((LogFragment) fragments[1]).actualName = "f1";
        fragments[2] = PokemonMapFragment.newInstance();
        ((LogFragment) fragments[2]).actualName = "f2";

        itemFragmentManager = new ItemFragmentManager(this,
                R.id.fragmentContainer,
                fragments,
                defaultSelectedDrawerIndex);

        itemFragmentManager.setOnBackStackChangedListener(this);

        // Set a Toolbar to replace the ActionBar.
        // so it would be laid below the drawer when the drawer comes out
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences preferences = getSharedPreferences(Application.class.getName(), MODE_PRIVATE);
        String profileName = preferences.getString(MainActivity.nameTextKey, "Batman");
        String email = preferences.getString(MainActivity.emailKey, "batman@gmail.com");
        String imgUrl = preferences.getString(MainActivity.profileImgUrlKey, null);

        if(imgUrl == null) {
            Drawable profileIcon = null;
            profileIcon = Utils.getDrawable(this, R.drawable.profile3);
            profile = new ProfileDrawerItem().withName(profileName).withEmail(email).withIcon(profileIcon);
        }
        else {
            profile = new ProfileDrawerItem().withName(profileName).withEmail(email);
            ImageLoader.getInstance().loadImage(imgUrl, this);
        }

        buildDrawerHeader(false, savedInstanceState);

        //create the drawer
        naviDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .inflateMenu(R.menu.drawer_view)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        //first item come with index 1
                        itemFragmentManager.attachFragment(fragments[position - 1], true);
                        return false; //return false to bound back the drawer after clicking
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

        //don't fire the listener
        naviDrawer.setSelectionAtPosition(defaultSelectedDrawerIndex + 1, false);

    }

    private void buildDrawerHeader(boolean compact, Bundle savedInstanceState) {
        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .withCompactStyle(compact)
                .addProfiles(profile)
                .withSavedInstance(savedInstanceState)
                .build();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = naviDrawer.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        outState = headerResult.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (naviDrawer != null && naviDrawer.isDrawerOpen()) {
            naviDrawer.closeDrawer();
        }
        else if(itemFragmentManager.mFragmentManager.getBackStackEntryCount() != 0) { //only popstack if stack is not empty
            itemFragmentManager.mFragmentManager.popBackStack();
        }
        else {
            super.onBackPressed();
        }

    }

    @Override
    public void onPushIntoBackStack() {
        //do nothing
    }

    @Override
    public void onPopOutBackStack() {
        naviDrawer.setSelectionAtPosition(itemFragmentManager.mVisibleFragment.itemIndex + 1, false);
    }

    @Override
    protected void onDestroy() {
        itemFragmentManager.releaseAll();
        itemFragmentManager = null;

        profile = null;
        headerResult = null;
        naviDrawer = null;

        fragments = null;
        super.onDestroy();
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {

    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        profile.withIcon(loadedImage);
        headerResult.clear();
        headerResult.addProfile(profile, 0);
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {

    }
}
