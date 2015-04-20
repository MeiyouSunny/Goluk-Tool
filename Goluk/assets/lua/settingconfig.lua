--各模块配置信息 接口
--主要模块：地图、导航、平台设置
--考虑预留uID，根据用户ID保存其配置信息

require"lua/systemapi/sys_settingconfig"

--KEY键值名及默认值
--[[
modulecfg{
 naviNaviPriority = 1,                导航优选级选项           默认值：时间最少 
 naviMapDirection = 2,                地图方向的枚举值         默认值：上为航向
 naviNaviVoiceType = 1,               语音类型                 默认值：普通话女声
 navibAvoidTollRoad = 1,              是否避开收费道路         默认值：不避开true
 navibVoiceCueSimple = 0,             语音提示                 默认值：详细提示false
 naviNaviTrafficRecRoute = 0,         导航时是否根据路况规划推荐路线 默认：规划并提示
 platformNaviBacklight = 1,           背景灯选项               默认值：一直开启
 platformNaviZoom = 1,                自动缩放是否开启         默认值：开启true
 platformVolume = 80,                 音量20%~100%             默认值：80% 80
 platformOffCourseVoice = 1,          偏离航线提示（语音、静音）默认值：语音 1
 platformPowerSavingMode = 1,         开启省电模式（开启，关闭）默认值：开启 1
 mapMapRoadConditionType = 0,         实时路况显示设置          默认值：关闭
 mapDayOrNightMode = 1,               白天/黑夜模式            默认值：1白天  （0表示黑夜）
}
--]]

--注意：platformVolume 音量默认值是80% 存储时按整数80存储

--KEY 命名规则
--由于在配置信息中没有了moduleID，所以为了标识出各项配置所属的模块，键值名前加上小写的模块名：

--[[testcode
tiros.settingconfig.settingconfig_open()
print(tiros.settingconfig.settingconfig_getinfo("naviNaviPriority"))
print(tiros.settingconfig.settingconfig_getinfo("naviMapDirection"))
print(tiros.settingconfig.settingconfig_getinfo("naviNaviVoiceType"))
print(tiros.settingconfig.settingconfig_getinfo("navibAvoidTollRoad"))
print(tiros.settingconfig.settingconfig_getinfo("navibVoiceCueSimple"))
print(tiros.settingconfig.settingconfig_getinfo("naviNaviTrafficRecRoute"))

tiros.settingconfig.settingconfig_setinfo("naviNaviVoiceType", 3)
print(tiros.settingconfig.settingconfig_getinfo("naviNaviVoiceType"))
tiros.settingconfig.settingconfig_close()
--]]
