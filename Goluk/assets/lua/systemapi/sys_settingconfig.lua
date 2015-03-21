--各模块配置信息 接口
--主要模块：地图、导航、平台设置
--考虑预留uID，根据用户ID保存其配置信息

require"lua/systemapi/sys_namespace"
require"lua/config"
require"lua/moduledata"
require"lua/commfunc"

--模块配置文件夹
local settingconfigpath = "api"
--模块配置文件名
local settingconfigname = "settingcfg"

local DEFAULT_PROMPT_TYPE = 2

--拼接路径
local function settingconfigfilepath()
	local filepath = ""

	filepath = filepath .. settingconfigpath

	return filepath
end

--读取配置文件
--uID：用户唯一标识 没有传 nil
local function setting_config_open()
	local filepath = settingconfigfilepath()
	if filepath == nil then
		return nil
	end
	tiros.config.ProfileStart(filepath, settingconfigname)
end

--保存并关闭配置文件
local function setting_config_close()
	tiros.config.ProfileStop(settingconfigname)
end

--设置存储配置信息
--key 需要设置的数据的键值
--valuea 需要设置的值
local function setting_config_setinfo(key, value)
	tiros.config.setValue(settingconfigname, key, value)
end

--获取配置信息
--key 需要获得的数据的键值
local function setting_config_getinfo(key)
	local retvalue = tiros.config.getValue(settingconfigname, key)
	return retvalue
end

--功能：设置用户性别
--value:intger型参数 用户性别：1男2女
--无返回值
local function config_set_usergender(value)
	if nil == value then
		value = 2;
	end
	setting_config_open();
	setting_config_setinfo("usergender",tonumber(value));
	setting_config_close();
	tiros.moduledata.moduledata_set('framework','gender',tonumber(value));
end

--功能：获取用户性别
--value:
--返回值: inter型参数 用户性别：1男2女
local function config_get_usergender()
	local gender = tiros.moduledata.moduledata_get('framework','gender');
	if nil == gender then
		gender = 2;
	end
	return tonumber(gender);
end

--功能：设置用户提示音类型
--value:intger型参数 用户性别：1男2女3葛优4宝宝
--无返回值
local function config_set_prompttype(value)
	if nil == value then
		value = DEFAULT_PROMPT_TYPE;
	end

	setting_config_open();
	if DEFAULT_PROMPT_TYPE ~= tonumber(value) then
		setting_config_setinfo("changeprompt",1);
	end
	setting_config_setinfo("prompttype",tonumber(value));
	setting_config_close();
	tiros.moduledata.moduledata_set('framework','prompttype',tonumber(value));
end

--功能：获取用户提示音类型
--value:
--返回值: intger型参数 用户性别：1男2女3葛优4宝宝
local function config_get_prompttype()
	local prompttype = tiros.moduledata.moduledata_get('framework','prompttype');
	if nil == prompttype then
		prompttype = DEFAULT_PROMPT_TYPE;
	end
	return tonumber(prompttype);
end

--功能：设置用户保存配置
--key:string
--value:
--无返回值
local function config_set_configinfo(key,value)
	if nil == key or nil == value then
		return;
	end
	setting_config_open();
	setting_config_setinfo(tostring(key),tonumber(value));
	setting_config_close();
end

--功能：获取用户配置信息
--value:
--返回值: intger型参数 
local function config_get_configinfo(key)
	print("yaoyt config_get_configinfo key:" .. key)
	if nil == key then
		return 0;
	end
	setting_config_open();
	local result = setting_config_getinfo(tostring(key));
	setting_config_close();
	if nil == result then
		return 0;
	end
	print("yaoyt config_get_configinfo value:" .. result)
	return tonumber(result);
end

--获取整个配置文件的json串
local function config_get_settingconfig()
	print("yaoyt config_get_settingconfig in")
	setting_config_open();
	local contentTable = tiros.config.getValue(settingconfigname, nil);
	setting_config_close();
	if nil ==  contentTable then
		return "";
	end

	local contentStr = tiros.json.encode(contentTable);
	print(contentStr)
	return contentStr;
end

--开机时将用户性别写入数据仓库
local function user_gender_init()
	setting_config_open();
	local gender = setting_config_getinfo("usergender");
	local promptType = setting_config_getinfo("prompttype");
	setting_config_close();
	if nil == gender then
		gender = 2
	end
	if nil == promptType then
		promptType = DEFAULT_PROMPT_TYPE
	end
	tiros.moduledata.moduledata_set('framework','gender',tonumber(gender));
	tiros.moduledata.moduledata_set('framework','prompttype',tonumber(promptType));
	--通过性别设置webres路径
	tiros.commfunc.setWebresPath(gender);
end

--接口table
local interface = {}

--------对外公有函数
--功能：打开配置文件
createmodule(interface,"settingconfig_open",function()
	setting_config_open()
end)

--功能：关闭配置文件
--无参数无返回值
createmodule(interface,"settingconfig_close",function()
	setting_config_close()
end)

--功能：获取配置信息
--key：string型参数 配置信息的键值
--返回值：integer类型，配置值，键值对应value
createmodule(interface,"settingconfig_getinfo",function(key)
	return setting_config_getinfo(key)
end)

--功能：设置配置信息
--key：string型参数 配置信息的键值
--value:integer型参数 设置的值
--无返回值
createmodule(interface,"settingconfig_setinfo",function(key, value)
	setting_config_setinfo(key, value)
end)

--功能：设置用户性别
--value:intger型参数 用户性别：1男2女
--无返回值
createmodule(interface,"set_usergender",function(value)
	config_set_usergender(value)
end)

--功能：获取用户性别
--value:
--返回值: inter型参数 用户性别：1男2女
createmodule(interface,"get_usergender",function()
	return config_get_usergender()
end)

--功能：设置用户提示音类型
--value:intger型参数 用户性别：1男2女3葛优4宝宝
--无返回值
createmodule(interface,"set_prompttype",function(value)
	config_set_prompttype(value)
end)

--功能：获取用户提示音类型
--value:
--返回值: intger型参数 用户性别：1男2女3葛优4宝宝
createmodule(interface,"get_prompttype",function()
	return config_get_prompttype()
end)

--功能：设置用户保存配置
--key:string
--value:
--无返回值
createmodule(interface,"set_configinfo",function(key,value)
	config_set_configinfo(key,value)
end)

--功能：获取用户配置信息
--value:
--返回值: intger型参数 
createmodule(interface,"get_configinfo",function(key)
	return config_get_configinfo(key)
end)

--功能：获取整个配置信息
--value:
--返回值: json
createmodule(interface,"get_settingconfig",function()
	return config_get_settingconfig()
end)

--开机时将用户性别写入数据仓库
createmodule(interface,"usergender_init",function()
	user_gender_init()
end)

tiros.settingconfig = readOnly(interface)
