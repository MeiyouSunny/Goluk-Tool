package com.rd.veuisdk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.Unbinder;

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
    private Unbinder unBinder;
    private int mTransitionCount;

    @BindView(R2.id.gridview_transition)
    GridView mGridview;
    @BindView(R2.id.cbRandomTransition)
    CheckBox mCbRandomTransition;
    @BindView(R2.id.tvTransitionDuration)
    TextView mTvTransitionDuration;
    @BindView(R2.id.sbTransitionTime)
    SeekBar mSbTransitionDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStrActivityPageName = getString(R.string.addmenu_transition);
        setContentView(R.layout.activity_transition_preview);
        unBinder = ButterKnife.bind(this);
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

    @OnCheckedChanged(R2.id.cbRandomTransition)
    public void useRandomTransition(boolean isChecked) {
        if (isChecked) {
            mTransitionAdapter.setChecked(-1);
        }
    }

    @OnCheckedChanged(R2.id.cbTransitionApplyToAll)
    public void applyToAll(boolean isChecked) {
        mApplyToAll = isChecked;
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
        unBinder.unbind();
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
                    arrTransitions.add(mTransition);
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
