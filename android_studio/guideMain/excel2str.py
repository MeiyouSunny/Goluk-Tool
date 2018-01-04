#!/usr/bin/env python
# -*- coding:utf-8 -*-

#Android���ʻ��� ��excel�е�����ת����xml��

from xml.dom import minidom
from xlrd import open_workbook
import codecs
import os
import sys

#######################################################
def mkdir(path):
    # ����ģ��
    import os

    # ȥ����λ�ո�
    path=path.strip()
    # ȥ��β�� \ ����
    path=path.rstrip("\\")

    # �ж�·���Ƿ����
    # ����     True
    # ������   False
    isExists=os.path.exists(path)

    # �жϽ��
    if not isExists:
        # ����������򴴽�Ŀ¼
        # ����Ŀ¼��������
        os.makedirs(path)
        print (path +' created successfully')
        return True
    else:
        # ���Ŀ¼�����򲻴���������ʾĿ¼�Ѵ���
        print (path +' dir existed')
        return False
#######################################################

#reload(sys)
#sys.setdefaultencoding('utf-8')

#��excel
workbook = open_workbook('goluk_strings.xlsx')
sheet = workbook.sheet_by_index(0)

#����ַ���
for col_index in range(sheet.ncols):
	if col_index > 1:
		#�½�xml�ĵ�
		xml_doc = minidom.Document()
		#��Ӹ�Ԫ��
		en_resources = xml_doc.createElement('resources')
		language = sheet.cell(0, col_index).value
		for row_index in range(sheet.nrows):
			if row_index != 0:
				key = sheet.cell(row_index, 0).value
				result_content = sheet.cell(row_index, col_index).value
				if (key != '' and result_content != ''):
					#�½�һ���ı�Ԫ��
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
