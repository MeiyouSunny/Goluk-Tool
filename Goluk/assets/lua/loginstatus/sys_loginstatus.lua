--[[
-- @描述:登录状态web前端注册一个回调函数以后,接收犬号登录和爱滔客登录的状态通知
-- @编写人:宣东言
-- @创建日期: 2013-1-16 13:40:16
-- @修改内容:
--]]

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"

--登录类型 1.版本检测 2.犬号登录 3.mobile_aid爱滔客登录 4.uid_aid爱滔客登录
local gnType;
--登录状态 1.请求 2.应答 3.数据体 4.完成 5.错误
local gnLoginStatus;
--存放Web前端注册过来的回调函数的表
local gtHandle = {};
--保存所有id
local gtIDs = {};
--接口table
local interface = {};

--[[
--@描述:注册登录状态的回调函数
--@param  fnNotify ,注册的回调函数地址
--@param  sID ,调用着唯一id
--@return 无
--]]
createmodule(interface,"getLoginStatus",function (sID, fnNotify)

	--保存sID对应的回调函数
	local tLoginStatus = {};
	tLoginStatus.notify = fnNotify;
	registerHandle(gtHandle, nil ,sID, tLoginStatus);

	--保存sID
	table.insert(gtIDs, sID);

	fnNotify(sID, gnType ,gnLoginStatus);
end)

--[[
--@描述:取消注册登录状态的回调函数
--@param  无
--@return 无
--]]
createmodule(interface,"cancelLoginStatus",function (sID)

	local tLoginStatus = getHandle(gtHandle, sID);
	if(tLoginStatus ~= nil) then
		tLoginStatus.notify = nil;
	end
end)

--[[
--@描述:设置登录状态
--@param  nType 1.版本检测 2.犬号登录 3.mobile_aid爱滔客登录 4.uid_aid爱滔客登录
--@param  nStatus 成功:true 失败:false
--@return 无
--]]
createmodule(interface,"setLoginStatus",function (nType, nStatus)

	gnType = nType;
	gnLoginStatus = nStatus;

	for key,value in pairs(gtIDs) do
		local tLoginStatus = getHandle(gtHandle, value);
		if(tLoginStatus.notify ~= nil) then
			tLoginStatus.notify(value, gnType ,gnLoginStatus);
		end
	end

--	if nType == 1 and sStatus == true then
--		tiros.airtalkeemgr.login(3,"");
--	end
end)
tiros.loginstatus = readOnly(interface);

--file end

