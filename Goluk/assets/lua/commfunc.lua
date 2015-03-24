--[[
 @描述:公共函数库 commFunc
 @编写人:孔祥宇
 @创建日期: 2012-08-02 下午 10:40:00
 @新增接口：URL编码，提供url的网络编码及解码接口	孔祥宇 2012-08-02	
--]]
require"lua/systemapi/sys_commfunc"

--测试代码
--[[
local e = tiros.commfunc.EnCodeUrl("-")
print(e)
local k = tiros.commfunc.UnEscape(e)
print(k)
--]]
