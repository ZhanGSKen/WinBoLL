#!/system/bin/sh
## 逻辑：按时间取最新标签 → 拉取该标签目录合并

# 按创建时间取模块最新标签（最新打的标签排最后）
get_latest_module_tag(){
    local mod=$1
    git for-each-ref --sort=-creatordate --format='%(refname)' refs/tags/${mod}-* \
    | grep -v '\^{}' \
    | head -1 \
    | sed 's/refs\/tags\///'
}

# 通过标签获取commit
get_commit_from_tag(){
    local tag=$1
    git rev-parse --short "${tag}^{commit}"
}

# 工作目录
TARGET_DIR="/sdcard/AppProjects/Projects_Keeper_Tag"
echo "进入目录：${TARGET_DIR}"
cd "${TARGET_DIR}" || exit 1

# 同步远程
echo "========================================"
echo "同步远程分支与全部版本标签"
echo "========================================"
git fetch origin --prune
git fetch origin --tags
echo "同步完成"
echo ""

# 校验目标分支
NOW_BRANCH=$(git symbolic-ref --short HEAD)
TARGET_BRANCH="projects_keeper_tag"
if [ "${NOW_BRANCH}" != "${TARGET_BRANCH}" ];then
    echo "错误：请先切换到 ${TARGET_BRANCH} 分支"
    exit 1
fi

# 目录结构校验
MERGE_OBJECTS_LIST=(
.git
.gitignore
.gitmodules
.winboll
GenKeyStore
LICENSE
LICENSE-Private-Demo
LICENSE-Private-Demo_docs
README.md
aes
appbase
autonfc
build.gradle
contacts
debugtemp
gallery
gpsrelaysentinel
gradle
gradle.properties-android-demo
gradle.properties-androidx-demo
gradlew
libaes
libappbase
libdebugtemp
libgpsrelaysentinel
libwinboll
local.properties-demo
mymessagemanager
positions
powerbell
settings.gradle-demo
winboll
winboll.properties-demo
)

REAL_ITEMS=()
while IFS= read -r line; do
    [[ $line != "." && $line != ".." ]] && REAL_ITEMS+=("$line")
done < <(ls -a)

check_diff(){
    local miss=() extra=()
    for i in "${MERGE_OBJECTS_LIST[@]}";do
        [[ ! " ${REAL_ITEMS[@]} " =~ " ${i} " ]] && miss+=("$i")
    done
    for i in "${REAL_ITEMS[@]}";do
        [[ ! " ${MERGE_OBJECTS_LIST[@]} " =~ " ${i} " ]] && extra+=("$i")
    done
    if [[ ${#miss[@]} -gt 0 || ${#extra[@]} -gt 0 ]];then
        echo "本地目录结构不一致，终止运行"
        exit 1
    fi
}
check_diff

echo -e "#@@@ 按时间获取最新标签合并模块源码 @@@#"

# 应用型模块
MERGE_APP_PROJECT_LIST=(DemoAPP)
echo -e "---------- 应用型模块 ----------"
for name in "${MERGE_APP_PROJECT_LIST[@]}";do
    low_name=$(echo "$name" | tr 'A-Z' 'a-z')
    tag=$(get_latest_module_tag "${low_name}")
    if [[ -z "${tag}" ]];then
        echo "跳过 ${low_name}：无远程标签"
        continue
    fi
    commit=$(get_commit_from_tag "${tag}")
    if [[ -z "${commit}" ]];then
        echo "跳过 ${low_name}：标签无有效提交点"
        continue
    fi
    echo "模块:${low_name} 最新时间标签:${tag} 提交ID:${commit}"
    # 强制拉取覆盖
    git checkout -f "${tag}" -- "${low_name}"
    git add "${low_name}"
    git commit -m "合并模块${name} 同步最新时间标签${tag}"
done

# 类库模块
MERGE_LIB_PROJECT_LIST=(WinBoLL APPBase AES)
echo -e "---------- 类库模块 ----------"
for name in "${MERGE_LIB_PROJECT_LIST[@]}";do
    low_name=$(echo "$name" | tr 'A-Z' 'a-z')
    tag=$(get_latest_module_tag "${low_name}")
    if [[ -z "${tag}" ]];then
        echo "跳过 ${low_name}：无远程标签"
        continue
    fi
    commit=$(get_commit_from_tag "${tag}")
    if [[ -z "${commit}" ]];then
        echo "跳过 ${low_name}：标签无有效提交点"
        continue
    fi
    echo "模块:${low_name} 最新时间标签:${tag} 提交ID:${commit}"
    git checkout -f "${tag}" -- "${low_name}" "lib${low_name}"
    git add "${low_name}" "lib${low_name}"
    git commit -m "合并模块${name} 同步最新时间标签${tag}"
done

echo "全部模块合并执行完毕"
echo "执行推送：git push"
git push
