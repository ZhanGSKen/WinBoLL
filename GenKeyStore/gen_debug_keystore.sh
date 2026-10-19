#!/bin/bash
# 应用秘钥创建脚本
# Linux 命令行创建JKS秘钥，alias和keyAlias可配置，文件名含时间戳

# 可配置参数（按需修改）
ALIAS="WinBoLL.CC_Debug"  # 别名（与keyAlias一致）
STORE_PASS="androiddebugkey"
KEY_PASS="androiddebugkey"
COUNTRY="CN"        # 国家代码

# 获取当前时间戳
TIMESTAMP=$(date +%Y%m%d%H%M%S)
FILENAME="${ALIAS}_${TIMESTAMP}.jks"
STORENAME="${ALIAS}_${TIMESTAMP}.keystore"

# 生成JKS文件（alias与keyAlias同步）
keytool -genkeypair \
  -alias "${ALIAS}" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 1 \
  -keystore "${FILENAME}" \
  -dname "CN=WBFans, OU=Studio, O=WinBoLL, L=Shanwei, ST=Guangdong, C=${COUNTRY}" \
  -storepass "${STORE_PASS}" \
  -keypass "${KEY_PASS}"

# 写入配置文件
cat <<EOF > ${STORENAME}
keyAlias=${ALIAS}
keyPassword=${KEY_PASS}
storeFile=../appkey.jks
storePassword=${STORE_PASS}
EOF

echo "已生成秘钥：${FILENAME}"
echo "配置已写入 ${STORENAME}（keyAlias=${ALIAS}）"

# 询问是否复制文件
read -p "是否需要将文件复制为 appkey.jks 和 appkey.keystore？(y/n): " CONFIRM

if [[ $CONFIRM =~ ^[Yy]$ ]]; then
  # 复制 jks 文件为 appkey.jks
  cp -v ${FILENAME} ../appkey.jks
  # 复制 keystore 文件为 appkey.keystore
  cp -v ${STORENAME} ../appkey.keystore
  echo "文件复制完成"
else
  echo "已取消文件复制"
fi
