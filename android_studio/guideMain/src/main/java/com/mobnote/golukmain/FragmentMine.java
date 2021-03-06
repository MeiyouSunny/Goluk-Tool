package com.mobnote.golukmain;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.Event;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventMessageUpdate;
import com.mobnote.eventbus.EventUtil;
import com.mobnote.golukmain.http.HttpManager;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.photoalbum.PhotoAlbumActivity;
import com.mobnote.golukmain.userinfohome.UserInfohomeRequest;
import com.mobnote.golukmain.userinfohome.bean.UserLabelBean;
import com.mobnote.golukmain.userinfohome.bean.UserinfohomeRetBean;
import com.mobnote.golukmain.videosuqare.VideoSquareManager;
import com.mobnote.golukmain.watermark.BandCarBrandsRequest;
import com.mobnote.golukmain.watermark.CarBrandsRequest;
import com.mobnote.golukmain.watermark.WatermarkSettingActivity;
import com.mobnote.golukmain.watermark.bean.BandCarBrandResultBean;
import com.mobnote.golukmain.watermark.bean.CarBrandBean;
import com.mobnote.golukmain.watermark.bean.CarBrandsResultBean;
import com.mobnote.manager.MessageManager;
import com.mobnote.user.UserInterface;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.util.ZhugeUtils;

import org.json.JSONObject;

import java.util.List;

import androidx.fragment.app.Fragment;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * @author ?????????
 * @ ????????????:Goluk??????????????????
 */

@SuppressLint({"HandlerLeak", "Instantiatable"})
public class FragmentMine extends Fragment implements OnClickListener,
        UserInterface, VideoSuqareManagerFn, IRequestResultListener {

    /**
     * ????????????
     **/
    private RelativeLayout mUserCenterItem = null;
    /** ????????????????????????id **/
    // private RelativeLayout mUserCenterId = null;
    /**
     * ????????????
     **/
    private TextView mVideoItem = null;
    /**
     * ???????????????
     **/
    private TextView mCameraItem = null;
    /**
     * ????????????
     **/
    private TextView mSetItem = null;
    /**
     * ??????????????????
     **/
    private TextView mSkillItem = null;
    /**
     * ????????????
     **/
    private TextView mInstallItem = null;
    /**
     * ????????????
     **/
    private TextView mQuestionItem = null;
    // /** ??????????????? **/
    // private TextView mShoppingItem = null;
    /**
     * ????????????
     **/
    private TextView mProfitItem = null;
    /**
     *  ????????????
     * */
    private TextView mFeedBackItem = null;
    /**
     * ????????????
     */
    private RelativeLayout mMsgCenterItem = null;
    private TextView mMessageTip;

    private TextView mPraisedListItem = null;

    /**
     * ???????????????????????????????????????
     */
    private ImageView mImageHead, mImageAuthentication;
    private TextView mTextName, mTextIntroduction;
    private LinearLayout mVideoLayout;
    private LinearLayout mLLSSSS;
    private TextView mTextShare, mTextFans, mTextFollow;
    /**
     * ???????????? ????????????
     **/
    private LinearLayout mShareLayout, mFansLayout, mFollowLayout;

    /**
     * ??????????????????loading?????????
     **/
    private SharedPreferences mPreferences = null;
    private Editor mEditor = null;
    // LinearLayout mRootLayout = null;
    private MainActivity ma;

    /**
     * ????????????
     **/
    private String userHead, userName, userDesc, userUId, userSex,
            customavatar, userPhone;
    private int newFansCout;

    /**
     * ????????????
     **/
    private static final int TYPE_USER = 1;
    /**
     * ???????????????????????????
     **/
    private static final int TYPE_SHARE_PRAISE = 2;
    /**
     * ????????????
     **/
    private static final int TYPE_PROFIT = 3;
    /**
     * ????????????
     **/
    private static final int TYPE_FOLLOWING = 4;
    private static final String TAG = "FragmentMine";
    private int mServerCarBrandCount = 0;
    private int mClientCacheCount = 0;

    LinearLayout mMineRootView = null;
    private UserinfohomeRetBean mUserinfohomeRetBean;
    private ImageView mNewFansIv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GolukDebugUtils.d(TAG, "onCreate");
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GolukDebugUtils.d(TAG, "onDestroy");
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        GolukDebugUtils.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.index_more, null);
        mMineRootView = (LinearLayout) rootView;
        ma = (MainActivity) getActivity();
        setListener();
        initView();
        setupView();
        return rootView;
    }

    private void initDataFor4SShop() {
        if (!SharedPrefUtil.getUserIs4SShop()) {
            return;
        }
        if (SharedPrefUtil.getCacheCarBrand()) {
            startDownLoad(true);
            return;
        }
        mLLSSSS.setVisibility(View.VISIBLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(ma);
        builder.setMessage("???????????????????????????????????????????????????")
                .setTitle("??????")
                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startDownLoad(false);
                    }
                })
                .create().show();
    }

    private void startDownLoad(final boolean checkCacheValid) {
        //??????4S????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        final CarBrandsRequest request = new CarBrandsRequest(new IRequestResultListener() {
            @Override
            public void onLoadComplete(int requestType, Object result) {
                CarBrandsResultBean bean = (CarBrandsResultBean) result;
                if (bean == null
                        ||
                        bean.code != GolukConfig.SERVER_RESULT_OK
                        ||
                        bean.carBrands == null
                        ||
                        bean.carBrands.list == null
                        ) {
                    return;
                }
                mServerCarBrandCount = bean.carBrands.list.size();
                boolean sameAsServer = false;
                if (checkCacheValid) {
                    List<CarBrandBean> oldList = GolukFileUtils.restoreFileToList(GolukFileUtils.CAR_BRAND_OBJECT);
                    if (oldList != null) {
                        sameAsServer = oldList.size() == mServerCarBrandCount;
                    }
                    if (sameAsServer) {
                        return;
                    }
                }

                final ProgressDialog progressDialog = new ProgressDialog(ma);
                if (!GolukFileUtils.saveListToFile(bean.carBrands.list, GolukFileUtils.CAR_BRAND_OBJECT)) {
                    //TODO ???????????????????????????????????????
                    return;
                }
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setTitle("??????");
                progressDialog.setMessage("??????????????????");
                progressDialog.setMax(mServerCarBrandCount);
                progressDialog.setIndeterminate(false);
                progressDialog.setCancelable(false);
                progressDialog.show();
                for (final CarBrandBean carBrandBean : bean.carBrands.list) {
                    final ImageRequest request = new ImageRequest(carBrandBean.logoUrl,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {
                                    String imageName = carBrandBean.code + ".jpg";
                                    if (!GolukFileUtils.saveImageToExternalStorage(bitmap, imageName)) {
                                        progressDialog.dismiss();
                                        return;
                                    }
                                    mClientCacheCount++;
                                    progressDialog.setProgress(mClientCacheCount);
                                    //????????????????????????????????????????????????????????????
                                    if (mServerCarBrandCount != 0 && mClientCacheCount != 0 && mServerCarBrandCount == mClientCacheCount) {
                                        SharedPrefUtil.saveCacheCarBrand(true);
                                        mServerCarBrandCount = 0;
                                        mClientCacheCount = 0;
                                        progressDialog.dismiss();
                                    }
                                }
                            }, 0, 0, null, null, null);
                    HttpManager.getInstance().add(request);
                }
            }
        });
        request.get(GolukConfig.SERVER_PROTOCOL_V2, ma.mApp.mCurrentUId);
    }


    @Override
    public void onResume() {
        super.onResume();
        GolukDebugUtils.d(TAG, "onResume");
        resetLoginState();
        int msgCount = MessageManager.getMessageManager()
                .getMessageTotalCount();
        setMessageTipCount(msgCount);
        sendGetUserHomeRequest();
        if (GolukApplication.getInstance().isMainland()) {
            mProfitItem.setVisibility(View.VISIBLE);
        } else {
            mProfitItem.setVisibility(View.GONE);
        }
    }

    public void onEventMainThread(Event event) {
        if(EventUtil.isFollowEvent(event))
            sendGetUserHomeRequest();
    }

    public void onEventMainThread(EventMessageUpdate event) {
        if (null == event) {
            return;
        }

        switch (event.getOpCode()) {
            case EventConfig.MESSAGE_UPDATE:

                int msgCount = MessageManager.getMessageManager()
                        .getMessageTotalCount();
                setMessageTipCount(msgCount);
                break;
            default:
                break;
        }
    }

    private void setMessageTipCount(int total) {

        String strTotal = null;
        if (total > 99) {
            strTotal = "99+";
            mMessageTip.setVisibility(View.VISIBLE);
        } else if (total <= 0) {
            strTotal = "0";
            mMessageTip.setVisibility(View.GONE);
        } else {
            mMessageTip.setVisibility(View.VISIBLE);
            strTotal = String.valueOf(total);
        }

        mMessageTip.setText(strTotal);
    }

    public void setupView() {
        ma.mApp.mUser.setUserInterface(this);
    }

    /**
     * ???????????????
     */
    private void initView() {

        // ???????????? ???????????? ??????????????? ???????????? ?????????????????? ???????????? ???????????? ???????????????
        mUserCenterItem = (RelativeLayout) mMineRootView
                .findViewById(R.id.user_center_item);
        mVideoItem = (TextView) mMineRootView.findViewById(R.id.video_item);
        mCameraItem = (TextView) mMineRootView.findViewById(R.id.camera_item);
        mSetItem = (TextView) mMineRootView.findViewById(R.id.set_item);
        mSkillItem = (TextView) mMineRootView.findViewById(R.id.skill_item);
        mInstallItem = (TextView) mMineRootView.findViewById(R.id.install_item);
        mQuestionItem = (TextView) mMineRootView
                .findViewById(R.id.about_item);
        mProfitItem = (TextView) mMineRootView.findViewById(R.id.profit_item);
        mMsgCenterItem = (RelativeLayout) mMineRootView
                .findViewById(R.id.rl_my_message);
        mMessageTip = (TextView) mMineRootView
                .findViewById(R.id.tv_my_message_tip);
        mPraisedListItem = (TextView) mMineRootView
                .findViewById(R.id.tv_praise_item);

        // ??????????????????id
        mImageHead = (ImageView) mMineRootView
                .findViewById(R.id.user_center_head);
        mImageAuthentication = (ImageView) mMineRootView
                .findViewById(R.id.im_user_center_head_authentication);
        mTextName = (TextView) mMineRootView
                .findViewById(R.id.user_center_name_text);
        mTextIntroduction = (TextView) mMineRootView
                .findViewById(R.id.user_center_introduction_text);
        mVideoLayout = (LinearLayout) mMineRootView
                .findViewById(R.id.user_center_video_layout);
        mTextShare = (TextView) mMineRootView
                .findViewById(R.id.user_share_count);
        mTextFans = (TextView) mMineRootView.findViewById(R.id.user_fans_count);
        mTextFollow = (TextView) mMineRootView
                .findViewById(R.id.user_follow_count);
        mShareLayout = (LinearLayout) mMineRootView
                .findViewById(R.id.user_share);
        mFansLayout = (LinearLayout) mMineRootView.findViewById(R.id.user_fans);
        mFollowLayout = (LinearLayout) mMineRootView
                .findViewById(R.id.user_follow);
        mNewFansIv = (ImageView) mMineRootView.findViewById(R.id.iv_new_fans);
        mLLSSSS = (LinearLayout) mMineRootView.findViewById(R.id.ll_advanced_setting);
        if (SharedPrefUtil.getUserIs4SShop()) {
            mLLSSSS.setVisibility(View.VISIBLE);
        }
        mFeedBackItem = (TextView) mMineRootView.findViewById(R.id.opinion_item);

        // ????????????
        // ???????????? ???????????? ??????????????? ???????????? ?????????????????? ???????????? ???????????? ???????????????
        mUserCenterItem.setOnClickListener(this);
        mVideoItem.setOnClickListener(this);
        mCameraItem.setOnClickListener(this);
        mSetItem.setOnClickListener(this);
        mSkillItem.setOnClickListener(this);
        mInstallItem.setOnClickListener(this);
        mQuestionItem.setOnClickListener(this);
        // mShoppingItem.setOnClickListener(this);
        mShareLayout.setOnClickListener(this);
        mFansLayout.setOnClickListener(this);
        mFollowLayout.setOnClickListener(this);
        mProfitItem.setOnClickListener(this);
        mMsgCenterItem.setOnClickListener(this);
        mPraisedListItem.setOnClickListener(this);
        mLLSSSS.setOnClickListener(this);
        mFeedBackItem.setOnClickListener(this);
    }

    // ?????????????????????????????????
    private void resetLoginState() {

        mPreferences = getActivity().getSharedPreferences("firstLogin",
                Context.MODE_PRIVATE);
        ma.mApp.mUser.setUserInterface(this);

        GolukDebugUtils.i("lily", "--------" + ma.mApp.autoLoginStatus
                + ma.mApp.isUserLoginSucess + "=====mApp.registStatus ===="
                + ma.mApp.registStatus);
        if (ma.mApp.isUserLoginSucess || ma.mApp.registStatus == 2) {// ?????????
            GolukDebugUtils.i("lily", "---------------"
                    + ma.mApp.autoLoginStatus + "------loginStatus------"
                    + ma.mApp.loginStatus);
            // ????????????
            personalChanged();
        } else {
            // ?????????
            mLLSSSS.setVisibility(View.GONE);
            mVideoLayout.setVisibility(View.GONE);
            mImageAuthentication.setVisibility(View.GONE);
            GlideUtils.loadLocalHead(getActivity(), mImageHead,
                    R.drawable.usercenter_head_default);
            mTextName.setText(getActivity().getResources().getString(
                    R.string.str_click_to_login));
            mTextIntroduction.setTextColor(Color.rgb(128, 138, 135));
            mTextIntroduction.setText(getActivity().getResources().getString(
                    R.string.str_login_tosee_usercenter));
        }
    }

    AlertDialog dialog = null;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent = null;
        if (id == R.id.back_btn) {
            ma.mApp.mUser.setUserInterface(null);
        } else if (id == R.id.user_share) {
            if (isLoginInfoValid()) {
                GolukUtils.startUserCenterActivity(getActivity(), userUId);
            } else {
                clickToLogin(TYPE_SHARE_PRAISE);
            }
        } else if (id == R.id.user_fans) {
        } else if (id == R.id.user_follow) {
        } else if (id == R.id.user_center_item) {
            if (isLoginInfoValid()) {
                GolukUtils.startUserCenterActivity(getActivity(), userUId);
            } else {
                clickToLogin(TYPE_USER);
            }
        } else if (id == R.id.video_item) {
            ma.mApp.mUser.setUserInterface(null);
            intent = new Intent(getActivity(), PhotoAlbumActivity.class);
            intent.putExtra("from", "local");
            getActivity().startActivity(intent);
        } else if (id == R.id.camera_item) {
            Intent itCamera = new Intent(getActivity(), UnbindActivity.class);
            getActivity().startActivity(itCamera);
        } else if (id == R.id.set_item) {
            Intent itSet = new Intent(getActivity(), UserSetupActivity.class);
            getActivity().startActivity(itSet);

        } else if (id == R.id.skill_item) {
            Intent itSkill = new Intent(getActivity(), UserOpenUrlActivity.class);
            itSkill.putExtra(UserOpenUrlActivity.FROM_TAG, "skill");
            getActivity().startActivity(itSkill);
        } else if (id == R.id.install_item) {
            Intent itInstall = new Intent(getActivity(), UserOpenUrlActivity.class);
            itInstall.putExtra(UserOpenUrlActivity.FROM_TAG, "install");
            getActivity().startActivity(itInstall);
        } else if (id == R.id.about_item) {
            Intent itQuestion = new Intent(getActivity(), UserVersionActivity.class);
            getActivity().startActivity(itQuestion);
        } else if (id == R.id.profit_item) {
        } else if (id == R.id.rl_my_message) {
        } else if (id == R.id.tv_praise_item) {
        } else if (id == R.id.ll_advanced_setting) {
            gotoSSSSSetting();
        } else if (id == R.id.opinion_item) {
            Intent itOpinion = new Intent(getActivity(), UserOpinionActivity.class);
            startActivity(itOpinion);
        }
    }

    /**
     * ??????4S??????????????????????????????
     */
    private void gotoSSSSSetting() {
        if (ma.mApp.isIpcConnSuccess && SharedPrefUtil.getCacheCarBrand()) {
            if (ma.mApp.mIPCControlManager.isT1Relative()) {
                Intent specialSetting = new Intent(this.getActivity(), WatermarkSettingActivity.class);
                startActivity(specialSetting);
            } else {
                Toast.makeText(getActivity(), R.string.not_support_g, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), getActivity().getString(R.string.str_ipc_no_connect_str), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ????????????????????????s
     *
     * @return
     */
    private boolean isLoginInfoValid() {
        if (ma.mApp.loginStatus == 1 || ma.mApp.registStatus == 2
                || ma.mApp.autoLoginStatus == 2) {// ?????????
            return true;
        } else {
            return false;
        }
    }

    /**
     * ???????????????
     *
     * @param intentType
     */
    private void clickToLogin(int intentType) {
    }

    private void dismissDialog() {
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
    }

    // ????????????
    private void setListener() {
        // ????????????
        VideoSquareManager mVideoSquareManager = GolukApplication.getInstance()
                .getVideoSquareManager();
        if (null != mVideoSquareManager) {
            if (mVideoSquareManager
                    .checkVideoSquareManagerListener("indexmore")) {
                mVideoSquareManager
                        .removeVideoSquareManagerListener("indexmore");
            }
            mVideoSquareManager
                    .addVideoSquareManagerListener("indexmore", this);
        }
        //2.8.10????????????????????????????????????????????????????????????????????????????????????
        final BandCarBrandsRequest request = new BandCarBrandsRequest(new IRequestResultListener() {
            @Override
            public void onLoadComplete(int requestType, Object result) {
                BandCarBrandResultBean bean = (BandCarBrandResultBean) result;
                if (bean == null || bean.code != GolukConfig.SERVER_RESULT_OK) {
                    return;
                }
                SharedPrefUtil.removeBandCarRequest();
            }
        });
        if (request.resotreCacheRequest()) {
            request.postCache();
        }
    }

    /**
     * ????????????????????????
     */
    private void sendGetUserHomeRequest() {
        UserInfohomeRequest request = new UserInfohomeRequest(
                IPageNotifyFn.PageType_UserinfoHome, this);

        if ((ma.mApp.isUserLoginSucess == true || ma.mApp.registStatus == 2)
                && !TextUtils.isEmpty(userUId)) {
            request.get("100", userUId, userUId);
        }
    }

    /**
     * ??????????????????
     */
    public void initData() {
    }

    private void showHead(ImageView view, String headportrait) {
    }

    @Override
    public void VideoSuqare_CallBack(int event, int msg, int param1,
                                     Object param2) {
        if (event == VSquare_Req_MainPage_UserInfor) {
            if (RESULE_SUCESS == msg) {
                try {
                    String jsonStr = (String) param2;
                    GolukDebugUtils
                            .e("", "=======VideoSuqare_CallBack====jsonStr???"
                                    + jsonStr);
                    JSONObject dataObj = new JSONObject(jsonStr);
                    JSONObject data = dataObj.optJSONObject("data");
                    String praisemenumber = data.optString("praisemenumber");
                    String sharevideonumber = data
                            .optString("sharevideonumber");
                    GolukDebugUtils.e("",
                            "=======VideoSuqare_CallBack====praisemenumber???"
                                    + praisemenumber);
                    if ("".equals(praisemenumber)) {
                        praisemenumber = "0";
                    }
                    if ("".equals(sharevideonumber)) {
                        sharevideonumber = "0";
                    }
                    // mTextFans.setText(GolukUtils.getFormatNumber(praisemenumber));
                    // mTextShare.setText(GolukUtils.getFormatNumber(sharevideonumber));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * ???????????????????????? ???????????????
     */
    @Override
    public void statusChange() {
        if (ma.mApp.autoLoginStatus == 2) {
            dismissDialog();
            personalChanged();
        } else if (ma.mApp.autoLoginStatus == 3 || ma.mApp.autoLoginStatus == 4
                || !ma.mApp.isUserLoginSucess) {
            dismissDialog();
            personalChanged();
            mVideoLayout.setVisibility(View.GONE);
            mImageAuthentication.setVisibility(View.GONE);
            mTextName.setText(getActivity().getResources().getString(
                    R.string.str_click_to_login));
            mTextIntroduction.setTextColor(Color.rgb(128, 138, 135));
            mTextIntroduction.setText(getActivity().getResources().getString(
                    R.string.str_login_tosee_usercenter));
        } else if (ma.mApp.autoLoginStatus == 5) {
            mVideoLayout.setVisibility(View.VISIBLE);
            mImageAuthentication.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ???????????????????????????
     */
    public void personalChanged() {
        GolukDebugUtils.i("lily", "======registStatus====" + ma.mApp.registStatus);
        if (ma.mApp.autoLoginStatus == 3 || ma.mApp.autoLoginStatus == 4) {
            mVideoLayout.setVisibility(View.GONE);
            mImageAuthentication.setVisibility(View.GONE);
            if (null != getActivity()) {
                mTextName.setText(getActivity().getResources().getString(
                        R.string.str_click_to_login));
            }
            mTextIntroduction.setTextColor(Color.rgb(128, 138, 135));
            if (null != getActivity()) {
                mTextIntroduction.setText(getActivity().getResources().getString(
                        R.string.str_login_tosee_usercenter));
            }
            showHead(mImageHead, "7");
            return;
        }
        if (ma.mApp.loginStatus == 1 || ma.mApp.autoLoginStatus == 1
                || ma.mApp.autoLoginStatus == 2) {// ???????????????????????????????????????????????????
            mVideoLayout.setVisibility(View.VISIBLE);
            mImageAuthentication.setVisibility(View.VISIBLE);
            showHead(mImageHead, "7");
            initData();
        } else {// ??????????????????
            mVideoLayout.setVisibility(View.GONE);
            mImageAuthentication.setVisibility(View.GONE);
            if (null != getActivity()) {
                mTextName.setText(getActivity().getResources().getString(
                        R.string.str_click_to_login));
            }
            mTextIntroduction.setTextColor(Color.rgb(128, 138, 135));
            if (null != getActivity()) {
                mTextIntroduction.setText(getActivity().getResources().getString(
                        R.string.str_login_tosee_usercenter));
            }
            showHead(mImageHead, "7");
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        // TODO Auto-generated method stub
        if (requestType == IPageNotifyFn.PageType_UserinfoHome) {
            mUserinfohomeRetBean = (UserinfohomeRetBean) result;
            if (null != mUserinfohomeRetBean
                    && null != mUserinfohomeRetBean.data) {
                if ((ma.mApp.isUserLoginSucess || ma.mApp.registStatus == 2)
                        && !TextUtils.isEmpty(userUId)) {
                    mTextShare
                            .setText(GolukUtils
                                    .getFormatedNumber(mUserinfohomeRetBean.data.sharevideonumber));
                    mTextFans
                            .setText(GolukUtils
                                    .getFormatedNumber(mUserinfohomeRetBean.data.fansnumber));
                    mTextFollow
                            .setText(GolukUtils
                                    .getFormatedNumber(mUserinfohomeRetBean.data.followingnumber));

                    int newFansNumber = 0;
                    if (!TextUtils
                            .isEmpty(mUserinfohomeRetBean.data.newfansnumber)) {
                        newFansNumber = Integer
                                .valueOf(mUserinfohomeRetBean.data.newfansnumber);
                    }

                    if (newFansNumber > 0) {
                        mNewFansIv.setVisibility(View.VISIBLE);
                    } else {
                        mNewFansIv.setVisibility(View.GONE);
                    }

                    if (mUserinfohomeRetBean.data.user != null
                            && mUserinfohomeRetBean.data.user.label != null) {
                        UserLabelBean lable = mUserinfohomeRetBean.data.user.label;
                        //????????????4S???
                        if ("1".equals(lable.is4s)) {
                            mLLSSSS.setVisibility(View.VISIBLE);
                            //Cache the flag , we can setIpc watermark when offline
                            SharedPrefUtil.saveUserIs4SShop(true);
                            initDataFor4SShop();
                        } else {
                            mLLSSSS.setVisibility(View.GONE);
                            SharedPrefUtil.saveUserIs4SShop(false);
                        }
                        mImageAuthentication.setVisibility(View.VISIBLE);
                        if ("1".equals(lable.approvelabel)) {
                            mImageAuthentication
                                    .setImageResource(R.drawable.authentication_bluev_icon);
                        } else if ("1".equals(lable.headplusv)) {
                            mImageAuthentication
                                    .setImageResource(R.drawable.authentication_yellowv_icon);
                        } else if ("1".equals(lable.tarento)) {
                            mImageAuthentication
                                    .setImageResource(R.drawable.authentication_star_icon);
                        } else {
                            mImageAuthentication.setVisibility(View.GONE);
                        }
                    } else {
                        mImageAuthentication.setVisibility(View.GONE);
                    }

                    //??????????????????
                    if (null != mUserinfohomeRetBean && null != mUserinfohomeRetBean.data
                            && null != mUserinfohomeRetBean.data.user) {
                        ZhugeUtils.userInfoAnalyze(getActivity(), mUserinfohomeRetBean.data.user.uid,
                                mUserinfohomeRetBean.data.user.nickname, mUserinfohomeRetBean.data.user.introduce,
                                mUserinfohomeRetBean.data.sharevideonumber, mUserinfohomeRetBean.data.followingnumber,
                                mUserinfohomeRetBean.data.fansnumber);
                    }
                }
            }
        }
    }

}
