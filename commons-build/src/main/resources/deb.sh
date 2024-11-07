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

mkdir -p dpkg${app_home}/lib
## 拷贝文件
mv bin dpkg${app_home}
mv "$app_name"-"$app_version".jar dpkg${app_home}/lib
cp -rf classes/config dpkg${app_home}

mkdir -p dpkg/etc/systemd/system
## 系统服务
cat <<EOF > dpkg/etc/systemd/system/${app_name}.service
[Unit]
Description=${app_name} ${app_version}
StartLimitIntervalSec=5min
StartLimitBurst=5

[Service]
ExecStart=/bin/bash ${app_home}/bin/run.sh start
ExecStop=/bin/bash ${app_home}/bin/run.sh stop
Restart=on-failure
Type=forking

[Install]
WantedBy=multi-user.target
EOF

env_config="${app_home}/bin/env.properties"
bak_config="${app_home}/bin/env.properties.bak"

mkdir -p dpkg/DEBIAN
## 安装前操作
cat <<EOF > dpkg/DEBIAN/preinst
#! /bin/bash

if [ -f "${env_config}" ];then
    cp -f ${env_config} ${bak_config}
fi
EOF
chmod +x dpkg/DEBIAN/preinst

## 安装后操作
cat <<EOF > dpkg/DEBIAN/postinst
#! /bin/bash

if [ -f "${bak_config}" ];then
    read -p "reuse the existing config and scripts [y/n](default: y)? " input
    input=\${input:-"yes"}
    case \$input in
        [Yy][Ee][Ss]|[Yy])
            rm -f ${env_config}
            mv ${bak_config} ${env_config}
            ;;
        *)
            rm -f ${bak_config}
            ;;
    esac
fi

systemctl daemon-reload
systemctl enable ${app_name}
systemctl restart ${app_name}
echo "${app_name} is starting, see details with \"journalctl -u ${app_name}\""
EOF
chmod +x dpkg/DEBIAN/postinst

## 打包描述
cat <<EOF > dpkg/DEBIAN/control
Package: ${app_name}
Version: ${app_version}
Section: service
Priority: optional
Architecture: amd64
Depends:
Maintainer: cowave.com
Description: ${app_name} ${app_version}
EOF

## 构建deb
dpkg-deb -b dpkg ${app_name}_${app_version}.deb
}
