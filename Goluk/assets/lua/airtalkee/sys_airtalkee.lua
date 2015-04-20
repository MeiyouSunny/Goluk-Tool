--[[
-- @描述:爱滔客接口绑定
-- @编写人:魏俊
-- @创建日期: 2013-01-17 10:56
-- @修改内容：
--]]

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/json"
require"lua/file"

--_gATObjlist：全局变量，用于存放正在使用的所有ATObj句柄
local _gATObjlist = {}
--_gATObjweaklist：全局变量，用于存放所有ATObj句柄的week表，week表中既包含正使用的句柄，也包含即将回收的句柄
local _gATObjweaklist = {}
setmetatable(_gATObjweaklist,{__mode ="v" })

--local ATObj = {};
--[[
	.atype	= atype:用于lua引擎回调时获取句柄使用
	.notify = notify
	.name = name
	.password = password
	.loginstatus = 0:未登录，1：登录中，2：登录成功, 3:登出中
]]--

--爱滔客系统回调接口
DeclareGlobal("sys_airtalkee_Notify",function (atype, dwEvent, json, dwParam1, dwParam2)
	local ATObj = getHandle(_gATObjlist, atype);
	if ATObj ~=  nil and ATObj.notify ~= nil then
		ATObj.notify(ATObj, dwEvent, json, dwParam1, dwParam2);
	end
end)

--创建ATObj实例
--输出：创建后airtalkee实例句柄
local function airtalkee_create(ip)
	local h = airtalkeelib.create(ip);
	if h ~= nil then
		local ATObj = {};
		ATObj.handle = h;
		ATObj.loginstatus = 0;
		ATObj.atype = "ATObjType_" .. tostring(timelib.clock());
		registerHandle(_gATObjlist, _gATObjweaklist, ATObj.atype, ATObj);
		airtalkeelib.registnotify(h, ATObj.atype, "sys_airtalkee_Notify");
		print("wjun=====airtalkee_create  1")
		--设置录音文件路径
		if filelib.fdiskexist("fs1:/") == false then
			print("wjun=====airtalkee_create  2")
			filelib.fmkdir("fs0:/Records/");
			airtalkeelib.SetMessageRecordPath(ATObj.handle, "fs0:/Records/");
		else
			print("wjun=====airtalkee_create  3")
			filelib.fmkdir("fs1:/Records/");
			airtalkeelib.SetMessageRecordPath(ATObj.handle, "fs1:/Records/");
		end
		print("wjun=====airtalkee_create  4")
		return ATObj;
	end
	return nil;
end

--销毁指定ATObj实例
--ATObj：airtalkee实例句柄
--输出：无
local function airtalkee_destory(ATObj)
	if ATObj ~= nil and ATObj.handle ~= nil then
		airtalkeelib.destroy(ATObj.handle);
		ATObj.handle = nil;
		ATObj.name = nil;
		ATObj.password = nil;
		ATObj.loginstatus = 0;
		releaseHandle(_gATObjlist, ATObj.atype);
		ATObj = nil;
	end
end

--注册回调
--ATObj：airtalkee实例句柄
--notify：lua的回调函数对象地址
--输出：无
local function airtalkee_registnotify(ATObj, notify)
	if ATObj ~= nil and notify ~= nil then
		ATObj.notify = notify
	end
end


--配置服务器参数
--ATObj：airtalkee实例句柄
--name：用户名
--password：密码
--输出：无
local function airtalkee_configserver(ATObj, cfg_sp, cfg_sp_port, cfg_sp_lport, cfg_mdsr, cfg_mdsr_port)
	if ATObj ~= nil then
		print("wjun************airtalkee_configserver ");
		airtalkeelib.configserver(ATObj.handle, cfg_sp, cfg_sp_port, cfg_sp_lport, cfg_mdsr, cfg_mdsr_port);
	end
end


--登录
--ATObj：airtalkee实例句柄
--name：用户名
--password：密码
--输出：无
local function airtalkee_login(ATObj, name, password)
	if ATObj ~= nil then
		if ATObj.loginstatus == 1 or ATObj.loginstatus == 2 then
			--如果当前状态为登陆中或者登录成功，则先登出
			--tiros.airtalkee.logout(ATObj);
			print("wjun************airtalkee_login Error Already login!!!");
			return;
		end
		ATObj.name = name;
		ATObj.password = password;
		ATObj.loginstatus = 1;
		airtalkeelib.login(ATObj.handle, name, password);
	end
end

--登出
--ATObj：airtalkee实例句柄
--输出：无
local function airtalkee_logout(ATObj)
	if ATObj ~= nil then
		ATObj.name = nil;
		ATObj.password = nil;
		if ATObj.loginstatus ~= 0 and ATObj.loginstatus ~= 3 then --不处于空闲状态，且不处于登出过程中状态，则需要登出
			if ATObj.handle ~= nil then
				ATObj.loginstatus = 0;
				airtalkeelib.logout(ATObj.handle);
			end
		end
	end
end

--UDP上报数据
--ATObj：airtalkee实例句柄
--msg：string型，上报消息体
--输出：无
local function airtalkee_SystemCusromReport(ATObj, msg)
	if ATObj ~= nil and msg ~= nil and #msg > 0 then
		if ATObj.loginstatus == 2 then
			airtalkeelib.SystemCusromReport(ATObj.handle, msg);
		end
	end
end

--从服务器取所有群组的在线人数(仅获取一次)
--ATObj：airtalkee实例句柄
--输出：无
local function airtalkee_ChannelOnLineCountGet(ATObj)
	if ATObj ~= nil and ATObj.loginstatus == 2 then
		airtalkeelib.ChannelOnLineCountGet(ATObj.handle);
	end
end

--从服务器周期性的获取所有群组的在线人数(间隔秒数)
--ATObj：airtalkee实例句柄
--_time：integer型，间隔秒数
--输出：无
local function airtalkee_ChannelOnLineCountStart(ATObj, _time)
	if ATObj ~= nil and ATObj.loginstatus == 2 then
		airtalkeelib.ChannelOnLineCountStart(ATObj.handle, _time);
	end
end 

--停止获取所有群的在线人数
--ATObj：airtalkee实例句柄
--输出：无
local function airtalkee_ChannelOnLineCountGetStop(ATObj)
	if ATObj ~= nil and ATObj.loginstatus == 2 then
		airtalkeelib.ChannelOnLineCountGetStop(ATObj.handle);
	end
end

--呼叫群组或个人
--ATObj：airtalkee实例句柄
--bGroup：1：是群组 0：个人
--channelID：呼叫的ID（群组或者个人）
--输出：无
local function airtalkee_SessionCall(ATObj, bGroup, channelID)
	if ATObj ~= nil and ATObj.loginstatus == 2 then
		if #channelID > 0 then
			airtalkeelib.SessionCall(ATObj.handle, bGroup, channelID);
		end
	end
end

--结束会话
--ATObj：airtalkee实例句柄
--输出：无
local function airtalkee_SessionBye(ATObj)
	if ATObj ~= nil and ATObj.loginstatus == 2 then
		airtalkeelib.SessionBye(ATObj.handle);
	end
end

--接听临时呼叫来电
--ATObj：airtalkee实例句柄
--输出：无
local function airtalkee_SessionIncomingAccept(ATObj)
	if ATObj ~= nil and ATObj.loginstatus == 2 then
		airtalkeelib.SessionIncomingAccept(ATObj.handle);
	end
end

--拒绝临时呼叫
--ATObj：airtalkee实例句柄
--输出：无
local function airtalkee_SessionIncomingReject(ATObj)
	if ATObj ~= nil and ATObj.loginstatus == 2 then
		airtalkeelib.SessionIncomingReject(ATObj.handle);
	end
end

--在当前会话中申请话语权
--ATObj：airtalkee实例句柄
--输出：无
local function airtalkee_TalkRequest(ATObj)
	if ATObj ~= nil and ATObj.loginstatus == 2 then
		airtalkeelib.TalkRequest(ATObj.handle);
	end
end

--在当前会话中释放已拥有的话语权
--ATObj：airtalkee实例句柄
--输出：无
local function airtalkee_TalkRelease(ATObj)
	if ATObj ~= nil and ATObj.loginstatus == 2 then
		airtalkeelib.TalkRelease(ATObj.handle);
	end
end

--发送自定义消息
--ATObj：airtalkee实例句柄
--msg：string型，消息体
--custom:是否自定义1：自定义/0非自定义
--输出：无
local function airtalkee_MessageSend1(ATObj, msg, custom, allowOfflineSend)
	if ATObj ~= nil and ATObj.loginstatus == 2 then
		if #msg > 0 then
			return airtalkeelib.MessageSend1(ATObj.handle, msg, custom, allowOfflineSend);
		end
	end
end

--发送自定义消息
--ATObj：airtalkee实例句柄
--aidlist:用户队列
--msg：string型，消息体
--custom:是否自定义1：自定义/0非自定义
--输出：无
local function airtalkee_MessageSend2(ATObj, aidlist, msg, custom, allowOfflineSend)
	if ATObj ~= nil  and ATObj.loginstatus == 2 then
		if #msg > 0 then
			return airtalkeelib.MessageSend2(ATObj.handle, aidlist, msg, custom, allowOfflineSend);
		end
	end
end

--发送自定义消息
--ATObj：airtalkee实例句柄
--aid:指定用户
--msg：string型，消息体
--custom:是否自定义1：自定义/0非自定义
--输出：无
local function airtalkee_MessageSend3(ATObj, aid, msg, custom, allowOfflineSend)
	if ATObj ~= nil and ATObj.loginstatus == 2  then
		if #msg > 0 then
			print("jiangdezheng 2请求方开始发送视频请求 ");
			return airtalkeelib.MessageSend3(ATObj.handle, aid, msg, custom, allowOfflineSend);
		end
	end
end

--发送自定义消息
--ATObj：airtalkee实例句柄
--groupid:指定群组id
--msg：string型，消息体
--custom:是否自定义1：自定义/0非自定义
--输出：无
local function airtalkee_MessageSend4(ATObj, groupid, msg, custom, allowOfflineSend)
	print("wjun=====airtalkee_MessageSend4 1");
	print("wjun=====airtalkee_MessageSend4 2 groupid = " .. groupid .. ",msg = " .. msg);
	if ATObj ~= nil  and ATObj.loginstatus == 2 then
		if #msg > 0 then
			print("wjun=====airtalkee_MessageSend4 3");
			return airtalkeelib.MessageSend4(ATObj.handle, groupid, msg, custom, allowOfflineSend);
		end
	end
	print("wjun=====airtalkee_MessageSend4 4");
end

-- 设置本地语音文件存储路径
--ATObj：airtalkee实例句柄
--path - 语音文件存储路径
--输出：无
local function airtalkee_SetMessageRecordPath(ATObj, path)
	if ATObj ~= nil and ATObj.loginstatus == 2  then
		airtalkeelib.SetMessageRecordPath(ATObj.handle, path);
	end
end

--给当前群组频道开始语音录制
--ATObj：airtalkee实例句柄
--输出：无
local function airtalkee_MessageRecordStart1(ATObj, allowOfflineSend)
	if ATObj ~= nil and ATObj.loginstatus == 2 then
		airtalkeelib.MessageRecordStart1(ATObj.handle, allowOfflineSend);
	end
end

--给多个用户开始语音录制
--ATObj：airtalkee实例句柄
--aidlist - 用户队列
--输出：无
local function airtalkee_MessageRecordStart2(ATObj, aidlist, allowOfflineSend)
	if ATObj ~= nil and ATObj.loginstatus == 2  then
		airtalkeelib.MessageRecordStart2(ATObj.handle, aidlist, allowOfflineSend);
	end
end

--给指定用户开始语音录制
--ATObj：airtalkee实例句柄
--aid - 指定用户
--输出：无
local function airtalkee_MessageRecordStart3(ATObj, aid, allowOfflineSend)
	if ATObj ~= nil and ATObj.loginstatus == 2  then
		airtalkeelib.MessageRecordStart3(ATObj.handle, aid, allowOfflineSend);
	end
end

--给指定用户开始语音录制
--ATObj：airtalkee实例句柄
--groupid - 指定群组id
--输出：无
local function airtalkee_MessageRecordStart4(ATObj, groupid, allowOfflineSend)

	if ATObj ~= nil and ATObj.loginstatus == 2  then
		airtalkeelib.MessageRecordStart4(ATObj.handle, groupid, allowOfflineSend);
	end
end

--停止语音录制
--ATObj：airtalkee实例句柄
--iscancel - true:正常结束开始语音发送，false为：取消刚录制的语音
--输出：无
local function airtalkee_MessageRecordStop(ATObj, iscancel)
	if ATObj ~= nil then
		airtalkeelib.MessageRecordStop(ATObj.handle, iscancel);
	end
end

--给多个用户开始重发语音数据
--ATObj：airtalkee实例句柄
--aidlist - 用户队列
--msgid - 消息编码
--resid - 语音文件资源编码
--time - 语音文件时长
--输出：无
local function airtalkee_MessageRecordResend1(ATObj, aidlist, msgid, resid, _time, allowOfflineSend)
	if ATObj ~= nil and ATObj.loginstatus == 2  then
		return airtalkeelib.MessageRecordResend1(ATObj.handle, aidlist, msgid, resid, _time, allowOfflineSend);
	end

	return nil;
end

--给单个用户开始重发语音数据
--ATObj：airtalkee实例句柄
--aid - 单个用户
--msgid - 消息编码
--resid - 语音文件资源编码
--time - 语音文件时长
--输出：无
local function airtalkee_MessageRecordResend2(ATObj, aid, msgid, resid, _time, allowOfflineSend)
	if ATObj ~= nil and ATObj.loginstatus == 2 then
		return airtalkeelib.MessageRecordResend2(ATObj.handle, aid, msgid, resid, _time, allowOfflineSend);
	end

	return nil;
end

--给指定群组开始重发语音数据
--ATObj：airtalkee实例句柄
--groupid - 指定群组id
--msgid - 消息编码
--resid - 语音文件资源编码
--time - 语音文件时长
--输出：无
local function airtalkee_MessageRecordResend3(ATObj, groupid, msgid, resid, _time, allowOfflineSend)
	if ATObj ~= nil and ATObj.loginstatus == 2  then
		return airtalkeelib.MessageRecordResend3(ATObj.handle, groupid, msgid, resid, _time, allowOfflineSend);
	end
	return nil;
end

--播放指定消息及资源语音文件
--ATObj：airtalkee实例句柄
--msgid - 消息编码
--resid - 语音文件资源编码
--输出：无
local function airtalkee_MessageRecordPlayStart(ATObj, msgid, resid)
	airtalkeelib.MessageRecordPlayStart(ATObj.handle, msgid, resid);
end

--开始下载指定语音文件
--ATObj：airtalkee实例句柄
--msgid - 消息编码
--resid - 语音文件资源编码
--输出：无
local function airtalkee_MessageRecordPlayDownload(ATObj, msgid, resid)
	airtalkeelib.MessageRecordPlayDownload(ATObj.handle, msgid, resid);
end



--播放指定消息及资源语音文件
--ATObj：airtalkee实例句柄
--msgid - 消息编码
--resid - 语音文件资源编码
--输出：无
local function airtalkee_MessageRecordPlayStop(ATObj)
	airtalkeelib.MessageRecordPlayStop(ATObj.handle);
end

--通知爱滔客网络链接打开
--ATObj：airtalkee实例句柄
--输出：无
local function airtalkee_NetWorkOpen(ATObj)
	if ATObj ~= nil and ATObj.handle ~= nil then
		airtalkeelib.NetWorkOpen(ATObj.handle);
	end
end

--通知爱滔客网络链接关闭
--ATObj：airtalkee实例句柄
--输出：无
local function airtalkee_NetWorkClose(ATObj)
	if ATObj ~= nil and ATObj.handle ~= nil then
		airtalkeelib.NetWorkClose(ATObj.handle);
	end
end

--设置登录状态，成功还是失败
--ATObj：airtalkee实例句柄
--blogin:true/false是否成功登录
--输出：无
local function airtalkee_SetLoginStatus(ATObj, blogin)
	if ATObj ~= nil and ATObj.handle ~= nil then
		if blogin == true then	--登录成功
			ATObj.loginstatus = 2;
		else 					--登出，或者登录失败
			ATObj.loginstatus = 0;
		end
	end
end

local function airtalkee_GetLoginStatus(ATObj)
	if ATObj ~= nil and ATObj.handle ~= nil then
		if ATObj.loginstatus == 2 then	--登录成功
			return true;
		end
	end
	return false;
end

local function airtalkee_isNeedLogout(ATObj)
	if ATObj ~= nil and ATObj.handle ~= nil then
		if ATObj.loginstatus ~= 0 then	--不处于空闲状态
			return true;
		end
	end
	return false;
end

--设根据群组ID获取当前群组成员信息
--ATObj：airtalkee实例句柄
--groupID:群组id
--输出：无
local function airtalkee_GetGroupMemberList(ATObj, groupID)
	if ATObj ~= nil and ATObj.loginstatus == 2 then
		if type(groupID) == "string" and #groupID > 0 then
			airtalkeelib.GetGroupMemberList(ATObj.handle, groupID);
		end
	end
end

--设根据群组ID获取当前群组成员信息
--ATObj：airtalkee实例句柄
--resID:资源id
--输出：无
local function airtalkee_MessageRecordFileDel(ATObj, resID)
	if ATObj ~= nil then
		if type(resID) == "string" and #resID > 0 then
			airtalkeelib.MessageRecordFileDel(ATObj.handle, resID);
		end
	end
end



--=============================================================================
--接口table
local interface = {}

createmodule(interface,"create", function (ip)
	return airtalkee_create(ip);
end)

createmodule(interface,"destory", function (ATObj)
	airtalkee_destory(ATObj);
end)

createmodule(interface,"registnotify", function (ATObj, notify)
	airtalkee_registnotify(ATObj, notify);
end)

createmodule(interface,"configserver", function (ATObj, cfg_sp, cfg_sp_port,cfg_sp_lport,cfg_mdsr,cfg_mdsr_port)
	airtalkee_configserver(ATObj, cfg_sp, cfg_sp_port,cfg_sp_lport,cfg_mdsr,cfg_mdsr_port);
end)

createmodule(interface,"login", function (ATObj, name, password)
	airtalkee_login(ATObj, name, password);
end)

createmodule(interface,"logout", function (ATObj)
	airtalkee_logout(ATObj);
end)

createmodule(interface,"SystemCusromReport", function (ATObj, msg)
	airtalkee_SystemCusromReport(ATObj, msg);
end)

createmodule(interface,"ChannelOnLineCountGet", function (ATObj)
	airtalkee_ChannelOnLineCountGet(ATObj)
end)

createmodule(interface,"ChannelOnLineCountStart", function (ATObj, _time)
	airtalkee_ChannelOnLineCountStart(ATObj, _time)
end)

createmodule(interface,"ChannelOnLineCountGetStop", function (ATObj)
	airtalkee_ChannelOnLineCountGetStop(ATObj)
end)

createmodule(interface,"SessionCall", function (ATObj, bGroup, channelID)
	airtalkee_SessionCall(ATObj, bGroup, channelID)
end)

createmodule(interface,"SessionBye", function (ATObj)
	airtalkee_SessionBye(ATObj)
end)

createmodule(interface,"SessionIncomingAccept", function (ATObj)
	airtalkee_SessionIncomingAccept(ATObj)
end)

createmodule(interface,"SessionIncomingReject", function (ATObj)
	airtalkee_SessionIncomingReject(ATObj)
end)

createmodule(interface,"TalkRequest", function (ATObj)
	airtalkee_TalkRequest(ATObj)
end)

createmodule(interface,"TalkRelease", function (ATObj)
	airtalkee_TalkRelease(ATObj)
end)

createmodule(interface,"MessageSend1", function (ATObj, msg, custom, allowOfflineSend)
	return airtalkee_MessageSend1(ATObj, msg, custom, allowOfflineSend)
end)

createmodule(interface,"MessageSend2", function (ATObj, aidlist, msg, custom, allowOfflineSend)
	return airtalkee_MessageSend2(ATObj, aidlist, msg, custom, allowOfflineSend)
end)

createmodule(interface,"MessageSend3", function (ATObj, aid, msg, custom, allowOfflineSend)
	return airtalkee_MessageSend3(ATObj, aid, msg, custom, allowOfflineSend)
end)

createmodule(interface,"MessageSend4", function (ATObj, groupid, msg, custom, allowOfflineSend)
	return airtalkee_MessageSend4(ATObj, groupid, msg, custom, allowOfflineSend)
end)

createmodule(interface,"SetMessageRecordPath", function (ATObj, path)
	airtalkee_SetMessageRecordPath(ATObj, path)
end)

createmodule(interface,"MessageRecordStart1", function (ATObj, allowOfflineSend)
	airtalkee_MessageRecordStart1(ATObj, allowOfflineSend)
end)

createmodule(interface,"MessageRecordStart2", function (ATObj, aidlist, allowOfflineSend)
	airtalkee_MessageRecordStart2(ATObj, aidlist, allowOfflineSend)
end)

createmodule(interface,"MessageRecordStart3", function (ATObj, aid, allowOfflineSend)
	airtalkee_MessageRecordStart3(ATObj, aid, allowOfflineSend)
end)

createmodule(interface,"MessageRecordStart4", function (ATObj, groupid, allowOfflineSend)
	airtalkee_MessageRecordStart4(ATObj, groupid, allowOfflineSend)
end)

createmodule(interface,"MessageRecordStop", function (ATObj, iscancel)
	airtalkee_MessageRecordStop(ATObj, iscancel)
end)

createmodule(interface,"MessageRecordResend1", function (ATObj, aidlist, msgid, resid, _time, allowOfflineSend)
	return airtalkee_MessageRecordResend1(ATObj, aidlist, msgid, resid, _time, allowOfflineSend)
end)

createmodule(interface,"MessageRecordResend2", function (ATObj, aid, msgid, resid, _time, allowOfflineSend)
	return airtalkee_MessageRecordResend2(ATObj, aid, msgid, resid, _time, allowOfflineSend)
end)

createmodule(interface,"MessageRecordResend3", function (ATObj, groupid, msgid, resid, _time, allowOfflineSend)
	return airtalkee_MessageRecordResend3(ATObj, groupid, msgid, resid, _time, allowOfflineSend)
end)

createmodule(interface,"MessageRecordPlayStart", function (ATObj, msgid, resid)
	airtalkee_MessageRecordPlayStart(ATObj, msgid, resid)
end)

createmodule(interface,"MessageRecordPlayStop", function (ATObj)
	airtalkee_MessageRecordPlayStop(ATObj)
end)

createmodule(interface,"NetWorkOpen", function (ATObj)
	airtalkee_NetWorkOpen(ATObj)
end)

createmodule(interface,"NetWorkClose", function (ATObj)
	airtalkee_NetWorkClose(ATObj)
end)

createmodule(interface,"SetLoginStatus", function (ATObj, blogin)
	airtalkee_SetLoginStatus(ATObj, blogin);
end)

createmodule(interface,"GetLoginStatus", function (ATObj)
	return airtalkee_GetLoginStatus(ATObj);
end)

createmodule(interface,"isNeedLogout", function (ATObj)
	return airtalkee_isNeedLogout(ATObj);
end)

createmodule(interface,"GetGroupMemberList", function (ATObj, groupID)
	airtalkee_GetGroupMemberList(ATObj, groupID)
end)

createmodule(interface,"MessageRecordFileDel", function (ATObj, resID)
	airtalkee_MessageRecordFileDel(ATObj, resID)
end)

createmodule(interface,"MessageRecordPlayDownload", function (ATObj, msgid, resid)
	airtalkee_MessageRecordPlayDownload(ATObj, msgid, resid)
end)

tiros.airtalkee = readOnly(interface);

--=========================================end
