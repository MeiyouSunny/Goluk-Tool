
#参数  
#	$1 构建包类型 test  nvd  dev  如果这个参数不存在那么认为构建test包
#	$2 构建tag包version路径 如：~/version_453_tiros/ （如果存在，构建则为tag包）
#	$3 构建包名称,不带后缀，注意，需要使用APK工程的文件夹名称

echo "生成so文件"
./makeSo.sh
echo "检查so是否编译成功"

cd ../

#判断第一个参数 test等是否存在
echo "拷贝gitversion和version"

project_source_path=android_studio/golukMobile/src/main
rm -rf ${project_source_path}/assets/serverflag

if [ -n "$1" ];
then
	echo $1 >> ${project_source_path}/assets/serverflag
	if [ $1 = "dev" ];
	then
		echo "build dev"
		cp -f   version_test/version  ${project_source_path}/assets/version
	elif [ $1 = "test" ];
	then
		echo "build test"
		cp -f   version_test/version  ${project_source_path}/assets/version
	elif [ $1 = "nvd" ];
	then
		echo "build nvd"
		cp -f 	version_nvd/version ${project_source_path}/assets/version
	elif [ $1 = "invd" ];
	then
		echo "build invd"
		cp -f 	version_invd/version ${project_source_path}/assets/version
	elif [ $1 = "txy" ];
	then
		echo "build txy"
		cp -f 	version_nvd/version ${project_source_path}/assets/version
	else
		echo "$1 error"
	fi
else
	echo "build test"
	echo "test" >> ${project_source_path}/assets/serverflag
	cp -f   version_test/version  ${project_source_path}/assets/version
fi

	#echo "拷贝so文件到工程目录下"
	#mkdir -p ${project_source_path}/jniLibs/armeabi/
	#cp -f tiros-module4x/libs/armeabi/* ${project_source_path}/jniLibs/armeabi/


echo "构建apk包"
cd makeApk/

#判断 $2参数是否存在，存在说明构建tag包
./makeApk.sh $3


