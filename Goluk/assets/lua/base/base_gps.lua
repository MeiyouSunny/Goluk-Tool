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
 @ 功能描述：gps接口
 @ 提供接口：
（1）(GetLonLat)对外公开获取gps经纬度数据；
（2）(GetLocation)判断gps是否已经定位成功；
</pre>

--]]

require "lua/systemapi/sys_namespace"
require "lua/systemapi/sys_handle"


local longitude = "419031106";
local latitude = "143670827";

local interface = {};
local localobj = getmodule("location");
local moduleobj = getmodule("moduledata");

--对外公开获取gps经纬度数据
--返回当前经纬度及地图中心点经纬度
createmodule(interface,"GetLonLat",function()
	local longitude = "419031106";
	local latitude = "143670827";
	local selflon = "419031106";
	local selflat = "143670827";
	
	--获取当前gps位置
	local lon,lat = localobj.lkgetlastposition_mem();
	if lon == nil or lat == nil then
		local outlon,outlat = localobj.lkgetlastposition_file();
		if outlon ~= nil and outlat ~= nil then
			selflon = tostring(math.ceil(outlon));
			selflat = tostring(math.ceil(outlat));
			longitude = tostring(math.ceil(outlon));
			latitude = tostring(math.ceil(outlat));
			if tonumber(outlon) < tonumber(outlat) then
				selflon = tostring(math.ceil(outlat));
				selflat = tostring(math.ceil(outlon));
				longitude = tostring(math.ceil(outlat));
				latitude = tostring(math.ceil(outlon));
			end
		end
	else
		selflon = tostring(math.ceil(lon));
		selflat = tostring(math.ceil(lat));
		longitude = tostring(math.ceil(lon));
		latitude = tostring(math.ceil(lat));
		if tonumber(lon) < tonumber(lat) then
			selflon = tostring(math.ceil(lat));
			selflat = tostring(math.ceil(lon));
			longitude = tostring(math.ceil(lat));
			latitude = tostring(math.ceil(lon));
		end
	end

	--获取地图中心点经纬度
	local mapcenterlon = moduleobj.moduledata_get("web","mapcenterlon");
	local mapcenterlat = moduleobj.moduledata_get("web","mapcenterlat");
	if mapcenterlon ~= "" and mapcenterlon ~= nil and mapcenterlon ~= "0" and mapcenterlat ~= "" and mapcenterlat ~= nil and mapcenterlat ~= "0" then
		longitude = tostring(math.ceil(mapcenterlon));
		latitude = tostring(math.ceil(mapcenterlat));
		
		--[[
		if longitude < latitude then
			longitude = tostring(math.ceil(mapcenterlat));
			latitude = tostring(math.ceil(mapcenterlon));
		end
		--]]
	end

	--用户gps 经纬度 地图中心点经纬度
	return selflon,selflat,longitude,latitude;
end);


--判断gps是否已经定位成功
--返回定位的经纬度
createmodule(interface,"GetLocation",function()
	--获取当前gps位置
	local lon,lat = localobj.lkgetlastposition_mem();
	if lon ~= nil and lat ~= nil and lon ~= "" and lat ~= "" then
		if lon < lat then
			return lat,lon;
		end
	end
	return lon,lat;
end);

--设置 tiros.base.gps 为只读权限
tiros.base.gps = readOnly(interface);
