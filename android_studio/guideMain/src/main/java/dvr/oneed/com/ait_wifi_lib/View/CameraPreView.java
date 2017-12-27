package dvr.oneed.com.ait_wifi_lib.View;//package dvr.oneed.com.ait_wifi_lib.View;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.content.res.Configuration;
//import android.graphics.ImageFormat;
//import android.graphics.PixelFormat;
//import android.os.Handler;
//import android.os.Message;
//import android.preference.PreferenceManager;
//import android.util.AttributeSet;
//import android.view.Display;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//
//import com.zhy.http.okhttp.callback.StringCallback;
//
//import org.videolan.libvlc.EventHandler;
//import org.videolan.libvlc.IVideoPlayer;
//import org.videolan.libvlc.LibVLC;
//import org.videolan.libvlc.LibVlcException;
//import org.videolan.vlc.Util;
//import org.videolan.vlc.WeakHandler;
//
//import java.util.LinkedList;
//import java.util.List;
//
//import dvr.oneed.com.ait_wifi_lib.api.AitProvide;
//import dvr.oneed.com.ait_wifi_lib.utils.L;
//import okhttp3.Call;
//
///**
// * Created by Administrator on 2016/6/7 0007.
// *创建这个类 主要是用来做 camera preview 使用
// * 主要处理libvlc之间的的代用关系
// * 1.首先获取rtsp的码流路径
// * 2.建立vlc的setview  与  surfaceview之间的关系
// * 3.进入该页面的时候,可以自动链接,断掉之后,可以自动链接
// *
// */
//public class CameraPreView extends SurfaceView implements SurfaceHolder.Callback,IVideoPlayer {
//    Context mContext;
//    private SurfaceHolder mSurfaceHolder ;
//    //用于码流播放
//    private LibVLC mLibVLC ;
//    //摄像头比例
//    private static final int SURFACE_BEST_FIT = 0 ;
//    private static final int SURFACE_FIT_HORIZONTAL = 1 ;
//    private static final int SURFACE_FIT_VERTICAL = 2 ;
//    private static final int SURFACE_FILL = 3 ;
//    private static final int SURFACE_16_9 = 4 ;
//    private static final int SURFACE_4_3 = 5 ;
//    private static final int SURFACE_ORIGINAL = 6 ;
//    private int mCurrentSize = SURFACE_BEST_FIT ;
//    // size of the video
//    private int mVideoHeight ;
//    private int mVideoWidth ;
//    private int mVideoVisibleHeight ;
//    private int mVideoVisibleWidth ;
//    private int mSarNum ;
//    private int mSarDen ;
//    //根据状态判断当前用户处于的模式
//    public static String mSensorMode="";
//    //配置一个直播的地址
//    public static String mLiveStreamUrl="";
//    // 消息接收
//// Message ID
//    private static final int	MSG_VIDEO_RECORD = 1;
//    private static final int	MSG_VIDEO_STOP   = 2;
//    private static final int	MSG_MODE_CHANGE  = 3;
//    private static final int	MSG_MODE_WRONG   = 4;
//    private static final int	MSG_MODE_LDWS_LEFT   = 5;
//    private static final int	MSG_MODE_LDWS_RIGHT   = 6;
//    private static final int	MSG_MODE_FCWS_ALARM   = 7;
//    private static final int	MSG_MODE_SAG_ALARM   = 8;
//    private static final int	MSG_MODE_UPDATE_ADASROI_ON   = 9;
//    private static final int	MSG_MODE_UPDATE_ADASROI_OFF   = 10;
//    private static final int	MSG_MODE_RTSP_PLAYING   = 11;
//
//
//    public CameraPreView(Context context) {
//        super(context);
//        mContext=context;
//    }
//    public CameraPreView(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        mContext=context;
//
//    }
//    public CameraPreView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        mContext=context;
//        initData();
//    }
//
//    /**
//     *初始化 surfaceView过程,包括增加 libvlc过程
//     *
//     * */
//    public void initData(){
//        //
//        try {
//            mLibVLC = Util.getLibVlcInstance(mContext) ;
//        } catch (LibVlcException e) {
//            e.printStackTrace();
//        }
//        EventHandler em = EventHandler.getInstance() ;
//        em.addHandler(eventHandler) ;
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext) ;
//        mSurfaceHolder = this.getHolder() ;
//        String chroma = pref.getString("chroma_format", "") ;
//        if (chroma.equals("YV12")) {
//            mSurfaceHolder.setFormat(ImageFormat.YV12) ;
//        } else if (chroma.equals("RV16")) {
//            mSurfaceHolder.setFormat(PixelFormat.RGB_565) ;
//            PixelFormat info = new PixelFormat() ;
//            PixelFormat.getPixelFormatInfo(PixelFormat.RGB_565, info) ;
//        } else {
//            mSurfaceHolder.setFormat(PixelFormat.RGBX_8888) ;
//            PixelFormat info = new PixelFormat() ;
//            PixelFormat.getPixelFormatInfo(PixelFormat.RGBX_8888, info) ;
//        }
//        mSurfaceHolder.addCallback(this) ;
//        LibVLC.restart(mContext) ;
//    }
//
//
//
//
//
//
//
//
//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        if (format == PixelFormat.RGBX_8888)
//            L.d( "Pixel format is RGBX_8888") ;
//        else if (format == PixelFormat.RGB_565)
//            L.d( "Pixel format is RGB_565") ;
//        else if (format == ImageFormat.YV12)
//            L.d( "Pixel format is YV12") ;
//        else
//            L.d( "Pixel format is other/unknown") ;
//
//        if(mLibVLC!=null) mLibVLC.attachSurface(holder.getSurface(), this) ;
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        L.v("surfaceDestroyed======");
//        //  mLibVLC.detachSurface() ;
//    }
//
//    @Override
//    public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
//
//        if (width * height == 0)
//            return ;
//
//        // store video size
//        mVideoHeight = height ;
//        mVideoWidth = width ;
//        mVideoVisibleHeight = visible_height ;
//        mVideoVisibleWidth = visible_width ;
//        mSarNum = sar_num ;
//        mSarDen = sar_den ;
//        Message msg = mHandler.obtainMessage(SURFACE_SIZE) ;
//        mHandler.sendMessage(msg) ;
//    }
//
//    /**
//     * 根据size大小的值设置view的视频大小
//     * */
//    private void changeSurfaceSize() {
//        // get screen size
//        Display display= ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//        int dw = display.getWidth();
//        int dh = display.getHeight();
//        // getWindow().getDecorView() doesn't always take orientation into
//        // account, we have to correct the values
//        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ;
//        if (dw > dh && isPortrait || dw < dh && !isPortrait) {
//            int d = dw ;
//            dw = dh ;
//            dh = d ;
//        }
//
//        // sanity check
//        if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {
//            L.e( "Invalid surface size") ;
//            return ;
//        }
//
//        // compute the aspect ratio
//        double ar, vw ;
//        double density = (double) mSarNum / (double) mSarDen ;
//        if (density == 1.0) {
//			/* No indication about the density, assuming 1:1 */
//            vw = mVideoVisibleWidth ;
//            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight ;
//        } else {
//			/* Use the specified aspect ratio */
//            vw = mVideoVisibleWidth * density ;
//            ar = vw / mVideoVisibleHeight ;
//        }
//
//        // compute the display aspect ratio
//        double dar = (double) dw / (double) dh ;
//
//        switch (mCurrentSize) {
//            case SURFACE_BEST_FIT:
//                if (dar < ar)
//                    dh = (int) (dw / ar) ;
//                else
//                    dw = (int) (dh * ar) ;
//                break ;
//            case SURFACE_FIT_HORIZONTAL:
//                dh = (int) (dw / ar) ;
//                break ;
//            case SURFACE_FIT_VERTICAL:
//                dw = (int) (dh * ar) ;
//                break ;
//            case SURFACE_FILL:
//                break ;
//            case SURFACE_16_9:
//                ar = 16.0 / 9.0 ;
//                if (dar < ar)
//                    dh = (int) (dw / ar) ;
//                else
//                    dw = (int) (dh * ar) ;
//                break ;
//            case SURFACE_4_3:
//                ar = 4.0 / 3.0 ;
//                if (dar < ar)
//                    dh = (int) (dw / ar) ;
//                else
//                    dw = (int) (dh * ar) ;
//                break ;
//            case SURFACE_ORIGINAL:
//                dh = mVideoVisibleHeight ;
//                dw = (int) vw ;
//                break ;
//        }
//
//        // force surface buffer size
//        mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight) ;
//
//        // set display size
//        ViewGroup.LayoutParams lp = getLayoutParams() ;
//        lp.width = dw * mVideoWidth / mVideoVisibleWidth ;
//        lp.height = dh * mVideoHeight / mVideoVisibleHeight ;
//        setLayoutParams(lp) ;
//        // set frame size (crop if necessary)
//        invalidate() ;
//    }
//
//
//    private final Handler mHandler = new VideoPlayerHandler(this) ;
//    private static final int SURFACE_SIZE = 1 ;
//
//    private static class VideoPlayerHandler extends WeakHandler<CameraPreView> {
//        public VideoPlayerHandler(CameraPreView owner) {
//            super(owner) ;
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            CameraPreView activity = getOwner() ;
//            if (activity == null) // WeakReference could be GC'ed early
//                return ;
//
//            switch (msg.what) {
//
//                case SURFACE_SIZE:
//                    activity.changeSurfaceSize() ;
//                    break ;
//            }
//        }
//    } ;
//
//
//    /**
//     * Handle libvlc asynchronous events
//     */
//    private final Handler eventHandler = new VideoPlayerEventHandler(this) ;
//
//    private static class VideoPlayerEventHandler extends WeakHandler<CameraPreView> {
//        public VideoPlayerEventHandler(CameraPreView owner) {
//            super(owner) ;
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            CameraPreView activity = getOwner() ;
//            if (activity == null)
//                return ;
//            switch (msg.getData().getInt("event")) {
//                case EventHandler.MediaPlayerPlaying:
//                    L.i( "MediaPlayerPlaying") ;
//                    // activity.mCameraStatusHandler.sendMessage(activity.buildMessage(MSG_MODE_RTSP_PLAYING));
//                    //new GetCameraCamid().execute();
//                    if(mPlayListener!=null)mPlayListener.onPlaying();
//                    break ;
//                case EventHandler.MediaPlayerPaused:
//                    L.i( "MediaPlayerPaused") ;
//                    if(mPlayListener!=null)mPlayListener.onStop();
//                    break ;
//                case EventHandler.MediaPlayerStopped:
//                    if(mPlayListener!=null)mPlayListener.onStop();
//                    L.i( "MediaPlayerStopped") ;
//                    break ;
//                case EventHandler.MediaPlayerEndReached:
//                    L.i( "MediaPlayerEndReached") ;
//                    activity.endReached() ;
//                    break ;
//                case EventHandler.MediaPlayerVout:
//                    L.i( "MediaPlayerVout") ;
//                    activity.handleVout(msg) ;
//                    break ;
//                case EventHandler.MediaPlayerPositionChanged:
//                    // don't spam the logs
//                    break ;
//                case EventHandler.MediaPlayerEncounteredError:
//
//                    L.i("fMediaPlayerEncounteredError") ;
//                    activity.encounteredError();
//                    if(mPlayListener!=null)mPlayListener.onStop();
//                    break ;
//                default:
//                    L.e( String.format("Event not handled (0x%x)", msg.getData().getInt("event"))) ;
//                    break ;
//            }
//        }
//    } ;
//
//    private void endReached() {
//		/* Exit player when reach the end */
////        mEndReached = true ;
////        if (mProgressDialog != null && mProgressDialog.isShowing()) {
////            mProgressDialog.dismiss() ;
////            mProgressDialog = null ;
////        }
//        //  mContext.onBackPressed() ;
//
//        //play() ;
//    }
//
//    private void encounteredError() {
////        if (mProgressDialog != null && mProgressDialog.isShowing()) {
////            mProgressDialog.dismiss() ;
////            mProgressDialog = null ;
////        }

////        }
//
//    }
//
//    private void handleVout(Message msg) {
//        //if (msg.getData().getInt("data") == 0 && mEndReached) {
//        //stop() ;//chrison
//        //playLiveStream() ;//chrison
//        // }
//    }
//
//
//
//
//
//    /**
//     * 播放 预览视频
//     * **/
//    public void play(int connectionDelay,Handler mHandler, final String mMediaUrl) {
//
//        if (mContext != null) {
//            //   mLibVLC.playMRL(mMediaUrl) ;
//            Handler handler = mHandler;
//            handler.postDelayed(new Runnable() {
//                public void run() {
////                    if ( mLibVLC != null && IsCameraInPreviewMode())
////                    {
//                    if(mLibVLC!=null){
//                        mLibVLC.playMRL(mMediaUrl) ;
////                        mLibVLC.closeAout();
//                        // mLibVLC.pauseAout();
//                    }
//                    // invalidate() ;
////                    }
//
//                }
//            }, connectionDelay) ;
//        }
//    }
//
//    public boolean IsCameraInPreviewMode()
//    {
//        return mSensorMode.equals("Videomode") ||
//                mSensorMode.equals("VIDEO")     ||
//                mSensorMode.equals("Capturemode") ||
//                mSensorMode.equals("CAMERA") ||
//                mSensorMode.equals("BURST")  ||
//                mSensorMode.equals("TIMELAPSE");
//    }
//
//
//
//    public void  getMedioUrl(AitProvide.OparetionListener mListener,VlcPlayListener playListener){
//
//        AitProvide.getInstance().getCameraRtspUrl(mListener);
//        mPlayListener=playListener;
//    }
//
//    public void getAdasEnable(final AitProvide.OparetionListener mListener,
//                              final VlcPlayListener playListener){
//        AitProvide.getInstance().getAdasEnable(new StringCallback() {
//            @Override
//            public void onError(Call call, Exception e) {
//
//            }
//
//            @Override
//            public void onResponse(String response) {
//                //setInputEnabled(false);
//                getMedioUrl(mListener,playListener);
//            }
//        });
//    };
//
//    /**
//     * 判断是否正在播放
//     * */
//    public boolean isPlaying(){
//        if(mLibVLC!=null)
//            return  mLibVLC.isPlaying();
//        else
//            return false;
//    }
//
//
//    /**
//     * 停止播放预览
//     * */
//    public void stopVLC(){
//
//        //mLibVLC.detachSurface();
//        // mLibVLC.closeAout();
//        // mLibVLC.pause();
//        if (mLibVLC != null) {
//            // mLibVLC.playMRL("rtsp://");
//            //
//            mLibVLC.pause();
//            mLibVLC.stop() ;
//            //  mLibVLC.destroy();
//            mLibVLC = null ;
//        }
//        removePlayListener();
//        EventHandler em = EventHandler.getInstance() ;
//        em.removeHandler(eventHandler) ;
//
//    }
//    public void stop(){
//        // if (mLibVLC != null && mLibVLC.isPlaying()) {
//        mLibVLC.stop() ;
//        mLibVLC = null ;
//        // }
//    }
//
//    private List<View> mViewList = new LinkedList<View>() ;
//    private void setInputEnabled(boolean enabled) {
//        for (View view : mViewList) {
//            view.setEnabled(enabled) ;
//        }
//    }
//
//
//
//    public interface  VlcPlayListener{
//        public void onPlaying();
//        public void onStop();
//
//    }
//
//    public static VlcPlayListener mPlayListener;
//    public void removePlayListener( ){
//        mPlayListener=null;
//    }
//
//
//}
