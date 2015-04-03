--lua脚本poi_fav对外接口

require"lua/poi_favorites/sys_poi_favorites"

--(1) bool poifav_add(jsonStr)
--添加单个poi点函数
--[[添加单个poi点的json串格式：
	  {"poigid":"456789","name":"华贸1","categoryId":"15","lon":"123456789","lat":"123456789","address":"北京市朝阳区","tel":"13468495433","remark":"我的家"}
--]]
--输入：jsonStr:string型参数
--输出：bool型，返回true：成功 false：失败
--接口约定：
-- 	(a)jsonStr中 "poigid", "name", "categoryId", "lon"及"lat" 五个字段的key和value必须存在且不能传入空值或者""
-- 	(b)jsonStr中 "address", "tel", "remark" 如果不传入值那么默认置为""值
 
--(2) bool poifav_addpois(jsonStr)
--添加多个poi点
--[[添加多个poi点的json串格式：
{"pois":[
	  {"poigid":"456789","name":"华贸1","categoryId":"15","lon":"123456789","lat":"123456789","address":"北京市朝阳区","tel":"13468495433","remark":"我的家"},
	  {"poigid":"456799","name":"华贸2","categoryId":"16","lon":"123456789","lat":"123456789","address":"北京市朝阳区","tel":"13468495433","remark":"我的家"},
	  ......	
	]
}
--]]
--输入：jsonStr:string型参数
--输出：bool型，返回true：成功 false：失败
--接口约定：
-- 	(a)jsonStr中 "poigid", "name", "categoryId", "lon"及"lat" 五个字段的key和value必须存在且不能传入空值或者""
-- 	(b)jsonStr中 "address", "tel", "remark" 如果不传入值那么默认置为""值

--(3) bool poifav_del(id)
--删除一个poi点
--输入：id:string型参数
--输出：bool型，返回true：成功 false：失败

--(4) bool poifav_modify(jsonStr)
--修改poigid对应到poi点信息，此poi点由poigid唯一标识
--[[编辑单个poi点的json串格式：
	  {"poigid":"456789","name":"华贸1","categoryId":"15","lon":"123456789","lat":"123456789","address":"北京市朝阳区","tel":"13468495433","remark":"我的家"},
--]]
--输入：jsonStr:string型参数
--输出：bool型，返回true：成功 false：失败
--接口约定：
-- 	(a)jsonStr中 "poigid", "name", "categoryId", "lon"及"lat" 五个字段的key和value必须存在且不能传入空值或者""
-- 	(b)jsonStr中 "address", "tel", "remark" 如果不传入值那么默认修改为""值

--(5) int poifav_getcount()
--获取收藏夹中poi点总个数
--输出：number 总数

--(6) char* poifav_getpoi(id)
--获取单个poi点收藏夹信息
--输入：id:string型参数
--输出：返回string的json字符串，如果没有找到则返回""字符串
--[[返回的单个poi点的json串格式：
	  {"poigid":"456789","name":"华贸1","categoryId":"15","lon":"123456789","lat":"123456789","address":"北京市朝阳区","tel":"13468495433","remark":"我的家"}
--]]

--(7) char* poifav_getpois(indexStart, indexEnd, sorttype, sortmethod)
--获取多个poi点收藏夹信息
--输入索引下标从1开始
--输入：indexStart:number型参数 下表索引 开始位置
--输入：indexEnd:number型参数 下表索引   结束位置
--输入：sorttype:number型参数  排序类型 1、按照时间排序  2、按照拼音排序 (不输入此参数，则默认为 1)
--输入：sortmethod:number型参数 排序方法 1、升序  2、降序  (不输入此参数，则默认为 1)
--输出：返回string的json字符串，如果没有找到则返回""字符串
--[[返回多个poi点的json串格式：
{"pois":[
	  {"poigid":"456789","name":"华贸1","categoryId":"15","lon":"123456789","lat":"123456789","address":"北京市朝阳区","tel":"13468495433","remark":"我的家"},
	  {"poigid":"456799","name":"华贸2","categoryId":"15","lon":"123456789","lat":"123456789","address":"北京市朝阳区","tel":"13468495433","remark":"我的家"},
	  ......	
	]
}
--]]

--(8) bool poifav_isexist(id)
--判断某个poi点是否在收藏夹中存在
--输入：id:string型参数
--输出：bool型，返回true：存在 false：不存在

--(9) bool poifav_getid(poigid)
--根据poigid在收藏夹中获取id
--输入：poigid:string型参数
--输出：string型，返回id

--(10) bool poifav_delall()
--删除所有收藏夹数据
--输出：bool型，返回true：成功 false：失败

--(11) bool poifav_update()
--根据新协议版本升级数据文件
--输出：bool型，返回true：成功 false：失败

---------------------------测试代码----------------------------------------------------------

--[[
print("Test Start")

--print(poifav_update())
--poifav_delall()

local errcode
local id

--print(newguid())

print("poifav_add1=", tiros.poifavorites.poifav_add("{\"poigid\":\"6834888\",\"name\":\"通华贸88\",\"categoryId\":\"15\",\"lon\":\"144456\",\"lat\":\"456777\",\"tel\":\"1334595433\",\"city\":\"北京市\",\"address\":\"北京市朝阳区88\",\"remark\":\"我的家88\"}"))

print("poifav_add2=", tiros.poifavorites.poifav_add("{\"poigid\":\"7934888\",\"name\":\"水华贸88\",\"categoryId\":\"15\",\"lon\":\"144456\",\"lat\":\"456777\",\"tel\":\"1334595433\",\"city\":\"北京市\",\"address\":\"北京市朝阳区88\",\"remark\":\"我的家88\"}"))

print("poifav_add3=", tiros.poifavorites.poifav_add("{\"poigid\":\"9456888\",\"name\":\"文华贸88\",\"categoryId\":\"15\",\"lon\":\"144456\",\"lat\":\"456777\",\"tel\":\"1334595433\",\"city\":\"北京市\",\"address\":\"北京市朝阳区88\",\"remark\":\"我的家88\"}"))

print("poifav_add4=", tiros.poifavorites.poifav_add("{\"name\":\"aa江华贸88\",\"categoryId\":\"15\",\"lon\":\"144456\",\"lat\":\"456777\",\"tel\":\"1334595433\",\"city\":\"北京市\",\"address\":\"北京市朝阳区88\",\"remark\":\"我的家88\"}"))

print("poifav_addpois1=", tiros.poifavorites.poifav_addpois("{\"pois\":[{\"poigid\":\"45679989\",\"name\":\"非华贸1\",\"categoryId\":\"15\",\"lon\":\"123456\",\"lat\":\"456789\",\"tel\":\"13468495433\",\"city\":\"北京市\",\"address\":\"北京市朝阳区\",\"remark\":\"我的家\"},{\"poigid\":\"922955999\",\"name\":\"李华贸2\",\"categoryId\":\"15\",\"lon\":\"123456\",\"lat\":\"456789\",\"city\":\"北京市\",\"tel\":\"13468495433\"}]}"))


errcode, id = tiros.poifavorites.poifav_getid("6834888")
print("poifav_getid(\"6834888\")=",tiros.poifavorites.poifav_getid("6834888"))
print("poifav_isexist(\""..id.."\")=",tiros.poifavorites.poifav_isexist(id))
print("poifav_getpoi(\""..id.."\")=",tiros.poifavorites.poifav_getpoi(id))
print("poifav_getcount()=", tiros.poifavorites.poifav_getcount())
print("poifav_getpois(1,3,2,2)=",tiros.poifavorites.poifav_getpois(1,3,2,2))
print("poifav_getpois(1,3,2,1)=",tiros.poifavorites.poifav_getpois(1,3,2,1))
print("poifav_getpois(1,3)=",tiros.poifavorites.poifav_getpois(1,3))
print("poifav_getpois(3,4,1,1)=",tiros.poifavorites.poifav_getpois(3,4,1,1))
print("poifav_getpois(1,3)=",tiros.poifavorites.poifav_getpois(1,3))
print("modify856799=",tiros.poifavorites.poifav_modify("{\"poigid\":\"856799\",\"name\":\"kk华贸4667\",\"categoryId\":\"15\",\"lon\":\"127756\",\"lat\":\"433789\",\"tel\":\"13345295433\",\"city\":\"yy北京市\",\"address\":\"jj北京市朝阳区\",\"remark\":\"jj我的家\"}"))
errcode, id = tiros.poifavorites.poifav_getid("45679989")
print("modify45679989=",tiros.poifavorites.poifav_modify("{\"id\":\""..id.."\",\"poigid\":\"45679989\",\"name\":\"kk华贸4667\",\"categoryId\":\"15\",\"lon\":\"127756\",\"lat\":\"433789\",\"tel\":\"13345295433\",\"city\":\"uu北京市\",\"address\":\"jj北京市朝阳区\",\"remark\":\"jj我的家\"}"))
print("poifav_getid(\"45679989\")=",tiros.poifavorites.poifav_getid("45679989"))
print("poifav_isexist(\""..id.."\")=",tiros.poifavorites.poifav_isexist(id))
print("poifav_getpoi(\""..id.."\")=",tiros.poifavorites.poifav_getpoi(id))
print("poifav_del(\"45679989\")=",tiros.poifavorites.poifav_del(id))
print("poifav_getid(\"45679989\")=",tiros.poifavorites.poifav_getid("45679989"))
print("poifav_isexist(\""..id.."\")=",tiros.poifavorites.poifav_isexist(id))
id = "105E0352-DD47-D375-7C32-B883D5B77A9C"
print("poifav_getpoi(\""..id.."\")=",tiros.poifavorites.poifav_getpoi(id))
print("poifav_getcount()=", tiros.poifavorites.poifav_getcount())

--poifav_delall()


print("Test End")
--]]
------------------------------------------------------------------------------------------------------------
