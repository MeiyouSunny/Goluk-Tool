--[[
-- @描述:公交换乘相关接口
-- @编写人:宣东言
-- @创建日期: 2013-3-31 13:33:11
--]]

require"lua/json"
require"lua/moduledata"
require"lua/systemapi/sys_namespace"

--follow head file for bus information check
require"lua/http"
require"lua/systemapi/sys_commfunc"
require"lua/framework/sys_framework"

--MODULE_NAVI
local gnModuleNavi = 1;
--NAVICOMMAND_STOPNAVI
local gnStopBus = 10;
--NAVICOMMAND_BUSSTART
local gnStartBus = 30;
--NAVICOMMAND_BUSGETDETAILS
local gnBusDetails = 31;
--回调函数
local gfnNotify;
--句柄(假的)
local gsType = "bus-test";
--接口table
local interface = {};

--[[
--@描述:lua层发送消息给logic模块
--@param  param1 参数1
--@param  param2 参数2
--@return 无
--]]
local function sendMessageToLogic(param1,param2)
	print("bus----sendMessageToLogic",param1,param2)
	local nFunction = tiros.moduledata.moduledata_get("framework", "pLogicFunction");
	local nUser = tiros.moduledata.moduledata_get("framework", "pLogicUser"); 
	print("bus----sendMessageToLogic--1")
	if nFunction ~= nil then
	print("bus----sendMessageToLogic--2")
		commlib.universalnotifyFun(nFunction, gsType, nUser, gnModuleNavi, param1, param2);
	print("bus----sendMessageToLogic--3")
	end
end

--为了便于管理归类，将周边公交查询功能放入此文件
--=========================================公交查询和数据提取内部数据=========================================
local httpData = nil
local callerInfo = {}

--=========================================公交查询和数据提取内部实现=========================================

local function sys_CancelBusListItemDetailRequest(id)
	local handler = "businfodetail" .. tostring(id)
	--print("CancelBusListItemDetailRequest cancel handler is " .. handler)
	tiros.http.httpabort(handler)
	if callerInfo.id ~= nil then
		--print("cancel CancelBusListItemDetailRequest")
		callerInfo.id = nil
	end
end

local function sys_CancelGetBusListInfoRequest(id)
	local handler = "businfo" .. tostring(id)
	--print("CancelGetBusListInfoRequest cancel handler is " .. handler)
	tiros.http.httpabort(handler)
	if callerInfo.id ~= nil then
		--print("cancel CancelGetBusListInfoRequest")
		callerInfo.id = nil
	end
end

local function processBusInfoData(httpData, httpHander)
	--print(httpData)
	tiros.moduledata.moduledata_set("web","buslineneardataset_ptp",httpData)
	--print(httpHander)
	local id = string.sub(httpHander, 8, -1)
	--print(id)
	if callerInfo.id ~= nil then
		--print("processBusInfoData call user callback")
		callerInfo.id(id, 1)
		callerInfo.id = nil
	end
end


local function processBusInfoDetailData(httpData, httpHander)
	--print(httpData)
	tiros.moduledata.moduledata_set("web","buslinenearitem_ptp",httpData)
	--print(httpHander)
	local id = string.sub(httpHander, 14, -1)
	--print(id)
	if callerInfo.id ~= nil then
		--print("processBusInfoDetailData call user callback")
		callerInfo.id(id, 1)
		callerInfo.id = nil
	end
end

local function busInfoErrorProcess(errorType, httpHander)
	--print(errorType)
	--print(httpHander)
	tiros.moduledata.moduledata_set("web","buslinenearerr_ptp",errorType)
	local id = string.sub(httpHander, 8, -1)
	--print(id)
	if callerInfo.id ~= nil then
		--print("busInfoErrorProcess call user callback")
		callerInfo.id(id, 0)
		callerInfo.id = nil
	end
end

local function busInfoDetailErrorProcess(errorType, httpHander)
	--print(errorType)
	tiros.moduledata.moduledata_set("web","buslinenearerr_ptp",errorType)
	--print(httpHander)
	local id = string.sub(httpHander, 14, -1)
	--print(id)
	if callerInfo.id ~= nil then
		--print("busInfoDetailErrorProcess call user callback")
		callerInfo.id(id, 0)
		callerInfo.id = nil
	end
end



--http 回调函数
local function busInfoHttpCallback(pType, nEvent, param1, param2)
	--print("businfo businfoHttpCallback  run in",pType, nEvent, param1, param2)
	--print("businfo http call back")
	if nEvent == 1 then
	elseif nEvent == 2 then
		if param1 ~= 200 then
		clearMemberData()
		local err = tostring(param2)
		errorProcess(err)
		--NOTIFY CLIENT ERROR
		httpData = nil
		tiros.http.httpabort(pType)
		end
	elseif nEvent == 3 then
		if httpData ~= nil then
			httpData = httpData..string.sub(param2, 1, param1);
		else
			httpData = string.sub(param2, 1, param1);
		end
	elseif nEvent == 4 then
		--print("http callback 4 ok: handler is " .. pType)
		if string.find(pType, "businfodetail") ~= nil then
			processBusInfoDetailData(httpData, pType)
		elseif string.find(pType, "businfo") ~= nil then
			processBusInfoData(httpData, pType)
		end
		httpData = nil
		tiros.http.httpabort(pType)
	elseif nEvent == 5 then
		local err = tostring(param2)
		--print("http callback 5 error: handler is " .. pType)
		if string.find(pType, "businfodetail") ~= nil then
			busInfoDetailErrorProcess(err, pType)
		elseif string.find(pType, "businfo") ~= nil then
			busInfoErrorProcess(err, pType)
		end
		httpData = nil
		tiros.http.httpabort(pType)
	else
	end
	--print("businfo businfoHttpCallback  run out")
end

local function sys_GetBusListInfo(id, callbackFn)

	local paraStr = tiros.moduledata.moduledata_get("web","buslinenear_ptp")
	--print(paraStr)
	local para = tiros.json.decode(paraStr)
	local businfoURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",2105)
	local data = {}
	data.qtype="14"
	data.mobileid="00001"
	data.currentPage="1"
	data.pageSize="10"
	data.lon=tostring(para.lon)
	data.lat=tostring(para.lat)
	data.keyword=""
	data.qr="14,15"
	data.areacode=""
	data.ra=tostring(para.ra)
	data.uid=""
	data.selflon=""
	data.selflat=""
	data.bpointlist="0"
	data.agreementVer="2"
	data.vsource="-1"
	data.escapes="1"
	data.xv="2"
	local postData = tiros.json.encode(data)
	postData = "json=" .. postData;
	local httpHandler = "businfo" .. tostring(id)
	--print("GetBusListInfo request handler is " .. httpHandler)
	callerInfo.id = callbackFn
        tiros.http.httpsendforlua("cdc_client", "queryAjax_resList", httpHandler, businfoURL, busInfoHttpCallback, postData,  "Content-Type:application/x-www-form-urlencoded", "actionlocation:/QueryServer3.0/queryAjax_resList.htm")

end

local function sys_GetBusListItemDetail(id, poigid,callbackFn)
	local url = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",2105)
	local data = {}
	data.source = ""
	data.poigid = poigid
	data.mobileid="0000"
	data.uid="2323"
	data.selflon="0"
	data.selflat="0"
	data.vsource="-1"
	data.xv="1"
	local postData = tiros.json.encode(data)
	postData = "json=" .. postData
	--print(sendUrl)
	local httpHandler = "businfodetail" .. tostring(id)
	--print("GetBusListItemDetail request handler is " .. httpHandler)
	callerInfo.id = callbackFn
        tiros.http.httpsendforlua("cdc_client", "queryAjax_single", httpHandler, url, busInfoHttpCallback, postData, "Content-Type:application/x-www-form-urlencoded", "actionlocation:/QueryServer3.0/queryAjax_single.htm")

end

local function sys_GetTrafficRouteDataToLogic()
	local jsonStr = tiros.moduledata.moduledata_get("web","buslinenearitem_ptp")
	local all = tiros.json.decode(jsonStr)
	local obj = all.comObj
	local stops = obj.stops
	--print("GetTrafficRouteDataToLogic get stops")
	local stopsStr = tiros.json.encode(stops)
	--print(stopsStr)
	--print("\r\n=====================================================\r\n")
	--for draw line
	local lines = obj.lines
	local lineStr =	tiros.json.encode(lines)
	local result = "{\"stops\":" ..  stopsStr .."," .. "\"lines\":" ..  lineStr .. "}"
	--print(result)
	--print("\r\n=====================================================\r\n")
	local lineName = obj.name
	--print(lineName)
	local param = all.param
	local poiid = param.poigid
	return result, lineName, poiid
end

local function sys_GetUserStopSelected()
	local stopInfo = tiros.moduledata.moduledata_get("web","busnear_ptp")
	local stopTable = tiros.json.decode(stopInfo)
	if tonumber(stopTable.tag) == 0 then
		--print("GetUserStopSelected is empty")
		return "empty"
	else
		--print("GetUserStopSelected is ")
		--print(stopTable.popshow.poigid)
		return stopTable.popshow.poigid
	end
end


local function sys_SetCurrentStopInformation(poigid, linename, stopname, lon, lat)
	local stopInfo = {}
	stopInfo.address = tostring(linename)
	stopInfo.name = tostring(stopname)
	stopInfo.lon = tostring(lon)
	stopInfo.lat = tostring(lat)
	stopInfo.poigid = tostring(poigid)
	local stopInfoJson = tiros.json.encode(stopInfo)
	--print("current stop information is " .. stopInfoJson)
	tiros.moduledata.moduledata_set("web","poiinfo.detail" , stopInfoJson)
end




--=========================================公交查询和数据提取外部接口=========================================
--@描述:获取附近公交列表
--@param id：调用者的id（string）
--@param callbackfn：web的回调函数（callbackfn(string id, int status)）
--@输出：无
createmodule(interface, "GetBusListInfo", function(id, callbackFn)
	 sys_GetBusListInfo(id, callbackFn)
end)
--@@描述:取消上面的网络请求，参数见上面的解释
createmodule(interface, "CancelGetBusListInfoRequest", function(id)
	 sys_CancelGetBusListInfoRequest(id)
end)

--@@描述:获取列表中某项详细信息
--@param id：调用者的id（string）
--@param poigid：公交列表中给出的poigid字符串
--@param callbackfn：web的回调函数（callbackfn(string id, int status)）
--@输出：无
createmodule(interface, "GetBusListItemDetail", function(id, poigid,callbackFn)
	 sys_GetBusListItemDetail(id, poigid,callbackFn)
end)
--@@描述:取消上面的网络请求，参数见上面的解释
createmodule(interface, "CancelBusListItemDetailRequest", function(id)
	 sys_CancelBusListItemDetailRequest(id)
end)


--@@描述:提取
--@param id：调用者的id（string）
--@param poigid：公交列表中给出的poigid字符串
--@param callbackfn：web的回调函数（callbackfn(int type, ...)）
--@输出：无
createmodule(interface, "GetTrafficRouteDataToLogic", function()
	return sys_GetTrafficRouteDataToLogic()
end)

createmodule(interface, "GetUserStopSelected", function()
	return sys_GetUserStopSelected()
end)

createmodule(interface, "SetCurrentStopInformation", function(poigid, linename, stopname, lon, lat)
	return sys_SetCurrentStopInformation(poigid, linename, stopname, lon, lat)
end)


--=========================================以上为公交查询和数据提取的功能代码=========================================



--[[
--@描述:lua层回调处理函数
--@param  event 事件类型 0规划数据，1规划详情数据 2周边列表数据 3周边详情数据
--@param  param 数据在数据仓库的位置
--@return 无
--]]
createmodule(interface,"busNotify",function (event, param)
	--回调给web前端
	print("bus---busNotify-event,param=", event, param);

	gfnNotify(gsType, event, param);
end)


--[[
--@描述:对外声明 开始获取公交换乘结果
--@param  sType 类似句柄（假的）
--@param  notify 回调函数
--@param  param 请求参数在数据仓库的位置
--@return 请求成功返回详情结果json串，失败返回nil
--]]
createmodule(interface,"busStart",function (sType, notify, param)
	if param == nil or notify == nil then
	   return nil;
	end
	
	gfnNotify = notify;
	gsType = sType;

	sendMessageToLogic(gnStartBus, param);
end)

--[[
--@描述:对外声明 取消公交换乘请求
--@param  param 无
--@return 成功返回true，失败返回false
--]]
createmodule(interface,"busStop",function ()
	sendMessageToLogic(gnStopBus, "");
end)

--[[
--@描述:对外声明 获取展现的公交规划线路
--@param  busid integer型参数,公交规划结果id
--@return 请求成功返回详情结果json串，失败返回nil
--]]
createmodule(interface,"getDetails",function (busid)
	if busid == nil then
	   return nil;
	end
	sendMessageToLogic(gnBusDetails, tostring(busid));
end)

tiros.bus = readOnly(interface);

--file end
