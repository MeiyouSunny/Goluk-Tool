package com.mobnote.golukmain.wifibind;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.mobnote.golukmain.R;

/**
 * 选择设备类型
 */
public class DeviceTypeSelector {

    public void showDeviceTypeList(Context context, final OnDeviceTypeSelectListener listener) {
        final String[] types = context.getResources().getStringArray(R.array.device_type);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.unbind_select_title_txt)
                .setItems(types, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null)
                            listener.onTypeSelected(types[which]);
                    }
                })
                .setCancelable(false);
        builder.create().show();
    }

    public interface OnDeviceTypeSelectListener {
        void onTypeSelected(String type);
    }

}
