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
 @ 功能描述：http接口
 @ 提供接口：
（1）(HttpSend)对外公开http请求接口；
（2）(HttpAbort)中断http请求接口；
</pre>

--]]
print("air lua base 1")
require "lua/systemapi/sys_namespace"
print("air lua base 2")
require "lua/systemapi/sys_handle"
print("air lua base 3")
require "lua/json"
print("air lua base 4")
require "lua/base/base_file"
print("air lua base 5")

local interface = {};
--资源文件地址路径
local resPath = "res/api/api.rs";

--文档格式,注释内容都需要//..//包裹起来
--[[
{
	"poi_keyword":[1101,"key_search"],
}
//poi_keyword:请求服务别名,自定义//
//1101:服务对应id,魏俊提供//
//key_search:请求服务对应的性能统计key,张亚磊提供//
--]]
--获取web服务配置文档
--[[
local webServerConfig = nil;
local webConfigStr = tiros.base.file.ReadFile("fs6:/" .. tiros.web.FilePath .. "source/http");
if webConfigStr ~= nil and #webConfigStr > 0 then
	--格式化字符串,删除注释内容
	local webSubStr = string.gsub(webConfigStr, "//.*//", "");
	if #webSubStr > 0 then
		webServerConfig = tiros.json.decode(webSubStr);
	end
end
--]]
print("air lua base 6")
--获取logic服务配置文档
local logicServerConfig = nil;
local logicConfigStr = tiros.base.file.ReadFile("fs6:/lua/config/logichttpconfig");
if logicConfigStr ~= nil and #logicConfigStr > 0 then
	--格式化字符串,删除注释内容
	local logicSubStr = string.gsub(logicConfigStr, "//.*//", "");
	if #logicSubStr > 0 then
		logicServerConfig = tiros.json.decode(logicSubStr);
	end
end

--保存已经获取的url
local webSendUrl = {};
local logicSendUrl = {};

--统计模块标识
local moduleType = {web = "cdc_client",logic = "cdc_client"};

--local urlTable = {[2302] = "http://192.168.3.197:9080/navidog4MeetTrans/addtalkgroup.htm",[2303] = "http://192.168.3.197:9080/cdcServer/exitGroup.htm"};

local urlTable = {[2302] = "http://server.xiaocheben.com/cdcServer/getGroupInfo.htm",[2303] = "http://server.xiaocheben.com/cdcServer/exitGroup.htm"};


--服务返回数据
local serverData = {};

print("air lua base 7")
local function send(id,callback,url,opt,stype)
	local data = nil;
	local method = opt.method;
	if method == "GET" then
		if opt.data ~= nil then
			local condi = "";
			if type(opt.data) == "table" then
				for k,v in pairs(opt.data) do
					condi = condi .. k .. "=" .. tostring(v) .. "&";
				end
			else
				condi = tostring(opt.data);
			end
			if #condi > 0 then
				url = url .. "?" .. tiros.commfunc.EnCodeUrl(string.sub(condi,0,string.len(condi)-1));
			end
			
			print("grouplist getGroupidByType begin-------request url : " .. tostring(url));
		end
	else
		--POST请求
		if opt.data ~= nil then
			local condi = "";
			if type(opt.data) == "table" then
				for k,v in pairs(opt.data) do
					condi = condi .. k .. "=" .. tostring(v) .. "&";
				end
			else
				condi = tostring(opt.data);
			end
			if #condi > 0 then
				data = tiros.commfunc.EnCodeUrl(string.sub(condi,0,string.len(condi)-1));
			end
		end
		if (type(opt.header)) == "table" then
			if opt.header["Content-Type"] == nil then
				opt.header["Content-Type"] = "application/x-www-form-urlencoded";
			end
		else
			opt.header = {};
			opt.header["Content-Type"] = "application/x-www-form-urlencoded";
		end
	end
	
	--获取服务模块
	local mtype = moduleType[opt.form];

	--print("webres------url------"..url);
	tiros.http.httpsendforlua(mtype,stype,id,url,function(id,state,code,content)
		--print("------"..id.."---"..state.."---"..code.."---"..content);
		if state == 2 then
			if code ~= 200 then
				interface.WebHttpAbort(id);
				if type(callback) == "string" then
					local s = string.format("%s('%s',%u,'%s');",callback,id,0,tostring(content));
					commlib.calljavascript(s);
				elseif type(callback) == "function" then
					callback(id,0,tostring(content));
				end	
			end
		elseif state == 3 then
			if serverData[id] == nil then
				serverData[id] = string.sub(content,1,code);
			else
				serverData[id] = serverData[id]..string.sub(content,1,code);
			end
		elseif state == 4 then
			--{"istr":"服务器出错了","itype":"3"}
			if type(callback) == "string" then
				--local s = string.format("%s('%s',%u,'%s');",callback,id,1,serverData[id]);
				local s = callback .. "('" .. id .. "'," .. 1 .. "," .. serverData[id] .. ")";
				commlib.calljavascript(s);
			elseif type(callback) == "function" then
				callback(id,1,serverData[id]);
			end
			serverData[id] = nil;
		elseif state == 5 then
			if type(callback) == "string" then
				local s = string.format("%s('%s',%u,'%s');",callback,id,0,tostring(content));
				commlib.calljavascript(s);
			elseif type(callback) == "function" then
				callback(id,0,tostring(content));
			end
			serverData[id] = nil;
		end
	end,data,opt.header);
end

--[[
http请求通用接口
id:请求标识句柄
callback:请求回调函数
server:请求服务标识
opt:请求参数,数据模板如下
opt = {
	form = "web", --请求发起模块 web 或者 logic
	method = "GET",--请求方式 GET 或者 POST
	header = {actionlocation = ""},--请求头信息
	data = {a=1}, --请求参数
}
devurl:开发测试url
请求实例
local opt = {};
	opt.form = "web";
	opt.header = {};
	opt.header["actionlocation"] = "/navidog2Advert/smartad/smartad_getSmartAd.htm";
	opt.method = "GET";
	--请求参数
	local condi = { a = 1};
	--绑定参数
	opt.data = condi;
	tiros.base.http.HttpSend(id,getAdComplete,"mainad",opt);
--]]
createmodule(interface,"HttpSend",function(id,callback,server,opt,devurl)
	if id == nil or callback == nil or server == nil then 
		return false;
	end

	local url = nil;
	--服务地址标识
	local serverMark = nil;
	--服务统计关键字
	local serverType = nil;

	if opt.form == "web" then
		--获取服务地址标识
		if webServerConfig ~= nil then
			if webServerConfig[server] ~= nil then
				serverMark = webServerConfig[server][1];
				--获取服务统计关键字
				serverType = webServerConfig[server][2];
			else
				--print("!!!!!!!!!!!!!!web serverMark no found!! serverMark = " .. server);
			end
		else
			--print("!!!!!!!!!!!!!!webServerConfig no found!!!!");
		end
		url = webSendUrl[server];
	elseif opt.form == "logic" then
		if logicServerConfig ~= nil then
			if logicServerConfig[server] ~=nil then
				--获取服务地址标识
				serverMark = logicServerConfig[server][1];
				--获取服务统计关键字
				serverType = logicServerConfig[server][2];
			else
				--print("!!!!!!!!!!!!!!Logic serverMark no found!! serverMark = " .. server);
			end
		else
			--print("!!!!!!!!!!!!!!logicServerConfig no found!!!!");
		end
		url = logicSendUrl[server];
	end

	if serverType ~= nil then
		if url == nil then
			--url = tiros.framework.getUrlFromResource(resPath,serverMark);
			url = urlTable[serverMark];
			--保存已请求的服务地址
			if opt.form == "web" then
				webSendUrl[server] = url;
			elseif opt.form == "logic" then
				logicSendUrl[server] = url;
			end
		end
	else
		serverType = "";
		url = "";
	end
	--开发专用地址
	if devurl ~= nil then
		url = devurl;
		if opt.method == "GET" then
			--如果是get请求,过滤请求参数			
			opt.data = {};
		end
	end
	if url == "" then
		--print("!!!!!!!!!!!!!!no url request!!!!!!!!!!!!!!");
		return;
	end
	send(id,callback,url,opt,serverType);
end);
print("air lua base 8")
--中断http请求
createmodule(interface,"HttpAbort",function(id)
	if id ~= nil then
		tiros.http.httpabort(id);
		--删除请求对象
		serverData[id] = nil;
	end 
end);
print("air lua base 9")

tiros.base.http = readOnly(interface);
