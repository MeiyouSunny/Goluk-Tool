
--[[
 @Module Name: grouplist
 @Module Date: 2014.5.8
 @Module Auth: 王佳
 @Description: 该文件主要用于对讲机中，频道列表的相关功能
 @Others     : 无
 @Code Conventions:
 1.编辑器必须显示空白处
 2.所有代码必须使用TAB键缩进
 3.类首字母大写,函数、变量使用驼峰式命名,常量所有字母大写
 4.注释必须在行首写.(枚举除外)
 5.函数使用块注释,代码逻辑使用行注释
 6.文件头部必须写功能说明
 7.所有代码文件头部必须包含规则 说明
--]]
require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/framework/sys_framework"
require"lua/json"
require"lua/commfunc"
require"lua/http"
require"lua/moduledata"
require"lua/database"
require"lua/base/base_moduledata"

--当前运行状态机枚举值
local EStatus_Idle,						--空闲
	  EStatus_GetAid,					--正在获取Aid
	  EStatus_GetAidOk,					--获取Aid成功
	  EStatus_Login,					--正在登录爱淘客
	  EStatus_LoginOk,					--登录爱淘客成功
	  EStatus_GetGroupid,				--正在获取群组信息
	  EStatus_GetGroupidOk,				--获取群组信息成功
	  EStatus_JoinGroup,				--正在加入群组
	  EStatus_JoinGroupOk 				--加入群组成功
	  = 0, 1, 2, 3, 4, 5, 6, 7, 8;--通知上层回调消息类型

local ETalkerEvent_GroupEnter = 2 	--进入频道相关事件
local ETalkerEvent_GroupList = 5 	--频道列表相关事件

--进入频道相关事件
local EGroupEnterEvent_GettingGid = 0 	--正在获取频道信息
local EGroupListEvent_GetGroupListFail = 0 		--频道列表信息获取失败
local EGroupListEvent_GetGroupListSuccess = 1 		--频道列表信息获取成功

--命名空间
local interface = {};
--存放频道列表信息
local moreGroupList = {};
--上层模块ID
local KMODULE_HEADLIST = 1;
local CHANNELLIST = 1;
local HOTCHANNELLIST = 2;
local EMPTYCHANNELLIST = 3;

--[[
@描述:日志封装接口
@return 无
--]]
local function DebugLog( ... )
	print("wjun-----grouplist" .. ...);
end

--[[
 @描述:根据state生成指定Json串接口
 @param 状态
 @return json字符串
--]]
local function GenJson_State(state)
	local t = {};
	t.state = state;
	return tiros.json.encode(t);
end

--[[
 @描述:通知通用逻辑模块统一接口
 @parpam msgid 消息id
 @parpam param1	参数1 具体消息见logic模块与其他模块对外交协议文档
 @parpam param2	参数2 具体消息见logic模块与其他模块对外交协议文档
--]]
local function SendMsgToLogic( msgid, param1, param2 )
	local nFunction = tiros.moduledata.moduledata_get("framework", "pLogicFunction");
	local nUser = tiros.moduledata.moduledata_get("framework", "pLogicUser");
	if nFunction == nil or nUser == nil then
		return
	end
	commlib.universalnotifyFun(nFunction,"LuaToLogicMsg", nUser, param1, msgid, param2);
end

--[[
 @描述:对讲机更多群组列表，数据库表grouplist的创建
 @return 无
--]]
local function CreateGroupListTable()
	print("grouplist CreateGroupListTable 111111111111")
	local createTableSql = "CREATE TABLE IF NOT EXISTS  grouplist(groupnumber integer PRIMARY KEY, \
								grouptype VARCHAR(2) NOT NULL,\
								membercount integer NOT NULL,\
								title varchar(1000) NOT NULL, \
								groupid VARCHAR (10) NOT NULL,\
								snumber VARCHAR (10) NOT NULL\
								);"
	tiros.database.database_execSQL(createTableSql);
	print("grouplist CreateGroupListTable 22222222222")
end

--[[
@描述：对讲机收藏频道，数据库表favChannel的创建
@return 无
--]]
local function createFavouriteTable()
	print("grouplist createFavouriteTable 1111111111");
	local createTableSql = "CREATE TABLE IF NOT EXISTS favChannel(groupnumber integer NOT NULL, \
								grouptype VARCHAR(2) NOT NULL,\
								membercount integer NOT NULL,\
								title varchar(1000) NOT NULL, \
								groupid VARCHAR (10) NOT NULL,\
								tag integer NOT NULL,\
								PRIMARY KEY(groupnumber,tag));"
	tiros.database.database_execSQL(createTableSql);
	print("grouplist createFavouriteTable 22222222");
end

--[[
@描述：当收藏的频道标题有更新时，更新数据库表中的数据
@param 要更新的频道信息
@return 无
--]]
local function updateDataForFavTable(favDataTab)
	print("grouplist updateDataForFavTable 111111111");
	if favDataTab ~= nil then
		local selectGroupNumberSql = string.format("SELECT * FROM favChannel WHERE groupnumber = %d;",tonumber(favDataTab.groupnumber));
		local selectGroupNumberSqlResult = tiros.database.database_Query(selectGroupNumberSql);
		if selectGroupNumberSqlResult ~= nil then
			local updataGroupNameSql = string.format("UPDATE favChannel SET title = '%s' WHERE groupnumber = '%d';",
															tostring(favDataTab.groupname),
															tonumber(favDataTab.groupnumber));
			print("grouplist updateDataForFavTable 22222222222");
			tiros.database.database_execSQL(updataGroupNameSql);
			print("grouplist updateDataForFavTable 33333333333");
		end
	end
	print("grouplist updateDataForFavTable 4444444444");
end

--[[
@描述：当用户收藏某个频道时，平台调用该函数
@param 平台传入的要收藏的频道信息
@return 无
--]]
local function platInsertDataIntoFavTable(favDataJson)
	print("grouplist platInsertDataIntoFavTable 1111" .. favDataJson);
	local favDataTab = tiros.json.decode(favDataJson);
	if favDataTab ~= nil then
		local insertChannelSql = string.format("INSERT INTO favChannel VALUES(%d, '%s', %d, '%s', '%s', %d);",
													tonumber(favDataTab.groupnumber),
													tostring(favDataTab.grouptype),
													tonumber(favDataTab.membercount),
													tostring(favDataTab.title),
													tostring(favDataTab.groupid),
													tonumber(favDataTab.tag)
													);
		print("grouplist platInsertDataIntoFavTable 2222");
		tiros.database.database_execSQL(insertChannelSql);
		print("grouplist platInsertDataIntoFavTable 3333");
	end
	print("grouplist platInsertDataIntoFavTable 4444");
	tiros.TalkerMgr.JoinGroup(favDataJson);
	print("grouplist platInsertDataIntoFavTable 5555");

end

--[[
@描述：当用户取消收藏某个频道时，平台调用该函数
@param 平台传入的要取消收藏的频道信息
@return 无
--]]
local function platDeleteDataFromFavTable(favDataJson)
	print("grouplist platDeleteDataFromFavTable 1111");
	local favDataTab = tiros.json.decode(favDataJson);
	if favDataTab ~= nil then
		local deleteChannelSql = string.format("DELETE FROM favChannel WHERE groupnumber = %d;", tonumber(favDataTab.groupnumber));
		print("grouplist platDeleteDataFromFavTable 2222");
		tiros.database.database_execSQL(deleteChannelSql);
		--tiros.TalkerMgr.JoinPublicGroup();
		print("grouplist platDeleteDataFromFavTable 3333");
	end
end

--[[
@描述：平台查询用户收藏的所有频道信息，平台调用该函数
@return 无
--]]
local function platSelectDataFromFavTable()
	print("grouplist platSelectDataFromFavTable 11111")
	local selectAllFavChannelInfoSql = "SELECT * FROM favChannel;"
	local favInfoJson = tiros.database.database_Query(selectAllFavChannelInfoSql);
	if favInfoJson == nil then
		print("grouplist platSelectDataFromFavTable nil")
		return nil;
	end
	print("grouplist platSelectDataFromFavTable 22222")
	local favInfoTab = tiros.json.decode(favInfoJson);
	--组织数据 给平台发送
	local t_data = {};
	local t_Array = {};
	print("grouplist platSelectDataFromFavTable 4444444444444444")
	for i = 1, #(favInfoTab), 1 do
		local t_singleData = {};
		t_singleData.groupnumber = tonumber(favInfoTab[i].groupnumber);
		t_singleData.grouptype = tostring(favInfoTab[i].grouptype);
		t_singleData.membercount = tonumber(favInfoTab[i].membercount);
		t_singleData.title = tostring(favInfoTab[i].title);
		t_singleData.groupid = tostring(favInfoTab[i].groupid);
		t_singleData.tag = tostring(favInfoTab[i].tag);

		t_Array[i] = t_singleData;
	end

	t_data.groupInfo = t_Array;

	local result = tiros.json.encode(t_data);
	print("grouplist platSelectDataFromFavTable result data"..result);
	print("grouplist platSelectDataFromFavTable 55555555555555555")
	return result;

end

--[[
@描述:从数据库表中获取频道列表信息，供平台调用
@param roomfrom 平台传入的频道起始列表编号
@param roomto   平台传入的频道终止列表编号
@return 无
]]
local function selectChannelList(roomFrom, roomTo)
	print("grouplist selectChannelList 111111111111")
	local selectFitGroupListSql = string.format("SELECT * FROM grouplist ORDER BY groupnumber ASC LIMIT %d, %d;", tonumber(roomFrom), tonumber(roomTo));
	local groupListInfoJson = tiros.database.database_Query(selectFitGroupListSql);
	print("grouplist selectChannelList 222222222222 groupListInfoJson = " .. groupListInfoJson)
	local groupListInfoTab = tiros.json.decode(groupListInfoJson);
	if groupListInfoTab == nil then
		print("grouplist selectChannelList 3333333333333333")
		return nil;
	end

	--组织数据 给平台发送
	local t_data = {};
	local t_Array = {};
	print("grouplist selectChannelList 4444444444444444")
	for i = 1, #(groupListInfoTab), 1 do 
		local t_singleData = {};
		t_singleData.groupnumber = tonumber(groupListInfoTab[i].groupnumber);
		t_singleData.grouptype = tostring(groupListInfoTab[i].grouptype);
		t_singleData.membercount = tonumber(groupListInfoTab[i].membercount);
		t_singleData.title = tostring(groupListInfoTab[i].title);
		t_singleData.groupid = tostring(groupListInfoTab[i].groupid);
			
		t_Array[i] = t_singleData;
	end

	t_data.groupInfo = t_Array;

	local result = tiros.json.encode(t_data);
	print("grouplist selectChannelList result data"..result);
	print("grouplist selectChannelList 55555555555555555")
	return result;

end

--[[
@描述:从数据库表中获取热门频道信息，供平台调用
@return 无
]]
local function selectHotChannelList(roomFrom, roomTo)
	print("grouplist selectHotChannelList 11111111111")
	local selectTopNineHotGroupListSql = string.format("SELECT * FROM grouplist WHERE membercount > 0 ORDER BY membercount DESC, groupnumber ASC LIMIT %d, %d;", tonumber(roomFrom), tonumber(roomTo));
	local groupHotListInfoJson = tiros.database.database_Query(selectTopNineHotGroupListSql);
	print("grouplist selectHotChannelList 22222222222")
	local groupHotListInfoTab = tiros.json.decode(groupHotListInfoJson);
	if groupHotListInfoTab == nil then
		print("grouplist selectHotChannelList 333333333333")
		return nil;
	end

	--组织数据 给平台发送
	local t_data = {};
	local t_Array = {};
	print("grouplist selectHotChannelList 444444444444")
	for i = 1, #(groupHotListInfoTab), 1 do
		local t_singleData = {};
		t_singleData.groupnumber = tonumber(groupHotListInfoTab[i].groupnumber);
		t_singleData.grouptype = tostring(groupHotListInfoTab[i].grouptype);
		t_singleData.membercount = tonumber(groupHotListInfoTab[i].membercount);
		t_singleData.title = tostring(groupHotListInfoTab[i].title);
		t_singleData.groupid = tostring(groupHotListInfoTab[i].groupid);
		
		t_Array[i] = t_singleData;

	end

	t_data.groupInfo = t_Array;

	local result = tiros.json.encode(t_data);
	print("grouplist selectHotChannelList result data" ..result);
	print("grouplist selectHotChannelList 5555555555555")
	return result;
end

--[[
@描述:从数据库表中获取空频道列表信息，供平台调用
@return 无
]]
local function selectEmptyChannelList(roomFrom, roomTo)
	print("grouplist selectEmptyChannelList 1111111111111")
	local selectTopNineEmptyGroupListSql = string.format("SELECT * FROM grouplist WHERE membercount = 0 ORDER BY groupnumber ASC LIMIT %d, %d;", tonumber(roomFrom), tonumber(roomTo));
	local groupEmptyListInfoJson = tiros.database.database_Query(selectTopNineEmptyGroupListSql);
	print("grouplist selectEmptyChannelList 2222222222222")
	local groupEmptyListInfoTab = tiros.json.decode(groupEmptyListInfoJson);
	if groupEmptyListInfoTab == nil then
		print("grouplist selectEmptyChannelList 3333333333333333")
		return nil;
	end

	--组织数据 给平台发送
	local t_data = {};
	local t_Array = {};
	print("grouplist selectEmptyChannelList 444444444444")
	for i = 1, #(groupEmptyListInfoTab), 1 do 
		local t_singleData = {};
		t_singleData.groupnumber = tonumber(groupEmptyListInfoTab[i].groupnumber);
		t_singleData.grouptype = tostring(groupEmptyListInfoTab[i].grouptype);
		t_singleData.membercount = tonumber(groupEmptyListInfoTab[i].membercount);
		t_singleData.title = tostring(groupEmptyListInfoTab[i].title);
		t_singleData.groupid = tostring(groupEmptyListInfoTab[i].groupid);
			
		t_Array[i] = t_singleData;
	end

	t_data.groupInfo = t_Array;

	print("grouplist selectEmptyChannelList 55555555555")
	local result = tiros.json.encode(t_data);
	print("grouplist selectEmptyChannelList result data" .. result);
	return result;
end

--[[
@描述:频道列表模糊查询
@param content 由平台传入的要查询的内容(只对频道名称进行模糊查询)
@return 返回满足条件的频道信息
]]
local function blurSelect( content )
	print("grouplist blurSelect 1111");
	local blurSql = string.format("SELECT * FROM grouplist WHERE title LIKE \"%s%s%s\" OR snumber LIKE \"%s%s%s\";", "%", tostring(content), "%","%", tostring(content), "%");
	print("grouplist blurSelect sql:" .. blurSql);
	local blueSqlJson = tiros.database.database_Query(blurSql);
	print("grouplist blurSelect 2222" .. blueSqlJson);
	if blueSqlJson == nil then
		return nil;
	end
	local blueSqlTab = tiros.json.decode(blueSqlJson);

	--组织数据 给平台发送
	local t_data = {};
	local t_Array = {};
	for i = 1, #(blueSqlTab), 1 do 
		local t_singleData = {};
		t_singleData.groupnumber = tostring(blueSqlTab[i].groupnumber);
		t_singleData.grouptype = tostring(blueSqlTab[i].grouptype);
		t_singleData.membercount = tonumber(blueSqlTab[i].membercount);
		t_singleData.title = tostring(blueSqlTab[i].title);
		t_singleData.groupid = tostring(blueSqlTab[i].groupid);
			
		t_Array[i] = t_singleData;
	end

	t_data.groupInfo = t_Array;
	print("grouplist blurSelect 3333");
	local result = tiros.json.encode(t_data);
	print("grouplist blurSelect 4444 result data" .. result);
	return result;
end

--[[
@描述:将从服务器获取的群组列表信息，依次存入表中
@return 无
]]
local function insertDataIntoGroupListTable(grouplistData)
	print("grouplist insertDataIntoGroupListTable 111111111111")
	local deleteAllGroupListSql = "DELETE FROM grouplist;"
	tiros.database.database_execSQL(deleteAllGroupListSql);
	print("grouplist insertDataIntoGroupListTable 2222222222")
	for i = 1, #grouplistData, 1 do
		local insertGroupItemSql = string.format("INSERT INTO grouplist VALUES(%d, '%s', %d, '%s', '%s','%03d');",
														tonumber(grouplistData[i].groupnumber),
														tonumber(grouplistData[i].grouptype),
														tonumber(grouplistData[i].memberscount),
														tostring(grouplistData[i].groupname),
														tostring(grouplistData[i].groupid),
														tonumber(grouplistData[i].groupnumber));
	print("grouplist insertDataIntoGroupListTable 3333333333 groupname = " .. grouplistData[i].groupname .. ", id = " .. grouplistData[i].groupid)
	tiros.database.database_execSQL(insertGroupItemSql);
	end
	--获取频道列表信息成功
	SendMsgToLogic(ETalkerEvent_GroupList, KMODULE_HEADLIST, GenJson_State(EGroupListEvent_GetGroupListSuccess));
	print("grouplist insertDataIntoGroupListTable 4444444444")
	local buttonType = tiros.base.moduledata.GetTempData("logic", "buttonType");
	print("grouplist insertDataIntoGroupListTable 5555555555")
	local page = tiros.base.moduledata.GetTempData("logic", "pageType");
	print("grouplist insertDataIntoGroupListTable 6666666666")
	local roomFrom = tiros.base.moduledata.GetTempData("logic", "roomFrom");
	print("grouplist insertDataIntoGroupListTable 77777777777")
	local roomTo = tiros.base.moduledata.GetTempData("logic", "roomTo");
	print("grouplist insertDataIntoGroupListTable 8888888888")
	--如果用户点击更多按钮，默认显示频道列表的1-9号频道信息
	if tonumber(buttonType) == 1 then
		selectChannelList(1,9);
	else
		--如果用户点击刷新按钮，分别处理频道列表、热门频道、空频道显示的内容
		if tonumber(page) == CHANNELLIST then
			selectChannelList(roomFrom, roomTo);
		elseif tonumber(page) == HOTCHANNELLIST then
			print("grouplist insertDataIntoGroupListTable 99999999")
			selectHotChannelList(roomFrom, roomTo);
			print("grouplist insertDataIntoGroupListTable 101010110101")
		elseif tonumber(page) == EMPTYCHANNELLIST then
			selectEmptyChannelList(roomFrom, roomTo);
		end
	end	
	print("grouplist insertDataIntoGroupListTable 77777777777")
end

--[[
@描述:修改当前群组标题回调
返回：无
]]
local function modifyGroupTitleHttpEvent(id,state,data)
	DebugLog("modifyGroupTitleHttpEvent Event!!!");
	if state == 1 then	--成功
		DebugLog("modifyGroupTitleHttpEvent ok!!! data = ".. data);
		local dataObj = tiros.json.decode(data);
		if dataObj.success == true then	
			DebugLog("modifyGroupTitleHttpEvent success!");
		else
			DebugLog("modifyGroupTitleHttpEvent false!");
		end
	else
		DebugLog("modifyGroupTitleHttpEvent error!");
	end
end

--[[
@描述:修改当前群组标题
返回：无
]]
local function modifyGroupTitle(title)
	local groupMarkAndStatus = tiros.TalkerMgr.GetCurrentGroupMarkAndStatus();
	if groupMarkAndStatus.status >= EStatus_JoinGroupOk then
		--如果当前在群组中才允许修改
		local sURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",2305);
		local opt = {};
		opt.form = "logic";
		opt.header = {};
		opt.header["groupname"] = title;
		opt.header["groupid"] = groupMarkAndStatus.curGroupid;
		opt.header["groupnumber"] = groupMarkAndStatus.groupnumber;
		opt.header["type"] = groupMarkAndStatus.grouptype;
		opt.method = "GET";
		tiros.base.http.HttpSend("TalkerMgr_modifyGroupTitle", modifyGroupTitleHttpEvent,"modifyGroupTitle", opt, sURL);
		DebugLog("modifyGroupTitle request end-------2");
		--通知上层状态，正在获取群组信息
		--SendMsgToLogic(ETalkerEvent_GroupEnter, KMODULE_HEADLIST, GenJson_State(EGroupEnterEvent_GettingGid));
		return true;
	end
	return false;
end

--[[
@描述:获取更多群组列表回调
返回：无
]]
local function getMoreGroupListHttpEvent(id,state,data)
	DebugLog("getMoreGroupListHttpEvent Event!!!");
	if state == 1 then	--成功
		DebugLog("getMoreGroupListHttpEvent ok!!! data = ".. data);
		local dataObj = tiros.json.decode(data);
		if dataObj.success == true then
			DebugLog("getMoreGroupListHttpEvent success!");
			moreGroupList = {};
			if dataObj.groups ~= nil then
				local index = 1;
				local groupinfo = nil;
				for k,v in pairs(dataObj.groups) do
					groupinfo = {};
					groupinfo.groupid = v.groupid;
					groupinfo.groupnumber = v.groupnumber;
					groupinfo.grouptype = v.type;
					groupinfo.groupname = v.groupname;
					groupinfo.memberscount = v.count;
					moreGroupList[index] = groupinfo;
					index = index + 1;
				end
				print("grouplist insert begin 1111111111 moreGroupList count = " .. #moreGroupList);
				insertDataIntoGroupListTable(moreGroupList);
				print("grouplist insert end 22222222222");
			else

			end
		end
	else
		--通知上层状态，获取群组列表失败
		SendMsgToLogic(ETalkerEvent_GroupList, KMODULE_HEADLIST, GenJson_State(EGroupListEvent_GetGroupListFail));
		DebugLog("getMoreGroupListHttpEvent Error!!!");
	end
end

--[[
@描述:获取更多群组频道信息，供平台调用
@param type 1代表 点击更多按钮 2代表点击刷新按钮
@param page 1代表频道列表 2代表热门频道 3代表空频道
@param roomFrom :平台传入的频道起始列表编号
@param roomTo :平台传入的频道终止列表编号
@return 获取所有频道信息
--]]
local function getMoreGroupList(type, page, roomFrom, roomTo)
	DebugLog("getMoreGroupList request begin-------1");
	if (type ~= nil) then
		tiros.base.moduledata.SetTempData("logic", "buttonType", type);
	end
	if (page ~= nil) then
		tiros.base.moduledata.SetTempData("logic", "pageType", page);
	end
	if (roomFrom ~= nil) then
		tiros.base.moduledata.SetTempData("logic", "roomFrom", roomFrom);
	end
	if (roomTo ~= nil) then
		tiros.base.moduledata.SetTempData("logic", "roomTo", roomTo);
	end
	local sURL = tiros.framework.getUrlFromResource("fs0:/res/api/api.rs",2304);
	local opt = {};
	opt.form = "logic";
	opt.header = {};
	opt.method = "GET";
	tiros.base.http.HttpSend("TalkerMgr_getMoreGroupList", getMoreGroupListHttpEvent,"getMoreGroupList", opt, sURL);
	--tiros.TalkerMgr.ModifyCurrentStatus(EStatus_GetGroupid);
	DebugLog("getMoreGroupList request end-------2");
	--通知上层状态，正在获取群组信息
	SendMsgToLogic(ETalkerEvent_GroupEnter, KMODULE_HEADLIST, GenJson_State(EGroupEnterEvent_GettingGid));
end

local function ModifyTitle(GroupNumber,GroupName)
	if( GroupName == "路况频道" ) then
		return;
	end
	local selectGroupNumberSql = string.format("SELECT * FROM favChannel WHERE groupnumber = %d;",tonumber(GroupNumber));

	local selectGroupNumberSqlResult = tiros.database.database_Query(selectGroupNumberSql);
	if selectGroupNumberSqlResult ~= nil then
		local updataGroupNameSql = string.format("UPDATE favChannel SET title = '%s' WHERE groupnumber = '%d';",
														tostring(GroupName),
														tonumber(GroupNumber));
		
		tiros.database.database_execSQL(updataGroupNameSql);
	end	
end
--============================================================================
--[[
 @描述:对讲机更多群组列表，数据库表grouplist的创建
 @return 无
--]]
createmodule(interface,"create", function()
	CreateGroupListTable();
end)

--[[
@描述：对讲机收藏频道，数据库表favChannel的创建
@return 无
--]]
createmodule(interface, "createFavourite", function()
	createFavouriteTable();
end)

--[[
@描述：当收藏的频道标题有更新时，更新数据库表中的数据(服务器推下消息后调用)
@param 要更新的频道信息
@return 无
--]]
createmodule(interface, "updatefav", function(favData)
	updateDataForFavTable(favData);
end)

--[[
@描述：当用户收藏某个频道时，平台调用该函数
@param 平台传入的要收藏的频道信息
@return 无
--]]
createmodule(interface, "platInsertfav", function(favData)
	platInsertDataIntoFavTable(favData);
end)

--[[
@描述：当用户取消收藏某个频道时，平台调用该函数
@param 平台传入的要取消收藏的频道信息
@return 无
--]]
createmodule(interface, "platDeletefav", function(favData)
	platDeleteDataFromFavTable(favData);
end)

--[[
@描述：平台查询用户收藏的所有频道信息，平台调用该函数
@return 频道收藏表中的所有信息
--]]
createmodule(interface, "platSelectfav", function()
	return platSelectDataFromFavTable();
end)

--[[
@描述:从数据库表中获取频道列表信息，供平台调用
@param roomfrom 平台传入的频道起始列表编号
@param roomto   平台传入的频道终止列表编号
@return 频道列表的频道信息
--]]
createmodule(interface, "GetChannelList", function(roomfrom,roomto)
	return selectChannelList(roomfrom,roomto);
end)

--[[
@描述:从数据库表中获取热门频道信息，供平台调用
@return 热门频道的频道信息
--]]
createmodule(interface, "GetHotChannelList", function(hotRoomFrom, hotRoomTo)
	return selectHotChannelList(hotRoomFrom, hotRoomTo);
end)

--[[
@描述:从数据库表中获取空频道列表信息，供平台调用
@return 空频道的频道信息
]]
createmodule(interface, "GetEmptyChannelList", function(roomfrom, roomto)
	return selectEmptyChannelList(roomfrom, roomto);
end)

--[[
@描述:获取更多群组频道信息，供平台调用
@param type 1代表 点击更多按钮 2代表点击刷新按钮
@param page 1代表频道列表 2代表热门频道 3代表空频道
@param roomFrom :平台传入的频道起始列表编号
@param roomTo :平台传入的频道终止列表编号
@return 获取所有频道信息
--]]
createmodule(interface,"GetMoreGroupListRequest",function(type, page, roomFrom, roomTo)
	return getMoreGroupList(type, page, roomFrom, roomTo);
end);

--[[
@描述:修改群组标题
@return 无
]]
createmodule(interface,"ModifyGroupTitleRequest",function(title)
	return modifyGroupTitle(title);
end);

--[[
@描述:频道列表模糊查询
@param content 由平台传入的要查询的内容(只对频道名称进行模糊查询)
@return 返回满足条件的频道信息
]]
createmodule(interface,"blurSelectRequest",function(content)
	print("blurSelectRequest begin " .. content)
	return blurSelect(content);
end);

--[[
@描述:进入群组时如果该群组number已经在数据库的收藏表中存在， 则更新所有number记录中的title字段
@param  GroupNumber群组ID，GroupName群组标题
@return 无
]]
createmodule(interface,"AgainModifyTitle",function(GroupNumber,GroupName)
	ModifyTitle(GroupNumber,GroupName);
end);

tiros.grouplist = readOnly(interface);
