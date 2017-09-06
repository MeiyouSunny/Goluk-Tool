package com.rd.veuisdk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseRoot extends SQLiteOpenHelper {

    private final static int NEWVERSION = 1;

    public DatabaseRoot(Context context) {
        super(context, "veuisdk.db", null, NEWVERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        TTFData.createTable(db);
        SubData.createTable(db);
        SpecialData.createTable(db);
        SDMusicData.createTable(db);
        WebMusicData.createTable(db);
        HistoryMusicData.createTable(db);
        MVData.createTable(db);
        HistoryMusicCloud.createTable(db);
    }

    @Override
    public synchronized void close() {
        super.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }
}
