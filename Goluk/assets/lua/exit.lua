require "lua/netManager"
require "lua/airtalkeemgr"
require "lua/chatrecord"
tiros.netManager.netManagerRelease();
tiros.airtalkeemgr.Release();
--database
require"lua/database"
tiros.database.database_close()

-- 释放语音相关的操作
--require "lua/ttsmgr"
--tiros.ttsmgr.destroy();

tiros.chatrecord.destroy();

