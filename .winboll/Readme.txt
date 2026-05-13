## WinBoLL 主机编译事项提醒

## 类库类型源码发布
# 类库发布使用以下面命令
git pull && bash .winboll/bashPublishLIBAddTag.sh <类库模块文件夹名称>

## 纯应用类型源码发布
# 应用发布使用以下命令
git pull && bash .winboll/bashPublishAPKAddTag.sh <应用模块文件夹名称>

## 编译时提问。Add Github Workflows Tag? (yes/No)
回答yes: 将会添加一个 GitHub 工作流标签
        GitHub 仓库会执行以该标签为标志的编译工作流。
回答No(默认): 就忽略 GitHub 标签，忽略 GitHub 工作流调用。

## Github Workflows 工作流设置注意事项
应用名称改变时需要修改.github/workflows/android.yml文件设置，
在第79行：asset_name: 处有应用包名称设置。
