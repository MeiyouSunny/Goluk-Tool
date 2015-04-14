--[[
-- @描述:tts媒体播放器接口
-- @编写人:宣东言
-- @创建日期: 2013-09-13 10:34:21
--]]

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/moduledata"

--[[
htype目前用到的值
1."ttsmanagertype_navigation" --导航
2."ttsmanagertype_voiceplay"  --语音播放平台
--]]

--全局变量，用于存放正在使用的所有句柄
local _gMedialist = {}
--全局变量，用于存放所有句柄的week表，week表中既包含正使用的句柄，也包含即将回收的句柄
local _gMediaWeaklist = {}
setmetatable(_gMediaWeaklist,{__mode ="v" })

--[[
--@描述:创建TTSManager句柄
--@param  无
--@return 成功返回实际创建的句柄，失败返回nil
--]]
local function sys_TTSManagerCreate(htype)
	print("sys_TTSManager---Create");
	local htable = getHandle(_gMediaWeaklist,htype);
	if htable == nil then
		htable = {};
	   	htable[0] = ttsmanagerlib.create();
	end
	registerHandle(_gMedialist,_gMediaWeaklist,htype,htable)
	return htable;
end

--[[
--@描述:删除TTSManager句柄
--@param  htype 句柄的唯一标识符
--@return 无
--]]
local function sys_TTSManagerDestroy(htype)
	print("sys_TTSManager---Destroy");
	local htable =getHandle(_gMedialist,htype);
	if htable ~= nil then
		ttsmanagerlib.destroy(htable[0]);
	end
	releaseHandle(_gMedialist,htype)	
end

--[[
--@描述:lua层TTSManager事件回调处理函数
--@param  htype 句柄的唯一标识符
--@param  event 事件类型
--@param  param1 参数1
--@param  param2 参数2
--@return 无
--]]
DeclareGlobal("sys_TTSManagerEvnet",function (htype, event, param1, param2)
	local htable = getHandle(_gMedialist,htype)
	print("sys_TTSManagerEvnet",htype,event,param1, param2)
	if htable ~= nil then
		if htable[1] ~= nil then
			if (type(htable[1])) == "function" then 
				print("sys_TTSManagerEvnet---------lua 1 \r\n");
				htable[1](htype,event,param1, param2);
				print("sys_TTSManagerEvnet---------lua 2 \r\n");
			else
				print("sys_TTSManagerEvnet---------C1 \r\n");
				
				commlib.universalnotifyFun(htable[1],htype, htable[2], event,param1,param2);
				print("sys_TTSManagerEvnet---------C2 \r\n");
			end
		end
	end
end)

--[[
--@描述:注册回调函数
--@param  htype 句柄的唯一标识符
--@param fnNotify-回调函数地址
--@return 无
--]]
local function sys_TTSManagerRegistNotify(htype, fnNotify, nUser)
	print("sys_TTSManager---RegistNotify---1");
	if (htype == nil) then
		print("sys_TTSManager---RegisterNotify--error");
		return;
	end
	print("sys_TTSManager---RegistNotify---2");
	local htable = getHandle(_gMedialist,htype);
	print("sys_TTSManager---RegistNotify---3");
	htable[1] = fnNotify;
	htable[2] = nUser;
	if htable ~= nil then
	print("sys_TTSManager---RegistNotify---4");
		ttsmanagerlib.registnotify(htable[0],"sys_TTSManagerEvnet",htype)
		print("sys_TTSManager---RegistNotify---5");
	end
end


--[[
--@描述:初始化
--@param  htype 句柄的唯一标识符
--@return 错误代码
--]]
local function sys_TTSManagerInit(htype)
	print("sys_TTSManager---Init");
	if (htype == nil) then
		print("sys_TTSManager---Init--Error");
		return;
	end
	local htable = getHandle(_gMedialist,htype);
	if htable ~= nil then
		ttsmanagerlib.init(htable[0]);
	end
end

--[[
--@描述:获取播放状态是否繁忙
--@param  htype 句柄的唯一标识符
--@return 错误代码
--]]
local function sys_TTSManagerIsBusy(htype)
	print("sys_TTSManager---isbusy");
	if (htype == nil) then
		print("sys_TTSManager---isbusy--Error");
		return;
	end
	local htable = getHandle(_gMedialist,htype);
	if htable ~= nil then
		return ttsmanagerlib.isbusy(htable[0]);
	end
end

--[[
--@描述:播放
--@param  htype 句柄的唯一标识符
--@param  sTest 要播放
--@return 错误代码
--]]
local function sys_TTSManagerPlay(htype, sTest)
	print("sys_TTSManager---Play");
	if (htype == nil or sTest == nil) then
		print("sys_TTSManager---Play--Error");
		return;
	end
	local htable = getHandle(_gMedialist,htype);
	if htable ~= nil then
		return ttsmanagerlib.play(htable[0], sTest);
	end
end


--[[
--@描述:停止
--@param  htype 句柄的唯一标识符
--@return 错误代码
--]]
local function sys_TTSManagerStop(htype)
	print("sys_TTSManager---Stop"..htype);
	if (htype == nil) then
		print("sys_TTSManager---Stop--Error");
		return;
	end
	local htable = getHandle(_gMedialist,htype);
	if htable ~= nil then
		return ttsmanagerlib.stop(htable[0]);
	end
end

--接口table
local interface = {}

--[[
--@描述:播放
--@param  htype 句柄的唯一标识符
--@param  fnNotify 回调函数
--@param  sText 文件名
--@return 无
--]]
createmodule(interface,"Play",function (htype, sText, fnNotify, nUser)
	if(htype == nil  or sText == nil) then
		print("TTSManager--Play---Error");
		return;
	end

	print("TTSManager--Play-htype="..htype)
	print("TTSManager--Play-sText="..sText)

	local htable = getHandle(_gMedialist,htype);
	if htable == nil then
		--创建句柄
		htable = sys_TTSManagerCreate(htype);
		if htable == nil then	
			return;
		end
		--初始化
		sys_TTSManagerInit(htype)
	else
		--sys_TTSManagerStop(htype)
	end
	
	--注册回调函数
	sys_TTSManagerRegistNotify(htype, fnNotify, nUser);

	--判断是否空闲
	local bIsBusy = sys_TTSManagerIsBusy(htype);
	if(bIsBusy == false) then
		print("TTSManager---Play")
		sys_TTSManagerPlay(htype, sText);
	else 
		print("TTSManager---Play--is busy");
	end
end)

--[[
--@描述:判断是否繁忙（全部）
--@param  htype 句柄的唯一标识符
--@return 无
--]]
createmodule(interface,"IsBusy",function ()
	print("ttsmanager-IsBusy---------1")
	local bBusyNavigation = sys_TTSManagerIsBusy("ttsmanagertype_navigation");
	local bBusyVoiceplay = sys_TTSManagerIsBusy("ttsmanagertype_voiceplay");
	if bBusyNavigation == true or bBusyVoiceplay == true then
		return true;
	end
	return false
--[[
	for key,value in pairs(_gMedialist) do
		local bBusy = sys_TTSManagerIsBusy(value);
		if(bBusy == true) then
			return true;
		end
	end
	return false
--]]
end)

--[[
--@描述:停止
--@param  htype 句柄的唯一标识符
--@return 无
--]]
createmodule(interface,"Stop",function ()
print("ttsmanager-Stop---------1")
	sys_TTSManagerStop("ttsmanagertype_navigation");
	sys_TTSManagerStop("ttsmanagertype_voiceplay");
--[[
	for key,value in pairs(_gMedialist) do
		print("ttsmanager-Stop---------1.5"..value)
		sys_TTSManagerStop(value)
	end
--]]
print("ttsmanager-Stop---------2")	
	return true;
end)



tiros.ttsmanager = readOnly(interface);


