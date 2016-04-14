package org.mamkschools.mhs.fbla_mobileapp_2016;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class ViewMe extends Fragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView title;
    private TextView ucount;
    private TextView dcount;
    private TextView ellapsed;

    private ImageView image;

    private String titles;
    private int views;
    private int dislikes;
    private int likes;
    private int pid;
    private boolean showDiv;
    private long ellapsedHours;

    private File location;
    private Bitmap imageData;


    public static ViewMe newInstance(int pid, String title, int dislikes, int likes, int views, File location, long ellapsedHours) {
        ViewMe frag =  new ViewMe();
        frag.titles = title;
        frag.pid = pid;
        frag.dislikes = dislikes;
        frag.likes = likes;
        frag.views = views;
        frag.location = location;
        frag.showDiv = true;
        frag.ellapsedHours = ellapsedHours;
        return frag;
    }

    public void setShow(boolean showDiv){
        this.showDiv = showDiv;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title = (TextView) view.findViewById(R.id.TitleText);
        ucount = (TextView) view.findViewById(R.id.ucount);
        dcount = (TextView) view.findViewById(R.id.dcount);
        ellapsed = (TextView) view.findViewById(R.id.date);

        title.setText(titles);
        ucount.setText(Integer.toString(likes));
        dcount.setText(Integer.toString(dislikes));
        ellapsed.setText(String.format(getString(R.string.hours_ago), ellapsedHours));

        View div = view.findViewById(R.id.div);
        div.setVisibility(showDiv ? View.VISIBLE : View.GONE);
        view.setOnClickListener(this);
    }

    public ViewMe() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_single_me, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        Intent myintent = new Intent(getContext(), DetailMeActivity.class);
        myintent.putExtra("pid", pid);
        myintent.putExtra("title", titles);
        startActivity(myintent);
    }

}
