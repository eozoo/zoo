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
##   └─config
##      └─application.yml
##      └─...

## 工作目录为target
build(){
## 准备文件
mkdir -p dpkg/opt/cowave/${app_name}/lib
mv bin dpkg/opt/cowave/${app_name}
mv "$app_name"-"$app_version".jar dpkg/opt/cowave/${app_name}/lib
cp -rf classes/config dpkg/opt/cowave/${app_name}

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
