--lua脚本收藏夹同步对外接口
require"lua/favoritesync/sys_favoritesync"

--[[
--@描述:创建收藏夹以及同步引擎
--@param  sUid String型参数，登录成功的用户的UID
--@return 请求成功返回true，失败返回false
--]]
--tiros.favoritesync.create("8881")

--[[
--@描述:销毁收藏夹以及同步引擎
--@param  无
--@return 请求成功返回true，失败返回false
--]]
--tiros.favoritesync.destroy()


--[[
--@描述:注册回调函数
--@param  sNotify String型参数，回调函数名字
--@return 请求成功返回true，失败返回false
--]]
--tiros.favoritesync.notify("fnFavoriteNotify")


--[[
--功能说明: 得到POI收藏的PO点的总个数
--参数:无
--返回值:  收藏点的总个数
--]]
--tiros.favoritesync.getcount()

--[[
--功能说明: 得到下标范围内POI点的数据，POI点下标 从0开始
--参数: nStart [in]开始的位置
--参数: nEnd   [in]结束的位置
--返回值:  成功返回要获得的POI数据所对应的json格式(时间倒序排列)
--]]
--tiros.favoritesync.getdata(1,10)


--[[
--功能说明: 根据PID得到某个POI点的数据
--参数:PID
--返回值:  成功返回要获得的POI数据所对应的json格式
--]]
--tiros.favoritesync.getsingledata("ABCDEFG")


--[[
--功能说明: 根据POIGID得到PID
--参数:sPoigid
--返回值:  PID
--]]
--tiros.favoritesync.getpid("ABCDEFG")

--[[
--功能说明: 添加一个POI点数据
--参数:sJsonData [in]要添加的POI点数据,json格式
--参数:bIsRawData [in]外层连续添加时 传送true（for循环添加），外层单个添加-非连续添加时 传送false
--返回值:  false代表添加失败，true代表添加成功
--]]
--tiros.favoritesync.add('{"lon":142312309,"lat":4123423}',false)


--[[
--功能说明: 修改一个poi点
--参数:sJsonData [in]要修改的POI点数据,json格式（带着PID）
--返回值:  false代表添加失败，true代表添加成功
--]]
--tiros.favoritesync.modify('{"lon":142312309,"lat":4123423,"PID":"ABCDE"}')

--[[
--功能说明: 删除一个POI点数据
--参数:sPid [in]要删除的POI点数据对应的PID
--返回值:  false代表添加失败，true代表添加成功
--]]
--tiros.favoritesync.delbypid("ABCDE")


--[[
--功能说明: 删除所有的POI点的数据
--参数:无
--返回值:  false代表删除失败，true代表删除成功
--]]
--tiros.favoritesync.delall()


--[[
--功能说明: 用poigid判断一个点是否存在
--参数:无
--返回值:  false代表不存在，true代表存在
--]]
--tiros.favoritesync.exist("ABCDE")


--[[
--功能说明: 判断POI点的链表是否已满
--参数:无
--返回值:  false未满，true代表满
--]]
--tiros.favoritesync.isfull()


--[[
--功能说明: 开始同步
--参数:无
--返回值:  false同步失败，true正常请求网络，等待回调
--]]
--tiros.favoritesync.startsync()


--[[
--功能说明: 判断是否正在同步中
--参数:无
--返回值:  false空闲，true繁忙
--]]
--tiros.favoritesync.syncisbusy()


--[[
--功能说明: 中途结束当前同步
--参数:无
--返回值:无
--]]
--tiros.favoritesync.syncstop()


