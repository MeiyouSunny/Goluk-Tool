--lua脚本http对外接口

require"lua/systemapi/sys_httpupload"


--[[
require "lua/httpupload"

local function httpuploadnotify(ptype, event, param1, param2)
	print("aaaaaaaaaaa--httpnotify --",ptype,event,param1,param2);
	if event == 1 then
	elseif event == 2 then
	elseif event == 3 then
	elseif event == 4 then
	elseif event == 5 then
	end
end

tiros.httpupload.HttpUpLoad("UploadHeadImage", httpuploadnotify, "http://192.168.1.111:8081/general_Test", "fs0:/test.jpg", "/navidog2Taxi/imageUpload.htm")

--tiros.httpupload.HttpUpLoadAbort("UploadHeadImage");

--]]
