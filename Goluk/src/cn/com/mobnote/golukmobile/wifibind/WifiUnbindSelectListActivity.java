package cn.com.mobnote.golukmobile.wifibind;
import java.util.List;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.wifibind.WifiUnbindSelectListAdapter.HeadViewHodler;
import cn.com.mobnote.golukmobile.wifidatacenter.WifiBindDataCenter;
import cn.com.mobnote.golukmobile.wifidatacenter.WifiBindHistoryBean;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
public class WifiUnbindSelectListActivity extends BaseActivity implements OnClickListener {

	/** 关闭按钮 **/
	private ImageView mCloseBtn;

	/** 数据列表 **/
	private ListView mListView;

	/** 没有数据时的默认布局 **/
	private RelativeLayout mEmptyLayout;

	/** 编辑按钮 **/
	private Button mEditBtn;
	
	/**连接中headView**/
	public View mHeadView = null;
	
	public HeadViewHodler  mHeadData = null;
	

	private WifiUnbindSelectListAdapter mListAdapter;

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
	private void initView() {
		mListView = (ListView) findViewById(R.id.listView);
		mEmptyLayout = (RelativeLayout) findViewById(R.id.emptyLayout);
		mCloseBtn = (ImageView) findViewById(R.id.close_btn);
		mEditBtn = (Button) findViewById(R.id.edit_btn);

		findViewById(R.id.addMoblieBtn).setOnClickListener(this);
	}

	/**
	 * 初始化view的监听
	 */
	private void initLisenner() {
		mCloseBtn.setOnClickListener(this);
		mEditBtn.setOnClickListener(this);
	}
	
	/**初始化数据**/
	private void initData(){
		mListView.setEmptyView(mEmptyLayout);
		mListAdapter = new WifiUnbindSelectListAdapter(this);
		mListView.setAdapter(mListAdapter);
		getBindHistoryData();
		
	}
	
	/**
	 * 获取最新的bind数据
	 * @return
	 */
	public void getBindHistoryData(){
		List<WifiBindHistoryBean> binds = WifiBindDataCenter.getInstance().getAllBindData();
		if(binds != null){
			for (int i = 0; i < binds.size(); i++) {
				WifiBindHistoryBean bind = binds.get(i);
				if(bind.state == WifiBindHistoryBean.CONN_USE){
					if(mHeadView == null){
						mHeadView = LayoutInflater.from(this).inflate(R.layout.unbind_connection_head,null);
						addListViewHead(mHeadView);
					}
					
					if(mHeadData == null){
						mHeadData = new HeadViewHodler();
						mHeadData.connHeadIcon = (ImageView) mHeadView.findViewById(R.id.conn_head_icon);
						mHeadData.connTxt = (TextView) mHeadView.findViewById(R.id.conn_txt);
						mHeadData.golukDelIcon =(ImageView) mHeadView.findViewById(R.id.goluk_del_icon);
						mHeadData.golukIcon  = (ImageView) mHeadView.findViewById(R.id.goluk_icon);
						mHeadData.golukName = (TextView) mHeadView.findViewById(R.id.goluk_name);
					}
					
					mHeadData.golukName.setText(bind.ipc_ssid);
				}
			}
			mListAdapter.setData(binds);
			mListAdapter.notifyDataSetChanged();
		}
	}


	/**
	 * 添加头部
	 * 
	 * @param view
	 */
	public void addListViewHead(View view) {
		mListView.addHeaderView(view);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.close_btn:
			this.finish();
			break;
		case R.id.edit_btn:
			if (mListAdapter.mEditState) {
				mListAdapter.mEditState = false;
				mEditBtn.setText(this.getResources().getString(R.string.edit_text));// 编辑
			} else {
				mListAdapter.mEditState = true;
				mEditBtn.setText(this.getResources().getString(R.string.user_personal_title_right));// 保存
			}
			mListAdapter.notifyDataSetChanged();
			break;
		case R.id.addMoblieBtn:
			click_AddIpc();
			break;
		default:
			break;
		}
	}

	private void click_AddIpc() {
		Intent intent = new Intent(this, WifiUnbindSelectTypeActivity.class);
		startActivity(intent);
	}

}
