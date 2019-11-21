package com.rd.veuisdk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseRoot extends SQLiteOpenHelper {

    private final static int NEWVERSION = 6;

    public DatabaseRoot(Context context) {
        super(context, "veuisdk.db", null, NEWVERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        TTFData.createTable(db);
        SubData.createTable(db);
        StickerData.createTable(db);
        SDMusicData.createTable(db);
        WebMusicData.createTable(db);
        HistoryMusicData.createTable(db);
        MVData.createTable(db);
        HistoryMusicCloud.createTable(db);
        FilterData.createTable(db);
        TransitionData.createTable(db);
        DraftData.createTable(db);
        EffectData.createTable(db);
    }

    @Override
    public synchronized void close() {
        super.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 3 && newVersion >= 3) {
            TTFData.createTable(db);
            SubData.createTable(db);
            StickerData.createTable(db);
            MVData.createTable(db);
            FilterData.createTable(db);
            TransitionData.createTable(db);
        }
        if (oldVersion <= 3 && newVersion >= 4) {
            DraftData.createTable(db);
        }
        if (oldVersion <= 4 && newVersion >= 6) {
            EffectData.createTable(db);
        }
    }
}
