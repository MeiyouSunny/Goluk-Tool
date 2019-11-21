package com.rd.veuisdk;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.utils.InputUtls;
import com.rd.vecore.models.AEFragmentInfo;
import com.rd.veuisdk.TTFHandler.ITTFHandlerListener;
import com.rd.veuisdk.adapter.TTFAdapter;
import com.rd.veuisdk.ae.model.AETextLayerInfo;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.AETextMediaInfo;
import com.rd.veuisdk.utils.AEText2Bitmap;
import com.rd.veuisdk.utils.BitmapUtils;
import com.rd.veuisdk.utils.PathUtils;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * AE编辑ReplaceableText (写字板)
 */
public class AETextActivity extends BaseActivity {
    private GridView mGvTTF;
    private TTFHandler mTTFHandler;
    private IntentFilter mIntentFilter;


    private static final String PARAM_TEXT = "param_text";
    private static final String PARAM_TEXT_MEDIA_LIST = "param_text_media_list";
    private static final String PARAM_TEXT_MEDIA_USER_TEXT_LIST = "param_text_media_user_text_list";
    private static final String PARAM_TTF_INDEX = "param_ttf_index";
    private static final String PARAM_TTF_PATH = "param_ttf_path";
    private static final String PARAM_AETEXT_LAYER = "param_aetext_layer";
    private static final String PARAM_AE_RESULT = "param_ae_result";
    private static final String PARAM_AE_RESULT_LIST = "param_ae_result_list";
    private static final String PARAM_AE_TEXT_CONTENT = "param_ae_text_content";
    private int maxNum = 20, maxLine = 1;

    /**
     * 每次编辑单个media对应的文本
     *
     * @param context
     * @param aeTextLayerInfo
     * @param text
     * @param ttfIndex
     * @param ttfPath
     * @param requestCode
     */
    static void onAEText(Context context, AETextLayerInfo aeTextLayerInfo, String text, int ttfIndex, String ttfPath, int requestCode) {
        Intent intent = new Intent(context, AETextActivity.class);
        intent.putExtra(PARAM_TEXT, !TextUtils.isEmpty(text) ? text : aeTextLayerInfo.getTextContent());
        intent.putExtra(PARAM_TTF_INDEX, ttfIndex);
        intent.putExtra(PARAM_TTF_PATH, ttfPath);
        intent.putExtra(PARAM_AETEXT_LAYER, aeTextLayerInfo);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    /**
     * 每次编辑多个Media 对应的文本 （例如：Boxed   ）
     *
     * @param context
     * @param list
     * @param requestCode
     */
    static void onAEText(Context context, ArrayList<AETextMediaInfo> list, ArrayList<String> mUserTextList, String content, int requestCode) {
        Intent intent = new Intent(context, AETextActivity.class);
        intent.putParcelableArrayListExtra(PARAM_TEXT_MEDIA_LIST, list);
        intent.putStringArrayListExtra(PARAM_TEXT_MEDIA_USER_TEXT_LIST, mUserTextList);
        intent.putExtra(PARAM_AE_TEXT_CONTENT, content);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    /**
     * 每次编辑多个Media 对应的文本 （例如：Boxed   ）
     *
     * @param context
     * @param list
     * @param requestCode
     */
    static void onAEText(Context context, ArrayList<AETextMediaInfo> list, ArrayList<String> mUserTextList, int requestCode) {
        onAEText(context, list, mUserTextList, null, requestCode);
    }

    private ArrayList<AETextMediaInfo> mQuikTextMediaInfos;
    private boolean isBoxed = false;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_edit_ae_text);
        initView();
        mIntentFilter = new IntentFilter(TTFAdapter.ACTION_TTF);
        registerReceiver(mReceiver, mIntentFilter);
        root = findViewById(android.R.id.content);
        mViewGroup = $(R.id.mFrameGroup);
        mQuikTextMediaInfos = getIntent().getParcelableArrayListExtra(PARAM_TEXT_MEDIA_LIST);
        String content = getIntent().getStringExtra(PARAM_AE_TEXT_CONTENT);
        final String temp = !TextUtils.isEmpty(content) ? content.trim() : "";
        mEditText.setText(temp);
        mEditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                mEditText.setSelection(temp.length());
            }
        }, 300);
        String text = null;
        if (null == mQuikTextMediaInfos) {
            isBoxed = false;
            AETextLayerInfo info = getIntent().getParcelableExtra(PARAM_AETEXT_LAYER);
            if (null == info) {
                finish();
                return;
            }
            maxNum = info.getMaxNum();
            maxLine = info.getLineNum();
            mTTFPosition = getIntent().getIntExtra(PARAM_TTF_INDEX, 0);
            String ttfPath = getIntent().getStringExtra(PARAM_TTF_PATH);
            text = getIntent().getStringExtra(PARAM_TEXT);
            try {
                Typeface typeface = Typeface.createFromFile(ttfPath);
                mEditText.setTypeface(typeface);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            isBoxed = true;
            int len = mQuikTextMediaInfos.size();
            if (len == 0) {
                finish();
                return;
            }

            ArrayList<String> mUserTextList = getIntent().getStringArrayListExtra(PARAM_TEXT_MEDIA_USER_TEXT_LIST);
            StringBuffer sb = new StringBuffer();
            int tmpMaxNum = 0;
            int tmpMaxLine = 0;
            AETextMediaInfo item = null;
            for (int i = 0; i < len; i++) {
                item = mQuikTextMediaInfos.get(i);

                String tmp = null;
                if (i < mUserTextList.size()) {
                    tmp = mUserTextList.get(i);
                }
                String str = TextUtils.isEmpty(tmp) ? item.getAETextLayerInfo().getTextContent() : tmp;
                tmpMaxNum += item.getAETextLayerInfo().getMaxNum();
                if (i == (len - 1)) {
                    sb.append(str);
                } else {
                    sb.append(str + "\n");
                    //处理换行符
                    tmpMaxNum += 1;
                }
                tmpMaxLine++;

            }

            maxNum = tmpMaxNum;
            maxLine = tmpMaxLine;
            text = sb.toString();
            try {
                Typeface typeface = Typeface.createFromFile(item.getTtf());
                mEditText.setTypeface(typeface);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (!TextUtils.isEmpty(text)) {
            tvAeInfo.setText(getString(R.string.ae_text_info, text.length(), maxNum, maxLine));
            String str = text.trim();
//            mEditText.setText(str);
//            mEditText.setSelection(str.length());
        } else {
            tvAeInfo.setText(getString(R.string.ae_text_info, 0, maxNum, maxLine));
        }
        mEditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputUtls.showInput(mEditText);
            }
        }, 300);
//        mEditText.setMaxLines(maxLine);
//        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxNum), new NewlineFilter()});


    }

    public class NewlineFilter implements InputFilter {

        /**
         * @param source 输入的文字
         * @param start  输入-0，删除-0
         * @param end    输入-文字的长度，删除-0
         * @param dest   原先显示的内容
         * @param dstart 输入-原光标位置，删除-光标删除结束位置
         * @param dend   输入-原光标位置，删除-光标删除开始位置
         * @return null表示原始输入，""表示不接受输入，其他字符串表示变化值
         */
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source.toString().contains("\n")) {
                String str = dest.toString();
                int count = str.length();
                if (!TextUtils.isEmpty(str)) {
                    String[] arr = str.split("\n");
                    String noLine = str.trim();
//                    Log.e(TAG, "filter: " + Arrays.toString(arr) + ">>" + str + "<< " + str.length() + "   >>:" + str.indexOf("\n") + " last:" + str.lastIndexOf("\n") + " ??:" + noLine.length() + " <" + count);
                    if (null != arr && (arr.length >= maxLine || noLine.length() < count)) {
                        return "";
                    }
                }
            }
            return source;
        }
    }


    private IViewTreeLayoutListener mOnGlobalLayoutListener;


    @Override
    protected void onResume() {
        mOnGlobalLayoutListener = new IViewTreeLayoutListener(root, mViewGroup);
        root.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
        super.onResume();
        mEditText.addTextChangedListener(mTextWatcher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        root.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
        mEditText.removeTextChangedListener(mTextWatcher);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            tvAeInfo.setText(getString(R.string.ae_text_info, text.length(), maxNum, maxLine));

        }
    };

    private ViewGroup mViewGroup;
    private View root;

    private class IViewTreeLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
        private View root;
        private View scrollToView;
        private int[] location = new int[2];

        public IViewTreeLayoutListener(View root, View scrollToView) {
            this.root = root;
            this.scrollToView = scrollToView;
        }

        @Override
        public void onGlobalLayout() {
            Rect rectVisible = new Rect();
            root.getWindowVisibleDisplayFrame(rectVisible);
            int rHeight = root.getRootView().getHeight();
            int rootInvisibleHeight = rHeight - rectVisible.height();
//            Log.e(TAG, "onGlobalLayout: " + rectVisible + ">>>" + rootInvisibleHeight + ">>" + rHeight);
            if (rootInvisibleHeight > 100) {
                // 获取scrollToView在窗体的坐标
                scrollToView.getLocationInWindow(location);
                int tY = rHeight - rootInvisibleHeight - scrollToView.getHeight();
//                Log.e(TAG, "onGlobalLayout:   ty:" + tY + "  " + Arrays.toString(location));
                if (location[1] > tY) { // 输入法打开对于目标区域有遮挡
                    scrollToView.setY(tY);
                }

            } else {
                // 键盘隐藏
                int re = rHeight - scrollToView.getHeight();
//                Log.e(TAG, "onGlobalLayout: yincang  " + re);
                scrollToView.setY(re);
            }

        }
    }

    ;


    /**
     * 点击输入法开关
     */

    public void clickView(View v) {
        int id = v.getId();
        if (id == R.id.public_menu_sure) {
            onSure();
        } else if (id == R.id.public_menu_cancel) {
            onBackPressed();
        }
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            mTtfPath = intent.getStringExtra(TTFAdapter.TTF_ITEM);
            mTTFPosition = intent.getIntExtra(TTFAdapter.TTF_ITEM_POSITION, 0);
            try {
                Typeface typeface = Typeface.createFromFile(mTtfPath);
                mEditText.setTypeface(typeface);
            } catch (Exception e) {
                e.printStackTrace();
            }
            onTTFItemClick(mTtfPath, mTTFPosition);

        }

    };


    private OnClickListener mOnLeftListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };
    private EditText mEditText;
    private TextView tvAeInfo;
    private boolean isTTFing = false;

    private void initView() {
        mEditText = $(R.id.etEditPic);
        mGvTTF = $(R.id.gvTTF);
        tvAeInfo = $(R.id.tv_ae_text_info);
        ExtButton left = $(R.id.btnLeft);
        left.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.public_menu_cancel, 0, 0, 0);
        left.setOnClickListener(mOnLeftListener);

        ExtButton right = $(R.id.btnRight);
        right.setText("");
        right.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                R.drawable.public_menu_sure, 0);
        right.setVisibility(View.VISIBLE);
        right.setOnClickListener(mOnRightListener);

        TextView title = $(R.id.tvTitle);
        title.setText(R.string.et_subtitle_ttf);
        title.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTTFing) {
                    isTTFing = true;
                    mGvTTF.setVisibility(View.VISIBLE);
                    tvAeInfo.setVisibility(View.GONE);
                } else {
                    onExitTTF();
                }
            }
        });
        UIConfiguration mUIConfig = SdkEntry.getSdkService().getUIConfig();
        mTTFHandler = new TTFHandler(mGvTTF, mTTFListener, false, (null != mUIConfig ? mUIConfig.fontUrl : null));
        mTTFHandler.setChecked(mTTFPosition);
    }

    private void onExitTTF() {
        isTTFing = false;
        mGvTTF.setVisibility(View.GONE);
        tvAeInfo.setVisibility(View.VISIBLE);
    }

    private String mTtfPath;
    private int mTTFPosition = 0;
    private ITTFHandlerListener mTTFListener = new ITTFHandlerListener() {

        @Override
        public void onItemClick(String file, int position) {
            onTTFItemClick(file, position);
            onExitTTF();
        }
    };

    private void onTTFItemClick(String file, int position) {
        mTTFPosition = position;
        mTtfPath = file;
        try {
            Typeface typeface = Typeface.createFromFile(mTtfPath);
            mEditText.setTypeface(typeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (isTTFing) {
            onExitTTF();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        mTTFHandler.onDestory();
        super.onDestroy();
    }


    private OnClickListener mOnRightListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            onSure();
        }
    };

    static class AEText implements Parcelable {

        public AEText(String text, String ttf, int ttfIndex) {
            this.text = text;
            this.ttf = ttf;
            this.ttfIndex = ttfIndex;
        }

        protected AEText(Parcel in) {
            text = in.readString();
            ttf = in.readString();
            ttfIndex = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(text);
            dest.writeString(ttf);
            dest.writeInt(ttfIndex);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<AEText> CREATOR = new Creator<AEText>() {
            @Override
            public AEText createFromParcel(Parcel in) {
                return new AEText(in);
            }

            @Override
            public AEText[] newArray(int size) {
                return new AEText[size];
            }
        };

        public String getText() {
            return text;
        }

        private String text;

        public String getTtf() {
            return ttf;
        }

        public int getTtfIndex() {
            return ttfIndex;
        }

        private String ttf;
        private int ttfIndex;


    }

    private void writeTxtToFile(String strcontent, String path) {
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
                Log.d("TestFile", "Create the file:" + path);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }


    private void onSure() {
        Intent intent = new Intent();
        if (isBoxed) {
            int len = mQuikTextMediaInfos.size();
            String tmp = mEditText.getText().toString();
            String[] arr = tmp.split("\n");
            ArrayList<AEText> list = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                if (i < arr.length) {
                    //有效的文本
                    String str = arr[i];
                    AEText aeText = new AEText(str, mTtfPath, mTTFPosition);
                    list.add(aeText);
                } else {
                    //没有文本，截取Boxed中的部分layer
                    list.add(null);
                }

            }
            writeTxtToFile(mEditText.getText().toString(), PathUtils.getAssetFileNameForSdcard("zishuotest", ".txt"));
            intent.putExtra(PARAM_AE_RESULT_LIST, list);
        } else {
            AEText aeText = new AEText(mEditText.getText().toString(), mTtfPath, mTTFPosition);
            intent.putExtra(PARAM_AE_RESULT, aeText);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    static AEText getAEText(Intent data) {
        AEText aeText = data.getParcelableExtra(PARAM_AE_RESULT);
        return aeText;
    }

    static List<AEText> getAETextList(Intent data) {
        return data.getParcelableArrayListExtra(PARAM_AE_RESULT_LIST);
    }

    private static final String TAG = "AETextActivity";

    /**
     * 保存成图片文件
     *
     * @param layerInfo
     * @param text
     * @param ttf
     * @return
     */
    static String fixAEText(AETextLayerInfo layerInfo, String text, String ttf) {
        return fixAEText(layerInfo, text, ttf, null);
    }

    static String fixAEText(AETextLayerInfo layerInfo, String text, String ttf, AEFragmentInfo.LayerInfo info) {


        Bitmap bitmap = new AEText2Bitmap().fixAEText(layerInfo, text, ttf);

        String file = null;
        if (null != info) {
            file = PathUtils.getTempFileNameForSdcard("Temp", layerInfo.getName() + ".png");
//            file = PathUtils.getTempFileNameForSdcard("Temp", layerInfo.getName() + info.getRefId() + ">>" + info.getName() + ".png");
        } else {
            file = PathUtils.getTempFileNameForSdcard("Temp", layerInfo.getName() + ".png");
        }

        BitmapUtils.saveBitmapToFile(bitmap, file, true);
        bitmap.recycle();

        return file;

    }

}
