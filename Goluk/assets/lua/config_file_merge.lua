--lua脚本配置文件融合对外接口
--备注：函数前缀为该函数返回类型

--dfilename:目标文件路径
--sfilename:源文件路径
--fname：配置文件名称
--备注：fname必须满足配置文件命名规范，如system api的配置文件名称为settingcfg，则文件内容格式必须为：
--tiros.config.settingcfg{
--...
--}
--file_Merge(dfilename,sfilename,fname)
require"lua/systemapi/sys_config_file_merge"
tiros.filemerge.file_Merge("config/api/settingcfg","config_backup/api/settingcfg","settingcfg");
tiros.filemerge.file_Merge("config/api/moduledatacfg","config_backup/api/moduledatacfg","moduledatacfg");
tiros.filemerge.file_Merge("config/logic/logiccfg","config_backup/logic/logiccfg","logiccfg");
tiros.filemerge.file_Merge("config/api/mobileid","config_backup/api/mobileid","mobileid");
require"lua/navidogshop"
tiros.navidogshop.audiogoods.MergeAudiodata("config/api/audiodata","config_backup/api/audiodata");
--测试代码
--[[
tiros.filemerge.file_Merge("config/api/settingcfg","config/api/settingcfg2","settingcfg");
--]]
