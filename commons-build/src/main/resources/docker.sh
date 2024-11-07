## 可用环境变量见env.properties：
## app_name    默认从pom.xml获取，可以在env.properties中设置覆盖
## app_version 默认从pom.xml获取，可以在env.properties中设置覆盖

## 默认打包内容：
## ${app_name}_${app_version}
##   ├─bin
##   │  └─env.properties
##   │  └─run.sh
##   │  └─setenv.sh
##   ├─lib
##   │  └─${app_name}_${app_version}.jar
##   └─config
##      └─application.yml
##      └─...

## 工作目录为target
build(){
cat <<EOF > Dockerfile
FROM openjdk:17-oracle

WORKDIR /opt/cowave/${app_name}

ADD bin /opt/cowave/${app_name}/bin/
ADD classes/config /opt/cowave/${app_name}/config/
ADD "$app_name"-"$app_version".jar /opt/cowave/${app_name}/lib/

ENTRYPOINT ["bin/run.sh", "up"]
EOF

docker build -t cowave/$app_name:$app_version .
}

