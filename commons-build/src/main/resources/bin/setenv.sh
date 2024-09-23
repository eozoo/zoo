#! /bin/bash

## 应用名称
export app_name=""
## 应用版本
export app_version=""
## 应用目录
export app_home="/opt/cowave/$app_name"
## 代码版本
export code_version=""
## 打包时间
export build_time=""
## 安装时间
export install_time=""
## 启动时间
export start_time=""

## Java路径
pwd=$(cd "$(dirname "$0")" && pwd) || { echo "cd failed"; exit 1; }
file="$pwd/env.properties"
if [ -f "$file" ];then
    line_java=$(< "$file" sed '/^#.*/d' | sed '/^[ \t ]*$/d' | grep = | sed 's/[ \t]*=[ \t]*/=/' | grep java_home)
    if [ -n "$line_java" ]; then
        java_home=$(echo "$line_java" | awk -F '=' '{ key=$1; sub(/^[ \t]+/, "", key); sub(/[ \t]+$/, "", key); value=substr($0,length(key)+2); print value}')
        export java_home="$java_home"
    fi
fi

# 替换配置，env.properties中的配置即环境变量
config(){
  echo -n ""
}

# 安装文件拷贝，工作目录为当前安装包解压目录
install_copy(){
    cp -rf bin lib config $app_home
}

# 卸载时备份操作，工作目录为安装包当前解压目录
uninstall_bak(){
    cp -rf $app_home/log .
}

LogSuccess(){
    echo -en "\\033[1;32m"
    echo "$(date "+%Y-%m-%d %H:%M:%S") [INFO] $*"
    echo -en "\\033[0;39m"
}

LogInfo(){
    echo "$(date "+%Y-%m-%d %H:%M:%S") [INFO] $*"
}

LogError(){
    echo -en "\\033[0;31m"
    echo "$(date "+%Y-%m-%d %H:%M:%S") [ERROR] $*"
    echo -en "\\033[0;39m"
}

LogWarn(){
    echo -en "\\033[1;33m"
    echo "$(date "+%Y-%m-%d %H:%M:%S") [WARN] $*"
    echo -en "\\033[0;39m"
}

setenv(){
    if [ ! -f "$file" ];then
        LogError "setenv failed, env.properties not exist."
        exit 1
    fi

    temp=temp.conf.$(date +%s)
    < "$file" sed '/^#.*/d' | sed '/^[ \t ]*$/d' | grep = | sed 's/[ \t]*=[ \t]*/=/' > "$temp"

    option_env="true"
    while read -r line
    do
         key=$(echo "$line" | awk -F "=" '{print $1}')
         if [ -z "$key" ] || [ "$key" = "app_name" ] || [ "$key" = "app_version" ] || [ "$key" = "app_home" ]; then
            continue
         fi

         ev=$(eval echo '$'"$key")
         pv=$(echo "$line" | awk -F '=' '{ key=$1; sub(/^[ \t]+/, "", key); sub(/[ \t]+$/, "", key); value=substr($0,length(key)+2); print value}')
         if [ -z "$ev" ]; then
             if [ "jvm_option" == "$key" ]; then
                 export jvm_option="$jvm_option $pv"
                 option_env="false"
             elif [ "java_home" != "$key" ]; then
                 export "$key"="$pv"
                 if [ "file" == $1 ]; then
                     echo "[Properties]: $key=$pv" | tee -a "$app_home/log/boot.log"
                 else
                     echo "[Properties]: $key=$pv"
                 fi

             fi
         else
             if [ "jvm_option" == "$key" ]; then
                 if [ -n "$pv" ] && [ "false" == "$option_env" ]; then
                     export jvm_option="$jvm_option $pv"
                 fi
             elif [ "java_home" != "$key" ]; then
                  if [ "file" == $1 ]; then
                     echo "[Environment]: $key=$ev" | tee -a "$app_home/log/boot.log"
                  else
                     echo "[Environment]: $key=$ev"
                  fi
             fi
         fi
    done < "$temp"
    rm -f "$temp"
}

replace(){
    file=$1
    key=$2
    value=$3
    index=$4
    if [ -z "$file" ] || [ -z "$key" ] || [ -z "$index" ]; then
      LogError "Usage: replace <file> <key> <value> <index>"
      exit 1
    fi

    line_numbers=($(grep -n "$key:" "$file" | awk -F':' '{print $1}' | tr '\n' ' '))
    if [ "${#line_numbers[@]}" -lt "$index" ]; then
      LogError "there are less than $index occurrences of \"$key:\" in $file"
      exit 1
    fi

    line_number="${line_numbers[$index-1]}"
    sed -i "${line_number}s|$key:.*$|$key: $value|" "$file"
}
