package org.mamkschools.mhs.fbla_mobileapp_2016;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.design.widget.FloatingActionButton;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.Commands;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.Constants;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.PictureEntry;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.PictureHelper;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.SecureAPI;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.Debug;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import im.delight.android.location.SimpleLocation;

/**
 * Fragment for evaluating pictures. This is on the main screen when the app is launched.
 * Created by jackphillips on 2/16/16.
 */
public class FragmentEvaluate extends Fragment implements View.OnClickListener{
    private SQLiteDatabase db;
    private int picNumber;
    private File location;
    private SimpleLocation simpleLocation;
    private boolean runOnce = true;

    private View rootView;

    private ImageView image;
    private TextView descriptionLabel;
    private TextView titleLabel;
    private TextView additionalLabel;


    private View ratingLayout;
    private View buttonLayout;
    private View instructions;

    private int currentRating;

    private Cursor c;

    private GetPicture picDl;


    public static FragmentEvaluate newInstance(SQLiteDatabase db, int picNumber, File location, SimpleLocation simpleLocation) {
        FragmentEvaluate fragment = new FragmentEvaluate();
        fragment.db = db;
        fragment.picNumber = picNumber;
        fragment.location = location;
        fragment.simpleLocation = simpleLocation;
        return fragment;
    }

    public FragmentEvaluate() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_view_evaluate, container, false);
        titleLabel = (TextView) rootView.findViewById(R.id.title_label);
        titleLabel.setText(R.string.no_pics);



        ratingLayout = rootView.findViewById(R.id.ratingLayout);
        buttonLayout = rootView.findViewById(R.id.buttonLayout);
        instructions = rootView.findViewById(R.id.instructions);
        additionalLabel = (TextView) rootView.findViewById(R.id.additional_label);


        image = (ImageView) rootView.findViewById(R.id.imageView);
        image.setOnClickListener(this);
        descriptionLabel = (TextView) rootView.findViewById(R.id.description_label);



        FloatingActionButton yes = (FloatingActionButton) rootView.findViewById(R.id.yesButton);
        yes.setOnClickListener(this);

        FloatingActionButton no = (FloatingActionButton) rootView.findViewById(R.id.noButton);
        no.setOnClickListener(this);

        SeekBar style = (SeekBar) rootView.findViewById(R.id.styleRating);
        style.setProgress(style.getMax() / 2);

        final Button submit = (Button) rootView.findViewById(R.id.submit_button);
        submit.setOnClickListener(this);

        // your text box
        EditText edit_txt = (EditText) rootView.findViewById(R.id.commentText);

        edit_txt.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submit.performClick();
                    return true;
                }
                return false;
            }
        });

        Button cancel = (Button) rootView.findViewById(R.id.cancel_button);
        cancel.setOnClickListener(this);



        c = getInfo();
        runFetch(picNumber);

        if(c.getCount() > 0 && picNumber < c.getCount()) {
            Debug.log("From: " + picNumber);
            c.moveToPosition(picNumber);
            int itemId = c.getInt(c.getColumnIndexOrThrow(PictureEntry.COLUMN_NAME_PICTURE_ID));
            String title = c.getString(c.getColumnIndexOrThrow(PictureEntry.COLUMN_NAME_TITLE));
            String user = c.getString(c.getColumnIndexOrThrow(PictureEntry.COLUMN_NAME_USERNAME));
            int views = c.getInt(c.getColumnIndexOrThrow(PictureEntry.COLUMN_NAME_VIEWS));
            titleLabel.setText(title.length() > 20 ? title.substring(0, 20) : title);
            descriptionLabel.setText(user);
        }

        hideKeyboard();

        return rootView;
    }

    public int getPictureId(int picture){
        if(c.getCount() > 0 && picture < c.getCount()) {
            c.moveToPosition(picture);
            return c.getInt(c.getColumnIndexOrThrow(PictureEntry.COLUMN_NAME_PICTURE_ID));
        }
        return -1;

    }

    public String[] getData(int picture){
        String[] data = new String[3];
        if(c.getCount() > 0 && picture < c.getCount()) {
            c.moveToPosition(picture);
            data[0] = c.getString(c.getColumnIndexOrThrow(PictureEntry.COLUMN_NAME_TITLE));
            data[1] = c.getString(c.getColumnIndexOrThrow(PictureEntry.COLUMN_NAME_USERNAME));
            double hours = Double.parseDouble(c.getString(c.getColumnIndexOrThrow(PictureEntry.COLUMN_NAME_HOURS)));
            double miles = Double.parseDouble(c.getString(c.getColumnIndexOrThrow(PictureEntry.COLUMN_NAME_DIST)));
            data[2] = hours + (hours == 1 ? " hour ago, " : " hours ago, ");
            data[2] += miles + (miles == 1 ? " mile away" : " miles away");
            return data;
        }
        data[0] = "No More Pictures";
        data[1] = "";
        data[2] = "";
        return data;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (picDl != null && !picDl.getStatus().equals(AsyncTask.Status.FINISHED)) {
            picDl.cancel(true);
        }
    }

    public void runFetch(int itemId) {
        int picID = getPictureId(itemId);
        String[] data = getData(itemId);
        titleLabel.setText(data[0].length() > 20 ? data[0].substring(0, 20) : data[0]);
        descriptionLabel.setText(data[1]);
        additionalLabel.setText(data[2]);
        if(picID > 0) {
            picDl = new GetPicture();
            picDl.execute(picID);
        }else{
            if(runOnce) {
                image.setImageResource(R.drawable.finish);
                new GetPictureInfo().execute((Void) null);
                runOnce = false;
            }
        }
    }
    @Override
    public void onClick(View v) {
        Map<String, String> postParams = new HashMap<>();
        int pid = getPictureId(picNumber);
        postParams.put("pid", "" + pid);
        postParams.put("like", "" + currentRating);

        if(pid == -1){
            Toast.makeText(getContext(), "No pictures left to rate", Toast.LENGTH_SHORT).show();
            return;
        }

        switch(v.getId()){
            case R.id.yesButton:
                currentRating = 1;
                break;
            case R.id.noButton:
                currentRating = 0;
                break;

            case R.id.cancel_button:
                hideKeyboard();
                new SubmitRating().execute(postParams);
                this.picNumber += 1;
                runFetch(picNumber);
                break;
            case R.id.submit_button:
                hideKeyboard();
                new SubmitRating().execute(getRateParams(postParams));
                this.picNumber += 1;
                runFetch(picNumber);
                break;
            case R.id.imageView:
                new PictureDialog(getContext(),
                        Constants.imageBitmap, titleLabel.getText().toString()).show();
                break;
        }

        if(ratingLayout.getVisibility() == View.INVISIBLE) {
            ratingLayout.setVisibility(View.VISIBLE);
            additionalLabel.setVisibility(View.INVISIBLE);
            buttonLayout.setVisibility(View.INVISIBLE);
            instructions.setVisibility(View.INVISIBLE);
        } else {
            ratingLayout.setVisibility(View.INVISIBLE);
            additionalLabel.setVisibility(View.VISIBLE);
            buttonLayout.setVisibility(View.VISIBLE);
            instructions.setVisibility(View.VISIBLE);
        }
    }

    private Map<String, String> getRateParams(Map<String, String> params) {
        SeekBar style = (SeekBar) rootView.findViewById(R.id.styleRating);
        int srating = 1 + (int) Math.round((double) style.getProgress() / (double) style.getMax() * 9.0);
        params.put("style", "" + srating);
        style.setProgress(style.getMax() / 2);

        EditText commentView = (EditText) rootView.findViewById(R.id.commentText);

        String comment = commentView.getText().toString();
        Debug.log(comment);
        commentView.setText("");
        if(comment.length() > 0)
            params.put("comment", comment);
        return params;
    }

    public Cursor getInfo(){
        String[] projection = {
                PictureEntry._ID,
                PictureEntry.COLUMN_NAME_PICTURE_ID,
                PictureEntry.COLUMN_NAME_GEOLONG,
                PictureEntry.COLUMN_NAME_GEOLAT,
                PictureEntry.COLUMN_NAME_USERNAME,
                PictureEntry.COLUMN_NAME_VIEWS,
                PictureEntry.COLUMN_NAME_TITLE,
                PictureEntry.COLUMN_NAME_DIST,
                PictureEntry.COLUMN_NAME_HOURS
        };

        String sortOrder =
                PictureEntry.COLUMN_NAME_PICTURE_ID + " ASC";
        if(db != null) {
            return db.query(
                    PictureEntry.TABLE_NAME,                  // The table to query
                    projection,                               // The columns to return
                    null,                                     // The columns for the WHERE clause
                    null,                                     // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder                                 // The sort order
            );
        } else {
            return null;
        }
    }


    private class GetPicture extends AsyncTask<Integer, Void, Boolean> {
        SecureAPI picture = SecureAPI.getInstance(getContext());

        @Override
        protected Boolean doInBackground(Integer... params) {
            try{
                int pid = params[0];
                Debug.log("picture/" + pid + "?authcode=" + Constants.AUTHCODE);
                picture.HTTPSFETCHPIC("picture/" + pid + "?authcode=" + Constants.AUTHCODE, new File(location, "picture.jpg"));
                Constants.imageBitmap = PictureHelper.getPictureBitmap(new File(location, "picture.jpg"));
            }catch(Exception e){
                Debug.log(e.getMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean v) {
            if(v){
                image.setImageBitmap(Constants.imageBitmap);
            }else{
                Debug.log("Life will go on");
            }
        }
    }

    private class SubmitRating extends AsyncTask<Map<String, String>, Void, Boolean> {
        SecureAPI picture = SecureAPI.getInstance(getContext());
        JSONObject result;

        @SafeVarargs
        @Override
        protected final Boolean doInBackground(Map<String, String>... params) {

            Map<String, String> finalParams = params[0];
            try {
                result = picture.HTTPSPOST("picture/" + finalParams.get("pid")
                        + "/comment?authcode=" + Constants.AUTHCODE, finalParams);
            } catch(Exception e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean v) {
            if(v) {
                Debug.log("Rating worked");
            } else {
                Toast.makeText(rootView.getContext(), "Rating failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class GetPictureInfo extends AsyncTask<Void, Void, Void> {

        private ArrayList<JSONObject> ret = new ArrayList<>();
        SecureAPI picture = SecureAPI.getInstance(getContext());


        @Override
        protected Void doInBackground(Void... params) {
            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;


            int dist = 10000;
            int view = 15;

            //debug added actual locations
            Constants.LATITUDE = simpleLocation.getLatitude();
            Constants.LONGITUDE = simpleLocation.getLongitude();

            db.execSQL("Delete from " + PictureEntry.TABLE_NAME);
            ContentValues values = new ContentValues();

            Debug.log(Constants.LATITUDE + " " + Constants.LONGITUDE);

            try {
                JSONObject response = picture.HTTPSGET(Commands.Get.FETCH + Constants.AUTHCODE + "&ft_me=1" + "&ft_views=" + view
                        + "&geolong=" + Constants.LONGITUDE + "&geolat=" + Constants.LATITUDE + "&ft_dist=" + dist);

                JSONArray array = response.getJSONArray("data");

                for(int i = 0; i < array.length(); i++ ){

                    int views = array.getJSONObject(i).getInt("views");
                    values.put(PictureEntry.COLUMN_NAME_PICTURE_ID, array.getJSONObject(i).getInt("pid"));
                    values.put(PictureEntry.COLUMN_NAME_GEOLAT, array.getJSONObject(i).getDouble("geolat"));
                    values.put(PictureEntry.COLUMN_NAME_GEOLONG, array.getJSONObject(i).getDouble("geolong"));
                    values.put(PictureEntry.COLUMN_NAME_DIST, Math.round(array.getJSONObject(i).getDouble("dist")));
                    values.put(PictureEntry.COLUMN_NAME_TITLE, array.getJSONObject(i).getString("title"));
                    values.put(PictureEntry.COLUMN_NAME_USERNAME, array.getJSONObject(i).getString("username"));
                    values.put(PictureEntry.COLUMN_NAME_VIEWS, views);
                    values.put(PictureEntry.COLUMN_NAME_CREATED, array.getJSONObject(i).getString("created"));

                    //Calculates priority
                    int p;
                    if(views <10)
                        p = (int) ((30 * Math.log(11 - views))/(Math.log(11)) + 40);
                    else
                        p = 30/(views - 10);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    long different = new Date().getTime() -
                            simpleDateFormat.parse(array.getJSONObject(i)
                                    .getString("created")).getTime();
                    long elapsedHours = different / hoursInMilli;
                    if(elapsedHours < 10) {
                        p += 3 * elapsedHours;
                    } else {
                        p += 30;
                    }
                    values.put(PictureEntry.COLUMN_NAME_HOURS, elapsedHours);
                    values.put(PictureEntry.COLUMN_NAME_PRIORITY, p);

                    //adds only pictures we want to db
                    if(views < 15 && elapsedHours < 120) {
                        db.insert(
                                PictureEntry.TABLE_NAME,
                                "null",
                                values);
                    }

                }
            }catch (Exception e){
                if(Debug.DEBUG_MODE){
                    Debug.log(e.getMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            Debug.log("Finished getting Picture Information");
            c = getInfo();
            if(c.getCount() > 0){
                runOnce = true;
            }else{
                runOnce = false;
            }
            picNumber = 0;
            runFetch(picNumber);

        }
    }
    public void hideKeyboard(){
        try {
            InputMethodManager inputManager = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(
                    getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception ignored){

        }
    }

}