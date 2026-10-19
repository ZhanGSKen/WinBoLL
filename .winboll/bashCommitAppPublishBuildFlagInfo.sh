#!/usr/bin/bash
## 提交新的 APK 编译配置标志信息，并推送到Git仓库。

# 使用 `-z` 命令检查变量是否为空
if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ] || [ -z "$4" ]; then
    echo "$0 Script parameter error."
    echo "(Script Demo : [ bashCommitAppPublishBuildFlagInfo.sh <RootProjectDir> <VersionName> <BuildType Name> <RootProject Name> ])"
    exit 2
fi

# 进入项目根目录
cd ${1}
echo -e "Work dir : \n"`pwd`

git add .
git commit -m "<$4>APK ${2} ${3} Publish."
git push origin && git push origin --tags
