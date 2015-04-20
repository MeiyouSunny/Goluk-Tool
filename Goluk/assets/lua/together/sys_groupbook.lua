--[[
 @描述：通讯录上传
 @编写人：wangcheng
 @创建日期：2013-4-2 下午 16:00:00
 @版本：0.1.0
--]]
require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/framework"
require"lua/http"
require"lua/json"
require"lua/database"
require"lua/moduledata"
require"lua/net-httpheaders"
require"lua/timer"
require"lua/together/sys_together"

local gt = {};
local gFilePath =  "fs2:/webcache/groupbook";
local gAppHeadImgPath = "fs0:/friendhead/"; 
local gDefaultHeadImgPath = "fs0:/lua/together/mr.png"; --默认头像路径
local gGroupBookList = {}
local gDownloading = false
local gSelfReq = false
local gRetry = 0
local tmrobj = getmodule("timer")

--_gHttplist：全局变量，用于存放正在使用的所有http句柄
local gtaddressbooklist = {}
--_gHttpweaklist：全局变量，用于存放所有http句柄的week表，week表中既包含正使用的句柄，也包含即将回收的句柄
local gtaddressbookweaklist = {}
setmetatable(gtaddressbookweaklist,{__mode ="v" })


--资源文件中编号
local RES_STR_GENERAL_POST_URL = 2103;

--资源文件地址路径
local RES_FILE_PATH = "fs0:/res/api/api.rs";
--获取URL:服务器地址及端口号  http://dev8.lbs8.com/general_Post
--local gURL = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_GENERAL_POST_URL);
--gURL = "http://dev8.lbs8.com/general_Upload"
local ContentType = "application/octet-stream"

local EVT_SYNC_GROUPADDRBOOK = 162
--通知头像变化，第二个参数0，第三个参数：{"uid":"","path":""}
local 	 EVT_MEET_HEADIMG_REPLACE = 158

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
	local date = ""..yy..MM..dd;
	return date;
end

local function getFunctionAndUser()
    local nFunction = tiros.moduledata.moduledata_get("framework", "pfunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "puser");    
    return nFunction, nUser;   
end

local function sendmessagetoApp(msgype,msgresult,param)	
	local nFunction, nUser = getFunctionAndUser();
	if nFunction ~= nil then
		commlib.initNotifyFun(nFunction, nUser, msgype, msgresult,param);
	end
end

--创建数据库表
createmodule(gt,  "createtable", function()
	--USERDETAIL[UID,AID,NICKNAME,PHONE,HEADPATH,HEADURL]
	local sql1 = "CREATE TABLE IF NOT EXISTS GROUPADDRBOOK (UID varchar (32) NOT NULL UNIQUE, \
							PHONE varchar (11) ,\
							NICKNAME varchar (128), \
							STATE varchar (1) ,\
							HEADPATH varchar (256),\
							HEADURL varchar (256),\
							AID varchar (32));"
	local sql2 = "CREATE TABLE IF NOT EXISTS GROUPADDRBOOKTIME (TIME varchar (32) );"
	tiros.database.database_open()
	tiros.database.database_execSQL(sql1)
	tiros.database.database_execSQL(sql2) 
	tiros.database.database_close()
end)

--[[
state
0.不是导航犬用户
1.是导航犬用户
2.是导航去用户，但没有aid
--]]
local function InsertUser(uid,phone,nickname,state,headpath,headurl,aid)
	local sql = nil
	if  state ~= 0 then
		sql = string.format("INSERT OR IGNORE  INTO GROUPADDRBOOK VALUES('%s','%s','%s','%s','%s','%s','%s');", uid,phone,nickname,state,headpath,headurl,aid);
	else
		sql = string.format("INSERT OR IGNORE  INTO GROUPADDRBOOK VALUES('%s','%s','%s','%s','%s','%s','%s');", "",phone,"",state,"","","");
	end
	tiros.database.database_open()
	tiros.database.database_execSQL(sql)
	tiros.database.database_close()
end

local function Inserttime(time)
	local sql = string.format("INSERT OR IGNORE  INTO GROUPADDRBOOKTIME VALUES('%s');", time);
	tiros.database.database_open()
	tiros.database.database_execSQL(sql)
	tiros.database.database_close()
end

local function updatetime(time)
	local sql = string.format("UPDATE GROUPADDRBOOKTIME SET TIME = '%s' ;", time)
	tiros.database.database_open()
	tiros.database.database_execSQL(sql)
	tiros.database.database_close()
end

local function updateuser(uid,phone,nickname,state,aid)
	local sql = string.format("UPDATE GROUPADDRBOOK SET PHONE = '%s', NICKNAME ='%s',STATE ='%s',AID='%s' WHERE UID = '%s';", phone,nickname,state,aid,uid)
	tiros.database.database_open()
	tiros.database.database_execSQL(sql)
	tiros.database.database_close()
end

--查询个人信息
createmodule(gt,"dbqueryuserinfo", function(phone)
	if phone ~= nil and string.len(phone)> 0 then
		local sql = string.format("SELECT * \
			FROM GROUPADDRBOOK \
			WHERE GROUPADDRBOOK.PHONE = '%s' ;", phone)
		tiros.database.database_open()
		local users = tiros.database.database_Query(sql)
		tiros.database.database_close()
		local decodeT = tiros.json.decode(users)
		local result = nil		
		if decodeT ~= nil and type(decodeT) == "table" and decodeT[1] ~= nil  then
			result =  tiros.json.encode(decodeT[1])
		end
		return result;
	end
	return nil
end)

createmodule(gt,"dbqueryuserinfobyuid", function(uid)
	if uid ~= nil and string.len(uid)> 0 then
		local sql = string.format("SELECT * \
			FROM GROUPADDRBOOK \
			WHERE GROUPADDRBOOK.UID = '%s' ;", uid)
		tiros.database.database_open()
		local users = tiros.database.database_Query(sql)
		tiros.database.database_close()
		local decodeT = tiros.json.decode(users)
		local result = nil		
		if decodeT ~= nil and type(decodeT) == "table" and decodeT[1] ~= nil  then
			result =  tiros.json.encode(decodeT[1])
		end
		return result;
	end
	return nil
end)

--查询时间戳
createmodule(gt,"dbqueryedittime", function()	
	local sql = string.format("SELECT * FROM GROUPADDRBOOKTIME ;");
	tiros.database.database_open()
	local users = tiros.database.database_Query(sql)
	tiros.database.database_close()


	local decodeT = tiros.json.decode(users)
	local result = nil		
	if decodeT ~= nil and type(decodeT) == "table" and decodeT[1] ~= nil  then
		result =  tiros.json.encode(decodeT[1])
	end
	if result == nil then
		return ""
	end
	return result;
	
end)

--创建addressbook句柄函数接口
--ptype：string型参数，用于唯一标识该addressbook句柄
--输出：实际创建的addressbook句柄描述
local function AddressBookCreate(ptype)
	local taddressbook = getHandle(gtaddressbookweaklist, ptype);
	if taddressbook == nil then
		taddressbook = {};
		taddressbook.http = httpuploadlib.create();--创建httpupload句柄
		httpuploadlib.registernotify(taddressbook.http, "GroupbookUploadHttpEvent", ptype)
	end
	registerHandle(gtaddressbooklist, gtaddressbookweaklist, ptype, taddressbook);
	return taddressbook;	
end

--销毁addressbook句柄函数接口
--ptype为string型参数，用于唯一标识该addressbook句柄
--该函数并没有立即销毁addressbook句柄，而是等到下一个回收cd之后才会彻底销毁
--输出：无
local function AddressBookDestroy(ptype)
	local htable = getHandle(gtaddressbooklist, ptype);
	if htable ~= nil then
		httpuploadlib.cancel(htable.http);
	end
	releaseHandle(gtaddressbooklist, ptype)			
end

--添加headers接口
--htable:table型参数，用于标识addressbook句柄描述
--rtype: string型参数 "logic"或者"taxi",区分登录注册和出租车，通讯录上传的标识
--输出：无
local function AddHeaders(htable)
	local headersCount = tiros.nethttpheaders.httpheaderscount();
	for i = 1, headersCount do
		local gValues,gHeaders = tiros.nethttpheaders.httpgetheader(i);
		httpuploadlib.addheader(htable.http, gHeaders, tostring(gValues));
	end
	httpuploadlib.addheader(htable.http, "actionlocation", "/Tirosdatabase/UserFriendsPost4xServlet");--设置登录注册服务的头信息	
end

--保存头像地址到数据库
local function UpdataHeadPathToDatabase(uid,path,url)
	local sql = string.format("UPDATE GROUPADDRBOOK SET HEADPATH = '%s', HEADURL ='%s' WHERE UID = '%s'", path,url,uid)
	tiros.database.database_open()
	tiros.database.database_execSQL(sql)	
	tiros.database.database_close()
end

local function downgrouperheadimghttpevent(stype,dwEvent, dwParam1, dwParam2)
	--print("downgrouperheadimghttpevent-1")
	if stype == "groupbookuserimg" then
		if dwParam1 == 1 then
				
			local t = gGroupBookList.downimglist[1];			
			--修改
			--print("downgrouperheadimghttpevent-ok"..t.path)
			UpdataHeadPathToDatabase(t.uid,t.path,t.url)
			table.remove(gGroupBookList.downimglist,1)	
			local strold = tiros.moduledata.moduledata_get("web", "groupaddrbookdata")
			if strold ~= nil and strold ~= "" then
				local tempT = tiros.json.decode(strold);
				for k,v in pairs(tempT) do
					if tostring(v.UID) == tostring(t.uid) then
						v.HEADPATH = t.path
						break;
					end 
				end
				local strnew = tiros.json.encode(tempT);
				tiros.moduledata.moduledata_set("web", "groupaddrbookdata",strnew)
			end
			
			local tmp = {}
			tmp.path = t.path
			tmp.uid = t.uid
			local str = tiros.json.encode(tmp);
			sendmessagetoApp(EVT_MEET_HEADIMG_REPLACE,0,str)
			gRetry = 0;
		end
		if gRetry < 3 then
			gDownloading = false
			gRetry = gRetry + 1
			gt.checkstackdownimg()		
		end
	end
end

createmodule(gt, "checkstackdownimg",function()
	--print("checkstackdownimg-1")
	if gGroupBookList.downimglist ~= nil and gDownloading == false then
		local t = gGroupBookList.downimglist[1];
		if t ~= nil then
			gDownloading = true
			--print("checkstackdownimg-2")
			tiros.downloadimg.downloadimage("groupbookuserimg",t.url,t.path,downgrouperheadimghttpevent,nil)
		end
	end
end)

local function pushstackofdownimg(uid,url)
	--图片url不能为空
	if uid == nil or url == nil or uid == "" or url == "" then
		--print("pushstackofdownimg-1")
		return	
	end 
	local t = gt.GetUserinfo(uid)
	if t ~= nil then
		if tostring(t.HEADURL) == tostring(url) and tostring(t.HEADPATH) ~= gDefaultHeadImgPath then
			--print("pushstackofdownimg-2")
			return;
		end
	end
	if gGroupBookList.downimglist == nil then
		gGroupBookList.downimglist = {}
	end
	local t = {}
	--存储路径赋值
	local path = gAppHeadImgPath..uid..tiros.friendmanger.getfiletype(url)

	t.uid = uid
	t.path = path	
	t.url = url
	table.insert(gGroupBookList.downimglist, t)
	gt.checkstackdownimg();
	--print("pushstackofdownimg-ok")
end


--[[
{“success”:true,“data”:{},“msg”:””}
data:{
“mobileid”:“50”,
“recordupload”:[{},{},{}],
“recordupdate”:[{},{},{}],
“edittime”:”20130101”
}
--]]
local function jsonStrParser(str)
	if str ~= nil then
		local t = tiros.json.decode(str);
		gRetry = 0;
		if t.data ~=nil then
			if t.data.recordupload ~= nil then
				for k,v in pairs(t.data.recordupload) do
					--保存新用户
					InsertUser(v.uid,v.phone,v.ppname,v.state,gDefaultHeadImgPath,v.n_head_path,v.aid)
				
					pushstackofdownimg(v.uid,v.n_head_path)
				end
			end
			if t.data.recordupdate ~= nil then
				for k,v in pairs(t.data.recordupdate) do
					--更新旧用户
					updateuser(v.uid,v.phone,v.ppname,v.state,v.aid)
				
					pushstackofdownimg(v.uid,v.n_head_path)
					
				end
			end
			local time = gt.dbqueryedittime()
			if t.data.edittime ~= nil then
				if  time ~= "" then
					updatetime(t.data.edittime)
				else
					Inserttime(t.data.edittime)
				end
			end
		end
	end
end
--http回调
DeclareGlobal("GroupbookUploadHttpEvent", function(ptype, nEvent, param1, param2)
	local taddressbook = getHandle(gtaddressbooklist, ptype);
	if taddressbook == nil then
		sendmessagetoApp(EVT_SYNC_GROUPADDRBOOK,3,"")
		return;
	end
	if nEvent == 2 then
		taddressbook.rspdata = nil
		if param1 ~= 200 then	--不是200，都为http错误，param1返回错信息
			AddressBookDestroy("GROUPBOOK");
			sendmessagetoApp(EVT_SYNC_GROUPADDRBOOK,2,"")
		end
	elseif nEvent == 3 then
		if taddressbook.rspdata == nil then
			taddressbook.rspdata = string.sub(param2,1,param1);
		else
			taddressbook.rspdata = taddressbook.rspdata .. string.sub(param2,1,param1);
		end
	elseif nEvent == 4 then
		--处理应答提，json数据解析
		jsonStrParser(taddressbook.rspdata); 	
		local str = gt.addrbookupdate()
		sendmessagetoApp(EVT_SYNC_GROUPADDRBOOK,1, str)
		AddressBookDestroy("GROUPBOOK");
	elseif nEvent == 5 then
		AddressBookDestroy("GROUPBOOK");
		sendmessagetoApp(EVT_SYNC_GROUPADDRBOOK,0,param1)
	end
end)


--[[
 @brief 向服务器发送数据请求
 @param ptype string型参数，唯一标识符
 @param cbfunc 注册的回调函数地址
 @param nUser number型参数，可为nil，c端注册的调用者参数地址
 @return bool 成功返回true 否则false
--]]
createmodule(gt,"AddressBookRequest", function(ptype)	
	if ptype == nil then 
		return false;
	end
	local taddressbook = AddressBookCreate("GROUPBOOK")	
	if taddressbook == nil then	
	   	return false;
	end
	AddHeaders(taddressbook);

	if httpuploadlib.isbusy(taddressbook.http) == true then
		httpuploadlib.cancel(taddressbook.http);	--取消之前请求
	end
	local strtime = gt.dbqueryedittime()
	
	local postT = {}
	postT.method = "addressBook_new"
	if strtime ~= "" then
		local timeT = tiros.json.decode(strtime);
		postT.edittime = timeT.TIME
	else
		postT.edittime = ""
	end
	postT.data = {}

	for k,v in pairs(gGroupBookList.addrbooklist) do
		local temp = gt.dbqueryuserinfo(k)
		if temp == nil then
			local t = {}
			t.name = tostring(v)
			t.phone = tostring(k)
			
			table.insert(postT.data, t)
			
		end
	end

	local strData = tiros.json.encode(postT);
	local sURL = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_GENERAL_POST_URL);
	httpuploadlib.post(taddressbook.http, sURL, ContentType,1, strData, #strData);
end)

--[[
 @brief 对外声明终止通讯录上传POST请求接口
 @param ptype string型参数，用于标识该请求的唯一标识符
 @return 无
--]]
createmodule(gt, "addressbookabort", function()
	AddressBookDestroy("GROUPBOOK");
end)

createmodule(gt, "addrbookinit", function(t)
	if t ~= nil then
		--更新电话本
		gGroupBookList.addrbooklist = t;
		local datalist = {}
		for k,v in pairs(t) do
			local temp = {}
			temp.NAME = tostring(v)
			temp.PHONE = tostring(k)			
			temp.UID = ""
			temp.NICKNAME = ""
			temp.AID = ""
			temp.HEADPATH = ""
			temp.HEADURL = ""		
			temp.STATE = "0"			
			table.insert(datalist, temp)
		end
		local str = tiros.json.encode(datalist);
		tiros.moduledata.moduledata_set("web", "groupaddrbookdata",str)
			
		local bUpdate = false;	
		local curdate = GetDate()
		local filedata = tiros.file.Readfile(gFilePath);			
		if filedata ~= nil and filedata ~= "" then
			gGroupBookList.updatetime = tostring(filedata);
			if tostring(gGroupBookList.updatetime) ~= tostring(curdate) then
				bUpdate = true;
			end
		else
			bUpdate = true;
		end	
		--一天刷新一次
		if bUpdate then			
			gt.groupbook_tmrstart()
		end
		gt.addrbookupdate()	
	end
end)

--timer回调 处理消息
createmodule(gt,"groupbook_timerCB",function(handletype)
	gSelfReq = true;
	gt.AddressBookRequest("mybook")
end)

--启动消息处理定时器
createmodule(gt, "groupbook_tmrstart", function()			
	if tmrobj.timerisbusy("groupbook_upload") then
		tmrobj.timerabort("groupbook_upload")
	end
	tmrobj.timerstartforlua("groupbook_upload", 500, gt.groupbook_timerCB)
end)



createmodule(gt, "addrbookupdate", function()
	if gGroupBookList.addrbooklist ~= nil then
		--更新电话本
		local datalist = {}
		for k,v in pairs(gGroupBookList.addrbooklist) do
			local temp = {}
			temp.NAME = tostring(v)
			temp.PHONE = tostring(k)
			local str = gt.dbqueryuserinfo(temp.PHONE)
			if str ~= nil then	
				local userT = tiros.json.decode(str)
				temp.UID = tostring(userT.UID)
				temp.NICKNAME = tostring(userT.NICKNAME)
				temp.AID = tostring(userT.AID)
				temp.HEADPATH = tostring(userT.HEADPATH)
				temp.HEADURL = tostring(userT.HEADURL)
				temp.STATE = tostring(userT.STATE)
			else
				temp.STATE = "0"
			end
			table.insert(datalist, temp)
		end
		local str = tiros.json.encode(datalist);
		tiros.moduledata.moduledata_set("web", "groupaddrbookdata",str)
		local curdate = GetDate()
		gGroupBookList.updatetime = curdate		
		tiros.file.Writefile(gFilePath,curdate,true);		
		return str;
	end
	return ""
end)

createmodule(gt, "update", function()		
	gSelfReq = false;
	gt.AddressBookRequest("mybook")
end)

createmodule(gt, "queryusrinfo", function(phone)		
	local str = gt.dbqueryuserinfo(phone)
	if str == nil then
		return ""
	else
		local userT = tiros.json.decode(str)
		if gGroupBookList.addrbooklist ~= nil then
			for k,v in pairs(gGroupBookList.addrbooklist) do
				if  tostring(k) == tostring(phone) then		
					userT.NAME = tostring(v)	
					break;				
				end
			end
		end
		if userT.NAME == nil then
			userT.NAME = ""
		end			
		return tiros.json.encode(userT)
	end
end)

--2，保存个人信息
createmodule(gt,"SaveUserinfo",function(t)
	print("jiayufeng-------------------AID---NULL-----------------222222:" .. tostring(t.AID) .. "  " .. tostring(t.NICKNAME) .. "  " .. tostring(t.PHONE));	
	InsertUser(t.UID,t.PHONE,t.NICKNAME,"1",t.HEADPATH,t.HEADURL,t.AID)
end)
--3，获取个人信息
--UID AID NICKNAME PHONE HEADPATH HEADURL ADDBOOKNAME SEX
createmodule(gt,"GetUserinfo",function(uid)
	local str = gt.dbqueryuserinfobyuid(uid)	
	local t = {}
	if str ~= nil then
		t = tiros.json.decode(str)
	end
	return t;
end)

--4，更新个人信息
createmodule(gt,"UpdateUserinfo",function(t)	
	updateuser(t.UID,t.PHONE,t.NICKNAME,"1",t.AID)
	UpdataHeadPathToDatabase(t.UID,t.HEADPATH,t.HEADURL)
	local uid = tiros.moduledata.moduledata_get("framework", "uid"); 
	if uid == tostring(t.UID) then
		tiros.together.together_updateself(t);
	end
end)

tiros.groupbook  = readOnly(gt)

