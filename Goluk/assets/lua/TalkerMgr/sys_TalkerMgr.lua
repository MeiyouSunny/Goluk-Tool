--[[
* 1.类命名首字母大写
* 2.公共函数驼峰式命名
* 3.属性函数驼峰式命名
* 4.变量/参数驼峰式命名
* 5.操作符之间必须加空格
* 6.注释都在行首写
* 7.后续人员开发保证代码格式一致
]]--
require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/framework/sys_framework"
require"lua/json"
require"lua/commfunc"
require"lua/config"
require"lua/moduledata"
require"lua/database"
require"lua/file"
require"lua/timer"
require"lua/base/base_http"
require"lua/base/base_common"
require"lua/grouplist"
require"lua/base/base_moduledata"
require"lua/settingconfig"
--require"lua/httpupload"
require"lua/moduledata"
--[[
	对讲机管理模块数据成员描述
	.config{}
		.aid:			用户Aid
		.uid:                   用户id
		.password:		用户密码
		.cfg_sp 		服务器配置参数
		.cfg_sp_port
		.cfg_sp_lport
		.cfg_mdsr
		.cfg_mdsr_port
	.atknetError		爱淘客服务器是否出现问题true/false
	.status:			当前运行状态机：见枚举值EStatus_开头
						0：空闲
						1：正在获取aid及配置参数
						2：获取aid及配置参数完成
						3：正在登录爱淘客
						4：登录爱淘客成功
						5：正在获取群组ID
						6：获取群组ID成功
						7：正在加入群组
						8：加入群组成功
	.group{}			--当前群组相关信息
		.speaker		当前说话的人
		.curGroupid:	用户当前所在的群组id
		.groupnumber 	当前群组的标号
		.grouptype 		当前群组的类别（大区/公共路况频道，小区）
		.title			用户当前所在群组的标题
		.membercount	用户当前所在群组的人数(前期在没有进入群组前为从后台服务获取，进入爱淘客群组之后通过爱淘客维护)
		.maxlon			地图显示的最大经度
		.maxlat			地图显示的最大维度
		.leastlon		地图显示的最小经度
		.leastlat		地图显示的最小维度
		.members:{}	用户当前所在群组的成员列表
			"xxx":{}
				.lon:
				.lat:
				.speed:
				.direction:
				.date:
				.aid:
				.groupid:
	.TaskArray{}		:当前任务队列(主要是在爱淘客还没有登录成功时，执行的其他操作需要缓存，等到登录成功之后再执行)
		task{}
			.cmd 		:当前任务指令
						见枚举值ETaskCMD_
			.data

--]]
--当前运行状态机枚举值
local EStatus_Idle,						--空闲
	  EStatus_GetAid,					--正在获取Aid
	  EStatus_GetAidOk,					--获取Aid成功
	  EStatus_Login,					--正在登录爱淘客
	  EStatus_LoginOk,					--登录爱淘客成功
	  EStatus_GetGroupid,				--正在获取群组信息
	  EStatus_GetGroupidOk,				--获取群组信息成功
	  EStatus_JoinGroup,				--正在加入群组
	  EStatus_JoinGroupOk 				--加入群组成功
	  = 0, 1, 2, 3, 4, 5, 6, 7, 8;

--通知上层回调消息类型
local ETalkerEvent_Login,				--登录及心跳相关事件
	  ETalkerEvent_GroupEnter,			--进入频道相关事件
	  ETalkerEvent_GroupTalk,			--频道内交互事件
	  ETalkerEvent_GroupInfo, 			--频道内位置信息及标题信息变更事件
	  ETalkerEvent_NetError
	  = 1, 2, 3, 4, 5;

--登录及心跳相关事件----类型枚举
local ELoginEvent_NetError,				--网络掉线
	  ELoginEvent_GettingAid,			--正在获取aid及其配置参数
	  ELoginEvent_GetAidOk,				--获取aid及其配置参数成功
	  ELoginEvent_GetAidErr,			--获取aid及其配置参数失败
	  ELoginEvent_LoginAid,				--正在登录爱淘客
	  ELoginEvent_LoginAidOk,			--登录爱淘客成功
	  ELoginEvent_AutoReLoginAidOk,		--自动重新登录爱淘客成功
	  ELoginEvent_LoginAidErr,			--登录爱淘客失败
	  ELoginEvent_LoginAidDouble,		--用户重复登录爱淘客
	  ELoginEvent_RecoveryNetwork,		--用户恢复网络
	  ELoginEvent_UserCenterLogining,	--用户中心正在登录
	  ELoginEvent_GetAidOverTime 	        --获取aid超时
	  = -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10;

--进入频道相关事件----类型枚举
local EGroupEnterEvent_GettingGid,		--正在获取频道信息
	  EGroupEnterEvent_GetGidOk,		--获取频道信息成功
	  EGroupEnterEvent_GetGidErr,		--获取频道信息失败
	  EGroupEnterEvent_Entering,		--正在进入爱淘客频道
	  EGroupEnterEvent_EnterOk,			--进入爱淘客频道成功
	  EGroupEnterEvent_AutoReEnterOk,	--自动重新进入爱淘客频道成功
	  EGroupEnterEvent_EnterErr,		--进入爱淘客频道失败
	  EGroupEnterEvent_Quit,			--频道退出
	  EGroupEnterEvent_Change,			--频道动态更换
	  EGroupEnterEvent_Kickout,			--用户从频道内被提出
	  EGroupInfoEvent_PlayJoinPrompt	--第一次进入群组播放声音
	  = 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10;

--频道内交互事件----类型枚举
local EGroupTalkEvent_Talking,			--有人开始说话
	  EGroupTalkEvent_Release,			--有人结束说话
	  EGroupTalkEvent_MeRequestErr,		--本人说话请求被拒绝
	  EGroupTalkEvent_MeRequestQueue,	--本人说话请求正在排队
	  EGroupTalkEvent_MeRequestErr2,	--本人说话请求状态错误
	  EGroupTalkEvent_Volume			--说话音频强度变更
	  = 0, 1, 2, 3, 4, 5;

--频道内位置信息及标题信息变更事件----类型枚举
local EGroupInfoEvent_MembersCountUpdate,		--群组人数发生变化
	  EGroupInfoEvent_PosUpdate,				--群组人员位置信息发生变化
	  EGroupInfoEvent_TitleUpdate				--群组标题发生变化
	  = 0, 1, 2;

--任务指令枚举
local ETaskCMD_GetGroupInfo,			--获取群组信息
	  ETaskCMD_JoinGroup,				--加入群组
	  ETaskCMD_ExitGroup				--退出群组
	  = 0, 1, 2;
--用户设置昵称与性别时状态枚举	  
local ESetUserInfo_ParametersIsNull,	--参数为空
	  ESetUserInfo_SendSucceeded,		--设置的用户信息发送到服务器成功
	  ESetUserInfo_SendFail				--设置的用户信息发送到服务器失败
	  = 100, 101, 102;
	  
--用户观看视频时的相关状态  
local EUserWatchVideo_begin =103;	--开始观看视频
local EUserAcceptVideoRequest =104;	--用户接受视频请求
local ESetUserShareVideoStateSucceeded = 105;		--设置用户的视频分享状态成功
local MMID_RESPONSE_RECORDVIDEOERROR = 10042;			--录制视频出错
--用户主动播放视频
local EUserDrivingWatchVideo = "0"; 
--用户被动播放视频
local EUserPassiveWatchVideo = "1";
--被看方退出群组，取消视频观看 
local EUserQuitGroupCancelVedio = "9";

--观看视频时通知平台的事件中状态  
local  EUserNotifyPlatEvent = 6;

--修改用户信息时通知平台的事件  
local  EModifyUserInfoNotifyPlatEvent = 1;
--上传图片http回调信息
local gUploadPictureData = nil;


--上层模块ID
local KModule_Headlist = 1;

--对讲机管理模块全局句柄
local gTalkerMgrObj = nil;

--对讲机的当前用户昵称
local gUserName = "";
--对讲机的当前用户性别
local gUserSex = "";
--对讲机的当前用户头像
local gUserHead = "";
--用户uid
local gUserID = "";
--对讲机分享计划开关
local gVedioShare = "";
--对讲机摄像头开关
local gVedioTurn = "";
--对讲机的当前用户唯一标识
local gUserKey = "";
--对讲机的当前用户是否开启自动循环录像
local gAotuLoopVideo = "";
--被直播请求次数
local gLiveWatchCount = 0;
--播放视频时本人的AID
local gSelfAid = "";
--播放视频时被观看人的AID
local gDesAid = "";
--需要发送给服务器的视频播放信息列表
local gSendToPlatMsg = {}; 
--观看视频状态，0为不观看，1为观看
local WatchVideoState = "0";

local HTTP_RESOURCE_ID = "upgradeResource";
local LOAD_PICTURE_FILE_PATH = "fs1:/Picture/"



--声明工具函数--添加任务接口
local TalkerMgr_AddTask = nil;
--声明工具函数--执行任务接口
local TalkerMgr_DoTask = nil;
--声明工具函数--添加任务接口
local TalkerMgr_AddTask = nil;
--声明工具函数--情况任务接口
local TalkerMgr_ClearAllTask = nil;
--日志封装接口
local function DebugLog( ... )
	print("wjun" .. ...);
end

--日志封装接口
local function WriteLog(str)
	local time = tiros.commfunc.CurrentTime();
	tiros.file.Writefile("fs1:/gpsLog/log.txt",time .. ":" .. str .. "\n",false);
end

--根据state生成指定Json串接口
--参数state	状态
--返回json字符串
local function GenJson_State(state)
	local t = {};
	t.state = state;
	return tiros.json.encode(t);
end


--[[
	通知平台统一接口
参数:msgid	消息id
	param1	参数1
	param2	参数2
具体消息见framework模块与其他模块对外交协议文档
]]--
local function SendMsgToPlatform( msgid, param1, param2 )
	local nFunction = tiros.moduledata.moduledata_get("framework", "pfunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "puser");
    if nFunction == nil or nUser == nil then
       return
    end
	commlib.initNotifyFun(nFunction, nUser, msgid, param1, param2);
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

--[[
	对讲机管理模块初始化
]]
local function TalkerMgr_Init()
	gVedioShare = tiros.settingconfig.get_configinfo("videoshare");
	gVedioTurn = tiros.settingconfig.get_configinfo("videoturn");
    	gAotuLoopVideo = tiros.settingconfig.get_configinfo("aotuloopvideo");
	gLiveWatchCount = tiros.settingconfig.get_configinfo("livewatchcount");

	if gTalkerMgrObj == nil then
		tiros.grouplist.create();
		tiros.grouplist.createFavourite();
		gTalkerMgrObj = {};
		gTalkerMgrObj.config = {};
		gTalkerMgrObj.status = EStatus_Idle;
		gTalkerMgrObj.atknetError = false;
		gTalkerMgrObj.group = nil;
		gTalkerMgrObj.moreGroupList = {};
		gTalkerMgrObj.TaskArray = {};
	end
end

--[[
--@描述:平台设置用户的昵称，性别，头像保存配置文件并给服务器发送HTTP消息告知
--@param  usernmae 用户昵称
		   usersex  用户性别
		   userhead 用户头像
--@return 无
--]]
local function TalkerMgr_SetUserInfo(username,usersex,userhead)
	gUserName = username;
	gUserSex  = usersex;
	gUserHead = userhead;

---[[
	gTalkerMgrObj.group.members[gTalkerMgrObj.config.aid].name = tostring(gUserName);
	gTalkerMgrObj.group.members[gTalkerMgrObj.config.aid].sex = tostring(gUserSex);
	gTalkerMgrObj.group.members[gTalkerMgrObj.config.aid].head = tostring(gUserHead);
	gTalkerMgrObj.group.members[gTalkerMgrObj.config.aid].uid = tostring(gUserID);
--]]
	DebugLog("TalkerMgr_SetUserInfo");
	SendMsgToLogic(EModifyUserInfoNotifyPlatEvent, KModule_Headlist, GenJson_State(ESetUserShareVideoStateSucceeded));
end

--获取aid成功后登录爱滔客
local function TalkerMgr_LoginByAid(dataObj)
	DebugLog("usercenter LoginByAid 1" .. tiros.json.encode(dataObj))
	--{result:”succeeded”,data:{data:{head:”xxx”,nickname:”xxx”,sex:”1”,uid:”xxxx”,aid:”xxxx”},info:{...}}}
	--保存用户及爱淘客服务器配置信息
	gTalkerMgrObj.config.aid = tostring(dataObj.data.data.aid);
	gTalkerMgrObj.config.password = dataObj.data.info.mid;
	gTalkerMgrObj.config.cfg_sp = dataObj.data.info.cfg_sp;
	gTalkerMgrObj.config.cfg_sp_port = dataObj.data.info.cfg_sp_port;
	gTalkerMgrObj.config.cfg_sp_lport = dataObj.data.info.cfg_sp_lport;
	gTalkerMgrObj.config.cfg_mdsr = dataObj.data.info.cfg_mdsr;
	gTalkerMgrObj.config.cfg_mdsr_port = dataObj.data.info.cfg_mdsr_port;
	--TalkerMgr_SetUserInfo(dataObj.data.data.nickname,dataObj.data.data.sex,dataObj.data.data.head,dataObj.data.data.uid);
	gUserName = dataObj.data.data.nickname;
	gUserSex  = dataObj.data.data.sex;
	gUserHead = dataObj.data.data.head;
	gUserID = dataObj.data.data.uid;

	gUserKey = dataObj.data.info.key;
	tiros.settingconfig.set_configinfo("userkey",gUserKey);
	--更新状态为获取Aid成功
	gTalkerMgrObj.status = EStatus_GetAidOk;
	--通知上层状态，获取Aid及服务器配置参数成功
	SendMsgToLogic(ETalkerEvent_Login, KModule_Headlist, GenJson_State(ELoginEvent_GetAidOk));
	--登录爱淘客服务器
	tiros.airtalkeemgr.configserver(dataObj.data.info.cfg_sp, dataObj.data.info.cfg_sp_port, dataObj.data.info.cfg_sp_lport, dataObj.data.info.cfg_mdsr, dataObj.data.info.cfg_mdsr_port);
	tiros.airtalkeemgr.login(gTalkerMgrObj.config.aid, gTalkerMgrObj.config.password);
	gTalkerMgrObj.status = EStatus_Login;
	--通知上层状态，正在登录爱淘客服务器
	SendMsgToLogic(ETalkerEvent_Login, KModule_Headlist, GenJson_State(ELoginEvent_LoginAid));

	tiros.moduledata.moduledata_set("framework", "uid", dataObj.data.info.mid);
	return;	
end

local function TalkerMgr_UserCenterLogin(userInfo)
		--userInfo = '{"result":"3","data":{"code":"200","state":"true","msg":"登陆成功","data":{"head":"2","nickname":"小苹果","sex":"2","uid":"MB1BJM","aid":"213922915"},"info":{"mid":"40e48ca4-a3d7-4efb-bc19-0733d1c2d12f","aid":"213922915","cfg_sp":"211.103.234.238","cfg_sp_port":"6660","cfg_mdsr":"211.103.234.238","cfg_mdsr_port":"3012","cfg_sp_lport":"6601","key":"MB1BJM","city":1,"success":true}}}'
	--收到aid信息，定制定时器
    	tiros.timer.timerabort("getAid_TimerStart");
	local dataObj = tiros.json.decode(userInfo);
	if gTalkerMgrObj.status == EStatus_Idle and dataObj ~= nil then
		TalkerMgr_LoginByAid(dataObj);
	end
end

local function TalkerMgr_UserCenterLogout()
	tiros.airtalkeemgr.logout(gTalkerMgrObj.config.aid);
	gTalkerMgrObj.status = EStatus_Idle;
end

--获取aid计时器结束回调
local function TalkerMgr_GetAid_ErrorReport_TimerExpried(sType)
    --通知上层状态，获取aid超时
    SendMsgToLogic(ETalkerEvent_Login, KModule_Headlist, GenJson_State(ELoginEvent_GetAidOverTime));    
end

--从后台服务获取AID及其爱淘客服务器配置参数
local function TalkerMgr_getAidFromUserCenter()
	DebugLog("usercenter getAidFromUserCenter 1")
	--确保初始化
	TalkerMgr_Init();
	if gTalkerMgrObj.status == EStatus_Idle  then
		DebugLog("usercenter getAidFromUserCenter 2")
		--通知上层状态，去用户中心获取aid
		SendMsgToLogic(ETalkerEvent_GroupEnter, KModule_Headlist, GenJson_State(ELoginEvent_GettingAid));
		DebugLog("usercenter getAidFromUserCenter 3")
                local userInfo = tiros.moduledata.moduledata_get("logic", "logic_userInfo");

		--userInfo = '{"result":"3","data":{"code":"200","state":"true","msg":"登陆成功","data":{"head":"2","nickname":"小苹果","sex":"2","uid":"MB1BJM","aid":"213922915"},"info":{"mid":"40e48ca4-a3d7-4efb-bc19-0733d1c2d12f","aid":"213922915","cfg_sp":"211.103.234.238","cfg_sp_port":"6660","cfg_mdsr":"211.103.234.238","cfg_mdsr_port":"3012","cfg_sp_lport":"6601","key":"MB1BJM","city":1,"success":true}}}'
		

		if nil ~= userInfo then
			DebugLog("usercenter getAidFromUserCenter 4:" .. userInfo)
			--{result:"succeeded",data:{data:{head:"xxx",nickname:"xxx",sex:"1",uid:"xxxx",aid:"xxxx"},info:{...}}}
			local dataObj = tiros.json.decode(userInfo);

			if dataObj ~= nil then
				--用户登录成功
				DebugLog("usercenter getAidFromUserCenter 5")

				--dataObj.result = "3"

				if "3" == dataObj.result then
					TalkerMgr_LoginByAid(dataObj);
					return;

				elseif "2" == dataObj.result then
					--用户正在登录中
					SendMsgToLogic(ETalkerEvent_Login, KModule_Headlist, GenJson_State(ELoginEvent_UserCenterLogining));
					DebugLog("GetAid Error, server Error 1, data = " .. data);
					tiros.timer.timerstartforlua("getAid_TimerStart",30000,TalkerMgr_GetAid_ErrorReport_TimerExpried,false);
					return;
				end
			end	
		end
		tiros.timer.timerstartforlua("getAid_TimerStart",30000,TalkerMgr_GetAid_ErrorReport_TimerExpried,false);
		gTalkerMgrObj.status = EStatus_Idle;
		--通知上层状态，获取Aid及服务器配置参数失败
		SendMsgToLogic(ETalkerEvent_Login, KModule_Headlist, GenJson_State(ELoginEvent_GetAidErr));
		--重试？
	end
end

--[[
根据群组配置信息，请求加入群组
参数groupcfg = {} 
	.groupid
	.title
	.membercount
]]--
local function TalkerMgr_realJoinGroupByCfg( groupcfg )
	DebugLog("TalkerMgr_realJoinGroupByCfg 请求加入群组 json = " .. tiros.json.encode(groupcfg));
	if gTalkerMgrObj.status >= EStatus_LoginOk then
		--当前爱淘客环境必须为登录成功
		DebugLog("TalkerMgr_realJoinGroupByCfg 请求加入群组 当前爱淘客环境必须为登录成功");
		if gTalkerMgrObj.status == EStatus_JoinGroup or gTalkerMgrObj.status == EStatus_JoinGroupOk then
			--如果当前已经在一个群组或者正在加入一个群组，则先退出当前群组
			DebugLog("TalkerMgr_realJoinGroupByCfg 请求加入群组 如果当前已经在一个群组或者正在加入一个群组，则先退出当前群组");
			tiros.airtalkeemgr.SessionBye();
		end
		DebugLog("TalkerMgr_realJoinGroupByCfg 请求加入群组 2");
		if gTalkerMgrObj.atknetError == false then
			--爱淘客环境正常	
			tiros.airtalkeemgr.SessionCall(groupcfg.curGroupid);
			--更新状态
			gTalkerMgrObj.status = EStatus_JoinGroup;
			--通知上层状态，正在加入爱淘客群组
			SendMsgToLogic(ETalkerEvent_GroupEnter, KModule_Headlist, GenJson_State(EGroupEnterEvent_Entering));
		else
			--如果当前爱淘客网络出现问题，则放入任务当中
			--创建任务队列,等待爱淘客登录成功之后执行
			local task = {};
			task.cmd = ETaskCMD_JoinGroup;
			task.data = tiros.base.common.CopyTable(groupcfg);
			--目前只保留一个执行任务
			TalkerMgr_AddTask(task);
		end
		return true;
	else
		--没有登录成功，则返回false
		DebugLog("TalkerMgr_realJoinGroupByCfg 请求加入群组 没有登录成功，则返回false ");
		return false;
	end
end

--退出群组回调
local function TalkerMgr_quitGroupHttpEvent(id,state,data)
	DebugLog("TalkerMgr_quitGroupHttpEvent Event!!!");
	if state == 1 then	--成功
		DebugLog("quitGroupHttpEvent ok!!! data = ".. data);
	else
		DebugLog("quitGroupHttpEvent error!");
	end
end

--退出当前群组
local function TalkerMgr_QuitGroup()
	local t1 = {};
	t1.state = EGroupTalkEvent_Release;
	SendMsgToLogic(ETalkerEvent_GroupTalk, KModule_Headlist, tiros.json.encode(t1));
	--gTalkerMgrObj.status = EStatus_LoginOk;
	--情况当前任务列表
	DebugLog("wjun---TalkerMgr_QuitGroup  退出爱淘客频道1");
	TalkerMgr_ClearAllTask();
	if gTalkerMgrObj.status == EStatus_GetGroupid then
		--如果当前正在请求获取群组信息，则停止请求
		tiros.base.http.HttpAbort("TalkerMgr_GetGroupid");
		gTalkerMgrObj.status = EStatus_LoginOk;
		DebugLog("wjun---TalkerMgr_QuitGroup  退出爱淘客频道2");
	elseif gTalkerMgrObj.status >= EStatus_JoinGroup or gTalkerMgrObj.status >= EStatus_JoinGroupOk then
		--如果当前已经在一个群组或者正在加入一个群组，则先退出当前群组
		if gTalkerMgrObj.atknetError == false  then
			--退出群组时，如果网络正常，则直接退出，否则必须等到爱淘客恢复登录成功之后再退出
			tiros.airtalkeemgr.SessionBye();
			gTalkerMgrObj.status = EStatus_LoginOk;
			DebugLog("wjun---TalkerMgr_QuitGroup  退出爱淘客频道3");
		else
			--不正常的情况下，需要保存当前任务，等待网络恢复的时候退出
			local task = {};
			task.cmd = ETaskCMD_ExitGroup;
			task.data = nil;
			TalkerMgr_AddTask(task);
			--gTalkerMgrObj.status = EStatus_LoginOk;
			DebugLog("wjun---TalkerMgr_QuitGroup  退出爱淘客频道4");
		end
		--停止位置上报
		tiros.airtalkeemgr.StopUploadPositionInfo();
		--通知CDC服务器，退出当前群组
		if gTalkerMgrObj.group.curGroupid ~= nil then
			local opt = {};
			opt.form = "logic";
			opt.header = {};
			opt.header["aid"] = gTalkerMgrObj.config.aid;
			opt.header["groupid"] = gTalkerMgrObj.group.curGroupid;
			opt.method = "GET";
			--不处理返回消息
			--tiros.base.http.HttpSend("TalkerMgr_QuitGroup", TalkerMgr_quitGroupHttpEvent,"quitGroup", opt, "http://server.xiaocheben.com/cdcServer/exitGroup.htm");
			tiros.base.http.HttpSend("TalkerMgr_QuitGroup", TalkerMgr_quitGroupHttpEvent,"quitGroup", opt, nil);
			DebugLog("TalkerMgr_QuitGroup 退出群组 id = " .. gTalkerMgrObj.group.curGroupid );
		end
	end
	--退出群组，则清空当前群组所以数据
	gTalkerMgrObj.group = nil;
end
------------------------------------------------------------------------------------------------------------------------------
--设置用户信息回调，用于通知平台
local function TalkerMgr_SetUserInfoHttpEvent(id,state,data)
	if state == 1 then	--成功
		local dataObj = {};
		dataObj = tiros.json.decode(data);

		if dataObj.success == true then	
			--平台成功设置用户信息后，修改gTalkerMgrObj.group.members用户列表里的本人信息
			gTalkerMgrObj.group.members[gTalkerMgrObj.config.aid].share = tostring(gVedioShare);
			gTalkerMgrObj.group.members[gTalkerMgrObj.config.aid].camera = tostring(gVedioTurn);
			SendMsgToLogic(EModifyUserInfoNotifyPlatEvent, KModule_Headlist, GenJson_State(ESetUserInfo_SendSucceeded));
		else
			SendMsgToLogic(EModifyUserInfoNotifyPlatEvent, KModule_Headlist, GenJson_State(ESetUserInfo_SendFail));
		end
	else
		SendMsgToLogic(EModifyUserInfoNotifyPlatEvent, KModule_Headlist, GenJson_State(ESetUserInfo_SendFail));	
	end
end
-------------------------------------------------------------------------------------------------------------------------------
--获取频道内群组信息回调
local function TalkerMgr_getSpecialGroupInfoHttpEvent(id,state,data)
	DebugLog("getGroupidHttpEvent Event!!!");
	if state == 1 then	--成功
		DebugLog("getGroupidHttpEvent ok!!! data = ".. data);
		local dataObj = tiros.json.decode(data);
		if dataObj.success == true then	
			--清空之前群组数据
			gTalkerMgrObj.group = {};
			gTalkerMgrObj.group.curGroupid = dataObj.data.groupid;
			gTalkerMgrObj.group.type = dataObj.data.type;
			gTalkerMgrObj.group.groupnumber = dataObj.data.groupnumber;
			gTalkerMgrObj.group.membercount = dataObj.data.num;
			gTalkerMgrObj.group.title = dataObj.data.groupname;
			gTalkerMgrObj.group.members = dataObj.data.aids;
			gTalkerMgrObj.group.maxlon = dataObj.data.maxlon;
			gTalkerMgrObj.group.maxlat = dataObj.data.maxlat;
			gTalkerMgrObj.group.leastlon = dataObj.data.leastlon;
			gTalkerMgrObj.group.leastlat = dataObj.data.leastlat;
			--进入群组时如果该群组number已经在数据库的收藏表中存在， 则更新所有number记录中的title字段
			tiros.grouplist.AgainModifyTitle(gTalkerMgrObj.group.groupnumber,gTalkerMgrObj.group.title);

			--更新状态为获取群组ID成功
			gTalkerMgrObj.status = EStatus_GetGroupidOk;
			DebugLog("getGroupidHttpEvent success!!! SessionCall: " .. gTalkerMgrObj.group.curGroupid);
			--通知上层状态，获取群组信息成功
			local s = {};
			s.state = EGroupEnterEvent_GetGidOk;
			s.title = gTalkerMgrObj.group.title;
			s.membercount = gTalkerMgrObj.group.membercount;
			s.grouptype = gTalkerMgrObj.group.type;
			s.groupnumber = gTalkerMgrObj.group.groupnumber;
			SendMsgToLogic(ETalkerEvent_GroupEnter, KModule_Headlist, tiros.json.encode(s));
			--加入群组
			TalkerMgr_realJoinGroupByCfg(gTalkerMgrObj.group);
			return;
		else
			DebugLog("getGroupidHttpEvent Error!!! 1");
		end
	else
		DebugLog("getGroupidHttpEvent Error!!! 2");
			TalkerMgr_realJoinGroupByCfg(gTalkerMgrObj.group);
	end
	--通知上层状态，获取群组信息失败
	SendMsgToLogic(ETalkerEvent_GroupEnter, KModule_Headlist, GenJson_State(EGroupEnterEvent_GetGidErr));
	--获取群组id失败，重试
end

--[[
    加入群组时，如果当前爱淘客引擎处于网络错误过程中，则异步计时通知上层加入群组失败
]]
--加入群组计时器结束回调
local function TalkerMgr_joinGroup_ErrorReport_TimerExpried(sType)
    --通知上层状态，获取群组信息失败
    SendMsgToLogic(ETalkerEvent_GroupEnter, KModule_Headlist, GenJson_State(EGroupEnterEvent_GetGidErr));    
end

--加入群组计时器开始接口
local function TalkerMgr_joinGroup_ErrorReport_TimerStart()
    tiros.timer.timerabort("joinGroup_TimerStart");
    tiros.timer.timerstartforlua("joinGroup_TimerStart",1000,TalkerMgr_joinGroup_ErrorReport_TimerExpried,false);
end

--加入群组计时器取消接口
local function TalkerMgr_joinGroup_ErrorReport_TimerCancel()
    tiros.timer.timerabort("joinGroup_TimerStart");
end
--[[
--@描述:其他lua文件获取用户的昵称，性别，头像
--@param  无
--@return gUserName 用户昵称
	      gUserSex  用户性别
	      gUserHead  用户头像
--]]
local function TalkerMgr_GetUserInfo()
	return gUserName,gUserSex,gUserHead;
end
--[[
--@描述:平台获取用户的昵称
--@param  无
--@return gUserName 用户昵称
--]]
local function TalkerMgr_GetUserName()
	if gUserName == "0" then 
		--昵称获取失败
		return "103";
	else
		return gUserName;
	end
end
--[[
--@描述:平台获取用户的性别
--@param  无
--@return gUserSex  用户性别
--]]
local function TalkerMgr_GetUserSex()
	return gUserSex;
end
--[[
--@描述:平台获取用户的头像
--@param  无
--@return gUserHead  用户头像
--]]
local function TalkerMgr_GetUserHead()
	return gUserHead;
end

--[[
--@描述:平台设置用户是否共享视频
--@param  status 状态：1不共享，2共享
--@return 无
--]]
local function TalkerMgr_SetVideoShare(status)
	gVedioShare = status;
	tiros.settingconfig.set_configinfo("videoshare",status);
	SendMsgToLogic(EModifyUserInfoNotifyPlatEvent, KModule_Headlist, GenJson_State(ESetUserShareVideoStateSucceeded));

	local opt = {};
	opt.form = "logic";
	opt.method = "GET";
	opt.header = {};
	
	local sendDataLast = {};
	sendDataLast["share"] = tostring(status);
	sendDataLast["camera"] = tostring(tiros.settingconfig.get_configinfo("videoturn"));
	sendDataLast["aid"] = gTalkerMgrObj.config.aid;
	opt.data = sendDataLast;

	tiros.base.http.HttpSend("TalkerMgr_SetUserInfo", TalkerMgr_SetUserInfoHttpEvent,"setUserInfo", opt, nil);
end
--[[
--@描述:获取用户是否共享视频
--@param  无
--@return  gVideoShare：1不共享，2共享
--]]
local function TalkerMgr_GetVideoShare()
	return 	tiros.settingconfig.get_configinfo("videoshare");
end

--[[
--@描述:平台设置用户是否打开摄像头
--@param  status 状态：2打开摄像头，1关闭摄像头
--@return 无
--]]
local function TalkerMgr_SetVideoTurn(status)
	gVedioTurn = status;
	tiros.settingconfig.set_configinfo("videoturn",status);
	--SendMsgToLogic(EModifyUserInfoNotifyPlatEvent, KModule_Headlist, GenJson_State(ESetUserShareVideoStateSucceeded));
	local sendDataLast = {};
	sendDataLast["share"] = tostring(tiros.settingconfig.get_configinfo("videoshare"));
	sendDataLast["camera"] = tostring(status);
	sendDataLast["aid"] = gTalkerMgrObj.config.aid;
	opt.data = sendDataLast;
	tiros.base.http.HttpSend("TalkerMgr_SetUserInfo", TalkerMgr_SetUserInfoHttpEvent,"setUserInfo", opt, nil);
end

--[[
--@描述:获取用户是否打开摄像头
--@param  无
--@return  gVideoShare：1打开0关闭
--]]
local function TalkerMgr_GetVideoTurn()
	return 	tonumber(tiros.settingconfig.get_configinfo("videoturn"));
end

--[[
--@描述:平台设置用户是否自动循环录像
--@param  status 状态：1不自动，2自动
--@return 无
--]]
local function TalkerMgr_SetAotuLoopVideo(status)
	tiros.settingconfig.set_configinfo("aotuloopvideo",status);
	gAotuLoopVideo = status;
end

--[[
--@描述:获取用户是否自动循环录像
--@param  无
--@return  gAotuLoopVideo：1不自动，2自动
--]]
local function TalkerMgr_GetAotuLoopVideo()
	return gAotuLoopVideo;
end

--[[
--@描述:用户观看其他人的视频
--@param  SourceAid本人（观看者）爱淘客ID,DesAid目的端（被观看者）爱淘客ID
--@return  无
--]]
local function TalkerMgr_WatchVideo(SourceAid,DesAid)
	WatchVideoState = "1";
	print("jiangdezheng 1请求方 ");
	if SourceAid ~= "" then
		--播放视频时本人的AID
		gSelfAid = SourceAid;
		--播放视频时被观看人的AID
		gDesAid = DesAid;
	else
		--什么都不做
	end
	--SendMsgToLogic(1, KModule_Headlist, GenJson_State(EUserWatchVideo_begin));
	
	local t = {};
	--用户接受视频请求标示号s
	t.customflag = EUserAcceptVideoRequest;	
	t.sourceAid = gSelfAid;
	t.desAid = gDesAid;
	tiros.airtalkeemgr.MessageSendToOtherUser( t, gDesAid );
	
end

local function TalkerMgr_NotifyVideoIDHttpEvent(id,state,data)
	if state == 1 then
		print("TalkerMgr_NotifyVideoIDHttpEvent is :" .. data);
		local dataObj = {};
		dataObj = tiros.json.decode(data);
		if dataObj.code == "200" then	--成功
			print("jiangdezheng  6被请求方，向服务器发送播放ID succeeded:");
		end
	end
end
--[[
--@描述:平台告知的视频播放ID
--@param  SourceAid本人（观看者）爱淘客ID , VideoID 视频观看ID
--@return  0:成功，1：失败
--]]
local function TalkerMgr_NotifyVideoID(VideoID)
	--VideoRequestInfo["sourceAid"] = tostring(t.msg.sourceAid);
	--VideoRequestInfo["desAid"] = tostring(t.msg.desAid);
	print("jiangdezheng 4被请求方，收到平台播放ID ");
	local SendToServerMsg = {};
	local newMsg = {};
	for key, value in pairs(gSendToPlatMsg) do  
		--if  key == SourceAid then
			SendToServerMsg = {};
			SendToServerMsg["zmid"] = value.desAid;
			SendToServerMsg["qmid"] = value.sourceAid;
			SendToServerMsg["vid"] = VideoID;
			table.insert(newMsg,SendToServerMsg);
			--gSendToPlatMsg.key  = nil;
		--end
	end
	gSendToPlatMsg = {};
	if SendToServerMsg.vid == nil then
		return 1;
	end
	--发送给服务器
	local opt = {};
	opt.form = "logic";
	opt.header = {};
	opt.method = "POST";
	opt.data = {};
	
	local SendMsg  = {};
	SendMsg.AllReqData = newMsg;
	SendMsg.bitrate = "";
	SendMsg.fps = "";
	SendMsg.size = "";
	SendMsg.Status = EUserPassiveWatchVideo;
	
	
	opt.data.data =  tiros.json.encode(SendMsg);
	--table.insert(opt.data , SendToServerMsg);
	local tempj = tiros.json.encode(opt);
	print("jiangdezheng  5被请求方，向服务器发送播放ID:" .. tempj);
	
	tiros.base.http.HttpSend("TalkerMgr_RequestURL", TalkerMgr_NotifyVideoIDHttpEvent,"RequestURL", opt, nil);
	--tiros.base.http.HttpSend("TalkerMgr_RequestURL", TalkerMgr_NotifyVideoIDHttpEvent,"RequestURL", opt, "http://192.168.3.197:9080/cdcRegister/transmit.htm");
	return 0;
end

--[[
--@描述:平台告知的取消视频播放
--@param  无
--@return  无
--]]
local function TalkerMgr_CancelWatchVideo()
	WatchVideoState = "0";
end

--[[
 @描述:平台告知的录制视频出现异常
 @param:errorCode 错误码
 @return:无
--]]
local function TalkerMgr_RecordVideoError(errorCode)
	local msg = {};
	msg.customflag = MMID_RESPONSE_RECORDVIDEOERROR;
	msg.x = errorCode;
	tiros.airtalkeemgr.MessageSendToGroup(msg, gTalkerMgrObj.group.curGroupid);
end

--------------------------------------------------------------------------------------------------------------------
--[[
--根据频道唯一标示，加入指定频道服务器群组信息
参数：groupcfg {}
		.groupid
		.groupnumber
		.grouptype
]]
local function TalkerMgr_joinGroup( groupcfg )
	DebugLog("grouplist getGroupidByType begin--------->1 ");
	if gTalkerMgrObj.status == EStatus_Idle then
		DebugLog("grouplist getGroupidByType begin-------EStatus_Idle  ");
		--如果爱淘客还没有获取Aid，则先获取Aid
		TalkerMgr_getAidFromUserCenter();
	elseif gTalkerMgrObj.status == EStatus_GetAidOk then
		DebugLog("grouplist getGroupidByType begin-------EStatus_GetAidOk  ");
		--如果爱淘客还没有开始登录，则开始登录
		--登录爱淘客服务器
		tiros.airtalkeemgr.login(gTalkerMgrObj.config.aid, gTalkerMgrObj.config.password);
		gTalkerMgrObj.status = EStatus_Login;
		--通知上层状态，正在登录爱淘客服务器
		SendMsgToLogic(ETalkerEvent_Login, KModule_Headlist, GenJson_State(ELoginEvent_LoginAid));
	elseif gTalkerMgrObj.atknetError == true then
		--如果爱淘客网络错误，则等待恢复
		DebugLog("grouplist getGroupidByType begin-------atknetError == true  ");
		--如果当前在一个群组内
		if gTalkerMgrObj.status == EStatus_JoinGroupOk and gTalkerMgrObj.group ~= nil then
			if gTalkerMgrObj.group.type ~= groupcfg.grouptype then
				TalkerMgr_QuitGroup();
			elseif groupcfg.grouptype ~= "2" and gTalkerMgrObj.group.groupnumber ~= groupcfg.groupnumber then
				TalkerMgr_QuitGroup();
			end
		end
        	TalkerMgr_joinGroup_ErrorReport_TimerStart();
        	return;
	elseif gTalkerMgrObj.status >= EStatus_LoginOk then
		DebugLog("grouplist getGroupidByType begin-------request 1");
		--爱淘客环境正常
		--进入新频道,清空及退出之前群组数据
		TalkerMgr_QuitGroup();
		DebugLog("grouplist getGroupidByType begin-------request 2");
		--获取群组信息		
		local opt = {};
		opt.form = "logic";
		opt.header = {};
		opt.header["groupid"] = groupcfg.groupid;
		opt.header["type"] = groupcfg.grouptype;
		opt.header["groupnumber"] = groupcfg.groupnumber;
		opt.header["aid"] = gTalkerMgrObj.config.aid;
		opt.method = "GET";
		
		
		local sendDataLast = {};
		sendDataLast["name"] = gUserName;
		sendDataLast["sex"] = gUserSex;
		sendDataLast["head"] = gUserHead;
		sendDataLast["groupid"] = groupcfg.groupid;
		sendDataLast["type"] = groupcfg.grouptype;
		sendDataLast["groupnumber"] = groupcfg.groupnumber;
		sendDataLast["aid"] = "213923402";
		sendDataLast["uid"] = "MB1ABZ";
		sendDataLast["mobileid"] = "e90a39c3-e375-4b09-8d96-d32a4eb2c6f9";
		sendDataLast["selflon"] = 419028675;
		sendDataLast["selflat"] = 143805905;
		sendDataLast["speed"] = 0;
		sendDataLast["orien"] = 0;
		opt.data = sendDataLast;
		
		--tiros.base.http.HttpSend("TalkerMgr_GetGroupid", TalkerMgr_getSpecialGroupInfoHttpEvent,"getGroupid", opt, "http://192.168.3.90:8086/cdcServer/getGroupInfo.htm");
		--tiros.base.http.HttpSend("TalkerMgr_GetGroupid", TalkerMgr_getSpecialGroupInfoHttpEvent,"getGroupid", opt, "http://server.xiaocheben.com/cdcServer/getGroupInfo.htm");
		tiros.base.http.HttpSend("TalkerMgr_GetGroupid", TalkerMgr_getSpecialGroupInfoHttpEvent,"getGroupid", opt, nil );
		gTalkerMgrObj.status = EStatus_GetGroupid;
		DebugLog("grouplist getGroupidByType begin-------request 3 EStatus_GetGroupid");
		--通知上层状态，正在获取群组信息
		SendMsgToLogic(ETalkerEvent_GroupEnter, KModule_Headlist, GenJson_State(EGroupEnterEvent_GettingGid));
		return;
	end
	--创建任务队列,等待爱淘客登录成功之后执行
	local task = {};
	task.cmd = ETaskCMD_GetGroupInfo;
	task.data = tiros.base.common.CopyTable(groupcfg);
	TalkerMgr_AddTask(task);
	DebugLog("grouplist getGroupidByType begin-------end create task！");
end

--获取公共频道服务器群组信息
local function TalkerMgr_joinPublicGroup()
	DebugLog("TalkerMgr_joinPublicGroup begin--------->1 ");
	print("11yue4hao  TalkerMgr_joinPublicGroup");
	local tPublicGroupCfg = {};
	tPublicGroupCfg.groupid = "";
	tPublicGroupCfg.groupnumber = "";
	tPublicGroupCfg.grouptype = "2";			 --大区
	TalkerMgr_joinGroup(tPublicGroupCfg);
	return false;
end



--[[
结束PTT实时对讲
返回：无
]]
local function TalkerMgr_talkRelease()
	if gTalkerMgrObj.status == EStatus_JoinGroupOk then
		--加入群组成功，则允许说话请求
		tiros.airtalkeemgr.TalkRequest(0);
	end
end

--[[
获取群组信息
返回：群组信息的json串
]]
local function TalkerMgr_GetGroupInfo()
	if gTalkerMgrObj ~= nil then
		local tTalkerMgrInfo = tiros.base.common.CopyTable(gTalkerMgrObj);
		if gTalkerMgrObj.group ~= nil then
			if gTalkerMgrObj.group.members ~= nil then
				tTalkerMgrInfo.group.members = {};
				local index = 1;
				local member = nil;
				for k,v in pairs(gTalkerMgrObj.group.members) do
					member = {};
					member.aid = k;
					member.data = v;
					tTalkerMgrInfo.group.members[index] = member;
					index = index + 1;
				end
			end
		end
		DebugLog("TalkerMgr_GetGroupInfo json = " .. tiros.json.encode(tTalkerMgrInfo));
		return tiros.json.encode(tTalkerMgrInfo);
	end
	return "{}";
end

--[[
获取更多群组列表回调
返回：无
]]
--[[
local function TalkerMgr_getMoreGroupListHttpEvent(id,state,data)
	DebugLog("TalkerMgr_getMoreGroupListHttpEvent Event!!!");
	if state == 1 then	--成功
		DebugLog("getMoreGroupListHttpEvent ok!!! data = ".. data);
		local dataObj = tiros.json.decode(data);
		if dataObj.success == true then	
			DebugLog("getMoreGroupListHttpEvent success!");
			gTalkerMgrObj.moreGroupList = {};
			if dataObj.groups ~= nil then
				local index = 1;
				local groupinfo = nil;
				for k,v in pairs(dataObj.groups) do
					groupinfo = {};
					groupinfo.groupid = v.groupid;
					groupinfo.groupnumber = v.groupnumber;
					groupinfo.grouptype = v.type;
					groupinfo.groupname = v.groupname;
					groupinfo.memberscount = v.count;
					gTalkerMgrObj.moreGroupList[index] = groupinfo;
					index = index + 1;
				end
				print("grouplist insert begin 1111111111");
				tiros.grouplist.insert(gTalkerMgrObj.moreGroupList);
				print("grouplist insert end 22222222222");
			else

			end
		end
	else
		DebugLog("getMoreGroupListHttpEvent Error!!!");
	end
end
--]]
--获取更多群组列表请求
--[==[
local function TalkerMgr_getMoreGroupList(type, page, roomFrom, roomTo)
	DebugLog("TalkerMgr_getMoreGroupList request begin-------1");
	if (type ~= nil) then
		tiros.base.moduledata.SetTempData("logic", "buttonType", type);
	end
	if (page ~= nil) then
		tiros.base.moduledata.SetTempData("logic", "pageType", page);
	end
	if (roomFrom ~= nil) then
		tiros.base.moduledata.SetTempData("logic", "roomFrom", roomFrom);
	end
	if (roomTo ~= nil) then
		tiros.base.moduledata.SetTempData("logic", "roomTo", roomTo);
	end
	local opt = {};
	opt.form = "logic";
	opt.header = {};
	opt.method = "GET";
	tiros.base.http.HttpSend("TalkerMgr_getMoreGroupList", TalkerMgr_getMoreGroupListHttpEvent,"getMoreGroupList", opt, "http://server.xiaocheben.com/cdcServer/getGroups.htm");
	gTalkerMgrObj.status = EStatus_GetGroupid;
	DebugLog("TalkerMgr_getMoreGroupList request end-------2");
	--通知上层状态，正在获取群组信息
	SendMsgToLogic(ETalkerEvent_GroupEnter, KModule_Headlist, GenJson_State(EGroupEnterEvent_GettingGid));
end
--]==]
--[[
请求PTT实时对讲
返回：true：请求被接受
	 false：请求状态错误
]]
local function TalkerMgr_talkRequest()
	if gTalkerMgrObj.status == EStatus_JoinGroupOk then
		--加入群组成功，则允许说话请求
		if gTalkerMgrObj.atknetError == false then
			tiros.airtalkeemgr.TalkRequest(1);
			return true;
		end
	end
	return false;
end
--[[
 @描述:图片下载回调
 @param:ptype 回调对象句柄
 @param:event 回调事件类型
 @param:param1 回调事件传递参数1
 @param:param2 回调事件传递参数2
 @return:无
--]]
local function LoadPictureHttpEvent(ptype, event, param1, param2)
	print("jiangdezheng 5请求方收到服务器URL:" );
	if event == 1 then
	elseif event == 2 then --回调状态
		if param1 ~= 200  then--http状态出错
			tiros.http.httpabort(ptype);
		end
		if filelib.fexist(LOAD_PICTURE_FILE_PATH .. gDesAid .. ".jpg") then
			tiros.file.Removefile(LOAD_PICTURE_FILE_PATH .. gDesAid .. ".jgp");
		end
	elseif event == 3 then  --持续下载
		--LOAD_PICTURE_FILE_PATH
		tiros.file.Writefile(LOAD_PICTURE_FILE_PATH .. gDesAid .. ".jpg",string.sub(param2, 1, param1),false);
	elseif event == 4 then --完成
		--下载完成
		--tiros.file.R
		print("jiangdezheng  124");
		local toPlat = {};
		toPlat.state = "12";
		toPlat.picPath = LOAD_PICTURE_FILE_PATH .. gDesAid .. ".jpg";
		SendMsgToLogic(6, KModule_Headlist, tiros.json.encode(toPlat));
		print("jiangdezheng  preview pic:" .. tiros.json.encode(toPlat));
	elseif event == 5 then
		SendMsgToLogic(6, KModule_Headlist, GenJson_State("13"));
	end
end

--[[
 @描述:图片下载回调
 @param:msgInfo table {sourceAid:"手机爱滔客id",desAid:"被请求平板爱滔客id"}
 @return:无
--]]
local function ReceiveWactchMsg(msgInfo)
		print("jiangdezheng clientrequest 44");
	--保存被直播次数
	gLiveWatchCount = gLiveWatchCount + 1;
	tiros.settingconfig.set_configinfo("livewatchcount", gLiveWatchCount);

	--存在本地
	local VideoRequestInfo = {};
	VideoRequestInfo["sourceAid"] = tostring(msgInfo.sourceAid);
	VideoRequestInfo["desAid"] = tostring(msgInfo.desAid);
	--存在本地
	gSendToPlatMsg[VideoRequestInfo["sourceAid"]] = VideoRequestInfo;
	local VideoRequestToPlat = {};
	VideoRequestToPlat["state"] = "3";
	VideoRequestToPlat["localAID"] = gTalkerMgrObj.config.aid;
	VideoRequestToPlat["liveWatchCount"] = gLiveWatchCount;

		print("jiangdezheng clientrequest 55" .. tiros.json.encode(VideoRequestToPlat));

	--告知平台开始录制视频，参数中带有请求端的AID
	SendMsgToLogic(EUserNotifyPlatEvent, KModule_Headlist, tiros.json.encode(VideoRequestToPlat));
		print("jiangdezheng clientrequest 66");
	print("jiangdezheng 3被请求方，通知平台录制完毕" .. tiros.json.encode(VideoRequestToPlat));
end

--[[
修改当前群组标题回调
返回：无
]]
--[===[
local function TalkerMgr_modifyGroupTitleHttpEvent(id,state,data)
	DebugLog("TalkerMgr_modifyGroupTitleHttpEvent Event!!!");
	if state == 1 then	--成功
		DebugLog("modifyGroupTitleHttpEvent ok!!! data = ".. data);
		local dataObj = tiros.json.decode(data);
		if dataObj.success == true then	
			DebugLog("modifyGroupTitleHttpEvent success!");
		else
			DebugLog("modifyGroupTitleHttpEvent false!");
		end
	else
		DebugLog("modifyGroupTitleHttpEvent error!");
	end
end

--修改当前群组标题
local function TalkerMgr_modifyGroupTitle(title)
	if gTalkerMgrObj.status >= EStatus_JoinGroupOk then
		--如果当前在群组中才允许修改
		local opt = {};
		opt.form = "logic";
		opt.header = {};
		opt.header["groupname"] = title;
		opt.header["groupid"] = gTalkerMgrObj.group.curGroupid;
		opt.header["groupnumber"] = gTalkerMgrObj.group.groupnumber;
		opt.header["type"] = gTalkerMgrObj.group.grouptype;
		opt.method = "GET";
		tiros.base.http.HttpSend("TalkerMgr_modifyGroupTitle", TalkerMgr_modifyGroupTitleHttpEvent,"modifyGroupTitle", opt, "http://server.xiaocheben.com/cdcServer/updateGroup.htm");
		DebugLog("TalkerMgr_getMoreGroupList request end-------2");
		--通知上层状态，正在获取群组信息
		--SendMsgToLogic(ETalkerEvent_GroupEnter, KModule_Headlist, GenJson_State(EGroupEnterEvent_GettingGid));
			return true;
	end
	return false;
end
--]===]
--爱淘客管理模块系统回调
local function TalkerMgr_AirtalkeeResponseEvent( msg )
	--如果收到爱淘客消息，则暂定为网络恢复
	--gTalkerMgrObj.atknetError = false;
	local t = tiros.json.decode(msg)
	if type(t) == "table" then
		if t.type == 10014 then	--登录状态消息
			if t.success == true then
				DebugLog("wjun---AirtalkeeResponseEvent login msg success! ");
                gTalkerMgrObj.atknetError = false;
				if gTalkerMgrObj.status < EStatus_LoginOk then
					--如果是首次爱淘客登录成功，则更新状态为爱淘客登录成功
					gTalkerMgrObj.status = EStatus_LoginOk;
					DebugLog("wjun---AirtalkeeResponseEvent fisrt login msg success! ");
					--通知上层状态，爱淘客登录成功
					SendMsgToLogic(ETalkerEvent_Login, KModule_Headlist, GenJson_State(ELoginEvent_LoginAidOk));
				else
					DebugLog("wjun---AirtalkeeResponseEvent auto relogin msg success! ");
				end
				--执行任务队列中的任务
				TalkerMgr_DoTask();
				
	--[[
				if gTalkerMgrObj.group == nil then
					--如果是当前没有进入群组，则默认进入公共频道
					TalkerMgr_joinPublicGroup();
				elseif	gTalkerMgrObj.status == EStatus_JoinGroup then
					--如果当前状态为正在加入群组，则重新加入群组
					tiros.airtalkeemgr.SessionCall(gTalkerMgrObj.group.curGroupid);
				end
	]]
			else
				DebugLog("wjun---AirtalkeeResponseEvent login msg error! ");
				--登录失败，重置状态为获取AID成功
				gTalkerMgrObj.status = EStatus_GetAidOk;
				--通知上层状态，爱淘客登录失败
				SendMsgToLogic(ETalkerEvent_Login, KModule_Headlist, GenJson_State(ELoginEvent_LoginAidErr));
				--是否重新登录?
			end

		elseif t.type == 10015 then		--网络出错消息
			--更新爱淘客服务器网络状态为出错
			gTalkerMgrObj.atknetError = true;
			--通知上层状态，网络异常
			SendMsgToLogic(ETalkerEvent_Login, KModule_Headlist, GenJson_State(ELoginEvent_NetError));
			--如果当前用户在频道内，则通知logic播放网络异常TTS
			if gTalkerMgrObj.status == EStatus_JoinGroupOk then
				print("wjun----->atknetError play TTS 失去与服务器连接");
				SendMsgToLogic(ETalkerEvent_NetError, KModule_Headlist, "失去与服务器连接");
			end

		elseif t.type == 10016 then		--临时或群组会话状态事件
			--如果群组id为当前的群组id
			if gTalkerMgrObj.group == nil then
				DebugLog("wjun---AirtalkeeResponseEvent 当前不处于群组中，抛弃群组消息！");
			elseif t.groupid == gTalkerMgrObj.group.curGroupid then
				if t.status == 0 then 				--临时或群组会话正在建立中
					DebugLog("wjun---AirtalkeeResponseEvent 临时或群组会话正在建立中 ");
					gTalkerMgrObj.status = EStatus_JoinGroup;
					--通知上层状态，正在进入爱淘客群组中
					SendMsgToLogic(ETalkerEvent_GroupEnter, KModule_Headlist, GenJson_State(EGroupEnterEvent_Entering));
				elseif t.status == 1 then 			--临时或群组会话建立完成
					if t.msg  == true then 			--建立会话成功
						DebugLog("wjun---AirtalkeeResponseEvent 临时或群组建立会话成功1");
						gTalkerMgrObj.status = EStatus_JoinGroupOk;
						tiros.airtalkeemgr.UploadPositionInfo();
						--通知上层状态，进入爱淘客群组成功
						print("11yue4hao  进入爱淘客群组成功");
						SendMsgToLogic(ETalkerEvent_GroupEnter, KModule_Headlist, GenJson_State(EGroupEnterEvent_EnterOk));
						gTalkerMgrObj.group.fistIn = true;
					else 							--建立会话失败
						DebugLog("wjun---AirtalkeeResponseEvent 临时或群组建立会话失败");
						gTalkerMgrObj.status = EStatus_GetGroupidOk;
						--通知上层状态，进入爱淘客群组失败
						SendMsgToLogic(ETalkerEvent_GroupEnter, KModule_Headlist, GenJson_State(EGroupEnterEvent_EnterErr));
				        --是否重试?
					end
				elseif t.status == 2 then 			--临时或群组会话结束事件
					DebugLog("wjun---AirtalkeeResponseEvent 临时或群组会话结束事件");
					tiros.airtalkeemgr.StopUploadPositionInfo();
					--gTalkerMgrObj.status = EStatus_LoginOk;
					--通知上层状态，爱淘客群组退出
					--SendMsgToLogic(ETalkerEvent_GroupEnter, KModule_Headlist, GenJson_State(EGroupEnterEvent_Quit));
				elseif t.status == 3 then 			--临时或群组会话用户被踢
					DebugLog("wjun---AirtalkeeResponseEvent 临时或群组会话用户被踢! ");
					--如果被踢的人是自己，需要重新加入
					if t.aid == gTalkerMgrObj.config.aid then
						--通知上层当前用户从频道内被踢出，准备重新进入
						SendMsgToLogic(ETalkerEvent_GroupEnter, KModule_Headlist, GenJson_State(EGroupEnterEvent_Kickout));
						local tgroupcfg = {};
						tgroupcfg.groupid = gTalkerMgrObj.group.curGroupid;
						tgroupcfg.groupnumber = gTalkerMgrObj.group.groupnumber;
						tgroupcfg.grouptype = gTalkerMgrObj.group.grouptype;
						TalkerMgr_joinGroup(tgroupcfg);
					end

				else   --会话状态错误
					DebugLog("wjun---AirtalkeeResponseEvent 临时或群组会话状态事件 状态错误 error! ");
					gTalkerMgrObj.status = EStatus_LoginOk;
				end
			else --群组id与当前群组id不一致
				DebugLog("wjun---AirtalkeeResponseEvent 临时或群组会话状态事件 群组id不一致错误 error! ");
				--退出上次所在群组，再加入当前群组
				tiros.airtalkeemgr.SessionBye();
				--再加入当前所在群组
				TalkerMgr_realJoinGroupByCfg(gTalkerMgrObj.group);
			end

		elseif t.type == 10011 then		--话语权状态变更
			local t1 = {};
			t1.aid = t.aid;
			t1.name = gTalkerMgrObj.group.members[t1.aid].name;
			if t.aid == gTalkerMgrObj.config.aid then		--本人话语权
				t1.isme = true;
				if t.isVoice == 1 then	                --本人开始说话
					gTalkerMgrObj.group.speaker = gTalkerMgrObj.config.aid;
					t1.state = EGroupTalkEvent_Talking;
				elseif t.isVoice == 0 then 	            --本人停止说话
					gTalkerMgrObj.group.speaker = nil;
					t1.state = EGroupTalkEvent_Release;
				elseif t.isVoice == -1 then 			--本人申请话语权失败
					t1.state = EGroupTalkEvent_MeRequestErr;
				elseif t.isVoice == 2 then 			    --本人申请话语权排队
					--如果当前正在排队，则直接结束说话
					TalkerMgr_talkRelease();
					t1.state = EGroupTalkEvent_MeRequestErr2;
				elseif t.isVoice == 3 then 			    --本人申请话语权被禁言
					--如果当前请求说话被禁言，则直接结束说话
					TalkerMgr_talkRelease();
					t1.state = EGroupTalkEvent_MeRequestErr2;
				end
			else 										--其他人话语权
				t1.isme = false;
				if t.isVoice == 1 then	                --其他人开始说话
					gTalkerMgrObj.group.speaker = t.aid;
					t1.state = EGroupTalkEvent_Talking;
				elseif t.isVoice == 0 then 				--其他人停止说话
					gTalkerMgrObj.group.speaker = nil;
					t1.state = EGroupTalkEvent_Release;
				end
			end
			--通知上层状态，频道内交互事件信息变更
			SendMsgToLogic(ETalkerEvent_GroupTalk, KModule_Headlist, tiros.json.encode(t1));

		elseif t.type == 10005 then		--请求向当前群组上报本人位置信息
			--t.text
			t.text.groupid = gTalkerMgrObj.group.curGroupid;
			t.text.type = gTalkerMgrObj.group.type;
			t.text.groupnumber = gTalkerMgrObj.group.groupnumber;
			t.text.aid = gTalkerMgrObj.config.aid;
			tiros.airtalkeemgr.MessageSendToGroup(t.text, gTalkerMgrObj.group.curGroupid);
		elseif t.type == 10010 then		--收到群组内其他人上报的位置信息
			--t.groupid, aid, msgid, date, msg.lon, msg.lat, msg.direction, msg.speed
			--更新群组成员信息
			if gTalkerMgrObj.group == nil or t.groupid ~= gTalkerMgrObj.group.curGroupid or gTalkerMgrObj.status < EStatus_JoinGroupOk then
				DebugLog("wjun---AirtalkeeResponseEvent 收到成员位置信息，但是与当前群组id不一致则丢弃！");
				return;
			end
			if gTalkerMgrObj.group.members[t.aid] == nil then
				DebugLog("wjun---AirtalkeeResponseEvent 当前群组不存在该组员！");
				return;
			end
			gTalkerMgrObj.group.members[t.aid].lon = t.msg.lon;
			gTalkerMgrObj.group.members[t.aid].lat = t.msg.lat;
			gTalkerMgrObj.group.members[t.aid].direction = t.msg.direction;
			gTalkerMgrObj.group.members[t.aid].speed = t.msg.speed;
			--gTalkerMgrObj.group.members[t.aid].aid = t.aid;
			gTalkerMgrObj.group.members[t.aid].radius = t.msg.radius;
			
			gTalkerMgrObj.group.members[t.aid].name = t.msg.name;
			gTalkerMgrObj.group.members[t.aid].sex = t.msg.sex;
			gTalkerMgrObj.group.members[t.aid].head = t.msg.head;
			gTalkerMgrObj.group.members[t.aid].share = t.msg.videoshare;
			gTalkerMgrObj.group.members[t.aid].camera = t.msg.videoturn;
			--gTalkerMgrObj.group.members[t.aid].groupid = t.groupid;
			gTalkerMgrObj.group.members[t.aid].date = t.date;
			--通知上层状态，群组人员位置信息发生变化
			local t1 = {};
			t1.state = EGroupInfoEvent_PosUpdate;
			t1.aid = t.aid;
			t1.data = gTalkerMgrObj.group.members[t.aid];
			SendMsgToLogic(ETalkerEvent_GroupInfo, KModule_Headlist, tiros.json.encode(t1));
			DebugLog("wjun---AirtalkeeResponseEvent 收到成员位置信息 json = " .. tiros.json.encode(t1));
		elseif t.type == 10040 then    --视频请求
			print("jiangdezheng 2被请求方 ");
			ReceiveWactchMsg(t.msg);
		elseif t.type == 10041 then    --接收到服务器的视频URL地址等信息
			print("jiangdezheng  3请求方收到服务器URL:" .. msg);
			--发送给平台
			--SendMsgToLogic(1, 1, tiros.json.encode(t));
			 --"zmid": "11",
			--"qmid": "213916322",
			--"vid": "33",
			--"anurl": "",
			--"iosurl": "",
			--"picurl": "",
			--"type": "3"
			if( WatchVideoState == "1") then
				t.data["state"] = "4";
				local LoadPictureURL = t.data.picurl;
				--告知平台播放URL
				
				SendMsgToLogic(EUserNotifyPlatEvent, KModule_Headlist,tiros.json.encode(t.data));
				print("jiangdezheng  4请求方收到服务器URL，并通知平台完毕:" .. tiros.json.encode(t.data));
				--下载图片
				-- need add if
				if nil ~= LoadPictureURL  or "" == LoadPictureURL then
					tiros.http.httpsendforlua("cdc_client", "DownLoadPictureFile",HTTP_RESOURCE_ID,LoadPictureURL,LoadPictureHttpEvent,nil);
				else
					SendMsgToLogic(6, KModule_Headlist, GenJson_State("13"));
				end
			end
		elseif t.type == 10042 then    --接收到群组有人录制视频出错
			local errorToPlat = {};
			errorToPlat["state"] = "6";
			errorToPlat["errorCode"] = t.errorCode;
			SendMsgToLogic(EUserNotifyPlatEvent, KModule_Headlist, tiros.json.encode(errorToPlat));
		elseif t.type == 10043 then    
			--接收到直播视频请求
		print("jiangdezheng clientrequest 33");
			ReceiveWactchMsg(t.data);
		elseif t.type == 10012 then		--音频强度变化消息标识
			--t.msg.volume
			local t1 = {};
			t1.state = EGroupTalkEvent_Volume;
			t1.volume = t.msg.volume;
			--通知上层状态，音频强度变化消息标识
			SendMsgToLogic(ETalkerEvent_GroupTalk, KModule_Headlist, tiros.json.encode(t1));
		elseif t.type == 10013 then		--有另一个设备用同一个号登录,被登录消息标识
			--被踢之后，重置状态为获取AID成功
			gTalkerMgrObj.status = EStatus_GetAidOk;
			--通知上层状态，用户重复登录爱淘客
			SendMsgToLogic(ETalkerEvent_Login, KModule_Headlist, GenJson_State(ELoginEvent_LoginAidDouble));

		elseif t.type == 10030 then		--群组成员信息发生变化
			--t.allcount = t.allcount	
			--t.onlinecount = t.onlinecount	
			--t.members = t.members  --->["123","456", "789"]	
			--融合爱淘客群组成员数据和本地成员数据
			DebugLog("wjun---AirtalkeeResponseEvent 当前群组json = " .. tiros.json.encode(gTalkerMgrObj.group.members));
			local addMembers = {};		--新增成员数组
			local rmMembers = {};
			if type(t.members) == "table" then
				local newMembers = {};
				local index = 1;
				local newcount = 0;
				--生成最新的成员数据
				--获取新增成员
				for k,v in pairs(t.members) do
					newMembers[v] = gTalkerMgrObj.group.members[v];
					if newMembers[v] == nil then
						newMembers[v] = {};
						--新增成员
						addMembers[index] = v;
						index = index + 1;
					end
					newcount = newcount + 1;
				end
				--获取离开成员
				index = 1;
				for k,v in pairs(gTalkerMgrObj.group.members) do
					if newMembers[k] == nil then
						--离开成员
						rmMembers[index] = k;
						index = index + 1;
						--正在观看的成员离开群组，通知平台取消视频框
						if k == gDesAid and WatchVideoState == "1" then
							SendMsgToLogic(EUserNotifyPlatEvent, KModule_Headlist,GenJson_State(EUserQuitGroupCancelVedio));
						end
					end
				end
				--更新成员数据
				gTalkerMgrObj.group.members = newMembers;
				gTalkerMgrObj.group.membercount = newcount;
			end
			--通知上层状态，群组人数发生变化
			local msg = {};
			msg.state = EGroupInfoEvent_MembersCountUpdate;
			msg.data = {};
			msg.data.membercount = gTalkerMgrObj.group.membercount;
			msg.data.addMembers = addMembers;
			msg.data.rmMembers = rmMembers;
			SendMsgToLogic(ETalkerEvent_GroupInfo, KModule_Headlist, tiros.json.encode(msg));
			DebugLog("wjun---AirtalkeeResponseEvent 群组成员信息发生变化 3 json = " .. tiros.json.encode(msg));

			--如果是加入群组后第一次人数变化，播放加入群组语音
			if true == gTalkerMgrObj.group.fistIn then
				gTalkerMgrObj.group.fistIn = false;
				local promptMsg = {};
				promptMsg.state = EGroupInfoEvent_PlayJoinPrompt;
				promptMsg.membercount = gTalkerMgrObj.group.membercount;
				promptMsg.groupnumber = gTalkerMgrObj.group.groupnumber;
				promptMsg.grouptype = gTalkerMgrObj.group.type;
				SendMsgToLogic(ETalkerEvent_GroupEnter, KModule_Headlist, tiros.json.encode(promptMsg));
			end		

		elseif t.type == 10031 then		--群组需要重新进入
			DebugLog("wjun---AirtalkeeResponseEvent 收到关键包：大区频道发生变化，群组需要重新进入 1");
			--通知上层状态，大区频道发生变化
			SendMsgToLogic(ETalkerEvent_GroupEnter, KModule_Headlist, GenJson_State(EGroupEnterEvent_Change));
			--重新进入大区频道
			TalkerMgr_joinPublicGroup();
			DebugLog("wjun---AirtalkeeResponseEvent 收到关键包：群组需要重新进入 2");
		elseif t.type == 10032 then		--群组标题修改
			DebugLog("wjun---AirtalkeeResponseEvent 收到关键包：群组名称修改 1");
			if gTalkerMgrObj.status >= EStatus_JoinGroupOk then
				--如果当前已经在一个群组内，则处理
				if gTalkerMgrObj.group.curGroupid == t.data.groupid then
					DebugLog("wjun--- 收到关键包：群组名称修改 成功 name = " .. t.data.groupname);
					gTalkerMgrObj.group.title = t.data.groupname;
					--通知上层状态，群组名称修改
					local s = {};
					s.state = EGroupInfoEvent_TitleUpdate;
					s.title = gTalkerMgrObj.group.title;
					SendMsgToLogic(ETalkerEvent_GroupInfo, KModule_Headlist, tiros.json.encode(s));
					
					local temp = {}
					temp.groupname = gTalkerMgrObj.group.title;
					temp.groupnumber = gTalkerMgrObj.group.groupnumber;
					tiros.grouplist.updatefav(temp);

				else
					DebugLog("wjun--- 收到关键包：群组名称修改 失败 与当前群组ID不符, name = " .. t.data.groupname);
				end
			end
			DebugLog("wjun---AirtalkeeResponseEvent 收到关键包：群组名称修改 2");
		end
	end
end

--[[
	工具函数，任务管理接口之添加任务接口
]]
TalkerMgr_AddTask = function(task)
	if gTalkerMgrObj ~= nil and type(task) == "table" then
		--目前只保留一个执行任务
		gTalkerMgrObj.TaskArray[#gTalkerMgrObj.TaskArray+1] = task;
	end
end

--[[
	工具函数，任务管理接口之处理任务接口
]]
TalkerMgr_DoTask = function()
	while #gTalkerMgrObj.TaskArray > 0 do
		DebugLog("wjun---AirtalkeeResponseEvent 爱淘客登录成功，有未完成任务要执行");
		--如果当前任务队列有任务未执行完，则在爱淘客登录成功后开始执行
		local id = 1;
		local task = gTalkerMgrObj.TaskArray[id];
		--执行完任务就清除该任务
		table.remove(gTalkerMgrObj.TaskArray, id); 
		if task.cmd == ETaskCMD_GetGroupInfo then
			--请求加入新群组
			DebugLog("wjun---AirtalkeeResponseEvent 爱淘客登录成功，自动执行TalkerMgr_joinGroup");
			tiros.TalkerMgr.JoinGroup(tiros.json.encode(task.data));
		elseif task.cmd == ETaskCMD_JoinGroup then
			--实际加入爱淘客频道
			DebugLog("wjun---AirtalkeeResponseEvent 爱淘客登录成功，自动执行TalkerMgr_realJoinGroupByCfg data = " .. tiros.json.encode(task.data));
			TalkerMgr_realJoinGroupByCfg(task.data);
		elseif task.cmd == ETaskCMD_ExitGroup then
			--实际退出爱淘客频道
			DebugLog("wjun---AirtalkeeResponseEvent 爱淘客登录成功，自动执行退出爱淘客频道");
			tiros.airtalkeemgr.SessionBye();
			gTalkerMgrObj.status = EStatus_LoginOk;
		end
	end
end

--[[
	工具函数，任务管理接口之情况任务接口
]]
TalkerMgr_ClearAllTask = function()
	if gTalkerMgrObj ~= nil and gTalkerMgrObj.TaskArray ~= nil then
		--目前只保留一个执行任务
		gTalkerMgrObj.TaskArray= {};
	end
end

local function TalkerMgr_getCurrentGroupMarkAndStatus()
	local groupMark = {}
	groupMark.status = gTalkerMgrObj.status;
	groupMark.curGroupid = gTalkerMgrObj.group.curGroupid;
	groupMark.groupnumber = gTalkerMgrObj.group.groupnumber;
	groupMark.grouptype = gTalkerMgrObj.group.grouptype;
	return groupMark;
end

local function TalkerMgr_modifyCurrentStatus(status)
	gTalkerMgrObj.status = status;
end
local function TalkerMgr_RecoveryNetwork()
	if gTalkerMgrObj.status >= EStatus_GetAidOk then
		SendMsgToLogic(ETalkerEvent_Login, KModule_Headlist, GenJson_State(ELoginEvent_RecoveryNetwork));
	else
		TalkerMgr_joinPublicGroup();
	end
end

--[[
 @描述:上传图片http回调
 @return:无
--]]
local function sys_uploadPictureHttpCB(ptype, nEvent, param1, param2)
	print("gouluk upload picture event:" .. nEvent);
--[[
	if nEvent == 2 then
		gUploadPictureData = "";
	elseif nEvent == 3 then
		gUploadPictureData = gUploadPictureData .. param2;
	elseif nEvent == 4 then
		local uploadTable = tiros.json.decode(gUploadPictureData);
		print("gouluk upload sys_uploadPictureHttpCB info:" .. uploadTable);
		WriteLog("upload success");
	elseif nEvent == 5 then
		DebugLog("gouluk upload sys_uploadPictureHttpCB fail net error");
	end
]]
end

--[[
 @描述:平台告知上传图片
 @param:picPath 图片路径
 @return:无
--]]
local function TalkerMgr_UploadPicture(picPath)
	local sURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",2322);
	print("gouluk upload UploadPicture url" .. sURL);

	--sURL = "http://192.168.3.90:8086/navidog4MeetTrans/imageUpload.htm"

	--sURL = "http://svr.xiaocheben.com/cdcReportServer/ugpsreport.htm"
	local lon,lat,speed,course,altitude,radius,rawLon,rawLat = tiros.location.lkgetlastposition_mem();



	--http头
	local header = {};
	header["userid"] = gUserID;
	header["aid"] = gTalkerMgrObj.config.aid;
	header["selflon"] = math.ceil(rawLon);
	header["selflat"] = math.ceil(rawLat);
	header["speed"] = speed;
	header["mobileid"] = tiros.moduledata.moduledata_get('framework','mobileid');
	print("gouluk upload picture");
	--tiros.httpupload.HttpUpLoad("pictureUpload",sys_uploadPictureHttpCB,sURL,picPath,"cdc-golukpictureUpload",header);
	print("gouluk upload picture 5555");

	WriteLog("upload yuansheng lon:" .. header["selflon"] .. ",lat:" .. header["selflat"]);
	WriteLog("upload jiami lon:" .. lon .. ",lat:" .. lat);
end

--[[
--对外接口发布
]]
local interface = {};

--初始化引擎
createmodule(interface,"Init",function()
	return TalkerMgr_Init();
end);

--获取Aid及其服务器配置参数
createmodule(interface,"GetAid",function()
	print("usercenter GetAid")
	return TalkerMgr_getAidFromUserCenter();
end);

--根据频道标识获取群组频道信息
createmodule(interface,"JoinGroup",function(groupcfg)
	print("grouplist JoinGroup 111" .. groupcfg);
	local tgroupcfg = tiros.json.decode(groupcfg);
	return TalkerMgr_joinGroup(tgroupcfg);
end);

--指定加入公共频道
createmodule(interface,"JoinPublicGroup",function()
	return TalkerMgr_joinPublicGroup();
end);

--退出当前群组
createmodule(interface,"QuitGroup",function()
	return TalkerMgr_QuitGroup();
end);


--请求说话
createmodule(interface,"TalkRequest",function()
	return TalkerMgr_talkRequest();
end);

--说话完毕
createmodule(interface,"TalkRelease",function()
	return TalkerMgr_talkRelease();
end);

--对讲管理模块接收爱淘客事件
createmodule(interface,"AirtalkeeResponseEvent",function(msg)
	return TalkerMgr_AirtalkeeResponseEvent( msg )
end);

--获取当前群组相关Json数据
createmodule(interface,"GetGroupInfo",function()
	return TalkerMgr_GetGroupInfo()
end);

--获取更多群组频道信息
--createmodule(interface,"GetMoreGroupListRequest",function(type, page, roomFrom, roomTo)
--	return TalkerMgr_getMoreGroupList(type, page, roomFrom, roomTo);
--end);

--修改群组标题
--createmodule(interface,"ModifyGroupTitleRequest",function(title)
--	return TalkerMgr_modifyGroupTitle(title);
--end);

createmodule(interface, "GetCurrentGroupMarkAndStatus",function()
	return TalkerMgr_getCurrentGroupMarkAndStatus();
end);

createmodule(interface, "ModifyCurrentStatus",function()
	return TalkerMgr_modifyCurrentStatus();
end);
-------------------------------------------------------------------------------------------------
--平台设置用户信息
createmodule(interface,"SetUserInfo",function(username,usersex,userhead)
	TalkerMgr_SetUserInfo(username,usersex,userhead);
end);

createmodule(interface,"GetUserInfo",function()
	 return TalkerMgr_GetUserInfo();
end);

createmodule(interface,"GetUserName",function()
	 return TalkerMgr_GetUserName();
end);
createmodule(interface,"GetUserSex",function()
	 return TalkerMgr_GetUserSex();
end);
createmodule(interface,"GetUserHead",function()
	 return TalkerMgr_GetUserHead();
end);

--平台设置是否共享视频
createmodule(interface,"SetVideoShare",function(status)
	 TalkerMgr_SetVideoShare(status);
end);
--获取是否共享视频
createmodule(interface,"GetVideoShare",function()
	 return TalkerMgr_GetVideoShare();
end);


--平台设置是否打开摄像头2开1关
createmodule(interface,"SetVideoTurn",function(status)
	 TalkerMgr_SetVideoTurn(status);
end);
--获取是否打开摄像头
createmodule(interface,"GetVideoTurn",function()
	 return TalkerMgr_GetVideoTurn();
end);

--平台设置是否自动循环录像
createmodule(interface,"SetAotuLoopVideo",function(status)
	 TalkerMgr_SetAotuLoopVideo(status);
end);
--获取是否自动循环录像
createmodule(interface,"GetAotuLoopVideo",function()
	 return TalkerMgr_GetAotuLoopVideo();
end);

--用户点击观看视频或平台触发
createmodule(interface,"WatchVideo",function(SourceAid,DesAid)
	 return TalkerMgr_WatchVideo(SourceAid,DesAid);
end);
--平台告知的视频播放ID
createmodule(interface,"NotifyVideoID",function(VideoID)
	 return TalkerMgr_NotifyVideoID(VideoID);
end);

--平台告知的取消观看视频
createmodule(interface,"CancelWatchVideo",function()
	 return TalkerMgr_CancelWatchVideo();
end);

--[[
 @描述:平台告知的录制视频出现异常
 @param:errorCode 错误码
 @return:无
--]]
createmodule(interface,"RecordVideoError",function(errorCode)
	 return TalkerMgr_RecordVideoError(errorCode);
end);

createmodule(interface,"RecoveryNetwork",function()
	 TalkerMgr_RecoveryNetwork();
end);

--[[
 @描述:平台告知上传图片
 @param:picPath 图片路径
 @return:无
--]]
createmodule(interface,"UploadPicture",function(picPath)
	 TalkerMgr_UploadPicture(picPath);
end);

--[[
 @描述:用户中心登录成功
 @param:用户信息
 @return:无
--]]
createmodule(interface,"UserCenterLogin",function(userInfo)
	 TalkerMgr_UserCenterLogin(userInfo);
end);

--[[
 @描述:用户中心登出
 @param:用户信息
 @return:无
--]]
createmodule(interface,"UserCenterLogout",function()
	 TalkerMgr_UserCenterLogout();
end);

-------------------------------------------------------------------------------------------------



tiros.TalkerMgr = readOnly(interface);

DebugLog("111111111111 TalkerMgr-----Over!");

