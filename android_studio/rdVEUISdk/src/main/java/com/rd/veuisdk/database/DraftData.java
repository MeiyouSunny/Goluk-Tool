package com.rd.veuisdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.rd.vecore.models.EffectInfo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.Transition;
import com.rd.veuisdk.IShortVideoInfo;
import com.rd.veuisdk.model.ShortVideoInfoImp;
import com.rd.veuisdk.utils.EffectManager;
import com.rd.veuisdk.utils.ParcelableUtils;
import com.rd.veuisdk.utils.TransitionManager;

import java.util.ArrayList;
import java.util.List;


/**
 * 草稿箱
 *
 * @author JIAN
 * @create 2018/11/7
 * @Describe
 */
public class DraftData {

    private DraftData() {

    }

    private final static String TABLE_NAME = "draftInfo";
    private final static String ID = "_id";
    private final static String CREATE_TIME = "_ctime";
    private final static String VER = "_ver";
    private final static String DATA = "_data";

    /**
     * 创建表
     *
     * @param db
     */
    public static void createTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" + ID
                + " INTEGER PRIMARY KEY," + CREATE_TIME + " LONG ," + VER
                + " INTEGER  ," + DATA + " TEXT )";
        // 如果该表已存在则删除
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(sql);
    }

    private DatabaseRoot root;

    private static DraftData instance = null;

    public static DraftData getInstance() {

        if (null == instance) {
            instance = new DraftData();
        }
        return instance;
    }

    public void initilize(Context context) {
        if (null == root) {
            root = new DatabaseRoot(context.getApplicationContext());
        }
    }


    /**
     * 新增单条短视频
     *
     * @param info
     * @return
     */
    public long insert(ShortVideoInfoImp info) {
        SQLiteDatabase db = root.getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put(VER, info.getVer());
            cv.put(DATA, ParcelableUtils.toParcelStr(info));
            cv.put(CREATE_TIME, info.getCreateTime());
            long re = db.insert(TABLE_NAME, null, cv);
            db.close();
            return re;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

    }

    /**
     * @param info
     * @return
     */
    public long update(ShortVideoInfoImp info) {
        SQLiteDatabase db = root.getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put(VER, info.getVer());
            cv.put(DATA, ParcelableUtils.toParcelStr(info));
            cv.put(CREATE_TIME, info.getCreateTime());

            long re = db.update(TABLE_NAME, cv, ID + " = ? ", new String[]{Integer.toString(info.getId())});
            db.close();
            return re;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

    }

    /**
     * 全部草稿箱视频
     *
     * @return
     */
    public ArrayList<IShortVideoInfo> getAll() {
        ArrayList<IShortVideoInfo> list = new ArrayList<IShortVideoInfo>();
        SQLiteDatabase db = root.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, null, null, null, null, CREATE_TIME + " desc ");
        if (null != c) {
            while (db.isOpen() && c.moveToNext()) {
                ShortVideoInfoImp infoImp = readItem(c);
                if (null != infoImp) {
                    list.add(infoImp);
                }
            }
            c.close();
        }
        return list;
    }

    /**
     * 读取单行数据
     *
     * @param c
     * @return
     */
    private ShortVideoInfoImp readItem(Cursor c) {
        String data = c.getString(3);
        ShortVideoInfoImp shortVideoInfoImp = ParcelableUtils.toParcelObj(data, ShortVideoInfoImp.CREATOR);
        if (null != shortVideoInfoImp) {
            shortVideoInfoImp.setId(c.getInt(0));
            restoreData(shortVideoInfoImp);
        }
        return shortVideoInfoImp;
    }

    /**
     * 还原草稿箱数据时，需要重新检测媒体绑定的滤镜 （同一个滤镜文件，每次注册返回的Id会变）
     *
     * @param shortVideoInfoImp
     */
    private void restoreData(ShortVideoInfoImp shortVideoInfoImp) {
        ArrayList<Scene> list = shortVideoInfoImp.getSceneList();
        if (null != list && list.size() > 0) {
            int len = list.size();
            for (int i = 0; i < len; i++) {
                Scene scene = list.get(i);
                //恢复转场
                Transition transition = scene.getTransition();
                if (null != transition) {
                    Object object = transition.getTag();
                    //转场，特别处理自定义转场
                    if (object instanceof String) {
                        String url = (String) object;
                        if (url.toLowerCase().startsWith("http".toLowerCase())) {
                            //一定要优先确保滤镜已经注册了
                            transition.setFilterId(TransitionManager.getInstance().getFilterId(url));
                        }
                    }
                }


                //恢复滤镜特效
                List<MediaObject> arrayList = scene.getAllMedia();
                int count = arrayList.size();
                for (int j = 0; j < count; j++) {
                    MediaObject mediaObject = arrayList.get(j);
                    ArrayList<EffectInfo> effectInfos = mediaObject.getEffectInfos();
                    int effectLen = 0;
                    if (null != effectInfos && (effectLen = effectInfos.size()) > 0) {
                        for (int n = 0; n < effectLen; n++) {
                            EffectInfo effectInfo = effectInfos.get(n);
                            Object object = effectInfo.getTag();
                            if (object instanceof String) {
                                String filter = (String) object;
                                //一定要优先确保滤镜已经注册了
                                effectInfo.setFilterId(EffectManager.getInstance().getFilterId(filter));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 查询单个草稿箱视频
     *
     * @param id
     * @return
     */
    public ShortVideoInfoImp queryOne(int id) {
        ShortVideoInfoImp shortVideoInfoImp = null;
        SQLiteDatabase db = root.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, ID + " = ?", new String[]{Integer.toString(id)}, null, null, null);
        if (null != c) {
            if (c.moveToFirst()) {
                shortVideoInfoImp = readItem(c);
            }
            c.close();
        }
        return shortVideoInfoImp;
    }


    private int delete(SQLiteDatabase db, int id) {
        return db.delete(TABLE_NAME, ID + " = ?", new String[]{Integer.toString(id)});
    }


    /**
     * 删除草稿箱视频
     *
     * @param id
     * @return
     */
    public int delete(int id) {
        if (null != root) {
            SQLiteDatabase db = root.getWritableDatabase();
            int re = delete(db, id);
            db.close();
            return re;
        }
        return -1;
    }


    /**
     * 关闭数据库连接
     */
    public void close() {
        if (null != root) {
            root.close();
        }
        instance = null;
    }
}
