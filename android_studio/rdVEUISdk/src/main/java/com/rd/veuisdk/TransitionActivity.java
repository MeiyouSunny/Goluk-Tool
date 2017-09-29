package com.rd.veuisdk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.models.Transition;
import com.rd.vecore.models.TransitionType;
import com.rd.veuisdk.adapter.TransitionAdapter;
import com.rd.veuisdk.model.TransitionInfo;
import com.rd.veuisdk.utils.IntentConstants;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;


/**
 * 切换转场
 *
 * @author scott
 */
public class TransitionActivity extends BaseActivity {

    private float mTransitionDuration;
    private Transition mTransition;
    private boolean mApplyToAll = false;
    private TransitionAdapter mTransitionAdapter;
    private int mTransitionCount;

    GridView mGridview;
    CheckBox mCbRandomTransition;
    TextView mTvTransitionDuration;
    SeekBar mSbTransitionDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStrActivityPageName = getString(R.string.addmenu_transition);
        setContentView(R.layout.activity_transition_preview);

        mTransitionAdapter = new TransitionAdapter(this);
        mTransition = getIntent().getParcelableExtra(IntentConstants.INTENT_EXTRA_TRANSITION);
        mTransitionCount = getIntent().getIntExtra(IntentConstants.INTENT_TRANSITION_COUNT, 1);
        if (mTransition == null) {
            mTransition = new Transition(TransitionType.TRANSITION_NULL);
        }
        mTransitionDuration = mTransition.getDuration();
        initViews();
        mGridview.setAdapter(mTransitionAdapter);
        mGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mTransition = getTransition(i);
                mCbRandomTransition.setChecked(false);
            }
        });

        mTransHandler = new TransitionHandler(this);
        ThreadPoolUtils.executeEx(new Runnable() {
            @Override
            public void run() {
                mTransHandler.init();
                mHandler.removeMessages(MSG_UI);
                mHandler.sendEmptyMessage(MSG_UI);
            }
        });
    }


    private Transition getTransition(int itemPosition) {
        TransitionInfo info = mTransitionAdapter.getItem(itemPosition);
//        Log.e("setOnItemClickListener", "onItemClick: " + info.getText());
        mTransitionAdapter.setChecked(itemPosition);
        TransitionType type;
        if (info.getText() == getString(R.string.show_style_item_null)) {
            type = TransitionType.TRANSITION_NULL;
        } else if (info.getText() == getString(R.string.show_style_item_recovery)) {
            type = TransitionType.TRANSITION_OVERLAP;
        } else if (info.getText() == getString(R.string.show_style_item_to_up)) {
            type = TransitionType.TRANSITION_TO_UP;
        } else if (info.getText() == getString(R.string.show_style_item_to_down)) {
            type = TransitionType.TRANSITION_TO_DOWN;
        } else if (info.getText() == getString(R.string.show_style_item_to_left)) {
            type = TransitionType.TRANSITION_TO_LEFT;
        } else if (info.getText() == getString(R.string.show_style_item_to_right)) {
            type = TransitionType.TRANSITION_TO_RIGHT;
        } else if (info.getText() == getString(R.string.show_style_item_flash_white)) {
            type = TransitionType.TRANSITION_BLINK_WHITE;
        } else if (info.getText() == getString(R.string.show_style_item_flash_black)) {
            type = TransitionType.TRANSITION_BLINK_BLACK;
        } else {
            type = TransitionType.TRANSITION_GRAY;
        }

        Transition transition = new Transition(type, info.getIconPath());
        transition.setTitle(info.getText());

        return transition;
    }

    private TransitionHandler mTransHandler;
    private final int MSG_UI = 1561;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_UI: {
                    if (null != mTransHandler) {
                        mTransitionAdapter.updateData(mTransHandler.getList());
                    }
                }
                break;
                default: {
                }
                break;
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        mHandler.removeMessages(MSG_UI);
        mGridview.setOnItemClickListener(null);
        if (null != mTransitionAdapter) {
            mTransitionAdapter.recycle();
        }
        super.onDestroy();

        if (null != mTransHandler) {
            mTransHandler.recycle();
            mTransHandler = null;
        }
    }

    @Override
    public void onBackPressed() {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                TransitionActivity.this.finish();
                TransitionActivity.this.overridePendingTransition(0, 0);
            }
        });

    }

    private Transition getRandomTransition() {
        Random random = new Random();
        int nTmp = random.nextInt(mTransitionAdapter.getCount());
        return getTransition(nTmp);
    }

    private void initViews() {
         mGridview = (GridView) findViewById(R.id.gridview_transition);
         mCbRandomTransition = (CheckBox) findViewById(R.id.cbRandomTransition);
         mTvTransitionDuration = (TextView) findViewById(R.id.tvTransitionDuration);
         mSbTransitionDuration= (SeekBar) findViewById(R.id.sbTransitionTime);

        mCbRandomTransition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mTransitionAdapter.setChecked(-1);
                }
            }
        });

        ((CheckBox)findViewById(R.id.cbTransitionApplyToAll)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mApplyToAll = isChecked;
            }
        });

        ExtButton left = (ExtButton) findViewById(R.id.btnLeft);
        ExtButton right = (ExtButton) findViewById(R.id.btnRight);

        left.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.public_menu_cancel, 0, 0, 0);
        right.setText("");
        right.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                R.drawable.public_menu_sure, 0);
        right.setVisibility(View.VISIBLE);
        right.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ArrayList<Transition> arrTransitions = new ArrayList<Transition>();
                if (mCbRandomTransition.isChecked()) {
                    if (mApplyToAll) {
                        for (int nTemp = 0; nTemp < mTransitionCount; nTemp++) {
                            arrTransitions.add(getRandomTransition());
                        }
                    } else {
                        arrTransitions.add(getRandomTransition());
                    }
                } else {
                    if (mApplyToAll) {
                        for (int nTemp = 0; nTemp < mTransitionCount; nTemp++) {
                            arrTransitions.add(mTransition);
                        }
                    } else {
                        arrTransitions.add(mTransition);
                    }
                }
                for (Transition transition : arrTransitions) {
                    transition.setDuration(mTransitionDuration);
                }
                Intent intent = new Intent();
                intent.putExtra(IntentConstants.INTENT_EXTRA_TRANSITION, arrTransitions);
                intent.putExtra(IntentConstants.TRANSITION_APPLY_TO_ALL, mApplyToAll);
                setResult(RESULT_OK, intent);
                onBackPressed();
            }
        });

        left.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                onBackPressed();
            }
        });

        ((TextView) findViewById(R.id.tvTitle)).setText(mStrActivityPageName);

        mSbTransitionDuration.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                double duration = (0.30f + ((float) progress / 100));
                DecimalFormat fnum = new DecimalFormat("##0.00");
                String dd = fnum.format(duration);
                mTvTransitionDuration.setText(getString(R.string.long_s, dd));
                mTransitionDuration = (float) duration;
            }
        });

        double duration = mTransitionDuration;
        DecimalFormat fnum = new DecimalFormat("##0.00");
        String dd = fnum.format(duration);
        mTvTransitionDuration.setText(getString(R.string.long_s, dd));
        mSbTransitionDuration.setProgress((int) ((duration - 0.30d) * 100));
    }
}
