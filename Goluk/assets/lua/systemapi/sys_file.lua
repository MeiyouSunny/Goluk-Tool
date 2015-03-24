--file接口封装
--目前主要对外接口有fileCopy，Readfile，Writefile，Removefile

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"

--file  ErrCode
local FileErr_false = 0    --失败
local FileErr_true = 1        --成功
local FileErr_SrcFileNotExist = 2	--源文件不存在
local FileErr_SrcOpenFalse = 3     	--源文件打开失败

local FileErr_DstFileNotExist = 4	--目标文件创建失败
local FileErr_DstWriteFalse = 5		--目标文件写失败
local FileErr_FileCoverFalse = 6	--目标文件存在，文件覆盖失败
local FileErr_SrcFileAndDstFileDirSame = 7		--目标文件和源文件路径相同,文件名也一样，拷贝失败


--返回一个字符串再另一个字符串中最后的位置
--all:string型参数 源字符串
--word:string型参数 想要查找的字符串
--输出：word在all中的位置
local function get_last_word(all,word)
    local b = 0
    local last = nil
    while true do
        local s,e = string.find(all, word, b) -- find 'next' word
        if s == nil then
         break
        else
         last = s
        end
         b = s + string.len(word)
    end
    
    return last
end


--fmkdir functiron
--fname:string型参数 
local function fmkdir(fname)
	if(fname == nil)then
	return false
	end

	local bmkdir = filelib.fmkdir(fname)
	if(bmkdir)then
		return true
	else
		return false
	end
end



--file Copy functiron
--Srcf:string型参数
--Dstf:string型参数
--bCover:number型参数
--输出：ErrCode
local function file_Copy(Srcf,Dstf,bCover)
	if(Srcf == nil) or (Dstf == nil)then
		return FileErr_false;
	end

	--源文件不存在，返回SrcFileNotExist错误
	local bExist = filelib.fexist(Srcf);
	if(bExist == false)then
		return FileErr_SrcFileNotExist;	
	end

	--目标文件存在，且不覆盖拷贝，返回FileCoverFalse错误
	bExist = filelib.fexist(Dstf);

	if (bExist) and (bCover == false) then

		return FileErr_FileCoverFalse;
	end

	--判断源路径与目标路径是否完全相同，由于默认路径为fs0://开头，所以需要加以过滤
	local fsStart = string.find(Srcf,"fs0:/");
	if(fsStart == nil)then
		fsStart = 0;
	end
	local fsEnd = string.len(Srcf);
	local Str1 = string.sub(Srcf,fsStart,fsEnd);
	fsStart = string.find(Dstf,"fs0:/");
	if(fsStart == nil)then
		fsStart = 0;
	end
	fsEnd = string.len(Dstf);
	local Str2 = string.sub(Dstf,fsStart,fsEnd);
	if Str1 == Str2 then
		return FileErr_SrcFileAndDstFileDirSame;
	end
	
	--读取源文件数据
	local file = filelib.fopen(Srcf,1);
	if file == nil then				
	   return FileErr_SrcOpenFalse;
	end
	local s = filelib.fread(file,filelib.fgetsize(Srcf));
	filelib.fclose(file);
	--删除目标文件
	filelib.fremove(Dstf);
	--保证目标文件路径存在
	fsEnd = get_last_word(Dstf,"/");
	if(fsEnd ~= nil)then
		Str1 = string.sub(Dstf,0,fsEnd);
		fmkdir(Str1);
	end
	--创建目标文件
	file = filelib.fopen(Dstf,3);
	if file == nil then
		return FileErr_DstFileNotExist;
	end
	--如果源文件内容存在，且不为空，则写入目标文件
	if(#s ~= 0) then
		local len = #s;
		local len1 = filelib.fwrite(file,s, len);
		if len1 < len then --写文件内容长度不足，返回写失败
			filelib.fclose(file);
			return FileErr_DstWriteFalse;
		else
			filelib.fclose(file);
			return FileErr_true;
		end
	end
	filelib.fclose(file);
	return FileErr_true;
end



--Read file  functiron
--fname:string型参数
--输出：ErrCode
local function Read_file(fname)
	if fname==nil then
		return FileErr_SrcFileNotExist,"";
	end
	local bExist = filelib.fexist(fname);
	local retStr ="";
	if (bExist) then
		local f = filelib.fopen(fname,0);
		if (f ~= nil) then				
			local len = filelib.fgetsize(fname);
			if len>0 then
				retStr = filelib.fread(f,len);
			end
			filelib.fclose(f);
		else
			return FileErr_SrcOpenFalse,""
		end
	else
		return FileErr_SrcFileNotExist,"";
	end
	return FileErr_true, retStr
end

--Write file  functiron
--fname:string型参数
--data:string型参数
--bturncate：bool型  false为直接追加写，true为清空内容重写 
--输出：ErrCode
local function Write_file(fname,data,bturncate)
	if fname == nil or data == nil then
		return FileErr_false
	end
	local bExist = filelib.fexist(fname);
	local f
	if (bExist) then
		f = filelib.fopen(fname,1);
	else
		--截取目录名
		local End = get_last_word(fname,"/");
		--print(End)
		if(End ~= nil)then
			local Str = string.sub(fname,0,End);
			local bfmkdir = fmkdir(Str);
		end	
		f = filelib.fopen(fname,3);	
	end

	if f == nil then				
	   	return FileErr_false;
	end
	
	if bturncate == true then
		filelib.fchsize(f, 0);
	end

	filelib.fseek(f,1,0);
	
	local len = #data;
	local WriteSize = filelib.fwrite(f,data,#data);
	if(len == WriteSize)then
		filelib.fclose(f);
		return FileErr_true;
	else
		filelib.fclose(f);
		return FileErr_false;
	end
end




--Remove file  functiron
--fname:string型参数
--输出：ErrCode
local function Remove_file(fname)	
	local bfile = filelib.fremove(fname);
	if(bfile)then
		return FileErr_true;
	else
		
		return FileErr_false;
	end
end

----------------------------------------------------------------------
--接口table
local interface = {}

--对外声明拷贝文件函数接口
--Srcf:string型参数
--Dstf:string型参数
--bCover:number型参数
--输出：ErrCode
createmodule(interface,"fileCopy", function (Srcf,Dstf,bCover)
	local rev = file_Copy(Srcf,Dstf,bCover)
	return rev 
end)

--对外声明读文件函数接口
--fname:string型参数
--输出：第一个返回值是读出的数据 -string型，第二个是errcode
createmodule(interface,"Readfile", function (fname)
	local errcode, data
	errcode, data = Read_file(fname)
	return data, errcode
end)

--对外声明写文件函数接口
--fname:string型参数
--data：string型参数
--bturncate：bool型  false为直接追加写，true为清空内容重写 
--输出：ErrCode
createmodule(interface,"Writefile", function (fname,data,bturncate)
	local rev = Write_file(fname,data,bturncate)
	return rev
end)

--对外声明删除文件函数接口
--fname:string型参数
--输出：ErrCode
createmodule(interface,"Removefile", function (fname)
	local rev = Remove_file(fname)
	return rev
end)
-----------------------------------------------------------------------
tiros.file  =  readOnly(interface)
