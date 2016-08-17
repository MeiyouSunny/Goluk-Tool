package com.goluk.crazy.panda.ipc.database;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;

import com.goluk.crazy.panda.ipc.database.table.TableAlbum;
import com.j256.ormlite.dao.Dao;

public class AlbumDAO {
    private Dao<TableAlbum, Integer> albumDAO;
    private DatabaseHelper helper;

    public AlbumDAO() {
        helper = DatabaseHelper.getHelper();
        albumDAO = helper.getDao(TableAlbum.class);
    }

    /**
     * @param album
     */
    public int add(TableAlbum album) {
        try {
            int count = albumDAO.create(album);
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public List<TableAlbum> getAll() {
        List<TableAlbum> albumList = null;
        try {
            albumList = albumDAO.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return albumList;
    }

    public int modify(TableAlbum old) {
        try {
            int count = albumDAO.update(old);
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int delete(int id) {
        try {
            int count = albumDAO.deleteById(id);
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
