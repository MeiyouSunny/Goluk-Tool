--检索域名获取IP地址接口

require"lua/domainmanager/sys_domainmanager"

--(1) bool DM_RegistCallbackFn( User, Fn )
--注册调用者的回调函数
--输入：User，字符串型，调用者名称
--输入：Fn，回调函数名，要求名称全局唯一
--输出：true--成功; false--失败
--接口约定：
--调用者定义回调函数的格式：
-- (Fn)( event, domain, ip )
-- 参数： event, 返回事件， 0--成功， 1--失败
-- 参数： event为0时，domain返回查询的域名，ip为查询成功的ip字符串
-- 参数： event为1时，domain返回查询的域名，ip为nil

--(2) DM_ReleaseCallbackFn( User )
--释放调用者的回调函数
--输入：User，字符串型，调用者名称
--输出：无

--(3) DM_GetIP( User, domain )
--根据域名获取相应的ip地址
--输入：User，字符串型，调用者名称
--输入：domain，字符串
--输出：无

---------------------------测试代码--------------------------------------------------------------

--[[
print(checkip("www.hubo.com"))
print(checkip("192.168.1"))
print(checkip("192.168.4.1"))
print(checkip("192.168"))
print(checkip("192.168.1.6.43"))
print(checkip("1934342.168.1"))
print(checkip("19234.138.1.234"))
--]]

--[[
DeclareGlobal("dddFn", function (event, domain, ip)
	print("event,domain,ip==",event, domain, ip)
end)

DM_RegistCallbackFn( "aaa", dddFn )

tiros.domainmanager.DM_GetIP( "aaa", "www.hubo.com" )
tiros.domainmanager.DM_GetIP( "aaa", "www.hubo.net" )
tiros.domainmanager.DM_GetIP( "aaa", "www.sina.com.cn" )
tiros.domainmanager.DM_GetIP( "aaa", "www.163.com" )
tiros.domainmanager.DM_GetIP( "aaa", "178.0.2.56" )
tiros.domainmanager.DM_GetIP( "aaa", "wwwsdfasfads" )

--tiros.ReleaseCallbackFn( "aaa" )

--]]


