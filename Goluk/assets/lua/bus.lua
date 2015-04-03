--公交换乘相关接口

--[[
--@描述:对外声明 开始获取公交换乘结果
--@param  sType 类似句柄（假的）
--@param  param 请求参数在数据仓库的位置
--@param  notify 回调函数
--@return 请求成功返回详情结果json串，失败返回nil
--]]
--tiros.bus.busStart(sType, notify, param)

--[[
--@描述:对外声明 取消公交换乘请求
--@param  param 无
--@return 成功返回true，失败返回false
--]]
--tiros.bus.busStop()

--[[
--@描述:对外声明 获取展现的公交规划线路
--@param  busid integer型参数,公交规划结果id
--@return 请求成功返回详情结果json串，失败返回nil
--]]
--tiros.bus.getDetails(busid)

require"lua/bus/sys_bus"

