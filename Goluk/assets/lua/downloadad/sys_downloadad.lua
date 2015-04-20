--[[
-- @描述:下载广告,目前包含开屏广告+搜索框上面的广告
-- @编写人:宣东言
-- @创建日期: 2012-11-20 14:38:11
--]]
require"lua/json"
require"lua/http"
require"lua/tapi"
require"lua/location"
require"lua/moduledata"
require"lua/framework"
require"lua/systemapi/sys_namespace"
require"lua/commfunc"

--下载广告器返回数据拼接池
local gsHttpData;
--网络错误或者超时重试时间
local gTimeOut = 3000;
--http连接服务器重试次数
local gnHttpRetryTimes = 10;
--广告图片下载路径
local gsDownLoadDir = "fs0:/addata/";
--广告位图片后缀名
local gsImageName = ".jpg"
--广告历史记录保存文件名
local gsFileHistory = "history"
--存放广告图片的URL的全局表
local gtURL = {};
--写图片用的全局文件句柄
local gFile = nil;
--广告服务标识(服务器端开发人员指定)
local gsActionLocation = "/navidog2Advert/smartadall/smartadall_getAdAll.htm";
--下载广告的URL
--local gsURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",2101);
--local gsURL = "http://192.168.1.111:8081/general_Get"

-- 1. 智能广告
-- 2. 搜索框广告
-- 3. 开屏广告

--[[
--@描述:根据广告类型删除广告历史记录
--@param  nAdType 广告类型
--@return 无
--]]
local function removeAdHistoryByType(nAdType)
	local sFileName = gsDownLoadDir..nAdType.."/"..gsFileHistory;
	local bExist = filelib.fexist(sFileName);
	if (bExist) then
		tiros.file.Removefile(sFileName);
	end
end

--[[
--@描述:根据广告类型获取广告历史记录
--@param  nAdType 广告类型
--@return 单个历史记录json字符串
--]]
local function getAdHistoryByType(nAdType)
	local sHistory = nil;
	local sFileName = gsDownLoadDir..nAdType.."/"..gsFileHistory;
	
	local bExist = filelib.fexist(sFileName);
	if (bExist) then
		sHistory = tiros.file.Readfile(sFileName);
	else
		sHistory = nil;
	end

	return sHistory;
end


--[[
--@描述:获得开屏广告的统计信息
--@param  无
--@return 无
--]]
local function getLogoAdHistory()
	local sHistory = getAdHistoryByType(3);

	if sHistory ~= nil then
		--URL编码
		local sEncode = tiros.commfunc.EnCodeUrl(sHistory);
		sHistory = sEncode;
	else
		sHistory = nil;	
	end

	if sHistory == nil then
	   sHistory = "";
	end

	print("downloadad--LogoAdHistory="..sHistory);
	return sHistory;
end


--[[
--@描述:获得智能主页广告的统计信息
--@param  无
--@return 无
--]]
local function getSearchAdHistory()
	local sHistory = getAdHistoryByType(2);

	if sHistory ~= nil then
		--URL编码
		local sEncode = tiros.commfunc.EnCodeUrl(sHistory);
		sHistory = sEncode;
	else
	   sHistory = "";
	end

	print("downloadad--getSearchAdHistory="..sHistory);
	return sHistory;
end
--[[
--@描述:获得智能主页广告的统计信息
--@param  无
--@return 无
--]]
local function getSmartAdHistory()
	local sHistory = getAdHistoryByType(1);

	if sHistory ~= nil then
		--URL编码
		local sEncode = tiros.commfunc.EnCodeUrl(sHistory);
		sHistory = sEncode;
	else
	   sHistory = "";
	end

	print("downloadad--getSmartAdHistory="..sHistory);
	return sHistory;
end
--[[
--@描述:获取广告历史记录
--@param  无
--@return 历史记录json字符串数组
--note: 原有的history仅仅保留标签，内容为空。
--]]
local function getAdHistory()
--[[
	local sHistory = nil;
	local sHistory2 = getAdHistoryByType(2);

	if sHistory2 ~= nil then
		sHistory = "["..sHistory2.."]";
		--URL编码
		local sEncode = tiros.commfunc.EnCodeUrl(sHistory);
		sHistory = sEncode;
	else
		sHistory = nil;	
	end

	if sHistory == nil then
	   sHistory = "";
	end

	print("downloadad--sHistory="..sHistory);
]]
    local sHistory = "";
	return sHistory;
end

--[[
--@描述:从数据仓库获取犬号(uid)
--@param  无
--@return 犬号
--]]
local function getUserID()
	local sUserID = tiros.moduledata.moduledata_get("framework", "uid");
	if sUserID == nil then
	   sUserID = "";
	end
	print("downloadad--getUserID="..sUserID);
	return sUserID;
end

--[[
--@描述:从数据仓库获取手机型号,目前采用manufacturername+devicemodel的格式
--@param  无
--@return 手机型号
--]]
local function getPhoneType()
	local sManufacturerName = tiros.moduledata.moduledata_get("framework", "manufacturername");
	local sDeviceModel = tiros.moduledata.moduledata_get("framework", "devicemodel");
	if sDeviceModel == nil then
	  sDeviceModel = "";
	end
	if sManufacturerName == nil then
	  sManufacturerName = "";
	end
	print("downloadad--getPhoneType="..sManufacturerName..sDeviceModel);
	return sManufacturerName..sDeviceModel;
end

--[[
--@描述:从数据仓库获取设备OSVersion
--@param  无
--@return 设备OSVersion
--]]
local function getOSVersion()
	local sOSVersion = tiros.moduledata.moduledata_get("framework", "osversion");
	if sOSVersion == nil then
	  sOSVersion = "";
	end
	return sOSVersion;
end

--[[
--@描述:从数据仓库获取设备屏幕密度
--@param  无
--@return 屏幕密度
--]]
local function getDPI()
	local sDPI = tiros.moduledata.moduledata_get("framework", "dpi");
	if sDPI == nil then
	   sDPI = "";
	end
	print("downloadad--getDPI="..sDPI);
	return sDPI;
end

--[[
--@描述:根据广告类型获取智能广告id
--@param  nAdType 广告类型
--@return 单个历史记录json字符串
--]]
local function getSmartidByType(nAdType)
	local sSmartid = nil;
	local sData = nil;
	local sFileName = gsDownLoadDir.."1/"..nAdType.."/"..nAdType;
	
	local bExist = filelib.fexist(sFileName);
	if (bExist) then
		sData = tiros.file.Readfile(sFileName);
		local tJsonData = tiros.json.decode(sData);
		sSmartid = tJsonData["id"];
	else
		sSmartid = 0;
	end

	return sSmartid;
end

--[[
--@描述:从文件获取智能广告id
--@param  无
--@return 智能广告id
--]]
local function getSmartid()
	local tSmart = {};
	tSmart.a = getSmartidByType("a");
	tSmart.b = getSmartidByType("b");
	tSmart.c = getSmartidByType("c");
	tSmart.d = getSmartidByType("d");
	tSmart.e = getSmartidByType("e");

	local sSmartid = tiros.json.encode(tSmart);

	print("downloadad--sSmartid1="..sSmartid);
	local sEncode = tiros.commfunc.EnCodeUrl(sSmartid);

	sSmartid = sEncode;

	return sSmartid;
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
	local file;
	if (bExist) then
		filelib.fremove(fname);
		file = filelib.fopen(fname, 3);
		--file = filelib.fopen(fname, 1);
	else
		--截取目录名
		local End = getLastWord(fname, "/");
		--print(End)
		if(End ~= nil)then
			local Str = string.sub(fname, 0, End);
			local bfmkdir = fmkdir(Str);
		end	
		file = filelib.fopen(fname, 3);	
	end

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

--[[
--@描述:构成下载广告服务完整的URL
--@param  无
--@return 下载广告服务的URL
--]]
local function getURL()
	local sUserID = getUserID();
	local sPhoneType = getPhoneType();
	local sOSVersion = getOSVersion();
	local sDPI = getDPI();
	local sHistory = getAdHistory();
	local sSmartid = getSmartid();
	local sBasicURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",2101);
	--local sBasicURL = "http://192.168.1.111:8081/general_Get"
	local sLogoHistory = getLogoAdHistory();
    local smartlog = getSmartAdHistory();
    local searchlog = getSearchAdHistory();
	if sBasicURL == nil then
		return nil;
	end
	local sURL = sBasicURL.."?"
		   .."userid="..sUserID.."&"
		   .."phonetype="..sPhoneType.."&"
		   .."ostype="..sOSVersion.."&"
		   .."history="..sHistory.."&"
		   .."smartid="..sSmartid.."&"
		   .."resolution="..sDPI.."&"
		   .."smartlog="..smartlog.."&"
		   .."searchlog="..searchlog.."&"
		   .."countlog="..sLogoHistory;

	print("downloadad--getURL = ",sURL);
	return sURL;
end

--为了做网络错误或者超时重试而做的函数声明
local downLoadImageHttpnotifyFun = nil;
local gnImageTimes = 0;

--[[
--@描述:下载广告图片重试函数
--@param  无
--@return 无
--]]
local function downLoadImageCB()
	gnImageTimes = gnImageTimes + 1;
	if gnImageTimes == gnHttpRetryTimes then
		tiros.timer.timerabort("downLoadImage");
		gnImageTimes = 0;
		return;
	end

	local sURL = nil;
	local sType = nil;
	for key,value in pairs(gtURL) do
		local tUrl = tiros.json.decode(value);
		sType = nil;
		sURL = nil;
		local tTemp = {};
		tTemp.dir = tUrl.dir;
		tTemp.type = tUrl.type;
		tTemp.key = key;
		local sJsonTemp = tiros.json.encode(tTemp);

		sType = sJsonTemp;
		sURL = tUrl.url;
	end

	if(sType ~= nil and sURL ~= nil) then
                tiros.http.httpsendforlua("cdc_client", "smartadallgetAdAll", sType, sURL, downLoadImageHttpnotifyFun, nil);
	end
end

--[[
--@描述:图片下载的http回调函数
--@param  ptype 回调对象句柄
--@param  event 回调事件类型
--@param  param1 回调事件传递参数1
--@param  param2 回调事件传递参数2
--@return 无
--]]
local function downLoadImageHttpnotify(ptype, event, param1, param2)
	--print("downloadad--downLoadImageHttpnotify --",ptype,event,param1,param2);
	local tType = tiros.json.decode(ptype);
	if event == 1 then
	elseif event == 2 then
		if param1 ~= 200 then--http状态出错
			tiros.http.httpabort(ptype);
		end
		if ptype ~= nil then
			local sFileName = gsDownLoadDir..tType.dir.."/"..tType.type..gsImageName;
			print("downloadad--sFileName=",sFileName);
			gFile = createFilebyName(sFileName);
		end

	elseif event == 3 then
		if ptype ~= nil then
			fileWrite(gFile, param2, param1, false);
		end

	elseif event == 4 then
		if ptype ~= nil then
			if gFile ~= nil then
				filelib.fclose(gFile);
				gFile = nil;
			end
			tiros.file.Writefile(gsDownLoadDir..tType.dir.."/".."ok" ,"ok", true);
		end
		--删除已经下载的图片的URL
		if ptype ~= nil then
			local tType = tiros.json.decode(ptype);
		
			gtURL[tType.key] = nil;
			tiros.http.httpabort(ptype);
		end

		--下载下一张广告图片
		downLoadImageCB();
	elseif event == 5 then
		if gFile ~= nil then
			filelib.fclose(gFile);
			gFile = nil;
		end
		tiros.http.httpabort(ptype);
		--网络错误或者超时重试机制
		if(param1 == 1 or param1 == 2) then
			print("downloadad--downLoadImageHttpnotify-err1or2=", param1, param2);
			tiros.timer.timerabort("downLoadImage");
			tiros.timer.timerstartforlua("downLoadImage", gTimeOut, downLoadImageCB, false);
		elseif (param1 == 3) then
			print("downloadad--downLoadImageHttpnotify-err3=", param1, param2);
			downLoadImageCB();
		end
	end
end

--为了做网络错误或者超时重试而备份函数
downLoadImageHttpnotifyFun = downLoadImageHttpnotify;

--[[
--@描述:下载广告图片
--@param  sID 广告类型
--@param  sURL广告图片下载URL
--@return 无
--]]
local function downLoadImage()
	local sURL = nil;
	local sType = nil;
	for key,value in pairs(gtURL) do
		local tUrl = tiros.json.decode(value);
		sType = nil;
		sURL = nil;
		local tTemp = {};
		tTemp.dir = tUrl.dir;
		tTemp.type = tUrl.type;
		tTemp.key = key;
		local sJsonTemp = tiros.json.encode(tTemp);

		sType = sJsonTemp;
		sURL = tUrl.url;
	end

	if(sType ~= nil and sURL ~= nil) then
                tiros.http.httpsendforlua("cdc_client", "downloadad", sType, sURL, downLoadImageHttpnotify, nil);
	end
end

--[[
--@描述:判断如果下行数据里没有开平广告数据则删除本地开平广告数据
--@显示完不删除，请求服务器，下行里，如果跟本地的一样，则不删除，如果不一样或者没有广告 则删除
--@param  tData 下行数据的table表
--@return 本地开平广告的ID
--]]
local function removeLocalAd(tData)
	local bRemove = true;
	local nAdID = nil;
	local nLocalAdID = nil;
	for key,value in pairs(tData) do
		local sJsonData = tiros.json.encode(value);
		if (sJsonData ~= nil and value.advertising ~= nil) then
			--如果有开平广告
			if(value.advertising == 3) then
				nAdID = value.id;
				--bRemove = false;
			end
		end
	end
	
	local sFileName = gsDownLoadDir.."3".."/".."3";

	local bExist = filelib.fexist(sFileName);
	if (bExist) then
		local sLocalData = tiros.file.Readfile(sFileName);
		if sLocalData ~= nil then
			local tLocalData = tiros.json.decode(sLocalData);
			print("sLocalData="..sLocalData);
			nLocalAdID = tLocalData["id"];
			if nLocalAdID == nAdID then
				bRemove = false;
			end
		end
	end

	if bRemove == true then
		print("downloadad--remove-3 data")
		tiros.file.Removefile(gsDownLoadDir.."3".."/".."3");
		tiros.file.Removefile(gsDownLoadDir.."3".."/".."3"..gsImageName);
		tiros.file.Removefile(gsDownLoadDir.."3".."/".."ok");
	end

	return nLocalAdID;
end

--[[
--@描述:解析下载广告下行数据
--@param sData:服务器返回的完整数据
--@return 无
--]]
local function parseData(sData)
--sData = '{"smartdata":[{"id":34,"advertising":1,"isclick":1,"clickurl":"2333","priority":2323,"adtype":5,"parameter":0,"dispaly":null,"imageurl":"http://192.168.1.234/files/ad/20130110/1357788079500.jpg","opentype":1,"browser":1,"key":"ticketdetail","smartid":"a","flog":3},{"id":35,"advertising":1,"isclick":1,"clickurl":"2333","priority":2323,"adtype":5,"parameter":0,"dispaly":null,"imageurl":"http://192.168.1.234/files/ad/20130110/1357788079500.jpg","opentype":1,"browser":1,"key":"ticketdetail","smartid":"b","flog":1},{"id":36,"advertising":1,"isclick":1,"clickurl":"2333","priority":2323,"adtype":5,"parameter":0,"dispaly":null,"imageurl":"http://192.168.1.234/files/ad/20130110/1357788079500.jpg","opentype":1,"browser":1,"key":"ticketdetail","smartid":"c","flog":1},{"id":37,"advertising":1,"isclick":1,"clickurl":"2333","priority":2323,"adtype":5,"parameter":0,"dispaly":null,"imageurl":"http://192.168.1.234/files/ad/20130110/1357788079500.jpg","opentype":1,"browser":1,"key":"ticketdetail","smartid":"d","flog":1}],"data":[{"id":100,"advertising":2,"imageurl":"http://192.168.1.234/files/ad/20130110/1357788079500.jpg","isclick":1,"clickurl":"http://www.baidu.com"},{"id":200,"advertising":3,"imageurl":"http://192.168.1.234/files/ad/20130110/1357788079500.jpg","isclick":1,"clickurl":"http://www.sina.com"}],"success":true}'
	print("downloadad--http-alldata---------=",sData);
	local tHttpData = tiros.json.decode(sData);
	local bSuccess = tHttpData["success"];
	if bSuccess == true then
		--删除搜索框广告的历史记录
        removeAdHistoryByType(1);
		removeAdHistoryByType(2);
        removeAdHistoryByType(3);
		local tData = tHttpData["data"];
		if tData ~= nil then
			--判断是否需要删除本地的开屏广告，如果需要则删除
			local nLocalAdID = removeLocalAd(tData);
			--把广告数据写入文件
			for key,value in pairs(tData) do
				local sJsonData = tiros.json.encode(value);
				if (sJsonData ~= nil and value.advertising ~= nil) then
					local bExist = filelib.fexist(gsDownLoadDir.."3".."/".."ok");
					if value.advertising == 3 and value.id == nLocalAdID and bExist == true then
						--判断开平广告本地id和下发是否一致 如果一致而且本地有完整的开屏广告则不下载
					else
						local nType = value.advertising;
						tiros.file.Writefile(gsDownLoadDir..nType.."/"..nType ,sJsonData, true);
						--记录下载图片的URL
						local tUrl = {};
						tUrl.dir = nType;
						tUrl.type = value.advertising;
						tUrl.url = value.imageurl;
						local jsonUrl = tiros.json.encode(tUrl);
						table.insert(gtURL, jsonUrl);
					end
				end
			end
		end

		local tSmartData = tHttpData["smartdata"];
		if tSmartData ~= nil then
			--把广告数据写入文件
			for key,value in pairs(tSmartData) do
				local sJsonData = tiros.json.encode(value);
				if (sJsonData ~= nil and value.smartid ~= nil) then
					--1下载2删除3已存在
					print("downloadad-smartid-flog=",value.smartid,value.flog)
					if value.flog == 1 then
						tiros.file.Writefile(gsDownLoadDir.."1/"..value.smartid.."/"..value.smartid ,sJsonData, true);
						--记录下载图片的URL
						local tUrl = {};
						tUrl.dir = "1/"..value.smartid;
						tUrl.type = value.smartid;
						tUrl.url = value.imageurl;
						local jsonUrl = tiros.json.encode(tUrl);
						table.insert(gtURL, jsonUrl);
					elseif value.flog == 2 then
						tiros.file.Removefile(gsDownLoadDir.."1/"..value.smartid.."/"..value.smartid);
						tiros.file.Removefile(gsDownLoadDir.."1/"..value.smartid.."/"..value.smartid..gsImageName);
						tiros.file.Removefile(gsDownLoadDir.."1/"..value.smartid.."/".."/ok");
					elseif value.flog == 3 then
						local bExist = filelib.fexist(gsDownLoadDir.."1/"..value.smartid.."/".."/ok");
						if bExist == false then
							tiros.file.Removefile(gsDownLoadDir.."1/"..value.smartid.."/"..value.smartid);
							tiros.file.Removefile(gsDownLoadDir.."1/"..value.smartid.."/"..value.smartid..gsImageName);
							tiros.file.Removefile(gsDownLoadDir.."1/"..value.smartid.."/".."/ok");
							tiros.file.Writefile(gsDownLoadDir.."1/"..value.smartid.."/"..value.smartid ,sJsonData, true);
							--记录下载图片的URL
							local tUrl = {};
							tUrl.dir = "1/"..value.smartid;
							tUrl.type = value.smartid;
							tUrl.url = value.imageurl;
							local jsonUrl = tiros.json.encode(tUrl);
							table.insert(gtURL, jsonUrl);
						end
					else
					end
				end
			end
		end
		--开始下载广告图片
		downLoadImage();
	elseif bSuccess == false then
		print("downloadad-downloadad-success == false");
	end
	sData = nil;
end



--接口table
local interface = {};

--下载广告超时重试次数记录
local gnTimes = 0;

--[[
--@描述:下载广告请求重试函数
--@param  pType integer型参数,标记时间回调句柄
--@return 无
--]]
local function downloadadCB(pType)
	gnTimes = gnTimes + 1;
	if gnTimes == gnHttpRetryTimes then
		tiros.timer.timerabort(pType);
		gnTimes = 0;
		return;
	end
	interface.downloadad();
end

--[[
--@描述:下载广告的http回调函数http://www.baidu.com/
--@param  ptype 回调对象句柄
--@param  event 回调事件类型
--@param  param1 回调事件传递参数1
--@param  param2 回调事件传递参数2
--@return 无
--]]
local function httpNotify(ptype, event, param1, param2)
	print("downloadad--httpnotify --",ptype,event,param1,param2);
	if event == 1 then
		gsHttpData = nil;
	
	elseif event == 2 then
		if param1 ~= 200 then--http状态出错
			tiros.http.httpabort(ptype);
		end	

	elseif event == 3 then
		if gsHttpData ~= nil then
			gsHttpData = gsHttpData..string.sub(param2, 1, param1);
		else
			gsHttpData = string.sub(param2, 1, param1);
		end

	elseif event == 4 then
		parseData(gsHttpData);
		gsHttpData = nil;
		tiros.http.httpabort(ptype);

	elseif event == 5 then
		if(param1 == 1 or param1 == 2) then
			print("downloadad--httpnotify-err1or2=",param1, param2);
			tiros.timer.timerabort(ptype);
			tiros.timer.timerstartforlua(ptype, gTimeOut, downloadadCB, false);
		elseif (param1 == 3) then
			print("downloadad--httpnotify-err3=",param1, param2);
			downloadadCB(ptype);
		end

		tiros.http.httpabort(ptype);
	end
end

--[[
--@描述:对外声明调用下载广告请求函数接口
--@param  notify integer型参数，注册回调函数地址
--@param  pUser  integer型参数，注册的调用者参数地址
--@param  sToken string型参数，要上传的手机令牌字符串
--@return 请求成功返回true，失败返回false
--]]
createmodule(interface,"downloadad",function ()
	local sURL = getURL();
	if sURL == nil then
	   return nil;
	end

        return tiros.http.httpsendforlua("cdc_client", "downloadad","downloadad", sURL, httpNotify, nil,
		   "actionlocation:"..gsActionLocation);		
end)


--[[
--@描述:通知平台启动广告监控接口
--@param  
--@param  jsonStr string型参数，广告文件的内容
--@return 无返回值
--]]
createmodule(interface,"startAdvertisementMonitor",function (jsonStr)
	print("startAdvertisementMonitor data is " .. jsonStr);
    local nFunction = tiros.moduledata.moduledata_get("framework", "pfunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "puser");
    if nFunction == nil or nUser == nil then
        print("startAdvertisementMonitor can not found callback function");
        return;
    else
        return commlib.initNotifyFun(nFunction, nUser, 173, 0, jsonStr);
    end
end)

tiros.downloadad = readOnly(interface);

--file end

