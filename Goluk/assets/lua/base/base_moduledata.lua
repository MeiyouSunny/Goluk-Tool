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
 @ 功能描述：数据仓库操作封装
 @ 提供接口：
（1）(GetModuleData)对外公开获取数据；
（2）(SetModuleData)对外公开存储数据；
</pre>

--]]

require "lua/systemapi/sys_namespace"
require "lua/systemapi/sys_handle"


local KSysModule_Global = "global"
local interface = {};
local codemsg = {"失败","成功","模块未注册","参数错误","没有数据"};
--[[
local MODULEERR_SUCCESS = 1		--成功
local MODULEERR_FAILED = 0		--失败
local MODULEERR_UNREGMODULEID = 2	--模块未注册
local MODULEERR_PARAM = 3		--参数错误
local MODULEERR_NODATA = 4		--没有数据
--]]

local moduleobj = getmodule("moduledata");

--过滤单引号
local function repStr(str)
	local s1 = string.gsub(str,"\\'","'");
	return string.gsub(s1,"'","\\'");
end

local function udpreport(code)
	local state = 1;
	if code ~=1 and code ~=4 then
		--tiros.base.udp.ErrorReport("",(code-0+10));
		state = 0;
	end
	return state;
end

--存储数据
local function setmoduledata(module,key,data)
	local code = moduleobj.moduledata_set(module,key,data);
	local state = udpreport(code);
	local msg = codemsg[code+1];
	return state,msg;
end

--获取数据
local function getdata(module,key)
	local data,code = moduleobj.moduledata_get(module,key);
	local state = 1;
	if code ~= nil then
		state = udpreport(code);
	end
	local msg = codemsg[code+1];
	return data,state,msg;
end


--对外公开存储全局数据
--key 存储数据key
--data 存储数据
--返回参数
--state 返回状态 0失败 1成功
--msg 错误信息
createmodule(interface,"SetGlobalData",function(key,data)
	if key == nil then 
		return 0,"参数错误";
	end
	local state,msg = setmoduledata(KSysModule_Global,key,data);
	return state,msg;
end);

--对外公开获取全局数据
--key 数据key
--返回参数
--data 获取的数据
--state 返回状态 0失败 1成功
--msg 错误信息
createmodule(interface,"GetGlobalData",function(key)
	if key == nil then 
		return nil,0,"参数错误";
	end
	local data,state,msg = getdata(KSysModule_Global,key);
	return data,state,msg;
end);

--对外公开存储临时模块数据,只能一次性获取
--module 模块id. 例如：web，logic，platform
--key 存储数据key
--data 存储数据
--返回参数
--state 返回状态 0失败 1成功
--msg 错误信息
createmodule(interface,"SetTempData",function(module,key,data)
	if key == nil and module == nil then 
		return 0,"参数错误";
	end
	local state,msg = setmoduledata(module,key,data);
	return state,msg;
end);

--对外公开获取临时模块数据,获取成功后立即清除
--module 模块id. 例如：web，logic，platform
--key 数据key
--返回参数
--data 获取的数据
--state 返回状态 0失败 1成功
--msg 错误信息
createmodule(interface,"GetTempData",function(module,key)
	if module == nil or key == nil then 
		return nil,0,"参数错误";
	end
	local data,state,msg = getdata(module,key);
	if state == 1 then
		--获取成功清除数据仓库
		setmoduledata(module,key,nil);
	end
	return data,state,msg;
end);

--设置 tiros.base.moduledata 为只读权限
tiros.base.moduledata = readOnly(interface);