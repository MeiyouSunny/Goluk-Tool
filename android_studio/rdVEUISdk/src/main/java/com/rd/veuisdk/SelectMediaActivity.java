package com.rd.veuisdk;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.gallery.IImage;
import com.rd.gallery.IVideo;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.VideoConfig;
import com.rd.veuisdk.fragment.IStateCallBack;
import com.rd.veuisdk.fragment.PhotoSelectFragment;
import com.rd.veuisdk.fragment.PhotoSelectFragment.IMediaSelector;
import com.rd.veuisdk.fragment.VideoSelectFragment;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.ImageItem;
import com.rd.veuisdk.ui.ExtViewPagerNoScroll;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 媒体选择页
 *
 * @author jian
 * @author abreal
 */
public class SelectMediaActivity extends BaseActivity implements IMediaSelector, IStateCallBack {
    private final String TAG = "SelectMediaActivity";
    // 请求code:读取外置存储
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS = 1;
    // 请求code:摄像头权限
    private static final int REQUEST_CODE_CAMERA_PERMISSIONS = 2;
    // 请求code:导出后返回
    private static final int REQUEST_CODE_EXPORT = 10;
    // 视频源列表

    public static boolean mIsAppend = false;

    private VideoSelectFragment mVideoFragment;
    private PhotoSelectFragment mPhotoFragment;
    private ArrayList<Integer> mAllMediaKeySelected = new ArrayList<Integer>();

    private ArrayList<ImageItem> mItemArray = new ArrayList<ImageItem>();
    private int mSelectedTotal = 0;
    private final int REQUESTCODE_FOR_CAMERA = 2;

    private boolean mAddPhoto;

    private RadioButton mRbVideo;
    private RadioButton mRbPhoto;
    private ExtViewPagerNoScroll mVpMedia;
    private TextView mTvImportInfo;

    public final static String ALBUM_FORMAT_TYPE = "album_format_type";
    public final static String ALBUM_ONLY = "album_only";

    private int mFormatType = -1;
    //是否直接返回文件路径string  (true)，还是返回mediaobject (false)
    private boolean mIsAlbumOnly = false;

    private int mMediaCountLimit;

    private UIConfiguration mUIConfig;
    private boolean hasJb2 = true; //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mIsAppend = getIntent().getBooleanExtra(EditPreviewActivity.ACTION_APPEND, false);
        mIsAlbumOnly = getIntent().getBooleanExtra(ALBUM_ONLY, false);
        mUIConfig = SdkEntry.getSdkService().getUIConfig();
        if (!mIsAppend) {
//            VideoEditActivity.orientationType = mUIConfig.orientation;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_media_layout);
        mStrActivityPageName = getString(R.string.select_medias);
        mAddPhoto = getIntent().getBooleanExtra(
                EditPreviewActivity.APPEND_IMAGE, false);
        hasJb2 = CoreUtils.hasJELLY_BEAN_MR2();
        if (mIsAlbumOnly) {
            mFormatType = getIntent().getIntExtra(ALBUM_FORMAT_TYPE, -1);
        } else {
            mFormatType = mUIConfig.albumSupportFormatType;
        }

        mMediaCountLimit = mUIConfig.mediaCountLimit;

        if (getLastCustomNonConfigurationInstance() != null) {
            mAddPhoto = (Boolean) getLastCustomNonConfigurationInstance();
        }

        initView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasReadPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            List<String> permissions = new ArrayList<String>();
            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (!permissions.isEmpty()) {
                requestPermissions(
                        permissions.toArray(new String[permissions.size()]),
                        REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS);
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
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        loadFragments();
                    } else {
                        SysAlertDialog.showAutoHideDialog(this, null,
                                getString(R.string.permission_ablum_error),
                                Toast.LENGTH_SHORT);
                        finish();
                    }
                }
            }
            break;
            case REQUEST_CODE_CAMERA_PERMISSIONS:
                if (grantResults != null && grantResults.length > 0
                        && mRunnableCheckCamera != null
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mRunnableCheckCamera.run();
                    mRunnableCheckCamera = null;
                } else {
                    SysAlertDialog.showAutoHideDialog(this, null,
                            getString(R.string.permission_camera_error),
                            Toast.LENGTH_SHORT);
                }
                break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == VideoSelectFragment.CODE_EXT_PIC) {

                Intent intent = new Intent(SelectMediaActivity.this,
                        EditPreviewActivity.class);

                intent.putExtra(IntentConstants.EXTRA_EXT_ISEXTPIC, 1);
                intent.putParcelableArrayListExtra(
                        IntentConstants.EXTRA_MEDIA_LIST,
                        data.getParcelableArrayListExtra(IntentConstants.EXTRA_MEDIA_LIST));
                intent.putExtra(IntentConstants.EXTRA_EXT_PIC_INFO, data
                        .getParcelableExtra(IntentConstants.EXTRA_EXT_PIC_INFO));
                setResult(RESULT_OK, intent);
                finish();

            } else if (requestCode == REQUESTCODE_FOR_CAMERA) {
                // data.hasExtra(SdkEntry.INTENT_KEY_PICTURE_PATH);getStringExtra(SdkEntry.INTENT_KEY_PICTURE_PATH);
                if (data.hasExtra(SdkEntry.INTENT_KEY_PICTURE_PATH)) {// RecorderActivity内部已经执行了插入相册的操作，这里只要刷新一下就好
                    if (mFormatType == UIConfiguration.ALBUM_SUPPORT_DEFAULT) {
                        setchecked(1);
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
                            insertToGalleryr(path, Utils.s2ms(duration),
                                    vcMediaInfo.getVideoWidth(),
                                    vcMediaInfo.getVideoHeight());
                        } catch (Exception ex) {
                        } finally {
                            if (mFormatType == UIConfiguration.ALBUM_SUPPORT_DEFAULT) {
                                setchecked(0);
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

    private String getPhotopath() {
        // 照片全路径
        String fileName = "";
        // 文件夹路径
        String pathUrl = Environment.getExternalStorageDirectory()
                + "/DCIM/Camera/";
        String imageName = "noname.jpg";
        File file = new File(pathUrl);
        file.mkdirs();// 创建文件夹
        fileName = pathUrl + imageName;
        return fileName;
    }

    private void insertToGalleryr(String path, int duration, int width,
                                  int height) {
        ContentValues videoValues = new ContentValues();
        videoValues.put(Video.Media.TITLE, getString(R.string.undefine));
        videoValues.put(Video.Media.MIME_TYPE, "video/mp4");
        videoValues.put(Video.Media.DATA, path);
        videoValues.put(Video.Media.ARTIST, getString(R.string.app_name));
        videoValues.put(Video.Media.DATE_TAKEN,
                String.valueOf(System.currentTimeMillis()));
        videoValues.put(Video.Media.DESCRIPTION, getString(R.string.app_name));
        videoValues.put(Video.Media.DURATION, duration);
        videoValues.put(Video.Media.WIDTH, width);
        videoValues.put(Video.Media.HEIGHT, height);
        getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoValues);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mItemArray.clear();
        mSelectedTotal = 0;
    }

    @Override
    public void finish() {
        super.finish();
        if (null != mPhotoFragment) {
            mPhotoFragment.onBackPressed();
        }
    }

    @Override
    public boolean isHideText() {
        return SdkEntry.getSdkService().getUIConfig().isHideText();
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

    private void setchecked(int paramInt) {
        switch (paramInt) {
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

    private void initView() {
        mRbVideo = (RadioButton) findViewById(R.id.rbVideo);
        mRbPhoto = (RadioButton) findViewById(R.id.rbPhoto);
        mVpMedia = (ExtViewPagerNoScroll) findViewById(R.id.mediaViewPager);
        mTvImportInfo = (TextView) findViewById(R.id.import_info_text);

        mVpMedia.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                setchecked(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        findViewById(R.id.import_btn).setOnClickListener(new OnClickListener() {

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
//                if (mIsAlbumOnly) {
//                    setResult(SdkEntry.RESULT_ALBUM_TO_CAMERA);
//                    finish();
//                    return;
//                }
                if (mFormatType == UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
                    openPhotoCamera();
                } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY) {
                    openVideoCamera();
                } else {
                    showPopupWindow(v);
                }
            }
        });
        if (mIsAlbumOnly && !mUIConfig.enableAlbumCamera) {
            findViewById(R.id.btnCamera).setVisibility(View.INVISIBLE);
        }
        if (!hasJb2) {
            findViewById(R.id.btnCamera).setVisibility(View.INVISIBLE);
        }

        mRbVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mVpMedia.getCurrentItem() != 0) {
                    setchecked(0);
                    mVpMedia.setCurrentItem(0, true);
                }
            }
        });


        mRbPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVpMedia.getCurrentItem() != 1) {
                    setchecked(1);
                    mVpMedia.setCurrentItem(1, true);
                }
            }
        });

        if (mMediaCountLimit == 1 && mIsAlbumOnly) {
            findViewById(R.id.rlAlbumBottomBar).setVisibility(View.GONE);
        }

        if (mFormatType == UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
            showPhotoOnly();
        } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY) {
            showVideoOnly();
        } else {
            mVpMedia.enableScroll(true);
        }
    }

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

    /**
     * 弹出拍照录像选择窗口
     *
     * @param view
     * @return
     */
    private void showPopupWindow(View view) {
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.camera_popup, null);

        final PopupWindow popupWindow = new PopupWindow(contentView,
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);

        TextView tvPhoto = (TextView) contentView.findViewById(R.id.tvPhoto);
        TextView tvVideo = (TextView) contentView.findViewById(R.id.tvVideo);

        tvPhoto.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                openPhotoCamera();
            }
        });

        tvVideo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                openVideoCamera();
            }
        });

        popupWindow.setTouchable(true);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        ColorDrawable colorDrawable = new ColorDrawable(getResources()
                .getColor(R.color.white));
        popupWindow.setBackgroundDrawable(colorDrawable);
        popupWindow.showAsDropDown(view, -75, 10);
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

    /**
     * 响应按下导入时
     */
    protected void onImportClick() {
        if (mFormatType != UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
            if (null == mVideoFragment) {
                return;
            }
        }
        if (mFormatType == UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
            if (mPhotoFragment.getMedia().size() == 0) {
                SysAlertDialog.showAlertDialog(this,
                        getString(R.string.album_no_photo),
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
        } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY) {
            if (mVideoFragment.getMedia().size() == 0) {
                SysAlertDialog.showAlertDialog(this,
                        getString(R.string.media_select),
                        getString(R.string.album_no_video),
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
        } else {
            if (mVideoFragment.getMedia().size() == 0
                    && mPhotoFragment.getMedia().size() == 0) {
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
        }
        if (mIsAlbumOnly) {
            ArrayList<String> arrMediaPath = new ArrayList<String>();
            for (Integer nMediaKey : mAllMediaKeySelected) {
                IImage item = null;
                if (mVideoFragment != null) {
                    item = mVideoFragment.getMedia().get(nMediaKey.intValue());
                }
                String path;
                if (item != null && item instanceof IVideo) {
                    path = item.getDataPath();
                } else {
                    if (mPhotoFragment != null) {
                        item = mPhotoFragment.getMedia().get(
                                nMediaKey.intValue());
                    }
                    if (item != null) {
                        path = item.getDataPath();
                    } else {
                        continue;
                    }
                }
                arrMediaPath.add(path);
            }
            Intent intent = new Intent();
            intent.putStringArrayListExtra(SdkEntry.ALBUM_RESULT,
                    arrMediaPath);
            setResult(RESULT_OK, intent);
            finish();
            return;
        }
        ThreadPoolUtils.executeEx(new ThreadPoolUtils.ThreadPoolRunnable() {
            Intent intent;
            ArrayList<MediaObject> allMedia = new ArrayList<MediaObject>();
            ArrayList<Scene> arrScenes = new ArrayList<Scene>();

            @Override
            public void onStart() {
                SysAlertDialog
                        .showLoadingDialog(SelectMediaActivity.this, null);
            }

            @Override
            public void onBackground() {
                for (Integer nMediaKey : mAllMediaKeySelected) {
                    IImage item = null;
                    item = mVideoFragment.getMedia().get(
                            nMediaKey.intValue());

                    MediaObject io = null;
                    if (item != null && item instanceof IVideo) {
                        try {
                            io = VirtualVideo.createScene().addMedia(item
                                    .getDataPath());
                        } catch (InvalidArgumentException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (mPhotoFragment != null) {
                            item = mPhotoFragment.getMedia().get(
                                    nMediaKey.intValue());
                        }
                        if (item != null) {
                            try {
                                io = VirtualVideo.createScene().addMedia(item
                                        .getDataPath());
                            } catch (InvalidArgumentException e) {
                                e.printStackTrace();
                            }
                        } else {
                            continue;
                        }
                    }
                    if (null != io) {
                        allMedia.add(io);
                        Scene scene = new Scene();
                        scene.addMedia(io);
                        arrScenes.add(scene);
                    }
                }
            }

            @Override
            public void onEnd() {
                SysAlertDialog.cancelLoadingDialog();
                if (allMedia.size() == 0) {
                    SysAlertDialog.showAlertDialog(SelectMediaActivity.this,
                            getString(R.string.media_select),
                            getString(R.string.unsupport_video_o_photo),
                            getString(R.string.iknow),
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

                if (getIntent().getBooleanExtra(IntentConstants.EDIT_TWO_WAY,
                        false)) {
                    UIConfiguration config = SdkEntry.getSdkService().getUIConfig();

                    if (config.isEnableWizard()
                            && !config.isHidePartEdit()) {
                        intent = new Intent(SelectMediaActivity.this,
                                EditPreviewActivity.class);
                    } else {
                        intent = new Intent(SelectMediaActivity.this,
                                VideoEditActivity.class);
                    }
                } else {
                    intent = new Intent(SelectMediaActivity.this,
                            EditPreviewActivity.class);
                }

                intent.putParcelableArrayListExtra(
                        IntentConstants.EXTRA_MEDIA_LIST, allMedia);
                if (mIsAppend) {
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    intent.putParcelableArrayListExtra(
                            IntentConstants.INTENT_EXTRA_SCENE, arrScenes);
                    if (getIntent().getBooleanExtra(
                            IntentConstants.EDIT_TWO_WAY, false)) {
                        intent.putExtra(IntentConstants.EDIT_TWO_WAY, true);
                        startActivityForResult(intent, REQUEST_CODE_EXPORT);
                    } else {
                        startActivity(intent);
                        finish();
                    }
                }

            }
        });
    }

    private void setMediaCountText(int videoCount, int imageCount) {
        if (mFormatType == -1) {
            if (mFormatType == UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
                mTvImportInfo.setText(getString(R.string.import_info_photo_only,
                        imageCount));
            } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY) {
                mTvImportInfo.setText(getString(R.string.import_info_video_only,
                        videoCount));
            } else {
                mTvImportInfo.setText(getString(R.string.import_info,
                        videoCount, imageCount));
            }
        } else {
            if (mFormatType == UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
                mTvImportInfo.setText(getString(R.string.import_info_photo_only,
                        imageCount));
            } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY) {
                mTvImportInfo.setText(getString(R.string.import_info_video_only,
                        videoCount));
            } else {
                mTvImportInfo.setText(getString(R.string.import_info,
                        videoCount, imageCount));
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
        if (mFormatType == UIConfiguration.ALBUM_SUPPORT_IMAGE_ONLY) {
            photoSize = mPhotoFragment.getMedia().size();
        } else if (mFormatType == UIConfiguration.ALBUM_SUPPORT_VIDEO_ONLY) {
            videoSize = mVideoFragment.getMedia().size();
        } else {
            photoSize = mPhotoFragment.getMedia().size();
            videoSize = mVideoFragment.getMedia().size();
        }
        setMediaCountText(videoSize, photoSize);
    }

    @Override
    public int addMediaItem(ImageItem item) {

        if (mIsAlbumOnly) {
            if (mMediaCountLimit == 1) {
                mAllMediaKeySelected.add(item.imageItemKey);
                item.position = mSelectedTotal;
                mSelectedTotal++;
                mItemArray.add(item);
                return 1;
            }
            if (mMediaCountLimit != 0
                    && mSelectedTotal >= mMediaCountLimit) {
                SysAlertDialog.showAutoHideDialog(this, null,
                        getString(R.string.once_un_exceed_num, mMediaCountLimit),
                        Toast.LENGTH_SHORT);
                return 2;
            }
        }


        mAllMediaKeySelected.add(item.imageItemKey);
        item.position = mSelectedTotal;
        mSelectedTotal++;
        mItemArray.add(item);
        return 0;
    }

    @Override
    public void removeMediaItem(ImageItem item) {
        mAllMediaKeySelected.remove((Integer) item.imageItemKey);
        mSelectedTotal--;
        mItemArray.remove(item.position);
    }

    @Override
    public void resetPosition() {
        for (int n = 0; n < mItemArray.size(); n++) {
            mItemArray.get(n).position = n;
        }
    }

    @Override
    public void replaceItem(ImageItem item) {
        for (int n = 0; n < mItemArray.size(); n++) {
            if (mItemArray.get(n).imageItemKey == item.imageItemKey) {
                item.position = n;
                mItemArray.set(n, item);
            }
        }
    }
}
