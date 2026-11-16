#!/usr/bin/bash
## 提交新的 Library 编译配置标志信息，并推送到Git仓库。

# 检查是否指定了将要发布的类库名称
# 使用 `-z` 命令检查变量是否为空
if [ -z "$1" ]; then
    echo "Library name error: $0"
    exit 2
fi

## 开始执行脚本
echo -e "Current dir : \n"`pwd`
# 检查当前目录是否是项目根目录
if [[ -e $1/build.properties ]]; then
    echo "The $1/build.properties file exists."
    echo -e "Work dir correctly."
else
    echo "The $1/build.properties file does not exist."
    echo "尝试进入根目录"
    # 进入项目根目录
    cd ..
fi
## 本脚本需要在项目根目录下执行
echo -e "Current dir : \n"`pwd`
# 检查当前目录是否是项目根目录
if [[ -e $1/build.properties ]]; then
    echo "The $1/build.properties file exists."
    echo -e "Work dir correctly."
else
    echo "The $1/build.properties file does not exist."
    echo -e "Work dir error."
    exit 1
fi

# 就读取脚本 .winboll/winboll_app_build.gradle 生成的 publishVersion。
# 如果文件中有 publishVersion 这一项，
# 使用grep找到包含"publishVersion="的那一行，然后用awk提取其后的值
PUBLISH_VERSION=$(grep -o "publishVersion=.*" $1/build.properties | awk -F '=' '{print $2}')
echo "< $1/build.properties publishVersion : ${PUBLISH_VERSION} >"
## 设新的 WinBoLL 标签
# 脚本调试时使用
#tag="v7.6.4-test1"
# 正式设置标签时使用
#tag="v"${PUBLISH_VERSION}

git add .
git commit -m "<$1>Library Release ${PUBLISH_VERSION}"
git push origin && git push origin --tags
