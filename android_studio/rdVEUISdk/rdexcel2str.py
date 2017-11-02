#!/usr/bin/env python
# -*- coding:utf-8 -*-

#Android国际化： 将excel中的内容转化到xml中

from xml.dom import minidom
from xlrd import open_workbook
import codecs
import os
import sys

#######################################################
def mkdir(path):
    # 引入模块
    import os

    # 去除首位空格
    path=path.strip()
    # 去除尾部 \ 符号
    path=path.rstrip("\\")

    # 判断路径是否存在
    # 存在     True
    # 不存在   False
    isExists=os.path.exists(path)

    # 判断结果
    if not isExists:
        # 如果不存在则创建目录
        # 创建目录操作函数
        os.makedirs(path)
        print (path +' created successfully')
        return True
    else:
        # 如果目录存在则不创建，并提示目录已存在
        print (path +' dir existed')
        return False
#######################################################

reload(sys)
sys.setdefaultencoding('utf-8')

#打开excel
workbook = open_workbook('rd_strings.xlsx')
sheet = workbook.sheet_by_index(0)

#添加字符串
for col_index in range(sheet.ncols):
	if col_index > 1:
		#新建xml文档
		xml_doc = minidom.Document()
		#添加根元素
		en_resources = xml_doc.createElement('resources')
		language = sheet.cell(0, col_index).value
		for row_index in range(sheet.nrows):
			if row_index != 0:
				key = sheet.cell(row_index, 0).value
				result_content = sheet.cell(row_index, col_index).value
				if (key != '' and result_content != ''):
					#新建一个文本元素
					print ("key = %s, content = %s" % (key,result_content))
					text_element = xml_doc.createElement('string')
					text_element.setAttribute('name', key)
					text_element.appendChild(xml_doc.createTextNode(str(result_content)))
					en_resources.appendChild(text_element)
		xml_doc.appendChild(en_resources)
		mkdir("src/main/res/" + language)
		file = codecs.open('src/main/res/'+language+'/strings.xml','w',encoding='utf-8')
		file.write(xml_doc.toprettyxml(indent='    '))
		file.close()
