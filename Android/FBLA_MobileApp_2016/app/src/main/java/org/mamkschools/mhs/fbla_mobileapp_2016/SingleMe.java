package org.mamkschools.mhs.fbla_mobileapp_2016;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.Constants;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.PictureItem;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.PictureItemAdapter;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.SecureAPI;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.Debug;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class SingleMe extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener{
    //Some static request number that is attatched to the uploader activity
    public static final int PIC_UPLOAD_REQUEST = 20;
    public FloatingActionButton fab;

    private SwipeRefreshLayout swipeRefresh;

    private File picLoc;
    private AsyncTask picGet;

    public static SingleMe newInstance(File picLoc) {
        SingleMe singleMe =  new SingleMe();
        singleMe.picLoc = picLoc;
        return singleMe;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fab = (FloatingActionButton) getActivity().findViewById(R.id.cameraButton);
        fab.setOnClickListener(this);

        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.myPicRefresh);
        swipeRefresh.setOnRefreshListener(this);

        picGet = new GetMyPictureInfo().execute();
    }


    public SingleMe() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(picGet != null && !picGet.getStatus().equals(AsyncTask.Status.FINISHED)){
            picGet.cancel(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_view_me, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.cameraButton:
                default:
                startActivityForResult(new Intent(getContext(), UploadActivity.class),
                        PIC_UPLOAD_REQUEST);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PIC_UPLOAD_REQUEST){
            new GetMyPictureInfo().execute((Void) null);
        }
    }

    @Override
    public void onRefresh() {
        picGet = new GetMyPictureInfo().execute();
    }

    private class GetMyPictureInfo extends AsyncTask<Void, Boolean, Boolean> {

        private ArrayList<PictureItem> ret = new ArrayList<>();
        SecureAPI picture = SecureAPI.getInstance(getContext());


        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                JSONObject response = picture.HTTPSGET("picture/fetch/me?authcode=" + Constants.AUTHCODE);
                Debug.log("picture/fetch/me?authcode=" + Constants.AUTHCODE);

                JSONArray array = response.getJSONArray("data");

                for(int i = 0; i < array.length(); i++ ){
                    int pid = array.getJSONObject(i).getInt("pid");
                    String title = array.getJSONObject(i).getString("title");
                    int views = array.getJSONObject(i).getInt("views");
                    int dislikes = array.getJSONObject(i).getInt("dislikes");
                    int likes = array.getJSONObject(i).getInt("likes");

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    long different = new Date().getTime() - simpleDateFormat.parse(
                            array.getJSONObject(i).getString("created")).getTime();
                    long elapsedHours = different / (1000 * 60 * 60);

                    ret.add(new PictureItem(title, elapsedHours, likes, dislikes, views, pid));
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
                RecyclerView picList = (RecyclerView) getView().findViewById(R.id.picList);
                LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
                picList.setLayoutManager(layoutManager);

                picList.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

                PictureItemAdapter adapter=new PictureItemAdapter(ret,getContext());
                picList.setAdapter(adapter);
            }else{
                Debug.log("Did not work_111");
            }
            swipeRefresh.setRefreshing(false);
        }
    }
}
