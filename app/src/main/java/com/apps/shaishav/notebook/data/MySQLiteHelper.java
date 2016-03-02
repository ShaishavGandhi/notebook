package com.apps.shaishav.notebook.data;

/**
 * Created by Shaishav on 25-06-2015.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_COMMENTS = "answers";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_QUESTION = "question";
    public static final String COLUMN_ANSWER = "answer";
    public static final String COLUMN_LINK="link";
    public static final String COLUMN_FAVORITED = "favorite";
    public static final String COLUMN_AUTHOR="author";
    public static final String COLUMN_CATEGORY="category";
    public static final String COLUMN_OBJECTID="objectId";


    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 2;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_COMMENTS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_QUESTION
            + " text not null,"+COLUMN_ANSWER+" text not null, "+COLUMN_LINK+" text not null, "+COLUMN_AUTHOR+" text not null" +
            ", "+COLUMN_CATEGORY+" text not null,"+COLUMN_FAVORITED+" boolean not null default false,"+COLUMN_OBJECTID+" text default 'not set');";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion<newVersion){
            final String ALTER_TBL = "ALTER TABLE "+MySQLiteHelper.TABLE_COMMENTS+" ADD COLUMN "+MySQLiteHelper.COLUMN_OBJECTID+" text default 'not set'";
            db.execSQL(ALTER_TBL);
        }
    }

}

