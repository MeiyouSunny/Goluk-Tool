--[[
 @描述:地图下载功能
 @编写人:孔祥宇
 @创建日期: 2012-10-30 下午 19:40:00	
--]]
require"lua/citymap/sys_citymap"

--[[
--lua回调函数
DeclareGlobal("luacb", function (status, param1, param2)
print("luacb",status, param1, param2);
end)
--获取本地地图状态
--print(tiros.citymap.GetCompleteCity())
--删除全部地图
print(tiros.citymap.DeleteAllMap())
--地图下载
--print(tiros.citymap.DownloadCityMap(110000,luacb));

--暂停下载
print(tiros.citymap.StopDownload(110000))
--取消下载
print(tiros.citymap.CancelDownload(110000))
--销毁citymap
--print(tiros.citymap.DestroyDownload())
--]]


