#!/system/bin/sh
## 流程：获取远程最新Tag → 获取Tag对应Commit → 按Commit合并远程模块目录到本地

# ====================== 1. 获取模块远程最新版本TAG ======================
get_module_latest_tag() {
    local module_name="$1"
    git fetch origin --tags 2>/dev/null
    # 筛选 模块名- 开头标签，按版本排序取最新
    git ls-remote --tags origin "${module_name}-*" 2>/dev/null \
    | grep -v '\^{}' \
    | sort -V \
    | tail -1 \
    | awk '{print $2}' \
    | sed 's/refs\/tags\///'
}

# ====================== 2. 通过TAG获取对应提交Commit哈希 ======================
get_commit_by_tag() {
    local tag_name="$1"
    git rev-list -1 "$tag_name" 2>/dev/null
}

# ====================== 进入工作目录 ======================
TARGET_DIR="/sdcard/AppProjects/Projects_Keeper_Tag"
echo "切换工作目录：$TARGET_DIR"
if ! cd "$TARGET_DIR"; then
    echo "目录切换失败！"
    exit 1
fi

# ====================== 同步远程全部标签 ======================
echo "=============================================="
echo "同步远程所有分支与版本标签"
echo "=============================================="
git fetch origin --prune
git fetch origin --tags
echo "同步完成"
echo ""

# ====================== 锁定本地目标分支 ======================
CUR_BRANCH=$(git symbolic-ref --short HEAD 2>/dev/null)
TARGET_BRANCH="projects_keeper_tag"
if [ "$CUR_BRANCH" != "$TARGET_BRANCH" ]; then
    echo "错误：当前不在 $TARGET_BRANCH 分支，请先切换！"
    exit 1
fi

# ====================== 目录结构校验（保留原有） ======================
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

# ====================== 开始批量合并模块 ======================
echo -e "#@@@ 开始按【最新Tag+对应Commit】合并模块 @@@#"

## 合并应用型模块
MERGE_APP_PROJECT_LIST=(DemoAPP)
echo -e "---------- 应用型模块合并 ----------"
for item in "${MERGE_APP_PROJECT_LIST[@]}"; do
    item_lower=$(echo "$item" | tr 'A-Z' 'a-z')
    # 1.获取远程最新TAG
    LATEST_TAG=$(get_module_latest_tag "$item_lower")
    if [ -z "$LATEST_TAG" ]; then
        echo "跳过 $item_lower ：未查询到任何版本标签"
        continue
    fi
    # 2.通过TAG获取对应提交点Commit
    TARGET_COMMIT=$(get_commit_by_tag "$LATEST_TAG")
    if [ -z "$TARGET_COMMIT" ]; then
        echo "跳过 $item_lower ：标签 $LATEST_TAG 未查询到对应提交点"
        continue
    fi

    echo "模块：$item_lower"
    echo "最新标签：$LATEST_TAG"
    echo "对应提交点：$TARGET_COMMIT"
    echo "----------------------------------------"

    # 3.使用Commit从远程模块分支拉取目录合并到本地
    git checkout origin/${item_lower} ${TARGET_COMMIT}:${item_lower}
    git add ${item_lower}
    git commit -m "合并模块${item} 来源最新标签:${LATEST_TAG} 提交点:${TARGET_COMMIT}"
done

## 合并类库模块
MERGE_LIB_PROJECT_LIST=(WinBoLL APPBase AES)
echo -e "---------- 类库模块合并 ----------"
for item in "${MERGE_LIB_PROJECT_LIST[@]}"; do
    item_lower=$(echo "$item" | tr 'A-Z' 'a-z')
    # 1.获取远程最新TAG
    LATEST_TAG=$(get_module_latest_tag "$item_lower")
    if [ -z "$LATEST_TAG" ]; then
        echo "跳过 $item_lower ：未查询到任何版本标签"
        continue
    fi
    # 2.通过TAG获取对应提交点Commit
    TARGET_COMMIT=$(get_commit_by_tag "$LATEST_TAG")
    if [ -z "$TARGET_COMMIT" ]; then
        echo "跳过 $item_lower ：标签 $LATEST_TAG 未查询到对应提交点"
        continue
    fi

    echo "模块：$item_lower"
    echo "最新标签：$LATEST_TAG"
    echo "对应提交点：$TARGET_COMMIT"
    echo "----------------------------------------"

    # 3.拉取主目录 + lib目录
    git checkout origin/${item_lower} ${TARGET_COMMIT}:${item_lower} ${TARGET_COMMIT}:lib${item_lower}
    git add ${item_lower} lib${item_lower}
    git commit -m "合并模块${item} 来源最新标签:${LATEST_TAG} 提交点:${TARGET_COMMIT}"
done

echo "所有模块合并流程执行完毕"
echo "准备推送至远程分支"
git push
