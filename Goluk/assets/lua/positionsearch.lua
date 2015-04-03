--[[
 @描述：大头针地点区域接口，获取地图点经纬度，并post给服务器，等待服务器返回数据，解析传给调用者
 @编写人：fengfx
 @创建日期：2012-03-11 下午 15:40:00
 @修改内容：添加测试代码	fengfx 2012-04-09
 @修改内容：修改测试代码	fengfx 2012-04-18
 @修改内容：按照新的LUA编码规范修改源码，添加注释 fengfx 2012-08-01
 @版本：0.1.3
--]]
require"lua/positionsearch/sys_positionsearch"
--[[
可用经纬度
lon=397075183&lat=72068504
lon=418675084&lat=143709094
lon=419468200&lat=143453975 
--]]

--[[测试代码
DeclareGlobal("luaacb1", function (ptype, status, param1, param2)
print("luaacb1",ptype, status, param1, param2);
end)


DeclareGlobal("luacb2", function (ptype, status, param1, param2, err)
print("luacb2",ptype, status, param1, param2);
end)

DeclareGlobal("luacb3", function (ptype, status, param1, param2, err)
print("luacb3",ptype, status, param1, param2);
end)

tiros.POIsearch.positionsearchforlua("type_luaacb1", luaacb1, 419251744,143742263 ,419251744,143742263 ,"圣世一品")
--tiros.POIsearch.positiondescriptionforlua("type_luacb2", luacb2, 419031151,143668825 )
--tiros.POIsearch.positiondescriptionabort("type_luacb2")
--tiros.POIsearch.positiondescriptionforlua("type_luacb3", luacb3, 418675084,143709094 )
--]]
