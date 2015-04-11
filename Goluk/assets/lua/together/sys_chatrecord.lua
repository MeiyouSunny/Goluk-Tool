--[[
--@描述:聊天记录管理
--（1）添加一条语音。
--（2）添加一条文本记录。
--（3）添加一条消息记录。
--（4）获取指定消息
--（5）修改一条记录
--@编写人:贾玉峰
--@创建日期: 2013-06-8 13:56
--]]

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/framework/sys_framework"
require"lua/json"
require"lua/commfunc"
require"lua/http"
require"lua/moduledata"
require"lua/database"

--命名空间
local interface = {};
--全局变量
local gchat = nil;

--消息类型
local MSG_TYPE_TEXT = 1;		--文本消息
local MSG_TYPE_SPEEACH = 2;		--语音消息
local MSG_TYPE_TIMESPE = 3;		--时间分隔
local MSG_TYPE_GROUP_SYS = 4;		--系统推送消息

--界面切换
local PAGE_MAP = 1;		--地图界面
local PAGE_CHATLIST = 2;	--聊天列表
local currentPage = 1;		--默认地图界面

--与平台交互消息
local MSG_NEW_MSG = 167;		--新消息的framework值
local MSG_OFFLINE_MSG = 168; -- 离线消息来时通知平台刷新
local MSG_CHAT_COUNT = 169;	--未显示消息framework值
local MSG_CHAT_SEND_RESULT = 171; -- 发送成功或失败
local EVT_MEET_AUDIO_STATUS = 156

local MSG_PLATFORM_KEY_NEW_MSG = "userchatlistnewmsg_ptp";	--保存在framework中的key值(新消息)
local MSG_PLATFORM_KEY_OFFLINE_DATA = "userchatlistofflinedata_ptp";
local MSG_PLATFORM_KEY_CHAT_COUNT = "userchatlistmsgcount_ptp";	-- 保存在framework中的key值(未显示消息数)
local MSG_PLATFORM_KEY_PLAY_ID = "userchatlistplayid_ptp";
local MSG_PLATFORM_KEY_SEND_RESULT = "userchatlistsenderror_ptp";


--一次查询的记录条数
local MSG_SPACE = 10;
-- 当前登录用户的aid
local CurrentLoginAid = "10000";
--添加时间分隔的间隔
local TIME_SPACE =3 * 60 * 1000;

--[[
ID:自动增长ID
AID：用户ID
MSGID：消息唯一ID
MSGTYPE：消息类型 1:文本，2：语音 3：时间分隔，4：系统消息
MSGCONTENT: 消息内容 	如果：MSGTYPE ==1：则为文本
			如果：MSGTYPE ==2：则为语音ID
			如果：MSGTYPE ==3： 则为时间分隔
			如果：MSGTYPE ==4： 则为系统消息
MSGDATE：消息日期
ISSEND： 是否发送成功1＝成功，0＝ 失败 (这个只针对我个人的消息，如果是接受的消息，则一率为 1)
ISSHOW： 是否已显示 1＝显示，0＝未显示 (这个是针对地图右下角的数字的计算方式，针对语音与文本消息)
ISPLAY： 是否已播放 1=已播放 0=未播放 (这人只针对语音消息 ，其余的消息一率为1)
SPEECHLEN：语音的时长 (只针对语音，其余的消息一率为 0)
GROUPID: 群号
AGROUPID: 爱滔客群号

--]]
local function CreateDatabaseTable()
	
	local str = "CREATE TABLE IF NOT EXISTS CHATRECORD (ID  integer PRIMARY KEY autoincrement, \
							AID CHAR (24) ,\
							MSGID CHAR (24) ,\
							MSGTYPE INTEGER NOT NULL, \
							MSGCONTENT VARCHAR (24) NOT NULL, \
							MSGDATE CHAR (24) NOT NULL, \
							ISSEND INTEGER NOT NULL, \
							ISSHOW INTEGER NOT NULL, \
							ISPLAY INTEGER NOT NULL, \
							SPEECHLEN INTEGER NOT NULL,\
							AGROUPID CHAR (24)\
							);"		
	tiros.database.database_execSQL(str);

	local sql = "CREATE TABLE IF NOT EXISTS GROUPMEMBER (AID  CHAR (24) PRIMARY KEY NOT NULL, \
							NAME VARCHAR (24) ,\
							URL CHAR (24)\
							);"

	tiros.database.database_execSQL(sql);
	
end


--获取当前登录用户的aid
local function getCurrentOnlineAid()
	local aid = tiros.moduledata.moduledata_get("framework", "aid");
	return aid;
end

--得到当前登录的爱滔客groupid
local function getCurrentAgroupid()
	return tostring(tiros.together.getcurrentgroupid());
end

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

-- 把字符串分隔为年月日时分秒
local function getSpliteDate(date)
	-- 2013	06 09 16 27 34
	local y = string.sub(date,1,4); --年
	local m = string.sub(date,5,6);	--月
	local d	= string.sub(date,7,8);	--日
	local h = string.sub(date,9,10);	--时
	local mi = string.sub(date,11,12);	--分
	local s = string.sub(date,13,14);	--秒
	return y,m,d,h,mi,s;	
end

-- 拼	年-月-日 时：分 (用于时间分隔符保存的时间)
local function associationDate(h,m,d,h,mi,s)
	return ""..h.."-"..m.."-"..d.." "..h..":"..mi..":"..s;
end

-- 拼	年-月-日 时：分:秒 (用于把爱滔客给的时间转换为数据库中需要的时间)
local function trasformAirtalkTimeToDB(date)
	
	local y = string.sub(date,1,4); --年
	local m = string.sub(date,5,6);	--月
	local d	= string.sub(date,7,8);	--日
	local h = string.sub(date,9,10);	--时
	local mi = string.sub(date,11,12);	--分
	local s = string.sub(date,13,14);	--秒
	
	return ""..y.."-"..m.."-"..d.." "..h..":"..mi..":"..s;
end

--把数据库中的时间转换成爱滔客给的时间
-- date:   2013-05-06 01:02:28
local function trasformDBToAirtalkTime(date)
	local y = string.sub(date,1,4); --年
	local m = string.sub(date,6,7);	--月
	local d	= string.sub(date,9,10);	--日
	local h = string.sub(date,12,13);	--时
	local mi = string.sub(date,15,16);	--分
	local s = string.sub(date,18,19);	--秒
	
	return ""..y..m..d..h..mi..s;
end

--保存成员信息
local function L_SaveGroupUserInfo(aid,name,url)
	print("jiayufeng-------------L_SaveGroupUserInfo--------11111: " .. tostring(aid) .. "   " .. tostring(name) .. " " .. tostring(url));
	if aid == nil or aid== "" then
		return;	
	end

	local sql = string.format("INSERT OR IGNORE  INTO GROUPMEMBER \
				(AID,NAME,URL) \
				VALUES('%s','%s','%s');",
				tostring(aid),
				tostring(name),
				tostring(url)
				);
	tiros.database.database_execSQL(sql);

	print("jiayufeng-------------L_SaveGroupUserInfo----------------22222:");

	
end

--修改头像
local function L_EditGroupUserHead(aid,url)
	print("jiayufeng-------------L_EditGroupUserHead----------------111111:");
	if aid == nil or aid == "" or url == nil or url == "" then
		return;
	end
	local sql_editmsg = string.format("UPDATE GROUPMEMBER SET URL='%s' WHERE AID='%s';",tostring(url),tostring(aid));
	tiros.database.database_execSQL(sql_editmsg);	

	print("jiayufeng-------------L_EditGroupUserHead----------------222222:");
end



--[[
****************************************给平台发消息*******************************
--]]

--获取平台注册的回调
local function getFunctionAndUser()
    local nFunction = tiros.moduledata.moduledata_get("framework", "pfunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "puser");    
    return nFunction, nUser;   
end

--给平台发消息
local function sendmessagetoApp(msgype,msgresult,param)
	local nFunction, nUser = getFunctionAndUser();
	if nFunction ~= nil then
		commlib.initNotifyFun(nFunction, nUser, msgype, msgresult,param);
	end
end

--通知平台刷新未显示的消息数
local function refreshNoShowCount()
	if gchat.currentPage == PAGE_MAP then
		tiros.moduledata.moduledata_set("web",MSG_PLATFORM_KEY_CHAT_COUNT,gchat.notShowCount);
		sendmessagetoApp(MSG_CHAT_COUNT,0,nil);
	end
end

--通知Web前端更新未显示的数据
local function refreshWebNoShowCount()

	tiros.moduledata.moduledata_set("logic", "chatlist_msg_count",gchat.notShowCount);
	commlib.calljavascript("system.callback(522);");

end

--示显示的消息数减1
local function noCountShowSubOne()
	if gchat.currentPage ~= PAGE_CHATLIST then
		if gchat.notShowCount > 0 then
			gchat.notShowCount = gchat.notShowCount -1;
			refreshNoShowCount();
			refreshWebNoShowCount();
		else
			gchat.notShowCount = 0;
			refreshNoShowCount();
			refreshWebNoShowCount();	
		end	
	end
end

--通知平台有新的数据
local function refreshNewData(result,isOffline)
	print("jiayufeng----LUA------chatrecord-----refreshNewData ----111 : ".. tostring(result));
	if gchat.currentPage == PAGE_CHATLIST then
		print("jiayufeng----LUA------chatrecord-----refreshNewData ----222222");
		if isOffline == true then
			print("jiayufeng----LUA------chatrecord-----refreshNewData ----333333");
			tiros.moduledata.moduledata_set("web",MSG_PLATFORM_KEY_OFFLINE_DATA,result);
			sendmessagetoApp(MSG_OFFLINE_MSG,0,"");
		else
			print("jiayufeng----LUA------chatrecord-----refreshNewData ----44444");
			tiros.moduledata.moduledata_set("web",MSG_PLATFORM_KEY_NEW_MSG,result);
			print("jiayufeng----LUA------chatrecord-----refreshNewData ----3333");
			sendmessagetoApp(MSG_NEW_MSG,0,"");

			print("jiayufeng----LUA------chatrecord-----refreshNewData ----6666666");
		end	
	end
end
--通知平台发送状态 (是否成功)
--msgid :消息自增长ID
--state: 1=成功  0=失败
local function refreshSendState(id,state)

	print("jiayufeng----LUA------chatrecord-----refreshSendState ----111" .. tostring(id).."	".. tostring(state));

	local t = {};
	t.id = tonumber(id);
	t.state = tonumber(state);

	local result = tiros.json.encode(t);

	print("jiayufeng----LUA------chatrecord-----refreshSendState ----2222:  " .. tostring(result));

	tiros.moduledata.moduledata_set("web",MSG_PLATFORM_KEY_SEND_RESULT,result);
	print("jiayufeng----LUA------chatrecord-----refreshSendState ----33333");
	sendmessagetoApp(MSG_CHAT_SEND_RESULT,0,"");

	print("jiayufeng----LUA------chatrecord-----refreshSendState ----444444");
end

local function L_SelectUserInfo(aid)
	print("jiayufeng---------------------------SelectUserInfo----------11111: " .. tostring(aid));
	local sql_id = string.format("SELECT * FROM GROUPMEMBER WHERE AID='%s';",tostring(aid));
	local result_ID = tiros.database.database_Query(sql_id);

	print("jiayufeng---------------------------SelectUserInfo----------22222: " .. tostring(result_ID));

	if result_ID == nil or result_ID == "[]" or result_ID == "" then
		return "","";
	end

	print("jiayufeng---------------------------SelectUserInfo----------33333: ");

	local T_result = tiros.json.decode(result_ID);

	print("jiayufeng---------------------------SelectUserInfo----------44444: ");

	return T_result[1].NAME,T_result[1].URL;

end

local function editMemberInfo(aid,nickname,url)

	print("jiayufeng-----------------editMemberInfo--------------11111");

	if aid == nil or url == "" or nickname == "" then
		return;
	end

	print("jiayufeng-----------------editMemberInfo--------------222222");

	local sql_AID = string.format("SELECT * FROM GROUPMEMBER WHERE AID='%s';" , tostring(aid));
	local result_AID = tiros.database.database_Query(sql_AID);

	print("jiayufeng-----------------editMemberInfo--------------333333");

	if result_AID ~= nil and result_AID ~= "" and result_AID  ~= "[]" then --有记录

		print("jiayufeng-----------------editMemberInfo--------------444444");

		local sql_editmsg = string.format("UPDATE GROUPMEMBER SET URL='%s',NAME='%s' WHERE AID='%s';",
								tostring(url),tostring(nickname),tostring(aid));
		tiros.database.database_execSQL(sql_editmsg);	
	
	else
		print("jiayufeng-----------------editMemberInfo--------------55555555");
		L_SaveGroupUserInfo(aid,nickname,url);	
	end
end

--通过aid查询个人信息,如昵称，头像
--输出	nickname: 昵称
--	headurl:  头像 
local function seletSingleMessage(aid,msgtype)

	print("jiayufeng----LUA------chatrecord-----seletSingleMessage-----1111" .. "aid " .. tostring(aid));

	local nickname = "";	--昵称
	local headurl = "";	--头像

	if aid == nil and string.len(aid) <= 0 then
		return nickname,headurl;
	end
	-- 系统消息与时间分隔不查询
	if msgtype == MSG_TYPE_TIMESPE or msgtype == MSG_TYPE_GROUP_SYS  then 
		return nickname,headurl;
	end

	--如果是自己的话，用单独的方式获取自己的头像地址和昵称
	if getCurrentOnlineAid() == aid then 
		local mePhonenum = tiros.moduledata.moduledata_get("framework","phone");
		print("wjuns++++++++Me Phone number = " .. mePhonenum);
		local sMeInfo = tiros.groupbook.queryusrinfo(mePhonenum);
		print("wjuns++++++++Me info = " .. sMeInfo);

		local tMeInfo = tiros.json.decode(sMeInfo);

		if sMeInfo ~= nil then
			nickname = tMeInfo.NICKNAME;
			headurl = tMeInfo.HEADPATH;
		end
		return nickname,headurl;
	end

	print("jiayufeng----LUA------chatrecord-----seletSingleMessage-----222222");
	--从本地文件中查询信息
	local nicknameFromFile,urlFromFile = tiros.together.together_getUserInfo(aid);
	print("jiayufeng----LUA------chatrecord-----seletSingleMessage-----3333333");
	local niState = false;
	local nheState =false;
	if nicknameFromFile ~= nil and nicknameFromFile ~= ""  then
		niState = true;
	end

	print("jiayufeng----LUA------chatrecord-----seletSingleMessage-----444444");

	if urlFromFile ~= nil and urlFromFile ~= ""  then
		nheState = true;
	end

	print("jiayufeng----LUA------chatrecord-----seletSingleMessage-----5555555");

	if niState == true or nheState == true then
	print("jiayufeng----LUA------chatrecord--seletSingleMessage--6666666: " .. tostring(nicknameFromFile) .. "  " .. tostring(urlFromFile));
		
		nickname = nicknameFromFile;
		headurl = urlFromFile;
		--更改数据库个人信息
		editMemberInfo(aid,nickname,headurl);
		print("jiayufeng----LUA------chatrecord-----seletSingleMessage-----7777777" );
		return nickname,headurl; 
	end

	print("jiayufeng----LUA------chatrecord-----seletSingleMessage-----44444");

	--从数据库里查询信息
	local nicknameFromDB,urlFromDB = L_SelectUserInfo(aid);
	nickname = nicknameFromDB;
	headurl = urlFromDB;

	print("jiayufeng----LUA------chatrecord---seletSingleMessage-5555555" .. "name: " .. tostring(nickname) .. "  h: " .. tostring(headurl));

	return nickname,headurl;	
end

--[[
****************************************数据库操作*******************************
--]]


local function getQueryData(isFromNew,endid,space)
	--查询结果
	local resultSql = nil;
	if isFromNew == true then -- 从最新的记录开始查询
		resultSql = string.format("SELECT * FROM CHATRECORD WHERE AGROUPID='%s' ORDER BY ID DESC LIMIT %d;",
					getCurrentAgroupid(),tonumber(space));
	else	--从指定记录开始查询
		resultSql = string.format("SELECT * FROM CHATRECORD WHERE ID <%d AND AGROUPID='%s' \
					ORDER BY ID DESC LIMIT %d;" ,
					tonumber(endid),
					getCurrentAgroupid(),
					tonumber(space)
					);
	end

	print("jiayufeng----LUA------chatrecord-----queryRecordFromBase-----444444");

	local users = tiros.database.database_Query(resultSql);
	print("jiayufeng----LUA------chatrecord-----queryRecordFromBase-----555555:  " .. users);
	local T = tiros.json.decode(users);
	return T;		
end


--从selectbase开始查询10条数据
local function queryRecordFromBase(endid,space)

	print("jiayufeng----LUA------chatrecord-----queryRecordFromBase-----111");

	if space == nil or tonumber(space) <= 0 then
		return nil;	
	end

	local isFromNew = false; -- 是否从最新的记录中取
	local startDate = nil;	-- 开始查询的记录时间

	print("jiayufeng----LUA------chatrecord-----queryRecordFromBase-----222222");

	--查询endid最新的时间
	if endid ~= nil and endid ~= "" then 
		isFromNew = false;
	else
		isFromNew = true;
	end

	print("jiayufeng----LUA------chatrecord-----queryRecordFromBase-----333333");

	local T = getQueryData(isFromNew,endid,space);
	if T == nil then
		return nil;	
	end

	--组织数据 给平台发送
	local t_data = {};
	local t_Array = {};

	for i = #(T) ,1,-1 do 
		print("jiayufeng----LUA------chatrecord-----queryRecordFromBase-----66666");
		if T[i].ID ~= endid then
			local t_singledata = {};
			t_singledata.id = tonumber(T[i].ID);
			t_singledata.msgtype = tonumber(T[i].MSGTYPE);
			t_singledata.msgid = tostring(T[i].MSGID);
			t_singledata.msgcontent = tostring(T[i].MSGCONTENT);
			t_singledata.issend = tonumber(T[i].ISSEND);
			t_singledata.isplay = tonumber(T[i].ISPLAY);
			t_singledata.speechlen = tonumber(T[i].SPEECHLEN);
			t_singledata.aid = tostring(T[i].AID);
			--查询个人信息
			local nickname,headpath = seletSingleMessage(t_singledata.aid,t_singledata.msgtype);
			t_singledata.nickname = tostring(nickname);
			t_singledata.url = tostring(headpath);
			
			
			table.insert(t_Array,t_singledata);
		
		end
	end
	
	print("jiayufeng----LUA------chatrecord-----queryRecordFromBase-----777777");

	t_data.message = t_Array;

	local result = tiros.json.encode(t_data);

	print("jiayufeng----LUA------chatrecord-----queryRecordFromBase-----8888: " .. tostring(result));

	return result;

end

--添加离线数据后，查询最新的10条数据，通知平台刷新
local function getTopData()
	local result = queryRecordFromBase(nil,10);

	if result == nil or result == "" then
		return;	
	end
	--重新刷新数据
	refreshNewData(result,true);

end

--从指定的id开始 ，查询所有比id号大的未读的语音消息
--id: 消息的自增id
local function queryNoPlayRecord(id)

	print("jiayufeng----LUA------chatrecord-----queryNoPlayRecord---111");

	if id == nil or id == "" then
		return;
	end

	--结果添加到
	local T_result = {};

	print("jiayufeng----LUA------chatrecord-----queryNoPlayRecord---222222");
	--查询当前用户点击的语音的日期
	local Sql_voiceDate = string.format("SELECT * FROM CHATRECORD WHERE ID=%d AND AGROUPID='%s';",tonumber(id),getCurrentAgroupid());
	local users = tiros.database.database_Query(Sql_voiceDate);
	print("jiayufeng----LUA------chatrecord-----queryNoPlayRecord---232323: " .. tostring(users));

	local T_voiceDate = tiros.json.decode(users);
	if T_voiceDate == nil or type(T_voiceDate) ~= "table" then
		return nil;
	end

	print("jiayufeng----LUA------chatrecord-----queryNoPlayRecord---444444");
	--这条记录是用户点击的一条，不管是否播放过，都应该加入队列中的第一条记录中
	local T_currentRecord = {};
	T_currentRecord.msgid = T_voiceDate[1].MSGID;
	T_currentRecord.resid = T_voiceDate[1].MSGCONTENT;
	T_currentRecord.aid = T_voiceDate[1].AID;
	T_currentRecord.groupid = T_voiceDate[1].AGROUPID;
	table.insert(T_result,T_currentRecord);

	print("jiayufeng----LUA------chatrecord-----queryNoPlayRecord---666666");

	--查询比当前用户点击的语音大的所有未读的语音，加入队列
	local Sql_voiceResult = string.format("SELECT * FROM CHATRECORD WHERE ID >%d AND \
									MSGTYPE =2 AND \
									ISPLAY=0 AND\
									AGROUPID='%s'\
									ORDER BY ID ASC;",
							tonumber(id),getCurrentAgroupid());
	local id_voiceResult = tiros.database.database_Query(Sql_voiceResult);
	local T_voiceResult = tiros.json.decode(id_voiceResult);

	print("jiayufeng----LUA------chatrecord-----queryNoPlayRecord---888888   " .. tostring(id_voiceResult));

	if T_voiceResult == nil or type(T_voiceResult) ~= "table" then
		tiros.together.together_fromDbVoiceToQueue(T_result);
		return nil;	
	end

	print("jiayufeng----LUA------chatrecord-----queryNoPlayRecord---99999999");

	for k,v in pairs(T_voiceResult) do
		print("jiayufeng----LUA------chatrecord-----queryNoPlayRecord---AAAAAAAAAA :  " .. tostring(v.ID));
		local tmp = {};
		tmp.msgid = tostring(v.MSGID);
		tmp.resid = tostring(v.MSGCONTENT);
		tmp.aid = tostring(v.AID);
		tmp.groupid = tostring(v.AGROUPID);

		table.insert(T_result,tmp);
	end
	local resu_content = tiros.json.encode(T_result);
	print("jiayufeng----LUA------chatrecord-----queryNoPlayRecord---AAAAAAAAAA: " .. tostring(resu_content));
	--插入播放队列中()
	tiros.together.together_fromDbVoiceToQueue(T_result);
	
	print("jiayufeng----LUA------chatrecord-----queryNoPlayRecord---BBBBBBBB");
end

--设置所有的消息的ISSHOW状态为已显示,用于用户进入聊天列表时设置
local function setAllRecordShowState()

	local sql = string.format("UPDATE CHATRECORD SET ISSHOW=%d;",1);
	tiros.database.database_execSQL(sql);

	gchat.notShowCount = 0;
	--通知平台刷新
	refreshNoShowCount();
	--通知Web前端刷新
	refreshWebNoShowCount();	

end

-- 设置指定id号的语音(ISPLAY)状态的已播放
local function L_SetVoicePlayState(msgid,isPlay)

	if msgid == nil or msgid == "" then
		return;	
	end

	local state = 0;
	if isPlay == true then
		state = 1;	
	else
		state = 0;
	end
	--设置播放状态
	local sql = string.format("UPDATE CHATRECORD SET ISPLAY=%d WHERE MSGID='%s';",tonumber(state),tostring(msgid));	
	tiros.database.database_execSQL(sql);
	
	--设置显示状态(已显示)
	local sql_noshowState = string.format("UPDATE CHATRECORD SET ISSHOW=%d WHERE MSGID='%s';",1,tostring(msgid));
	tiros.database.database_execSQL(sql);
	
end

--设置我发送的消息状态(ISSEND)为已发送
local function L_setMsgSendState(msgid,issendSucess)
	print("jiayufeng----LUA------chatrecord-----L_setMsgSendState ----111");
	if msgid ==nil or msgid == "" then
		return;
	end

	print("jiayufeng----LUA------chatrecord-----L_setMsgSendState ----22222");

	local state = 1;
	if issendSucess == true then
		state = 1;
	else
		state = 0;
	end

	print("jiayufeng----LUA------chatrecord-----L_setMsgSendState ----3333");

	local sql = string.format("UPDATE CHATRECORD SET ISSEND=%d WHERE MSGID='%s';",tonumber(state),tostring(msgid));
	tiros.database.database_execSQL(sql);

	print("jiayufeng----LUA------chatrecord-----L_setMsgSendState ----4444444");

	-- 通知平台更新

	local selectID = string.format("SELECT * FROM CHATRECORD WHERE MSGID ='%s';",tostring(msgid));
	local id_Result = tiros.database.database_Query(selectID);
	if id_Result == nil or id_Result == "" then
		return;
	end

	print("jiayufeng----LUA------chatrecord-----L_setMsgSendState ----555555: " .. tostring(id_Result));

	local T_resultID = tiros.json.decode(id_Result);

	if T_resultID == nil or type(T_resultID) ~= "table" then
		return;	
	end

	local ID = T_resultID[1].ID;

	print("jiayufeng----LUA------chatrecord-----L_setMsgSendState ----6666666:  "  .. tostring(ID));

	refreshSendState(ID,state);

end

--查询是否有相同的数据
--比id大
local function isSameTypeRecord(id,datetype)
	local isSame,lastSameTypeDate;

	local sql_same = string.format("SELECT * FROM CHATRECORD WHERE ID > %d AND MSGTYPE=%d \
										AND AGROUPID='%s' ORDER BY ID DESC LIMIT 1",
								tonumber(id),tonumber(datetype),getCurrentAgroupid());
	local result_same = tiros.database.database_Query(sql_same);
	if result_same == nil or result_same == "" or result_same == "[]" then
		isSame = false;
		lastSameTypeDate = nil;
	else
		local T_same = tiros.json.decode(result_same);
		isSame = true;
		lastSameTypeDate = T_same[1].MSGDATE;
	end

	return isSame,lastSameTypeDate;
end
--new 是否比 old大三分钟
--old 2013-05-06 01:02:28
--new 2013-05-06 01:02:28
local function isSubThreeSecord(old,new)

	print("jiayufeng----LUA------chatrecord-----isSubThreeSecord --111111:" .. old .. "   new:" .. new );

	local old_airtalkee = trasformDBToAirtalkTime(old);
	local new_airtalkee = trasformDBToAirtalkTime(new);

	local oy,om,od,oh,omi = getSpliteDate(old_airtalkee);
	local ny,nm,nd,nh,nmi = getSpliteDate(new_airtalkee);

	
	--判断年
	if tonumber(ny) > tonumber(oy) then
		return true;
	end

	if tonumber(ny) < tonumber(oy) then
		return false;	
	end

	--判断月
	if tonumber(nm) > tonumber(om) then
		return true;
	end

	if tonumber(nm) < tonumber(om) then
		return false;
	end


	--判断日
	if tonumber(nd) > tonumber(od) then
			return true;
	end

	if tonumber(nd) < tonumber(od) then
			return false;
	end

	--判断 时分
	local newTime = nh * 60 + nmi;
	local oldTime = oh * 60 + omi;
	local sub = newTime - oldTime;
	print("jiayufeng----LUA------chatrecord-----isSubThreeSecord --2222:" .."newTime: " .. tostring(newTime));
	print("jiayufeng----LUA------chatrecord-----isSubThreeSecord --2222:" .."oldTime: " .. tostring(oldTime));
	print("jiayufeng----LUA------chatrecord-----isSubThreeSecord --2222:" .."sub: " .. tostring(sub));
	if sub >= 3 then
		return true;
	else
		return false;
	end	
end

--新的时间是否比旧的时间大
--old 2013-05-06 01:02:28
--new 2013-05-06 01:02:28
local function isNewBigThanOld(old,new)

	print("jiayufeng----LUA------chatrecord-----isNewBigThanOld --111111:" .. old .. "   new:" .. new );

	local old_airtalkee = trasformDBToAirtalkTime(old);
	local new_airtalkee = trasformDBToAirtalkTime(new);

	local oy,om,od,oh,omi = getSpliteDate(old_airtalkee);
	local ny,nm,nd,nh,nmi = getSpliteDate(new_airtalkee);

	print("jiayufeng----LUA------chatrecord-----isNewBigThanOld --2222:" .. tostring(old_airtalkee) .. "   new:" .. tostring(new_airtalkee) );

	print("jiayufeng----LUA------chatrecord-----isNewBigThanOld --3333:" .. tostring(oy) .. "  :" .. tostring(om) .. "  :" .. tostring(od).. "  :" .. tostring(oh).. "  :" .. tostring(omi) );

	print("jiayufeng----LUA------chatrecord-----isNewBigThanOld --44444:" .. tostring(ny) .. "  :" .. tostring(nm) .. "  :" .. tostring(nd).. "  :" .. tostring(nh).. "  :" .. tostring(nmi) );


	--年
	if tonumber(ny) > tonumber(oy) then
		print("jiayufeng----LUA------chatrecord-----isNewBigThanOld --555555:" );
		return true;	
	end

	if tonumber(ny) < tonumber(oy) then
		print("jiayufeng----LUA------chatrecord-----isNewBigThanOld --555555:" );
		return false;	
	end

	
	--月
	if tonumber (nm) > tonumber(om) then
		print("jiayufeng----LUA------chatrecord-----isNewBigThanOld --666666:" );
		return true;
	end

	if tonumber (nm) < tonumber(om) then
		print("jiayufeng----LUA------chatrecord-----isNewBigThanOld --666666:" );
		return false;
	end
	
	--日
	if tonumber (nd) > tonumber(od) then
		print("jiayufeng----LUA------chatrecord-----isNewBigThanOld --777777:" );
		return true;
	end

	if tonumber (nd) < tonumber(od) then
		print("jiayufeng----LUA------chatrecord-----isNewBigThanOld --777777:" );
		return false;
	end
	--时
	if tonumber (nh) > tonumber(oh) then
		print("jiayufeng----LUA------chatrecord-----isNewBigThanOld --888888:" );
		return true;
	end

	if tonumber (nh) < tonumber(oh) then
		print("jiayufeng----LUA------chatrecord-----isNewBigThanOld --888888:" );
		return false;
	end

	--分
	if tonumber (nmi) > tonumber(omi) then
		print("jiayufeng----LUA------chatrecord-----isNewBigThanOld --99999:" );
		return true;
	end

	print("jiayufeng----LUA------chatrecord-----isNewBigThanOld --AAAAAA:" );

	return false;
end

--插入时间分隔符
--willAddRecordTime格式 2013-06-05 00:24:57
local function InsertTimeLine(willAddRecordTime,agroupid)

	print("jiayufeng----LUA------chatrecord-----InsertTimeLine --111111:" );

	local nisshow =nil;
	if gchat.currentPage == PAGE_MAP then
		nisshow = 0;
	else
		nisshow = 1;
	end 

	local sql = string.format("INSERT OR IGNORE  INTO CHATRECORD \
				(AID,MSGID,MSGTYPE,MSGCONTENT,MSGDATE,ISSEND,ISSHOW,ISPLAY,SPEECHLEN,AGROUPID) \
				VALUES('%s','%s',%d,'%s','%s',%d,%d,%d,%d,'%s');",
				"","",tonumber(MSG_TYPE_TIMESPE),willAddRecordTime,willAddRecordTime,1,tonumber(nisshow),1,
				0,tostring(agroupid));
	
	tiros.database.database_execSQL(sql);

	print("jiayufeng----LUA------chatrecord-----InsertTimeLine --2222222:" );
end

--查询大于id所有的记录最大的时间 
local function SelectMaxDate(id,groupid)
	local sql_lastMaxDate = string.format("SELECT * FROM CHATRECORD WHERE ID>=%d AND AGROUPID='%s' \
										ORDER BY MSGDATE DESC LIMIT 1",tonumber(id),getCurrentAgroupid());
										
	local lastMaxDate = tiros.database.database_Query(sql_lastMaxDate);
	
	if lastMaxDate == nil or lastMaxDate == "" or lastMaxDate == "[]" then
		return nil;
	end
	
	local T_lastMaxDate = tiros.json.decode(lastMaxDate);
	return T_lastMaxDate[1].MSGDATE;

end

--查找最后一个时间分隔符的时间
local function getLastTimeLine()
	print("jiayufeng----LUA------chatrecord-----getLastTimeLine --11111:");
	local sql_lastTimeLine = string.format("SELECT * FROM CHATRECORD WHERE MSGTYPE=3 AND AGROUPID='%s' \
										ORDER BY ID DESC LIMIT 1",getCurrentAgroupid());
	local lastTimeLine = tiros.database.database_Query(sql_lastTimeLine);
	print("jiayufeng----LUA------chatrecord-----getLastTimeLine --2222:" .. tostring(lastTimeLine));
	if lastTimeLine == nil or lastTimeLine == "" or lastTimeLine=="[]" then
		return nil,nil;
	else
		print("jiayufeng----LUA------chatrecord-----getLastTimeLine --333333:");
		local T_timeline = tiros.json.decode(lastTimeLine);
		print("jiayufeng----LUA------chatrecord-----getLastTimeLine --4444:");
		if T_timeline == nil or type(T_timeline)~= "table" or T_timeline =="[]" then
			print("jiayufeng----LUA------chatrecord-----getLastTimeLine --555555:");
			return nil,nil;
		else
			print("jiayufeng----LUA------chatrecord-----getLastTimeLine --6666666:");
 			return T_timeline[1].MSGDATE,T_timeline[1].ID;
		end
	end
	
	return nil,nil;
end

--添加时间分隔符 (new) willAddRecordTime为即将插入数据库的记录的时间
--willAddRecordTime格式 2013-06-05 00:24:57
local function addTimeLine(willAddRecordTime,datetype,agroupid)
	if willAddRecordTime == nil or willAddRecordTime == "" then
		return false;	
	end

	print("jiayufeng----LUA------chatrecord-----addTimeLine --111111:" .. tostring(willAddRecordTime));
	-- 查询数据库中最后一条数据
	local sql_lastRecord = string.format("SELECT * FROM CHATRECORD WHERE AGROUPID='%s' ORDER BY ID DESC LIMIT 1",getCurrentAgroupid());
	local lastRecord = tiros.database.database_Query(sql_lastRecord);
	print("jiayufeng----LUA------chatrecord-----addTimeLine --22222222:" .. lastRecord);
	if lastRecord ==nil or lastRecord == "[]" then --数据库中没有记录，直接插入时间分隔
		print("jiayufeng----LUA------chatrecord-----addTimeLine --333333:");
		InsertTimeLine(willAddRecordTime,agroupid);
		return true;
	end
	--比最后一条记录时间相比，不插入分隔符
	local T_lastRecord = tiros.json.decode(lastRecord);
	local lastRecordDate = T_lastRecord[1].MSGDATE; -- 时间格式为 2013-06-05 00:24:57
	print("jiayufeng----LUA------chatrecord-----addTimeLine --4444444:" .. tostring(lastRecordDate));
	if isNewBigThanOld(lastRecordDate,willAddRecordTime) == false then 
		print("jiayufeng----LUA------chatrecord-----addTimeLine --55555555:");
		return false;
	end
	-- 查询最后一条时间分隔符
	print("jiayufeng----LUA------chatrecord-----addTimeLine --6666666:");
	local lastLineTime,lastLineID = getLastTimeLine(); 
	print("jiayufeng----LUA------chatrecord-----addTimeLine --77777777: " .. tostring(lastLineTime));
	if lastLineTime == nil or lastLineTime == "" then
		print("jiayufeng----LUA------chatrecord-----addTimeLine --888888888:");
		--TODO 比较两个时间是否相差3分钟
		if isSubThreeSecord(lastRecordDate,willAddRecordTime) == true then
			print("jiayufeng----LUA------chatrecord-----addTimeLine --9999999999:");
			InsertTimeLine(willAddRecordTime,agroupid);
			return true;
		else
			print("jiayufeng----LUA------chatrecord-----addTimeLine --AAAAAAAAAA:");
			return false;
		end
	end
	--比上个时间分隔符相小,直接插入
	if isNewBigThanOld(lastLineTime,willAddRecordTime) == false then 
		print("jiayufeng----LUA------chatrecord-----addTimeLine --BBBBBBBBB:");
		return false;
	end
	--查询比最后一个分隔符大的最大时间
	local lastMaxDate = SelectMaxDate(lastLineID,agroupid);
	print("jiayufeng----LUA------chatrecord-----addTimeLine --FFFFFFFFFF: " .. tostring(lastMaxDate));
	if lastMaxDate == nil or lastMaxDate == "" then
		if isSubThreeSecord(lastRecordDate,willAddRecordTime) == true then
			print("jiayufeng----LUA------chatrecord-----addTimeLine --FFFFFFFFFF:");
			InsertTimeLine(willAddRecordTime,agroupid);
			print("jiayufeng----LUA------chatrecord-----addTimeLine --GGGGGGGG:");
			return true;
		else
			print("jiayufeng----LUA------chatrecord-----addTimeLine --HHHHHHHHH:");
			return false;						
		end
	end
	
	if isSubThreeSecord(lastMaxDate,willAddRecordTime) == false then --当前时间小于最后的时间
		return false;
	end
	
	InsertTimeLine(willAddRecordTime,agroupid);
	print("jiayufeng----LUA------chatrecord-----addTimeLine --GGGGGGGG:");
	return true;
end


--新添加的数据 需要通知平台
local function dataSendToPlatform(isAddTime,naid,nmsgid,nmsgtype,nmsgcontent,nmsgdate,nspeechlen,nissend,nisshow,nisplay,agroupid)

	print("jiayufeng----LUA------chatrecord-----dataSendToPlatform --111111");

	local sql = nil;
	--查询时间最大的记录
	if isAddTime == true then -- 需要包括时间与分隔符本身
		 sql = string.format("SELECT * FROM CHATRECORD WHERE AGROUPID='%s' ORDER BY ID DESC LIMIT 2;",getCurrentAgroupid());
	else
		 sql = string.format("SELECT * FROM CHATRECORD WHERE AGROUPID='%s' ORDER BY ID DESC LIMIT 1;",getCurrentAgroupid());
	end
	local ids = tiros.database.database_Query(sql);
	local decodeT = tiros.json.decode(ids);

	local t_data = {};
	local t_Array = {};

	print("jiayufeng----LUA------chatrecord-----dataSendToPlatform --222222");

	for i = #(decodeT),1,-1 do
		print("jiayufeng----LUA------chatrecord-----dataSendToPlatform --33333");
		local t_singledata = {};
		t_singledata.id = tonumber(decodeT[i].ID)
		t_singledata.msgtype = tonumber(decodeT[i].MSGTYPE);
		print("jiayufeng----LUA------chatrecord-----dataSendToPlatform --4444");
		t_singledata.msgid = tostring(decodeT[i].MSGID);
		print("jiayufeng----LUA------chatrecord-----dataSendToPlatform --55555");
		t_singledata.msgcontent = tostring(decodeT[i].MSGCONTENT);
		print("jiayufeng----LUA------chatrecord-----dataSendToPlatform --6666");
		t_singledata.issend = tonumber(decodeT[i].ISSEND);
		print("jiayufeng----LUA------chatrecord-----dataSendToPlatform --777777");
		t_singledata.isshow = tonumber(decodeT[i].ISSHOW);
		print("jiayufeng----LUA------chatrecord-----dataSendToPlatform --88888");
		t_singledata.isplay =tonumber(decodeT[i].ISPLAY);
		print("jiayufeng----LUA------chatrecord-----dataSendToPlatform --99999999");
		t_singledata.speechlen =tonumber(decodeT[i].SPEECHLEN);
		t_singledata.aid = tostring(decodeT[i].AID);
		--查询个人信息
		local nickname,headpath = seletSingleMessage(t_singledata.aid,t_singledata.msgtype);
		t_singledata.nickname = nickname;
		t_singledata.url = headpath;
		

		table.insert(t_Array,t_singledata);	
	end

	print("jiayufeng----LUA------chatrecord-----dataSendToPlatform --3333333333");

	t_data.message = t_Array;
	local result = tiros.json.encode(t_data);

	print("jiayufeng----LUA------chatrecord-----dataSendToPlatform --444444:".. tostring(result));
	
	--通知平台有新的数据
	refreshNewData(result,false);

	--通知平台有新的未显示消息
	refreshNoShowCount();
	--通知Web前端
	refreshWebNoShowCount();
end

--输出格式 2013-06-05 00:24:57 (数据库保存格式)
local function getAccuTime(aid,recvtime)
	local resultDate = nil;

	print("jiayufeng----LUA------chatrecord-----getAccuTime --11111:" .. tostring(aid) .. "recvtime :" .. tostring(recvtime));

	print("jiayufeng----LUA------chatrecord-----getAccuTime --121212:" .. tostring(getCurrentOnlineAid()));

	if getCurrentOnlineAid() == aid or recvtime == "" then --保存本人的信息
		print("jiayufeng----LUA------chatrecord-----getAccuTime --222222");
		resultDate = trasformAirtalkTimeToDB(GetDate());
	else --接受群内的消息
		print("jiayufeng----LUA------chatrecord-----getAccuTime --333333");
		resultDate = trasformAirtalkTimeToDB(recvtime);
	end

	print("jiayufeng----LUA------chatrecord-----getAccuTime --44444:" .. tostring(resultDate));

	return resultDate;
end

--[[
****************************************内部接口*******************************
--]]

--计算在添加数据时 nisshow的值
local function getIsShow(aid,msgtype)
	local nisshow = 0;
	
	if gchat.currentPage == PAGE_MAP then --地图界面

		if msgtype == MSG_TYPE_TEXT or msgtype == MSG_TYPE_SPEEACH then --文本消息　与 语音消息
				if getCurrentOnlineAid() ~= aid then --其它人发的消息
					gchat.notShowCount = gchat.notShowCount + 1;
					refreshNoShowCount();
					refreshWebNoShowCount();
					nisshow = 0;
				else --本人发的消息，
					nisshow = 1;
				end
		else
				nisshow = 1;
		end
		
	else --　在List界面，所有的消息都为已读状态
		nisshow = 1;
	end

	return tonumber(nisshow);

end

-- 添加记录到数据库
local function L_addRecord(aid,msgid,msgtype,msgcontent,recvtime,speechlen,agroupid)

	print("jiayufeng----LUA------chatrecord-----L_addRecord --111",aid,msgid,msgtype,msgcontent,recvtime,speechlen);

	--添加时间分隔
	local isAddTime = addTimeLine(getAccuTime(aid,recvtime),msgtype,agroupid);

	print("jiayufeng----LUA------chatrecord-----L_addRecord --222222");
	
	--要插入数据库的字段
	local naid = aid;
	local nmsgid = msgid;
	local nmsgtype = tonumber(msgtype);
	local nmsgcontent = msgcontent;
	local nmsgdate = getAccuTime(aid,recvtime);
	local nissend = 1;
	local nisshow = 0;
	local nisplay = 0;
	local nspeechlen = tonumber(speechlen);

	print("jiayufeng----LUA------chatrecord-----L_addRecord --333333");
	-- 设置isshow
	--[[
	if gchat.currentPage == PAGE_MAP then --地图界面
		if aid ~= "" and getCurrentOnlineAid() ~= aid then --其它人发的消息

			if msgtype == MSG_TYPE_TEXT or msgtype == MSG_TYPE_SPEEACH then
				gchat.notShowCount = gchat.notShowCount + 1;
				refreshNoShowCount();
				refreshWebNoShowCount();
				nisshow = 0;
			else
				nisshow = 1;
			end
	
		else --本人发消息
			nisshow = 1;
		end
		
	else
		nisshow = 1;
	end
	--]]
	
	nisshow = getIsShow(aid,msgtype);
	
	--设置issend
	if getCurrentOnlineAid() == aid then
		nissend = 2; --发送中
	else
		nissend = 1;
	end

	print("jiayufeng----LUA------chatrecord-----L_addRecord --444444");

	--逻辑处理
	if msgtype == MSG_TYPE_TEXT then
		nspeechlen = 0;
		nisplay = 1;

	elseif msgtype == MSG_TYPE_SPEEACH then
		--如果是本人发送的语音，则设置为已播放状态
		if getCurrentOnlineAid() == aid then
			nisplay = 1;
		else
			nisplay = 0;
		end

	elseif msgtype == MSG_TYPE_GROUP_SYS then
		nspeechlen = 0;
		nisplay = 1;
	end

	print("jiayufeng----LUA------chatrecord-----L_addRecord --5555555");

	--插入数据库

	local sql = string.format("INSERT OR IGNORE  INTO CHATRECORD \
				(AID,MSGID,MSGTYPE,MSGCONTENT,MSGDATE,ISSEND,ISSHOW,ISPLAY,SPEECHLEN,AGROUPID) \
				VALUES('%s','%s',%d,'%s','%s',%d,%d,%d,%d,'%s');",
				naid,
				nmsgid,
				tonumber(nmsgtype),
				nmsgcontent,
				tostring(nmsgdate),
				tonumber(nissend),
				tonumber(nisshow),
				tonumber(nisplay),
				tonumber(nspeechlen),
				tostring(agroupid));
	tiros.database.database_execSQL(sql);

	print("jiayufeng----LUA------chatrecord-----L_addRecord --666666666");

	--组织数据并通知平台有新的记录(这个记录有可能包括时间分隔符 和 记录本身)
	dataSendToPlatform(isAddTime,naid,nmsgid,nmsgtype,nmsgcontent,nmsgdate,nspeechlen,nissend,nisshow,nisplay,agroupid);

	print("jiayufeng----LUA------chatrecord-----L_addRecord --77777777");
		
end

local function L_currengPage(where)
	
	if tonumber(where) == PAGE_MAP or tonumber(where) == PAGE_CHATLIST then
		gchat.currentPage = tonumber(where);
	end
	--设置所有消息为已读
	if gchat.currentPage == PAGE_CHATLIST then
		setAllRecordShowState();		
	end
end

local function L_getNotShowMsgCount()

	if gchat == nil then
		return 0;
	else
		return tonumber(gchat.notShowCount);
	end
end

local function L_reSend(id)
	print("jiayufeng----LUA------chatrecord-----L_reSend----1111");
	if tonumber(id) < 0 then
		return;	
	end

	local sql_resend = string.format("SELECT * FROM CHATRECORD WHERE ID = %d ;", tonumber(id));
	local result_resend = tiros.database.database_Query(sql_resend);

	print("jiayufeng----LUA------chatrecord-----L_reSend----22222:  " .. tostring(result_resend));

	if result_resend == nil or result_resend == "[]" or result_resend == "" then
		return;
	end

	print("jiayufeng----LUA------chatrecord-----L_reSend----3333");

	local T_resend = tiros.json.decode(result_resend);
	local msgtype = T_resend[1].MSGTYPE;

	print("jiayufeng----LUA------chatrecord-----L_reSend----444444");

	local groupid = "";
	local msgid = T_resend[1].MSGID;
	local resid = "";
	local content = "";
	local length = 0;

	print("jiayufeng----LUA------chatrecord-----L_reSend----55555 :  " .. tostring(msgtype));

	if tonumber(msgtype) == tonumber(MSG_TYPE_TEXT) then --文本

		print("jiayufeng----LUA------chatrecord-----L_reSend----666666");
		content = T_resend[1].MSGCONTENT;
		tiros.together.together_ReSend(MSG_TYPE_TEXT,id,msgid,resid,length,content);
		print("jiayufeng----LUA------chatrecord-----L_reSend----77777");
		
	
	else --语音

		print("jiayufeng----LUA------chatrecord-----L_reSend----88888");
		resid = T_resend[1].MSGCONTENT;
		length = T_resend[1].SPEECHLEN;
		tiros.together.together_ReSend(MSG_TYPE_SPEEACH,id,msgid,resid,length,content);

		print("jiayufeng----LUA------chatrecord-----L_reSend----999999");
	end
end


local function L_stop(id)
	tiros.together.together_StopPlay();
end

local function L_getCurrentPlayId()

	return gchat.currentPlayId;
end

local function L_getdMaxId()

	local sql = "select max(ID) from CHATRECORD";
	local ids = tiros.database.database_Query(sql);
	local decodeT = tiros.json.decode(ids);
	local result = 0;		
	if decodeT ~= nil and type(decodeT) == "table" and decodeT[1] ~= nil  then
		result =  decodeT[1].ID;
	end
	return result;

end

--添加离线消息时间分隔符
local function addOfflineTimeLine(newtime,groupid)

	print("jiayufeng----Lua---chatrecord-----addOfflineTimeLine--111:" .. tostring(newtime));

	if newtime == "" or newtime == nil then
		return;	
	end

	local sql = string.format("INSERT OR IGNORE  INTO CHATRECORD \
				(AID,MSGID,MSGTYPE,MSGCONTENT,MSGDATE,ISSEND,ISSHOW,ISPLAY,SPEECHLEN,AGROUPID) \
				VALUES('%s','%s',%d,'%s','%s',%d,%d,%d,%d,'%s');",
				"",
				"",
				tonumber(MSG_TYPE_TIMESPE),
				tostring(newtime),
				tostring(newtime),
				1,
				1,
				1,
				0,tostring(groupid));
	
			tiros.database.database_execSQL(sql);

end

--添加离线消息
local function L_AddOfflineRecord(T_fflinemsg)

	local result_offline = tiros.json.encode(T_fflinemsg);
	print("jiayufeng----LUA------chatrecord-----L_AddOfflineRecord----1111:" .. tostring(result_offline));

	if T_fflinemsg == nil or type(T_fflinemsg) ~="table" then
		return;	
	end

	print("jiayufeng----LUA------chatrecord-----L_AddOfflineRecord----222222");

	local isAddLine = false;

	for k,v in pairs(T_fflinemsg.msg) do

	print("jiayufeng----LUA------chatrecord-----L_AddOfflineRecord----3333333");

		local m_type = tonumber(v.type);
		print("jiayufeng----LUA------chatrecord-----L_AddOfflineRecord----44444:" .. tostring(m_type));
		if m_type == 10009 or m_type == 10008 then

			print("jiayufeng----LUA------chatrecord-----L_AddOfflineRecord----45454545");
			
			local naid = v.aid;
			local nmsgid = v.msgid;
			local nmsgtype = tonumber(MSG_TYPE_TEXT);
			local nmsgcontent = "";
			local nmsgdate = getAccuTime(naid,v.date);
			local nissend = 1;
			local nisshow = 0;
			local nisplay = 0;
			local nspeechlen = 0;
			local groupid = v.groupid;
			--所有离线消息添加一个时间分隔符
			if isAddLine == false then
				isAddLine = true;
				addOfflineTimeLine(nmsgdate,groupid);			
			end

			print("jiayufeng----LUA------chatrecord-----L_AddOfflineRecord----46464646");

			if gchat.currentPage == PAGE_CHATLIST then
				nisshow = 1;
			else
				gchat.notShowCount = gchat.notShowCount + 1;
				nisshow = 0;
			end 

			print("jiayufeng----LUA------chatrecord-----L_AddOfflineRecord----474747");
			if m_type == 10009  then --文本
				print("jiayufeng----LUA------chatrecord-----L_AddOfflineRecord----484848");
				nmsgtype = tonumber(MSG_TYPE_TEXT);
				nmsgcontent = v.msg;
				nisplay = 1;
				nspeechlen = 0;
				print("jiayufeng----LUA------chatrecord-----L_AddOfflineRecord----494949");
			elseif m_type == 10008 then--语音 
				print("jiayufeng----LUA------chatrecord-----L_AddOfflineRecord----4a4a4a4a");
				nmsgtype = tonumber(MSG_TYPE_SPEEACH);
				nmsgcontent = v.msg.resid;
				nisplay = 0;
				nspeechlen = v.msg.time;
				print("jiayufeng----LUA------chatrecord-----L_AddOfflineRecord----4b4b4b");
			end

			-- 插入数据库

			local sql = string.format("INSERT OR IGNORE  INTO CHATRECORD \
				(AID,MSGID,MSGTYPE,MSGCONTENT,MSGDATE,ISSEND,ISSHOW,ISPLAY,SPEECHLEN,AGROUPID) \
				VALUES('%s','%s',%d,'%s','%s',%d,%d,%d,%d,'%s');",
				naid,
				nmsgid,
				tonumber(nmsgtype),
				nmsgcontent,
				tostring(nmsgdate),
				tonumber(nissend),
				tonumber(nisshow),
				tonumber(nisplay),
				tonumber(nspeechlen),
				tostring(groupid));
	
			tiros.database.database_execSQL(sql);

		end
			
	end

	print("jiayufeng----LUA------chatrecord-----L_AddOfflineRecord----55555555");

	-- 通知平台显示
	if gchat.currentPage == PAGE_CHATLIST then
		print("jiayufeng----LUA------chatrecord-----L_AddOfflineRecord----66666666");
		local result = queryRecordFromBase(nil,10);
		if result ==nil or result == "" then
			return;		
		end

		print("jiayufeng----LUA------chatrecord-----L_AddOfflineRecord----7777777");
		
		refreshNewData(result,true);
		print("jiayufeng----LUA------chatrecord-----L_AddOfflineRecord----888888");

	elseif gchat.currentPage == PAGE_MAP then
		print("jiayufeng----LUA------chatrecord-----L_AddOfflineRecord----99999");
		--通知平台更新 noshowcount
		refreshNoShowCount();
		refreshWebNoShowCount();
	end

	print("jiayufeng----LUA------chatrecord-----L_AddOfflineRecord----AAAAAAAAAA");	
end

--设置当前正在播放的语音(更新数据库与通知平台)
local function L_SetCurrentPlayVoice(msgid)
	print("jiayufeng----LUA------chatrecord-----L_SetCurrentPlayVoice----111111");	
	if msgid == nil or msgid == "" then
		return;
	end
	print("jiayufeng----LUA------chatrecord-----L_SetCurrentPlayVoice----222222");	
	--更新数据库(已播放状态)
	L_SetVoicePlayState(msgid,true);

	print("jiayufeng----LUA------chatrecord-----L_SetCurrentPlayVoice----33333");	

	--未显示数据减1
	noCountShowSubOne();

	print("jiayufeng----LUA------chatrecord-----L_SetCurrentPlayVoice----4444444");

	--通知平台显示
	local sql_ID = string.format("SELECT * FROM CHATRECORD WHERE MSGID='%s';",tostring(msgid));
	local result_ID = tiros.database.database_Query(sql_ID);
	print("jiayufeng----LUA------chatrecord-----L_SetCurrentPlayVoice----55555");
	local T_ID = tiros.json.decode(result_ID);
	if T_ID == nil or type(T_ID) ~= "table" then
		return;	
	end 

	print("jiayufeng----LUA------chatrecord-----L_SetCurrentPlayVoice----6666666");

	local ID = T_ID[1].ID;
	--保存当前播放的ID号，以便平台可以获取
	gchat.currentPlayId = ID;

	print("jiayufeng----LUA------chatrecord-----L_SetCurrentPlayVoice----7777777");
	
end

local function L_SetPlayVoiceState(state,aid,msgid)

	print("jiayufeng----LUA------chatrecord-----L_SetPlayVoiceState----111111");

	local sql_ID = string.format("SELECT * FROM CHATRECORD WHERE MSGID='%s';",tostring(msgid));
	local result_ID = tiros.database.database_Query(sql_ID);

	print("jiayufeng----LUA------chatrecord-----L_SetPlayVoiceState----2222:" .. tostring(result_ID));

	local T_ID = tiros.json.decode(result_ID);
	if T_ID == nil or type(T_ID) ~= "table" then
		return;	
	end 

	--如果是开始播放状态，保存数据库(已播放状态)
	if state == 1 then
		L_SetCurrentPlayVoice(msgid);
	end
	print("jiayufeng----LUA------chatrecord-----L_SetPlayVoiceState----3333");
	local T_msg = {};
	T_msg.aid = tostring(aid);
	T_msg.id = tonumber(T_ID[1].ID);
	local msg = tiros.json.encode(T_msg);
	sendmessagetoApp(EVT_MEET_AUDIO_STATUS, state, msg);	
end

local function L_EditVoiceResId(msgid,resid)
	if msgid == nil or resid == nil then
		return;
	end

	local sql = string.format("UPDATE CHATRECORD SET MSGCONTENT='%s' WHERE MSGID='%s';",tostring(resid),tostring(msgid));
	tiros.database.database_execSQL(sql);	
end

--修改msgid
local function L_EditMsgid(id,msgid)
	if msgid == nil or msgid == "" then
		return;
	end

	print("jiayufeng--------------chatrecord----------------L_EditMsgid1111 ," .. tostring(msgid));
	local sql_editmsg = string.format("UPDATE CHATRECORD SET MSGID='%s' WHERE ID=%d;",tostring(msgid),tonumber(id));
	tiros.database.database_execSQL(sql_editmsg);

	print("jiayufeng--------------chatrecord----------------L_EditMsgid2222 ," .. tostring(id) );
end

--删除数据库中所有记录 以及本地的语音文件
local function L_DellAllRecord()

	print("jiayufeng--------------------chatrecord-----------DellAll---1111");
	--删除语音文件
	local sql_allmsg = string.format("SELECT MSGID,MSGCONTENT FROM CHATRECORD WHERE MSGTYPE=2");
	local result_allmsg = tiros.database.database_Query(sql_allmsg);

	if result_allmsg ~= nil and result_allmsg ~= "" and result_allmsg ~= "[]" then
		local T_allmsg = tiros.json.decode(result_allmsg);

		for k,v in pairs(T_allmsg) do
			if v.MSGCONTENT ~= nil and v.MSGCONTENT ~= "" then
				print("jiayufeng--------------------chatrecord-----------DellAll---3333:" .. tostring(v.MSGCONTENT));
				tiros.together.together_DeleteLocalVoiceFile(v.MSGCONTENT);

			end
		end	
	end
	--删除数据库所有记录
	local sql_deleall = string.format("DELETE  FROM CHATRECORD");
	tiros.database.database_execSQL(sql_deleall);
	--删除群人员信息表中所有数据
	local sql_deleteAllInfo = string.format("DELETE FROM GROUPMEMBER");
	tiros.database.database_execSQL(sql_deleteAllInfo);

	gchat.notShowCount = 0;
	--通知平台刷新
	refreshNoShowCount();
	--通知Web前端刷新
	refreshWebNoShowCount();

	print("jiayufeng--------------------chatrecord-----------DellAll---22222");
end

--把数据库中所有(发送中)的消息 改为 (发送失败)
local function UpdateAllSendState()

	print("jiayufeng--------------chatrecord-------destroy -----11111");
	--修改所有发送中的记录为发送失败
	local sql_updatesendstate = "UPDATE CHATRECORD SET ISSEND=0 WHERE ISSEND=2";
	tiros.database.database_execSQL(sql_updatesendstate);
	print("jiayufeng--------------chatrecord-------destroy -----22222222");

end
--查询数据库中未读消息
local function getNoShowCountInDb()
	print("jiayufeng--------------chatrecord-------getNoShowCountInDb-----1111111-");
	local sql_noShowcount = string.format("SELECT * FROM CHATRECORD WHERE ISSHOW=0");
	print("jiayufeng--------------chatrecord-------getNoShowCountInDb-----222222-");
	local result_noShowcount = tiros.database.database_Query(sql_noShowcount);
	print("jiayufeng--------------chatrecord-------getNoShowCountInDb-----33333333-");
	if result_noShowcount == nil or result_noShowcount == "" or result_noShowcount == "[]" then
		gchat.notShowCount = 0;
		return;
	end
	
	print("jiayufeng--------------chatrecord-------getNoShowCountInDb-----444444-");
	
	local T_noShowCount = tiros.json.decode(result_noShowcount);
	
	print("jiayufeng--------------chatrecord-------getNoShowCountInDb-----55555555-");
	
	for k,v in pairs(T_noShowCount) do 
		print("jiayufeng--------------chatrecord-------getNoShowCountInDb-----666666666-");
		gchat.notShowCount = gchat.notShowCount + 1;
	end
	
end

-- 初始化
local function initChat()
	if gchat ~= nil then
		return;
	end

	CreateDatabaseTable();

	gchat = {};
	gchat.baseselect = 0;
	gchat.currentPage = PAGE_MAP;--默认当前是地图界面
	gchat.notShowCount = 0; -- 当前未显示的消息
	gchat.currentPlayId = 0; -- 当前正在播放的语音的id
	
	gchat.isbackground = false; -- 前后台切换
	gchat.isUpdate = false; -- 是否有更新
	--更新所有发送中状态的记录为发送失败
	UpdateAllSendState();
	--查询数据库中所有未读消息的个数
	getNoShowCountInDb();

end

--程序退出时调用
local function L_destroy()
	UpdateAllSendState();
end

--[[
****************************************对外接口*(包括平台与together调用)******************************
--]]

--添加一条记录到数据库并通知平台显示
--[[
aid:那个用户说话
msgid:消息唯一id
msgtype: 消息类型
msgcontent:消息内容
recvtime： 接受时间
speechlen：语音时长
agroupid:爱滔客groupid
groupid爱滔客群组id

--]]
createmodule(interface,"AddRecord", function(aid,msgid,msgtype,msgcontent,recvtime,speechlen,agroupid)
	print("jiayufeng----LUA------chatrecord-----AddRecord",aid,msgid,msgtype,msgcontent,recvtime,speechlen);

	L_addRecord(aid,msgid,msgtype,msgcontent,recvtime,speechlen,agroupid);
end)

--添加离线消息到数据库(together调用)
createmodule(interface,"AddOfflineRecord", function(T_fflinemsg)
	
	print("jiayufeng----LUA------chatrecord-----AddOfflineRecord");
	L_AddOfflineRecord(T_fflinemsg);
end)

--指定某条记录的发送状态(together调用)
--msgid  消息的唯一id
createmodule(interface,"SetMsgSendState",function(msgid,issendSucess)
	print("jiayufeng----LUA------chatrecord-----SetMsgSendState");
	L_setMsgSendState(msgid,issendSucess);
end)

--设置播放状态(together调用)
--msgid  消息的唯一id
createmodule(interface,"SetVoicePlayState",function(msgid,isPlay)
	print("jiayufeng----LUA------chatrecord-----SetVoicePlayState");
	L_SetVoicePlayState(msgid,isPlay);
end)

--设置当前正在播放的语音(together调用)
--msgid  消息的唯一id
createmodule(interface,"SetCurrentPlayVoice",function(msgid)
	print("jiayufeng----LUA------chatrecord-----SetCurrentPlayVoice");
	L_SetCurrentPlayVoice(msgid);
end)

--播放状态的回调，包括播放开始，结束(together调用)
--msgid  消息的唯一id
createmodule(interface,"setPlayVoiceState",function(state,aid,msgid)
	L_SetPlayVoiceState(state,aid,msgid);
end)

--修改语音的resid(together调用)
--msgid  消息的唯一id
createmodule(interface,"EditVoiceResId",function(msgid,resid)
	L_EditVoiceResId(msgid,resid);
end)

--修改记录的msgid(together调用)
-- id ：数据库自增id
--msgid  消息的唯一id
createmodule(interface,"EditMsgid",function(id,msgid)
	L_EditMsgid(id,msgid);
end)

--删除所有同行组中数据(together调用)
createmodule(interface,"DellAllRecord",function()
	L_DellAllRecord();
end)

--保存群成员信息(together调用)
createmodule(interface,"SaveGroupUserInfo",function(uid,name,url)
	--L_SaveGroupUserInfo(uid,name,url);
	editMemberInfo(uid,name,url);
end)

--保存群成员信息(together调用)
createmodule(interface,"EditGroupUserHead",function(uid,url)
	L_EditGroupUserHead(uid,url);
end)


--[[***************************对外接口****平台调用************************]]--

--查询指定记录 (平台调用)
--endid 开始id
--space 取多少条记录
createmodule(interface,"QueryRecord",function(endid, space)
	print("jiayufeng----LUA------chatrecord-----QueryRecord");

	return queryRecordFromBase(endid,space);

end)

--界面切换 (平台调用)
--当前id:状态 1：地图  2：聊天界面
createmodule(interface,"CurrentPage", function(where)
	L_currengPage(where)
end)


--获取当前未显示的消息的条数 (平台调用)
createmodule(interface,"GetNotShowMsgCount", function()
	return L_getNotShowMsgCount();
end)

--重新发送消息 (平台调用)
--id:语音id
createmodule(interface,"ReSend", function(id)
	print("jiayufeng----LUA------ReSend-----111111");
	L_reSend(id);
end)

--播放消息(平台调用)
--id:语音id
createmodule(interface,"Play", function(id)
	print("jiayufeng----LUA------chatrecord-----Play");
	queryNoPlayRecord(id);
end)

--停止播放 (平台调用)
--id:语音id
createmodule(interface,"Stop", function(id)
	L_stop(id);
end)

--得到当前正在播放的id(平台调用)
createmodule(interface,"GetCurrentPlayId", function()
	return L_getCurrentPlayId();
end)

--初始化模块
createmodule(interface,"init", function()
	initChat();
end)

--销毁模块
createmodule(interface,"destroy", function()
	L_destroy();	
end)

tiros.chatrecord = readOnly(interface);
