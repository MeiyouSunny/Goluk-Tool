package com.goluk.crazy.panda.ipc.database;

import java.sql.SQLException;

import android.content.Context;

import com.goluk.crazy.panda.ipc.database.table.TableAlbum;
import com.j256.ormlite.dao.Dao;

public class AlbumDAO {
    private Context context;
    private Dao<TableAlbum, Integer> userDaoOpe;
    private DatabaseHelper helper;

    public AlbumDAO(Context context) {
        this.context = context;
        helper = DatabaseHelper.getHelper(context);
        userDaoOpe = helper.getDao(TableAlbum.class);
    }

    /**
     * @param album
     */
    public void add(TableAlbum album) {
        try {
            userDaoOpe.create(album);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
