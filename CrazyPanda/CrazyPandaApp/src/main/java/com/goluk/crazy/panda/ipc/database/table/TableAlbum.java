package com.goluk.crazy.panda.ipc.database.table;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by pavkoo on 2016/8/17.
 * Album表
 */
@DatabaseTable(tableName = "tb_album")
public class TableAlbum {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "filename")
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public TableAlbum() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", fileName=" + fileName + "]";
    }

}
