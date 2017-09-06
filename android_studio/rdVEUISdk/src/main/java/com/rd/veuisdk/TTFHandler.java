package com.rd.veuisdk;

import android.content.Context;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.rd.cache.GalleryImageFetcher;
import com.rd.cache.ImageCache.ImageCacheParams;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.veuisdk.adapter.TTFAdapter;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.model.TtfInfo;
import com.rd.veuisdk.net.TTFUtils;
import com.rd.veuisdk.ui.CircleProgressBarView;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;


/**
 * 字幕->字体，文字版->字体
 */
class TTFHandler {

    private GridView gridview;
    private GalleryImageFetcher fetcher;
    public TTFAdapter ttfAdapter;
    private Context context;

    public TTFHandler(GridView ttf, ITTFHandlerListener listener) {
        context = ttf.getContext();
        this.listener = listener;
        gridview = ttf;
        ImageCacheParams cacheParams = new ImageCacheParams(context,
                null);
        cacheParams.compressFormat = CompressFormat.PNG;
        // 缓冲占用系统内存的10%
        cacheParams.setMemCacheSizePercent(0.1f);
        fetcher = new GalleryImageFetcher(context, 220, 98);
        fetcher.addImageCache(context, cacheParams);

        ttfAdapter = new TTFAdapter(context, fetcher);

        gridview.setAdapter(ttfAdapter);

        gridview.setOnItemClickListener(onItemClickListener);

        refleshData(false);
    }

    void ToReset() {
        if (null != ttfAdapter) {
            ttfAdapter.ToReset();
        }
    }

    void refleshData(boolean toast) {

        fetcher.setExitTasksEarly(false);
        int count = ttfAdapter.getCount();
        if (count == 0 || count == 1) {
            int re = CoreUtils.checkNetworkInfo(gridview.getContext().getApplicationContext());
            if (re == CoreUtils.UNCONNECTED) {
                TTFData.getInstance().initilize(context);
                Utils.autoToastNomal(gridview.getContext(),
                        R.string.please_check_network);
                list_ttf = TTFData.getInstance().getAll();
                mHandler.sendEmptyMessage(TTF_PREPARED);
            } else {
                ThreadPoolUtils.execute(new Runnable() {

                    @Override
                    public void run() {
                        TTFData.getInstance().initilize(gridview.getContext());
                        list_ttf = TTFUtils.getTTF();
                        if (null != list_ttf) {
                            TTFData.getInstance().replaceAll(list_ttf);
                        } else {
                            list_ttf = TTFData.getInstance().getAll();
                        }
                        mHandler.sendEmptyMessage(TTF_PREPARED);

                    }
                });
            }
        }

    }

    private OnItemClickListener onItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            TtfInfo info = ttfAdapter.getItem(position);
            if (info.isdownloaded() || position == 0) {
                listener.onItemClick(info.local_path, position);
                ttfAdapter.setCheck(position);
            } else {
                ImageView state = (ImageView) view.findViewById(R.id.ttf_state);
                CircleProgressBarView pbar = (CircleProgressBarView) view
                        .findViewById(R.id.ttf_pbar);
                if (null != state && null != pbar) {
                    ttfAdapter.onDown(position, state, pbar);
                } else {
                    Utils.autoToastNomal(parent.getContext(),
                            R.string.download_error);
                }
            }
        }
    };

    void setChecked(int position) {
        ttfAdapter.setCheck(position);
    }

    private ITTFHandlerListener listener;

    interface ITTFHandlerListener {
        /**
         * @param ttf
         */
        public void onItemClick(String ttf, int position);
    }

    private ArrayList<TtfInfo> list_ttf;
    private final int TTF_PREPARED = 4;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {

                case TTF_PREPARED: {
                    if (list_ttf == null) {
                        break;
                    }
                    TtfInfo defaultTtf = new TtfInfo();
                    defaultTtf.local_path = context.getString(R.string.default_ttf);
                    defaultTtf.code = "fefaultttf";
                    list_ttf.add(defaultTtf);
                    for (int i = list_ttf.size() - 2; i >= 0; i--) {
                        list_ttf.set(i + 1, list_ttf.get(i));
                    }
                    list_ttf.set(0, defaultTtf);

                    ttfAdapter.add(list_ttf);
                    this.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            ttfAdapter.setListview(gridview);

                        }
                    }, 300);
                }
                break;

                default:
                    break;
            }

        }

        ;
    };

    void onPasue() {
        if (null != fetcher) {
            fetcher.setExitTasksEarly(true);
            fetcher.flushCache();
        }
        ttfAdapter.onDestory();
    }

    void onDestory() {
        onPasue();
        fetcher.cleanUpCache();
        fetcher = null;
        listener = null;
    }

}
