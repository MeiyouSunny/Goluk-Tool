--[[
 @描述：配置文件读写
 @编写人：王成 <wangcheng@tiros.com.cn>
 @创建日期：2012-03-2 
 @版本：0.1.0
--]]
require"lua/systemapi/sys_globalmamanger"
require"lua/systemapi/sys_handle"
require"lua/systemapi/sys_namespace"

--全局表变量
local gT = {}
--全局文件名称变量
local gfilename = nil
--全局文件数据变量
local gfiledata=nil

--初始化全局表
createmodule(gT, "configlist", {})

--[[
  @描述：获取全局表
 @param strUser string型参数，唯一标识符
 @param bCreate bool，用于标识是否创建全局表
 @return table类型全局表对象
--]]
local function GetCfgTable(strUser, bCreate)
	luaprint("GetCfgTable")
	if strUser then
		--从全局表获取strUser对应的配置文件table
		local t = gT.configlist[strUser]
		--若为空，并且需要创建
		if not t and bCreate then
			--创建时置为空表
			t = {};	
			gT.configlist[strUser] = t
		end	
	luaprint("GetCfgTable1")	
		return t;
	end
	return nil	
end

--[[
  @描述：释放全局表
 @param strUser string型参数，唯一标识符
 @return 无
--]]
local function RemoveCfgTable(strUser)
	luaprint("RemoveCfgTable")
	if strUser then
		--在全局表中删除strUser对应的字段
		rawset(gT, strUser, nil)
		gT.configlist[strUser] = nil		
	end
	luaprint("RemoveCfgTable1")
end

--[[
  @描述：保存文件数据到全局表
 @param strFname string型参数，文件名称
 @param data string型参数，文件数据
 @return 无
--]]
local function Savefiledata(strFname, strData)
	luaprint("Savefiledata")		
	if strFname == nil or strData == nil then
		--文件名与数据都不能为空
		return 
	end
	
	if gfiledata == nil then--数据为空时
		--初始化赋值文件数据
		gfiledata = strData;		
		gfilename = strFname;
	else--数据不为空时
		--追加文件数据
		gfiledata = gfiledata..strData;
	end
	luaprint("Savefiledata1")
end

--[[
  @描述：将文件数据写入文件
 @param strFname string型参数，文件名称
 @param strData string型参数，文件数据
 @return 成功返回true，否则失败
--]]
local function Writefile(strFname, strData)
	luaprint("Writefile")
	if strFname == nil or strData == nil then
		--文件名与数据都不能为空
		return false
	end
	
	--文件是否存在
	local bExist = filelib.fexist(strFname);
	--文件句柄
	local f

	--若文件存在
	if (bExist) then
		--直接打开文件
		f = filelib.fopen(strFname, 1);
	else	
		--创建并打开文件	
		f = filelib.fopen(strFname, 3);	
	end
		
	--创建失败返回	
	if f == nil then				
	   	return false;
	end

	--文件指针移到文件末尾
	filelib.fseek(f, 1, 0);
	
	--文件数据大小
	local nLen = string.len(strData);
	--写数据
	local nWriteSize = filelib.fwrite(f, strData, nLen);
	--关闭文件
	filelib.fclose(f);
	luaprint("Writefile",nWriteSize,nLen)
	--实际写入数据大小与目标大小相等返回true
	if(nLen == nWriteSize)then		
		return true;
	else		
		return false;
	end
end

--[[
  @描述：删除文件
 @param strFname string型参数，文件名称
 @return 成功返回true，否则失败
--]]
local function Removefile(strFname)
	--删除文件
	luaprint("Removefile")
	local bfile = filelib.fremove(strFname);
	luaprint("Removefile",bfile)
	--成功则返回true
	if(bfile)then
		return true;
	else
		
		return false;
	end

end


--[[
 @描述：迭代写文件
 @param o table型参数，文件数据表
 @param fname string型参数，文件名称
 @param tname string型参数，类型名称
 @return 无
--]]
local function serialize (o, fname, tname)
    --拼接文件中table名称 例如：tiros.config.logic
    luaprint("serialize")
    tname = "tiros.config."..tname

    --写数据为bool型	
    if type(o) == "boolean" then
	if o then
	 	--迭代保存本次数据到全局文件数据
		Savefiledata(fname, "true")
	else
		Savefiledata(fname, "false")
	end   	      
    elseif type(o) == "number" then--写数据为number型
       Savefiledata(fname, o)    
    elseif type(o) == "string" then--写数据为string型
	--字符串转换
	local str =string.format("%q", o)
	Savefiledata(fname, str)   
    elseif type(o) == "table" then--写数据为table型
       Savefiledata(fname, tname.."{\n")

       --保存子table每个k-v
       for k,v in pairs(o) do
	   Savefiledata(fname, " ")
	   Savefiledata(fname, k)
           Savefiledata(fname, " = ")
           --嵌套
           serialize(v,fname,tname)
           Savefiledata(fname, ",\n")
       end
       Savefiledata(fname, "}\n")
    else
       --类型不对时处理
       --error("cannot serialize a " .. type(o))
    end
    luaprint("serialize1")
end


--[[
 @描述：检查文件是否存在，无则创建
 @param profilename string型参数，文件路径
 @param strUser string型参数，惟一标识
 @return 无
--]]
local function checkfile(profilename, strUser) 	
	--文件是否存在	
	luaprint("checkfile")
	local bExist = filelib.fexist(profilename);
	if not bExist then	
		--若不存在则创建
		local f = filelib.fopen(profilename, 3);
		--声明空表数据
		local data = "tiros.config."..strUser.."{}"
		local len = string.len(data);
		--默认希尔空表数据
		filelib.fwrite(f, data, len);
		filelib.fclose(f);	
	end
	luaprint("checkfile1")
end

--[[
 @描述：对外声明释放配置文件缓存函数接口
 @param strUser string型参数，文件名
 @return 无
--]]
createmodule(gT,"ProfileStop", function (strUser) 	
	--保存文件
	luaprint("ProfileStop")
	gT.SaveTable(strUser)
	--释放全局表中该文件字段
	RemoveCfgTable(strUser)
	luaprint("ProfileStop1")
end)

--[[
 @描述：对外声明保存一次配置文件函数接口
 @param strUser string型参数，文件名
 @return bool型参数， 成功返回true，失败返回false
--]]
createmodule(gT,"SaveTable", function (strUser) 
	luaprint("SaveTable")
	if strUser~=nil then
		--获取需要保存的文件table
		local t = GetCfgTable(strUser)

		--文件table为nil则返回
		if not t then 
		 	return false
		end
		
		--删除旧文件
		Removefile(t.profilename)
		
		--全局变量置空
		gfilename = nil
		gfiledata=nil
		
		--循环写入文件数据并保存到本地
		serialize(t.Entry_Table, t.profilename, strUser)
		Writefile(gfilename, gfiledata)
	luaprint("SaveTable1")
		return true
	end
	return false
end)

--[[
 @描述：对外声明获取指定key的值函数接口
 @param strUser string型参数，文件名
 @param key string型，索引名
 @return 失败返回nill,成功:如果key等于nil返回一个table，如果key不为nil，则返回相应的value(string型，integer型，bool型)
--]]
createmodule(gT,"getValue",function(strUser, key)
	luaprint("getValue")
	if strUser~=nil then
		--获取需要保存的文件table
		local t = GetCfgTable(strUser)
	
		--文件table为nil则返回
		if not t then 
		 	return nil
		end

		--文件table中文件内容为空则返回
		if t.Entry_Table == nil then 
			return nil
		end
		luaprint("getValue1")
		--key为空返回全部内容，否则返回对应的value
		if key== nil then
			return t.Entry_Table
		else
			return t.Entry_Table[key]
		end
	end
 end)

--[[
 @描述：对外声明设置一条记录函数接口
 @param strUser string型参数，文件名
 @param key string型，索引名
 @return 无
--]]

createmodule(gT,"setValue",function(strUser,key,value) 
	luaprint("setValue")
	if strUser~=nil then
		--获取需要保存的文件table
		local t = GetCfgTable(strUser)

		--文件table为nil则返回
		if not t then 
		 	return
		end
		
		--文件table保存文件内容
		t.Entry_Table[key] = value
	end
	luaprint("setValue1")
end)

--[[
 @描述：对外声明删除一条记录函数接口
 @param strUser string型参数，文件名
 @param key string型，索引名
 @return 无
--]]
createmodule(gT,"removeValue",function(strUser, key) 
	luaprint("removeValue")
	if strUser~=nil then
		--获取需要保存的文件table
		local t = GetCfgTable(strUser)

		--文件table为nil则返回
		if not t then 
		 	return
		end	
		
		--key为空删除全部内容，否则删除对应的value
		if key == nil then
			t.Entry_Table = {}
		else
			t.Entry_Table[key] = nil
		end	
	end	
	luaprint("removeValue1")
end)

--[[
 @描述：对外声明创建配置文件函数接口
 @param floder string型参数，文件目录（在config目录中的子目录名）
 @param fname string型，文件名
 @return 无
--]]
createmodule(gT,"ProfileStart",function(floder, fname)
	luaprint("ProfileStart")
	if fname then 	
		--获取需要保存的文件table，若无则创建
		local t = GetCfgTable(fname, true)
		
		--文件全路径
		t.profilename = "config/"..floder.."/"..fname	

		--检查文件是否存在
		checkfile(t.profilename, fname)
		
		--声明配置文件读取函数	
		rawset(gT, fname, function(b) t.Entry_Table = b end)
		
		--加载文件，执行配置文件读取函数
		dofile(t.profilename) 
	end
	luaprint("ProfileStart1")
end)

--将全局表设置为只读
tiros.config = readOnly(gT)


