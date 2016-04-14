package org.mamkschools.mhs.fbla_mobileapp_2016.lib;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mamkschools.mhs.fbla_mobileapp_2016.DetailMeActivity;
import org.mamkschools.mhs.fbla_mobileapp_2016.R;

import java.util.ArrayList;

public class PictureItemAdapter extends RecyclerView.Adapter<PictureItemAdapter.PictureViewHolder> {

    ArrayList<PictureItem> pictureList;

    public PictureItemAdapter(ArrayList<PictureItem> pictureList, Context context) {
        this.pictureList = pictureList;
    }

    @Override
    public PictureItemAdapter.PictureViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.target_single_picture, parent, false);
        PictureViewHolder viewHolder = new PictureViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PictureItemAdapter.PictureViewHolder holder, int position) {
        holder.title.setText(pictureList.get(position).getTitle());
        holder.pid = pictureList.get(position).getPid();
       holder.upCount.setText(Integer.toString(pictureList.get(position).getUp()));
        holder.downCount.setText(Integer.toString(pictureList.get(position).getDown()));
        holder.hours.setText(String.format(holder.hours.getContext()
                .getString(pictureList.get(position).getTime() == 1 ?
                        R.string.hour_ago : R.string.hours_ago)
                    , pictureList.get(position).getTime()));
    }
    @Override
    public int getItemCount() {
        return pictureList.size();
    }

    public static class PictureViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView upCount;
        protected TextView downCount;
        protected TextView hours;
        protected int pid;
        protected TextView title;

        public PictureViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.TitleText);
            hours = (TextView) itemView.findViewById(R.id.date);
            upCount = (TextView) itemView.findViewById(R.id.ucount);
            downCount = (TextView) itemView.findViewById(R.id.dcount);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent myIntent = new Intent(v.getContext(), DetailMeActivity.class);
            myIntent.putExtra("pid", pid);
            myIntent.putExtra("title", title.getText());
            v.getContext().startActivity(myIntent);

        }
    }
}