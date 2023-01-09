#!/usr/bin/env bash
git config --global http.postBuffer 1048576000
WORKSPACERoot=$(
  cd $(dirname $0)
  pwd
)
WORKSPACE=${WORKSPACERoot}/app

rm -rf ${WORKSPACE}/build/outputs
mkdir ${WORKSPACE}/build/outputs

echo 开始打包
sh ./gradlew assembleDebug
#上传
echo 开始上传蒲公英
ls ${WORKSPACE}/build/outputs/apk/debug/ | grep -v 'apkuploadlist.txt' >${WORKSPACE}/build/apkuploadlist.txt
while read uplodline; do
  path=${WORKSPACE}/build/outputs/apk/debug/${uplodline}
  if [[ $path =~ apk$ ]]; then
    ./pgy_upload.sh "aecf0c2e00c013a3911a436317f100af" $path "来伊份wifi扫描"
    echo path
  fi
done <${WORKSPACE}/build/apkuploadlist.txt
echo 上传蒲公英结束
