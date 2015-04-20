require "lua/friend/sys_friendmanger"
require "lua/friend/sys_addbook"
require "lua/friend/sys_downloadimg"
--[[
1)注册回调函数
tiros.friendmanger.friendlist_notify(stype, notify, user)

ps:然后调用以下请求，stype必须与注册的回调一直才能收到下行数据
2)删除好友
tiros.friendmanger.deleteFriend(stype,htype,flag,fuid)
回调数据格式：{"uid":"50","fuid":"90","deltime":"2013011417:31:46"}

2)添加好友
tiros.friendmanger.addFriend(stype,htype,flag,fuid)
回调数据格式：{"uid":"50","addtime":"2013011417:32:14","phone":"","nickname":"dog90test","aid":"111010156","fuid":"90","url":""}

3)查询手机号
tiros.friendmanger.queryPhone(stype,htype,queryPhone)
回调数据格式：{“result”，“1”，“uid”，“50”，“phone”，“15187654321”,“pname”，“英雄”,“headPath”,“http://”},“msg”:“”}

4)邀请好友短信
tiros.friendmanger.friendlist_sms(stype,htype,selfuid)
回调数据格式：{"result”:“短信内容”}

5)好友详情
tiros.friendmanger.friendlist_friendinfo(stype,htype,fuid)
回调数据格式：{"uid":"50","phone":"12222222222","nickname":"王鹏","fuid":"50","url":"http://192.168.1.234/files/headpic/avatar-01.gif"}

6)好友列表更新
tiros.friendmanger.friendlist_update(stype,htype)
回调数据格式：{"newfriends":[{"aid":"","fuid":"281821","nickname":"手机用户535","phone":"13318725755","url":"http://192.168.1.234/files/headpic/gs.gif"},
						{"aid":"","fuid":"87","nickname":"suntest87","phone":"","url":"http://192.168.1.234/files/headpic/avatar-01.gif"}],
			"delfriends":[{"fuid":88}]}


7)取消注册回调
tiros.friendmanger.friendlist_notifycancel(stype)
8)获取本地数据库中好友
tiros.friendmanger.friendlist_get()
返回值格式：{"data":[{"HEADURL":"HEADURL","AID":"AID","HEADPATH":"HEADPATH","NICKNAME":"NICKNAME","PHONE":"PHONE","UID":"UID","ADDBOOKNAME":"add"}]}

9)保存个人信息
tiros.friendmanger.SaveUserinfo(uid,aid,nickname,phone,headpath,headurl,addbookname,sex)
10)获取个人信息,返回值janson，例如：{"HEADURL":"HEADURL","AID":"AID","HEADPATH":"HEADPATH","NICKNAME":"NICKNAME","PHONE":"PHONE","UID":"UID","ADDBOOKNAME":"add"}
tiros.friendmanger.GetUserinfo(uid)
11)更新个人信息
tiros.friendmanger.UpdateUserinfo(uid,aid,nickname,phone,headpath,headurl,addbookname,sex)
12)头像下载到指定路径
tiros.downloadimg(htype,url,path,notify,user)
13）好友头像下载
tiros.friendmanger.friendheadimgupdate(fuid, url)
--]]




--[[
test addressbook
require"lua/json"
require"lua/moduledata"
--tiros.friendmanger.friendlist_update()

DeclareGlobal("luacb1", function (ptype, status, param1, param2)
print("OUT luacb1=====================================",ptype, status, param1, param2);
end)

local addbookData = {};
local jsonData;
local t1 = {}
t1.name = "zhanghua"
t1.phone = "18613824544"
table.insert(addbookData, t1)

local t2 = {}
t2.name = "xiaozhu"
t2.phone = "13643232321"
table.insert(addbookData, t2)
jsonData = tiros.json.encode(addbookData);

local t3 = {}
t3.name = "weijun1"
t3.phone = "18743200321"
table.insert(addbookData, t3)
local t4 = {}
t4.name = "wangcheng1"
t4.phone = "15843245321"
table.insert(addbookData, t4)
jsonData = tiros.json.encode(addbookData);
local t5 = {}
t5.name = "weijunjun"
t5.phone = "18732100123"
table.insert(addbookData, t5)
local t6 = {}
t6.name = "wangcheng"
t6.phone = "15812343333"
table.insert(addbookData, t6)
jsonData = tiros.json.encode(addbookData);

tiros.moduledata.moduledata_set("web", "addressbookData", jsonData);
tiros.moduledata.moduledata_set("framework", "uid", "4142043");
tiros.addbook.addressbookuploadforlua("testaddressbook", "login", luacb1, 2)

--tiros.moduledata.moduledata_set("web", "taxiaddressbookData", jsonData);
--tiros.moduledata.moduledata_set("framework", "uid", "1028250");
--tiros.addbook.taxiaddressbookuploadforlua("testTAXIaddressbook", luacb1)
--]]

