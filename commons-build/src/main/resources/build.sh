#! /bin/bash

pwd=$(cd "$(dirname "$0")" && pwd) || { echo "cd failed"; exit 1; }
if [ -f "$pwd/setenv.sh" ];then
    source "$pwd/setenv.sh"
fi
if [ -f "$pwd/bin/setenv.sh" ];then
    source "$pwd/bin/setenv.sh"
fi

## 工作目录为Target
build_prepare(){
    #git fetch --all
    #git reset --hard origin/master

    if [ -z "$app_name" ]; then
      app_name=$(grep -B 4 packaging ../pom.xml | grep artifactId | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')
    fi
    buildTime=$(date "+%Y-%m-%d %H:%M:%S")
    app_version=$(grep -B 4 packaging ../pom.xml | grep version | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')

    #commit=`svn info|awk 'NR==9{print $4}'`
    commit=$(git log -n 1 --pretty=oneline | awk '{print $1}')
    branch=$(git name-rev --name-only HEAD)
    codeVersion="$branch $commit"

    commit_msg=$(git log --pretty=format:"%s" -1)
    commit_time=$(git log --pretty=format:"%cd" -1)
    commit_author=$(git log --pretty=format:"%an" -1)

    echo "${app_name} ${app_version}(${branch} ${commit} @${commit_author})"
    if [ -f classes/META-INF/info.yml ];then
        ## info.application
        replace classes/META-INF/info.yml name "$app_name" 1
        replace classes/META-INF/info.yml version "$app_version" 1
        replace classes/META-INF/info.yml build "$buildTime" 1
        ## info.commit
        replace classes/META-INF/info.yml version \""$codeVersion"\" 2
        replace classes/META-INF/info.yml Msg \""$commit_msg"\" 1
        replace classes/META-INF/info.yml Time "$commit_time" 1
        replace classes/META-INF/info.yml Author "$commit_author" 1
        ## spring.application.name
        replace classes/META-INF/info.yml name "$app_name" 2
    fi
}

## 工作目录为Target
build_tar(){
    jarName=$(grep -B 4 packaging ../pom.xml | grep artifactId | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')
    if [ -z "$app_name" ]; then
        app_name=$jarName
    fi
    app_version=$(grep -B 4 packaging ../pom.xml | grep version | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')

    buildTime=$(date "+%Y-%m-%d %H:%M:%S")
    commit=$(git log -n 1 --pretty=oneline | awk '{print $1}')
    branch=$(git name-rev --name-only HEAD)
    codeVersion="$branch $commit"

    mkdir -p "$app_name"_"$app_version"/lib
    cp -rf bin "$app_name"_"$app_version"
    if [ -f ../changelog.md ];then
        cp -f ../changelog.md "$app_name"_"$app_version"
    else
        touch "$app_name"_"$app_version"/changelog.md
    fi
    mv "$app_name"_"$app_version"/bin/install.sh "$app_name"_"$app_version"

    if [ -f "$jarName"-"$app_version"-encrypted.jar ];then
        cp "$jarName"-"$app_version"-encrypted.jar "$app_name"_"$app_version"/lib/"$app_name"-"$app_version".jar
    else
        cp "$jarName"-"$app_version".jar "$app_name"_"$app_version"/lib/"$app_name"-"$app_version".jar
    fi

    cp -rf classes/config "$app_name"_"$app_version"

    sed -i 's#export app_name=.*#export app_name="'"$app_name"'"#' "$app_name"_"$app_version"/bin/setenv.sh
    sed -i 's#export app_version=.*#export app_version="'"$app_version"'"#' "$app_name"_"$app_version"/bin/setenv.sh
    sed -i 's#export code_version=.*#export code_version="'"$codeVersion"'"#' "$app_name"_"$app_version"/bin/setenv.sh
    sed -i 's#export build_time=.*#export build_time="'"$buildTime"'"#' "$app_name"_"$app_version"/bin/setenv.sh

    build_time=$(date "+%Y%m%d")
    build_tar="$app_name"_"$app_version"_"$build_time".tar.gz
    tar zcvf "$build_tar" "$app_name"_"$app_version"
    md5sum "$build_tar" > "$build_tar.md5"
}

## 工作目录为Target，基于Tar包的基础构建image
build_docker(){
  jarName=$(grep -B 4 packaging ../pom.xml | grep artifactId | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')
  if [ -z "$app_name" ]; then
      app_name=$jarName
  fi
  app_version=$(grep -B 4 packaging ../pom.xml | grep version | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')
  app_source="$app_name"_"$app_version"

cat <<EOF > Dockerfile
FROM openjdk:17-oracle

WORKDIR /opt/cowave/${app_name}

ADD ${app_source}/bin /opt/cowave/${app_name}/bin/
ADD ${app_source}/lib /opt/cowave/${app_name}/lib/
ADD ${app_source}/config /opt/cowave/${app_name}/config/
ADD ${app_source}/changelog.md /opt/cowave/${app_name}/

ENTRYPOINT ["bin/run.sh", "up"]
EOF

  docker build -t cowave/$app_name:$app_version .
  #docker rmi -f $(docker images | grep "<none>" | awk '{print $3}')  2>/dev/null
}

## 工作目录为Target，基于Tar包的基础构建deb
build_deb(){
  jarName=$(grep -B 4 packaging ../pom.xml | grep artifactId | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')
  if [ -z "$app_name" ]; then
      app_name=$jarName
  fi
  app_version=$(grep -B 4 packaging ../pom.xml | grep version | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')
  app_source="$app_name"_"$app_version"

## 复制文件
mkdir -p dpkg/opt/cowave/${app_name}
cp -rf ${app_source}/bin dpkg/opt/cowave/${app_name}
cp -rf ${app_source}/lib dpkg/opt/cowave/${app_name}
cp -rf ${app_source}/config dpkg/opt/cowave/${app_name}
cp -rf ${app_source}/changelog.md dpkg/opt/cowave/${app_name}

## 覆盖文件
cat <<EOF > dpkg/conffiles
/opt/cowave/${app_name}/bin/env.properties
EOF

## 系统服务
mkdir -p dpkg/etc/systemd/system
cat <<EOF > dpkg/etc/systemd/system/${app_name}.service
[Unit]
Description=sys-eureka service

[Service]
ExecStart=/opt/cowave/${app_name}/bin/run.sh start
ExecStop=/opt/cowave/${app_name}/bin/run.sh stop
Restart=on-failure
Type=forking

[Install]
WantedBy=multi-user.target
EOF

## 安装后操作
mkdir -p dpkg/DEBIAN
cat <<EOF > dpkg/DEBIAN/postinst
#! /bin/bash
systemctl daemon-reload
systemctl enable ${app_name}
EOF
chmod +x dpkg/DEBIAN/postinst

## 打包描述
cat <<EOF > dpkg/DEBIAN/control
Package: ${app_name}
Version: ${app_version}
Section: utils
Priority: optional
Architecture: amd64
Depends:
Maintainer: cowave.com
Description: cowave sys service
EOF

## 打包
dpkg-deb -b dpkg ${app_name}_${app_version}.deb
}
