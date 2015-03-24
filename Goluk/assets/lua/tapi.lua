--lua脚本tapi对外接口
--
require"lua/systemapi/sys_tapi"

--测试代码
--备注：notify为js端回调函数名称
--[[
print(tapigetbscount())
local lac,cellid,mcc,mnc,signalstrength,lat,lon
local lac,cellid,mcc,mnc,signalstrength,lat,lon = tiros.tapi.tapigetbsbyindex(0)
print(lac,cellid,mcc,mnc,signalstrength,lat,lon)
local name,mac,ip,signalstrength = tiros.tapi.tapigetconnwifiinfo()
print(name,mac,ip,signalstrength)
print(tapigetmobileid())
print(tapigetimsi())
print(tapigetnettype())
--]]


