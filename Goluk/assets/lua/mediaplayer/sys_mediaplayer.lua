--[[
-- @描述:媒体播放器接口
-- @编写人:宣东言
-- @创建日期: 2013-08-13 10:34:21
--]]

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/moduledata"

--全局变量，用于存放正在使用的所有句柄
local _gMedialist = {}
--全局变量，用于存放所有句柄的week表，week表中既包含正使用的句柄，也包含即将回收的句柄
local _gMediaWeaklist = {}
setmetatable(_gMediaWeaklist,{__mode ="v" })

--播放状态
local MEDIA_PLAYER_STATE_NOT_INIT = 0    --没有初始化
local MEDIA_PLAYER_STATE_IDLE 	  = 1    --闲状态，可以启动播放
local MEDIA_PLAYER_STATE_PLAYING  = 2    --正在播放状态
local MEDIA_PLAYER_STATE_BUFFERING= 3    --缓冲中状态
local MEDIA_PLAYER_STATE_PAUSE    = 4    --暂停播放状态
local MEDIA_PLAYER_STATE_ERROR    = 5    --出错


--[[
--@描述:创建MediaPlayer句柄
--@param  无
--@return 成功返回实际创建的句柄，失败返回nil
--]]
local function sys_MediaPlayerCreate(htype)
	print("sys_Mediaplayer---Create");
	local htable = getHandle(_gMediaWeaklist,htype);
	if htable == nil then
		htable = {};
	   	htable[0] = mediaplayerlib.create();
	end
	registerHandle(_gMedialist,_gMediaWeaklist,htype,htable)
	return htable;
end

--[[
--@描述:删除MediaPlayer句柄
--@param  htype 句柄的唯一标识符
--@return 无
--]]
local function sys_MediaplayerDestroy(htype)
	print("sys_Mediaplayer---Destroy");
	local htable =getHandle(_gMedialist,htype);
	if htable ~= nil then
		mediaplayerlib.destroy(htable[0]);
	end
	releaseHandle(_gMedialist,htype)	
end

--[[
--@描述:lua层MediaPlayer事件回调处理函数
--@param  htype 句柄的唯一标识符
--@param  event 事件类型
--@param  param1 参数1
--@param  param2 参数2
--@return 无
--]]
DeclareGlobal("sys_MediaPlayerEvnet",function (htype, event, param1, param2)
	local htable = getHandle(_gMedialist,htype)
	print("sys_MediaPlayerEvnet",htype,event,param1, param2)
	if htable ~= nil then
		if htable[1] ~= nil then
			htable[1](htype,event,param1, param2);
			--播放完毕到消息销毁播放器
			--if(event == 3) then
				--print("sys_MediaPlayerEvnet == 3 mediaplayerlib.destroy")
				--mediaplayerlib.destroy(htable[0]);
			--end
		end
	end
end)

--[[
--@描述:注册回调函数
--@param  htype 句柄的唯一标识符
--@param fnNotify-回调函数地址
--@return 无
--]]
local function sys_MediaplayerRegistNotify(htype, fnNotify)
	print("sys_Mediaplayer---RegistNotify");
	if (htype == nil  or fnNotify == nil) then
		print("sys_Mediaplayer---RegisterNotify--error");
		return;
	end
	
	local htable = getHandle(_gMedialist,htype);
	htable[1] = fnNotify;
	if htable ~= nil then
		mediaplayerlib.notify(htable[0],"sys_MediaPlayerEvnet",htype)
	end
end


--[[
--@描述:初始化
--@param  htype 句柄的唯一标识符
--@return 错误代码
--]]
local function sys_MediaPlayerInit(htype)
	print("sys_MediaPlayer---Init");
	if (htype == nil) then
		print("sys_MediaPlayer---Init--Error");
		return;
	end
	local htable = getHandle(_gMedialist,htype);
	if htable ~= nil then
		mediaplayerlib.init(htable[0]);
	end
end

--[[
--@描述:获取播放状态
--@param  htype 句柄的唯一标识符
--@return 错误代码
--]]
local function sys_MediaPlayerGetState(htype)
	print("sys_MediaPlayer---GetState");
	if (htype == nil) then
		print("sys_MediaPlayer---GetState--Error");
		return;
	end
	local htable = getHandle(_gMedialist,htype);
	if htable ~= nil then
		return mediaplayerlib.getstate(htable[0]);
	end
end

--[[
--@描述:播放本地文件
--@param  htype 句柄的唯一标识符
--@param  sFileName 要播放
--@return 错误代码
--]]
local function sys_MediaPlayerPlayLocalFile(htype, sFileName)
	print("sys_MediaPlayer---PlayLocalFile");
	if (htype == nil or sFileName == nil) then
		print("sys_MediaPlayer---PlayLocalFile--Error");
		return;
	end
	local htable = getHandle(_gMedialist,htype);
	if htable ~= nil then
		return mediaplayerlib.playlocalfile(htable[0], sFileName);
	end
end


--[[
--@描述:播放流媒体
--@param  htype 句柄的唯一标识符
--@return 错误代码
--]]
local function sys_MediaPlayerPlayStream(htype, pStream, nLen)
	print("sys_MediaPlayer---PlayStream--1");
	if (htype == nil or pStream == nil) then
		print("sys_MediaPlayer---PlayStream--Error");
		return;
	end
	print("sys_MediaPlayer---PlayStream--2");
	local htable = getHandle(_gMedialist,htype);
	print("sys_MediaPlayer---PlayStream--3");
	if htable ~= nil then
		print("sys_MediaPlayer---PlayStream--4");
		local ret = mediaplayerlib.playstream(htable[0], pStream, nLen);
		print("sys_MediaPlayer---PlayStream--5");
		print("sys_MediaPlayer---PlayStream--6"..ret);
		return ret;
	end
end

--[[
--@描述:暂停
--@param  htype 句柄的唯一标识符
--@return 错误代码
--]]
local function sys_MediaPlayerPause(htype)
	print("sys_MediaPlayer---Pause");
	if (htype == nil) then
		print("sys_MediaPlayer---Pause--Error");
		return;
	end
	local htable = getHandle(_gMedialist,htype);
	if htable ~= nil then
		return mediaplayerlib.pause(htable[0]);
	end
end

--[[
--@描述:恢复播放
--@param  htype 句柄的唯一标识符
--@return 错误代码
--]]
local function sys_MediaPlayerResume(htype)
	print("sys_MediaPlayer---Resume");
	if (htype == nil) then
		print("sys_MediaPlayer---Resume--Error");
		return;
	end
	local htable = getHandle(_gMedialist,htype);
	if htable ~= nil then
		return mediaplayerlib.resume(htable[0]);
	end
end

--[[
--@描述:停止
--@param  htype 句柄的唯一标识符
--@return 错误代码
--]]
local function sys_MediaPlayerStop(htype)
	print("sys_MediaPlayer---Stop");
	if (htype == nil) then
		print("sys_MediaPlayer---Stop--Error");
		return;
	end
	local htable = getHandle(_gMedialist,htype);
	if htable ~= nil then
		return mediaplayerlib.stop(htable[0]);
	end
end


--接口table
local interface = {}

--[[
--@描述:播放本地文件
--@param  htype 句柄的唯一标识符
--@param  fnNotify 回调函数
--@param  sFileName 文件名
--@return 无
--]]
createmodule(interface,"PlayLocalFile",function (htype, fnNotify, sFileName)
	if(htype == nil or fnNotify == nil or sFileName == nil) then
		print("PlayLocalFile---Error");
		return;
	end

	print("MediaPlayer--PlayLocalFile-htype=",htype)
	print("MediaPlayer--PlayLocalFile-fnNotify=",fnNotify)
	print("MediaPlayer--PlayLocalFile-sFileName=",sFileName)

	local htable = getHandle(_gMedialist,htype);
	if htable == nil then
		--创建句柄
		htable = sys_MediaPlayerCreate(htype);
		if htable == nil then	
			return;
		end
		--初始化
		sys_MediaPlayerInit(htype)
	else
		sys_MediaPlayerStop(htype)
	end
	
	--注册回调函数
	sys_MediaplayerRegistNotify(htype, fnNotify);

	--判断是否空闲
	local nState = sys_MediaPlayerGetState(htype);
	if(nState == MEDIA_PLAYER_STATE_IDLE) then
		print("MediaPlayer---PlayLocalFile")
		sys_MediaPlayerPlayLocalFile(htype, sFileName);
	else 
		print("MediaPlayer---PlayLocalFile--err = ",nState)
	end
end)

--[[
--@描述:播放流媒体
--@param  htype 句柄的唯一标识符
--@param  fnNotify 回调函数
--@param  pStream 流媒体
--@param  nLen 流长度
--@return 无
--]]
createmodule(interface,"PlayStream",function (htype, fnNotify, pStream, nLen)
	if(htype == nil or fnNotify == nil or pStream == nil) then
		print("PlayStream---Error");
		return;
	end

	print("MediaPlayer--PlayStream-htype=",htype)
	print("MediaPlayer--PlayStream-fnNotify=",fnNotify)

	--判断是否空闲
	local nState = -1;

	local htable = getHandle(_gMedialist,htype);
	if htable == nil then
		--创建句柄
		htable = sys_MediaPlayerCreate(htype);
		if htable == nil then	
			return;
		end
		--初始化
		sys_MediaPlayerInit(htype)
		nState = sys_MediaPlayerGetState(htype);
	else
		nState = sys_MediaPlayerGetState(htype);
		
		if(nState == MEDIA_PLAYER_STATE_PLAYING) then
			print("MediaPlayer---MediaPlayerStop")
			sys_MediaPlayerStop(htype)
		end
	end

	--注册回调函数
	sys_MediaplayerRegistNotify(htype, fnNotify);
	nState = sys_MediaPlayerGetState(htype);
	if(nState == MEDIA_PLAYER_STATE_IDLE) then
		print("MediaPlayer---PlayStream")
		sys_MediaPlayerPlayStream(htype, pStream, nLen);
	else 
		print("MediaPlayer---PlayStream--errcode = ",nState)
	end
end)


--[[
--@描述:暂停
--@param  htype 句柄的唯一标识符
--@return 无
--]]
createmodule(interface,"Pause",function (htype)
	if(htype == nil) then
		print("Pause---Error");
		return;
	end

	return sys_MediaPlayerPause(htype)
end)

--[[
--@描述:恢复
--@param  htype 句柄的唯一标识符
--@return 无
--]]
createmodule(interface,"Resume",function (htype)
	if(htype == nil) then
		print("Resume---Error");
		return;
	end

	return sys_MediaPlayerResume(htype)
end)

--[[
--@描述:停止
--@param  htype 句柄的唯一标识符
--@return 无
--]]
createmodule(interface,"Stop",function (htype)
print("Stop---------1")
	if(htype == nil) then
		print("Stop---Error");
		return;
	end

	return sys_MediaPlayerStop(htype)
end)

--[[
--@描述:销毁
--@param  htype 句柄的唯一标识符
--@return 无
--]]
createmodule(interface,"Destroy",function (htype)
print("Destroy---------1")
	if(htype == nil) then
		print("Destroy---Error");
		return;
	end

	return sys_MediaplayerDestroy(htype)
end)

tiros.mediaplayer = readOnly(interface);


--[[
sys_MediaPlayerCreate
sys_MediaPlayerInit
sys_MediaplayerRegistNotify
sys_MediaPlayerStop
sys_MediaPlayerResume
sys_MediaPlayerPause
sys_MediaPlayerPlayStream
sys_MediaPlayerPlayLocalFile
sys_MediaPlayerGetState
sys_MediaplayerDestroy
--]]
