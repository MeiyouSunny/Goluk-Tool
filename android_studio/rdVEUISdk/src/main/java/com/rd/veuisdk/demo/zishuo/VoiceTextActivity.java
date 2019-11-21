package com.rd.veuisdk.demo.zishuo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.ExtTextView;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.InputUtls;
import com.rd.vecore.Music;
import com.rd.vecore.VirtualAudio;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.veuisdk.BaseActivity;
import com.rd.veuisdk.MoreMusicActivity;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SdkEntry;
import com.rd.veuisdk.model.AudioMusicInfo;
import com.rd.veuisdk.mvp.model.VoiceTextModel;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  文字语音转换
 */
public class VoiceTextActivity extends BaseActivity implements View.OnClickListener {

    public static final int IMPORT = 10;
    private int mRequestCode = 102;
    /**
     * 顶部、底部、下一步（文字直接下一步）、题词
     */
    private RelativeLayout mRlTopMenu;
    private RelativeLayout mRlBottomMenu;
    private ExtButton mBtnRight;
    private ListTextView mListTextView;//题词显示
    //自定义题词
    private InscriptionLibraryFragment mLibraryFragment;
    private LinearLayout mRlTextCustom;
    private RelativeLayout mRlInput;
    private EditText mEtTextCustom;
    private ExtButton mBtnTextSave;
    /**
     * 语音转文字
     */
    private TextView mTvRecordingDuration, mTvCountdown;//时长显示、倒计时
    private ExtTextView mBtnImport, mBtnDelete, mBtnNext;//导入、删除、继续、下一步
    private ImageView mBtnContinue;//暂停 继续
    private LinearLayout mLlRecorded, mLlRecording;//录音、录音中
    private WaveformView mWvRecording;//波形图
    private ImageView mBtnRecordingAudition;//试听
    private long mTime = 0;//当前波形图滑动对应时间
    private Animation mAlpha;//倒计时动画
    private VoiceTextModel mModel;
    /**
     * 录音状态 0表示未开始 1表示暂停 2表示录音中
     */
    private int mRecordingState = 0;
    /**
     * 音频管理器、媒体捕获控件
     */
    private AudioManager mAudioManager;
    private MediaRecorder mMediaRecorder = null;
    private boolean mMusicStreamMute;//静音标识
    //虚拟音频
    private VirtualAudio mVirtualAudio;
    /**
     * 录音保存文件
     */
    private File mAudioFile = null;
    private ArrayList<Music> mMusic = new ArrayList<>();
    private ArrayList<File> mFiles = new ArrayList<>();
    /**
     * handler
     */
    private Handler mHandler;
    private View mContent;
    //AI语音失败超额
    private boolean mAIExcess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_text);
        initView();
        init();
        initHandler();
    }

    private void initView() {
        mContent = findViewById(android.R.id.content);
        mRlTopMenu = findViewById(R.id.rl_top_menu);
        mRlBottomMenu = findViewById(R.id.rl_bottom_menu);
        mBtnRight = findViewById(R.id.btnRight);
        //语音转文字
        mTvRecordingDuration = findViewById(R.id.tv_recording_duration);
        mTvCountdown = findViewById(R.id.tv_countdown);
        mBtnImport = findViewById(R.id.btn_import);
        mBtnDelete = findViewById(R.id.btn_delete);
        mBtnNext = findViewById(R.id.btn_next);
        mBtnContinue = findViewById(R.id.btn_continue);
        mLlRecorded = findViewById(R.id.ll_recorded);
        mLlRecording = findViewById(R.id.ll_recording);
        mWvRecording = findViewById(R.id.recording_track);
        mBtnRecordingAudition = findViewById(R.id.btn_recording_audition);
        mListTextView = findViewById(R.id.ltv_text);
        mRlTextCustom = findViewById(R.id.rl_text_custom);
        mRlInput = findViewById(R.id.rl_custom_input);
        mEtTextCustom = findViewById(R.id.et_text_custom);
        mBtnTextSave = findViewById(R.id.btn_et_save);

        mBtnRight.setOnClickListener(this);
        findViewById(R.id.btn_inscription_library).setOnClickListener(this);
        mBtnImport.setOnClickListener(this);
        mBtnRecordingAudition.setOnClickListener(this);
        findViewById(R.id.btn_recording).setOnClickListener(this);
        mBtnDelete.setOnClickListener(this);
        mBtnContinue.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mBtnTextSave.setOnClickListener(this);
        findViewById(R.id.btnLeft).setOnClickListener(this);

        //音轨回调
        mWvRecording.setListener(new WaveformView.WaveListener() {
            @Override
            public void onTime(long time) {
                mTime = time;
                //显示时间
                mTvRecordingDuration.setText(DateTimeUtils.stringForTimeSS(time));
                //判断时间是否大于3分钟  大于就停止
                if (time >= 3 * 60 * 1000) {
                    pauseRecording();
                }
            }

            @Override
            public void onCanRecording(boolean can) {
                mBtnContinue.setClickable(can);
                //图标变灰
                if (can) {
                    mBtnContinue.setImageResource(R.drawable.voice_text_btn_recording_continue);
                } else {
                    mBtnContinue.setImageResource(R.drawable.voice_text_btn_continue_prohibited);
                }
            }

            @Override
            public void onDrag() {
                //暂停试听
                auditionPause();
            }

        });

        //文本显示单击回调
        mListTextView.setListener(new ListTextView.onListClickListener() {
            @Override
            public void onClick(String s) {
                controlKeyboardLayout();
                InputUtls.showInput(mEtTextCustom);
                mEtTextCustom.setText(s);
                mEtTextCustom.setSelection(s.length());
            }

            @Override
            public void isEmpty(boolean b) {
                if (b) {
                    mBtnRight.setVisibility(View.INVISIBLE);
                } else {
                    mBtnRight.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void init() {
        //音频管理器
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //倒计时动画
        mAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha_countdown);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnLeft) {
            //取消返回
            //判断 是否输入文字或者录音
            if (!mListTextView.isEmpty()) {
                finish();
            } else {
                onShowAlert();
            }
        } else if (id == R.id.btnRight) {
            //下一步
            text2Voice();
        } else if (id == R.id.btn_inscription_library) {
            //题词库 如果没有就进入题词库选择 否则弹出菜单
            if (mListTextView.isEmpty()) {
                //弹出菜单
                inscriptionMenu();
            } else {
                //进入题词库
                getInscriptionLibrary();
            }
        } else if (id == R.id.btn_et_save) {
            //自定义题词库 确定
            mListTextView.setCustomText(mEtTextCustom.getText().toString());
            removeInputListener();
            InputUtls.hideKeyboard(mEtTextCustom);
        } else if (id == R.id.btn_recording) {
            //开始录音
            //判断sdk超额
            if (mAIExcess) {
                onToast(R.string.auto_server_error);
                return;
            }
            beginRecording();
        } else if (id == R.id.btn_import) {
            //导入
            if (mAIExcess) {
                onToast(R.string.auto_server_error);
                return;
            }
            mBtnImport.setEnabled(false);
            MoreMusicActivity.onLocalMusic(this, IMPORT);
        }  else if (id == R.id.btn_delete) {
            //删除录音
            onShowDelete();
        }  else if (id == R.id.btn_continue) {
            //暂停录音
            pauseRecording();
        } else if (id == R.id.btn_next) {
            //下一步
            voice2Text();
        } else if (id == R.id.btn_recording_audition) {
            //录音 试听
            if (mVirtualAudio != null) {
                if (mVirtualAudio.isPlaying()) {
                    auditionPause();
                } else {
                    auditionPlay();
                }
            } else {
                //录音试听
                SysAlertDialog.showLoadingDialog(VoiceTextActivity.this, R.string.isloading);
                mHandler.sendEmptyMessage(AUDITION);
            }
        }
    }

    /**
     * 题词库弹窗
     */
    private void inscriptionMenu() {
        String[] menu = getResources().getStringArray(R.array.inscription_library);
        SysAlertDialog.showListviewAlertMenu(this, null, menu,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which == 0) {
                            //更换题词 进入题词库
                            getInscriptionLibrary();
                        } else if (which == 1) {
                            //自定义题词 重新编辑
                            mHandler.sendEmptyMessageDelayed(CUSTOM, 100);
                        } else if (which == 2) {
                            //清除题词
                            mListTextView.clear();
                        }
                    }
                });
    }

    /**
     * 录音 准备 倒计时
     */
    private void beginRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && AppConfiguration.isFirstShowAudio()) {
            int hasReadPermission = checkSelfPermission(
                    Manifest.permission.RECORD_AUDIO);

            List<String> permissions = new ArrayList<String>();
            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 1);
                //删除录音返回
                deleteRecording();
                return;
            }
        }
        if (mRecordingState == 0) {
            //开始录音
            //UI设置 隐藏顶部、底部菜单、录音
            mRlTopMenu.setVisibility(View.INVISIBLE);
            mRlBottomMenu.setVisibility(View.INVISIBLE);
            mLlRecorded.setVisibility(View.GONE);
            //倒计时
            mCountdown = 3;
            mBtnContinue.setClickable(false);
            mHandler.sendEmptyMessage(COUNTDOWN);
            //删除、下一步隐藏
            mLlRecording.setVisibility(View.VISIBLE);
            mBtnDelete.setVisibility(View.INVISIBLE);
            mBtnNext.setVisibility(View.INVISIBLE);
            mBtnContinue.setVisibility(View.VISIBLE);
            mBtnContinue.setImageResource(R.drawable.voice_text_btn_recording_pause);
            mRecordingState = 2;
        }
    }

    //开始录制时间和结束时间
    private long mStartTime, mEndTime;

    /**
     * 开始录音 继续录音
     */
    private void startRecording() {
        //静音
        setMusicStreamMute(true);
        //播放器停止、隐藏试听按钮
        autionStop();
        mBtnRecordingAudition.setVisibility(View.GONE);
        //初始化开始录音
        try {
            // 初始化MediaRecorder
            initMediaRecorder();
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            mStartTime = System.currentTimeMillis();
            mHandler.sendEmptyMessage(PUTVALUE);
        } catch (Exception ex) {
            SysAlertDialog.showAutoHideDialog(this, null, getString(R.string.error_record_audio_retry),
                    Toast.LENGTH_SHORT);
            return;
        }
        mWvRecording.setRecordingPause(false);
    }

    /**
     * 停止录音 暂停录音
     */
    private void stopRecording() {
        if (mMediaRecorder == null) {
            return;
        }
        mMediaRecorder.stop();
        mEndTime = System.currentTimeMillis();
        mMediaRecorder = null;
        //判断是否录制有效
        mFiles.add(mAudioFile);
        if (!mWvRecording.isLimited() && mMusic.size() == 0) {
            //有效内容太少
            Toast.makeText(this, "有效内容太少", Toast.LENGTH_SHORT).show();
            deleteRecording();
        } else {
            Music music = VirtualVideo.createMusic(mAudioFile.getAbsolutePath());
            float start = 0;
            if (mMusic.size() > 0) {
                start = mMusic.get(mMusic.size() - 1).getTimelineEnd();
            }
            //设置时间区域 秒
            music.setTimelineRange(start, start + Utils.ms2s(mEndTime - mStartTime));
            mMusic.add(music);
            setMusicStreamMute(false);
            mBtnRecordingAudition.setImageResource(R.drawable.voice_text_btn_play);
            mBtnRecordingAudition.setVisibility(View.VISIBLE);
            mWvRecording.setRecordingPause(true);
        }
    }

    /**
     * 继续录音
     */
    private void pauseRecording() {
        if (mCountdown != 3) {
            return;
        }
        //继续
        if (mRecordingState == 1) {
            //继续录音
            //大于3分钟就不能继续录制
            if (mWvRecording.getDuration() >= 3 * 60 * 1000) {
                mBtnContinue.setEnabled(false);
                mBtnContinue.setImageResource(R.drawable.voice_text_btn_continue_prohibited);
                return;
            }
            //UI设置
            mBtnDelete.setVisibility(View.INVISIBLE);
            mBtnNext.setVisibility(View.INVISIBLE);
            mBtnContinue.setImageResource(R.drawable.voice_text_btn_recording_pause);
            mRecordingState = 2;
            startRecording();
        } else if (mRecordingState == 2) {
            //暂停录音
            //UI设置 显示删除、继续录、下一步
            mBtnDelete.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            mBtnContinue.setImageResource(R.drawable.voice_text_btn_recording_continue);
            mRecordingState = 1;
            stopRecording();
        }
    }

    /**
     * 删除录音
     */
    private void deleteRecording() {
        //删除文件
        for (File f : mFiles) {
            if (f.exists()) {
                f.delete();
            }
        }
        mFiles.clear();
        mMusic.clear();
        mAudioFile = null;
        //UI显示 波形图恢复、录音状态为0、顶部菜单和底部显示、试听按钮隐藏
        mWvRecording.deleteAll();
        mRecordingState = 0;
        mRlTopMenu.setVisibility(View.VISIBLE);
        mRlBottomMenu.setVisibility(View.VISIBLE);
        mLlRecorded.setVisibility(View.VISIBLE);
        mLlRecording.setVisibility(View.GONE);
        mBtnRecordingAudition.setVisibility(View.GONE);
    }

    /**
     * 初始化 录音
     */
    private void initMediaRecorder() {
        // 设置录制的音频文件的存放位置
        mAudioFile = new File(PathUtils.getTempFileNameForSdcard("recording", "aac"));
        // 初始化Mp3音频录制
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.reset();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置麦克风
            /*
             * 设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            /* 设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //设置采样率 44100
            mMediaRecorder.setAudioSamplingRate(44100);
            //设置码率
            mMediaRecorder.setAudioEncodingBitRate(96000);
            mMediaRecorder.setOutputFile(mAudioFile.getAbsolutePath());
        }
    }

    /**
     * 下一步 跳转 传递文字
     */
    private void onNext() {
        SysAlertDialog.cancelLoadingDialog();
        //保存
        startActivityForResult(new Intent(this, ZiShuoActivity.class), mRequestCode);
    }

    /**
     * 设定音乐流是否禁音
     *
     * @param bMute 是否禁音
     */
    public synchronized void setMusicStreamMute(boolean bMute) {
        if (mMusicStreamMute != bMute) {
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, bMute);
            mMusicStreamMute = bMute;
        }
    }

    /**
     * 语音转文字
     */
    private void voice2Text() {
        //语音转文字   保存语音-->转换成文字-->保存文字-->跳转
       if (mMusic.size() > 0) {
           TempZishuoParams.getInstance().setMusicList(mMusic);
       } else {
           //错误 没有音频保存
           return;
       }
       //没有网络
        if (CoreUtils.checkNetworkInfo(VoiceTextActivity.this) == CoreUtils.UNCONNECTED) {
            //没有网络
            SysAlertDialog.showAutoHideDialog(VoiceTextActivity.this, 0,
                    R.string.please_check_network, Toast.LENGTH_SHORT);
            return;
        }
        if (mModel == null) {
            mModel = new VoiceTextModel();
        }
        SysAlertDialog.showLoadingDialog(VoiceTextActivity.this, R.string.isloading);
       //上传转文字
        mModel.onAI(this, mMusic, new VoiceTextModel.IVoice2TextCallBack(){

            @Override
            public void onResult(List<TextNode> list, String error) {
                if (list != null && list.size() > 0) {
                    ArrayList<TextNode> textNodes = new ArrayList<>();
                    TextNode newNode = null;
                    for (TextNode node : list) {
                        //0.32/13.14===叫你们看看啊，这双眼皮单眼皮儿之间的区别，我说这帮小姑娘血量脑瓜了，不要去啦，算确实呢，当时就惊。
                        String text = node.getText();
                        float end = node.getBegin();//每段结束时间 也是下一段开始时间
                        //判断中间是否不连续 例如：0-3 aaaaa    4-6 bbbbbb
                        if (textNodes.size() > 0 && end > textNodes.get(textNodes.size() - 1).getEnd()) {
                            newNode = new TextNode();
                            newNode.setBegin(textNodes.get(textNodes.size() - 1).getEnd());
                            newNode.setEnd(end);
                            textNodes.add(newNode);
                        }
                        int num = text.length() - getSymbolNum(text);//字数 计算比例
                        String[] substrs = breakSentence(text);
                        for (int i = 0; i < substrs.length; i++) {
                            String item = substrs[i];
                            while (item.length() > 8) {
                                newNode = new TextNode();
                                newNode.setText(item.substring(0, 8));
                                newNode.setBegin(end);
                                newNode.setEnd(end + (newNode.getText().length() + 0.0f) / num * (node.getEnd() - node.getBegin()));
                                textNodes.add(newNode);
                                item = item.substring(8);
                                end = newNode.getEnd();
                            }
                            newNode = new TextNode();
                            newNode.setText(item);
                            newNode.setBegin(end);
                            newNode.setEnd(end + (newNode.getText().length() + 0.0f) / num * (node.getEnd() - node.getBegin()));
                            textNodes.add(newNode);
                            end = newNode.getEnd();
                        }
                    }
                    TempZishuoParams.getInstance().setTextNodes(textNodes);
                    onNext();
                } else {
                    if (!TextUtils.isEmpty(error)) {
                        if ("error".equals(error)) {
                            onToast(R.string.auto_server_error);
                        } else {
                            onToast(error);
                        }
                        mAIExcess = true;
                    } else {
                        //转换失败
                        onToast(R.string.auto_recognition_failed);
                    }
                    SysAlertDialog.cancelLoadingDialog();
                }
            }
        });
    }

    /**
     * 文字转语音  直接文字下一步
     */
    private void text2Voice(){
        SysAlertDialog.showLoadingDialog(this, R.string.isloading);
        //文字转语音 保存文字-->转换成语音-->保存语音-->跳转
        ArrayList<TextNode> mNodes = new ArrayList<>();
        //自定义题词
        if (!TextUtils.isEmpty(mListTextView.getCustomText())) {
            participle(mNodes, mListTextView.getCustomText(), 0);
        } else {
            ArrayList<String> arrayList = mListTextView.getLocalTextList();
            for (int i = 0; i < arrayList.size(); i++) {
                if (i == 0) {
                    participle(mNodes, arrayList.get(i), 0);
                } else {
                    participle(mNodes, arrayList.get(i), mNodes.get(mNodes.size() - 1).getEnd());
                }
            }
        }
        TempZishuoParams.getInstance().setTextNodes(mNodes);
        //避免因为添加音乐 取消后没有删除音乐
        TempZishuoParams.getInstance().removeMusic();
        onNext();
    }

    //分词 保证每一句不大于8个字
    private void participle(ArrayList<TextNode> mNodes, String s, float begin) {
        if (TextUtils.isEmpty(s)) {
            TextNode node = new TextNode();
            node.setBegin(begin);
            node.setEnd(begin + 1.5f);
            node.setContinued(0.3f);
            mNodes.add(node);
            return;
        }
        String[] substrs = breakSentence(s);
        TextNode node;
        for (int i = 0, j = 0; i < substrs.length; i++) {
            //判断单句是否大于8个字
            String string = substrs[i];
            while (string.length() > 8) {
                node = new TextNode();
                node.setText(string.substring(0, 8));
                node.setBegin((float) (j++ * 1.5) + begin);
                node.setEnd((float) (node.getBegin() + 1.5));
                node.setContinued(0.3f);
                mNodes.add(node);
                string = string.substring(8);
            }
            node = new TextNode();
            node.setText(string);
            node.setBegin((float) (j++ * 1.5) + begin);
            node.setEnd((float) (node.getBegin() + 1.5));
            node.setContinued(0.4f);
            mNodes.add(node);
        }
    }

    /**
     * 标点符号、换行等断句
     * @param s 文字
     * @return
     */
    private String[] breakSentence(String s) {
        String regEx="[。 ？！?.!,，:：“”、`\"\";；‘’''\t|\n|\\s|\r]";
        Pattern p = Pattern.compile(regEx);
        return p.split(s);
    }

    /**
     * 返回断句的标点的个数
     * @param str
     * @return
     */
    private int getSymbolNum(String str){
        String regEx="[。 ？！?.!,，:：“”、`\"\";；‘’''\t|\n|\\s|\n]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        int count = 0;
        while(m.find()){
            count ++;
        }
        return count;
    }

    @Override
    public void onBackPressed() {
        //如果倒计时中不响应返回
        if (mCountdown != 3) {
            return;
        }
        if (mRecordingState == 1) {
            //如果暂停中  删除录音返回
            onShowDelete();
        } else if (mRecordingState == 2) {
            //如果录音中 暂停录音
            pauseRecording();
        } else if (findViewById(R.id.ll_inscription).getVisibility() == View.VISIBLE) {
            //如果在题词库中
            mLibraryFragment.onBackPressed();
        } else {
            //否则弹窗提示
            if (!mListTextView.isEmpty()) {
                finish();
            } else {
                onShowAlert();
            }
        }
    }

    /**
     * 提示是否放弃保存
     */
    private void onShowAlert() {
        SysAlertDialog.createAlertDialog(this,
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
                        finish();
                    }
                }, false, null).show();
    }

    /**
     * 提示是否删除录音
     */
    private void onShowDelete() {
        SysAlertDialog.createAlertDialog(this,
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
                        deleteRecording();
                        dialog.dismiss();
                    }
                }, false, null).show();
    }

    /**
     * 进入题词库
     */
    private void getInscriptionLibrary() {
        if (mLibraryFragment == null) {
            mLibraryFragment = new InscriptionLibraryFragment();
            mLibraryFragment.setListener(new InscriptionLibraryFragment.InscriptionListener() {
                @Override
                public void cancel() {
                    mHandler.sendEmptyMessage(INSCRIPTION);
                }

                @Override
                public void sure() {
                    mHandler.sendEmptyMessage(INSCRIPTION);
                    mListTextView.setStringList(TempZishuoParams.getInstance().getStrings());
                }

                @Override
                public void onCustom() {
                    mHandler.sendEmptyMessageDelayed(CUSTOM, 100);
                }
            });
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.ll_inscription, mLibraryFragment);
            ft.commit();
        }
        findViewById(R.id.ll_inscription).setVisibility(View.VISIBLE);

    }

    /**
     * 倒计时、传声贝、试听、题词库、自定义题词
     */
    private int COUNTDOWN = 500;
    private int PUTVALUE = 501;
    private int AUDITION = 502;
    private int INSCRIPTION = 503;
    private int CUSTOM = 504;
    private int mCountdown = 3;
    //至少录制1秒才能停止
    private int mClickable = 0;

    private void initHandler() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == COUNTDOWN) {
                    //倒计时
                    if (mCountdown <= 0) {
                        //开始录音
                        mTvCountdown.setText("");
                        mTvCountdown.setVisibility(View.GONE);
                        mCountdown = 3;
                        //开始录音
                        startRecording();
                        mClickable = 1;
                    } else {
                        mTvCountdown.setVisibility(View.VISIBLE);
                        mTvCountdown.setText(String.valueOf(mCountdown--));
                        mTvCountdown.startAnimation(mAlpha);
                        Message message = mHandler.obtainMessage();
                        message.what = COUNTDOWN;
                        mHandler.sendMessageDelayed(message, 1000);
                    }
                } else if (msg.what == PUTVALUE) {
                    //录音传递声贝
                    if (mRecordingState == 2) {
                        mHandler.sendEmptyMessageDelayed(PUTVALUE, 50);
                        int db = (int) (20 * Math.log10((double) mMediaRecorder.getMaxAmplitude() / 1));
                        mWvRecording.putValue(db);
                        if (mClickable == 20) {
                            mBtnContinue.setClickable(true);
                            mClickable = 0;
                        } else {
                            mClickable++;
                        }
                    }
                } else if (msg.what == AUDITION) {
                    //试听
                    if (mVirtualAudio == null) {
                        mVirtualAudio = new VirtualAudio(VoiceTextActivity.this);
                        mVirtualAudio.setOnPlaybackListener(new VirtualVideoView.VideoViewListener() {
                            @Override
                            public void onPlayerPrepared() {
                                auditionPlay();
                            }

                            @Override
                            public void onPlayerCompletion() {
                                auditionPause();
                                mWvRecording.playComplete();
                            }

                            @Override
                            public void onGetCurrentPosition(float position) {
                                //单位秒
                                mWvRecording.setTime(position);
                            }
                        });
                    }
                    try {
                        for (Music music : mMusic) {
                            mVirtualAudio.addMusic(music);
                        }
                        mVirtualAudio.build();
                    } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                        autionStop();
                    }
                } else if (msg.what == INSCRIPTION) {
                    findViewById(R.id.ll_inscription).setVisibility(View.GONE);
                } else if (msg.what == CUSTOM) {
                    findViewById(R.id.ll_inscription).setVisibility(View.GONE);
                    //自定义题词 重新编辑
                    mListTextView.clear();
                    controlKeyboardLayout();
                    InputUtls.showInput(mEtTextCustom);
                    mEtTextCustom.setText("");
                }
                return false;
            }
        });
    }

    /**
     * 录音 试听播放
     */
    private void auditionPlay() {
        //声音轨道传递出的时间和生成的声音时间长度有误差
        //前者根据线的数量计算时间
        if (Math.abs(mTime - Utils.s2ms(mVirtualAudio.getDuration())) < 2000) {
            mVirtualAudio.seekTo(0);
        } else {
            mVirtualAudio.seekTo(Utils.ms2s(mTime));
        }
        mVirtualAudio.start();
        //设置图标
        mBtnRecordingAudition.setImageResource(R.drawable.voice_text_btn_pause);
        SysAlertDialog.cancelLoadingDialog();
    }

    /**
     * 录音 试听暂停
     */
    private void auditionPause() {
        if (mVirtualAudio != null) {
            mVirtualAudio.pause();
        }
        //设置图标
        mBtnRecordingAudition.setImageResource(R.drawable.voice_text_btn_play);
    }

    /**
     * 录音 试听停止
     */
    private void autionStop() {
        if (mVirtualAudio != null) {
            mVirtualAudio.setOnPlaybackListener(null);
            mVirtualAudio.reset();//重置
            mVirtualAudio = null;
            SysAlertDialog.cancelLoadingDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (permissions.length > 0 &&  grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                beginRecording();
            } else {
                Toast.makeText(this, "需要录音权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMPORT) {
            mBtnImport.setEnabled(true);
            if (resultCode == Activity.RESULT_OK) {
                AudioMusicInfo audioMusic = (AudioMusicInfo) data
                        .getParcelableExtra(MoreMusicActivity.MUSIC_INFO);
                Music ao = VirtualVideo.createMusic(audioMusic.getPath());
                ao.setTimeRange(Utils.ms2s(audioMusic.getStart()), Utils.ms2s(audioMusic.getEnd()));
                mMusic.clear();
                mMusic.add(ao);
                //获取到音乐   接口获取文字
                voice2Text();
            }
        } else if (requestCode == mRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                String outpath = data.getStringExtra(SdkEntry.EDIT_RESULT);
                Intent intent = new Intent();
                intent.putExtra(SdkEntry.EDIT_RESULT, outpath);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFiles.clear();
        mMusic.clear();
        mWvRecording.recycle();
        mHandler.removeCallbacksAndMessages(null);
        mMediaRecorder = null;
        mAudioManager = null;
        autionStop();
        removeInputListener();
        InputUtls.hideKeyboard(mEtTextCustom);
        if (mModel != null) {
            mModel.recycle();
        }
    }

    //注册输入法监听，动态调整bu布局setY
    private com.rd.veuisdk.listener.OnGlobalLayoutListener mGlobalLayoutListener;

    private void controlKeyboardLayout() {
        removeInputListener();
        if (null != mContent) {
            mGlobalLayoutListener = new com.rd.veuisdk.listener.OnGlobalLayoutListener(mContent, mRlInput, mRlTextCustom);
            mGlobalLayoutListener.setEditHeight(CoreUtils.dpToPixel(200));
            mContent.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
        }
    }

    private void removeInputListener() {
        if (null != mContent) {
            if (null != mGlobalLayoutListener) {
                //先移除监听再隐藏输入法(移除的时候调用了强制恢复布局)
                mContent.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
                mGlobalLayoutListener.resetUI();
                mGlobalLayoutListener = null;
            }
        }
    }

}
