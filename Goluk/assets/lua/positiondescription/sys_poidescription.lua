--[[
 @描述：根据poigid获取兴趣点详细信息
 @编写人：王成
 @创建日期：2012-11-20 
 @修改内容：
 @版本：0.1.0
--]]
require "lua/systemapi/sys_namespace"
require "lua/systemapi/sys_handle"
require "lua/systemapi/sys_http"
require "lua/framework/sys_framework"

--资源文件中编号
local RES_STR_POIDESCRIPTION_BY_POIGID_URL = 1103;

--资源文件地址路径
local RES_FILE_PATH = "fs0:/res/api/api.rs";

--获取URL:服务器地址及端口号
--"http://testx.lbs8.com/poi_detail"
--local POIDESCRIPTION_URL = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_POIDESCRIPTION_BY_POIGID_URL);

local httpEngine = getmodule("http")
local gt = {}
local rsp_data = nil

createmodule(gt, "poidescriptlist",{})
--cellidweaklist：全局变量,用于存放所有cellid句柄的week表,week表中既包含正使用的句柄,也包含即将回收的句柄
createmodule(gt, "poidescriptweaklist",{})
setmetatable(gt.poidescriptweaklist,{__mode ="v" })

local function poidescptcreate()
	local gTable =getHandle(gt.poidescriptweaklist,"poidescripthandle");
	if gTable == nil then
		gTable = {};			
	end	
	registerHandle(gt.poidescriptlist,gt.poidescriptweaklist,"poidescripthandle",gTable);
	return gTable;
end

--[[
{
"base":	{
"comObj":{"name":"兴华公寓中餐厅","address":"北京市朝阳区中纺里３７－５","tel":"","lat":"143737596","lon":"419231682","poigid":"10019331","categoryId":"1","city":"北京市 朝阳区","source":"poi2"}
		},
"more":{}
}
--]]
local function poidescpt_sendmessage(event,data)	
	local gTable =getHandle(gt.poidescriptweaklist,"poidescripthandle");
	if gTable == nil then
		return;			
	end	
	local poiinfo = nil;
    if data ~= nil then
			local jsonT = tiros.json.decode(data)
	
			if jsonT ~= nil  or type(jsonT) == "table" then
					local infoT = jsonT.base.comObj;					
					poiinfo = tiros.json.encode(infoT)
			end 
	end
	commlib.universalnotifyFun(gTable.func, gTable.poigid, gTable.user, event, 0, poiinfo);
end

createmodule(gt,"poidescpt_httpEvent",function(htype,event,param1, param2)

	local e = "event"..event
		if e=="event1" then 				
		elseif e=="event2" then 
			if param1 ~= 200  then
				poidescpt_sendmessage(event)		
			end
		elseif e== "event3" then 						
			if rsp_data then			
				rsp_data = rsp_data..param2
			else			
			 	rsp_data = param2
			end 		
		elseif e == "event4" then
				poidescpt_sendmessage(event,rsp_data)				
		elseif e == "event5" then 
			poidescpt_sendmessage(event)
		end	
		
end)


createmodule(gt,"poidescriptionrequest",function(poigid,lon,lat,cbfunc,user)

	if cbfunc == nil or user == nil or poigid == nil then
		return false;
	end
	local gTable = poidescptcreate();
    
	gTable.func= cbfunc;
	gTable.user= user;	
	gTable.poigid= poigid;
	local POIDESCRIPTION_URL = tiros.framework.getUrlFromResource(RES_FILE_PATH, RES_STR_POIDESCRIPTION_BY_POIGID_URL);
	local url = POIDESCRIPTION_URL.."?method=get&source=&poigid="..poigid.."&selflon="..lon.."&selflat="..lat;
	rsp_data = nil	
        httpEngine.httpsendforlua("cdc_client","poidescript","poidescripthandle", url , gt.poidescpt_httpEvent)
	
end)

--开始定位函数接口
--输出：无
createmodule(gt,"poidescriptionabort",function()
	httpEngine.httpabort("poidescripthandle");
end)


createmodule(gt,"poidescriptiondestroy",function()
	local gTable =getHandle(gt.poidescriptlist,"poidescripthandle")
	
	if gTable ~= nil then		
		poidescriptionabort();
	   	releaseHandle(gt.poidescriptlist,"poidescripthandle")						
	end
end)

tiros.poidescription = readOnly(gt);
