package com.rd.veuisdk;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.veuisdk.adapter.TTFAdapter;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.model.TtfInfo;
import com.rd.veuisdk.net.IconUtils;
import com.rd.veuisdk.net.TTFUtils;
import com.rd.veuisdk.ui.CircleProgressBarView;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;


/**
 * 字幕->字体，文字版->字体
 */
public class TTFHandler {

    private GridView mGridView;
    public TTFAdapter mAdapter;
    private Context mContext;
    private boolean isHorizontal;
    private String mFontUrl = null;
    private boolean bCustomApi = false;


    public TTFHandler(GridView ttf, ITTFHandlerListener listener, boolean isHorizontal, String _fontUrl) {
        mContext = ttf.getContext();
        this.listener = listener;
        mGridView = ttf;
        mFontUrl = _fontUrl;
        bCustomApi = !(TextUtils.isEmpty(mFontUrl));
        this.isHorizontal = isHorizontal;
        mAdapter = new TTFAdapter(mContext, bCustomApi);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(onItemClickListener);
        refleshData();
    }

    public void ToReset() {
        if (null != mAdapter) {
            mAdapter.ToReset();
        }
    }

    public void refleshData() {
        int count = mAdapter.getCount();
        if (count == 0 || count == 1) {
            int re = CoreUtils.checkNetworkInfo(mGridView.getContext().getApplicationContext());
            TTFData.getInstance().initilize(mContext);
            if (re == CoreUtils.UNCONNECTED) {
                Utils.autoToastNomal(mGridView.getContext(),
                        R.string.please_check_network);
                list_ttf = TTFData.getInstance().getAll(bCustomApi);
                mHandler.sendEmptyMessage(TTF_PREPARED);
            } else {
                ThreadPoolUtils.execute(new Runnable() {

                    @Override
                    public void run() {
                        if (bCustomApi) {
                            list_ttf = TTFUtils.getTTFNew(mFontUrl);
                        } else {
                            list_ttf = TTFUtils.getTTF(mGridView.getContext(), new IconUtils.IconListener() {
                                @Override
                                public void prepared() {
                                    mHandler.obtainMessage(TTF_ICON_PREPARED).sendToTarget();
                                }
                            });
                        }
                        if (null != list_ttf) {
                            TTFData.getInstance().replaceAll(list_ttf);
                        } else {
                            list_ttf = TTFData.getInstance().getAll(bCustomApi);
                        }
                        mHandler.sendEmptyMessage(TTF_PREPARED);

                    }
                });
            }
        }

    }

    private OnItemClickListener onItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            TtfInfo info = mAdapter.getItem(position);
            if (info.isdownloaded() || position == 0) {
                listener.onItemClick(info.local_path, position);
                mAdapter.setCheck(position);
            } else {
                ImageView state = Utils.$(view, R.id.ttf_state);
                CircleProgressBarView pbar = Utils.$(view, R.id.ttf_pbar);
                if (null != state && null != pbar) {
                    mAdapter.onDown(position, state, pbar);
                } else {
                    Utils.autoToastNomal(parent.getContext(),
                            R.string.download_error);
                }
            }
        }
    };

    void setChecked(int position) {
        mAdapter.setCheck(position);
    }

    private ITTFHandlerListener listener;

    public interface ITTFHandlerListener {
        /**
         * @param ttf
         */
        void onItemClick(String ttf, int position);
    }

    private ArrayList<TtfInfo> list_ttf;
    private final int TTF_PREPARED = 4;
    private final int TTF_ICON_PREPARED = 5;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case TTF_PREPARED: {
                    if (list_ttf == null) {
                        break;
                    }
                    TtfInfo defaultTtf = new TtfInfo();
                    defaultTtf.local_path = mContext.getString(R.string.default_ttf);
                    defaultTtf.code = "defaultttf";
                    list_ttf.add(defaultTtf);
                    for (int i = list_ttf.size() - 2; i >= 0; i--) {
                        list_ttf.set(i + 1, list_ttf.get(i));
                    }
                    list_ttf.set(0, defaultTtf);

                    mAdapter.add(list_ttf);
                    this.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            mAdapter.setListview(mGridView);

                        }
                    }, 300);
                    if (isHorizontal) {
                        setTTFGridView();
                    }
                }
                break;
                case TTF_ICON_PREPARED:
                    if (null != mAdapter) {
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void setTTFGridView() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DisplayMetrics metrics = CoreUtils.getMetrics();
                int line = 2;
                int count = mAdapter.getCount();
                //列
                int columns = (count % line == 0) ? (count / line) : (count / line) + 1;

                int nItemWidth = metrics.widthPixels / 2;

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(columns * nItemWidth,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                mGridView.setLayoutParams(params);
                mGridView.setColumnWidth(nItemWidth);
                mGridView.setStretchMode(GridView.NO_STRETCH);
                mGridView.setNumColumns(columns);
            }
        }, 1000);
    }

    public void setItemHeight(int height) {
        mAdapter.setItemHeight(height);
    }

    public void onPasue() {
        mAdapter.onDestory();
    }

    public void onDestory() {
        onPasue();
        listener = null;
    }

}
