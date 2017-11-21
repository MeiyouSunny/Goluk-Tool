package com.mobnote.golukmain;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.http.HttpManager;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.internation.login.InternationUserLoginActivity;
import com.mobnote.golukmain.userlogin.UpHeadData;
import com.mobnote.golukmain.userlogin.UpHeadResult;
import com.mobnote.golukmain.userlogin.UpdUserHeadBeanRequest;
import com.mobnote.golukmain.userlogin.UploadUtil;
import com.mobnote.user.UserUtils;
import com.mobnote.util.ClipImageView;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.api.FileUtils;

public class ImageClipActivity extends BaseActivity implements OnClickListener, IRequestResultListener {

    private ClipImageView imageView;

    private Button saveHead;

    private Button cancelBtn;

    private CustomLoadingDialog mCustomProgressDialog = null;


    private boolean isSave = true;

    /**
     * 视频存放外卡文件路径
     */
    private static final String APP_FOLDER = android.os.Environment.getExternalStorageDirectory().getPath();

    private static final String headCachePatch = APP_FOLDER + "/goluk/head_cache/";

    private String cachePath = "";

    private Handler mHandler = null;

    private final static int UPLOAD_HEAD_PIC = 9000;

    private UpdUserHeadBeanRequest updUserHeadBeanRequest = null;

    private String urlhead = null;

    private static String mRequestUrl = HttpManager.getInstance().getWebDirectHost() + "/fileService/HeadUploadServlet";
    private boolean isRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.roadbook_crop_pic);

        mCustomProgressDialog = new CustomLoadingDialog(ImageClipActivity.this, this.getResources().getString(
                R.string.str_save_head_ongoing));
        saveHead = (Button) findViewById(R.id.saveBtn);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        imageView = (ClipImageView) findViewById(R.id.src_pic);

        try {
            String uriStr = getIntent().getStringExtra("imageuri");
            Uri uri = null;
            Bitmap bitmap = null;
            if (uriStr != null && !"".equals(uriStr)) {
                uri = Uri.parse(uriStr);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                options.inSampleSize = 4;// 图片宽高都为原来的4分之一，即图片为原来的8分之一
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
            } else {
                bitmap = getIntent().getParcelableExtra("imagebitmap");
            }
            if (bitmap == null) {
                GolukUtils.showToast(ImageClipActivity.this,
                        this.getResources().getString(R.string.str_file_format_error));
                this.finish();
            } else {
                if (bitmap.getHeight() < bitmap.getWidth()) {
                    Bitmap bp = bitmap;
                    bitmap = this.rotaingImageView(90, bp);
                    bp.recycle();
                }
                imageView.setImageBitmap(bitmap);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == UPLOAD_HEAD_PIC) {

                    try {
                        JSONObject result = new JSONObject(msg.obj.toString());
                        Boolean suc = result.getBoolean("success");

                        if (suc) {
                            JSONObject data = result.getJSONObject("data");
                            String rst = data.getString("result");
                            // 图片上传成功
                            if ("0".equals(rst)) {
                                if (cachePath != null && !"".equals(cachePath)) {
                                    File file = new File(cachePath);
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                    cachePath = "";
                                }

                                urlhead = data.getString("url");
                                updUserHeadBeanRequest.get(GolukApplication.getInstance().getMyInfo().uid, GolukApplication.getInstance().getMyInfo().phone, "2", urlhead, "");

                            } else {
                                GolukUtils.showToast(ImageClipActivity.this,
                                        ImageClipActivity.this.getResources().getString(R.string.str_save_photo_fail));
                            }

                        } else {
                            isSave = true;

                            if (mCustomProgressDialog.isShowing()) {
                                mCustomProgressDialog.close();
                            }
                            GolukUtils.showToast(ImageClipActivity.this,
                                    ImageClipActivity.this.getResources().getString(R.string.str_save_photo_fail));
                        }
                    } catch (JSONException e) {
                        isSave = true;

                        if (mCustomProgressDialog.isShowing()) {
                            mCustomProgressDialog.close();
                        }
                        GolukUtils.showToast(ImageClipActivity.this,
                                ImageClipActivity.this.getResources().getString(R.string.str_save_photo_fail));
                        e.printStackTrace();
                    }
                }
            }
        };

        initListener();

    }


    /*
     * 旋转图片
     *
     * @param angle
     *
     * @param bitmap
     *
     * @return Bitmap
     */
    public Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    private void initListener() {
        saveHead.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.saveBtn) {
            if (GolukUtils.isFastDoubleClick() || isRequest) {
                return;
            }
            isRequest = true;
            updUserHeadBeanRequest = new UpdUserHeadBeanRequest(IPageNotifyFn.PageType_ModifyHeadPic, this);
            if (isSave) {
                isSave = false;

                Bitmap bitmap = imageView.clip();

                if (bitmap == null) {
                    isSave = true;
                    GolukUtils.showToast(ImageClipActivity.this,
                            this.getResources().getString(R.string.request_data_error));
                    return;
                }

                if (!UserUtils.isNetDeviceAvailable(this)) {
                    isSave = true;
                    GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
                    return;
                }

                try {

//					this.saveBitmap(SettingImageView.toRoundBitmap(bitmap));
                    this.saveBitmap(bitmap);
                    isSave = true;
                    if (mCustomProgressDialog != null) {
                        mCustomProgressDialog.show();
                    }

                } catch (IOException e) {
                    isSave = true;
                    e.printStackTrace();
                } catch (JSONException e) {
                    isSave = true;
                    e.printStackTrace();
                }
                bitmap.recycle();
            }
        } else if (id == R.id.cancelBtn) {
            this.finish();
        } else {
        }

    }

    /**
     * 保存头像并上传
     *
     * @throws IOException
     * @throws JSONException
     */
    @SuppressWarnings("finally")
    public String saveBitmap(Bitmap bm) throws IOException, JSONException {


        JSONObject requestStr = null;

        String md5key = "";

        String picname = System.currentTimeMillis() + ".png";

        this.makeRootDirectory(headCachePatch);
        cachePath = headCachePatch + picname;

        File f = new File(cachePath);
        if (f.exists()) {
            f.delete();
        }
        FileOutputStream out = null;
        try {

            f.createNewFile();
            out = new FileOutputStream(f);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap cacheBitmap = this.compress(bm);

            cacheBitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
            byte[] bb = baos.toByteArray();

            md5key = GolukUtils.compute32(bb);
            baos.writeTo(out);
            requestStr = new JSONObject();
            requestStr.put("md5", md5key);
            requestStr.put("PicPath", FileUtils.javaToLibPath(cachePath));
            requestStr.put("channel", "2");

            final Map<String, File> files = new HashMap<String, File>();
            File file = new File(cachePath);
            files.put("head", file);

            final Map<String, String> params = new HashMap<String, String>();
            params.put("md5", GolukUtils.getFileMD5(file));


            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        String result = UploadUtil.post(mRequestUrl, params, files);
                        Message message = new Message();
                        message.obj = result;
                        message.what = UPLOAD_HEAD_PIC;
                        mHandler.sendMessage(message);
                        System.out.println("ddd" + result);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
            if (requestStr != null) {
                return requestStr.toString();
            } else {
                return null;
            }
        }

    }


    /**
     * 把图片压缩到100k 之下
     *
     * @param image
     * @return
     */
    public Bitmap compress(Bitmap image) {
        // 图片允许最大空间 单位：KB
        double maxSize = 50.00;
        // 将bitmap放至数组中，意在bitmap的大小（与实际读取的原文件要大）
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        // 将字节换成KB
        double mid = b.length / 1024;
        // 判断bitmap占用空间是否大于允许最大空间 如果大于则压缩 小于则不压缩
        if (mid > maxSize) {
            // 获取bitmap大小 是允许最大大小的多少倍
            double i = mid / maxSize;
            // 开始压缩 此处用到平方根 将宽带和高度压缩掉对应的平方根倍
            // （1.保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小）
            image = zoomImage(image, image.getWidth() / Math.sqrt(i), image.getHeight() / Math.sqrt(i));
        }

        return image;

    }

    /***
     * 图片的缩放方法
     *
     * @param bgimage
     *            ：源图片资源
     * @param newWidth
     *            ：缩放后宽度
     * @param newHeight
     *            ：缩放后高度
     * @return
     */
    public static Bitmap zoomImage(Bitmap bgimage, double newWidth, double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width, (int) height, matrix, true);
        return bitmap;
    }

    public void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {

        }
    }

    @Override
    protected void onResume() {
        mBaseApp.setContext(this, "imageClipActivity");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mCustomProgressDialog && mCustomProgressDialog.isShowing()) {
            mCustomProgressDialog.close();
            mCustomProgressDialog = null;
        }
    }

    public void startUserLogin() {
        Intent loginIntent = null;
        if (GolukApplication.getInstance().isMainland() == false) {
            loginIntent = new Intent(this, InternationUserLoginActivity.class);
        } else {
            loginIntent = new Intent(this, UserLoginActivity.class);
        }
        startActivity(loginIntent);
    }


    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (IPageNotifyFn.PageType_ModifyHeadPic == requestType) {
            if (null != mCustomProgressDialog && mCustomProgressDialog.isShowing()) {
                mCustomProgressDialog.close();
            }
            UpHeadResult headResult = (UpHeadResult) result;
            if (headResult != null && headResult.data != null) {
                if (!GolukUtils.isTokenValid(headResult.data.result)) {
                    startUserLogin();
                    return;
                }
            }
            if (headResult != null && headResult.success) {
                UpHeadData data = headResult.data;
                String rst = data.result;
                // 图片上传成功
                if ("0".equals(rst)) {

                    GolukApplication.getInstance().setMyinfo("", "", "", urlhead);
                    GolukUtils.showToast(ImageClipActivity.this, this.getResources().getString(R.string.str_save_success));
                    GolukUtils.showToast(ImageClipActivity.this, ImageClipActivity.this.getResources().getString(R.string.str_save_photo_success));
                    //修改头像之后，清除缓存 。 这里会让界面延迟销毁
                    //https://stackoverflow.com/questions/32406489/glide-how-to-find-if-the-image-is-already-cached-and-use-the-cached-version
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.get(ImageClipActivity.this).clearDiskCache();
                        }
                    }).start();
                    Intent it = new Intent(ImageClipActivity.this, UserPersonalInfoActivity.class);
                    it.putExtra("imagepath", urlhead);
                    ImageClipActivity.this.setResult(RESULT_OK, it);
                    ImageClipActivity.this.finish();

                } else {
                    GolukUtils.showToast(ImageClipActivity.this, this.getResources().getString(R.string.str_save_network_error));
                }
            } else {
                GolukUtils.showToast(ImageClipActivity.this, this.getResources().getString(R.string.str_save_network_error));
            }
            isSave = true;
        }
        isRequest = false;
    }

}
