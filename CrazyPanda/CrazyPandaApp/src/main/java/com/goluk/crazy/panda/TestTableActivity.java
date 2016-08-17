package com.goluk.crazy.panda;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.goluk.crazy.panda.ipc.database.AlbumDAO;
import com.goluk.crazy.panda.ipc.database.table.TableAlbum;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestTableActivity extends AppCompatActivity {
    @BindView(R.id.textView)
    TextView tvInfo;
    AlbumDAO dao = new AlbumDAO();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_table);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_insert)
    void onInsert() {
        tvInfo.setText("");
        TableAlbum row = new TableAlbum();
        row.setFileName("first one");
        int count = dao.add(row);
        tvInfo.setText("Add " + String.valueOf(count) + " Row :" + row.toString());
        row.setFileName("second");
        count = dao.add(row);
        tvInfo.setText(tvInfo.getText().toString() + "\nAdd " + String.valueOf(count) + " Row :" + row.toString());
    }

    @OnClick(R.id.btn_delete)
    void onDelete() {
        List<TableAlbum> albumList = dao.getAll();
        printAll(albumList);
        dao.delete(albumList.get(0).getId());
        printAll(albumList);
    }

    @OnClick(R.id.btn_list)
    void onList() {
        printAll(dao.getAll());
    }

    private void printAll(List<TableAlbum> albumList) {
        tvInfo.setText("");
        for (TableAlbum album : albumList) {
            tvInfo.setText(tvInfo.getText().toString() + "\n" + album.toString());
        }
    }

    @OnClick(R.id.btn_modify)
    void onModify() {
        List<TableAlbum> albumList = dao.getAll();
        TableAlbum album = albumList.get(0);
        tvInfo.setText("OLD value :" + album.toString());
        album.setFileName("Goning");
        int count = dao.modify(album);
        tvInfo.setText(tvInfo.getText().toString() + "\n modify " + String.valueOf(count) + "  " + album.toString());
    }
}
