--
--Http性能分析脚本
--

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/moduledata"
require"lua/tapi"
require"lua/udpreport"
require"lua/json"


local _gUserTable = {}
local Plugin_PMAnalysis_Event_Analysis = 512
local Plugin_PMAnalysis_Event_Cancel = 513


local MODULETYPE = "type"
local DATAVER = "dataver"
local MOBILEID = "mobileid"
local VERSION = "version"
local DATE = "date"
local ENDTIME = "endtime"
local LON = "lon"
local LAT = "lat"
local NETTYPE = "nettype"
local SERVICETYPE = "servertype"
local TIMECONSUMING = "timeConsuming"
local ISSUCCESS = "issuccess"
local ERRORCODE = "errorcode"
local JIP = "ip"

local PM_VER = "20120508"
--添加最大异常应答时间为100秒
local MAX_RESP_TIME = 100000;
--最大应答时间映射错误码
local MAX_RESP_ERROR = 1000;

--[[
htable[0]-->发起请求计时
htable[1]-->收到响应计时
htable[2]-->请求结束计时
htable[3]-->http响应状态码
]]--


local function getUserHandle(htype)
	local htable =getHandle(_gUserTable,htype)
	if htable == nil then
		htable = {};
	end
	registerHandle(_gUserTable,nil,htype,htable)
	return htable;	
end

--[[]]--
-- 格林威治时间转换为北京时间
-- 输入参数 年(4位)，月，日，时，分,秒
-- 输出参数 年(4位)，月，日，时，分,秒
local function localtime(y, m, d, hh, mm, ss )

	hh =  hh+8    -- 格林威治时间 + 8 小时 = 北京时间

	if ( hh < 24 ) then   --没有跨天，则计算完成
		return y, m, d, hh, mm, ss		
	end

	-----下面是跨天后的计算--------------------
	
	hh = hh-24
	d = d+1        -- 日期加一天

	--按月判断
	if (m ==4) or (m==6) or (m==9) or (m==11) then  --跨小月的判断
		if d > 30 then 
			d = 1
			m = m+1
		end
	elseif (m ==1) or (m==3) or (m==5) or (m==7) or (m==8) or (m==10) then  --跨大月的判断
		if d > 31 then 
			d = 1
			m = m+1
		end
	elseif m==12 then	--12 月，要判断是否跨年
		if d>31 then
			y = y+1
			d = 1
			m = 1
		end
	elseif m==2 then	--2 月，要判断是否是闰年
		if( ( y%400 == 0 ) or       	     -- 能被400整除，一定是闰年
       		( y%4 ==0 ) and ( y%100 ~=0 ) ) then 	--能被4整除，但不能被100整除，一定是闰年
			if( d>29 ) then	--闰年2月，可以有29号
				m = 3
				d = 1
			end
		elseif ( d>28 ) then		--非闰年2月，可以有28号
			m = 3
			d = 1
		end		
		
	end

	return y, m, d, hh, mm, ss --计算完成，开始输出
end

local function UPD_Encode_Report(moduletype, servicetype, resptime, alltime, errorcode)
	local tb = {}
	tb.type = moduletype
	tb.dataver = PM_VER

	local smobileid = tiros.moduledata.moduledata_get("framework", "mobileid");
	if smobileid == nil then
		smobileid = "123456789012345";
	end
	tb.mobileid = smobileid

	local sversion = tiros.moduledata.moduledata_get("framework", "version");
	if sversion == nil then
		sversion = "0.0.0";
	end
	tb.version =  sversion

	local date = string.format("%04u%02u%02u%02u%02u%02u",localtime(timelib.time()));
	tb.date =  date

	tb.endtime = alltime

	local lon;
	local nlon = tiros.moduledata.moduledata_get("logic", "lon");
	if nlon == nil then
		nlon = 0;
	else
		nlon = tonumber(nlon);
		if nlon == nil then
			nlon = 0;
		end
	end

	tb.lon = nlon

	local lat;
	local nlat = tiros.moduledata.moduledata_get("logic", "lat");
	if nlat == nil then
		nlat = 0;
	else
		nlat = tonumber(nlat);
		if nlat == nil then
			nlat = 0;
		end
	end
	
	tb.lat = nlat
	tb.nettype = tiros.tapi.tapigetnettype();
	tb.servertype =  servicetype
	tb.timeConsuming = resptime

	local issuccess;
	if  errorcode == 200 then
		issuccess = 0;
	else
		issuccess = 1;
	end

	tb.issuccess = issuccess
    if errorcode == 200 and resptime >=  MAX_RESP_TIME then
        tb.errorcode = MAX_RESP_ERROR
    else
        tb.errorcode = errorcode
    end

	local jsonstr = tiros.json.encode(tb)
	tiros.udpreport.UdpReport(jsonstr);
	jsonstr = nil
	tb = nil
end

--[[
moduletype: 模块名称
servicetype: 服务名称
success: 网络访问是否成功
param1: 成功-应答时间/失败-错误类型
param2: 成功-完整网络接收时间/错误码
]]
local function HttpAlysis( moduletype, servicetype, success, param1, param2 )
	if success == 1 then
		--网络访问成功
		UPD_Encode_Report(moduletype, servicetype, param1, param2, 200);
	else
		--网络访问失败
		UPD_Encode_Report(moduletype, servicetype, 0, 0, param2);
	end
end

--接口table
local interface = {}

--对外声明Http性能分析接口
--输出：无
createmodule(interface,"httppmanalysis", function (moduletype, servicetype, success, param1, param2)
	return HttpAlysis( moduletype, servicetype, success, param1, param2 );
end)
--[[]]--
tiros.nethttpperformanceanalysis = readOnly(interface)

