package com.rd.veuisdk.mvp.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.models.DewatermarkObject;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.PathUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 马赛克|去水印的样式数据
 *
 * @author JIAN
 * @create 2019/3/26
 * @Describe
 */
public class MOFragmentModel {
    //正在获取数据中...
    private boolean isUnZipIng = false;

    /**
     * 获取样式列表
     */
    public void getData(Context context, ArrayList dbStyleList) {
        if (!isUnZipIng && dbStyleList.size() == 0) {
            isUnZipIng = true;
            initStyleList(context, dbStyleList);
        }
    }


    private void initStyleList(Context context, ArrayList<StyleInfo> list) {
        list.clear();
        fixAsset(context, list);
        isUnZipIng = false;
    }


    /**
     * @param context
     * @param dstList
     */
    private void fixAsset(Context context, ArrayList<StyleInfo> dstList) {
        AssetManager assetManager = context.getAssets();
        String file = "mosaic_square";
        String path = PathUtils.getAssetFileNameForSdcard(file, ".zip");
        CoreUtils.assetRes2File(assetManager, "mosaic/" + file + ".zip", path);
        String dstDir = PathUtils.getRdAssetPath();
        try {

            String dst = FileUtils.unzip(path, dstDir);
            {
                //1.高斯模糊
                StyleInfo info = new StyleInfo(false, false);
                info.mlocalpath = new File(dst).getAbsolutePath();
                if (!TextUtils.isEmpty(info.mlocalpath)) {
                    File f = new File(info.mlocalpath);
                    CommonStyleUtils.checkStyle(f, info);
                }
                info.setType(DewatermarkObject.Type.blur);
                dstList.add(info);
            }

            {
                //2.马赛克|像素化
                StyleInfo info = new StyleInfo(false, false);
                info.mlocalpath = new File(dst).getAbsolutePath();
                if (!TextUtils.isEmpty(info.mlocalpath)) {
                    File f = new File(info.mlocalpath);
                    CommonStyleUtils.checkStyle(f, info);
                }
                info.pid = info.hashCode();
                info.setType(DewatermarkObject.Type.mosaic);
                dstList.add(info);
            }

            {  //3.去水印
                //水印使用马赛克相同的背景资源
                StyleInfo info = new StyleInfo(false, false);
                info.mlocalpath = new File(dst).getAbsolutePath();
                if (!TextUtils.isEmpty(info.mlocalpath)) {
                    File f = new File(info.mlocalpath);
                    CommonStyleUtils.checkStyle(f, info);
                }
                info.pid = info.hashCode();
                info.setType(DewatermarkObject.Type.watermark);
                dstList.add(info);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
