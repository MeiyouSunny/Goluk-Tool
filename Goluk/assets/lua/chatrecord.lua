
require"lua/together/sys_chatrecord"


--[[
local t_data = {};
local t_Array = {};
local t_singledata = {};

	

t_singledata.msgtype = "1111";
t_singledata.msgid = "2222";
t_singledata.msgcontent = "3333";
table.insert(t_Array,t_singledata);
t_data.message = t_Array;

local datajson = tiros.json.encode(t_data);

print(datajson);
--]]
