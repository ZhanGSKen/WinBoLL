#!/system/bin/sh
## 合并远程模块指定标签对应提交点文件夹到本地 projects_keeper_tag 分支

# ====================== 获取模块标签与对应Commit ======================
get_module_tag_commit() {
    local module_dir="$1"
    local remote_branch="origin/${module_dir}"

    git fetch origin "$module_dir" 2>/dev/null
    git fetch origin --tags 2>/dev/null

    # 获取远程分支最新commit
    local latest_commit
    latest_commit=$(git log -1 --pretty=format:%H "$remote_branch" 2>/dev/null)

    echo "  调试：模块[$module_dir] 远程分支最新Commit = $latest_commit" >&2

    if [ -z "$latest_commit" ]; then
        echo ""
        return
    fi

    # 匹配模块名-开头标签，取出标签名
    local target_tag
    target_tag=$(git ls-remote --tags origin "${module_dir}-*" 2>/dev/null \
    | grep -v '\^{}' \
    | awk -v cm="$latest_commit" '$1==cm{print $2}' \
    | sed 's/refs\/tags\///')

    # 输出格式：标签名|commit哈希
    echo "${target_tag}|${latest_commit}"
}

# ====================== 进入工作目录 ======================
TARGET_DIR="/sdcard/AppProjects/Projects_Keeper_Tag"
echo "切换工作目录：$TARGET_DIR"
if ! cd "$TARGET_DIR"; then
    echo "目录切换失败！"
    exit 1
fi

# ====================== 同步远程代码与标签 ======================
echo "=============================================="
echo "同步远程代码及全部版本标签"
echo "=============================================="
git pull
git fetch origin --tags
echo "同步完成"
echo ""

# ====================== 锁定目标分支 ======================
CUR_BRANCH=$(git symbolic-ref --short HEAD 2>/dev/null)
TARGET_BRANCH="projects_keeper_tag"
if [ "$CUR_BRANCH" != "$TARGET_BRANCH" ]; then
    echo "错误：当前不在 $TARGET_BRANCH 分支，请先切换！"
    exit 1
fi

# ====================== 目录结构校验 ======================
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
echo -e "#@@@ 开始按标签对应提交点合并模块 @@@#"

## 合并应用型模块
MERGE_APP_PROJECT_LIST=(DemoAPP)
echo -e "#@@@ 应用型模块合并开始 @@#"
for item in "${MERGE_APP_PROJECT_LIST[@]}"; do
    item_lower=$(echo "$item" | tr 'A-Z' 'a-z')
    tag_info=$(get_module_tag_commit "$item_lower")

    # 分割 标签名 和 commit哈希
    MOD_TAG=${tag_info%%|*}
    MOD_COMMIT=${tag_info##*|}

    if [ -z "$MOD_TAG" ] || [ -z "$MOD_COMMIT" ]; then
        echo "跳过 $item_lower 未匹配有效标签与提交点"
        continue
    fi

    echo "模块：$item_lower  标签：$MOD_TAG  对应提交点：$MOD_COMMIT"
    # 核心：从远程模块分支 指定commit拉取对应文件夹
    git checkout "origin/${item_lower}" "${MOD_COMMIT}:${item_lower}"
    git add "${item_lower}"
    git commit -m "合并模块 $item ，来源标签:$MOD_TAG 提交点:$MOD_COMMIT"
done

## 合并类库模块
MERGE_LIB_PROJECT_LIST=(WinBoLL APPBase AES)
echo -e "#@@@ 类库模块合并开始 @@#"
for item in "${MERGE_LIB_PROJECT_LIST[@]}"; do
    item_lower=$(echo "$item" | tr 'A-Z' 'a-z')
    tag_info=$(get_module_tag_commit "$item_lower")

    MOD_TAG=${tag_info%%|*}
    MOD_COMMIT=${tag_info##*|}

    if [ -z "$MOD_TAG" ] || [ -z "$MOD_COMMIT" ]; then
        echo "跳过 $item_lower 未匹配有效标签与提交点"
        continue
    fi

    echo "模块：$item_lower  标签：$MOD_TAG  对应提交点：$MOD_COMMIT"
    # 拉取主目录 + lib目录 指定commit文件
    git checkout "origin/${item_lower}" "${MOD_COMMIT}:${item_lower}" "${MOD_COMMIT}:lib${item_lower}"
    git add "${item_lower}" "lib${item_lower}"
    git commit -m "合并模块 $item ，来源标签:$MOD_TAG 提交点:$MOD_COMMIT"
done

echo "所有模块合并完成"
echo "准备推送远程分支"
git push
