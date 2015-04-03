--[[
-- @描述:3.x升级到4.x的逻辑操作，包括帐户的读取，收藏夹的数据迁移
-- @编写人:贾玉峰
-- @创建日期: 2013-4-10 13:40:16
-- @修改内容:
--]]
require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/framework/sys_framework"
require"lua/json"
require"lua/commfunc"
require"lua/http"
require"lua/moduledata"
require"lua/database"

------------------资源----------------------------
local RES_STR_FRIEND_GET_URL = 2101; --资源文件中编号
local RES_STR_FRIEND_POST_URL = 2102; --资源文件中编号
local RES_FILE_PATH = "fs0:/res/api/api.rs"; --资源文件地址路径
--给Logic发消息
local LOGIC_MSG_GETUID = 0;		--获取用户UID与密码
local LOGIC_MSG_READ_FACTORY = 1;	--读取收藏夹数据
local LOGIC_MSG_DEL_LOGINFILE = 2;	--删除3.X登录文件

--命名空间
local interface = {};
--全局变量
local gFriendList = {};

local ContentType = "Content-Type:application/x-www-form-urlencoded";
local moduledataobj = getmodule("moduledata");
local httpEngine = getmodule("http");
local gsMtype ="cdc_client";
local gsStype ="UserFriendsPost4x";
local gsHtype = "update_login";
local actionlocation = "actionlocation:/Tirosdatabase/UserFriendsPost4xServlet";

local rspdata =	nil;

--请求最新4.x信息url
--local updateloginurl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_POST_URL);

--lua通知logic更新数据
local function getLogicFunctionAndUser()
    local nFunction = tiros.moduledata.moduledata_get("framework", "pLogicFunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "pLogicUser");    

    return nFunction, nUser;    
end
--给Logic发消息
local function sendmessagetoLogic(event,param1,param2)

	local func, usr = getLogicFunctionAndUser();

	if func ~= nil then
		commlib.universalnotifyFun(func,"LuaToLogicMsg", usr, event,param1,param2);
	end
end

--发送最终结果(成功／失败)给loginManager
local function sendResultToOther(isSucess,data)
	if(isSucess == true) then
		--保存data到数据仓库
		tiros.moduledata.moduledata_set("framework", "3xData",data);
	end

	tiros.loginstatus.setLoginStatus(0,isSucess);
end 

--删除3.x数据
local function delete3xFile(isLoginFile)
	if isLoginFile == true then --删除登录文件
		sendmessagetoLogic(13,LOGIC_MSG_DEL_LOGINFILE,"");
	end
end

-- 通知读取收藏夹数据
local function readFavoriteData(uid)
	sendmessagetoLogic(13,LOGIC_MSG_READ_FACTORY,uid);
end


--获取3.x的用户名与密码去服务端获取4.x 信息
local function update_login(userid,password)

	if userid == nil or password ==nil then
		-- 发送失败消息
		sendResultToOther(false,nil)
		return
	end

	local datajson = {};
	datajson.method = "getPhone3x";
	datajson.uid = userid
	datajson.pwd = password
	local data = "parameters="..tiros.json.encode(datajson);

	local updateloginurl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_FRIEND_POST_URL);
	local url = updateloginurl;

	tiros.http.httpabort(gsStype);
	httpEngine.httpsendforlua(gsMtype,
				gsStype,
				gsHtype,
				url,
				interface.update_login_httpevent,
				data,
				ContentType,
				actionlocation);
	
end

--请求4.x信息回调
createmodule(interface,"update_login_httpevent", function(_htype,event,param1,param2)

	if _htype == gsHtype then
		if event == 1 then --请求
			
		elseif event == 2 then --应答

			if param1 ~= 200  then	
				tiros.http.httpabort(gsHtype);
				sendResultToOther(false,nil);
			end	
				
		elseif event == 3 then --数据体
			if rspdata == nil then
				rspdata = string.sub(param2,1,param1);
			else
				rspdata = rspdata..string.sub(param2,1,param1);
			end
		elseif event == 4 then --完成
			local decodeT = tiros.json.decode(rspdata);
			if decodeT ~= nil then	
				if decodeT.success == true then
					local result = tiros.json.encode(decodeT.data);	--JSON串
					sendResultToOther(true,result);		--数据
					delete3xFile(true);			--删除3.x登录文件
				else
					-- 发送错误
					sendResultToOther(false,nil);
					delete3xFile(true);
				end
			end	
		elseif event == 5 then --错误
			sendResultToOther(false,nil);
			rspdata = nil;
		end
	end
end)



local function UpgradeCB(pType)

	--给Logic发消息获取帐户密码
	sendmessagetoLogic(13,LOGIC_MSG_GETUID,"");
	--去数据仓库获取数据

	local userid = tiros.moduledata.moduledata_get("framework", "upgrade_uid");
	local password = tiros.moduledata.moduledata_get("framework", "upgrade_pass");

	if(userid == nil or password == nil) then
		readFavoriteData(nil);		--复制公共收藏夹数据
		delete3xFile(true);			--删除3.x登录文件
		sendResultToOther(false,nil);	--给loginmanager发消息3.x信息读取失败
		return false;
	end

	if(tiros.loginmanager.hasAutoLogin() == true) then --满足自动登录
		delete3xFile(true);
		sendResultToOther(false,nil);
		return false;	
	end

	--3.x文件去请求4.x信息
	update_login(userid,password);

	--读取uid对应的收藏夹数据
	readFavoriteData(userid);


	return true;
end

--对外开放3.x到4.x升级接口
createmodule(interface,"Upgrade", function()
	local bExist = filelib.fexist("user.db");
	if bExist == true then
		tiros.timer.timerstartforlua("Upgrade", 2000, UpgradeCB, false);
	else
		sendResultToOther(false, nil);
	end
end)

tiros.loginupdate = readOnly(interface);

