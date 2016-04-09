package com.mobnote.golukmain.carrecorder;

import android.content.Context;

import com.rd.car.CarRecorderMessageReceiver;
import com.rd.car.ResultConstants;

/**
 * 消息接收基类
 * 
 * @author abreal<br/>
 * 
 */
public class RecorderMsgReceiverBase extends CarRecorderMessageReceiver {

    /**
     * 响应CarRecorder绑定
     * 
     * @param context
     *            app上下文
     * @param nResult
     *            返回值 >={@link ResultConstants#SUCCESS} 代表成功，否则为失败
     * @param strResultInfo
     *            具体返回消息
     */
    @Override
    public void onManagerBind(Context context, int nResult, String strResultInfo) {
	// TODO Auto-generated method stub

    }

    /**
     * 响应后台录制中时通知栏点击
     * 
     * @param context
     *            app上下文
     */
    @Override
    public void onRecordingNotifacationClick(Context context) {
	// TODO Auto-generated method stub

    }

    /**
     * 收到新分片录制消息
     * 
     * @param context
     *            app上下文
     */
    @Override
    public void onNewSplitVideo(Context context, String strResultInfo,
	    String strVideoPath) {
	// TODO Auto-generated method stub

    }

    /**
     * 响应已直播时间，这里只包括已直播视频(已上传的视频数据).
     * 
     * @param context
     *            app上下文
     * @param nPosition
     *            已直播时间 (ms)
     * 
     */
    @Override
    public void onGetLiveRecordPosition(Context context, int nPosition) {
	// TODO Auto-generated method stub

    }

    /**
     * 响应直播开始
     * 
     * @param context
     *            app上下文
     * @param nResult
     *            返回值 >={@link ResultConstants#SUCCESS} 代表成功，否则为失败
     * @param strResultInfo
     *            具体返回消息
     */
    @Override
    public void onLiveRecordBegin(Context context, int nResult,
	    String strResultInfo) {
	// TODO Auto-generated method stub
    }

    /**
     * 响应直播异常
     * 
     * @param context
     *            app上下文
     * @param nResult
     *            返回值 >={@link ResultConstants#SUCCESS} 代表成功，否则为失败
     * @param strResultInfo
     *            具体返回消息
     */
    @Override
    public void onLiveRecordFailed(Context context, int nResult,
	    String strResultInfo) {
	// TODO Auto-generated method stub

    }

    /**
     * 响应直播结束
     * 
     * @param context
     *            app上下文
     * @param nResult
     *            返回值 >={@link ResultConstants#SUCCESS} 代表成功，否则为失败
     * @param strResultInfo
     */
    @Override
    public void onLiveRecordEnd(Context context, int nResult,
	    String strResultInfo) {
	// TODO Auto-generated method stub

    }

    /**
     * 响应本地录制开始
     * 
     * @param context
     *            app上下文
     * @param nResult
     *            返回值 >={@link ResultConstants#SUCCESS} 代表成功，否则为失败
     * @param strResultInfo
     *            具体返回消息
     */
    @Override
    public void onLocalRecordBegin(Context context, int nResult, String strResultInfo) {
	// TODO Auto-generated method stub

    }

    /**
     * 响应本地录制结束
     * 
     * @param context
     *            app上下文
     * @param nResult
     *            返回值 >={@link ResultConstants#SUCCESS} 代表成功，否则为失败
     * @param strResultInfo
     */
    @Override
    public void onLocalRecordEnd(Context context, int nResult,
	    String strResultInfo) {
	// TODO Auto-generated method stub

    }

    /**
     * 响应本地录制异常
     * 
     * @param context
     *            app上下文
     * @param nResult
     *            返回值 >={@link ResultConstants#SUCCESS} 代表成功，否则为失败
     * @param strResultInfo
     *            具体返回消息
     */
    @Override
    public void onLocalRecordFailed(Context context, int nResult,
	    String strResultInfo) {
	// TODO Auto-generated method stub

    }

    /**
     * 响应开始时间范围内保存视频
     * 
     * @param context
     *            app上下文
     * @param nResult
     *            返回值 >={@link ResultConstants#SUCCESS} 代表成功，否则为失败
     * @param strResultInfo
     *            具体返回消息
     * @param strVideoPath
     *            当前保存的文件路径
     */
    @Override
    public void onTimeRangeRecordBegin(Context context, int nResult,
	    String strResultInfo, String strVideoPath) {
	// TODO Auto-generated method stub

    }

    /**
     * 响应时间范围内保存视频进行中
     * 
     * @param context
     *            app上下文
     * @param nProgress
     *            保存进度
     * @param nMax
     *            进度最大值
     * @param strVideoPath
     *            当前保存的文件路径
     */
    @Override
    public void onTimeRangeRecordProgress(Context context, int nProgress,
	    int nMax, String strVideoPath) {
	// TODO Auto-generated method stub

    }

    /**
     * 响应完成时间范围内保存视频
     * 
     * @param context
     *            app上下文
     * @param nResult
     *            返回值 >={@link ResultConstants#SUCCESS} 代表成功，否则为失败
     * @param strResultInfo
     *            具体返回消息
     * @param strVideoPath
     */
    @Override
    public void onTimeRangeRecordFinish(Context context, int nResult,
	    String strResultInfo, String strVideoPath) {
	// TODO Auto-generated method stub

    }

    /**
     * 响应保存截图
     * 
     * @param context
     *            app上下文
     * @param nResult
     *            返回值 >={@link ResultConstants#SUCCESS} 代表成功，否则为失败
     * @param path
     *            截图路径
     */
    @Override
    public void onScreenShot(Context context, int nResult, String path) {
	// TODO Auto-generated method stub

    }

}
