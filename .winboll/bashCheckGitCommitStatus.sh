#!/usr/bin/bash

# 使用 `-z` 命令检查变量是否为空
if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Script parameter error: $0"
    exit 2
fi

# 进入项目根目录
cd ${1}
echo -e "Work dir : \n"`pwd`

git config --global --add safe.directory "${1}"
echo "Current dir : "`pwd`
versionName=${2}

## 设置要检查的标签
tag="v"${versionName}

## 如果Git已经提交了所有代码就执行标签检查操作
if [[ -n $(git diff --stat)  ]]
then
  echo 'Source is no commit git completely, tag action cancel.'
  exit 1
else
  echo "Git status is clean."
  if [ "$(git tag -l ${tag})" == "${tag}" ]; then
      echo "Tag ${tag} exist."
      exit 2
  fi
  echo "${0}: Git tag is checked OK: (${tag})"
fi
