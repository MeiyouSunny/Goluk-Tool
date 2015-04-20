--[[
-- @描述:主题相关配置文件读写方法
-- @编写人:宣东言
-- @创建日期: 2012-10-26 15:40:11
--]]

require"lua/config"
require"lua/file"

--模块配置文件夹
local gsThemeConfigFilePath = "api"
--模块配置文件名
local gsThemeConfigFileName = "themecfg"
--存储用的键值
local gsThemeConfigKey= "themecfg"
--设定只读用的变量
local interFace = {};

--[[
--@主题相关配置文件写方法
--@param  sData string型参数,要写入文件的字符串(json格式)
--@return bool型,成功返回true,失败返回false
--]]
createmodule(interFace,"ThemeConfigWrite",function (sData)
	tiros.file.Writefile("fs2:/webcache/theme", sData, true);
end)

--[[
--@主题相关配置文件读方法
--@param  sData string型参数,要写入文件的字符串(json格式)
--@return 成功返回读取的数据,失败返回nil
--]]
createmodule(interFace,"ThemeConfigRead",function ()
	return tiros.file.Readfile("fs2:/webcache/theme");
end)

tiros.themeconfig = readOnly(interFace);

--file end


