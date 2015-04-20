--[[
-- @描述:上传手机令牌
-- @编写人:宣东言
-- @创建日期: 2012-11-14 10:52:11
--]]

require"lua/json"
require"lua/http"
require"lua/moduledata"
require"lua/framework"
require"lua/systemapi/sys_namespace"

--上传手机令牌服务回调函数
local gfnNotify;
--上传手机令牌服务用户指针地址
local gUser;
--手机令牌
local gsToken;
--上传手机令牌服务通知平台的消息(EVT_DEVICE_TOKEN)
local gnEvt = 130;
--上传手机令牌服务器返回数据拼接池
local gsHttpData;
--网络错误或者超时重试时间
local gTimeOut = 3000;
--上传手机令牌服务的URL
--local gsURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",2002);
--local gsURL = "http://dev8.lbs8.com/msgpushPost"

--[[
--@描述:从数据仓库获取设备mobileid
--@param  无
--@return 设备mobileid
--]]
local function getMobileid()
	local sMobileid = tiros.moduledata.moduledata_get("framework", "mobileid");
	if sMobileid == nil then
	  sMobileid = "";
	end
	print("uploadtoken-getMobileid="..sMobileid);
	return sMobileid;
end

--[[
--@描述:获取手机设备类型,目前是从平台传的来的屏幕密度去区分 0:其他 1:iOS; 2:android 
--@param  无
--@return 手机设备类型
--]]
local function getSourceType()
	local sSourceType = tiros.moduledata.moduledata_get("framework", "devicetype");
	if sSourceType == nil then
	   sSourceType = 0;
	end
	print("uploadtoken--getSourceType=",sSourceType);
	return sSourceType;
end

--[[
--@描述:解析上传手机令牌服务下行数据
--@param sHttpData:服务器返回的完整数据
--@return 无
--]]
local function parseData(sHttpData)
	print("uploadtoken--http-alldata----------",sHttpData);
	local tHttpData = tiros.json.decode(sHttpData);
	local bSuccess = tHttpData["success"];
	local sToken = tHttpData["devicetoken"];
	if bSuccess == true then
		--将上传手机令牌服务数据存入数据仓库
		tiros.moduledata.moduledata_set("framework", "tokensuccess", sToken);
		--通知平台版本检测数据已经存入数据仓库
		commlib.initNotifyFun(gfnNotify, gUser, gnEvt, 1, "framework");
	elseif bSuccess == false then
		commlib.initNotifyFun(gfnNotify, gUser, gnEvt, 0, "error");
	end
	sHttpData = nil
end

--接口table
local interface = {};

--上传手机令牌服务超时重试次数记录
local gnTimes = 0;

--[[
--@描述:上传手机令牌服务请求重试函数
--@param  pType integer型参数,标记时间回调句柄
--@return 无
--]]
local function uploadtokenCB(pType)
	gnTimes = gnTimes + 1;
	if gnTimes == 10 then
		tiros.timer.timerabort(pType);
		gnTimes = 0;
		return;
	end
	interface.uploadtoken(gfnNotify, gUser, gsToken);
end

--[[
--@描述:上传手机令牌服务的http回调函数
--@param  ptype 回调对象句柄
--@param  event 回调事件类型
--@param  param1 回调事件传递参数1
--@param  param2 回调事件传递参数2
--@return 无
--]]
local function httpNotify(ptype, event, param1, param2)
	print("uploadtoken--httpnotify --",ptype,event,param1,param2);
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
			print("uploadtoken--httpnotify-err1or2=",param1, param2);
			tiros.timer.timerstartforlua(ptype, gTimeOut, uploadtokenCB, false);
		elseif (param1 == 3) then
			print("uploadtoken--httpnotify-err3=",param1, param2);
			uploadtokenCB(ptype);
		end

		commlib.initNotifyFun(gfnNotify, gUser, gnEvt, 0, param2);
		tiros.http.httpabort(ptype);
	end
end

--[[
--@描述:对外声明调用上传手机令牌服务请求函数接口
--@param  notify integer型参数，注册回调函数地址
--@param  pUser  integer型参数，注册的调用者参数地址
--@param  sToken string型参数，要上传的手机令牌字符串
--@return 请求成功返回true，失败返回false
--]]
createmodule(interface,"uploadtoken",function (nNotify, nUser, sToken)
 	gfnNotify = nNotify;
 	gUser = nUser;
	gsToken = sToken;
	local sURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",2002);
	local sMethod = "tokenupload";
	local sMobileid = getMobileid();
	local sSourceType = getSourceType();
	--组织post数据
	local sPostData = "method="..sMethod.."&"
					.."mobileid="..sMobileid.."&"
					.."source="..sSourceType.."&"
					.."devicetoken="..sToken;
	print("uploadtoken--sPostData=",sPostData);
        return tiros.http.httpsendforlua("cdc_client", "uploadtoken",
				"uploadtoken", sURL, httpNotify, 
				sPostData,"Content-Type:application/x-www-form-urlencoded");
				
end)

tiros.uploadtoken = readOnly(interface);

--file end
