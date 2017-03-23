#!/bin/bash
#当前路径
mybuildpath=$(cd "$(dirname "$0")"; pwd)
#父目录
buildparentdir=${mybuildpath%/*}
#所在文件夹名称
mybuilddirname=${mybuildpath##*/}

apkName="GolukMobile"

echo "开始构建android脚本"

#写日志文件
#logfile=build-"`date +%Y%m%d%H%M`".log

	#打包存放目录
	echo "创建apk包存放目录"
	mkdir AndroidCDC

	cd makeApk/

	#$1 平台缩写 qt、android、ios
	#$2 编译后lib库的目标目录
	#$3 debug、release、appstore
	#$4 使用服务器标志dev、test、nvd

	echo "开始构建单个测试包"

	if [ -n "$4" ];
	then
		./batchbuild.sh $4 "" $apkName
	else
		./batchbuild.sh "" "" $apkName
	fi

	cd $mybuildpath
	echo "压缩"
	tar -zcvf $buildpath/$apkName.tar.gz AndroidCDC

	echo "拷贝到FTP的发布目录"
	name=$apkName-"`date +%Y%m%d%H%M`".tar.gz
	cp $buildpath/$apkName.tar.gz /home/ftp-dir/compileDir/$name 2>>$logpath


	echo "退出到根目录并删除临时文件"

	rm -rf AndroidCDC

