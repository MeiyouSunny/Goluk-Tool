--[[
 @描述：大头针地点区域接口，获取地图点经纬度，并post给服务器，等待服务器返回数据，解析传给调用者
 @编写人：fengfx
 @创建日期：2012-03-11 下午 15:40:00
 @修改内容：修改positionDescriptionSendMessage接口，发送数据中添加http的回调事件	fengfx 2012-04-09
 @修改内容：修改positionDescriptionSendMessage接口，修改js、c端 回调 拼接格式bug	fengfx 2012-04-09
 @修改内容：修改positionDescriptionSendMessage接口中的BUG							fengfx 2012-04-10
 @修改内容：修改positionDescriptionSendMessage接口，发送数据中去掉http的事件信息	fengfx 2012-04-18
 @修改内容：按照新的LUA编码规范修改源码，添加注释，对外接口名暂未修改。 			fengfx 2012-08-01
 @版本：0.1.6
--]]
require "lua/systemapi/sys_namespace"
require "lua/systemapi/sys_handle"
require "lua/systemapi/sys_http"
require "lua/framework/sys_framework"

--资源文件中编号
local RES_STR_PST_URL = 1002;

--资源文件地址路径
local RES_FILE_PATH = "fs0:/res/api/api.rs";

--获取URL:服务器地址及端口号 目前使用测试服务器及端口 http://119.254.82.237:8080
--local POI_DESCRIPTION_URL = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_PST_URL);

--[[
gtpositionDescriptionList存放大头针数据，结构和数据如下
gtpositionDescriptionList = {
	ptype = {
		1:调用方类型：0：lua，1：js， 2：c
		2:lua回调函数地址
		3:js注册回调函数名称
		4:c回调函数指针地址
		5:c调用者传输数据地址
		6:待解析数据
		7:数据，解析后的描述信息 
		8:数据，解析后的城市代码
	}
}
--]]
local gtpositionDescriptionList = {};

--gtpositionDescriptionList的weak表
local gtpositionDescriptionWeakList = {};

--设置gtpositionDescriptionWeakList为弱表
setmetatable(gtpositionDescriptionWeakList, {__mode = "v"});

--[[
 通过经纬度向服务器发送该点大头针数据请求
 @param ptype string型参数，唯一标识符
 @param ntype number型参数，用于标识该回调函数类型（lua：0，js：1，c：2）
 @param cbfunc 注册的回调函数地址
 @param nlon 大头针点的经度
 @param nlat 大头针点的纬度
 @param nUser number型参数，可为nil，c端注册的调用者参数地址
 @return 无
--]]
local function positionDescriptionRequest(ptype, ntype, cbfunc, nlon, nlat, nUser)	
	local tpositionList = nil;
	local slon = nil;
	local slat = nil;
	local sURL = nil;
	--通过ptype从gtpositionDescriptionWeakList中获取对应的大头针表
	tpositionList = getHandle(gtpositionDescriptionWeakList, ptype);
	--如果gtpositionDescriptionWeakList中没有相应的大头针表，则重新创建一个空表
	if tpositionList == nil then
		tpositionList = {};
	end
	tpositionList[1] = ntype;	--注册回调函数 c回调注册User
	if ntype == 0 then			--lua脚本注册回调函数
		tpositionList[2] = cbfunc;
	elseif ntype == 1 then		--js注册回调函数
		tpositionList[3] = cbfunc;
	else						--c注册回调函数和User
		tpositionList[4] = cbfunc;
		tpositionList[5] = nUser;
	end
	tpositionList[6] = "";		--初始化 预留存储待解析数据
	slon = tostring(nlon);
	slat = tostring(nlat);
	local POI_DESCRIPTION_URL = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_PST_URL);
	sURL = POI_DESCRIPTION_URL .. "t=6&lon=" .. slon .. "&lat=" .. slat;--拼接get请求的URL地址
	tiros.http.httpabort(ptype);--取消之前同类请求
	--将修改后或新创建的表tpositionList重新注册到gtpositionDescriptionList和gtpositionDescriptionWeakList中
	registerHandle(gtpositionDescriptionList, gtpositionDescriptionWeakList, ptype, tpositionList);
        return tiros.http.httpsendforlua("cdc_client", "ck_map", ptype, sURL, positionDescriptionHttpNotify, nil);
end

--[[
 解析返回json串，获取字符串数据
 @param ptype string型参数，唯一标识符
 @param sjsonStr json格式的字符串数据：完整的包体数据
 @return bool值 解析成功返回true，否则返回false
--]]
local function jsonStrParser(ptype, sjsonStr)
	local tjsonObj = nil;
	local tpositionList = nil;
	if nil == sjsonStr then
		return false;
	end
	tjsonObj = tiros.json.decode(sjsonStr);		--解析json串
	if nil == tjsonObj then
		return false;
	end
	tpositionList = getHandle(gtpositionDescriptionList, ptype);
	if tpositionList == nil then
		return false;
	else
		tpositionList[7] = tjsonObj.description;--解析后的描述信息 
		tpositionList[8] = tjsonObj.citycode;	--解析后的城市代码
		tpositionList[6] = "";					--清空数据
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
DeclareGlobal("positionDescriptionHttpNotify", function (ptype, nEvent, param1, param2)
	local tpositionList = nil;
	local nStatus = 0;  --标识当前网络、数据状态，0为异常，1为正常
	tpositionList = getHandle(gtpositionDescriptionList, ptype);
	if tpositionList ~= nil then
		if nEvent == 2 then
			if param1 ~= 200 then	--不是200，都为http错误，param1返回错信息
				nStatus = 0;
				positionDescriptionSendMessage(ptype, nStatus, param1, nil);	--http状态出错，错误信息回调发给调用者
				tpositionList = nil;
			end
		elseif nEvent == 3 then
			if tpositionList ~= nil then
				tpositionList[6] = tpositionList[6] .. param2;
			end
		elseif nEvent == 4 then
			--示例：{"description":"北京市朝阳区西大望路","citycode":110105} 
			if jsonStrParser(ptype, tpositionList[6]) then			--处理应答提，json数据解析
				nStatus = 1;
				positionDescriptionSendMessage(ptype, nStatus, tpositionList[8], tpositionList[7]);
				tpositionList = nil;
				tiros.http.httpabort(ptype);
			end
		elseif nEvent == 5 then
			nStatus = 0;
			positionDescriptionSendMessage(ptype, nStatus, param1, nil);--http错误，错误信息回调发给调用者
			tpositionList = nil;
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
DeclareGlobal("positionDescriptionSendMessage", function (ptype, nStatus, param1, param2)
	local tpositionList = nil;
	tpositionList = getHandle(gtpositionDescriptionList, ptype);
	if tpositionList ~= nil  then
		if nStatus == 1 then						--状态正确，正常发送数据
			if param1 ~= nil and param2 ~= nil then
				if tpositionList[1] == 0 then		--lua回调
					if tpositionList[2] ~= nil then
						tpositionList[2](ptype, nStatus, param1, param2);
					end
				elseif tpositionList[1] == 1 then --js回调
					if tpositionList[3] ~= nil then
						local sCallJS;
						sCallJS = string.format("%s('%s', %u, %u, '%s');", tpositionList[3], ptype, nStatus, param1, param2);
						commlib.calljavascript(sCallJS);
					end
				else								--c回调
					if tpositionList[4] ~= nil then
						commlib.universalnotifyFun(tpositionList[4], ptype, tpositionList[5], nStatus, param1, param2);
					end
				end
			end
		else										--状态异常，发送错误信息
			if tpositionList[1] == 0 then			--lua回调，发送标识和错误提示
				if tpositionList[2] ~= nil then 
					tpositionList[2](ptype, nStatus, param1, nil);
				end
			elseif tpositionList[1] == 1 then		--js回调，发送标识和错误提示
				if tpositionList[3] ~= nil then 
					local sCallJS;
					sCallJS = string.format("%s('%s', %u, %u, '%s');", tpositionList[3], ptype, nStatus, param1, "");
					commlib.calljavascript(sCallJS);
				end
			else
				if tpositionList[4] ~= nil then		--c回调，发送标识和错误提示
					commlib.universalnotifyFun(tpositionList[4], ptype, tpositionList[5], nStatus, param1, nil);
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
createmodule(interface, "positiondescriptionforlua", function (ptype, cbfunc, nlon, nlat)
	return positionDescriptionRequest(ptype, 0, cbfunc, nlon, nlat, nil);
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
createmodule(interface, "positiondescriptionforc", function (ptype, cbfunc, nlon, nlat, nUser)
	return positionDescriptionRequest(ptype, 2, cbfunc, nlon, nlat, nUser);
end)

--[[
 对外声明终止大头针请求函数接口
 @param ptype string型参数，用于标识该请求的唯一标识符
 @return 无
--]]
createmodule(interface, "positiondescriptionabort", function (ptype)
	local tpositionList = getHandle(gtpositionDescriptionList, ptype);
	if (tpositionList ~= nil) then
		tpositionList = nil;
	end
	tiros.http.httpabort(ptype);
end)

--添加interface内接口为只读属性，同时注册接口到tiros.PSTdescription中
tiros.PSTdescription = readOnly(interface);

