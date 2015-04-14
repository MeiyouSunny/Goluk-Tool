--[[
 @描述：文件上传
 @编写人：王成
 @创建日期：2013-2-26
 @版本：0.1.0
--]]
require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/framework"
require"lua/http"
require"lua/json"
require"lua/database"
require"lua/moduledata"
require"lua/net-httpheaders"

local RES_STR_UPLOAD_GET_URL = 2101; --资源文件中编号
local RES_FILE_PATH = "fs0:/res/api/api.rs"; --资源文件地址路径
local STR_HEADER_ACTIONLOCATION_KEY = "actionlocation"
local STR_HEADER_ACTIONLOCATION_VALUE = "/navidogUpFile/imageUpload.htm"

local gt = {};
local gPoststep = 300000 --每次最大上传字节数
local gContentType = "application/octest-stream"
--local gUrl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_UPLOAD_GET_URL);
local gCurrent = 0   --当前上传位置


createmodule(gt, "uploadfilelist",{})
createmodule(gt, "uploadfileweaklist",{})
setmetatable(gt.uploadfileweaklist,{__mode ="v" })

local function createhandle(htype, ftype, path,func, user)
	if htype == nil or path == nil or func == nil then
		return false
	end
	local ht = getHandle(gt.uploadfileweaklist,"uploadfilehandle");
	if ht == nil then
		ht = {};
		local bExist = filelib.fexist(path);
		if not bExist then 
			return false
		end
		ht.file = filelib.fopen(path, 1);
		ht.fsize= filelib.fgetsize(path)
		ht.sendedsize = 0		
		ht.http = httpuploadlib.create()
		ht.serverpath = ""
		ht.fname = gt.getfilename(path)						   	
	end
	if ht.http ~= nil then
		httpuploadlib.registernotify(ht.http, "UploadFileHttpEvent", "uploadfilehandle")
	else
		return false
	end
	ht.ftype = ftype	
	ht.path = path
	ht.htype = htype
	ht.rspdata = nil
	ht.func = func
	ht.user = user
	registerHandle(gt.uploadfilelist, gt.uploadfileweaklist, "uploadfilehandle", ht);
	return true
end

local function releasehandle()
	local ht =getHandle(gt.uploadfilelist,"uploadfilehandle")
	if ht ~= nil then
		httpuploadlib.destroy(ht.http)
	   	releaseHandle(gt.uploadfilelist,"uploadfilehandle")		
	end
end

--添加headers
local function AddHeaders()
	local t = getHandle(gt.uploadfilelist,"uploadfilehandle")
	if t ~= nil then
		local headersCount = tiros.nethttpheaders.httpheaderscount();
		for i = 1, headersCount do
			local gValues,gHeaders = tiros.nethttpheaders.httpgetheader(i);
			httpuploadlib.addheader(t.http, gHeaders, tostring(gValues));
		end		
	end		
end

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
createmodule(gt, "getfilename", function(path)
	if path == nil then
		return ""
	end
	local pos = getLastWord(path, '/')
	local strname = string.sub(path, pos+1, string.len(path))
 	return strname
end)

--发送消息
local function sendmessage(stype, dwEvent, dwParam1, dwParam2, func, user)
	print("sendmessage",stype, dwEvent, dwParam1, dwParam2)
	
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
end

local function postfiledata(htype, path)
	local t = getHandle(gt.uploadfilelist,"uploadfilehandle")
	if t ~= nil then
		if t.sendedsize + 1 < t.fsize then
			local lsize = t.fsize - t.sendedsize --剩余文件大小
			local csize = lsize        --当前发送大小
			if gPoststep < lsize then
				csize = gPoststep
			end
			local strdata = filelib.fread(t.file, csize);
			local from = t.sendedsize
			local to = t.sendedsize + csize - 1
			gCurrent = to			
				
			local para = STR_HEADER_ACTIONLOCATION_VALUE .. "?key=server1&fileType="..t.ftype.."&flag=1&filePath="..t.serverpath.."&range="..from.."-"..to.."&fileName="..t.fname.."&fsize="..t.fsize
			
			httpuploadlib.addheader(t.http, STR_HEADER_ACTIONLOCATION_KEY, para);
			local sUrl = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_UPLOAD_GET_URL);
			httpuploadlib.data(t.http , sUrl, gContentType, strdata, #strdata);
		else
			sendmessage(htype, 1, 0, "", t.func, t.user);
		end
	end	
end

local function pushfile(htype, path)
	local t = getHandle(gt.uploadfilelist,"uploadfilehandle")
	if t ~= nil then
		httpuploadlib.cancel(t.http)
		AddHeaders()
		postfiledata(htype, path)
	end
end



DeclareGlobal("UploadFileHttpEvent", function(htype, event, param1, param2)
	local t = getHandle(gt.uploadfilelist,"uploadfilehandle")
	if t == nil then
		return
	end
	
	if event == 1 then --请求
	elseif event == 2 then --应答
		if param1 ~= 200  and t ~= nil then	
			httpuploadlib.cancel(t.http);
			sendmessage(t.htype, 0, 0, param2, t.func, t.user);
		end	
	elseif event == 3 then --数据体SUCCESS
		if t.rspdata == nil then
			t.rspdata = string.sub(param2,1,param1);
		else
			t.rspdata = t.rspdata..string.sub(param2,1,param1);
		end			
	elseif event == 4 then --完成	
	--{"totallen":19,"templen":2,"filePath":"/opt/teData/fm/temp/server1/20130228/8590A010817911E2B859A63448E850E3.tmp","curlen":2}
		local decodeT = tiros.json.decode(t.rspdata);
		if decodeT ~= nil then	
			if decodeT.templen ~= decodeT.totallen then
				t.serverpath = decodeT.filePath
				t.sendedsize = 	decodeT.templen		
				postfiledata(t.htype, t.path)
			elseif decodeT.templen == decodeT.totallen then
				sendmessage(t.htype, 1, 0, decodeT.msg, t.func, t.user);
			else
				sendmessage(t.htype, 2, 0, decodeT.msg, t.func, t.user);
			end
		end
		t.rspdata = nil;
	elseif event == 5 then --错误
		if t ~= nil then
			sendmessage(t.htype, 0, 0, param2, t.func, t.user);
		end
		t.rspdata = nil;
	end
	
end)

--ftype,2:文件  1:图片
createmodule(gt,"uploadfile", function(htype, ftype, path,func, user)	
	local bCreate = createhandle(htype, ftype, path,func, user)
	if bCreate then
		gCurrent = 0
		return pushfile(path)
	end
	return false
end)

tiros.uploadfile  = readOnly(gt)
