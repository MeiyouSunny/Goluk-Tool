--[[
-- @描述:http文件上传接口
-- @编写人:宣东言
-- @创建日期: 2012-12-19 13:15:21
--]]

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/net-httpheaders"
require"lua/moduledata"

--全局变量，用于存放正在使用的所有http句柄
local _gHttplist = {}
--全局变量，用于存放所有http句柄的week表，week表中既包含正使用的句柄，也包含即将回收的句柄
local _gHttpweaklist = {}
setmetatable(_gHttpweaklist,{__mode ="v" })

--设置Contenttype
local gsContenttype = "application/octet-stream"

--[[
--@描述:创建HttpUpLoad句柄
--@param  无
--@return 成功返回实际创建的http句柄，失败返回nil
--]]
local function sys_httpUpLoadCreate(htype)
	local htable = getHandle(_gHttpweaklist,htype);
	if htable == nil then
		htable = {};
	   	htable[0] = httpuploadlib.create();
	end
	registerHandle(_gHttplist,_gHttpweaklist,htype,htable)
	return htable;
end

--[[
--@描述:删除HttpUpLoad句柄
--@param  htype 句柄的唯一标识符
--@return 无
--]]
local function sys_httpUpLoadDestroy(htype)
	local htable =getHandle(_gHttplist,htype);
	if htable ~= nil then
		httpuploadlib.destroy(htable[0]);
	end
	releaseHandle(_gHttplist,htype)	
end

--[[
--@描述:设置URL
--@param  htable 句柄的唯一标识符
--@param  sUrl URL
--@param  sContenttype Contenttype
--@return 成功设置返回true,否则false
--]]
local function sys_httpUpLoadSetUrl(htable, sUrl, sContenttype)
	luaprint("sys_httpUpLoad---SetUrl");
	if (htable == nil or sUrl == nil or sContenttype == nil) then
		luaprint("sys_httpUpLoad---SetUrl--Error");
		return;
	end
	return httpuploadlib.seturl(htable[0], sUrl, sContenttype);
end

--[[
--@描述:上传文件
--@param  htable 句柄的唯一标识符
--@param  sFileName 上传文件名(全路径)
--@return 成功开始上传返回true，否则false
--]]
local function sys_httpUpLoadPushFile(htable, sFileName)
	luaprint("sys_httpUpLoad---PushFile");
	if (htable == nil or sFileName == nil) then
		luaprint("sys_httpUpLoad---PushFile--Error");
		return;
	end
	return httpuploadlib.pushfile(htable[0], sFileName);
end

--[[
--@描述:http是否已经请求
--@param  htable 句柄的唯一标识符
--@return 已请求则返回true，否则false
--]]
local function sys_httpUpLoadIsBusy(htable)
	luaprint("sys_httpUpLoad---IsBusy");
	if (htable == nil) then
		luaprint("sys_httpUpLoad---IsBusy--Error");
		return;
	end
	return httpuploadlib.isbusy(htable[0]);
end

--[[
--@描述:取消请求
--@param  htable 句柄的唯一标识符
--@return 无
--]]
local function sys_httpUpLoadCancel(htable)
	luaprint("sys_httpUpLoad---Cancel");
	if (htable == nil) then
		luaprint("sys_httpUpLoad---Cancel--Error");
		return;
	end
	luaprint("zhaoqlll","aaaa");
	httpuploadlib.cancel(htable);
	luaprint("zhaoqlll","bbbb");
end

--[[
--@描述:增加http头信息
--@param  htable 句柄的唯一标识符
--@param  sHeaderKey 头信息Key
--@param  sHeaderValue 头信息Value
--@return 添加成功true 添加失败false
--]]
local function sys_httpUpLoadAddHeader(htable, sHeaderKey, sHeaderValue)
	luaprint("sys_httpUpLoad---AddHeader");
	if (htable == nil or sHeaderKey == nil or sHeaderValue == nil) then
		luaprint("sys_httpUpLoad---AddHeader--Error");
		return;
	end
	return httpuploadlib.addheader(htable[0], sHeaderKey, sHeaderValue);
end

--[[
--@描述:删除http头信息
--@param  htable 句柄的唯一标识符
--@param  sHeaderKey 头信息Key
--@return 无
--]]
local function sys_httpUpLoadRemoveHeader(htable, sHeaderKey)
	luaprint("sys_httpUpLoad---RemoveHeader");
	if (htable == nil or sHeaderKey == nil) then
		luaprint("sys_httpUpLoad---RemoveHeader--Error");
		return;
	end
	httpuploadlib.removeheader(htable[0], sHeaderKey);
end

--[[
--@描述:设置http传输超时，如果不设置或者设置为0则使用默认时间,默认等待连接时间为30s,接收数据片段间隔时间为20s
--@param  htype 句柄的唯一标识符
--@param  nTime1 http发送请求到收到服务器响应的等待连接超时时间
--@param  nTime2 http接收数据片段间隔超时时间
--@return 无
--]]
local function sys_httpUpLoadSetTimeout(htable, nTime1, nTime2)
	luaprint("sys_httpUpLoad---SetTimeout");
	if (htable == nil) then
		luaprint("sys_httpUpLoad---SetTimeout--Error");
		return;
	end
	httpuploadlib.settimeout(htable[0], nTime1, nTime2);
end

--[[
--@描述:lua层http事件回调处理函数
--@param  htype 句柄的唯一标识符
--@param  event 事件类型
--@param  param1 参数1
--@param  param2 参数2
--@return 无
--]]
DeclareGlobal("sys_HttpUpLoadEvnet",function (htype,event,param1, param2)
	local htable = getHandle(_gHttplist,htype)
	--luaprint("sys_HttpEvnet",htype,event,param1, param2)
	if event == 4 or event == 5 then
		sys_httpUpLoadCancel(htable[0]);
	end
	if htable ~= nil then
		if htable[1] ~= nil then
			htable[1](htype,event,param1, param2);
		end
	end
end)

--[[
--@描述:注册HttpUpLoad回调函数
--@param htype 句柄的唯一标识符
--@param fnNotify-回调函数地址
--@param nUser-调用者参数地址
--@return 无
--]]
local function sys_httpUpLoadRegisterNotify(htype, fnNotify)
	luaprint("sys_httpUpLoad---RegisterNotify");
	if (htype == nil  or fnNotify == nil) then
		luaprint("sys_httpUpLoad---RegisterNotify--error");
		return;
	end

	local htable = getHandle(_gHttplist,htype);

	htable[1] = fnNotify;

	httpuploadlib.registernotify(htable[0],"sys_HttpUpLoadEvnet",htype)
end

--接口table
local interface = {}

--[[
--@描述:上传文件
--@return 无
--]]
createmodule(interface,"HttpUpLoad",function (htype, fnNotify, sUrl, sFileName, sAactionLocation)
	if(htype == nil or fnNotify == nil or sUrl == nil or sFileName == nil or sAactionLocation == nil) then
		luaprint("HttpUpLoad---Error");
		return;
	end

	luaprint("HttpUpLoad-htype=",htype)
	luaprint("HttpUpLoad-fnNotify=",fnNotify)
	luaprint("HttpUpLoad-sUrl=",sUrl)
	luaprint("HttpUpLoad-sFileName=",sFileName)
	luaprint("HttpUpLoad-sAactionLocation=",sAactionLocation)

	--创建句柄
	local htable = sys_httpUpLoadCreate(htype);
	if htable == nil then	
	   	return;
	end
	--注册回调函数
	sys_httpUpLoadRegisterNotify(htype, fnNotify);
	--设置URL
	sys_httpUpLoadSetUrl(htable, sUrl, gsContenttype);
	--设置区分服务的头信息
	sys_httpUpLoadAddHeader(htable, "actionlocation", sAactionLocation);
	--设置uid头信息
	local sUid = tiros.moduledata.moduledata_get("framework", "uid");
	sys_httpUpLoadAddHeader(htable, "uid", sUid);
	--判断是否繁忙
	local bIsBusy = sys_httpUpLoadIsBusy(htable);
	if(bIsBusy == false) then
		sys_httpUpLoadPushFile(htable, sFileName);
	end
end)

--[[
--@描述:释放句柄
--@param  htype 句柄的唯一标识符
--@return 无
--]]
createmodule(interface,"HttpUpLoadAbort", function (htype)
	if (htype == nil) then
		luaprint("HttpUpLoadAbort--Error");
		return;
	end
	local htable = getHandle(_gHttplist,htype)
	sys_httpUpLoadCancel(htable[0]);
end)

tiros.httpupload = readOnly(interface);

--[[
local nHandle = sys_httpUpLoadCreate()
sys_httpUpLoadRegisterNotify(nHandle, "fnNotify", "nUser")
sys_httpUpLoadSetUrl(nHandle, "http://www.baidu.com", "txt")
sys_httpUpLoadPushFile(nHandle, "fs0:/gitversion")
sys_httpUpLoadIsBusy(nHandle)
sys_httpUpLoadCancel(nHandle)
sys_httpUpLoadAddHeader(nHandle, "sHeaderKey", "sHeaderValue")
sys_httpUpLoadRemoveHeader(nHandle, "sHeaderKey")
sys_httpUpLoadSetTimeout(nHandle, 1, 2)
sys_httpUpLoadDestroy(nHandle)
--]]

