--数据库操作
require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/framework/sys_framework"
require"lua/json"
require"lua/commfunc"
require"lua/moduledata"

local gt = {};
gt.bRunRefCount = 0;


createmodule(gt,"database_open", function()
	if gt.bRunRefCount == 0 then
		dblib.open();
	end
	gt.bRunRefCount = gt.bRunRefCount + 1;
end)

createmodule(gt,"database_close", function()
	gt.bRunRefCount = gt.bRunRefCount - 1;
	if gt.bRunRefCount == 0 then
		dblib.close()
	end
end)

createmodule(gt,"database_execSQL", function(sql)
	if sql == nil then
		return false
	else
		gt.database_open();
		dblib.execSQL(sql);
		gt.database_close();
	end
end)

createmodule(gt,"database_Query", function(sql)
	if sql == nil then
		return nil
	else
		gt.database_open();
		local str = dblib.Query(sql);
		gt.database_close();
		return str
	end
end)

tiros.database  = readOnly(gt)

