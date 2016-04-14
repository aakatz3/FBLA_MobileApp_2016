package org.mamkschools.mhs.fbla_mobileapp_2016;

import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.Constants;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.PictureHelper;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.SecureAPI;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.Debug;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by jackphillips on 2/26/16.
 */
public class DetailMeActivity extends AppCompatActivity  {

    ImageView myImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_me);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Bundle extras = getIntent().getExtras();
        myImage = (ImageView) findViewById(R.id.myImage);
        if (extras != null) {
            int pid = extras.getInt("pid");
            String imgTitle = extras.getString("title");
            setTitle(imgTitle);
            TextView titleText = (TextView) findViewById(R.id.myImageTitle);
            titleText.setText(imgTitle);
            new GetPicture().execute(pid);
            new GetComments().execute(pid);
        }
    }

    private class GetComments extends AsyncTask<Integer, Boolean, Boolean> {

        private ArrayList<SingleComment> ret = new ArrayList<>();
        SecureAPI picture = SecureAPI.getInstance(getApplicationContext());


        @Override
        protected Boolean doInBackground(Integer... params) {

            try {
                JSONObject response = picture.HTTPSGET("picture/" + params[0] + "/comment?authcode=" + Constants.AUTHCODE);
                Debug.log("picture/" + params[0] + "/comment?authcode=" + Constants.AUTHCODE);

                JSONArray array = response.getJSONArray("data");

                for(int i = 0; i < array.length(); i++ ){
                    String style = array.getJSONObject(i).getString("style");
                    String user = array.getJSONObject(i).getString("username");
                    String comment = array.getJSONObject(i).getString("comment");
                    if(!style.equals("null")) {
                        ret.add(SingleComment.newInstance(user, comment.equals("null") ? "No comment" : comment, style));
                    }

                }
            }catch (Exception e){
                if(Debug.DEBUG_MODE){
                    Debug.log("mypics error " + e.getMessage());
                }
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean v) {
            if(v){
                Debug.log("Finished getting my pics");
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                for(int i = 0; i < ret.size(); i++){
                    ret.get(i).setShowDiv(i != ret.size() - 1);
                    transaction.add(R.id.commentList, ret.get(i), "Fragment_" + i);
                }
                transaction.commit();
            }else{
                Debug.log("Did not work_111");
            }
        }
    }

    private class GetPicture extends AsyncTask<Integer, Void, Boolean> {
        SecureAPI picture = SecureAPI.getInstance(getApplicationContext());
        Bitmap imageData;
        File picFile;

        @Override
        protected Boolean doInBackground(Integer... params) {
            try{
                int pid = params[0];
                Debug.log("picture/" + pid + "?authcode=" + Constants.AUTHCODE);
                picFile = new File(getFilesDir(), "picture.jpg");
                picture.HTTPSFETCHPIC("picture/" + pid + "?authcode=" + Constants.AUTHCODE, picFile);
            }catch(Exception e){
                Debug.log(e.getMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean v) {
            if(v){
                Constants.imageBitmap = PictureHelper.getPictureBitmap(picFile);
                myImage.setImageBitmap(Constants.imageBitmap);
            }else{
                Debug.log("Life will go on");
            }
        }
    }
}
