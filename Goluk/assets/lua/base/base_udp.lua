--[[
<pre>
 * 1.全局函数首字母大写
 * 2.私有函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写
 * 7.后续人员开发保证代码格式一致

 @ 创建时间:2013-07-19
 @ 功能描述：udp接口
 @ 提供接口：
（1）(DataReport)对外公开udp数据上报接口；
</pre>

--]]

require "lua/systemapi/sys_namespace"
require "lua/systemapi/sys_handle"
require "lua/udpmanager"
require "lua/systemapi/sys_socket"
require "lua/framework/sys_framework"
require "lua/json"
require "lua/base/base_common"
require "lua/base/base_gps"

local interface = {};

local moduleobj = getmodule("moduledata");

local function report(tb)
	local str = "";
	local obj = {};
	--获取udp上报公共数据
	obj.mobileid = moduleobj.moduledata_get("framework","mobileid");
	obj.version = moduleobj.moduledata_get("framework","version");
	local uid = moduleobj.moduledata_get("framework","uid");
	if uid ~= "" and uid ~= nil then
		obj.uid = uid;
	end
	obj.date = tiros.base.common.GetDate();
	local lon,lat = tiros.base.gps.GetLonLat();
	obj.lon = tonumber(lon);
	obj.lat = tonumber(lat);
	
	--获取其他上报数据
	if type(tb) == "table" then
		for k,v in pairs(tb) do
			obj[k] = v;
		end
	end

	local str = tiros.json.encode(obj);
	tiros.udpreport.UdpReport(str);
end

--对外公开lua接口错误上报
--stype udp上报动态标识
--etype 错误类型
createmodule(interface,"ErrorReport",function(stype,etype)
	local obj = {};
	obj.type = "web_client";
	obj.dataver = "20120508";
	obj.nettype = 0;
	obj.servertype = stype;
	obj.timeConsuming = 0;
	obj.issuccess = 1;
	obj.errorcode = etype;
	obj.endtime = 0;
	obj.ip = "";
	report(obj);
end);

--udp上报接口
--tb udp上报数据,不需要传功能参数,如: version,uid,moblieid,lon,lat,date
createmodule(interface,"DataReport",function(tb)
	if type(tb) == "table" then
		report(tb);
	end
end);

--设置只读
tiros.base.udp = readOnly(interface);
