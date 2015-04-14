--[[
 @描述：通讯录上传
 @编写人：fengfx
 @创建日期：2013-1-9 下午 18:00:00
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

local gt = {};
local gAddbookList = {}
local framework = getmodule("framework");
local httpEngine = getmodule("http");
local moduledataobj = getmodule("moduledata");
local gfriend = getmodule("friendmanger");

local gDefaultHeadImgPath = "fs0:/lua/together/mr.png";

--资源文件中编号
local RES_STR_GENERAL_POST_URL = 2102;

--资源文件地址路径
local RES_FILE_PATH = "fs0:/res/api/api.rs";

--获取URL:服务器地址及端口号  http://dev8.lbs8.com/general_Post
--local gURL = framework.getUrlFromResource(RES_FILE_PATH, RES_STR_GENERAL_POST_URL);
--gURL = "http://dev8.lbs8.com/general_Post"
--gURL = "http://dev.taxidog.cn/general_Post"
--gURL = "http://testx.lbs8.com/general_Post"
local ContentType = "application/x-www-form-urlencoded"

--上行协议数据，从数据仓库中获取并拼接
local gtUpLoad = {};

--[[
	@brief 通讯录数据表，结构和数据如下  服务器下发数据
示例:{“success”:true,“data”:{},“msg”:””}
}
--]]
createmodule(gt, "gtaddressbooklist",{})
--gtaddbookweaklist：全局变量,用于存放所有gtaddbooklist句柄的week表
createmodule(gt, "gtaddressbookweaklist",{})

setmetatable(gt.gtaddressbookweaklist,{__mode ="v" })

-------------------------------------------------------------
--[[
------taxi-----------
数据库表
ADDRBOOK（name，phone，flag）flag=0 需要邀请，flag=1 可以添加为好友
USERDETAIL（uid,name,phone）
BASEUID（UID，ADDTIME，DELTIME，ADDBOOKTIME）
REQ
取电话本，去掉数据库2个表中有的记录，加时间戳，上传给服务器
RSP
解析下行数据,对ADDRBOOK表增加删除记录，存储时间戳， UI界面显示ADDRBOOK表内容

----------login-------------
REQ
取电话本，去掉数据库好友表中有的记录，并在数据仓库中存储它，然后上传给服务器
RSP
解析下行数据,对好友表增加删除记录，UI界面显示通讯录内容
--]]

--保存时间戳到表BASEUID中
local function savetimetotable(uid,time)
	--判断BASEUID有没有对应的uid记录，有则更新，无则插入	
	local sql = string.format("SELECT * \
			FROM BASEUID \
			WHERE BASEUID.UID = '%s'", uid)
	local users = tiros.database.database_Query(sql)
	local T = tiros.json.decode(users)
	if T == nil or T.UID ~= uid then
		sql = string.format("INSERT OR IGNORE  INTO BASEUID\
		VALUES('%s','','','%s');", uid,time);		
	else
		sql = string.format("UPDATE BASEUID SET ADDBOOKTIME='%s' WHERE UID='%s';", time,uid);	
	end 	
	tiros.database.database_execSQL(sql)
end

--获取时间戳
local function gettimefromtable(uid)
	local sql = string.format("SELECT * \
			FROM BASEUID \
			WHERE BASEUID.UID = '%s'", uid)
	local users = tiros.database.database_Query(sql)

	local T = tiros.json.decode(users)

	if T ~= nil and T[1] ~= nil and type(T[1]) == "table" and T[1].ADDBOOKTIME ~= nil then
		return T[1].ADDBOOKTIME
	end
	return ""
end

--{"uid":,"phone":"","name":"","fname":,"head":,"fid":,"aid":,"state":2,"py":"z"}
--创建addbook表
local function CreateAddbookDB()
	local sql = "CREATE TABLE IF NOT EXISTS ADDRBOOK (UID varchar (24) NOT NULL, PHONE varchar (24) NOT NULL UNIQUE,NAME varchar (24) NOT NULL,FLAG varchar (24) NOT NULL,FUID varchar (24),FNAME varchar (24),HEAD varchar (24),AID varchar (24),PY varchar (24),UNIQUE(UID,PHONE));"
	tiros.database.database_execSQL(sql)
end

--get all data
local function GetAllAddbookFromDB(uid)
	local sql = string.format("SELECT* FROM ADDRBOOK WHERE ADDRBOOK.UID = '%s';",uid)
	local retS = tiros.database.database_Query(sql)
	return retS
end

--插入addbook表记录
local function InsertAddbookDB(uid,name,phone,flag,fuid,fname,head,aid,py)
	local sql = string.format("INSERT OR IGNORE  INTO ADDRBOOK VALUES('%s','%s','%s','%s','%s','%s','%s','%s','%s');", uid,phone,name,flag,fuid,fname,head,aid,py);	
	tiros.database.database_execSQL(sql)	
end

--更新addbook表记录
local function UpdateAddbookDB(uid,name,phone,flag,fuid,fname,head,aid,py)
	--判断指定手机号在addbook中是否存在
	local sql = string.format("SELECT * FROM ADDRBOOK WHERE ADDRBOOK.PHONE = '%s' AND ADDRBOOK.UID = '%s';",phone,uid);
	local retS = tiros.database.database_Query(sql)

	local retT = tiros.json.decode(retS)
	if retT ~= nil and retT[1] ~= nil and retT[1].UID ~= nil and string.len(retT[1].UID) > 0 then 
		sql = string.format("UPDATE ADDRBOOK SET NAME='%s',FLAG='%s',FUID='%s',FNAME='%s',HEAD='%s',AID='%s',PY='%s' WHERE PHONE='%s' AND UID='%s';",name,flag,fuid,fname,head,aid,py, phone,uid)			
		tiros.database.database_execSQL(sql)
		return true
	end
	
	InsertAddbookDB(uid,name,phone,flag,fuid,fname,head,aid,py)	
end

--删除addbook表记录
local function DeleteAddbookDB(phone)
	local sql = string.format("DELETE FROM ADDRBOOK WHERE PHONE = '%s'", phone)
	tiros.database.database_execSQL(sql)
end

--添加好友到数据库中
local function savefriendtotable(fuid,aid,nickname,phone,headpath,headurl,addbookname,sex)
	tiros.friendmanger.friend_db_add(fuid,aid,nickname,phone,headpath,headurl,addbookname,sex);
	tiros.friendmanger.friendheadimgupdate(fuid, headurl)	
end

--判断login数据库中是否有指定手机号
local function isexistinlogintable(uid,phone)
	--判断指定手机号在USERDETAIL中是否存在
	local sql = string.format("SELECT * FROM FRIEND WHERE FRIEND.UID = '%s' and FRIEND.FUID IN (SELECT UID FROM USERDETAIL WHERE USERDETAIL.PHONE ='%s')",uid,phone);
	local retS = tiros.database.database_Query(sql)
	local retT = tiros.json.decode(retS)

	if retT.FUID ~= nil and string.len(retT.FUID) > 0 then 
		return true
	end
	return false
end
--判断taxi数据库中是否有指定手机号
local function isexistintaxitable(uid,phone)
	--判断指定手机号在USERDETAIL中是否存在
	local sql = string.format("SELECT * FROM FRIEND WHERE FRIEND.UID = '%s' and FRIEND.FUID IN (SELECT UID FROM USERDETAIL WHERE USERDETAIL.PHONE ='%s')",uid,phone);
	local retS = tiros.database.database_Query(sql)

	local retT = tiros.json.decode(retS)
	
	if retT ~= nil and retT.FUID ~= nil and string.len(retT.FUID) > 0 then 		
		return true
	end
	--判断指定手机号在addbook中是否存在
	sql = string.format("SELECT * FROM ADDRBOOK WHERE ADDRBOOK.PHONE = '%s' AND ADDRBOOK.UID = '%s'",phone,uid);
	retS = tiros.database.database_Query(sql)

	retT = tiros.json.decode(retS)
	if retT ~= nil and retT.UID ~= nil and string.len(retT.UID) > 0 then 		
		return true
	end
	return false
end

--处理登录注册时响应数据
local function DealWithLoginRspData(uid, data)
	if data then		
		local t = tiros.json.decode(data);
--		local nNum = table.maxn(t);
--		for i = 1, nNum do
	    for k,v in pairs(t) do			
			savefriendtotable(v.fuid,v.aid,v.nickname,v.phone,gDefaultHeadImgPath,v.url, tiros.friendmanger.GetAddrBookName(v.phone),"")
	    end
--		end
	end
end

--uid---
local function AddressBookGetUid()
	local myuid = moduledataobj.moduledata_get("framework", "uid");
	if myuid == nil then
		myuid = "145358";--登录犬号
	elseif type(myuid) == "string" and string.len(myuid) == 0 then
		myuid = "145358";--登录犬号
	end
	return myuid;
end

--获取登录注册请求数据
local function GetLoginRequestData(uid)
	--取电话本，判断数据库好友表中是否存在tel，并分别存储它，然后上传给服务器
	local addrdata = moduledataobj.moduledata_get("web", "addressbookData")
	if addrdata == nil or string.len(addrdata)== 0 then
		return nil
	end

	local t = tiros.json.decode(addrdata);
	for k,v in pairs(t) do
		if v.phone ~= nil then
			if isexistinlogintable(uid,v.phone) then
				table.insert(gAddbookList.exist,v)
			else
				table.insert(gAddbookList.notexist,v)
			end
		end
	end
	local postdata = {}
	local str = nil
	postdata.source = tostring(gAddbookList.nsource);
	postdata.method = "addressBook";
	postdata.uid = AddressBookGetUid();
	postdata.data = gAddbookList.notexist
	if postdata.data ~= nil then
		str = "parameters="..tiros.json.encode(postdata);
	end
 	return str
end

--处理出租车下行数据
--data -- table
--{"uid":,"phone":"","name":"","fname":,"head":,"fid":,"aid":,"state":2,"py":"z"}
local function DealWithTaxiRspData(uid, data)
	if data and type(data) == "table" then		
		for k,v in pairs(data) do		
		    if v.uid ~= nil and v.phone ~= nil and v.state ~= nil and v.name ~= nil then
			if v.fuid ==nil then
				v.fuid =""			
			end
			if v.fname ==nil then
				v.fname =""			
			end
			if v.head ==nil then
				v.head =""			
			end
			if v.aid ==nil then
				v.aid =""			
			end
			if v.py ==nil then
				v.py =""			
			end			
		        UpdateAddbookDB(v.uid,v.name,v.phone,v.state,v.fuid,v.fname,v.head,v.aid,v.py)
		    end
		end		
	end
end

--获取出租车请求数据
local function GetTaxiRequestData(uid,data)
	--取电话本，判断数据库好友表中是否存在tel，并分别存储它，然后上传给服务器
--[[
res=｛data：[{name:张三,tell:13466525197},{name:李四,tell:13466525197}]
,uid:25，addtime：xxx，flag：1｝
--]]
	local addrdata = moduledataobj.moduledata_get("web", "addressbookData")
	if addrdata == nil or string.len(addrdata)== 0 then
		return nil
	end
	local t = tiros.json.decode(addrdata);
	for k,v in pairs(t) do
		if v.phone ~= nil then
			if isexistintaxitable(uid,v.phone) then
				table.insert(gAddbookList.exist,v)
			else
				table.insert(gAddbookList.notexist,v)
			end
		end
	end
	local postdata = {}
	local str = nil
	

	postdata.addtime = gettimefromtable(uid)
	if postdata.addtime == "" then
		postdata.flag = "0";
	else	
		postdata.flag = "1";
	end
	--postdata.method = "addressBook";
	postdata.uid = uid;
	postdata.data = gAddbookList.notexist
	if postdata.data ~= nil then
		str = "res="..tiros.json.encode(postdata);
	end
 	return str
end

--获取请求数据
local function GetRequestData(uid, nsource)
	gAddbookList.exist = {}
	gAddbookList.notexist = {}
	gAddbookList.nsource = nsource
--	httpEngine.httpabort(gtUpLoad, gAddbookList.htype);
	if gtUpLoad.project == "login" then
		return GetLoginRequestData(uid)
	elseif gtUpLoad.project == "taxi" then
		return GetTaxiRequestData(uid)
	end	
end
--处理响应数据
local function DealWithRspData(uid, data)
	if gtUpLoad.project == "login" then
		return DealWithLoginRspData(uid,data)
	elseif gtUpLoad.project == "taxi" then
		return DealWithTaxiRspData(uid,data)
	end	
end
-------------------------------------------------------------

--创建addressbook句柄函数接口
--ptype：string型参数，用于唯一标识该addressbook句柄
--输出：实际创建的addressbook句柄描述
local function AddressBookCreate(ptype)
	local taddressbook = getHandle(gt.gtaddressbookweaklist, ptype);
	if taddressbook == nil then
		taddressbook = {};
		taddressbook[0] = httpuploadlib.create();--创建httpupload句柄
	end
	registerHandle(gt.gtaddressbooklist, gt.gtaddressbookweaklist, ptype, taddressbook);
	return taddressbook;	
end

--销毁addressbook句柄函数接口
--ptype为string型参数，用于唯一标识该addressbook句柄
--该函数并没有立即销毁addressbook句柄，而是等到下一个回收cd之后才会彻底销毁
--输出：无
local function AddressBookDestroy(ptype)
	local htable = getHandle(gt.gtaddressbooklist, ptype);
	if htable ~= nil then
		httpuploadlib.cancel(htable[0]);
	end
	releaseHandle(gt.gtaddressbooklist, ptype)			
end

--注册addressbook回调函数接口
--htable:table型参数，用于标识addressbook句柄描述
--cbfunc:会动态依据不同的ntype来确定类型(lua：function型，js：string型，c：integer型)
--ptype:string型参数，js端用于标识该http句柄的唯一标识符
--ntype:integer型参数，用于标识该回调函数类型（lua：0，js：1，c：2）
--nuser:ntype为2时,c传入的调用者指针
--输出：无
local function AddressBookNotify(htable, cbfunc, ptype, ntype, nuser)
	htable[1] = ntype;
	if ntype == 0 then		--lua脚本注册回调函数
		htable[2] = cbfunc;
	elseif ntype == 1 then		--js注册回调函数
		htable[3] = cbfunc;
	else				--c回调函数地址
		htable[4] = cbfunc;
		htable[5] = nuser;
	end
	htable[6] = "";		--存储返回数据
	httpuploadlib.registernotify(htable[0], "HttpUpLoudEvent", ptype)--注册httpupload回调
end

--添加headers接口
--htable:table型参数，用于标识addressbook句柄描述
--rtype: string型参数 "logic"或者"taxi",区分登录注册和出租车，通讯录上传的标识
--输出：无
local function AddHeaders(htable, rtype)
	local headersCount = tiros.nethttpheaders.httpheaderscount();
	for i = 1, headersCount do
		local gValues,gHeaders = tiros.nethttpheaders.httpgetheader(i);
		httpuploadlib.addheader(htable[0], gHeaders, tostring(gValues));
	end

	if rtype == "login" then
		httpuploadlib.addheader(htable[0], "actionlocation", "/Tirosdatabase/UserFriendsPost4xServlet");--设置登录注册服务的头信息
	elseif rtype == "taxi" then
		httpuploadlib.addheader(htable[0], "actionlocation", "/navidog2Taxi/addressBook.htm");--设置出租车服务的头信息
	end
end

local function jsonStrParser_taxi(ptype, sjsonStr)
--[[
{"data":[{"uid":18388808,"phone":"18613824544","name":"zhanghua","fname":null,"head":null,"fid":null,"aid":null,"state":2,"py":"z"},{"uid":18388808,"phone":"13643232321","name":"xiaozhu","fname":null,"head":null,"fid":null,"aid":null,"state":2,"py":"x"},{"uid":18388808,"phone":"18743200321","name":"weijun1","fname":null,"head":null,"fid":null,"aid":null,"state":2,"py":"w"},{"uid":18388808,"phone":"15843245321","name":"wangcheng1","fname":null,"head":null,"fid":null,"aid":null,"state":2,"py":"w"},{"uid":18388808,"phone":"18732100123","name":"weijunjun","fname":null,"head":null,"fid":null,"aid":null,"state":2,"py":"w"},{"uid":18388808,"phone":"15812343333","name":"wangcheng","fname":null,"head":null,"fid":null,"aid":null,"state":2,"py":"w"}],"addtime":"20130204170318","success":true}
--]]
	if sjsonStr == nil then
		return 0
	end
	local dataT = tiros.json.decode(sjsonStr);
	if dataT.success == false then
		return 0
	end
	local uid =AddressBookGetUid()
	savetimetotable(uid,dataT.addtime)
	DealWithRspData(uid,dataT.data)
	local strData = GetAllAddbookFromDB(uid)
	tiros.moduledata.moduledata_set("web", "usertaxilist_ptp", strData);
	return 1,strData;
end
	 
local function jsonStrParser_login(ptype, sjsonStr)
	local tjsonObj = nil;
	local sJsonData = nil;
	if nil == sjsonStr then
		return 0;
	end
	tjsonObj = tiros.json.decode(sjsonStr);--解析json串
	if nil == tjsonObj or type(tjsonObj) ~= "table" then
		return 0;
	end

	--数据 {“success”:true/false, “data”:{}, “msg”:””}
	if false == tjsonObj.success then
		return 0;
	elseif true == tjsonObj.success then
		if tjsonObj.data == nil then
			return 1;
		end
		if tjsonObj.data.addyes ~= nil and table.maxn(tjsonObj.data.addyes) ~= 0 then
			DealWithRspData(0, tiros.json.encode(tjsonObj.data.addyes) );
			for i = 1, table.maxn(tjsonObj.data.addyes) do
				tjsonObj.data.addyes[i].name = tiros.friendmanger.GetAddrBookName(tjsonObj.data.addyes[i].phone)
				tjsonObj.data.addyes[i].url = nil;
				tjsonObj.data.addyes[i].photo = gDefaultHeadImgPath; --默认头像路径
			end
		end

		--用手机号匹配通讯录里的名字
		if tjsonObj.data.addno ~= nil and table.maxn(tjsonObj.data.addno) ~= 0 then
			for j = 1, table.maxn(tjsonObj.data.addno) do
				tjsonObj.data.addno[j].name = tiros.friendmanger.GetAddrBookName(tjsonObj.data.addno[j].phone)
			end
		end

		sJsonData = tiros.json.encode(tjsonObj.data);	--解析后的数据转为json		
		tiros.moduledata.moduledata_set("web", "userfriendlist_ptp", sJsonData);
		if tjsonObj.data.addtime ~= nil  then --保存时间戳到数据库
			local uid =AddressBookGetUid()
			gfriend.friend_update_addtime(uid, tjsonObj.data.addtime);
		end
		return 1;		
	end
end 

--[[
 解析返回json串，获取字符串数据
 @param ptype string型参数，唯一标识符
 @param sjsonStr json格式的字符串数据：完整的包体数据
 @return bool值 解析成功返回true，否则返回false
--]]
local function jsonStrParser(ptype, sjsonStr)
	if gtUpLoad.project == "login" then
		return jsonStrParser_login(ptype, sjsonStr)
	elseif gtUpLoad.project == "taxi" then
		return jsonStrParser_taxi(ptype, sjsonStr)
	end
end

--[[
 @brief 向服务器发送数据请求
 @param ptype string型参数，唯一标识符
 @param ntype number型参数，用于标识该回调函数类型（lua：0，js：1，c：2）
 @param cbfunc 注册的回调函数地址
 @param nsource number型，来源：1.注册时导入通讯录 2.添加时导入通讯录
 @param nUser number型参数，可为nil，c端注册的调用者参数地址
 @return bool 成功返回true 否则false
--]]
local function AddressBookRequest(ptype, ntype, rtype ,cbfunc, nsource, nUser)	
	if ptype == nil then 
		return false;
	end
	local taddressbook = AddressBookCreate(ptype)	
	if taddressbook == nil then	
	   	return false;
	end
	gtUpLoad.project = rtype; --区分登录注册和出租车，通讯录上传的标识
	AddressBookNotify(taddressbook, cbfunc, ptype, ntype, nUser);
	AddHeaders(taddressbook, rtype);

	local data = GetRequestData(AddressBookGetUid(), nsource);
	if data == nil then
		gt.addressbookSendMessage(taddressbook, ptype, 0, 0, nil);	
		return
	end
	if httpuploadlib.isbusy(taddressbook[0]) == true then
		httpuploadlib.cancel(taddressbook[0]);	--取消之前请求
	end
	local sURL = framework.getUrlFromResource(RES_FILE_PATH, RES_STR_GENERAL_POST_URL);
	httpuploadlib.data(taddressbook[0], sURL, ContentType, data, #data);
end

--[[
 @brief addressbookSendMessage 给调用者发送消息
 @param table型参数，用于标识addressbook句柄描述
 @param ptype string型参数，唯一标识符
 @param nStatus number型参数，标识当前网络、数据状态，0为异常，1为正常
 @param param1 当nStatus=0：错误类型；当nStatus=1：解析后的城市代码  
 @param param2 当nStatus=0：无内容；当nStatus=1：解析后的描述信息
 @return 无
--]]
createmodule(gt, "addressbookSendMessage", function (htable, ptype, nStatus, param1, param2)
	if htable ~= nil  then
		if nStatus == 1 then						--状态正确，正常发送数据
			if htable[1] == 0 then		--lua回调
				if htable[2] ~= nil then
					htable[2](ptype, nStatus, param1, param2);
				end
			elseif htable[1] == 1 then --js回调
				if htable[3] ~= nil then
					local sCallJS;
					sCallJS = string.format("%s('%s', %u, %u, '%s');", htable[3], ptype, nStatus, param1, param2);
					commlib.calljavascript(sCallJS);
				end
			else								--c回调
				if htable[4] ~= nil then
					commlib.universalnotifyFun(htable[4], ptype, htable[5], nStatus, param1, param2);
				end
			end
		else										--状态异常，发送错误信息
			if htable[1] == 0 then			--lua回调，发送标识和错误提示
				if htable[2] ~= nil then 
					htable[2](ptype, nStatus, param1, nil);
				end
			elseif htable[1] == 1 then		--js回调，发送标识和错误提示
				if htable[3] ~= nil then 
					local sCallJS;
					sCallJS = string.format("%s('%s', %u, %u, '%s');", htable[3], ptype, nStatus, param1, "");
					commlib.calljavascript(sCallJS);
				end
			else
				if htable[4] ~= nil then		--c回调，发送标识和错误提示
					commlib.universalnotifyFun(htable[4], ptype, htable[5], nStatus, param1, nil);
				end
			end
		end
	end
end)

DeclareGlobal("HttpUpLoudEvent", function(ptype, nEvent, param1, param2)
	local taddressbook = getHandle(gt.gtaddressbooklist, ptype);
	local nStatus = 0;  --标识当前网络、数据状态，0为异常，1为正常
	if nEvent == 2 then
		if param1 ~= 200 then	--不是200，都为http错误，param1返回错信息
			nStatus = 0;
			AddressBookDestroy(ptype);
			gt.addressbookSendMessage(taddressbook, ptype, nStatus, param1, nil);--http状态出错，错误信息回调发给调用者
		end
	elseif nEvent == 3 then
		taddressbook[6] = taddressbook[6] .. param2;
	elseif nEvent == 4 then
		--处理应答提，json数据解析
		local strdata = nil 
		nStatus, strdata=  jsonStrParser(ptype, taddressbook[6]); 			
		gt.addressbookSendMessage(taddressbook, ptype, nStatus, 1, strdata); --成功 发送 1,1 
		AddressBookDestroy(ptype);
	elseif nEvent == 5 then
		nStatus = 0;
		AddressBookDestroy(ptype);
		gt.addressbookSendMessage(taddressbook, ptype, nStatus, param1, nil);--http错误，错误信息回调发给调用者
	end
end)

--[[
 @brief addressbookuploadforc 对外声明c请求上传通讯录函数
 @param ptype string型参数，c端用于标识该请求的唯一标识符
 @param cbfunc number型参数，c端注册的回调函数地址
 @param nsource number型，来源：1.注册时导入通讯录 2.添加时导入通讯录
 @param nUser number型参数，可为nil，c端注册的调用者参数地址
 @return 请求成功返回true，失败返回false
--]]
createmodule(gt,"addressbookuploadforc", function(ptype, rtype, cbfunc, nsource, nUser)
	return AddressBookRequest(ptype, 2, rtype, cbfunc, nsource, nUser);
end)

createmodule(gt,"addressbookuploadforlua", function(ptype, rtype, cbfunc, nsource)
	return AddressBookRequest(ptype, 0, rtype, cbfunc, nsource, nil);
end)

createmodule(gt,"addressbookdatabase_init", function(ptype, rtype, cbfunc, nsource)
	CreateAddbookDB();
end)


--[[
 @brief 对外声明终止通讯录上传POST请求接口
 @param ptype string型参数，用于标识该请求的唯一标识符
 @return 无
--]]
createmodule(gt, "addressbookabort", function(ptype)
	AddressBookDestroy(ptype);
end)

tiros.addbook  = readOnly(gt)



