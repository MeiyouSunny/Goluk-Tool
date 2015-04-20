--[[
 @描述：poi点，名称在该点周边150米范围内搜索，并post给服务器，等待服务器返回数据，解析传给调用者
 @编写人：fengfx
 @创建日期：2012-10-24 下午 15:40:00
 @修改内容：
 @版本：0.1.0
--]]
require "lua/systemapi/sys_namespace"
require "lua/systemapi/sys_handle"
require "lua/systemapi/sys_http"
require "lua/framework/sys_framework"
require "lua/commfunc"

--搜索半径(单位：米)
local SEARCH_RADIUS = 1500;

--资源文件中编号(搜索--需要关键字)
local RES_STR_POI_LIST_INFO_URL = 1105;
----资源文件中编号(搜索--不需要关键字)
local RES_STR_POI_LIST_URL = 1102;

--资源文件地址路径
local RES_FILE_PATH = "fs0:/res/api/api.rs";

--获取URL:服务器地址及端口号
--local POSITION_SEARCH_URL = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_POI_LIST_INFO_URL);

--[[
gtpositionSearchList存放搜索数据，结构和数据如下
gtpositionSearchList = {
	ptype = {
		1:调用方类型：0：lua，1：js， 2：c
		2:lua回调函数地址
		3:js注册回调函数名称
		4:c回调函数指针地址
		5:c调用者传输数据地址
		6:待解析数据
		7:数据，--解析后的地址
		8:数据，--解析后的名称
	}
}
--]]
local gtpositionSearchList = {};

--gtpositionSearchList的weak表
local gtpositionSearchWeakList = {};

--设置gtpositionSearchWeakList为弱表
setmetatable(gtpositionSearchWeakList, {__mode = "v"});

--[[
 通过经纬度向服务器发送该点搜索数据请求
 @param ptype string型参数，唯一标识符
 @param ntype number型参数，用于标识该回调函数类型（lua：0，js：1，c：2）
 @param cbfunc 注册的回调函数地址
 @param nlon 大头针点的经度
 @param nlat 大头针点的纬度
 @param nUser number型参数，可为nil，c端注册的调用者参数地址
 @return 无
--]]
local function positionSearchRequest(ptype, ntype, cbfunc, nlon, nlat, nselflon, nselflat, keyword, nUser)
	local tSearchList = nil;
	local jsonstring = nil;
	local slon = nil;
	local slat = nil;
	local sURL = nil;
	--通过ptype从gtpositionSearchWeakList中获取对应的大头针表
	tSearchList = getHandle(gtpositionSearchWeakList, ptype);
	--如果gtpositionSearchWeakList中没有相应的大头针表，则重新创建一个空表
	if tSearchList == nil then
		tSearchList = {};
	end
	tSearchList[1] = ntype;	--注册回调函数 c回调注册User
	if ntype == 0 then			--lua脚本注册回调函数
		tSearchList[2] = cbfunc;
	elseif ntype == 1 then		--js注册回调函数
		tSearchList[3] = cbfunc;
	else						--c注册回调函数和User
		tSearchList[4] = cbfunc;
		tSearchList[5] = nUser;
	end
	tSearchList[6] = "";		--初始化 预留存储待解析数据
	local POSITION_SEARCH_URL = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_POI_LIST_INFO_URL);
	sURL = POSITION_SEARCH_URL .. "?lon=" .. tostring(nlon) .. "&qtype=11&lat=" .. tostring(nlat) .. "&keyword=" .. tiros.commfunc.EnCodeUrl(keyword) .. "&method=get&bpointlist=1&ra=1500&selflat=" .. tostring(nselflat) .. "&selflon=" .. tostring(nselflon) .. "&currentPage=1&qr=&pageSize=5&areacode="
	print("TTTTTTTTTTTTTTTTTTTTSSSS:  " .. sURL)

	tiros.http.httpabort(ptype);--取消之前同类请求
	--将修改后或新创建的表tSearchList重新注册到gtpositionSearchList和gtpositionSearchWeakList中
	registerHandle(gtpositionSearchList, gtpositionSearchWeakList, ptype, tSearchList);
        return tiros.http.httpsendforlua("cdc_client", "ck_map", ptype, sURL, positionSearchHttpNotify, nil);
end

--[[
 通过经纬度向服务器发送该点搜索数据请求
 @param ptype string型参数，唯一标识符
 @param ntype number型参数，用于标识该回调函数类型（lua：0，js：1，c：2）
 @param cbfunc 注册的回调函数地址
 @param nlon 大头针点的经度
 @param nlat 大头针点的纬度
 @param nUser number型参数，可为nil，c端注册的调用者参数地址
 @return 无
--]]
local function positionSearchRequest1(ptype, ntype, cbfunc, nlon, nlat, nselflon, nselflat, ncount,nra,nqr,nUser)
	local tSearchList = nil;
	local jsonstring = nil;
	local slon = nil;
	local slat = nil;
	local sURL = nil;
	--通过ptype从gtpositionSearchWeakList中获取对应的大头针表
	tSearchList = getHandle(gtpositionSearchWeakList, ptype);
	--如果gtpositionSearchWeakList中没有相应的大头针表，则重新创建一个空表
	if tSearchList == nil then
		tSearchList = {};
	end
	tSearchList[1] = ntype;	--注册回调函数 c回调注册User
	if ntype == 0 then			--lua脚本注册回调函数
		tSearchList[2] = cbfunc;
	elseif ntype == 1 then		--js注册回调函数
		tSearchList[3] = cbfunc;
	else						--c注册回调函数和User
		tSearchList[4] = cbfunc;
		tSearchList[5] = nUser;
	end
	tSearchList[6] = "";		--初始化 预留存储待解析数据
	print("TTTTTTTTTTTTTTTTT-----start");
	local POSITION_SEARCH_URL = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_POI_LIST_URL);

	sURL = POSITION_SEARCH_URL .. "?lon=" .. tostring(nlon) .. "&qtype=11&lat=" .. tostring(nlat) .. "&keyword=&method=get&bpointlist=1&ra=" .. tonumber(nra) .. "&selflat=" .. tostring(nselflat) .. "&selflon=" .. tostring(nselflon) .. "&currentPage=1&qr=".. tostring(nqr) .. "&pageSize=" .. tonumber(ncount) .. "&xv=2&areacode=";


	print("TTTTTTTTTTTTTTTTTTTTSSSS:  " .. sURL);

	tiros.http.httpabort(ptype);--取消之前同类请求
	--将修改后或新创建的表tSearchList重新注册到gtpositionSearchList和gtpositionSearchWeakList中
	registerHandle(gtpositionSearchList, gtpositionSearchWeakList, ptype, tSearchList);
        return tiros.http.httpsendforlua("cdc_client", "ck_map", ptype, sURL, positionSearchHttpNotify, nil);
end

--[[
 解析返回json串，获取字符串数据
 @param ptype string型参数，唯一标识符
 @param sjsonStr json格式的字符串数据：完整的包体数据
 @return bool值 解析成功返回true，否则返回false
--]]
local function jsonStrParser(ptype, sjsonStr)
	local tjsonObj = nil;
	local tSearchList = nil;
	if nil == sjsonStr then
		return false;
	end
	tjsonObj = tiros.json.decode(sjsonStr);		--解析json串
	if nil == tjsonObj then
		return false;
	end
	if nil == tjsonObj.commonObjList[1] then
		return false;
	end

	tSearchList = getHandle(gtpositionSearchList, ptype);
	if tSearchList == nil then
		return false;
	else
		tSearchList[7] = tjsonObj.commonObjList[1].address;	--解析后的地址 
		tSearchList[8] = tjsonObj.commonObjList[1].name;	--解析后的名称
		tSearchList[6] = "";					--清空数据
		return true;
	end
end

--[[
 全局的http回调大头针函数
 @param ptype string型参数，唯一标识符
 @param nEvent number型参数，http回调事件类型
 @param param1 当nEvent=2：状态码；当nEvent=3：包体大小(uint32)；当nEvent=5：错误类型
 @param param2 当nEvent=2：数据体长度，状态码为200系列，才有该事件；当nEvent=3：包体数据(void *)；当nEvent=5：错误码，600之后为自定义错误
 @return 无
--]]
DeclareGlobal("positionSearchHttpNotify", function (ptype, nEvent, param1, param2)
	print("positionSearchHttpNotify-----------------------",ptype,nEvent,param1);
	local tSearchList = nil;
	local nStatus = 0;  --标识当前网络、数据状态，0为异常，1为正常
	tSearchList = getHandle(gtpositionSearchList, ptype);
	if tSearchList ~= nil then
		if nEvent == 2 then
			if param1 ~= 200 then	--不是200，都为http错误，param1返回错信息
				nStatus = 0;
				positionSearchSendMessage(ptype, nStatus, param1, nil);	--http状态出错，错误信息回调发给调用者
				tSearchList = nil;
			end
		elseif nEvent == 3 then
			if tSearchList ~= nil then
				tSearchList[6] = tSearchList[6] .. param2;
			end
		elseif nEvent == 4 then
			--if jsonStrParser(ptype, tSearchList[6]) then			--处理应答提，json数据解析
				nStatus = 1;
				positionSearchSendMessage(ptype, nStatus, 0, tSearchList[6]);--6 json串
				tSearchList = nil;
				tiros.http.httpabort(ptype);
			--end
		elseif nEvent == 5 then
			nStatus = 0;
			positionSearchSendMessage(ptype, nStatus, param1, nil);--http错误，错误信息回调发给调用者
			tSearchList = nil;
			tiros.http.httpabort(ptype);
		end
	end
end)

--[[
 positionDescription发送消息函数
 @param ptype string型参数，唯一标识符
 @param nStatus number型参数，标识当前网络、数据状态，0为异常，1为正常
 @param param1 当nStatus=0：错误类型；当nStatus=1：解析后的城市代码  
 @param param2 当nStatus=0：无内容；当nStatus=1：解析后的描述信息
 @return 无
--]]
DeclareGlobal("positionSearchSendMessage", function (ptype, nStatus, param1, param2)
	local tSearchList = nil;
	tSearchList = getHandle(gtpositionSearchList, ptype);
	if tSearchList ~= nil  then
		if nStatus == 1 then						--状态正确，正常发送数据
			if param1 ~= nil and param2 ~= nil then
				if tSearchList[1] == 0 then		--lua回调
					if tSearchList[2] ~= nil then
						tSearchList[2](ptype, nStatus, param1, param2);
					end
				elseif tSearchList[1] == 1 then --js回调
					if tSearchList[3] ~= nil then
						local sCallJS;
						sCallJS = string.format("%s('%s', %u, %u, '%s');", tSearchList[3], ptype, nStatus, param1, param2);
						commlib.calljavascript(sCallJS);
					end
				else								--c回调
					if tSearchList[4] ~= nil then
						commlib.universalnotifyFun(tSearchList[4], ptype, tSearchList[5], nStatus, param1, param2);
					end
				end
			end
		else										--状态异常，发送错误信息
			if tSearchList[1] == 0 then			--lua回调，发送标识和错误提示
				if tSearchList[2] ~= nil then 
					tSearchList[2](ptype, nStatus, param1, nil);
				end
			elseif tSearchList[1] == 1 then		--js回调，发送标识和错误提示
				if tSearchList[3] ~= nil then 
					local sCallJS;
					sCallJS = string.format("%s('%s', %u, %u, '%s');", tSearchList[3], ptype, nStatus, param1, "");
					commlib.calljavascript(sCallJS);
				end
			else
				if tSearchList[4] ~= nil then		--c回调，发送标识和错误提示
					commlib.universalnotifyFun(tSearchList[4],ptype, tSearchList[5], nStatus, param1, nil);
				end
			end
		end
	end
end)


--对外接口，此声明后都为全局接口。
local interface = {};

--[[
 对外声明lua层调用大头针请求函数接口
 @param ptype string型参数，lua端用于标识该请求的唯一标识符
 @param cbfunc function型参数，lua端注册的回调函数地址
 @param nlon 大头针点的经度
 @param nlat 大头针点的纬度
 @return 请求成功返回true，失败返回false
--]]
createmodule(interface, "positionsearchforlua", function (ptype, cbfunc, nlon, nlat, nselflon, nselflat, keyword)
	return positionSearchRequest(ptype, 0, cbfunc, nlon, nlat, nselflon, nselflat, keyword, nil);
end)

--[[
 对外声明c层调用大头针请求函数接口
 @param ptype string型参数，c端用于标识该请求的唯一标识符
 @param cbfunc number型参数，c端注册的回调函数地址
 @param nlon 大头针点的经度
 @param nlat 大头针点的纬度
 @param nUser number型参数，可为nil，c端注册的调用者参数地址
 @return 请求成功返回true，失败返回false
--]]
createmodule(interface, "positionsearchforc", function (ptype, cbfunc, nlon, nlat, nselflon, nselflat, keyword, nUser)
	return positionSearchRequest(ptype, 2, cbfunc, nlon, nlat, nselflon, nselflat, keyword, nUser);
end)
--[[
 对外声明c层调用大头针请求函数接口
 @param ptype string型参数，c端用于标识该请求的唯一标识符
 @param cbfunc number型参数，c端注册的回调函数地址
 @param nlon 大头针点的经度
 @param nlat 大头针点的纬度
 @param nselflon 我的位置
 @param nselflat 我的位置
 @param ncount 请求多少条数据
 @param nra    半径
 @param nqr    分类
 @param nUser number型参数，可为nil，c端注册的调用者参数地址
 @return 请求成功返回true，失败返回false
--]]
createmodule(interface, "positionsearchMyPositionforc", function (ptype, cbfunc, nlon, nlat, nselflon, nselflat, ncount,nra,nqr,nUser)
	print("positionsearchMyPositionforc-----------------------------------------------------");
	return positionSearchRequest1(ptype, 2,cbfunc, nlon, nlat, nselflon, nselflat, ncount,nra,nqr,nUser);
end)

--[[
 对外声明终止大头针请求函数接口
 @param ptype string型参数，用于标识该请求的唯一标识符
 @return 无
--]]
createmodule(interface, "positionsearchabort", function (ptype)
	local tSearchList = getHandle(gtpositionSearchList, ptype);
	if (tSearchList ~= nil) then
		tSearchList = nil;
	end
	tiros.http.httpabort(ptype);
end)

--添加interface内接口为只读属性，同时注册接口到tiros.PSTdescription中
tiros.PSTsearch = readOnly(interface);
