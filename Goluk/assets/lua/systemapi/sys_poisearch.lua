--poi点周边搜索接口
--功能：获取地图点经纬度以及poi点的名字，并post给服务器，等待服务器返回数据，解析传给调用者"
require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/systemapi/sys_http"
require"lua/framework/sys_framework"

local RES_STR_POI_LIST_URL = 1102 --资源文件中编号
local RES_FILE_PATH = "fs0:/res/api/api.rs" --资源文件地址路径
--获取URL:服务器地址及端口号 目前使用测试服务器及端口 http://nvd.lbs8.com/poi_list?qtype=12&keyword=%E5%8C%97%E4%BA%AC&lon=0&lat=0&selflon=0&selflat=0&areacode=0&bpointlist=1&qr=0&ra=0&currentPage=1&pageSize=10&method=get&xv=1
local URL = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_POI_LIST_URL) 
local Str = "&selflon=0&selflat=0&areacode=0&bpointlist=1&qr=0&ra=0&currentPage=1&pageSize=10&method=get&xv=1"

--[[
poisearchlist存放poi点数据，结构和数据如下
poisearchlist = {
	ptype = {
		1:调用方类型：0：lua，1：js， 2：c
		2:lua回调函数地址
		3:js注册回调函数名称
		4:c回调函数指针地址
		5:c调用者传输数据地址
		6:待解析数据
	}
}
--]]
local poisearchlist = {}
local poisearchweaklist = {}
setmetatable(poisearchweaklist,{__mode ="v" })


--向服务器发送请求
--ptype:string型参数，唯一标识符
--ntype:integer型参数，用于标识该回调函数类型（lua：0，js：1，c：2）
--cbfunc:integer型参数，注册的回调函数地址
--lon poi点的经度
--lat poi点的纬度
--name poi点的名字
--user:integer型参数，可为nil，c端注册的调用者参数地址
local function poisearchrequest(ptype, ntype, cbfunc, lon, lat, name,user)
	local POIlist = getHandle(poisearchweaklist,ptype)
	if POIlist == nil then
		POIlist = {};
	end
	
	--注册回调函数 c回调注册user
	
	POIlist[1] = ntype;
	if ntype == 0 then		--lua脚本注册回调函数
		POIlist[2] = cbfunc;
	elseif ntype == 1 then		--js注册回调函数
		POIlist[3] = cbfunc;
	else				--c回调函数地址
		POIlist[4] = cbfunc;
		POIlist[5] = user;
	end
	POIlist[6] = "" --存储待解析数据

	local stlon = tostring(lon)
	local stlat = tostring(lat)

	local url = URL .. "?qtype=12&keyword="..name.."&lon=" .. stlon .. "&lat=" .. stlat..Str --get请求URL
--print(url)
	tiros.http.httpabort(ptype)--取消之前同类请求

	registerHandle(poisearchlist,poisearchweaklist,ptype,POIlist)

        return tiros.http.httpsendforlua("cdc_client","se_search",ptype, url, poisearchhttpnotify ,nil)
end


local  function poisearchsendmessage(ptype, status, param1,param2)
	local POIlist = getHandle(poisearchlist,ptype)
	if POIlist ~= nil  then
		if (status == 1) then --状态正确 正常发送数据
			if(param1 ~= nil) then
				if (POIlist[1] == 0) then --lua回调
					if (POIlist[2] ~= nil) then
						POIlist[2](ptype, status, param1,param2)
					end
				elseif (POIlist[1] == 1) then --js回调
					if (POIlist[3] ~= nil) then 
						local s;
						s = string.format("%s('%s',%u,%u,'%s');",
								POIlist[3],ptype,status,param1,param2);
						commlib.calljavascript(s);
					end
				else	--c回调
					if POIlist[4] ~= nil then
						commlib.universalnotifyFun(POIlist[4],ptype, 
								POIlist[5],status,param1,param2);
					end
				end
			end
		else  --状态异常 发送错误信息
			if (POIlist[1] == 0) then --lua回调 发送 标识 和 错误提示
				if (POIlist[2] ~= nil) then 
					POIlist[2](ptype, status, param1,param2)
				end
			elseif (POIlist[1] == 1) then --js回调 发送 标识 和 错误提示
				if (POIlist[3] ~= nil) then 
					local s;
					s = string.format("%s('%s',%u,%u,'%s');",
								POIlist[3],ptype,status,param1,param2);
					commlib.calljavascript(s);
				end
			else
				if POIlist[4] ~= nil then --c回调 发送 标识 和 错误提示
					commlib.universalnotifyFun(POIlist[4],ptype, 
								POIlist[5],status,param1,param2);
				end
			end
		end
	end
end



DeclareGlobal("poisearchhttpnotify", function (ptype,event,param1, param2)
	local POIlist = getHandle(poisearchlist,ptype)
	if POIlist ~= nil then
		if event == 2 then
			if param1 ~= 200 then
				--http状态出错  错误信息回调发给调用者
				poisearchsendmessage(ptype, 0, param1,nil)
				POIlist = nil
				tiros.http.httpdestroy(ptype)
			end
		elseif event == 3 then
			--拼接应答体
			if(POIlist ~= nil) then
				POIlist[6] = POIlist[6] .. param2	
			end
			
		elseif event == 4 then
			poisearchsendmessage(ptype, 1, 0,POIlist[6] )
			POIlist = nil
			tiros.http.httpdestroy(ptype)
		elseif event == 5 then
			--http错误  错误信息回调发给调用者
			poisearchsendmessage(ptype, 0, param1,nil)
			POIlist = nil
			tiros.http.httpdestroy(ptype)
		end
	end
end)


--------对外接口

local interface = {}

--对外声明lua层调用poi点周边搜索请求函数接口
--ptype:string型参数，js端用于标识该请求的唯一标识符
--cbfunc:function型参数，lua端注册的回调函数地址
--lon:请求的点的经度
--lat:请求的点的纬度
--name: poi点名字
--输出:请求成功返回true，失败返回false
createmodule(interface, "poisearchforlua", function (ptype, cbfunc, lon, lat,name)
	return poisearchrequest(ptype, 0, cbfunc, lon, lat, name,nil)
end)

--对外声明终止poi点周边搜索请求函数接口
--ptype:string型参数，js端用于标识该请求的唯一标识符
--输出:无
createmodule(interface, "poisearchabort", function (ptype)
	local POIlist = getHandle(poisearchlist,ptype)
	if (POIlist ~= nil) then
		POIlist = nil
	end
	tiros.http.httpabort(ptype)
end)

tiros.poisearch = readOnly(interface)





















