--[[
<pre>
 * 1.全局函数首字母大写
 * 2.私有函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写
 * 7.后续人员开发保证代码格式一致

 @ 创建时间:2013-07-19
 @ 功能描述：公用lua方法
 @ 提供接口：
（1）(GetTodayDate)对外公开获取系统今天日期方法；
（2）(GetDate)对外公开获取系统时间方法；
（3）(Split)截取字符串；
（4）(IsWifiType)判断当前是否wifi网络；
（5）(HasNetType)判断当前有无网络；
</pre>

--]]
require "lua/systemapi/sys_namespace"
require "lua/systemapi/sys_handle"

local interface = {};


-- 格林威治时间转换为北京时间
-- 输入参数 年(4位)，月，日，时，分,秒
-- 输出参数 年(4位)，月，日，时，分,秒
local function localTime(y, m, d, hh, mm, ss )
	-- 格林威治时间 + 8 小时 = 北京时间
	hh = hh + 8;
	--没有跨天，则计算完成
	if ( hh < 24 ) then
		return y, m, d, hh, mm, ss;
	end

	-----下面是跨天后的计算--------------------
	
	hh = hh-24;
	-- 日期加一天
	d = d + 1;

	--按月判断
	if (m == 4) or (m == 6) or (m == 9) or (m == 11) then
		--跨小月的判断
		if d > 30 then
			d = 1;
			m = m + 1;
		end
	elseif (m == 1) or (m == 3) or (m==5) or (m==7) or (m==8) or (m==10) then
		--跨大月的判断
		if d > 31 then
			d = 1;
			m = m+1;
		end
	elseif m ==12 then
		--12 月，要判断是否跨年
		if d > 31 then
			y = y + 1;
			d = 1;
			m = 1;
		end
	elseif m == 2 then
		--2 月，要判断是否是闰年
		--能被400整除，一定是闰年 或者 能被4整除，但不能被100整除，一定是闰年
		if((y%400 == 0 ) or ( y%4 ==0 ) and ( y%100 ~=0 ) ) then
			--闰年2月，可以有29号
			if( d > 29 ) then
				m = 3;
				d = 1;
			end
		elseif ( d > 28 ) then
			--非闰年2月，可以有28号
			m = 3;
			d = 1;
		end
	end

	--计算完成，开始输出
	return y, m, d, hh, mm, ss;
end

--合并日期字符串
local function getDate()
	local y,m,d,h,m2,s = timelib.time();
	local yy,MM,dd,hh,mm,ss = localTime(y,m,d,h,m2,s);
	if MM < 10 then
		MM = "0" .. MM;
	end
	if dd < 10 then
		dd = "0"..dd;
	end
	if hh < 10 then
		hh = "0" .. hh;
	end
	if mm < 10 then
		mm = "0" .. mm;
	end
	if ss < 10 then
		ss = "0" .. ss;
	end
	local date = "" .. yy .. MM .. dd .. hh .. mm .. ss;
	return date;
end

--获取今天日期时间
--return 年,月,日,时,分秒
local function getTodayDate()
	local y,m,d,h,m2,s = timelib.time();
	local yy,MM,dd,hh,mm,ss = localTime(y,m,d,h,m2,s);
	if MM < 10 then
		MM = "0"..MM;
	end
	if dd < 10 then
		dd = "0"..dd;
	end
	if hh < 10 then
		hh = "0"..hh;
	end
	if mm < 10 then
		mm = "0"..mm;
	end
	if ss < 10 then
		ss = "0"..ss;
	end
	return yy,MM,dd,hh,mm,ss;
end

local function comm_CopyTable(sTable)
	if type(sTable) ~= "table" then
		return sTable;
	end
	local dTable = {};
    for k, v in pairs(sTable or {}) do
        if type(v) ~= "table" then
            dTable[k] = v;
        else
            dTable[k] = comm_CopyTable(v);
        end
    end
    return dTable;
end;


--判断当前有无网络
--返回网络状态，0无网络，1有网络
createmodule(interface,"HasNetType",function()
	--nettype -1:无网络 1:wifi 2:gsm 3:cdma 4:tdcdma 5:cdma2000 6:wcdma ...
	local nettype = tiros.tapi.tapigetnettype();
	local state = 1;
	if nettype == -1 then
		state = 0;
	end	
	return state;
end);

--判断当前是否wifi网络
--返回wifi状态，0不是，1是
createmodule(interface,"IsWifiType",function()
	--nettype -1:无网络 1:wifi 2:gsm 3:cdma 4:tdcdma 5:cdma2000 6:wcdma ...
	local nettype = tiros.tapi.tapigetnettype();
	local state = 0;
	if nettype == 1 then
		state = 1;
	end
	return state;
end);


--split函数
--szFullString 要截取的字符串 54,5
--szSeparator 截取操作符 ,
--返回一个数组
createmodule(interface,"Split",function(szFullString, szSeparator)
	local nFindStartIndex = 1;
	local nSplitIndex = 1;
	local nSplitArray = {};
	while true do
		local nFindLastIndex = string.find(szFullString, szSeparator, nFindStartIndex);
		if not nFindLastIndex then
			nSplitArray[nSplitIndex] = string.sub(szFullString, nFindStartIndex, string.len(szFullString));
		break
	end
		nSplitArray[nSplitIndex] = string.sub(szFullString, nFindStartIndex, nFindLastIndex - 1);
		nFindStartIndex = nFindLastIndex + string.len(szSeparator);
		nSplitIndex = nSplitIndex + 1;
	end
	return nSplitArray;
end);

--对外公开获取系统时间方法
--返回当前时间 年月日时分秒
createmodule(interface,"GetDate",function()
	return getDate();
end);

--对外公开获取系统今天日期方法
--format 格式化日期的字符
--返回格式化后的字符串
createmodule(interface,"GetTodayDate",function(format)
	if format == nil then
		format = "-";
	end
	local yy,MM,dd = getTodayDate();
	return yy .. format .. MM .. format .. dd;
end);

--对外公开深度拷贝Table对象
--返回拷贝后的Table对象
createmodule(interface,"CopyTable",function(sTable)
	return comm_CopyTable(sTable);
end);


tiros.base.common = readOnly(interface);