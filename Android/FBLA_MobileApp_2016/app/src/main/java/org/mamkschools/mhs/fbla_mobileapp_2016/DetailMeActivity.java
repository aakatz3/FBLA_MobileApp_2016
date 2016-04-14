package org.mamkschools.mhs.fbla_mobileapp_2016;

import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.Commands;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.CommentItem;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.CommentItemAdapter;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.Constants;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.PictureHelper;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.PictureItemAdapter;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.SecureAPI;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.Debug;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.SimpleDividerItemDecoration;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by jackphillips on 2/26/16.
 */
public class DetailMeActivity extends AppCompatActivity  {

    ImageView myImage;
    GetPicture picDownload;
    GetComments comDownload;

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
            picDownload = new GetPicture();
            picDownload.execute(pid);
            comDownload = new GetComments();
            comDownload.execute(pid);
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(comDownload != null && !comDownload.getStatus().equals(AsyncTask.Status.FINISHED)){
            comDownload.cancel(true);
        }
        if(picDownload != null && !picDownload.getStatus().equals(AsyncTask.Status.FINISHED)){
            picDownload.cancel(true);
        }
    }


    private class GetComments extends AsyncTask<Integer, Boolean, Boolean> {

        private ArrayList<CommentItem> ret = new ArrayList<>();
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
                        ret.add(new CommentItem(
                                comment.equals("null") ? "No comment" : comment, user, style));
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
                RecyclerView commentList = (RecyclerView) findViewById(R.id.commentList);

                LinearLayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
                commentList.setLayoutManager(layoutManager);

                commentList.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));

                CommentItemAdapter adapter = new CommentItemAdapter(ret,getApplicationContext());
                commentList.setAdapter(adapter);
            } else {
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
                String picFileName = Commands.Get.RAW_PIC + pid + Commands.AUTHCODE_BASE + Constants.AUTHCODE;
                Debug.log(picFileName);
                picFile = new File(getFilesDir(), "picture.jpg");
                picture.HTTPSFETCHPIC(picFileName, picFile);
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
