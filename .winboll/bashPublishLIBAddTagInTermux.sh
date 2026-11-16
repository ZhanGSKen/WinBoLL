#!/usr/bin/bash

# aapt2本地覆盖参数
AAPT2_OVERRIDE_ARG="-Pandroid.aapt2FromMavenOverride=/data/data/com.termux/files/usr/bin/aapt2"
# Gradle禁用守护进程参数
GRADLE_NO_DAEMON="--no-daemon"

# 检查是否指定了将要发布的类库名称
# 使用 `-z` 命令检查变量是否为空
if [ -z "$1" ]; then
    echo "No Library name specified : $0"
    exit 2
fi

## 正式发布使用
git pull && bash gradlew ${AAPT2_OVERRIDE_ARG} ${GRADLE_NO_DAEMON} :$1:publishReleasePublicationToWinBoLLReleaseRepository && bash .winboll/bashCommitLibReleaseBuildFlagInfo.sh $1

## 调试使用
#bash gradlew ${AAPT2_OVERRIDE_ARG} ${GRADLE_NO_DAEMON} :$1:publishSnapshotWinBoLLPublicationToWinBoLLSnapshotRepository && bash .winboll/bashCommitLibReleaseBuildFlagInfo.sh $1

