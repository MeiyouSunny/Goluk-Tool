
--[[
-- @描述:把收藏夹中公共数据导入到第一个登录用户的帐户下，并且删除公共数据
-- @编写人:宣东言
-- @创建日期: 2013-05-23 13:51:11
--]]

require"lua/moduledata"
require"lua/systemapi/sys_namespace"

--接口table
local interface = {};


local function upgrade()

	print("favupgrade------------------------------------11111");
	--获取用户登录id
	local suid = tiros.moduledata.moduledata_get("framework", "uid");
	if suid == nil or suid == "" then
		return;	
	end
	--收藏夹中数据个数
	local errcode;
	local count ;

	errcode, count = tiros.poifavorites.poifav_getcount();

	if count <= 0 then 
		print("favupgrade------------------------3333: " .. tostring(count));
		return;	
	end
	--读取公共收藏夹数据,保存到已登录用户收藏夹里，
	local spois; 
	errcode, spois = tiros.poifavorites.poifav_getpois(1, count, 1, 1);

	if spois == nil or spois == "" then
		print("favupgrade------------------------------------5555555");
		return;	
	end
	print("favupgrade-----------------5656565 ：  " .. tostring(spois));
	local t_pois = tiros.json.decode(spois);

	for k,v in pairs(t_pois.pois) do

		local t_singleData = {};

		t_singleData.lon = v.lon;
		t_singleData.poigid = v.poigid;
		--pid
		t_singleData.lat = v.lat;
		t_singleData.tel = v.tel;
		t_singleData.name = v.name;
		t_singleData.address = v.address;
		--time
		t_singleData.remark = v.remark;
		t_singleData.categoryId = v.categoryId;
		--city
		
		local data = tiros.json.encode(t_singleData);

		print("favupgrade------777777:".. tostring(data));


		tiros.favoritesync.add(data,true);
		tiros.poifavorites.poifav_del(v.pid);
					
	end

	print("favupgrade------------------------------------8888");
	--删除公共收藏夹数据
	--tiros.poifavorites.poifav_delall();

	print("favupgrade------------------------------------99999");

end


--[[
--功能说明: 把未登录用户的收藏夹数据 复制到已登录用户里，并删除数据
--参数:无
--返回值:无
--]]
createmodule(interface,"favupgrade",function ()

	upgrade();

end)

tiros.favoriteupgrade = readOnly(interface);

