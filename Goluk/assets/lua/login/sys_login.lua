--[[
-- @描述:登录服务,目前包含版本检测服务,智能主界面广告更新和广告图片下载等
-- @编写人:宣东言
-- @创建日期: 2011-11-20 11:40:11
-- @修改内容:增加广告更新功能和广告图片下载替换功能, 修改人:宣东言 修改日期:2012-09-18 14:32:28
--]]

require"lua/json"
require"lua/http"
require"lua/tapi"
require"lua/location"
require"lua/moduledata"
require"lua/framework"
require"lua/systemapi/sys_namespace"
require"lua/loginstatus"

--登录回调函数
local gfnLoginNotify;
--版本检测回调函数
local gfnCheckVersionNotify;
--登录用户指针地址
local gLoginUser;
--版本检测用户指针地址
local gCheckVersionUser;
--登录服务器返回数据拼接池
local gsLoginHttpData;
--版本检测服务器返回数据拼接池
local gsCheckVersionhttpData;
--登录服务通知平台的消息(EVT_LOGIN_SERVICES)
local gnEvtLogin = 113;
--http连接类型
local gsConnectionType = "keep-alive";
--login服务协议版本号
local gsProtocolVersion = "2";
--网络错误或者超时重试时间
local gTimeOut = 3000; 
-- 是否已经通知平台版本更新了
local isCheckVersion = false;
--登录服务的URL
--local gsLoginURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",1001);
--版本检测服务的URL
--local gsCheckVersionURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",1011);

--[[
--@描述:从数据仓库获取设备IMSI
--@param  无
--@return 设备IMSI
--]]
local function getIMSI()
	local sIMSI = tiros.moduledata.moduledata_get("framework", "imsi");
	if sIMSI == nil then
	   sIMSI = "";
	end
	return sIMSI;
end

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
--@描述:从数据仓库获取设备OSVersion
--@param  无
--@return 设备OSVersion
--]]
local function getOSVersion()
	local sOSVersion = tiros.moduledata.moduledata_get("framework", "osversion");
	if sOSVersion == nil then
	  sOSVersion = "";
	end
	return sOSVersion;
end

--[[
--@描述:从数据仓库获取设备DeviceModel
--@param  无
--@return 设备DeviceModel
--]]
local function getDeviceModel()
	local sDeviceModel = tiros.moduledata.moduledata_get("framework", "devicemodel");
	if sDeviceModel == nil then
	  sDeviceModel = "";
	end
	return sDeviceModel;
end

--[[
--@描述:从数据仓库获取设备ManufacturerName
--@param  无
--@return 设备ManufacturerName
--]]
local function getManufacturerName()
	local sManufacturerName = tiros.moduledata.moduledata_get("framework", "manufacturername");
	if sManufacturerName == nil then
	  sManufacturerName = "";
	end
	return sManufacturerName;
end

--[[
--@描述:从数据仓库获取客户端的版本号
--@param  无
--@return 客户端的版本号
--]]
local function getVersion()
	local sVersion = tiros.moduledata.moduledata_get("framework", "version");
	if sVersion == nil then
	   sVersion = "";
	end
	print("login--sVersion-return-sVersion="..sVersion);
	return sVersion;
end

--[[
--@描述:从文件取获取‎最后一次定位成功的经度和纬度
--@param  无
--@return 经度和纬度
--]]
local function getLonAndLat()
	local nLon, nLat = tiros.location.lkgetlastposition_file();
	if nLon == nil then
	  nLon = "";
	end
	if nLat == nil then
	  nLat = "";
	end	
	return nLon, nLat;
end

--[[
--@描述:从数据仓库获取设备屏幕密度
--@param  无
--@return 屏幕密度
--]]
local function getDPI()
	local sDPI = tiros.moduledata.moduledata_get("framework", "dpi");
	if sDPI == nil then
	   sDPI = "";
	end
	print("login--getDPI-return-dpi="..sDPI);
	return sDPI;
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
	print("login--getSourceType=",sSourceType);
	return sSourceType;
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
--@描述:获取爱滔客ID(aid):如果有uid对应的aid优先
--@param  无
--@return 爱滔客ID(aid)
--]]
local function getAID()
	local sAID = tiros.moduledata.moduledata_get("framework", "uid_aid");

	if (sAID == nil or sAID == "") then
	   sAID = tiros.moduledata.moduledata_get("framework", "mobileid_aid");
	end

	if sAID == nil then
	   sAID = "";
	end
	print("login--getAID=",sAID);
	return sAID;
end

--[[
--@描述:构成登录服务完整的URL
--@param  无
--@return 登录服务的URL
--]]
local function getLoginURL()
	local nLon,nLat = getLonAndLat();
	local sProtocolVersion = gsProtocolVersion;
	local sSourceType = getSourceType();
	local sAID = getAID();
	--版本来源
	local appid = tiros.moduledata.moduledata_get("framework", "appid");
	local sLoginURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",1001);
	if sLoginURL == nil then
		return nil;
	end

	local sURL = sLoginURL.."?"
		   .."lon="..nLon.."&"
		   .."lat="..nLat.."&"
		   .."sourcetype="..sSourceType.."&"
		   .."appid="..appid.."&"
		   .."v="..sProtocolVersion;	
print("login--getLoginURL = ",sURL);
	return sURL;
end

--[[
--@描述:构成版本检测服务完整的URL
--@param  无
--@return 版本检测服务的URL
--]]
local function getCheckVersionURL()
	local nLon,nLat = getLonAndLat();
	local sCheckVersionURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",1011);
	if sCheckVersionURL == nil then
		return nil;
	end
	local sURL = sCheckVersionURL.."?"
		   .."lon="..nLon.."&"
		   .."lat="..nLat;
	print("login--getCheckVersionURL = ",sURL);
	return sURL;
end

--[[
--@描述:解析登录服务下行数据
--@param  服务器返回的完整数据
--@return 无
--]]
local function parseLoginData(sHttpData)
	--sHttpData = '{"login":{"resultcode":0,"msg":["服务器未找到对应的版本信息，版本号：4.0.0.249"]},"aid":{"aid":"1234567890"}}'

	print("login--login-http-alldata="..sHttpData);
	local tHttpData = tiros.json.decode(sHttpData);
	local tCheckVersionData = tHttpData["login"];
	--新客户端访问老服务下行里面没有login就直接返回
	if(tCheckVersionData == nil) then
		return nil
	end
	local nCheckVersionResultCode = tCheckVersionData["resultcode"];
	local nCheckVersionType = tCheckVersionData["type"];
	local tCheckVersionMsg = tCheckVersionData["msg"];
	local sCheckVersionURL = tCheckVersionData["url"];
	local nNum = 0;
	local sCheckVersionMsg = nil;
	--如果版本检测消息表不为空则拼接成原有的json数组格式
	if tCheckVersionMsg ~= nil
	then
		sCheckVersionMsg = '["';
		nNum = table.maxn(tCheckVersionMsg);
		for key,value in pairs(tCheckVersionMsg) do
		  sCheckVersionMsg = sCheckVersionMsg..value;
		  if key < nNum then
			sCheckVersionMsg = sCheckVersionMsg..'","';
		  else
			sCheckVersionMsg = sCheckVersionMsg..'"]';
		  end
		end
	end
	--将版本检测数据存入数据仓库
	tiros.moduledata.moduledata_set("upgrade", "alldata", sHttpData);
	tiros.moduledata.moduledata_set("upgrade", "resultcode", nCheckVersionResultCode);
	tiros.moduledata.moduledata_set("upgrade", "type", nCheckVersionType);
	tiros.moduledata.moduledata_set("upgrade", "msg", sCheckVersionMsg);
	tiros.moduledata.moduledata_set("upgrade", "url", sCheckVersionURL);
	if isCheckVersion == false then
		isCheckVersion = true;
		--通知平台版本检测数据已经存入数据仓库
		commlib.initNotifyFun(gfnLoginNotify, gLoginUser, gnEvtLogin, 2, "upgrade");
	end
	sHttpData = nil;
end

--[[
--@描述:解析版本检测服务下行数据
--@param sHttpData:服务器返回的完整数据
--@return 无
--]]
local function parseCheckVersionData(sHttpData)
	print("login--CheckVersion-http-alldata----------",sHttpData);
	local tHttpData = tiros.json.decode(sHttpData);
	local nCheckVersionResultCode = tHttpData["resultcode"];
	local nCheckVersionType = tHttpData["type"];
	local tCheckVersionMsg = tHttpData["msg"];
	local sCheckVersionURL = tHttpData["url"];
	local nNum = 0;
	local sCheckVersionMsg = nil;
	--如果版本检测消息表不为空则拼接成原有的json数组格式
	if tCheckVersionMsg ~= nil
	then
		sCheckVersionMsg = '["';
		nNum = table.maxn(tCheckVersionMsg);
		for key,vaule in pairs(tCheckVersionMsg) do
		  sCheckVersionMsg = sCheckVersionMsg..vaule;
		  if key < nNum then
			sCheckVersionMsg = sCheckVersionMsg..'","';
		  else
			sCheckVersionMsg = sCheckVersionMsg..'"]';	
		  end
		end
	end
	--将版本检测数据存入数据仓库
	tiros.moduledata.moduledata_set("upgrade", "alldata", sHttpData);
	tiros.moduledata.moduledata_set("upgrade", "resultcode", nCheckVersionResultCode);
	tiros.moduledata.moduledata_set("upgrade", "type", nCheckVersionType);
	tiros.moduledata.moduledata_set("upgrade", "msg", sCheckVersionMsg);
	tiros.moduledata.moduledata_set("upgrade", "url", sCheckVersionURL);
	--通知平台版本检测数据已经存入数据仓库
	commlib.initNotifyFun(gfnCheckVersionNotify, gCheckVersionUser, gnEvtLogin, 2, "upgrade")
	sHttpData = nil
end

--接口table
local interface = {};

--登录服务超时重试次数记录
local gnLoginTimes = 0;

--[[
--@描述:登录服务请求重试函数
--@param  pType integer型参数,标记时间回调句柄
--@return 无
--]]
local function loginCB(pType)
	gnLoginTimes = gnLoginTimes + 1;
	if gnLoginTimes == 10 then
		tiros.timer.timerabort(pType);
		gnLoginTimes = 0;

		return;
	end
	interface.login(gfnLoginNotify, gLoginUser);
end

--[[
--@描述:登录服务的http回调函数
--@param  ptype 回调对象句柄
--@param  event 回调事件类型
--@param  param1 回调事件传递参数1
--@param  param2 回调事件传递参数2
--@return 无
--]]
local function loginHttpnotify(ptype, event, param1, param2)
	print("login--loginHttpnotify --",ptype,event,param1,param2);

	if event == 1 then
		gsLoginHttpData = nil;
	
	elseif event == 2 then
		if param1 ~= 200 then--http状态出错
			commlib.initNotifyFun(gfnLoginNotify, gLoginUser, gnEvtLogin, 1, param2);
			tiros.http.httpabort(ptype);
		end	

	elseif event == 3 then
		if gsLoginHttpData ~= nil then
			gsLoginHttpData = gsLoginHttpData..string.sub(param2, 1, param1);
		else
			gsLoginHttpData = string.sub(param2, 1, param1);
		end

	elseif event == 4 then
		parseLoginData(gsLoginHttpData);
		gsLoginHttpData = nil
		tiros.http.httpabort(ptype);

	elseif event == 5 then
		if(param1 == 1 or param1 == 2) then
			print("login--loginHttpnotify-err1or2=",param1, param2);
			tiros.timer.timerabort(ptype);
			tiros.timer.timerstartforlua(ptype, gTimeOut, loginCB, false);
		elseif (param1 == 3) then
			print("login--loginHttpnotify-err3=",param1, param2);
			loginCB(ptype);
		end

		commlib.initNotifyFun(gfnLoginNotify, gLoginUser, gnEvtLogin, 1, param2);
		tiros.http.httpabort(ptype);
	end
end

--[[
--@描述:版本检测的http回调函数
--@param  ptype 回调对象句柄
--@param  event 回调事件类型
--@param  param1 回调事件传递参数1
--@param  param2 回调事件传递参数2
--@return 无
--]]
local function checkVersionHttpnotify(ptype, event, param1, param2)
	print("login--checkVersion_httpnotify --",ptype,event,param1,param2);
	if event == 1 then
		gsCheckVersionhttpData = nil;
	
	elseif event == 2 then
		if param1 ~= 200 then--http状态出错
			commlib.initNotifyFun(gfnCheckVersionNotify, gCheckVersionUser, gnEvtLogin, 1, param2);
			tiros.http.httpabort(ptype);
		end	

	elseif event == 3 then
		if gsCheckVersionhttpData ~= nil then
			gsCheckVersionhttpData = gsCheckVersionhttpData..string.sub(param2, 1, param1);
		else
			gsCheckVersionhttpData = string.sub(param2, 1, param1);
		end

	elseif event == 4 then
		parseCheckVersionData(gsCheckVersionhttpData);
		gsCheckVersionhttpData = nil;
		tiros.http.httpabort(ptype);

	elseif event == 5 then
		commlib.initNotifyFun(gfnCheckVersionNotify, gCheckVersionUser, gnEvtLogin, 1, param2);
		tiros.http.httpabort(ptype);
	end
end

--[[
--@描述:对外声明调用登录服务请求函数接口
--@param  notify integer型参数，注册login的回调函数地址
--@param  pUser integer型参数，注册的调用者参数地址
--@return 请求成功返回true，失败返回false
--]]
createmodule(interface, "login", function (notify, pUser)

 	gfnLoginNotify = notify;
 	gLoginUser = pUser;

	local url = getLoginURL();
	local mobileid = getMobileid();
	local version = getVersion();
	local osversion = getOSVersion();
	local devicemodel = getDeviceModel();
	local manufacturername = getManufacturerName();
	local imsi = getIMSI();
	local dpi = getDPI();
	local uid = getUID();

print("jiayufeng-----login----mobileid:"..mobileid);

	if url == nil then
	   return nil;
	end
        return tiros.http.httpsendforlua("cdc_client", "login",
				"login", url, loginHttpnotify, nil,
				"Connection:"..gsConnectionType,
				"mobileid:"..mobileid,
				"version:"..version,
				"osversion:"..osversion,
				"devicemodel:"..devicemodel,
				"manufacturername:"..manufacturername,
				"imsi:"..imsi,
				"uid:"..uid,
				"dpi:"..dpi
				);
end)

--[[
--@描述:对外声明调用版本检测服务请求函数接口
--@param  notify integer型参数，注册checkVersion的回调函数地址
--@param  pUser integer型参数，注册的调用者参数地址
--@return 请求成功返回true，失败返回false
--]]
createmodule(interface,"checkVersion",function (notify, pUser)

 	gfnCheckVersionNotify = notify;
 	gCheckVersionUser = pUser;

	local url = getCheckVersionURL();
	local mobileid = getMobileid();
	local version = getVersion();
	local osversion = getOSVersion();
	local devicemodel = getDeviceModel();
	local manufacturername = getManufacturerName();
	local imsi = getIMSI();

	if url == nil then
	   return nil;
	end

        return tiros.http.httpsendforlua("cdc_client", "login",
				"checkVersion",url,checkVersionHttpnotify, nil,
				"Connection:"..gsConnectionType,
				"mobileid:"..mobileid,
				"version:"..version,
				"osversion:"..osversion,
				"devicemodel:"..devicemodel,
				"manufacturername:"..manufacturername,
				"imsi:"..imsi
				);
end)

tiros.login = readOnly(interface);

--file end
