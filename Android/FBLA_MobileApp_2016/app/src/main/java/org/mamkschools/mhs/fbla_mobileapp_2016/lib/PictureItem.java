package org.mamkschools.mhs.fbla_mobileapp_2016.lib;

/**
 * Created by Andrew A. Katz on 4/7/2016.
 */
public class PictureItem {
    private String title;
    private long time;
    private int rate;
    private int views;
    private int pid;



    public PictureItem(String title, long time, int up, int down, int views, int pid) {
        this.title = title;
        this.time = time;
        this.rate = up - down;
        this.views = views;
        this.pid = pid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }
}
