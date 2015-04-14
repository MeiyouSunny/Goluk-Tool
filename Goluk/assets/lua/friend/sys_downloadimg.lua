require"lua/systemapi/sys_handle"
require"lua/http"

local gt = {};
local gDownloadImageList = {}
local gCopyPath = "fs0:/friendhead/copy"

--[[
--@描述:返回一个字符串再另一个字符串中最后的位置
--@param  word string型参数 想要查找的字符串
--@return word在all中的位置
--]]
local function getLastWord(all, word)
	local b = 0;
	local last = nil;
	while true do
		local s,e = string.find(all, word, b); -- find 'next' word
		if s == nil then
			break;
		else
			last = s;
		end
			b = s + string.len(word);
	end
	
	return last;
end

--[[
--@描述:新建文件夹
--@param  fname string型参数 想要创建的文件夹名字
--@return 成功返回true，失败返回false
--]]
local function fmkdir(fname)
	if(fname == nil)then
		return false;
	end

	local bmkdir = filelib.fmkdir(fname);
	if(bmkdir)then
		return true;
	else
		return false;
	end
end

--[[
--@描述:创建文件句柄
--@param fname string型参数 文件名
--@return 成功返回文件句柄，失败返回nil
--]]
local function createFilebyName(fname)
	if (fname == nil) then
		return nil;
	end
	local bExist = filelib.fexist(fname);
	
	if (bExist) then
		filelib.fremove( fname );
	end
			
	local file = filelib.fopen(fname, 3);	
	

	return file;
end

--[[
--@描述:文件写入(支持二进制数据)
--@param file 文件句柄
--@param data 写入的数据
--@param size 数据大小
--@param bturncate bool型 false为直接追加写,true为清空内容重写
--@return 成功返回true,失败返回false
--]]
local function fileWrite(file, data, size, bturncate)

	if file == nil or data == nil then
		return false;
	end

	if bturncate == true then
		filelib.fchsize(file, 0);
	end

	filelib.fseek(file, 1, 0);
	
	local WriteSize = filelib.fwrite(file, data, size);

	if(size == WriteSize) then
		return true;
	else
		return false;
	end
end

--发送消息
local function sendmessage(stype, dwEvent, dwParam1, dwParam2, func, user)
	--print("sendmessage",stype, dwEvent, dwParam1, dwParam2)
	if func ==nil and user ==nil then		
	elseif user then
		--call c func
		--print("lksendmessage----c")
		commlib.universalnotifyFun(func,stype,user,dwEvent, dwParam1, dwParam2);
	elseif type(func) == "string" then
		--JS
		--print("lksendmessage----JS")
		local sCallJS;
		sCallJS = string.format("%s('%s', %u, %u, '%s');", func, stype,dwEvent, dwParam1, dwParam2);
		commlib.calljavascript(sCallJS);			
	else
		--lua
		--print("lksendmessage----LUA")
		func(stype,dwEvent, dwParam1, dwParam2);
	end
	
	--luaprint("sendmessage-end")
end
--[[
--@描述:图片下载的http回调函数
--@param  ptype 回调对象句柄
--@param  event 回调事件类型
--@param  param1 回调事件传递参数1
--@param  param2 回调事件传递参数2
--@return 无
--]]
createmodule(gt,"downLoadImageHttpEvent", function(htype,event,param1,param2)
	if htype ~= gDownloadImageList.htype then
		return
	end

	if event == 1 then
	elseif event == 2 then
		if param1 == 200 then
			if htype ~= nil then
				gDownloadImageList.file = createFilebyName( gCopyPath );
			end
		else
			--回调
			tiros.http.httpabort(htype);
			sendmessage(htype,event,0,0,gDownloadImageList.notify,gDownloadImageList.user)
		end	
	elseif event == 3 then
		if gDownloadImageList.file ~= nil then
			fileWrite(gDownloadImageList.file, param2, param1, false);			
		end
	elseif event == 4 then
		if gDownloadImageList.file ~= nil then							
			filelib.fclose(gDownloadImageList.file);
			gDownloadImageList.file = nil;
			tiros.file.fileCopy(gCopyPath,gDownloadImageList.path,true)
			filelib.fremove( gCopyPath );			
		end				
		--回调
		sendmessage(htype,event,1,0,gDownloadImageList.notify,gDownloadImageList.user)
	elseif event == 5 then
		if gDownloadImageList.file ~= nil then			
			filelib.fclose(gDownloadImageList.file);
			gDownloadImageList.file = nil;			
			filelib.fremove( gCopyPath );
		end
		sendmessage(htype,event,0,0,gDownloadImageList.notify,gDownloadImageList.user)	
	end
	--回调	
	
end)



--对外接口：下载图片
--url 头像服务器url
--notify(htype,dwEvent, dwParam1, dwParam2)
--path--fs0:/exam.jpg

createmodule(gt, "downloadimage", function(htype,url,path,notify,user)
	if url ~= nil and url ~= "" and path ~= nil and path ~= "" and htype ~= nil and htype ~= "" then	
		gDownloadImageList.htype = htype
		gDownloadImageList.path = path
		gDownloadImageList.notify = notify		
		gDownloadImageList.url = url				
		gDownloadImageList.bDownloaded = false
		gDownloadImageList.file = nil
		gDownloadImageList.user = user
                tiros.http.httpsendforlua("cdc_client",
					"webdownloadimg", 
					htype, 
					url, 
					gt.downLoadImageHttpEvent,
					nil);
		return true;
	end	
	
	return false;
end)

tiros.downloadimg = readOnly(gt);
