## jar包构建之后执行，工作目录为Target，
## 环境变量已设置：app_source="$app_name"_"$app_version"，
## app_source中已有内容：/bin、/lib（空目录）、/config、changelog.md、install.sh
build(){
    jar_name=$(grep -B 4 packaging ../pom.xml | grep artifactId | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')
    jar_version=$(grep -B 4 packaging ../pom.xml | grep version | awk -F ">" '{print $2}' | awk -F "<" '{print $1}')
    ## 拷贝jar包，如果使用了classfinal加密，重新命名下
    if [ -f "$jar_name"-"$jar_version"-encrypted.jar ];then
        cp "$jar_name"-"$jar_version"-encrypted.jar "$app_source"/lib/"$app_name"-"$app_version".jar
    else
        cp "$jar_name"-"$jar_version".jar "$app_source"/lib/"$app_name"-"$app_version".jar
    fi

## 构建镜像
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

