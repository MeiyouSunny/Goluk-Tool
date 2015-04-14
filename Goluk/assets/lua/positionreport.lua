--[[
 @描述：首次GPS定位成功后，以GPS经纬度请求服务器，解析传给调用者
 @编写人：fengfx
 @创建日期：2012-12-13 下午 17:47:00
 @修改内容：2012-12-14 fengfx 取掉无用参数，修改测试代码并测试
 @修改内容：
 @修改内容：
 @版本：0.1.1
--]]
require"lua/positionreport/sys_positionreport"

--[[
可用经纬度
lon=397075183&lat=72068504
lon=418675084&lat=143709094
lon=419468200&lat=143453975 
--]]

----[[测试代码
--tiros.PSTreport.positionreportforlua("typeluaacb1", 419251744,143742263)
--tiros.PSTreport.positionreportforlua("type_luacb2", 419031151,143668825 )
--tiros.PSTreport.positionreportforlua("type_luacb2")
--tiros.PSTreport.positionreportforlua("type_luacb3", 418675084,143709094 )
--]]
