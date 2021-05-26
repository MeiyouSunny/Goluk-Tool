package com.mobnote.golukmain;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

@SuppressLint({"HandlerLeak", "Instantiatable"})
public class FragmentDevice extends Fragment implements OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.index_device, null);

        rootView.findViewById(R.id.connect).setOnClickListener(this);

        final String connStr2 = getResources().getString(R.string.wifi_link_wifi_name) + "<font color=\"#0587ff\">"
                + "Goluk_xx_xxxxxx" + "</font>" + getResources().getString(R.string.wifi_link_wifi_name2);
        TextView connectDec = rootView.findViewById(R.id.connectDec);
        connectDec.setText(Html.fromHtml(connStr2));


        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.connect) {
            ((MainActivity) getActivity()).connectGoluk(false);
        }
    }

}
