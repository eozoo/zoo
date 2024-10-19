#! /bin/bash

prepare_build(){
  ## 检查自定义的文件
  if [ -f ../bin/env.properties ];then
      cp -f ../bin/env.properties bin/env.properties
  fi
  if [ -f ../bin/setenv.sh ];then
      cp -f ../bin/setenv.sh bin/setenv.sh
  fi
  if [ -f ../bin/run.sh ];then
      cp -f ../bin/run.sh bin/run.sh
  fi
  if [ -f ../bin/install.sh ];then
      cp -f ../bin/install.sh bin/install.sh
  fi
  if [ -f ../tar.sh ];then
      cp -f ../tar.sh ./tar.sh
  fi
  if [ -f ../docker.sh ];then
      cp -f ../docker.sh ./docker.sh
  fi
  if [ -f ../deb.sh ];then
      cp -f ../deb.sh ./deb.sh
  fi
  find . -type f -name "*.sh" -exec chmod 744 {} \;
  find . -type f -name "*.sh" -exec dos2unix {} \;

  ## 获取app_name和app_version（优先取env.properties，没有则从pom.xml获取），然后写到setenv.sh中
  line_app_name=$(< "./bin/env.properties" sed '/^#.*/d' | sed '/^[ \t ]*$/d' | grep = | sed 's/[ \t]*=[ \t]*/=/' | grep app_name)
  app_name=$(echo "$line_app_name" | awk -F '=' '{ key=$1; sub(/^[ \t]+/, "", key); sub(/[ \t]+$/, "", key); value=substr($0,length(key)+2); print value}')
  if [ -z "$app_name" ]; then
      app_name=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='artifactId']/text()" ../pom.xml)
      if [ -z "$app_name" ]; then
          app_name=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='parent']/*[local-name()='artifactId']/text()" ../pom.xml)
      fi
      ## app_name=$(grep -B 4 packaging ../pom.xml | grep artifactId | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')
  fi

  line_app_version=$(< "./bin/env.properties" sed '/^#.*/d' | sed '/^[ \t ]*$/d' | grep = | sed 's/[ \t]*=[ \t]*/=/' | grep app_version)
  app_version=$(echo "$line_app_version" | awk -F '=' '{ key=$1; sub(/^[ \t]+/, "", key); sub(/[ \t]+$/, "", key); value=substr($0,length(key)+2); print value}')
  if [ -z "$app_version" ]; then
      app_version=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='version']/text()" ../pom.xml)
      if [ -z "$app_version" ]; then
          app_version=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='parent']/*[local-name()='version']/text()" ../pom.xml)
      fi
      ## app_version=$(grep -B 4 packaging ../pom.xml | grep version | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')
  fi

  line_app_home=$(< "./bin/env.properties" sed '/^#.*/d' | sed '/^[ \t ]*$/d' | grep = | sed 's/[ \t]*=[ \t]*/=/' | grep app_home)
  app_home=$(echo "$line_app_home" | awk -F '=' '{ key=$1; sub(/^[ \t]+/, "", key); sub(/[ \t]+$/, "", key); value=substr($0,length(key)+2); print value}')
  if [ -z "$app_home" ]; then
      app_home="/opt/cowave/$app_name"
  fi

  ## 设置setenv.sh中的变量，比如app_name、app_version、app_home
  build_time=$(date "+%Y-%m-%d %H:%M:%S")
  sed -i 's#export app_home=.*#export app_home="'"$app_home"'"#' bin/setenv.sh
  sed -i 's#export app_name=.*#export app_name="'"$app_name"'"#' bin/setenv.sh
  sed -i 's#export app_version=.*#export app_version="'"$app_version"'"#' bin/setenv.sh
  sed -i 's#export build_time=.*#export build_time="'"$build_time"'"#' bin/setenv.sh

  ## 尝试改下META-INF/git.info
  if [ -f classes/META-INF/git.info ];then
      sed -i "1 s/{/{\n    \"build.time\": \"$build_time\",/" classes/META-INF/git.info
      sed -i "1 s/{/{\n    \"app.version\": \"$app_version\",/" classes/META-INF/git.info
      sed -i "1 s/{/{\n    \"app.name\": \"$app_name\",/" classes/META-INF/git.info
      ## 获取代码版本信息
      commit=$(git log -n 1 --pretty=oneline | awk '{print $1}')
      branch=$(git name-rev --name-only HEAD)
      code_version="$branch $commit"
      sed -i 's#export code_version=.*#export code_version="'"$code_version"'"#' bin/setenv.sh
  else
cat <<EOF > classes/META-INF/git.info
{
    "app.name": "$app_home",
    "app.version": "$app_version",
    "build.time": "$build_time"
}
EOF
  fi
}

tar_build(){
  . "./bin/setenv.sh"
  ## jar_name=$(grep -B 4 packaging ../pom.xml | grep artifactId | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')
  ## jar_version=$(grep -B 4 packaging ../pom.xml | grep version | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')
  jar_name=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='artifactId']/text()" ../pom.xml)
  if [ -z "$jar_name" ]; then
      jar_name=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='parent']/*[local-name()='artifactId']/text()" ../pom.xml)
  fi
  jar_version=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='version']/text()" ../pom.xml)
  if [ -z "$jar_version" ]; then
      jar_version=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='parent']/*[local-name()='version']/text()" ../pom.xml)
  fi

  ## 如果使用了classfinal加密，重新命名下
  if [ -f "$jar_name"-"$jar_version"-encrypted.jar ];then
      mv "$jar_name"-"$jar_version"-encrypted.jar "$app_name"-"$app_version".jar
  else
      mv "$jar_name"-"$jar_version".jar "$app_name"-"$app_version".jar
  fi

  . "./tar.sh"
  build
}

docker_build(){
  . "./bin/setenv.sh"
  ## jar_name=$(grep -B 4 packaging ../pom.xml | grep artifactId | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')
  ## jar_version=$(grep -B 4 packaging ../pom.xml | grep version | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')
  jar_name=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='artifactId']/text()" ../pom.xml)
  if [ -z "$jar_name" ]; then
      jar_name=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='parent']/*[local-name()='artifactId']/text()" ../pom.xml)
  fi
  jar_version=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='version']/text()" ../pom.xml)
  if [ -z "$jar_version" ]; then
      jar_version=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='parent']/*[local-name()='version']/text()" ../pom.xml)
  fi

  ## 如果使用了classfinal加密，重新命名下
  if [ -f "$jar_name"-"$jar_version"-encrypted.jar ];then
      mv "$jar_name"-"$jar_version"-encrypted.jar "$app_name"-"$app_version".jar
  else
      mv "$jar_name"-"$jar_version".jar "$app_name"-"$app_version".jar
  fi

  rm -f bin/install.sh

  . "./docker.sh"
  build
}

deb_build(){
  . "./bin/setenv.sh"
  ## jar_name=$(grep -B 4 packaging ../pom.xml | grep artifactId | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')
  ## jar_version=$(grep -B 4 packaging ../pom.xml | grep version | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')
  jar_name=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='artifactId']/text()" ../pom.xml)
  if [ -z "$jar_name" ]; then
      jar_name=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='parent']/*[local-name()='artifactId']/text()" ../pom.xml)
  fi
  jar_version=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='version']/text()" ../pom.xml)
  if [ -z "$jar_version" ]; then
      jar_version=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='parent']/*[local-name()='version']/text()" ../pom.xml)
  fi

  ## 如果使用了classfinal加密，重新命名下
  if [ -f "$jar_name"-"$jar_version"-encrypted.jar ];then
      mv "$jar_name"-"$jar_version"-encrypted.jar "$app_name"-"$app_version".jar
  else
      mv "$jar_name"-"$jar_version".jar "$app_name"-"$app_version".jar
  fi

  rm -f bin/install.sh

  . "./deb.sh"
  build
}

case "$1" in
    prepare)
        prepare_build $2
        ;;
    tar)
        tar_build
        ;;
    docker)
        docker_build
        ;;
    deb)
        deb_build
        ;;
    *)
esac
