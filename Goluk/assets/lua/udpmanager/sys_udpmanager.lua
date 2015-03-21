--封装Udp发送

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/domainmanager"
require"lua/systemapi/sys_socket"

---------------------------静态局部变量-----------------------------------------------------------
--udp发送所需要的stype资源数组
--格式：
--{ stype1, stype2, ... }
local stypearray = {}	

--dataarray数组，用于存放等待udp发送的数据
--每条记录格式
--address的值包含domain或者ip
--[[
	 {
		{ address = address1, port = port1, data = data1 },
		{ address = address2, port = port2, data = data2 }
	 }
--]]
local dataarray = {}

--当前stype
local stype_id = 0

--当前要发送的数据总长度
local datalen = 0

---------------------------静态局部常量-----------------------------------------------------------

--stype资源最多个数
local G_STYPE_COUNT = 1

--stype前缀
local G_STYPE_PROFIX = "udpmanager"

--用于域名解析时注册回调输入的调用者名称
local G_UM_USER = "udpmanager"

--要发送的数据长度最大值
local G_DATA_MAXLEN = 1024 

---------------------------公共函数--------------------------------------------------------------

---------------------------局部函数--------------------------------------------------------------
--获取一个新的stype资源
--输入：id--当前要设置的stype位置(number型)
--返回: stype--新的stype值(string型)
local function GetNewStype(id)
	if ( "number"~=type(id) ) then
		return nil
	else
		return G_STYPE_PROFIX.."-"..string.format("%04u%02u%02u%02u%02u%02u",timelib.time()).."-"..id
	end
end

--获取stype资源
--返回: stype值（string型)
local function GetStype()

	if  ( G_STYPE_COUNT==stype_id ) then
		stype_id = 0
	end	

	stype_id = stype_id + 1

	if (nil==stypearray[stype_id]) then
		stypearray[stype_id] = GetNewStype(stype_id)
	end

	return stypearray[stype_id]
end

--回调函数
local function CallbackFn( event, domain, ip )
	
	if ( (nil==event) or (nil==domain) ) then
		return
	end
	
	if (0==event) then
		--域名解析成功，发送解析之前保存在dataarray中的数据
		if ( (nil~=dataarray) and (0~=(#dataarray)) ) then
				for k,v in ipairs(dataarray) do
					if ( (nil~=v) and (domain==v.address) ) then					
						tiros.socket.udpsendtoforlua( GetStype(), ip, v.port, v.data )
						datalen = datalen - #v.data
						dataarray[k] = nil
					end
				end				
		end
	elseif (1==event) then
		--域名解析失败，不处理
	end
end

--发送data数据函数
local function Send( address, port, data )

	if ( (nil==address) or (""==address) or (nil==port) or ("number"~=type(port)) or (nil==data) or (""==data) ) then
		return
	end

	--将要发送的数据插入到dataarray表中
	--处理dataarray				
	while ( (G_DATA_MAXLEN < (datalen+(#data))) and (nil~=dataarray) and (0~=(#dataarray)) and (nil~=dataarray[#dataarray]) ) do
	--删除旧数据
		datalen = datalen - (#(dataarray[#dataarray].data))
		table.remove(dataarray)
	end
	
	table.insert( dataarray, 1, { address = address, port = port, data = data } )
	datalen = datalen + (#data)

	--注册域名请求回调(对于同一个G_UM_USER多次调用实际上也只注册了一次)
	tiros.domainmanager.DM_RegistCallbackFn( G_UM_USER, tiros.udpmanager.UM_CallbackFn )
	--发送域名请求
	tiros.domainmanager.DM_GetIP( G_UM_USER, address )
end

--接口table
local interface = {}

--对域名管理模块声明的UM_CallbackFn函数接口
--检索IP的回调函数，不用知道此接口用法
createmodule(interface,"UM_CallbackFn", function ( event, domain, ip )
	return CallbackFn( event, domain, ip )
end)

---------------------------全局函数(对外接口)------------------------------------------------------

--对外申明的Udp发送数据的接口
--输入：address，域名或者ip地址(string型)
--输入：port，发送端口(number型)
--输入：data，要发送的数据(string型)
--输出：无
createmodule(interface,"UM_Send", function ( address, port, data )
	return Send( address, port, data )
end)

tiros.udpmanager  = readOnly(interface)
