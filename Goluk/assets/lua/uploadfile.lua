require"lua/uploadfile/sys_uploadfile"
--htype 句柄标识
--ftype 2文件1图片
--path 文件路径
--func 回调函数指针
--user 回调对象
--bool uploadfile(htype, ftype, path,func, user)

--[[
DeclareGlobal("luacb1", function (htype, status, param1, param2)
print("OUT luacb1=====================================",htype, status, param1, param2);
end)

tiros.uploadfile.uploadfile("testuploadfile", 2, "fs0:/a.pdf",luacb1)
--]]
