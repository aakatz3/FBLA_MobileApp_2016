package org.mamkschools.mhs.fbla_mobileapp_2016.lib;

import android.provider.BaseColumns;


/**
 * Contains base information about database columns
 */

public class PictureEntry  implements BaseColumns {

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


        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + PictureEntry.TABLE_NAME + " (" +
                        PictureEntry._ID + " INTEGER PRIMARY KEY," +
                        PictureEntry.COLUMN_NAME_PICTURE_ID + " INTEGER," +
                        PictureEntry.COLUMN_NAME_GEOLAT + " DECIMAL(4,2)," +
                        PictureEntry.COLUMN_NAME_GEOLONG + " DECIMAL(4,2)," +
                        PictureEntry.COLUMN_NAME_DIST  + " DECIMAL(7,12)," +
                        PictureEntry.COLUMN_NAME_TITLE  + " TEXT," +
                        PictureEntry.COLUMN_NAME_USERNAME  + " TEXT," +
                        PictureEntry.COLUMN_NAME_VIEWS + " INTEGER," +
                        PictureEntry.COLUMN_NAME_CREATED + " DATETIME," +
                        PictureEntry.COLUMN_NAME_PRIORITY + " INTEGER," +
                        PictureEntry.COLUMN_NAME_HOURS + " INTEGER" +
                        ")";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + PictureEntry.TABLE_NAME;

}
