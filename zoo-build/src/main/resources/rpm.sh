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

## rpm目录结构
mkdir -p rpm_build/{BUILD,BUILDROOT,RPMS,SOURCES,SPECS,SRPMS}

## 安装文件
mkdir -p rpm_build/BUILDROOT${app_home}/lib
mv bin rpm_build/BUILDROOT${app_home}
mv "$app_name"-"$app_version".jar rpm_build/BUILDROOT${app_home}/lib
cp -rf classes/config rpm_build/BUILDROOT${app_home}

## 系统服务
mkdir -p rpm_build/BUILDROOT/etc/systemd/system
cat <<EOF > rpm_build/BUILDROOT/etc/systemd/system/${app_name}.service
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

pkg_time=$(date "+%Y%m%d.%H%M%S")

## SPEC
cat <<EOF > rpm_build/SPECS/${app_name}.spec
Name: ${app_name}
Version: ${app_version}
Release: ${pkg_time}
License: Apache-2.0
Summary: ${app_name} ${app_version}

%description
${app_name} ${app_version}

%files
/etc/systemd/system/${app_name}.service
${app_home}/lib/${app_name}-${app_version}.jar
${app_home}/config/*

%config(noreplace) ${app_home}/bin/env.properties
%attr(0755, root, root) ${app_home}/bin/run.sh
%attr(0755, root, root) ${app_home}/bin/setenv.sh

%post
systemctl daemon-reload
systemctl enable ${app_name}
systemctl restart ${app_name}

%preun
systemctl disable ${app_name}
systemctl stop ${app_name}

%postun
systemctl daemon-reload
level_count=\$(echo "$app_home" | tr -cd '/' | wc -c)
if [ "\$level_count" -ge 2 ]; then
    rm -rf ${app_home}
fi
EOF

## 构建rpm
dir=$(pwd)
rpmbuild --define "_topdir ${dir}/rpm_build" --buildroot "${dir}/rpm_build/BUILDROOT" -ba rpm_build/SPECS/${app_name}.spec

## rpm -ivh --nodeps demo-mvc-1.0.0-20241108.144313.x86_64.rpm
## rpm -e --nodeps demo-mvc
}
