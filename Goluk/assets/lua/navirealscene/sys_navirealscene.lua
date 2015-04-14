--[[
-- @描述:导航实景操作
-- @编写人:lyfsteven
-- @创建日期: 2013-5-22
--]]

require"lua/json"
require"lua/moduledata"
require"lua/systemapi/sys_namespace"
require"lua/http"
require"lua/systemapi/sys_commfunc"
require"lua/framework/sys_framework"
require"lua/systemapi/sys_handle"


local interface = {}

--=========================================内部数据=========================================
local completed = {}
local uncompleted = {}
--导航实景对象
local nrsHandler = nil
local completeCityCode = nil;

local REALSCENE_CFG_PATH = "fs1:/navidog/realscene/realscene.cfg"
local REALSCENE_FOLDER = "fs1:/navidog/realscene/"
local CFG_EXTEND = "cfg"       -- 配置文件后缀
local LOADING_EXTEND = "_"   -- 正在下载文件后缀
local NEW_LOADING_EXTEND = ".rb"   -- 正在下载文件后缀

--=========================================内部实现=========================================
local function sys_DelOnlineMapTimerExpried(ttype)
	local delPathFolder = "fs1:/navidog/onlinerealscene/" .. tostring(completeCityCode);
	local files = filelib.fenumstart(delPathFolder, false);
	if nil == files then
		return;		
	end
	while true do
		local fileName = filelib.fenumnext(files);
		if nil ~= fileName then
			local fullName = delPathFolder .. "/" .. fileName;
			filelib.fremove(fullName);
		else
			break;			
		end
	end
	filelib.fenumend(files);
end

local function sys_SetHttpStateValue(event, data)
	local temp = {}
	temp[1] = event
	temp[2] = data
	local jsonStr = tiros.json.encode(temp)
	tiros.moduledata.moduledata_set("web","realimagehttpevent_ptp",jsonStr)

	--当地图下载完成，删除online下载的实景图
	if 4 == tonumber(event) and nil ~= data then
		print("navirealscene sys_SetHttpStateValue:", data);
		completeCityCode = data;
		tiros.timer.timerstartforlua("Navirealsencese", 100, sys_DelOnlineMapTimerExpried, false);
	end
end

local function sys_SetHttpDataSize(dataSize)
	print("navirealscene sys_SetHttpDataSize", tostring(dataSize))
	tiros.moduledata.moduledata_set("web","realimagesize_ptp",dataSize)
end

local function  sys_CleanCityMapData()
	print("navirealscene sys_CleanCityMapData")
	completed = {}
	uncompleted = {}
end

local function  sys_SetCityMapState(cityCode, state)
	print("navirealscene sys_SetCityMapState" .. tostring(cityCode) .. "," .. tostring(state))
	if state == 1 then
		completed[#completed + 1] = cityCode
	elseif state == 2 then
		uncompleted[#uncompleted + 1] = cityCode
	end
end

local function  sys_WriteAllCityMapState()
	local temp = {}
	temp.completed = completed
	temp.uncompleted = uncompleted
	local jsonStr = tiros.json.encode(temp)
	print("navirealscene sys_WriteAllCityMapState" .. jsonStr)
	tiros.moduledata.moduledata_set("web","realpicstate_ptp",jsonStr)
end

local function sys_navirealsceneinstance()
	print("navirealscene sys_navirealsceneinstance")
	if nrsHandler == nil then
		nrsHandler = navirealscenelib.create()
		return nrsHandler
	end
	return nrsHandler
end

local function sys_navirealscenedestroy()
	print("navirealscene sys_navirealscenedestroy")
	if nrsHandler ~= nil then
	   	navirealscenelib.destroy(nrsHandler)
		nrsHandler = nil
	end	
end

--保存是否下载过实景图信息
--value:0/1,0没有下载；1下载过
local function sys_saveRealsceneDownInfo(value)
	local bExist = filelib.fexist(REALSCENE_CFG_PATH);
	if not bExist then
		sys_init();
	end
	if not filelib.fexist(REALSCENE_CFG_PATH) then
		return;
	end

	if value == nil then
		value = 0;
	end	

	local strJson = tiros.file.Readfile(REALSCENE_CFG_PATH);
	local strTable = {};
	
	print("yaoyt realscene saveRealsceneCFG 22222");
	strTable = tiros.json.decode(strJson);
	strTable["isDownloadRealSecne"] = tonumber(value);
	tiros.file.Writefile(REALSCENE_CFG_PATH,tiros.json.encode(strTable),true);
end

--是否下载过实景图信息
local function sys_isRealsceneDown()
	local bExist = filelib.fexist(REALSCENE_CFG_PATH);
	if not bExist then
		return false;
	end


	local strJson = tiros.file.Readfile(REALSCENE_CFG_PATH);
	local strTable = {};
	
	print("yaoyt realscene saveRealsceneCFG 22222");
	strTable = tiros.json.decode(strJson);
	if 1 == strTable["isDownloadRealSecne"] then 
		return true;
	else
		return false;
	end
end

local function sys_navirealscenerequestmap(cityCode)
	print("navirealscene sys_navirealscenerequestmap" .. tostring(cityCode))
	sys_saveRealsceneDownInfo(1);
	if nrsHandler ~= nil then
	   	navirealscenelib.requestmap(nrsHandler, tonumber(cityCode))
	else
		nrsHandler = sys_navirealsceneinstance()
		navirealscenelib.requestmap(nrsHandler, tonumber(cityCode))
	end
end

local function sys_navirealscenecheckmapstate(cityCode)
	print("navirealscene sys_navirealscenecheckmapstate" .. tostring(cityCode))
	if nrsHandler ~= nil then
	   	return navirealscenelib.checkmapstate(nrsHandler, tonumber(cityCode))
	else
		nrsHandler = sys_navirealsceneinstance()
		return navirealscenelib.checkmapstate(nrsHandler, tonumber(cityCode))
	end
end


local function sys_navirealscenecheckallmapstate()
	print("navirealscene sys_navirealscenecheckallmapstate")
	if nrsHandler ~= nil then
	   	return navirealscenelib.checkallmapstate(nrsHandler)
	else
		nrsHandler = sys_navirealsceneinstance()
		return navirealscenelib.checkallmapstate(nrsHandler)
	end
end

local function sys_navirealscenecancelrequest(cityCode)
	print("navirealscene sys_navirealscenecancelrequest" .. tostring(cityCode))
	if nrsHandler ~= nil then
	   	return navirealscenelib.cancelrequest(nrsHandler, tonumber(cityCode))
	else
		nrsHandler = sys_navirealsceneinstance()
		return navirealscenelib.cancelrequest(nrsHandler, tonumber(cityCode))
	end
end


local function sys_navirealscenepauserequest(cityCode)
	print("navirealscene sys_navirealscenepauserequest" .. tostring(cityCode))
	if nrsHandler ~= nil then
	   	return navirealscenelib.pauserequest(nrsHandler, tonumber(cityCode))
	else
		nrsHandler = sys_navirealsceneinstance()
		return navirealscenelib.pauserequest(nrsHandler, tonumber(cityCode))
	end
end
local function sys_navirealscenedelmap(cityCode)
	print("navirealscene sys_navirealscenedelmap" .. tostring(cityCode))
	if nrsHandler ~= nil then
	   	return navirealscenelib.delmap(nrsHandler, tonumber(cityCode))
	else
		nrsHandler = sys_navirealsceneinstance()
		return navirealscenelib.delmap(nrsHandler, tonumber(cityCode))
	end
end

local function sys_init()
	local freeSpace = filelib.fgetfreespace("fs1:/");
	if filelib.fdiskexist("fs1:/") == false or freeSpace < 100 then
		return;
	end

	local bExist = filelib.fexist(REALSCENE_CFG_PATH);
	if not bExist then
		local mapcfg = {};
		mapcfg["count"] = 0;
		mapcfg["realscenelist"] = {};
		mapcfg["noticeDate"] = 0;
		mapcfg["DownloadSizeDate"] = 0;
		mapcfg["isDownloadRealSecne"] = 0;
		tiros.file.Writefile(REALSCENE_CFG_PATH,tiros.json.encode(mapcfg),true);
	end
end

--删除navirealscen所有文件，重新生成配置文件
local function sys_navirealscenedelallmap()
	print("navirealscene sys_navirealscenedelallmap")
	local isDown = sys_isRealsceneDown();
	if nrsHandler ~= nil then
	   	navirealscenelib.delallmaps(nrsHandler)
	else
		nrsHandler = sys_navirealsceneinstance()
		navirealscenelib.delallmaps(nrsHandler)
	end
	--重新创建配置文件
	sys_init();
	if isDown then
		sys_saveRealsceneDownInfo(1);
	else
		sys_saveRealsceneDownInfo(0);
	end
end

--将实景图信息写入配置文件
local function sys_saveRealsceneCFG(citycode,date,ver)
	print("yaoyt realscene saveRealsceneCFG in citycode" .. citycode);
	local bExist = filelib.fexist(REALSCENE_CFG_PATH);
	if not bExist then
		sys_init();
	end
	if not filelib.fexist(REALSCENE_CFG_PATH) then
		return false;
	end
	print("yaoyt realscene saveRealsceneCFG 1111");
	local strJson = tiros.file.Readfile(REALSCENE_CFG_PATH);
	local strTable = {};
	local index = 0;
	local map = {};
	
	print("yaoyt realscene saveRealsceneCFG 22222");
	strTable = tiros.json.decode(strJson);

	for i = 1,#strTable["realscenelist"] do
		print("yaoyt realscene saveRealsceneCFG in");
		local mapinfo = strTable["realscenelist"][i]
		-- 这个实景图已经存在
		if tonumber(mapinfo["citycode"]) == tonumber(citycode) then
			print("yaoyt realscene saveRealsceneCFG exist");
			-- 只保存新的date
			if tonumber(date) > tonumber(mapinfo["date"]) then
				print("yaoyt realscene saveRealsceneCFG date new");
				map = strTable["realscenelist"][i];
				index = i;
				break;
			else
				print("yaoyt realscene saveRealsceneCFG date old");
				return;
			end
		end
	end

	print("yaoyt realscene saveRealsceneCFG 33333");
	if index == 0 then
		print("yaoyt realscene saveRealsceneCFG not exist");
		map["citycode"] = tostring(citycode);
		index = #strTable["realscenelist"] + 1;
	end

	map["date"] = tostring(date);
	if nil ~= ver then
		map["ver"] = ver;
	end

	strTable["realscenelist"][index] = map;
	strTable["count"] = tostring(#strTable["realscenelist"]);
	tiros.file.Writefile(REALSCENE_CFG_PATH,tiros.json.encode(strTable),true);
	print("yaoyt realscene saveRealsceneCFG end");
end

--检查地图是否需要更新
local function sys_checkDownloadUpdate(date,isNotice,citycode)
	print("yaoyt realscene sys_checkDownloadUpdate in date:" .. date);
	if not filelib.fexist(REALSCENE_CFG_PATH) then
		return false;
	end
	local strJson = tiros.file.Readfile(REALSCENE_CFG_PATH);
	local strTable = tiros.json.decode(strJson);
	print("yaoyt realscene sys_checkDownloadUpdate in:" .. date)
	if nil == citycode then
		--开机弹框提示
		if true == isNotice then
			--如果这个date已经提示过了，不再提示
			if tonumber(date) <= tonumber(strTable["noticeDate"]) then
				return false;
			end
		end

		for i = 1,#strTable["realscenelist"] do
			local mapinfo = strTable["realscenelist"][i]
			if tonumber(mapinfo["date"]) < tonumber(date) then
				return true;
			end
		end
	else
		for i = 1,#strTable["realscenelist"] do
			local mapinfo = strTable["realscenelist"][i]
			if mapinfo["citycode"] == citycode then
				if tonumber(mapinfo["date"]) < tonumber(date) then
					return true;
				else
					return false;
				end
			end
		end
	end
	return false;
end

--获取暂停下载的地图table
local function getIngMapTable()
	print("yaoyt realscene getIngMapTable in");
	local ingCity= {};
	local newVersion = navirealscenelib.getNewVersion();
	local isUpdate = sys_checkDownloadUpdate(newVersion,false);

	local files = filelib.fenumstart(REALSCENE_FOLDER, false);
	if nil == files then
		return;		
	end
	while true do
		local fileName = filelib.fenumnext(files);
		if nil ~= fileName then
			local citycode = nil;
			local old_head,old_tail = string.find(fileName,LOADING_EXTEND);
			if nil ~= old_head then
				citycode = string.sub(fileName,1,old_head - 1);
				print("yaoyt realscene getIngMapTable line name:" .. citycode);
			else
				local rb_head,rb_tail = string.find(fileName,NEW_LOADING_EXTEND);
				if nil ~= rb_head then
					citycode = string.sub(fileName,1,rb_head - 1);
					print("yaoyt realscene getIngMapTable rb name:" .. citycode);
				end
			end
			if nil ~= citycode then
				table.insert(ingCity,tonumber(citycode));
			end
		else
			break;			
		end
	end
	filelib.fenumend(files);
	print(tiros.json.encode(ingCity));
	return ingCity;
end

--获取下载完成的地图table
local function getDownloadMapTable()
	local DownloadCity= {};
	if not filelib.fexist(REALSCENE_CFG_PATH) then
		return false;
	end
	local strJson = tiros.file.Readfile(REALSCENE_CFG_PATH);
	local strTable = tiros.json.decode(strJson);

	for index = 1,tonumber(strTable["count"]) do
		local mapinfo = {}
		mapinfo = strTable["realscenelist"][tonumber(index)];
		if nil ~= mapinfo then
			print("yaoyt realscene getDownloadCitycode not nil" .. tonumber(mapinfo["citycode"]))
			table.insert(DownloadCity,tonumber(mapinfo["citycode"]))
		end
	end
	print(tiros.json.encode(DownloadCity));
	return DownloadCity;
end

--获取需要更新的下载完成的地图table
local function getUpdateMapTable()
	local UpdateCity= {};
	local newVersion = navirealscenelib.getNewVersion();
	if not filelib.fexist(REALSCENE_CFG_PATH) then
		return false;
	end
	local strJson = tiros.file.Readfile(REALSCENE_CFG_PATH);
	local strTable = tiros.json.decode(strJson);

	for index = 1,tonumber(strTable["count"]) do
		local mapinfo = {}
		mapinfo = strTable["realscenelist"][tonumber(index)];
		if nil ~= mapinfo then
			print("yaoyt realscene getDownloadCitycode not nil" .. tonumber(mapinfo["citycode"]))
			if tonumber(newVersion) > tonumber(mapinfo["date"]) then
				table.insert(UpdateCity,tonumber(mapinfo["citycode"]))
			end
		end
	end
	print(tiros.json.encode(UpdateCity));
	return UpdateCity;
end

local function sys_deleteRepeat(tDownload,tCompleteCity,tUpdateCity)
	for index = 1,#tDownload do
		local citycode = tDownload[index];
		local sameComp = 0;
		for compIndex = 1,#tCompleteCity do
			if citycode == tCompleteCity[compIndex] then
				sameComp = compIndex;
				break;
			end
		end
		if 0 ~= sameComp then
			table.remove(tCompleteCity,sameComp);
		end

		local sameUpdate = 0;
		for updateIndex = 1,#tUpdateCity do
			if citycode == tUpdateCity[updateIndex] then
				sameUpdate = updateIndex;
				break;
			end
		end
		if 0 ~= sameUpdate then
			table.remove(tUpdateCity,sameUpdate);
		end
	end
end
--[[
 @描述:获取本地实景图状态
 @return  table型 {"completecity":[110000,120000,...],"download":[412300,512300,...],"updates":[120000,...]}
--]]
local function sys_getCompleteCity()
	local state = {};
	local tCompleteCity = {};
	local tDownload = {};
	local tUpdateCity = {};
	tCompleteCity = getDownloadMapTable();
	tDownload = getIngMapTable();
	tUpdateCity = getUpdateMapTable();
	state["completecity"] = tCompleteCity;
	state["download"] = tDownload;
	state["updates"] = tUpdateCity;
	sys_deleteRepeat(tDownload,tCompleteCity,tUpdateCity)
	
	print(tiros.json.encode(state));
	return state;
end

--检查是否需要下载新的地图大小的数据
local function sys_checkNeedDownloadRealsceneSize()
	local newDate = navirealscenelib.getNewVersion();
	if not filelib.fexist(REALSCENE_CFG_PATH) then
		return false;
	end
	local strJson = tiros.file.Readfile(REALSCENE_CFG_PATH);
	local strTable = tiros.json.decode(strJson);
	
	if tonumber(newDate) > strTable["DownloadSizeDate"] then
		return true,newDate;
	else
		return false,0;
	end
end

--记录下载的地图大小版本号
local function sys_writeDownloadRealsceneSizeVersion(version)
	if not filelib.fexist(REALSCENE_CFG_PATH) then
		return false;
	end
	local strJson = tiros.file.Readfile(REALSCENE_CFG_PATH);
	local strTable = tiros.json.decode(strJson);
	
	if tonumber(version) > strTable["DownloadSizeDate"] then
		strTable["DownloadSizeDate"] = tonumber(version);
		tiros.file.Writefile(REALSCENE_CFG_PATH,tiros.json.encode(strTable),true);
	end
end

local function sys_saveOldCFG()
	local files = filelib.fenumstart(REALSCENE_FOLDER, false);
	if nil == files then
		return;		
	end

	while true do
		local fileName = filelib.fenumnext(files);
		if nil ~= fileName then
			local cfg_head,cfg_tail = string.find(fileName,CFG_EXTEND);
			local load_head,load_tail = string.find(fileName,LOADING_EXTEND);
			local rb_head,rb_tail = string.find(fileName,NEW_LOADING_EXTEND);
			if nil == cfg_head and nil == load_head and nil == rb_head then
				sys_saveRealsceneCFG(fileName,0);
			end
		else
			break;			
		end
	end
	filelib.fenumend(files);
end

local function sys_saveNoticeDate(date)
	if not filelib.fexist(REALSCENE_CFG_PATH) then
		return false;
	end
	local strJson = tiros.file.Readfile(REALSCENE_CFG_PATH);
	local strTable = tiros.json.decode(strJson);
	
	if tonumber(date) > strTable["noticeDate"] then
		strTable["noticeDate"] = tonumber(date);
		tiros.file.Writefile(REALSCENE_CFG_PATH,tiros.json.encode(strTable),true);
	end
end

--=========================================外部接口=========================================
--@@描述:设置Http的状态和数据
--@param event：http状态
--@param data：数据
--@输出：无
createmodule(interface, "SetHttpStateValue", function(event, data)
	return sys_SetHttpStateValue(event, data)
end)
--@@描述:设置导航实景图片的总大小
--@param dataSize：文件大小
--@输出：无
createmodule(interface, "SetHttpDataSize", function(dataSize)
	return sys_SetHttpDataSize(dataSize)
end)


--@@描述:清空城市状态表的数据
--@输出：无
createmodule(interface, "CleanCityMapData", function()
	return sys_CleanCityMapData()
end)
--@@描述:设置城市导航实景图片的状态
--@param cityCode：城市代码
--@param state：城市导航实景图片的状态，1表示有，2表示导航实景图片不完整
--@输出：无
createmodule(interface, "SetCityMapState", function(cityCode, state)
	return sys_SetCityMapState(cityCode, state)
end)

--@@描述:将所有图片状态写入数据仓库
--@输出：无
createmodule(interface, "WriteAllCityMapState", function()
	return sys_WriteAllCityMapState()
end)

--@@描述:通知模块销毁导航实景对象
--@输出：无
createmodule(interface, "navirealscenedestroy", function()
	return sys_navirealscenedestroy()
end)

--@@描述:请求实景图片下载
--@param cityCode：城市代码
--@输出：无
createmodule(interface, "navirealscenerequestmap", function(cityCode)
return sys_navirealscenerequestmap(cityCode)
end)
--[[ navirealscenerequestmap使用说明
typedef enum _NaviRealSceneMapEvent
{
    NRSMS_DL_RESPONSE, ///<导航实景图片下载的总大小  size
    NRSMS_DL_NO_SPACE_SIZE,///<判断空间是否足够大   null
    NRSMS_DL_NO_DISK, // has not memory cark
    NRSMS_DL_PROGRESS, ///<导航实景图片下载的百分比  20,30
    NRSMS_DL_COMPLETED, ///<导航实景图片下载完成  
    NRSMS_DL_ERR      ///<导航实景图片下载错误
} NaviRealSceneMapEvent

说明：当调用navirealscenerequestmap之后，http请求发生的回调事件将不断写入lua仓库，
web将启动一个200ms的定时器不断读取lua仓库。http描述如上面的枚举。
lua仓库数据：
realimagesize_ptp，用于存放下载的文件总大小，内容是一个number。
realimagehttpevent_ptp， 用于不断写入http事件， 内容为json串{key value}，例子{3,18}，下载百分比
]]



--@@描述:测试实景图片是否存在
--@param cityCode：城市代码
--@输出：0表示没有图片文件，1表示有图片文件，2表示下载了一部分
createmodule(interface, "navirealscenecheckmapstate", function(cityCode)
return sys_navirealscenecheckmapstate(cityCode)
end)


--@@描述:测试所有的实景图片是否存在。调用该函数后，数据将被保存在lua仓库中。
--	 key：realpicstate_ptp， value是json串{"completed":[110000,120000],"uncompleted":[310000,610000]}
--@输出：函数无返回值
createmodule(interface, "navirealscenecheckallmapstate", function()
return sys_navirealscenecheckallmapstate()
end)

--@@描述:取消实景图片下载，同时删除已下载的不全的文件
--@输出：boolean  true 成功 false 失败
createmodule(interface, "navirealscenecancelrequest", function(cityCode)
return sys_navirealscenecancelrequest(cityCode)
end)


--@@描述:暂停实景图片下载
--@输出：boolean  true 成功 false 失败
createmodule(interface, "navirealscenepauserequest", function(cityCode)
return sys_navirealscenepauserequest(cityCode)
end)


--@@描述:删除导航实景图片
--@param cityCode：城市代码
--@输出：boolean  true 成功 false 失败
createmodule(interface, "navirealscenedelmap", function(cityCode)
return sys_navirealscenedelmap(cityCode)
end)
--@@描述:删除所有本地导航实景图片
--@输出：boolean  true 成功 false 失败
createmodule(interface, "navirealscenedelallmap", function()
return sys_navirealscenedelallmap()
end)

--@@描述:判断是否下载了我的位置所在的离线实景图
--@输出：boolean  true 有实景图 false 没有
createmodule(interface, "isDownloadMypsotionRealsencese", function()
	local cityInfo = tiros.moduledata.moduledata_get("web", "currentcity_ptp");

	if "" ~= cityInfo then
		local cityTable = {}
		cityTable = tiros.json.decode(cityInfo);
		local fname = "fs1:/navidog/realscene/" .. cityTable["province"][1];
		return filelib.fexist(fname);
	end

	return false;
end)

--[[
 @描述:获取实景图的下载状态
 @return  table型 {"completecity":[110000,120000,...],"download":[412300,512300,...]}
--]]
createmodule(interface,"GetCompleteCity",function ()
	return sys_getCompleteCity();
end)

--[[
 @描述:检查是否需要下载新的实景图大小
 @return true/false：是否需要
--]]
createmodule(interface,"CheckNeedDownloadRealsceneSize",function ()
	return sys_checkNeedDownloadRealsceneSize();
end)

--[[
 @描述:保存新下载的实景图大小的版本号
--]]
createmodule(interface,"WriteDownloadRealsceneSizeVersion",function (version)
	return sys_writeDownloadRealsceneSizeVersion(version);
end)


--[[
 @描述:实景图大小文件路径
--]]
createmodule(interface,"GetRealsceneSizeFilePath",function ()
	return "fs0:/res/logic/loadfile/mapsize";
end)

--[[
 @描述:检查已下载完成的实景图是否需要弹框提示更新，一个版本只弹一次
 @checkDate 实景图图版本日期
 @return true：需要更新，false不需要更新
--]]
createmodule(interface,"CheckDownloadNotify",function (checkDate)
	return sys_checkDownloadUpdate(checkDate,true);
end)

--[[
 @描述:检查已下载完成的实景图是否需要更新
 @return true：需要更新，false不需要更新
--]]
createmodule(interface,"CheckDownloadUpdate",function ()	
	local newDate = navirealscenelib.getNewVersion();
	return sys_checkDownloadUpdate(newDate,false);
end)

--[[
 @描述:将已经下载的实景图信息写入配置文件
--]]
createmodule(interface,"SaveDownloadCFG",function (citycode,date)
	sys_saveRealsceneCFG(citycode,date);
end)

--[[
 @描述:保存更新提示日期
--]]
createmodule(interface,"SaveNoticeDate",function (date)
	sys_saveNoticeDate(date);
end)

--[[
 @描述:将已经下载的实景图信息写入配置文件
--]]
createmodule(interface,"SaveOldCFG",function ()
	sys_saveOldCFG();
end)

createmodule(interface,"IsDownRealscene",function ()
	return sys_isRealsceneDown();
end)

--[[
 @描述:初始化配置文件，如果不存在配置文件创建配置文件
 @return 下载完成地图数目
--]]
createmodule(interface,"Init",function ()
	sys_init()
end)

tiros.navirealscene = readOnly(interface)
--file end


