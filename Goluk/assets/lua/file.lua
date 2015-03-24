--lua脚本file对外接口
--备注：函数前缀为该函数返回类型

--1 int fileCopy(Srcf,Dstf,bCover)其中bCover为0为不覆盖，1为覆盖
--3 char* Readfile(fname) 
--4 bool Writefile(fname,data,bturncate)其中bturncate：false为直接追加写，true为清空内容重写
--5 bool Removefile(fname)

require"lua/systemapi/sys_file"

--测试代码
--[[
--tiros.file.fileCopy("readme", "copy.txt", 0)
--local s = tiros.file.Readfile("aaa/bbb/ccc/u.txt")
--tiros.file.Writefile("aaa/bbb/ccc/u.txt",s,)
--tiros.file.Removefile("aaa/bbb/ccc/u.txt")
--]]
