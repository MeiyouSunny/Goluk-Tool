--[[
-- @描述:框架脚本
-- @	(1)把常用信息存入lua数据仓库,比如客户端版本号,mobileid,imsi,手机系统版本等
-- @	(2)从rs文件获取url,然后根据gitversion区分服务器指向拼接成完整的url
-- @编写人:宣东言
-- @创建日期: 2011-11-16 12:30:21
-- @修改内容:修改GetUrlFromResource接口,增加UDP的URL 修改人:宣东言 修改日期:2012-04-09 18:30:28
-- @修改内容:增加替换单引号,双引号,左右斜杠接口, 修改人:宣东言 修改日期:2012-10-29 16:43:28
--]]
require"lua/file"
require"lua/json"
require"lua/tapi"
require"lua/moduledata"
require"lua/systemapi/sys_namespace"

--开发服务器标识
local gsDev = "dev";
--测试服务器标识
local gsTest = "test";
--正式服务器标识
local gsPublic = "nvd";

--开发服务器http地址
local gsDevURL = "svr.xiaocheben.com";
--测试服务器http地址
local gsTestURL = "server.xiaocheben.com";
--正式服务器http地址
local gsPublicURL = "server.xiaocheben.com";


--开发服务器UDP地址
local gsDevUDPURL = "udp.xiaocheben.com:6001";
--测试服务器UDP地址
local gsTestUDPURL = "udp.xiaocheben.com:6001";
--正式服务器UDP地址
local gsPublicUDPURL = "udp.xiaocheben.com:6001";

--开发服务器airtalkee地址
local gsDevAirTalkeeURL = "192.168.1.228";
--测试服务器airtalkee地址
local gsTestAirTalkeeURL = "119.2.12.35";
--正式服务器airtalkee地址
local gsPublicAirTalkeeURL = "sns.lbs8.com";


--开发服务器Tcp地址
local gsTcpDevURL = "svr.xiaocheben.com";
--测试服务器Tcp地址
local gsTcpTestURL = "server.xiaocheben.com";
--正式服务器Tcp地址
local gsTcpPublicURL = "server.xiaocheben.com";

--开发服务器Tcp端口
local gsTcpDevPort = "40172";
--测试服务器Tcp端口
local gsTcpTestPort = "40172";
--正式服务器Tcp端口
local gsTcpPublicPort = "40172";


--version文件路径
local gsVersionFileName = "fs0:/version";
--user配置文件路径
local gsUserFileName = "fs4:/user"
--设定只读用的变量
local interFace = {};
--要替换的源字符串
local gtFind = {a="'", b="\"", c="\\", d="/",e=" "};
--要替换的源字符串
local gsReplece = "_"

--[[
--@描述:查找和替换字符串里面的字符
--@param  sSource:类型string,源字符串
--@param  tFind:类型table,要替换的源字符串
--@param  sReplace:类型string,要替换的目标字符串
--@return 替换完的字符串
--]]
local function replaceString(sSource, tFind, sReplace)
	local sRetrun = sSource;
	for key,value in pairs(tFind) do
		sRetrun = string.gsub(sRetrun, value, sReplace);
	end
	return sRetrun;
end

--[[
--@描述:从tapi接口获取mobileid并存入lua数据仓库
--@param  无
--@return 无
--]]
local function setMobileid()
	local sMobileid = tiros.tapi.tapigetmobileid();
	if sMobileid == nil then
		sMobileid = "";
	else
		sMobileid = replaceString(sMobileid, gtFind, gsReplece);
	end
	tiros.moduledata.moduledata_set("framework", "mobileid", sMobileid);
	--tiros.moduledata.moduledata_set("framework", "mobileid", "123456789011111");
end

--[[
--@描述:从数据仓库获取平台标识，并存入lua数据仓库
--@param  无
--@return 无
--]]
local function setPlatForm()
	local nAppid = tiros.moduledata.moduledata_get("framework", "appid");

	local sPlatform = "other";

	if nAppid == "0" then
		sPlatform = "other";
	elseif nAppid == "1" then
		sPlatform = "ios"
	elseif nAppid == "2" then
		sPlatform = "android"
	elseif nAppid == "3" then
		sPlatform = "android"
	end
	print("platform=",sPlatform)
	tiros.moduledata.moduledata_set("framework", "platform", sPlatform);
end

--[[
--@从tapi接口获取imsi并存入lua数据仓库
--@param  无
--@return 无
--]]
local function setImsi()
	local sImsi = tiros.tapi.tapigetimsi();
	if sImsi == nil then
		sImsi = "";
	else
		sImsi = replaceString(sImsi, gtFind, gsReplece);
	end
	tiros.moduledata.moduledata_set("framework", "imsi", sImsi);
end

--[[
--@从version文件获取版本信息并存入lua数据仓库
--@param  无
--@return 无
--]]
local function setVersion()
	if gsVersionFileName == nil then
		return;
	end
	--如果fs0里没有version文件则返回nil
	if not filelib.fexist(gsVersionFileName) then
		print("there is no version file in fs0:/");
		return;
	end
	--打开并读取数据
	local sVersion = "undefineversion";
	local file = filelib.fopen(gsVersionFileName, 1);
	if (file ~= nil) then	
		local nFileDataLength = filelib.fgetsize(gsVersionFileName);
		if nFileDataLength >= 8 then
			local fileData = memorylib.malloc(nFileDataLength + 1);
			memorylib.memset(fileData, 0, nFileDataLength + 1);
			filelib.fmemread(file, fileData, nFileDataLength);

			local sUnit1 = packedlib.readuint8(fileData, 0);
			local sUnit2 = packedlib.readuint8(fileData, 1);
			local sUnit3 = packedlib.readuint16(fileData, 2);
			local sUnit4 = packedlib.readuint32(fileData, 4);
			--拼接字符
			sVersion = sUnit1.."."..sUnit2.."."..sUnit3.."."..sUnit4;

			memorylib.free(fileData);
		end
		filelib.fclose(file);
	end
	--写入数据仓库
	tiros.moduledata.moduledata_set("framework", "version", sVersion);
end

--[[
--@从tapi接口获取sOSVersion并存入lua数据仓库
--@param  无
--@return 无
--]]
local function setOSVersion()
	local sManufacturerName, sDeviceModel, sOSVersion = tiros.tapi.tapigetdeviceinfo();
	if sOSVersion == nil then
		sOSVersion = "";
	else
		sOSVersion = replaceString(sOSVersion, gtFind, gsReplece);
	end
	--写入数据仓库
	tiros.moduledata.moduledata_set("framework", "osversion", sOSVersion);
end

--[[
--@从tapi接口获取devicemodel并存入lua数据仓库
--@param  无
--@return 无
--]]
local function setDeviceModel()
	local sManufacturerName, sDeviceModel, sOSVersion = tiros.tapi.tapigetdeviceinfo();

	if sDeviceModel == nil then
		sDeviceModel = "";
	else
		sDeviceModel = replaceString(sDeviceModel, gtFind, gsReplece);
	end
	--写入数据仓库
	tiros.moduledata.moduledata_set("framework", "devicemodel", sDeviceModel);
end

--[[
--@从tapi接口获取manufacturername并存入lua数据仓库
--@param  无
--@return 无
--]]
local function setManufacturerName()
	local sManufacturerName, sDeviceModel, sOSVersion = tiros.tapi.tapigetdeviceinfo();
	if sManufacturerName == nil then
		sManufacturerName = "";
	else
		sManufacturerName = replaceString(sManufacturerName, gtFind, gsReplece);
	end
	--写入数据仓库
	tiros.moduledata.moduledata_set("framework", "manufacturername", sManufacturerName);
end

--[[
--@从fs4:/user文件读取uid存入lua数据仓库
--@param  无
--@return 无
--]]
local function setUID()
	if gsUserFileName == nil then
		return;
	end
	--如果fs4里没有user文件则返回nil
	if not filelib.fexist(gsUserFileName) then
		print("there is no user file in fs4:/");
		return;
	end
	--打开并读取数据
	local sData = tiros.file.Readfile(gsUserFileName);
	local tData;
	local sUID;

	if (sData ~= nil and sData ~= "") then
		--转换成table
		tData = tiros.json.decode(sData);
		--获取对应value
		sUID = tData["uid"];
	end

	if sUID == nil then
		sUID = "";
	end
	--写入数据仓库
	tiros.moduledata.moduledata_set("framework", "uid", sUID);
end


--[[
--@从fs4:/user文件读取手机号码存入lua数据仓库
--@param  无
--@return 无
--]]
local function setPhone()
	if gsUserFileName == nil then
		return;
	end
	--如果fs4里没有user文件则返回nil
	if not filelib.fexist(gsUserFileName) then
		print("there is no user file in fs4:/");
		return;
	end
	--打开并读取数据
	local sData = tiros.file.Readfile(gsUserFileName);
	local tData;
	local sPhone;

	if (sData ~= nil and sData ~= "") then
		--转换成table
		tData = tiros.json.decode(sData);
		--获取对应value
		sPhone = tData["phone"];
	end

	if sPhone == nil then
		sPhone = "";
	end
	--写入数据仓库
	tiros.moduledata.moduledata_set("framework", "phone", sPhone);
end


--[[
--@从fs4:/user文件读取uid_aid存入lua数据仓库
--@param  无
--@return 无
--]]
local function setUidAid()
	if gsUserFileName == nil then
		return;
	end
	--如果fs4里没有user文件则返回nil
	if not filelib.fexist(gsUserFileName) then
		print("there is no user file in fs4:/");
		return;
	end
	--打开并读取数据
	local sData = tiros.file.Readfile(gsUserFileName);
	local tData;
	local sUidAid;
	if (sData ~= nil and sData ~= "") then
		--转换成table
		tData = tiros.json.decode(sData);
		--获取对应value
		sUidAid = tData["uid_aid"];
	end

	if sUidAid == nil then
		sUidAid = "";
	end
	--写入数据仓库
	tiros.moduledata.moduledata_set("framework", "uid_aid", sUidAid);
end

--[[
--@从fs4:/user文件读取mobileid_aid存入lua数据仓库
--@param  无
--@return 无
--]]
local function setMobileidAid()
	if gsUserFileName == nil then
		return;
	end
	--如果fs4里没有user文件则返回nil
	if not filelib.fexist(gsUserFileName) then
		print("there is no user file in fs4:/");
		return;
	end
	--打开并读取数据
	local sData = tiros.file.Readfile(gsUserFileName);
	local tData;
	local sMobileidAid;

	if (sData ~= nil and sData ~= "") then
		--转换成table
		tData = tiros.json.decode(sData);
		--获取对应value
		sMobileidAid = tData["mobileid_aid"];
	end

	if sMobileidAid == nil then
		sMobileidAid = "";
	end
	--写入数据仓库
	tiros.moduledata.moduledata_set("framework", "mobileid_aid", sMobileidAid);
end

--[[
--@把常用信息存入lua数据仓库,比如客户端版本号,mobileid,imsi,手机系统版本等
--@param  无
--@return 无
--]]
createmodule(interFace, "SetCommonlyInfoIntoModuledata", function ()
	setMobileid();
	setImsi();
	setVersion();
	setOSVersion();
    	setDeviceModel();
	setManufacturerName();
	setMobileidAid();
	setUidAid();
	setUID();
	setPhone();
	setPlatForm();
end)

--[[
--@在sSource字符串里查找sBegin和sEnd中间的部分并返回
--@param  sSource string型参数,源字符串
--@param  sBegin string型参数,开始字符串
--@param  sEnd string型参数,结束字符串
--@return string型,sBegin和sEnd中间的部分
--]]
local function findString(sSource,sBegin,sEnd)
	local sBeginBegin, sBeginEnd = string.find(sSource, sBegin);
	local sEndBegin, sEndEnd = string.find(sSource, sEnd,sBeginEnd + 1);
	local sDest = string.sub(sSource, sBeginEnd + 1, sEndBegin - 1);
	return sDest;
end

--[[
--@从rs文件获取url,然后根据gitversion区分服务器指向拼接成完整的url
--@param  sFileName string型参数,rs文件全名
--@param  nID int型参数,rs文件里面资源id
--@return string型,完整的URL
--]]
createmodule(interFace,"getUrlFromResource",function (sFileName, nID)
	local sBasicURL = resourcelib.str_s(sFileName, nID);
	local sGitVersion = tiros.file.Readfile("fs0:/gitversion");
	local nLenVersion = string.len(sGitVersion);
	local sVersion = nil;
	local sUrl = nil
	--如果内容大于200字符则认为是子模组生成的文件
	if nLenVersion > 200 then
		local sModule = findString(sFileName, "res/", "/");
		local sModuleBegin, sModuleEnd = string.find(sGitVersion, sModule);
		local sQuotationStart, sQuotationEnd = string.find(sGitVersion, "'", sModuleEnd);
		local sEnteringStart, sEnteringEnd = string.find(sGitVersion, "Entering", sQuotationEnd);
		if ((sQuotationEnd ~= nil) and (sEnteringStart ~= nil)) then
			sVersion = string.sub(sGitVersion, sQuotationEnd + 1, sEnteringStart - 1);
		else
			sVersion = ""
		end
	else
		sVersion = sGitVersion;
	end

	--如果id是1003则为UDP的URL,如果id是101则为TCP的URL,201未TCP的端口否则为http的URL
	if (string.find(sFileName,"api.rs") ~= nil) and (nID == 1003) then
		if string.find(sVersion,gsPublic) ~= nil then
			sUrl = string.format(sBasicURL, gsPublicUDPURL);
		elseif string.find(sVersion,gsTest) ~= nil then
			sUrl = string.format(sBasicURL, gsTestUDPURL);
		elseif string.find(sVersion,gsDev) ~= nil then
			sUrl = string.format(sBasicURL, gsDevUDPURL);
		else
			sUrl = string.format(sBasicURL, gsTestUDPURL);
		end
	elseif (string.find(sFileName,"module.rs") ~= nil) and (nID == 101) then
		if string.find(sVersion,gsPublic) ~= nil then
			sUrl = string.format(sBasicURL, gsTcpPublicURL);
		elseif string.find(sVersion,gsTest) ~= nil then
			sUrl = string.format(sBasicURL, gsTcpTestURL);
		elseif string.find(sVersion,gsDev) ~= nil then
			sUrl = string.format(sBasicURL, gsTcpDevURL);
		else
			sUrl = string.format(sBasicURL, gsTcpTestURL);
		end
	elseif (string.find(sFileName,"module.rs") ~= nil) and (nID == 201) then
		if string.find(sVersion,gsPublic) ~= nil then
			sUrl = string.format(sBasicURL, gsTcpPublicPort);
		elseif string.find(sVersion,gsTest) ~= nil then
			sUrl = string.format(sBasicURL, gsTcpTestPort);
		elseif string.find(sVersion,gsDev) ~= nil then
			sUrl = string.format(sBasicURL, gsTcpDevPort);
		else
			sUrl = string.format(sBasicURL, gsTcpTestPort);
		end
	else
		if string.find(sVersion,gsPublic) ~= nil then
			sUrl = string.format(sBasicURL, gsPublicURL);
		elseif string.find(sVersion,gsTest) ~= nil then
			sUrl = string.format(sBasicURL, gsTestURL);
		elseif string.find(sVersion,gsDev) ~= nil then
			sUrl = string.format(sBasicURL, gsDevURL);
		else
			sUrl = string.format(sBasicURL, gsTestURL);
		end
	end
	print("getUrlFromResource=", sFileName, nID, sUrl);
	return sUrl;
end)

--[[
--@根据gitversion的内容返回不同爱滔客服务器的url地址
--@return string型,完整的URL
--]]
createmodule(interFace,"getAirTalkeeUrl",function ()

	local sGitVersion = tiros.file.Readfile("fs0:/gitversion");
	local nLenVersion = string.len(sGitVersion);
	local sVersion = nil;
	local sUrl = nil
	--如果内容大于200字符则认为是子模组生成的文件
	if nLenVersion > 200 then
		local sModule = "api"
		local sModuleBegin, sModuleEnd = string.find(sGitVersion, sModule);
		local sQuotationStart, sQuotationEnd = string.find(sGitVersion, "'", sModuleEnd);
		local sEnteringStart, sEnteringEnd = string.find(sGitVersion, "Entering", sQuotationEnd);
		if ((sQuotationEnd ~= nil) and (sEnteringStart ~= nil)) then
			sVersion = string.sub(sGitVersion, sQuotationEnd + 1, sEnteringStart - 1);
		else
			sVersion = ""
		end
	else
		sVersion = sGitVersion;
	end

	if string.find(sVersion,gsPublic) ~= nil then
		sUrl = gsPublicAirTalkeeURL;
	elseif string.find(sVersion,gsTest) ~= nil then
		sUrl = gsTestAirTalkeeURL;
	elseif string.find(sVersion,gsDev) ~= nil then
		sUrl = gsDevAirTalkeeURL;
	else
		sUrl = gsTestAirTalkeeURL;
	end
	print("getAirTalkeeUrl=", sUrl);
	return sUrl;
end)

tiros.framework = readOnly(interFace);
tiros.framework.SetCommonlyInfoIntoModuledata();

