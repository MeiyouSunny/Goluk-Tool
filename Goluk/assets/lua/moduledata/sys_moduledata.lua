--模块数据存储接口

--require"lua/systemapi/sys_globalmamanger"
require"lua/systemapi/sys_namespace"
require"lua/config/sys_config"

--数据仓库错误码		
local MODULEERR_SUCCESS = 1		--成功
local MODULEERR_FAILED = 0		--失败
local MODULEERR_UNREGMODULEID = 2	--模块未注册
local MODULEERR_PARAM = 3		--参数错误
local MODULEERR_NODATA = 4		--没有数据

--moduledatatable:用于存储数据
local moduledatatable = {}

--moduledatacfgtable存储已注册的moduleID
local moduledatacfgtable = nil


--从文件中读取已添加的moduleID，若传入moduleID未在文件中找到则不允许进行数据获取和设置
--moduleID:string型参数，模块ID
local function modulecfg(moduleID)
--[[
	if moduledatacfgtable == nil then
		tiros.config.ProfileStart("api", "moduledatacfg")
		moduledatacfgtable = tiros.config.getValue("moduledatacfg", nil)
		tiros.config.ProfileStop("moduledatacfg")
	end
	
	if moduledatacfgtable[moduleID] == nil then
		return false
	end
]]--
	return true
end


--添加设置模块数据
--moduleID:string型参数，模块ID
--key:string型参数，需设置的数据键值
--value:string型或者integer型，需保存的数据
--返回值: 数据仓库错误码
local function moduledataset(moduleID,key,value)
	if moduleID == nil or key == nil then
		return MODULEERR_PARAM
	end
	if (not modulecfg(moduleID)) then
		return MODULEERR_UNREGMODULEID
	end
	if moduledatatable[moduleID] == nil then
		moduledatatable[moduleID] = {}
	end
	moduledatatable[moduleID][key] = value
	return MODULEERR_SUCCESS
end

--获取模块数据
--moduleID:string型参数，模块ID
--key:string型参数，需获取的数据键值
--返回值:数据仓库错误码
local function moduledataget(moduleID,key)
	local st, retvalue
	if (not modulecfg(moduleID)) then
		return MODULEERR_UNREGMODULEID,nil
	end
	local retvalue
	if moduledatatable[moduleID] == nil then
		return MODULEERR_NODATA,nil
	end
	retvalue = moduledatatable[moduleID][key]
	if retvalue ~= nil then
		return MODULEERR_SUCCESS,retvalue
	else
		return MODULEERR_NODATA,""
	end
end

--清除模块数据
--moduleID:string型参数，模块ID
--返回值: 数据仓库错误码
local function moduledataclean(moduleID)
	if (not modulecfg(moduleID)) then
		return MODULEERR_UNREGMODULEID
	end

	if moduledatatable[moduleID] ~= nil then
		moduledatatable[moduleID] = nil
		return MODULEERR_SUCCESS
	end
end

--判断模块数据是否存在
--moduleID:string型参数，模块ID
--key:string型参数，数据键值
--返回值: true,存在; false,不存在
local function moduledataexist(moduleID,key)
	if moduledatatable[moduleID] ~= nil and moduledatatable[moduleID][key] ~= nil then
		return true	
	end
	return false
end

--lua对外接口

local interface = {}

--添加设置模块数据
--moduleID:模块ID
--key:需设置的数据键值
--value:需保存的数据
createmodule(interface, "moduledata_set",function(moduleID,key,value)
	local rev = moduledataset(moduleID,key,value)
	return rev	
end)

--获取模块数据
--moduleID:模块ID
--key:需获取的数据键值
--返回值:数据值
createmodule(interface, "moduledata_get",function(moduleID,key)
	local errcode, data = moduledataget(moduleID,key)
	return data,errcode
end)

--清除模块数据
--moduleID:模块ID
createmodule(interface, "moduledata_clean",function(moduleID)
	return moduledataclean(moduleID)
end)

--判断模块数据是否存在
--moduleID:模块ID
--key:数据键值
--返回值: true,存在; false,不存在
createmodule(interface, "moduledata_exist",function(moduleID,key)
	local rev = moduledataexist(moduleID,key)
	if rev == true then
		rev = 1
	else
		rev = 0
	end
	return rev
end)


tiros.moduledata = readOnly(interface)
