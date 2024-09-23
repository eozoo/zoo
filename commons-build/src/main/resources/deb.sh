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

## 构建deb
dpkg-deb -b dpkg ${app_name}_${app_version}.deb
}
