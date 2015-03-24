--网络管理模块
--
require"lua/systemapi/sys_namespace"


--接口table
local interface = {}

createmodule(interface,"netManagerInit",function ()
	netmanagerlib.netManagerInit()
end)

createmodule(interface,"netManagerRelease",function ()
	netmanagerlib.netManagerRelease()
end)

tiros.netManager  =  readOnly(interface)

