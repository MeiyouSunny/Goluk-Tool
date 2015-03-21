--lua脚本socket对外接口
--备注：函数前缀为该函数返回类型
--bool udpsendto(stype, ip, port, data );
--void socketabort(stype);

require"lua/systemapi/sys_socket"

--测试代码
--备注：notify为js端回调函数名称
--[[
print("ready to test udpsendto api~~~~~~~");
print(tiros.socket.udpsendto("xxx", "192.168.1.136", 6001, "q234567890"));
print(tiros.socket.socketabort("xxx"));
--]]

