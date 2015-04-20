--登录状态

require"lua/loginstatus/sys_loginstatus"


--[[---------------------------------------------------------------------

require"lua/loginstatus"

local function loginStatusNotify(sID ,nType, nStatus)

	print("loginStatusNotify--", sID, nType, nStatus);

	--nType	1.请求aid 2.犬号登录 3.mobile_aid爱滔客登录 4.uid_aid爱滔客登录

	--nStatus 成功:true 失败:false
end

--[
--@描述:注册登录状态的回调函数
--@param  fnNotify ,注册的回调函数地址
--@param  sID ,调用着唯一id
--@return 无
--]
tiros.loginstatus.getLoginStatus(sID, fnNotify)

--[
--@描述:取消注册登录状态的回调函数
--@param  sID 调用着唯一id
--@return 无
--]
tiros.loginstatus.cancelLoginStatus(sID)

--[
--@描述:设置登录状态
--@param  nType 1.请求aid 2.犬号登录 3.mobile_aid爱滔客登录 4.uid_aid爱滔客登录
--@param  nStatus 成功:true 失败:false
--@return 无
--]
tiros.loginstatus.setLoginStatus(nType, nStatus)

--]]----------------------------------------------------------------------
