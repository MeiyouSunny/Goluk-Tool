package cn.com.mobnote.golukmobile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import cn.com.mobnote.util.ClipImageView;
import cn.com.mobnote.util.SettingImageView;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ImageClipActivity extends BaseActivity implements OnClickListener {

	private ClipImageView imageView;
	private Button saveHead;
	
	private SettingImageView siv = null;
	
	/** 视频存放外卡文件路径 */
	private static final String APP_FOLDER = android.os.Environment
			.getExternalStorageDirectory().getPath();

	private static final String headCachePatch = APP_FOLDER + "/goluk/head_cache/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.roadbook_crop_pic);
		Uri uri = Uri.parse(getIntent().getStringExtra("imageuri"));
		saveHead = (Button) findViewById(R.id.saveBtn);
		imageView = (ClipImageView) findViewById(R.id.src_pic);
		imageView.setImageURI(uri);
		siv = new SettingImageView(this);
		initListener();

	}

	private void initListener() {
		saveHead.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Bitmap bitmap = imageView.clip();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] bitmapByte = baos.toByteArray();

		try {
			this.saveBitmap(siv.toRoundBitmap(bitmap));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Intent it = new Intent(ImageClipActivity.this,
				UserPersonalInfoActivity.class);
		it.putExtra("bitmap", bitmapByte);
		bitmap.recycle();
		bitmapByte = null;
		this.setResult(7000, it);
		this.finish();
	}

	/**
	 * 保存方法
	 * 
	 * @throws IOException
	 */
	public String saveBitmap(Bitmap bm) throws IOException {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {

		}
		String picname = System.currentTimeMillis() + ".png";
		System.out.println("imagename" + picname);
		
		this.makeRootDirectory(headCachePatch);
		
		File f = new File(headCachePatch+picname);
		if (f.exists()) {
			f.delete();
		}
		FileOutputStream out = null;
		try {
			
			f.createNewFile();
			out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.PNG, 100, out);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(out !=null){
				out.flush();
				out.close();
			}
		}

		return headCachePatch + picname;

	}

	public static void makeRootDirectory(String filePath) {
		File file = null;
		try {
			file = new File(filePath);
			if (!file.exists()) {
				file.mkdir();
			}
		} catch (Exception e) {

		}
	}

}
