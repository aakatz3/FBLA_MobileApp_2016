package org.mamkschools.mhs.fbla_mobileapp_2016.lib;

import android.provider.BaseColumns;

public class PictureContract {
    public PictureContract() {}

    public static abstract class PictureEntry implements BaseColumns {
        public static final String TABLE_NAME = "picture";
        public static final String COLUMN_NAME_PICTURE_ID = "entryid";
        public static final String COLUMN_NAME_GEOLAT = "geolat";
        public static final String COLUMN_NAME_GEOLONG = "geolong";
        public static final String COLUMN_NAME_DIST = "dist";
        public static final String COLUMN_NAME_CREATED = "created";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_VIEWS = "views";
        public static final String COLUMN_NAME_PRIORITY = "priority";
        public static final String COLUMN_NAME_HOURS = "hours";
    }
}
