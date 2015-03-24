--udp上报 接口封装

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/udpmanager"
require"lua/systemapi/sys_socket"
require"lua/framework/sys_framework"
require"lua/json"
---------------------------静态局部变量-----------------------------------------------------------

--内存中保存的要提交的数据
local G_DataStr = nil

--是否第一次运行标志
local G_FirstRun = true

--是否同步文件
local G_SyncFile = false

---------------------------静态局部常量-----------------------------------------------------------

--临时存放内存中的数据文件，已防丢失数据
local reportFileName = "rep/rep.tmp" 

--一次最大上传数据的大小限制
local REPORT_MAXLEN = 512

local RES_STR_UDP_URL = 1003 --资源文件中编号
local RES_FILE_PATH = "fs0:/res/api/api.rs" --资源文件地址路径
--获取udp上报URL服务器地址及端口号  http://127.0.0.1:6001
--local UDP = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_UDP_URL)


---------------------------局部函数--------------------------------------------------------------

---------获取udp上报IP地址和端口号----------
local function getUDPAddressAndPort()

	local UDP = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_UDP_URL)
	--取得UDP字符串中子串 "http://" 的首位和末尾下标
	local subscript_start ,subscript_end = string.find(UDP,"http://")
	local URL_TEMP = string.sub(UDP, subscript_end + 1) --去掉url中的子串"http://" 
	--udp上报的IP地址
	local UDP_ADDRESS = string.sub(URL_TEMP, 1, string.find(URL_TEMP, ":") -1)
	--udp上报的端口
	local UDP_PORT = tonumber(string.sub(URL_TEMP, string.find(URL_TEMP,":")+1))


	return UDP_ADDRESS , UDP_PORT
end

--根据原有要上传的Json字符串G_DataStr获得要上传的JsonData对象
local function GetJsonData()

	local JsonData = nil

	--根据G_DataStr获得JsonData对象
	if ( (nil==G_DataStr) or (""==G_DataStr) ) then
		JsonData = {}
	else
		JsonData = tiros.json.decode(G_DataStr)
		if ( nil==JsonData ) then 
			return nil
		end	
	end
	
	return JsonData
end

--将Json字符串Str解析为Json对象JsonStr后插入到JsonData对象中
local function InsertJsonStr(JsonData, Str)

	local bOK = true
	local JsonStr = nil

	if ( (nil==JsonData) or (nil==Str) or (""==Str) ) then
		bOK = false
		return bOK
	end

	JsonStr = tiros.json.decode(Str)
	if ( nil==JsonStr ) then 
		bOK = false
		return bOK
	end

	table.insert(JsonData, JsonStr)

	return bOK
end

--首次运行FirstRun函数，处理遗留未发数据
local function FirstRun()

	local f = nil
	local f_len = 0
	local f_data = nil

	if G_SyncFile then

		--开机第一次运行，如果存在 reportFileName 文件，那么直接读取文件内容发送		
		if ( filelib.fexist(reportFileName) and (0~=filelib.fgetsize(reportFileName)) ) then
			f = filelib.fopen(reportFileName, 0)
			f_len = filelib.fgetsize(reportFileName)
			if (nil~=f) then
				f_data = filelib.fread(f, f_len)
				if (nil~=f_data) then
					local sAddress, sPort = getUDPAddressAndPort()
					tiros.udpmanager.UM_Send( sAddress, sPort, f_data )
				end
			end
		end
			
		if (nil~=f) then
			filelib.fclose(f)
		end
		if filelib.fexist(reportFileName) then
			filelib.fremove(reportFileName)
		end
	end

	--将G_FirstRun设置为false
	G_FirstRun = false 	
end

--根据G_DataStr同步reportFileName文件
local function SyncFile()

	local f = nil

	if G_SyncFile then

		if filelib.fexist(reportFileName) then
			filelib.fremove(reportFileName)
		end

		if ( (nil~=G_DataStr) and (""~=G_DataStr) ) then
			f = filelib.fopen(reportFileName, 3)
			if ( nil~=f ) then
				filelib.fwrite(f, G_DataStr, #G_DataStr)
				filelib.fclose(f)
			end
		end
	end
end

--Udp上报
local function udpReport( Str )

	local bOK = true
	local JsonData = nil
	local newDataStr = nil

	if G_FirstRun then	
		FirstRun()
	end
	
	JsonData = GetJsonData()
	if ( nil==JsonData ) then
		bOK = false
		return bOK
	end

	bOK = InsertJsonStr(JsonData, Str)
	if not bOK then
		JsonData = nil
		return bOK
	end

	newDataStr =  tiros.json.encode(JsonData)
	JsonData = nil
	if ( (nil==newDataStr) or (""==newDataStr) ) then
		bOK = false
		return bOK
	end

	--处理G_DataStr与newDataStr
	if ( #newDataStr<=REPORT_MAXLEN ) then  --要上传的数据小于udp上报最大数据大小
		G_DataStr = newDataStr
		SyncFile()		--文件要与G_DataStr同步
		bOK = true
	else 	--要上传的数据大于udp上报最大数据大小
		local sAddress,sProt = getUDPAddressAndPort();
		if ( (nil==G_DataStr) or (""==G_DataStr) ) then --内存G_DataStr中原来不存在数据
			--上报 newDataStr
			tiros.udpmanager.UM_Send( sAddress, sProt, newDataStr )
		else 	--内存G_DataStr中存在数据
			--上报 G_DataStr
			tiros.udpmanager.UM_Send( sAddress, sProt, G_DataStr )

			G_DataStr = ""
			SyncFile()	--文件要与G_DataStr同步
			bOK = udpReport( Str )			
		end
	end

	return bOK	
end

local function localtime(y, m, d, hh, mm, ss )
	hh =  hh+8;    -- 格林威治时间 + 8 小时 = 北京时间
	if ( hh < 24 ) then   --没有跨天，则计算完成
		return y, m, d, hh, mm, ss;		
	end

	-----下面是跨天后的计算--------------------
	
	hh = hh-24;
	d = d+1;      -- 日期加一天

	--按月判断
	if (m ==4) or (m==6) or (m==9) or (m==11) then  --跨小月的判断
		if d > 30 then 
			d = 1;
			m = m+1;
		end
	elseif (m ==1) or (m==3) or (m==5) or (m==7) or (m==8) or (m==10) then  --跨大月的判断
		if d > 31 then 
			d = 1;
			m = m+1;
		end
	elseif m==12 then	--12 月，要判断是否跨年
		if d>31 then
			y = y+1;
			d = 1;
			m = 1;
		end
	elseif m==2 then	--2 月，要判断是否是闰年
		if( ( y%400 == 0 ) or       	     -- 能被400整除，一定是闰年
       		( y%4 ==0 ) and ( y%100 ~=0 ) ) then 	--能被4整除，但不能被100整除，一定是闰年
			if( d>29 ) then	--闰年2月，可以有29号
				m = 3;
				d = 1;
			end
		elseif ( d>28 ) then		--非闰年2月，可以有28号
			m = 3;
			d = 1;
		end		
		
	end

	return y, m, d, hh, mm, ss; --计算完成，开始输出
end

local function GetDate()
	local y,m,d,h,m2,s = timelib.time();
	local yy,MM,dd,hh,mm,ss = localtime(y,m,d,h,m2,s);

	if MM < 10 then
		MM = "0"..MM;
	end
	if dd < 10 then
		dd = "0"..dd;
	end
	if hh < 10 then
		hh = "0"..hh;
	end
	if mm < 10 then
		mm = "0"..mm;
	end
	if ss < 10 then
		ss = "0"..ss;
	end
	local date = ""..yy..MM..dd..hh..mm..ss;
	return date;
end

local function udpReport_ResUpdateError(resType)
	print("yaoyt udpReport_ResUpdateError in")
	local mobileid = tiros.moduledata.moduledata_get('framework','mobileid');
        local nlon,nlat = tiros.location.lkgetlastposition_mem();
	if nil == nlon  or 0 == nlon or nil == nlat or 0 == nlat then
		print("yaoyt udpReport_ResUpdateError nil")
		nlon = 419251335;
		nlat = 143728257;	
	end


	local date = GetDate();
	print(date)

	local sendStr = '{"type":"web_client","dataver":"20120508","servertype":"error_source","mobileid":"' .. tostring(mobileid) .. '","version":"4.1.15.219","lon":' .. nlon .. ',"lat":' .. nlat ..  ',"tag":0,"endtime":0,"date":"' .. tostring(date) .. '","nettype":1,"timeConsuming":0,"issuccess":0,"errorcode":' .. tonumber(resType) .. '}';
	local result = udpReport(sendStr);
	print("yaoyt udpReport_ResUpdateError result:" .. tostring(result))
end

--接口table
local interface = {}
---------------------------全局函数(对外接口)--------------------------------------------------
--对外声明UdpReport函数接口
--输入：Str:string型参数
--输出：bool型，返回true：成功 false：失败
createmodule(interface,"UdpReport", function ( Str )
	udpReport( Str )
end)

--对外声明,资源更新后，客户端发现资源文件错误，向服务端发送
--输入：intger资源类型
--输出：
createmodule(interface,"UdpReport_ResUpdateError", function ( resType )
	udpReport_ResUpdateError( resType )
end)

tiros.udpreport  = readOnly(interface)
