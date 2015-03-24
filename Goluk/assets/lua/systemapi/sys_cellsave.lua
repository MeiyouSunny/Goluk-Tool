--udp上报 接口封装

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/udpreport"
require"lua/framework/sys_framework"
require"lua/json"
require"lua/tapi"
require"lua/commfunc"
require"lua/moduledata"
require"lua/udpmanager"
----------------------------
local RES_STR_UDP_URL = 1003 --资源文件中编号
local RES_FILE_PATH = "fs0:/res/api/api.rs" --资源文件地址路径
--获取udp上报URL服务器地址及端口号  http://127.0.0.1:6001
local UDP = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_UDP_URL)
---------获取udp上报IP地址和端口号----------
--取得UDP字符串中子串 "http://" 的首位和末尾下标
local subscript_start ,subscript_end = string.find(UDP,"http://")
local URL_TEMP = string.sub(UDP, subscript_end + 1) --去掉url中的子串"http://" 
--udp上报的IP地址
local UDP_ADDRESS = string.sub(URL_TEMP, 1, string.find(URL_TEMP, ":") -1)
--udp上报的端口
local UDP_PORT = tonumber(string.sub(URL_TEMP, string.find(URL_TEMP,":")+1))
-----------------------------------
local gt = {}

local tmrobj = getmodule("timer")
local moduledataobj = getmodule("moduledata")
local locationEngine = getmodule("location")
local tapiEngine = getmodule("tapi")

local  gCollectdataList= {}
local  nps = {}
local  wifi = {}
local  timeoffset = 120000
local  CELL_SAVE_INTERAL = 60000
local  last_save_time = 0
--[[

[{"lon":0,"mid":"123456789012345","source":2,"lat":0,"alt":0,"ts":1356429758,"ver":"4.1.2.2506","ef":0,"time":"20121225180238","vf":3,"wifi":{"mac":"00254b960bbb","ssid":"tiros608","channel":0,"snt":0,"age":0,"ss":100,"su":1},"rtype":"user_behavior","mtype":"lbsdata","nps":{"bst":0,"lac":4440,"mcc":460,"ta":0,"su":1,"mnc":0,"ss":100,"cid":50927}}
,{"lon":419031151,"mid":"123456789012345","source":1,"lat":143668825,"alt":0,"ts":1356429759,"ver":"4.1.2.2506","ef":0,"time":"20121225180239","vf":3,"wifi":{"mac":"00254b960bbb","ssid":"tiros608","channel":0,"snt":0,"age":0,"ss":100,"su":1},"rtype":"user_behavior","mtype":"lbsdata","nps":{"bst":0,"lac":4440,"mcc":460,"ta":0,"su":1,"mnc":0,"ss":100,"cid":50927}}]

--]]--
----------------------------
local function udpreport(str)
	if str ~= nil then
		tiros.udpmanager.UM_Send( UDP_ADDRESS, UDP_PORT, str )
	end
end

local function comparecell(nps, lon, lat, ts)
	luaprint("comparecell")
	for k,v in pairs(gCollectdataList) do
		luaprint(k,v)
		if  v.nps and nps and v.nps.lac == nps.lac and v.nps.cid == nps.cid then
			if v.nps.ss < nps.ss then
				v.nps.ss = nps.ss					
				v.lon = lon
				v.lat = lat
				v.ts = ts	
				return true			
			end
		end		
	end
	return false
end

local function comparewifi(wifi, lon, lat, ts)
	luaprint("comparewifi")
	for k,v in pairs(gCollectdataList) do
		luaprint(k,v)
		if  v.wifi and wifi and v.wifi.mac == wifi.mac and v.wifi.cid == wifi.cid then
			if v.wifi.ss < wifi.ss then
				v.wifi.ss = wifi.ss					
				v.lon = lon
				v.lat = lat
				v.ts = ts	
				return true			
			end
		end		
	end	
	return false		
end

local function savecollectdata(cellsavedata)
	luaprint("cellsave-savecollectdata")
	cellsavedata.rtype = "user_behavior"
	cellsavedata.mtype = "lbsdata"
	cellsavedata.time = tiros.commfunc.CurrentTime()
	cellsavedata.mid = moduledataobj.moduledata_get("framework", "mobileid")
	cellsavedata.ver = moduledataobj.moduledata_get('framework', 'version')
	local y, m, d, hh, mm, ss = timelib.time()	
	cellsavedata.ts = timelib.mktime(y, m, d, hh, mm, ss)
	cellsavedata.ef = 0
	cellsavedata.vf = 3
	cellsavedata.alt = 0

	
	local bCellExist =  comparecell(cellsavedata.nps, cellsavedata.lon, cellsavedata.lat, cellsavedata.ts)
	local bWifiExist =  comparewifi(cellsavedata.wifi, cellsavedata.lon, cellsavedata.lat, cellsavedata.ts)
	luaprint("cellsave-bCellExist-bWifiExist",bCellExist,bWifiExist)
	if not bCellExist or not bWifiExist then
		luaprint("cellsave-insert")
		table.insert(gCollectdataList, cellsavedata)	
	end		
end

local function reporttimerstart()
	if tmrobj.timerisbusy("cellsave") then
		tmrobj.timerabort("cellsave")
	end
	tmrobj.timerstartforlua("cellsave",timeoffset, gt.cellid_timerCB)
end

local function checklisttoreport(bForce)
	luaprint("cellsave-checklisttoreport",bForce)
	if bForce then
		local result = tiros.json.encode(gCollectdataList)
		luaprint("cellsave-encode",result)
		--tiros.udpreport.UdpReport(result)
		udpreport(result)
		gCollectdataList = {}
		reporttimerstart()
	else
		local count = table.maxn(gCollectdataList)
		luaprint("cellsave-table.maxn",count)
		if count >= 50 then
			local result = tiros.json.encode(gCollectdataList)
			luaprint("cellsave-encode",result)
			--tiros.udpreport.UdpReport(result)
			udpreport(result)
			gCollectdataList = {}
			reporttimerstart()
		end
	end
end

--timer回调
createmodule(gt,"cellid_timerCB",function(handletype)
	if handletype == "cellsave" then 
		luaprint("cellsave-cellid_timerCB")
		checklisttoreport(true)		
	end
end)

createmodule(gt,"cellsave_cdmanotify", function ( lac, cid, mcc, mnc, signal, lat, lon )
	luaprint("cellsave_cdmanotify",lac, cid, mcc, mnc, signal, lat, lon )
	local  cellsavedata= {}

	nps.lac = lac
	nps.cid = cid
	nps.mcc = mcc
	nps.mnc = mnc
	nps.ss = signal
	nps.su = 1
	nps.ta = 0
	nps.bst = 0
	cellsavedata.nps = nps

	local name,mac,ip,signalstrength = tapiEngine.tapigetconnwifiinfo()
	if name ~= nil and mac ~=nil then 
		luaprint( "cellsave_gpsnotify",name,mac,ip,signalstrength )
		wifi.mac = mac
		wifi.ssid = name
		wifi.ss = signalstrength
		wifi.su = 1
		wifi.age = 0
		wifi.channel = 0
		wifi.snt = 0
		cellsavedata.wifi = wifi
	end

	cellsavedata.lat = tostring(lat)
	cellsavedata.lon = tostring(lon)
	cellsavedata.source = 2
	
	savecollectdata(cellsavedata)	
	checklisttoreport(false)
end)

createmodule(gt,"cellsave_gpsnotify", function ( lon,lat,speed,course)
	local time = timelib.clock();
	--一分钟采集一次，低于一分钟不采集
	if time - last_save_time  > CELL_SAVE_INTERAL then
		last_save_time = time;

		local  cellsavedata= {}
		luaprint( "cellsave_gpsnotify",lon,lat,speed,course)
		local conut = tapiEngine.tapigetbscount()
		if conut > 0 then
			local lac,cid,mcc,mnc,signal = tapiEngine.tapigetbsbyindex(0)
			if lac ~= nil and cid ~= nil then 
				luaprint( "cellsave_gpsnotify",lac,cid,mcc,mnc,signal )
				nps.lac = lac
				nps.cid = cid
				nps.mcc = mcc
				nps.mnc = mnc
				nps.ss = signal
				nps.su = 1
				nps.ta = 0
				nps.bst = 0
				cellsavedata.nps = nps
			end
		end

		local name,mac,ip,signalstrength = tapiEngine.tapigetconnwifiinfo()
		if name ~= nil and mac ~=nil then 
			luaprint( "cellsave_gpsnotify",name,mac,ip,signalstrength)
			wifi.mac = mac
			wifi.ssid = name
			wifi.ss = signalstrength
			wifi.su = 1
			wifi.age = 0
			wifi.channel = 0
			wifi.snt = 0
			cellsavedata.wifi = wifi
		end

		cellsavedata.lat = tostring(lat / 3.6)
		cellsavedata.lon = tostring(lon / 3.6)
		cellsavedata.source = 1

		savecollectdata(cellsavedata)
		checklisttoreport(false)
	end
end)

createmodule(gt,"cellsave_open", function ()
	luaprint("cellsave_open---")	
	locationEngine.lkcellsave_notify(gt.cellsave_gpsnotify, gt.cellsave_cdmanotify)
	reporttimerstart()
end)


tiros.cellsave  = readOnly(gt)

