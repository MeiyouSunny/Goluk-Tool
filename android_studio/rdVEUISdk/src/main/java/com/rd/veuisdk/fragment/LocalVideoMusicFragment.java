package com.rd.veuisdk.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.Music;
import com.rd.vecore.VirtualVideo;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SightseeingFileActivity;
import com.rd.veuisdk.adapter.MyMusicAdapter;
import com.rd.veuisdk.adapter.MyMusicAdapter.TreeNode;
import com.rd.veuisdk.model.MyMusicInfo;
import com.rd.veuisdk.model.OnShowScanFileInterface;
import com.rd.veuisdk.model.WebMusicInfo;
import com.rd.veuisdk.utils.CheckSDSize;
import com.rd.veuisdk.utils.ExtScanMediaDialog;
import com.rd.veuisdk.utils.StorageUtils;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * 本地音乐
 */
public class LocalVideoMusicFragment extends BaseV4Fragment {

    private static final String TAG = "LocalVideoMusicFragment";
    private View mRoot;
    private Button mScanSd;//扫描视频
    private ListView mListView;//列表

    private MyMusicAdapter mMusicAdapter;
    private ArrayList<TreeNode> mTreeNodeList = new ArrayList<>();
    /**
     * 新增
     */
    private ArrayList<TreeNode> mNewTreeNodeList = new ArrayList<>();
    private int mSectionPosition = 0, mListPosition = 0;
    /**
     * 刷新
     */
    private static final int NOTIFYDATA = 500;
    private Handler mHandler;
    /**
     * 显示扫描文件的路径的接口
     */
    private OnShowScanFileInterface mScanFileInterface;
    /**
     * 弹框
     */
    private ExtScanMediaDialog mMusicDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.rdveuisdk_localmusic_layout, container, false);
        initView();
        init();
        return mRoot;
    }

    private void init() {
        //设置ListView
        mMusicAdapter = new MyMusicAdapter(getContext());
        mListView.setAdapter(mMusicAdapter);

        initHandler();

        mListener = new ScanCustomizeListener() {
            @Override
            public void begin(ArrayList<String> scanDirectory) {
                scanCustomize(scanDirectory);
            }

            @Override
            public void cancelScan() {
                mIsCancel = true;
            }

            @Override
            public void complete() {

            }

            @Override
            public void setOnShowScanFileInterface(OnShowScanFileInterface fileInterface) {
                mScanFileInterface = fileInterface;
            }
        };

        //扫描视频
        scanSd();
    }

    private void initView() {
        mScanSd = mRoot.findViewById(R.id.scan_sd);
        mListView = mRoot.findViewById(R.id.expandable_mymusic);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mMusicAdapter.onItemClick(view, position, false);
            }
        });

        mScanSd.setText(getString(R.string.scan_video));
        mScanSd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //扫描视频
                showScanType();
            }
        });
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case NOTIFYDATA:
                        if (mNewTreeNodeList.size() > 0 && !mIsCancel) {
                            ArrayList<TreeNode> temp = new ArrayList<>();
                            for (int j = 0; j < mNewTreeNodeList.size(); j++) {
                                int i = 0;
                                for (; i < mTreeNodeList.size(); i++) {
                                    if (mNewTreeNodeList.get(j).childs.getmInfo().getLocalPath().equals(mTreeNodeList.get(i).childs.getmInfo().getLocalPath())) {
                                        break;
                                    }
                                }
                                if (i == mTreeNodeList.size()) {
                                    temp.add(mNewTreeNodeList.get(j));
                                }
                            }
                            if (!mIsCancel && temp.size() > 0) {
                                mTreeNodeList.addAll(temp);
                            }
                            if (mScanFileInterface != null) {
                                mScanFileInterface.scanNewFileNum(mIsCancel ? 0 : temp.size());
                            }
                            if (mMusicDialog != null) {
                                mMusicDialog.scanNewFileNum(mIsCancel ? 0 : temp.size());
                            }
                            if (!mIsCancel && temp.size() > 0) {
                                mMusicAdapter.setCanAutoPlay(false);
                                mMusicAdapter.replace(mTreeNodeList, "");
                                mMusicAdapter.setListView(mListView);
                            }
                            temp.clear();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * 显示扫描样式
     */
    public void showScanType() {
        if (!CheckSDSize.ExistSDCard()) {
            SysAlertDialog.showAutoHideDialog(getActivity(), "",
                    getString(R.string.sd_umount), Toast.LENGTH_LONG);
            return;
        }
        mIsCancel = false;
        SysAlertDialog.showListviewAlertMenu(getActivity(), "", getResources()
                        .getStringArray(R.array.scan_sd_menu),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            mMusicDialog = new ExtScanMediaDialog(getActivity());
                            mMusicDialog.setVideo();
                            mMusicDialog.setonScanMusicClickInterface(new ExtScanMediaDialog.onScanMusicClickInterface() {

                                @Override
                                public void cancel() {
                                    mIsCancel = true;
                                }

                                @Override
                                public void accomplish() {
                                    //完成

                                }
                            });
                            mMusicDialog.show();
                            ThreadPoolUtils.executeEx(new Runnable() {
                                public void run() {
                                    // 快速扫描本地文件夹
                                    scanSd();
                                }
                            });
                        } else {
                            Intent intent = new Intent(getActivity(), com.rd.veuisdk.SightseeingFileActivity.class);
                            SightseeingFileActivity.setScanListener(mListener);
                            startActivity(intent);
                        }
                    }
                });
    }

    //扫描视频 快速扫描
    private void scanSd() {
        if (!CheckSDSize.ExistSDCard()) {
            SysAlertDialog.showAutoHideDialog(getActivity(), "",
                    getString(R.string.sd_umount), Toast.LENGTH_LONG);
            return;
        }
        mNewTreeNodeList.clear();
        Cursor cursor = mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        TreeNode treeNode;
        MyMusicInfo minfoInfo;
        if (cursor != null) {
            while (cursor.moveToNext() && !mIsCancel) {
//                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
//                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));  //视频文件的标题内容
//                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));//专辑
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));//艺术家
                String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));//文件显示名字
//                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));  //地址
                long duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
//                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));

                Music music = VirtualVideo.createMusic(path);
                WebMusicInfo webMusicInfo = new WebMusicInfo();
                webMusicInfo.setLocalPath(music.getMusicPath());
                webMusicInfo.checkExists();

                //如果文件不存在 不添加
                if (!webMusicInfo.exists()) {
                    continue;
                }
                if (TextUtils.isEmpty(displayName)) {
                    displayName = new File(path).getName();
                }
                if (TextUtils.isEmpty(displayName)) {
                    displayName = "未命名";
                }
                //判断名字超过20 就中间不显示
                if (displayName.length() > 25) {
                    displayName = displayName.substring(0, 12) + "..." + displayName.substring(displayName.length() - 10);
                }
                webMusicInfo.setMusicName(displayName);
                webMusicInfo.setArtName(artist);
                webMusicInfo.setDuration(duration);

                minfoInfo = new MyMusicInfo();
                minfoInfo.setmInfo(webMusicInfo);

                treeNode = new TreeNode();
                treeNode.childs = minfoInfo;
                treeNode.tag = 2;
                treeNode.type = TreeNode.ITEM;
                treeNode.sectionPosition = mSectionPosition;
                treeNode.listPosition = mListPosition++;
                if (null != mMusicAdapter) {
                    mMusicAdapter.addTreeNode(treeNode);
                }
                mNewTreeNodeList.add(treeNode);
            }
            cursor.close();
        }
        mHandler.sendEmptyMessage(NOTIFYDATA);
    }

    /**
     * 取消
     */
    private boolean mIsCancel = false;
    private static final String[] VIDEO_FORMAT = new String[]{"mp4", "avi", "mov",};
    /**
     * 最近一次扫描路径
     */
    private String lastScanPath;

    //自定义目录扫描
    private void scanCustomize(ArrayList<String> scanDirectory) {
        mNewTreeNodeList.clear();
        for (int i = 0; i < scanDirectory.size(); i++) {
            if (mIsCancel) {
                break;
            }
            try {
                File file = new File(scanDirectory.get(i))
                        .getCanonicalFile();
                if (isFilterDirectoryOrFile(file.getAbsolutePath())
                        || file.getAbsolutePath().equals(lastScanPath)) {
                    continue;
                }
                lastScanPath = file.getAbsolutePath();
                scanFile(file);
                mHandler.sendEmptyMessage(NOTIFYDATA);
            } catch (IOException e) {

            }
        }
    }

    /**
     * 扫描视频文件
     *
     * @param file
     */
    private void scanFile(File file) {
        if (!file.exists() || mIsCancel) {
            return;
        }
        // 判断是否是一个标准的文件,
        if (file.isFile() && !file.isHidden()) {
            String path = null;
            try {
                path = file.getCanonicalPath().toLowerCase(Locale.getDefault());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mScanFileInterface != null) {
                mScanFileInterface.scanFilePath(path);
            }
            if (isVideoAvaliable(path)) {
                Music music = VirtualVideo.createMusic(file.getPath());
                MyMusicInfo minfoInfo = new MyMusicInfo();
                WebMusicInfo webMusicInfo = new WebMusicInfo();
                webMusicInfo.setLocalPath(music.getMusicPath());
                //判断名字超过20 就中间不显示
                String name = file.getName();
                if (name.length() > 25) {
                    name = name.substring(0, 12) + "..." + name.substring(name.length() - 10);
                }
                webMusicInfo.setMusicName(name);
                webMusicInfo.setArtName("");
                webMusicInfo.setDuration((long) music.getDuration());
                minfoInfo.setmInfo(webMusicInfo);
                //如果文件不存在 不添加
                if (!webMusicInfo.exists()) {
                    return;
                }
                TreeNode treeNode = new TreeNode();
                treeNode.childs = minfoInfo;
                treeNode.tag = 2;
                treeNode.type = TreeNode.ITEM;
                treeNode.sectionPosition = mSectionPosition;
                treeNode.listPosition = mListPosition++;
                mMusicAdapter.addTreeNode(treeNode);
                mNewTreeNodeList.add(treeNode);
            }
        } else if (file.isDirectory()) {// 文件是否是一个目录
            // 获取路径名中的所有目录中的文件，返回一个文件数组
            File[] files = file.listFiles();
            if (files != null) {
                // 先排除当前是否存在".nomedia"的情况，在
                for (int i = 0; i < files.length; i++) {
                    if (mIsCancel) {
                        return;
                    }
                    if (isFilterDirectoryOrFile(files[i].getAbsolutePath().toLowerCase())) {
                        continue;
                    }
                    try {
                        File file2 = new File(files[i].getCanonicalPath());
                        if (file2.getAbsolutePath().equals(lastScanPath)) {
                            continue;
                        }
                        lastScanPath = file2.getAbsolutePath();
                        scanFile(file2);
                    } catch (IOException e) {

                    }
                }
            }
        }
    }

    /**
     * 视频文件是否可用
     *
     * @param path
     * @return
     */
    private boolean isVideoAvaliable(String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            return checkValidExtVideoFile(path) && file.exists();
        } else {
            return false;
        }
    }

    /**
     * 检查是否为可支持的视频文件
     *
     * @param strPath
     * @return
     */
    public boolean checkValidExtVideoFile(String strPath) {
        if (!TextUtils.isEmpty(strPath)) {
            boolean re = false;
            int len = VIDEO_FORMAT.length;
            for (int i = 0; i < len; i++) {

                if (strPath.toLowerCase().endsWith(VIDEO_FORMAT[i].toLowerCase())) {
                    re = true;
                    break;
                }
            }
            return re;
        }
        return false;
    }

    /**
     * 过滤文件
     *
     * @param name
     * @return
     */
    private boolean isFilterDirectoryOrFile(String name) {
        return name.indexOf(StorageUtils.getStorageDirectory() + "data/") != -1
                || name.indexOf("/sys/") != -1;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMusicAdapter != null) {
            mMusicAdapter.onStart();
        }
    }

    @Override
    public void onPause() {
        if (null != mMusicAdapter)
            mMusicAdapter.onPause();
        super.onPause();

    }

    @Override
    public void onStop() {
        mMusicAdapter.onStop();
        super.onStop();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //强制取消扫描
        mIsCancel = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMusicAdapter.onDestroy();
        mTreeNodeList.clear();
        mTreeNodeList = null;
        mNewTreeNodeList.clear();
        mNewTreeNodeList = null;
        mHandler.removeCallbacksAndMessages(null);
    }

    private ScanCustomizeListener mListener;

    public interface ScanCustomizeListener {
        /**
         * 扫描文件夹
         *
         * @param scanDirectory
         */
        void begin(ArrayList<String> scanDirectory);

        /**
         * 取消
         */
        void cancelScan();

        /**
         * 完成
         */
        void complete();

        /**
         * 注册显示扫描文件发路径的接口
         */
        void setOnShowScanFileInterface(OnShowScanFileInterface fileInterface);

    }

}
