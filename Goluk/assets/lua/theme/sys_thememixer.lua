
require "lua/systemapi/sys_namespace"
require "lua/systemapi/sys_handle"
require "lua/systemapi/sys_http"
require "lua/framework/sys_framework"
require "lua/themeconfig"
require "lua/json"

--资源文件中编号
local RES_STR_PST_URL = 1002;

--资源文件地址路径
local RES_FILE_PATH = "fs0:/res/api/api.rs";

--获取URL:服务器地址及端口号 目前使用测试服务器及端口 http://119.254.82.237:8080
--local POI_DESCRIPTION_URL = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_PST_URL);

--[[
gtThemeMixerList存放主题兴趣点融合数据，结构和数据如下
gtThemeMixerList = {
	ptype = {
		1:调用方类型：0：lua，1：js， 2：c
		2:lua回调函数地址
		3:js注册回调函数名称
		4:c回调函数指针地址
		5:c调用者传输数据地址
		6:待解析数据
		success:yes|no
	}
}
--]]

--主题兴趣点融合table
local gtThemeMixerList = nil;
local RES_FILE_PATH = "fs0:/res/api/api.rs";
local RES_STR_THEMEMIXER_URL = 1803;
--local ThemeMixerServerUrl = "http://dev8.lbs8.com/theme_blendSort?method=marksortquery&member=0&uid=0&";--//融合点主题类别
--local ThemeMixerServerUrl = "http://192.168.1.79:8081/theme_blendSort?method=marksortquery&member=0&";--//融合点主题类别
--local ThemeMixerServerUrl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_THEMEMIXER_URL);
--ThemeMixerServerUrl = ThemeMixerServerUrl .. "?method=marksortquery&member=0&";
local THEMECODE_TICKET = "000002000001";
local THEMECODE_HOTEL = "000002000004"
local THEMECODE_DANGEROUSROAD = "000004000001"

--地图界面设置部分主题临时设置数据,主要用于在主题融合还没有完成前如果触发主题设置修改,则需保留此次修改,等主题融合完毕之后再设置
local glThemeTempSetData = nil;

--更新地图界面中主题配置修改到已经融合后的主题配置中去
local function getURL()
	local ThemeMixerServerUrl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_THEMEMIXER_URL);
	ThemeMixerServerUrl = ThemeMixerServerUrl .. "?method=marksortquery&member=0&";
	return ThemeMixerServerUrl;
end

--匹配增删指定表元素
--element对象结构:
--.id :二级主题编码id
--.state:1/0/-1 打开/关闭/默认
--t对象结构为全编码id的数组从1~#t
local function table_modify_element(t, element)
    if type(t) == "table" and type(element) == "table" then
        if element.state == -1 then --如果用户没有修改,则为默认
            return
        end
        local bfind = false;
        for i = 1, #t do
            if t[i] == element.id then
                bfind = true;
                if element.state== 0 then   --删除
                    table.remove(t , i);
                    return;
                end
            end
        end
        if bfind == false and element.state == 1 then --没有找到,且是要添加的主题,则新增主题
            t[#t+1] = element.id;
        end
    end
end

--更新地图界面中主题配置修改到已经融合后的主题配置中去
local function updatespecialthemedataset(t, data)
    --景点门票二级主题配置
    table_modify_element(t, data.ticket);
    --快捷酒店二级主题配置
    table_modify_element(t, data.hotel);
    --危险路段二级主题配置
    table_modify_element(t, data.dangerousroad);
end

--保存地图界面中修改主题配置信息融合到本地配置信息文件中
local function savespecialthemedata(data)
    if data.ticket.state == -1 and data.hotel.state== -1 and data.dangerousroad.state == -1 then
        return;
    end
    local tThemeConfig = nil;
    local mixerdata = tiros.themeconfig.ThemeConfigRead();
    --print("theme read data is " .. mixerdata);
	if mixerdata == nil or #mixerdata == 0 then
		--数据为空
        tThemeConfig = {};
	else
		tThemeConfig  =  tiros.json.decode(mixerdata);
		if tThemeConfig == nil or type(tThemeConfig) ~= "table" then
            tThemeConfig = {};
		end
    end
    --查找本地配置信息中是否包含景点门票，危险路段和快捷酒店相关主题配置信息
    local bticket = false;
    local bhotel = false;
    local bdangerousroad = false;
    for k,v in pairs(tThemeConfig) do
        if v.id == data.ticket.id then
            if data.ticket.state == 0 then
                tThemeConfig[k].status = "off"
            elseif data.ticket.state == 1 then
                tThemeConfig[k].status = "on"
            end
            bticket = true;
        elseif v.id == data.hotel.id then
            if data.hotel.state == 0 then
                tThemeConfig[k].status = "off"
            elseif data.hotel.state == 1 then
                tThemeConfig[k].status = "on"
            end
            bhotel = true;
        elseif v.id == data.dangerousroad.id then
            if data.dangerousroad.state == 0 then
                tThemeConfig[k].status = "off"
            elseif data.dangerousroad.state == 1 then
                tThemeConfig[k].status = "on"
            end
            bdangerousroad = true;
        end
    end
    if bticket == false and data.ticket.state ~= -1 then
        local t1 = {};
        if data.ticket.state == 0 then
            t1.status = "off";
        else
            t1.status = "on";
        end
        t1.id = data.ticket.id;
        t1.changed = true;
        tThemeConfig[#tThemeConfig+1] = t1;
    end
    if bhotel == false and data.hotel.state ~= -1 then
        local t1 = {};
        if data.hotel.state == 0 then
            t1.status = "off";
        else
            t1.status = "on";
        end
        t1.id = data.hotel.id;
        t1.changed = true;
        tThemeConfig[#tThemeConfig+1] = t1;
    end
    if bdangerousroad == false and data.dangerousroad.state ~= -1 then
        local t1 = {};
        if data.dangerousroad.state == 0 then
            t1.status = "off";
        else
            t1.status = "on";
        end
        t1.id = data.dangerousroad.id;
        t1.changed = true;
        tThemeConfig[#tThemeConfig+1] = t1;
    end
    mixerdata = tiros.json.encode(tThemeConfig);
    --print("theme will write data " .. mixerdata);
    tiros.themeconfig.ThemeConfigWrite(mixerdata);
end

--获取本地主题设置配置信息
local function getThemeMixerConfigInfo()
	local sSelectTheme = "";
	local sUnSelectTheme = "";
	local mixerdata = tiros.themeconfig.ThemeConfigRead();
	if mixerdata == nil or #mixerdata == 0 then
		--数据为空
	else
		local tThemeConfig  =  tiros.json.decode(mixerdata);
		if tThemeConfig == nil or type(tThemeConfig) ~= "table" then
		
		else
				for k,v in pairs(tThemeConfig) do
					if v.status == "on" then
						sSelectTheme = sSelectTheme .. v.id .. ",";
					elseif v.status == "off" then
						sUnSelectTheme = sUnSelectTheme .. v.id .. ",";
					end
				end
				if #sSelectTheme > 0 then
					sSelectTheme = string.sub(sSelectTheme, 1, -2);
				end
				if #sUnSelectTheme > 0 then
					sUnSelectTheme = string.sub(sUnSelectTheme, 1, -2);
				end
    	end
		
	end
	return sSelectTheme, sUnSelectTheme;
end

local function parseThememixerData()
	if type(gtThemeMixerList[6]) == "string" then
		local t = tiros.json.decode(gtThemeMixerList[6]);
		if t ~= nil and type(t) == "table" then
			if t.success == true then
			--成功
				local t1 = {};
				local i = 1;
				for k,v in pairs(t.markcode) do
					t1[i] = v;
					i = i + 1;
				end
                if glThemeTempSetData ~= nil then --如果在融合完成前,有新的主题配置更改,则修改
                    updatespecialthemedataset(t1, glThemeTempSetData);
                end
				tiros.moduledata.moduledata_set('web','theme_ptp',t1);
			end
		end	
	end
end

local function ThemeMixer_Notify(sType, event, dwparam1, dwparam2)
	if event == 1 then
	
	elseif event == 2 then
		
	elseif event == 3 then
		gtThemeMixerList[6] = gtThemeMixerList[6] .. dwparam2;
	elseif event == 4 then
		parseThememixerData();
		gtThemeMixerList.success = "yes";
		commlib.universalnotifyFun(gtThemeMixerList[4], "theme", gtThemeMixerList[5], 1, 0, "");
	else--error
		print("ThemeMixer_Notify Error!\r\n", dwparam1, dwparam2);
        gtThemeMixerList.success = "no";
		commlib.universalnotifyFun(gtThemeMixerList[4], "theme", gtThemeMixerList[5], 0, 0, "");
	end
end

local function thememixerCancel()
	tiros.http.httpabort("ThemeMixer");--取消之前同类请求
    if gtThemeMixerList ~= nil then
        gtThemeMixerList.success = "no";
    end
end
--[[
--]]
local function thememixerRequest(ntype, cbfunc, nUser)	
	if gtThemeMixerList == nil then
		gtThemeMixerList = {};
	elseif gtThemeMixerList.success == "yes"  or gtThemeMixerList.success == "wait" then
		--主题融合点只融合一次
		return false;
	end
    gtThemeMixerList.success = "wait";
	gtThemeMixerList[1] = ntype;	--注册回调函数 c回调注册User
	if ntype == 0 then			--lua脚本注册回调函数
		gtThemeMixerList[2] = cbfunc;
	elseif ntype == 1 then		--js注册回调函数
		gtThemeMixerList[3] = cbfunc;
	else						--c注册回调函数和User
		gtThemeMixerList[4] = cbfunc;
		gtThemeMixerList[5] = nUser;
	end
	gtThemeMixerList[6] = "";		--初始化 预留存储待解析数据
--获取上次用户设置融合点配置数据	
	local sSelectTheme, sUnSelectTheme = getThemeMixerConfigInfo();
	local sThemeMixerServerUrl = getURL();
	local RequestThemeMixerServerUrl = sThemeMixerServerUrl .. "markcode=" .. sSelectTheme.."&" .. "nomarkcode=" .. sUnSelectTheme;
	tiros.http.httpabort("ThemeMixer");--取消之前同类请求
	print(RequestThemeMixerServerUrl);
	--将修改后或新创建的表gtThemeMixerList重新注册到gtpositionDescriptionList和gtpositionDescriptionWeakList中
        return tiros.http.httpsendforlua("cdc_client", "thememixer", "ThemeMixer", RequestThemeMixerServerUrl, ThemeMixer_Notify, nil,"uid:0");
end

local function thememixerdatacount()
	--此处保证在没有融合完主题之前，则直接默认为融合完毕，因为如果触发此函数会有2种情况
	--1为融合完毕后开始请求主题数据，2为在客户端没有融合完毕之前，通过web进行融合后开始请求主题数据
	if gtThemeMixerList == nil then
		gtThemeMixerList = {};
	end
	gtThemeMixerList.success = "yes";
	local t = tiros.moduledata.moduledata_get('web','theme_ptp');
	if t ~= nil and type(t) == "table" then
		return #t
	end
	return 0
end

local function thememixerdata(index)
	local t = tiros.moduledata.moduledata_get('web','theme_ptp');
	if t ~= nil and type(t) == "table"  and #t > index then
		if t[index+1] ~= nil then	
			return t[index+1];
		end
	end
	return ""
end

--新增地图界面直接对部分二级主题设置与关闭接口
--themedata数据结构:
--themedata = {};
--.ticket = 1/0/-1(打开/关闭/默认)
--.hotel = 1/0/-1(打开/关闭/默认)
--.dangerousroad = 1/0/-1(打开/关闭/默认)
--返回：1需要刷新主题，0：不需要刷新主题
local function specialthemecodeset(themedata)
	if type(themedata) ~= "string" or #themedata <= 0 then
		return 0;
	end
	local themeObj = tiros.json.decode(themedata);
	if type(themeObj) == "table" then
		local data = {};
		data.ticket = {};
		data.ticket.id = THEMECODE_TICKET;
		data.ticket.state = themeObj.ticket;
		data.hotel = {};
		data.hotel.id = THEMECODE_HOTEL;
		data.hotel.state = themeObj.hotel;
        data.dangerousroad = {};
        data.dangerousroad.id = THEMECODE_DANGEROUSROAD;
        data.dangerousroad.state = themeObj.dangerousroad;
		savespecialthemedata(data);
		--主题融合完毕
        if gtThemeMixerList.success == "yes" then
			local t = tiros.moduledata.moduledata_get('web','theme_ptp');
            if t == nil or type(t) ~= "table" then
                --更新主题配置
                t = {};
            end
            updatespecialthemedataset(t, data);
            tiros.moduledata.moduledata_set('web','theme_ptp',t);
            glThemeTempSetData = nil;
            return 1;
        else --主题没有融合完毕
			glThemeTempSetData = data;
        end
    end
    return 0;
end

--获取景点门票及快捷酒店主题编码的本地存储状态
local function specialthemecodeget()
	local tstatus = 0;      --景点门票主题状态
    local hstatus = 0;      --快捷酒店主题状态
    local drstatus = 0;      --危险路段主题状态
    --主题融合完毕
    if gtThemeMixerList.success == "yes" then
		local tThemeConfig = tiros.moduledata.moduledata_get('web','theme_ptp');
		if type(tThemeConfig) == "table" then
			for k,v in pairs(tThemeConfig) do
				if v == THEMECODE_TICKET then            --景点门票
					tstatus = 1;
				elseif v == THEMECODE_HOTEL  then     	--快捷酒店
					hstatus = 1;
				elseif v == THEMECODE_DANGEROUSROAD  then     	--危险路段
					drstatus = 1;
				end
			end
		end
    else 
		--主题没有融合完毕
		--读取本地配置文件
		local mixerdata = tiros.themeconfig.ThemeConfigRead();
		if type(mixerdata) == "string" and #mixerdata > 0 then
            local tThemeConfig  =  tiros.json.decode(mixerdata);
			if type(tThemeConfig) == "table" then
                for k,v in pairs(tThemeConfig) do
					if v.id == THEMECODE_TICKET then        --景点门票
						if v.status == "on" then
							tstatus = 1;
						end
					elseif v.id == THEMECODE_HOTEL then     --快捷酒店
						if v.status == "on" then
							hstatus = 1;
						end
					elseif v.id == THEMECODE_DANGEROUSROAD then     --危险路段
						if v.status == "on" then
							drstatus = 1;
						end
					end
				end
			end
		end
	end
	return  tstatus, hstatus, drstatus;
end

--对外接口，此声明后都为全局接口。
local interface = {};

createmodule(interface, "thememixerdata", function (index)
	return thememixerdata(index);
end)

createmodule(interface, "thememixerdatacount", function ()
	return thememixerdatacount();
end)

createmodule(interface, "thememixerforc", function (cbfunc, pvuser)
	return thememixerRequest(2, cbfunc, pvuser);
end)

createmodule(interface, "thememixercancel", function ()
	thememixerCancel();
end)

createmodule(interface, "specialthemecodeget", function ()
	return specialthemecodeget();
end)

createmodule(interface, "specialthemecodeset", function (themedata)
	return specialthemecodeset(themedata);
end)

--添加interface内接口为只读属性，同时注册接口到tiros.PSTdescription中
tiros.thememixer = readOnly(interface);

