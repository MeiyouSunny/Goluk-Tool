package com.mobnote.golukmain.photoalbum;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.mobnote.eventbus.EventAddTailer;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.videosuqare.RingView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import cn.npnt.ae.AfterEffectListener.SimpleExporterListener;
import cn.npnt.ae.SimpleExporter;
import cn.npnt.ae.exceptions.EffectException;
import de.greenrobot.event.EventBus;

/**
 * Created by leege100 on 16/5/20.
 */
public class AddTailerDialogFragment extends DialogFragment implements SimpleExporterListener{
    RingView mAddTailerRingview;
    TextView mAddTailerProgressTv;
    View mRootView;
    Dialog mDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Dialog_No_Border);
        EventBus.getDefault().register(this);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        mRootView = inflater.inflate(R.layout.dialog_add_tailer, container, false);
        mAddTailerRingview = (RingView) mRootView.findViewById(R.id.ringview_addtailer_loading);
        mAddTailerProgressTv = (TextView) mRootView.findViewById(R.id.tv_addtailer_loadingprogress);
        return mRootView;
    }

    public void onEventMainThread(EventAddTailer event){
        if(event != null){
            if(event.getExportStatus() == EventAddTailer.EXPORT_STATUS_EXPORTING){
                int process = (int) (event.getExportProcess() * 100);
                mAddTailerRingview.setProcess(process);
                Log.i("msg","视频导出:导出eventBus+ " + String.valueOf(event.getExportProcess()));

                String org = getString(R.string.str_video_export_progress);
                //String formattedOrg = String.format(org, process);
                this.mAddTailerProgressTv.setText(org);
            }else if(event.getExportStatus() == EventAddTailer.EXPORT_STATUS_FINISH){
                mAddTailerRingview.setProcess(100);
                this.mAddTailerProgressTv.setText(getString(R.string.str_video_export_succeed));
                if(mDialog != null && mDialog.isShowing()){
                    mDialog.dismiss();
                }
            }else if(event.getExportStatus() == EventAddTailer.EXPORT_STATUS_FAILED){
            }
        }
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onStartToExport(SimpleExporter mSimpleExporter) {
        EventBus.getDefault().post(new EventAddTailer(EventAddTailer.EXPORT_STATUS_START,0,null));
    }

    @Override
    public void onExporting(SimpleExporter mSimpleExporter, float v) {
        EventBus.getDefault().post(new EventAddTailer(EventAddTailer.EXPORT_STATUS_EXPORTING,v,null));
    }

    @Override
    public void onExportFinished(SimpleExporter mSimpleExporter, String path) {
        EventBus.getDefault().post(new EventAddTailer(EventAddTailer.EXPORT_STATUS_FINISH,0,path));
    }

    @Override
    public void onExportFailed(SimpleExporter mSimpleExporter, EffectException e) {
        EventBus.getDefault().post(new EventAddTailer(EventAddTailer.EXPORT_STATUS_FAILED,0,null));
    }
}
