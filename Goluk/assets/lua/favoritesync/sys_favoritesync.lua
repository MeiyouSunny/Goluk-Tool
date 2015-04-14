
--[[
-- @描述:收藏夹以及同步接口
-- @编写人:宣东言
-- @创建日期: 2013-05-23 13:51:11
--]]

require"lua/moduledata"
require"lua/systemapi/sys_namespace"

local gHandle;
local gNotify;
--接口table
local interface = {};

--[[
--@描述:创建收藏夹以及同步引擎
--@param  sUid String型参数，登录成功的用户的UID
--@return 请求成功返回true，失败返回false
--]]
createmodule(interface,"create",function (sUid)
	print("favortesync----create--",sUid);
	if sUid ~= nil then
		gHandle = favsynclib.create(sUid);
		return true;
	else
		return false;
	end
end)

--[[
--@描述:销毁收藏夹以及同步引擎
--@param  无
--@return 请求成功返回true，失败返回false
--]]
createmodule(interface,"destroy",function ()
	print("favortesync----destroy");
	favsynclib.destroy(gHandle);
end)

DeclareGlobal("sys_SyncNotify",function (event, param)
	print("favv-lua-sys_SyncNotify",event, param)
	gNotify(event, param);
end)

--[[
--@描述:注册回调函数
--@param  sNotify String型参数，回调函数名字
--@return 请求成功返回true，失败返回false
--]]
createmodule(interface,"notify",function (sNotify)
	print("favortesync----notify",sNotify);
	gNotify = sNotify;
	favsynclib.notify(gHandle,"sys_SyncNotify");
end)


--[[
--功能说明: 得到POI收藏的PO点的总个数
--参数:无
--返回值:  收藏点的总个数
--]]
createmodule(interface,"getcount",function ()
	print("favortesync----getcount");
	local nCount = favsynclib.getcount(gHandle);
	print("favortesync----getcount-",nCount);
	return nCount;
end)

--[[
--功能说明: 得到下标范围内POI点的数据，POI点下标 从0开始
--参数: nStart [in]开始的位置
--参数: nEnd   [in]结束的位置
--返回值:  成功返回要获得的POI数据所对应的json格式(时间倒序排列)
--]]
createmodule(interface,"getdata",function (nStart, nEnd)
	print("favortesync----getdata-",nStart, nEnd);
	local sData = favsynclib.getdata(gHandle,nStart, nEnd);
	print("favortesync----getdata-",sData);
	return sData;
end)


--[[
--功能说明: 根据PID得到某个POI点的数据
--参数:PID
--返回值:  成功返回要获得的POI数据所对应的json格式
--]]
createmodule(interface,"getsingledata",function (sPid)
	print("favortesync----getsingledata",sPid);
	local sData = favsynclib.getsingledata(gHandle, sPid);
	print("favortesync----getsingledata-",sData);
	return sData;
end)


--[[
--功能说明: 根据POIGID得到PID
--参数:sPoigid
--返回值:  PID
--]]
createmodule(interface,"getpid",function (sPoigid)
	print("favortesync----getpid");
	local sPid = favsynclib.getpid(gHandle,sPoigid);
	print("favortesync----getpid-",sPid);
	return sPid;
end)

--[[
--功能说明: 添加一个POI点数据
--参数:sJsonData [in]要添加的POI点数据,json格式
--参数:bIsRawData [in]外层连续添加时 传送true（for循环添加），外层单个添加-非连续添加时 传送false
--返回值:  false代表添加失败，true代表添加成功
--]]
createmodule(interface,"add",function (sJsonData, bIsRawData)
	print("favortesync----add",sJsonData,bIsRawData);
	local bSuccess = favsynclib.add(gHandle,sJsonData, bIsRawData);
	print("favortesync----add-",bSuccess);
	return bSuccess;
end)

--[[
--功能说明: 修改一个poi点
--参数:sJsonData [in]要修改的POI点数据,json格式（带着PID）
--返回值:  false代表添加失败，true代表添加成功
--]]
createmodule(interface,"modify",function (sJsonData)
	print("favortesync----modify",sJsonData);
	local bSuccess = favsynclib.modify(gHandle,sJsonData);
	print("favortesync----modify-",bSuccess);
	return bSuccess;
end)


--[[
--功能说明: 删除一个POI点数据
--参数:sPid [in]要删除的POI点数据对应的PID
--返回值:  false代表添加失败，true代表添加成功
--]]
createmodule(interface,"delbypid",function (sPid)
	print("favortesync----delbypid",sPid);
	local bSuccess = favsynclib.delbypid(gHandle,sPid);
	print("favortesync----delbypid-",bSuccess);
	return bSuccess;
end)


--[[
--功能说明: 删除所有的POI点的数据
--参数:无
--返回值:  false代表删除失败，true代表删除成功
--]]
createmodule(interface,"delall",function ()
	print("favortesync----delall");
	local bSuccess = favsynclib.delall(gHandle);
	print("favortesync----delall-",bSuccess);
	return bSuccess;
end)

--[[
--功能说明: 用poigid判断一个点是否存在
--参数:无
--返回值:  false代表不存在，true代表存在
--]]
createmodule(interface,"exist",function (sPoigid)
	print("favortesync----exist",sPoigid);
	local bEist = favsynclib.exist(gHandle,sPoigid);
	print("favortesync----exist-",bEist);
	return bEist;
end)


--[[
--功能说明: 判断POI点的链表是否已满
--参数:无
--返回值:  false未满，true代表满
--]]
createmodule(interface,"isfull",function ()
	print("favortesync----isfull");
	local bFull = favsynclib.isfull(gHandle);
	print("favortesync----isfull-",bFull);
	return bFull;
end)

--[[
--功能说明: 开始同步
--参数:无
--返回值:  false同步失败，true正常请求网络，等待回调
--]]
createmodule(interface,"startsync",function ()
	print("favortesync----startsync");
	local bSuccess = favsynclib.startsync(gHandle);
	print("favortesync----startsync-",bSuccess);
	return bSuccess;
end)


--[[
--功能说明: 判断是否正在同步中
--参数:无
--返回值:  false空闲，true繁忙
--]]
createmodule(interface,"syncisbusy",function ()
	print("favortesync----syncisbusy");
	local bBusy = favsynclib.syncisbusy(gHandle);
	print("favortesync----syncisbusy-",bBusy);
	return bBusy;
end)

--[[
--功能说明: 中途结束当前同步
--参数:无
--返回值:无
--]]
createmodule(interface,"syncstop",function ()
	print("favortesync----syncstop");
	favsynclib.syncstop(gHandle);
end)

tiros.favoritesync = readOnly(interface);

