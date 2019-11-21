package com.rd.veuisdk;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.TextView;

import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.veuisdk.adapter.MediaCheckedAdapter;
import com.rd.veuisdk.adapter.MediaListAdapter;
import com.rd.veuisdk.fragment.IStateCallBack;
import com.rd.veuisdk.fragment.PhotoSelectFragment;
import com.rd.veuisdk.fragment.PhotoSelectFragment.IMediaSelector;
import com.rd.veuisdk.fragment.VideoSelectFragment;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.ExtPicInfo;
import com.rd.veuisdk.model.ImageItem;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.ui.ExtViewPagerNoScroll;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SelectMediaPopHandler;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 媒体选择页
 *
 * @author jian
 * @author abreal
 */
public class SelectMediaActivity extends BaseActivity implements IMediaSelector, IStateCallBack, MediaListAdapter.IAdapterListener {
    // 请求code:读取外置存储
    private static final int REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS = 1;
    // 请求code:摄像头权限
    private static final int REQUEST_CODE_CAMERA_PERMISSIONS = 2;
    // 请求code:导出后返回
    private static final int REQUEST_CODE_EXPORT = 10;
    private boolean mIsAppend = false;
    // 视频源列表
    private VideoSelectFragment mVideoFragment;
    private PhotoSelectFragment mPhotoFragment;
    private final int REQUESTCODE_FOR_CAMERA = 2;

    private boolean mAddPhoto;

    private RadioButton mRbVideo;
    private RadioButton mRbPhoto;
    private ExtViewPagerNoScroll mVpMedia;
    private TextView mTvImportInfo;

    final static String ALBUM_FORMAT_TYPE = "album_format_type";
    final static String ALBUM_ONLY = "album_only";
    private final static String ALBUM_APPEND_MAX = "append_max";
    private static final String ACTION_APPEND = "action_append";
    static final String APPEND_IMAGE = "edit.addmenu.addimage";
    static final String PARAM_LIMIT_MIN = "param_limit_min";
    static final String LOTTIE_IMAGE = "lottie_image";


    private int mFormatType = UIConfiguration.ALBUM_SUPPORT_DEFAULT;
    //是否直接返回文件路径string  (true)，还是返回mediaobject (false)
    private boolean mIsAlbumOnly = false;

    private int mMediaCountLimit;
    private int appendMax = -1;//最大允许追加多少个

    private UIConfiguration mUIConfig;
    private boolean hasJb2 = true;

    /**
     */
    public static void appendMedia(Context context, boolean appImage, int maxAppend, int code) {
        appendMedia(context, appImage, false, maxAppend, code);
    }

    /**
     */
    public static void appendMedia(Context context, boolean appImage, boolean isLottieImage, int maxAppend, int code) {
        Intent intent = new Intent(context, SelectMediaActivity.class);
        intent.putExtra(ACTION_APPEND, true);
        intent.putExtra(APPEND_IMAGE, appImage);
        intent.putExtra(ALBUM_APPEND_MAX, maxAppend);
        intent.putExtra(LOTTIE_IMAGE, isLottieImage);
        ((Activity) context).startActivityForResult(intent, code);
    }


    /**
     * 选择媒体
     */
    @Deprecated
    public static void appendMedia(Context context, int maxAppend, int code) {
        Intent intent = new Intent(context, SelectMediaActivity.class);
        intent.putExtra(ACTION_APPEND, true);
        intent.putExtra(ALBUM_APPEND_MAX, maxAppend);
        ((Activity) context).startActivityForResult(intent, code);
    }


    /**
     * 选择单个Layer对应的媒体
     *
     * @param formatType 视频|图片
     */
    public static void appendMedia(Context context, boolean isAlbumOnly, int formatType, int maxAppend, int code) {
        Intent intent = new Intent(context, SelectMediaActivity.class);
        intent.putExtra(ACTION_APPEND, true);
        intent.putExtra(ALBUM_ONLY, isAlbumOnly);
        intent.putExtra(ALBUM_FORMAT_TYPE, formatType);
        intent.putExtra(ALBUM_APPEND_MAX, maxAppend);
        intent.putExtra(PARAM_AE_MEDIA, true);
        ((Activity) context).startActivityForResult(intent, code);
    }

    /**
     * AE详情->选择指定的类型的媒体
     *
     * @param enableRepeat 是否允许重复（允许重复时只能添加图片）， true 允许添加那个图片（无限制）；false 依据模板中的layer.size()
     */
    static void onAEMedia(Context context, int picNum, int videoNum, int requestCode, boolean enableRepeat) {
        Intent intent = new Intent(context, SelectMediaActivity.class);
        intent.putExtra(PARAM_AE_MEDIA_DETAIL, true);
        intent.putExtra(PARAM_AE_MEDIA_PIC_NUM, picNum);
        intent.putExtra(PARAM_AE_MEDIA_VIDEO_NUM, videoNum);
        intent.putExtra(PARAM_AE_REPEAT, enableRepeat);
        ((Activity) context).startActivityForResult(intent, requestCode);

    }


    private static final String PARAM_AE_MEDIA = "ae_media";
    private static final String PARAM_AE_MEDIA_DETAIL = "PARAM_AE_MEDIA_detail";
    private static final String PARAM_AE_MEDIA_PIC_NUM = "ae_media_pic_num";
    private static final String PARAM_AE_MEDIA_VIDEO_NUM = "ae_media_video_num";
    private static final String PARAM_AE_REPEAT = "ae_media_pic_repeat";
    //是否隐藏文字板
    private boolean bHideText = false;
    private RecyclerView mMediaChecked;

    private MediaCheckedAdapter mMediaCheckedAdapter;
    private View mTvMediaHint;
    private int nEditMediaIndex = -1;
    private static final int REQUEST_EDIT_TRIM = 989;
    private static final int REQUEST_EDIT_ROTATE = 990;
    private static final int REQUEST_IMPORT_TRIM = 9991;
    private static final int REQUEST_IMPORT_ROTATE = 992;

    private static final int REQUEST_EDIT_TEXT_PIC = 993;

    /**
     * 是否需要返回mediaObject 对象，如不需要（不用裁剪 、旋转 等）
     */
    private boolean bNeedResultMediaObject = true;

    private void initMediaChecked() {
        mMediaCheckedAdapter = new MediaCheckedAdapter();
        LinearLayoutManager mVerticalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mMediaChecked.setLayoutManager(mVerticalLayoutManager);
        mTvMediaHint = findViewById(R.id.tv_media_hint);
        mMediaCheckedAdapter.setOnItemClickListener(new MediaCheckedAdapter.OnItemClickListener() {
            @Override
            public void onDelete(int position) {
                if (mMediaCheckedAdapter.getItemCount() == 0) {
                    mTvMediaHint.setVisibility(View.VISIBLE);
                    btnNext.setEnabled(false);
                }
                onRefreshCount();
            }

            @Override
            public void onItemClick(int position) {
                nEditMediaIndex = position;
                if (bNeedResultMediaObject) {
                    MediaObject mediaObject = mMediaCheckedAdapter.getItem(position);
                    if (null != mediaObject) {
                        if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                            cancelLoadPhotos();
                            TrimMediaActivity.onImportTrim(SelectMediaActivity.this, mediaObject, REQUEST_EDIT_TRIM);
                        } else {
                            Object tmp = mediaObject.getTag();
                            ExtPicInfo extPicInfo;
                            if (null != tmp && tmp instanceof VideoOb && (extPicInfo = ((VideoOb) tmp).getExtpic()) != null) {
                                cancelLoadPhotos();
                                ExtPhotoActivity.editTextPic(SelectMediaActivity.this, extPicInfo, REQUEST_EDIT_TEXT_PIC);
                            } else {
                                cancelLoadPhotos();
                                Scene scene = VirtualVideo.createScene();
                                scene.addMedia(mediaObject);
                                CropRotateMirrorActivity.onImportImage(SelectMediaActivity.this, scene, REQUEST_EDIT_ROTATE);
                            }
                        }
                    }
                }
            }
        });

    }

    // -1 表示无限制，可添加任意数量个文件
    private final int DEFAULT_AE_MEDIA_LIMIT = -1;


    private int nPicLimit = DEFAULT_AE_MEDIA_LIMIT;
    private int nVideoLimit = DEFAULT_AE_MEDIA_LIMIT;
    private boolean isAEDetail = false;
    private boolean enableAERepeat = false;

    private int nMediaLimitMin = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TAG = "SelectMediaActivity";
        showNotchScreen();
        Intent intent = getIntent();
        nMediaLimitMin = intent.getIntExtra(PARAM_LIMIT_MIN, 0);
        isAEDetail = intent.getBooleanExtra(PARAM_AE_MEDIA_DETAIL, false);
        if (isAEDetail) {
            //Ae详情，选择指定类型的文件
            nPicLimit = intent.getIntExtra(PARAM_AE_MEDIA_PIC_NUM, DEFAULT_AE_MEDIA_LIMIT);
            nVideoLimit = intent.getIntExtra(PARAM_AE_MEDIA_VIDEO_NUM, DEFAULT_AE_MEDIA_LIMIT);
            enableAERepeat = intent.getBooleanExtra(PARAM_AE_REPEAT, false);
        }
        mIsAppend = intent.getBooleanExtra(ACTION_APPEND, false);
        mIsAlbumOnly = intent.getBooleanExtra(ALBUM_ONLY, false);
        if (mIsAlbumOnly) {
            //只需要返回路径即可
            bNeedResultMediaObject = false;
            bShowAddBtn = false;
        } else {
            bNeedResultMediaObject = true;
        }
        appendMax = intent.getIntExtra(ALBUM_APPEND_MAX, -1);
        mUIConfig = SdkEntry.getSdkService().getUIConfig();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_media_layout);
        mMediaChecked = $(R.id.rvCheckedMedia);
        initMediaChecked();
        mStrActivityPageName = getString(R.string.select_medias);
        mAddPhoto = intent.getBooleanExtra(APPEND_IMAGE, false);
        hasJb2 = CoreUtils.hasJELLY_BEAN_MR2();
        if (intent.getBooleanExtra(PARAM_AE_MEDIA, false)) {
            bHideText = true;
            mFormatType = intent.getIntExtra(ALBUM_FORMAT_TYPE, UIConfiguration.ALBUM_SUPPORT_DEFAULT);
        } else {
            if (intent.getBooleanExtra(LOTTIE_IMAGE, false)) {
                mFormatType = UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY;
            } else if (mIsAlbumOnly) {
                mFormatType = intent.getIntExtra(ALBUM_FORMAT_TYPE, -1);
            } else {
                mFormatType = mUIConfig.albumSupportFormatType;
            }
        }
        if (mIsAlbumOnly) {
            mMediaCheckedAdapter.setHideMediaDuration(true);
        }
        mSelectMediaPopHandler = new SelectMediaPopHandler(this, new SelectMediaPopHandler.Callback() {
            @Override
            public void onPhoto() {
                openPhotoCamera();
            }

            @Override
            public void onVideo() {
                openVideoCamera();
            }
        });
        if (isAEDetail) {
            mMediaCheckedAdapter.setEnableEditClick(false);
            mMediaCheckedAdapter.setHideMediaDuration(true);
            bHideText = true;
            //AEDetail选择媒体
            if (nVideoLimit == 0) {
                //只能选图片
                mFormatType = UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY;
            }
            mIsAlbumOnly = false;
            mIsAppend = true;
            bNeedResultMediaObject = true;
            mMediaCheckedAdapter.setHideMediaDuration(true);
        } else {
            mMediaCountLimit = mUIConfig.mediaCountLimit;
            if (appendMax > -1) {
                mMediaCountLimit = appendMax;
            }
        }


        //是否隐藏被选中的媒体类型
        if (mFormatType != UIConfiguration.ALBUM_SUPPORT_DEFAULT) {
            mMediaCheckedAdapter.setHideMediaType(true);
        }


        mMediaChecked.setAdapter(mMediaCheckedAdapter);

        Object tmp = getLastCustomNonConfigurationInstance();
        if (tmp != null) {
            mAddPhoto = (Boolean) tmp;
        }
        initView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS);
            } else {
                loadFragments();
            }
        } else {
            loadFragments();
        }
    }

    private void loadFragments() {
        mVpMedia.setAdapter(new MPageAdapter(getSupportFragmentManager()));
        if (mAddPhoto) {
            mVpMedia.post(new Runnable() {

                @Override
                public void run() {
                    mRbVideo.setChecked(false);
                    mRbPhoto.setChecked(true);
                    mVpMedia.setCurrentItem(1, false);
                }
            });
        } else {
            mRbVideo.setChecked(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        onToast(R.string.permission_album_error);
                        finish();
                        return;
                    }
                }
                loadFragments();
            }
            break;
            case REQUEST_CODE_CAMERA_PERMISSIONS:
                if (grantResults != null && grantResults.length > 0
                        && mRunnableCheckCamera != null
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mRunnableCheckCamera.run();
                    mRunnableCheckCamera = null;
                } else {
                    onToast(R.string.permission_camera_error);
                }
                break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
            }
        }
    }

    private void onAdd(MediaObject mediaObject) {
        if (View.VISIBLE == mTvMediaHint.getVisibility()) {
            mTvMediaHint.setVisibility(View.GONE);
        }
        btnNext.setEnabled(true);
        mMediaCheckedAdapter.add(mediaObject);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMPORT_TRIM) {
                Scene newScene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
                if (null != newScene) {
                    onAdd(newScene.getAllMedia().get(0));
                }
            } else if (requestCode == REQUEST_IMPORT_ROTATE) {
                Scene newScene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
                if (null != newScene) {
                    onAdd(newScene.getAllMedia().get(0));
                }
            } else if (requestCode == REQUEST_EDIT_TRIM) {

                if (nEditMediaIndex >= 0) {
                    Scene newScene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
                    if (null != newScene) {
                        mMediaCheckedAdapter.update(nEditMediaIndex, newScene.getAllMedia().get(0));
                    }
                    nEditMediaIndex = -1;
                }


            } else if (requestCode == REQUEST_EDIT_ROTATE) {

                if (nEditMediaIndex >= 0) {
                    Scene newScene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
                    if (null != newScene) {
                        mMediaCheckedAdapter.update(nEditMediaIndex, newScene.getAllMedia().get(0));
                    }
                    nEditMediaIndex = -1;
                }


            } else if (requestCode == REQUEST_EDIT_TEXT_PIC) {
                MediaObject media = data
                        .getParcelableExtra(IntentConstants.EXTRA_MEDIA_OBJECTS);
                VideoOb nvb = new VideoOb(media.getTrimStart(), media.getTrimEnd(),
                        media.getTrimStart(), media.getTrimEnd(),
                        media.getTrimStart(), media.getTrimEnd(),
                        1, (ExtPicInfo) data.getParcelableExtra(IntentConstants.EXTRA_EXT_PIC_INFO), VideoOb.DEFAULT_CROP);
                media.setTag(nvb);

                if (nEditMediaIndex >= 0) {
                    mMediaCheckedAdapter.update(nEditMediaIndex, media);
                    nEditMediaIndex = -1;
                }

            } else if (requestCode == VideoSelectFragment.CODE_EXT_PIC) {


                ArrayList<MediaObject> tempMedias = data
                        .getParcelableArrayListExtra(IntentConstants.EXTRA_MEDIA_LIST);
                int isextPic = data.getIntExtra(IntentConstants.EXTRA_EXT_ISEXTPIC, 0);

                ExtPicInfo extPicInfo = data.getParcelableExtra(IntentConstants.EXTRA_EXT_PIC_INFO);

                MediaObject tmp = tempMedias.get(0);
                if (null != tmp) {
                    tmp.setTag(new VideoOb(tmp.getTrimStart(), tmp.getTrimEnd(), tmp
                            .getTrimStart(), tmp.getTrimEnd(), tmp.getTrimStart(),
                            tmp.getTrimEnd(), isextPic, extPicInfo, VideoOb.DEFAULT_CROP));
                    onAdd(tmp);
                }


            } else if (requestCode == REQUESTCODE_FOR_CAMERA) {
                // data.hasExtra(SdkEntry.INTENT_KEY_PICTURE_PATH);getStringExtra(SdkEntry.INTENT_KEY_PICTURE_PATH);
                if (data.hasExtra(SdkEntry.INTENT_KEY_PICTURE_PATH)) {// RecorderActivity内部已经执行了插入相册的操作，这里只要刷新一下就好
                    if (mFormatType == UIConfiguration.ALBUM_SUPPORT_DEFAULT) {
                        setCheck(1);
                        mVpMedia.setCurrentItem(1, true);
                        mPhotoFragment.refresh();
                        mVideoFragment.refresh();
                    } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
                        mPhotoFragment.refresh();
                    } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY) {
                        mVideoFragment.refresh();
                    }

                } else {
                    String path = data
                            .getStringExtra(SdkEntry.INTENT_KEY_VIDEO_PATH);
                    if (!TextUtils.isEmpty(path)) {
                        try {
                            VideoConfig vcMediaInfo = new VideoConfig();
                            float duration = VirtualVideo.getMediaInfo(path, vcMediaInfo);
                            Utils.insertToGallery(SelectMediaActivity.this, path, Utils.s2ms(duration),
                                    vcMediaInfo.getVideoWidth(),
                                    vcMediaInfo.getVideoHeight());
                        } catch (Exception ex) {
                        } finally {
                            if (mFormatType == UIConfiguration.ALBUM_SUPPORT_DEFAULT) {
                                setCheck(0);
                                mVpMedia.setCurrentItem(0, true);
                                mPhotoFragment.refresh();
                                mVideoFragment.refresh();
                            } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
                                mPhotoFragment.refresh();
                            } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY) {
                                mVideoFragment.refresh();
                            }
                        }
                    } else {
                        Log.d(TAG, "path  is  null");
                    }
                }
            } else if (requestCode == REQUEST_CODE_EXPORT) {
                setResult(RESULT_OK, data);
                finish();
            }
            onRefreshCount();
        } else {
            if (requestCode == REQUEST_CODE_EXPORT) {
                setResult(RESULT_CANCELED);
                finish();
            }
            if (mFormatType == UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
                mPhotoFragment.refresh();
            } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY) {
                mVideoFragment.refresh();
            } else {
                mPhotoFragment.refresh();
                mVideoFragment.refresh();
            }
        }

    }


    @Override
    public void finish() {
        super.finish();
        if (null != mPhotoFragment) {
            mPhotoFragment.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaCheckedAdapter.purge();
        if (null != mVpMedia) {
            mVpMedia.removeOnPageChangeListener(mPageChangeListener);
        }
    }

    @Override
    public boolean isHideText() {
        if (bHideText) {
            return bHideText;
        }
        return SdkEntry.getSdkService().getUIConfig().isHideText();
    }


    @Override
    public void onAdd(ImageItem item) {
        MediaObject mediaObject = aeMediaLimitCheck(item);
        if (null != mediaObject) {
            if (checkCanAddMedia()) {
                onAddImp(mediaObject);
            }
        }
    }

    /**
     * 添加媒体到recycleview
     *
     * @param mediaObject
     */
    private void onAddImp(MediaObject mediaObject) {
        onAdd(mediaObject);
        onRefreshCount();
    }


    @Override
    public boolean isShowAddBtn() {
        return bShowAddBtn;
    }

    @Override
    public boolean isAppend() {
        return mIsAppend;
    }

    private class MPageAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments = new ArrayList<Fragment>();

        public MPageAdapter(FragmentManager fm) {
            super(fm);
            if (fm.getFragments() != null && fm.getFragments().size() > 0) {
                if (mFormatType == UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
                    Fragment fragment = fm.getFragments().get(0);
                    mPhotoFragment = (PhotoSelectFragment) fragment;
                } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY) {
                    Fragment fragment = fm.getFragments().get(0);
                    mVideoFragment = (VideoSelectFragment) fragment;
                } else {
                    for (int n = 0; n < 2; n++) {
                        Fragment fragment = fm.getFragments().get(n);
                        if (fragment instanceof VideoSelectFragment) {
                            mVideoFragment = (VideoSelectFragment) fragment;
                        } else if (fragment instanceof PhotoSelectFragment) {
                            mPhotoFragment = (PhotoSelectFragment) fragment;
                        }
                    }
                }
            } else {
                if (mFormatType == UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
                    mPhotoFragment = new PhotoSelectFragment();
                } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY) {
                    mVideoFragment = new VideoSelectFragment();
                } else {
                    mVideoFragment = new VideoSelectFragment();
                    mPhotoFragment = new PhotoSelectFragment();
                }
            }
            if (mFormatType == UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
                this.fragments.add(mPhotoFragment);
            } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY) {
                this.fragments.add(mVideoFragment);
            } else {
                this.fragments.add(mVideoFragment);
                this.fragments.add(mPhotoFragment);
            }
        }

        public int getCount() {
            if (mFormatType == UIConfiguration.ALBUM_SUPPORT_DEFAULT) {
                return 2;
            } else {
                return 1;
            }
        }

        public Fragment getItem(int paramInt) {
            return (Fragment) this.fragments.get(paramInt);
        }

    }

    private void setCheck(int index) {
        switch (index) {
            case 0:
                this.mRbVideo.setChecked(true);
                this.mRbPhoto.setChecked(false);
                mVideoFragment.resetAdapter();
                break;
            case 1:
                this.mRbVideo.setChecked(false);
                this.mRbPhoto.setChecked(true);
                mPhotoFragment.resetAdapter();
                break;
            default:
                break;
        }
    }

    private View btnNext;
    private SelectMediaPopHandler mSelectMediaPopHandler;
    private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            setCheck(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void initView() {
        mRbVideo = (RadioButton) findViewById(R.id.rbVideo);
        mRbPhoto = (RadioButton) findViewById(R.id.rbPhoto);
        mVpMedia = (ExtViewPagerNoScroll) findViewById(R.id.mediaViewPager);
        mTvImportInfo = (TextView) findViewById(R.id.import_info_text);

        mVpMedia.addOnPageChangeListener(mPageChangeListener);

        btnNext = findViewById(R.id.import_btn);
        btnNext.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onImportClick();
            }
        });

        setMediaCountText(0, 0);

        findViewById(R.id.btnBack).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.btnCamera).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mFormatType == UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
                    openPhotoCamera();
                } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY) {
                    openVideoCamera();
                } else {
                    mSelectMediaPopHandler.showPopupWindow(v);
                    mSelectMediaPopHandler.toggleBright();
                }
            }
        });
        if (!mUIConfig.enableAlbumCamera) {
            findViewById(R.id.btnCamera).setVisibility(View.INVISIBLE);
        }
        if (!hasJb2) {
            findViewById(R.id.btnCamera).setVisibility(View.INVISIBLE);
        }

        mRbVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mVpMedia.getCurrentItem() != 0) {
                    setCheck(0);
                    mVpMedia.setCurrentItem(0, true);
                }
            }
        });


        mRbPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVpMedia.getCurrentItem() != 1) {
                    setCheck(1);
                    mVpMedia.setCurrentItem(1, true);
                }
            }
        });
        if (mIsAlbumOnly) {
            //只返回路径即可
            bShowAddBtn = false;
            if (mMediaCountLimit == 1) {
                findViewById(R.id.rlAlbumBottomBar).setVisibility(View.GONE);
            } else {
                bShowAddBtn = true;
            }
        } else {
            //返回MediaObject，
            bShowAddBtn = true;
        }

        if (mFormatType == UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
            showPhotoOnly();
        } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY) {
            showVideoOnly();
        } else {
            mVpMedia.enableScroll(true);
        }
    }

    /**
     * 是否显示item中的add按钮
     */
    private boolean bShowAddBtn = false;

    private void showPhotoOnly() {
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        findViewById(R.id.rgFormat).setVisibility(View.GONE);
        tvTitle.setText(R.string.select_media_title_photo);
        tvTitle.setVisibility(View.VISIBLE);
    }

    private void showVideoOnly() {
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        findViewById(R.id.rgFormat).setVisibility(View.GONE);
        tvTitle.setText(R.string.select_media_title_video);
        tvTitle.setVisibility(View.VISIBLE);
    }

    private Runnable mRunnableCheckCamera;

    private void checkCameraPermission(Runnable runnable) {
        mRunnableCheckCamera = runnable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasReadPermission = checkSelfPermission(Manifest.permission.CAMERA);

            List<String> permissions = new ArrayList<String>();
            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (!permissions.isEmpty()) {
                requestPermissions(
                        permissions.toArray(new String[permissions.size()]),
                        REQUEST_CODE_CAMERA_PERMISSIONS);
            } else {
                if (null != mRunnableCheckCamera) {
                    mRunnableCheckCamera.run();
                    mRunnableCheckCamera = null;
                }
            }
        } else if (null != mRunnableCheckCamera) {
            mRunnableCheckCamera.run();
            mRunnableCheckCamera = null;
        }
    }


    // 照相机
    private void openPhotoCamera() {
        checkCameraPermission(new Runnable() {
            public void run() {
                Intent intent = new Intent(SelectMediaActivity.this,
                        RecorderActivity.class);
                intent.putExtra(ALBUM_FORMAT_TYPE, mFormatType);
                intent.putExtra(IntentConstants.DEFAULT_OPEN_PHOTO_MODE, true);
                intent.putExtra(IntentConstants.EDIT_CAMERA_WAY, true);
                intent.putExtra(RecorderActivity.ACTION_TO_EDIT, false);
                startActivityForResult(intent, REQUESTCODE_FOR_CAMERA);
            }
        });
    }

    // 摄像机
    private void openVideoCamera() {
        checkCameraPermission(new Runnable() {
            public void run() {
                Intent intent = new Intent(SelectMediaActivity.this,
                        RecorderActivity.class);
                intent.putExtra(ALBUM_FORMAT_TYPE, mFormatType);
                intent.putExtra(IntentConstants.DEFAULT_OPEN_PHOTO_MODE, false);
                intent.putExtra(IntentConstants.EDIT_CAMERA_WAY, true);
                intent.putExtra(RecorderActivity.ACTION_TO_EDIT, false);
                startActivityForResult(intent, REQUESTCODE_FOR_CAMERA);
            }
        });
    }

    private void cancelLoadPhotos() {
        if (null != mPhotoFragment) {
            //防止部分文件夹资源太多，正在扫描中... (强制退出扫描)
            mPhotoFragment.cancelLoadPhotos();
        }
    }

    /**
     * 响应按下导入时
     */
    private void onImportClick() {
        if (mFormatType != UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
            if (null == mVideoFragment) {
                return;
            }
        }
        if (mMediaCheckedAdapter.getItemCount() == 0) {
            SysAlertDialog.showAlertDialog(this,
                    getString(R.string.media_select),
                    getString(R.string.album_no_all),
                    getString(R.string.album_dialog_ok),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            SysAlertDialog.cancelLoadingDialog();
                            dialog.dismiss();
                        }
                    }, null, null).show();
            return;
        }
        if (isAEDetail && enableAERepeat && mMediaCheckedAdapter.getItemCount() < nPicLimit) {
            onToast(getString(R.string.select_media_at_least_limit, nPicLimit));
            return;
        }

        int len = mMediaCheckedAdapter.getItemCount();
        if (nMediaLimitMin != 0 && len < nMediaLimitMin) {
            //至少选择%d个
            onToast(getString(R.string.select_media_at_least_limit, nMediaLimitMin));
            return;
        }

        cancelLoadPhotos();
        List<MediaObject> list = mMediaCheckedAdapter.getList();
        len = list.size();
        if (mIsAlbumOnly) {
            ArrayList<String> arrMediaPath = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                arrMediaPath.add(list.get(i).getMediaPath());
            }
            Intent intent = new Intent();
            intent.putStringArrayListExtra(SdkEntry.ALBUM_RESULT, arrMediaPath);
            setResult(RESULT_OK, intent);
            finish();
            return;
        }
        if (len == 0) {
            SysAlertDialog.showAlertDialog(this,
                    getString(R.string.media_select),
                    getString(R.string.unsupport_video_o_photo),
                    getString(R.string.album_dialog_ok),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            SysAlertDialog.cancelLoadingDialog();
                            dialog.dismiss();
                        }
                    }, null, null).show();
            return;
        }
        ArrayList<MediaObject> allMedia = new ArrayList<>();
        ArrayList<Scene> arrScenes = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            MediaObject mediaObject = list.get(i);
            Scene scene = VirtualVideo.createScene();
            scene.addMedia(mediaObject);
            arrScenes.add(scene);
            allMedia.add(mediaObject);
        }
        Intent intent = new Intent();
        if (mIsAppend) {
            intent.putParcelableArrayListExtra(IntentConstants.EXTRA_MEDIA_LIST, allMedia);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            intent.putParcelableArrayListExtra(IntentConstants.INTENT_EXTRA_SCENE, arrScenes);
            UIConfiguration config = SdkEntry.getSdkService().getUIConfig();
            if (config.isEnableWizard() && !config.isHidePartEdit()) {
                intent.putParcelableArrayListExtra(IntentConstants.EXTRA_MEDIA_LIST, allMedia);
                intent.setClass(this, EditPreviewActivity.class);
            } else {
                intent.setClass(this, VideoEditActivity.class);
            }
            startActivityForResult(intent, REQUEST_CODE_EXPORT);
            overridePendingTransition(0, 0);
        }

    }

    private void setMediaCountText(int videoCount, int imageCount) {
        if (isAEDetail) {
            if (enableAERepeat) {
                //190528 只能选择图片
                mTvImportInfo.setText(getString(R.string.select_ae_enable_repeatlimit, nPicLimit, imageCount));
            } else {
                //ae 有数目限制
                if (mFormatType == UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
                    mTvImportInfo.setText(getString(R.string.select_ae_picture_limit, imageCount, nPicLimit));
                } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY) {
                    mTvImportInfo.setText(getString(R.string.select_ae_video_limit, videoCount, nVideoLimit));
                } else {
                    if (nPicLimit > 0) {
                        //190505 明确模板中没有图片时
                        mTvImportInfo.setText(getString(R.string.select_ae_media_limit, videoCount, nVideoLimit, imageCount, (nPicLimit + nVideoLimit)));
                    } else {
                        mTvImportInfo.setText(getString(R.string.select_ae_media_no_pic_limit, videoCount, nVideoLimit));
                    }
                }
            }
        } else {
            if (mFormatType == UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
                mTvImportInfo.setText(getString(R.string.import_info_photo_only, imageCount));
            } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY) {
                mTvImportInfo.setText(getString(R.string.import_info_video_only,
                        videoCount));
            } else {
                mTvImportInfo.setText(getString(R.string.import_info, videoCount, imageCount));
            }
        }
    }

    @Override
    public void onImport() {
        onImportClick();
    }

    @Override
    public void onRefreshCount() {
        int videoSize = 0;
        int photoSize = 0;
        List<MediaObject> list = mMediaCheckedAdapter.getList();
        if (null != list) {
            int len = list.size();
            if (isAEDetail && nPicLimit == 0) {
                videoSize = len;
            } else {
                for (int i = 0; i < len; i++) {
                    if (list.get(i).getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                        videoSize++;
                    }
                }
            }
            photoSize = len - videoSize;
        }
        setMediaCountText(videoSize, photoSize);
    }

    /**
     * 检测是否满足最大媒体数目
     *
     * @return true 可以继续添加媒体 ；false不可以继续添加媒体
     */
    private boolean checkCanAddMedia() {
        if (mMediaCountLimit != 0 && mMediaCheckedAdapter.getItemCount() >= mMediaCountLimit) {
            onToast(getString(R.string.once_un_exceed_num, mMediaCountLimit));
            return false;
        }
        return true;
    }

    /**
     * 确认当前媒体，是否可以被添加
     *
     * @param item
     * @return ！=null  允许添加； ==null 不允许被添加（超过数目限制）
     */
    private MediaObject aeMediaLimitCheck(ImageItem item) {
        MediaObject mediaObject = null;
        try {
            mediaObject = new MediaObject(this, item.image.getDataPath());
            if (isAEDetail && !enableAERepeat) {
                //190528enableAERepeat  不限制数目
                int count = mMediaCheckedAdapter.getItemCount();
                if (nPicLimit == 0) {
                    //190505 nPicLimit==0时，可选择图片或视频
                    if (count >= nVideoLimit) {
                        onToast(getString(R.string.select_media_limit, nVideoLimit));
                        return null;
                    }
                } else {
                    int videoCount = mMediaCheckedAdapter.getVideoCount();
                    int allLimit = nPicLimit + nVideoLimit;
                    if (count >= allLimit) {
                        onToast(getString(R.string.select_media_limit, allLimit));
                        return null;
                    }
                    if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE && videoCount >= nVideoLimit) {
                        onToast(getString(R.string.select_video_limit, nVideoLimit));
                        return null;
                    }
                }
            }
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        return mediaObject;
    }

    @Override
    public int addMediaItem(ImageItem item) {

        MediaObject mediaObject = aeMediaLimitCheck(item);
        if (null == mediaObject) {
            //超过数目限制
            return 2;
        }
        if (!checkCanAddMedia()) {
            return 2;
        }
        if (mIsAlbumOnly || getIntent().getBooleanExtra(LOTTIE_IMAGE, false)) {
            if (mMediaCountLimit == 1) {
                mMediaCheckedAdapter.add(mediaObject);
                return 1;
            }
        }
        if (bNeedResultMediaObject) {
            if (null != mediaObject) {
                if (isAEDetail) {
                    //aedetail->此时不编辑媒体
                    onAddImp(mediaObject);
                } else {
                    if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                        TrimMediaActivity.onImportTrim(SelectMediaActivity.this, mediaObject, REQUEST_IMPORT_TRIM);
                    } else {
                        Scene scene = VirtualVideo.createScene();
                        scene.addMedia(mediaObject);
                        CropRotateMirrorActivity.onImportImage(SelectMediaActivity.this, scene, REQUEST_IMPORT_ROTATE);
                    }
                }
            }
        } else {
            //不需要返回obj, 不用导入编辑
            try {
                onAdd(new MediaObject(this, item.image.getDataPath()));
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }


}
