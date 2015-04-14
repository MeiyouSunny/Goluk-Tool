--[[
 @描述：气泡的操作
 @编写人：jiayf
 @创建日期：2013-11-28 下午 18:47:00
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
--存放数据仓库的位置
local MAP_BUBBLE_KEY = "mapbubble_ptp";
local MAP_BUBBLE_NET_KEY = "mapbubble_net_ptp";
local MAP_BUBBLE_SELECTPOI_KEY= "mapbubble_selectpoi_ptp";
local MAP_BUBBLE_NAVITIME_KEY = "mapbubble_navitime_ptp";
-- Lua回调Logic，请求导航时间
local EVENT_BUBBLE = 19;
local EVENT_BUBBLE_REQUESTTIME = 0; -- 请求导航时间
local EVENT_BUBBLE_CURRENT = 1;  --当前选择那个气泡

local MSG_BUBBLE_UPDATE = 180;	--更新气泡framework值
local BUBBLE_SHOW = 1;	-- 显示气泡
local BUBBLE_HIDE = 2;	-- 隐藏气泡
local BUBBLE_UPDATE = 3; -- 刷新数据

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

--获取logic注册的回调
local function getLogicFunctionAndUser()
    local nFunction = tiros.moduledata.moduledata_get("framework", "pLogicFunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "pLogicUser");    
    return nFunction, nUser;    
end

--给logic发消息
local function sendmessagetoLogic(event,param1,param2)
	local func, usr = getLogicFunctionAndUser();
	if func ~= nil then
		commlib.universalnotifyFun(func,"LuaToLogicMsg", usr, event,param1,param2);
	end
end

--保存基础数据
local function baseData(source,name,address,lon,lat,poigid,radius,isrefresh)
	print("bubble---------baseData" .. tostring(source) .. "   name: " .. tostring(name) .. "  address " .. tostring(address) .. "isrefresh:" .. tostring(isrefresh));
	local t = {};
	t.source = source;
	t.datatype = tostring(1);
	local t_data = {};
	t_data.name = tostring(name);
	t_data.address = tostring(address);
	t_data.lon = tostring(lon);
	t_data.lat = tostring(lat);
	t_data.poigid = tostring(poigid);
	t_data.radius = tostring(radius);

	t.data = t_data;
	local jsonData = tiros.json.encode(t);

	print("bubble---------baseData ----json: " .. jsonData);
	
	local t_platform = {};
	t_platform.source = tostring(source);
	t_platform.isshowMenuBan = tostring(0);
	local platformjsonData = tiros.json.encode(t_platform);

	tiros.moduledata.moduledata_set("web",MAP_BUBBLE_KEY,jsonData);
	if 1 == tonumber(isrefresh) then
		sendmessagetoApp(MSG_BUBBLE_UPDATE,BUBBLE_UPDATE,platformjsonData);
	else
		sendmessagetoApp(MSG_BUBBLE_UPDATE,BUBBLE_SHOW,platformjsonData);
	end
	
		
end

--保存网络数据
local function serverData(source,data,isrefresh,isshowMenuBan)
	if nil == data then
		return;
	end

	-- Web
	local t = {};
	t.source = tostring(source);
	t.datatype = tostring(2);
	t.data = tiros.json.decode(data);

	local jsonData = tiros.json.encode(t);
	
	tiros.moduledata.moduledata_set("web",MAP_BUBBLE_NET_KEY,jsonData);
	
	print("bubble----------LUA--------- " .. tostring(jsonData));
	-- platform
	local t_platform = {};
	t_platform.source = tonumber(source);
	t_platform.isshowMenuBan = tostring(isshowMenuBan);
	local platformjsonData = tiros.json.encode(t_platform);
	
	print("bubble------------lua--------------platofm:" .. tostring(platformjsonData));
	
	if tonumber(isrefresh) == 1 then
		sendmessagetoApp(MSG_BUBBLE_UPDATE,BUBBLE_UPDATE,nil);
	else
		sendmessagetoApp(MSG_BUBBLE_UPDATE,BUBBLE_SHOW,platformjsonData);
	end
	
end
--保存导航时间
local function navitimeData(source,gid,navitime,lon,lat)
	
	local t = {};
	t.source = tostring(6);
	local t_data = {};
	t_data.gid = tostring(gid);
	t_data.time = tostring(navitime);
	t_data.lon = tostring(lon);
	t_data.lat = tostring(lat);
	t_data.mysource = tostring(source);

	t.data = t_data;

	local jsonData = tiros.json.encode(t);
	
	print("bubble----------LUA---------	navitimeData :  " .. tostring(jsonData));

	tiros.moduledata.moduledata_set("web",MAP_BUBBLE_NAVITIME_KEY,jsonData);
	sendmessagetoApp(MSG_BUBBLE_UPDATE,BUBBLE_UPDATE,nil);
		
end

--通知Logic请求导航时间
local function requestNaviTime(jsondata)

	print("bubble---------LUA----------requestNaviTime START");

	if nil == jsondata or "" == jsondata then
		return;
	end
	
	local t_json = tiros.json.decode(jsondata);

	local t = {};
	t.source = tonumber(t_json.source);
	t.gid = tostring(t_json.gid);
	t.lon = tonumber(t_json.lon);
	t.lat = tonumber(t_json.lat);

	local jsonData = tiros.json.encode(t);
	
	print("bubble---------LUA----------requestNaviTime :" .. tostring(jsonData));

	sendmessagetoLogic(EVENT_BUBBLE,EVENT_BUBBLE_REQUESTTIME,jsonData);
end

--通知平台不显示气泡
--source: 那种类型的气泡不显示
local function notshow(source)
	sendmessagetoApp(MSG_BUBBLE_UPDATE,BUBBLE_HIDE,source);
end

--Logic通知Web前端当前用户选择那个气泡
local function currentshowtoweb(source,poigid,lon,lat,index)
	local t = {};
	t.source = tostring(source);
	local t_data = {};
	t_data.gid = tostring(poigid);
	t_data.lon = tostring(lon);
	t_data.lat = tostring(lat);
	t_data.index = tostring(index);
	t.data = t_data;

	local jsonData = tiros.json.encode(t);

	print("bubble---------LUA---------currentshowtoweb: " .. tostring(jsonData));
	
	tiros.moduledata.moduledata_set("web",MAP_BUBBLE_SELECTPOI_KEY,jsonData);
	sendmessagetoApp(MSG_BUBBLE_UPDATE,BUBBLE_UPDATE,nil);
	
end
--清空数据仓库的数据
local function cleardata(base,net,navitime,select)
	print("bubble----------LUA----------cleardata");

	local defaultData = "";
	if 1 == tonumber(base) then
		tiros.moduledata.moduledata_set("web",MAP_BUBBLE_KEY,defaultData);
	end
	if 1 == tonumber(net) then
		tiros.moduledata.moduledata_set("web",MAP_BUBBLE_NET_KEY,defaultData);
	end
	if 1 == tonumber(navitime) then
		tiros.moduledata.moduledata_set("web",MAP_BUBBLE_NAVITIME_KEY,defaultData);
	end
	if 1 == tonumber(select) then
		tiros.moduledata.moduledata_set("web",MAP_BUBBLE_SELECTPOI_KEY,defaultData);
	end
end

local function RebuildMeet2Data(jsonstr,name,address,distance,lon,lat)

	print("LUA-----------json  " .. tostring(jsonstr) );
	local t_json = tiros.json.decode(jsonstr);
	t_json.name = tostring(name);
	t_json.address = tostring(address);
	t_json.distance = tostring(distance);
	t_json.lon = tostring(lon);
	t_json.lat = tostring(lat);
	
	return tiros.json.encode(t_json);

end

--[[
 @brief Web前端通知Logic当前点击那个气泡 -----给Web前端调用
 @param source : 用于标识使用那个功能
 @param gid : POI的gid
 @return 无
--]]
createmodule(gt, "bubblecurrent", function(source,gid)
	print("bubble----------LUA----------WEBres-----11111:" .. tostring(source) .. "	" .. tostring(gid));
	local t = {};
	t.source = tonumber(source);
	t.gid = tostring(gid);
	local jsonData = tiros.json.encode(t);
	
	print("bubble----------LUA----------WEBres-----222: " .. tostring(jsonData));

	sendmessagetoLogic(EVENT_BUBBLE,EVENT_BUBBLE_CURRENT,jsonData);
	-- clear select db
	cleardata(0,0,0,1);
	
	print("bubble----------LUA----------WEBres-----: 3333333333");
	
end)

--[[
 @brief Web前端通知Logic请求当前点的导航 -----给Web前端调用
 @param jsondata : {"source",gid,lat,lon}
 @param gid : POI的gid
 @param lon lat: POI点的经纬度 
 @return 无
--]]
createmodule(gt, "bubblerequestnavitime", function(jsondata)
	requestNaviTime(jsondata);
end)


--[[
 @brief 组织气泡的基础数据并发送给Web前端
 @param ptype string型参数，用于标识该请求的唯一标识符
 @return 无
--]]
createmodule(gt, "bubblebasedata", function(source,name,address,lon,lat,poigid,radius,isrefresh)
	baseData(source,name,address,lon,lat,poigid,radius,isrefresh);
end)

--[[
 @brief 组织气泡的服务端下发数据并发送给Web前端
 @param source string型参数，用于标识使用那个功能
 @param data string型参数，从网络下发的数据
 @param isrefresh string型参数，如果 ＝＝ 1 则为刷新，如果等于0 则为显示
 @param isshowMenuBan 
 @return 无
--]]
createmodule(gt, "bubbleserverdata", function(source,data,isrefresh,isshowMenuBan)
	serverData(source,data,isrefresh,isshowMenuBan);
end)

--[[
 @brief 组织气泡的时间规划数据并发送给Web前端
 @param source : 用于标识使用那个功能
 @param gid : POI的gid
 @param time : 从我的位置到当前POI的规划时间
 @return 无
--]]
createmodule(gt, "bubblenavitimedata", function(source,gid,navitime,lon,lat)
	navitimeData(source,gid,navitime,lon,lat);
end)

--[[
 @brief 通知平台不显示
 @param source : 用于标识使用那个功能
 @param gid : POI的gid
 @param time : 从我的位置到当前POI的规划时间
 @return 无
--]]
createmodule(gt, "bubblenotshow", function(source)
	notshow(source);	
end)
--[[
  @brief logic通知Web前端当前选择那个气泡
  @param source: 用于标识功能
  @param poigid: 用于标识气泡的gid
  @param lon  lat: 经纬度
--]]
createmodule(gt, "bubblecurrentshowtoweb", function(source,poigid,lon,lat,index)
	currentshowtoweb(source,poigid,lon,lat,index);
end)

--[[
   @brief 清空数据仓库
   @param base  1:清空base仓库，0:不清空仓库
   @param net   1:清空net仓库，0:不清空仓库
   @param navitime   1:清空net仓库，0:不清空仓库
   @param select    1:清空net仓库，0:不清空仓库
]]--
createmodule(gt, "bubblecleardata", function(base,net,navitime,select)
	cleardata(base,net,navitime,select);
end)

--[[
   @brief 清空数据仓库
   @param base  1:清空base仓库，0:不清空仓库
   @param net   1:清空net仓库，0:不清空仓库
   @param navitime   1:清空net仓库，0:不清空仓库
   @param select    1:清空net仓库，0:不清空仓库
]]--
createmodule(gt, "bubbleRebuildMeet2Data", function(json,name,address,distance,lon,lat)
	return RebuildMeet2Data(json,name,address,distance,lon,lat);
end)

tiros.bubble = readOnly(gt)
