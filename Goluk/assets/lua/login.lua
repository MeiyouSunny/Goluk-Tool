--客户端初始化服务 目前包含用户版本检验和用户深度信息获取。
--客户端通过lua脚本访问nodejs轻量级服务，nodejs再访问后台相关的逻辑业务处理服务获取用户相关的信息。

--bool login(notify,pUser);
require"lua/login/sys_login"



