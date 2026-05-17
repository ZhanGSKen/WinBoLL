#!/system/bin/sh
## 流程：获取远程最新Tag → 获取Tag对应远程Commit → 从远程分支指定提交点合并模块目录

# 获取模块远程完整最新TAG 无多余空格
get_module_latest_tag() {
    local module_name="$1"
    git fetch origin --tags 2>/dev/null
    git ls-remote --tags origin "${module_name}-*" 2>/dev/null \
    | grep -v '\^{}' \
    | sort -V \
    | tail -1 \
    | awk '{print $2}' \
    | sed 's/refs\/tags\///'
}

# 通过远程标签获取对应提交哈希
get_remote_commit_by_tag() {
    local tag_name="$1"
    git rev-parse --verify "${tag_name}^{commit}" 2>/dev/null
}

# 进入工作目录
TARGET_DIR="/sdcard/AppProjects/Projects_Keeper_Tag"
echo "切换工作目录：$TARGET_DIR"
if ! cd "$TARGET_DIR"; then
    echo "目录切换失败！"
    exit 1
fi

# 同步远程
echo "=============================================="
echo "同步远程分支与全部版本标签"
echo "=============================================="
git fetch origin --prune
git fetch origin --tags
echo "同步完成"
echo ""

# 锁定目标分支
CUR_BRANCH=$(git symbolic-ref --short HEAD 2>/dev/null)
TARGET_BRANCH="projects_keeper_tag"
if [ "$CUR_BRANCH" != "$TARGET_BRANCH" ]; then
    echo "错误：当前不在 $TARGET_BRANCH 分支，请先切换！"
    exit 1
fi

# 目录校验
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
    if [[ "$line" != "." && "$line" != ".." ]]; then
        REAL_ITEMS+=("$line")
    fi
done < <(ls -a)

check_diff() {
    local missing=()
    local extra=()
    for item in "${MERGE_OBJECTS_LIST[@]}"; do
        local found=0
        for r in "${REAL_ITEMS[@]}"; do
            if [[ "$item" == "$r" ]]; then
                found=1
                break
            fi
        done
        if (( found == 0 )); then
            missing+=("$item")
        fi
    done
    for r in "${REAL_ITEMS[@]}"; do
        local found=0
        for item in "${MERGE_OBJECTS_LIST[@]}"; do
            if [[ "$item" == "$r" ]]; then
                found=1
                break
            fi
        done
        if (( found == 0 )); then
            extra+=("$r")
        fi
    done
    if [[ ${#missing[@]} -gt 0 || ${#extra[@]} -gt 0 ]]; then
        echo "目录结构不匹配，终止执行"
        exit 1
    fi
}
check_diff

echo -e "#@@@ 开始按远程最新标签合并模块 @@@#"

# 应用型模块
MERGE_APP_PROJECT_LIST=(DemoAPP)
echo -e "---------- 应用型模块合并 ----------"
for item in "${MERGE_APP_PROJECT_LIST[@]}"; do
    item_lower=$(echo "$item" | tr 'A-Z' 'a-z')
    TAG=$(get_module_latest_tag "${item_lower}")
    if [[ -z "$TAG" ]]; then
        echo "跳过 ${item_lower}：无远程版本标签"
        continue
    fi
    COMMIT=$(get_remote_commit_by_tag "$TAG")
    if [[ -z "$COMMIT" ]]; then
        echo "跳过 ${item_lower}：标签 $TAG 无有效提交点"
        continue
    fi
    echo "模块：${item_lower} | 标签：$TAG | 提交哈希：$COMMIT"
    # 从远程分支该提交点拉取目录
    git checkout origin/${item_lower} ${COMMIT} -- ${item_lower}
    git add ${item_lower}
    git commit -m "合并模块${item} 来源远程标签:${TAG} 提交点:${COMMIT}"
done

# 类库模块
MERGE_LIB_PROJECT_LIST=(WinBoLL APPBase AES)
echo -e "---------- 类库模块合并 ----------"
for item in "${MERGE_LIB_PROJECT_LIST[@]}"; do
    item_lower=$(echo "$item" | tr 'A-Z' 'a-z')
    TAG=$(get_module_latest_tag "${item_lower}")
    if [[ -z "$TAG" ]]; then
        echo "跳过 ${item_lower}：无远程版本标签"
        continue
    fi
    COMMIT=$(get_remote_commit_by_tag "$TAG")
    if [[ -z "$COMMIT" ]]; then
        echo "跳过 ${item_lower}：标签 $TAG 无有效提交点"
        continue
    fi
    echo "模块：${item_lower} | 标签：$TAG | 提交哈希：$COMMIT"
    git checkout origin/${item_lower} ${COMMIT} -- ${item_lower} lib${item_lower}
    git add ${item_lower} lib${item_lower}
    git commit -m "合并模块${item} 来源远程标签:${TAG} 提交点:${COMMIT}"
done

echo "所有模块合并完成"
echo "准备推送远程"
git push
