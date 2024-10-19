## 已设置环境变量：
## app_name    默认从pom.xml获取，可以在env.properties中设置覆盖
## app_version 默认从pom.xml获取，可以在env.properties中设置覆盖
## app_source="$app_name"_"$app_version"

## 默认打包内容：
## ${app_name}_${app_version}
##   ├─bin
##   │  └─env.properties
##   │  └─run.sh
##   │  └─setenv.sh
##   ├─lib
##   │  └─${app_name}_${app_version}.jar
##   ├─config
##   │  └─application.yml
##   │  └─...
##   └─install.sh

## 工作目录为target
build(){
    app_source="$app_name"_"$app_version"
    mkdir -p "$app_source"/lib
    mv bin "$app_source"
    mv "$app_source"/bin/install.sh "$app_source"
    mv "$app_name"-"$app_version".jar "$app_source"/lib
    cp -rf classes/config "$app_source"

    build_date=$(date "+%Y%m%d")
    build_tar="$app_name"_"$app_version"_"$build_date".tar.gz
    tar zcvf "$build_tar" "$app_source"
    md5sum "$build_tar" > "$build_tar.md5"
}
