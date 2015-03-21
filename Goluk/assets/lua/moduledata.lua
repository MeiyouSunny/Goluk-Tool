--lua脚本moduledata对外接口
--
require"lua/moduledata/sys_moduledata"

--测试代码
--备注：
--[[

tiros.moduledata.moduledata_set("map", "location", nil, nil, nil)
tiros.moduledata.moduledata_set("map", "test", "312312312312311adsqwetyui")
tiros.moduledata.moduledata_set("map", "1", "1248974532")
tiros.moduledata.moduledata_set("map", "2", "asdfghgjkl")
tiros.moduledata.moduledata_set("map", "3", "0988765436")
tiros.moduledata.moduledata_set("map", "4", "mnbvccccccvb")
tiros.moduledata.moduledata_set("navi", "abc", "string:abcde")
tiros.moduledata.moduledata_set("web", "poi", "88888888")

print("\"map\"-location:", tiros.moduledata.moduledata_get("map", "location"))
print("\"navi\"-abc:", tiros.moduledata.moduledata_get("navi", "abc"))
print(tiros.moduledata.moduledata_exist("navi", "abc"))

tiros.moduledata.moduledata_set("map", "location", "lon=1111111;lat=3333333")
print("\"map\"-location:", tiros.moduledata.moduledata_get("map", "location"))

tiros.moduledata.moduledata_clean("map")
print(tiros.moduledata.moduledata_exist("map", "location"))
print("\"map\"-location:", tiros.moduledata.moduledata_get("map", "location"))
--]]

