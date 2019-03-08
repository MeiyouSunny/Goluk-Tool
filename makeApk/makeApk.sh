echo "打Apk包"
echo "传递一个参数$1：APK包名称"
cd ../android_studio


echo "清除构建目录"
#rm -rf $1/bin/
gradle clean


echo "调用gradle打包"
#export ANT_OPTS="-javaagent:/home/builder/class.rewriter.jar"

gradle assembleInternal

echo "拷贝APK文件"
cp golukMobile/build/outputs/apk/internal/release/*-release.apk  ../AndroidCDC/$1
cp golukMobile/build/outputs/apk/internal/debug/*-debug.apk  ../AndroidCDC/$1
cp golukMobile/build/outputs/apk/international/release/*-release.apk  ../AndroidCDC/$1
cp golukMobile/build/outputs/apk/international/debug/*-debug.apk  ../AndroidCDC/$1

echo "回到版本库目录，删除修改和未纳入版本库的文件"

cd ../

#清除工程未纳入版本库和未更新的文件
#git clean -df

