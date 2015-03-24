--[[
<pre>
 * 1.全局函数首字母大写
 * 2.私有函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写
 * 7.后续人员开发保证代码格式一致

 @ 创建时间:2013-07-19
 @ 功能描述：文件操作封装
 @ 提供接口：
（1）(CopyFile)对外公开复制文件；
（2）(RemoveFile)对外公开删除文件；
（3）(WriteFile)对外公开写文件；
（4）(ReadFile)对外公开读取文件；
</pre>

--]]

require "lua/systemapi/sys_namespace"
require "lua/systemapi/sys_handle"

local interface = {};
local codemsg = {"失败","成功","源文件不存在","源文件打开失败","目标文件创建失败","目标文件写失败","目标文件存在，文件拷贝失败","目标文件和源文件路径相同,文件名也一样，拷贝失败"};
--[[
local filemsg = {
	0="失败",
	1="成功",
	2="源文件不存在",
	3="源文件打开失败",
	4="目标文件创建失败",
	5="目标文件写失败",
	6="目标文件存在，文件拷贝失败",
	7="目标文件和源文件路径相同,文件名也一样，拷贝失败"
};
--]]
local fileobj = getmodule("file");

local function udpreport(code)
	local state = 1;
	if code ~=1 and code ~=2 then
		--tiros.base.udp.ErrorReport("",(code-0+20));
		state = 0;
	end
	return state;
end


--读取文件
local function read(path)
	local data,code = fileobj.Readfile(path);
	local state = udpreport(code);
	local msg = codemsg[code+1];
	return data,state,msg;
end


--写文件
local function write(path,data,cover)
	local code = fileobj.Writefile(path,data,cover);
	local state = udpreport(code);
	local msg = codemsg[code+1];
	return state,msg;
end

--删除文件
local function remove(path)
	local code = fileobj.Removefile(path);
	local state = udpreport(code);
	local msg = codemsg[code+1];
	return state,msg;
end

--复制文件
local function copy(spath,epath,cover)
	local code = fileobj.fileCopy(spath,epath,cover);
	local state = udpreport(code);
	local msg = codemsg[code+1];
	return state,msg;
end


--对外公开读取文件
--path 文件路径
--返回参数
--data 读取的数据
--state 返回状态 0失败 1成功
--msg 错误信息
createmodule(interface,"ReadFile",function(path)
	if path == nil then 
		return nil,0,"参数错误";
	end
	local data,state,msg = read(path);
	return data,state,msg;
end);

--对外公开写文件
--path 文件路径
--data 数据
--cover false为直接追加写，true为清空内容重写
--返回参数
--state 返回状态 0失败 1成功
--msg 错误信息
createmodule(interface,"WriteFile",function(path,data,cover)
	if path == nil or data == nil then 
		return nil,0,"参数错误";
	end
	local c = false;
	if cover == true then
		c = true;
	end
	local state,msg = write(path,data,c);
	return state,msg;
end);

--对外公开删除文件
--path 文件路径
--返回参数
--state 返回状态 0失败 1成功
--msg 错误信息
createmodule(interface,"RemoveFile",function(path)
	if path == nil then 
		return nil,0,"参数错误";
	end
	local state,msg = remove(path);
	return state,msg;
end);

--对外公开复制文件
--spath 文件路径
--epath 数据
--cover true覆盖，false不覆盖
--返回参数
--state 返回状态 0失败 1成功
--msg 错误信息
createmodule(interface,"CopyFile",function(spath,epath,cover)
	if spath == nil or epath == nil then 
		return nil,0,"参数错误";
	end
	local c = true;
	if cover == false then
		c = false;
	end
	local state,msg = copy(spath,epath,c);
	return state,msg;
end);

--设置只读权限
tiros.base.file = readOnly(interface);