--[[
-- @描述:通过mobileid或者uid获取aid
-- @编写人:宣东言
-- @创建日期: 2013-1-28 15:07:11
--]]

require"lua/json"
require"lua/http"
require"lua/moduledata"
require"lua/systemapi/sys_namespace"
require"lua/framework"

--user配置文件路径
local gsUserFileName = "fs4:/user"
--获取aid的类型:1为mobileid,2为uid
local gnGetType;
--getAid服务器返回数据拼接池
local gsHttpData;
--网络错误或者超时重试时间
local gTimeOut = 3000;
--getAid服务的URL
--local gsURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",2101);
--getAid服务标识(服务器端开发人员指定)
local gsActionLocation = "/navidog2News/meet_matchingAtkId.htm";

--[[
--@描述:从数据仓库获取设备Mobileid
--@param  无
--@return 设备Mobileid
--]]
local function getMobileid()
	local sMobileid = tiros.moduledata.moduledata_get("framework", "mobileid");
	if sMobileid == nil then
	  sMobileid = "";
	end
	print("login--sMobileid-return-sMobileid="..sMobileid);
	return sMobileid;
end

--[[
--@描述:获取犬号(uid)
--@param  无
--@return 犬号(uid)
--]]
local function getUID()
	local sUID = tiros.moduledata.moduledata_get("framework", "uid");
	if sUID == nil then
	   sUID = "";
	end
	print("login--getUID=",sUID);
	return sUID;
end

--[[
--@描述:构成getAid服务完整的URL
--@param 无
--@return 失败返回nil,成功返回getAid服务的URL
--]]
local function getURL()
	local sRequestURL = nil;
	local sMobileid = getMobileid();
	local sUID = getUID();
	local sBasicURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",2101);
	if(gnGetType == 1) then
		sRequestURL = sBasicURL.."?".."mobileid="..sMobileid;
	elseif (gnGetType == 2) then
		sRequestURL = sBasicURL.."?".."uid="..sUID;
	end

	print("getaid--getURL = ",sRequestURL);
	return sRequestURL;
end

--[[
--@描述:解析getAid服务下行数据
--@param sHttpData:服务器返回的完整数据
--@return 无
--]]
local function parseData(sHttpData)
	print("getaid--http-alldata----------",sHttpData);
	local tHttpData = tiros.json.decode(sHttpData);
	local bSuccess = tHttpData["success"];

	if bSuccess == true then
		local sAID = tHttpData["aid"];
		if sAID == nil then
			sAID = "";
		end

		local sData = nil;
		local tUserData = {};
		if(gnGetType == 1) then
			--将Aid存入数据仓库
			tiros.moduledata.moduledata_set("framework", "mobileid_aid", sAID);
			if not filelib.fexist(gsUserFileName) then
				tUserData.mobileid_aid = tHttpData["aid"];
				tUserData.cfg_mdsr = tHttpData["cfg_mdsr"];
				tUserData.cfg_mdsr_port = tHttpData["cfg_mdsr_port"];
				tUserData.cfg_sp = tHttpData["cfg_sp"];
				tUserData.cfg_sp_port = tHttpData["cfg_sp_port"];
				tUserData.cfg_sp_lport = tHttpData["cfg_sp_lport"];
				sData = tiros.json.encode(tUserData);
				tiros.file.Writefile(gsUserFileName, sData, true);
				--设置登录状态
				tiros.loginstatus.setLoginStatus(1, true);
			else
				sData = tiros.file.Readfile(gsUserFileName);
				local tData;
				if(sData ~= nil and sData ~= "") then
					tData = tiros.json.decode(sData);
					tData.mobileid_aid = tHttpData["aid"];
					tData.cfg_mdsr = tHttpData["cfg_mdsr"];
					tData.cfg_mdsr_port = tHttpData["cfg_mdsr_port"];
					tData.cfg_sp = tHttpData["cfg_sp"];
					tData.cfg_sp_port = tHttpData["cfg_sp_port"];
					tData.cfg_sp_lport = tHttpData["cfg_sp_lport"];

					sData = tiros.json.encode(tData);
				else
					tUserData.mobileid_aid = tHttpData["aid"];
					tUserData.cfg_mdsr = tHttpData["cfg_mdsr"];
					tUserData.cfg_mdsr_port = tHttpData["cfg_mdsr_port"];
					tUserData.cfg_sp = tHttpData["cfg_sp"];
					tUserData.cfg_sp_port = tHttpData["cfg_sp_port"];
					tUserData.cfg_sp_lport = tHttpData["cfg_sp_lport"];
					sData = tiros.json.encode(tUserData);
				end

				tiros.file.Writefile(gsUserFileName, sData, true);
				--设置登录状态
				tiros.loginstatus.setLoginStatus(1, true);
			end
		elseif (gnGetType == 2) then
			--将getAid服务数据存入数据仓库
			tiros.moduledata.moduledata_set("framework", "uid_aid", sAID);
			if not filelib.fexist(gsUserFileName) then
				tUserData.uid_aid = tHttpData["aid"];
				tUserData.cfg_mdsr = tHttpData["cfg_mdsr"];
				tUserData.cfg_mdsr_port = tHttpData["cfg_mdsr_port"];
				tUserData.cfg_sp = tHttpData["cfg_sp"];
				tUserData.cfg_sp_port = tHttpData["cfg_sp_port"];
				tUserData.cfg_sp_lport = tHttpData["cfg_sp_lport"];
				sData = tiros.json.encode(tUserData);
				tiros.file.Writefile(gsUserFileName, sData, true);
				--设置登录状态
				tiros.loginstatus.setLoginStatus(1, true);
			else
				sData = tiros.file.Readfile(gsUserFileName);
				local tData;
				if(sData ~= nil and sData ~= "") then
					tData = tiros.json.decode(sData);
					tData.uid_aid = tHttpData["aid"];
					tData.cfg_mdsr = tHttpData["cfg_mdsr"];
					tData.cfg_mdsr_port = tHttpData["cfg_mdsr_port"];
					tData.cfg_sp = tHttpData["cfg_sp"];
					tData.cfg_sp_port = tHttpData["cfg_sp_port"];
					tData.cfg_sp_lport = tHttpData["cfg_sp_lport"];

					sData = tiros.json.encode(tData);
				else
					tUserData.uid_aid = tHttpData["aid"];
					tUserData.cfg_mdsr = tHttpData["cfg_mdsr"];
					tUserData.cfg_mdsr_port = tHttpData["cfg_mdsr_port"];
					tUserData.cfg_sp = tHttpData["cfg_sp"];
					tUserData.cfg_sp_port = tHttpData["cfg_sp_port"];
					tUserData.cfg_sp_lport = tHttpData["cfg_sp_lport"];
					sData = tiros.json.encode(tUserData);
				end

				tiros.file.Writefile(gsUserFileName, sData, true);
				--设置登录状态
				tiros.loginstatus.setLoginStatus(1, true);
			end
		end

	elseif bSuccess == false then
		print("getaid--http-error");
		--设置登录状态
		tiros.loginstatus.setLoginStatus(1, false);
	end
	
	sHttpData = nil
end

--接口table
local interface = {};

--getAid服务超时重试次数记录
local gnTimes = 0;

--[[
--@描述:getAid服务请求重试函数
--@param  pType integer型参数,标记时间回调句柄
--@return 无
--]]
local function getaidCB(pType)
	gnTimes = gnTimes + 1;
	if gnTimes == 10 then
		tiros.timer.timerabort(pType);
		gnTimes = 0;
		return;
	end
	interface.getaid(gnGetType);
end

--[[
--@描述:getAid服务的http回调函数
--@param  ptype 回调对象句柄
--@param  event 回调事件类型
--@param  param1 回调事件传递参数1
--@param  param2 回调事件传递参数2
--@return 无
--]]
local function httpNotify(ptype, event, param1, param2)
	print("getaid--httpnotify --",ptype,event,param1,param2);
	if event == 1 then
		gsHttpData = nil;
	
	elseif event == 2 then
		if param1 ~= 200 then--http状态出错
			tiros.http.httpabort(ptype);
		end	

	elseif event == 3 then
		if gsHttpData ~= nil then
			gsHttpData = gsHttpData..string.sub(param2, 1, param1);
		else
			gsHttpData = string.sub(param2, 1, param1);
		end

	elseif event == 4 then
		parseData(gsHttpData);
		gsHttpData = nil;
		tiros.http.httpabort(ptype);

	elseif event == 5 then
		if(param1 == 1 or param1 == 2) then
			print("getaid--httpnotify-err1or2=",param1, param2);
			tiros.timer.timerabort(ptype);
			tiros.timer.timerstartforlua(ptype, gTimeOut, getaidCB, false);
		elseif (param1 == 3) then
			print("getaid--httpnotify-err3=",param1, param2);
			getaidCB(ptype);
		end

		tiros.http.httpabort(ptype);
	end
end

--[[
--@描述:对外声明调用getAid服务请求函数接口
--@param  nType integer型参数,获取aid的类型,1为mobileid,2为uid
--@return 请求成功返回true，失败返回false
--]]
createmodule(interface,"getaid",function (nType)
	gnGetType = nType;
	local sURL = getURL();
	if sURL == nil then
	   return nil;
	end
        return tiros.http.httpsendforlua("cdc_client", "meetmatchingAtkId","getaid", sURL, httpNotify, nil,
				 "actionlocation:"..gsActionLocation);	

end)

tiros.getaid = readOnly(interface);

--file end
