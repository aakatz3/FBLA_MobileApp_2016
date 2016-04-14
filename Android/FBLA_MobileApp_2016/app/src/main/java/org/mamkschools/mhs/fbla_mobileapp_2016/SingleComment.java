package org.mamkschools.mhs.fbla_mobileapp_2016;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class SingleComment extends android.app.Fragment {

    private String user;
    private String comment;
    private String style;
    private boolean showDiv;

    public SingleComment() {
        // Required empty public constructor
    }

    public static SingleComment newInstance(String user, String comment, String style) {
        SingleComment fragment = new SingleComment();
        fragment.user = user;
        fragment.comment = comment;
        fragment.style = style;
        fragment.showDiv = true;
        return fragment;
    }

    public void setShowDiv(boolean showDiv){
        this.showDiv = showDiv;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView text = (TextView) view.findViewById(R.id.commentText);
        text.setText(comment);
        TextView textUser = (TextView) view.findViewById(R.id.username);
        textUser.setText(user);
        TextView count = (TextView) view.findViewById(R.id.dcount);
        count.setText(style);
        //View sep = view.findViewById(R.id.sep);
        //sep.setVisibility(showDiv ? View.VISIBLE : View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.target_single_comment, container, false);
    }
}
