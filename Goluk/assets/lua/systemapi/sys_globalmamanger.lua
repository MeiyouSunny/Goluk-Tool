--globalmamanger
--管理全局变量的定义

function readOnly (t)
local proxy = {}

local mt = {
-- create metatable
__index = t,
__newindex = function (t,k,v)
error("attempt to update a read-only table", 2)
end
}
setmetatable(proxy, mt)
return proxy
end

declaredNames = {}

function DeclareGlobal(name, initval)
	if rawget(_G, name) == nil then
			rawset(_G, name, initval or false)
    else
			error("the variable have declared"..n, 2)
    end
end

setmetatable(_G, {
	__newindex = function (_, n)
		error("attempt to write to undeclared variable "..n, 2)
	end,
	
	__index = function (_, n)
		error("attempt to read undeclared variable "..n, 2)
	end,
	}
)
