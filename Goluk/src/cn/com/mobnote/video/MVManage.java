package cn.com.mobnote.video;

import android.annotation.SuppressLint;
import android.content.Context;
import java.util.ArrayList;

import cn.com.mobnote.golukmobile.R;


/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
 * @ 功能描述:视频编辑页面MV管理类
 * 
 * @author 陈宣宇
 * 
 */

@SuppressLint("SimpleDateFormat")
public class MVManage {
	
	//private Context mContext = null;
	/** 视频存放外卡文件路径 */
	//private static final String APP_FOLDER = "golukvideo";
	//private String mFilePath = android.os.Environment.getExternalStorageDirectory().getPath() + "/" + APP_FOLDER;
	
	public MVManage(Context context){
		//mContext = context;
	}
	
	/**
	 * 获取本地滤镜主题列表
	 * @return
	 */
	public ArrayList<MVEditData> getLocalVideoList(){
		ArrayList<MVEditData> list = new ArrayList<MVEditData>();
		//保存数据
		MVEditData data1 = new MVEditData();
		data1.src = R.drawable.filter_nothing;
		data1.name = "无";
		data1.display = true;
		list.add(data1);
		
		MVEditData data2 = new MVEditData();
		data2.src = R.drawable.filter_qingxin;
		data2.name = "清新淡雅";
		list.add(data2);
		
		MVEditData data3 = new MVEditData();
		data3.src = R.drawable.filter_heibai;
		data3.name = "黑白经典";
		list.add(data3);
		
		MVEditData data4 = new MVEditData();
		data4.src = R.drawable.filter_duocai;
		data4.name = "多彩夏日";
		list.add(data4);
		
		MVEditData data5 = new MVEditData();
		data5.src = R.drawable.filter_fugu;
		data5.name = "复古怀旧";
		list.add(data5);
		
		MVEditData data6 = new MVEditData();
		data6.src = R.drawable.filter_binfen;
		data6.name = "缤纷梦幻";
		list.add(data6);
		
		MVEditData data7 = new MVEditData();
		data7.src = R.drawable.filter_rouhe;
		data7.name = "柔和静谧";
		list.add(data7);
		
		MVEditData data8 = new MVEditData();
		data8.src = R.drawable.filter_gudian;
		data8.name = "古典胶片";
		list.add(data8);
		return list;
	}
	

	public class MVEditData{
		
		//图片路径
		public int src;
		//名称
		public String name;
		//选中标识
		public boolean display=false;
		
	}
}










