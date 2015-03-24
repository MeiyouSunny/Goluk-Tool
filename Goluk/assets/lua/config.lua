require"lua/config/sys_config"
--[[
ProfileStart(floder,fname)
--创建配置文件
--floder 文件目录名（在config目录中的子目录名）
--fname  文件名

ProfileStop(user) 
--释放配置文件缓存
--user 文件名

SaveTable(user) 
--保存一次配置文件
--user 文件名

getValue(user,key)
--获取指定key的值
--user 文件名
--key  索引名（只能为字符串）

setValue(user,key, value) 
--设置一条记录
--user 文件名
--key  索引名
--value  值 （可为字符串 整型 布尔）

removeValue(user,key) 	
--删除一条记录
--user 文件名
--key  索引名
--]]



