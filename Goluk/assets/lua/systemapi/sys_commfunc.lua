--[[
 @描述:公共函数库
 @编写人:孔祥宇
 @创建日期: 2012-08-02 下午 10:40:00
 @新增接口：URL编码，提供url的网络编码及解码接口	孔祥宇 2012-08-02	
--]]

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"

--[[
 @描述:把不许要编码的字符贮存在一个table中当key保存，并且把相应的value值赋值为1
 @param tCharacter	table型参数 为不许要编码的字符
 @return 经过标注的table
--]]
local function makeSet(tCharacter)
	local tCher = {};
	for i,v in ipairs(tCharacter) do
		tCher[tCharacter[i]] = 1;
	end
	return tCher;
end

--设置不需要编码的特殊字符
local avoidEncodeSet;	
			
avoidEncodeSet = makeSet {
    "/", "_", ".", "!", "~", "*", "'", "(",
	")", ":", "@", "&", "=", "+", "$", ",",
	"#",";","?","%",
}

--[[
 @描述:URL编码
 @param str	string型参数 需要编码的字符串
 @return 编码后的字符或者字符串
--]]
local function enCodeUrl(str)
	return string.gsub(str, "([^A-Za-z0-9_])", function (c)
		if avoidEncodeSet[c] then 
			return c;
		else 
			return string.format("%%%02X", string.byte(c)) ;
		end
	end)
end

--[[
 @描述:URL解码
 @param str	string型参数 需要解码的字符串
 @return 解码后的字符或者字符串
--]]
local function unEscape(str)
	str = string.gsub(str, "+", " ");
	str = string.gsub(str, "%%(%x%x)", function(h)return string.char(tonumber(h, 16))end);
	return str;
end

--[[]]--
-- 格林威治时间转换为北京时间
-- 输入参数 年(4位)，月，日，时，分,秒
-- 输出参数 年(4位)，月，日，时，分,秒
local function currenttime(y, m, d, hh, mm, ss )

	hh =  hh+8    -- 格林威治时间 + 8 小时 = 北京时间

	if ( hh < 24 ) then   --没有跨天，则计算完成
		return y, m, d, hh, mm, ss		
	end

	-----下面是跨天后的计算--------------------
	
	hh = hh-24
	d = d+1        -- 日期加一天

	--按月判断
	if (m ==4) or (m==6) or (m==9) or (m==11) then  --跨小月的判断
		if d > 30 then 
			d = 1
			m = m+1
		end
	elseif (m ==1) or (m==3) or (m==5) or (m==7) or (m==8) or (m==10) then  --跨大月的判断
		if d > 31 then 
			d = 1
			m = m+1
		end
	elseif m==12 then	--12 月，要判断是否跨年
		if d>31 then
			y = y+1
			d = 1
			m = 1
		end
	elseif m==2 then	--2 月，要判断是否是闰年
		if( ( y%400 == 0 ) or       	     -- 能被400整除，一定是闰年
       		( y%4 ==0 ) and ( y%100 ~=0 ) ) then 	--能被4整除，但不能被100整除，一定是闰年
			if( d>29 ) then	--闰年2月，可以有29号
				m = 3
				d = 1
			end
		elseif ( d>28 ) then		--非闰年2月，可以有28号
			m = 3
			d = 1
		end		
		
	end

	return y, m, d, hh, mm, ss --计算完成，开始输出
end

local function sys_modifyResCFGFile(fileName,id)
	print("yaoyt sys_modifyResCFGFile in")
	if nil == fileName or nil == fileName then
		print("yaoyt sys_modifyResCFGFile nil")
		return;
	end
	print("yaoyt sys_modifyResCFGFile file:" .. fileName .. ",id:" .. id)
	local contentJson = tiros.file.Readfile(fileName);
	print("yaoyt sys_modifyResCFGFile 222")
	if nil == contentJson then
		print("yaoyt sys_modifyResCFGFile read fail")
		return;
	end
    print("yaoyt sys_modifyResCFGFile 333:" .. contentJson)
	local contentStr = {};
	contentStr = tiros.json.decode(contentJson);
	print("yaoyt sys_modifyResCFGFile contentStr:" .. type(contentStr));
	local array = {};
	array = contentStr.data.rs;
	print("yaoyt sys_modifyResCFGFile 111")
	for key,value in pairs(array) do
		if tonumber(value["categoryid"]) == tonumber(id) then
			print("yaoyt sys_modifyResCFGFile find")
			value["isdownload"] = "0"
			value["vs"] = ""		
		end
	end 
	local saveStr = tiros.json.encode(contentStr);
	print("yaoyt sys_modifyResCFGFile saveStr:" .. saveStr)
	tiros.file.Writefile(fileName,saveStr,true)
end


--接口table
local interface = {};

--[[
 @描述: URL编码
 @param str	string型参数 需要编码的字符串
 @return 编码后的字符或者字符串
--]]
createmodule(interface,"EnCodeUrl", function (str)
	return enCodeUrl(str);
end)

--[[
 @描述:URL解码
 @param str	string型参数 需要解码的字符串
 @return 解码后的字符或者字符串
--]]
createmodule(interface,"UnEscape", function (str)
	return unEscape(str);
end)

--[[
 @描述:获取当前时间字符串
 @return 当前时间字符串
--]]
createmodule(interface,"CurrentTime", function ()
	return string.format("%04u%02u%02u%02u%02u%02u",currenttime(timelib.time()));
end)

--[[
 @描述:解压
 @param sFileName string型参数 需要解压的文件名
 @return 解压是否成功
--]]
createmodule(interface,"unzip", function (sFileName)
	return commlib.unzip(sFileName)
end)

createmodule(interface,"modifyResCFGFile", function (sFileName,id)
	return sys_modifyResCFGFile(sFileName,id)
end)

createmodule(interface,"getWebresPath", function ()
	print("yaoyt getWebresPath" .. tiros.web.FilePath)
	return tiros.web.FilePath;
end)

createmodule(interface,"setWebresPath", function (gender)
	print("yaoyt setWebresPath in")
	if nil == gender then
		print("yaoyt setWebresPath nil")
		return;
	end
	if 1 == tonumber(gender) then
		print("yaoyt setWebresPath man")
		tiros.web.FilePath = "webres-man/";
	else
		print("yaoyt setWebresPath woman")
		tiros.web.FilePath = "webres-woman/";
	end
end)

--提供对外公共函数库，并且设置接口的只读属性
tiros.commfunc  =  readOnly(interface);
