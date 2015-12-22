package cn.com.mobnote.golukmobile.wifibind;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class WifiUnbindSelectListActivity extends BaseActivity implements OnClickListener {
	
	/**关闭按钮**/
	private ImageView mCloseBtn;
	
	/**数据列表**/
	private ListView mListView;
	
	/**没有数据时的默认布局**/
	private RelativeLayout mEmptyLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unbind_connection_list);
		
		initView();
		initLisenner();
		initData();
	}
	
	/**
	 * 初始化view
	 */
	private void initView(){
		mListView = (ListView) findViewById(R.id.listView);
		mEmptyLayout = (RelativeLayout) findViewById(R.id.emptyLayout);
		mCloseBtn = (ImageView) findViewById(R.id.close_btn);
	}
	
	/**
	 * 初始化view的监听
	 */
	private void initLisenner(){
		mCloseBtn.setOnClickListener(this);
	}
	
	/**初始化数据**/
	private void initData(){
		mListView.setEmptyView(mEmptyLayout);
	}


	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.close_btn:
			this.finish();
			break;
		default:
			break;
		}
	}

}
