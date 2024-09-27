## 已设置环境变量：
## app_name    默认从pom.xml获取，可以在env.properties中设置覆盖
## app_version 默认从pom.xml获取，可以在env.properties中设置覆盖
## app_source="$app_name"_"$app_version"

## app_source目录已创建，内容包括：
## target/app_source
##   ├─bin
##   │  └─env.properties
##   │  └─run.sh
##   │  └─setenv.sh
##   ├─lib
##   │  └─${app_name}_${app_version}.jar
##   ├─config
##   │  └─application.yml
##   │  └─...
##   ├─install.sh
##   └─changelog.md

## 工作目录为target
build(){
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
}

