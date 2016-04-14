package org.mamkschools.mhs.fbla_mobileapp_2016.lib;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.mamkschools.mhs.fbla_mobileapp_2016.DetailMeActivity;
import org.mamkschools.mhs.fbla_mobileapp_2016.R;

import java.util.ArrayList;

public class CommentItemAdapter extends RecyclerView.Adapter<CommentItemAdapter.PictureViewHolder> {

    ArrayList<CommentItem> commentList;

    public CommentItemAdapter(ArrayList<CommentItem> commentList, Context context) {
        this.commentList = commentList;
    }

    @Override
    public CommentItemAdapter.PictureViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.target_single_comment
                , parent, false);
        PictureViewHolder viewHolder = new PictureViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CommentItemAdapter.PictureViewHolder holder, int position) {
        holder.commentText.setText(commentList.get(position).getComment().trim());
        holder.userName.setText(commentList.get(position).getUser());
        holder.style.setText(commentList.get(position).getStyleRating());

    }
    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class PictureViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView commentText;
        private TextView userName;
        private TextView style;


        public PictureViewHolder(View itemView) {
            super(itemView);
            commentText = (TextView) itemView.findViewById(R.id.commentText);
            userName = (TextView) itemView.findViewById(R.id.username);
            style = (TextView) itemView.findViewById(R.id.style);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(v.getContext(), commentText.getText(), Toast.LENGTH_LONG).show();
        }
    }
}