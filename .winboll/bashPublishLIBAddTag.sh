#!/usr/bin/bash

# 检查是否指定了将要发布的类库名称
# 使用 `-z` 命令检查变量是否为空
if [ -z "$1" ]; then
    echo "No Library name specified : $0"
    exit 2
fi

## 正式发布使用
git pull && bash gradlew :$1:publishReleasePublicationToWinBoLLReleaseRepository && bash .winboll/bashCommitLibReleaseBuildFlagInfo.sh $1

## 调试使用
#bash gradlew :$1:publishSnapshotWinBoLLPublicationToWinBoLLSnapshotRepository && bash .winboll/bashCommitLibReleaseBuildFlagInfo.sh $1
