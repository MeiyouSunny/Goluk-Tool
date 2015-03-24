--cjson库
require"lua/json/sys_json"

--测试代码
--[[
local test = {
  one='first',two='second',three={2,3,5}
}

local jsonTest = tiros.json.encode(test)

print('JSON encoded test is: ' .. jsonTest)

-- Now JSON decode the json string
local result = tiros.json.decode(jsonTest)

print ("The decoded table result:")
table.foreach(result,print)
print ("The decoded table result.three")
table.foreach(result.three, print)
--]]
