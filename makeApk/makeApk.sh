echo "打Apk包"
echo "传递一个参数$1：APK包名称"
cd ../

#echo "拷贝库中的最新资源"
./makeApk/copyResources.sh

echo "删除$1/bin目录"
rm -rf $1/bin/

echo "进入$1目录"
cd $1

echo "调用ant打包"
export ANT_OPTS="-javaagent:oneapm/class.rewriter.jar"
ant release

echo "拷贝APK文件"
cp bin/*-release.apk  ../AndroidCDC/$1

echo "回到版本库目录，删除修改和未纳入版本库的文件"

cd ../

#清除工程未纳入版本库和未更新的文件
#git clean -df

