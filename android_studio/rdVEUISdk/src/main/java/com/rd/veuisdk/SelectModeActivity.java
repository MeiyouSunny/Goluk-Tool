package com.rd.veuisdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.ui.RotateImageView;
import com.rd.veuisdk.mix.ModeInfo;
import com.rd.veuisdk.mix.ModeUtils;
import com.rd.veuisdk.ui.CircleImageView;
import com.rd.veuisdk.ui.VideoPreviewLayout;

import java.util.ArrayList;
import java.util.List;


/**
 * 选择画中画模块
 * Created by JIAN on 2017/8/28.
 */

public class SelectModeActivity extends BaseActivity {

    private PreviewFrameLayout mPreviewFrame;
    private HomeAdapter mAdapter;
    private List<Integer> mDatas = new ArrayList<>();
    private ModeInfo currentMode = null;
    private RotateImageView mBtnBack;
    private ExtButton mBtnNext;
    private FrameLayout mModeParent;
    private RecyclerView mRecyclerModeList;
    private String TAG = "SelectModeActivity";
    public static float ASP_RATION = 1.0f;

    private void initView() {
        mBtnNext = (ExtButton) findViewById(R.id.btnNext);
        mPreviewFrame = (PreviewFrameLayout) findViewById(R.id.previewFrame);
        mBtnBack = (RotateImageView) findViewById(R.id.btnBack);
        mModeParent = (FrameLayout) findViewById(R.id.modeParent);
        mRecyclerModeList = (RecyclerView) findViewById(R.id.recyclerModeList);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextClicked();
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化画中画模式集合
        ModeUtils.init();
        setContentView(R.layout.activity_select_mode);
        initView();
        mPreviewFrame.setAspectRatio(ASP_RATION);
        initData();
        mAdapter = new HomeAdapter();
        //设置布局管理器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerModeList.setLayoutManager(linearLayoutManager);
        mRecyclerModeList.setAdapter(mAdapter);

        mAdapter.setOnItemClickLitener(new OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
                int len = listPreview.size();
                for (int i = 0; i < len; i++) {
                    mModeParent.removeView(listPreview.get(i));
                }
                listPreview.clear();
                currentMode = ModeUtils.getListMode().get(position);
                initCurrentModeLayout();

            }
        });
        currentMode = ModeUtils.getListMode().get(0);
        initCurrentModeLayout();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    static final int REQUSET_MIX_VIDEO = 1 << 2;

    public void onNextClicked() {
        Intent i = new Intent(this, MixRecordActivity.class);
        i.putExtra(MixRecordActivity.PARAM_MODE, currentMode.getList());
        i.putExtra(MixRecordActivity.PARAM_ASSET_BG, currentMode.getAssetBg());
        startActivityForResult(i, REQUSET_MIX_VIDEO);
    }


    protected void initData() {
        mDatas = new ArrayList<Integer>();
        int len = ModeUtils.getListMode().size();
        for (int i = 0; i < len; i++) {
            mDatas.add(ModeUtils.getListMode().get(i).getResId());
        }
    }


    ArrayList<Button> btnList = new ArrayList<>();
    ArrayList<VideoPreviewLayout> listPreview = new ArrayList<>();

    private void initCurrentModeLayout() {
        listPreview.clear();
        btnList.clear();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //初始化一个播放器

        int len = currentMode.getList().size();
        Context context = mModeParent.getContext();
        for (int i = 0; i < len; i++) {
            VideoPreviewLayout itemVideoParent = new VideoPreviewLayout(context, null);
            itemVideoParent.setId(i);
            mModeParent.addView(itemVideoParent, lp);
            //绘制边框线
            itemVideoParent.setCustomRect(currentMode.getNoBorderLineList().get(i));
            itemVideoParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.e(TAG, "onClick: " + view.toString());
                }
            });
            listPreview.add(itemVideoParent);

        }
    }


    private class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView mView;

        public MyViewHolder(View view) {
            super(view);
            mView = (CircleImageView) view.findViewById(R.id.item_mode);
        }

    }

    class HomeAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private int checkIndex = 0;
        private int color_ed, color_n;

        public HomeAdapter() {

            color_ed = getResources().getColor(R.color.red);
            color_n = getResources().getColor(R.color.transparent);
        }

        public int getCheckIndex() {
            return checkIndex;
        }

        private CircleImageView lastCheckEd;

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
                    SelectModeActivity.this).inflate(R.layout.item_mode_layout, parent,
                    false));

            return holder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            holder.mView.setImageResource(mDatas.get(position));
            if (checkIndex == position) {
                holder.mView.setBgColor(color_ed);
                holder.mView.setChecked(true);
                lastCheckEd = holder.mView;
            } else {
                holder.mView.setBgColor(color_n);
                holder.mView.setChecked(false);
            }

            //如果设置了回调，则设置点击事件

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkIndex != position) {
                        if (null != lastCheckEd) {
                            lastCheckEd.setBgColor(color_n);
                            lastCheckEd.setChecked(false);
                        }
                        holder.mView.setBgColor(color_ed);
                        holder.mView.setChecked(true);
                        checkIndex = position;
                        lastCheckEd = holder.mView;
                        if (mOnItemClickLitener != null) {
                            mOnItemClickLitener.onItemClick(holder.itemView, position);
                        }
                    }

                }
            });


        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        private OnItemClickLitener mOnItemClickLitener;

        public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
            this.mOnItemClickLitener = mOnItemClickLitener;
        }


    }

    /**
     * ItemClick的回调接口
     *
     * @author zhy
     */
    public interface OnItemClickLitener {
        void onItemClick(View view, int position);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent temp = new Intent();
            temp.putExtra(SdkEntry.INTENT_KEY_VIDEO_PATH, data.getStringExtra(SdkEntry.INTENT_KEY_VIDEO_PATH));
            setResult(RESULT_OK, temp);
        }
        finish();
    }
}
