#!/system/bin/sh
## 合并其他项目分支的模块源码到projects_keeper_tag分支。

# ====================== 函数定义：获取模块最新提交对应的远程标签 ======================
# 参数1：模块名（小写如appbase、aes）
# 自动以 origin/模块名 作为远程分支
# 返回：有标签输出标签名，无标签输出空
get_module_latest_tag() {
    local module_dir="$1"
    local remote_branch="origin/${module_dir}"

    # 先同步远程分支元数据
    git fetch origin "$module_dir" 2>/dev/null

    # 获取该模块目录最新一次提交CommitHash
    local latest_commit
    latest_commit=$(git log -1 --pretty=format:%H "$remote_branch" -- "$module_dir"/ 2>/dev/null)

    # 无commit直接返回空
    [ -z "$latest_commit" ] && return

    # 匹配远程同commit、模块前缀的标签，过滤废弃^{}引用
    git ls-remote --tags origin "${module_dir}*" 2>/dev/null \
    | grep -v '\^{}' \
    | grep "$latest_commit" \
    | awk '{print $2}' \
    | sed "s/refs\/tags\///"
}

# ====================== 0. 进入目标目录 ======================
TARGET_DIR="/sdcard/AppProjects/Projects_Keeper_Tag"

echo "切换工作目录到：$TARGET_DIR"
if ! cd "$TARGET_DIR"; then
    echo "=============================================="
    echo "错误：无法进入目标目录 $TARGET_DIR"
    echo "=============================================="
    exit 1
fi

# ====================== 1. 拉取远程最新源码(失败直接退出) ======================
echo "=============================================="
echo "正在拉取远程最新源码..."
echo "=============================================="
if ! git pull; then
    echo "=============================================="
    echo "错误：拉取远程源码失败！"
    echo "请检查网络、仓库冲突或手动处理 git 状态后再执行脚本。"
    echo "=============================================="
    exit 1
fi
echo "✅ 源码已拉取为最新状态"
echo ""

# ====================== 2. 目录路径检查 ======================
CUR_DIR=$(pwd)
if [ "$CUR_DIR" != "$TARGET_DIR" ]; then
    echo "错误：当前目录不是项目根目录！"
    echo "请先进入目录：$TARGET_DIR"
    exit 1
fi

# ====================== 3. Git 分支检查 ======================
CUR_BRANCH=$(git symbolic-ref --short HEAD 2>/dev/null)
TARGET_BRANCH="projects_keeper_tag"

if [ "$CUR_BRANCH" != "$TARGET_BRANCH" ]; then
    echo "错误：当前不在 $TARGET_BRANCH 分支！"
    echo "当前分支：$CUR_BRANCH"
    echo "请先执行：git checkout $TARGET_BRANCH"
    exit 1
fi

# ====================== 4. 预设标准模块列表 ======================
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

# ====================== 5. 获取当前目录真实文件列表 ======================
REAL_ITEMS=()
while IFS= read -r line; do
    if [[ "$line" != "." && "$line" != ".." ]]; then
        REAL_ITEMS+=("$line")
    fi
done < <(ls -a)

# ====================== 6. 差异比对函数 ======================
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
        echo "=============================================="
        echo "【错误】合并环境与脚本预设列表不匹配！"
        echo "请手动核对并修改合并脚本。"
        echo "=============================================="

        if [[ ${#missing[@]} -gt 0 ]]; then
            echo -e "\n👉 本地缺失项目："
            for m in "${missing[@]}"; do echo "  $m"; done
        fi

        if [[ ${#extra[@]} -gt 0 ]]; then
            echo -e "\n👉 本地多余项目："
            for e in "${extra[@]}"; do echo "  $e"; done
        fi

        echo -e "\n=============================================="
        exit 1
    fi
}

# 执行差异校验
check_diff

# ====================== 全部校验通过，继续执行合并 ======================
echo -e "#@@@ 开始合并模块源码 @@@#
## 目标合并对象列表："

for item in "${MERGE_OBJECTS_LIST[@]}"; do
    echo "$item"
done

echo -e "## 对象列表结束
"

## 合并 APP 项目
MERGE_APP_PROJECT_LIST=(
DemoAPP
)
echo -e "#@@@ 开始合并应用型模块源码 @@@#"

for item in "${MERGE_APP_PROJECT_LIST[@]}"; do
    item_lower=$(echo "$item" | tr 'A-Z' 'a-z')
    MOD_TAG=$(get_module_latest_tag "$item_lower")
    if [ -z "$MOD_TAG" ]; then
        echo "跳过 $item_lower ：最新提交未打标签"
        continue
    fi
    echo "合并 $item_lower （远程分支：origin/$item_lower，标签：$MOD_TAG）"
    git checkout origin/$item_lower $item_lower
    git add .
    # 提交备注带上标签名
    git commit -m "合并 $item 项目，关联标签：$MOD_TAG"
done

## 合并 LIB 项目
MERGE_LIB_PROJECT_LIST=(
WinBoLL
APPBase
AES
)
echo -e "#@@@ 开始合并类库型模块源码 @@@#"

for item in "${MERGE_LIB_PROJECT_LIST[@]}"; do
    item_lower=$(echo "$item" | tr 'A-Z' 'a-z')
    MOD_TAG=$(get_module_latest_tag "$item_lower")
    if [ -z "$MOD_TAG" ]; then
        echo "跳过 $item_lower ：最新提交未打标签"
        continue
    fi
    echo "合并 $item_lower （远程分支：origin/$item_lower，标签：$MOD_TAG）"
    git checkout origin/$item_lower $item_lower lib$item_lower
    git add .
    # 提交备注带上标签名
    git commit -m "合并 $item 项目，关联标签：$MOD_TAG"
done

echo '正在推送 Projects_Keeper 项目'
git push
