require"lua/systemapi/sys_poisearch"

--[[
DeclareGlobal("luacb1", function (ptype, status, param1,param2)
tiros.poisearch.poisearchabort("type_luacb1")
print("luacb1",ptype, status, param1,param2)

end)

--test code

tiros.poisearch.poisearchforlua("type_luacb1", luacb1, 0,0,"%E5%8C%97%E4%BA%AC" )
--]]


