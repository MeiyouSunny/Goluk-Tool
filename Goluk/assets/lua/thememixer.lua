--[[
 @描述：主题兴趣点融合
 @编写人：harry
 @创建日期：2012-11-09
 @修改内容：
 @修改内容：
 @版本：0.1.0
--]]

require"lua/theme/sys_thememixer"

--[[
可用经纬度
lon=397075183&lat=72068504
lon=418675084&lat=143709094
lon=419468200&lat=143453975 
--]]

--[[测试代码
DeclareGlobal("luacb1", function (ptype, status, param1, param2)
print("luacb1",ptype, status, param1, param2);
end)

DeclareGlobal("luacb2", function (ptype, status, param1, param2)
print("luacb2",ptype, status, param1, param2);
end)

DeclareGlobal("luacb3", function (ptype, status, param1, param2)
print("luacb3",ptype, status, param1, param2);
end)

tiros.PSTdescription.positiondescriptionforlua("type_luacb1", luacb1, 419320552,143664440 )
tiros.PSTdescription.positiondescriptionforlua("type_luacb2", luacb2, 419031151,143668825 )
tiros.positiondescriptionabort("type_luacb2")
tiros.positiondescriptionforlua("type_luacb3", luacb3, 418675084,143709094 )
positiondescriptionforlua("test3", luacb2, 397075183,72068504 )
--]]
