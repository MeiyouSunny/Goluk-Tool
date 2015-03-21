--管理全局句柄的注册、释放、及状态显示接口

--依据htype标识查找该标识对应的句柄是否已经注册到week表中
--weaklist：weektable，用于注册htype类型对应的句柄
--htype：string型参数，用于记录句柄的关联型标识
--返回值：与htype相关连的value
DeclareGlobal("getHandle", function (weaklist,htype)
  	local h = weaklist[htype];
	return h;
end)

--注册htype标识查找该标识对应的句柄是否已经注册到week表中
--weaklist：weektable，用于注册htype类型对应的句柄
--htype：string型参数，用于记录句柄的关联型标识
--h：userdata型参数，与htype关联的句柄
--返回值：无
DeclareGlobal("registerHandle", function (list,weaklist,htype,h)
	if weaklist ~= nil then
		weaklist[htype]= h;
	end
	if list ~= nil then
		list[htype]= h;
	end
end)

--释放htype标识所对应的句柄，将该句柄从全局对象表中删除，week表中还继续保存
--list：weektable，用于注册htype类型对应的句柄
--htype：string型参数，用于记录句柄的关联型标识
--返回值：成功返回true，失败返回false
DeclareGlobal("releaseHandle", function (list,htype)
	if (list == nil) or (htype == nil) or (type(list) ~= "table") then
		return false;
	end 
	local b =false;
	for k,v in pairs(list) do
		if htype == k then
			list[htype]= nil;
			b =true;
			break;
		end
    	end	
	return b;			
end)
--输出调试信息
DeclareGlobal("luaprint", function (...)
	print(...);
end)
--输出_G全局表中所有的key和value
DeclareGlobal("showglobal", function()
--[[	
for k,v in pairs(_G) do
       print("global-",k,v)
end
--]]
end)

--输出table中所有的key和value
DeclareGlobal("showlist", function (t)
--[[
	for k,v in pairs(t) do
       print("list-",k,v)
    end
--]]
end)
