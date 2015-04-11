--[[
 @描述：首次GPS定位成功后，以GPS经纬度请求服务器，解析传给调用者
 @编写人：fengfx
 @创建日期：2012-12-13 下午 14:15:00
 @修改内容：2012-12-14 fengfx 去掉无用参数、函数，修改HTTP回调逻辑与错误处理机制
 @版本：0.1.1
--]]
require "lua/systemapi/sys_namespace"
require "lua/systemapi/sys_handle"
require "lua/systemapi/sys_http"
require "lua/framework/sys_framework"
require "lua/commfunc"

--资源文件中编号
local RES_STR_GENERAL_GET_URL = 2101;

--资源文件地址路径
local RES_FILE_PATH = "fs0:/res/api/api.rs";

--获取URL:服务器地址及端口号  http://192.168.1.95:8081/general_Get
--local POSITION_REPORT_URL = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_GENERAL_GET_URL);

--请求中header参数 actionlocation=/navidog2Goods/hotelInterface.htm
local HEADER = "actionlocation:/navidog2Goods/hotelInterface.htm"

--记录请求经纬度
local gnLon = nil;
local gnLat = nil;

--请求次数
local gnCount = 1;

--[[
gtpositionReportList存放搜索数据，结构和数据如下
gtpositionReportList = {
	ptype = {
		1:调用方类型：0：lua，1：js， 2：c
		2:待解析数据
		3:数据，--解析后的数据
		4:数据，--暂nil 
	}
}
--]]
local gtpositionReportList = {};

--gtpositionReportList的weak表
local gtpositionReportWeakList = {};

--设置gtpositionReportWeakList为弱表
setmetatable(gtpositionReportWeakList, {__mode = "v"});

--[[
 通过经纬度向服务器发送数据请求
 @param ptype string型参数，唯一标识符
 @param ntype number型参数，用于标识该回调函数类型（lua：0，js：1，c：2）
 @param cbfunc 注册的回调函数地址
 @param nlon 大头针点的经度
 @param nlat 大头针点的纬度
 @param nUser number型参数，可为nil，c端注册的调用者参数地址
 @return 无
--]]
local function positionReportRequest(ptype, ntype, nlon, nlat)
	local tReportList = nil;
	local jsonstring = nil;
	local sURL = nil;
	--通过ptype从gtpositionReportWeakList中获取对应的大头针表
	tReportList = getHandle(gtpositionReportWeakList, ptype);
	--如果gtpositionReportWeakList中没有相应的大头针表，则重新创建一个空表
	if tReportList == nil then
		tReportList = {};
	end
	tReportList[1] = ntype;
	tReportList[2] = "";		--初始化 预留存储待解析数据

	--记录经纬度
	gnLon = nlon;
	gnLat = nlat;
	local POSITION_REPORT_URL = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_GENERAL_GET_URL);
	sURL = POSITION_REPORT_URL .. "?method=getCityThree&xieyi=100" .. "&lon=" .. tostring(nlon) .. "&lat=" .. tostring(nlat);
	--sURL = "http://192.168.1.95:8081/general_Get" .. "?method=getCityThree&xieyi=100" .. "&lon=" .. tostring(nlon) .. "&lat=" .. tostring(nlat);
--print(sURL)
	tiros.http.httpabort(ptype);--取消之前同类请求
	--将修改后或新创建的表tReportList重新注册到gtpositionReportList和gtpositionReportWeakList中
	registerHandle(gtpositionReportList, gtpositionReportWeakList, ptype, tReportList);
        return tiros.http.httpsendforlua("cdc_client", "ck_map", ptype, sURL, positionReportHttpNotify, nil, HEADER);
end

--[[
 解析返回json串，获取字符串数据
 @param ptype string型参数，唯一标识符
 @param sjsonStr json格式的字符串数据：完整的包体数据
 @return bool值 解析成功返回true，否则返回false
--]]
local function jsonStrParser(ptype, sjsonStr)
	local tjsonObj = nil;
	local tReportList = nil;
	if nil == sjsonStr then
		return false;
	end
	tjsonObj = tiros.json.decode(sjsonStr);		--解析json串
	if nil == tjsonObj then
		return false;
	end
	
	--数据 {“success”:true/false, “data”:{}, “msg”:””}
	if false == tjsonObj.success then
		return false;
	elseif true == tjsonObj.success then
		tReportList = getHandle(gtpositionReportList, ptype);
		if tReportList == nil then
			return false;
		else
			tReportList[3] = tiros.json.encode(tjsonObj.data);	--解析后的数据
			tReportList[2] = "";	--清空数据
			tiros.moduledata.moduledata_set("web", "currentcity_ptp", tReportList[3]);
--[[data:{
	“province”:[“code”,“name”,“lon”,“lat”],
 	“city”: [“code”,“name”,“lon”,“lat”],
	“county”: [“code”,“name”,“lon”,“lat”]	
} --]]
--local temp = tiros.moduledata.moduledata_get("web", "currentcity_ptp");
--print("fengfx --- data --- ", tReportList[3])
--print("fengfx --- DATA ---", temp)
			return true;
		end
	end
end

--[[
 全局的http回调函数
 @param ptype string型参数，唯一标识符
 @param nEvent number型参数，http回调事件类型
 @param param1 当nEvent=2：状态码；当nEvent=3：包体大小(uint32)；当nEvent=5：错误类型
 @param param2 当nEvent=2：数据体长度，状态码为200系列，才有该事件；当nEvent=3：包体数据(void *)；当nEvent=5：错误码，600之后为自定义错误
 @return 无
--]]
DeclareGlobal("positionReportHttpNotify", function (ptype, nEvent, param1, param2)
	local tReportList = nil;
	local nStatus = 0;  --标识当前网络、数据状态，0为异常，1为正常
	tReportList = getHandle(gtpositionReportList, ptype);
	if tReportList ~= nil then
		if nEvent == 2 then
			if param1 ~= 200 then	--不是200，都为http错误，param1返回错信息
				nStatus = 0;
				tReportList = nil;
				tiros.http.httpabort(ptype);
				gnCount = gnCount + 1;
				if (gnCount ~= 3) then
					positionReportRequest(ptype, 0, gnLon, gnLat);
				end
			end
		elseif nEvent == 3 then
			if tReportList ~= nil then
				tReportList[2] = tReportList[2] .. param2;
			end
		elseif nEvent == 4 then
			if jsonStrParser(ptype, tReportList[2]) then			--处理应答提，json数据解析
				nStatus = 1;
				tReportList = nil;
				tiros.http.httpabort(ptype);
			end
		elseif nEvent == 5 then
			nStatus = 0;
			tReportList = nil;
			tiros.http.httpabort(ptype);
			gnCount = gnCount + 1;
			if (gnCount ~= 3) then
				positionReportRequest(ptype, 0, gnLon, gnLat);
			end
		end
	end
end)

--对外接口，此声明后都为全局接口。
local interface = {};

--[[
 对外声明lua层调用请求函数接口
 @param ptype string型参数，lua端用于标识该请求的唯一标识符
 @param cbfunc function型参数，lua端注册的回调函数地址
 @param nlon 大头针点的经度
 @param nlat 大头针点的纬度
 @return 请求成功返回true，失败返回false
--]]
createmodule(interface, "positionreportforlua", function (ptype, nlon, nlat)
	return positionReportRequest(ptype, 0, nlon, nlat);
end)

--[[
 对外声明c层调用请求函数接口
 @param ptype string型参数，c端用于标识该请求的唯一标识符
 @param cbfunc number型参数，c端注册的回调函数地址
 @param nlon 大头针点的经度
 @param nlat 大头针点的纬度
 @param nUser number型参数，可为nil，c端注册的调用者参数地址
 @return 请求成功返回true，失败返回false
--]]
createmodule(interface, "positionreportforc", function (ptype, nlon, nlat)
	return positionReportRequest(ptype, 2, nlon, nlat);
end)

--[[
 对外声明终止请求函数接口
 @param ptype string型参数，用于标识该请求的唯一标识符
 @return 无
--]]
createmodule(interface, "positionreportabort", function (ptype)
	local tReportList = getHandle(gtpositionReportList, ptype);
	if (tReportList ~= nil) then
		tReportList = nil;
	end
	tiros.http.httpabort(ptype);
end)

--添加interface内接口为只读属性，同时注册接口到tiros.PSTreport中
tiros.PSTreport = readOnly(interface);
