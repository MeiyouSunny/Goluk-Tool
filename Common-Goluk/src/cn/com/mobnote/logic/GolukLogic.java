package cn.com.mobnote.logic;

import cn.com.mobnote.module.ipcmanager.IPCManagerAdapter;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.page.PageNotifyAdapter;
import cn.com.mobnote.module.talk.ITalkFn;
import cn.com.mobnote.module.talk.TalkNotifyAdapter;
import cn.com.mobnote.module.videosquare.VideoSquareManagerAdapter;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;

public class GolukLogic {

	/** goluk Logic模块指针 */
	private long pLogic;

	public GolukLogic() {
		pLogic = GolukLogicJni.GolukLogicCreate();
	}

	/**
	 * 销毁本模块
	 * 
	 * @author jiayf
	 * @date Mar 26, 2015
	 */
	public void GolukLogicDestroy() {
		GolukLogicJni.GolukLogicDestroy(pLogic);
	}

	/**
	 * 注册回调函数,传入模块标识会注册本模块回调
	 * 
	 * @param mId
	 *            模块标识 (参见GolukModule类)
	 * @return
	 * @author jiayf
	 * @date Mar 26, 2015
	 */
	public int GolukLogicRegisterNotify(int mId, IGolukCommFn fn) {
		registerAdapter(mId, fn);
		return GolukLogicJni.GolukLogicRegisterNotify(pLogic, mId);
	}

	/**
	 * 向自自的Adapter注册回调
	 * 
	 * @param mId
	 *            模块标识
	 * @param fn
	 *            回调函数
	 * @author jiayf
	 * @date Mar 27, 2015
	 */
	private void registerAdapter(int mId, IGolukCommFn fn) {
		// LogUtil.e("", "jyf-------goluk----registerAdapter: " + mId);
		switch (mId) {
		case GolukModule.Goluk_Module_HttpPage:
			PageNotifyAdapter.setNotify((IPageNotifyFn) fn);
			break;
		case GolukModule.Goluk_Module_Talk:
			TalkNotifyAdapter.setNotify((ITalkFn) fn);
			break;
		case GolukModule.Goluk_Module_IPCManager:
			IPCManagerAdapter.setIPcManageListener((IPCManagerFn) fn);
			break;
		case GolukModule.Goluk_Module_Square:
			VideoSquareManagerAdapter.setVideoSuqareListener((VideoSuqareManagerFn) fn);
			break;
		default:
			break;
		}
	}

	/**
	 * 通用请求函数
	 * 
	 * @param mId
	 *            模块ID (参见GolukModule类)
	 * @param cmd
	 *            模块命令
	 * @param param
	 *            调用参数
	 * @return 返回结果
	 * @author jiayf
	 * @date Mar 26, 2015
	 */
	public boolean GolukLogicCommRequest(int mId, int cmd, String param) {
		return GolukLogicJni.GolukLogicCommRequest(pLogic, mId, cmd, param);
	}

	/**
	 * 通用获取函数
	 * 
	 * @param mId
	 *            模块ID (参见GolukModule类)
	 * @param cmd
	 *            模块命令
	 * @param param
	 *            调用参数
	 * @return 返回结果
	 * @author jiayf
	 * @date Mar 26, 2015
	 */
	public String GolukLogicCommGet(int mId, int cmd, String param) {
		return GolukLogicJni.GolukLogicCommGet(pLogic, mId, cmd, param);
	}

}
