package com.example.bbcarticlereader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class MyOpener extends SQLiteOpenHelper {

    //Declare database information
    protected final static String DATABASE_NAME = "FAVOURITESDB";
    protected final static int VERSION_NUM = 1;
    public final static String TABLE_NAME = "FAVOURITES";
    public final static String COL_TITLE = "ArticleTitle";
    public final static String COL_PUBDATE = "PublicationDate";
    public final static String COL_LINK = "ArticleLink";
    public final static String COL_DESC = "ArticleDescription";
    public final static String COL_ID = "_id";

    public MyOpener(Context ctx){super (ctx, DATABASE_NAME, null, VERSION_NUM);}


    @Override
    public void onCreate(SQLiteDatabase db){
        //create database
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " text,"
                + COL_PUBDATE + " text,"
                + COL_LINK + " text,"
                + COL_DESC + " text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME);

        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME);

        onCreate(db);
    }
}
