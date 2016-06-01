package cn.com.tiros.airtalkee;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.telephony.TelephonyManager;
import cn.com.tiros.api.Const;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.debug.GolukDebugUtils;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;

// 对通用逻辑组开发接口开放接口
public class TaxiAirTalkee extends AbstractTaxiAirTalkee {
	
//	private static final String TAG = TaxiAirTalkee.class.getSimpleName();

	public static int mairTalkeeHandler; // c端句柄

	private TaxiAirTalkee() {
		super();
	}

	@Override
	public void create(int _mairTalkeeHandler, String ip) {
		mairTalkeeHandler = _mairTalkeeHandler;
//		GolukDebugUtils.i(TAG, "caoyp ------- create ");

		TelephonyManager mTelephonyManager = (TelephonyManager)Const.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
		/**
		 * 开启aitalkee 系统api  log日志  
		 * AirTalkeeConfig 函数 添加参数 AirtalkeeAccount.TRACE_MODE_ON_FILE
		 */
		
//		ip = "192.168.1.228";
//		ip = "115.29.232.121";
//		ip = "211.103.234.238";
		GolukDebugUtils.i("123", "wjun=======ip111 = " + ip);
		if(ip != null && !ip.trim().equals("")){
			GolukDebugUtils.i("123", "wjun=======ip");
//			mhandleAccount.AirTalkeeConfig(ip, 4001,  mTelephonyManager.getDeviceId());
			mhandleAccount.AirTalkeeConfig(ip, 4001, "android", mTelephonyManager.getDeviceId(), "1.0.0", AirtalkeeAccount.TRACE_MODE_OFF);
		}else{
			mhandleAccount.AirTalkeeConfig("sns.lbs8.com", 4001, "android",  mTelephonyManager.getDeviceId(), "1.0.0");
		}
	}
	
	@Override
	public void configserver(String spIp, int spPort, int spLocalPort, String mdsrIp, int mdsrPort) {
		if(mhandleAccount == null){
			return;
		}
//		spIp = "115.29.232.121";
//		mdsrIp = "115.29.232.121";
//		spIp = "192.168.1.228";
//		mdsrIp = "192.168.1.228";
		GolukDebugUtils.i("123", "wjun ----configserver----- spIp == " + spIp);
		GolukDebugUtils.i("123", "wjun ----configserver----- spPort == " + spPort);
		GolukDebugUtils.i("123", "wjun ----configserver----- spLocalPort == " + spLocalPort);
		GolukDebugUtils.i("123", "wjun ----configserver----- mdsrIp == " + mdsrIp);
		GolukDebugUtils.i("123", "wjun ----configserver----- mdsrPort == " + mdsrPort);

		mhandleAccount.AirTalkeeConfigServer(spIp, spPort, spLocalPort, mdsrIp, mdsrPort);
	}

	// 登录
	@Override
	public void login(String _userId, String _password) {
		if (mhandleAccount == null) {
			return;
		}
//		GolukDebugUtils.i(TAG, "caoyp ------- login ");
//		_userId = "111010002";
//		_password = "123456";
		GolukDebugUtils.i("123", "wjun ----login----- aid= " + _userId + ",and password = " + _password);
		mhandleAccount.Login(_userId, _password);
	}

	// 退出登录
	@Override
	public void logout() {
		if (mhandleAccount == null) {
			return;
		}
		mhandleAccount.Logout();
	}

	@Override
	public void systemCusromReport(String _updData) {
		if (mhandleAccount == null || _updData == null) {
			return;
		}
		mhandleAccount.SystemCustomReport(_updData);
	}

	@Override
	public void channelOnLineCountGet() {
		mhandleChannel.ChannelOnlineCountGet();
	}

	@Override
	public void channelOnLineCountStart(int _timeSeconds) {
		mhandleChannel.ChannelOnlineCountGetStart(_timeSeconds);
	}

	@Override
	public void channelOnLineCountGetStop() {
		mhandleChannel.ChannelOnlineCountGetStop();
	}

	// 群组管理员　设置某人禁音30分钟
	public void channelSessionManageShutup(String _userid) {
		if (_userid == null || msession == null) {
			return;
		}

		AirContact member = new AirContact();
		member.setIpocId(_userid);

//		mhandleSession.ChannelSessionManageShutup(msession, member);
	}

	// 群组管理员踢出组员 30分钟
	public void channelSessionManageKickout(String _userid) {
		if (_userid == null || msession == null) {
			return;
		}

		AirContact member = new AirContact();
		member.setIpocId(_userid);

//		mhandleSession.ChannelSessionManageKickout(msession, member);
	}

	// 群成员自己设置静音 true为静音 false为非静音
	public void channelSessionSetSilence(boolean isSlient) {
		if (msession == null) {
			return;
		}

//		mhandleSession.ChannelSessionSetSilence(msession, isSlient);
	}

	// 获得指定群组的成员列表
	public void channelMemberGet(String _channelId) {
		if (_channelId == null) {
			return;
		}
		mhandleChannel.ChannelMemberGet(_channelId);
	}

	@Override
	public void sessionCall(boolean isGroup, String _channelId) {
		if (mhandleSession == null) {
			return;
		}
//		_channelId = "C1438";
//		_channelId = "C6927";
//		_channelId = "C3297";
//		_channelId = "C1439";
		GolukDebugUtils.i("123", "wjun ----sessionCall----- _channelId= " + _channelId);
		if (isGroup) {
			msession = mhandleSession.SessionCall(_channelId);
		} else {
			mCallMemberList.clear();
			AirContact member = new AirContact();
			member.setIpocId(_channelId);
			mCallMemberList.add(member);
			AirSession sessionTemp = mhandleSession.SessionCall(mCallMemberList);
			if (sessionTemp == null) {// 呼叫失败
			}
		}
		if (msession == null)
		{
			GolukDebugUtils.i("123", "wjun ----sessionCall----- Sesion call error! ");
		}
		else
			GolukDebugUtils.i("123", "wjun ----sessionCall----- Sesioncall ok! ");
	}

	@Override
	public void sessionBye() {
		GolukDebugUtils.i("123", "wjun ----sessionBye----- ");
		if (msession == null || mhandleSession == null) {
			return;
		}

		mhandleSession.SessionBye(msession);
	}

	@Override
	public void sessionIncomingAccept() {
		GolukDebugUtils.i("123", "wjun ----sessionIncomingAccept----- ");
		if (msession != null) {
			mhandleSession.SessionIncomingAccept(msession);
		}
	}
	
	@Override
	public void sessionIncomingReject() {
		GolukDebugUtils.i("123", "wjun ----sessionIncomingReject----- ");
		if (msession != null) {
			mhandleSession.SessionIncomingReject(msession);
		}
	}

	@Override
	public void talkRequest() {
		GolukDebugUtils.i("123", "wjun ----talkRequest----- ");
		if (msession != null) {
			mhandleSession.TalkRequest(msession);
		}
	}

	@Override
	public void talkRelease() {
		GolukDebugUtils.i("123", "wjun ----talkRelease----- ");
		if (msession != null) {
			mhandleSession.TalkRelease(msession);
		}
	}

	@Override
	public String messageSend1(String _messageBody, boolean isCustom, boolean allowOfflineSend) {
		if (msession != null) {
			AirMessage  aim = mhandleMessage.MessageSend(msession, _messageBody, isCustom, allowOfflineSend);
			return aim.getMessageCode();
		}
		return null;
	}
	
	@Override
	public String messageSend2(String aidlist, String messageBody, boolean isCustom, boolean allowOfflineSend) {
		AirMessage  aim = mhandleMessage.MessageSend(MemberListJson(aidlist), messageBody, isCustom, allowOfflineSend);
		return aim.getMessageCode();
	}

	@Override
	public String messageSend3(String aid, String messageBody, boolean isCustom, boolean allowOfflineSend) {
			AirContact airc = new AirContact();
			airc.setIpocId(aid);
			AirMessage  aim = mhandleMessage.MessageSend(airc, messageBody, isCustom, allowOfflineSend);
			return aim.getMessageCode();
	}

	@Override
	public String messageSend4(String groupid, String messageBody, boolean isCustom, boolean allowOfflineSend) {
			GolukDebugUtils.i("123", "wjun--------messageSend4: groupid = " + groupid + ",messageBody = " + messageBody);
			isCustom = true;
			AirMessage aim = mhandleMessage.MessageSend(groupid, messageBody, isCustom, allowOfflineSend);
			return aim.getMessageCode();
	}

	@Override
	public void setMessageRecordPath(String path) {
		mhandleMessage.setMessageRecordPath(FileUtils.libToJavaPath(path));
	}

	@Override
	public void messageRecordStart1(boolean allowOfflineSend) {
		mhandleMessage.MessageRecordStart(msession, allowOfflineSend);
	}

	@Override
	public void messageRecordStart2(String aidlist, boolean allowOfflineSend) {
		mhandleMessage.MessageRecordStart(MemberListJson(aidlist), allowOfflineSend);		
	}

	@Override
	public void messageRecordStart3(String aid, boolean allowOfflineSend) {
		AirContact airc = new AirContact();
		airc.setIpocId(aid);
		mhandleMessage.MessageRecordStart(airc, allowOfflineSend);
	}

	@Override
	public void messageRecordStart4(String groupid, boolean allowOfflineSend) {
		mhandleMessage.MessageRecordStart(groupid, allowOfflineSend);
	}

	@Override
	public void messageRecordStop(boolean iscancel) {
		mhandleMessage.MessageRecordStop(iscancel);
	}

	@Override
	public String messageRecordResend1(String aidlist, String msgid, String resid, int time, boolean allowOfflineSend) {
		String msgID = mhandleMessage.MessageRecordResend(MemberListJson(aidlist), msgid, resid, time, allowOfflineSend);
		return msgID;
	}

	@Override
	public String messageRecordResend2(String aid, String msgid, String resid, int time, boolean allowOfflineSend) {
		AirContact airc = new AirContact();
		airc.setIpocId(aid);
		String msgID = mhandleMessage.MessageRecordResend(airc, msgid, resid, time, allowOfflineSend);
		return msgID;
	}

	@Override
	public String messageRecordResend3(String groupid, String msgid, String resid, int time, boolean allowOfflineSend) {
		String msgID = mhandleMessage.MessageRecordResend(groupid, msgid, resid, time, allowOfflineSend);
		return msgID;
	}

	@Override
	public void messageRecordPlayStart(String msgid, String resid) {
		mhandleMessage.MessageRecordPlayStart(msgid, resid);
	}

	@Override
	public void messageRecordPlayStop() {
		mhandleMessage.MessageRecordPlayStop();
	}
	
	@Override
	public void messageRecordFileDel(String resid) {
		if (msession != null) {
			mhandleMessage.RecordFileClean(resid);
		}
	}
	
	@Override
	public void MessageRecordPlayDownload(String msgid, String resid) {
		mhandleMessage.MessageRecordPlayDownload(msgid, resid);
	}

	@Override
	public void netWorkOpen() {
		mhandleAccount.NetworkOpen();
	}

	@Override
	public void netWorkClose() {
		mhandleAccount.NetworkClose();

	}

	@Override
	public void AirTalkeeEvent(int statecode, String json, int param1, int param2) {
		sys_AirTalkeeEvent(mairTalkeeHandler, statecode, json, param1, param2);
	}

	@Override
	public void destroy() {
//		if (msession != null) {
//			mhandleSession.SessionBye(msession);
//		}
		unregisterAirltalkListener();
		mhandleAccount.Logout();
	}

	// 向通用Logic发送消息
	public static native void sys_AirTalkeeEvent(int http, int statecode, String json, int param1, int param2);

	
	private List<AirContact> MemberListJson(String jsonmembers) {
		JSONArray jsonarray = null;
		try {
			jsonarray = new JSONArray(jsonmembers);
		} catch (JSONException e1) {
			return null;
		}
		
		int nsize = jsonarray.length();
		List<AirContact> list = null;
		if(nsize > 0){
			list = new ArrayList<AirContact>();
		}else{
			return null;
		}
		for (int i = 0; i < nsize; i++) {
			AirContact airc = new AirContact();
			try {
				airc.setIpocId(jsonarray.getString(i));
				list.add(airc);
			} catch (JSONException e) {}
			airc = null;
		}
		return list;
	}

	@Override
	public void onContactOperationAdd(boolean arg0, AirContact arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onContactOperationDel(boolean arg0, AirContact arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onContactPush(AirContact arg0, AirMessage arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onContactSearch(boolean arg0, List<AirContact> arg1, int arg2,
			boolean arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessageUpdated(AirMessage arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMediaStateListenVoice(AirSession arg0) {
		// TODO Auto-generated method stub
		
	}

}
