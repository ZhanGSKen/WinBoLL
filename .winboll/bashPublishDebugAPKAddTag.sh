#!/usr/bin/bash

# 检查是否指定了将要发布的调试版应用名称
# 使用 `-z` 命令检查变量是否为空
if [ -z "$1" ]; then
    echo "No APP name specified : $0"
    exit 2
fi

## 定义相关函数
## 检查 Git 源码是否完全提交了，完全提交就返回0
function checkGitSources {
    #local input="$1"
    #echo "The string is: $input"
    git config --global --add safe.directory `pwd`
    if [[ -n $(git diff --stat)  ]]
    then
        local result="Source is no commit completely."
        echo $result
        # 脚本调试时使用
        #return 0
        # 正式检查源码时使用
        return 1
    fi
    local result="Git Source Check OK."
    echo $result
    return 0
}

function askAddWorkflowsTag {
    read answer
    if [[ $answer =~ ^[Yy]$ ]]; then
        #echo "You chose yes."
        return 1
    else
        #echo "You chose no."
        return 0
    fi
}

function addWinBoLLTag {
    # 就读取脚本 .winboll/winboll_app_build.gradle 生成的 publishVersion。
    # 如果文件中有 publishVersion 这一项，
    # 使用grep找到包含"publishVersion="的那一行，然后用awk提取其后的值
    PUBLISH_VERSION=$(grep -o "publishVersion=.*" $1/build.properties | awk -F '=' '{print $2}')
    echo "< $1/build.properties publishVersion : ${PUBLISH_VERSION} >"
    ## 设新的 WinBoLL 标签
    # 脚本调试时使用
    #tag="v7.6.4-test1"
    # 正式调试版设置标签时使用
    tag=$1"-v"${PUBLISH_VERSION}"-debug"
    echo "< WinBoLL Tag To: $tag >";
    # 检查是否已经添加了 WinBoLL Tag
    if [ "$(git tag -l ${tag})" == "${tag}" ]; then
        echo -e "< WinBoLL Tag ${tag} exist! >"
        return 1 # WinBoLL标签重复
    fi
    # 添加WinBoLL标签
    git tag -a ${tag} -F $1/app_update_description.txt
    return 0
}

function addWorkflowsTag {
    # 就读取脚本 .winboll/winboll_app_build.gradle 生成的 baseBetaVersion。
    # 如果文件中有 baseBetaVersion 这一项，
    # 使用grep找到包含"baseBetaVersion="的那一行，然后用awk提取其后的值
    BASE_BETA_VERSION=$(grep -o "baseBetaVersion=.*" $1/build.properties | awk -F '=' '{print $2}')
    echo "< $1/build.properties baseBetaVersion : ${BASE_BETA_VERSION} >"
    ## 设新的 workflows 标签
    # 脚本调试时使用
    #tag="v7.6.4-beta"
    # 正式设置标签时使用
    tag=$1"-"v"${BASE_BETA_VERSION}-beta-debug
    echo "< Workflows Tag To: $tag >";
    # 检查是否已经添加了工作流 Tag
    if [ "$(git tag -l ${tag})" == "${tag}" ]; then
        echo -e "< Github Workflows Tag ${tag} exist! >"
        return 1 # 工作流标签重复
    fi
    # 添加工作流标签
    git tag -a ${tag} -F $1/app_update_description.txt
    return 0
}

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

# 检查源码状态
result=$(checkGitSources)
if [[ $? -eq 0 ]]; then
    echo $result
    # 如果Git已经提交了所有代码就执行标签和应用发布操作

    # 预先询问是否添加工作流标签
    echo "Add Github Workflows Tag? (yes/no)"
    result=$(askAddWorkflowsTag)
    nAskAddWorkflowsTag=$?
    echo $result

    # 发布应用
    echo "Publishing WinBoLL Debug APK ..."
    # 脚本调试时使用
    #bash gradlew :$1:assembleBetaDebug
    # 正式发布调试版
    bash gradlew :$1:assembleStageDebug
    echo "Publishing WinBoLL Debug APK OK."
    
    # 添加 WinBoLL 标签
    result=$(addWinBoLLTag $1)
    echo $result
    if [[ $? -eq 0 ]]; then
        echo $result
        # WinBoLL 标签添加成功
    else
        echo -e "${0}: addWinBoLLTag $1\n${result}\nAdd WinBoLL tag cancel."
        exit 1 # addWinBoLLTag 异常
    fi
    
    # 添加 GitHub 工作流标签
    if [[ $nAskAddWorkflowsTag -eq 1 ]]; then
        # 如果用户选择添加工作流标签
        result=$(addWorkflowsTag $1)
        if [[ $? -eq 0 ]]; then
            echo $result
            # 工作流标签添加成功
        else
            echo -e "${0}: addWorkflowsTag $1\n${result}\nAdd workflows tag cancel."
            exit 1 # addWorkflowsTag 异常
        fi
    fi
    
    ## 清理更新描述文件内容
    echo "" > $1/app_update_description.txt
    
    # 设置新版本开发参数配置
    # 提交配置
    git add .
    git commit -m "<$1>Start New Stage Debug Version."
    echo "Push sources to git repositories ..."
    # 推送源码到所有仓库
    git push origin && git push origin --tags
else
    echo -e "${0}: checkGitSources\n${result}\nShell cancel."
    exit 1 # checkGitSources 异常
fi
