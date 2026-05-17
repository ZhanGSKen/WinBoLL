#!/system/bin/sh
## 合并远程指定版本标签下模块文件夹到本地 projects_keeper_tag 分支

# ====================== 获取模块对应远程版本标签 ======================
get_module_latest_tag() {
    local module_dir="$1"
    local remote_branch="origin/${module_dir}"

    # 强制拉取远程分支+全部远程标签到本地
    git fetch origin "$module_dir"
    git fetch origin --tags

    # 获取远程分支最新提交哈希
    local latest_commit
    latest_commit=$(git log -1 --pretty=format:%H "$remote_branch" 2>/dev/null)

    # 调试信息输出到错误流，不污染返回值
    echo "  调试：模块[$module_dir] 远程最新Commit = $latest_commit" >&2

    if [ -z "$latest_commit" ]; then
        echo "  调试：无有效提交" >&2
        echo ""
        return
    fi

    # 匹配 模块名-xxx 格式标签，绑定对应commit
    local tag_val
    tag_val=$(git ls-remote --tags origin "${module_dir}-*" 2>/dev/null \
    | grep -v '\^{}' \
    | awk -v cm="$latest_commit" '$1==cm{print $2}' \
    | sed 's/refs\/tags\///')

    # 纯净返回标签字符串
    echo "$tag_val"
}

# ====================== 进入工作目录 ======================
TARGET_DIR="/sdcard/AppProjects/Projects_Keeper_Tag"
echo "切换工作目录：$TARGET_DIR"
if ! cd "$TARGET_DIR"; then
    echo "目录切换失败！"
    exit 1
fi

# ====================== 同步远程代码与所有标签 ======================
echo "=============================================="
echo "同步远程代码及全部版本标签"
echo "=============================================="
git pull
git fetch origin --tags
echo "同步完成"
echo ""

# ====================== 强制锁定本地分支 ======================
CUR_BRANCH=$(git symbolic-ref --short HEAD 2>/dev/null)
TARGET_BRANCH="projects_keeper_tag"
if [ "$CUR_BRANCH" != "$TARGET_BRANCH" ]; then
    echo "错误：当前不在 $TARGET_BRANCH 分支，请先切换！"
    exit 1
fi

# ====================== 目录校验数组(保留原样) ======================
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

# ====================== 开始合并模块 ======================
echo -e "#@@@ 开始合并标签版本模块源码 @@@#"

## 合并应用型模块
MERGE_APP_PROJECT_LIST=(DemoAPP)
echo -e "#@@@ 应用型模块开始合并 @@#"
for item in "${MERGE_APP_PROJECT_LIST[@]}"; do
    item_lower=$(echo "$item" | tr 'A-Z' 'a-z')
    MOD_TAG=$(get_module_latest_tag "$item_lower")

    # 无标签直接跳过，不执行任何操作
    if [ -z "$MOD_TAG" ]; then
        echo "跳过 $item_lower 未匹配对应版本标签"
        continue
    fi

    echo "正在合并 $item_lower 标签版本：$MOD_TAG"
    # 从远程标签拉取文件夹覆盖到本地
    git checkout origin/tags/"$MOD_TAG" -- "$item_lower"
    # 仅添加当前模块文件，不全局add
    git add "$item_lower"
    git commit -m "合并模块 $item ，来源版本标签：$MOD_TAG"
done

## 合并类库模块
MERGE_LIB_PROJECT_LIST=(WinBoLL APPBase AES)
echo -e "#@@@ 类库模块开始合并 @@#"
for item in "${MERGE_LIB_PROJECT_LIST[@]}"; do
    item_lower=$(echo "$item" | tr 'A-Z' 'a-z')
    MOD_TAG=$(get_module_latest_tag "$item_lower")

    if [ -z "$MOD_TAG" ]; then
        echo "跳过 $item_lower 未匹配对应版本标签"
        continue
    fi

    echo "正在合并 $item_lower 标签版本：$MOD_TAG"
    git checkout origin/tags/"$MOD_TAG" -- "$item_lower" "lib$item_lower"
    git add "$item_lower" "lib$item_lower"
    git commit -m "合并模块 $item ，来源版本标签：$MOD_TAG"
done

echo "所有模块合并执行完毕"
echo "准备推送远程分支"
git push
