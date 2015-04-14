--[[
 @描述:地图下载功能
 @编写人:孔祥宇
 @创建日期: 2012-10-30 下午 19:40:00	
--]]

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/moduledata"
require"lua/json"


--地图下载状况 CityMapStat
local CMS_START = 0; --开始地图下载
local CMS_STOP = 1;  --暂停地图下载
local CMS_CANCLE = 2; --取消地图下载  删除
local CMS_BUSY = 3; --判断是否繁忙
local CMS_DEL = 4; --删除

--地图下载消息值 CityMapEvent
local CME_DL_REQUSET = 0; --提交地图下载请求
local CME_DL_RESPONSE = 1; --地图下载的总大小
local CME_DL_NO_SPACE_SIZE = 2; --判断空间是否足够大
local CME_DL_PROGRESS = 3; --地图下载的百分比
local CME_DL_COMPLETED = 4; --地图下载完成
local CME_DL_ERR = 5;      --地图下载错误
local CME_IS_START = 6;    --地图下载完成后 开始安装
local CME_IS_DONE = 7;     --地图下载完成后 安装完成
local CME_IS_ERR = 8;      --地图安装错误

--当前本地保存指定地图资源状态码
local MAP_RS_BYCODE_NOTEXIST = 0;--不存在
local MAP_RS_BYCODE_EXIST = 1;--完全存在
local INCOMPLETE_OR_IMPERFECT_NOTEXIST = 2;--非完全存在 或者非完全不存在

local oldparam = -1;

local CITYMAP_DOWNLOAD_CFG_FOLDER = "fs1:/navidog/map/basemap1280/config"      --下载的地图中的配置文件文件加
local CITYBUILD_DOWNLOAD_CFG_FOLDER = "fs1:/navidog/map/building1280/config"   --下载的build中的配置文件文件加
local CITYMAP_CFG_PATH = "fs1:/navidog/map/basemap1280/map.cfg"                --地图配置文件路径
local CITYBUILD_CFG_PATH = "fs1:/navidog/map/building1280/build.cfg"           --build配置文件路径
local CITYZIP_SOURCE_PATH = "fs1:/navidog/mapsource/"                          --地图下载临时存放文件夹路径
local FILE_EXTEND = ".rb"                                                      --旧版本临时文件后缀
local FILE_EXTEND_OLD = ".ra"                                                  --新版本临时文件后缀

--[[
 @描述:地图资源下载回调函数 luaFunction(event,nCityCode, param);
 @param event - number型参数 地图下载消息值
 @param nCityCode - 当前操作的城市地图的城市码
 @param param - 回调数据 
 @par 接口使用约定：
  	1.返回消息为CME_DL_RESPONSE : param表示数据总大小。单位（M）
  	2.返回为CME_DL_PROGRESS ：param表示已接收数据与总数据的百分比  例如20% param = 20
	3.返回CME_DL_ERR ： param表示 http错误代码
    4.其他时候 param = nil
--]]

--地图下载句柄
local hCityMap = nil;
--注册地图下载回调函数
local luaFunction = nil;

--[[
 @描述:创建citymap句柄函数接口
 @param Ctype string型参数，用于唯一标识该citymap句柄
 @return 实际创建的citymap句柄
--]]
local function citymapcreate()
	if hCityMap == nil then	
	   	hCityMap = citymaplib.create();
	end	
	return hCityMap;
end

--[[
 @描述:销毁citymap句柄函数接口（该函数并没有立即销毁citymap句柄，而是等到下一个回收cd之后才会彻底销毁）
 @param Ctype string型参数，用于唯一标识该citymap句柄
 @return 无
--]]
local function citymapdestroy()
	if hCityMap ~= nil then
	   	citymaplib.destroy(hCityMap);
		hCityMap = nil;
	end	
end

--[[
 @描述:注册cityMap回调函数接口
 @return 无
--]]
local function citymapnotify()
	local citymap = citymapcreate();
	if citymap ~= nil then
		citymaplib.notify(citymap,"sys_CityMapEvnet");
	end
end

--[[
 @描述:地图下载
 @param nCityCode number型参数，城市Code
 @param cbkname luaFun型参数，注册地图下载回调函数
 @param nCityMapStat number型参数 地图下载状况 CityMapStat
 @param bIsAppoint bool型参数 如果指定城市删除bIsAppoint值为true（表示删除单个城市），如果删除省或地区的全部城市，该值为false。
 @return  bool型 成功返回true；失败返回false。 
--]]
local function downloadCityMap(nCityCode,cbkname,nCityMapStat,bIsAppoint)
	if nCityCode == nil then
		return false;
	end
	if cbkname == nil then
		return false;
	else
		luaFunction = cbkname;
	end

	local citymap = citymapcreate();	
	if citymap == nil then	
	   	return false;
	end

	citymapnotify();

	return citymaplib.enumByCode(citymap,nCityCode,nCityMapStat,bIsAppoint);
end

--lua层citymap事件回调处理函数：
DeclareGlobal("sys_CityMapEvnet",function (event,nCityCode, param)
     local data = {event,param};
     local json = tiros.json.encode(data);
     if event == 1 then
  		tiros.moduledata.moduledata_set("web","mapsize_ptp",param);
     elseif event == 3 then
         if oldparam ~= param then
             oldparam = param;
			 tiros.moduledata.moduledata_set("web","mapdownload_ptp",json);
         end
     else
		tiros.moduledata.moduledata_set("web","mapdownload_ptp",json);
     end
end)

--[[
 @描述:暂停下载
 @param nCityCode number型参数，城市Code
 @return bool型 成功返回true；失败返回false。
--]]
local function stopDownload(nCityCode)
	local citymap = citymapcreate();	
	if citymap == nil then	
	   	return false;
	end

	if nCityCode == nil then
		return false;
	end

	return citymaplib.enumByCode(citymap,nCityCode,CMS_STOP,true);
end

--[[
 @描述:取消下载
 @param nCityCode number型参数，城市Code
 @return bool型 成功返回true；失败返回false。
--]]
local function cancelDownload(nCityCode)
	local citymap = citymapcreate();	
	if citymap == nil then	
	   	return false;
	end
	if nCityCode == nil then
		return false;
	end
	return citymaplib.enumByCode(citymap,nCityCode,CMS_CANCLE,true);
end

--[[
 @描述:销毁citymap句柄函数接口
 @return 无
--]]
local function destroyDownload()
	local citymap = citymapcreate();	
	if citymap == nil then	
	   	return false;
	end

	citymaplib.destroy(citymap);
end

--[[
 @描述:删除全部地图 
 @return  bool型 成功返回true，失败返回false
--]]
local function deleteAllMap()
	local citymap = citymapcreate();	
	if citymap == nil then	
	   	return false;
	end

	return citymaplib.allCity(citymap,CMS_DEL);
end

--检查地图是否需要更新
local function checkDownloadMapUpdateByPath(path,date,isNotice,citycode)
	if not filelib.fexist(path) then
		return false;
	end

	local strJson = tiros.file.Readfile(path);
	local strTable = tiros.json.decode(strJson);
	print("yaoyt city checkDownloadMapUpdateByPath in:" .. date)
	if nil == citycode then
		--开机弹框提示
		if true == isNotice then
			--如果这个date已经提示过了，不再提示
			if tonumber(date) <= tonumber(strTable["noticeDate"]) then
				return false;
			end
		end

		for i = 1,#strTable["maplist"] do
			local mapinfo = strTable["maplist"][i]
			if tonumber(mapinfo["date"]) < tonumber(date) then
				return true;
			end
		end
	else
		for i = 1,#strTable["maplist"] do
			local mapinfo = strTable["maplist"][i]
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

--检查地图是否需要更新
local function checkDownloadUpdate(mapDate,BuildDate,isNotice,citycode)
	print("yaoyt city checkDownloadUpdate in")
	local mapResult = checkDownloadMapUpdateByPath(CITYMAP_CFG_PATH,mapDate,isNotice,citycode);
	local buildResult = checkDownloadMapUpdateByPath(CITYBUILD_CFG_PATH,BuildDate,isNotice,citycode);
	return mapResult or buildResult;
end

--获取暂停下载的地图table
local function getIngMapTable()
	local ingCity= {};
	local newVersion = citymaplib.getNewVersion();
	local isUpdate = checkDownloadUpdate(newVersion,newVersion,false);

	local files = filelib.fenumstart(CITYZIP_SOURCE_PATH, false);
	if nil == files then
		return;		
	end
	while true do
		local fileName = filelib.fenumnext(files);
		if nil ~= fileName then
			local rb_head,rb_tail = string.find(fileName,FILE_EXTEND);
			local citycode = nil;
			if nil ~= rb_head then
				citycode = string.sub(fileName,1,rb_head - 1);
			else
				local ra_head,ra_tail = string.find(fileName,FILE_EXTEND_OLD);
				if nil ~= ra_head then
					citycode = string.sub(fileName,1,ra_head - 1);
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
	return ingCity;
end

--获取下载完成的地图table
local function getDownloadMapTable()
	local DownloadCity= {};
	if not filelib.fexist(CITYMAP_CFG_PATH) then
		return false;
	end
	local strJson = tiros.file.Readfile(CITYMAP_CFG_PATH);
	local strTable = tiros.json.decode(strJson);

	for index = 1,tonumber(strTable["count"]) do
		local mapinfo = {}
		mapinfo = strTable["maplist"][tonumber(index)];
		if nil ~= mapinfo then
			print("yaoyt city getDownloadCitycode not nil" .. tonumber(mapinfo["citycode"]))
			table.insert(DownloadCity,tonumber(mapinfo["citycode"]))
		end
	end
	return DownloadCity;
end

--获取需要更新的下载完成的地图table
local function getUpdateMapTable()
	local UpdateCity= {};
	local newVersion = citymaplib.getNewVersion();
	if not filelib.fexist(CITYMAP_CFG_PATH) then
		return false;
	end
	local strJson = tiros.file.Readfile(CITYMAP_CFG_PATH);
	local strTable = tiros.json.decode(strJson);

	for index = 1,tonumber(strTable["count"]) do
		local mapinfo = {}
		mapinfo = strTable["maplist"][tonumber(index)];
		if nil ~= mapinfo then
			print("yaoyt city getDownloadCitycode not nil" .. tonumber(mapinfo["citycode"]))

			if tonumber(newVersion) > tonumber(mapinfo["date"]) then
				table.insert(UpdateCity,tonumber(mapinfo["citycode"]))
			end
		end
	end
	return UpdateCity;
end

--[[
 @描述:获取本地地图状态
 @return  table型 {"completecity":[110000,120000,...],"download":[412300,512300,...],"updates":[120000,...]}
--]]

local function getCompleteCity()
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
	
	return state;
end

--删除地图配置文件中的全部信息
local function clearDownloadCFGByPath(path)
	if not filelib.fexist(path) then
		return false;
	end
	local strJson = tiros.file.Readfile(path);
	local strTable = tiros.json.decode(strJson);
	strTable["maplist"] = {}
	strTable["count"] = 0;
	tiros.file.Writefile(path,tiros.json.encode(strTable),true);
end
local function clearDownloadCFG(path)
	clearDownloadCFGByPath(CITYMAP_CFG_PATH)
	clearDownloadCFGByPath(CITYBUILD_CFG_PATH)
end

local function init()

	if filelib.fdiskexist("fs1:/") == false then
		return;
	end

	local bExist = filelib.fexist(CITYMAP_CFG_PATH);
	if not bExist then
		local mapcfg = {};
		mapcfg["count"] = 0;
		mapcfg["maplist"] = {};
		mapcfg["noticeDate"] = 0;
		mapcfg["DownloadSizeDate"] = 0;
		tiros.file.Writefile(CITYMAP_CFG_PATH,tiros.json.encode(mapcfg),true);
	end

	local bExist = filelib.fexist(CITYBUILD_CFG_PATH);
	if not bExist then
		local mapcfg = {};
		mapcfg["count"] = 0;
		mapcfg["maplist"] = {};
		mapcfg["noticeDate"] = 0;
		mapcfg["DownloadSizeDate"] = 0;
		tiros.file.Writefile(CITYBUILD_CFG_PATH,tiros.json.encode(mapcfg),true);
	end
end

--将下载完成的一个城市的信息写入配置文件
local function saveCityCFG(path,citycode,date,cityname,ver)
	print("yaoyt city saveCityCFG in citycode" .. citycode)
	local bExist = filelib.fexist(path);
	if not bExist then
		init();
	end
	if not filelib.fexist(path) then
		return false;
	end
	local strJson = tiros.file.Readfile(path);
	local strTable = {};
	local index = 0;
	local map = {};
	
	strTable = tiros.json.decode(strJson);
	for i = 1,#strTable["maplist"] do
		local mapinfo = strTable["maplist"][i]
		if tonumber(mapinfo["citycode"]) == tonumber(citycode) then
			map = strTable["maplist"][i];
			index = i;
			break;
		end
	end
	
	if index == 0 then
		map["citycode"] = tostring(citycode);
		index = #strTable["maplist"] + 1;
	end
	map["date"] = tostring(date);
	if nil ~= cityname then
		map["cityname"] = tostring(cityname);
	end
	if nil ~= ver then
		map["ver"] = ver;
	end

	strTable["maplist"][index] = map;
	strTable["count"] = tostring(#strTable["maplist"]);
	tiros.file.Writefile(path,tiros.json.encode(strTable),true);
end

--将下载的config文件夹下的所有文件保存配置
local function saveDownloadCFGByPath(download_Folder,CFG_Path)
	print("yaoyt city saveDownloadCFGByPath in")
	local files = filelib.fenumstart(download_Folder, false);
	if nil == files then
		return;		
	end
	while true do
		local fileName = filelib.fenumnext(files);
		if nil ~= fileName then
			local fullName = download_Folder .. "/" .. fileName;
			local strJson = tiros.file.Readfile(fullName);
			local strTable = tiros.json.decode(strJson);
			saveCityCFG(CFG_Path,strTable["citycode"],strTable["date"],strTable["name"],strTable["ver"])
			filelib.fremove(fullName);
		else
			break;			
		end
	end
	filelib.fenumend(files);
end

--保存下载的地图信息到配置文件
local function saveDownloadCFG()
	print("yaoyt city saveDownloadCFG in")
	saveDownloadCFGByPath(CITYMAP_DOWNLOAD_CFG_FOLDER,CITYMAP_CFG_PATH);
	filelib.frmdir(CITYMAP_DOWNLOAD_CFG_FOLDER);
	saveDownloadCFGByPath(CITYBUILD_DOWNLOAD_CFG_FOLDER,CITYBUILD_CFG_PATH);
	filelib.frmdir(CITYBUILD_DOWNLOAD_CFG_FOLDER);
end

--保存owner信息到配置文件
local function saveOwnerCFG(citycode,date,cityname,ver)
	print("yaoyt city saveDownloadMapCFG in")
	saveCityCFG(CITYMAP_CFG_PATH,citycode,date,cityname,ver)
	saveCityCFG(CITYBUILD_CFG_PATH,citycode,date,cityname,ver)
end


--检查是否需要下载新的地图大小的数据
local function checkNeedDownloadMapSize()
	local newDate = citymaplib.getNewVersion();
	if not filelib.fexist(CITYMAP_CFG_PATH) then
		return true,tonumber(newDate);
	end
	local strJson = tiros.file.Readfile(CITYMAP_CFG_PATH);
	local strTable = tiros.json.decode(strJson);
	
	print(strTable["DownloadSizeDate"])
	if tonumber(newDate) > strTable["DownloadSizeDate"] then
		return true,tonumber(newDate);
	else
		return false,0;
	end
end

--记录下载的地图大小版本号
local function writeDownloadMapSizeVersion(version)
	if not filelib.fexist(CITYMAP_CFG_PATH) then
		return false;
	end
	local strJson = tiros.file.Readfile(CITYMAP_CFG_PATH);
	local strTable = tiros.json.decode(strJson);
	
	if tonumber(version) > strTable["DownloadSizeDate"] then
		strTable["DownloadSizeDate"] = tonumber(version);
		tiros.file.Writefile(CITYMAP_CFG_PATH,tiros.json.encode(strTable),true);
	end
end

local function saveNoticeDate(date)
	if not filelib.fexist(CITYMAP_CFG_PATH) then
		return false;
	end
	if not filelib.fexist(CITYBUILD_CFG_PATH) then
		return false;
	end
	local strJson = tiros.file.Readfile(CITYMAP_CFG_PATH);
	local strTable = tiros.json.decode(strJson);
	
	if tonumber(date) > strTable["noticeDate"] then
		strTable["noticeDate"] = tonumber(date);
		tiros.file.Writefile(CITYMAP_CFG_PATH,tiros.json.encode(strTable),true);
	end

	local strJson = tiros.file.Readfile(CITYBUILD_CFG_PATH);
	local strTable = tiros.json.decode(strJson);
	
	if tonumber(date) > strTable["noticeDate"] then
		strTable["noticeDate"] = tonumber(date);
		tiros.file.Writefile(CITYBUILD_CFG_PATH,tiros.json.encode(strTable),true);
	end
end

--接口table
local interface = {};

--[[
 @描述:获取本地地图状态
 @return  table型 {"completecity":[110000,120000,...],"download":[412300,512300,...]}
--]]
createmodule(interface,"GetCompleteCity",function ()
	return getCompleteCity();
end)

--[[
 @描述:删除全部地图 
 @return  bool型 成功返回true，失败返回false
--]]
createmodule(interface,"DeleteAllMap",function ()
	return deleteAllMap();
end)

--[[
 @描述:地图下载
 @param nCityCode number型参数，城市Code
 @param cbkname luaFun型参数，注册地图下载回调函数
 @return  bool型 成功返回true；失败返回false。 
--]]
createmodule(interface,"DownloadCityMap",function (nCityCode,cbkname)
	return downloadCityMap(nCityCode,cbkname,CMS_START,true);
end)

--[[
 @描述:暂停下载
 @param nCityCode number型参数，城市Code
 @return bool型 成功返回true；失败返回false。
--]]
createmodule(interface,"StopDownload",function (nCityCode)
	return stopDownload(nCityCode);
end)

--[[
 @描述:取消下载
 @param nCityCode number型参数，城市Code
 @return bool型 成功返回true；失败返回false。
--]]
createmodule(interface,"CancelDownload",function (nCityCode)
	return cancelDownload(nCityCode);
end)

--[[
 @描述:销毁citymap句柄函数接口
 @return 无
--]]
createmodule(interface,"DestroyDownload",function ()
	destroyDownload();
	hCityMap = nil;
end)

--[[
 @描述:将下载完成的地图的配置文件保存
 @return 无
--]]
createmodule(interface,"SaveDownloadCFG",function ()
	return saveDownloadCFG();
end)

--[[
 @描述:检查是否需要下载新的地图大小
 @return true/false：是否需要
--]]
createmodule(interface,"CheckNeedDownloadMapSize",function ()
	return checkNeedDownloadMapSize();
end)

--[[
 @描述:保存新下载的地图大小的版本号
--]]
createmodule(interface,"WriteDownloadMapSizeVersion",function (version)
	return writeDownloadMapSizeVersion(version);
end)

--[[
 @描述:保存新下载的地图大小文件路径
--]]
createmodule(interface,"GetMapSizeFilePath",function ()
	local path = "fs0:/res/logic/loadfile/mapsize"
	return path;
end)

createmodule(interface,"GetCityListFilePath",function ()
	local path = "fs0:/res/logic/citylist/citylist"
	return path;
end)

--[[
 @描述:检查已下载完成的地图是否需要弹框提示更新，一个版本只弹一次
 @mapDate 地图版本日期
 @BuildDate 建筑面版本日期
 @citycode 地图代码，可选，没有这个参数代表全部
 @return true：需要更新，false不需要更新
--]]
createmodule(interface,"CheckDownloadMapNotify",function (mapDate,BuildDate)
	return checkDownloadUpdate(mapDate,BuildDate,true);
end)

--[[
 @描述:检查已下载完成的地图是否需要更新
 @mapDate 地图版本日期
 @BuildDate 建筑面版本日期
 @citycode 地图代码，可选，没有这个参数代表全部
 @return true：需要更新，false不需要更新
--]]
createmodule(interface,"CheckDownloadMapUpdate",function ()
	local newDate = citymaplib.getNewVersion();
	return checkDownloadUpdate(newDate,newDate,false);
end)

--删除所有地图buliding信息
createmodule(interface,"ClearDownloadCFG",function ()
	clearDownloadCFG();
end)

--[[
 @描述:将owner一个城市地图信息写入配置文件
 @citycode 地图代码，比选
 @date 地图版本日期，比选
 @cityname 地图名字
 @ver 地图协议号
 @return 整数，citycode
--]]
createmodule(interface,"SaveOwnerCFG",function (citycode,date,cityname,ver)
	saveOwnerCFG(citycode,date,cityname,ver);
end)

createmodule(interface,"SaveNoticeDate",function (date)
	saveNoticeDate(date);
end)

--[[
 @描述:初始化配置文件，如果不存在配置文件创建配置文件
 @return 下载完成地图数目
--]]
createmodule(interface,"Init",function ()
	init()
end)


--地图下载功能接口库设置只读属性
tiros.citymap = readOnly(interface);

