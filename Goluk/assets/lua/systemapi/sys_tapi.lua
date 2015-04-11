--tapi接口封装
--目前主要对外tapigetbscount及tapigetbsbyindex,tapigetconnwifiinfo,tapigetmobileid接口

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"


--tapilist：全局变量，用于存放正在使用的所有tapi句柄的
local _gTapi
local TAPI_ERR_SUCCESS = 1

--创建tapi句柄函数接口
----输出：实际创建的tapi句柄
local function tapiget()
	if _gTapi == nil  then	
	   	_gTapi = tapilib.create()
	end	
	return _gTapi;	
end

--销毁tapi句柄函数接口：Ttype为string型参数，用于唯一标识该tapi句柄
--输出：无
local function tapidestroy()
	if _gTapi ~= nil then
	   	tapilib.destroy(_gTapi)
		_gTapi = nil
	end
end

--获取可搜寻到的基站个数函数接口
--输出：返回实际获取到的基站个数，失败返回0 -integer型
local function getbscount()
	local _tapi = tapiget()
	if _tapi ~= nil  then
		return tapilib.getbscount(_tapi)
	end
end


--根据索引获取指定基站信息，其中0为主基站索引，其它按信号强度排序
--index - 索引值(0~tr_tapigetbscount()之间) -integer型
--输出：根据指定索引值返回基站信息(double)型lac,cellid,mcc,mnc,signalstrength,lat,lon（若索引值不在有效范围内，则返回NULL）
local function getbsbyindex(index)
	local _tapi = tapiget()
	if _tapi ~= nil  then
	   	return tapilib.getbsbyindex(_tapi,index)
	end

end

--获取当前已连接的wifi信息
--输出：返回实际已连接的wifi信息(string型name，mac，ip，double型的signalstrength),失败返回NULL
local function getconnwifiinfo()
	local _tapi = tapiget()
	if _tapi ~= nil  then
	   	return tapilib.getconnwifiinfo(_tapi)
	end
end


--获取MobileID
--输出：实际获取的MpbileID -string型
local function getmobileid()
	local _tapi = tapiget()
	if _tapi ~= nil  then
	   	return tapilib.getmobileid(_tapi)
	end
end

--获取设备卡的IMSI
--输出：实际获取设备卡的IMSI -string型
local function getimsi()
	local _tapi = tapiget()
	if _tapi ~= nil  then
	   	return tapilib.getimsi(_tapi)
	end
end

--获取当前网络的联网类别
--输出：实时获取当前网络的联网类别：0:普通网络(默认及获取不到具体类型) 1:wifi 2:gsm 3:cdma 4:tdcdma 5:cdma2000 6:wcdma。。。(int型)
local function getnettype()
	local _tapi = tapiget()
	if _tapi ~= nil  then
	   	return tapilib.getnettype(_tapi)
	end
end

--获取设备信息
--返回设备信息结构体(string型manufacturername,devicemodel,osversion),失败返回NULL
local function getdeviceinfo()
	local _tapi = tapiget()
	if _tapi ~= nil  then
	   	return tapilib.getdeviceinfo(_tapi);
	end
end
---------------------------------------------------------------------------------
---------------------------------------------------------------------------------

--接口table
local interface = {}

--对外声明接口
--对外声明获取可搜寻到的基站个数函数接口
--输出：返回实际获取到的基站个数，失败返回0
createmodule(interface,"tapigetbscount", function()
	local rev = getbscount()
	return rev
end)


--对外声明根据索引获取指定基站信息，其中0为主基站索引，其它按信号强度排序
--index - 索引值(0~tr_tapigetbscount()之间)
--输出：根据指定索引值返回基站信息(double)型lac,cellid,mcc,mnc,signalstrength,lat,lon（若索引值不在有效范围内，则返回NULL）
createmodule(interface,"tapigetbsbyindex", function(index)
	local lac, cellid, mcc ,mnc,signalstrength,lat,lon
	lac, cellid, mcc ,mnc,signalstrength,lat,lon = getbsbyindex(index)
	return lac, cellid, mcc ,mnc,signalstrength,lat,lon
end)

--对外声明获取当前已连接的wifi信息
--输出：返回实际已连接的wifi信息(string型name，mac，ip，double型的signalstrength),失败返回NULL
createmodule(interface,"tapigetconnwifiinfo", function()
	local name, mac, ip, signalstrength
	name, mac, ip, signalstrength = getconnwifiinfo()
	if name == nil then
		name = ""
	end
	if mac == nil then
		mac = ""
	end
	if ip == nil then
		ip = ""
	end
	if signalstrength == nil then
		signalstrength = 0
	end	
	return name,mac, ip, signalstrength
end)


--对外声明获取MobileID
--输出：实际获取的MpbileID(string型)
createmodule(interface,"tapigetmobileid", function()
	local rev = getmobileid()
	if rev == nil then 
		rev = ""
	end
	return rev
end)

--对外声明获取设备卡的IMSI
--输出：实际获取设备卡的IMSI(string型)
createmodule(interface,"tapigetimsi", function()
	local rev = getimsi()
	if rev == nil then 
		rev = ""
	end
	return rev
end)

--对外声明获取当前网络的联网类别
--输出：实时获取当前网络的联网类别：0:普通网络(默认及获取不到具体类型) 1:wifi 2:gsm 3:cdma 4:tdcdma 5:cdma2000 6:wcdma。。。(int型)
createmodule(interface,"tapigetnettype", function()
	local rev = getnettype()
	if rev == nil then 
		rev = 0
	end
	return rev
end)

--对外声明获取设备信息
--输出：设备信息结构体(string型manufacturername,devicemodel,osversion),失败返回NULL
createmodule(interface,"tapigetdeviceinfo", function()
	local manufacturername, devicemodel, osversion	= getdeviceinfo();
	if manufacturername == nil then
		manufacturername = ""
	end
	if devicemodel == nil then
		devicemodel = ""
	end
	if osversion == nil then
		osversion = ""
	end
	return manufacturername,devicemodel, osversion
end)

tiros.tapi  =  readOnly(interface)


