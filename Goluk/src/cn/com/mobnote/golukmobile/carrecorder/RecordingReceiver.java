/**
 * BackgroupRecordingReceiver.java
 * classes：com.rd.car.demo.BackgroupRecordingReceiver
 * @author abreal
 */
package cn.com.mobnote.golukmobile.carrecorder;


import com.rd.car.CarRecorderManager;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * 消息接收
 * 
 * @author abreal<br/>
 * 
 */
public class RecordingReceiver extends RecorderMsgReceiverBase {

    private static final String TAG = "RecordingReceiver";

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rd.car.demo.receiver.RecorderMsgReceiverBase#onRecordingNotifacationClick
     * (android.content.Context)
     */
    @Override
    public void onRecordingNotifacationClick(Context context) {
	CarRecorderManager.stopBackgroudStatus();
	Intent intent = new Intent(context, CarRecorderActivity.class);
	intent.setAction("com.rd.car.demo.RESUME_FROM_BACK");
	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	context.startActivity(intent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rd.car.demo.receiver.RecorderMsgReceiverBase#onNewSplitVideo(android
     * .content.Context, java.lang.String, java.lang.String)
     */
    @Override
    public void onNewSplitVideo(Context context, String strResultInfo,
	    String strVideoPath) {
//	Toast.makeText(context, strResultInfo, Toast.LENGTH_SHORT).show();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rd.car.demo.receiver.RecorderMsgReceiverBase#onTimeRangeRecordBegin
     * (android.content.Context, int, java.lang.String, java.lang.String)
     */
    @Override
    public void onTimeRangeRecordBegin(Context context, int nResult,
	    String strResultInfo, String strVideoPath) {
//	Toast.makeText(context, strResultInfo, Toast.LENGTH_SHORT).show();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rd.car.demo.receiver.RecorderMsgReceiverBase#onTimeRangeRecordProgress
     * (android.content.Context, int, int, java.lang.String)
     */
    @Override
    public void onTimeRangeRecordProgress(Context context, int nProgress,
	    int nMax, String strVideoPath) {
	Log.d(TAG, "onTimeRangeRecordProgress progress:" + nProgress + ",nMax:"
		+ nMax);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.rd.car.demo.receiver.RecorderMsgReceiverBase#onTimeRangeRecordFinish
     * (android.content.Context, int, java.lang.String, java.lang.String)
     */
    @Override
    public void onTimeRangeRecordFinish(Context context, int nResult,
	    String strResultInfo, String strVideoPath) {
//	Toast.makeText(context, strResultInfo, Toast.LENGTH_SHORT).show();
    }
}
