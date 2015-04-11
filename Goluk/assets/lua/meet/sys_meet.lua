require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/framework/sys_framework"
require"lua/systemapi/sys_file"
require"lua/json/sys_json"
require"lua/commfunc"
require"lua/http"
require"lua/moduledata"
require"lua/database"


--[[
这个脚本要解决的任务：
1，见面一短信获取，通知调用者结果
2，见面二短信获取，通知调用者结果
3，停止分享信息，通知调用者结果
4，见面一一旦有一个链接，就发送tiros.airtalkeemgr.UploadPositionInfo给airtalkee
5，见面二一旦有一个链接，就发送tiros.airtalkeemgr.UploadPositionInfo给airtalkee
6，见面一一旦没有任何有效的链接，就发送tiros.airtalkeemgr.StopPositionInfo给airtalkee
7，见面二一旦没有任何有效的链接，就发送tiros.airtalkeemgr.StopPositionInfo给airtalkee
8，见面2 web网页停止分享位置， 得到服务器发送来的消息，发送518通知，notify518()    type =10
9，服务器发来 web端连续上报位置接收坐标，通知给logic        type=11
10，服务器发来 接收坐标   发commlib.initNotifyFun(nFunction, nUser, 145, 0, sPosition);   type =1
11，服务器发来“停止分享接收服务”，更新所有链接的状态，发518通知给web刷新 type = 2
]]


--=========================================内部数据=========================================
local interface={}

--table, element is table{meetid phone name status}
--[[
见面一 record format: phone, name, meetid, status
13800001111, 甲, 23929392, -1
13591829912, 乙, 23239888, 1
13810001444, 丙, 33929212, 0
---------------------------
]]
--见面一数据表
local whoShareMyInfo = {}

--[[
见面二 record format: phone, name, meetid, status, x, y, time
13800001111, 甲, 23929392, -1, 23283, 238823  afadfasf
13591829912, 乙, 23239888, 1, 23283, 238823 adfasf
13810001444, 丙, 33929212, 0, 23283, 238823  asdf
---------------------------
]]
--见面二数据表
local iShareWho = {}


local userId = nil
local httpData = nil
local userCallBackFn = nil
local oldMeetId = nil
local tempUser = nil
local removeWho = 1
local firstCallInit = 0

local times = 0


--=========================================内部实现=========================================
--更新数据仓库只负责更新，具体发通知，由具体环境来调用
local function updateMeetButtonDataStatus(meetType, valueStr)
	--print("meet updateMeetButtonDataStatus  run in")
	local smeet_state_ptp = tiros.moduledata.moduledata_get("web", "meet_state_ptp");
	local t = nil ;
	if smeet_state_ptp == nil or #smeet_state_ptp == 0 then
		t = {};
		t["here"] = '0';
		t["where"] = '0';
		t["together"] = '0';
	else
		t = tiros.json.decode(smeet_state_ptp);
	end
	if meetType == 1 then
		t["here"] = valueStr;
	else
		t["where"] = valueStr;
	end
	tiros.moduledata.moduledata_set("web", "meet_state_ptp", tiros.json.encode(t));
	--print("meet updateMeetButtonDataStatus  run out")
end

local function notify518()
--print("meet notify518  run in")
	commlib.calljavascript("system.callback(518);")
end

--通知逻辑模块的句柄
local function getLogicFunctionAndUser()
    local nFunction = tiros.moduledata.moduledata_get("framework", "pLogicFunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "pLogicUser");
    return nFunction, nUser;
end


--获取通知平台的对象
local function getPlatformFunctionAndUser()
    local nFunction = tiros.moduledata.moduledata_get("framework", "pfunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "puser");
    if nFunction == nil or nUser == nil then
       --print("getPlatformFunctionAndUser--error");
       return nil,nil;
    else
        return nFunction, nUser;
    end
end



--写文件保存记录
local function refreshHistory(meetType)
	if meetType == 1 then
		if #whoShareMyInfo > 0 then
			local txt = tiros.json.encode(whoShareMyInfo)
			tiros.file.Writefile("fs2:/meet/one", txt, true);
		else
			tiros.file.Removefile("fs2:/meet/one")
		end
	else
		if #iShareWho > 0 then
			local data = tiros.json.encode(iShareWho)
			tiros.file.Writefile("fs2:/meet/two", data, true);
		else
			tiros.file.Removefile("fs2:/meet/two")
		end
	end
end


--清理数据成员，以便不同信息之间无干扰
local function clearMemberData()
	userId = nil
	oldMeetId = nil
	httpData = nil
	userCallBackFn = nil

end


--得到见面的数据记录
local function sys_GetMeetRecord(id, meetType, callbackFn)
--print("meet sys_GetMeetRecord  run in")
	local status = 0
	if meetType ==  1 then
		if #whoShareMyInfo > 0 then
			status = 1
		end
		callbackFn(id, status, whoShareMyInfo)
	else
		if #iShareWho > 0 then
			status = 1
		end
		callbackFn(id, status, iShareWho)
	end
	--print("meet sys_GetMeetRecord  run out")
end

--删除共享
local function sys_DeleteShareByMeetId(id, meetId, meetType, callbackFn)
	--print("meet sys_DeleteShareByMeetId  run in")
	local result = 0
	for i = 1, #iShareWho do
		if iShareWho[i]["meetid"] == meetId then
			table.remove(iShareWho, i)
			result = 1
			--print("meet sys_DeleteShareByMeetId  meet 2 delete meet item")
			if #iShareWho == 0 then
				updateMeetButtonDataStatus(2, "0")
				tiros.airtalkeemgr.StopUploadPositionInfo(2)
				--print("meet sys_DeleteShareByMeetId meet 2  update button state, stop upload")
			end
			refreshHistory(2)
			break
		end
	end
	callbackFn(id, result)
	--print("meet sys_DeleteShareByMeetId  run out")
end


--处理见面一默认短信内容
local function processMeetOneDefaultSMS(httpData)
	--read info
	--print("meet processMeetOneDefaultSMS  run in")
	local temp = tiros.json.decode(httpData)
	local result = 1
	if temp["success"] == true then
		--print("meet processMeetOneDefaultSMS  get response success")
		--save information to history table
		tempUser["meetid"] = temp["meetid"]
		tempUser["status"] = 1
		whoShareMyInfo[#whoShareMyInfo + 1] = tempUser
		-- update history
		refreshHistory(1)
		-- write moduledata
		local jsonPara = "{\"type\":\"meet_here\",\"msg\":\"" .. temp["content"] .. "\", \"phone\":\"" .. tempUser["phone"]
			.. "\", \"name\":\"" .. tempUser["name"] .. "\"}"
		----print(jsonPara)
		tiros.moduledata.moduledata_set("web","sharepoi",jsonPara)
		tempUser = nil
		tempUser = {}
		--change meet button color
		if #whoShareMyInfo == 1 then
			--print("meet processMeetOneDefaultSMS  get response success")
			updateMeetButtonDataStatus(1, "1")
		end
		tiros.airtalkeemgr.UploadPositionInfo(1)
	else
		result = 0
	--print("meet processMeetOneDefaultSMS  get response fail")
	end
	-- notify
	if userCallBackFn ~= nil then
		--print("meet processMeetOneDefaultSMS  userCallBackFn")
		userCallBackFn(userId, result)
	else
		local msg = nil
		if result == 1 then
			msg = "{\"meetid\":" .. temp["meetid"] .. ", \"module\":\"web\", \"key\":\"sharepoi\"}"
		end
		--print("meet processMeetOneDefaultSMS  initNotifyFun")
		local nFunction, nUser = getPlatformFunctionAndUser();
		commlib.initNotifyFun(nFunction, nUser, 159, result, msg);
	end
	--print("meet processMeetOneDefaultSMS  run out")
end




--处理见面二默认短信内容
local function processMeetTwoDefaultSMS(httpData)
--print("meet processMeetTwoDefaultSMS  run in")
	--read info
	local temp = tiros.json.decode(httpData)
	local result = 1
	if temp["success"] == true then
		--print("meet processMeetTwoDefaultSMS  temp in ")
		--save information to history table
		tempUser["meetid"] = temp["meetid"]
		tempUser["status"] = -1
		iShareWho[#iShareWho + 1] = tempUser
		-- update history
		refreshHistory(2)
		-- write moduledata
		local jsonPara = "{\"type\":\"meet_where\",\"msg\":\"" .. temp["content"] .. "\", \"phone\":\"" .. tempUser["phone"]
			.. "\", \"name\":\"" .. tempUser["name"] .. "\"}"
		----print(jsonPara)
		tiros.moduledata.moduledata_set("web","sharepoi",jsonPara)
		tempUser = nil
		tempUser = {}
		tiros.airtalkeemgr.UploadPositionInfo(2)
	else
		result = 0
	end
	--notify
	if userCallBackFn ~= nil then
		--print("meet processMeetTwoDefaultSMS  userCallBackFn ")
		userCallBackFn(userId, result)
	else
		--print("meet processMeetTwoDefaultSMS  initNotifyFun ")
		local msg = nil
		if result == 1 then
			msg = "{\"meetid\":" .. temp["meetid"] .. ", \"module\":\"web\", \"key\":\"sharepoi\"}"
		end
		local nFunction, nUser = getPlatformFunctionAndUser();
		commlib.initNotifyFun(nFunction, nUser, 159, result, msg);
	end

	--print("meet processMeetTwoDefaultSMS  run out")
end

--再次发送链接请求，得到新的短信
local function processRefreshMeetOne(httpData)
	local temp = tiros.json.decode(httpData)
	local result = 1
	--print("meet processRefreshMeetOne  run in")
	if temp["success"] ==  true then
		--print("meet processRefreshMeetOne  success RUN IN")
		for i = 1, #whoShareMyInfo do
			if whoShareMyInfo[i]["meetid"] == oldMeetId then
			whoShareMyInfo[i]["meetid"] =temp["meetid"]
			whoShareMyInfo[i]["status"] = 1
			-- write moduledata
			local jsonPara = "{\"type\":\"meet_here\",\"msg\":\"" .. temp["content"] .. "\", \"phone\":\"" .. whoShareMyInfo[i]["phone"]
				.. "\", \"name\":\"" .. whoShareMyInfo[i]["name"] .. "\"}"
			tiros.moduledata.moduledata_set("web","sharepoi",jsonPara)
			refreshHistory(1)
			tiros.airtalkeemgr.UploadPositionInfo(1)
			break
			end
		end
	else
		--print("meet processRefreshMeetOne  FAIL to get")
		for i = 1, #whoShareMyInfo do
			if whoShareMyInfo[i]["meetid"] == oldMeetId then
			table.remove(whoShareMyInfo, i)
			result = 0
			if #whoShareMyInfo == 0  then
				--print("meet processRefreshMeetOne  updateMeetButtonDataStatus")
				updateMeetButtonDataStatus(1, "0")
				tiros.airtalkeemgr.StopUploadPositionInfo(1)
			end
			refreshHistory(1)
			break
			end
		end
	end
	if userCallBackFn ~= nil then
		--print("meet processRefreshMeetOne  userCallBackFn")
		userCallBackFn(userId, result)
	else
		local msg = nil
		--print("meet processRefreshMeetOne  initNotifyFun")
		if result == 1 then
			msg = "{\"meetid\":" .. temp["meetid"] .. ", \"module\":\"web\", \"key\":\"sharepoi\"}"
		end
		local nFunction, nUser = getPlatformFunctionAndUser();
		commlib.initNotifyFun(nFunction, nUser, 161, result, msg);
	end
	--print("meet processRefreshMeetOne  run out")
end

--再次发送链接请求，得到新的短信，见面二
local function processRefreshMeetTwo(httpData)
	--print("meet processRefreshMeetTwo  run in")
	local temp = tiros.json.decode(httpData)
	local result = 1
	if temp["success"] ==  true then
		--print("meet processRefreshMeetTwo  success to get")
		for i = 1, #iShareWho do
			if iShareWho[i]["meetid"] == oldMeetId then
			iShareWho[i]["meetid"] =temp["meetid"]
			--print("meet processRefreshMeetTwo  meetid refresh")
			refreshHistory(2)
			-- write moduledata
			local jsonPara = "{\"type\":\"meet_here\",\"msg\":\"" .. temp["content"] .. "\", \"phone\":\"" .. iShareWho[i]["phone"]
				.. "\", \"name\":\"" .. iShareWho[i]["name"] .. "\"}"
			tiros.moduledata.moduledata_set("web","sharepoi",jsonPara)
			tiros.airtalkeemgr.UploadPositionInfo(2)
			break
			end
		end
	else
		--print("meet processRefreshMeetTwo  failed to get")
		for i = 1, #iShareWho do
			if iShareWho[i]["meetid"] == oldMeetId then
			--print("meet processRefreshMeetTwo  set status 0")
			iShareWho[i]["status"] = 0
			refreshHistory(2)
			result = 0
			
			local stop = 1
			local updateColor = 1
			--print("meet processRefreshMeetTwo test share count")
			for k = 1, #iShareWho do
				if iShareWho[k]["status"] == 1 then
					stop = 0
					updateColor = 0
					break
				elseif iShareWho[k]["status"] == -1 then
					stop = 0
				end
			end
			--如果都处于断开状态，需要修改update moduledata
			if updateColor == 1 then
				--print("meet processRefreshMeetTwo updateColor")
				updateMeetButtonDataStatus(2, "0")
			end
			--如果没有数据或者全都是断开状态，停止上报。
			if stop == 1 then
				--print("meet processRefreshMeetTwo stop upload")
				tiros.airtalkeemgr.StopUploadPositionInfo(2)
			end
			
			
			break
			end
		end
	end
	if userCallBackFn ~= nil then
		--print("meet processRefreshMeetTwo userCallBackFn")
		userCallBackFn(userId, result)
	else
		--print("meet processRefreshMeetTwo initNotifyFun")
		local msg = nil
		if result == 1 then
			msg = "{\"meetid\":" .. temp["meetid"] .. ", \"module\":\"web\", \"key\":\"sharepoi\"}"
		end
		local nFunction, nUser = getPlatformFunctionAndUser();
		commlib.initNotifyFun(nFunction, nUser, 161, result, msg);
	end
end


--当发生错误的时候，通知调用者
local function errorProcess(errorType)
	--print("meet errorProcess  run in")
	if errorType == "meetone" or errorType == "meettwo" then
		if userCallBackFn ~= nil then
			--print("meet errorProcess  userCallBackFn  meetone or meettwo");
			userCallBackFn(userId, 0)
		else
			local nFunction, nUser = getPlatformFunctionAndUser()
			commlib.initNotifyFun(nFunction, nUser, 159, 0, "\"error\":\"http error\"");
			--print("meet errorProcess  initNotifyFun  meetone or two");
		end

	elseif errorType == "remeetone" or errorType == "remeettwo" then
		if userCallBackFn ~= nil then
			--print("meet errorProcess  userCallBackFn  remeetone or remeettwo");
			userCallBackFn(userId, 0)
		else
			local nFunction, nUser = getPlatformFunctionAndUser()
			commlib.initNotifyFun(nFunction, nUser, 161, 0, "\"error\":\"http error\"");
			--print("meet errorProcess  initNotifyFun  remeetone or remeettwo");
		end
	elseif errorType == "reshareafterstop" then
		if userCallBackFn ~= nil then
			--print("meet errorProcess  userCallBackFn  reshareafterstop");
			userCallBackFn(userId, 0)
		end
	else  -- stop event
		if userCallBackFn ~= nil then
			--print("meet errorProcess  userCallBackFn  stop event");
			userCallBackFn(userId, 0)
		else
		local nFunction, nUser = getPlatformFunctionAndUser()
			commlib.initNotifyFun(nFunction, nUser, 160, 0, "\"error\":\"http error\"");
			--print("meet errorProcess  initNotifyFun  stop event");
		end
	end

	--print("meet errorProcess  run out")
end



local function processStopShare(httpData)
	local temp = tiros.json.decode(httpData)
	local result = 0
	--print("meet processStopShare  run in")

	if removeWho == 1 then
		if temp["success"] ==  true then
			--print("meet processStopShare  success true remove1")
			for i = 1, #whoShareMyInfo do
				if whoShareMyInfo[i]["meetid"] == temp["meetid"] then
					table.remove(whoShareMyInfo, i)
					if #whoShareMyInfo == 0 then
					--print("meet processStopShare  before pdateMeetButtonDataStatus")
						updateMeetButtonDataStatus(1, "0")
						tiros.airtalkeemgr.StopUploadPositionInfo(1)
					end
					result = 1
					refreshHistory(1)
					break
				end
			end
		end
		
	else
		if temp["success"] ==  true then
			--print("meet processStopShare  success true remove2")
			for i = 1, #iShareWho do
				if iShareWho[i]["meetid"] == temp["meetid"] then
				iShareWho[i]["status"] = 0
				local stopupload = 1
				for k = 1, #iShareWho do
					if iShareWho[k]["status"] == -1 or iShareWho[k]["status"] == 1 then
						stopupload = 0
						break
					end
				end
				if stopupload == 1 then
					tiros.airtalkeemgr.StopUploadPositionInfo(2)
				end
				refreshHistory(2)
				--print("meet processStopShare  refresh history")
				result = 1
				break
				end
			end
		end
	end
	if userCallBackFn ~= nil then
		--print("meet processStopShare  userCallBackFn")
		userCallBackFn(userId, result)
	else
		local msg = nil
		--print("meet processStopShare  initNotifyFun")
		local nFunction, nUser = getPlatformFunctionAndUser();
		commlib.initNotifyFun(nFunction, nUser, 160, result, msg);
	end

	--print("meet processStopShare  run out")
end


local function processRefreshAfterStop(httpData)
	--print("meet processRefreshAfterStop  run in")
	local temp = tiros.json.decode(httpData)
	local result = 0
	if temp["success"] ==  true then
		--print("meet processRefreshAfterStop success get sms")
		for i = 1, #iShareWho do
		--print("meet in meetid = , oldMeetId= " ,iShareWho[i]["meetid"], oldMeetId);
			if iShareWho[i]["meetid"] == oldMeetId then
				iShareWho[i]["meetid"] = temp["meetid"]
				iShareWho[i]["status"] = -1
				-- write moduledata
				local jsonPara = "{\"type\":\"meet_here\",\"msg\":\"" .. temp["content"] .. "\", \"phone\":\"" .. iShareWho[i]["phone"]
					.. "\", \"name\":\"" .. iShareWho[i]["name"] .. "\"}"
				tiros.moduledata.moduledata_set("web","sharepoi",jsonPara)
				refreshHistory(2)
				--print("meet processRefreshAfterStop refreshHistory")
				result = 1
				tiros.airtalkeemgr.UploadPositionInfo(2)
			break
			end
		end
	end
	if userCallBackFn ~= nil then
		--print("meet processRefreshAfterStop userCallBackFn")
		userCallBackFn(userId, result)
	else
		local msg = nil
		if result == 1 then
			msg = "{\"meetid\":" .. temp["meetid"] .. ", \"module\":\"web\", \"key\":\"sharepoi\"}"
		end
		local nFunction, nUser = getPlatformFunctionAndUser();
		--print("meet processRefreshAfterStop initNotifyFun")
		commlib.initNotifyFun(nFunction, nUser, 161, result, msg);
	end

	--print("meet processRefreshAfterStop  run out")
end


--http 回调函数
local function meetHttpCallback(pType, nEvent, param1, param2)
	--print("meet meetHttpCallback  run in")
	----print("meet http call back")
	if nEvent == 1 then
	elseif nEvent == 2 then
		if param1 ~= 200 then
		clearMemberData()
		errorProcess(pType)
		--NOTIFY CLIENT ERROR
		tiros.http.httpabort(pType)
		end
	elseif nEvent == 3 then
		if httpData ~= nil then
			httpData = httpData..string.sub(param2, 1, param1);
		else
			httpData = string.sub(param2, 1, param1);
		end
	elseif nEvent == 4 then
		if pType == "meetone" then
			processMeetOneDefaultSMS(httpData)
		elseif pType == "meettwo" then
			processMeetTwoDefaultSMS(httpData)
		elseif pType == "remeetone" then
			processRefreshMeetOne(httpData)
		elseif pType == "remeettwo" then
			processRefreshMeetTwo(httpData)
		elseif pType == "stopmeet" then
			processStopShare(httpData)
		elseif pType == "reshareafterstop" then
			processRefreshAfterStop(httpData)
		end
		clearMemberData()
		tiros.http.httpabort(pType)
	elseif nEvent == 5 then
		errorProcess(pType)
		clearMemberData()
		tiros.http.httpabort(pType)
	else
	end

	--print("meet meetHttpCallback  run out")
end





--发送请求获取短信
local function sys_GetDefaultMessage(id, jsonStr, meetType, callbackFn)
	--print("meet sys_GetDefaultMessage  run in")
	local meetURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",2101)
	local typeInfo = "?type=" .. meetType
	local version = "&v=2"
	meetURL = meetURL .. typeInfo .. version
	local httpHandler = "meetone"
	if meetType == 2 then
		httpHandler = "meettwo"
	end
	userId = id
	local input = tiros.json.decode(jsonStr)
	tempUser = {}
	tempUser["phone"] = input["phone"]
	tempUser["name"] = input["name"]
	tempUser["meetid"] = nil
	tempUser["status"] = -1
	----print("GetDefaultMessage name is " .. tempUser["name"])

	if meetType == 2 then
		tempUser["x"] = 0
		tempUser["y"] = 0
		tempUser["time"] = "0"
	end
	userCallBackFn = callbackFn
        tiros.http.httpsendforlua("cdc_client", "meet_ShareURL", httpHandler, meetURL, meetHttpCallback, nil,
	 "actionlocation:/navidog2News/meet_ShareURL.htm")

	 --print("meet sys_GetDefaultMessage  run out")
end

--停止共享
local function sys_StopShare(id, meetId, meetType, callbackFn)
	--print("meet sys_StopShare  run in")
	local stopURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",2101)
	local mid = "?meetid=" .. meetId
	local clientType = "&clienttype="
	local cType = 1
	if callbackFn == nil then
		cType =  2
	end
	clientType = clientType .. cType
	stopURL = stopURL .. mid .. clientType
	local httpHandler = "stopmeet"
	userCallBackFn = callbackFn
	userId = id
	oldMeetId = meetId
	removeWho = meetType

        tiros.http.httpsendforlua("cdc_client", "meet_stopTypeShare", httpHandler, stopURL, meetHttpCallback, nil,
	 "actionlocation:/navidog2News/meet_stopTypeSharing.htm")

	 --print("meet sys_StopShare  run out")
end

--发送重新获取的请求，得到默认短信，更新内部信息
local function sys_RefreshShare(id, meetId, meetType, callbackFn)
	--print("meet sys_RefreshShare  run in")
	local meetURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",2101)
	local version = "?v=2"
	local sendType = "&type=" .. meetType

	local sendMeetId = "&meetid=".. meetId
	meetURL = meetURL .. version .. sendType .. sendMeetId

	local httpHandler = "remeetone"
	if meetType == 2 then
		httpHandler = "remeettwo"
	end
	userId = id
	userCallBackFn = callbackFn
	oldMeetId = meetId
        tiros.http.httpsendforlua("cdc_client", "meet_reShareURL", httpHandler, meetURL, meetHttpCallback, nil,
	 "actionlocation:/navidog2News/meet_reShareURL.htm")

	 --print("meet sys_RefreshShare  run out")

end

--只有见面二才用
local function sys_RefreshShareAfterStop(id, meetId, callbackFn)
	--print("meet sys_RefreshShareAfterStop  run in")
	local meetURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",2101)
	local version = "?v=2"
	local sendType = "&type=2"

	local sendMeetId = "&meetid=".. meetId
	meetURL = meetURL .. version .. sendType .. sendMeetId

	local httpHandler = "reshareafterstop"
	userId = id
	userCallBackFn = callbackFn
	oldMeetId = meetId
        tiros.http.httpsendforlua("cdc_client", "meet_reShareURL", httpHandler, meetURL, meetHttpCallback, nil,
	 "actionlocation:/navidog2News/meet_reShareURL.htm")
--print("meet sys_RefreshShareAfterStop  run out")
end


--logic端获取某联系人的坐标
local function sys_GetPositionInfoByMeetId(meetId)
	--print("meet sys_GetPositionInfoByMeetId  run in parameter meetid is " .. meetId)
	for i = 1, #iShareWho do
		--print("meet sys_GetPositionInfoByMeetId  iShareWho meetid is " .. iShareWho[i]["meetid"])
		if iShareWho[i]["meetid"] == meetId then
		--print("meet sys_GetPositionInfoByMeetId  get result, x, y and time are" ..iShareWho[i]["x"] ..",".. iShareWho[i]["y"]..","..iShareWho[i]["time"])
		return iShareWho[i]["x"], iShareWho[i]["y"], iShareWho[i]["time"]
		end
	end
	--print("meet sys_GetPositionInfoByMeetId  run out")
	return 0, 0, "error"
end

local function sys_RebuildPoiJsonStr(jsonStr, lon, lat)
	local jsonData = tiros.json.decode(jsonStr)
	if jsonData.address ~= nil then
		jsonData.address = ""
	end
	if jsonData.lon ~= nil then
		jsonData.lon = tostring(lon)
	end
	if jsonData.lat ~= nil then
		jsonData.lat = tostring(lat)
	end
	local result = tiros.json.encode(jsonData)
	return result
end



local function sys_PorcessServerBatchEvent(job)
--print("meet sys_PorcessServerBatchEvent  run in")
	if job["type"] == 11 then
		--用于获取接收客户端上报位置信息，更新见面二的坐标系统，加上meetid，转发通知
		--print("meet sys_PorcessServerBatchEvent  event type is 11")
		for j = 1, #iShareWho do
			if iShareWho[j]["meetid"] == job["meetid"] then
				--print("meet sys_PorcessServerBatchEvent  event type is 11 fine the meetid")
				if job["isdeviate"] ~= 2 then
					iShareWho[j]["x"],iShareWho[j]["y"] = encryptiongpslib.encryptiongps(job["x"], job["y"]);
				else
					iShareWho[j]["x"] = job["x"]
					iShareWho[j]["y"] = job["y"]
				end
				iShareWho[j]["time"] = tiros.commfunc.CurrentTime()
				--print("meet sys_PorcessServerBatchEvent type is 11  update x y time, meetid is " ..job["meetid"])
				--notify logic
				local func, usr = getLogicFunctionAndUser();
				if func ~= nil then
					commlib.universalnotifyFun(func,"meet2 send position update to you", usr, 7, 6, job["meetid"]);
				end			
			break
			end
		end

	elseif job["type"] == 10 then
	--print("meet sys_PorcessServerBatchEvent  event type is 10, somebody stop share")
	--见面2功能web网页停止分享位置
		for j = 1, #iShareWho do
			if iShareWho[j]["meetid"] == job["meetid"] then
				--print("meet sys_PorcessServerBatchEvent  event type is 10 fine the meetid")
				iShareWho[j]["status"] = -2
				refreshHistory(2)
				break
			end
		end
	elseif job["type"] == 1 then	--见面二，对方接受，收到对方经纬度信息,通知平台更新位置
		local nFunction, nUser = getPlatformFunctionAndUser();
		--print("meet sys_PorcessServerBatchEvent  event type is 1, somebody agree")
		for j = 1, #iShareWho do
			if iShareWho[j]["meetid"] == job["meetid"] then
				--print("meet sys_PorcessServerBatchEvent  event type is 1 fine the meetid")
				iShareWho[j]["status"] = 1
				if job["isdeviate"] ~= 2 then
					iShareWho[j]["x"],iShareWho[j]["y"] = encryptiongpslib.encryptiongps(tonumber(job["x"]), tonumber(job["y"]));
				else
					iShareWho[j]["x"] = tonumber(job["x"])
					iShareWho[j]["y"] = tonumber(job["y"])
				end
				iShareWho[j]["time"] = tiros.commfunc.CurrentTime()
				refreshHistory(2)
				local para = {}
				para.meetid = iShareWho[j]["meetid"]
				para.phone = iShareWho[j]["phone"]
				para.name = iShareWho[j]["name"]
				para.lon = tostring(iShareWho[j]["x"])
				para.lat = tostring(iShareWho[j]["y"])
				para.poigid = ""
				local data145 = tiros.json.encode(para)
				commlib.initNotifyFun(nFunction, nUser, 145, 0, data145);
				break
			end
		end

	elseif job["type"] == 2 then
		--print("meet sys_PorcessServerBatchEvent  event type is 2 clean")
		whoShareMyInfo = {}
		refreshHistory(1)
		for j = 1, #iShareWho do
			iShareWho[j]["status"] = 0
		end
		refreshHistory(2)
	end
	--print("meet sys_PorcessServerBatchEvent  run out")
end

--处理server主动发过来的事件，通常会通知client
local function sys_ProcessServerEvent(jsonStr)
	--print("meet sys_ProcessServerEvent  run in")
	local temp = tiros.json.decode(jsonStr)
	if temp["type"] == 11 then
		--用于获取接收客户端上报位置信息，更新见面二的坐标系统，加上meetid，转发通知
		--update my data
		--print("meet sys_ProcessServerEvent  event type is 11")
		for j = 1, #iShareWho do
			if iShareWho[j]["meetid"] == temp["meetid"] then
			times = times + 1
			if times > 11 then
				print("meet stop send msg to logic")
				break
			end
			if temp["isdeviate"] ~= 2 then
				iShareWho[j]["x"],iShareWho[j]["y"] = encryptiongpslib.encryptiongps(tonumber(temp["x"]), tonumber(temp["y"]));
			else
				iShareWho[j]["x"] = tonumber(temp["x"])
				iShareWho[j]["y"] = tonumber(temp["y"])
			end
			iShareWho[j]["time"] = tiros.commfunc.CurrentTime()
			--print("meet sys_ProcessServerEvent  event type is 11  update x y time")
			--notify logic
			local func, usr = getLogicFunctionAndUser();
			if func ~= nil then
				print("meet send msg to logic")
				commlib.universalnotifyFun(func,"meet2 send position update to you", usr, 7, 6, temp["meetid"]);
			end			
			break
			end
		end

	elseif temp["type"] == 10 then
	--print("meet sys_ProcessServerEvent  event type is 10, somebody stop share")
	--见面2功能web网页停止分享位置
		for j = 1, #iShareWho do
			if iShareWho[j]["meetid"] == temp["meetid"] then
				iShareWho[j]["status"] = -2
				refreshHistory(2)
				local stop = 1
				local updateColor = 1
				--print("meet sys_ProcessServerEvent  event type is 10  after refreshHistory")
				for k = 1, #iShareWho do
					if iShareWho[k]["status"] == 1 then
						stop = 0
						updateColor = 0
						break
					elseif iShareWho[k]["status"] == -1 then
						stop = 0
					end
				end
				--如果都处于断开状态，需要修改update moduledata
				if updateColor == 1 then
					--print("meet sys_ProcessServerEvent  event type is 10 updateMeetButtonDataStatus")
					updateMeetButtonDataStatus(2, "0")
				end
				print("meet sys_ProcessServerEvent send 518")
				notify518()
				--如果没有数据或者全都是断开状态，停止上报。
				if stop == 1 then
					--print("meet sys_ProcessServerEvent StopUploadPositionInfo")
					tiros.airtalkeemgr.StopUploadPositionInfo(2)
				end
				break
			end
		end
	elseif temp["type"] == 1 then	--见面二，对方接受，收到对方经纬度信息,通知平台更新位置
		local nFunction, nUser = getPlatformFunctionAndUser();
		--print("meet sys_ProcessServerEvent  event type is 1, somebody agree")--#对于非table而言，for会崩溃
		for j = 1, #iShareWho do
			if iShareWho[j]["meetid"] == temp["meetid"] then
				iShareWho[j]["status"] = 1
				if temp["isdeviate"] ~= 2 then
					iShareWho[j]["x"],iShareWho[j]["y"] = encryptiongpslib.encryptiongps(tonumber(temp["x"]), tonumber(temp["y"]));
				else
					iShareWho[j]["x"] = tonumber(temp["x"])
					iShareWho[j]["y"] = tonumber(temp["y"])
				end
				iShareWho[j]["time"] = tiros.commfunc.CurrentTime()
				refreshHistory(2)
				--print("meet sys_ProcessServerEvent before updateMeetButtonDataStatus")
				updateMeetButtonDataStatus(2, "1")				
				notify518()
				--print("meet sys_ProcessServerEvent after 518")
				local para = {}
				para.meetid = iShareWho[j]["meetid"]
				para.phone = iShareWho[j]["phone"]
				para.name = iShareWho[j]["name"]
				para.lon = tostring(iShareWho[j]["x"])
				para.lat = tostring(iShareWho[j]["y"])
				para.poigid = ""
				local data145 = tiros.json.encode(para)
				commlib.initNotifyFun(nFunction, nUser, 145, 0, data145)
			break
			end
		end

	elseif temp["type"] == 2 then
		--print("meet sys_ProcessServerEvent  event type is 2 clean")
		whoShareMyInfo = {}
		for j = 1, #iShareWho do
			iShareWho[j]["status"] = 0
		end
		refreshHistory(1)
		updateMeetButtonDataStatus(1, "0")
		tiros.airtalkeemgr.StopUploadPositionInfo(1)
		refreshHistory(2)
		updateMeetButtonDataStatus(2, "0")
		tiros.airtalkeemgr.StopUploadPositionInfo(2)
		--print("meet sys_ProcessServerEvent  send 518")
		notify518()
	elseif temp["type"] == 10021 then
		--print("meet sys_ProcessServerEvent  event type is 10021 bath process")
		local cmds = temp["msg"]--多个消息
		if cmds ~= nil then
			for i = 1, #cmds do
				local job = cmds[i]
				--print("meet sys_ProcessServerEvent  10021 do batch" .. i)
				sys_PorcessServerBatchEvent(job)
			end

			local stop = 1
			local hasShare = 0
			for k = 1, #iShareWho do
				if iShareWho[k]["status"] == 1 then
					stop = 0
					hasShare = 1
					break
				elseif iShareWho[k]["status"] == -1 then
					stop = 0
				end
			end
			--如果都处于断开状态，需要修改update moduledata
			if hasShare == 0 then
				updateMeetButtonDataStatus(2, "0")
			else
				updateMeetButtonDataStatus(2, "1")
			end
			--print("meet sys_ProcessServerEvent send 518")
			notify518()
			--如果没有数据或者全都是断开状态，停止上报。
			if stop == 1 then
				--print("meet sys_ProcessServerEvent StopUploadPositionInfo")
				tiros.airtalkeemgr.StopUploadPositionInfo(2)
			end
		end
	end
	--print("meet sys_ProcessServerEvent  run out")
end



--初始化meet的数据，从文件中获取历史记录
local function sys_InitMeet()
	--print("meet sys_InitMeet  run in")
	if firstCallInit == 0 then
		firstCallInit = 1
		local data = nil
		local err = nil
		data, err = tiros.file.Readfile("fs2:/meet/one");
		if err == 1 then
			whoShareMyInfo = tiros.json.decode(data)
		else
			--print("nothing in whosharemyinfo")
		end

		data, err = tiros.file.Readfile("fs2:/meet/two");
		if err == 1 then
			iShareWho = tiros.json.decode(data)
		else
			--print("nothing in iShareWho")
		end
	end

	local send = 0
	local upload = 0
	if #iShareWho > 0 then
	--print("meet sys_InitMeet  update  meet button 2")
		for i = 1, #iShareWho do
			if iShareWho[i]["status"] == 1 then
			send = 1
			upload = 1
			break
			elseif  iShareWho[i]["status"] == -1 then
			upload = 1
			end
		end
		if send == 1 then
			updateMeetButtonDataStatus(2, "1")
		else
			updateMeetButtonDataStatus(2, "0")
		end
		if upload == 1 then
			tiros.airtalkeemgr.UploadPositionInfo(2)
		end
	end

	if #whoShareMyInfo > 0 then
	--print("meet sys_InitMeet  update  meet button 1")
		updateMeetButtonDataStatus(1, "1")
		tiros.airtalkeemgr.UploadPositionInfo(1)
	end
	
	if send == 1 then
		notify518()
	end

--print("meet sys_InitMeet  run out")

end




--=========================================外部接口=========================================


--获取见面一或见面二的历史记录
--id：调用者的id（string），web需要
--meettype：见面一还是见面二（1，2）
--callbackfn：web的回调函数（callbackfn(string id, int status， table data)）
--输出：无
createmodule(interface, "GetMeetRecord", function(id, meetType, callbackFn)
	 sys_GetMeetRecord(id, meetType, callbackFn)
end)

--获得见面一或见面二默认短信
--id：调用者的id（string），web需要
--jsonStr：接收短信的人的手机号码,名字
--meettype：见面一还是见面二（1，2）
--callbackfn：回调函数  callbackfn(string id, int status)，平台（nil）
--输出：无
createmodule(interface, "GetDefaultMessage", function(id, jsonStr, meetType, callbackFn)
	 sys_GetDefaultMessage(id, jsonStr, meetType, callbackFn)
end)

--停止共享
--id：调用者的id（string），web需要
--meetid：见面id
--meettype：取消的id属于见面一还是见面二（1，2）
--callbackfn：回调函数  callbackfn(string id, int status)，平台（nil）
--输出：无
createmodule(interface, "StopShare", function(id, meetId, meetType, callbackFn)
	 sys_StopShare(id, meetId, meetType, callbackFn)
end)

--重新获得见面一或见面二的默认短信
--id：调用者的string id标识，web需要
--meetid：见面id
--meettype：见面一还是见面二（1，2）
--callbackfn：回调函数  callbackfn(string id, int status)，平台（nil）
--输出：无
createmodule(interface, "RefreshShare", function(id, meetId, meetType, callbackFn)
	 sys_RefreshShare(id, meetId, meetType, callbackFn)
end)

--处理server发来的相关事件
--jsonstr：JSon字符串
createmodule(interface, "ProcessServerEvent", function(jsonStr)
	sys_ProcessServerEvent(jsonStr)
end)

--加载文件中的数据到内存
createmodule(interface, "InitMeet", function()
	sys_InitMeet()
end)


--logic获取见面二中某个联系人的坐标变化
--meetId:logic端想获得哪个联系人的坐标
--输出：x坐标和y坐标，两个返回值
createmodule(interface, "GetPositionInfoByMeetId", function(meetId)
	return sys_GetPositionInfoByMeetId(meetId)
end)

createmodule(interface, "RebuildPoiJsonStr", function(jsonStr, lon, lat)
	return sys_RebuildPoiJsonStr(jsonStr, lon, lat)
end)


--delete share
createmodule(interface, "DeleteShareByMeetId", function(id, meetId, meetType, callbackFn)
	return sys_DeleteShareByMeetId(id, meetId, meetType, callbackFn)
end)

--send share request after stop share
createmodule(interface, "RefreshShareAfterStop", function(id, meetId, callbackFn)
	return sys_RefreshShareAfterStop(id, meetId, callbackFn)
end)


tiros.meet = readOnly(interface)


