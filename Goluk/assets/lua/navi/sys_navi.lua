-- @描述:导航概要界面
-- @编写人:lyfsteven
-- @创建日期: 2013-12-02

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/framework/sys_framework"
require"lua/systemapi/sys_file"
require"lua/json/sys_json"
require"lua/commfunc"
require"lua/http"
require"lua/moduledata"
require"lua/database"
require"lua/mediaplayer"
require (tiros.web.FilePath .. "bubble/lua/routeview/sys_routeview")


local interface = {}

--=========================================内部数据=========================================

--=========================================内部实现=========================================


--===================================

local function sys_SwitchGeneralRoute(id)
    print ("logic navi sys_SwitchGeneralRoute web notify logic to change the line")
    local nFunction = tiros.moduledata.moduledata_get("framework", "pLogicFunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "pLogicUser");
    if nFunction ~= nil then
        commlib.universalnotifyFun(nFunction,"navi", nUser, 1, 50, tostring(id));        
    end
end

local function sys_SetGeneralRouteMoreInfoData(data)
    print("logic navi sys_SetGeneralRouteMoreInfo" .. data);
    tiros.moduledata.moduledata_set("web", "navigeneralroutemoreinfodata_ptp", data);
end

local function sys_SetGeneralRouteStartAndEndPointData(data)
    print("navi sys_SetGeneralRouteStartAndEndPointData" .. data);
    tiros.moduledata.moduledata_set("web", "navigeneralroutestartendpointdata_ptp", data);
end


local function sys_SetGeneralRouteLineData(data)
    print("logic navi sys_SetGeneralRouteLineData" .. data);
    tiros.moduledata.moduledata_set("web", "navigeneralroutelinedata_ptp", data);
end

local function sys_SendWebSwitchRouteEvent(line)
    print("logic navi sys_SendWebSwitchRouteEvent:" .. tostring(line));
    tiros.moduledata.moduledata_set("web", "navigeneralroutecurrentline_ptp", line);
    tiros.web.routeview.RouteChanged2Web(line);
end

local function sys_SetGeneralRouteCurrentLine(line)
    print("logic navi sys_SetGeneralRouteCurrentLine:" .. tostring(line));
    tiros.moduledata.moduledata_set("web", "navigeneralroutecurrentline_ptp", line);
end

--=========================================外部接口=========================================
--@@描述:web通知logic概要路线的web数据, 1为常规路线，2为推荐路线
createmodule(interface, "SwitchGeneralRoute", function(id)
	return sys_SwitchGeneralRoute(id);
end)


--[[
require lua路径：webres/bubble/lua/routeview
lua接口：RouteChanged2Web(id)  路线ID，id=1为common路线，id=2为approve路线
lua全称：tiros.web.routeview.RouteChanged2Web
]]
--@@描述:logic通知web概要路线更改, 1为常规路线，2为推荐路线
createmodule(interface, "SendWebSwitchRouteEvent", function(line)
	return sys_SendWebSwitchRouteEvent(line);
end)

--@@描述:概要路线点击更多按钮所需要的数据NaviEnvironment
--    typedef struct _NaviEnvironment
--    {
--        NaviPlanType iPlanType;                         --/<路线规划方式
--        bool bAvoidTollRoad;                            --/<是否避开收费道路(false：避开  true：不避开)
--        bool bAvoidTrafficLights;                       --/<是否避开红绿灯(false:不避开 ture:避开)
--        bool bVoiceCueSimple;                           --/<语音提示(false:详细提示、true:简单提示)
--        NaviVoiceType iNaviVoiceType;                   --/<导航语音类型
--        NaviBypassPromptType iNaviBypassPromptType;     --/<绕行提示设置
--        NaviMode iNaviMode;                             --/<导航类型（驾车、步行）
--    } NaviEnvironment;
createmodule(interface, "SetGeneralRouteMoreInfoData", function(data)
	return sys_SetGeneralRouteMoreInfoData(data);
end)
--@@描述:概要路线的起点终点的数据
createmodule(interface, "SetGeneralRouteStartAndEndPointData", function(data)
	return sys_SetGeneralRouteStartAndEndPointData(data);
end)

--@@描述:概要路线的web弹出框的数据
createmodule(interface, "SetGeneralRouteLineData", function(data)
	return sys_SetGeneralRouteLineData(data);
end)

--@@描述:当前用户选择了哪条线，0表示只有常规路线一条，1表示用户选择了两条线中的常规线，2表示用户选择推荐线
createmodule(interface, "SetGeneralRouteCurrentLine", function(line)
	return sys_SetGeneralRouteCurrentLine(line);
end)



tiros.navi = readOnly(interface);
--file end


