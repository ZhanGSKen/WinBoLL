#!/usr/bin/bash
## 跳转到 libjc 项目目录查找源码文件夹 src 和编译环境目录 jcc 目录，如果两个目录其中一个不存在就退出
cd ..

## 记录当前工作目录
workpath=$(pwd)

## 检查源码目录与 jcc 工作目录
srcdir="src"
if [ -d "${srcdir}" ]; then
    echo "Src dir ${srcdir} found."
else
    echo "Src dir ${srcdir} not exist."
    exit 1
fi
jccdir="jcc"
if [ -d "${jccdir}" ]; then
    echo "Jcc dir ${jccdir} found."
else
    echo "Jcc dir ${jccdir} not exist."
    exit 1
fi

jarversion="1.0.1"
jarname="libjc-"${jarversion}".jar"
classespath="${workpath}/${jccdir}/classes"
jarspath="${workpath}/${jccdir}/output_jars"
srcpath="${workpath}/${srcdir}/main/java"
libspath="${workpath}/${jccdir}/libs"

echo "Gen class to "${classespath}" :"
cd ${srcpath}
javac -cp ".:${libspath}/android-29.jar"  -d ${classespath} $(find . -name "*.java")
echo "Build "${jarname}" :"
cd ${classespath};
jar -cvfm ${jarspath}/${jarname} ${workpath}/${jccdir}/MANIFEST.MF cc
echo "Jar Build OK."

cd ..
bash test_jar.sh ${jarversion}
