--封装Udp发送

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/socket"

---------------------------静态局部变量-----------------------------------------------------------
--域名解析stype表
--格式：
--{ stype1=domain1, stype2=domain2, ... }
--约定： domain有三个值： nil--表示释放资源; ""--让stype空闲; 其他域名值--对应相应的域名 
local stypetable = {}	

--所有需要解析的域名表
--格式：
--{ domain1 = {user1,user2,...}, domain2 = {user3,user4,...} ... }
--注意：如果用户没有等到域名解析完成就release了回调函数，那么会有一种情况，那就是某个user在Fntable表中找不到Fn
local domaintable = {}

--回调函数表，用于存放所有者对应的回调函数
--格式：
--{ user1 = Fn1, user2 = Fn2, ... }
local Fntable = {}

--iptable表，用于存放所有解析成功的域名对应的ip值
--格式：
--{ domain1 = ip1, domain2 = ip2, ... }
local iptable = {}

---------------------------静态局部常量-----------------------------------------------------------
--stype资源最多个数
local G_STYPE_COUNT = 1

--stype前缀
local G_STYPE_PROFIX = "domain"

---------------------------公共函数--------------------------------------------------------------

--检查域名字符串
--注意：此处只是简单的校验IP地址是否正确
--返回ip地址字符串或者nil(没有找到)
local function checkip( domain )
	return string.match( domain, "^%d?%d?%d%.%d?%d?%d%.%d?%d?%d%.%d?%d?%d$" )
end

---------------------------局部函数--------------------------------------------------------------

--获取一个新的stype资源
--输入：count--当前已经存在的stype个数(number型)
--返回: 新的stype值(string型)
local function GetNewStype(count)
	if ( "number"~=type(count) ) then
		return nil
	else
		return G_STYPE_PROFIX.."-"..string.format("%04u%02u%02u%02u%02u%02u",timelib.time()).."-"..(count+1)
	end
end

--获取stype资源
--返回: nil--没有空闲资源; stype--返回的stype值
local function GetStype()

	local count = 0
	
	--检索stypetable表
	for k,v in pairs(stypetable) do
		count = count + 1
		if (""==v) then
			--找到闲置stype资源
			return k
		end
	end
	
	--没有找到闲置stype资源
	if (G_STYPE_COUNT>count) then  --还可以产生新的stype
		return GetNewStype(count)
	elseif (G_STYPE_COUNT<=count) then 	--stype资源已满
		return nil
	end
end

--解析IP地址回调函数
local function CallBackFn(stype, event, param1, param2)

	--处理域名解析结果
	if ( (nil~=stype) and (nil~=stypetable[stype]) and (""~=stypetable[stype]) and (nil~=param1) ) then
		--将解析成功的域名及对应的IP加入iptable表
		if (0==event) then --解析域名成功
			iptable[stypetable[stype]] = param1
		end
	
		--发送回调
		if (nil~=domaintable[stypetable[stype]]) then	
			for k,v in ipairs(domaintable[stypetable[stype]]) do
				if (nil~=Fntable[v]) then	
					if (0==event) then --解析域名成功	
						Fntable[v]( 0, stypetable[stype], param1 )
					elseif  (4==event) then	--解析域名失败	
						Fntable[v]( 1, stypetable[stype], nil )
					end
				end
			end
			--删除已经完成解析的域名任务
			domaintable[stypetable[stype]] = nil
		end
	end

	--闲置stype资源
	stypetable[stype] = ""

	--检查等待查询的域名队列，发起一个请求
	for k,v in pairs(domaintable) do
		stypetable[stype] = k
		tiros.socket.sktobtainipforlua( stype, k, tiros.domainmanager.DM_CallBackFn )
		break
	end
end



--注册调用者的回调函数
-- 参数：User---用于域名解析时注册回调输入的调用者名称(string型)
-- 参数：Fn -- 用者定义回调函数(string型)
--调用者定义回调函数的格式：
-- (Fn)( event, domain, ip )
-- 参数： event, 返回事件， 0--成功， 1--失败(event为number型)
-- 参数： event为0时，domain返回查询的域名，ip为查询成功的ip(event为number型)
-- 参数： event为1时，domain返回查询的域名，ip为nil(event为number型)
local function RegistCallbackFn( User, Fn )

	if ( (nil==User) or (nil==Fn) or (nil~=Fntable[User]) ) then
		return false
	end

	Fntable[User] = Fn
	return true
end

--释放调用者的回调函数
local function ReleaseCallbackFn( User )

	Fntable[User] = nil
end

--根据域名获取相应的ip地址
--输入：User，调用者名称(string型)
--输入：domain，域名(string型)
--输出：无
local function GetIP( User, domain )

	local ip = nil
	local stype = nil

	if ( (nil==User) or (""==User) or (nil==Fntable[User]) or (nil==domain) or (""==domain) ) then
		return
	end
	
	--检查域名是否为ip
	ip = checkip(domain)

	if (nil~=ip) then	--此域名为ip地址，直接返回ip地址
		Fntable[User]( 0, domain, ip )
	else 	--此域名不为ip地址
		--检索iptable表
		if (nil~=iptable[domain]) then	--已经成功解析过此域名，直接返回ip地址
			Fntable[User]( 0, domain, iptable[domain] )
		else 	--还没有解析过此域名，或者没有解析完成，或者解析没有成功
			--处理domaintable				
			if (nil==domaintable[domain]) then 
				domaintable[domain] = {}
			end
			table.insert( domaintable[domain], User )

			--获取stype资源
			stype = GetStype()
			if ( nil~=stype ) then 	--成功，发送查询请求
				--处理stypetable表
				stypetable[stype] = domain
				--发送查询请求	
				tiros.socket.sktobtainipforlua( stype, domain, tiros.domainmanager.DM_CallBackFn )
			end
		end	
	end	
end

--接口table
local interface = {}

--对请求域名声明的DM_CallBackFn函数接口
--检索IP的回调函数，不用知道此接口用法
createmodule(interface,"DM_CallBackFn", function (stype, event, param1, param2)
	return CallBackFn(stype, event, param1, param2)
end)

---------------------------全局函数(对外接口)------------------------------------------------------

--注册调用者的回调函数
--输入：User，调用者名称(string型)
--输入：Fn，回调函数名，要求名称全局唯一
--输出：true--成功; false--失败
--调用者定义回调函数的格式：
-- (Fn)( event, domain, ip )
-- 参数： event, 返回事件， 0--成功， 1--失败
-- 参数： event为0时，domain返回查询的域名，ip为查询成功的ip字符串
-- 参数： event为1时，domain返回查询的域名，ip为nil
createmodule(interface,"DM_RegistCallbackFn", function ( User, Fn )
	return RegistCallbackFn( User, Fn )
end)

--释放调用者的回调函数
--输入：User，调用者名称(string型)
--输出：无
createmodule(interface,"DM_ReleaseCallbackFn", function ( User )
	return ReleaseCallbackFn( User )
end)

--根据域名获取相应的ip地址
--输入：User，调用者名称(string型)
--输入：domain，域名 (string型)
--输出：无
createmodule(interface,"DM_GetIP", function ( User, domain )
	return GetIP( User, domain )
end)

tiros.domainmanager = readOnly(interface)

