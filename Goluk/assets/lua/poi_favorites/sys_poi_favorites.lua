--poi的收藏夹 接口封装

--协议修改poigid,poicategoryId,poilon,poilat为字符串，添加poicity字段
--hubo
--Date:  2012/05/08
--增加id字段
--hubo
--Date:  2012/05/16
--去除拼音模块（加载时间过长）
--hubo
--Date:  2012/06/19
--getpois接口输出字串中增加count键值对，修改返回的错误json串
--hubo
--Date:  2012/07/01
--使用新的json接口
--hubo
--Date:  2012/07/01


require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"
require"lua/json"
--require"lua/pinyin/pinyin"

--配置文件cfgFileName的格式：
--100
--索引文件indexFileName的每条记录格式：
--status,id,poigid,datapos
--数据文件dataFileName的每条记录格式：
--poiname,poipinyin,poicategoryId,poilon,poilat,poicity,poiaddress,poiphone,poiremark,poitime

--内存中索引表indextable的每条记录格式：
--indextable = {id = indexpos}
--内存中poigid表poigidtable的每条记录格式：
--poigidtable = {poigid = id}
--内存中空闲资源数组freearray的每个元素格式：
--freearray = {indexpos}
--内存中排序数组sortarray的每条记录格式：
--sortarray = {id .. HYPHEN .. sortvalue}

---------------------------静态局部变量-----------------------------------------------------------

local init_flag = false

local indextable = nil	--索引表
local poigidtable = nil --poigid表
local freearray = nil	--空闲资源数组

--排序相关
local sortarray = nil		--排序数组
local now_sorttype = 1		--当前排序类型 1、按照poitime排序  2、按照poipinyin排序
local now_sortmethod = 1  	--当前排序方法 1、升序  2、降序

---------------------------静态局部常量-----------------------------------------------------------

--执行错误码
local POIFAV_SUCCESS = 1			--操作成功
local POIFAV_FAILD = 0				--操作失败
local POIFAV_FILE_ERR = 2			--文件错误
local POIFAV_VER_ERR = 3			--版本错误
local POIFAV_UPDATE_ERR = 4			--升级错误
local POIFAV_INIT_ERR = 5			--初始化错误
local POIFAV_PARAM_ERR = 6			--参数错误
local POIFAV_JSON_ERR = 7			--Json错误
local POIFAV_POIEXIST_ERR = 8		--poi点存在
local POIFAV_POINOTEXIST_ERR = 9	--poi点不存在
local POIFAV_POIGID_ERR = 10		--poigid错误
local POIFAV_RANGE_ERR = 11			--数组范围错误
local POIFAV_ID_ERR = 12			--id错误

--版本数据数组（最后一个元素为最新版本号）
--格式：  local verarray = { 100,101,200,201,202 }
local verarray = { 100 }

local cfgFileName = "poifav/poifavcfg"		--配置文件名
local indexFileName = "poifav/poifavindex" 	--索引文件名
local dataFileName = "poifav/poifavdata"	--数据文件名

--索引文件indexFileName的每个字段长度
--status,id,poigid,datapos
local STATUS_LEN = 1
local ID_LEN = 36
local POIGID_LEN = 32
local DATAPOS_LEN = 4

local INDEX_ITEM_LEN = STATUS_LEN + ID_LEN + POIGID_LEN + DATAPOS_LEN  --每条记录的长度

--数据文件dataFileName的每个字段长度
--poiname,poipinyin,poicategoryId,poilon,poilat,poicity,poiaddress,poiphone,poiremark,poitime
local HZ_NUM = 5		--把poiname转换成拼音的汉字的个数
local ONEPY_MAXLEN = 6		--每个汉字转换成拼音的最大长度

local POINAME_LEN = 80
local POIPINYIN_LEN = HZ_NUM * ONEPY_MAXLEN
local POICATEGORYID_LEN = 8
local POILON_LEN = 16
local POILAT_LEN = 16
local POICITY_LEN = 50
local POIADDRESS_LEN = 320
local POIPHONE_LEN = 20
local POIREMARK_LEN = 320
local POITIME_LEN = 14

local DATA_ITEM_LEN = POINAME_LEN + POIPINYIN_LEN + POICATEGORYID_LEN + POILON_LEN + POILAT_LEN 
			+ POICITY_LEN + POIADDRESS_LEN + POIPHONE_LEN + POIREMARK_LEN + POITIME_LEN --每条记录的长度

--JSON字符串中key值
local J_KEY_POIS = "pois"
local J_KEY_POISCOUNT = "count"
local J_KEY_ID = "pid"
local J_KEY_POIGID = "poigid"
local J_KEY_POINAME = "name"
local J_KEY_POIPINYIN = "pinyin"
local J_KEY_POICATEGORYID = "categoryId"
local J_KEY_POILON = "lon"
local J_KEY_POILAT = "lat"
local J_KEY_POIPHONE = "tel"
local J_KEY_POICITY = "city"
local J_KEY_POIADDRESS = "address"
local J_KEY_POIREMARK = "remark"
local J_KEY_POITIME = "time"

local HYPHEN = "@"	--连接符

---------------------------公共函数-----------------------------------------------------------

--去除字符串头尾的空格
local function trim(s)
	return (string.gsub(s, "^%s*(.-)%s*$", "%1"))
end

--将整数num转换成n个字节数据
local function numbertoascii(num, n)

	local str = ""
	local j = 0
	if ( (nil==num) or (nil==n) or ("number"~=type(num)) or ("number"~=type(n)) or (0==n) or (num>(256^n-1)) ) then
		return ""
	end

	repeat
		j = num%256 --j 余数
		str = string.char(j) .. str  --从左到右由高到低
		num = (num-j)/256
	until num == 0

	if n>#str then
		str = string.rep("\0", n-#str) .. str
	end

	return str
end

--将字节数据转换成整数
local function asciitonumber(str)

	local num = 0
	if nil==str then
		return 0
	end
	if 0 ~= #str then
		for i=1, #str do
			num = num * 256 + string.byte(str,1*i)
		end
	end
	return num
end

-- 格林威治时间转换为北京时间
-- 输入参数 年(4位)，月，日，时，分,秒
-- 输出参数 年(4位)，月，日，时，分,秒
local function localtime(y, m, d, hh, mm, ss)

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

--生成唯一的guid标识
local function newguid()

	local guid = nil
	
	--math.randomseed(timelib.clock())   --此函数必须放在循环体外
	guid = string.format( "%04X%04X-%04X-%04X-%04X-%04X%04X%04X",
							math.random(0,0xFFFF),
							math.random(0,0xFFFF),
							math.random(0,0xFFFF),
							math.random(0,0xFFFF),
							math.random(0,0xFFFF),
							math.random(0,0xFFFF),
							math.random(0,0xFFFF),
							math.random(0,0xFFFF)
						)
	return guid
end

---------------------------局部函数-----------------------------------------------------------

--检查数据文件版本与程序版本是否一致
local function poifav_checkversion()

	local f_cfg = nil
	local f_cfg_len = 0
	local poifav_ver = nil
	local is_same_ver = true
	
	if ( (not filelib.fexist(indexFileName)) and (not filelib.fexist(dataFileName)) ) then	--没有索引文件和数据文件，初始状态
		--新建配置文件
		filelib.fremove(cfgFileName)
		f_cfg = filelib.fopen(cfgFileName, 3)
		if ( nil==f_cfg ) then
			is_same_ver = false
		else
			is_same_ver = true
			poifav_ver = tostring(verarray[#verarray])
			if ( #poifav_ver == filelib.fwrite(f_cfg, poifav_ver, #poifav_ver) ) then
				is_same_ver = true
			else
				is_same_ver = false
			end
		end
	elseif (not filelib.fexist(cfgFileName)) then	--配置文件不存在
		is_same_ver = false
	elseif ( (not filelib.fexist(indexFileName)) or (not filelib.fexist(dataFileName)) ) then	--文件不完整
		is_same_ver = false
	else 	--所有文件都存在
		f_cfg = filelib.fopen(cfgFileName, 0)
		f_cfg_len = filelib.fgetsize(cfgFileName)
		if (nil==f_cfg) then
			is_same_ver = false
		elseif (0==f_cfg_len) then
			is_same_ver = false
		else		
			poifav_ver = filelib.fread(f_cfg, f_cfg_len)
			if ( (nil==poifav_ver) or (tonumber(poifav_ver)~=verarray[#verarray]) ) then
				is_same_ver = false
			end
		end
	end
	if (nil~=f_cfg) then
		filelib.fclose(f_cfg)
	end
	return is_same_ver
end

--增量升级函数
local function updateFn(lowver, highver)		--在此编辑增量升级函数
	--只需要升级 indexFileName 和 dataFileName 文件
	local is_ok = true

	--在此添加函数
	--print("lowver = ", lowver, " highver=", highver) 

	return is_ok
end

--根据新版本升级数据文件
local function fav_update()

	local f_cfg = nil
	local f_cfg_len = 0
	local poifav_ver = nil
	local update_flag = POIFAV_SUCCESS

	--判断协议版本
	if poifav_checkversion() then
		update_flag = POIFAV_SUCCESS
	elseif ( not filelib.fexist(cfgFileName) ) then
		update_flag = POIFAV_FILE_ERR
	else
		f_cfg = filelib.fopen(cfgFileName, 0)
		f_cfg_len = filelib.fgetsize(cfgFileName)
		if (nil==f_cfg) then
			update_flag = POIFAV_FILE_ERR
		elseif (0==f_cfg_len) then
			update_flag = POIFAV_FILE_ERR
		else			
			poifav_ver = filelib.fread(f_cfg, f_cfg_len)
			if (nil==poifav_ver) then
				update_flag = POIFAV_FILE_ERR
			else
				poifav_ver = tonumber(poifav_ver)
				if ( (poifav_ver<verarray[1]) or (poifav_ver>verarray[#verarray]) ) then  --文件版本值异常
					update_flag = POIFAV_VER_ERR			
				elseif ( poifav_ver==verarray[#verarray] ) then	--与最新协议相同，不用更新
					update_flag = POIFAV_SUCCESS
				else
					--更新数据文件
					for k,v in ipairs(verarray) do						
						if ( v>poifav_ver ) then
							if ( not updateFn(verarray[k-1], v) ) then
								update_flag = POIFAV_UPDATE_ERR
								break
							end
						end
					end
					if update_flag then
						--修改为最新版本
						filelib.fremove(cfgFileName)
						f_cfg = filelib.fopen(cfgFileName, 3)
						if ( nil==f_cfg ) then
							update_flag = POIFAV_FILE_ERR
						else
							update_flag = POIFAV_SUCCESS
							poifav_ver = tostring(verarray[#verarray])
							if ( #poifav_ver == filelib.fwrite(f_cfg, poifav_ver, #poifav_ver) ) then
								update_flag = POIFAV_SUCCESS
							else
								update_flag = POIFAV_FILE_ERR
							end
						end
					end
				end				
			end
		end
		if nil~=f_cfg then
			filelib.fclose(f_cfg)
		end
	end
	
	return update_flag
end

--初始化函数，建立索引表indextable、poigid表poigidtable、空闲资源数组freearray
local function poifavinit()

	local f_index = nil
	local buff = nil
	local status = 0	--状态值：0、删除；1、添加；2、编辑；3、其他
	local id = ""
	local poigid = ""
	local indexpos = 0
	local datapos = 0

	init_flag = true	

	--判断协议版本
	if not poifav_checkversion() then
		init_flag = false
		return init_flag
	end

	if ( (not filelib.fexist(indexFileName)) or (0==filelib.fgetsize(indexFileName)) ) then
		init_flag = true
		indextable = {}
		poigidtable = {}
		freearray = {}
		return init_flag
	end
	f_index = filelib.fopen(indexFileName, 0)
	if nil == f_index then
		init_flag = false
		return init_flag
	end

	init_flag = true
	indextable = {}
	poigidtable = {}
	freearray = {}
	while true do 
		buff = filelib.fread( f_index, INDEX_ITEM_LEN )
		if nil == buff then
			break
		end
		--status
		status = tonumber( string.sub(buff, 1, STATUS_LEN) )
		if ((0 == status) or (3 == status)) then			
			table.insert(freearray,indexpos)
		else
			--id
			id = trim( string.sub(buff, STATUS_LEN+1, STATUS_LEN+ID_LEN) )
			--indexpos			
			indextable[id] = indexpos
			--poigid
			poigid = trim( string.sub(buff, STATUS_LEN+ID_LEN+1, STATUS_LEN+ID_LEN+POIGID_LEN) )
			
			if ( "" ~= poigid ) then
				poigidtable[poigid] = id
			end
			
		end
		indexpos = indexpos + INDEX_ITEM_LEN
	end

	filelib.fclose(f_index)

	return init_flag
end

--获取收藏夹poi点总个数
local function fav_getcount()

	local count = 0

	if ( (not init_flag) and (not poifavinit()) ) then
		return POIFAV_INIT_ERR, 0
	end

	for k,v in pairs(indextable) do
		count = count + 1
	end

	return POIFAV_SUCCESS, count  --返回实际收藏夹个数
end

--判断某个id是否在收藏夹中存在
local function  fav_isexist(id)

	if ( (not init_flag) and (not poifavinit()) ) then
		return POIFAV_INIT_ERR, false
	end

	if ( (nil == indextable) or (nil == id) or ("string" ~= type(id)) or ("" == id) ) then
		return POIFAV_ID_ERR, false
	end

	if ( nil == indextable[id] ) then
		return POIFAV_SUCCESS, false
	end

	return POIFAV_SUCCESS, true	
end

--根据poigid在收藏夹中获取id
local function  fav_getid(poigid)
	
	if ( (not init_flag) and (not poifavinit()) ) then
		return POIFAV_INIT_ERR, false
	end

	if ( (nil == poigidtable) or (nil == poigid) or ("string" ~= type(poigid)) or ("" == poigid) ) then
		return POIFAV_POIGID_ERR, ""
	end

	if ( nil == poigidtable[poigid] ) then
		return POIFAV_SUCCESS, ""
	else
		--if ( nil == indextable[poigidtable[poigid]] ) then
		--	poigidtable[poigid] = nil
		--	return POIFAV_SUCCESS, ""
		--else
			return POIFAV_SUCCESS, poigidtable[poigid]
		--end		
	end
end

--根据id取出一条poi信息到poitable表中
local function poifav_getpoitable(id)

	local f_index = nil	
	local f_data = nil
	local poigid = ""
	local indexpos = 0
	local datapos = 0
	local buff = nil
	local pos = 0
	local errcode = nil
	local existflag = false
	local poitable = nil

	if ( (not init_flag) and (not poifavinit()) ) then
		return nil
	end

	if ( (nil == id) or ("" == id) or (nil == indextable)  
			or (not filelib.fexist(indexFileName)) or (not filelib.fexist(dataFileName)) ) then
		return nil
	end

	errcode, existflag = fav_isexist(id)
	if not existflag then
		return nil
	end

	indexpos = indextable[id]
	f_index = filelib.fopen(indexFileName, 0)
	if nil == f_index then
		return nil
	end
	filelib.fseek(f_index, 0, indexpos)
	buff = filelib.fread(f_index, INDEX_ITEM_LEN)
	poigid = trim( string.sub(buff, STATUS_LEN+ID_LEN+1, STATUS_LEN+ID_LEN+POIGID_LEN) )
	datapos = asciitonumber( string.sub(buff, STATUS_LEN+ID_LEN+POIGID_LEN+1, -1) )
	filelib.fclose(f_index)

	f_data = filelib.fopen(dataFileName, 0)
	if nil == f_data then
		return nil
	end

	buff = nil
	filelib.fseek(f_data, 0, datapos)
	buff = filelib.fread(f_data, DATA_ITEM_LEN)
	filelib.fclose(f_data)

	poitable = {}

	--从buff提取数据
	--poiname,poipinyin,poicategoryId,poilon,poilat,poicity,poiaddress,poiphone,poiremark,poitime
	--id
	poitable[J_KEY_ID] = id
	--poigid
	poitable[J_KEY_POIGID] = poigid
	--poiname
	poitable[J_KEY_POINAME] =  trim( string.sub(buff, pos+1, pos+POINAME_LEN) )
	pos = pos + POINAME_LEN
	--poipinyin
	poitable[J_KEY_POIPINYIN] =  trim( string.sub(buff, pos+1, pos+POIPINYIN_LEN) )
	pos = pos + POIPINYIN_LEN
	--poicategoryId
	poitable[J_KEY_POICATEGORYID] = trim( string.sub(buff, pos+1, pos+POICATEGORYID_LEN) )
	pos = pos + POICATEGORYID_LEN
	--poilon
	poitable[J_KEY_POILON] = trim( string.sub(buff, pos+1, pos+POILON_LEN) )
	pos = pos + POILON_LEN
	--poilat
	poitable[J_KEY_POILAT] = trim( string.sub(buff, pos+1, pos+POILAT_LEN) )
	pos = pos + POILAT_LEN
	--poicity
	poitable[J_KEY_POICITY] =  trim( string.sub(buff, pos+1, pos+POICITY_LEN) )
	pos = pos + POICITY_LEN
	--poiaddress
	poitable[J_KEY_POIADDRESS] =  trim( string.sub(buff, pos+1, pos+POIADDRESS_LEN) )
	pos = pos + POIADDRESS_LEN
	--poiphone
	poitable[J_KEY_POIPHONE] =  trim( string.sub(buff, pos+1, pos+POIPHONE_LEN) )
	pos = pos + POIPHONE_LEN
	--poiremark
	poitable[J_KEY_POIREMARK] =  trim( string.sub(buff, pos+1, pos+POIREMARK_LEN) )
	pos = pos + POIREMARK_LEN
	--poitime
	poitable[J_KEY_POITIME] =  string.sub(buff, pos+1, pos+POITIME_LEN)
	pos = pos + POITIME_LEN

	return poitable
end

--获取单个poi点收藏夹信息，返回一个json字符串
local function fav_getpoi(id)

	local outstr = ""
	local val = nil
	local poitable = nil 
	local jsonpoi = nil

	local errcode = nil
	local existflag = false
	
	if ( (nil == id) or ("" == id) ) then
		return POIFAV_PARAM_ERR, ""
	end

	if ( (not init_flag) and (not poifavinit()) ) then
		return POIFAV_INIT_ERR, ""
	end

	errcode, existflag = fav_isexist(id)

	if existflag then
		poitable = poifav_getpoitable(id)
		if nil~=poitable then
			poitable[J_KEY_POIPINYIN] = nil		--去除拼音键值
			outstr = tiros.json.encode(poitable)
			poitable = nil
			return POIFAV_SUCCESS, outstr
		else
			return POIFAV_FAILD, ""
		end
	else
		return POIFAV_POINOTEXIST_ERR, ""
	end	
end

--初始化排序数组sortarray
local function sortarrayInit()

	local poitable = nil

	if ( (not init_flag) and (not poifavinit()) ) then
		return
	end

	if nil~= sortarray then
		return
	end

	sortarray = {}

	for k,v in pairs(indextable) do
		poitable = poifav_getpoitable(k)
		if ( nil ~= poitable ) then
			if (1==now_sorttype) then	--按照poitime排序
				table.insert( sortarray, k .. HYPHEN .. poitable[J_KEY_POITIME] )				
			elseif (2==now_sorttype) then	--按照poipinyin排序
				table.insert( sortarray, k .. HYPHEN .. poitable[J_KEY_POIPINYIN] )
			else				--按照其他字段排序（若增加其他字段排序，此处可编辑）
				table.insert( sortarray, k .. HYPHEN )
			end
			poitable = nil
		end

	end
end

--排序函数
local function sortFn(a, b)

	local asub = nil
	local bsub = nil
	local apos = nil
	local bpos = nil

	apos = string.find(a, HYPHEN)
	if ( (nil==apos) or (1==apos) or (#a==apos) ) then
		asub = ""
	else
		asub = string.sub(a, apos+#HYPHEN, -1)
	end
	bpos = string.find(b, HYPHEN)
	if ( (nil==bpos) or (1==bpos) or (#b==bpos) ) then
		bsub = ""
	else
		bsub = string.sub(b, bpos+#HYPHEN, -1)
	end
	if ( 1==now_sortmethod ) then  		--升序
		return asub<bsub
	elseif ( 2==now_sortmethod ) then	--降序
		return asub>bsub
	else					--其他排序方法
		return asub<bsub
	end
end

--根据排序的类型获取多个poi点收藏夹信息
--输入参数：indexStart： 开始序号，下标从1开始
--输入参数：endStart：   结束序号，下标从1开始
--输入参数： sorttype:     1、按照poitime排序  2、按照poipinyin排序 （默认按照1排序）
--输入参数： sortmethod:   1、升序  2、降序  （默认按照1排序）
local function fav_getpois(indexStart, indexEnd, sorttype, sortmethod)

	local outstr = ""
	local outstrerr = "{\""..J_KEY_POIS.."\":[],\""..J_KEY_POISCOUNT.."\":0}"
	local count = 0
	local errcode = nil
	local jsonpois = nil
	local jsonpoi = nil
	local jsonarray = nil
	local jsonpoiscount = nil
	local poitable = nil
	local pos = nil
	local id = ""

	if ( (nil==indexStart) or (nil==indexEnd) or (indexStart>indexEnd) ) then
		return POIFAV_PARAM_ERR, outstrerr
	end
	if ( (nil==sorttype) or ("number"~=type(sorttype)) or (1>sorttype) or (2<sorttype) ) then   --默认 按照poitime排序（若增加其他字段排序，此处可编辑）
		sorttype = 1
	end
	if ( (nil==sortmethod) or ("number"~=type(sortmethod)) or (1>sortmethod) or (2<sortmethod) ) then  --默认 升序（若增加其他方法排序，此处可编辑）
		sortmethod = 1
	end

	if ( (not init_flag) and (not poifavinit()) ) then
		return POIFAV_INIT_ERR, outstrerr
	end

	errcode, count = fav_getcount()
	if ( (0==count) or (count<indexStart) ) then
		return POIFAV_RANGE_ERR, outstrerr
	end
	if indexEnd>count then
		indexEnd = count
	end

	--整理sortarray、同时排序

	if (nil==sortarray) then	--没有初始化过sortarray，或者是添加、编辑、删除过poi收藏夹
		now_sorttype = sorttype
		now_sortmethod = sortmethod
		sortarrayInit()
		table.sort( sortarray, sortFn )
	else	--没有变动过poi收藏夹数据
		if (now_sorttype==sorttype) then	--排序类型一样
			if (now_sortmethod~=sortmethod) then	--排序类型一样，但是排序方法不一样
				now_sortmethod = sortmethod
				table.sort( sortarray, sortFn )	--只要重新排序
			else
				--（此处为排序类型一样，排序方法也一样）
			end
			
		else	--排序类型不一样
			now_sorttype = sorttype
			now_sortmethod = sortmethod
			sortarray = nil
			sortarrayInit()
			table.sort( sortarray, sortFn )
		end 
	end		

	if (nil==sortarray) then
		return POIFAV_FAILD, outstrerr
	end

	--输出JSON字符串

	jsonarray = {}

	for k,v in ipairs(sortarray) do

		if k>=indexStart then
			if k<=indexEnd then
				pos = string.find(v, HYPHEN)
				if ( (nil ==pos) or (1==pos) or (#v==pos) ) then
					return POIFAV_FAILD, outstrerr
				end
				id = string.sub(v, 1, pos-1)
				poitable = poifav_getpoitable(id)
				if ( nil ~= poitable ) then
					poitable[J_KEY_POIPINYIN] = nil		--去除拼音键值
					table.insert( jsonarray, poitable )
					poitable = nil
				end
			end
		end
		if (k==indexEnd) then
			break
		end

	end

	jsonpois = {}
	jsonpois[J_KEY_POIS] = jsonarray
	jsonpois[J_KEY_POISCOUNT] = count

	outstr = tiros.json.encode(jsonpois)
	jsonpois = nil

	if nil == outstr then
		return POIFAV_JSON_ERR, outstrerr
	end

	return POIFAV_SUCCESS, outstr	
end

--从poitable表中改变数据（添加或者编辑，不包括删除）
--status:  1-添加  2-编辑
--(1)更新内存中表indextable
--(1)更新内存中表poigidtable
--(2)更新文件indexFileName和dataFileName
--(3)更新freearray
--(4)sortarray = nil
local function poifav_changedata(poitable,status)

	local f_index = nil
	local f_data = nil
	local indexpos = 0
	local datapos = 0
	local buff = ""
	local pinyin = ""
	local poitable_temp = nil
	local poigid_temp = ""

	if ( (nil==poitable) 
		 or (nil == poitable[J_KEY_POILON]) or ("" == poitable[J_KEY_POILON]) 
		 or (nil == poitable[J_KEY_POILAT]) or ("" == poitable[J_KEY_POILAT])
		 or ( (2 == status) and ( (nil == poitable[J_KEY_ID]) or ("" == poitable[J_KEY_ID]) ) )
	   ) then
		return false
	end

	if ( (not init_flag) and (not poifavinit()) ) then
		return false
	end

	--保存原有poigid
	if ( 2 == status ) then
		poitable_temp = poifav_getpoitable(poitable[J_KEY_ID])
		if ( nil ~= poitable_temp ) then
			if ( (nil~=poitable_temp[J_KEY_POIGID]) and (""~=poitable_temp[J_KEY_POIGID]) ) then
				poigid_temp = poitable_temp[J_KEY_POIGID]
			end
			poitable_temp = nil
		end
		
	end

	--打开indexFileName和dataFileName文件
	if filelib.fexist(indexFileName) then
		--读写
		f_index = filelib.fopen(indexFileName, 1)
	else
		--创建
		f_index = filelib.fopen(indexFileName, 3)
	end
	if nil == f_index then
		return false
	end
	if filelib.fexist(dataFileName) then
		--读写
		f_data = filelib.fopen(dataFileName, 1)
	else
		--创建
		f_data = filelib.fopen(dataFileName, 3)
	end
	if nil == f_data then
		filelib.fclose(f_index)
		return false
	end

	--获取indexpos和datapos
	if ( 1 == status ) then  --此为添加		
		--从freearray中取资源
		if ( (nil~=freearray) and (#freearray>0) ) then  --freearray中有资源
			indexpos = freearray[#freearray]
			filelib.fseek(f_index, 0, indexpos)
			buff = filelib.fread(f_index, INDEX_ITEM_LEN)
			filelib.fseek(f_index, 3, INDEX_ITEM_LEN)
			datapos = asciitonumber( string.sub(buff, STATUS_LEN+ID_LEN+POIGID_LEN+1, -1) )
			filelib.fseek(f_data, 0, datapos)
			table.remove(freearray)  --更新freearray
		else  --freearray中无资源
			indexpos = filelib.fgetsize(indexFileName)
			filelib.fseek(f_index, 0, indexpos)
			datapos = filelib.fgetsize(dataFileName)
			filelib.fseek(f_data, 0, datapos)			
		end
	elseif ( 2 == status ) then --此为编辑
		--status = 2
		indexpos = indextable[poitable[J_KEY_ID]]
		filelib.fseek(f_index, 0, indexpos)
		buff = filelib.fread(f_index, INDEX_ITEM_LEN)
		filelib.fseek(f_index, 3, INDEX_ITEM_LEN)
		datapos = asciitonumber( string.sub(buff, STATUS_LEN+ID_LEN+POIGID_LEN+1, -1) )
		filelib.fseek(f_data, 0, datapos)
	end

	--写入indexFileName文件
	--组建要写入indexFileName文件的数据buff
	buff = ""
	--status
	buff = buff .. status
	--id
	if ( 1 == status ) then  --此为添加
		poitable[J_KEY_ID] = newguid()			
	end
	buff = buff .. poitable[J_KEY_ID] .. string.rep(" ", ID_LEN-#poitable[J_KEY_ID])
	--poigid
	if ( nil==poitable[J_KEY_POIGID] ) then
		poitable[J_KEY_POIGID] = ""
	end	
	buff = buff .. poitable[J_KEY_POIGID] .. string.rep(" ", POIGID_LEN-#poitable[J_KEY_POIGID])
	--datapos
	buff = buff .. numbertoascii(datapos, DATAPOS_LEN)
	--写入indexFileName
	if INDEX_ITEM_LEN ~= filelib.fwrite(f_index, buff, INDEX_ITEM_LEN) then
		filelib.fclose(f_index)
		filelib.fclose(f_data)
		return false
	end
	filelib.fclose(f_index)

	--写入dataFileName文件
	--组建要写入dataFileName文件的数据buff
	--poiname,poipinyin,poicategoryId,poilon,poilat,poiaddress,poiphone,poiremark,poitime
	buff = ""
	--poiname
	if ( nil==poitable[J_KEY_POINAME] ) then
		poitable[J_KEY_POINAME] = ""
	end	
	buff = buff .. poitable[J_KEY_POINAME] .. string.rep(" ", POINAME_LEN-#poitable[J_KEY_POINAME])
	--poipinyin
	if ( ""==poitable[J_KEY_POINAME] ) then
		pinyin =  ""
	else	
		pinyin =  ""	--tiros.pinyin.utf8_to_pinyin( string.sub(poitable[J_KEY_POINAME], 1, HZ_NUM*3), false, false, false, true )  --UTF8编码 一个汉字3个字节
	end
	buff = buff .. pinyin .. string.rep(" ", POIPINYIN_LEN-#pinyin)
	--poicategoryId
	if ( nil==poitable[J_KEY_POICATEGORYID] ) then
		poitable[J_KEY_POICATEGORYID] = ""
	end	
	buff = buff .. poitable[J_KEY_POICATEGORYID] .. string.rep(" ", POICATEGORYID_LEN-#poitable[J_KEY_POICATEGORYID])
	--poilon	
	buff = buff .. poitable[J_KEY_POILON] .. string.rep(" ", POILON_LEN-#poitable[J_KEY_POILON])
	--poilat	
	buff = buff .. poitable[J_KEY_POILAT] .. string.rep(" ", POILAT_LEN-#poitable[J_KEY_POILAT])
	--poicity
	if ( nil==poitable[J_KEY_POICITY] ) then
		poitable[J_KEY_POICITY] = ""
	end	
	buff = buff .. poitable[J_KEY_POICITY] .. string.rep(" ", POICITY_LEN-#poitable[J_KEY_POICITY])
	--poiaddress
	if ( nil==poitable[J_KEY_POIADDRESS] ) then
		poitable[J_KEY_POIADDRESS] = ""
	end
	buff = buff .. poitable[J_KEY_POIADDRESS] .. string.rep(" ", POIADDRESS_LEN-#poitable[J_KEY_POIADDRESS])
	--poiphone
	if (  nil==poitable[J_KEY_POIPHONE] ) then
		poitable[J_KEY_POIPHONE] = ""
	end
	buff = buff .. poitable[J_KEY_POIPHONE] .. string.rep(" ", POIPHONE_LEN-#poitable[J_KEY_POIPHONE])
	--poiremark
	if (  nil==poitable[J_KEY_POIREMARK] ) then
		poitable[J_KEY_POIREMARK] = ""
	end
	buff = buff .. poitable[J_KEY_POIREMARK] .. string.rep(" ", POIREMARK_LEN-#poitable[J_KEY_POIREMARK])
	--poitime
	poitable[J_KEY_POITIME] = string.format("%04u%02u%02u%02u%02u%02u",localtime(timelib.time()))
	buff = buff .. poitable[J_KEY_POITIME]
	--写入dataFileName
	if DATA_ITEM_LEN ~= filelib.fwrite(f_data, buff, DATA_ITEM_LEN) then
		filelib.fclose(f_data)
		return false
	end
	filelib.fclose(f_data)

	--更新索引表indextable
	indextable[poitable[J_KEY_ID]] = indexpos

	--更新poigid表poigidtable
	--1、去除原有的项
	if ( (2==status) and (nil~=poigid_temp) and (""~=poigid_temp) ) then
		poigidtable[poigid_temp] = nil
	end
	--2、新增现有项
	if ( (nil ~= poitable[J_KEY_POIGID]) and ("" ~= poitable[J_KEY_POIGID]) ) then
		poigidtable[poitable[J_KEY_POIGID]] = poitable[J_KEY_ID]
	end

	--删除sortarray
	sortarray = nil

	return true
end

--添加单个poi点
--[[添加单个poi点的json串格式：
	  {"poigid":"456789","name":"华贸1","categoryId":"14","lon":"123456789","lat":"123456789","address":"北京市朝阳区","tel":"13468495433","remark":"我的家"}
--]]
local function  fav_add(jsonStr)
	local id = nil
	local poitable = nil
	local errcode = nil
	local existflag = false
	
	if ( (nil==jsonStr) or (""==jsonStr) ) then
		return POIFAV_PARAM_ERR,""
	end

	if ( (not init_flag) and (not poifavinit()) ) then
		return POIFAV_INIT_ERR,""
	end

	poitable = tiros.json.decode(jsonStr)
	if nil == poitable then 
		return POIFAV_JSON_ERR,""
	end

	if ( (nil ~= poitable[J_KEY_POIGID]) and ("" ~= poitable[J_KEY_POIGID]) and ("0" ~= poitable[J_KEY_POIGID]) ) then
		errcode, id = fav_getid(poitable[J_KEY_POIGID])
		errcode, existflag = fav_isexist(id)	
		if existflag then
			--添加不改名包含poigid的点，但是在收藏夹中存在
			return POIFAV_POIEXIST_ERR,""
		end
	end
	
	--将poitable表数据添加到文件中
	math.randomseed(timelib.clock())  --此句必须放在循环体外，否则newguid都一样
	if not poifav_changedata(poitable,1) then
		return POIFAV_FAILD,""
	end

	return POIFAV_SUCCESS,poitable[J_KEY_ID]
end

local function fav_add_single(T)
	local json = tiros.json.encode(T);
	print("poifav---single---json:"..json);
	local errcode
	local sid
	errcode,sid = fav_add(json)
	return errcode,sid	
end

--添加多个poi点
--[[添加多个poi点的json串格式：
{"pois":[
	  {"poigid":"456789","name":"华贸1","categoryId":"15","lon":"123456789","lat":"123456789","address":"北京市朝阳区","tel":"13468495433","remark":"我的家"},
	  {"poigid":"456799","name":"华贸2","categoryId":"16","lon":"123456789","lat":"123456789","address":"北京市朝阳区","tel":"13468495433","remark":"我的家"},
	  ......	
	]
}
--]]
local function  fav_addpois(jsonStr)

	local jsonpois = nil
	local jsonarray = nil
	local jsonpoi = nil
	local id = nil
	local errcode = nil
	local poitable = nil
	local existflag = false

	if ( (nil==jsonStr) or (""==jsonStr) ) then
		return POIFAV_PARAM_ERR
	end
	if ( (not init_flag) and (not poifavinit()) ) then
		return POIFAV_INIT_ERR
	end


	jsonpois = tiros.json.decode(jsonStr)
	if (nil==jsonpois) or (nil==jsonpois[J_KEY_POIS]) then 
		return POIFAV_JSON_ERR
	end


	jsonarray = jsonpois[J_KEY_POIS]


	if #jsonarray>0 then


		math.randomseed(timelib.clock())  --此句必须放在循环体外，否则newguid都一样


		for k,v in ipairs(jsonarray) do


			if ( (nil ~= v[J_KEY_POIGID]) and ("" ~= v[J_KEY_POIGID]) ) then
				errcode, id = fav_getid(v[J_KEY_POIGID])
				if ( (nil~=id) and (""~=id) ) then
					--添加不改名包含poigid的点，但是在收藏夹中存在
					existflag = true					
				end
			end

						
			if not existflag then --不存在则添加

				--将poitable表数据添加到文件中
				if not poifav_changedata(v,1) then
					jsonpois = nil
					return POIFAV_FAILD
				end
			end
		end
	end
	jsonpois = nil
	return POIFAV_SUCCESS
end

--修改一个poi点
--[[编辑单个poi点的json串格式：
	  {"poigid":"456789","name":"华贸1","categoryId":"14","lon":"123456789","lat":"123456789","address":"北京市朝阳区","tel":"13468495433","remark":"我的家"}
--]]
local function  fav_modify(jsonStr)

	local poitable = nil
	local id = nil
	local errcode = nil
	local existflag = false

	if ( (nil==jsonStr) or (""==jsonStr) ) then
		return POIFAV_PARAM_ERR
	end

	if ( (not init_flag) and (not poifavinit()) ) then
		return POIFAV_INIT_ERR
	end

	poitable = tiros.json.decode(jsonStr)
	if nil == poitable then 
		return POIFAV_JSON_ERR
	end

	if nil == poitable[J_KEY_ID] then
		return POIFAV_ID_ERR
	end
	if "" == poitable[J_KEY_ID] then
		return POIFAV_ID_ERR
	end
	errcode, existflag = fav_isexist(poitable[J_KEY_ID])
	if not existflag then  --如果不存在则不能修改
		return POIFAV_POINOTEXIST_ERR		
	end

	if ( (nil~=poitable[J_KEY_POIGID]) and (""~=poitable[J_KEY_POIGID]) ) then
		errcode, id = fav_getid(poitable[J_KEY_POIGID])
		errcode, existflag = fav_isexist(id)	
		if ( existflag and (id~=poitable[J_KEY_ID]) ) then	--要求更新后新的poigid在原有的收藏夹中存在，且为其他的收藏夹条目
			return POIFAV_POIGID_ERR
		end
	end

	--将修改数据添加到文件中
	math.randomseed(timelib.clock())  --此句必须放在循环体外，否则newguid都一样
	if not poifav_changedata(poitable,2) then
		return POIFAV_FAILD
	end

	return POIFAV_SUCCESS
end

--删除一个poi点
local function  fav_del(id)

	local status = 0
	local poitable = nil
	local indexpos = 0
	local f_index = nil
	local errcode = nil
	local existflag = false

	if ( (nil==id) or (""==id) ) then
		return POIFAV_PARAM_ERR
	end

	if ( (not init_flag) and (not poifavinit()) ) then
		return POIFAV_INIT_ERR
	end

	errcode, existflag = fav_isexist(id)
	if not existflag then  --如果不存在则不能删除
		return POIFAV_POINOTEXIST_ERR
	else
		--更新indexFileName文件的status字段
		indexpos = indextable[id]
		
		if not filelib.fexist(indexFileName) then
			return POIFAV_FILE_ERR
		end
		f_index = filelib.fopen(indexFileName, 1)  --读写
		if nil == f_index then
			return POIFAV_FILE_ERR
		end		
		filelib.fseek(f_index, 0, indexpos)
		if STATUS_LEN ~= filelib.fwrite(f_index, status, STATUS_LEN) then			
			filelib.fclose(f_index)
			return POIFAV_FILE_ERR
		end
		filelib.fclose(f_index)

		--更新indextable、poigidtable、freearray、sortarray
		table.insert(freearray, indextable[id])

		poitable = poifav_getpoitable(id)
		if nil~=poitable then
			if ( (nil~=poitable[J_KEY_POIGID]) and (""~=poitable[J_KEY_POIGID]) ) then
				poigidtable[poitable[J_KEY_POIGID]] = nil
			end
			poitable = nil
		end
				
		sortarray = nil

		indextable[id] = nil

		return POIFAV_SUCCESS	
	end
end

--删除所有收藏夹数据
local function  fav_delall()

	local bflag = POIFAV_FAILD

	init_flag = false
	
	indextable = nil
	poigidtable = nil
	freearray = nil	
	sortarray = nil
	
	if filelib.fremove(cfgFileName) and filelib.fremove(indexFileName) and filelib.fremove(dataFileName) then
		bflag = POIFAV_SUCCESS
	end
	return bflag	
end

---------------------------全局函数(对外接口)--------------------------------------------------
--接口table
local interface = {}

--对外声明poifav_getcount函数接口
--获取收藏夹中poi点总个数
--输出：number型 错误码,number型 总数
createmodule(interface,"poifav_getcount", function ()
	local errcode
	local count
	errcode, count = fav_getcount()
	return errcode, count
end)

--对外声明poifav_isexist函数接口
--判断某个poi点是否在收藏夹中存在
--输入：id:string型参数
--输出：number型错误码
createmodule(interface,"poifav_isexist", function (id)
	local errcode
	local bexist
	errcode, bexist = fav_isexist(id)
	return errcode, bexist
end)

--根据poigid在收藏夹中获取id
createmodule(interface,"poifav_getid", function (poigid)
	local errcode
	local sid
	errcode, sid = fav_getid(poigid)
	return errcode, sid
end)

--对外声明poifav_getpoi函数接口
--获取单个poi点收藏夹信息
--输入：id:string型参数
--输出：errcode错误码;string的json字符串，如果没有找到则返回""字符串
--[[返回的单个poi点的json串格式：
	  {"poigid":456789,"name":"华贸1","categoryId":15,"lon":123456789,"lat":123456789,"address":"北京市朝阳区","tel":"13468495433","remark":"我的家"}
--]]
createmodule(interface,"poifav_getpoi", function (id)
	local errcode
	local spoi
	errcode, spoi = fav_getpoi(id)
	return errcode, spoi
end)

--对外声明poifav_getpois函数接口
--获取多个poi点收藏夹信息
--输入索引下标从1开始
--输入：indexStart:number型参数 下表索引 开始位置
--输入：indexEnd:number型参数 下表索引   结束位置
--输入：sorttype:number型参数  排序类型 1、按照时间排序  2、按照拼音排序
--输入：sortmethod:number型参数 排序方法 1、升序  2、降序
--输出：errcode错误码;string的json字符串，如果没有找到则返回""字符串
--[[返回多个poi点的json串格式：
{"pois":[
	  {"poigid":456789,"name":"华贸1","categoryId":15,"lon":123456789,"lat":123456789,"address":"北京市朝阳区","tel":"13468495433","remark":"我的家"},
	  {"poigid":456799,"name":"华贸2","categoryId":15,"lon":123456789,"lat":123456789,"address":"北京市朝阳区","tel":"13468495433","remark":"我的家"},
	  ......	
	]
}
--]]
createmodule(interface,"poifav_getpois", function (indexStart, indexEnd, sorttype, sortmethod)
	local errcode
	local spois
	errcode, spois = fav_getpois(indexStart, indexEnd, sorttype, sortmethod)
	return errcode, spois
end)

--对外声明poifav_add函数接口
--添加单个poi点函数
--[[添加单个poi点的json串格式：
	  {"poigid":456789,"name":"华贸1","categoryId":15,"lon":123456789,"lat":123456789,"address":"北京市朝阳区","tel":"13468495433","remark":"我的家"}
--]]
--输入：jsonStr:string型参数
--输出：number型，执行结果代码
createmodule(interface,"poifav_add", function (jsonStr)
	local errcode
	local sid
	errcode,sid = fav_add(jsonStr)
	return errcode,sid
end)

--以参数的形式单个添加收藏夹记录
createmodule(interface,"poifav_add_single",function(T)

	local errcode
	local sid
	errcode,sid = fav_add_single(T)
	--print("poifav_add_single------------------------------------:"..errcode.."    "..sid);
	return errcode,sid
end)

--对外声明poifav_addpois函数接口
--添加多个poi点
--[[添加多个poi点的json串格式：
{"pois":[
	  {"poigid":456789,"name":"华贸1","categoryId":15,"lon":123456789,"lat":123456789,"address":"北京市朝阳区","tel":"13468495433","remark":"我的家"},
	  {"poigid":456799,"name":"华贸2","categoryId":16,"lon":123456789,"lat":123456789,"address":"北京市朝阳区","tel":"13468495433","remark":"我的家"},
	  ......	
	]
}
--]]
--输入：jsonStr:string型参数
--输出：number型，执行结果代码
createmodule(interface,"poifav_addpois", function (jsonStr)
	local errcode = fav_addpois(jsonStr)
	return errcode
end)

--对外声明poifav_modify函数接口
--修改poigid对应到poi点信息，此poi点由poigid唯一标识
--[[编辑单个poi点的json串格式：
	  {"poigid":456789,"name":"华贸1","categoryId":15,"lon":123456789,"lat":123456789,"address":"北京市朝阳区","tel":"13468495433","remark":"我的家"},
--]]
--输入：jsonStr:string型参数
--输出：number型，执行结果代码
createmodule(interface,"poifav_modify", function (jsonStr)
	local errcode = fav_modify(jsonStr)
	return errcode
end)

--对外声明poifav_del函数接口
--删除一个poi点
--输入：id:string型参数
--输出：number型，执行结果代码
createmodule(interface,"poifav_del", function (id)
	local errcode = fav_del(id)
	return errcode
end)

--对外声明poifav_delall函数接口
--删除所有收藏夹数据
--输出：number型，执行结果代码
createmodule(interface,"poifav_delall", function ()
	local errcode = fav_delall()
	return errcode
end)

--根据新协议版本升级数据文件
--输出：number型，执行结果代码
createmodule(interface,"poifav_update", function ()
	local errcode = fav_update()
	return errcode
end)

tiros.poifavorites = readOnly(interface)
---------------------------测试代码----------------------------------------------------------

