--filemerge接口封装
require"lua/systemapi/sys_globalmamanger"
require"lua/systemapi/sys_handle"
require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_file"

local gfiledata = nil;
local function Savefiledata(data)	
	if data == nil then
		return
	end
	if gfiledata == nil then
		gfiledata = data;
	else
		gfiledata = gfiledata..data;
	end
end

local function Writefile(fname,data)
	if fname == nil or data == nil then
		return false
	end
	local bExist = filelib.fexist(fname);
	local f
	if (bExist) then
		f = filelib.fopen(fname,1);
	else		
		f = filelib.fopen(fname,3);	
	end
	if f == nil then				
	   	return false;
	end
	filelib.fseek(f,1,0);
	filelib.fwrite(f,data,string.len(data));
	filelib.fclose(f);
end

--Remove file  functiron
local function Removefile(fname)
	local bfile = filelib.fremove(fname);
	if(bfile)then
		return true;
	else
		return false;
	end
end


--serialize table
local function serialize (o,tname)
	tname = "tiros.config."..tname	
    if type(o) == "boolean" then
		if o then
			Savefiledata("true")
		else
			Savefiledata("false")
		end   	  
    elseif type(o) == "number" then
       Savefiledata(o)
    elseif type(o) == "string" then
		local str =string.format("%q", o)
		Savefiledata(str)
    elseif type(o) == "table" then
       Savefiledata(tname.."{\n")
       for k,v in pairs(o) do
		   Savefiledata(" ")
		   Savefiledata(k)
           Savefiledata(" = ")
           serialize(v,tname)
           Savefiledata(",\n")
       end
       Savefiledata("}\n")
    else
       --error("cannot serialize a " .. type(o))
    end
end

local function do_file_Merge(dpathname, spathname, fname)
	if (filelib.fexist(dpathname) and filelib.fexist(spathname)) then
		print("do_file_Merge-Merge",dpathname,spathname);
		gfiledata = nil;
		local dt;
		rawset(tiros.config, fname, function(b) dt = b end)
		dofile(dpathname);
		local st;
		rawset(tiros.config, fname, function(b) st = b end)
		dofile(spathname);
		if type(dt) == "table" and type(st) == "table" then
			local sv = nil
			for k,v in pairs(dt) do
				sv = rawget(st, k)
				if sv ~= nil then
					rawset(dt, k, sv)
				end
			end
			--Removefile(spathname);
			Removefile(dpathname);
			serialize(dt, fname);
			Writefile(dpathname, gfiledata);
		end
		rawset(tiros.config, fname, nil)
	else
		if not filelib.fexist(dpathname) then
			print("do_file_Merge-Copy",dpathname,spathname);
			tiros.file.fileCopy(spathname, dpathname, false);
		end
	end
end

--接口table
local interface = {}

--对外声明取消已经启动timer句柄函数接口
--Ttype：string型参数，用于唯一标识该timer句柄
--输出：无
createmodule(interface,"file_Merge",function (dfilename,sfilename,fname)
	do_file_Merge(dfilename, sfilename,fname);
end)

tiros.filemerge = readOnly(interface)



