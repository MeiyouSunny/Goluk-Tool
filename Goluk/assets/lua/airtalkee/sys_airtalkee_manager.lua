--[[
-- @描述:爱滔客接口绑定
-- @编写人:魏俊
-- @创建日期: 2013-01-17 10:56
-- @修改内容：添加导航停止通知接口，用于见面一、二的上报逻辑问题 2013-01-24
--]]

require"lua/location"
require"lua/moduledata"
require"lua/framework"
require"lua/airtalkee"
require"lua/timer"
require"lua/meet"
require"lua/settingconfig"
require"lua/file"
require"lua/commfunc"
require"lua/TalkerMgr"

local gAirTalkeeMgr = nil;		--全局AirTalkee管理引擎
local gSendGPSTime1 = 30*1000;	--40s
local gSendGPSTime2 = 300*1000;	--界面1/2，5分钟
local gSendGPSTime3 = 10*1000;	--界面3上报时间间隔，动态计算
local MAXUdpUploadTime = 300*1000;	--最大UDP累计时长为5分钟

local gSendToPlatMsg = {}; --需要发送给服务器的视频播放信息列表

--MeetManager 消息ID声明
local MMID_REQUEST_SENDPOSITION = 10005;		--通知together模块，请求发送位置信息
--local MMID_RESPONSE_AUDIO = 10008				--收到群组内语音消息标识
--local MMID_RESPONSE_TEXT = 10009;				--收到群组内文本消息标识
local MMID_RESPONSE_POSITION = 10010;			--添加见面三自定义位置上报消息标识
local MMID_RESPONSE_TALK	= 10011;			--有人开始语音消息标识
local MMID_RESPONSE_VOLUME = 10012;				--音频强度变化消息标识
local MMID_RESPONSE_RELOGIN = 10013;			--有另一个设备用同一个号登录,被登录消息标识
local MMID_RESPONSE_LOGIN = 10014;				--uid、aid登录成功消息标识
local MMID_RESPONSE_NETERROR = 10015;			--网络出错回调消息标识
local MMID_RESPONSE_GROUP_STATUS_UPDATE = 10016;--临时或群组会话状态事件
local MMID_RESPONSE_GROUP_Members = 10030;		--群组成员信息发生变化
local MMID_RESPONSE_GROUP_ReEnter = 10031;		--群组重新进入
local MMID_RESPONSE_GROUP_UpdateTitle = 10032;	--群组标题修改
local MMID_RESPONSE_VIDEOREQ = 10040;			--视频请求
local MMID_RESPONSE_VideoUrlInfo = 10041;			--接收到服务器推送的视频URL等信息
local MMID_RESPONSE_RECORDVIDEOERROR = 10042;			--录制视频出错
local MMID_RESPONSE_PHONE_WATCH_VEDIO = 10043;			--接收到服务器推送手机客户端请求视频直播

local MMID_RESPONSE_AcceptVideoRequest =104;	--用户接受视频请求


--obj:爱滔客句柄
--lastGpsInfo:最近一次的gps位置信息
--iswaitgps:当前是否在等待定位
--UDPTimeout:0/N是否收到服务器应答标识（用于见面一、二简化上报UDP,N标识多次超时，没有收到服务器回应）
--aid,password//aid,password
--当前udp上报时间策略：前5分钟内每40s上报一次，如果在这5分钟内没有收到服务器push的关键包（type=9），则后续上报频次为5分钟/次，如果收到，则恢复之前前分钟内每40s上报一次策略
--CurUDPReportTime	:当前UDP上报计时
--SumUDPReportTime	:累计到没有收到服务器push的键包（type=9）的时间



--根据state生成指定Json串接口
--参数state	状态
--返回json字符串
local function GenJson_State(state)
	local t = {};
	t.state = state;
	return tiros.json.encode(t);
end
--[[
	通知通用逻辑模块统一接口
参数:msgid	消息id
	param1	参数1
	param2	参数2
具体消息见logic模块与其他模块对外交协议文档
]]--
local function SendMsgToLogic( msgid, param1, param2 )
	local nFunction = tiros.moduledata.moduledata_get("framework", "pLogicFunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "pLogicUser");    
    if nFunction == nil or nUser == nil then
       return
    end
	commlib.universalnotifyFun(nFunction,"LuaToLogicMsg", nUser, param1, msgid, param2);
	--SendMsgToPlatform(255, msgid, param2);
end



--lua通知平台的使用方法
--[[
--@描述:从数据仓库获取回调函数地址和pUser
--@param  无
--@return 回调函数地址和pUser
--]]
local function getFunctionAndUser()
    local nFunction = tiros.moduledata.moduledata_get("framework", "pfunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "puser");
    if nFunction == nil or nUser == nil then
       print("getFunctionAndUser--error");
       return nil,nil;
    else
        return nFunction, nUser;
    end
end 

--获取logic注册的回调
local function getLogicFunctionAndUser()
    local nFunction = tiros.moduledata.moduledata_get("framework", "pLogicFunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "pLogicUser");    
    return nFunction, nUser;    
end


--通知群组模块相关信息接口
local function SendMessageToGroupManager(msg, param1, param2)
	--tiros.together.WebCommonUpdateData(msg);
	tiros.TalkerMgr.AirtalkeeResponseEvent(msg)
end


--UDP位置上报接口
local function GpsInfoUdpUpload()
--[[
	if gAirTalkeeMgr.lastGpsInfo.elon == nil then	--没有定位成功，则直接返回
		gAirTalkeeMgr.iswaitgps = true;				--等待定位	
		return false;
	end
--]]

if gAirTalkeeMgr.lastGpsInfo.elon == 0 or gAirTalkeeMgr.lastGpsInfo.elon == nil then
--gAirTalkeeMgr.lastGpsInfo.elon,gAirTalkeeMgr.lastGpsInfo.elat,gAirTalkeeMgr.lastGpsInfo.speed,gAirTalkeeMgr.lastGpsInfo.course,gAirTalkeeMgr.lastGpsInfo.altitude,gAirTalkeeMgr.lastGpsInfo.radius,gAirTalkeeMgr.lastGpsInfo.funtype,gAirTalkeeMgr.lastGpsInfo.rawLon,gAirTalkeeMgr.lastGpsInfo.rawLat = tiros.location.lkgetlastposition_mem();
end

	gAirTalkeeMgr.iswaitgps = false;
	local aid = gAirTalkeeMgr.aid;
	if aid == nil then						--如果没有登录成功，则直接返回
		return false;
	end
	local t = {};
	t.mobileid = tiros.moduledata.moduledata_get("framework", "mobileid");
	t.uid = tiros.moduledata.moduledata_get("framework", "uid");
	t.aid = aid;
	t.v = 2;		--固定表示当前最新版本号
	t.Version = tiros.moduledata.moduledata_get("framework", "version");
	t.x = gAirTalkeeMgr.lastGpsInfo.elon;
	t.y = gAirTalkeeMgr.lastGpsInfo.elat;
	--原生gps信息
	t.rx = gAirTalkeeMgr.lastGpsInfo.rawLon;
	t.ry = gAirTalkeeMgr.lastGpsInfo.rawLat;
	t.radius = gAirTalkeeMgr.lastGpsInfo.radius;
	t.sd = gAirTalkeeMgr.lastGpsInfo.speed;
	t.d = gAirTalkeeMgr.lastGpsInfo.course;	--gps方向
	t.customflag = MMID_RESPONSE_POSITION;		--添加见面三自定义位置上报标识10010
	
	--用户信息，昵称与性别
	t.n,t.s,t.h= tiros.TalkerMgr.GetUserInfo();
	t.vs=tiros.TalkerMgr.GetVideoShare();
	t.vt=tiros.TalkerMgr.GetVideoTurn();
	
	local msg = {};
	msg.type = MMID_REQUEST_SENDPOSITION;
	msg.text = t;
	msg.aid = nil;
    SendMessageToGroupManager(tiros.json.encode(msg));
	print("ddddddddddddtest:" .. tiros.json.encode(msg))
    return true;
end

--重置UDP上报计时机制
local function AirTalkeeMgr_ResetUdpUploadTime()
	if gAirTalkeeMgr ~= nil then
		gAirTalkeeMgr.CurUDPReportTime = gSendGPSTime1;	--设定为40s上报
		gAirTalkeeMgr.SumUDPReportTime = 0;				--计时器累计清零
	end
end

local function AirTalkeeMgr_TimerExpried( ttype )
	--如果没有相关上报数据，则停止计时器上报流程
	if GpsInfoUdpUpload() == false then
		--return;
	end
	tiros.timer.timerstartforlua("AirTalkeeMgr", gSendGPSTime3, AirTalkeeMgr_TimerExpried, false);	
end

local function StartGpsUpload()
	local rev = GpsInfoUdpUpload();
	--if rev == true then
		tiros.timer.timercancel("AirTalkeeMgr");
		tiros.timer.timerstartforlua("AirTalkeeMgr", gSendGPSTime3, AirTalkeeMgr_TimerExpried, false);	
	--end
end

--根据单个接受的系统语音、文本及位置上报消息生成结伴通行模块协议消息
--sysMsg：api曾返回的单个语音、文本及位置上报消息数据
--返回：成功：具体生成的table对象,失败:nil
local function GenIncomingMsgFromSystemMsg(sysMsg)
	if sysMsg == nil then
		return nil;
	end
	local t = sysMsg;
	local msgtype = tonumber(t.msgtype );
	if msgtype == 0 then	--自定义消息
		if type(t.msg) == "string" then
			t.msg = tiros.json.decode(t.msg);
		end
		if t.msg.customflag == MMID_RESPONSE_POSITION then					--群组内自定义消息位置上报消息
			print("wjun===== 8014  msgtype == 0  position update 1!!!")
			local pos = {};
			pos.type = MMID_RESPONSE_POSITION;
			pos.groupid = t.groupid;
			pos.aid = t.aid;
			pos.msgid = t.msgid;
			pos.date = tiros.commfunc.CurrentTime();
			pos.msg = {};
			pos.msg.lon = t.msg.x;
			pos.msg.lat = t.msg.y
			pos.msg.direction = t.msg.d;
			pos.msg.speed = t.msg.sd;
			pos.msg.radius = t.msg.radius;
			pos.msg.name = t.msg.n;
			pos.msg.sex = t.msg.s;
			pos.msg.head = t.msg.h;
			pos.msg.videoshare = tostring(t.msg.vs);
			pos.msg.videoturn = tostring(t.msg.vt);

			--SendMessageToGroupManager(tiros.json.encode(pos));
			print("wjun===== 8014  msgtype == 0  position update 2!!!")
			return pos;
		elseif  t.msg.customflag == MMID_RESPONSE_AcceptVideoRequest then
			print("jiangdezheng 1被请求方 ");
			
			local pos = {};
			pos.type = MMID_RESPONSE_VIDEOREQ;
			pos.msg = {};
			pos.msg.sourceAid = tostring(t.msg.sourceAid);
			pos.msg.desAid = tostring(t.msg.desAid);
			return pos;
		elseif t.msg.customflag == MMID_RESPONSE_RECORDVIDEOERROR then
			local pos = {};
			pos.type = MMID_RESPONSE_RECORDVIDEOERROR;
			pos.errorCode = t.msg.x;
			return pos;
		end
	elseif msgtype == 2 then				--群组内文字聊天消息
		print("jiayufeng-------------------------------airtalkee_manager--------ACCept----TEXT");
		--local textmsg = {};
		--textmsg.type = MMID_RESPONSE_TEXT;
		--textmsg.groupid = t.groupid;
		--textmsg.aid = t.aid;
		--textmsg.msgid = t.msgid;
		--textmsg.date = t.date;
		--textmsg.msg = t.msg;
		--SendMessageToGroupManager(tiros.json.encode(textmsg));
		--return textmsg;	
		return nil;			
	elseif msgtype == 5 then				--群组内语音聊天消息
		print("jiayufeng-------------------------------airtalkee_manager--------ACCept----AUDIO");
		--local audiomsg = {};
		--audiomsg.type = MMID_RESPONSE_AUDIO;
		--audiomsg.groupid = t.groupid;
		--audiomsg.aid = t.aid;
		--audiomsg.msgid = t.msgid;
		--audiomsg.date = t.date;
		--if type(t.msg) == "string" then
		--	audiomsg.msg = tiros.json.decode(t.msg);
		--else
		--	audiomsg.msg = t.msg;
		--end
		--SendMessageToGroupManager(tiros.json.encode(audiomsg));	
		--return audiomsg;
		return nil;
	else
		return nil;
	end
end

--收到IMS系统推送的push消息处理逻辑
local function DoWithSys_Push_Msg(tpushmsg)
	--1:频道大区发生变化
	--2:修改群组名称
	
	
	local t = tpushmsg;
	local t1 = {};
	if t.type == 1 then
		--1:频道大区发生变化
		t1.type = MMID_RESPONSE_GROUP_ReEnter;		
	elseif t.type == 2 then
		--2:修改群组名称
		t1.type = MMID_RESPONSE_GROUP_UpdateTitle;
		t1.data = t.data;
	elseif t.type == "3" then
		--3：请求方收到服务器URL
		t1.type = MMID_RESPONSE_VideoUrlInfo;
		t1.data = t;
	elseif t.type == 4 then
		print("jiangdezheng clientrequest 22");
		--4:手机客户端请求视频直播
		t1.type = MMID_RESPONSE_PHONE_WATCH_VEDIO;
		t1.data = t.data;
	end
	if t1.type ~= nil then
		SendMessageToGroupManager(tiros.json.encode(t1));
	end

end
--爱滔客系统回调接口
local function airtalkee_NotifyEvent(ATObj, dwEvent, json, dwParam1, dwParam2)
	print("wjun====================recv " .. dwEvent .. " event:!!!!");
	if json ~= nil and type(json) == "string" then
		print("wjun============Json = "..json);
	end
	local t = nil;
	if dwEvent == 8000 then								--登出回调
		if gAirTalkeeMgr.aid ~= nil and gAirTalkeeMgr.password ~= nil then
			tiros.airtalkee.SetLoginStatus(gAirTalkeeMgr.obj, false);
			tiros.airtalkee.login(gAirTalkeeMgr.obj, gAirTalkeeMgr.aid, gAirTalkeeMgr.password);
		end
	elseif dwEvent == 8001 then							--登录回调{"state":0}
		t =  tiros.json.decode(json);
		if t ~= nil then
			local state = tonumber(t.state);
			if state == 0 then	--登录成功
				print("QQQ登录成功------------------aid = " .. gAirTalkeeMgr.aid);
				print("airtalkee login success!");
				tiros.moduledata.moduledata_set("framework", "aid", ATObj.name);	--将当前登录成功的aid更新到数据仓库中
				tiros.airtalkee.SetLoginStatus(gAirTalkeeMgr.obj, true);
				--如果此次登录的aid为uid对应情况，则通知结伴通行登录成功
				local t1 = {};
				t1.type = MMID_RESPONSE_LOGIN;
				t1.success = true;
				SendMessageToGroupManager(tiros.json.encode(t1));
				
			elseif state == 10 then				--同一设备登录
				print("airtalkee login out 8001!" .. t.state);
				tiros.airtalkee.SetLoginStatus(ATObj, false);				--通知底层，airtalkee被登出
				tiros.moduledata.moduledata_set("framework", "aid", nil);	--将当前登录成功的aid更新到数据仓库中
				tiros.moduledata.moduledata_set("framework", "uid", "");	--将当前登录成功的uid更新到数据仓库中
				--通知登录管理模块，使用mid对应aid登录愛淘客
				--通知平台当前设备被挤出
				local nFunction, nUser = getFunctionAndUser();
				commlib.initNotifyFun(nFunction, nUser, 148, 0, 0); 		--通知平台
				local t1 = {};
				t1.type = MMID_RESPONSE_RELOGIN;
				SendMessageToGroupManager(tiros.json.encode(t1));
				
			else
				print("airtalkee login Error!" .. t.state);
				local t1 = {};
				t1.type = MMID_RESPONSE_LOGIN;
				t1.success = false;
				SendMessageToGroupManager(tiros.json.encode(t1));
				tiros.airtalkee.SetLoginStatus(gAirTalkeeMgr.obj, false);
			end
		end
	elseif dwEvent == 8002 then						--心跳{"state":0}
		t =  tiros.json.decode(json);
		if t ~= nil then
			local state = tonumber(t.state);
			if state == 10 then									--有另一个设备用同一个号登录
				print("airtalkee login out! 8002!");
				tiros.airtalkee.SetLoginStatus(ATObj, false);				--通知底层，airtalkee被登出
				tiros.moduledata.moduledata_set("framework", "aid", nil);	--将当前登录成功的aid更新到数据仓库中
				tiros.moduledata.moduledata_set("framework", "uid", "");	--将当前登录成功的uid更新到数据仓库中
				--通知平台当前设备被挤出
				local nFunction, nUser = getFunctionAndUser();
				commlib.initNotifyFun(nFunction, nUser, 148, 0, 0); 		--通知平台
				local t1 = {};
				t1.type = MMID_RESPONSE_RELOGIN;
				SendMessageToGroupManager(tiros.json.encode(t1));
			elseif state == 9 then		--掉线
				tiros.airtalkee.SetLoginStatus(ATObj, false);				--通知底层，airtalkee被登出
				local t1 = {};
				t1.type = MMID_RESPONSE_NETERROR; 
				SendMessageToGroupManager(tiros.json.encode(t1));
			end
		end
	elseif dwEvent == 8003 then						--所有群的在线人数,[{"channelID":"1", "onlinecount":1},{"channelID":"2", "onlinecount":1}];

	elseif dwEvent == 8004 then						--会话,{"state":0},0:// 会话不可用,1:// 会话正在建立中,2:// 会话进行中

	elseif dwEvent == 8005 then						--当前群在线人数和总人数, {“onlinecount”:1,"mcount":1}
		print("wjun---当前群在线人数和总人数 1");
		t =  tiros.json.decode(json);
		if t ~= nil then
			print("wjun---当前群在线人数和总人数 2");
			local t1 = {};
			t1.type = MMID_RESPONSE_GROUP_Members;
			t1.onlinecount = t.onlinecount;
			t1.allcount = t.allcount;
			t1.members = t.members;
			print("wjun---当前群在线人数和总人数 3 json = " .. tiros.json.encode(t1));
			SendMessageToGroupManager(tiros.json.encode(t1));
			print("wjun---当前群在线人数和总人数 2");
		end
	elseif dwEvent == 8006 then						--当临时会话来电提醒, {“aid”:"1233123"}
		--收到临时会话来电提醒，测试直接接受
		print("QQQ当临时会话来电提醒---------------json = " .. json);
		tiros.airtalkee.SessionIncomingAccept(ATObj);
		print("QQQ当临时会话来电提醒--接受来电---------------");
		
	elseif dwEvent == 8007 then						--临时会话结束事件（本方及对方）
		print("QQQ临时会话结束事件（本方及对方）");
		
	elseif dwEvent == 8008 then						--本人申请到话语权
		print("QQQ本人申请到话语权---------------");
		t = {};
		t.aid = tiros.moduledata.moduledata_get("framework", "aid");
		t.isVoice = 1;
		t.type = MMID_RESPONSE_TALK;

		SendMessageToGroupManager(tiros.json.encode(t));
		
	elseif dwEvent == 8009 then						--本人释放话语权
		print("QQQ本人释放话语权---------------");
		t = {};
		t.aid = tiros.moduledata.moduledata_get("framework", "aid");
		t.isVoice = 0;
		t.type = MMID_RESPONSE_TALK;
		SendMessageToGroupManager(tiros.json.encode(t));

	elseif dwEvent == 8010 then						--本人申请功能话语权失败
		print("wjun----QQQ本人申请功能话语权失败---------------");
		t = {};
		t.aid = tiros.moduledata.moduledata_get("framework", "aid");
		t.isVoice = -1;
		t.type = MMID_RESPONSE_TALK;
		SendMessageToGroupManager(tiros.json.encode(t));

	elseif dwEvent == 8019 then						--本人申请功能话语权排队
		print("wjun----QQQ本人申请功能话语权排队---------------");
		t = {};
		t.aid = tiros.moduledata.moduledata_get("framework", "aid");
		t.isVoice = 2;
		t.type = MMID_RESPONSE_TALK;
		SendMessageToGroupManager(tiros.json.encode(t));

	elseif dwEvent == 8030 then
		print("wjun----QQQ本人申请功能话语权被禁言---------------");
		local t = {};
		t.aid = tiros.moduledata.moduledata_get("framework", "aid");
		t.isVoice = 3;
		t.type = MMID_RESPONSE_TALK;
		SendMessageToGroupManager(tiros.json.encode(t));

	elseif dwEvent == 8011 then						--别人开始讲话---8011, {“aid”:"1233123"}
		print("wjun----QQQ别人开始讲话---------------");
		t =  tiros.json.decode(json);
		if t ~= nil then
			local t1 = {};
			t1.type = MMID_RESPONSE_TALK;
			t1.aid = t.aid;
			t1.isVoice = 1;
			SendMessageToGroupManager(tiros.json.encode(t1));
		end	

	elseif dwEvent == 8012 then						--别人讲话结束
		print("wjun----QQQ别人讲话结束---------------");
		t =  tiros.json.decode(json);
		if t ~= nil then
			local t1 = {};
			t1.type = MMID_RESPONSE_TALK;
			t1.aid = t.aid;
			t1.isVoice = 0;
			SendMessageToGroupManager(tiros.json.encode(t1));
		end

	elseif dwEvent == 8013 then						--音频强度, {"volume":80}//0~100
		t =  tiros.json.decode(json);
		if t ~= nil and t.volume >=0 then
			local v = {};
			v.type = MMID_RESPONSE_VOLUME;
			v.msg = {};
			v.msg.volume = t.volume;
			SendMessageToGroupManager(tiros.json.encode(v));
		end

	elseif dwEvent == 8014 then						--收到自定义消息体, {"uid":"1","aid":"1","lon":"121","lat":"123","course":123}
		t =  tiros.json.decode(json);
		if t ~= nil then
			local newmsg = GenIncomingMsgFromSystemMsg(t);
			if newmsg ~= nil then
				SendMessageToGroupManager(tiros.json.encode(newmsg));
			else
				print("wjun====================recv 8014 Event Error! json = " .. json);
			end
		end
	elseif dwEvent == 8015 then						--自己发送自定上报信息状态, {"success":true/false,"msg":content}
		
	elseif dwEvent == 8016 then						--自收到服务器push消息
		t =  tiros.json.decode(json);
		if t ~= nil then
			DoWithSys_Push_Msg(t);
		end
	elseif dwEvent == 8017 then						--刷新说话的那按钮

	elseif dwEvent == 8018 then						--临时或群组会话状态事件
		t =  tiros.json.decode(json);
		if t ~= nil then
			local t1 = {};
			t1.type = MMID_RESPONSE_GROUP_STATUS_UPDATE;
			t1.groupid = t.groupid;
			t1.status = t.state;
			if t.state == 0 then	                --临时或群组会话正在建立中
				t1.msg = nil;
			elseif t.state == 1 then	            --临时或群组会话建立完成
				t1.msg = t.success;
			elseif t.state == 2 then	            --临时或群组会话结束事件
				t1.msg = t.reason;
			elseif t.state == 3 then	            --临时或群组会话用户被提出频道
				t1.aid = t.aid;
				t1.isme = t.isme;
			else
				print("wjun----临时或群组会话状态错误---------------");
				t1.msg = nil;
			end
			SendMessageToGroupManager(tiros.json.encode(t1));
			print("wjun=======8018= 2");
		end

	elseif dwEvent == 8020 then						--
		
	else
		print("wjun------>unsupport msg!!!!!!!!json= " .. json);
	end
	
end

--GPS位置回调接口
local function airtalkee_GPSNotifyEvent(stype,elon,elat,speed,course,altitude,radius,funtype,rawLon,rawLat)
	if gAirTalkeeMgr ~= nil  and elon ~= 0 and elat ~= 0 and elon ~= nil and elat ~= nil then
		gAirTalkeeMgr.lastGpsInfo.elon = elon;
		gAirTalkeeMgr.lastGpsInfo.elat = elat;
		gAirTalkeeMgr.lastGpsInfo.speed = speed;
		gAirTalkeeMgr.lastGpsInfo.course = course;
		gAirTalkeeMgr.lastGpsInfo.altitude = altitude;
		gAirTalkeeMgr.lastGpsInfo.radius = radius;
		gAirTalkeeMgr.lastGpsInfo.funtype = funtype;
		gAirTalkeeMgr.lastGpsInfo.rawLon = rawLon;
		gAirTalkeeMgr.lastGpsInfo.rawLat = rawLat;

		if gAirTalkeeMgr.iswaitgps == true then	--上报UDP信息
			print("wjun***********airtalkee_GPSNotifyEvent StartGpsUpload!!");
			StartGpsUpload();
		end
	end
end

local function airtalkeemgr_init()
	if gAirTalkeeMgr == nil then
		gAirTalkeeMgr = {};
		gAirTalkeeMgr.flag = 0;
		gAirTalkeeMgr.navi = nil;
		gAirTalkeeMgr.lastGpsInfo = {};
		gAirTalkeeMgr.iswaitgps = false;
		AirTalkeeMgr_ResetUdpUploadTime();											--重置UDP上报计时策略
		local sip = tiros.framework.getAirTalkeeUrl();								--获取配置ip地址
		print("wjun======>getAirTalkeeUrl = " .. sip);
		gAirTalkeeMgr.obj = tiros.airtalkee.create(sip);							--创建愛淘客对象句柄
		tiros.airtalkee.registnotify(gAirTalkeeMgr.obj, airtalkee_NotifyEvent);		--注册淘客事件回调
		--tiros.location.lkstart(3, airtalkee_GPSNotifyEvent, nil);					--注册gps回调

		tiros.timer.timerstartforlua("AirTalkeeMgr", gSendGPSTime3, AirTalkeeMgr_TimerExpried, false);	
	end
end

local function airtalkeemgr_Release()
	if gAirTalkeeMgr ~= nil then 
		if gAirTalkeeMgr.obj ~= nil then
			tiros.airtalkee.destory(gAirTalkeeMgr.obj);
			gAirTalkeeMgr.obj = nil;
		end
		gAirTalkeeMgr = nil;
	end
end

local function airtalkeemgr_configserver(cfg_sp, cfg_sp_port, cfg_sp_lport, cfg_mdsr, cfg_mdsr_port)
	print("wjun===configserver:",cfg_sp, cfg_sp_port, cfg_sp_lport, cfg_mdsr, cfg_mdsr_port)
	tiros.airtalkee.configserver(gAirTalkeeMgr.obj, cfg_sp, cfg_sp_port, cfg_sp_lport, cfg_mdsr, cfg_mdsr_port);
end

local function airtalkeemgr_login(name, password)
	print("wjun===login:" .. name .. "," .. password);
	airtalkeemgr_init();
	gAirTalkeeMgr.aid = name;
	gAirTalkeeMgr.password = password;
	if tiros.airtalkee.isNeedLogout(gAirTalkeeMgr.obj) == true then
		tiros.airtalkee.logout(gAirTalkeeMgr.obj);
	else
		tiros.airtalkee.login(gAirTalkeeMgr.obj, name, password);
	end
end

local function airtalkeemgr_logout()
	gAirTalkeeMgr.aid = nil;
	gAirTalkeeMgr.password = nil;
	if tiros.airtalkee.isNeedLogout(gAirTalkeeMgr.obj) == true then
		tiros.airtalkee.logout(gAirTalkeeMgr.obj);
	end
end

--见面一、二及三功能模块位置上报
local function airtalkeemgr_UploadPositionInfo()
	gAirTalkeeMgr.UDPTimeout = 0;								--有新的上报，则重置udp上报超时策略
	AirTalkeeMgr_ResetUdpUploadTime();							--重置udp上报计时策略
	StartGpsUpload();											--开始上报GPS
end

--停止上报
local function airtalkeemgr_StopUploadPositionInfo()
	print("wjun===airtalkeemgr_StopUploadPositionInfo ");
	tiros.timer.timercancel("AirTalkeeMgr");
end

--当前网络状态发生变化，state==0：当前网络断开，state == 1:当前网络恢复
local function airtalkeemgr_NetworkChanged( state )
	if gAirTalkeeMgr ~= nil then 
		if gAirTalkeeMgr.obj ~= nil then
			if state == 1 then 
				tiros.airtalkee.NetWorkOpen(gAirTalkeeMgr.obj);
				tiros.TalkerMgr.RecoveryNetwork();
			else
				tiros.airtalkee.NetWorkClose(gAirTalkeeMgr.obj);
			end
		end
	end
end

--语音请求：nRequest：1请求语音，0放弃语音
local function airtalkeemgr_TalkRequest( nRequest )
	if nRequest == 1 then
		tiros.airtalkee.TalkRequest(gAirTalkeeMgr.obj);
	elseif nRequest == 0 then
		tiros.airtalkee.TalkRelease(gAirTalkeeMgr.obj);
	else
		print("^^^^^^^^^^^airtalkeemgr_TalkRequest error!:"..nRequest);
	end
end

--请求进入群组：groupid：群组ID
local function airtalkeemgr_SessionCall( groupid )
	print("wjun=====  airtalkeemgr_SessionCall 1 groupid=" .. groupid);
	tiros.airtalkee.SessionCall(gAirTalkeeMgr.obj, 1, groupid);
	print("wjun=====  airtalkeemgr_SessionCall 2");
end

--请求退出当前群组
local function airtalkeemgr_SessionBye()
	print("wjun=====  airtalkeemgr_SessionBye 1");
	tiros.airtalkee.SessionBye(gAirTalkeeMgr.obj);
	print("wjun=====  airtalkeemgr_SessionBye 2");
end

--向当前所在群组发送文本信息：msg：文本信息
local function airtalkeemgr_MessageSendToGroup( text, groupid )
	tiros.airtalkee.MessageSend4(gAirTalkeeMgr.obj, groupid, tiros.json.encode(text),1, 0)
end
--向当前所在群组里的其他用户发送文本信息：msg：文本信息
local function airtalkeemgr_MessageSendToOtherUser( text, aid )
	tiros.airtalkee.MessageSend3(gAirTalkeeMgr.obj, aid, tiros.json.encode(text),1, 0)
end

local function airtalkeemgr_GetAirtalkeeHandle()
	return gAirTalkeeMgr.obj;
end

--=========================================================================================
--接口table
local interface = {};

createmodule(interface,"init", function ()
	airtalkeemgr_init();
end)

createmodule(interface,"Release", function ()
	airtalkeemgr_Release();
end)

createmodule(interface,"configserver", function (cfg_sp, cfg_sp_port, cfg_sp_lport, cfg_mdsr, cfg_mdsr_port)
	airtalkeemgr_configserver(cfg_sp, cfg_sp_port, cfg_sp_lport, cfg_mdsr, cfg_mdsr_port)
end)

createmodule(interface,"login", function (name, password)
	airtalkeemgr_login(name, password)
end)

createmodule(interface,"logout", function ()
	airtalkeemgr_logout();
	tiros.moduledata.moduledata_set("framework", "aid", nil);	--注销，则从数据仓库中清除当前aid
end)

--外部调用，当有新的上报请求过来时触发
createmodule(interface,"UploadPositionInfo", function ( )
	airtalkeemgr_UploadPositionInfo();
end)

createmodule(interface,"StopUploadPositionInfo", function ()
	airtalkeemgr_StopUploadPositionInfo();
end)

createmodule(interface,"NetworkChanged", function ( state )
	airtalkeemgr_NetworkChanged(state);
end)

createmodule(interface,"TalkRequest", function ( nRequest )
	airtalkeemgr_TalkRequest( nRequest );
end)

createmodule(interface,"SessionCall", function ( groupid )
	airtalkeemgr_SessionCall( groupid );
end)

createmodule(interface,"SessionBye", function ()
	airtalkeemgr_SessionBye();
end)

createmodule(interface,"MessageSendToGroup", function ( text , groupid )
	return airtalkeemgr_MessageSendToGroup( text, groupid );
end)
--发送消息给其他用户
createmodule(interface,"MessageSendToOtherUser", function ( text , aid )
	return airtalkeemgr_MessageSendToOtherUser( text, aid );
end)

createmodule(interface,"GetAirtalkeeHandle", function ()
	return airtalkeemgr_GetAirtalkeeHandle();
end)

tiros.airtalkeemgr = readOnly(interface);
print("OOOOOOO tiros.airtalkeemgr ok!");
--=========================================end





