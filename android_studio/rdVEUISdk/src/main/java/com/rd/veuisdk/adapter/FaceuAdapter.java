package com.rd.veuisdk.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.rd.cache.ImageResizer;
import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownFileListener;
import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.faceu.FaceuListener;
import com.rd.veuisdk.manager.FaceInfo;
import com.rd.veuisdk.ui.CircleImageView;
import com.rd.veuisdk.ui.CircleProgressBarView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class FaceuAdapter extends BaseAdapter {

    private ArrayList<FaceInfo> mFaceInfoList;
    private Context mContext;
    private LayoutInflater mInflater;
    private ImageResizer mFetcher;
    public static final String NONE = "111";
    private int borderColor = 0;

    /**
     * @param c
     * @param _fetcher
     * @param _list
     * @param currentP     当前页的下标
     * @param _mPargerSize 每页总的数据
     */
    public FaceuAdapter(Context c, ImageResizer _fetcher,
                        ArrayList<FaceInfo> _list, int currentP, int _mPargerSize) {

        mContext = c;
        borderColor = mContext.getResources().getColor(R.color.main_orange);
        mInflater = LayoutInflater.from(mContext);
        mFetcher = _fetcher;
        mCurrentPage = currentP;
        mPargerSize = _mPargerSize;
        mFaceInfoList = _list;
        maps.clear();

    }

    private int mCurrentPage = 0;// 当前是%d页
    private int mPargerSize = 1;// 每页数据有多少项

    public void add(ArrayList<FaceInfo> _list, int currentP, int _mPargerSize) {
        mCurrentPage = currentP;
        mPargerSize = _mPargerSize;
        mFaceInfoList = _list;
        maps.clear();
        notifyDataSetChanged();
    }

    public void setCheck(int mPosition) {
        checkPosition = mPosition;
        notifyDataSetChanged();
    }

    private FaceuListener fuListener;

    public void setFuListener(FaceuListener listener) {
        fuListener = listener;
    }

    private int checkPosition = 0;// 0---(mFaceInfoList.size()-1)

    @Override
    public int getCount() {
        return mFaceInfoList.size() > (mCurrentPage + 1) * mPargerSize ? mPargerSize
                : (mFaceInfoList.size() - mCurrentPage * mPargerSize);
    }

    @Override
    public FaceInfo getItem(int position) {
        // return mFaceInfoList.get(position);
        return mFaceInfoList.get(position + mCurrentPage * mPargerSize);
    }

    @Override
    public long getItemId(int position) {
        // return position;
        return position + mCurrentPage * mPargerSize;
    }

    public void resetChecked() {
        notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder vh;
        onDownLoadListener listener;
        onCheckListener checkListener;
        if (null == convertView) {
            vh = new ViewHolder();
            convertView = mInflater.inflate(R.layout.face_filter_list_item_jben,
                    null);
            vh.src = (CircleImageView) convertView
                    .findViewById(R.id.ivItemImage);
            vh.state = (ImageView) convertView.findViewById(R.id.down_state);
            vh.down_layout = (FrameLayout) convertView
                    .findViewById(R.id.down_item_layout);
            vh.pbar = (CircleProgressBarView) convertView
                    .findViewById(R.id.down_pbar);
            listener = new onDownLoadListener();
            vh.down_layout.setOnClickListener(listener);
            vh.state.setTag(listener);
            checkListener = new onCheckListener();
            vh.src.setOnClickListener(checkListener);
            vh.src.setTag(checkListener);
            convertView.setTag(vh);

        } else {
            vh = (ViewHolder) convertView.getTag();
            listener = (onDownLoadListener) vh.state.getTag();
            checkListener = (onCheckListener) vh.src.getTag();
        }
        vh.src.setBorderColor(borderColor);
        vh.src.setBorderWeight(4);
        FaceInfo info = getItem(position);
        if (null != info) {
            if (position == 0 && info.getPath().equals(NONE)) {// 第一张图片
                vh.down_layout.setVisibility(View.GONE);
                vh.src.setImageResource(Integer.parseInt(info.getIcon()));
            } else {
                if (info.isExists()) {
                    vh.down_layout.setVisibility(View.GONE);
                } else {
                    vh.down_layout.setVisibility(View.VISIBLE);
                    vh.state.setVisibility(View.VISIBLE);
                    vh.pbar.setVisibility(View.GONE);
                }
                mFetcher.loadImage(info.getIcon(), vh.src);
            }

            if (checkPosition == getPositon(position)) {
                vh.src.setChecked(true);
//                vh.checked.setImageResource(R.drawable.filter_p);
//                vh.checked.setVisibility(View.VISIBLE);
            } else {
                vh.src.setChecked(false);
//                vh.checked.setImageResource(0);
//                vh.checked.setVisibility(View.GONE);
            }

            listener.setP(position, vh.state, vh.pbar);
            checkListener.setP(position, vh.src);
        }

        return convertView;
    }

    private int getPositon(int position) {

        int pos = position + mCurrentPage * mPargerSize;// 假设mPageSiize
        return pos;
    }

    private GridView listview;

    public void setListview(GridView _listview) {
        listview = _listview;
        notifyDataSetChanged();
    }

    private View getChildAt(int position) {

        try {
            return listview.getChildAt(position
                    - listview.getFirstVisiblePosition());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private class onDownLoadListener implements OnClickListener {

        private int p;
        private ImageView state;
        private CircleProgressBarView pbar;

        public void setP(int _p, ImageView state, CircleProgressBarView pb) {
            p = _p;
            this.state = state;
            pbar = pb;
        }

        @Override
        public void onClick(View v) {

            onDown(p, state, pbar);

        }

    }

    private class onCheckListener implements OnClickListener {

        private int p;
        private CircleImageView selected;

        public void setP(int _p, CircleImageView selected) {
            p = _p;
            this.selected = selected;
        }

        @Override
        public void onClick(View v) {
            onCheckItem(selected, getItem(p).getPath(), checkPosition);
            checkPosition = getPositon(p);

        }

    }

    private void onCheckItem(CircleImageView selected, String path, int lastP) {
        if (null != selected) {
            selected.setChecked(true);

        }
        if (null != fuListener) {
            fuListener.onFUChanged(path, lastP);
        }
    }

    private HashMap<Long, LineProgress> maps = new HashMap<Long, LineProgress>();
    private ArrayList<Integer> arrPosition = new ArrayList<Integer>();

    /**
     * 执行下载
     */
    private void onDown(final int p, ImageView state, CircleProgressBarView pbar) {
        if (maps.size() > 3) { // 最多同时下载3个
            return;
        }
        if (CoreUtils.checkNetworkInfo(state.getContext()) == CoreUtils.UNCONNECTED) {
            com.rd.veuisdk.utils.Utils.autoToastNomal(state.getContext(),
                    R.string.please_check_network);
            return;
        }
        for (int position : arrPosition) {
            if (position == p) {
                return;
            }
        }

        final FaceInfo info = getItem(p);
        arrPosition.add(p);
        int id = info.getUrl().hashCode();
        DownLoadUtils utils = new DownLoadUtils(id, info.getUrl(), "");
        utils.setMethod(false);
        utils.DownFile(new IDownFileListener() {

            @Override
            public void onProgress(long mid, int progress) {
                // Log.e("onprogres...." + mid, progress + "......");
                LineProgress line = maps.get(mid);
                if (null != line) {
                    line.setProgress(progress);
                    maps.put(mid, line);
                    mhandler.obtainMessage(PROGRESS, mid).sendToTarget();
                }
            }

            @Override
            public void Canceled(long mid) {
                // Log.e("Canceled....", mid + ".........");
                mhandler.obtainMessage(CANCEL, String.valueOf(mid))
                        .sendToTarget();

            }

            @Override
            public void Finished(long mid, String localPath) {
                checkPosition = getPositon(p);
                arrPosition.remove((Object) p);

                File fsrc = new File(localPath);

                File ftarget = new File(info.getPath());
                boolean re = fsrc.renameTo(ftarget);

                mhandler.obtainMessage(FINISHED, String.valueOf(mid))
                        .sendToTarget();

            }
        });
        maps.put((long) id, new LineProgress(p, 0));
        state.setVisibility(View.GONE);
        pbar.setVisibility(View.VISIBLE);
        pbar.setProgress(0);

    }

    private final int PROGRESS = 2, FINISHED = 3, CANCEL = 4;
    private long lastReflesh = System.currentTimeMillis();
    private Handler mhandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case PROGRESS:
                    if (System.currentTimeMillis() - lastReflesh > 1500) { // 减少刷新频率

                        LineProgress temp = maps.get(Long.parseLong(msg.obj
                                .toString()));
                        if (null != temp) {
                            // Log.e("progress...1111111.", msg.arg1 + "..........."
                            // + msg.arg2 + "////" + temp.toString());
                            View convertView = getChildAt(temp.getPosition());
                            if (null != convertView) {

                                FrameLayout down_layout = (FrameLayout) convertView
                                        .findViewById(R.id.down_item_layout);

                                if (null != down_layout) {
                                    ImageView state = (ImageView) convertView
                                            .findViewById(R.id.down_state);
                                    CircleProgressBarView pbar = (CircleProgressBarView) convertView
                                            .findViewById(R.id.down_pbar);
                                    down_layout.setVisibility(View.VISIBLE);
                                    state.setVisibility(View.GONE);
                                    pbar.setVisibility(View.VISIBLE);
                                    pbar.setProgress(temp.getProgress());

                                }

                            }
                        }
                        lastReflesh = System.currentTimeMillis();
                    }
                    break;
                case FINISHED: {
                    long key = Long.parseLong(msg.obj.toString());
                    if (maps.containsKey(key)) {
                        LineProgress temp = maps.get(key);
                        if (null != temp) {
                            // Log.e("progress...1111111.", msg.arg1 + "..........."
                            // + msg.arg2 + "////" + temp.toString());
                            View convertView = getChildAt(temp.getPosition());
                            CircleImageView selected = null;
                            if (null != convertView) {

                                FrameLayout down_layout = (FrameLayout) convertView
                                        .findViewById(R.id.down_item_layout);
                                if (null != down_layout) {
                                    down_layout.setVisibility(View.GONE);
                                }
                                selected = (CircleImageView) convertView
                                        .findViewById(R.id.ivItemImage);
                            }
                            onCheckItem(selected, getItem(temp.position).getPath(),
                                    checkPosition);

                            notifyDataSetChanged();
                        }
                        maps.remove(key);
                    }
                }
                break;
                case CANCEL: {
                    long key = Long.parseLong(msg.obj.toString());
                    if (maps.containsKey(key)) {
                        LineProgress temp = maps.get(key);
                        if (null != temp) {
                            View convertView = getChildAt(temp.getPosition());
                            if (null != convertView) {

                                FrameLayout down_layout = (FrameLayout) convertView
                                        .findViewById(R.id.down_item_layout);

                                if (null != down_layout) {
                                    down_layout.setVisibility(View.VISIBLE);
                                    ImageView state = (ImageView) convertView
                                            .findViewById(R.id.down_state);
                                    CircleProgressBarView pbar = (CircleProgressBarView) convertView
                                            .findViewById(R.id.down_pbar);
                                    down_layout.setVisibility(View.VISIBLE);
                                    state.setVisibility(View.VISIBLE);
                                    pbar.setVisibility(View.GONE);

                                }

                            }

                            notifyDataSetChanged();
                        }
                        maps.remove(key);
                    }
                }
                break;

                default:
                    break;
            }
        }

        ;
    };

    /**
     * 退出全部下载
     */
    public void onDestory() {
        if (null != maps && maps.size() > 0) {
            maps.clear();
            DownLoadUtils.forceCancelAll();
        }
    }

    private class ViewHolder {
        private ImageView  state;
        private CircleProgressBarView pbar;
        private CircleImageView src;
        private FrameLayout down_layout;
    }

    /**
     * 下载进度
     *
     * @author JIAN
     */
    private class LineProgress {

        private int position, progress;

        public int getPosition() {
            return position;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        @Override
        public String toString() {
            return "LineProgress [position=" + position + ", progress="
                    + progress + "]";
        }

        public LineProgress(int position, int progress) {

            this.position = position;
            this.progress = progress;

        }

    }

}
