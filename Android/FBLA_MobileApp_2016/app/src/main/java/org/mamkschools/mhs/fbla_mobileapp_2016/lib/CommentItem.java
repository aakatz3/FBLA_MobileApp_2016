package org.mamkschools.mhs.fbla_mobileapp_2016.lib;

/**
 * Comment item in listview
 * Created by Andrew A. Katz on 4/7/2016.
 */
public class CommentItem {
    private String comment;
    private String user;
    private String styleRating;



    public CommentItem(String comment, String user, String styleRating) {

        this.comment = comment;
        this.user = user;
        this.styleRating = styleRating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getStyleRating() {
        return styleRating;
    }

    public void setStyleRating(String styleRating) {
        this.styleRating = styleRating;
    }
}
