--lua脚本http对外接口
--备注：函数前缀为该函数返回类型
--bool httpsend(htype,url,cbkname,data,...);
--void httpabort", function (htype);

require"lua/systemapi/sys_http"

--测试代码
--备注：notify为js端回调函数名称
--[[
--tiros.http.httpsendforlua("post", "http://www.baidu.com","notify","test-tiros","header1:value1","header2:value2")
--tiros.http.httpsendforjs("post", "http://www.baidu.com","notify","test-tiros","header1:value1","header2:value2")
--tiros.http.httpabort("post");
--]]


