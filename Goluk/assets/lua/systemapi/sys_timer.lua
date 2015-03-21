--timer接口封装
--

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"

--timerlist：用于存放正在使用的所有timer句柄
local _gTimerlist = {}
--timerweaklist：用于存放所有timer句柄的week表，week表中既包含正使用的句柄，也包含即将回收的句柄
local _gTimerweaklist = {}
setmetatable(_gTimerweaklist,{__mode ="v" })

--[[
timer全局变量table，协定
htable = {}
0: timer句柄
1：调用方类型：0：lua，1：js， 2：c
2：lua回调函数地址
3: js注册回调函数名称
4：c回调函数指针地址
5：c调用者传输数据地址
--]]

--创建timer句柄函数接口
--Ttype：string型参数，用于唯一标识该timer句柄
----输出：实际创建的timer句柄
local function timercreate(Ttype)
	local Ttable =getHandle(_gTimerweaklist,Ttype);
	if Ttable == nil then
		Ttable = {};
	   	Ttable[0] = timerlib.create();
	end
	registerHandle(_gTimerlist,_gTimerweaklist,Ttype,Ttable);	
	return Ttable	
end

--声明取消已经启动timer句柄函数接口
--Ttype：string型参数，用于唯一标识该timer句柄
--输出：无
local function timer_cancel(Ttype)
	local Ttable =getHandle(_gTimerlist,Ttype)
	if Ttable ~= nil then
	   	timerlib.cancel(Ttable[0]);
	end	
end


--销毁timer句柄函数接口：Ttype为string型参数，用于唯一标识该timer句柄
--该函数并没有立即销毁timer句柄，而是等到下一个回收cd之后才会彻底销毁
--输出：无
local function timerdestroy(Ttype)
	timer_cancel(Ttype)
	releaseHandle(_gTimerlist,Ttype)		
end


--开始start timer函数接口
--Ttable:table型参数，用于标识timer句柄描述
--cbkname:会动态依据不同的ntype来确定类型(lua：function型，js：string型，c：integer型)
--Ttype:string型参数，js端用于标识该timer句柄的唯一标识符
--ntype:integer型参数，用于标识该回调函数类型（lua：0，js：1，c：2）
--bRepeat:bool型参数，用于标识是否重复start.
--输出：成功返回true，失败返回false
local function timerstart(Ttype, time, cbkname,ntype,nuser,bRepeat)
	if Ttype == nil then 
		return false;
	end	

	if time == nil then
		return false;
	end

	local Ttable = timercreate(Ttype);	
	if Ttable == nil then	
	   	return false;
	end

	Ttable[1]=ntype;
	if ntype == 0 then		--lua脚本注册回调函数
		Ttable[2] = cbkname;
	elseif ntype == 1 then		--js注册回调函数
		Ttable[3] = cbkname;
	else				--c回调函数地址
		Ttable[4] = cbkname;
		Ttable[5] = nuser;		
	end
	Ttable[6] = bRepeat;
	Ttable[7] = time;
	timerlib.start(Ttable[0],time,"sys_TimerEvnet",Ttype)
	return true
end

--lua层timer事件回调处理函数：
DeclareGlobal("sys_TimerEvnet",function (Ttype)
	local Ttable =getHandle(_gTimerlist,Ttype)
	if Ttable ~= nil then
		--print(Ttable[6])

		if Ttable[6] == true then
			timerlib.start(Ttable[0],Ttable[7],"sys_TimerEvnet",Ttype)
		else
			timerdestroy(Ttype)
		end
		if Ttable[1] == 0 then
			if Ttable[2] ~= nil then
				Ttable[2](Ttype);
			end
		elseif Ttable[1] == 1 then
		--js回调
			if Ttable[3] ~= nil then
				local s;
				s = string.format("%s(\"%s\")",Ttable[3] ,Ttype);
				commlib.calljavascript(s);
			end	
		else
		--c回调
			if Ttable[4] ~= nil then
				commlib.timernotify(Ttable[4], Ttype, Ttable[5]);
			end
		end
	end
end)


--接口table
local interface = {}

--对外声明取消已经启动timer句柄函数接口
--Ttype：string型参数，用于唯一标识该timer句柄
--输出：无
createmodule(interface,"timercancel",function (Ttype)
	timer_cancel(Ttype);
end)


--对外声明判断timer是否已经启动函数接口
--Ttype：string型参数，用于唯一标识该timer句柄
--输出：已经开始定时:true,没有开始定时:false
createmodule(interface,"timerisbusy",function (Ttype)
	local Ttable = getHandle(_gTimerlist,Ttype)
	if Ttable ~= nil then
	   	return timerlib.isbusy(Ttable[0]);
	end	
end)


--对外声明lua层调用timerstart函数接口
--Ttype:string型参数，js端用于标识该timer句柄的唯一标识符
--time:int型参数，timer的时间（单位毫秒）
--cbkname：string型参数，js端注册timer的回调函数名称
--bRepeat:bool型参数，用于标识是否重复start.
--输出：成功返回true，失败返回false
createmodule(interface,"timerstartforlua",function(Ttype,time,cbkname,bRepeat)
	return timerstart(Ttype,time,cbkname,0,nil,bRepeat);
end)

--对外声明终止timer请求函数接口
--Ttype:string型参数，js端用于标识该timer句柄的唯一标识符
--输出：无
createmodule(interface,"timerabort", function (Ttype)
	timerdestroy(Ttype)
end)

tiros.timer = readOnly(interface)



