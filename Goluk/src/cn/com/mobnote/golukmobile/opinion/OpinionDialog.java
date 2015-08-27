package cn.com.mobnote.golukmobile.opinion;

import cn.com.mobnote.golukmobile.R;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class OpinionDialog extends Dialog implements android.view.View.OnClickListener {
	private TextView yingjian = null;
	private TextView anzhuang = null;
	private TextView tuxiang = null;
	private TextView shouji = null;
	private TextView qita = null;
	private TextView cancle = null;
	private Context mContext = null;
	public static String typeId = "5";

	private OpinionDialogFn mOpinionDialogFn = null;

	public OpinionDialog(Context context,OpinionDialogFn mOpinionDialogFn) {
		super(context, R.style.CustomDialog);
		setContentView(R.layout.user_opinon_select_type);
		mContext = context;
		this.mOpinionDialogFn = mOpinionDialogFn;
		initLayout();
	}

	private void setData(int type) {
		if (null != mOpinionDialogFn) {
			mOpinionDialogFn.showOpinionDialog(type);
		}
	}

	private void initLayout() {
		yingjian = (TextView) findViewById(R.id.yingjian);
		anzhuang = (TextView) findViewById(R.id.anzhuang);
		tuxiang = (TextView) findViewById(R.id.tuxiang);
		shouji = (TextView) findViewById(R.id.shouji);
		qita = (TextView) findViewById(R.id.qita);
		cancle = (TextView) findViewById(R.id.cancle);
		yingjian.setOnClickListener(this);
		anzhuang.setOnClickListener(this);
		tuxiang.setOnClickListener(this);
		shouji.setOnClickListener(this);
		qita.setOnClickListener(this);
		cancle.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.yingjian:
			dismiss();
			setData(OpinionDialogFn.TYPE_FIRST);
			break;
		case R.id.anzhuang:
			dismiss();
			setData(OpinionDialogFn.TYPE_SECOND);
			break;
		case R.id.tuxiang:
			dismiss();
			setData(OpinionDialogFn.TYPE_THIRD);
			break;
		case R.id.shouji:
			dismiss();
			setData(OpinionDialogFn.TYPE_FOUR);
			break;
		case R.id.qita:
			dismiss();
			setData(OpinionDialogFn.TYPE_FIVE);
			break;
		case R.id.cancle:
			dismiss();
			break;
		default:
			break;
		}
	}
	
	public interface OpinionDialogFn {

		public static final int TYPE_FIRST = 1;
		public static final int TYPE_SECOND = 2;
		public static final int TYPE_THIRD = 3;
		public static final int TYPE_FOUR = 4;
		public static final int TYPE_FIVE = 5;
		
		void showOpinionDialog(int type);
		
	}

}
