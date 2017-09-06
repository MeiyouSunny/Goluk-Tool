package com.rd.veuisdk;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.rd.recorder.ICameraZoomHandler;
import com.rd.veuisdk.ui.ZoomControl;

/**
 * 处理摄像头变焦
 *
 * @author abreal
 */
public class CameraZoomHandler implements ICameraZoomHandler {
    private final String TAG = "LiveCameraZoomHandler";
    private int mZoomState = ZOOM_STOPPED;
    private boolean mSmoothZoomSupported = false, mIsZoomSupported = false;
    private int mZoomValue; // The current zoom value.
    private int mZoomMax;
    private int mTargetZoomValue;
    private ZoomControl mZoomControl; // 缩放组件
    private ZoomListener mZoomListener = new ZoomListener();

    private boolean mPausing;
    private Camera mMainCamera;
    private ScaleGestureDetector mScaleDetector;
    private boolean mHandleScale = false;
    @SuppressWarnings("unused")
    private boolean mRecording;

    @Override
    public Camera getMainCamera() {
        return mMainCamera;
    }

    @Override
    public void setMainCamera(Camera m_mainCamera) {
        this.mMainCamera = m_mainCamera;
    }

    public boolean isPausing() {
        return mPausing;
    }

    public void setPausing(boolean m_bPausing) {
        this.mPausing = m_bPausing;
    }

    public void setRecording(boolean bRecording) {
        mRecording = bRecording;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.rd.RdVEUISdk.ICameraZoomHandler#getZoomState()
     */
    @Override
    public int getZoomState() {
        return mZoomState;
    }

    @Override
    public void setZoomState(int m_nZoomState) {
        this.mZoomState = m_nZoomState;
    }

    @Override
    public int getZoomValue() {
        return mZoomValue;
    }

    /**
     * 构造函数
     *
     * @param ctx
     * @param zoomCtrl
     */
    public CameraZoomHandler(Context ctx, ZoomControl zoomCtrl) {
        mZoomControl = zoomCtrl;
        mScaleDetector = new ScaleGestureDetector(ctx,
                m_scaleGestureListener);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.rd.RdVEUISdk.ICameraZoomHandler#initializeZoom()
     */
    @Override
    public void initializeZoom() {
        if (null != mMainCamera) {
            Parameters params = mMainCamera.getParameters();
            mSmoothZoomSupported = params.isSmoothZoomSupported();
            mIsZoomSupported = params.isZoomSupported();

            mZoomMax = params.getMaxZoom();
            // Currently we use immediate zoom for fast zooming to get better UX
            // and
            // there is no plan to take advantage of the smooth zoom.
            if (null != mZoomControl && mIsZoomSupported) {
                mZoomControl.setZoomMax(mZoomMax);
                mZoomControl.setZoomIndex(params.getZoom());
                mZoomControl.setSmoothZoomSupported(mSmoothZoomSupported);
                mZoomControl.setOnZoomChangeListener(new ZoomChangeListener());
            }
            mMainCamera.setZoomChangeListener(mZoomListener);
        }
    }

    /*
     * 响应手势
     */
    public boolean onTouch(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        return mHandleScale;
    }

    private class ZoomChangeListener implements
            ZoomControl.OnZoomChangedListener {
        // only for immediate zoom
        @Override
        public void onZoomValueChanged(int index) {
            CameraZoomHandler.this.onZoomValueChanged(index);
        }

        // only for smooth zoom
        @Override
        public void onZoomStateChanged(int state) {
            if (mPausing)
                return;

            if (state == ZoomControl.ZOOM_IN) {
                CameraZoomHandler.this.onZoomValueChanged(mZoomMax);
            } else if (state == ZoomControl.ZOOM_OUT) {
                CameraZoomHandler.this.onZoomValueChanged(0);
            } else {
                mTargetZoomValue = -1;
                if (mZoomState == ZOOM_START) {
                    mZoomState = ZOOM_STOPPING;
                    mMainCamera.stopSmoothZoom();
                }
            }
        }
    }

    private class ZoomListener implements
            android.hardware.Camera.OnZoomChangeListener {
        @Override
        public void onZoomChange(int value, boolean stopped,
                                 android.hardware.Camera camera) {
            mZoomValue = value;

            // Update the UI when we get zoom value.
            mZoomControl.setZoomIndex(value);

            setCameraZoom(value);

            if (stopped && mZoomState != ZOOM_STOPPED) {
                if (mTargetZoomValue != -1 && value != mTargetZoomValue) {
                    mMainCamera.startSmoothZoom(mTargetZoomValue);
                    mZoomState = ZOOM_START;
                } else {
                    mZoomState = ZOOM_STOPPED;
                }
            }
        }
    }

    private void onZoomValueChanged(int index) {
        // Not useful to change zoom value when the activity is paused.
        if (mPausing)
            return;

        if (mSmoothZoomSupported) {
            if (mTargetZoomValue != index && mZoomState != ZOOM_STOPPED) {
                mTargetZoomValue = index;
                if (mZoomState == ZOOM_START) {
                    mZoomState = ZOOM_STOPPING;
                    mMainCamera.stopSmoothZoom();
                }
            } else if (mZoomState == ZOOM_STOPPED && mZoomValue != index) {
                mTargetZoomValue = index;
                mMainCamera.startSmoothZoom(index);
                mZoomState = ZOOM_START;
            }
        } else {
            setCameraZoom(index);
        }
    }

    /**
     * 设置摄像头变焦值
     *
     * @param index
     */
    private void setCameraZoom(int index) {
        try {
            Parameters param = mMainCamera.getParameters();
            if (mZoomValue != index
                    && mIsZoomSupported
                    && (param.isZoomSupported() || param
                    .isSmoothZoomSupported())) {
                mZoomValue = index;
                param.setZoom(mZoomValue);
                mMainCamera.setParameters(param);
            }
        } catch (Exception ex) {

        }
    }

    private int m_nSetIndex;

    private ScaleGestureDetector.OnScaleGestureListener m_scaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            setCameraZoom(m_nSetIndex);
            mHandleScale = false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            m_nSetIndex = mZoomValue;
            mHandleScale = true;
            return mHandleScale;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();
            // Log.e(TAG, String.format("onScale scale:%.2f",scale));
            if (Float.isNaN(scale) || Float.isInfinite(scale)) {
                return true;
            }
            int nStepValue = 0; // 步进修正
            int nOldSetIndex = m_nSetIndex;
            // int nZoomStateTmp = ZoomControl.ZOOM_STOP;

            m_nSetIndex = Math.round(m_nSetIndex * scale);
            if (scale != 1.0f) {
                if (scale > 1.0f) { // 放大
                    nStepValue = 2;
                    // nZoomStateTmp = ZoomControl.ZOOM_IN;
                } else {
                    nStepValue = -2;
                    // nZoomStateTmp = ZoomControl.ZOOM_OUT;
                }
            }

            m_nSetIndex += nStepValue;

            if (m_nSetIndex > mZoomMax) {
                m_nSetIndex = mZoomMax;
            } else if (m_nSetIndex < 1) {
                m_nSetIndex = 1;
            }
            if (Math.abs(nOldSetIndex - m_nSetIndex) > 10) {
                m_nSetIndex = nOldSetIndex;
            }
            setCameraZoom(m_nSetIndex);
            // Log.d(TAG, String.format("New value:%d,scale:%.3f", m_nSetIndex,
            // scale));
            return true;
        }
    };

}
