-- @描述:小白商店语音列表的下载与交互实现
-- @编写人:lyfsteven
-- @创建日期: 2013-11-25

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/framework/sys_framework"
require"lua/systemapi/sys_file"
require"lua/json/sys_json"
require"lua/commfunc"
require"lua/http"
require"lua/moduledata"
require"lua/database"
require"lua/mediaplayer"



local interface = {}

--=========================================内部数据=========================================
-- 将来也许需要断点续传，保留这两个数据。
local completed = {};
local uncompleted = {};
-- 语音列表将传递给web一个json数组，名称，简介，价格等信息以后都会放在这个表里面传给web
local audioInfoList = {};
--[{name=afa, description= afvasdf, price=faowfl}, {afawfoaf}]
local audioInfoFullFileName = "fs0:/config/api/audiodata";
local DEFAULT_VOICE_ID = "2"; 

--=========================================内部实现=========================================


local function sys_SetHttpStateValue(event, data)
	local temp = {}
	temp[1] = event
	temp[2] = data
	local jsonStr = tiros.json.encode(temp)
    print("audiogoods sys_SetHttpStateValue jsonStr is " .. jsonStr);
	tiros.moduledata.moduledata_set("web","audiogoodshttpevent_ptp",jsonStr)
end


local function sys_DownloadAudioData(id)
    local nFunction = tiros.moduledata.moduledata_get("framework", "pLogicFunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "pLogicUser");
    if nFunction ~= nil then
        commlib.universalnotifyFun(nFunction,"audio", nUser, 18, 1, tostring(id));        
    end
end

local function sys_ResetCurrentAudio(id)
    print("yaoyt voice sys_ResetCurrentAudio")
    local name = audioInfoFullFileName;
    local data = tiros.file.Readfile(name);
    local dataTable = tiros.json.decode(data);
    for i=1, #dataTable do
        if dataTable[i].id == id then
            local nFunction = tiros.moduledata.moduledata_get("framework", "pLogicFunction");
            local nUser = tiros.moduledata.moduledata_get("framework", "pLogicUser");
            if nFunction ~= nil then
                commlib.universalnotifyFun(nFunction,"audio", nUser, 18, 2, dataTable[i].voicekey);        
            end
            dataTable[i].state = 2;
        else
            if dataTable[i].state == 2 then
                dataTable[i].state = 1;
            end
        end
    end
    local content = tiros.json.encode(dataTable);
    print("audiogoods sys_ResetCurrentAudio will write data to file->" .. content)
    tiros.file.Writefile(name, content, true);
end

local function sys_SetHttpDataSize(dataSize)
	print("audiogoods sys_SetHttpDataSize" .. tostring(dataSize))
	tiros.moduledata.moduledata_set("web","audiogoodsfilesize_ptp",dataSize)
end


local function sys_SetAudioInfoData(data)
    local dataTable = tiros.json.decode(data);
    print("audiogoods sys_SetAudioInfoData input:" .. data)
    local name = audioInfoFullFileName;
    local oldData = tiros.file.Readfile(name);
    local info = dataTable.data;
    if oldData == nil then
        local newData = {};
        newData.id = info.id;
        newData.voicename = info.voicename;
        newData.voicekey = info.voicekey;
        newData.state = 1;
        local content = tiros.json.encode(newData);
        tiros.file.Writefile(name, content, true);
    else
        print("audiogoods sys_SetAudioInfoData file data" .. oldData)
        local oldTable = tiros.json.decode(oldData);
        for i=1, #oldTable do
            print("audiogoods sys_SetAudioInfoData oldtable id:" .. oldTable[i].id);
            print("audiogoods sys_SetAudioInfoData info.id:" .. info.id);

            if oldTable[i].id == info.id then
                print("audiogoods sys_SetAudioInfoDat reset");
                oldTable[i].voicename = info.voicename;
                oldTable[i].voicekey = info.voicekey;
                oldTable[i].state = 1;
                local content = tiros.json.encode(oldTable);
                print("audiogoods sys_SetAudioInfoData will write data to file->" .. content)
                tiros.file.Writefile(name, content, true);
                break;
            end
        end
    end
end



local function sys_GetAudioListInfo()
    local name = audioInfoFullFileName;
    local data = tiros.file.Readfile(name);
    print("audiogoods sys_GetAudioListInfo file data" .. data)
    return data;
end


local function sys_CancelDownloadAudioData(id)
    local nFunction = tiros.moduledata.moduledata_get("framework", "pLogicFunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "pLogicUser");
    if nFunction ~= nil then
        commlib.universalnotifyFun(nFunction,"audio", nUser, 18, 3, tostring(id));        
    end
end

local function sys_DeleteAudioData(id)
    local name = audioInfoFullFileName;
    local oldData = tiros.file.Readfile(name);
    local oldTable = tiros.json.decode(oldData);
    for i=1, #oldTable do
        if oldTable[i].id == id then
            oldTable[i].state = 0;
            local content = tiros.json.encode(oldTable);
            print("audiogoods sys_DeleteAudioData write data to file->" .. content)
            tiros.file.Writefile(name, content, true);
            break;
        end
    end
    local nFunction = tiros.moduledata.moduledata_get("framework", "pLogicFunction");
    local nUser = tiros.moduledata.moduledata_get("framework", "pLogicUser");
    if nFunction ~= nil then
        commlib.universalnotifyFun(nFunction,"audio", nUser, 18, 4, tostring(id));        
    end
end

local function sys_GetVoiceKey(voiceID)
	if nil == voiceID then 
		return nil
	end
	local oldData = tiros.file.Readfile(audioInfoFullFileName);
	local oldTable = tiros.json.decode(oldData);
	for i=1, #oldTable do
		if oldTable[i].id == tostring(voiceID) then
			if "no" ~= oldTable[i].voicekey then
				return 	oldTable[i].voicekey;
			else
				return nil
			end	
		end	
	end
	return nil;
end

local function sys_MergeAudiodata(dFilename,sFilename)
	if nil == dFilename or nil == sFilename then
		return;
	end

	local sData = tiros.file.Readfile(sFilename);
    	local sTable = tiros.json.decode(sData);
	local dData = tiros.file.Readfile(dFilename);
    	local dTable = tiros.json.decode(dData);

	local useVoiceIndex;
	local defaultVoiceIndex;

    	for i=1, #dTable do
		for j=1, #sTable do
			if dTable[i].id == sTable[j].id then
			   	if 1 == sTable[j].state or 2 == sTable[j].state then
					--旧版本使用的语音
					if 2 == sTable[j].state then
						useVoiceIndex = j;					
					end
					dTable[i].state = sTable[j].state;
					dTable[i].voicekey = sTable[j].voicekey;
					if nil ~= sTable[j].version then
						dTable[i].version = sTable[j].version;
					else
						dTable[i].version = "1.0"
					end
				end
			end
		end

		if dTable[i].id == DEFAULT_VOICE_ID then
			defaultVoiceIndex = i;
		end
    	end

        --旧版本使用的不是默认语音
	if DEFAULT_VOICE_ID ~= dTable[useVoiceIndex].id then
		dTable[defaultVoiceIndex].state = 1;
	end

  	local content = tiros.json.encode(dTable);
 	tiros.file.Writefile(dFilename, content, true);
end

local function sys_StopAudioDemo(id)
    print("audiogoods sys_StopAudioDemo");
end
local function sys_PlayAudioDemo(id)
    print("audiogoods sys_PlayAudioDemo");
end

local function sys_Init()
	print("yaoyt voice sys_Init")
    	local voiceType = tiros.settingconfig.get_prompttype();
	print("yaoyt voice sys_Init 111")
    	sys_ResetCurrentAudio(tostring(voiceType));
end
--=========================================外部接口=========================================


--@@描述:停止语音试听
--@输出：无
createmodule(interface, "StopAudioDemo", function(id)
	return sys_StopAudioDemo(id)
end)



--@@描述:播放语音试听
--@输出：无
createmodule(interface, "PlayAudioDemo", function(id)
	return sys_PlayAudioDemo(id)
end)

--@@描述:删除语音数据
--@输出：true，删除成功，false，删除失败。
createmodule(interface, "DeleteAudioData", function(id)
	return sys_DeleteAudioData(id)
end)

--@@描述:请求语音下载
--@param id：语音id
--@输出：无
createmodule(interface, "DownloadAudioData", function(id)
return sys_DownloadAudioData(id)
end)

--@@描述:取消语音下载
--@param id：语音id
--@输出：无
createmodule(interface, "CancelDownloadAudioData", function(id)
return sys_CancelDownloadAudioData(id)
end)



--@@描述:设置Http的状态和数据
--@param event：http状态， 开始下载，下载百分比，下载结束
--@param data：数据
--@输出：无
--状态数据将存入lua仓库,web需要读取下列变量。
--audiogoodshttpevent_ptp， 用于不断写入http事件， 内容为json串{event value}，
--[[
例子：
{0, 0}表示正在下载信息数据从中获取语音地址

{3,18}，下载百分比，18%
{4,0},下载完成
{5,1},读取信息数据失败， {5,2}创建接收语音数据的文件失败，{5,3}没有足够的磁盘空间{5,其它网络错误代码}
{6,0},安装语音文件完成。
]]
createmodule(interface, "SetHttpStateValue", function(event, data)
	return sys_SetHttpStateValue(event, data)
end)
--@@描述:设置正在下载的音频数据的总大小。
--@param dataSize：文件大小
--@输出：无
--状态数据将存入lua仓库,web需要读取下列变量。
--audiogoodsfilesize_ptp，用于存放下载的文件总大小，内容是一个number。
createmodule(interface, "SetHttpDataSize", function(dataSize)
	return sys_SetHttpDataSize(dataSize)
end)


--@@描述:获取语音列表
--@输出：json string
 --[{"id":1, "voicename":"标准女音", "voicekey":"no", "state":0},{"id":2, "voicename":"标准男音", "voicekey":"no", "state":1},{"id":3, "voicename":"葛二爷", "voicekey":"no", "state":2}]

createmodule(interface, "GetAudioListInfo", function()
	return sys_GetAudioListInfo();
end)

--@@描述:重新设置用户当前使用的语音
--@输出：无
createmodule(interface, "ResetCurrentAudio", function(id)
	return sys_ResetCurrentAudio(id);
end)

--@@描述:下载结束后，需要记录信息
createmodule(interface, "SetAudioInfoData", function(data)
	return sys_SetAudioInfoData(data);
end)

--@@描述:根据voiceid获取voicekey
createmodule(interface, "GetVoiceKey", function(voiceID)
	return sys_GetVoiceKey(voiceID);
end)

--@@描述:覆盖安装时merge audiodata配置文件
createmodule(interface, "MergeAudiodata", function(dFilename,sFilename)
	return sys_MergeAudiodata(dFilename,sFilename);
end)

--@@语音类型初始化，设置上次选择的语音，需要在性别初始化之后
createmodule(interface, "Init", function()
	print("yaoyt voice init")
	return sys_Init();
end)

tiros.navidogshop.audiogoods = readOnly(interface);
--file end


