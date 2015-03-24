--lua脚本timer对外接口
--备注：函数前缀为该函数返回类型
--bool timerstartforjs(Ttype,time,cbkname);
--void timerabort", function (Ttype);
require"lua/systemapi/sys_timer"
--测试代码
--备注：notify为js端回调函数名称
--[[
DeclareGlobal("notify", function (ptype)
tiros.timer.timerabort("abc")
end)

--tiros.timer.timerstartforlua("abc",100,notify);
--tiros.timer.timerstartforc("abc",100,"notify",)
--print(timerisbusy("abc"))
--tiros.timer.timerabort("abc");
--]]
