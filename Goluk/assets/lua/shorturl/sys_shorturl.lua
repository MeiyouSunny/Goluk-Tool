--[[
-- @描述:通过短信里的短网址请求服务器生成长网址
-- @编写人:宣东言
-- @创建日期: 2012-10-19 13:51:11
--]]

require"lua/json"
require"lua/http"
require"lua/moduledata"
require"lua/framework"
require"lua/systemapi/sys_namespace"

--短网址服务回调函数
local gfnNotify;
--短网址服务用户指针地址
local gUser;
--短网址服务通知平台的消息(EVT_POISHARE_SMS)
local gnEvt = 128;
--短网址服务器返回数据拼接池
local gsHttpData;
--网络错误或者超时重试时间
local gTimeOut = 3000;
--短网址服务的URL
--local gsURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",1701);

--[[
--@描述:构成短网址服务完整的URL
--@param  无
--@return 失败返回nil,成功返回短网址服务的URL
--]]
local function getURL(sURL)
	if sURL == nil then
		return nil;
	end
	local sBasicURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",1701);
	if sBasicURL == nil then
		return nil;
	end
	local sRequestURL = sBasicURL.."?".."shorturl="..sURL;
		   
	print("shorturl--getURL = ",sRequestURL);
	return sRequestURL;
end

--[[
--@描述:解析短网址服务下行数据
--@param sHttpData:服务器返回的完整数据
--@return 无
--]]
local function parseData(sHttpData)
	print("shorturl--http-alldata----------",sHttpData);
	local tHttpData = tiros.json.decode(sHttpData);
	local bSuccess = tHttpData["success"];

	if bSuccess == true then
		local sLon = tHttpData["lon"];
		local sLat = tHttpData["lat"];
		local sPoiName = tHttpData["name"];
		local sPoiGid = tHttpData["gid"];
		--去掉success字段
		tHttpData.success = nil;
		local sPoiShare = tiros.json.encode(tHttpData);
		--将短网址服务数据存入数据仓库
		tiros.moduledata.moduledata_set("framework", "poishare", sPoiShare);
		--通知平台版本检测数据已经存入数据仓库
		commlib.initNotifyFun(gfnNotify, gUser, gnEvt, 1, "framework");
	elseif bSuccess == false then
		commlib.initNotifyFun(gfnNotify, gUser, gnEvt, 0, "error");
	end
	
	sHttpData = nil
end

--接口table
local interface = {};

--短网址服务超时重试次数记录
local gnTimes = 0;

--[[
--@描述:短网址服务请求重试函数
--@param  pType integer型参数,标记时间回调句柄
--@return 无
--]]
local function shortUrlCB(pType)
	gnTimes = gnTimes + 1;
	if gnTimes == 10 then
		tiros.timer.timerabort(pType);
		gnTimes = 0;
		return;
	end
	interface.shorturl(gfnNotify, gUser);
end

--[[
--@描述:短网址服务的http回调函数
--@param  ptype 回调对象句柄
--@param  event 回调事件类型
--@param  param1 回调事件传递参数1
--@param  param2 回调事件传递参数2
--@return 无
--]]
local function httpNotify(ptype, event, param1, param2)
	print("shortUrl--httpnotify --",ptype,event,param1,param2);
	if event == 1 then
		gsHttpData = nil;
	
	elseif event == 2 then
		if param1 ~= 200 then--http状态出错
			commlib.initNotifyFun(gfnNotify, gUser, gnEvt, 0, param2);
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
		gsHttpData = nil
		tiros.http.httpabort(ptype);

	elseif event == 5 then
		if(param1 == 1 or param1 == 2) then
			print("shortUrl--httpnotify-err1or2=",param1, param2);
			tiros.timer.timerabort(ptype);
			tiros.timer.timerstartforlua(ptype, gTimeOut, shortUrlCB, false);
		elseif (param1 == 3) then
			print("shortUrl--httpnotify-err3=",param1, param2);
			shortUrlCB(ptype);
		end

		commlib.initNotifyFun(gfnNotify, gUser, gnEvt, 0, param2);
		tiros.http.httpabort(ptype);
	end
end

--[[
--@描述:对外声明调用短网址服务请求函数接口
--@param  notify integer型参数，注册回调函数地址
--@param  pUser integer型参数，注册的调用者参数地址
--@return 请求成功返回true，失败返回false
--]]
createmodule(interface,"shorturl",function (nNotify, nUser, sShortURL)
 	gfnNotify = nNotify;
 	gUser = nUser;
	local sURL = getURL(sShortURL);
	if sURL == nil then
	   return nil;
	end
        return tiros.http.httpsendforlua("cdc_client", "shorturl",
				"shorturl", sURL, httpNotify, nil
				);
end)

tiros.shorturl = readOnly(interface);

--file end
