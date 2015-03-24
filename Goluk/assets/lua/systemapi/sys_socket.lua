--socket接口封装
--目前主要对外udpsendto及socketabort接口

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"

--socketlist：用于存放正在使用的所有socket句柄
local _gSocketlist = {}
--socketweaklist：用于存放所有socket句柄的week表，week表中既包含正使用的句柄，也包含即将回收的句柄
local _gSocketweaklist = {}
setmetatable(_gSocketweaklist,{__mode ="v" })

local _gSocketEnvReady = true;

--[[
socket全局变量table，协定
stable = {}
0: socket句柄
1：调用方类型：0：lua，1：js， 2：c
2：lua回调函数地址
3: js注册回调函数名称
4：c回调函数指针地址
5：c调用者传输数据地址
6: sockettype
--]]


--创建socket句柄函数接口
--stype：string型参数，用于唯一标识该socket句柄
--ntype:integer型参数，用于标识该回调函数类型（lua：0，js：1，c：2）
--sockettype:integer型参数(TCP:0,UDP:1,HOST:2)
--cbkname:会动态依据不同的ntype来确定类型(lua：function型，js：string型，c：integer型)
--user：integer型参数，可为nil，c端注册的调用者参数地址
----输出：实际创建的socket句柄
--备注：如果改stype所在的句柄存在，则覆盖，之前的句柄会在下次垃圾回收时回收
local function sktcreate(stype, ntype, sockettype, cbkname, user)
	local stable = getHandle(_gSocketweaklist,stype);
	if stable == nil then
		stable = {}
	   	stable[0] = socketlib.open(sockettype, "sys_lua_SocketEvent", stype);
	elseif stable[6] ~= sockettype then --sockettype类型不同，则需释放之前的socket句柄
		socketlib.close(stable[0]);
		stable[0] = socketlib.open(sockettype, "sys_lua_SocketEvent", stype);
	end
	stable[1] = ntype;
	if ntype == 0 then		--lua脚本注册回调函数
		stable[2] = cbkname;
	elseif ntype == 1 then		--js注册回调函数
		stable[3] = cbkname;
	else				--c回调函数地址
		stable[4] = cbkname;
		stable[5] = user;
	end
	stable[6] = sockettype;
  	registerHandle(_gSocketlist,_gSocketweaklist,stype,stable);
	return stable;
end

--绑定本地UDP端口
 --stable - Socket结构体指针
 --port - 本地端口号(integer型)
local function sktbind(stable, port)
	socketlib.bind(stable[0], port);
end


--[[
typedef enum _SocketEvent
{
    EVT_OBTAINED_IP = 0,   ///< 解析域名成功
    EVT_CONNECTED,         ///< 连接成功
    EVT_SENT,              ///< 再次调用send的通知
    EVT_RECEIVED,          ///< 再次调用recv的通知
    EVT_ERROR              ///< 发生错误
} SocketEvent;
--]]

--lua层socket事件回调处理函数：
DeclareGlobal("sys_lua_SocketEvent",function (stype,event,param1, param2)
	local stable = getHandle(_gSocketlist,stype)
	if stable ~= nil then
		if stable[6] == 2 then
			--sockettype为域名解析类型，则此处释放该句柄
			--releaseHandle(_gSocketlist,stype);
		end
		if stable[1] == 0 then
			if stable[2] ~= nil then
				stable[2](stype,event,param1, param2);
			end
		elseif stable[1] == 1 then
		--js回调
			if stable[3] ~= nil then
				local s;
				if event == 0 or event == 3 then --EVT_OBTAINED_IP or EVT_RECEIVED  %s( \"%s\", %u, \"%s\", %u );
					s = string.format("%s( '%s', %u, '%s', %u );", stable[3],stype,event,param1,param2);
				else
					s = string.format("%s( '%s', %u, %u, %u );", stable[3],stype,event,param1,param2);
				end
				commlib.calljavascript(s);
			end	
		else
		--c回调
			if stable[4] ~= nil then
				commlib.socketnotify(stable[4], stype, stable[5],event,param1,param2);
			end
		end
	end
end)


--向一指定目的地发送数据(UDP)
--stable：table型参数，用于唯一标识该socket句柄
--ip: string型参数，ip地址
--port:integer型参数，端口号
--data:string型参数，数据内容
local function sktsendto(stable, ip, port, data)
	if _gSocketEnvReady == true then
		socketlib.sendto(stable[0], ip, port, data, #data);
		return true;
	end
	return false;
end

--向一指定目的地发送数据(UDP)
--stype：string型参数，用于唯一标识该socket句柄
--ntype:integer型参数，用于标识该回调函数类型（lua：0，js：1，c：2）
--sockettype:integer型参数(TCP:0,UDP:1,HOST:2)
--ip: string型参数，ip地址
--port:integer型参数，端口号
--data:string型参数，数据内容
--cbkname:会动态依据不同的ntype来确定类型(lua：function型，js：string型，c：integer型)
--user：integer型参数，可为nil，c端注册的调用者参数地址
local function udpsendto(stype, ntype, sockettype, ip, port, data, cbkname, user)
	local stable = sktcreate(stype, ntype, sockettype, cbkname, user)
	if stable == nil then
		return false;
	end

	return sktsendto(stable, ip, port, data);
	--用于对没有注册回调函数直接由系统自动回收，注意此处不能显示调用系统回收函数
	--if cbkname == nil then
	--	releaseHandle(_gSocketlist,stype);
	--end
end

--获取域名对应的IP地址
--stype：string型参数，用于唯一标识该socket句柄
--ntype:integer型参数，用于标识该回调函数类型（lua：0，js：1，c：2）
--sockettype:integer型参数(TCP:0,UDP:1,HOST:2)
--domain: string型参数，域名
--cbkname:会动态依据不同的ntype来确定类型(lua：function型，js：string型，c：integer型)
--user：integer型参数，可为nil，c端注册的调用者参数地址
local function sktobtainip(stype, ntype, domain, cbkname, user)
	local stable = sktcreate(stype, ntype, 2, cbkname, user)
	if stable == nil then
		return false;
	end
	socketlib.obtainip(stable[0], domain);
end


--删除所有socket句柄，包括week表
local function sktdestroylist()
	for k,v in pairs(_gSocketlist) do
		_gSocketlist[k] = nil
	end
	for k,v in pairs(_gSocketweaklist) do
		if v ~= nil then
			if type(v) == "table" then
				socketlib.close(v[0])
			end
		end
		_gSocketweaklist[k] = nil
	end
end

--socket环境暂停
local function socketEnvSuspend()
	sktdestroylist();
	_gSocketEnvReady = false;
end

--socket环境恢复
local function socketEnvResume()
	_gSocketEnvReady = true;
end

--*******************************************************************************
--
--
--
--*******************************************************************************
--接口table
local interface = {}

--销毁socket句柄函数接口：stype为string型参数，用于唯一标识该socket句柄
--该函数并没有立即销毁socket句柄，而是等到下一个回收cd之后才会彻底销毁
--输出：无
createmodule( interface,"sktdestroy", function(stype)
	local stable = getHandle(_gSocketlist,stype);
	if stable ~= nil  then
		socketlib.close(stable[0]);
	end
	releaseHandle(_gSocketlist,stype);
end)

---对外声明lua层调用 -- 向一指定目的地发送数据(UDP)
--stype：string型参数，用于唯一标识该socket句柄
--ip: string型参数，ip地址
--port:integer型参数，端口号
--data:string型参数，数据内容
createmodule(interface,"udpsendtoforlua",function (stype, ip, port, data)
	return udpsendto(stype, 0, 1, ip, port, data, nil, nil)
end)

---对外声明lua层调用--获取域名对应的IP地址
--stype：string型参数，用于唯一标识该socket句柄
--domain: string型参数，域名
--cbkname:会动态依据不同的ntype来确定类型(lua：function型，js：string型，c：integer型)
createmodule(interface,"sktobtainipforlua",function (stype, domain, cbkname)
	return sktobtainip(stype, 0, domain, cbkname, nil)
end)

--对外声明终止socket请求函数接口
--stype:string型参数，js端用于标识该socket句柄的唯一标识符
--输出：无
createmodule(interface,"socketabort", function (stype)
	sktdestroy(stype)
end)

--对外声明释socekt环境暂停
--此接口主要用于类似ios平台在程序进入后台时暂停环境
--输出：无
createmodule(interface,"socketEnvSuspend", function ()
	socketEnvSuspend()
end)

--对外声明释放所有socket函数接口
--恢复socket
--此接口主要用于类似ios平台在程序进入前台时恢复环境
--输出：无
createmodule(interface,"socketEnvResume", function ()
	socketEnvResume()
end)

tiros.socket = readOnly(interface)




