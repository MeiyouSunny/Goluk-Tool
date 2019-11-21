package com.rd.veuisdk;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.ExtImageView;
import com.rd.veuisdk.ui.ExtSeekBar;
import com.rd.veuisdk.utils.AppConfiguration;

/**
 * 相机输出视频的配置
 */
public class RecorderConfigActivity extends BaseActivity {

    private ExtSeekBar mSeekBar;
    private final int MIN_BITRATE = 400;
    private final int MAX_BITRATE = 3000;
    private int max = MAX_BITRATE - MIN_BITRATE;
    private RadioGroup radioGroup;
    private RadioButton mRadioButton0;
    private RadioButton mRadioButton1;
    private RadioButton mRadioButton2;
    private RadioButton mRadioButton3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_config);
        ExtImageView left = (ExtImageView) findViewById(R.id.btnLeft);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.setting);
        mRadioButton0 = (RadioButton) findViewById(R.id.rbSize_0);
        mRadioButton1 = (RadioButton) findViewById(R.id.rbSize_1);
        mRadioButton2 = (RadioButton) findViewById(R.id.rbSize_2);
        mRadioButton3 = (RadioButton) findViewById(R.id.rbSize_3);
        mSeekBar = (ExtSeekBar) findViewById(R.id.sbBitrateBar);
        mSeekBar.setMinValue(MIN_BITRATE);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSeekBar.setMax(max);

        mSeekBar.setProgress(AppConfiguration.getRecorderBitrate() - MIN_BITRATE);

        radioGroup = (RadioGroup) findViewById(R.id.recordSizeRG);


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mRadioButton0.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mRadioButton1.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mRadioButton2.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                mRadioButton3.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                if (checkedId == R.id.rbSize_0) {
                    mSeekBar.setProgress(0);
                    mRadioButton0.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.config_rb_p, 0);
                } else if (checkedId == R.id.rbSize_1) {
                    mSeekBar.setProgress(850 - MIN_BITRATE);
                    mRadioButton1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.config_rb_p, 0);
                } else if (checkedId == R.id.rbSize_2) {
                    mSeekBar.setProgress(1800 - MIN_BITRATE);
                    mRadioButton2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.config_rb_p, 0);
                } else if (checkedId == R.id.rbSize_3) {
                    mSeekBar.setProgress(max);
                    mRadioButton3.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.config_rb_p, 0);
                }
            }
        });

        int mode = AppConfiguration.getRecorderSizeMode();
        if (mode == 0) {
            mRadioButton0.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.config_rb_p, 0);
            radioGroup.check(R.id.rbSize_0);
        } else if (mode == 1) {
            mRadioButton1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.config_rb_p, 0);
            radioGroup.check(R.id.rbSize_1);
        } else if (mode == 2) {
            mRadioButton2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.config_rb_p, 0);
            radioGroup.check(R.id.rbSize_2);
        } else {
            mRadioButton3.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.config_rb_p, 0);
            radioGroup.check(R.id.rbSize_3);
        }


    }

    private int size_mode = 0;

    @Override
    public void onBackPressed() {
        int bitRate = Math.min(MAX_BITRATE, MIN_BITRATE + mSeekBar.getProgress());
        int checkId = radioGroup.getCheckedRadioButtonId();
        if (checkId == R.id.rbSize_0) {
            size_mode = 0;
        } else if (checkId == R.id.rbSize_1) {
            size_mode = 1;
        } else if (checkId == R.id.rbSize_2) {
            size_mode = 2;
        } else if (checkId == R.id.rbSize_3) {
            size_mode = 3;
        }
        AppConfiguration.saveRecorderConfig(bitRate, size_mode);
        setResult(RESULT_OK);
        super.onBackPressed();
    }


}
