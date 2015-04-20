--[[
* 1.类命名首字母大写
* 2.公共函数驼峰式命名
* 3.属性函数驼峰式命名
* 4.变量/参数驼峰式命名
* 5.操作符之间必须加空格
* 6.注释都在行首写
* 7.后续人员开发保证代码格式一致
* file: 见面3
* author: 张建峰
* date:2013-01-14
]]--
require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/location"
require"lua/framework/sys_framework"
require"lua/json"
require"lua/commfunc"
require"lua/config"
require"lua/http"
require"lua/moduledata"
require"lua/database"
require"lua/file"
require"lua/timer"
require "lua/chatrecord"
require"lua/mediaplayer"

local gMsgIDResID;
local gsTypeMediaPlayerForAirtalk = "TypeMediaPlayerForAirtalk";

--存放Web前端注册过来的回调函数的表
local gtHandle = {};
--保存所有id
local gtIDs = {};

local interface = {};

--保存见面状态
local gMeetState = 0;
--[[文件格式
{"groupid":"123456783245",
"togetherlist":[{"aid":"1215454887878","uid":"50","phone":"12222222222","nickname":"aaaa","url":"fs2:/url","lon":"12213","lat":"2121","lasttime":"1215","direction":"11","isVioce":"1",”speed”:”fdsfsdf”}]},

"placelon":"12345",”placelat”:”5487”,”place”:”dsfdsf”,”address”:””,"time":"1234546",”headuid”:”45465”,”contracttime”:”7878654” }
--]]

local gApppath =  "fs2:/webcache/"; --文件根路径
local gFilePath = "" --存储群组信息文件路径
local gInviteDataFile = ""--默认人信息文件路径

local gchatDir = "fs1:/chatrecord/";
local gchatPath = "";

local gAppHeadImgPath = "fs0:/friendhead/"; --头像目录
local gDefaultHeadImgPath = "fs0:/lua/together/mr.png"; --默认头像路径
local gCheckServerState = 0 --0 未校验，1，正在校验，2，校验成功，3，校验失败
local gRecvInviteCount = 0 --接受到的邀请个数
--同行组信息
local gTogetherTable = {};--存储群组成员信息
local gInviteTable = {};--存储已发送给平台的邀请信息
local gInviteStackTable = {};--存储未发送给平台的邀请信息
local gTogetherList = {}--存储群组一些其他信息
local gAcceptGroupId = nil --接受的邀请组id
gTogetherList.msgsendlist = {}----存储语音消息id

local RES_STR_FRIEND_GET_URL = 2101; --资源文件中编号
local RES_STR_FRIEND_POST_URL = 2102; --资源文件中编号
local RES_FILE_PATH = "fs0:/res/api/api.rs"; --资源文件地址路径
--local gUrlGet = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_GET_URL);
--local gUrlPost = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_POST_URL);
--local gUrlPost = "http://192.168.1.111:8081/general_Post"

local localobj = getmodule("location");
local fileobj = getmodule("file");
local tmrobj = getmodule("timer")

local headerTrail = nil
local localHeaderTrail = nil

--通知平台修改地点，时间和续时成功通知，第二个参数0,第三个参数：{"groupid":"","lat":"","lon":"","poi":"","timeset":"","contracttime":"","address":""}
local    EVT_MEET_MODIFY_TIME_OR_PLACE = 139;  
   
--通知平台到时提醒的消息，第二个参数0，第三个参数：NULL
local    EVT_MEET_TIME_OUT = 140;       

--通知平台邀请通知的消息，第二个参数0，第三个参数：NULL
local    EVT_MEET_INVITE_MSG = 141;      
  
 --通知平台加入/退出时同步同行好友及变更组长通知，第二个参数：0：加入通知，1：退出通知，2:变更组长通知，第三个参数：NULL
local    EVT_MEET_UPDETE_FRIEND = 142;   
    
--通知平台解散结伴同行，第二个参数0，第三个参数：NULL
local    EVT_MEET_DELETE_FRIENDLIST = 143; 
    
--通知当已在结伴同行时，收到邀请，需要给相应的提示，第二个参数0，第三个参数：NULL
local    EVT_MEET_ALREADY_IN = 144;    
    
--通知平台位置变化,第二个 参数0，第三个参数：NULL
local    EVT_MEET_POSITION_CHANGE = 149; 
-------------------------------------	
--通知平台音频强度,第二个参数无效，第三个参数 string 
local EVT_MEET_VOICE_STRENGTH = 151

--通知平台收到群组文本,第二个 参数0，第三个参数：{"uid":"","aid":"","msgtype":"1","state":"1","recvtime":"","sendtime":"","msgid":"","content":"","dbid":""} 
local    EVT_MEET_GROUP_TEXT = 155; 

--通知平台收到群组语音,第二个 参数0，第三个参数：{"uid":"","aid":"","msgtype":"2","state":"1","recvtime":"","sendtime":"","msgid":"","resid":"","dbid":"","speechlen":""} 
local    EVT_MEET_GROUP_VOICE = 150; 

--通知语音播放状态,第二个参数 0/1/2 表示加载中/开始播放/停止播放,第三个参数:NULL
local	 EVT_MEET_AUDIO_STATUS = 156

--通知语音录制结果
--[[
第二个参数						第三个参数：
0-开始录音						NULL
1-录制时长未满足最小秒数 ，				NULL
3-录制完成，						{“dbid”:””,“msgid”:””,”time”：”语音时长”}
2-录制取消 						NULL
4-网络错误						NULL
--]]
local	 EVT_MEET_AUDIO_RECORD = 153

--通知平台聊天信息发送结果，第二个参数:0-发送失败,1-发送成功,2-开始发送，第三个参数:{“dbid”:””,“resid”:””,”msgid”:””}
local    EVT_MEET_CHATMESSAGESEND_RESULT = 154;

--通知头像变化，第二个参数0，第三个参数：{"uid":"","path":""}
local 	 EVT_MEET_HEADIMG_REPLACE = 158

--通知邀请个数 第二个参数number,第三个参数-空
local 	EVT_MEET_GROUPINVITECOUNT = 163
-------------------------
--通用消息
local    EVT_MEET_COMMON_FROM_LUA = 147;       
--第二个参数			
local ADDFRIEND_SUCCESS = 11 --添加好友服务请求成功
local ADDFRIEND_FAIL = 12	 --添加好友服务请求失败
local QUITGROUP_SUCCESS = 21 --退出结伴同行成功
local QUITGROUP_FAIL = 22 	 --退出结伴同行失败
local CREATEGROUP_SUCCESS = 31  --创建同行组成功
local CREATEGROUP_IN_ANOTHERGROUP_FAIL = 32  --创建同行组失败,用户已加入同行组
local CREATEGROUP_NORMAL_FAIL = 33  --创建同行组失败
local MODIFY_TIME_PLACE_SUCCESS = 41  --修改约定时间、地点及续约修改成功
local MODIFY_TIME_PLACE_FAIL = 42  --修改约定时间、地点及续约修改失败
local AGREEJOINGROUP_SUCCESS = 51  --同意加入结伴同行成功
local AGREEJOINGROUP_FAIL = 52  --同意加入结伴同行失败
local REGETGROUPINFO_SUCCESS = 61  --重新获得组信息成功
local REGETGROUPINFO_NORMAL_FAIL = 62  --重新获得组信息失败
local REGETGROUPINFO_NOEXIST_FAIL = 63  --同行组已经不存在
local GET_SMSMESAGE_SUCCESS = 71  --获取短信内容成功
local GET_SMSMESAGE_FAIL = 72  --获取短信内容失败
local GET_BEESMS_SUCCESS = 81  --获取小蜜短信内容成功
local GET_BEESMS_FAIL = 82  --获取小蜜短信内容失败

--媒体播放器的状态
local SYS_MEDIA_PLAYER_EVENT_BEGIN = 0     -- ///< 开始播放dwParam1 = 0,dwParam2 = 0;
local SYS_MEDIA_PLAYER_EVENT_PAUSE = 1     -- ///< 暂停播放dwParam1 = 0,dwParam2 = 0;
local SYS_MEDIA_PLAYER_EVENT_RESUME= 2     -- ///< 恢复播放dwParam1 = 0,dwParam2 = 0;
local SYS_MEDIA_PLAYER_EVENT_END   = 3     -- ///< 播放完毕dwParam1 = 0,dwParam2 = 0;

local function callbackAllCheck()
	for key,value in pairs(gtIDs) do
		local tCheckStatus = getHandle(gtHandle, value);
		if(tCheckStatus ~= nil and tCheckStatus.notify ~= nil) then
			tCheckStatus.notify(value, gCheckServerState);
		end
	end
	if 2 == gCheckServerState or 3 == gCheckServerState then
		gtHandle = {};
		gtIDs = {};
	end
end

local function writeChatLog(tempstr)
	--print("together-----------Log-------------------writeChatLog:" .. tostring(tempstr));
	--fileobj.Writefile(gchatPath,tempstr,false);
end

local function createChatLogFile()

	--print("together-----------Log---------createChatLogFile:");
	
	--filelib.fmkdir(gchatDir);
	--gchatPath = gchatDir.."/chat";
end

-----------------------------------------------------------
--[[
web端协议
meet_state_ptp中together
 	0 正在校验用户的群组信息（用户掉线状态到重新登录爱滔客之前的状态），
	1，在群组，进入结伴同行地图界面
	2，校验失败
	3，不再群组，进入结伴同行邀请界面，
--]]
----------------------------------------------------------------



local function  GetLonLat(id,callback)
	local longitude = "419031106";
	local latitude = "143670827";
	local selflon = "419031106";
	local selflat = "143670827";
	
	--获取当前gps位置
	local lon,lat = localobj.lkgetlastposition_mem();
	if lon == nil or lat == nil then
		local outlon,outlat = localobj.lkgetlastposition_file();
		if outlon ~= nil and outlat ~= nil then
			selflon = tostring(math.ceil(outlon));
			selflat = tostring(math.ceil(outlat));
			longitude = tostring(math.ceil(outlon));
			latitude = tostring(math.ceil(outlat));
			if longitude < latitude then
				selflon = tostring(math.ceil(outlat));
				selflat = tostring(math.ceil(outlon));
				longitude = tostring(math.ceil(outlat));
				latitude = tostring(math.ceil(outlon));
			end
		end
	else
		selflon = tostring(math.ceil(lon));
		selflat = tostring(math.ceil(lat));
		longitude = tostring(math.ceil(lon));
		latitude = tostring(math.ceil(lat));
		if longitude < latitude then
			selflon = tostring(math.ceil(lat));
			selflat = tostring(math.ceil(lon));
			longitude = tostring(math.ceil(lat));
			latitude = tostring(math.ceil(lon));
		end
	end

	--获取地图中心点经纬度
	local mapcenterlon = tostring(tiros.moduledata.moduledata_get("web","mapcenterlon"));
	local mapcenterlat = tostring(tiros.moduledata.moduledata_get("web","mapcenterlat"));
	if mapcenterlon ~= "" and mapcenterlon ~= nil and mapcenterlon ~= "0" and mapcenterlat ~= "" and mapcenterlat ~= nil and mapcenterlat ~= "0" then
		longitude = tostring(math.ceil(mapcenterlon));
		latitude = tostring(math.ceil(mapcenterlat));

		if longitude < latitude then
			longitude = tostring(math.ceil(mapcenterlat));
			latitude = tostring(math.ceil(mapcenterlon));
		end
	end

	if id ~= nil and callback ~= nil then
		local s = string.format("%s('%s',%u,'%s','%s','%s','%s');",callback,id,1,selflon,selflat,longitude,latitude);
		commlib.calljavascript(s);
		return;
	end
	--用户gps 经纬度 地图中心点经纬度
	return selflon,selflat,longitude,latitude;
end
--chenxy封装udp接口
-- 格林威治时间转换为北京时间
-- 输入参数 年(4位)，月，日，时，分,秒
-- 输出参数 年(4位)，月，日，时，分,秒
local function localtime(y, m, d, hh, mm, ss )
	hh =  hh+8;    -- 格林威治时间 + 8 小时 = 北京时间
	if ( hh < 24 ) then   --没有跨天，则计算完成
		return y, m, d, hh, mm, ss;		
	end

	-----下面是跨天后的计算--------------------
	
	hh = hh-24;
	d = d+1;      -- 日期加一天

	--按月判断
	if (m ==4) or (m==6) or (m==9) or (m==11) then  --跨小月的判断
		if d > 30 then 
			d = 1;
			m = m+1;
		end
	elseif (m ==1) or (m==3) or (m==5) or (m==7) or (m==8) or (m==10) then  --跨大月的判断
		if d > 31 then 
			d = 1;
			m = m+1;
		end
	elseif m==12 then	--12 月，要判断是否跨年
		if d>31 then
			y = y+1;
			d = 1;
			m = 1;
		end
	elseif m==2 then	--2 月，要判断是否是闰年
		if( ( y%400 == 0 ) or       	     -- 能被400整除，一定是闰年
       		( y%4 ==0 ) and ( y%100 ~=0 ) ) then 	--能被4整除，但不能被100整除，一定是闰年
			if( d>29 ) then	--闰年2月，可以有29号
				m = 3;
				d = 1;
			end
		elseif ( d>28 ) then		--非闰年2月，可以有28号
			m = 3;
			d = 1;
		end		
		
	end

	return y, m, d, hh, mm, ss; --计算完成，开始输出
end
--合并日期字符串
local function GetDate()
	local y,m,d,h,m2,s = timelib.time();
	local yy,MM,dd,hh,mm,ss = localtime(y,m,d,h,m2,s);
	if MM < 10 then
		MM = "0"..MM;
	end
	if dd < 10 then
		dd = "0"..dd;
	end
	if hh < 10 then
		hh = "0"..hh;
	end
	if mm < 10 then
		mm = "0"..mm;
	end
	if ss < 10 then
		ss = "0"..ss;
	end
	local date = ""..yy..MM..dd..hh..mm..ss;
	return date;
end

--保存头像与信息
local function SaveGroupUserInfo(aid,name,url) 
	tiros.chatrecord.SaveGroupUserInfo(aid,name,url);
end
--修改头像地址
local function EditGroupUserHead(aid,url)
	print("jiayufeng-----------together----------------EditGroupUserHead----1111");
	tiros.chatrecord.EditGroupUserHead(aid,url);
	print("jiayufeng-----------together----------------EditGroupUserHead----222222");
end


local function HttpSend(id,callback,url,opt,stype)
	
	local data = nil;
	local method = opt.method;
	if method == "GET" then
		if opt.data ~= nil then
			local condi = "";
			for k,v in pairs(opt.data) do
				condi = condi .. k .. "=" .. tostring(v) .. "&";
			end
			url = url .. "?" .. tiros.commfunc.EnCodeUrl(string.sub(condi,0,string.len(condi)-1));
		end
	else
		--POST请求
		if opt.data ~= nil then
			local condi = "";
			for k,v in pairs(opt.data) do
				condi = condi .. k .. "=" .. tostring(v) .. "&";
			end
			data = tiros.commfunc.EnCodeUrl(string.sub(condi,0,string.len(condi)-1));			
		end
		if (type(opt.header)) == "table" then
			opt.header["Content-Type"] = "application/x-www-form-urlencoded";
		else
			opt.header = {};
			opt.header["Content-Type"] = "application/x-www-form-urlencoded";
		end
	end
        tiros.http.httpsendforlua("cdc_client",stype,id,url,function(id,state,code,content)
		--print("cccchttp=",content)
		if state == 2 then
			gTogetherList.rpsdata = nil
			if code ~= 200 then
				tiros.http.httpabort(id);				
				if type(callback) == "string" then
					local s = string.format("%s('%s',%u,'%s');",callback,id,0,tostring(content));
					commlib.calljavascript(s);
				elseif type(callback) == "function" then
					callback(id,0,tostring(content));
				end	
			end
		elseif state == 3 then
			if gTogetherList.rpsdata == nil then
				gTogetherList.rpsdata = string.sub(content,1,code);
			else
				gTogetherList.rpsdata = gTogetherList.rpsdata..string.sub(content,1,code);
			end
		elseif state == 4 then
			--{"istr":"服务器出错了","itype":"3"}
			if type(callback) == "string" then
				--local s = string.format("%s('%s',%u,'%s');",callback,id,1,gTogetherList.rpsdata);
				local s = callback .. "('" .. id .. "'," .. 1 .. "," .. gTogetherList.rpsdata .. ")";
				commlib.calljavascript(s);
			elseif type(callback) == "function" then
				callback(id,1,gTogetherList.rpsdata);
			end
			gTogetherList.rpsdata = nil;
		elseif state == 5 then
			if type(callback) == "string" then
				local s = string.format("%s('%s',%u,'%s');",callback,id,0,tostring(content));
				commlib.calljavascript(s);
			elseif type(callback) == "function" then
				callback(id,2,tostring(content));
			end
			gTogetherList.rpsdata = nil;
		end
	end,data,opt.header);
end
--------------------------------------------------------
--获取平台注册的回调
local function getFunctionAndUser()
    local nFunction = tiros.moduledata.moduledata_get("framework", "pfunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "puser");    
    return nFunction, nUser;   
end

--获取logic注册的回调
local function getLogicFunctionAndUser()
    local nFunction = tiros.moduledata.moduledata_get("framework", "pLogicFunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "pLogicUser");    
    return nFunction, nUser;    
end

--给logic发消息
local function sendmessagetoLogic(event,param1,param2)
	local func, usr = getLogicFunctionAndUser();
	if func ~= nil then
		commlib.universalnotifyFun(func,"LuaToLogicMsg", usr, event,param1,param2);
	end
end

--给平台发消息
local function sendmessagetoApp(msgype,msgresult,param)
	local nFunction, nUser = getFunctionAndUser();
	if nFunction ~= nil then
		commlib.initNotifyFun(nFunction, nUser, msgype, msgresult,param);
	end
end

--处理时间字符串
local function  resolveposdate(date)
	if date == nil then
		return GetDate()
	end
	local start = 1;
	local send = string.len(date)
	if send == 0 then
		return GetDate()
	end

	local str = ""
	--循环遍历每个字符
	while true do
		local tmp = string.sub(date, start,start)
		--保存0-9字符
		if tmp >= "0" and tmp <= "9" then 
			str = str..tmp 
		end
		if start == send then
			break;
		else
			start = start + 1
		end
	end 
	return str;
end


local function LocalGetHeaderTrailHttpCallback(pType, nEvent, param1, param2)
	--print("logic lua localGetHeaderTrailHttpCallback  run in" .. tostring(pType) .. "," .. tostring(nEvent))
	if nEvent == 1 then
	elseif nEvent == 2 then
		if param1 ~= 200 then
		localHeaderTrail = nil
		local err = tostring(param2)
		tiros.moduledata.moduledata_set("web","headertrail_ptp","");
		end
	elseif nEvent == 3 then
		if localHeaderTrail ~= nil then
			localHeaderTrail = localHeaderTrail .. string.sub(param2, 1, param1);
		else
			localHeaderTrail = string.sub(param2, 1, param1);
		end
	elseif nEvent == 4 then
		--print("logic lua localGetHeaderTrailHttpCallback  complete: " ..localHeaderTrail)
		tiros.moduledata.moduledata_set("web","headertrail_ptp",localHeaderTrail);
		sendmessagetoLogic( 10, 7, "servertrack")
		localHeaderTrail = nil
	elseif nEvent == 5 then
		local err = tostring(param2)
		tiros.moduledata.moduledata_set("web","headertrail_ptp","");
		sendmessagetoLogic( 10, 7, err)
		localHeaderTrail = nil
	else
	end
	--print("logic lua localGetHeaderTrailHttpCallback  run out")
end


--内部函数， 获取队长轨迹
local function LocalGetHeaderTrail(groupid, headerid)
	local url = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",2101)
	url = url .. "?groupid=" .. tostring(groupid) .. "&headerid=" .. tostring(headerid)
	--print("logic lua " .. url)
        tiros.http.httpsendforlua("cdc_client", "getLeadeLocus", "getHeaderTrail", url, LocalGetHeaderTrailHttpCallback, nil, "Content-Type:application/x-www-form-urlencoded", "actionlocation:/navidog2News/weixin_getLeadeLocus.htm")
end



--下载头像回调
local function downgrouperheadimghttpevent(stype,dwEvent, dwParam1, dwParam2)

	if stype == "groupuserimg" then
		gTogetherList.DownloadingImg = false
		--下载成功
		if dwParam1 == 1 then
			--默认第一个为正在下载的人员信息
			local t = gTogetherList.downimglist[1];
			if  t.insertdb then
				--新信息保存
				tiros.groupbook.SaveUserinfo(t)								
			else
				--更新旧信息
				tiros.groupbook.UpdateUserinfo(t)
			end
			--通知平台头像更新	
			local tmp = {}
			tmp.path = t.HEADPATH
			tmp.uid = t.UID
			--修改头像
			EditGroupUserHead(t.AID,t.HEADPATH);
			local str = tiros.json.encode(tmp);
			sendmessagetoApp(EVT_MEET_HEADIMG_REPLACE,0,str)

			for k,v in pairs(gTogetherTable.togetherlist) do 
				if v.uid == tostring(t.UID) then
					v.url = t.HEADPATH
					--更新本地群组信息对应的文件
					local tempstr = tiros.json.encode(gTogetherTable)
					fileobj.Writefile(gFilePath,tempstr,true);
					tiros.moduledata.moduledata_set("web","togetherdetail_ptp",tempstr);
					
					--通知小宣头像更新
					local updateObj = {};
					updateObj.groupid = tostring(gTogetherTable.groupid);
					updateObj.atkgpid = tostring(gTogetherTable.atkgpid);
					updateObj.type = "2";
					updateObj.data = v;
					tiros.moduledata.moduledata_set("web","togetherupdate_ptp",tiros.json.encode(updateObj));
					sendmessagetoLogic( 10, 2, "togetherupdate_ptp")
					break;
				end
			end						
		end
		--更新队列继续下载
		table.remove(gTogetherList.downimglist,1)
		interface.checkstackdownimg()
	end
end

--检查下载头像队列
createmodule(interface, "checkstackdownimg",function()
	--队列不为空并且当前未下载其他头像
	if gTogetherList.downimglist ~= nil and not gTogetherList.DownloadingImg then
		local t = gTogetherList.downimglist[1];
		gTogetherList.DownloadingImg  = true
		tiros.downloadimg.downloadimage("groupuserimg",t.HEADURL,t.HEADPATH,downgrouperheadimghttpevent,nil)
	end
end)

local function pushstackofdownimg(insertdb,uid,aid,name,phone,headpath,headurl,addbookname,sex)
	--图片url不能为空
	if headurl == nil or string.len(headurl) == 0 then
		return	
	end 
	
	--不在群组时返回
	if gTogetherTable.groupid == nil then
		return;
	end
	if gTogetherList.downimglist == nil then
		gTogetherList.downimglist = {}
	end
	local t = {}
	--存储路径赋值
	if headpath == nil or string.len(headpath) == 0 then
		headpath = gAppHeadImgPath..uid..tiros.friendmanger.getfiletype(headurl)
		
	elseif headpath == gDefaultHeadImgPath then	
		return;
	end

	t.insertdb = insertdb
	t.UID = uid
	t.AID = aid
	t.NICKNAME = name
	t.PHONE = phone
	t.HEADPATH = headpath	
	t.HEADURL = headurl
	t.ADDBOOKNAME = addbookname
	t.SEX = sex
	--加入队列
	table.insert(gTogetherList.downimglist, t)
	--检验队列
	interface.checkstackdownimg()
end
----------------------------------------------------------
--清空群组
local function resetGroupinfo()

	tiros.moduledata.moduledata_set("web", "togetherdetail_ptp", "{}");
	fileobj.Removefile(gFilePath);
	gTogetherTable = {}		
end
--添加同行好友
local function addGpersonnelCallback(id,state,data)	
	if state == 1 then
		local obj = tiros.json.decode(data);
		if obj.success then			
			--存储发短信手机号
			if obj.phones ~= nil and  tostring(obj.phones) ~= "[]"then
				local jstrPhones = tiros.json.encode(obj.phones)
				tiros.moduledata.moduledata_set("web","togetherinvitephones_ptp",jstrPhones)
			else
				tiros.moduledata.moduledata_set("web","togetherinvitephones_ptp", "")
			end

			---------未加入成员-------------------
			local tempT = gTogetherTable.Notjoinedlist;
			for k,v in pairs(gTogetherList.addDataObj.phonename) do
				local  bExist = false;
				for k1,v1 in pairs(tempT) do
					if tostring(v1.PHONE) == tostring(v.phone) then
						bExist = true;
					end
				end
				if not bExist then
					local strDetail = tiros.groupbook.queryusrinfo(v.phone);
					if strDetail ~="" then
						local detailT = tiros.json.decode(strDetail);
						table.insert(gTogetherTable.Notjoinedlist,detailT);
					else
						local detailT = {}
						detailT.NAME = v.name;
						detailT.PHONE = v.phone;
						detailT.STATE = "0";
						detailT.NICKNAME = "";
						detailT.AID = "";
						detailT.UID = "";
						detailT.HEADPATH = "";
						detailT.HEADURL = "";
						table.insert(gTogetherTable.Notjoinedlist,detailT);
					end
				end
			end
			-------------------------------------------------
			-----------------------更新数据仓库--------------------------
			local strtogether = tiros.json.encode(gTogetherTable)
			tiros.moduledata.moduledata_set("web", "togetherdetail_ptp", strtogether);
			--保存信息到文件中----------	
			fileobj.Writefile(gFilePath,strtogether,true);
			------------------------------------------------	

			sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, ADDFRIEND_SUCCESS, "a")
			return			
		end
	end
	sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, ADDFRIEND_FAIL,"a")
end

--退出同行好友
local function quitGpersonnelCallback(id,state,data)			
	if state == 1 then --成功
		local obj = tiros.json.decode(data);
		if obj.success then
			resetGroupinfo()
			--删除数据库中的所有数据
			tiros.chatrecord.DellAllRecord();
			tiros.moduledata.moduledata_set("framework", "meet3currentgroupid" , "0");
			tiros.airtalkeemgr.StopUploadPositionInfo(4)
			sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, QUITGROUP_SUCCESS,"a")
			--for track 退出群组，清空队长轨迹
			--print("logic lua exitgroup event happened")
			sendmessagetoLogic( 10, 7, "exitgroup")
			local meetstate = tiros.moduledata.moduledata_get("web","meet_state_ptp");
			if meetstate ~= nil and meetstate ~= "" then
				local meetStatObj = tiros.json.decode(meetstate);
				meetStatObj.together = '3';
				local meetStatStr = tiros.json.encode(meetStatObj);
				tiros.moduledata.moduledata_set("web", "meet_state_ptp",meetStatStr);
			end
			if gTogetherList.commonCB ~= nil and type(gTogetherList.commonCB)== "function" then
				gTogetherList.commonCB(id,1)
			end
			return		
		else
			if gTogetherList.commonCB ~= nil and type(gTogetherList.commonCB)== "function" then
				gTogetherList.commonCB(id,0)
			end
		end
	elseif state == 0 then --失败
		if gTogetherList.commonCB ~= nil and type(gTogetherList.commonCB)== "function" then
			gTogetherList.commonCB(id,2)
		end		
	elseif state == 2 then --非200err
		if gTogetherList.commonCB ~= nil and type(gTogetherList.commonCB)== "function" then
			gTogetherList.commonCB(id,3)
		end
	end
	sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, QUITGROUP_FAIL,"a")	
	
end

--微信等创建同行组回调
local function WeChatCreateGpersonnelCallback(id,state,data)
print("weixin-WeChatCreateGpersonnelCallback-",id,state,data)
	--请求成功
	if state == 1 then
		local obj = tiros.json.decode(data);
		if tonumber(obj.responsecode) == 200 then			
			--取当前uid/aid
			local uid = tiros.moduledata.moduledata_get("framework", "uid"); 
			local aid = tiros.moduledata.moduledata_get("framework", "aid");
			--取平台创建同行组条件信息
			local createObj = tiros.json.decode(gTogetherList.createdata);						
			-------------------同行组信息保存-----------------------------------
			gTogetherTable = {}	
			gTogetherTable.joined = "0"		
			gTogetherTable.groupid = tostring(obj.groupid);
			gTogetherTable.atkgpid = tostring(obj.atkgpid)
			gTogetherTable.endtime = tostring(obj.endtime)
			gTogetherTable.message = tostring(obj.message)
			gTogetherTable.createtype = tostring(obj.createtype)
			gTogetherTable.placelon = tostring(createObj.lon);
			gTogetherTable.placelat = tostring(createObj.lat);
			gTogetherTable.place = createObj.poi;
			gTogetherTable.address = createObj.address;
			gTogetherTable.time = createObj.timeset;
			gTogetherTable.contracttime = "";
			gTogetherTable.headuid = tostring(uid);			
			gTogetherTable.togetherlist = {};
			gTogetherTable.Notjoinedlist = {};
			tiros.moduledata.moduledata_set('framework','meetheaderuid', gTogetherTable.headuid)
			--已加入成员------------------------------
			local member = {};
			member.aid = aid;
			member.uid = uid;
			--调用王成的接口，获取好友资料
			local friendObj = tiros.groupbook.GetUserinfo(uid);
			member.url = gDefaultHeadImgPath;
			member.nickname = "";
			member.phone = ""
			if friendObj ~= nil and friendObj.HEADPATH ~= nil then
				member.url = friendObj.HEADPATH;
				member.nickname = tostring(friendObj.NICKNAME);	
				member.phone = tostring(friendObj.PHONE)			
			end
			member.name = ""
			member.ishead = "1";
			member.lon = "";
			member.lat = "";
			member.lasttime = "";
			member.direction = "";
			member.isVioce = "0";
			member.speed = "";
			table.insert(gTogetherTable.togetherlist,member);
			--保存个人信息
			SaveGroupUserInfo(member.aid,member.nickname,member.url);
			----------------------------------------------------
			----]]---------------------------------------------
			local jtogetherlist = tiros.json.encode(gTogetherTable)
			tiros.moduledata.moduledata_set("web", "togetherdetail_ptp", jtogetherlist);
			--保存信息到文件中----------	
			fileobj.Writefile(gFilePath,jtogetherlist,true);

			------------------------------------------------					
			---------------------------------ccc修改，创建同行组之后保存同行状态--------------------------------------
			local meetstate = tiros.moduledata.moduledata_get("web","meet_state_ptp");
			if meetstate ~= nil and meetstate ~= "" then
				local meetStatObj = tiros.json.decode(meetstate);
				meetStatObj.together = '1';
				local meetStatStr = tiros.json.encode(meetStatObj);
				tiros.moduledata.moduledata_set("web", "meet_state_ptp",meetStatStr);
				commlib.calljavascript("system.callback(519);");		--通知前端web页面			
			end	
		
			--[[--------------------------------------------------------------------	
			--存储发短信手机号
			if obj.phones ~= nil and  obj.phones ~= "[]"then
				local jstrPhones = tiros.json.encode(obj.phones)
				tiros.moduledata.moduledata_set("web", "togetherinvitephones_ptp", jstrPhones);
			else
				tiros.moduledata.moduledata_set("web", "togetherinvitephones_ptp", "");
			end
			--]]------------------

			--创建同行组成功
			-- for track 队长微信方式创建组成功，打开队长轨迹的锁定，不请求服务器队长轨迹，直接记录轨迹点
			--print("logic lua creategroup ok groupid and headerid:" .. gTogetherTable.groupid .. "," .. gTogetherTable.headuid)
			sendmessagetoLogic( 10, 7, "creategroup")
			sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, CREATEGROUP_SUCCESS,"a")
			tiros.moduledata.moduledata_set("framework", "meet3currentgroupid" , gTogetherTable.groupid)
			tiros.airtalkeemgr.UploadPositionInfo(4)			
		elseif tonumber(obj.responsecode) == 300 then
			--创建同行组失败,用户已加入同行组
			sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, CREATEGROUP_IN_ANOTHERGROUP_FAIL,"a")
		elseif tonumber(obj.responsecode) == 400 then
			--创建同行组失败
			sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, CREATEGROUP_NORMAL_FAIL,"a")
		end
	else
		--创建同行组失败
		sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, CREATEGROUP_NORMAL_FAIL,"a")
	end
end

--通讯录创建同行组回调
local function createGpersonnelCallback(id,state,data)	
	--请求成功
	if state == 1 then
		local obj = tiros.json.decode(data);
		if tonumber(obj.responsecode) == 200 then			
			--取当前uid/aid
			local uid = tiros.moduledata.moduledata_get("framework", "uid"); 
			local aid = tiros.moduledata.moduledata_get("framework", "aid");
			--取平台创建同行组条件信息
			local createObj = tiros.json.decode(gTogetherList.createdata);						
			-------------------同行组信息保存-----------------------------------
			gTogetherTable = {}	
			gTogetherTable.joined = "0"		
			gTogetherTable.groupid = tostring(obj.groupid);
			gTogetherTable.atkgpid = tostring(obj.atkgpid)
			gTogetherTable.endtime = tostring(obj.endtime)
			gTogetherTable.placelon = tostring(createObj.lon);
			gTogetherTable.placelat = tostring(createObj.lat);
			gTogetherTable.place = createObj.poi;
			gTogetherTable.address = createObj.address;
			gTogetherTable.time = createObj.timeset;
			gTogetherTable.contracttime = "";
			gTogetherTable.headuid = tostring(uid);			
			gTogetherTable.togetherlist = {};
			gTogetherTable.Notjoinedlist = {};
			tiros.moduledata.moduledata_set('framework','meetheaderuid', gTogetherTable.headuid)
			--已加入成员------------------------------
			local member = {};
			member.aid = aid;
			member.uid = uid;
			--调用王成的接口，获取好友资料
			local friendObj = tiros.groupbook.GetUserinfo(uid);
			member.url = gDefaultHeadImgPath;
			member.nickname = "";
			member.phone = ""
			if friendObj ~= nil and friendObj.HEADPATH ~= nil then
				member.url = friendObj.HEADPATH;
				member.nickname = tostring(friendObj.NICKNAME);	
				member.phone = tostring(friendObj.PHONE)			
			end
			member.name = ""
			member.ishead = "1";
			member.lon = "";
			member.lat = "";
			member.lasttime = "";
			member.direction = "";
			member.isVioce = "0";
			member.speed = "";
			table.insert(gTogetherTable.togetherlist,member);
			SaveGroupUserInfo(member.aid,member.nickname,member.url);
			----------------------------------------------------
			---------未加入成员-------------------
			for k,v in pairs(createObj.phonename) do
				local strDetail = tiros.groupbook.queryusrinfo(v.phone);
				if strDetail ~="" then
					local detailT = tiros.json.decode(strDetail);
					table.insert(gTogetherTable.Notjoinedlist,detailT);
				else
					local detailT = {}
					detailT.NAME = v.name;
					detailT.PHONE = v.phone;
					detailT.STATE = "0";
					detailT.NICKNAME = "";
					detailT.AID = "";
					detailT.UID = "";
					detailT.HEADPATH = "";
					detailT.HEADURL = "";
					table.insert(gTogetherTable.Notjoinedlist,detailT);
				end
			end
			-------------------------------------------------
			local jtogetherlist = tiros.json.encode(gTogetherTable)
			tiros.moduledata.moduledata_set("web", "togetherdetail_ptp", jtogetherlist);
			--保存信息到文件中----------	
			fileobj.Writefile(gFilePath,jtogetherlist,true);

			------------------------------------------------					
			---------------------------------ccc修改，创建同行组之后保存同行状态--------------------------------------
			local meetstate = tiros.moduledata.moduledata_get("web","meet_state_ptp");
			if meetstate ~= nil and meetstate ~= "" then
				local meetStatObj = tiros.json.decode(meetstate);
				meetStatObj.together = '1';
				local meetStatStr = tiros.json.encode(meetStatObj);
				tiros.moduledata.moduledata_set("web", "meet_state_ptp",meetStatStr);
				commlib.calljavascript("system.callback(519);");		--通知前端web页面			
			end			
			-----------------------------------------------------------------------	
			--存储发短信手机号
			if obj.phones ~= nil and  obj.phones ~= "[]"then
				local jstrPhones = tiros.json.encode(obj.phones)
				tiros.moduledata.moduledata_set("web", "togetherinvitephones_ptp", jstrPhones);
			else
				tiros.moduledata.moduledata_set("web", "togetherinvitephones_ptp", "");
			end
			--创建同行组成功
			-- track 队长通讯录方式创建组成功，打开队长轨迹的锁定，不请求服务器队长轨迹，直接记录轨迹点
			--print("logic lua creategroup ok groupid and headerid:" .. gTogetherTable.groupid .. "," .. gTogetherTable.headuid)
			sendmessagetoLogic( 10, 7, "creategroup")
			sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, CREATEGROUP_SUCCESS,"a")
			tiros.moduledata.moduledata_set("framework", "meet3currentgroupid" , gTogetherTable.groupid);
			tiros.airtalkeemgr.UploadPositionInfo(4)
		elseif tonumber(obj.responsecode) == 300 then
			--创建同行组失败,用户已加入同行组
			sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, CREATEGROUP_IN_ANOTHERGROUP_FAIL,"a")
		elseif tonumber(obj.responsecode) == 400 then
			--创建同行组失败
			sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, CREATEGROUP_NORMAL_FAIL,"a")
		end
	else
		--创建同行组失败
		sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, CREATEGROUP_NORMAL_FAIL,"a")
	end
end

--修改约定时间、地点及续约24小时
local function editGathertimeCallback(id,state,data)
	if state == 1 then
		local resultObj = tiros.json.decode(data);
		if resultObj.success then
			local obj = tiros.json.decode(gTogetherList.jsonEdittime);
			local third = "";
			if obj.lon ~= '' and obj.lon ~= nil then				
				gTogetherTable.placelon = tostring(obj.lon);
				third = "1";
			end
			if obj.lat ~= '' and obj.lat ~= nil then
				gTogetherTable.placelat = tostring(obj.lat);
				third = "1";
			end
			if obj.poi ~= '' and obj.poi ~= nil then
				gTogetherTable.place = obj.poi;
				third = "1";
			end
			if obj.timeset ~= '' and obj.timeset ~= nil then
				gTogetherTable.time = obj.timeset;
				third = "2";
			end
			if obj.address ~= '' and obj.address ~= nil then
				gTogetherTable.address = obj.address;
				third = "1";
			end

			if tonumber(obj.contracttime) == 1 then
				gTogetherTable.contracttime = obj.contracttime;
				third = "3";
				gTogetherTable.endtime = tostring(obj.endtime)
			end
			local tmp = tiros.json.encode(gTogetherTable)
			fileobj.Writefile(gFilePath, tmp, true);
			tiros.moduledata.moduledata_set("web","togetherdetail_ptp",tmp);
			--修改后，调用小宣的接口通知他去获取最新数据
			local updateObj = {};
			updateObj.type = "3";
			updateObj.groupid = tostring(obj.groupid);
			updateObj.data = nil;
			tiros.moduledata.moduledata_set("web","togetherupdate_ptp",tiros.json.encode(updateObj));
			sendmessagetoLogic( 10, 2, "togetherupdate_ptp")
			--通知平台已更新数据成功
			sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA,  MODIFY_TIME_PLACE_SUCCESS,third)
			--通知前端web页面
			commlib.calljavascript("system.callback(519);");		
		else
			sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA,  MODIFY_TIME_PLACE_FAIL,"a");
		end
	else
		sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA,  MODIFY_TIME_PLACE_FAIL,"a")
	end
end

--同意加入结伴同行
--0-400错，1-成功，2-非200错，3-超时，4-已加入组
local function acceptInviteCallback(id,state,data)		
--[[
{"member":[
{"aid":"213912128","imgpath":"http://cdn2.lbs8.com/files/headpic/headpic824.png","name":"Adan","phone":"18910381812","sex":"1","uid":8881},
{"aid":"213912158","imgpath":"http://cdn2.lbs8.com/files/headpic/headpic856.png","name":"2220","phone":"13522222220","sex":null,"uid":30532734}],"poi":"","atkgpid":"C2885","lon":0,"timeset":"","address":"","responsecode":200,"groupid":1175,"lat":0,"headid":8881}
--]]
	interface.WebRefuseInvite(gAcceptGroupId)
		
	if state == 1 then
		local dataObj = tiros.json.decode(data);	
							
		if tonumber(dataObj.responsecode) == 200 then
			--同行组的信息
			gTogetherTable = {};
			gTogetherTable.joined = "1"
			gTogetherTable.togetherlist = {};
			gTogetherTable.Notjoinedlist = {};
			gTogetherTable.groupid = tostring(dataObj.groupid);
			gTogetherTable.atkgpid = tostring(dataObj.atkgpid);
			gTogetherTable.address = tostring(dataObj.address);
			gTogetherTable.placelon = tostring(dataObj.lon);
			gTogetherTable.placelat = tostring(dataObj.lat);
			gTogetherTable.endtime = tostring(dataObj.endtime);
			gTogetherTable.place = dataObj.poi;
			gTogetherTable.time = dataObj.timeset;
			gTogetherTable.headuid = tostring(dataObj.headid);
			gTogetherTable.contracttime = "";
			tiros.moduledata.moduledata_set('framework','meetheaderuid', gTogetherTable.headuid)
			for k,v in pairs(dataObj.member) do
				local obj = {};
				obj.aid = tostring(v.aid);
				obj.uid = tostring(v.uid);
				obj.phone = tostring(v.phone)
				obj.nickname = tostring(v.name);				
				obj.url = gDefaultHeadImgPath;				
				--获取本地好友资料
				local tempT = tiros.groupbook.GetUserinfo(tostring(v.uid));
				if tempT ~= nil and tempT.HEADPATH ~= nil then
					obj.url = tempT.HEADPATH;					
					--下载头像
					if tempT.HEADURL ~= v.imgpath then
					pushstackofdownimg(false,v.uid,v.aid,v.name,v.phone,tempT.HEADPATH,v.imgpath,tiros.friendmanger.GetAddrBookName(v.phone),"")
					end							
				else
					pushstackofdownimg(true,v.uid,v.aid,v.name,v.phone,"",v.imgpath,tiros.friendmanger.GetAddrBookName(v.phone),"")
				end
				obj.name = ""
				local addbookname = tiros.friendmanger.GetAddrBookName(v.phone)
				if addbookname ~= nil then
					obj.name = tostring(addbookname);
				end
				if tostring(dataObj.headid) == tostring(v.uid) then
				 	obj.ishead = "1";
				else
					obj.ishead = "0";
				end
				
				obj.lon = tostring(v.lon);
				obj.lat = tostring(v.lat);
				if obj.lon == "0" or obj.lat == "0" then
					obj.lon =""
					obj.lat =""
				end
				
				obj.lasttime = "";
				if v.pushdate ~= nil then
					obj.lasttime = resolveposdate(tostring(v.pushdate));
				end
				obj.direction = "";
				obj.isVioce = "0";
				obj.speed = "";
				table.insert(gTogetherTable.togetherlist,obj);
				--保存同行组所有成员信息到数据库
				if obj.name ~= nil and obj.name ~= "" then
					SaveGroupUserInfo(obj.aid,obj.name,obj.url);
				else
					SaveGroupUserInfo(obj.aid,obj.nickname,obj.url);
				end
				
			end		
			local jstrGroup = tiros.json.encode(gTogetherTable)	
			fileobj.Writefile(gFilePath, jstrGroup, true);
			tiros.moduledata.moduledata_set("web","togetherdetail_ptp", jstrGroup);
			--通知小宣
			local updateObj = {};
			updateObj.type = "3";
			tiros.moduledata.moduledata_set("web","togetherupdate_ptp",tiros.json.encode(updateObj));
			--sendmessagetoLogic( 10, 2, "togetherupdate_ptp")			
			--sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA,  AGREEJOINGROUP_SUCCESS,"a")
			-- track 队员同意加入组，第一次进入组，需要请求服务器队长轨迹，设置阻塞点的接收，等待回调后获取数据后，才能读取队长轨迹点
			--print("logic lua firstjoingroup groupid and headeruid:" .. gTogetherTable.groupid .. ", " .. gTogetherTable.headuid)
			sendmessagetoLogic( 10, 7, "firstjoingroup")
			LocalGetHeaderTrail(gTogetherTable.groupid, gTogetherTable.headuid);
			tiros.moduledata.moduledata_set("framework", "meet3currentgroupid" , gTogetherTable.groupid);
			tiros.airtalkeemgr.UploadPositionInfo(4)
			if gTogetherList.commonCB ~= nil and type(gTogetherList.commonCB)== "function" then
				
				gTogetherList.commonCB(id,1)
			end
			
		elseif tonumber(dataObj.responsecode) == 300 then
			--失败,用户已加入同行组
			if gTogetherList.commonCB ~= nil and type(gTogetherList.commonCB)== "function" then
				gTogetherList.commonCB(id,4)
			end

		elseif tonumber(dataObj.responsecode) == 400 then
			--失败
			if gTogetherList.commonCB ~= nil and type(gTogetherList.commonCB)== "function" then
				
				gTogetherList.commonCB(id,0)
			end			
		end
	elseif state == 0 then
		if gTogetherList.commonCB ~= nil and type(gTogetherList.commonCB)== "function" then
			
			gTogetherList.commonCB(id,2)
		end		
	elseif state == 2 then
		if gTogetherList.commonCB ~= nil and type(gTogetherList.commonCB)== "function" then
			
			gTogetherList.commonCB(id,3)
		end		
	end
	
end

--获取短信
local function getsmsmessagecallback(id,state,data)
	if  state == 1 then
		local resultObj = tiros.json.decode(data);
		if resultObj.success then
			sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, GET_SMSMESAGE_SUCCESS,resultObj.content )
			return;
		end
	end
	sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, GET_SMSMESAGE_FAIL,"a")
end

--获取小蜜短信
local function getBeeMessageCallback(id,state,data)
--print("bee---Callback",id,state,data)
	if  state == 1 then
		local resultObj = tiros.json.decode(data);
		if resultObj.success then
			sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, GET_BEESMS_SUCCESS, resultObj.content )
			return;
		end
	end
	sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, GET_BEESMS_FAIL,"a")
end


local function GetHeaderTrailHttpCallback(pType, nEvent, param1, param2)
	--print("GetHeaderTrailHttpCallback  run in",pType, nEvent, param1, param2)
	if nEvent == 1 then
	elseif nEvent == 2 then
		if param1 ~= 200 then
		headerTrail = nil
		local err = tostring(param2)
		tiros.moduledata.moduledata_set("web","headertrail_ptp","");
		end
	elseif nEvent == 3 then
		if headerTrail ~= nil then
			headerTrail = headerTrail .. string.sub(param2, 1, param1);
		else
			headerTrail = string.sub(param2, 1, param1);
		end
	elseif nEvent == 4 then
		tiros.moduledata.moduledata_set("web","headertrail_ptp",headerTrail);
		sendmessagetoLogic( 10, 7, "servertrack")
		headerTrail = nil
	elseif nEvent == 5 then
		local err = tostring(param2)
		tiros.moduledata.moduledata_set("web","headertrail_ptp","");
		sendmessagetoLogic( 10, 7, err)
		headerTrail = nil
	else
	end
	--print("GetHeaderTrailHttpCallback  run out")
end



-------------------对外接口---------------------------------------------------

--查询见面服务状态
--查询同行好友列表
local function getMeetStateCallback(id,state,data)
	if state == 1 then
		local obj = tiros.json.decode(data);
		if obj.state >= 4 and obj.success == true then
			gMeetState = 1;
			local readData = fileobj.Readfile(gFilePath);
			--判断本地群组信息是否存在，如果不存在则从服务器获取
			if readData ~= '' and readData ~= nil then
				gTogetherTable = tiros.json.decode(readData);
				local uid = tiros.moduledata.moduledata_get("framework", "uid"); 
				for k,v in pairs(gTogetherTable.togetherlist) do
					if v.uid ~= nil and v.uid ~= '' then						
						tiros.moduledata.moduledata_set("web","togetherdetail_ptp",readData);					
						sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, REGETGROUPINFO_SUCCESS,"a")
						return
					else
						interface.WebInitGpersonnel(id);
					end
				end
			else
				interface.WebInitGpersonnel(id);
			end
		else
			--群组已经不存在
			sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, REGETGROUPINFO_NOEXIST_FAIL,"a")
		end
	else
		
	end
end

--获取队长轨迹
createmodule(interface,"GetHeaderTrail", function(groupid, headerid)
	local url = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",2101)
	--print("logic lua url " .. url)
	url = url .. "?groupid=" .. groupid .. "&headerid=" .. headerid
        tiros.http.httpsendforlua("cdc_client", "getLeadeLocus", "getHeaderTrail", url, GetHeaderTrailHttpCallback, nil,  "Content-Type:application/x-www-form-urlencoded", "actionlocation:/navidog2News/weixin_getLeadeLocus.htm")
end);

--添加同行好友
createmodule(interface,"WebAddGpersonnel", function(json,id,callback)
	local jsonObj = tiros.json.decode(json);
	local opt = {};
	local condi = {};
	gTogetherList.addDataObj = jsonObj
	condi.groupid = jsonObj.groupid;
	condi.rediskey = GetDate();
	condi.phonename  = tiros.json.encode(jsonObj.phonename);
	opt.method = "POST";
	opt.data = condi;
	opt.header = {};
	opt.header["actionlocation"] = "/navidog2News/together_addGpersonnel.htm";
	opt.header["uid"] = tiros.moduledata.moduledata_get("framework", "uid"); 
	local sUrlPost = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_POST_URL);
	HttpSend(id,addGpersonnelCallback,sUrlPost,opt,"addGpersonnel");
end);

--退出同行好友
createmodule(interface,"WebQuitGpersonnel", function(id,callback)
	local opt = {};
	local condi = {};
	condi.groupid = gTogetherTable.groupid;
	condi.rediskey = GetDate();
	opt.method = "POST";
	opt.data = condi;
	opt.header = {};
	opt.header["actionlocation"] = "/navidog2News/together_quitGpersonnel.htm";
	opt.header["uid"] = tiros.moduledata.moduledata_get("framework", "uid"); 
	gTogetherList.commonCB = callback	
	local sUrlPost = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_POST_URL);
	HttpSend(id,quitGpersonnelCallback,sUrlPost,opt,"quitGpersonnel");
end);

--同行好友列表
createmodule(interface,"WebGetGpersonnelList", function(id,callback)
	--获取见面状态
	local opt = {};
	local condi = {};
	condi.uid = tiros.moduledata.moduledata_get("framework", "uid"); 
	opt.method = "GET";
	opt.data = condi;
	opt.header = {};
	opt.header["actionlocation"] = "/navidog2News/meet_getMeetstate.htm";
	opt.header["uid"] = tiros.moduledata.moduledata_get("framework", "uid"); 
	local sUrlGet = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_GET_URL);
	HttpSend(id,getMeetStateCallback,sUrlGet,opt,"getMeetstate");
end);

--创建通行组(通讯录创建)
local function webCreateNomalGroup(json,id,callback)
		gTogetherList.createdata = json;
		local opt = {};
		local condi = {};
		local jsonObj = tiros.json.decode(json);
		condi.timeset = jsonObj.timeset;
		condi.phonename = tiros.json.encode(jsonObj.phonename);
		condi.lon = jsonObj.lon;
		condi.lat = jsonObj.lat;
		condi.poi = jsonObj.poi;
		condi.address = jsonObj.address;
		--condi.message = jsonObj.message;
		condi.rediskey = GetDate();
		opt.method = "POST";
		opt.data = condi;
		opt.header = {};
		opt.header["uid"] = tiros.moduledata.moduledata_get("framework", "uid");
		opt.header["actionlocation"] = "/navidog2News/together_createGpersonnel.htm";
		local sUrlPost = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_POST_URL);
		HttpSend(id,createGpersonnelCallback,sUrlPost,opt,"createGpersonnel");
end

--创建通行组(0:通讯录创建;1:微信创建;2:朋友圈创建;3:新浪微博;4:腾讯微博;5:电子邮件)
local function webCreateGroupByWeChatEtc(json,id,nCreateType,callback)
		gTogetherList.createdata = json;
		local opt = {};
		local condi = {};
		local jsonObj = tiros.json.decode(json);
		condi.timeset = jsonObj.timeset;
		condi.phonename = tiros.json.encode(jsonObj.phonename);
		condi.lon = jsonObj.lon;
		condi.lat = jsonObj.lat;
		condi.poi = jsonObj.poi;
		condi.address = jsonObj.address;
		condi.message = jsonObj.message;
		condi.createtype = nCreateType;
		condi.rediskey = GetDate();
		condi.speed = tiros.moduledata.moduledata_get("logic","speed")
		opt.method = "POST";
		opt.data = condi;
		opt.header = {};
		opt.header["uid"] = tiros.moduledata.moduledata_get("framework", "uid");
		opt.header["actionlocation"] = "/navidog2News/weixin_weixinCreateGpersonnel.htm";
		local sUrlPost = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_POST_URL);
		HttpSend(id,WeChatCreateGpersonnelCallback,sUrlPost,opt,"WeChatCreateGpersonnel");
end

--创建同行组
createmodule(interface,"WebCreateGpersonnel", function(json,id,nCreateType,callback)
--{"poi":"","lon":0,"uids":["18942704"],"timeset":"2013-03-13 12:00","address":"","lat":0}
	local dataBase = fileobj.Readfile(gFilePath);	
	if dataBase ~= "" and dataBase ~= nil then
		--创建同行组失败
		sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, CREATEGROUP_NORMAL_FAIL,"a")
	else
		if nCreateType == 0 then
			--通讯录创建
			webCreateNomalGroup(json,id,callback)
		else
			--微信等创建
			webCreateGroupByWeChatEtc(json,id,nCreateType,callback)
		end
	end
end);

--修改约定时间、地点及续约24小时
createmodule(interface,"WebEditGathertime", function(json,id,callback)
	gTogetherList.jsonEdittime = json;
	local opt = {};
	local condi = {};
	local jsonObj = tiros.json.decode(json);
	
	condi.timeset = jsonObj.timeset;
	condi.groupid = jsonObj.groupid;
	condi.lon = jsonObj.lon;
	condi.lat = jsonObj.lat;
	condi.poi = jsonObj.poi;
	condi.address = jsonObj.address;
	condi.contracttime = jsonObj.contracttime;
	condi.rediskey = GetDate();
	opt.method = "POST";
	opt.data = condi;
	opt.header = {};
	opt.header["actionlocation"] = "/navidog2News/together_editGathertime.htm";
	opt.header["uid"] = tiros.moduledata.moduledata_get("framework", "uid"); 
	local sUrlPost = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_POST_URL);
	HttpSend(id,editGathertimeCallback,sUrlPost,opt,"editGathertime");
end);

--发起同意加入结伴同行请求
createmodule(interface,"WebSendAcceptInvite", function(groupid,id,callback)
	local opt = {};
	local condi = {};
	condi.groupid = groupid;
	condi.rediskey = GetDate();
	opt.method = "POST";
	opt.data = condi;
	opt.header = {};
	opt.header["actionlocation"] = "/navidog2News/together_agreeGpersonnel.htm";
	opt.header["uid"] = tiros.moduledata.moduledata_get("framework", "uid"); 
	gTogetherList.commonCB = callback
	local sUrlPost = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_POST_URL);
	HttpSend(id,acceptInviteCallback,sUrlPost,opt,"agreeGpersonnel");
end);

--同意加入结伴同行
createmodule(interface,"WebAcceptInvite", function(groupid,id,callback)	
	gAcceptGroupId = groupid	
	tiros.moduledata.moduledata_set("web","togetherdetail_ptp","{}");
	interface.WebSendAcceptInvite(groupid,id,callback);	
end);

--拒绝加入结伴同行
createmodule(interface,"WebRefuseInvite", function(groupid)	
	for k,v in pairs(gInviteTable) do 
		if v.groupid == groupid then
			table.remove(gInviteTable,k)
		end
	end
	local strData = tiros.json.encode(gInviteTable);
	tiros.moduledata.moduledata_set("web","together_invite_ptp",strData);
	fileobj.Writefile(gInviteDataFile,strData,true);
	return true
end);



--获取校验状态
createmodule(interface,"WebGetCheckGroupState", function(id,callback)	
	--保存id对应的回调函数
	local tCheckStatus = {};
	tCheckStatus.notify = callback;
	registerHandle(gtHandle, nil ,id, tCheckStatus);

	--保存sID
	table.insert(gtIDs, id);


	if gCheckServerState == 3 then
		ATKEvent_login()--重新校验
	end
	if nil ~= callback then
		callback(id, gCheckServerState);
	end
end);

--获取短信内容
createmodule(interface,"WebGetSMSMessage", function(json,id,callback)	
	local opt = {};
	local condi = {};
	local uid = tiros.moduledata.moduledata_get("framework","uid")
	local friendObj = tiros.groupbook.GetUserinfo(uid);
	local lon,lat = GetLonLat();
	condi.phone = friendObj.PHONE
	condi.phones = json
	condi.groupid = gTogetherTable.groupid;
	condi.type = "3";
	condi.speed = tiros.moduledata.moduledata_get("logic","speed")
	condi.rediskey = GetDate();
	opt.method = "POST";
	opt.data = condi;
	opt.header = {};
	opt.header["actionlocation"] = "/navidog2News/together_togetherURL.htm";
	opt.header["mobileid"] = tiros.moduledata.moduledata_get("framework","mobileid"); 
	opt.header["selflon"] =	lon;
	opt.header["selflat"] = lat;
	local sUrlPost = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_POST_URL);
	HttpSend("WebGetSMSMessage",getsmsmessagecallback, sUrlPost, opt,"togetherURL");
end);


--获取小蜜短信内容
createmodule(interface,"WebGetBeeMessage", function(id,callback)

	local opt = {};
	local condi = {};
	local lon,lat = GetLonLat();
	condi.phone = tiros.moduledata.moduledata_get("framework","phone");
print("bee---WebGetBeeMessage",condi.phone)
	condi.type = "4";
	opt.method = "POST";
	opt.data = condi;
	opt.header = {};
	opt.header["actionlocation"] = "/navidog2News/together_togetherVideo.htm";
	opt.header["mobileid"] = tiros.moduledata.moduledata_get("framework","mobileid");
	opt.header["uid"] = tiros.moduledata.moduledata_get("framework","uid");
	opt.header["selflon"] =	lon;
	opt.header["selflat"] = lat;
	opt.header["version"] = tiros.moduledata.moduledata_get("framework","version");
	--gUrlPost = "http://dev8.lbs8.com/general_Post"
	local sUrlPost = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_POST_URL);
	HttpSend("WebGetBeeMessage",getBeeMessageCallback, sUrlPost, opt, "togetherVideo");
end);

--------------------------------------------------------------------------------
--1，用户在群组里时的校验
local function getgroupwheningroup(dataObj)
--[[
 {"member":[
{"aid":"213912214","imgpath":"http://cdn2.lbs8.com/files/headpic/headpic930.png","lat":143720269,"lon":419256955,"name":"王成cc","phone":"13601248824","sex":"1","uid":7597642},
{"aid":"213912128","imgpath":"http://cdn2.lbs8.com/files/headpic/headpic929.png","lat":143719595,"lon":419257701,"name":"小宣的长名字够长长长长吗","phone":"18910381812","sex":"1","uid":8881}
],
"endtime":"2013-04-16 09:48:39",
"poi":"","atkgpid":"C3664","iseffect":1,"lon":0,"timeset":"","address":"","groupid":2019,
"success":true,"lat":0,"headid":7597642}
--]]	
	gTogetherTable.groupid = tostring(dataObj.groupid);
	gTogetherTable.atkgpid = tostring(dataObj.atkgpid);
	gTogetherTable.togetherlist = {};	
	gTogetherTable.address = dataObj.address;
	gTogetherTable.placelon = tostring(dataObj.lon);
	gTogetherTable.placelat = tostring(dataObj.lat);
	gTogetherTable.place = dataObj.poi;
	gTogetherTable.time = dataObj.timeset;
	gTogetherTable.contracttime = "";
	gTogetherTable.endtime = tostring(dataObj.endtime);
	gTogetherTable.headuid = tostring(dataObj.headid);
	tiros.moduledata.moduledata_set('framework','meetheaderuid', gTogetherTable.headuid)
	--初始化成功，拼接数据
	if dataObj.member ~= nil then
		for k,v in pairs(dataObj.member) do
			local obj = {};
			obj.aid = tostring(v.aid);
			obj.uid = tostring(v.uid);		
			--调用王成的接口，获取好友资料
			obj.url = gDefaultHeadImgPath			
			obj.phone = tostring(v.phone)
			local friendObj = tiros.groupbook.GetUserinfo(tostring(v.uid));			
			if friendObj ~= nil and friendObj.HEADPATH ~= nil then
				obj.url = friendObj.HEADPATH;		
				if friendObj.HEADURL ~= v.imgpath then
				pushstackofdownimg(false,v.uid,v.aid,v.name,v.phone,obj.url,v.imgpath,tiros.friendmanger.GetAddrBookName(v.phone),"")
				end
			else
				pushstackofdownimg(true,v.uid,v.aid,v.name,v.phone,"",v.imgpath,tiros.friendmanger.GetAddrBookName(v.phone),"")
			end			
			
			obj.nickname = tostring(v.name)
			if tostring(v.uid) == tostring(dataObj.headid) then
				obj.ishead ="1"
			else
				obj.ishead ="0"
			end					
			obj.name = ""
			local addbookname = tiros.friendmanger.GetAddrBookName(v.phone)
			if addbookname ~= nil then
				obj.name = tostring(addbookname);
			end	
			obj.lon = tostring(v.lon);
			obj.lat = tostring(v.lat);
			if obj.lon == "0" or obj.lat == "0" then
				obj.lon =""
				obj.lat =""
			end

			obj.lasttime = "";
			if v.pushdate ~= nil then
				obj.lasttime = resolveposdate(tostring(v.pushdate));
			end

			obj.direction = "";
			obj.isVioce = "0";
			obj.speed = "";
			table.insert(gTogetherTable.togetherlist,obj);
			if obj.name ~= nil and obj.name ~= "" then
				SaveGroupUserInfo(obj.aid,obj.name,obj.url);
			else
				SaveGroupUserInfo(obj.aid,obj.nickname,obj.url);
			end
			
			----------------------------------------------
			if gTogetherTable.Notjoinedlist ~= nil then
				for ka,va in pairs(gTogetherTable.Notjoinedlist) do
					if tostring(va.PHONE) == tostring(v.phone) then
						gTogetherTable.joined = "1"
						table.remove(gTogetherTable.Notjoinedlist,ka);
						break;
					end
				end
			end
			-------------------------------------------	
							
		end
		if #gTogetherTable.togetherlist >1 then
			gTogetherTable.joined = "1"
		end		
	end
	--将最新的群组信息保存到本地文件中
	local jsonFile = tiros.json.encode(gTogetherTable)
	fileobj.Writefile(gFilePath,jsonFile,true);
	--更新数据仓库
	tiros.moduledata.moduledata_set("web","togetherdetail_ptp",jsonFile);

	--通知小宣
	--track
	
	local selfuid = tiros.moduledata.moduledata_get("framework","uid");
	if selfuid == nil then
		--print("logic lua selfuid is nil")
	else
		--print("logic lua getgroupwheningroup servertrackrequestsend, selfuid, headeruid,groupid " .. selfuid .. ", " ..  gTogetherTable.headuid .. ", " .. gTogetherTable.groupid)
		sendmessagetoLogic( 10, 7, "servertrackrequestsend")
		LocalGetHeaderTrail(gTogetherTable.groupid, gTogetherTable.headuid);
	end

	local updateObj = {};
	updateObj.type = "3";
	updateObj.groupid = gTogetherTable.groupid;
	updateObj.headuid = gTogetherTable.headuid;
	updateObj.data = "";
	tiros.moduledata.moduledata_set("web","togetherupdate_ptp",tiros.json.encode(updateObj));
	sendmessagetoLogic( 10, 2, "togetherupdate_ptp")
	--通知web邀请信息	
	local invitedata = fileobj.Readfile(gInviteDataFile);
	if invitedata ~= nil and invitedata ~= "" then	
		tiros.moduledata.moduledata_set("web","together_invite_ptp",invitedata);
		gInviteTable = tiros.json.decode(invitedata)
	end 
	--通知web登录状态
	local meetdata = tiros.moduledata.moduledata_get("web","meet_state_ptp");
	local meetStatObj = nil;
	if meetdata ~= nil and meetdata ~= "" then
		meetStatObj = tiros.json.decode(meetdata);
		meetStatObj.together = '1';
	else
		meetStatObj = {};
		meetStatObj.here = '0';
		meetStatObj.where = '0';
		meetStatObj.together = '1';
		meetStatObj.lon = "";
		meetStatObj.lat = "";
		meetStatObj.time = "";
	end
	local meetStatStr = tiros.json.encode(meetStatObj);
	tiros.moduledata.moduledata_set("web","meet_state_ptp",meetStatStr);
	commlib.calljavascript("system.callback(519);");		--通知前端web页面
end
--用户不再群组里面
local function getgroupwhennotingroup()
	gTogetherTable = {}
	--用户不再群组里面,更改用户的结伴同行状态为“退出同行状态”
	local meetdata = tiros.moduledata.moduledata_get("web","meet_state_ptp");
	local meetStatObj = nil;
	if meetdata ~= nil and meetdata ~= "" then
		meetStatObj = tiros.json.decode(meetdata);
		meetStatObj.together = "3";
	else
		meetStatObj = {};
		meetStatObj.here = '0';
		meetStatObj.where = '0';
		meetStatObj.together = '3';
		meetStatObj.lon = "";
		meetStatObj.lat = "";
		meetStatObj.time = "";
	end
	--通知web邀请信息	
	local invitedata = fileobj.Readfile(gInviteDataFile);
	if invitedata ~= nil and invitedata ~= "" then	
		tiros.moduledata.moduledata_set("web","together_invite_ptp",invitedata);
		gInviteTable = tiros.json.decode(invitedata)
	else
		tiros.moduledata.moduledata_set("web","together_invite_ptp","{}");
		gInviteTable = {}
	end 
	local meetStatStr = tiros.json.encode(meetStatObj);
	tiros.moduledata.moduledata_set("web","meet_state_ptp",meetStatStr);
	commlib.calljavascript("system.callback(519);");		--通知前端web页面
end
-------------校验群组回调--------------------
local function getGroupFromServerCallback(id,state,data)
--[[
data={"member":[{"aid":"111033749","imgpath":null,"name":"无名氏","phone":"15101502334","sex":null,"uid":18688667}],"poi":"","atkgpid":"C2076","iseffect":1,"lon":0,"timeset":"","groupid":53,"success":true,"lat":0,"headid":18688667}
--]]
	--print("ccccheckgroup="..data)
	if state == 1 then
		local dataObj = tiros.json.decode(data);
		if dataObj.success == true then
			gCheckServerState = 2
			if dataObj.iseffect ~= 0 then
				--用户在群组里
				getgroupwheningroup(dataObj)
				tiros.moduledata.moduledata_set("framework", "meet3currentgroupid" , dataObj.groupid);
				tiros.airtalkeemgr.UploadPositionInfo(4)
	
			else
        			--用户不在群组里面
				getgroupwhennotingroup()
				resetGroupinfo()
				--删除数据库中的所有数据
				tiros.chatrecord.DellAllRecord();
			end
			
			interface.checkmessagestack();
			callbackAllCheck();						
		else
			--重新获取群组信息失败
			gCheckServerState = 3
			resetGroupinfo()
			callbackAllCheck();
			local meetdata = tiros.moduledata.moduledata_get("web","meet_state_ptp");
			if meetdata ~= nil and meetdata ~= "" then
				local meetStatObj = tiros.json.decode(meetdata);
				meetStatObj.together = '2';
				local meetStatStr = tiros.json.encode(meetStatObj);
				tiros.moduledata.moduledata_set("web","meet_state_ptp",meetStatStr);
				commlib.calljavascript("system.callback(519);");		--通知前端web页面
			end
			sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, REGETGROUPINFO_NORMAL_FAIL,"a")
		end
	else
		gCheckServerState = 3
		resetGroupinfo()
		--删除数据库中的所有数据
		tiros.chatrecord.DellAllRecord();
		callbackAllCheck();
		--重新获取群组信息失败
		sendmessagetoApp(EVT_MEET_COMMON_FROM_LUA, REGETGROUPINFO_NORMAL_FAIL,"a")
	end
end 
-----------------------------------------------ccc从服务器获取群组信息-----------------------


--处理10010消息,位置变化
local function ATKEvent_positionchange(msgT)
--[[
{"msgid":"18969963-90","type":10010,"date":"2013年03月28日100100","aid":"213912176","msg":{"lon":419255698,"lat":143727378,"speed":0,"direction":0},"groupid":"C2927"}
--]]
	if tostring(gTogetherTable.atkgpid) ~= tostring(msgT.groupid) then
		return;
	end
	for k,v in pairs(gTogetherTable.togetherlist) do
		
		if tostring(v.aid) == tostring(msgT.aid) then
			v.lon = tostring(msgT.msg.lon);
			v.lat = tostring(msgT.msg.lat);
			if msgT.msg.lon == "0" or msgT.msg.lat == "0" then
				v.lon =""
				v.lat =""
			end
			v.speed = tostring(msgT.msg.speed);
			v.direction = tostring(msgT.msg.direction);
			v.lasttime = resolveposdate(msgT.date);
			
			local tmpStr = tiros.json.encode(gTogetherTable)
			fileobj.Writefile(gFilePath,tmpStr,true);
			tiros.moduledata.moduledata_set("web","togetherdetail_ptp",tmpStr);
			--通知小宣
			local updateObj = {};
			updateObj.groupid = tostring(gTogetherTable.groupid);
			updateObj.atkgpid = tostring(gTogetherTable.atkgpid);
			updateObj.type = "2";
			updateObj.data = v;
			tiros.moduledata.moduledata_set("web","togetherupdate_ptp",tiros.json.encode(updateObj));
			sendmessagetoLogic( 10, 2, "togetherupdate_ptp")
			--通知平台
			sendmessagetoApp(EVT_MEET_POSITION_CHANGE, 0,"a")
			return	
		end
	end	
end

--处理10013消息,用户点击了被踢下线的按钮 
local function ATKEvent_logout()
	--清空本地数据
	gTogetherTable = {}
	fileobj.Removefile(gFilePath);
	--修改同行状态为“未知”状态
	local data = tiros.moduledata.moduledata_get("web","meet_state_ptp");
	if data ~= nil and data ~= "" then
		local meetStatObj = tiros.json.decode(data);
		meetStatObj.together = '0';
		local meetStatStr = tiros.json.encode(meetStatObj);
		tiros.moduledata.moduledata_set("web","meet_state_ptp",meetStatStr);
		commlib.calljavascript("system.callback(519);");		--通知前端web页面
	end
end


--处理10014消息，用户登录成功
local function ATKEvent_login()
	gTogetherList.neterror = false
	--从服务器获取群组信息
	local opt = {};
	local condi = {};
	local lon,lat = GetLonLat();
	condi.uid = tiros.moduledata.moduledata_get("framework", "uid"); 
	condi.lon = lon;
	condi.lat = lat;
	opt.method = "GET";
	opt.data = condi;
	opt.header = {};
	opt.header["actionlocation"] = "/navidog2News/together_initializeGpersonnel.htm";
	opt.header["uid"] = tiros.moduledata.moduledata_get("framework", "uid"); 
	tiros.http.httpabort("checkGroup");
	
	filelib.fmkdir(gApppath..condi.uid)

	print("");
	gFilePath = gApppath..condi.uid.."/together"
	gInviteDataFile = gApppath..condi.uid.."/invite"
	gCheckServerState = 1
	callbackAllCheck();
	local readData = fileobj.Readfile(gFilePath);
	--判断本地群组信息是否存在，如果不存在则从服务器获取	
	if readData ~= '' and readData ~= nil then
		gTogetherTable = tiros.json.decode(readData);
	else
		gTogetherTable = {}
		gTogetherTable.Notjoinedlist = {};
	end
	local sUrlGet = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_GET_URL);
	HttpSend("checkGroup",getGroupFromServerCallback,sUrlGet,opt,"initGpersonnel");

	createChatLogFile();
end
--10015收到网络出错消息,修改内存中的同行状态为“未知”状态
local function ATKEvent_neterror()
	gTogetherList.neterror = true;
	local data = tiros.moduledata.moduledata_get("web","meet_state_ptp");
	if data ~= nil and data ~= "" then
		local meetStatObj = tiros.json.decode(data);
		meetStatObj.together = '0';
		local meetStatStr = tiros.json.encode(meetStatObj);
		tiros.moduledata.moduledata_set("web","meet_state_ptp",meetStatStr);
		commlib.calljavascript("system.callback(519);");		--通知前端web页面
	end
end

--10016 用户重新加入群组回调消息
local function ATKEvent_rejoingroup()
	local data = tiros.moduledata.moduledata_get("web","meet_state_ptp");
	if data ~= nil and data ~= "" then
		local meetStatObj = tiros.json.decode(data);
		if obj.success == true then		--重新进入群组成功
			meetStatObj.together = '1';
		else
			meetStatObj.together = '2';
		end
		tiros.moduledata.moduledata_set("web","meet_state_ptp",tiros.json.encode(meetStatObj));
		commlib.calljavascript("system.callback(519);");		--通知前端web页面
	end
end

--时间地点变更消息
local function ATKEvent_changetimeorplace(obj)	
	local str = tiros.json.encode(obj)
	local modify = 0;
	if obj.lon ~= nil  and obj.lat ~= nil then
		gTogetherTable.address = obj.address;
		gTogetherTable.placelon = tostring(obj.lon);
		gTogetherTable.placelat = tostring(obj.lat);
		gTogetherTable.place = obj.poi;				
		modify = 1
	elseif obj.timeset ~= nil then
		gTogetherTable.time = obj.timeset;
		modify = 0;
	
	elseif obj.contracttime ~= nil and tonumber(obj.contracttime) == 1 then
		gTogetherTable.contracttime = obj.contracttime;
		modify = 2;	
	end
	local tempstr = tiros.json.encode(gTogetherTable)
	fileobj.Writefile(gFilePath,tempstr,true);
	tiros.moduledata.moduledata_set("web","togetherdetail_ptp",tempstr);
		
	--通知小宣
	local updateObj = {};
	updateObj.groupid = tostring(gTogetherTable.groupid);
	updateObj.atkgpid = tostring(gTogetherTable.atkgpid);
	updateObj.type = "3";
	updateObj.data = gTogetherTable;
	tiros.moduledata.moduledata_set("web","togetherupdate_ptp",tiros.json.encode(updateObj));
	sendmessagetoLogic( 10, 2, "togetherupdate_ptp")
	--{"timeset":"2013-03-21 12:00","groupid":452,"type":5}
	sendmessagetoApp( EVT_MEET_MODIFY_TIME_OR_PLACE, modify, str);	
end

local function together_invitetimercallback(htype)
	if htype == "together_invite" then	
		if tmrobj.timerisbusy("together_invite") then
			tmrobj.timerabort("together_invite")
		end
		gTogetherList.bInvitetmrstart = false;
		for k,v in pairs(gInviteStackTable) do
			gRecvInviteCount = gRecvInviteCount+1
			tiros.config.ProfileStart('logic','logiccfg')			
			tiros.config.setValue('logiccfg','groupinvite',gRecvInviteCount)
			tiros.config.ProfileStop('logiccfg')	
			sendmessagetoApp(EVT_MEET_GROUPINVITECOUNT, gRecvInviteCount, "");
			table.insert(gInviteTable, v)
		end

		local strData = tiros.json.encode(gInviteTable);
		tiros.moduledata.moduledata_set("web","together_invite_ptp",strData);
		fileobj.Writefile(gInviteDataFile,strData,true);
		commlib.calljavascript("system.callback(519);");
		
		local strNew = tiros.json.encode(gInviteStackTable);
		sendmessagetoApp(EVT_MEET_INVITE_MSG,  0, strNew)	
		gInviteStackTable = {}
		gTogetherList.bInvitetmrstart = false;
	end
end

local function together_invitetimerstart()		
	if not gTogetherList.bInvitetmrstart then
		gTogetherList.bInvitetmrstart = true
		tmrobj.timerstartforlua("together_invite",1000, together_invitetimercallback)
	end
end

--推送群组邀请
local function ATKEvent_invite(obj)
	if gFilePath ~= "" then
		if gTogetherTable == nil or gTogetherTable.atkgpid  == nil then			
			local dataTab = tiros.file.Readfile(gFilePath);			
			if dataTab ~= nil and dataTab ~= "" then
				gTogetherTable = tiros.json.decode(dataTab);
			end			
		end
	end	
	if gTogetherTable == nil or obj.groupid == nil or obj.pname == nil or obj.atkgpid == nil then		
		return;
	end
	--存储邀请人信息到栈
	local t = {}
	t.uid = tostring(obj.createuid)
	t.name = obj.pname
	t.atkgpid = tostring(obj.atkgpid)
	t.groupid = tostring(obj.groupid)
	t.lon = tostring(obj.lon)
	t.lat = tostring(obj.lat)
	t.poi = obj.poi
	t.timeset = obj.timeset
	t.address = obj.address
	
	for k,v in pairs(gInviteStackTable) do 
		if tostring(v.groupid) == tostring(t.groupid) then
			return;
		end
	end
	for k1,v1 in pairs(gInviteTable) do 
		if tostring(v1.groupid) == tostring(t.groupid) then	
			return;
		end
	end	
	
	gTogetherList.invitegpid = tostring(obj.groupid)
	table.insert(gInviteStackTable, t)	
	together_invitetimerstart()
end

--更换组长通知
local function changeLeader()
	print("jiayufeng------togher----------changeLeader-----------------11111");
	local leaderuid = tiros.moduledata.moduledata_get('framework','meetheaderuid');
	if leaderuid == nil or leaderuid == "" then
		return;	
	end

	print("jiayufeng------togher----------changeLeader-----------------22222: " .. tostring(leaderuid));

	local T_UserInfo = nil;
	local readData = fileobj.Readfile(gFilePath);		
	if readData == '' or readData == nil then
		return ;
	end
	local T_data = tiros.json.decode(readData);
	for k,v in pairs(T_data.togetherlist) do
		local result = tiros.json.encode(v);
		if tostring(v.uid) == leaderuid then
			T_UserInfo = v;
			break;
		end			
	end
	if T_UserInfo == nil then
		return ;
	end
	local nickname = T_UserInfo.nickname; -- 昵称
	local phone = T_UserInfo.phone; -- 电话
	--查询通迅录中的名称
	local addressName = tiros.friendmanger.GetAddrBookName(phone);
	if addressName ~= "" and addressName ~= nil then
		nickname = addressName;
	end
	
	local msg = "[" .. nickname .. "]" .."接管队长职位,大家欢迎";
	tiros.chatrecord.AddRecord("","",4,msg,"",0,gTogetherTable.atkgpid);
end

--state: 0 :退出  1：加入
local function exitAndEnterGroup(state,bookname,nickname,phone)
	local resultName = "";
	
	if bookname ~= nil and bookname ~= "" then
		resultName = bookname;
	else
		if nickname ~= nil and nickname ~= "" then
			resultName = nickname;
		else
			resultName = "手机" .. phone.sub(-1,-4);
		end
	end
	
	if resultName == nil or resultName == "" then
		return;
	end

	--把退出同行组的消息保存到数据库中
	local addMessage = nil;
	if state == 0 then --退出
		addMessage = "[" .. resultName .. "]" .."已退出同行状态";
	else
		addMessage = "[" .. resultName .. "]" .."已加入同行状态";
	end
	
	tiros.chatrecord.AddRecord("","",4,addMessage,"",0,gTogetherTable.atkgpid);

end

--推送群组消息，加入或退出时同步同行组信息
local function ATKEvent_groupsync(obj)

	local re = tiros.json.encode(obj);
	print("jiayufeng---together---ATKEvent_groupsync: " .. tostring(obj.handle) .. "   " .. tostring(obj.uid) .. "    " .. re);

	
	--如果为退出同行组时
	if tonumber(obj.handle) == 2 then
		--遍历数组，删除退出同行的uid数据
		local bChangeHead = false;
		local remove = nil;
		for k,v in pairs(gTogetherTable.togetherlist) do
			if tostring(v.uid) == tostring(obj.uid) then
				print("jiayufeng------------together------ATKEvent_groupsync---2222");				
				remove = k;
				if obj.nheadid ~= nil and string.len(obj.nheadid) >0 and 
							gTogetherTable.headuid ~= tostring(obj.nheadid) then
					--更换队长，保存队长信息
					gTogetherTable.headuid = tostring(obj.nheadid)
					tiros.moduledata.moduledata_set('framework','meetheaderuid', gTogetherTable.headuid)
					bChangeHead = true
				end			
								
			elseif tostring(v.uid) == tostring(obj.nheadid) then
				v.ishead = '1';
			end

		end
		--所最新的群组成员信息保存
		if remove ~= nil then
			table.remove(gTogetherTable.togetherlist, remove);
		end
		local tmpstr = tiros.json.encode(gTogetherTable)
		fileobj.Writefile(gFilePath,tmpstr,true);
		tiros.moduledata.moduledata_set("web","togetherdetail_ptp",tmpstr);
		--通知小宣
		local updateObj = {};
		updateObj.groupid = tostring(gTogetherTable.groupid);
		updateObj.atkgpid = tostring(gTogetherTable.atkgpid);
		updateObj.type = "0";
		updateObj.data = {}
		updateObj.data.aid = tostring(obj.aid);
		local updatastr = tiros.json.encode(updateObj)
		tiros.moduledata.moduledata_set("web","togetherupdate_ptp",updatastr);
		sendmessagetoLogic( 10, 2, "togetherupdate_ptp")
		--把退出人员的信息通知平台
		local delT = {}
		delT.uid = obj.uid 				
		delT.clicktime = obj.clicktime
		delT.phone = tostring(obj.phone)
		delT.nickname = tostring(obj.imgname)
		delT.name = ""
		local addbookname = tiros.friendmanger.GetAddrBookName(obj.phone)
		if addbookname ~= nil then
			delT.name = tostring(addbookname);
		end
		sendmessagetoApp(EVT_MEET_UPDETE_FRIEND, 1, tiros.json.encode(delT));
		
		print("jiayufeng------------together------ATKEvent_groupsync---3333333");
		--把退出同行组的消息保存到数据库中
		--local addMessage = "[" .. obj.imgname .. "]" .."已退出同行状态";
		--tiros.chatrecord.AddRecord("","",4,addMessage,"",0,gTogetherTable.atkgpid);
		--把退出同行组的消息保存到数据库中
		exitAndEnterGroup(0 ,addbookname,delT.nickname,delT.phone);

		if bChangeHead then
			--track，更换队长，需要ask clear，然后打开阻塞
			sendmessagetoLogic(10, 7, "headerchanged");
            --print("logic lua headerchanged send request groupid and headerid:" .. gTogetherTable.groupid .. ", " .. gTogetherTable.headuid)
			LocalGetHeaderTrail(gTogetherTable.groupid, gTogetherTable.headuid);
			updateObj.type = "3";
			updateObj.data = nil;
			tiros.moduledata.moduledata_set("web","togetherupdate_ptp",tiros.json.encode(updateObj));
			sendmessagetoLogic( 10, 2, "togetherupdate_ptp")
			local headT = {}
			headT.headuid = obj.nheadid 				
			headT.clicktime = obj.clicktime
			headT.phone = tostring(obj.phone)
			headT.nickname = tostring(obj.imgname)
			headT.name = ""
			local addbookname = tiros.friendmanger.GetAddrBookName(obj.phone)
			if addbookname ~= nil then
				headT.name = tostring(addbookname);
			end
			
			--把更换组长的消息保存到数据库中
			changeLeader();

			sendmessagetoApp( EVT_MEET_UPDETE_FRIEND, 2, tiros.json.encode(headT));
		end
	elseif tonumber(obj.handle) == 1 then--如果为加入同行组时
--[[
{"uid":8881,"imgpath":"http://cdn2.lbs8.com/files/headpic/headpic824.png","handle":1,"imgname":"Adan","aid":"213912128","type":7}
--]]		

		print("jiayufeng-----------together-------------------------ADD111111111");
		for k,v in pairs(gTogetherTable.togetherlist) do
			
			if tostring(v.uid) == tostring(obj.uid) then
				return;
			end
		end
		
		print("jiayufeng-----------together-------------------------ADD22222222");

		print("jiayufeng-------------------AID---NULL-----------------11111: " .. tostring(obj.aid) .. "   " .. tostring(obj.uid));
		--整理加入人员的信息
		local insertObj = {};
		insertObj.aid = tostring(obj.aid);
		insertObj.uid = tostring(obj.uid);
		
		print("jiayufeng-----------together-------------------------ADD333333333");
		--调用王成的接口，获取头像地址
		insertObj.url = gDefaultHeadImgPath
		local friendObj = tiros.groupbook.GetUserinfo(tostring(obj.uid));
		if friendObj ~= nil and friendObj.HEADPATH ~= nil then
			insertObj.url = friendObj.HEADPATH;			
			if friendObj.HEADURL ~= obj.imgpath then
				pushstackofdownimg(false,obj.uid,obj.aid,obj.imgname,obj.phone,friendObj.HEADPATH,obj.imgpath,"","")
			end
		else
			pushstackofdownimg(true,obj.uid,obj.aid,obj.imgname,obj.phone,"",obj.imgpath,"","")
		end
		
		print("jiayufeng-----------together-------------------------ADD44444444444444");
		--调用王成的接口，获取名称
		insertObj.name = ""
		local addbookname = tiros.friendmanger.GetAddrBookName(obj.phone)
		if addbookname ~= nil then
			insertObj.name = tostring(addbookname);
		end

		insertObj.phone = tostring(obj.phone)
		insertObj.nickname = tostring(obj.imgname)				
		insertObj.ishead = "0" 
		insertObj.lon = "";
		insertObj.lat = "";
		insertObj.lasttime = "";
		insertObj.direction = "";
		insertObj.isVioce = "0";
		insertObj.speed = "";
		
		print("jiayufeng-----------together-------------------------ADD555555555");
		table.insert(gTogetherTable.togetherlist,insertObj);
		if insertObj.name ~= nil and insertObj.name ~= "" then
			SaveGroupUserInfo(insertObj.aid,insertObj.name,insertObj.url);
		else
			SaveGroupUserInfo(insertObj.aid,insertObj.nickname,insertObj.url);
		end
		
		--print("jiayufeng-----------together-------------------------ADD666666666");
		
		--把加入同行组的消息保存到数据库中
		exitAndEnterGroup(1,insertObj.name,insertObj.nickname,insertObj.phone);
		
		--print("jiayufeng-----------together-------------------------ADD77777777777");

		
		--local addM = "[" .. insertObj.nickname .. "]" .."已加入同行状态";
		--tiros.chatrecord.AddRecord("","",4,addM,"",0,gTogetherTable.atkgpid);

		----------------------------------------------
		if gTogetherTable.Notjoinedlist ~= nil then
			for k,v in pairs(gTogetherTable.Notjoinedlist) do
				if tostring(v.PHONE) == tostring(obj.phone) then
					table.remove(gTogetherTable.Notjoinedlist,k);
					break;
				end
			end
		end
		--微信邀请的组通知平台不再绘制催人的条
		gTogetherTable.joined = "1"
		-------------------------------------------		
		--查看是否改变队长
		local bChangeHead = false;
		if obj.nheadid ~= nil and string.len(tostring(obj.nheadid)) >0  then
			gTogetherTable.headuid = tostring(obj.nheadid)
			tiros.moduledata.moduledata_set('framework','meetheaderuid', gTogetherTable.headuid)
			bChangeHead = true
		end	
		local tmpstr = tiros.json.encode(gTogetherTable)
		fileobj.Writefile(gFilePath,tmpstr,true);
		tiros.moduledata.moduledata_set("web","togetherdetail_ptp",tmpstr);
		
		print("jiayufeng-----------together-------------------------ADD888888888888");

		--通知小宣
		local updateObj = {};
		updateObj.groupid = tostring(gTogetherTable.groupid);
		updateObj.atkgpid = tostring(gTogetherTable.atkgpid);
		updateObj.type = "1";
		updateObj.data = insertObj;
		local strLogic = tiros.json.encode(updateObj)
		tiros.moduledata.moduledata_set("web","togetherupdate_ptp",strLogic);
		
		print("jiayufeng-----------together-------------------------ADD9999999999999999");

		sendmessagetoLogic( 10, 2, "togetherupdate_ptp")
		insertObj.clicktime = obj.clicktime;
		sendmessagetoApp( EVT_MEET_UPDETE_FRIEND, 0, tiros.json.encode(insertObj));
		
		print("jiayufeng-----------together-------------------------ADDAAAAAAAAAAAAAAAAAA");

		if bChangeHead then
			--track 更换了队长，需要重新请求轨迹
			sendmessagetoLogic(10, 7, "headerchanged");
            --print("logic lua headerchanged send request groupid and headerid:" .. gTogetherTable.groupid .. ", " .. gTogetherTable.headuid)
			LocalGetHeaderTrail(gTogetherTable.groupid, gTogetherTable.headuid);
			updateObj.type = "3";
			updateObj.data = nil;
			tiros.moduledata.moduledata_set("web","togetherupdate_ptp",tiros.json.encode(updateObj));
			sendmessagetoLogic( 10, 2, "togetherupdate_ptp")
			local headT = {}
			headT.headuid = obj.nheadid 				
			headT.clicktime = obj.clicktime
			headT.phone = tostring(obj.phone)
			headT.nickname = tostring(obj.imgname)
			headT.name = ""
			local addbookname = tiros.friendmanger.GetAddrBookName(obj.phone)
			if addbookname ~= nil then
				headT.name = tostring(addbookname);
			end
			
			--把更换组长的消息保存到数据库中
			changeLeader();
			sendmessagetoApp( EVT_MEET_UPDETE_FRIEND, 2, tiros.json.encode(headT));
		end	
	end
	
end

--同行组解散
local function ATKEvent_groupdismiss()
	resetGroupinfo()
	--删除数据库中的所有数据
	tiros.chatrecord.DellAllRecord();

	local meetstate = tiros.moduledata.moduledata_get("web","meet_state_ptp");
	if meetstate ~= nil and meetstate ~= "" then
		local meetStatObj = tiros.json.decode(meetstate);
		meetStatObj.together = '3';
		local meetStatStr = tiros.json.encode(meetStatObj);
		tiros.moduledata.moduledata_set("web", "meet_state_ptp",meetStatStr);
		commlib.calljavascript("system.callback(519);");		--通知前端web页面
	end

	

	--track  server 已经通知退出，因此不在这里添加清空操作了。
	tiros.moduledata.moduledata_set("web","togetherdetail_ptp","{}");
	sendmessagetoApp(EVT_MEET_DELETE_FRIENDLIST, 0,"a");
end

--发送位置
local function ATKEvent_sendposition(obj)
	if obj.text ~= nil and type(obj.text) == "table" then
		local strtxt = tiros.json.encode(obj.text);	
		local handle = tiros.airtalkeemgr.GetAirtalkeeHandle()
		if obj.aid == nil then
			--print("logic lua ATKEvent_sendposition call MessageSend4 atkgpid is " .. tostring(gTogetherTable.atkgpid))
		 	tiros.airtalkee.MessageSend4(handle, gTogetherTable.atkgpid, strtxt,1, 0)
		else
			--print("logic lua ATKEvent_sendposition call MessageSend3 aid is " .. tostring(obj.aid))
			tiros.airtalkee.MessageSend3(handle, obj.aid, strtxt,1, 0)
		end
	end
end
--发送文本
local function ATKEvent_sendtxt(obj)

	--添加网络判断
	if gTogetherList.neterror == true then 
		sendmessagetoApp(EVT_MEET_AUDIO_RECORD, 4, "");
		return;
	end

	--如果群组内只有一个人，则直接通知平台
	local count = interface.gettotalnumberofgroup();
	
	if count < 2 then	
		if gTogetherTable.Notjoinedlist == nil  then
			sendmessagetoApp(EVT_MEET_AUDIO_RECORD, 5, "");			
		elseif #gTogetherTable.Notjoinedlist == 0 then
			sendmessagetoApp(EVT_MEET_AUDIO_RECORD, 5, "");
		else	
			sendmessagetoApp(EVT_MEET_AUDIO_RECORD, 6, "");
		end
		return;
	end
	

	print("jiayufeng---------------systogether---------sendtxt-----11111");
	if obj.text ~= nil and type(obj.text) == "string" and string.len(obj.text) > 0 then
		print("jiayufeng-------------------systogether---------sendtxt-----2222");			
		local handle = tiros.airtalkeemgr.GetAirtalkeeHandle();
		if obj.aid == nil then
			print("jiayufeng---------systogether---------sendtxt-----3333");
		 	local textid = tiros.airtalkee.MessageSend4(handle, gTogetherTable.atkgpid, obj.text,0, 1)
			print("jiayufeng-----------------systogether---------sendtxt-----444444:  ".. tostring(textid));
			--加入发送对列中
			
			table.insert(gTogetherList.msgsendlist, textid);

			--把文本保存到数据库中
			local currentaid = tiros.moduledata.moduledata_get("framework", "aid");
			tiros.chatrecord.AddRecord(currentaid,tostring(textid),1,obj.text,0,0,gTogetherTable.atkgpid);

			print("jiayufeng--------------------systogether---------sendtxt-----555555");
			
		else
			print("jiayufeng---------------systogether---------sendtxt-----666666");
			local textid = tiros.airtalkee.MessageSend3(handle, obj.aid, obj.text,0, 0);
		end
	end
end

local function writeLog()
	--写日志
--[[
	local currentaid = tiros.moduledata.moduledata_get("framework", "aid");
		writeChatLog("消息: ".. obj.msgid.."	发送成功 " .." 时间：".. tostring(GetDate()) .. "  aid: ".. tostring(currentaid).."  group:" 
				.. tostring(gTogetherTable.groupid) .. "\r\n\r\n\r\n");

--]]
end
--发送结果
local function ATKEvent_sendresult(obj)	
	print("Send--------------------------22222   ".. tostring(obj.msgid)  .. "  state: " .. tostring(obj.success));
	for k,v in pairs(gTogetherList.msgsendlist) do
		print("Send--------------------------333333");
		if tostring(v) == tostring(obj.msgid) then

			print("Send--------------------------444444");
			--移除发送列表中的值
			table.remove(gTogetherList.msgsendlist,k)

			if obj.success == true then	
				print("Send--------------------------555555" .. tostring(obj.msgid));
				--更新数据库(发送状态)	
				tiros.chatrecord.SetMsgSendState(obj.msgid,true);
				if obj.msgtype == 2 then --语音消息
					--更新数据库中msgid所对应的resid
					tiros.chatrecord.EditVoiceResId(obj.msgid,obj.resid);
				end
			else
				--更新数据库(发送状态)	
				tiros.chatrecord.SetMsgSendState(obj.msgid,false);
			end
			break;
		end	
	end
end

local ATKEvent_audioStatusFun = nil;
--airtalkee调用媒体播放器的回调函数
local function airtalkee_mediaplayernotify(ptype, event, param1, param2)
	print("xuandy--airtalkee_mediaplayernotify --",ptype,event,param1,param2);

	local tType = tiros.json.decode(gMsgIDResID);
	local tState = {};
	tState.type = 10019;
	tState.msgid = tType.msgid;
	tState.resid = tType.resid;

print("airtalkee_mediaplayernotify -tType.msgid=",tType.msgid);
print("airtalkee_mediaplayernotify -tState.resid=",tState.resid);
	if event == SYS_MEDIA_PLAYER_EVENT_BEGIN then
		tState.state = 1;
	elseif event == SYS_MEDIA_PLAYER_EVENT_END then
		tState.state = 2;
	end
	ATKEvent_audioStatusFun(tState);
end

--爱滔客语音文件存储路径
local function getAirtalkee_amrfile_path()
		local sPath = "fs1:/Records/"; 
		if filelib.fdiskexist("fs1:/") == false then
			filelib.fmkdir("fs0:/Records/");
			sPath = "fs0:/Records/"
		else
			filelib.fmkdir("fs1:/Records/");
			sPath = "fs1:/Records/"
		end
		return sPath;
end

--检查队列中还有没有语音要播放？
local function CheckVoiceQueue()

	if gTogetherList.voicequeue ~= nil and gTogetherList.voicequeue[1] ~= nil  then

		local voice = gTogetherList.voicequeue[1];

		if tostring(gTogetherTable.atkgpid) == tostring(voice.groupid) then
			
			local sPath = getAirtalkee_amrfile_path()
			local sFileName = sPath..voice.resid..".amr";
			print("nativeplay-airtalkee-play-sFileName = "..sFileName)
			local bExist = filelib.fexist(sFileName);
			if (bExist) then
				gTogetherList.isplay = true;
				gTogetherList.playaid = voice.aid;
				local tType = {}
				tType.msgid = voice.msgid;
				tType.resid = voice.resid;
				gMsgIDResID = tiros.json.encode(tType)
				print("nativeplay-airtalkee-play----file is exist PlayLocalFile--start")
				tiros.mediaplayer.PlayLocalFile(gsTypeMediaPlayerForAirtalk, airtalkee_mediaplayernotify, sFileName);
				print("nativeplay-airtalkee-play----file is exist PlayLocalFile--end")
			else
				gTogetherList.isplay = false
				print("nativeplay-airtalkee-play----file is not exist MessageRecordPlayDownload--start")
				tiros.airtalkee.MessageRecordPlayDownload(tiros.airtalkeemgr.GetAirtalkeeHandle(), voice.msgid, voice.resid);
				print("nativeplay-airtalkee-play----file is not exist MessageRecordPlayDownload--end")
			end

			--tiros.airtalkee.MessageRecordPlayStart(tiros.airtalkeemgr.GetAirtalkeeHandle(), voice.msgid, voice.resid)
		else
			gTogetherList.isplay = false
			gTogetherList.playaid = nil
		end		
	end 
end

--语音加到队列中
local function pushPlayvoiceQueue(t)
	if gTogetherList.voicequeue == nil then
		gTogetherList.voicequeue = {}
	end
	table.insert(gTogetherList.voicequeue, t)
	if gTogetherList.isplay ~= true and gTogetherList.isrecord ~= true then
		CheckVoiceQueue();		
	end
end



--用户点击了要播放的语音，
local function fromDbVoiceToQueue(T_voice)
	print("fromDbVoiceToQueue----------------------------1111");

	if gTogetherList.voicequeue ~= nil and gTogetherList.voicequeue[1] ~= nil  then
		local voice = gTogetherList.voicequeue[1];
		local tType = {}
		tType.msgid = voice.msgid;
		tType.resid = voice.resid;
		local sType = tiros.json.encode(tType)
		tiros.mediaplayer.Stop(gsTypeMediaPlayerForAirtalk)
		
		--设置播放状态
		local tState = {};
		tState.type = 10019;
		tState.msgid = voice.msgid;
		tState.resid = voice.resid;
		tState.state = 2;
		ATKEvent_audioStatusFun(tState);
	end

	gTogetherList.isplay = false;
	gTogetherList.playaid = nil;

	print("fromDbVoiceToQueue----------------------------222222");
	
	--移除所有队列中的语音
	gTogetherList.voicequeue = {};

	print("fromDbVoiceToQueue----------------------------3333");

	--保存语音
	gTogetherList.voicequeue = T_voice;

	print("fromDbVoiceToQueue----------------------------44444");
	--检查队列播放
	CheckVoiceQueue();

	print("fromDbVoiceToQueue----------------------------555555");

end

--停止播放语音
local function StopPlay()

	--停止播放当前声音
	if gTogetherList.isplay == true then
		if gTogetherList.voicequeue ~= nil and gTogetherList.voicequeue[1] ~= nil  then
				local voice = gTogetherList.voicequeue[1];
				local tType = {}
				tType.msgid = voice.msgid;
				tType.resid = voice.resid;
				local sType = tiros.json.encode(tType)
				tiros.mediaplayer.Stop(gsTypeMediaPlayerForAirtalk)

				--设置播放状态
				local tState = {};
				tState.type = 10019;
				tState.msgid = voice.msgid;
				tState.resid = voice.resid;
				tState.state = 2;
				ATKEvent_audioStatusFun(tState);
		end
	end
	--tiros.airtalkee.MessageRecordPlayStop(tiros.airtalkeemgr.GetAirtalkeeHandle());
	
	gTogetherList.isplay = false;
	gTogetherList.playaid = nil;
	--把播放队列清空
	gTogetherList.voicequeue = nil;
end

--重发消息
local function Message_ReSend(sendtype,id,msgid,resid,length,content)

	print("jiayufeng------gether--Message_ReSend-----1111 resid:" ..tostring(resid) .. "  msgid:" .. tostring(msgid));

	local groupid = gTogetherTable.atkgpid;
	local handle = tiros.airtalkeemgr.GetAirtalkeeHandle();

	if sendtype == 1 then --文本

		print("jiayufeng------gether---------Message_ReSend-----2222222 :");
		--产生新的msgid
		local newmsgid = tiros.airtalkee.MessageSend4(handle, groupid, content,0, 1);
		print("jiayufeng------gether---------Message_ReSend-----33333333 :" .. tostring(newmsgid) .. "   msgid:" .. tostring(msgid));
		if newmsgid == nil or newmsgid == "" then
			return;		
		end
		--插入发送列表
		table.insert(gTogetherList.msgsendlist, newmsgid);
		print("jiayufeng------gether---------Message_ReSend-----444444 :");
		--更新数据库中的msgid
		tiros.chatrecord.EditMsgid(id,newmsgid);

		print("jiayufeng------gether---------Message_ReSend-----555555 :");

	elseif sendtype ==2 then -- 语音
		print("jiayufeng------gether-----Message_ReSend---6666666666 :".. tostring(msgid) .. "  resid:" .. tostring(resid));
		
		local newmsgid = tiros.airtalkee.MessageRecordResend3(handle,groupid,msgid,resid,length,1);
		if newmsgid == nil or newmsgid == "" then
			return;		
		end
		--插入发送列表
		table.insert(gTogetherList.msgsendlist, newmsgid);
		--修改数据库(用新的msgid 替换 旧的msgid)
		tiros.chatrecord.EditMsgid(id,newmsgid);

		print("jiayufeng------gether---------Message_ReSend-----77777777 :");
	end
end

--[[协议
.type:MMID_MSG_AUDIO	-->协议编码
.groupid:xxx	-->群组 id
.aid:xxx		-->发送方 aid
.msgid:xxx		-->接收消息的唯一编码
.date:20130306095300	-->消息接收时间:年月日时分秒
.msg:xxx		-->消息体内容(语音类型消息体协议为:	.resid:xxx	-->语音资源唯一编码
													.time:xxx	-->语音消息时长)
150-收到群组内语音消息,第二个参数 0,第三个参数:
{"uid":"","aid":"","msgtype":"2","state":"1","recvtime":"","sendtime":"","msgid":"","resid":"","dbid":"
","speechlen":""},
]]--
local function ATKEvent_recvVoice(obj)

	print("jiayufeng----------LUA------totether-----接受到语音");

	local tmp = {};
	tmp.aid = tostring(obj.aid);
	tmp.msgtype = "2";
	tmp.state = "";
	tmp.recvtime = tostring(obj.date);
	tmp.sendtime = "";
	tmp.msgid = tostring(obj.msgid);
	tmp.resid = tostring(obj.msg.resid);
	tmp.dbid = "";
	tmp.speechlen = tostring(obj.msg.time);
	tmp.groupid = tostring(obj.groupid);

	--把语音数据插入到数据库中
	tiros.chatrecord.AddRecord(tmp.aid,tmp.msgid,2,tmp.resid,tmp.recvtime,tmp.speechlen,tmp.groupid);
	--存储语音到播放队列
	pushPlayvoiceQueue(tmp);
end

--[[协议
.type:MMID_MSG_TEXT	-->协议编码
.groupid:xxx		-->群组 id
.aid:xxx			-->发送方 aid
.msgid:xxx			-->接收消息的唯一编码
.date:20130306095300-->消息接收时间:年月日时分秒
.msg:xxx			-->消息体内容(文本类型则直接为实际内容)

151-收到群组内文本消息,第二个参数 0,第三个参数:
{"uid":"","aid":"","msgtype":"1","state":"1","recvtime":"","sendtime":"","msgid":"","content":"","dbid
":""}
]]--
local function ATKEvent_recvTxt(obj)

	print("jiayufeng---------------together--------收到文本:" .. obj.msg);
	local tmp = {};
	tmp.aid = tostring(obj.aid);
	tmp.msgtype = "1";
	tmp.state = "1";
	tmp.recvtime = tostring(obj.date);
	tmp.sendtime = "";
	tmp.msgid = tostring(obj.msgid);
	tmp.content = tostring(obj.msg);
	tmp.dbid = "";
	sendmessagetoApp(EVT_MEET_GROUP_TEXT, 0, tiros.json.encode(tmp));
	--把这个文本信息保存到数据库中
	tiros.chatrecord.AddRecord(tmp.aid,tmp.msgid,1,tmp.content,tmp.recvtime,0,obj.groupid);
end

--[[
.type:MMID_MSG_AUDIOPLAY-->协议编码
.state:0/1/2	-->加载中/开始播放/停止播放
.msgid:xxx		-->消息唯一编码
.resid:xxx		-->资源唯一编码

152-通知语音播放状态,第二个参数 0/1/2 表示加载中/开始播放/停止播放,第三个参数:aid
]]--
local function ATKEvent_audioStatus(obj)

	local mes = tiros.json.encode(obj);

	print("ATKEvent_audioStatus---------111   " .. tostring(mes));

	print("ATKEvent_audioStatus---------2222   " .. tostring(obj.state) .. "    " .. tostring(obj.msgid));

	--把播放状态通知平台，并且更新数据库
	tiros.chatrecord.setPlayVoiceState(obj.state,gTogetherList.playaid,obj.msgid);

	if obj.state == 1 then

		for k,v in pairs(gTogetherTable.togetherlist) do 
			if v.aid == tostring(gTogetherList.playaid) then
				v.isVioce = "1"
				local tempstr = tiros.json.encode(gTogetherTable)
				fileobj.Writefile(gFilePath,tempstr,true);
				tiros.moduledata.moduledata_set("web","togetherdetail_ptp",tempstr);
			
				--通知小宣
				local updateObj = {};
				updateObj.groupid = tostring(gTogetherTable.groupid);
				updateObj.atkgpid = tostring(gTogetherTable.atkgpid);
				updateObj.type = "2";
				updateObj.data = v;
				tiros.moduledata.moduledata_set("web","togetherupdate_ptp",tiros.json.encode(updateObj));
				sendmessagetoLogic( 10, 2, "togetherupdate_ptp")	
				return;		
			end
		end		

	elseif obj.state == 2 then

		--播放结束，检查队列
		table.remove(gTogetherList.voicequeue,1);
		for k,v in pairs(gTogetherTable.togetherlist) do 
			if v.aid == tostring(gTogetherList.playaid) then
				v.isVioce = "0"
				local tempstr = tiros.json.encode(gTogetherTable)
				fileobj.Writefile(gFilePath,tempstr,true);
				tiros.moduledata.moduledata_set("web","togetherdetail_ptp",tempstr);
				
				--通知小宣
				local updateObj = {};
				updateObj.groupid = tostring(gTogetherTable.groupid);
				updateObj.atkgpid = tostring(gTogetherTable.atkgpid);
				updateObj.type = "2";
				updateObj.data = v;
				tiros.moduledata.moduledata_set("web","togetherupdate_ptp",tiros.json.encode(updateObj));
				sendmessagetoLogic( 10, 2, "togetherupdate_ptp")
				break;
			end
		end	
		gTogetherList.isplay = false
		CheckVoiceQueue()
	end
end

ATKEvent_audioStatusFun = ATKEvent_audioStatus;

--[[录音相关事件回调标识:MMID_MSG_AUDIORECORD = 10018;
.type:MMID_MSG_AUDIORECORD	-->协议编码
.state:0/1/2	-->开始录音/完成或停止录音/录音消息传输完成
.msg:xxx	-->回调数据:	开始:nil/
		完成或停止:	
.msgid:xxx	-->消息唯一编码
.time:-2/-3/>0 -->未满足最小秒数/取消/正常时长
	传输完成:	
.msgid:xxx -->消息唯一编码
resid:xxx -->资源唯一编码

153-通知语音录制结果,
第二个参数					第三个参数:
1-录制时长未满足最小秒数,			NULL
3-录制完成,					{“dbid”:””,“msgid”:””,”time”:”语音时长”}
2-录制取消					NULL
3 已经录制时间				字符串 ms 数“1000”
4-网络错误						NULL
5-组内成员只有自己，并且未邀请其他人				NULL
6-组内成员只有自己，有未加入的邀请人

录制完成后自动发送
154-通知语音发送结果,第二个参数 0 失败 1 成功,第三个参数:{“dbid”:””,“resid”:””,”msgid”:””}
]]--
local function ATKEvent_audioRecord(obj)

	local json11 = tiros.json.encode(obj);

	print("jiayufeng-----together-----------ATKEvent_requestRecord ------AAAAAAA:" .. tostring(json11));	
	local count = interface.gettotalnumberofgroup();
	
	if count < 2 then	
		if gTogetherTable.Notjoinedlist == nil  then
			sendmessagetoApp(EVT_MEET_AUDIO_RECORD, 5, "");			
		elseif #gTogetherTable.Notjoinedlist == 0 then
			sendmessagetoApp(EVT_MEET_AUDIO_RECORD, 5, "");
		else	
			sendmessagetoApp(EVT_MEET_AUDIO_RECORD, 6, "");
		end
		return;
	end

	print("jiayufeng-----together-----------ATKEvent_requestRecord ------AAAAAAA "  .. tostring(obj.state));

	if obj.state == 0 then
		sendmessagetoApp(EVT_MEET_AUDIO_RECORD, 0, "");--开始录音
		gTogetherList.isrecord = true;
		gTogetherList.isplay  = false	
		
		StopPlay()
	elseif obj.state == 1 then
		gTogetherList.isrecord = false
		CheckVoiceQueue()
		if obj.msg.time == -2 then --录制时长未满足最小秒数
			sendmessagetoApp(EVT_MEET_AUDIO_RECORD, 1, "");
		elseif obj.msg.time == -3 then --录制取消
			sendmessagetoApp(EVT_MEET_AUDIO_RECORD, 2, "");
		else

			print("jiayufeng-----together-----------ATKEvent_requestRecord ------BBBBBBBB :");
			local tmp = {};
			tmp.dbid = "";
			tmp.msgid = tostring(obj.msg.msgid);
			tmp.time = tostring(obj.msg.time);
			table.insert(gTogetherList.msgsendlist, obj.msg.msgid)
			sendmessagetoApp(EVT_MEET_AUDIO_RECORD, 3, tiros.json.encode(tmp));--录制完成

			--把这个发送语音保存到数据库中
			local currentaid = tiros.moduledata.moduledata_get("framework", "aid");
			tiros.chatrecord.AddRecord(currentaid,tmp.msgid,2,tmp.msgid,0,tmp.time,gTogetherTable.atkgpid);

			print("jiayufeng-----together-----------ATKEvent_requestRecord ------CCCCCCCCC :" .. tostring(tmp.msgid));

		end
	elseif obj.state == 2 then --录音消息传输完成

		print("jiayufeng-----together-----------ATKEvent_requestRecord ------DDDDDDDDDDDD :");
		local tmp = {};
		tmp.dbid = "";
		tmp.resid = tostring(obj.msg.resid);
		print("Play---------------------22222222222222222222---2222");
		tmp.msgid = tostring(obj.msg.msgid);
		print("jiayufeng-----together-----------ATKEvent_requestRecord ------EEEEEEEEEEEEEE :" .. tostring(tmp.msgid) .. "  " .. tostring(tmp.resid));
		--修改msgid的resid
		tiros.chatrecord.EditVoiceResId(tmp.msgid,tmp.resid);
		
		if string.len(tmp.resid) == 0 or string.len(tmp.msgid) == 0 then
			sendmessagetoApp(EVT_MEET_CHATMESSAGESEND_RESULT, 0, "{}");
		else
			--sendmessagetoApp(EVT_MEET_CHATMESSAGESEND_RESULT, 2, obj.msg.msgid);
		end
	end
end

--[[
请求录音
--]]
local function ATKEvent_requestRecord(obj)
	print("jiayufeng-----together-----------ATKEvent_requestRecord ------1111");

	local grpid = gTogetherTable.atkgpid;	

	if grpid == -1 or gTogetherList.neterror == true then --添加网络判断
		sendmessagetoApp(EVT_MEET_AUDIO_RECORD, 4, "");
		return;
	end

	print("jiayufeng-----together-----------ATKEvent_requestRecord ------222222");
	gTogetherList.isrecord = false;
	if obj.state == 0 then--开始
		print("jiayufeng-----together-----------ATKEvent_requestRecord ------3333333");
		if obj.aid ~= nil then
			print("jiayufeng-----together-----------ATKEvent_requestRecord ------6666666666");
			tiros.airtalkee.MessageRecordStart3(tiros.airtalkeemgr.GetAirtalkeeHandle(), obj.aid, 1)
			print("jiayufeng-----together-----------ATKEvent_requestRecord ------7777777");
		else
			print("jiayufeng-----together-----------ATKEvent_requestRecord ------888888");
			tiros.airtalkee.MessageRecordStart4(tiros.airtalkeemgr.GetAirtalkeeHandle(), grpid, 1)
			print("jiayufeng-----together-----------ATKEvent_requestRecord ------9999999999");
		end
	elseif obj.state == 1 then--停止
		print("jiayufeng-----together-----------ATKEvent_requestRecord ------444444");
		tiros.airtalkee.MessageRecordStop(tiros.airtalkeemgr.GetAirtalkeeHandle(), obj.cancel)
	elseif obj.state == 2 then--重发

		print("jiayufeng-----together-----------ATKEvent_requestRecord ------5555555555");
		table.insert(gTogetherList.msgsendlist, obj.msgid)
		if obj.aid ~= nil then
			tiros.airtalkee.MessageRecordResend2(tiros.airtalkeemgr.GetAirtalkeeHandle(), obj.aid, obj.msgid,obj.resid,obj.time, 1)
		else
			tiros.airtalkee.MessageRecordResend3(tiros.airtalkeemgr.GetAirtalkeeHandle(), grpid, obj.msgid,obj.resid,obj.time, 1)
		end
		
	end
end

--查询个人信息
local function getUserInfo(aid)

	print("jiayufeng------------together-----getUserInfo---1111: ");
	local T_UserInfo = nil;
	local readData = fileobj.Readfile(gFilePath);		
	if readData == '' or readData == nil then
		return "","";
	end
	local T_data = tiros.json.decode(readData);
	for k,v in pairs(T_data.togetherlist) do
		local result = tiros.json.encode(v);
		print("jiayufeng------------together-----getUserInfo--2222-: " .. tostring(result));
		if tostring(v.aid) == aid then
			T_UserInfo = v;
			break;
		end			
	end

	if T_UserInfo == nil then
		return "","";
	end
	local nickname = T_UserInfo.nickname; -- 昵称
	local phone = T_UserInfo.phone; -- 电话
	local url = T_UserInfo.url; -- 头像
	if phone == nil or phone == "" then
		return nickname,url;
	end
	--查询通迅录中的名称
	local addressName = tiros.friendmanger.GetAddrBookName(phone);
	if addressName ~= "" and addressName ~= nil then
		nickname = addressName;
	end
	return nickname,url;
end


--[[
.type:MMID_GROUP_MemberList	-->协议编码
.success:true/false;		-->成功/失败
.msg = {xxx,xxx}/nil;		-->成员数据/nil
]]--

createmodule(interface, "gettotalnumberofgroup",function()
	if gTogetherTable.togetherlist == nil then
		return 0
	else
		local number = #gTogetherTable.togetherlist
		return number;
	end	 
end)

createmodule(interface, "checkmessagestack",function()
	
	if gTogetherList.isMessageResolve == nil  and gTogetherList.neterror ~= true then
			
		interface.together_messagetimerstart()
	end
end)

--timer回调 处理消息
createmodule(interface,"together_timerCB",function(handletype)
	if gTogetherList.messagelist ~= nil and gTogetherList.messagelist[1] ~= nil then
		local t = gTogetherList.messagelist[1]			
		local str = tiros.json.encode(t);
		interface.WebCommonUpdateData(str,true)
		table.remove(gTogetherList.messagelist, 1 )
		interface.together_messagetimerstart()	
	else
		gTogetherList.isMessageResolve = nil
	end	
end)

--启动消息处理定时器
createmodule(interface, "together_messagetimerstart", function()
			
	if tmrobj.timerisbusy("togethertmr") then
		tmrobj.timerabort("togethertmr")
	end
	gTogetherList.isMessageResolve = true;
	tmrobj.timerstartforlua("togethertmr",1000,interface.together_timerCB)
end)

local function pushmessagestack(obj)
	if gTogetherList.messagelist == nil then
		gTogetherList.messagelist = {}
	end
	table.insert(gTogetherList.messagelist,obj)
	interface.checkmessagestack()
end

createmodule(interface,"UpdataHeadPath", function(fuid,name,path)
	if gTogetherTable == nil or gTogetherTable.togetherlist == nil or name == nil or path == nil  then		
		return 
	else			
		for k,v in pairs(gTogetherTable.togetherlist) do
			if tostring(fuid) == tostring(v.uid) then
				v.url = path;
				v.nickname = name;
				local json = tiros.json.encode(gTogetherTable);
		
				fileobj.Writefile(gFilePath,json,true);
				--更新数据仓库
				tiros.moduledata.moduledata_set("web","togetherdetail_ptp",json);
				return;
			end 
		end		
	end	 
end)

createmodule(interface,"GroupGetConfig", function()
	tiros.config.ProfileStart('logic','logiccfg')
	local ret = tiros.config.getValue('logiccfg','groupenter')
	gRecvInviteCount = tiros.config.getValue('logiccfg','groupinvite')
	if gRecvInviteCount == nil then
		gRecvInviteCount = 0
	end
	tiros.config.ProfileStop('logiccfg')
	return ret;
end)

createmodule(interface,"FirstEnterGroup", function()
	gRecvInviteCount = 0;
	tiros.config.ProfileStart('logic','logiccfg')
	tiros.config.setValue('logiccfg','groupenter',true)
	tiros.config.setValue('logiccfg','groupinvite',gRecvInviteCount)
	tiros.config.ProfileStop('logiccfg')	
	sendmessagetoApp(EVT_MEET_GROUPINVITECOUNT, 0, "");
end)


--获取邀请个数
--返回值：-1，显示new，0，不显示，>0，显示个数
createmodule(interface,"GetGroupInviteCount", function()
	local bEnter = interface.GroupGetConfig()
	
	if gRecvInviteCount > 0 then
		if not bEnter then
			tiros.config.ProfileStart('logic','logiccfg')
			tiros.config.setValue('logiccfg','groupenter',true)			
			tiros.config.ProfileStop('logiccfg')
		end
		return gRecvInviteCount;
	elseif bEnter then
		return 0;
	else
		return -1;
	end
	
end)


--覆盖安装以后，如果邀请个数和未读消息数都为0，则显示NEW
--返回值：无
createmodule(interface,"ShowRedIconAfterCoverInstallation", function()
	print("ShowRed----1")
	local bEnter = interface.GroupGetConfig()
	print("ShowRed----2")
	local nInviteCount = interface.GetGroupInviteCount();
	print("ShowRed----3")
	local nMsgCount = tiros.chatrecord.GetNotShowMsgCount();
	print("ShowRed----4")
	if nInviteCount == 0 and nMsgCount == 0 then
	print("ShowRed----5")
		if bEnter then
			print("ShowRed----6")
			tiros.config.ProfileStart('logic','logiccfg')
			tiros.config.setValue('logiccfg','groupenter',false)			
			tiros.config.ProfileStop('logiccfg')
			print("ShowRed----7")
		end
	end
end)

--------------------------
--消息通用接口
createmodule(interface,"WebCommonUpdateData", function(data, bresolve)	
	print("together------WebCommonUpdateData---"..data)
	local obj = tiros.json.decode(data);
	
	if not bresolve then
		if gCheckServerState ~= 2 and tonumber(obj.type) ~= 10014 then
			--未登录成功时保存消息到队列
			pushmessagestack(obj)
			return;
			
		elseif tonumber(obj.type) == 10010 or tonumber(obj.type) == 10009 then
			pushmessagestack(obj)
			return
		end
	end
	if tonumber(obj.type) == 10005 then
		--track, if the user is the header
		local selfuid = tiros.moduledata.moduledata_get('framework', 'uid');
		if selfuid ~= nil and gTogetherTable.headuid ~= nil and  gTogetherTable.headuid == selfuid then
			--print("logic lua 10005 group id headuid : " .. gTogetherTable.groupid .. ", " .. gTogetherTable.headuid)
			local headerinfo = {};
			--print("logic lua selfuid is " .. selfuid)
			headerinfo.lon =  obj.text.x;
			headerinfo.lat = obj.text.y;
			local headerStr = tiros.json.encode(headerinfo);
			tiros.moduledata.moduledata_set("web","headerlonlat_ptp", headerStr);
			sendmessagetoLogic(10, 7, "headerreportselftrack");
		end
		--请求发送位置信息
		ATKEvent_sendposition(obj)

	elseif tonumber(obj.type) == 10006 then	
		--请求发送文本信息
		ATKEvent_sendtxt(obj)

	elseif tonumber(obj.type) == 10007 then	
		--消息发送状态回调消息标识

		print("Send--------------------------111111");
		gTogetherList.neterror = false
		ATKEvent_sendresult(obj)

	elseif tonumber(obj.type) == 10008 then	
		--收到群组内语音消息标识
		gTogetherList.neterror = false
		ATKEvent_recvVoice(obj)

	elseif tonumber(obj.type) == 10009 then	
		--收到群组内文本消息标识
		gTogetherList.neterror = false
		ATKEvent_recvTxt(obj)

	elseif tonumber(obj.type) == 10010 then	
		--位置lon，lat变化,更新同行组中对应的好友信息
		gTogetherList.neterror = false
		ATKEvent_positionchange(obj)
	
	elseif tonumber(obj.type) == 10012 then	
		--当前说话的音频强弱
		gTogetherList.neterror = false
		sendmessagetoApp(EVT_MEET_VOICE_STRENGTH, 0, tostring(obj.msg.volume))

	elseif tonumber(obj.type) == 10013 then
		--用户点击了被踢下线的按钮 
		gTogetherList.neterror = false
		ATKEvent_logout()
		tiros.moduledata.moduledata_set("framework", "meet3currentgroupid" , "0");
		tiros.airtalkeemgr.StopUploadPositionInfo(4)

	elseif tonumber(obj.type) == 10014 then
		gTogetherList.neterror = false
		--uid_aid重新登录完成消息，
		--'{"groupid":222,"placelon":"ss","placelat":"dd","place":"dd","time":"ee","contracttime":"tt","headuid":"33","togetherlist":
		--{"aid":"dd","uid":"","url":"","nickname":"","lon":"","lat":"","lasttime":"","direction":"","isVioce":"","speed":""}}'
		ATKEvent_login()

	elseif tonumber(obj.type) == 10015 then
		--收到网络出错消息,修改内存中的同行状态为“未知”状态
		ATKEvent_neterror()
		tiros.moduledata.moduledata_set("framework", "meet3currentgroupid" , "0");
		tiros.airtalkeemgr.StopUploadPositionInfo(4)

	elseif tonumber(obj.type) == 10016 then
		gTogetherList.neterror = false
		--用户重新加入群组回调消息
		ATKEvent_rejoingroup()

	elseif tonumber(obj.type) == 10017 then
		--从爱滔客获取群组成员信息成功回调消息

	elseif tonumber(obj.type) == 10018 then
		--录音相关事件回调
		gTogetherList.neterror = false
		ATKEvent_audioRecord(obj)

	elseif tonumber(obj.type) == 10003 then
		--请求录音
		ATKEvent_requestRecord(obj)

	elseif tonumber(obj.type) == 10019 then
		print("Play-----------------播放状态----------------------");
		--通知语音播放状态
		gTogetherList.neterror = false
		ATKEvent_audioStatus(obj)

	elseif tonumber(obj.type) == 4 and tonumber(obj.st) == 1 then
		--到时提醒功能（消息推送）
		gTogetherList.neterror = false
		local t = {}
		t.groupid = gTogetherTable.groupid
		t.contracttime = 1
		local json = tiros.json.decode(t);
		interface.WebEditGathertime(json)
		
	elseif tonumber(obj.type) == 5 then
		--修改时间、地点及续约（消息推送）
		--0：修改地点成功，1：修改时间成功，2：续时成功
		gTogetherList.neterror = false
		ATKEvent_changetimeorplace(obj)

	elseif tonumber(obj.type) == 6 then	
		--推送邀请消息（消息推送）
		gTogetherList.neterror = false	
		ATKEvent_invite(obj)

	elseif tonumber(obj.type) == 7 then
		--加入/退出时同步同行好友及变更组长（消息推送）
		--0：加入通知，1：退出通知，2:变更组长通知
		gTogetherList.neterror = false
		ATKEvent_groupsync(obj)

	elseif tonumber(obj.type) == 8 and tonumber(obj.js) == 1 then
		--解散结伴同行（消息推送）
		gTogetherList.neterror = false
		ATKEvent_groupdismiss()
		tiros.moduledata.moduledata_set("framework", "meet3currentgroupid" , "0");
		tiros.airtalkeemgr.StopUploadPositionInfo(4)
	elseif tonumber(obj.type) == 10023 then
		--音频文件下载完成
		local bSuccess = obj.success;
		print("nativeplay-------10023---------1")
		if bSuccess then
			print("nativeplay-------10023---------2")
			local sResid = obj.resid;
			print("nativeplay-------10023---------3")
			if(sResid ~= nil) then
			print("nativeplay-------10023---------4")
				if gTogetherList.isplay == true then
					print("nativeplay-------10023---------4-gTogetherList.isplay == true")
				else
					print("nativeplay-------10023---------4-gTogetherList.isplay == false")
				end
				if gTogetherList.isrecord == true then
					print("nativeplay-------10023---------4-gTogetherList.isrecord == true")
				else
					print("nativeplay-------10023---------4-gTogetherList.isrecord == false")
				end
				if gTogetherList.isplay ~= true and gTogetherList.isrecord ~= true then
					print("nativeplay-------10023---------5")
					CheckVoiceQueue()
					print("nativeplay-------10023---------6")
				end
			end
		else
			print("nativeplay--together.lua-10023--obj.success = false")
		end

	elseif tonumber(obj.type) == 10020 then --离线消息
		local T_result = {};
		local T_res = {};
		for k,v in pairs(obj.msg) do
			local msgType = v.type;
			if tonumber(msgType) == 10009 or tonumber(msgType) == 10008 then --离线的语音与文字信息
				table.insert(T_res,v);
			else
				interface.WebCommonUpdateData(tiros.json.encode(v));
			end
			
		end

		--把离线的文本与语音保存数据库
		T_result.msg = T_res;
		tiros.chatrecord.AddOfflineRecord(T_result);
	end
end);


createmodule(interface,"together_updateself", function(t)
	if gTogetherTable.togetherlist == nil or type(gTogetherTable.togetherlist) ~= "table" then
		return false
	end
	for k,v in pairs(gTogetherTable.togetherlist) do 
		if tostring(v.uid) == tostring(t.UID) then
			v.nickname = tostring(t.NICKNAME)
			v.url = tostring(t.HEADPATH)
			local tempstr = tiros.json.encode(gTogetherTable)
			fileobj.Writefile(gFilePath,tempstr,true);
			tiros.moduledata.moduledata_set("web","togetherdetail_ptp",tempstr);
		
			--通知小宣
			local updateObj = {};
			updateObj.groupid = tostring(gTogetherTable.groupid);
			updateObj.atkgpid = tostring(gTogetherTable.atkgpid);
			updateObj.type = "2";
			updateObj.data = v;
			local updatestr = tiros.json.encode(updateObj)
			tiros.moduledata.moduledata_set("web","togetherupdate_ptp",updatestr);
			sendmessagetoLogic( 10, 2, "togetherupdate_ptp")
			break;			
		end
	end	
	return true
end)

createmodule(interface,"together_getlastinvitegpid", function()
	return gTogetherList.invitegpid	
end)

createmodule(interface,"getcurrentgroupid", function()
	return gTogetherTable.atkgpid;
end)

createmodule(interface,"together_isingroup", function()
	if gTogetherTable.atkgpid then
		return true
	end
	return false
end)

createmodule(interface,"together_groupid", function()
	if gTogetherTable.groupid then
		return gTogetherTable.groupid	
	end
	return -1
end)

createmodule(interface,"together_isjoined", function()
	if gTogetherTable.joined == "1"then
		print("ccc isjoined=1")
		return 1 --已有人加入过群组	
	end
	print("ccc isjoined=0")
	return 0
end)

createmodule(interface,"setcheckservertrackstate", function(state)
	if state == 1 then
		tiros.moduledata.moduledata_set("web","needaskservertrack_ptp", "yes");
	else
		tiros.moduledata.moduledata_set("web","needaskservertrack_ptp", "no");
	end
end)

--添加语音到播放列表
createmodule(interface,"together_fromDbVoiceToQueue", function(T_voice)
	fromDbVoiceToQueue(T_voice);
end)

--停止播放
createmodule(interface,"together_StopPlay", function()
	StopPlay();
end)

--重发消息
createmodule(interface,"together_ReSend", function(sendtype,id,msgid,resid,length,content)
	Message_ReSend(sendtype,id,msgid,resid,length,content);
end)

--删除本地文件
createmodule(interface,"together_DeleteLocalVoiceFile", function(resid)
	tiros.airtalkee.MessageRecordFileDel(tiros.airtalkeemgr.GetAirtalkeeHandle(),resid)
end)

--查询个人信息
createmodule(interface,"together_getUserInfo", function(aid)
	return getUserInfo(aid);
end)




tiros.together = readOnly(interface);


