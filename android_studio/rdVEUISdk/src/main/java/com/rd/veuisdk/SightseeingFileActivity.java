package com.rd.veuisdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.veuisdk.model.MusicItem;
import com.rd.veuisdk.model.MusicItems;
import com.rd.veuisdk.ui.CircleProgressBar;
import com.rd.veuisdk.utils.CheckSDSize;
import com.rd.veuisdk.utils.ExtScanMediaDialog;
import com.rd.veuisdk.utils.ExtScanMediaDialog.onScanMusicClickInterface;
import com.rd.veuisdk.utils.HanziToPinyin;
import com.rd.veuisdk.utils.StorageUtils;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.io.File;
import java.io.FileFilter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 扫描文件夹
 *
 * @author JIAN
 */
public class SightseeingFileActivity extends BaseActivity {
    private TextView m_tvRootPath;
    private ListView listView; // 用于显示文件的ListView组件对象
    private String rootPath = "/";
    private File sdRootPath;
    private ShowFileNameAdapter mFileNameAdapter;
    /**
     * 全选
     */
    private CheckBox mCheckAll;
    private ImageButton m_btnBackRootPath;
    private TextView tvBackRootPath;

    private MusicItems m_alLocalMusicItems = new MusicItems(this);

    /**
     * isLoading 是否正在加载 isCancelLoad 是否取消加载
     */
    private boolean isLoading, isCancelLoad;
    /**
     * 加载文件的进度
     */
    private CircleProgressBar m_cpbLoadFileProgress;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStrActivityPageName = getString(R.string.activity_label_sightseeing_file);
        setContentView(R.layout.rdveuisdk_sightseeing_file_folder);
        // 获取SD入口根目录
        sdRootPath = new File(StorageUtils.getStorageDirectory());

        initView();
    }

    private void initView() {
        // 返回
        Button mBack = (Button) this.findViewById(R.id.btn_Back);
        mBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // onBackPressed();
                finish();
            }
        });
        // 返回上层目录
        m_btnBackRootPath = (ImageButton) this
                .findViewById(R.id.btn_backRootPath);
        m_btnBackRootPath.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String path = m_tvRootPath.getText().toString();
                path = path.substring(0, path.lastIndexOf("/") + 1);
                loadingFileList(path);
            }
        });

        m_tvRootPath = (TextView) this.findViewById(R.id.tv_rootPath);
        m_tvRootPath.setText(sdRootPath.getAbsolutePath());
        mFileNameAdapter = new ShowFileNameAdapter(this);
        listView = (ListView) this.findViewById(R.id.lv_folderList);
        listView.setAdapter(mFileNameAdapter);

        mCheckAll = (CheckBox) this.findViewById(R.id.cb_selectAll);
        mCheckAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                mFileNameAdapter.setSelectAllFile(isChecked);
            }
        });

        // 开始扫描
        this.findViewById(R.id.tv_BeginScan).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        loadScanMusics();
                    }
                });
        m_cpbLoadFileProgress = (CircleProgressBar) this
                .findViewById(R.id.cpb_loadFileProgress);
        m_cpbLoadFileProgress.setVisibility(View.GONE);
        if (!CheckSDSize.ExistSDCard()) {
            SysAlertDialog.showAutoHideDialog(this, R.string.dialog_tips,
                    R.string.sd_umount, Toast.LENGTH_SHORT);
            finish();
        } else {
            loadingFileList(sdRootPath.getAbsolutePath());
        }
    }

    public void clickView(View v) {
        int id = v.getId();
        if (id == R.id.public_menu_cancel) {
            finish();
        }
    }

    private void loadScanMusics() {
        // Log.d(TAG, "选中路径的集合：" + mFileNameAdapter.getSelectFile());
        if (mFileNameAdapter.getSelectFile().size() <= 0) {
            SysAlertDialog.showAutoHideDialog(this, "",
                    getString(R.string.p_select_dir), Toast.LENGTH_SHORT);
            return;
        }
        ExtScanMediaDialog musicDialog = new ExtScanMediaDialog(this);
        musicDialog
                .setonScanMusicClickInterface(new onScanMusicClickInterface() {

                    @Override
                    public void cancel() {
                        m_alLocalMusicItems.setIsCancel(true);
                    }

                    @Override
                    public void accomplish() {
                        Intent intent = new Intent(
                                ExtScanMediaDialog.INTENT_SIGHTSEEING_UPATE);
                        intent.putExtra(
                                ExtScanMediaDialog.INTENT_SIGHTSEEING_DATA,
                                true);
                        sendBroadcast(intent);
                        finish();
                    }
                });
        musicDialog.show();
        // 注册显示音乐文件的回调接口
        m_alLocalMusicItems.setOnShowScanFileInterface(musicDialog);
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                m_alLocalMusicItems.setIsCancel(false);
                m_alLocalMusicItems.loadMusicItems(3,
                        mFileNameAdapter.getSelectFile());
            }
        });
    }

    @Override
    public void onBackPressed() {
        String path = m_tvRootPath.getText().toString();
        if (path.equals(rootPath)) {
            super.onBackPressed();
        } else {
            path = path.substring(0, path.lastIndexOf("/") + 1);
            loadingFileList(path);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != m_alLocalMusicItems) {
            m_alLocalMusicItems.setIsCancel(true);
        }
    }

    /**
     * 过滤文件
     */
    private FileFilter mFileFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return !pathname.isHidden();
        }
    };

    /**
     * 根据File[]加载相应数据的集合
     */
    private void loadingFileList(final String directory) {
        if (isLoading) {
            isCancelLoad = true;
        } else {
            isCancelLoad = false;
            m_cpbLoadFileProgress.setVisibility(View.VISIBLE);
            final ArrayList<ShowFileNameItem> showFileNames = new ArrayList<ShowFileNameItem>();
            ThreadPoolUtils.execute(new Runnable() {
                private File[] files = null; // File数组

                @Override
                public void run() {
                    isLoading = true;
                    // 判断路径是否为根目录
                    if (!directory.equals(rootPath)) {
                        final File file = new File(directory);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                m_btnBackRootPath.setEnabled(true);
                                m_tvRootPath.setText(file.getPath()); // 更新TextView组件显示的目录结构
                            }
                        });
                        files = file.listFiles(mFileFilter); // 获取该目录的所有文件及目录
                    } else {
                        // 获取根目录File对象
                        final File sdCardFile = new File(rootPath);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                m_btnBackRootPath.setEnabled(false);
                                m_tvRootPath.setText(sdCardFile.getPath()); // 设置TextView组件显示的目录结构
                            }
                        });
                        files = sdCardFile.listFiles(mFileFilter); // 获取根目录的所有文件及目录
                    }

                    if (files != null) {
                        File tStorageRootDir = new File(StorageUtils
                                .getStorageDirectory());
                        for (int i = 0; i < files.length; i++) { // 循环File数组
                            if (isCancelLoad) {
                                isLoading = false;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        m_cpbLoadFileProgress
                                                .setVisibility(View.GONE);
                                    }
                                });
                                return;
                            }
                            ShowFileNameItem fileNameItem = new ShowFileNameItem();
                            // 判断该文件是否是文件夹并且屏蔽程序自身的文件夹
                            if (files[i].isDirectory()) {
                                if (isFilterDirectoryOrFile(files[i].getName())) {
                                    continue;
                                }
                                if (!files[i].getParent().equals(
                                        tStorageRootDir.getParent())
                                        || files[i].getPath().equals(
                                        tStorageRootDir.getPath())) {
                                    fileNameItem
                                            .setImageRes(R.drawable.select_music_scan_content);
                                    fileNameItem.setMfileName(files[i]
                                            .getName());
                                    fileNameItem.setMfilePath(files[i]
                                            .getAbsolutePath());

                                    showFileNames.add(fileNameItem);
                                }
                            } else if (files[i].isFile()
                                    && isMusicAvaliable(files[i]
                                    .getAbsolutePath())) {
                                fileNameItem
                                        .setImageRes(R.drawable.select_music_list_left_icon);
                                fileNameItem.setMfileName(files[i].getName());
                                fileNameItem.setMfilePath(files[i]
                                        .getAbsolutePath());
                                showFileNames.add(fileNameItem);
                            }
                        }
                    }
                    Collections.sort(showFileNames, new MusicItemsComparator());// 排序
                    isLoading = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            m_cpbLoadFileProgress.setVisibility(View.GONE);
                            if (mFileNameAdapter != null) {
                                mCheckAll.setChecked(false);
                                mFileNameAdapter.clearUp();
                                mFileNameAdapter.addAllShowFileNames(
                                        showFileNames, true);
                            }
                        }
                    });
                }
            });
        }
    }

    /**
     * 首字母和拼音排序
     *
     * @author johnny
     */
    private static class MusicItemsComparator implements
            Comparator<ShowFileNameItem> {
        private final Collator mCollator = Collator.getInstance();

        @Override
        public int compare(ShowFileNameItem lhs, ShowFileNameItem rhs) {
            String strLHSTitle = HanziToPinyin
                    .getPinyinName(lhs.getMfileName());
            String strRHSTitle = HanziToPinyin
                    .getPinyinName(rhs.getMfileName());
            return mCollator.compare(strLHSTitle, strRHSTitle);
        }
    }

    /**
     * 过滤文件
     *
     * @param name
     * @return
     */
    private boolean isFilterDirectoryOrFile(String name) {
        return name.equals("17rd") || name.equals("proc");
    }

    private boolean isMusicAvaliable(String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            return MusicItem.checkValidExtMusicFile(path) && file.exists();
        } else {
            return false;
        }
    }

    /**
     * 显示文件名称的
     *
     * @author JIAN
     */
    class ShowFileNameAdapter extends BaseAdapter {
        private ArrayList<ShowFileNameItem> showFileNameItems;
        private Context mContext;

        public ShowFileNameAdapter(Context c) {
            this.mContext = c;
        }

        public void addAllShowFileNames(
                ArrayList<SightseeingFileActivity.ShowFileNameItem> arrayList,
                boolean notify) {
            showFileNameItems = arrayList;
            if (notify) {
                this.notifyDataSetChanged();
            }
        }

        public ArrayList<String> getSelectFile() {
            ArrayList<String> filePaths = new ArrayList<String>();
            for (ShowFileNameItem fileNameItem : showFileNameItems) {
                if (fileNameItem.getIsSelectFile()) {
                    String pathString = fileNameItem.getMfilePath();
                    filePaths.add(pathString);
                }
            }
            return filePaths;
        }

        @Override
        public int getCount() {
            return showFileNameItems != null && showFileNameItems.size() > 0 ? showFileNameItems
                    .size() : 0;
        }

        @Override
        public ShowFileNameItem getItem(int position) {
            return showFileNameItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ShowFileNameItem showFileNameItem = showFileNameItems
                    .get(position);
            if (null == convertView) {
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.folder_item, null);
            }
            final TextView m_tvFlieName = (TextView) convertView
                    .findViewById(R.id.tv_folderPathName);
            CheckBox m_cbSelectOneFolder = (CheckBox) convertView
                    .findViewById(R.id.cb_select_one_folder);
            m_tvFlieName.setCompoundDrawablesWithIntrinsicBounds(
                    showFileNameItem.getImageRes(), 0, 0, 0);
            m_tvFlieName.setText(showFileNameItem.getMfileName());
            m_cbSelectOneFolder
                    .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            m_tvFlieName.setSelected(isChecked);
                            showFileNameItem.setIsSelectFile(isChecked);
                        }
                    });

            // 如果是选择状态
            if (showFileNameItem.getIsSelectFile()) {
                m_cbSelectOneFolder.setChecked(true);
            } else {
                m_cbSelectOneFolder.setChecked(false);
            }
            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    File file = new File(showFileNameItem.getMfilePath());
                    if (file.isDirectory()) {
                        SightseeingFileActivity.this
                                .loadingFileList(showFileNameItem
                                        .getMfilePath());
                    }
                }
            });
            return convertView;
        }

        public void setSelectAllFile(boolean isSelected) {
            for (int i = 0; i < showFileNameItems.size(); i++) {
                showFileNameItems.get(i).setIsSelectFile(isSelected);
            }
            this.notifyDataSetChanged();
        }

        public void clearUp() {
            if (showFileNameItems != null) {
                showFileNameItems.clear();
                showFileNameItems = null;
            }
        }
    }

    /**
     * 游览显示文件的体类
     *
     * @author johnny
     */
    class ShowFileNameItem {
        private int mid;
        private String mfileName;
        private String mfilePath;
        private int imageRes;
        private boolean isSelectFile = false;

        public int getImageRes() {
            return imageRes;
        }

        public void setImageRes(int imageRes) {
            this.imageRes = imageRes;
        }

        public int getMid() {
            return mid;
        }

        public void setMid(int mid) {
            this.mid = mid;
        }

        public String getMfileName() {
            return mfileName;
        }

        public void setMfileName(String mfileName) {
            this.mfileName = mfileName;
        }

        public String getMfilePath() {
            return mfilePath;
        }

        public void setMfilePath(String mfilePath) {
            this.mfilePath = mfilePath;
        }

        public boolean getIsSelectFile() {
            return isSelectFile;
        }

        public void setIsSelectFile(boolean isSelectFile) {
            this.isSelectFile = isSelectFile;
        }

        @Override
        public String toString() {
            return "ShowFileNameItem [mid=" + mid + ",mfileName=" + mfileName
                    + ",mfilePath=" + mfilePath + ",imageRes=" + imageRes
                    + ",isSelectFile=" + isSelectFile + "]";

        }

    }
}
