--[[
--@描述:好友管理
--（1）删除单个好友。
--（2）搜索手机号。
--（3）添加单个好友。
--（4）邀请好友发送短信
--（5）好友资料
--（6）好友列表
--（7）下载头像
--@编写人:王成	曹瑞敏
--@创建日期: 2013-01-09 15:56
--]]

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/framework/sys_framework"
require"lua/json"
require"lua/commfunc"
require"lua/http"
require"lua/moduledata"
require"lua/database"
require"lua/together"
require"lua/together/sys_groupbook"
--命名空间
local interface = {};

--全局变量
local gFriendList = {};
--头像下载
local gDownimglist ={} --{ fuid=url,fuid=url} -- 存储需要下载的头像
local gdownloadpath = "fs0:/friendhead/";       --头像存储目录

local gDefaultHeadImgPath = "fs0:/lua/together/mr.png"; --默认头像路径
local gimagename = ""; --头像名称
local gFile = nil;     --文件句柄
local gReCount = 0     --重试次数
local gDownImgIsBusy = false   --是否正在下载
local gHeadURL = nil;     --正在下载的头像url
local gFUID = nil;        --正在下载头像的人员uid
---通讯录
local gAddrBookDataTable = nil  


local ContentType = "Content-Type:application/x-www-form-urlencoded";
local HEADER_FRIENDLIST = "actionlocation:/Tirosdatabase/UserFriendsGet4xServlet"; -- 好友列表头
local moduledataobj = getmodule("moduledata");
local httpEngine = getmodule("http");


------------------资源----------------------------
local RES_STR_FRIEND_GET_URL = 2101; --资源文件中编号
local RES_STR_FRIEND_POST_URL = 2102; --资源文件中编号
local RES_FILE_PATH = "fs0:/res/api/api.rs"; --资源文件地址路径

--[[
资源ID：2101
资源URI：http://%s/general_Get
资源ID：2102
资源URI：http://%s/general_Post
资源ID：2103
资源URI：http://%s/general_Upload 
--]]

--2 邀请好友发送短信 王鹏
--local smsmessageurl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_GET_URL);
--local smsmessageurl = "http://dev8.lbs8.com/general_Get";

--3 好友列表 赵闯
--增量更新好友列表
--local friendupdateurl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_GET_URL);
--出租车
--local friendupdateurl = "http://192.168.1.94:8081/general_Get";
--登录
--local friendupdateurl = "http://dev8.lbs8.com/general_Get";

--5 好友资料 赵闯
--local frienddetailurl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_GET_URL);
--local frienddetailurl = "http://dev8.lbs8.com/general_Get";

--1 查找用户手机号 王鹏
--local friendqueryurl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_GET_URL);
--local friendqueryurl = "http://dev8.lbs8.com/general_Get";

--4 添加单个好友 赵闯
--local friendaddurl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_POST_URL);
--出租车
--friendaddurl = "http://192.168.1.94:8081/general_Post";
--登录
--local friendaddurl = "http://dev8.lbs8.com/general_Post";

--6 删除单个好友 赵闯
--local frienddeleteurl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_POST_URL);
--出租车
--local frienddeleteurl = "http://192.168.1.94:8081/general_Post";
--登录
--local frienddeleteurl = "http://dev8.lbs8.com/general_Post";

-----------------------------uid------------------------
local function friend_getuid()
	local myuid = moduledataobj.moduledata_get("framework", "uid");
	if myuid == nil then
		myuid = "145358";--登录犬号
	elseif type(myuid) == "string" and string.len(myuid) == 0 then
		myuid = "145358";--登录犬号	
	end
	return myuid;
end
---------------------------friendlist--------------------------------------
--添加记录到数据库表
local function InsertUserdetailTable(uid,aid,nickname,phone,headpath,headurl,addbookname,sex)
	local sql = string.format("INSERT OR IGNORE  INTO USERDETAIL VALUES('%s','%s','%s','%s','%s','%s','%s','%s');", uid,aid,nickname,phone,headpath,headurl,addbookname,sex);
	tiros.database.database_execSQL(sql)
end

local function InsertFriendTable(uid, fuid)
	local sql = string.format("INSERT OR IGNORE  INTO FRIEND VALUES('%s','%s');", uid,fuid);
	tiros.database.database_execSQL(sql)
end

local function InsertBaseuidTable(uid,addtime, deltime)
	local sql = string.format("INSERT OR IGNORE  INTO BASEUID VALUES('%s','%s','%s','');", uid,addtime, deltime);
	tiros.database.database_execSQL(sql)
end

--删除记录从数据库表
local function deleteUserdetailItem(uid)
	local sql = string.format("DELETE FROM USERDETAIL\
	WHERE UID = '%s'", uid)
	tiros.database.database_execSQL(sql)
end

local function deleteFriendItem(uid,fuid)
	local sql = string.format("DELETE FROM FRIEND\
	WHERE FUID = '%s' AND UID = '%s'", fuid,uid)
	tiros.database.database_execSQL(sql)
end

local function deleteBaseuidItem(uid)
	local sql = string.format("DELETE FROM BASEUID\
	WHERE UID = '%s'", uid)
	tiros.database.database_execSQL(sql)
end


--保存好友头像地址到数据库
local function UpdataHeadPathToDatabase(fuid,path,url)
	local sql = string.format("UPDATE USERDETAIL SET HEADPATH = '%s', HEADURL ='%s' WHERE UID = '%s'", path,url,fuid)
	tiros.database.database_execSQL(sql)	
end

--查询好友信息
createmodule(interface,"friend_dbquery_friendinfo", function(fuid)
	if fuid ~= nil and string.len(fuid)> 0 then
		local sql = string.format("SELECT * \
			FROM USERDETAIL \
			WHERE USERDETAIL.UID = '%s'", fuid)
		local users = tiros.database.database_Query(sql)
		
		local decodeT = tiros.json.decode(users)
		local result = nil		
		if decodeT ~= nil and type(decodeT) == "table" and decodeT[1] ~= nil  then
			result =  tiros.json.encode(decodeT[1])
		end

		return result;
	end
	return nil
end)

--创建数据库表
createmodule(interface,  "createfriendtable", function()
	--USERDETAIL[UID,AID,NICKNAME,PHONE,HEADPATH,HEADURL]
	local strDetailSql = "CREATE TABLE IF NOT EXISTS USERDETAIL (UID varchar (24) NOT NULL UNIQUE, \
							AID varchar (24) ,\
							NICKNAME varchar (128), \
							PHONE varchar (11) ,\
							HEADPATH varchar (128),\
							HEADURL varchar (256), ADDBOOKNAME varchar (128), SEX varchar (8) );"

	tiros.database.database_execSQL(strDetailSql)

	--FRIEND[UID,FUID]
	local strFriendSql = "CREATE TABLE IF NOT EXISTS FRIEND (UID CHAR (24) NOT NULL, FUID CHAR (24) NOT NULL, UNIQUE(UID,FUID));"
	tiros.database.database_execSQL(strFriendSql)

	--BASEUID[UID,ADDTIME,DELTIME]
	local strBaseSql = "CREATE TABLE IF NOT EXISTS BASEUID (UID CHAR (24) NOT NULL, ADDTIME CHAR (24),DELTIME CHAR (24),ADDBOOKTIME CHAR (24),PRIMARY KEY(UID)) ;"
	tiros.database.database_execSQL(strBaseSql)
end)

--删除数据仓库中好友数据
createmodule(interface, "friend_md_delete", function(fuid)
	local jsonData = moduledataobj.moduledata_get("web", "userfriendlist_ptp");
	if jsonData == nil or #jsonData == 0 then
		return false;
	end
	local tData = tiros.json.decode(jsonData);
	if nil == tData or type(tData) ~= "table" then
		return false;
	end
	if tData.addyes ~= nil and table.maxn(tData.addyes) ~= 0 then
		for k,v in pairs(tData.addyes) do
			if v.fuid == fuid then
				table.remove(tData.addyes, k);
				return true;
			end
		end
	end
	local strData = tiros.json.encode(tData);
	moduledataobj.moduledata_set("web", "userfriendlist_ptp", strData);
end)

--本地删除一个好友
createmodule(interface,"friend_db_delete", function(fuid)
	local uid = friend_getuid();
	if uid ~= nil then
		deleteFriendItem(uid,fuid)
		--deleteUserdetailItem(fuid)
	end
end)

--本地添加一个好友
createmodule(interface,"friend_db_add", function(fuid,aid,nickname,phone,headpath,headurl,addbookname,sex)
	local guid = friend_getuid();
	if guid ~= nil then
		local str = interface.friend_dbquery_friendinfo(fuid)
		if str == nil then
			InsertUserdetailTable(fuid,aid,nickname,phone,headpath,headurl,addbookname,sex)
		else
			interface.friend_updatedetail(fuid,aid,nickname,phone,addbookname,sex)
		end
		InsertFriendTable(guid,fuid)
	end
end)

--更新详情
createmodule(interface,"friend_updatedetail", function(fuid,aid,nickname,phone,addbookname,sex)
	local sql = string.format("UPDATE USERDETAIL SET AID='%s',NICKNAME='%s',PHONE='%s',ADDBOOKNAME='%s',SEX='%s' WHERE UID='%s';",aid,nickname,phone,addbookname,sex,fuid)
	tiros.database.database_execSQL(sql)
end)

--修改添加好友时间戳
--... ADDTIME:20130104,DELTIME:20130104
createmodule(interface,"friend_update_addtime", function(uid,addtime)
	local sql = nil
	if uid and addtime then	
		sql = string.format("UPDATE BASEUID SET ADDTIME='%s' WHERE UID='%s'; ",addtime , uid )	
		
		tiros.database.database_execSQL(sql)
	end
end)

--更新删除好友时间戳
createmodule(interface,"friend_update_deltime", function(uid, deltime)
	local sql = nil
	if uid and deltime then	
		sql = string.format("UPDATE BASEUID SET DELTIME='%s' WHERE UID='%s'; ",deltime , uid )
		
		tiros.database.database_execSQL(sql)
	end
end)

--初始化添加uid对应的时间戳记录
createmodule(interface,"friend_init_adddeltime", function(uid)
	InsertBaseuidTable(uid,"", "")	
end)

--查询时间戳
--返回值为 addtime，deltime
createmodule(interface,"friend_query_adddeltime", function(uid)
	if uid == nil or uid == "" then
		return nil,nil
	end
	local sql = string.format("SELECT * \
			FROM BASEUID \
			WHERE BASEUID.UID == '%s'", uid)
	local users = tiros.database.database_Query(sql)
	local T = tiros.json.decode(users)
	if T ~=  nil and type(T) == "table" and T[1] ~= nil and type(T[1]) == "table" then	
		return T[1].ADDTIME,T[1].DELTIME	
	end
	return nil,nil
end)

--获取本地全部好友列表
createmodule(interface,"friendlist_get", function()
	local guid = friend_getuid();
	if guid == nil then
		return nil
	end
	
	local friendlist = {}
	friendlist.data = {}
	local sql = string.format("SELECT * FROM USERDETAIL WHERE USERDETAIL.UID IN (SELECT FUID FROM FRIEND WHERE FRIEND.UID = '%s');", guid)
	
	local users = tiros.database.database_Query(sql)
	local T = tiros.json.decode(users)
	if T == nil then 
		return nil
	else	
		for k,v in pairs(T) do
			v.ADDBOOKNAME = interface.GetAddrBookName(v.PHONE)
			table.insert(friendlist.data, v)
		end
		
		local result = tiros.json.encode(friendlist)
		--{"data":[{"HEADURL":"HEADURL","AID":"AID","HEADPATH":"HEADPATH","NICKNAME":"NICKNAME","PHONE":"PHONE","UID":"UID","ADDBOOKNAME":"add"}]}
		return result	
	end
end)
-------------------------------------

--[[
--@描述:返回一个字符串再另一个字符串中最后的位置
--@param  word string型参数 想要查找的字符串
--@return word在all中的位置
--]]
local function getLastWord(all, word)
	local b = 0;
	local last = nil;
	while true do
		local s,e = string.find(all, word, b); -- find 'next' word
		if s == nil then
			break;
		else
			last = s;
		end
			b = s + string.len(word);
	end
	
	return last;
end

--[[
--@描述:新建文件夹
--@param  fname string型参数 想要创建的文件夹名字
--@return 成功返回true，失败返回false
--]]
local function fmkdir(fname)
	if(fname == nil)then
		return false;
	end

	local bmkdir = filelib.fmkdir(fname);
	if(bmkdir)then
		return true;
	else
		return false;
	end
end

--[[
--@描述:创建文件句柄
--@param fname string型参数 文件名
--@return 成功返回文件句柄，失败返回nil
--]]
local function createFilebyName(fname)
	if (fname == nil) then
		return nil;
	end
	local bExist = filelib.fexist(fname);
	local file;
	if (bExist) then
		filelib.fremove(fname);
	end
	--截取目录名
	local End = getLastWord(fname, "/");
	if(End ~= nil)then
		local Str = string.sub(fname, 0, End);
		local bfmkdir = fmkdir(Str);
	end	
	file = filelib.fopen(fname, 3);	
	return file;
end

--[[
--@描述:文件写入(支持二进制数据)
--@param file 文件句柄
--@param data 写入的数据
--@param size 数据大小
--@param bturncate bool型 false为直接追加写,true为清空内容重写
--@return 成功返回true,失败返回false
--]]
local function fileWrite(file, data, size, bturncate)

	if file == nil or data == nil then
		return false;
	end

	if bturncate == true then
		filelib.fchsize(file, 0);
	end

	filelib.fseek(file, 1, 0);
	
	local WriteSize = filelib.fwrite(file, data, size);

	if(size == WriteSize) then
		return true;
	else
		return false;
	end
end

--发送消息
local function sendmessage(stype, dwEvent, dwParam1, dwParam2, func, user)
	--print("sendmessage",stype, dwEvent, dwParam1, dwParam2)
	
	if func ==nil and user ==nil then		
	elseif user then
		--call c func
		--print("lksendmessage----c")
		commlib.universalnotifyFun(func,stype,user,dwEvent, dwParam1, dwParam2);
	elseif type(func) == "string" then
		--JS
		--print("lksendmessage----JS")
		local sCallJS;
		sCallJS = string.format("%s('%s', %u, %u, '%s');", func, stype,dwEvent, dwParam1, dwParam2);
		commlib.calljavascript(sCallJS);			
	else
		--lua
		--print("lksendmessage----LUA")
		func(stype,dwEvent, dwParam1, dwParam2);
	end
end

local function getnotify()
	if gFriendList.cblist == nil then
		return nil;
	end
	--print("gFriendList.stype",gFriendList.stype)
	for k,v in pairs(gFriendList.cblist) do
		if type(v)== "table" then
			if v.stype == gFriendList.stype then
				return v;
			end
		end
	end
	return nil;
end


--注册回调函数
createmodule(interface,"friendlist_notify", function(stype, notify, user)
	local T = {func = notify, user = user , stype = stype}
	--print("friendlist_notify",stype, notify, user);
	if gFriendList.cblist == nil then
		gFriendList.cblist = {};
	end	
	for k,v in pairs(gFriendList.cblist) do
		if type(v)== "table" then
			if v.stype == stype then
				v.func = notify;
				v.user = user;
				return
			end
		end
	end
	
	table.insert(gFriendList.cblist,T);
end)

-- 取消注册回调函数
createmodule(interface,"friendlist_notifycancel", function(stype)		
	if gFriendList.cblist == nil then
		return
	end
	for k,v in pairs(gFriendList.cblist) do
		if type(v)== "table" then
			if v.stype == stype then
				table.remove(gFriendList.cblist, k);
				return
			end
		end
	end	
end)

createmodule(interface, "InitLocalAddrBook", function()	
	local strAddbookdata = moduledataobj.moduledata_get("web", "addressbookData");
	if strAddbookdata == nil then
		return;
	end
	--print("ccccInitLocalAddrBook="..strAddbookdata)
	local t = tiros.json.decode(strAddbookdata);
	if t ~= nil and type(t) == "table" then
		gAddrBookDataTable = {}
		for k,v in pairs(t) do 
			if v.phone ~= nil then
				gAddrBookDataTable[v.phone] = v.name
			end
		end
	end
	tiros.groupbook.addrbookinit(gAddrBookDataTable);
end)

createmodule(interface, "GetAddrBookName", function(phone)
	--print("ccccGetAddrBookName"..phone)
	local result = nil	
	if phone ~= nil and gAddrBookDataTable ~= nil then
		result = gAddrBookDataTable[phone]
		
	end
	
	if result == nil then
		return ""	
	else
		--print("ccccGetAddrBookName="..result)
		return result
	end
end)



--将下载的url添加到队列缓存
local function downloadimglistappend(fuid, url)
	if fuid and url then
		gDownimglist[fuid] = url
	end	
end
--移除队列中已经下载完的uid
local function downloadimglistremove(fuid)
	if fuid then
		gDownimglist[fuid] = nil
	end	
end
--检查是否有等待下载的uid，有则下载
local function downloadimglistcheck()
	gDownImgIsBusy = false
	for k,v in pairs(gDownimglist) do
		if k and v then			
			return interface.friendheadimgupdate(k,v)
		end
	end
end


-------------------------------------Http请求回调----------------------------------------------
local function requestCallback(htype, event, param1, param2)
	local t = getnotify();
	
	if htype == gFriendList.htype then
		if event == 1 then --请求
		elseif event == 2 then --应答
			if param1 ~= 200  and t ~= nil then	
				tiros.http.httpabort(gFriendList.stype);
				sendmessage(htype, 0, 0, param2, t.func, t.user);
			end	
		elseif event == 3 then --数据体SUCCESS
			if gFriendList.rspdata == nil then
				gFriendList.rspdata = string.sub(param2,1,param1);
			else
				gFriendList.rspdata = gFriendList.rspdata..string.sub(param2,1,param1);
			end			
		elseif event == 4 then --完成			
			local decodeT = tiros.json.decode(gFriendList.rspdata);
			if decodeT ~= nil and t ~= nil then	
				if decodeT.success == true then
					local result = tiros.json.encode(decodeT.data);
					
					sendmessage(htype, 1, 1, result, t.func, t.user);
				else
					sendmessage(htype, 2, 0, decodeT.msg, t.func, t.user);
				end
			end
			gFriendList.rspdata = nil;
		elseif event == 5 then --错误
			if t ~= nil then
				sendmessage(htype, 0, 0, param2, t.func, t.user);
			end
			gFriendList.rspdata = nil;
		end
	end
end

--[[
--@描述:图片下载的http回调函数
--@param  ptype 回调对象句柄
--@param  event 回调事件类型
--@param  param1 回调事件传递参数1
--@param  param2 回调事件传递参数2
--@return 无
--]]
createmodule(interface,"downLoadImageHttpEvent", function(htype,event,param1,param2)
	if htype ~= "downloadheadimg" then
		return
	end

	if event == 1 then
	elseif event == 2 then
		if param1 == 200 then
			if htype ~= nil then
				gFile = createFilebyName(gdownloadpath .. gimagename);
			end
		else
			tiros.http.httpabort(htype);
			--1 继续下载头像 gReCount 0-2
			gReCount = gReCount + 1
			if gReCount < 3 then				
				interface.downloadheadimage(gHeadURL);
			else
				downloadimglistcheck()
			end
		end	
	elseif event == 3 then
		if gFile ~= nil and param1 > 0 and param2 ~= nil then
			fileWrite(gFile, param2, param1, false);
		end
	elseif event == 4 then
		if gFile ~= nil then			
			filelib.fclose(gFile);
			gFile = nil;
			--1 更新本地数据库
			UpdataHeadPathToDatabase(gFUID, gdownloadpath..gimagename, gHeadURL)
			local t = getnotify();
			--2 回调给上层 JS/C/LUA 		
			local sql = string.format("SELECT * \
				FROM USERDETAIL \
				WHERE USERDETAIL.UID = '%s'", gFUID)
			local users = tiros.database.database_Query(sql)
			local decodeT = tiros.json.decode(users)
			local result = nil		
			if decodeT ~= nil and type(decodeT) == "table" and decodeT[1] ~= nil then
				result =  tiros.json.encode(decodeT[1])
			end
			sendmessage(htype, 1, 0, result, t.func, t.user);			
		end	
		--3 继续下载头像				
		downloadimglistcheck()
		
	elseif event == 5 then
		if gFile ~= nil then
			filelib.fclose(gFile);
			filelib.fremove(gdownloadpath .. gimagename);
			gFile = nil;
		end		
		--1 继续下载头像
		gReCount = gReCount + 1		
		if gReCount < 3 then
			interface.downloadheadimage(gHeadURL);
		else
			downloadimglistcheck()
		end
	end
end)

--好友列表增量更新回调
createmodule(interface,"flist_update_httpevent", function(htype,event,param1,param2)
	local t = getnotify();
	if htype == gFriendList.htype then
		if event == 1 then --请求			
		elseif event == 2 then --应答
			if param1 ~= 200  and t ~= nil then	
				tiros.http.httpabort(gFriendList.stype);
				sendmessage(htype, 0, param1, param2, t.func, t.user);
			end	
		elseif event == 3 then --数据体
			if gFriendList.rspdata == nil then
				gFriendList.rspdata = string.sub(param2,1,param1);
			else
				gFriendList.rspdata = gFriendList.rspdata..string.sub(param2,1,param1);
			end	
			--[[
				{"data":
				{"addtime":"2013-01-07 00:00:00.0",
				"newfriends":[{"aid":"","fuid":"281821","nickname":"手机用户535","phone":"13318725755","url":"http://192.168.1.234/files/headpic/gs.gif"},
								{"aid":"","fuid":"87","nickname":"suntest87","phone":"","url":"http://192.168.1.234/files/headpic/avatar-01.gif"}],
				"deltime":"20130107000000.0",
				"delfriends":[{"fuid":88}]},"msg":"","success":true
				}
			--]]
		elseif event == 4 then --完成
			local decodeT = tiros.json.decode(gFriendList.rspdata);
			--[[
				{"newfriends":[{"aid":"","fuid":"281821","nickname":"手机用户535","phone":"13318725755","url":"http://192.168.1.234/files/headpic/gs.gif"},
						{"aid":"","fuid":"87","nickname":"suntest87","phone":"","url":"http://192.168.1.234/files/headpic/avatar-01.gif"}],
				"delfriends":[{"fuid":88}]
				}
			--]]
			--更新本地的数据库
			if decodeT ~= nil and decodeT.data ~= nil then
				local guid = friend_getuid();
				if guid ~= nil then
					interface.friend_update_addtime(guid, decodeT.data.addtime);
					interface.friend_update_deltime(guid, decodeT.data.deltime);
					
					for k,v in pairs(decodeT.data.newfriends) do
						--本地添加一个好友
						v.addrname = interface.GetAddrBookName(v.phone)
						local sex = v.sex
						if sex == nil then
							sex =""
						end

						interface.friend_db_add(v.fuid,v.aid,v.nickname,v.phone,gDefaultHeadImgPath,v.url,v.addrname,sex);
						--更新头像
						interface.friendheadimgupdate(v.fuid,v.url);

					end
				
					if decodeT ~= nil and decodeT.data ~=nil and decodeT.data.delfriends ~=nil then
						for k,v in pairs(decodeT.data.delfriends) do
							--本地删除一个好友

							interface.friend_db_delete(v.fuid);
						end
					end					
				end
			end	

			if decodeT ~= nil and decodeT.data ~= nil and t ~= nil then	
				if decodeT.success == true then
					local temp = {};
					temp.newfriends = decodeT.data.newfriends;
					temp.delfriends = decodeT.data.delfriends;
					local result = tiros.json.encode(temp);
					--print("result++++++++++",result);
					
					sendmessage(htype, 1, param1, result, t.func, t.user);
				else
					sendmessage(htype, 2, param1, decodeT.msg, t.func, t.user);
				end
			end
			
			gFriendList.rspdata = nil;
			
			
		elseif event == 5 then --错误
			if t ~= nil then
				sendmessage(htype, 0, param1, param2, t.func, t.user);
			end
			gFriendList.rspdata = nil;
		end
	end
end)

--好友资料查询回调
createmodule(interface,"flist_finfo_httpevent", function(htype,event,param1,param2)
	
	--[[
		{"data":{"uid":"50","phone":"12222222222","nickname":"王鹏","fuid":"50","url":"http://192.168.1.234/files/headpic/avatar-01.gif"},
			"msg":"","success":true}
	--]]
	
	local t = getnotify();
	
	if htype == gFriendList.htype then
		if event == 1 then --请求
		elseif event == 2 then --应答
			if param1 ~= 200  and t ~= nil then	
				tiros.http.httpabort(gFriendList.stype);
				sendmessage(htype, 0, 0, param2, t.func, t.user);
			end	
		elseif event == 3 then --数据体SUCCESS
			if gFriendList.rspdata == nil then
				gFriendList.rspdata = string.sub(param2,1,param1);
			else
				gFriendList.rspdata = gFriendList.rspdata..string.sub(param2,1,param1);
			end			
		elseif event == 4 then --完成			
			local decodeT = tiros.json.decode(gFriendList.rspdata);
			if decodeT ~= nil and t ~= nil then	
				if decodeT.success == true then					
					if decodeT.data ~= nil then
						
						--fuid,aid,nickname,phone,headpath,headurl
						local fuid = decodeT.data.fuid;
						local aid = decodeT.data.aid;
						local nickname =  decodeT.data.nickname;
						local phone = decodeT.data.phone;
						local headurl = decodeT.data.url;
						decodeT.data.addrname = interface.GetAddrBookName(phone)
						local sex = decodeT.data.sex
						if sex == nil then
							sex = ""
						end
						--更新本地的数据库
						interface.friend_updatedetail(fuid,aid,nickname, phone,decodeT.data.addrname,sex);
		
						--下载头像
						if headurl ~= nil and headurl ~= "" then
							interface.friendheadimgupdate(fuid, headurl)
						end
					end
					local result = tiros.json.encode(decodeT.data);
					
					sendmessage(htype, 1, 1, result, t.func, t.user);
				else
					sendmessage(htype, 2, 0, decodeT.msg, t.func, t.user);
				end
			end
			gFriendList.rspdata = nil;
		elseif event == 5 then --错误
			if t ~= nil then
				sendmessage(htype, 0, 0, param2, t.func, t.user);
			end
			gFriendList.rspdata = nil;
		end
	end
	
	
end)

--好友邀请发送短信回调
createmodule(interface,"friendlist_sms_httpevent", function(htype,event,param1,param2)
	--print("--------------friendlist_sms_httpevent--------------");	
	requestCallback(htype, event, param1, param2);
end)

--[[
	查询手机号回调
	格式：{“result”，“1”，“uid”，“50”，“phone”，“15187654321”,“pname”，“英雄”,“headPath”,“http://”},“msg”:“”}
	result：查询结果（1.已注册，未加好友 2.已注册，已加好友 3.未注册）
	uid：犬号
	phone：手机号
	pname：昵称
	headPath：用户头像路径
--]]
createmodule(interface,"queryPhoneHttpEvent", function(htype, event, param1,param2)
	
	--{“result”，“1”，“uid”，“50”，“phone”，“15187654321”,“pname”，“英雄”,“headPath”,“http://”},“msg”:“”}	
	local t = getnotify();
	
	if htype == gFriendList.htype then
		if event == 1 then --请求
		elseif event == 2 then --应答
			if param1 ~= 200  and t ~= nil then	
				tiros.http.httpabort(gFriendList.stype);
				sendmessage(htype, 0, 0, param2, t.func, t.user);
			end	
		elseif event == 3 then --数据体SUCCESS
			if gFriendList.rspdata == nil then
				gFriendList.rspdata = string.sub(param2,1,param1);
			else
				gFriendList.rspdata = gFriendList.rspdata..string.sub(param2,1,param1);
			end			
		elseif event == 4 then --完成			
			local decodeT = tiros.json.decode(gFriendList.rspdata);
			if decodeT ~= nil and t ~= nil then	
				if decodeT.success == true then
					decodeT.data.addrname = interface.GetAddrBookName(decodeT.data.phone)
					local result = tiros.json.encode(decodeT.data);
					--print("result++++++++++++",result);					
					sendmessage(htype, 1, 1, result, t.func, t.user);
				else
					sendmessage(htype, 2, 0, decodeT.msg, t.func, t.user);
				end
			end
			gFriendList.rspdata = nil;
		elseif event == 5 then --错误
			if t ~= nil then
				sendmessage(htype, 0, 0, param2, t.func, t.user);
			end
			gFriendList.rspdata = nil;
		end
	end
end)

--添加好友回调
createmodule(interface,"addFriendHttpEvent", function(htype,event,param1,param2)
		
	--{"data":{"uid":"50","addtime":"2013011417:32:14","phone":"","nickname":"dog90test","aid":"111010156","fuid":"90","url":""},"msg":"","success":true}
	
	local t = getnotify();
	
	if htype == gFriendList.htype then
		if event == 1 then --请求
		elseif event == 2 then --应答
			if param1 ~= 200  and t ~= nil then	
				tiros.http.httpabort(gFriendList.stype);
				sendmessage(htype, 0, 0, param2, t.func, t.user);
			end	
		elseif event == 3 then --数据体SUCCESS
			if gFriendList.rspdata == nil then
				gFriendList.rspdata = string.sub(param2,1,param1);
			else
				gFriendList.rspdata = gFriendList.rspdata..string.sub(param2,1,param1);
			end			
		elseif event == 4 then --完成			
			local decodeT = tiros.json.decode(gFriendList.rspdata);
			if decodeT ~= nil and t ~= nil then	
				if decodeT.success == true then
					--本地添加好友:fuid,aid,nickname,phone,headpath,headurl
					if decodeT.data ~= nil then
						local guid = friend_getuid();
						if guid ~= nil then
							local fuid = decodeT.data.fuid;
							local aid = decodeT.data.aid;
							local nickname = decodeT.data.nickname;
							local phone = decodeT.data.phone;
							local headurl = decodeT.data.url;
							decodeT.data.addrname = interface.GetAddrBookName(phone)
							local sex = decodeT.data.sex
							if sex == nil then
								sex =""
							end
							--添加好友到数据库中，更新时间戳，下载头像
							interface.friend_db_add(fuid,aid,nickname,phone,gDefaultHeadImgPath,headurl,decodeT.data.addrname,sex);
							interface.friend_update_addtime(guid, decodeT.data.addtime);
							interface.friendheadimgupdate(fuid, headurl);
						else
							return
						end
					end	

					local result = tiros.json.encode(decodeT.data);	
					sendmessage(htype, 1, 1, result, t.func, t.user);
				else
					sendmessage(htype, 2, 0, decodeT.msg, t.func, t.user);
				end
			end
			gFriendList.rspdata = nil;
		elseif event == 5 then --错误
			if t ~= nil then
				sendmessage(htype, 0, 0, param2, t.func, t.user);
			end
			gFriendList.rspdata = nil;
		end
	end
	

end)

--删除好友回调
createmodule(interface,"deleteFriendHttpEvent", function(htype,event,param1,param2)
		
	--{"data":{"uid":"50","fuid":"90","deltime":"2013011417:31:46"},"msg":"","success":true}
	
	local t = getnotify();
	
	if htype == gFriendList.htype then
		if event == 1 then --请求
		elseif event == 2 then --应答
			if param1 ~= 200  and t ~= nil then	
				tiros.http.httpabort(gFriendList.stype);
				sendmessage(htype, 0, 0, param2, t.func, t.user);
			end	
		elseif event == 3 then --数据体SUCCESS
			if gFriendList.rspdata == nil then
				gFriendList.rspdata = string.sub(param2,1,param1);
			else
				gFriendList.rspdata = gFriendList.rspdata..string.sub(param2,1,param1);
			end			
		elseif event == 4 then --完成			
			local decodeT = tiros.json.decode(gFriendList.rspdata);
			if decodeT ~= nil and t ~= nil then	
				if decodeT.success == true then
					local result = tiros.json.encode(decodeT.data);			
					
					if decodeT.data ~= nil then
						local fuid = decodeT.data.fuid;
						--本地数据库删除好友 fuid
						interface.friend_db_delete(fuid);
						interface.friend_md_delete(fuid);
						local guid = friend_getuid();
						if guid == nil then
							return
						end
						--更新删除好友时间戳
						interface.friend_update_deltime(guid, decodeT.data.deltime);
					end	
					sendmessage(htype, 1, 1, result, t.func, t.user);
				else
					sendmessage(htype, 2, 0, decodeT.msg, t.func, t.user);
				end
			end
			gFriendList.rspdata = nil;
		elseif event == 5 then --错误
			if t ~= nil then
				sendmessage(htype, 0, 0, param2, t.func, t.user);
			end
			gFriendList.rspdata = nil;
		end
	end
	
	
end)

--获取文件后缀名称 例：png
createmodule(interface, "getfiletype", function(path)
	if path == nil then
		return ""
	end
	local pos = getLastWord(path, '%.')
	local strname = string.sub(path, pos, string.len(path))
 	return strname
end)

--------------------------------------------------------------------------------------------------------------
--下载头像接口
createmodule(interface, "downloadheadimage", function(url)
	if url ~= nil and url ~= "" then		
                tiros.http.httpsendforlua("cdc_client",
					"downloadheadimg", 
					"downloadheadimg", 
					url, 
					interface.downLoadImageHttpEvent,
					nil);
		return true;
	end	
	
	return false;
end)

--增量更新好友列表接口
createmodule(interface,"friendlist_update", function(stype,htype,flag)		
	local uid = friend_getuid();
	if uid == nil then
		return
	end

	local url = "";
	local parameters = "";
	local actionlocation = "";
	local addtime = "";
	local deltime = "";
	
	--用uid去数据库查询，有记录，取时间戳去请求，
	--没有记录，时间戳传空字符串，然后请求成功后添加uid到数据库
	addtime, deltime = interface.friend_query_adddeltime(uid)
	if addtime == nil and deltime == nil then
		interface.friend_init_adddeltime(uid)
		deltime =""
		addtime =""	
	end
	local friendupdateurl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_GET_URL);		
	--出租车好友	518801
	if flag == "taxi" then
		parameters = "?method=getMyFriends" .. "&uid=" .. uid .. "&flag=1" .. "&addtime=" .. addtime .. "&deltime=" .. deltime;
		url = friendupdateurl .. parameters;
		actionlocation = "actionlocation:/navidog2Taxi/getMyFriends.htm";
	--登录好友	--uid = 145358	fuid = 145359
	elseif flag == "login" then
		parameters = "?method=queryFriends" .. "&uid=" .. uid .. "&flag=1" .. "&addtime=" .. addtime .. "&deltime=" .. deltime;
		url = friendupdateurl .. parameters;
		actionlocation = "actionlocation:/Tirosdatabase/UserFriendsGet4xServlet";
	end
	
	tiros.http.httpabort(gFriendList.stype);
	gFriendList.stype = stype;
	gFriendList.htype = htype;
	gFriendList.rspdata = nil;
        httpEngine.httpsendforlua("cdc_client",
				stype,
				htype,
				url,
				interface.flist_update_httpevent,
				nil,
				ContentType,
				actionlocation);
				
end)

--好友资料查询接口
createmodule(interface,"friendlist_friendinfo", function(stype,htype,fuid)
	local uid = friend_getuid();
	if uid == nil then
		return
	end
	local parameters = "?method=viewFriend&uid=" .. uid .. "&fuid=" .. fuid;
	local frienddetailurl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_GET_URL);
	local url = frienddetailurl .. parameters;

	tiros.http.httpabort(gFriendList.stype)
	gFriendList.stype = stype;
	gFriendList.htype = htype;
	gFriendList.rspdata = nil;
        httpEngine.httpsendforlua("cdc_client",
				stype,
				htype,
				url,
				interface.flist_finfo_httpevent,
				nil,
				ContentType,
				HEADER_FRIENDLIST);				
end)

--好友邀请发送短信接口
createmodule(interface,"friendlist_sms", function(stype,htype)	
	local selfuid = friend_getuid();
	if selfuid == nil then
		return
	end		
	--msgType = 1：邀请好友
	local parameters = "?method=sendMessage&uid=" .. selfuid .. "&msgType=1";
	local smsmessageurl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_GET_URL);
	local url = smsmessageurl .. parameters;
	local actionlocation = "actionlocation:/Tirosdatabase/SendMessageServlet";
	
	tiros.http.httpabort(gFriendList.stype);
	gFriendList.stype = stype;
	gFriendList.htype = htype;
	gFriendList.rspdata = nil;
        httpEngine.httpsendforlua("cdc_client",
				stype,
				htype,
				url,
				interface.friendlist_sms_httpevent,
				nil,
				ContentType,
				actionlocation);				
end)

--[[
--@描述:搜索手机号函数接口 Get请求
--@param	stype string型参数，用于设定http请求的服务类型，
--@param	uid string型参数，自己犬号
--@param	phone string型参数，搜索手机号
--@return	请求成功返回true，失败返回false
--]]
createmodule(interface,"queryPhone",function(stype,htype,queryPhone)
	local selfuid = friend_getuid();
	if selfuid == nil then
		return
	end	
	local requestParameter = "?method=queryPhone" 
							.. "&uid=" .. selfuid 
							.. "&queryPhone=" .. queryPhone;
	local friendqueryurl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_GET_URL);
	local url = friendqueryurl .. requestParameter;
	local actionlocation = "actionlocation:/Tirosdatabase/UserInfo4xServlet";
	
	--对外声明lua层调用http请求函数接口
	--mtype:string型参数，用于设定该http请求的模块类型，其值具体参考邮件
	--stype：string型参数，用于设定http请求的服务类型，其值请参考邮件
	--htype:string型参数，js端用于标识该http句柄的唯一标识符
	--url:string型参数，http请求的url地址
	--cbkname：function型参数，lua端注册http的回调函数地址
	--data： 若为nil则为get请求，否则为post请求（string型参数，post请求的数据内容，该类型只能是string型数据）
	--为可变参数，用于追加http的请求头信息，可以为多个，每个请求头信息原型为："header:value"型字符串
	--输出：请求成功返回true，失败返回false
	tiros.http.httpabort(gFriendList.stype);
	gFriendList.stype = stype;
	gFriendList.htype = htype;
	gFriendList.rspdata = nil;
        tiros.http.httpsendforlua("cdc_client",
				stype,
				htype,
				url,
				interface.queryPhoneHttpEvent,
				nil,
				ContentType,
				actionlocation
				);
end)

--[[
--@描述:添加单个好友函数接口 Post请求
--@param	stype string型参数，用于设定http请求的服务类型，
--@param	flag string型参数，login / taxi
--@param	fuid string型参数，好友犬号
--@return	请求成功返回true，失败返回false
--]]
createmodule(interface,"addFriend",function(stype,htype,flag,fuid)		
	local parameters = "";
	local actionlocation = "";
	local selfuid = friend_getuid();
	if selfuid == nil then
		return
	end	
	--出租车好友
	if flag == "taxi" then
		--518801	518933
		parameters = "method=" .. "addFriend" .. "&" 
					.. "uid=" .. selfuid .. "&" 
					.. "fuid=" .. fuid;
		
		actionlocation = "actionlocation:/navidog2Taxi/addFriends.htm";
	--登录好友
	elseif flag == "login" then
		--uid = 145358	fuid = 145359
		local reqT = {};
		reqT.method = "addFriend";
		reqT.uid = selfuid;
		reqT.fuid = fuid;
		parameters = tiros.json.encode(reqT);
		parameters = "parameters=" .. parameters;
		
		actionlocation = "actionlocation:/Tirosdatabase/UserFriendsPost4xServlet";
	end
	local friendaddurl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_POST_URL);
	tiros.http.httpabort(gFriendList.stype);
	gFriendList.stype = stype;
	gFriendList.htype = htype;
	gFriendList.rspdata = nil;
	tiros.http.httpsendforlua(
                                "cdc_client",
				stype,
				htype,
				friendaddurl,
				interface.addFriendHttpEvent,
				parameters,
				ContentType,
				actionlocation
				);
end)

--[[
--@描述:删除单个好友接口 Post请求
--@param	stype string型参数，用于设定http请求的服务类型，
--@param	htype string型参数，用于设定http请求的对象类型，
--@param	flag string型参数，登录，出租车
--@param	fuid string型参数，好友犬号
--@return	请求成功返回true，失败返回false
--]]
createmodule(interface,"deleteFriend",function(stype,htype,flag,fuid)
	local parameters = "";
	local actionlocation = "";
	local selfuid = friend_getuid();
	if selfuid == nil then
		return
	end	
	if flag == "taxi" then
		--uid = 518801	fuid = 518933
		parameters = "method=" .. "delFriend" .. "&" 
					.. "uid=" .. selfuid .. "&" 
					.. "fuid=" .. fuid;
		
		actionlocation = "actionlocation:/navidog2Taxi/delFriends.htm";
	elseif flag == "login" then
		--uid = 145358	fuid = 145359
		local reqT = {};
		reqT.method = "delFriend";
		reqT.uid = selfuid;
		reqT.fuid = fuid;--145359
		parameters = tiros.json.encode(reqT);
		parameters = "parameters=" .. parameters;
		
		actionlocation = "actionlocation:/Tirosdatabase/UserFriendsPost4xServlet";
	end
	local frienddeleteurl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_POST_URL);
	tiros.http.httpabort(gFriendList.stype);
	gFriendList.stype = stype;
	gFriendList.htype = htype;
	gFriendList.rspdata = nil;
        tiros.http.httpsendforlua("cdc_client",
				stype,
				htype,
				frienddeleteurl,
				interface.deleteFriendHttpEvent,
				parameters,
				ContentType,
				actionlocation
				);
end)

--好友相关功能的网络请求取消
createmodule(interface,"friendRequestCancel",function()
	tiros.http.httpabort(gFriendList.htype)
end)

--头像下载取消
createmodule(interface,"friendheadimgupdateCancel",function()
	tiros.http.httpabort("downloadheadimg")
end)

--更新指定url的头像
--fuid,犬号
--url，头像的服务器端地址
createmodule(interface,"friendheadimgupdate",function(fuid, url)
	if url ~= nil and fuid ~= nil and string.len(fuid) > 0 and string.len(url) > 0 then
		local sql = string.format("SELECT * FROM USERDETAIL WHERE USERDETAIL.UID = '%s';", fuid)
		local users = tiros.database.database_Query(sql);
		local T = tiros.json.decode(users);
	
		if T ~= nil and type(T) == "table" and T[1] ~= nil and type(T[1]) == "table"  then
			--本地URL与最新url不同 或者 本地头像路径是默认头像路径 进行下载
			if T[1].HEADURL ~= url or T[1].HEADPATH == gDefaultHeadImgPath then
				--是否有正在下载的头像
				if not gDownImgIsBusy then
					gHeadURL = url;
					gFUID = fuid;
					gDownImgIsBusy = true
					gReCount = 0
					gimagename = gFUID..interface.getfiletype(gHeadURL);
					interface.downloadheadimage(gHeadURL);
					downloadimglistremove(fuid)
				else--繁忙时存入缓存
					downloadimglistappend(fuid, url)
				end
			end
		end
	end
--[[
	local selfuid = friend_getuid();
	if selfuid == nil then
		return
	end	

	local sql = string.format("SELECT * FROM USERDETAIL WHERE USERDETAIL.UID IN (SELECT FUID FROM FRIEND WHERE FRIEND.UID = '%s');", selfuid)
	if url == nil or fuid == nil then		
		local users = tiros.database.database_Query(sql);
		local T = tiros.json.decode(users);
		for k,v in pairs(T) do
			if v.HEADPATH ==  gDefaultHeadImgPath then
				if v.HEADURL ~= nil and v.HEADURL ~= "" then
					gHeadURL = v.HEADURL;
					gFUID = v.UID;
					gimagename =  gFUID..interface.getfiletype(gHeadURL);
					interface.downloadheadimage(gHeadURL);
					return
				end
			end			
		end
	else
--]]	
	
end)

--2，保存个人信息
createmodule(interface,"SaveUserinfo",function(t)
	InsertUserdetailTable(t.UID,t.AID,t.NICKNAME,t.PHONE,t.HEADPATH,t.HEADURL,t.ADDBOOKNAME,t.SEX)
end)
--3，获取个人信息
--UID AID NICKNAME PHONE HEADPATH HEADURL ADDBOOKNAME SEX
createmodule(interface,"GetUserinfo",function(uid)
	local str = interface.friend_dbquery_friendinfo(uid)	
	local t = {}
	if str ~= nil then
		t = tiros.json.decode(str)
	end
	return t;
end)

--4，更新个人信息
createmodule(interface,"UpdateUserinfo",function(t)
	interface.friend_updatedetail(t.UID,t.AID,t.NICKNAME,t.PHONE,t.ADDBOOKNAME,t.SEX)
	UpdataHeadPathToDatabase(t.UID,t.HEADPATH,t.HEADURL)
	tiros.together.UpdataHeadPath(t.UID,t.NICKNAME,t.HEADPATH)
end)

tiros.friendmanger = readOnly(interface);

