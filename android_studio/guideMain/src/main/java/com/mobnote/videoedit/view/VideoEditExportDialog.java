package com.mobnote.videoedit.view;

import android.app.Dialog;
import android.view.View;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.videoedit.AfterEffectActivity;
import com.mobnote.videoedit.constant.VideoEditConstant;

public class VideoEditExportDialog extends Dialog implements android.view.View.OnClickListener {
    private View m1080PTV;
    private View mHD720PTV;
    private View mCancelTV;
    private View m480PTV;
    private AfterEffectActivity mAEActivity;
    private boolean mLowQuality;
    private boolean mMidQuality;
    private boolean mHighQuality;

    public VideoEditExportDialog(AfterEffectActivity activity) {
        super(activity, R.style.CustomDialog);
        mAEActivity = activity;

        setContentView(R.layout.layout_dialog_video_export);
        initLayout();
    }

    public void setQualityVisibility(boolean lowQuality, boolean midQuality, boolean highQuality) {
        mLowQuality = lowQuality;
        mMidQuality = midQuality;
        mHighQuality = highQuality;

        if(mHighQuality) {
            m1080PTV.setVisibility(View.VISIBLE);
        } else {
            m1080PTV.setVisibility(View.GONE);
        }

        if(mMidQuality) {
            mHD720PTV.setVisibility(View.VISIBLE);
        } else {
            mHD720PTV.setVisibility(View.GONE);
        }

        if(mLowQuality) {
            m480PTV.setVisibility(View.VISIBLE);
        } else {
            m480PTV.setVisibility(View.GONE);
        }
    }

    private void initLayout() {
        m1080PTV = findViewById(R.id.ll_ae_video_export_dialog_1080p);
        mHD720PTV = findViewById(R.id.ll_ae_video_export_dialog_hd720p);
        m480PTV = findViewById(R.id.ll_ae_video_export_dialog_480p);
        mCancelTV = findViewById(R.id.tv_ae_video_export_cancel);

        m1080PTV.setOnClickListener(this);
        mHD720PTV.setOnClickListener(this);
        m480PTV.setOnClickListener(this);
        mCancelTV.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ll_ae_video_export_dialog_1080p) {
            mAEActivity.pause();
            mAEActivity.exportAfterEffectVideo(VideoEditConstant.EXPORT_1080P_WIDTH, VideoEditConstant.EXPORT_1080P_HEIGHT,
                    VideoEditConstant.BITRATE_10M, VideoEditConstant.FPS_30);
        } else if (id == R.id.ll_ae_video_export_dialog_hd720p) {
            mAEActivity.pause();
            mAEActivity.exportAfterEffectVideo(VideoEditConstant.EXPORT_HD720P_WIDTH, VideoEditConstant.EXPORT_HD720P_HEIGHT,
                    VideoEditConstant.BITRATE_4M, VideoEditConstant.FPS_30);
        } else if (id == R.id.ll_ae_video_export_dialog_480p) {
            mAEActivity.pause();
            mAEActivity.exportAfterEffectVideo(VideoEditConstant.EXPORT_480P_WIDTH, VideoEditConstant.EXPORT_480P_HEIGHT,
                    VideoEditConstant.BITRATE_2M, VideoEditConstant.FPS_25);
        } else if (id == R.id.tv_ae_video_export_cancel) {
//            dismiss();
        }
        dismiss();
    }
}

