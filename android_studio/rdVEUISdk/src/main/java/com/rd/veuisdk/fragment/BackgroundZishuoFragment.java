package com.rd.veuisdk.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SelectMediaActivity2;
import com.rd.veuisdk.demo.zishuo.adapter.ColorAdapter;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.model.ImageItem;
import com.rd.veuisdk.ui.ScrollLayout;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;

import static android.app.Activity.RESULT_OK;

public class BackgroundZishuoFragment extends BaseFragment{

    private IVideoEditorHandler mVideoEditorHandler;
    private RadioGroup mRadioGroup;
    private RadioButton rbVideo, rbPhoto;
    protected ScrollLayout mScrollLayout;
    private GalleryFragment mGalleryFragment;
    //控件
    private LinearLayout mLlMenu;//菜单
    private Button mBtnAddLocal;//添加本地
    private RecyclerView mRvColor;//纯色背景
    private SimpleDraweeView mIvBg;//背景图片
    private ColorAdapter mColorAdapter;

    //透明度
    private int mOldAlpha = 100;

    public static BackgroundZishuoFragment newInstance() {
        BackgroundZishuoFragment fragment = new BackgroundZishuoFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mVideoEditorHandler = (IVideoEditorHandler) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_zishuo_background, container, false);
        initView();
        //指定当前容器的高度
        Rect rect = new Rect();
        container.getGlobalVisibleRect(rect);
        Rect rect1 = new Rect();
        ViewGroup vp = (ViewGroup) container.getParent();
        vp.getGlobalVisibleRect(rect1);
        mScrollLayout.setDefaultHeight(mOtherFragmentHeight / (rect1.height() + 0.0f));
        return mRoot;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void initView() {
        mScrollLayout = $(R.id.collageScrollLayout);
        mRadioGroup = $(R.id.rgFormat);
        rbVideo = $(R.id.rbVideo);
        rbPhoto = $(R.id.rbPhoto);
        mLlMenu = $(R.id.ll_menu);
        mBtnAddLocal = $(R.id.btn_add_local);
        mRvColor = $(R.id.rv_color);
        mIvBg = $(R.id.iv_bg);

        setImage(mOldPath);

        mRadioGroup.setVisibility(View.GONE);
        $(R.id.recycleParent).setVisibility(View.GONE);
        $(R.id.tvTitle).setVisibility(View.GONE);
        $(R.id.mediaTypeLayout).setVisibility(View.VISIBLE);

        $(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取消
                back();
            }
        });

        $(R.id.btnRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //确定
                if (mLlMenu.getVisibility() == View.GONE) {
                    $(R.id.fragmentParent).setVisibility(View.GONE);
                    mLlMenu.setVisibility(View.VISIBLE);
                    mScrollLayout.setEnableFullParent(false);
                    mRadioGroup.setVisibility(View.GONE);
                } else {
                    mVideoEditorHandler.onSure();
                }
            }
        });

        mBtnAddLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //本地
                $(R.id.fragmentParent).setVisibility(View.VISIBLE);
                mLlMenu.setVisibility(View.GONE);
                mRadioGroup.setVisibility(View.VISIBLE);
                mColorAdapter.setChecked(-1);
            }
        });

        //图片
        mIvBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNewPath != null) {
                    if (mColorAdapter.getChecked() == -1) {
                        mIsAlpha = !mIsAlpha;
                        mListener.onAlpha(mIsAlpha);
                    } else {
                        mIsAlpha = true;
                        //恢复100
                        mListener.resetAlpha(100);
                        mListener.onAlpha(mIsAlpha);
                        mColorAdapter.setChecked(-1);
                        mNewColor = null;
                        mListener.onBackground(mNewPath, null);
                    }
                } else {
                    mColorAdapter.setChecked(-1);
                }

            }
        });

    }

    //返回取消
    private void back() {
        if (mLlMenu.getVisibility() == View.GONE) {
            $(R.id.fragmentParent).setVisibility(View.GONE);
            mLlMenu.setVisibility(View.VISIBLE);
            mScrollLayout.setEnableFullParent(false);
            mRadioGroup.setVisibility(View.GONE);
            //恢复
            if (mListener != null) {
                mListener.onBackground(mOldPath, mOldColor);
                mNewPath = null;
            }
        } else {
            if (mNewPath != null && !mNewPath.equals(mOldPath)
                    || mNewColor != null && !mNewColor.equals(mOldColor)) {
                onShowAlert();
            } else {
                mListener.resetAlpha(mOldAlpha);
                mVideoEditorHandler.onBack();
            }
        }
    }

    private void init() {
        //相册和视频
        mGalleryFragment = GalleryFragment.newInstance();
        rbVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mGalleryFragment) {
                    mGalleryFragment.onVideoClick();
                }
            }
        });
        rbPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mGalleryFragment) {
                    mGalleryFragment.onPhotoClick();
                }
            }
        });
        if (null == mGalleryFragment) {
            mGalleryFragment = GalleryFragment.newInstance();
        }
        mGalleryFragment.setCheckVideo(mRadioGroup.getCheckedRadioButtonId() == R.id.rbVideo);
        mGalleryFragment.setGallerySizeListener(new GalleryFragment.IGallerySizeListener() {
            @Override
            public void onGallerySizeClicked() {
                mScrollLayout.setEnableFullParent(!mScrollLayout.isFullParent());
            }
        });
        mGalleryFragment.setCallBack(new GalleryFragment.IGalleryCallBack() {
            @Override
            public void onVideo(ImageItem item) {
                mVideoCallBack.onItem(item);
            }

            @Override
            public void onPhoto(ImageItem item) {
                photoCallBack.onItem(item);
            }

            @Override
            public void onRGCheck(boolean isVideo) {
                mRadioGroup.check(isVideo ? R.id.rbVideo : R.id.rbPhoto);
            }
        });
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentParent, mGalleryFragment);
        ft.commit();

        //颜色
        mColorAdapter = new ColorAdapter(getContext(), 1);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvColor.setLayoutManager(layoutManager);
        mRvColor.setAdapter(mColorAdapter);
        mColorAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, Object item) {
                if (position != -1) {
                    mColorAdapter.setChecked(position);
                    if (mListener != null) {
                        if (TextUtils.isEmpty(mNewColor) || !mNewColor.equals((String) item)) {
                            //恢复100
                            mListener.resetAlpha(100);
                            mNewColor = (String) item;
                            mListener.onBackground(null, mNewColor);
                            mIsAlpha = true;
                        } else {
                            mIsAlpha = !mIsAlpha;
                        }
                        mListener.onAlpha(mIsAlpha);
                    }
                }
            }
        });
    }

    private void onChangeBackground(String path){
        setImage(path);
        if (mListener != null) {
            mListener.onBackground(path, null);
            mNewPath = path;
        }
    }

    /**
     * 提示是否放弃保存
     */
    private void onShowAlert() {
        SysAlertDialog.createAlertDialog(getContext(),
                getString(R.string.dialog_tips),
                getString(R.string.cancel_all_changed),
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, getString(R.string.sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mListener.resetAlpha(mOldAlpha);
                        if (mListener != null) {
                            mListener.onBackground(mOldPath, mOldColor);
                        }
                        mVideoEditorHandler.onBack();
                    }
                }, false, null).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_FOR_ADD_MEDIA) {
                String mediaPath = data.getStringExtra(IntentConstants.EXTRA_MEDIA_LIST);
                if (!TextUtils.isEmpty(mediaPath)) {
                    onChangeBackground(mediaPath);
                }
            }
        }
    }

    public static final int REQUESTCODE_FOR_ADD_MEDIA = 400;

    private GalleryFragment.ICallBack mVideoCallBack = new GalleryFragment.ICallBack() {
        @Override
        public void onItem(ImageItem item) {
            if (null == item) {
                SelectMediaActivity2.onMixMedia(getContext(), true, REQUESTCODE_FOR_ADD_MEDIA);
            } else {
                mScrollLayout.setEnableFullParent(false);
                onChangeBackground(item.image.getDataPath());
            }
        }
    };

    private GalleryFragment.ICallBack photoCallBack = new GalleryFragment.ICallBack() {
        @Override
        public void onItem(ImageItem item) {
            if (null == item) {
                SelectMediaActivity2.onMixMedia(getContext(), false, REQUESTCODE_FOR_ADD_MEDIA);
            } else {
                mScrollLayout.setEnableFullParent(false);
                onChangeBackground(item.image.getDataPath());
            }
        }
    };

    /**
     * 图片显示
     * @param url
     */
    private void setImage(String url) {
        if (TextUtils.isEmpty(url)) {
            mIvBg.setVisibility(View.GONE);
            return;
        }
        mIvBg.setVisibility(View.VISIBLE);
        mIvBg.setImageURI("file:///" + url);
    }

    private OnBackgroundListener mListener;
    private String mOldPath;
    private String mNewPath = null;
    private String mOldColor;
    private String mNewColor = null;
    private boolean mIsAlpha = true;

    public void setListener(OnBackgroundListener listener, String oldPath, String oldColor, int oldAlpha) {
        mListener = listener;
        this.mOldPath = oldPath;
        this.mOldColor = oldColor;
        mNewPath = mOldPath;
        this.mOldAlpha = oldAlpha;
    }

    public interface OnBackgroundListener {

        /**
         * 背景改变
         * @param path
         */
        void onBackground(String path, String color);

        /**
         * 透明度 显示消失
         */
        void onAlpha(boolean b);

        /**
         * 透明度恢复100
         */
        void resetAlpha(int alpha);

    }

    /**
     * 返回
     */
    @Override
    public int onBackPressed() {
        back();
        return super.onBackPressed();
    }

    public void setOtherFragmentHeight(int otherFragmentHeight) {
        mOtherFragmentHeight = otherFragmentHeight;
    }

    private int mOtherFragmentHeight = 500;

}
