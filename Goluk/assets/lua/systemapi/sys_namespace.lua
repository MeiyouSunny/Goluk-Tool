require"lua/systemapi/sys_globalmamanger"

DeclareGlobal("tiros",{})

tiros.http = {}
tiros.gps = {}
tiros.socket = {}
tiros.tapi = {}
tiros.timer = {}
tiros.platgps = {} 
tiros.file  = {}
tiros.framework  = {}
tiros.login  = {}
tiros.shorturl  = {}
tiros.moduledata  = {}
tiros.location  = {}
tiros.config  = {}
tiros.PSTdescription = {} --大头针
tiros.netManager = {} -- 网络管理
tiros.settingconfig = {} --各模块配置信息
tiros.domainmanager = {}
tiros.udpmanager = {}
tiros.udpreport  = {}
tiros.tirosbit = {}
tiros.pinyin ={}
tiros.poifavorites = {}
tiros.web = {}
tiros.poisearch = {}
tiros.nethttpheaders = {}
tiros.nethttpperformanceanalysis = {}
tiros.json = {}
tiros.commfunc = {}
tiros.filemerge = {}
tiros.citymap = {}
tiros.PSTsearch = {}
tiros.uploadtoken = {}
tiros.thirdparty = nil
tiros.Environment = nil
tiros.themeconfig = {}
tiros.PSTreport = {}
tiros.cellsave = {}
tiros.downloadad = {}
tiros.friendmanger = {} --好友管理
tiros.addbook = {} --通讯录
tiros.httpupload = {} --上传文件
tiros.database = {} --数据库
tiros.airtalkeemgr = {}
tiros.loginmanager = {}
tiros.getaid = {}
tiros.downloadimg ={} --图片下载
tiros.together = {} --群组
tiros.uploadfile = {} --文件或图片上传
tiros.bus = {} --公交换乘
tiros.groupbook = {} --通讯录上传扩展
tiros.loginupdate = {} --3x到4x升级
tiros.navirealscene = {} --导航实景图片处理
tiros.favoritesync = {} --收藏夹以及同步
--tiros.ttsmgr = {} -- ttsManager
--tiros.tts = {} -- tts
tiros.favoriteupgrade = {} --读取公共收藏夹数据
tiros.dynamicpage = {} --下载动态智能首页面的自拍包并替换
tiros.chatrecord = {} -- 聊天列表
tiros.voiceplay = {} --语音播报广告
tiros.getairtalkeeip = {} -- 获取爱滔客服务器的ip端口等
tiros.mediaplayer = {} --媒体播放器
tiros.ttsmanager = {} --tts播放器
tiros.bubble = {} -- 气泡的操作
tiros.navi = {} -- 导航相关内容
tiros.navidogshop = {} -- 小白商店
tiros.navidogshop.audiogoods = {} -- 语音商品
tiros.base = {}; 
tiros.base.file = {} -- 文件操作新接口
tiros.base.common = {} -- 常用函数新接口
tiros.base.gps = {} -- gps新接口
tiros.base.moduledata = {} -- 数据仓库新接口
tiros.base.udp = {} -- udp新接口
tiros.base.http = {} -- http新接口

DeclareGlobal("createmodule",function(t,name,value)
	if rawget(t,name) ~= nil then
		error("the variable have declared"..n, 2)
	else
		rawset(t, name, value )
	end
end)

DeclareGlobal("getmodule",function(name)
	local T = rawget(tiros,name) 
	if T~= nil then
		return T
	else
		error("the variable have declared"..n, 2)
	end
end)

DeclareGlobal("initgvEnvironment",function(Module)
	if Module == nil then
		tiros.Environment =  tostring(string.format("%04u%02u%02u%02u%02u%02u",timelib.time()))
		tiros.thirdparty = rawset(tiros, tiros.Environment, {} )
		declaredNames[tiros.Environment] = true
		return  tiros.thirdparty
	else
		tiros.Module = rawset(tiros, Module, {} )
		declaredNames[Module] = true
	end
		return  tiros.Module
end)

DeclareGlobal("getgvEnvironment",function(name,Module)
	if Module ~= nil then
			local v = rawget(tiros.Module,name) 
		if v~= nil then
			return v
		else
			error("the variable have declared"..n, 2)
		end
	else
		local v = rawget(tiros.thirdparty,name) 
		if v~= nil then
		print(v)
			return v
		else
			error("the variable have declared"..n, 2)
		end
	end
end)


DeclareGlobal("closegvEnvironment",function(Module)
	if Module == nil then
		tiros.thirdparty = nil
		tiros.Environment = nil
	else
		tiros.Module  = nil
	end
end)

