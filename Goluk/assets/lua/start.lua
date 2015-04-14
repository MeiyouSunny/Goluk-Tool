------软件启动以后自动运行的脚本------

--框架初始化脚本:
--(1)把常用信息存入lua数据仓库 比如客户端版本号,mobileid,imsi,手机系统版本等
--require "lua/framework"
--tiros.framework.SetCommonlyInfoIntoModuledata()

--初始化网络
require "lua/netManager"
tiros.netManager.netManagerInit()


--启动定位
require "lua/location"
tiros.location.lkstart(3)

--加载见面三管理模块初始化操作
require"lua/airtalkeemgr"
tiros.airtalkeemgr.init();

require"lua/TalkerMgr"
tiros.TalkerMgr.Init();


