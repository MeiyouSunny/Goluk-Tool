--定位
print("location----lua---1");
require"lua/systemapi/sys_location"
print("location----lua---2");
--[[
1.回调函数声明
C回调
void locationnotify(func,user,elon,elat,speed,course,altitude,raidus);
LUA回调
void func(stype,elon,elat,speed,course,altitude,raidus)
2，接口
lkstart(mode,cbfunc,user)//开始定位
lkdelmonitor(cbfunc,user)//删除回调
lkstop()//停止定位
lon,lat,speed,course,altitude,raidus=lkgetlastposition_mem()//获取上次成功定位的位置，从缓存
lon,lat,speed,course,altitude,raidus=lkgetlastposition_file()//获取上次停止定位时的位置，从文件
--]]


