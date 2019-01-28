package cn.edu.bit.codesky.minidouyin.db;

import android.provider.BaseColumns;

public final class VideoContract {

    private VideoContract() {
    }

    public static class VideoEntry implements BaseColumns{
        public static final String TABLE_NAME="Videolist";
        public static final String COLUMN_STUID="_stuid";
        public static final String COLUMN_USRNAME="usrname";
        public static final String COLUMN_IMGURL="imgurl";
        public static final String COLUMN_VIDURL="vidurl";
        public static final String COLUMN_ISLIKE="islike";
        public static final String COLUMN_LIKENUM="likenum";
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE "+ VideoEntry.TABLE_NAME+" ("+
                    VideoEntry.COLUMN_VIDURL+" TEXT PRIMARY KEY,"+
                    VideoEntry.COLUMN_STUID+" TEXT,"+
                    VideoEntry.COLUMN_USRNAME+" TEXT,"+
                    VideoEntry.COLUMN_IMGURL+" TEXT,"+
                    VideoEntry.COLUMN_ISLIKE+" INTEGER,"+
                    VideoEntry.COLUMN_LIKENUM+" INTEGER)";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "+ VideoEntry.TABLE_NAME;

}
