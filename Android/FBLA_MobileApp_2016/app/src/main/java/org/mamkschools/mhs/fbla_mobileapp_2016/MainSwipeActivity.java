package org.mamkschools.mhs.fbla_mobileapp_2016;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.mamkschools.mhs.fbla_mobileapp_2016.lib.Commands;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.Constants;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.PictureHelper;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.SecureAPI;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.Debug;

import java.io.File;

import im.delight.android.location.SimpleLocation;

public class MainSwipeActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    //Fragments
    private Fragment evaluatePictures;
    private Fragment myPictures;

    private static final int LOC_PERM_REQUEST_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Must be first
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_swipe_activity);

        SimpleLocation simplocation = new SimpleLocation(this);

        // if we can't access the location yet
        if (!simplocation.hasLocationEnabled()) {
            // ask the user to enable location access
            Toast.makeText(getApplicationContext(), "Please enable location", Toast.LENGTH_LONG).show();
            SimpleLocation.openSettings(this);
        }

        //Check if we have permission for location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String [] {Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION}, LOC_PERM_REQUEST_CODE);
        }



        if (!Constants.PREFS_RESTORED) {
            Constants.restorePrefs(getApplicationContext());
        }

        //PagerAdapter
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());

        //ViewPager
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapter);


        //Tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        //noinspection deprecation
        tabLayout.setTabsFromPagerAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        PictureHelper mDbHelper = new PictureHelper(getApplicationContext());

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int picture = 0;

        File picLocation = getFilesDir();
        evaluatePictures = FragmentEvaluate.newInstance(db, picture, picLocation, simplocation);
        myPictures = SingleMe.newInstance(picLocation);

        ImageButton more = (ImageButton) findViewById(R.id.more_stuff);
        more.setOnClickListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Constants.AUTHCODE == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
        Constants.restorePrefs(getApplicationContext());



        if(!isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), "Network is not availible. Please connect to the internet", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @Override
    protected void onStop() {
        super.onStop();
        Constants.PREFS_RESTORED = false;
        Constants.savePrefs(getApplicationContext(), false);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.more_stuff:
                PopupMenu popup = new PopupMenu(this, v);
                popup.setOnMenuItemClickListener(this);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_main_swipe_activity, popup.getMenu());

                popup.show();

                //startActivity(new Intent(getApplicationContext(), null));
                break;
            default:
                Toast.makeText(this, "No Assigned Action", Toast.LENGTH_SHORT).show();

        }


    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                new Logout().execute((Void) null);
                return true;
            case R.id.action_about:
                showAbout();
                return true;
            default:
                return false;
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */


    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        private String[] titles = {"Evaluate", "Me"};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch (pos) {
                case 0:
                    return evaluatePictures;
                case 1:
                    return myPictures;
                default:
                    return evaluatePictures;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    private class Logout extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... v) {
            SecureAPI HTTPS = SecureAPI.getInstance(getApplicationContext());

            try {
                HTTPS.HTTPSGET(Commands.Get.LOGOUT + Constants.AUTHCODE);
            } catch (Exception ex) {
                Debug.log(ex.getMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            if(b) {
                Constants.AUTHCODE = null;
                Constants.savePrefs(getApplicationContext(), false);
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Logout failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void showAbout(){
        //Create an alert dialog builder for a new alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Set the title and message
        builder.setIcon(R.mipmap.ic_launcher)
                .setMessage(R.string.about_message)
                .setTitle(R.string.about_title);

        //Add an OK button
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Close dialog when user presses OK
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton(R.string.more_info,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Close dialog when user presses more, and start activity
                        startActivity(new Intent(getApplicationContext(), LegalInfoActivity.class));
                        dialog.dismiss();
                    }
                });
        //Create the actual dialog from the builder, then show it.
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOC_PERM_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! we can do what we need

                } else {
                    // permission denied, boo!
                    // tell user they need to go to permissions

                    Toast.makeText(getApplicationContext(), "Please enable location permissions", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
