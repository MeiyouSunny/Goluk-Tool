package cn.com.tiros.airtalkee;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.tiros.debug.GolukDebugUtils;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeContact;
import com.airtalkee.sdk.AirtalkeeMediaAudioControl;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactTiny;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;



public abstract class AbstractTaxiAirTalkee implements TaxiAirTalkeeFn {

//	private static final String TAG = AbstractTaxiAirTalkee.class.getSimpleName();

	/** 爱淘客的语音对讲的对象 */
	protected AirtalkeeSessionManager mhandleSession = null;
	protected AirtalkeeContact mhandleContact = null;
	protected AirtalkeeChannel mhandleChannel = null;
	protected AirtalkeeAccount mhandleAccount = null;
	protected AirtalkeeMessage mhandleMessage = null;
	/** 会话类的对象 */
	protected AirSession msession;
	/** 音频控制接口对象 */
	protected AirtalkeeMediaAudioControl mhandleMediaAudioControl = null;

	// 私聊的呼叫列表
	List<AirContact> mCallMemberList = new ArrayList<AirContact>();

	protected AbstractTaxiAirTalkee() {
		initAirltalkListener();
	}

	private void initAirltalkListener() {
		
		mhandleSession = AirtalkeeSessionManager.getInstance();
		mhandleContact = AirtalkeeContact.getInstance();
		mhandleChannel = AirtalkeeChannel.getInstance();
		mhandleMessage = AirtalkeeMessage.getInstance();
		mhandleAccount = AirtalkeeAccount.getInstance();

		mhandleSession.setOnSessionListener(this);
		mhandleSession.setOnMediaListener(this);
		mhandleSession.setOnMediaWaveListener(this);
		mhandleSession.setOnSessionIncomingListener(this);
		mhandleContact.setContactListener(this);
		mhandleAccount.setOnAccountListener(this);
		mhandleMessage.setOnMessageListener(this);
		mhandleAccount.setOnSystemListener(this);
		mhandleChannel.setOnChannelListListener(this);
//		mhandleSession.setOnMediaChannelListener(this);
//		mhandleMediaAudioControl.setOnMediaAudioControlRecordListener(this);
	}
	
	public void unregisterAirltalkListener(){
		mhandleSession.setOnSessionListener(null);
		mhandleSession.setOnMediaListener(null);
		mhandleSession.setOnMediaWaveListener(null);
		mhandleSession.setOnSessionIncomingListener(null);
		mhandleContact.setContactListener(null);
		mhandleAccount.setOnAccountListener(null);
		mhandleMessage.setOnMessageListener(null);
		mhandleAccount.setOnSystemListener(null);
		mhandleChannel.setOnChannelListListener(null);
//		mhandleSession.setOnMediaChannelListener(null);
	}

	/**
	 * -----------------------------帐户相关的回调------------------------------------
	 * ------------------------------------------------------------------------
	 * */

	/**
	 * @Description：登录的回调消息
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-14
	 */
	@Override
	public void onLogin(int result) {
		String json = JsonState(result);	
//		GolukDebugUtils.i(TAG, "caoyp ------- onLogin   json == " + json);
		AirTalkeeEvent(AIRTALKEE_LOGIN, json, 0, 0);
	}

	// 注销回调消息
	@Override
	public void onLogout() {
		AirTalkeeEvent(AIRTALKEE_LOGOUT, null, 0, 0);
	}

	/**
	 * @Description：在运行过程中,网络变化引起的或与服务器实时连接引起的异常变化,会自动产生此事件通知
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-14
	 */
	@Override
	public void onHeartbeat(int result) {
		String json = JsonState(result);
		AirTalkeeEvent(AIRTALKEE_HEART, json, 0, 0);
	}

	@Override
	public void onAccountMatch(int arg0, String arg1) {

	}

	/**
	 * -----------------------------群组相关的回调------------------------------------
	 * -- ----------------------------------------------------------------------
	 * -- --
	 * */
	// 返回当前用户所属的群组
	@Override
	public void onChannelListGet(boolean isOk, List<AirChannel> channels) {
	}
	
    // 群组成员列表的返回
	@Override
	public void onChannelMemberListGet(String channelId, List<AirContact> members) {
		GolukDebugUtils.i("123", "wjun ----onChannelMemberListGet----- ");
		if (msession != null && channelId.equals(msession.getSessionCode())) {
			if (members != null && members.size() > 0) {
				String json = MemberListJson(true, members);
				AirTalkeeEvent(AIRTALKEE_MEMBERLIST, json, 0, 0);
			} else {
				String jsontemp = MemberListJson(false, null);
				AirTalkeeEvent(AIRTALKEE_MEMBERLIST, jsontemp, 0, 0);
			}
		}
	}

	/**
	 * @Description：返回所有群组在线人数的列表
	 * @param online
	 *            ：online中 String为群组ID，Integer为在线人数
	 * @author：孙小庆
	 * @Date：2013-1-14
	 */
	@Override
	public void onChannelOnlineCount(LinkedHashMap<String, Integer> online) {
		GolukDebugUtils.i("123", "wjun ----onChannelOnlineCount----- ");
		if (online != null && online.size() > 0) {
			String json = AllGroupOnlineCountJson(online);	
			AirTalkeeEvent(AIRTALKEE_ONLINECOUNT, json, 0, 0);
		}
	}

	@Override
	public void onChannelPersonalCreateNotify(AirChannel arg0) {
		GolukDebugUtils.i("123", "wjun ----onChannelPersonalCreateNotify----- ");
	}

	@Override
	public void onChannelPersonalDeleteNotify(AirChannel arg0) {
		GolukDebugUtils.i("123", "wjun ----onChannelPersonalDeleteNotify----- ");
	}

//	// 踢出
//	@Override
//	public void onChannelSessionManagerKickout(AirSession _session, boolean isMe, AirContactTiny userShutup) {
//		// TODO Auto-generated method stub
//		GolukDebugUtils.i("123", "wjun ----onChannelSessionManagerKickout----- isMe = " + isMe + ", userShutup = " + userShutup.getIpocId());		
//		String json = SessionManagerKickout(_session.getSessionCode(), userShutup, isMe);
//		AirTalkeeEvent(AIRTALKEE_ESTABLISH_STATUS, json, 0, 0);
//	}

	/**
	 * -----------------------------临时会话来电提醒相关的回调------------------------------
	 * ------------------------------------------------------------------------
	 * */

	/**
	 * @Description：临时会话来电提醒开始事件
	 * 
	 * @param session
	 *            ：会话实例；
	 * @param caller
	 *            ：主叫人信息；
	 * @param isAccepted
	 *            ：是否已自动接听(当用户设置为自动应答时有效)
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-14
	 */
	@Override
	public void onSessionIncomingAlertStart(AirSession session, AirContact caller, boolean isAccepted) {
		GolukDebugUtils.i("123", "wjun ----onSessionIncomingAlertStart----- ");
		if (session != null) {
			msession = session;
			String json = IncomingAlertStartJson(caller.getIpocId());	
			AirTalkeeEvent(AIRTALKEE_ALERTSTART, json, 0, 0);
		}

	}

	/**
	 * @Description：临时会话来电提醒结束事件
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-14
	 */
	@Override
	public void onSessionIncomingAlertStop(AirSession session) {
		GolukDebugUtils.i("123", "wjun ----onSessionIncomingAlertStop----- ");
		AirTalkeeEvent(AIRTALKEE_ALERTSTOP, null, 0, 0);
	}

	/**
	 * -----------------------------会话相关的回调--------------------------
	 * ------------------------------------------------------------------------
	 * */

	/**
	 * @Description：临时会话主叫呼叫对方,当对方来电振铃时,此时主叫方同时会触发主叫振铃,已表明呼叫正在等待接听
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-13
	 */
	@Override
	public void onSessionOutgoingRinging(AirSession session) {
		GolukDebugUtils.i("123", "wjun ----onSessionOutgoingRinging----- ");

	}

	/**
	 * @Description：临时或群组会话呼出开始建立事件,调用SessionCall后返回此事件
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-13
	 */
	@Override
	public void onSessionEstablishing(AirSession session) {
		GolukDebugUtils.i("123", "wjun ----onSessionEstablishing-----1111");
		if (msession != null) {
			String json = null;
			json = SessionEstablishingJson(session.getSessionCode());
			AirTalkeeEvent(AIRTALKEE_ESTABLISH_STATUS, json, 0, 0);
		}

	}

	/**
	 * @Description：来电或去电呼叫、或群组会话建立成功后返回此事件
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-13
	 */
	@Override
	public void onSessionEstablished(AirSession session, boolean isOk){

		GolukDebugUtils.i("123", "wjun ----onSessionEstablished-----isOK = " + isOk);
		String json = null;
		json = SessionEstablishedJson(isOk, session.getSessionCode());
		AirTalkeeEvent(AIRTALKEE_ESTABLISH_STATUS, json, 0, 0);

		// 会话链接失败
		if (!isOk || session == null) {
			return;
		}

		msession = session;

//		String sessionjson = JsonState(msession.getSessionState());
//		AirTalkeeEvent(AIRTALKEE_RINGING, sessionjson, 0, 0);
//		String mediajson = MediaStateJson();
//		AirTalkeeEvent(AIRTALKEE_OTHERTALKSTART, mediajson, 0, 0);

	}

	/**
	 * @Description：临时或群组会话结束事件
	 * 
	 * @author：孙小庆
	 * @Date：2012-12-12
	 */
	@Override
	public void onSessionReleased(AirSession session, int reason) {
		GolukDebugUtils.i("123", "wjun ----onSessionReleased-----reason = " + reason);
		String sessionjson = SessionReleasedJson(session.getSessionCode(), reason);
		AirTalkeeEvent(AIRTALKEE_ESTABLISH_STATUS, sessionjson, 0, 0);

	}

	/**
	 * @Description：会过程中,动态变化的用户参与此会话情况,均会通过此事件通知
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-13
	 */
	@Override
	public void onSessionPresence(AirSession session, List<AirContact> membersAll, List<AirContact> membersPresence) {
		if (session != null) {
			msession = session;
			int membercount = membersAll != null ? membersAll.size() : 0;
			int onlinecount = membersPresence != null ? membersPresence.size() : 0;

			String json = OnlineAndMemberJson(onlinecount, membercount, membersPresence);
			GolukDebugUtils.i("123", "wjun=====onSessionPresence json =  " + json);
			AirTalkeeEvent(AIRTALKEE_PRESENCE, json, 0, 0);
		}
	}

	@Override
	public void onSessionMemberUpdate(AirSession arg0, List<AirContact> arg1, boolean arg2) {

	}

	/**
	 * -----------------------------会话中的话语权相关的回调--------------------------
	 * ------------------------------------------------------------------------
	 * */
	
	/**
	 * @Description：本人申请话语权排队
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-14
	 */
	@Override
	public void onMediaQueue(AirSession arg0, ArrayList<AirContact> arg1) {
		GolukDebugUtils.i("123", "wjun ----排队onMediaQueue-------------------- ");
		boolean bHaveMe = false;
		//判断排队队列中是否包含自己
		for (int i = 0; i < arg1.size(); i++) 
		{
			String aid = arg1.get(i).getIpocId();
			if (aid.equals(mhandleAccount.getUser().getIpocId()))
			{
				bHaveMe = true;
				break;
			}
		}
		if (bHaveMe)
		{
			JSONObject jsonobject = new JSONObject();
			try {
				jsonobject.put(JSON_STATE, msession.getMediaState());
				jsonobject.put(JSON_AID, mhandleAccount.getUser().getIpocId());
				String json = jsonobject.toString();
				GolukDebugUtils.i("123", "wjun ----排队onMediaQueue------包含我自己json = " + json);
				AirTalkeeEvent(AIRTALKEE_TALKQUEUE, json, 0, 0);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @Description：别人开始讲话
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-14
	 */
	@Override
	public void onMediaStateListen(AirSession arg0, AirContact arg1) {
		GolukDebugUtils.i("123", "wjun ----别人开始讲话onMediaStateListen-------------------- ");
		String json = MediaStateJson();
		AirTalkeeEvent(AIRTALKEE_OTHERTALKSTART, json, 0, 0);
	}

	/**
	 * @Description：别人讲话结束
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-14
	 */
	@Override
	public void onMediaStateListenEnd(AirSession arg0) {
		GolukDebugUtils.i("123", "wjun ----别人讲话结束onMediaStateListenEnd-------------------- ");
		String json = MediaStateJson();
		AirTalkeeEvent(AIRTALKEE_OTHERTALKEND, json, 0, 0);
	}

	/**
	 * @Description：本人收到话语权，开始讲话
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-14
	 */
	@Override
	public void onMediaStateTalk(AirSession arg0) {
		GolukDebugUtils.i("123", "wjun ----本人收到话语权，开始讲话onMediaStateTalk-------------------- ");
		String json = MediaStateJson();
		AirTalkeeEvent(AIRTALKEE_TALKSTART, json, 0, 0);

	}

	/**
	 * @Description：本人释放话语权，结束讲话
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-14
	 */
	@Override
	public void onMediaStateTalkEnd(AirSession arg0, int arg1) {
		GolukDebugUtils.i("123", "wjun ----本人释放话语权，结束讲话onMediaStateTalkEnd---------------arg1 = " + arg1);
		String json = MediaStateJson();
		switch (arg1){
		case AirtalkeeSessionManager.TALK_FINISH_REASON_RELEASED:
			{//用户主动释放话语权
				AirTalkeeEvent(AIRTALKEE_TALKEND, json, 0, 0);
			}break;
		case AirtalkeeSessionManager.TALK_FINISH_REASON_GRABED:
			{//话语权被更高级别的用户抢断而终止
				AirTalkeeEvent(AIRTALKEE_TALKEND_REASON_GRABED, json, 0, 0);
			}break;
		case AirtalkeeSessionManager.TALK_FINISH_REASON_TIMEOUT:
			{//申请话语权超时,未申请上话语权而结束
				AirTalkeeEvent(AIRTALKEE_TALKFAILD, json, 0, 0);
			}break;
		case AirtalkeeSessionManager.TALK_FINISH_REASON_TIMEUP:
			{//本人发言时长已到,服务器自动终止用户的发言
				AirTalkeeEvent(AIRTALKEE_TALKEND_REASON_TIMEUP, json, 0, 0);
			}break;
		case AirtalkeeSessionManager.TALK_FINISH_REASON_EXCEPTION:
			{//由于用户网络原因,连续间隔一定时间服务器依然未能收到用户的通话数据包,而产生的异常,从而自 动收回用户的话语权
				AirTalkeeEvent(AIRTALKEE_TALKEND_REASON_EXCEPTION, json, 0, 0);
			}break;
		case AirtalkeeSessionManager.TALK_FINISH_REASON_LISTEN_ONLY:
			{//用户仅有只听权限,不允许发言
				AirTalkeeEvent(AIRTALKEE_CHANNELSESSIONSHUTUP, json, 0, 0);
			}break;
		case AirtalkeeSessionManager.TALK_FINISH_REASON_SPEAKING_FULL:
			{//允许讲话人数或排队人数已达上限。当频道内不允许排队时,别人在讲话,本人申请话语权会收到 此事件;当频道内允许排队时,一旦达到排队最大人数再申请话语权,也 会收到此事件
				AirTalkeeEvent(AIRTALKEE_TALKFAILD, json, 0, 0);
			}break;
		default:
			{
			
			}break;
		}

	}
	
	@Override
	public void onMediaQueueIn(AirSession arg0) {
		//当用户在允许排队的频道内申请话语权，同时当前有人正占用话语权时，服务器允许用户进入排队状态，会返回此事件
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_STATE, msession.getMediaState());
			jsonobject.put(JSON_AID, mhandleAccount.getUser().getIpocId());
			String json = jsonobject.toString();
			GolukDebugUtils.i("123", "wjun ----排队onMediaQueue------包含我自己json = " + json);
			AirTalkeeEvent(AIRTALKEE_TALKQUEUE, json, 0, 0);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onMediaQueueOut(AirSession arg0) {
		//当用户已在排队队列中，用户取消话语权后会返回此事件
		
	}

	@Override
	public void onMediaStateTalkPreparing(AirSession arg0) {
		//本人申请话语权时会第一个回调此事件,可用于 UI 对界面进行状态刷新
		
	}
	
	/**
	 * --------------------------接管音频录制时爱淘客sdk的事件回调--------------
	 * -----------------------------------------------------------------
	 * */
	//第三方开始录制音频数据
	@Override
	public void onMediaAudioControlRecordStart(int arg0) {
		// TODO Auto-generated method stub
		
	}

	//第三方结束音频录制
	@Override
	public void onMediaAudioControlRecordStop(int arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * --------------------------说话声音大小回调--------------
	 * -----------------------------------------------------------------
	 * */

	/**
	 * @Description：在录音和播放时返回音频强度的事件
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-14
	 */
	@Override
	public void onMediaAudioWaveChanged(int waveValue) {
		GolukDebugUtils.i("123", "wjun ----onMediaAudioWaveChanged----- ");
		String json = valumeJson(waveValue);
		AirTalkeeEvent(AIRTALKEE_VALUMECHANGE, json, 0, 0);

	}

	/**
	 * --------------------------用户发送消息接回调--------------
	 * -----------------------------------------------------------------
	 * */

	/**
	 * @Description：收到即时消息事件
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-14
	 */
	@Override
	public void onMessageIncomingRecv(boolean isCustom, AirMessage msg) {
		String msgid = msg.getMessageCode();
		int msgtype = msg.getType();
		//2013年06月20日
		
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DateFormat formatter1 = new SimpleDateFormat("yyyyMMddHHmmss");
		Date dates = null;
		try {
			dates = formatter.parse(msg.getDate() + " " + msg.getTime());
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		String date = formatter1.format(dates); // date : 20130306 time
		String aid = msg.getIpocidFrom();
		String groupid = msg.getSessionCode();
		String message = null;

		String josn = null;
		if (msgtype == AirMessage.TYPE_CUSTOM || msgtype == AirMessage.TYPE_TEXT) {
			// 自定义或者文本
			message = msg.getBody();

		} else {
			String resid = msg.getImageUri();
			int time = msg.getImageLength();
			message = messageIncomingRecvMessageJosn(resid, time);
			mhandleMessage.MessageRecordPlayDownload(msg);
		}
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_MSGID, msgid);
			jsonobject.put(JSON_MSGTYPE, msgtype);
			jsonobject.put(JSON_DATE, date);
			jsonobject.put(JSON_AID, aid);
			jsonobject.put(JSON_MSG, message);
			jsonobject.put(JSON_GROUPID, groupid);
			josn = jsonobject.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		GolukDebugUtils.i("123", "wjun------>onMessageIncomingRecv msg = " + josn);
		AirTalkeeEvent(AIRTALKEE_MESSAGEERC, josn, 0, 0);

	}
	
	@Override
	public void onMessageIncomingRecv(List<AirMessage> listmsg) {
		
		String msgid = null; //msg.getMessageCode();
		int msgtype = 0; // msg.getType();

		String date = null;// msg.getDate().replaceAll("-", "") + msg.getTime().replaceAll(":", ""); // date : 20130306 time
														// :095300
		String aid = null ; //msg.getIpocidFrom();
		String groupid = null; //msg.getSessionCode();
		String message = null;

		JSONArray jsonArray = new JSONArray();
		
		for(int i=0; i<listmsg.size(); i++){
			JSONObject jsonobject = new JSONObject();
			AirMessage msg = listmsg.get(i);
			
			msgid = msg.getMessageCode();
			msgtype =  msg.getType();
			
			DateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
			DateFormat formatter1 = new SimpleDateFormat("yyyyMMddHHmmss");
			Date dates = null;
			try {
				dates = formatter.parse(msg.getDate() + " " + msg.getTime());
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			date = formatter1.format(dates); // date : 20130306 time  :095300
			aid = msg.getIpocidFrom();
			groupid = msg.getSessionCode();
			
			if (msgtype == AirMessage.TYPE_CUSTOM || msgtype == AirMessage.TYPE_TEXT) {
				// 自定义或者文本
				message = msg.getBody();

			} else {
				String resid = msg.getImageUri();
				int time = msg.getImageLength();
				message = messageIncomingRecvMessageJosn(resid, time);
				mhandleMessage.MessageRecordPlayDownload(msg);
			}
			
			try {
				jsonobject.put(JSON_MSGID, msgid);
				jsonobject.put(JSON_MSGTYPE, msgtype);
				jsonobject.put(JSON_DATE, date);
				jsonobject.put(JSON_AID, aid);
				jsonobject.put(JSON_MSG, message);
				jsonobject.put(JSON_GROUPID, groupid);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			jsonArray.put(jsonobject);
		}
		GolukDebugUtils.i("123", "wjun------>onMessageIncomingRecv AIRTALKEE_OFFLINEMESSAGE msg = " + jsonArray.toString());
		AirTalkeeEvent(AIRTALKEE_OFFLINEMESSAGE, jsonArray.toString(), 0, 0);

	}
	
	public String messageIncomingRecvMessageJosn(String resid, int time){
		
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_RESID, resid);
			jsonobject.put(JSON_TIME, time);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonobject.toString();
	}

	/**
	 * @Description：发送即时消息结果事件
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-14
	 */
	@Override
	public void onMessageOutgoingSent(boolean isCustom, AirMessage message, boolean isSent) {
		if (message.getState() == AirMessage.STATE_RESULT_OK) {
			GolukDebugUtils.i("123", "onMessageOutgoingSent----msgcode = " + message.getMessageCode() + "   OK");
			String json = messageSentTypeJson(true, message.getMessageCode(),message.getType(),message.getImageUri());
			AirTalkeeEvent(AIRTALKEE_MESSAGESENTTYPE, json, 0, 0);
		}else if(message.getState() == AirMessage.STATE_RESULT_FAIL){
			GolukDebugUtils.i("123", "onMessageOutgoingSent----msgcode = " + message.getMessageCode() + "   Fail");
			String json = messageSentTypeJson(false, message.getMessageCode(),message.getType(),message.getImageUri());
			AirTalkeeEvent(AIRTALKEE_MESSAGESENTTYPE, json, 0, 0);
		}
	}

	/**
	 * --------------------------第三方推送消息回调--------------
	 * -----------------------------------------------------------------
	 * */

	/**
	 * @Description：收到系统推送信息的事件
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-14
	 */
	@Override
	public void onSystemCustomPush(String content) {
		AirTalkeeEvent(AIRTALKEE_SYSTEMCUSTOMPUSH, content, 0, 0);
	}
	
	/**
	 * @Description：收到系统推送多条信息的事件
	 * 
	 * @author：曹颖鹏
	 * @Date：2013-4-12
	 */
	@Override
	public void onSystemCustomPush(String[] content_list) {
		JSONArray jsonArray = new JSONArray();
		for(int i=0; i< content_list.length; i++){
			try {
				JSONObject json = new JSONObject(content_list[i]);
				jsonArray.put(json);
			} catch (JSONException e) {}
			
		}
		AirTalkeeEvent(AIRTALKEE_SYSTEMCUSTONPUSHLIST, jsonArray.toString(), 0, 0);
	}

	/**
	 * ----------------------用户联系人发生变化回调接口-----------------------
	 * -----------------------------------------------------------------
	 * */

//	@Override
//	public void onContactPresence() {
//
//	}
	
	@Override
	public void onMessageRecordPlayLoading(String msgid, String resid) {
		//语音消息播放开始加载数据的事件
		AirTalkeeEvent(AIRTALKEE_AUDIOPLAY, messageRecordOrPlayJson(0, msgid, resid), 0, 0);		
	}

	@Override
	public void onMessageRecordPlayStart(String msgid, String resid) {
		//语音消息开始播放的事件
		AirTalkeeEvent(AIRTALKEE_AUDIOPLAY, messageRecordOrPlayJson(1, msgid, resid), 0, 0);
	}

	@Override
	public void onMessageRecordPlayStop(String msgid, String resid) {
		//语音消息停止播放事件
		AirTalkeeEvent(AIRTALKEE_AUDIOPLAY, messageRecordOrPlayJson(2, msgid, resid), 0, 0);

	}

	@Override
	public void onMessageRecordStart() {
		//语音消息开始录音的事件
		AirTalkeeEvent(AIRTALKEE_RECORD, JsonState(0), 0, 0);
	}

	@Override
	public void onMessageRecordStop(int seconds, String msgCode) {
		//语音消息停止录音的事件
		//如果录音成功则返回录音的秒数
		  //其它的可能返回值： -2：未满足最小秒数（1秒以上） -3：录音被取消
		  //特别说明：录音时间长度设定为最小1秒钟，最大60秒钟
		// msgCode 语音消息的code
		AirTalkeeEvent(AIRTALKEE_RECORD, messageRecordStopJson(1, seconds, msgCode), 0, 0);
	}

	@Override
	public void onMessageRecordTransfered(String msgid, String resid) {
		//语音消息语音数据传送完成的事件
		AirTalkeeEvent(AIRTALKEE_RECORD, messageRecordTransferedJson(2, msgid, resid), 0, 0);
	}
	
	@Override
	public void onContactListGet(List<AirContact> arg0) {
		// TODO Auto-generated method stub
		GolukDebugUtils.i("123", "wjun ----onContactListGet----- ");
	}
	

	@Override
	public void onMessageRecordPlayLoaded(boolean isOk, String msgCode, String resId, byte[] resBytes){
		//为统一播放，修改的接口	
//		8025:
//		json 串内容为:
//		{"success" :true/false,
//		"msgid" :"XXX",
//		resid :"XXX"
//		}
		
		AirTalkeeEvent(AIRTALKEE_RECORDPLAYLOADED, messageRecordPlayLoaded(isOk, msgCode, resId), 0, 0);

//		GolukDebugUtils.i("AbstractTaxiAirTalkee", "onMessageRecordPlayLoaded isOk = " + isOk);
//		GolukDebugUtils.i("AbstractTaxiAirTalkee", "onMessageRecordPlayLoaded msgCode = " + msgCode);
//
//		GolukDebugUtils.i("AbstractTaxiAirTalkee", "onMessageRecordPlayLoaded resId = " + resId);
//
//		GolukDebugUtils.i("AbstractTaxiAirTalkee", "onMessageRecordPlayLoaded resBytes = " + resBytes);
//		
//		GolukDebugUtils.i("AbstractTaxiAirTalkee", "onMessageRecordPlayLoaded resBytes.length = " + resBytes.length);
		
//		MediaPlay mp = new MediaPlay();
//		mp.sys_mediaplayer_stop();
		
//		byte[] b = null;
//				new byte[resBytes.length - 6]; //去掉6个字节的amr 和 pcm 头
		
//		System.arraycopy(resBytes, 6, b, 0, resBytes.length - 6);
		
//		mp.sys_mediaplayer_playstream(b, resBytes.length - 6);
		
	}

	/**
	 * -----------------------拼JSON串工具方法---------------------
	 * ----------------------------------------------------------
	 * */
	
	public String messageRecordStopJson(int state, int seconds, String msgcode){
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_STATE, state);
			
			JSONObject jsonobject2 = new JSONObject();
			jsonobject2.put(JSON_TIME, seconds);
			jsonobject2.put(JSON_MSGID, msgcode);
			
			jsonobject.putOpt(JSON_MSG, jsonobject2);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonobject.toString();
	}
	
	public String messageRecordTransferedJson(int state, String msgId, String resId){
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_STATE, state);
			
			JSONObject jsonobject2 = new JSONObject();
			jsonobject2.put(JSON_MSGID, msgId);
			jsonobject2.put(JSON_RESID, resId);
			
			jsonobject.putOpt(JSON_MSG, jsonobject2);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonobject.toString();
	}
	
	public String messageRecordOrPlayJson(int state, String msgid, String resid){
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_STATE, state);
			jsonobject.put(JSON_MSGID, msgid);
			jsonobject.put(JSON_RESID, resid);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonobject.toString();
	}
	
	public String messageRecordPlayLoaded(boolean isok, String msgid, String resid){
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_SUCCESS, isok);
			jsonobject.put(JSON_MSGID, msgid);
			jsonobject.put(JSON_RESID, resid);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonobject.toString();
	}
	

	/**
	 * @Description：拼接登录回调，心跳回调，刷新会话状态的json文件
	 * 
	 * @param state
	 *            :状态
	 * @return 拼好的json串，如果抛异常则返回null
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-16
	 * @note：
	 */
	public String JsonState(int state) {
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_STATE, state);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonobject.toString();
	}
	
	

	public String OnlineCountJson() {

		return null;
	}

	/**
	 * @Description：所有群组在线情况的json串
	 * 
	 * @param online
	 * 
	 * @return 拼好的json串，如果抛异常则返回null
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-16
	 * @note：
	 */
	private String AllGroupOnlineCountJson(LinkedHashMap<String, Integer> online) {
		JSONArray jsonarray = new JSONArray();
		for (Iterator<String> it = online.keySet().iterator(); it.hasNext();) {
			String channelID = it.next();
			int count = online.get(channelID);
			JSONObject jsonobjecttemp = new JSONObject();
			try {
				jsonobjecttemp.put(JSON_CHANNELID, channelID);
				jsonobjecttemp.put(JSON_ONLINECOUNT, count);
				jsonarray.put(jsonobjecttemp);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return jsonarray.toString();
	}

	/**
	 * @Description：拼接在群组里面聊天时在线人数和总人数的json串,onSessionPresence回调函数调用
	 * 
	 * @param onlinecount
	 *            ：在线人数
	 * @param membercount
	 *            ：总人数
	 * @return 返回拼好的字符串，如果异常则返回null
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-16
	 * @note：
	 */
	private String OnlineAndMemberJson(int onlinecount, int membercount,List<AirContact> membersPresence) {
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_ONLINECOUNT, onlinecount);
			jsonobject.put(JSON_MEMBERCOUNT, membercount);
			
			JSONArray jsonarray = new JSONArray();
			int nsize = membersPresence.size();

			for (int i = 0; i < nsize; i++) {
				String aid = membersPresence.get(i).getIpocId();
//				JSONObject jsonobjecttemp = new JSONObject();
				try {
//					jsonobjecttemp.put(JSON_AID, aid);
					jsonarray.put(aid);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			
			jsonobject.put(JSON_MEMBERS, jsonarray);
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return jsonobject.toString();
	}

	/**
	 * @Description：会话来电，别人申请到话语权时需要拼的json串，onSessionIncomingAlertStart回调函数调用,onMediaStateListen调用
	 * 
	 * @param aid
	 *            :用户的aid
	 * @return 返回拼好的字符串，如果异常则返回null
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-16
	 * @note：
	 */
	private String IncomingAlertStartJson(String aid) {
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_AID, aid);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonobject.toString();
	}

	/**
	 * @Description：音频变化时拼JSON串，onMediaAudioWaveChanged回调函数调用
	 * 
	 * @param valume
	 *            :音频大小，0~100
	 * @return 返回拼好的字符串
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-16
	 * @note：
	 */
	private String valumeJson(int valume) {
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_VOLUME, valume);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonobject.toString();

	}

	/**
	 * @Description：发送完自定义消息时返回的内容拼成JSON串，onMessageOutgoingSent函数调用
	 * 
	 * @param issent
	 *            :是否发送成功;
	 * @param content
	 *            ：发送的内容;
	 * @param msgtype
	 * 			  :0/1/2	-->自定义类型/文本类型/语音类型
	 * @param resid
	 * 			  :	-->""/""/语音消息资源id
	 * @return
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-16
	 * @note：
	 */
	private String messageSentTypeJson(boolean issent, String msgid, int msgtype, String resid) {
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_SUCCESS, issent);
			jsonobject.put(JSON_MSGID, msgid);
			jsonobject.put(JSON_MSGTYPE, msgtype);
			if(resid == null){
				jsonobject.put(JSON_RESID, "");
			}else{
				jsonobject.put(JSON_RESID, resid);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonobject.toString();
	}

	/**
	 * @Description：来电或去电呼叫、或群组会话开始建立事件
	 * 
	 * @return
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-16
	 */
	private String SessionEstablishingJson(String groupid) {
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_GROUPID, groupid);
			jsonobject.put(JSON_STATE, 0);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonobject.toString();
	}
	
	/**
	 * @Description：来电或去电呼叫、或群组会话用户被提出频道
	 * 
	 * @return
	 * 
	 * @author：Harry
	 * @Date：2014-4-14
	 */
	private String SessionManagerKickout(String groupid, AirContactTiny userShutup, boolean isMe)
	{
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_GROUPID, groupid);
			jsonobject.put(JSON_STATE, 3);
			jsonobject.put(JSON_AID, userShutup.getIpocId());
			jsonobject.put(JSON_ISME, isMe);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonobject.toString();
	}
	
	/**
	 * @Description：来电或去电呼叫、或群组会话用户被禁言
	 * 
	 * @return
	 * 
	 * @author：Harry
	 * @Date：2014-4-14
	 */
	private String SessionManagerShutupt(AirContactTiny userShutup, boolean isMe)
	{
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_AID, userShutup.getIpocId());
			jsonobject.put(JSON_ISME, isMe);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonobject.toString();
	}
	
	/**
	 * @Description：来电或去电呼叫、或群组会话建立完成的JSON串
	 * 
	 * @param isOk
	 *            ：是否成功
	 * @return
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-16
	 */
	private String SessionEstablishedJson(boolean isOk, String groupid) {
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_SUCCESS, isOk);
			jsonobject.put(JSON_GROUPID, groupid);
			jsonobject.put(JSON_STATE, 1);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonobject.toString();
	}
	
	/**
	 * @Description：临时或群组会话结束事件的JSON串
	 * 
	 * @param isOk
	 *            ：是否成功
	 * @return
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-16
	 */
	private String SessionReleasedJson(String groupid, int reason) {
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_GROUPID, groupid);
			jsonobject.put(JSON_REASON, reason);
			jsonobject.put(JSON_STATE, 2);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonobject.toString();
	}

	/**
	 * @Description：获得讲话人的信息
	 * 
	 * @return：返回协议的字符串
	 * 
	 * @author：孙小庆
	 * @Date：2013-1-16
	 */
	private String MediaStateJson() {
		if (msession == null)
			return null;
		JSONObject jsonobject = new JSONObject();
		try {
			jsonobject.put(JSON_STATE, msession.getMediaState());
			AirContact contact = msession.getSpeaker();
			if (contact != null) {
				jsonobject.put(JSON_AID, contact.getIpocId());
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return jsonobject.toString();

	}

	/**
	 * @Description：
	 * 
	 * @return：返回协议的字符串
	 * 
	 * @author：孙小庆
	 * @Date：2013-2-1
	 */
	private String MemberListJson(boolean bissuccess, List<AirContact> members) {
		JSONObject jsonobject = new JSONObject();

		try {
			jsonobject.put(JSON_SUCCESS, bissuccess);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}

		if (!bissuccess) {
			return jsonobject.toString();
		}

		JSONArray jsonarray = new JSONArray();
		int nsize = members.size();

		for (int i = 0; i < nsize; i++) {
			String aid = members.get(i).getIpocId();
			JSONObject jsonobjecttemp = new JSONObject();
			try {
				jsonobjecttemp.put(JSON_AID, aid);
				jsonarray.put(jsonobjecttemp);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return jsonobject.toString();
	}

}
