--[[
-- @描述:客户端登录管理:目前管理版本检测,请求aid,犬号登录,爱滔客登录
-- @编写人:宣东言
-- @创建日期: 2013-1-17 13:40:16
-- @修改内容:
--]]

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/login/sys_login"
require"lua/loginstatus"
require"lua/getaid"
require"lua/loginupdate"
require"lua/airtalkeemgr"
require"lua/file"
require"lua/favupgrade"
require"lua/getairtalkeeip"

local isVersionCheck = false;

--启动自动犬号登录
local webres = tiros.web.FilePath;
local path = webres .. "user/lua/userautologin"
require (path)



--user配置文件路径
local gsUserFileName = "fs4:/user"
--Uid登录成功以后是否需要登录MobiAid
local gbMobiAid;
--接口table
local interface = {};

--[[
--@描述:从数据仓库获取回调函数地址和pUser
--@param  无
--@return 回调函数地址和pUser
--]]
local function getFunctionAndUser()
    local nFunction = tiros.moduledata.moduledata_get("framework", "pfunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "puser");
    if nFunction == nil or nUser == nil then
       print("loginmanager--getFunctionAndUser--error");
    else
        return nFunction, nUser;
    end
end

--[[
--@描述:版本检测登录(login)
--@param  无
--@return 无
--]]
local function checkVersionLogin()
	if isVersionCheck == true then
		return;	
	end
	isVersionCheck = true;
	local nFunction, nUser = getFunctionAndUser();
	return tiros.login.login(nFunction, nUser);
end

--[[
--@描述:犬号自动登录(uid)
--@param  无
--@return 无
--]]
local function uidAutoLogin()
	tiros.web.userautologin.WebAutoLogin();
end

--[[
--@描述:通过mobileid从服务器请求aid
--@param  无
--@return 无
--]]
local function getAidByMobileid()
	tiros.getaid.getaid(1);
end

--[[
--@描述:从数据仓库把3x->4x的数据拿出来写到数据仓库
--@param  无
--@return 无
--]]
local function save3xData()
	print("loginmanager--save3xData-start");
	local s3xData = tiros.moduledata.moduledata_get("framework", "3xData");
	print("loginmanager--3xData=",s3xData);

	local t3xData = tiros.json.decode(s3xData);
	local t4xData = {};
	t4xData.phone = t3xData.phone;
	t4xData.uid = t3xData.uid;
	t4xData.pwd = t3xData.pwd;
	t4xData.uid_aid = t3xData.aid;

	local s4xData = tiros.json.encode(t4xData);
	print("loginmanager--3xData=",s4xData);
	tiros.file.Writefile(gsUserFileName, s4xData, true);
end

--判断是否可以进行aid登录
local function canAidLogin(nType)

	local sData = tiros.file.Readfile(gsUserFileName);
	local tData;
	local sAid = nil
	local cfg_sp = nil
	local cfg_sp_port = nil
	local cfg_sp_lport = nil
	local cfg_mdsr = nil
	local cfg_mdsr_port = nil
	if (sData ~= nil and sData ~= "") then
		tData = tiros.json.decode(sData);
		if (nType == 3) then
			sAid = tData["mobileid_aid"];
			cfg_sp = tData["cfg_sp"];
			cfg_sp_port = tData["cfg_sp_port"];
			cfg_sp_lport = tData["cfg_sp_lport"];
			cfg_mdsr = tData["cfg_mdsr"];
			cfg_mdsr_port = tData["cfg_mdsr_port"];
		else
			sAid = tData["uid_aid"];
			cfg_sp = tData["cfg_sp"];
			cfg_sp_port = tData["cfg_sp_port"];
			cfg_sp_lport = tData["cfg_sp_lport"];
			cfg_mdsr = tData["cfg_mdsr"];
			cfg_mdsr_port = tData["cfg_mdsr_port"];
		end
	end

	if(sAid ~= "" and cfg_sp ~= "" and cfg_sp_port ~= "" and cfg_sp_lport ~= "" and cfg_mdsr ~= "" and cfg_mdsr_port ~= "") then
		return true
	else
		return false
	end
end


--nType	0.3x版本到4x版本升级 1.通过mobileid从服务器请求aid 2.犬号登录 3.mobile_aid爱滔客登录 4.uid_aid爱滔客登录 5.获取爱滔客的5个登录相关参数
--nStatus 成功:true 失败:false
local function loginStatusNotify(sID ,nType, nStatus)
	print("loginStatusNotify--", sID, nType, nStatus);

	--如果请求aid成功则通过mobile_aid登录
	if(nType == 1 and nStatus == true and gbMobiAid == true) then
		interface.AidLogin(3);
	end

	if(nType == 5 and nStatus == true) then
		interface.AidLogin(4);
	end

	--如果Uid登录成功则通过uid_aid登录
	if(nType == 2 and nStatus == true) then
		local ret = canAidLogin(4);
		if ret == true then
			interface.AidLogin(4);
		else
			tiros.getairtalkeeip.getairtalkeeip(1);
		end
		--复制公共收藏夹中的数据
		tiros.favoriteupgrade.favupgrade();
	end

	--如果3x版本到4x版本升级
	if(nType == 0) then
		if nStatus == true then
			save3xData();
		end
		interface.login();
	end
end

--[[
--@描述:爱滔客自动登录(aid)
--@param  nType 3代表MibiAid登录 4代表UidAid登录
--@return 无
--]]
createmodule(interface,"AidLogin",function (nType)
	--判断fs4里有没有user文件
	local bExist = filelib.fexist(gsUserFileName);

	--如果fs4里没有user文件
	if (bExist ~= true) then
		print("loginmanager--aidAutoLogin--error:user file not exist",nType);
		return;
	end
	--打开并读取数据
	local sData = tiros.file.Readfile(gsUserFileName);
	local tData;
	local sAid;
	local sPassword;
	local cfg_sp;
	local cfg_sp_port;
	local cfg_sp_lport;
	local cfg_mdsr;
	local cfg_mdsr_port;
	if (sData ~= nil and sData ~= "") then
		--转换成table
		tData = tiros.json.decode(sData);
		cfg_sp = tData["cfg_sp"];
		cfg_sp_port = tData["cfg_sp_port"];
		cfg_sp_lport = tData["cfg_sp_lport"];
		cfg_mdsr = tData["cfg_mdsr"];
		cfg_mdsr_port = tData["cfg_mdsr_port"];
		--获取对应value
		if(nType == 3) then
			sAid = tData["mobileid_aid"];
			sPassword = tiros.moduledata.moduledata_get("framework", "mobileid");

			if(sAid == nil or sAid == "") then
				gbMobiAid = true;
				getAidByMobileid();
				return;
			end
		else
			sAid = tData["uid_aid"];
			sPassword = tData["uid"];
		end
		tiros.airtalkeemgr.configserver(cfg_sp, cfg_sp_port, cfg_sp_lport, cfg_mdsr, cfg_mdsr_port);
		tiros.airtalkeemgr.login(nType, sAid, sPassword);
	end
end)

--[[
--@描述:登录
--@param  无
--@return 无
--]]
createmodule(interface,"login",function ()

	--判断fs4里有没有user文件
	local bExist = filelib.fexist(gsUserFileName);
	--如果fs4里没有user文件,则直接启动版本检测服务
	if (bExist == false) then
		gbMobiAid = true;
		getAidByMobileid();
		checkVersionLogin();
		return;
	end
	--如果fs4里有user文件
	if (bExist == true) then
		--打开并读取数据
		local sData = tiros.file.Readfile(gsUserFileName);
		local tData;
		local sUid, sPhone, sPasswd;
		--如果user文件有内容
		if (sData ~= nil and sData ~= "") then
			--转换成table
			tData = tiros.json.decode(sData);
			--获取对应value
			sUid = tData["uid"];
			sPhone = tData["phone"];
			sPasswd = tData["pwd"];
		end
		--如果uid phone passwd都有内容
		if(sUid ~= nil and sPhone ~= nil and sUid ~= "" and sPhone ~= "") then
			gbMobiAid = false;
			uidAutoLogin();
			checkVersionLogin();
		else
			gbMobiAid = true;
			getAidByMobileid();
			checkVersionLogin();
		end
	end
end)

--[[
--@描述:判断是否满足自动登录条件
--@param  无
--@return true代表满足 false代表不满足
--]]
createmodule(interface,"hasAutoLogin",function ()
	local bExist = filelib.fexist(gsUserFileName);

	if (bExist == false) then
		return false;
	end

	--如果fs4里有user文件
	if (bExist == true) then
		--打开并读取数据
		local sData = tiros.file.Readfile(gsUserFileName);
		local tData;
		local sUid, sPhone;
		--如果user文件有内容
		if (sData ~= nil and sData ~= "") then
			--转换成table
			tData = tiros.json.decode(sData);
			--获取对应value
			sUid = tData["uid"];
			sPhone = tData["phone"];
		end
		--如果uid phone都有内容
		if(sUid ~= nil and sPhone ~= nil and sUid ~= "" and sPhone ~= "") then
			return true;
		else
			return false;
		end
	end
end)

createmodule(interface,"loginmanager",function ()
	--注册登录监听回调
	tiros.loginstatus.getLoginStatus("loginManagerGetLoginStatus", loginStatusNotify);
	--调用3x->4x的升级脚本
	tiros.loginupdate.Upgrade();
end)

tiros.loginmanager = readOnly(interface);


