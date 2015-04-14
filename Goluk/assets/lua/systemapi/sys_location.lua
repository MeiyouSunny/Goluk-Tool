--cellid接口封装
require"lua/systemapi/sys_namespace"
require"lua/tapi"
require"lua/systemapi/sys_handle"
require"lua/framework"
require"lua/http"
require"lua/timer"
require"lua/config"
require"lua/moduledata"
require"lua/positionreport"

local gt = {}

local configEngine = getmodule("config")
local httpEngine = getmodule("http")
local tapiEngine = getmodule("tapi")
local frameworkEngine = getmodule("framework")
local tmrobj = getmodule("timer")
local moduleobj = getmodule("moduledata")
local PSTreportobj = getmodule("PSTreport")
--保存最近一次定位信息
local lastGPSInfo = {};

local GPSFUNTYPE = 1;
local CELLIDFUNTYPE = 2;
local PLATGPSFUNTYPE = 3;
local LOCGIC_LOCATION_MODULE = 2; 
local LOCGIC_GET_LAST_GPS = 1;
-------------------------------------------------
-----------------cellid start---------------------------
---------------------------------------------------
--cellidlist：全局变量,用于存放正在使用的所有http句柄
--{"cellidhandle" = 
--		{ notify,
--			{mnc,mcc,lac,cid,mac,ip,name,signal,lon,lat}
--		} 
--}
createmodule(gt, "cellidlist",{})
--cellidweaklist：全局变量,用于存放所有cellid句柄的week表,week表中既包含正使用的句柄,也包含即将回收的句柄
createmodule(gt, "cellidweaklist",{})
setmetatable(gt.cellidweaklist,{__mode ="v" })
local rsp_data
local lastreuqiredata = {}

--检测缓存表 
local function checktable()
luaprint("---wc---checktable")
	local cellTable =getHandle(gt.cellidlist,"cellidhandle")
	local cachecount = table.maxn(cellTable.cachelist)	
	if cachecount >= 50 then
		table.remove(cellTable.cachelist,1)	
	end
end

--保存已请求过的基站wifi经纬度
local function cellid_saveRspinfototable(lon,lat,speed,course)
luaprint("---wc---saveRspinfototable")
	local cellTable = getHandle(gt.cellidlist,"cellidhandle")	
	if cellTable ~= nil then
		local temp = {}
		if lastreuqiredata.cell ~= nil then
			temp.cell = {}
			temp.cell.lac = lastreuqiredata.cell.lac
			temp.cell.cid = lastreuqiredata.cell.cid
			temp.cell.mcc = lastreuqiredata.cell.mcc
			temp.cell.mnc = lastreuqiredata.cell.mnc
			temp.cell.signal = lastreuqiredata.cell.signal
		end

		if lastreuqiredata.wifi ~= nil then
			temp.wifi = {}
			temp.wifi.ssid = lastreuqiredata.wifi.ssid
			temp.wifi.mac = lastreuqiredata.wifi.mac
			temp.wifi.su = lastreuqiredata.wifi.su
			temp.wifi.ss = lastreuqiredata.wifi.ss
		end
		temp.lon = lon
		temp.lat = lat
		temp.speed = speed
		temp.course = course

		lastreuqiredata.lon = lon
		lastreuqiredata.lat = lat
		lastreuqiredata.speed = speed
		lastreuqiredata.course = course
		checktable()
		
		table.insert(cellTable.cachelist, temp)	
luaprint("---wc---saveRspinfototable-end")
	end
end


--判断是否已存在该基站wifi位置
--返回值 1，wifi有，2，基站有，3lon,4lat,5speed,6course
local function isexistintable()
luaprint("---wc---isexistintable-1")
	local cellTable = getHandle(gt.cellidlist,"cellidhandle")
	if cellTable.cachelist == nil then
		return
	end
	local bWifi = false
	local bCell = false
luaprint("---wc---isexistintable-2",cellTable.cachelist)

	for k,v in pairs(cellTable.cachelist) do	
		if v.cell ~= nil and lastreuqiredata.cell ~= nil then
			if v.cell.lac == lastreuqiredata.cell.lac and v.cell.cid == lastreuqiredata.cell.cid  then
				bCell = true
			end
		end
		
		if v.wifi ~= nil and lastreuqiredata.wifi ~= nil then
			if v.wifi.ssid == lastreuqiredata.wifi.ssid and v.wifi.mac == lastreuqiredata.wifi.mac then
				bWifi = true
			end
		end
		if bWifi or bCell then
			luaprint("---wc---isexistintable-end",bWifi, bCell, v.lon, v.lat, v.speed, v.course)
			return bWifi, bCell, v.lon, v.lat, v.speed, v.course
		end
	end	
	return false,false,nil,nil,nil,nil
end

local function cellid_timerstart()
	luaprint("cellid_timerstart1")
	if tmrobj.timerisbusy("cellidhandle") then
luaprint("cellid_timerstart2")
		tmrobj.timerabort("cellidhandle")
	end
luaprint("cellid_timerstart3")
	tmrobj.timerstartforlua("cellidhandle",10000,gt.cellid_timerCB)
luaprint("cellid_timerstart-ok")
end


--发送位置信息
local function cellid_sendposition(lon,lat,speed,course)	
luaprint("---wc---sendposition-1",lon,lat,speed,course)	
	local cellTable =getHandle(gt.cellidlist,"cellidhandle")
luaprint("---wc---sendposition-2",cellTable,cellTable.notify)
	if cellTable.notify ~= nil then 
		luaprint("---wc---sendposition-3")	
		cellTable.notify(lon,lat,speed,course)	
	end
luaprint("---wc---sendposition-4")
	local bWifi, bCell = isexistintable()
	--如果缓存没有则存储
	if not bCell and lastreuqiredata.cell and lastreuqiredata.cell.lac and lastreuqiredata.cell.cid then
		cellid_saveRspinfototable(lon,lat,speed,course)
	elseif not bWifi and lastreuqiredata.wifi and lastreuqiredata.wifi.ssid and lastreuqiredata.wifi.mac then
		cellid_saveRspinfototable(lon,lat,speed,course)
	end
	

luaprint("---wc---sendposition-end")	
end

--[[
//请求位置
{
"accordver":"4",//协议版本
"appver":"101101010101",//软件版本
“ef”:0,//坐标加密 
"localstyle":"1",//定位方式1系统默认方法，2CID + RTT方法
"respond":"1"//响应类型
//基站数据  
"cellids":[{"mcc":"231","mnc":"232","lac":"1231","cid":"4433","signal":"1"},
{"mcc":"231","mnc":"232","lac":"1231","cid":"4433","signal":"1"}],
//wifi数据
“wifis”:[{"mcc":460,"ss":0,"su":25380,"ssid":22151,"age":0,“channel”：0，“snt”：0}]
{"ssid":"name","mac":"1234567890","ss":"2","su":"1"}]
//驴博士
"locate_hex":"strlocate_hex"
}
]]
local function cellid_encode(version,celllist, wifilist)
	luaprint("cellid_encode -start")
	local jansonT = {
	accordver = 4,
	appver = version,
	ef = 0,
	localstyle = 1,
	respond = 1	
	}
	if celllist ~= nil then
		jansonT.cellids = celllist
	end
	if wifilist ~= nil then
		jansonT.wifis = wifilist
	end
	local result = tiros.json.encode(jansonT)
	return result
end
--[[
//返回定位结果
{
"accordver":"4",//协议版本
"datatype":"18",
"lon":"12312122",
"lat":"44112312",
"altitude":"412",
"raidus":"232"
}
//返回错误信息
{
"accordver":"4",//协议版本
"datatype":"6",
"errinfo":"客户端请求参数出现错误"
}
--]]
local function cellid_decode(jsonstr)	
	luaprint("cellid_decode-start")
	local jsonT = tiros.json.decode(jsonstr)
	
	if jsonT == nil  or type(jsonT) ~= "table" then
		return nil	
	end 

	local accordver = jsonT.accordver;
	luaprint("cellid_decode-accordver",accordver)
	if accordver == 1 then		
		local datatype = jsonT.datatype;		
		if datatype == 1 then --返回定位结果
		    	local lon = jsonT.lon;
		    	local lat = jsonT.lat;
		    	local altitude = jsonT.altitude;
		    	local raidus = jsonT.raidus;
			luaprint("cellid_decode",lon,lat,altitude,raidus)
			return lon,lat,altitude,raidus		
		end
	end    	
	luaprint("cellid_decode-end")
	return nil
end
--解析下行数据
local function cellid_decodeRsp(json)		
	luaprint("cellid_decodeRsp -start")
	local lon,lat,speed,course = cellid_decode(json)	
	return lon,lat,speed,course
end


--获取基站信息
local function getbaselocationinfo()
luaprint("---wc---getbaselocationinfo-1")
	local count = tapiEngine.tapigetbscount()
	local celllist ={}	
	local cell={}

	if count > 0 then
		cell.lac, cell.cid, cell.mcc, cell.mnc, cell.signal, cell.lat, cell.lon= tapiEngine.tapigetbsbyindex(0)

		if cell.cid == nil or cell.cid == 0 then
			cell = nil
			celllist = nil		
		else	
			table.insert(celllist,cell)	
		end
	else
		cell = nil
		celllist = nil
	end

	local wifi = {}
	local wifilist = {}
	local tempip
	wifi.ssid, wifi.mac,tempip, wifi.ss = tapiEngine.tapigetconnwifiinfo()
	wifi.su = 0
	luaprint("wifi.mac",wifi.mac)
	if wifi.mac == nil  or string.len(wifi.mac) < 2  then
		wifi = nil 
		wifilist = nil
	else
		table.insert(wifilist,wifi)	
	end
	--wifi与cell均为空值返回
	if wifilist == nil and celllist == nil then
		luaprint("---wc---getbaselocationinfo-3")
		return
	end	
	 
	if cell ~= nil and cell.lon ~= 0 and cell.lat ~= 0 then
		local cellTable =getHandle(gt.cellidlist,"cellidhandle")
		if cellTable ~= nil and cellTable.collectnotify ~= nil then
			cellTable.collectnotify(cell.lac, cell.cid, cell.mcc, cell.mnc, cell.signal, cell.lat, cell.lon)
		end
	end	

	luaprint("---wc---getbaselocationinfo-4")	
	local sotfver = moduleobj.moduledata_get('framework', 'version')
	luaprint("---wc---getbaselocationinfo-5",sotfver)
	return cell, wifi, cellid_encode(sotfver , celllist, wifilist)
end


local function cellid_require()
	local cellTable =getHandle(gt.cellidlist,"cellidhandle");
	if cellTable == nil then 
		return 
	end
luaprint("---wc---cellid_require-1")
	local cell, wifi, data = getbaselocationinfo()
	if cell == nil and wifi == nil then
		return
	end
	if  data == nil then
		return 
	end
	luaprint("---wc---cellid_require-2",cell,wifi,data)

	--如果是C网直接发送
	if cell ~= nil and cell.lon >0 and cell.lat > 0 then
		--保存基站到上次请求		
		lastreuqiredata.cell = cell
		lastreuqiredata.wifi = wifi
		lastreuqiredata.isRecv = true
		--发送位置
		luaprint("---wc---cellid_require-2")
		cellid_sendposition(cell.lon,cell.lat,0,100)
		return
	end
	-------------------	
	--如果不是C网，则判断
	--1，基站与wifi与上次请求的相同
	--2，基站与上次相同，wifi=nil
	--3，wifi与上次相同，但cell=nil
	local bCell = false
	local bWifi = false
	if cell ~= nil and lastreuqiredata.cell ~= nil then
		if cell.lac == lastreuqiredata.cell.lac and cell.cid == lastreuqiredata.cell.cid  then
			bCell = true
		end
	end
	
	if wifi ~= nil and lastreuqiredata.wifi ~= nil then
		if wifi.ssid == lastreuqiredata.wifi.ssid and wifi.mac == lastreuqiredata.wifi.mac then
			bWifi = true
		end
	end
luaprint("---wc---cellid_require-3", bCell , bWifi)
	--相同时能取到经纬度则发送，否则返回
	if bCell or bWifi then
		if lastreuqiredata.lat ~= nil and lastreuqiredata.lon ~= nil then
			cellid_sendposition(lastreuqiredata.lon, lastreuqiredata.lat, lastreuqiredata.speed, lastreuqiredata.course)
		else
			if lastreuqiredata.isRecv == false then
				luaprint("---wc---cellid_require-3-isRecv")
				return 
			end
		end		
	end
	----------------------------
	--不同时先保存请求的基站信息，
	lastreuqiredata.cell = cell
	lastreuqiredata.wifi = wifi
	lastreuqiredata.isRecv = false
	-- 分三种情况
	--1，缓存中有cell-wifi
	--2，缓存中有该cell
	--3，缓存中有该wifi
	local bwifiexist, bcellexist,lon, lat, speed, course = isexistintable()	
luaprint("---wc---cellid_require-44",bwifiexist, bcellexist,lon, lat, speed, course)
	--有的话取缓存经纬度发送
	if bwifiexist or bcellexist then
		lastreuqiredata.isRecv = true
		cellid_sendposition(lon, lat, speed, course)
luaprint("---wc---cellid_require-5")
		return 
	end 

	--没有的话则重复请求
	------------------------------		
	local postdata = "nps="..data
	rsp_data = false
luaprint("---wc---cellid_require-ok", cellTable.url, postdata)
        httpEngine.httpsendforlua("cdc_client","lcation","cellidhandle",cellTable.url,gt.cellid_httpEvent,postdata,"Content-Type:application/x-www-form-urlencoded")
end

local function cellid_start(notify,url, collectnotify)
	local cellTable =getHandle(gt.cellidweaklist,"cellidhandle");
	luaprint("cellid_create-start",url)
	if cellTable == nil then
		luaprint("cellid_create-ok")
		cellTable = {};	
		cellTable.notify = notify		
		cellTable.cachelist = {}			   	
	end	
luaprint("-------- cellid_start",collectnotify)
	cellTable.collectnotify = collectnotify
	cellTable.url = url
	registerHandle(gt.cellidlist,gt.cellidweaklist,"cellidhandle",cellTable);

	cellid_require();
	cellid_timerstart()
	luaprint("cellid_create-end")
end



local function cellid_cancel()
	luaprint("cellid_cancel-start")	
	local cellTable =getHandle(gt.cellidlist,"cellidhandle")
	if cellTable ~= nil then
		if tmrobj.timerisbusy("cellidhandle") then
			tmrobj.timerabort("cellidhandle")
		end
	   	httpEngine.httpabort("cellidhandle")
		luaprint("cellid_cancel-ok")
	end
	luaprint("cellid_cancel-end")
end

local function cellid_destroy()		
	luaprint("cellid_destroy-start")
	local cellTable =getHandle(gt.cellidlist,"cellidhandle")
	if cellTable ~= nil then
		luaprint("cellid_destroy-not nil")
	   	releaseHandle(gt.cellidlist,"cellidhandle")
		if tmrobj.timerisbusy("cellidhandle") then
			tmrobj.timerabort("cellidhandle")
		end
		httpEngine.httpabort("cellidhandle")
		luaprint("cellid_destroy-ok")
	end
	luaprint("cellid_destroy-end")
end

--timer回调
createmodule(gt,"cellid_timerCB",function(handletype)
luaprint("cellid_timerCB")
	cellid_require()
	cellid_timerstart()
end)

createmodule(gt,"cellid_httpEvent",function(htype,event,param1, param2)	
luaprint("cellid_httpEvent--wccccc",htype,event,param1, param2)	

local e = "event"..event
	if e=="event1" then 
		rsp_data = false		
	elseif e=="event2" then 		
	elseif e== "event3" then 						
		if rsp_data then			
			rsp_data = rsp_data..param2
		else			
		 	rsp_data = param2
		end 		
	elseif e == "event4" then 	
		local lon,lat,speed,course= cellid_decodeRsp(rsp_data)
		
		if lon ~= 0 and lat ~= 0 then
			lastreuqiredata.isResp = true;
			cellid_sendposition(lon,lat,speed,course)
		else
			lastreuqiredata = {};
		end		
	elseif e == "event5" then 
		lastreuqiredata = {};
	end	
end)
-------------------------------------------------
-----------------cellid end---------------------------
---------------------------------------------------


-------------------------------------------------
-----------------gps start---------------------------
---------------------------------------------------
--gps接口封装
----目前主要对外gpsstart及gpsabort接口

--gpslist：用于存放正在使用的gtype和cbkname
createmodule(gt, "_gpslist" , {})
--GpsStart：用于存放gps的userdata
createmodule(gt, "_gpsHandle" , nil)
local gps_lon = nil
local gps_lat = nil
local gps_speed = nil
local gps_course = nil
--开始gps start函数接口
--gtable:table型参数，用于标识gps描述
--cbkname:会动态依据不同的ntype来确定类型(lua：function型，js：string型，c：integer型)
--gtype:string型参数，js端用于标识该gps句柄的唯一标识符
--ntype:integer型参数，用于标识该回调函数类型（lua：0，js：1，c：2）
--nuser：integer型参数，可为nil，c端注册的调用者参数地址
--输出：成功返回true，失败返回false
local function gpsstart(stype,ntype,cbkname,nuser)
	if stype == nil or cbkname ==nil then 
		return false;
	end	

	local ltable =getHandle(gt._gpslist,stype);
	
	if ltable== nil then
			ltable= {};
	end
	registerHandle(gt._gpslist,nil,stype,ltable);

	ltable[1]=ntype;
	if ntype == 0 then		--lua脚本注册回调函数
		ltable[2] = cbkname;
	elseif ntype == 1 then		--js注册回调函数
		ltable[3] = cbkname;
	else				--c回调函数地址
		ltable[4] = cbkname;
		ltable[5] = nuser;		
	end
luaprint("gpsstart")
	if  gt._gpsHandle == nil  then
luaprint("gpsstart-wccccc--1")
		gt._gpsHandle = gpslib.start("sys_GpsEvnet","SYS_GPS");
luaprint("gpsstart---2")
	end
	return true
end

--lua层gps事件回调处理函数：
DeclareGlobal("sys_GpsEvnet",function (stype,lon,lat,speed,course)
luaprint("sys_GpsEvnet--wccccc",lon,lat)
--	if gps_lon == lon and 	gps_lat == lat and gps_speed == speed and gps_course == course then 
--		luaprint("sys_GpsEvnet--wccccc",lon,lat)
--		return 	
--	end
	if 0 == lon and 0 == lat and 0 == speed and 0 == course then 
		luaprint("sys_GpsEvnet",lon,lat)
		return 	
	end
	gps_lon = lon 
 	gps_lat = lat 
	gps_speed = speed 
	gps_course = course
	for k,v in pairs(gt._gpslist) do
			local selftype = k
				local ltable = getHandle(gt._gpslist,selftype)
				if ltable[1] == 0  and ltable[2] ~= nil then
							ltable[2](selftype,lon,lat,speed,course);
				elseif ltable[1] == 1  and ltable[3] ~= nil then      --js回调
					local s;
					s = string.format("%s('%s',%u,%u, %u, %u )",ltable[3] ,selftype,lon,lat,speed,course);
					commlib.calljavascript(s);
				else     --c回调
					if ltable[4] ~= nil then
						commlib.gpsnotify(ltable[4],ltable[5],lon,lat,speed,course);
					end
				end				
	end
end)

--接口table
--对外声明停止GPS定位函数接口
--gtype:string型参数，js端用于标识该gps句柄的唯一标识符
--输出：无
local function gpsstop(stype)
	releaseHandle(gt._gpslist,stype)
	local bEmpty = true
	for k,v in pairs(gt._gpslist) do
			bEmpty = false
	end
	if  bEmpty == true then
		luaprint("gpsstop-gpslib.stop")
--[[
		if gt._gpsHandle then
			gpslib.stop(gt._gpsHandle)
			gt._gpsHandle = nil
		end
]]--
	end
end

--对外声明判断GPS是否已经开始定位函数接口
--输出：bool型，已经开始定时:true,没有开始定时:false
local function gpsisbusy()
	if gt._gpsHandle ~= nil then
		return gpslib.isbusy(gt._gpsHandle)
	else
		return false;
	end
end

--对外声明销毁GPS定位函数接口
--gtype:string型参数，js端用于标识该gps句柄的唯一标识符
--输出：无
local function gpsabort()
			--for k,v in pairs(gt._gpslist) do
			--	releaseHandle(gt._gpslist,k)
			--end
			luaprint("gpsabort-gpslib.stop")
	if gt._gpsHandle ~= nil then
			gpslib.stop(gt._gpsHandle)
			gt._gpsHandle = nil
	end
end

--对外声明lua层调用gpsstart函数接口
--gtype:string型参数，js端用于标识该gps的唯一标识符
--cbkname：string型参数，lua端注册gps的回调函数名称
--输出：成功返回true，失败返回false
local function gpsstartforlua(stype,cbkname)
	return gpsstart(stype,0,cbkname,nil);
end

-------------------------------------------
-------------------gps end-----------------
------------------------------------------


-------------------------------------------
-------------------platgps start-----------------
------------------------------------------
--platgps接口封装
--


--platgpslist：全局变量，用于存放正在使用的所有platgps句柄
createmodule(gt,"platgpslist",{})
--platgpsweaklist：全局变量，用于存放所有platgps句柄的week表，week表中既包含正使用的句柄，也包含即将回收的句柄
createmodule(gt,"platgpsweaklist",{})
setmetatable(gt.platgpsweaklist,{__mode ="v" })
local platgps_lon = nil
local platgps_lat = nil
--[[
paltgps全局变量table，协定
ptable = {}
0: platgps句柄
1：调用方类型：0：lua，1：js， 2：c
2：lua回调函数地址
3: js注册回调函数名称
4：c回调函数指针地址
5：c调用者传输数据地址
--]]

--创建platgps句柄函数接口
--ptype：string型参数，用于唯一标识该platgps句柄
----输出：实际创建的platgps句柄
local function platgpscreate(ptype)
	local ptable =getHandle(gt.platgpsweaklist,ptype);
	if ptable == nil then
		ptable = {};
	   	ptable[0] = platgpslib.create();
	end
	registerHandle(gt.platgpslist,gt.platgpsweaklist,ptype,ptable);	
	return ptable	
end



--注册platgps回调函数接口
--ptable:table型参数，用于标识platgps句柄描述
--cbkname:会动态依据不同的ntype来确定类型(lua：function型，js：string型，c：integer型)
--ptype:string型参数，js端用于标识该platgps句柄的唯一标识符
--ntype:integer型参数，用于标识该回调函数类型（lua：0，js：1，c：2）
--输出：无
local function platgpsnotify(ptable,cbkname,ptype,ntype,nuser)
	ptable[1]=ntype;
	if ntype == 0 then		--lua脚本注册回调函数
		ptable[2] = cbkname;
	elseif ntype == 1 then		--js注册回调函数
		ptable[3] = cbkname;
	else				--c回调函数地址
		ptable[4] = cbkname;
		ptable[5] = nuser;
	end

	platgpslib.regist(ptable[0],"sys_PlatgpsEvnet",ptype)
end

--开始start platgps函数接口
--ptable:table型参数，用于标识platgps句柄描述
--cbkname:会动态依据不同的ntype来确定类型(lua：function型，js：string型，c：integer型)
--ptype:string型参数，js端用于标识该platgps句柄的唯一标识符
--ntype:integer型参数，用于标识该回调函数类型（lua：0，js：1，c：2）
--输出：成功返回true，失败返回false
local function platgpsstart(ptype, cbkname,ntype,nuser)
luaprint("platgpsstart----0")
	if ptype == nil then 
		return false;
	end
	local ptable = platgpscreate(ptype);	
	if ptable == nil then	
	   	return false;
	end
luaprint("platgpsstart--wccccc--1")
	--lua code notify
	platgpsnotify(ptable,cbkname,ptype,ntype,nuser);
	platgpslib.start(ptable[0]);
luaprint("platgpsstart----end")
	return true
end


--对外声明取消已经启动platgps句柄函数接口
--ptype：string型参数，用于唯一标识该platgps句柄
--输出：无
local function platgpsstop(ptype)
luaprint("platgpsstop")
	local ptable =getHandle(gt.platgpslist,ptype)
	if ptable ~= nil then
	   	platgpslib.stop(ptable[0]);
	end	
end

--销毁platgps句柄函数接口：ptype为string型参数，用于唯一标识该platgps句柄
--该函数并没有立即销毁platgps句柄，而是等到下一个回收cd之后才会彻底销毁
--输出：无
local function platgpsdestroy(ptype)
luaprint("platgpsdestroy")
	platgpsstop(ptype)
	releaseHandle(gt.platgpslist,ptype)		
end

--对外声明终止platgps请求函数接口
--ptype:string型参数，js端用于标识该platgps句柄的唯一标识符
--输出：无
local function platgpsabort(ptype)
luaprint("platgpsabort")
	--platgpsdestroy(ptype)
	platgpsstop(ptype)
end

--对外声明判断platgps是否已经开始定位接口
--ptype：string型参数，用于唯一标识该platgps句柄
--返回 定位已经启动则返回true,否则返回false
local function platgpsisbusy(ptype)
	local ptable =getHandle(gt.platgpslist,ptype)
	if ptable ~= nil then
	   	return platgpslib.isbusy(ptable[0])
	end	
	return nil
end

--对外声明lua层调用platgps start函数接口
--ptype:string型参数，js端用于标识该platgps句柄的唯一标识符
--cbkname：string型参数，js端注册platgps的回调函数名称
--输出：成功返回true，失败返回false
local function platgpsstartforlua(ptype,cbkname)
	return platgpsstart(ptype,cbkname,0,nil);
end

--lua层platgps事件回调处理函数：
DeclareGlobal("sys_PlatgpsEvnet",function (ptype,lon,lat,accuracy)
luaprint("sys_PlatgpsEvnet---wccccc",lon,lat)
	if 0 == lon and 0 == lat  then 
		luaprint("sys_PlatgpsEvnet",lon,lat)
		return 	
	end
	if platgps_lon == lon and platgps_lat == lat  then 
		luaprint("sys_PlatgpsEvnet---wccccc",lon,lat)
		return 	
	end
	platgps_lon = lon 
 	platgps_lat = lat 	
luaprint("sys_PlatgpsEvnet----1",ptype,lon,lat,accuracy)
	local ptable =getHandle(gt.platgpslist,ptype)
luaprint("sys_PlatgpsEvnet----2",ptable)
	--platgpsdestroy(ptype);
	if ptable ~= nil then
		if ptable[1] == 0 then
			if ptable[2] ~= nil then
				ptable[2](ptype,lon,lat,accuracy);
			end
		elseif ptable[1] == 1 then
		--js回调
			if ptable[3] ~= nil then
				local s;
				s = string.format("%s(\"%s\",%u, %u, %u )",Ptable[3] ,Ptype,lon,lat,accuracy);
				commlib.calljavascript(s);
			end	
		elseif ptable[1] == 2 then
		--c回调
			if ptable[4] ~= nil then
				commlib.platgpsnotify(ptable[4],ptype,ptable[5],lon,lat,accuracy);
			end
		end

	end
end)

----------------------------------------------------------
----------------------platgps end------------------------
-------------------------------------------------------


--------------------------------------------------------------
-----------------------location start------------------------
------------------------------------------------------------------

--location接口封装
--目前主要对外httpsend及httpabort接口


--t.locationlist：全局变量，用于存放正在使用的所有location句柄
--{"locationkit" = 
--		{ mode,gpshandle,cellhandle,timehandle,timecount,
--			{func= cbfunc,user=user}
--		} 
--}
createmodule(gt,"locationlist",{})
--t.locationweaklist：全局变量，用于存放所有location句柄的week表，week表中既包含正使用的句柄，也包含即将回收的句柄
createmodule(gt,"locationweaklist",{})
setmetatable(gt.locationweaklist,{__mode ="v" })



local lastpos = {}
local RES_STR_PST_URL = 1201 --资源文件中编号
local RES_FILE_PATH = "fs0:/res/api/api.rs" --资源文件地址路径--获取URL:服务器地址及端口号 目前使用测试服务器及端口
--local cellidurl = frameworkEngine.getUrlFromResource(RES_FILE_PATH, RES_STR_PST_URL)
local cellidurl = ""
local lklocationtime = 1000*60

luaprint("cellidurl= "..cellidurl)



local function lkgpsstop()
luaprint("lkgpsstop-start")
	local bExist = gpsisbusy()
	if bExist then
		luaprint("lkgpsstop-ok")
		gpsstop("lkhandle")
	end
luaprint("lkgpsstop-end")
end

local function savelastpos(lon,lat,speed,course,altitude,raidus)
	if lastpos.lon == nil and lastpos.lat == nil then
		--第一次定位成功请求城市信息		
		--PSTreportobj.positionreportforlua("locate",lon,lat)
		tiros.PSTreport.positionreportforlua("locate",lon-lon%1,lat-lat%1)
	end
	lastpos.lon=lon
	lastpos.lat=lat
	lastpos.speed=speed
	lastpos.course=course
	lastpos.altitude=altitude
	lastpos.raidus=raidus	
end

local function lksendmessage(stype,lon,lat,speed,course,altitude,raidus,func,user,funtype)
			print("logic gps lksendmessage in")	
    	local elon ;
    	local elat ;
    	elon,elat = encryptiongpslib.encryptiongps(lon, lat);
	elon = math.ceil(elon)
	elat = math.ceil(elat)
print("logic gps lksendmessage----wccccc",lon,lat,speed,course,altitude,raidus)
print("logic gps lksendmessage---elon-wccccc",elon,elat,speed,course,altitude,raidus)
	if func ==nil and user ==nil then		
	elseif user then
		--call c func
        print("logic gps lksendmessage----kongxiangyu")
		commlib.locationnotify(func,user,elon,elat,speed,course,altitude,raidus,funtype);
	elseif type(func) == "string" then
		--JS			
	else
		--lua
		func(stype,elon,elat,speed,course,altitude,raidus,funtype,lon,lat)
	end	
	
	moduleobj.moduledata_set("logic","lon",elon)
	moduleobj.moduledata_set("logic","lat",elat)
	moduleobj.moduledata_set("logic","speed",speed)
	moduleobj.moduledata_set("logic","course",course)
	moduleobj.moduledata_set("logic","altitude",altitude)
	moduleobj.moduledata_set("logic","raidus",raidus)
end

--timer相关 start
local function lktimerstart(htype)
luaprint("lktimerstart",htype)	
	if tmrobj.timerisbusy(htype) then		
		tmrobj.timerabort(htype)
	end
luaprint("lktimerstart -1 ")
	tmrobj.timerstartforlua(htype,lklocationtime,gt.lktimerCB)
luaprint("lktimerstart -ok ")
end


--定位接口控制函数-start
local function lkplatgpsstart()
luaprint("lkplatgpsstart-start---1")
	local bExist = platgpsisbusy("lkplathandle")
luaprint("lkplatgpsstart-start---111")
	if not bExist then	
		luaprint("lkplatgpsstart-start---2")	
		platgpsstartforlua("lkplathandle",gt.lkplatgpsCB)
		luaprint("lkplatgpsstart-3")
	end
luaprint("lkplatgpsstart-end")
end

local function lkplatgpsstop()
luaprint("lkplatgpsstop-start")
	local bExist = platgpsisbusy("lkplathandle")
	if bExist then
		luaprint("lkplatgpsstop-ok")
		platgpsabort("lkplathandle")		
	end
luaprint("lkplatgpsstop-end")
end

local function lkgpsstart()
luaprint("lkgpsstart-start")
	local bExist = gpsisbusy()
luaprint("lkgpsstart-start---1")
	if not bExist then
luaprint("lkgpsstart-start---2")
		gpsstartforlua("lkhandle",gt.lkgpsCB)
		luaprint("lkgpsstart-ok")
	end
luaprint("lkgpsstart-end")
end

local function lkcellstart()	
	local lkTable =getHandle(gt.locationlist,"lkhandle")	
	
	cellid_start(gt.lkcellidCB, cellidurl, lkTable.cdmanotify)
	--"http://119.254.82.237:8080/nps_location")

end

local function lkcellstop()
	cellid_cancel()
luaprint("lkcellstop")
end
--定位接口控制函数-end


--timer相关 end


-----更改定位模式
local function lkchangemode(mode)
luaprint("lkchangemode -start ",mode)
	local lkTable =getHandle(gt.locationlist,"lkhandle")
	if lkTable ~= nil then
		lkTable.mode = mode	
	
		if mode == 1 then	
			tmrobj.timerabort("lkgps")
			tmrobj.timerabort("lkplatgps")
			luaprint("lkchangemode -1-1")
			lkgpsstart()
			luaprint("lkchangemode -1-2")
			lkcellstop()
			luaprint("lkchangemode -1-3")
			lkplatgpsstop()
			luaprint("lkchangemode -1-4")
		elseif mode == 2 then
			luaprint("lkchangemode -2-1")
			lkcellstart()
			luaprint("lkchangemode -2-2")
			--lkgpsstop()
			luaprint("lkchangemode -2-3")
			--lkplatgpsstart()
			luaprint("lkchangemode -2-4")
		elseif mode == 3 then
			luaprint("lkchangemode -3-1")
			lkgpsstart()
			luaprint("lkchangemode -3-2")
			lkplatgpsstart()
			luaprint("lkchangemode -3-3")			
			lkcellstart()
			luaprint("lkchangemode -3-4")
		end		
	end			
luaprint("lkchangemode -end")
end
local function lkcreate()
luaprint("lkcreate -start")
	local lkTable =getHandle(gt.locationweaklist,"lkhandle");
	if lkTable == nil then
		lkTable = {};			
luaprint("lkcreate -registerHandle")
	end	
	registerHandle(gt.locationlist,gt.locationweaklist,"lkhandle",lkTable);
luaprint("lkcreate -end")
end

--timer回调
createmodule(gt,"lktimerCB",function(handletype)

luaprint("lktimerCB-start")
	if handletype == "lkgps" then 
		luaprint("lktimerCB-start,,lkgps")
		lkcellstart()	
		lkplatgpsstart()
	elseif handletype == "lkplatgps" then 
		luaprint("lktimerCB-start,,lkplatgps")
		lkcellstart()	
	end	
end)

--GPS回调
createmodule(gt,"lkgpsCB",function(stype,lon,lat,speed,course)
	luaprint("==1==wccccc==lkgpsCB",lon,lat,speed,course,0,0)	
	--savelastpos(lon,lat,speed,course)
	local handletype = "lkhandle"	
	----------------------
	local lkTable =getHandle(gt.locationlist,handletype)
	luaprint("==2====lkgpsCB")	
	if lkTable == nil then
		return
	end
	if lkTable ~= nil and lkTable.gpsnotify ~= nil then
		--lkTable.gpsnotify(lon,lat,speed,course); --基站采集
	end

	luaprint("==4====lkgpsCB",lkTable.mode)
	if lkTable.mode == 3 then
		luaprint("===5=======lkgpsCB end")
		--lkcellstop()
		--lkplatgpsstop()		
		--lktimerstart("lkgps")
	end

	if type(lkTable.monitor) == "table" then
		for k,v in pairs(lkTable.monitor) do
		luaprint("===77=======lkgpsCB end",lkTable.monitor[k].func,lkTable.monitor[k].user)
			lksendmessage(stype,lon,lat,speed,course,0,0,lkTable.monitor[k].func,lkTable.monitor[k].user,GPSFUNTYPE)	
		end
	end
	luaprint("==6====lkgpsCB")
end)
--platgps cb func
createmodule(gt,"lkplatgpsCB",function(ptype,lon,lat,accuracy)
print("logic gps lkplatgpsCB in ")
	print("logic gps lkplatgpsCB",lon,lat,accuracy)
	--savelastpos(lon,lat,0,0,0,accuracy)
	local handletype = "lkhandle"
	----------------------
	local lkTable =getHandle(gt.locationlist,handletype)
	if lkTable == nil then
		return
	end
	print("logic gps lkplatgpsCB 22")
	if lkTable.mode == 2 or lkTable.mode == 3 then
		--lkcellstop()				
		--lktimerstart("lkplatgps")
	end
	
	print("logic gps lkplatgpsCB 33")
	if type(lkTable.monitor) == "table" then
		for k,v in pairs(lkTable.monitor) do
			print("logic gps lkplatgpsCB 44")
			lksendmessage(nil,lon,lat,0,0,0,accuracy,lkTable.monitor[k].func,lkTable.monitor[k].user,PLATGPSFUNTYPE)	
		end
	end	
	---------------
	luaprint("=====22======lkplatgpsCB end")
	
end)

--cell callback
createmodule(gt,"lkcellidCB",function(lon,lat,altitude,raidus)
	luaprint("=====1==wccccc====lkcellidCB",lon,lat,altitude,raidus)
	--savelastpos(lon*3.6,lat*3.6,0,0,altitude,raidus)
	local handletype = "lkhandle"
	local lkTable =getHandle(gt.locationlist,handletype)	
	luaprint("=====12======lkcell")	
	if lkTable == nil then
		luaprint("=====2======lkcell")
		return
	end
	luaprint("=====22======lkcell",lkTable.monitor)
 
	
	if type(lkTable.monitor) == "table" then
		luaprint("=====3======lkcell")
		for k,v in pairs(lkTable.monitor) do
			luaprint("=====4======lkcell")
			lksendmessage(nil,lon,lat,0,0,altitude,raidus,lkTable.monitor[k].func,lkTable.monitor[k].user,CELLIDFUNTYPE)	
		end
	end
	luaprint("=====44======lkcell")		
end)


--对外声明
------添加回调
local function lkaddmonitor(cbfunc,user)
	local lkTable =getHandle(gt.locationlist,"lkhandle")
	local T = {func= cbfunc,user=user}
luaprint("lkaddmonitor",lkTable,cbfunc,user)
	if lkTable == nil then 
		return false	
	end
	if lkTable.monitor == nil then
		luaprint("lkaddmonitor1")
		lkTable.monitor={}
	end
	for k,v in pairs(lkTable.monitor) do
		if type(v)== "table" then								
			if v.func == cbfunc and v.user == user then			
				return
			end
		end
	end
	table.insert(lkTable.monitor,T)	
luaprint("monitorcount=",table.maxn(lkTable.monitor))		
end

--存储经纬度函数接口
--lon,integer型参数
--lat,integer型参数
--输出：无
local function lklastlocation_set(t)
	if t ~= nil and type(t) == "table" then	
		if t.lon~= nil and t.lat~= nil and t.speed~= nil and t.course~= nil and t.altitude~= nil and t.raidus~= nil then	
			local elon ;
			local elat ;
			elon,elat = encryptiongpslib.encryptiongps(t.lon, t.lat);
			elon = math.ceil(elon)
			elat = math.ceil(elat)

			configEngine.ProfileStart("logic","logiccfg")
			configEngine.setValue("logiccfg","lon",elon)
			configEngine.setValue("logiccfg","lat",elat)	
			configEngine.setValue("logiccfg","speed",t.speed)
			configEngine.setValue("logiccfg","course",t.course)
			configEngine.setValue("logiccfg","altitude",t.altitude)
			configEngine.setValue("logiccfg","raidus",t.raidus)
			configEngine.ProfileStop("logiccfg")
		end
	end
end

-----删除回调
createmodule(gt,"lkdelmonitor",function(cbfunc,user)
luaprint("lkdelmonitor-start",cbfunc,user)
	local lkTable =getHandle(gt.locationlist,"lkhandle")
	if lkTable ~= nil and lkTable.monitor ~= nil then	
		for k,v in pairs(lkTable.monitor) do
			local T = lkTable.monitor[k]
			if T and type(T)== "table" then								
				if T.func == cbfunc and T.user == user then			
					table.remove(lkTable.monitor, k)
					luaprint("lkdelmonitor-end")
					break;
				end
			end
		end
	end	
end)

--开始定位函数接口
--输出：无
createmodule(gt,"lkstart",function(mode,cbfunc,user)
luaprint("lkstart-----1",mode,cbfunc,user)	
	lkcreate()
luaprint("lkstart-----2")
	--lkchangemode(mode)
luaprint("lkstart-----3")
	if cbfunc ~= nil then
		luaprint("lkstart-----4")
		lkaddmonitor(cbfunc,user)
		luaprint("lkstart-----5")		
	end
luaprint("lkstart-----end")
end)


--对外声明定位释放函数接口
--输出：无
createmodule(gt,"lkstop",function()
--[[
--luaprint("lkstop-start")
	local lkTable =getHandle(gt.locationlist,"lkhandle")
	if lkTable ~= nil then
		luaprint("lkstop-1")
		--lklastlocation_set(lastpos)
		luaprint("lkstop-2")
	   	--lkgpsstop()
        --gpsabort()
		--lkcellstop()
		--lkplatgpsstop()
		--tmrobj.timerabort("lkgps")
		--tmrobj.timerabort("lkplatgps")	
		--cellid_destroy()
	end
luaprint("lkstop-start",gt.locationlist)
	releaseHandle(gt.locationlist,"lkhandle")	
]]
end)

createmodule(gt,"lkgetlastposition_mem", function()
	print("yaoyt---lkgetlastposition_mem in 00");

	--如果最近一次定位信息还没有数据，就从定位服务去取
	if nil == lastGPSInfo.lon or 0 == lastGPSInfo.lon then
	    	local nFunction = tiros.moduledata.moduledata_get("framework", "pLogicFunction");
	    	local nUser = tiros.moduledata.moduledata_get("framework", "pLogicUser");   

		print("yaoyt---lkgetlastposition_mem in");

		if nFunction ~= nil then
			print("yaoyt---lkgetlastposition_mem 00");
			commlib.universalnotifyFun(nFunction,"LuaToLogicMsg", nUser,3, 1,nil);
					print("yaoyt---lkgetlastposition_mem 01");
		end
				print("yaoyt---lkgetlastposition_mem in 111");

		local GPSInfo = tiros.moduledata.moduledata_get("logic", "logic_lastGPS");
				print("yaoyt---lkgetlastposition_mem  22");
		if nil == GPSInfo or "" == GPSInfo then
				print("yaoyt---lkgetlastposition_mem  33");
			return 0,0,0,0,0,0;
		end

		print("yaoyt---lkgetlastposition_mem 444")
		local t = {};
		local t = tiros.json.decode(GPSInfo)
		print("yaoyt---lkgetlastposition_mem 55" .. t.lon)
		return t.lon,t.lat,t.speed,t.course,t.altitude,t.radius,t.rawLon,t.rawLat;
	else
		--有最近一次定位信息
		print("yaoyt---lkgetlastposition_mem 666 lon:" .. lastGPSInfo.lon .. ",lat:" .. lastGPSInfo.lat)
		return lastGPSInfo.lon,lastGPSInfo.lat,lastGPSInfo.speed,lastGPSInfo.course,lastGPSInfo.altitude,lastGPSInfo.radius,lastGPSInfo.rawLon,lastGPSInfo.rawLat;
	end
	
end)

--
--对外声明获取经纬度函数接口
--输出：integer型，lon,lat
createmodule(gt,"lkgetlastposition_file",function()
	return gt.lkgetlastposition_mem();
end)

createmodule(gt,"lkcellsave_notify",function(gpsnotify,cdmanotify)
	local lkTable =getHandle(gt.locationlist,"lkhandle")
luaprint("---lkcellsave_notify",gpsnotify,cdmanotify)
	if lkTable ~= nil then
			lkTable.gpsnotify= gpsnotify;
			lkTable.cdmanotify= cdmanotify;
luaprint("---lkcellsave_notify",lkTable.gpsnotify,lkTable.cdmanotify)
	end	
end)

--处理定位模块发送来定位信息
createmodule(gt,"lklocationCallback",function(rawLon, rawLat, lon, lat, speed, course, altitude, raidus, locationType)
	print("logic gps lklocationCallback in")
	print("logic gps lklocationCallback " .. lon .. "," .. lat .. "," .. speed .. "," .. course .. "," .. altitude .. "," .. raidus .. "," .. locationType)

	--保存最近一次定位信息
	lastGPSInfo.lon = lon;
	lastGPSInfo.lat = lat;
	lastGPSInfo.speed = speed;
	lastGPSInfo.course = course;
	lastGPSInfo.altitude = altitude;
	lastGPSInfo.raidus = raidus;
	lastGPSInfo.funtype = locationType;
	lastGPSInfo.rawLon = rawLon;
	lastGPSInfo.rawLat = rawLat;

	if GPSFUNTYPE == locationType then
		print("logic gps lklocationCallback type 1");
		gt.lkgpsCB(nil,rawLon,rawLat,speed,course);
		print("logic gps lklocationCallback type end 1");
	elseif CELLIDFUNTYPE == locationType then
		print("logic gps lklocationCallback type 2");
		gt.lkcellidCB(rawLon,rawLat,altitude,raidus);
		
	elseif PLATGPSFUNTYPE == locationType then
		print("logic gps lklocationCallback type 3");
		gt.lkplatgpsCB(nil,rawLon,rawLat,raidus);
	end

end)

tiros.location  = readOnly(gt)
-------------------------------------------------------------
-------------------------end---------------------------------
-------------------------------------------------------------


