--udp管理 接口封装

require"lua/udpmanager/sys_udpmanager"

--(1) UM_Send( address, port, data )
--对外申明的Udp发送数据的接口
--输入：address，域名或者ip地址(string型)
--输入：port，发送端口(number型)
--输入：data，要发送的数据(string型)
--输出：无

---------------------------测试代码--------------------------------------------------------------

--[[
print(checkip("www.hubo.com"))
print(checkip("192.168.1"))
print(checkip("192.168.4.1"))
print(checkip("192.168"))
print(checkip("192.168.1.6.43"))
print(checkip("1934342.168.1"))
print(checkip("19234.138.1.234"))
--]]

--[[
tiros.udpmanager.UM_Send("www.hubo.com", 6002, "{aa--asfasfafafasfafasfasfasdf}")
--tiros.udpmanager.UM_Send("asdfadfadfsasdfm", 6002, "{bb---456456456456}")
tiros.udpmanager.UM_Send("192.168.1.192", 6002, "{cc--5555555555555555555}")
--tiros.udpmanager.UM_Send("127.0.0.1", 6002, "{dd--5555555555555555555}")
tiros.udpmanager.UM_Send("www.hubo.net", 6003, "{ee--asfas546456456464fafafasfafasfasfasdf}")
tiros.udpmanager.UM_Send("www.sina.com.cn", 6003, "{ff--sdsdfa}")
--tiros.udpmanager.UM_Send("www.sohu.com", 6003, "{gg--sdsdfa}")
--tiros.udpmanager.UM_Send("www.navidog.cn", 6003, "{hh--sdsdfa}")
tiros.udpmanager.UM_Send("www.163.com", 6003, "{ii--sdsdfa}")
--tiros.udpmanager.UM_Send("www.qq.com", 6003, "{jj--sdsdfa}")
--]]
