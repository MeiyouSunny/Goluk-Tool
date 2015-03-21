--http接口封装
--目前主要对外httpsend及httpabort接口

require"lua/systemapi/sys_namespace"
require"lua/systemapi/sys_handle"

--_gHttplist：全局变量，用于存放正在使用的所有http句柄
local _gHttplist = {}
--_gHttpweaklist：全局变量，用于存放所有http句柄的week表，week表中既包含正使用的句柄，也包含即将回收的句柄
local _gHttpweaklist = {}
setmetatable(_gHttpweaklist,{__mode ="v" })

--[[
http全局变量table，协定
htable = {}
0: http句柄
1：调用方类型：0：lua，1：js， 2：c
2：lua回调函数地址
3: js注册回调函数名称
4：c回调函数指针地址
5：c调用者传输数据地址
--]]

--创建http句柄函数接口
--htype：string型参数，用于唯一标识该http句柄
----输出：实际创建的http句柄描述
local function httpcreate(htype)
	local htable =getHandle(_gHttpweaklist,htype)
	if htable == nil then
		htable = {};
	   	htable[0] = httplib.plugincreate()
	end
	registerHandle(_gHttplist,_gHttpweaklist,htype,htable)
	return htable;	
end

--销毁http句柄函数接口：htype为string型参数，用于唯一标识该http句柄
--该函数并没有立即销毁http句柄，而是等到下一个回收cd之后才会彻底销毁
--输出：无
local function httpdestroy(htype)
	local htable =getHandle(_gHttplist,htype)
	if htable ~= nil then
	   	httplib.cancel(htable[0]);
	end
	releaseHandle(_gHttplist,htype)			
end

--添加http头信息接口
--h: userdata型参数，用于标识http句柄
--header:string型参数，http信息头
--value:string型参数，http信息头对应的内容
--输出：无
local function httpaddheader(h,header,value)
	httplib.addheader(h,header,value)
end

--注册http回调函数接口
--htable:table型参数，用于标识http句柄描述
--cbkname:会动态依据不同的ntype来确定类型(lua：function型，js：string型，c：integer型)
--htype:string型参数，js端用于标识该http句柄的唯一标识符
--ntype:integer型参数，用于标识该回调函数类型（lua：0，js：1，c：2）
--mtype:string型参数，用于设定该http请求的模块类型，其值具体参考邮件
--stype：string型参数，用于设定http请求的服务类型，其值请参考邮件
--输出：无
local function httpnotify(htable,cbkname,htype,ntype,nuser,mtype,stype)
	htable[1]=ntype;
	if ntype == 0 then		--lua脚本注册回调函数
		htable[2] = cbkname;
	elseif ntype == 1 then		--js注册回调函数
		htable[3] = cbkname;
	else				--c回调函数地址
		htable[4] = cbkname;
		htable[5] = nuser;
	end
	if mtype ~= nil and stype ~= nil then
		httplib.setservicetype(htable[0], mtype, stype);
	end
	httplib.registernotify(htable[0],"sys_HttpEvnet",htype)
end

--post请求http函数接口
--h:userdata型参数，用于标识http句柄
--url:string型参数，post请求的url地址
--data：string型参数，post请求的数据内容，该类型只能是string型数据
--输出：请求成功返回true，失败返回false
local function httppost(h,url,data,contenttype)
	local strlen = string.len(data)
	local content = "text/html"
	if contenttype then
		content = contenttype
	end
	return httplib.post(h,content,url,data,strlen)
end

--get请求http函数接口
--h:userdata型参数，用于标识http句柄
--url:string型参数，get请求的url地址
--输出：请求成功返回true，失败返回false
local function httpget(h,url)
	return httplib.get(h,url)
end

--http完整请求函数接口：
local function sys_httpsend(mtype,stype,htype,ntype,url,cbkname,nuser,data,...)
	if htype == nil then 
		return false;
	end
	if url == nil then
		return false;
	end
	local htable = httpcreate(htype);	
	if htable == nil then	
	   	return false;
	end
	--lua code notify
	httpnotify(htable,cbkname,htype,ntype,nuser,mtype,stype);
	local contenttype = nil
	
	for k,v in pairs(arg) do
		if type(v) == "table" then
			for k1,v1 in pairs(v) do
				if k1 == "Content-Type" then
					contenttype = v1
				else
					if k1 ~= nil and v1 ~= nil then
						httpaddheader(htable[0],k1,v1)
					end
				end
			end
		else
			local b = 0
			local s,e = string.find(v, ":", b) --解析header的field和val,协定以“:”分割
			if s == nil then
			 	break
			else
				local max = string.len(v)
				local strHeader = string.sub(v,1,s-1)
				local strValue = string.sub(v,s+1,max)
				if strHeader == "Content-Type" then
					contenttype = strValue
				else
			 		httpaddheader(htable[0],strHeader,strValue)
				end
			end 
		end     	  
    	end
	if data~= nil then
		return httppost(htable[0],url,data,contenttype)
	else
		return httpget(htable[0],url)
	end
end

--lua层http事件回调处理函数：
DeclareGlobal("sys_HttpEvnet",function (htype,event,param1, param2)
	local htable =getHandle(_gHttplist,htype)
	--print("sys_HttpEvnet",htype,event,param1, param2)
	if event == 4 or event == 5 then
		httpdestroy(htype);
	end
	if htable ~= nil then
		if htable[1] == 0 then
			if htable[2] ~= nil then
				htable[2](htype,event,param1, param2);
			end
		elseif htable[1] == 1 then
		--js回调
			if htable[3] ~= nil then
				local s;
				if event == 3 then --EVT_HTTP_REQUEST
					s = string.format("%s( '%s', %u, '%s', '%s' );", htable[3],htype,event,param1,param2);
				elseif event == 4 then
					s = string.format("%s( '%s', %u, %u, '%s' );", htable[3],htype, event,param1,string.sub(param2, 1, param1));
				else
					s = string.format("%s( '%s', %u, %u, %u );", htable[3],htype,event,param1,param2);
				end
				commlib.calljavascript(s);
			end	
		else
		--c回调
			if htable[4] ~= nil then
				commlib.httpnotify(htable[4], htype, htable[5],event,param1,param2);
			end
		end
	end
end)

--接口table
local interface = {}

--对外声明lua层调用http请求函数接口
--mtype:string型参数，用于设定该http请求的模块类型，其值具体参考邮件
--stype：string型参数，用于设定http请求的服务类型，其值请参考邮件
--htype:string型参数，js端用于标识该http句柄的唯一标识符
--url:string型参数，http请求的url地址
--cbkname：function型参数，lua端注册http的回调函数地址
--data： 若为nil则为get请求，否则为post请求（string型参数，post请求的数据内容，该类型只能是string型数据）
--...为可变参数，用于追加http的请求头信息，可以为多个，每个请求头信息原型为："header:value"型字符串
--输出：请求成功返回true，失败返回false
createmodule(interface,"httpsendforlua",function (mtype,stype,htype,url,cbkname,data,...)
--print(htype,url,cbkname,data,...)
	return sys_httpsend(mtype,stype,htype,0, url,cbkname,nil,data,...)
end)

--对外声明终止http请求函数接口
--htype:string型参数，js端用于标识该http句柄的唯一标识符
--输出：无
createmodule(interface,"httpabort", function (htype)
	httpdestroy(htype)
end)

tiros.http = readOnly(interface)

