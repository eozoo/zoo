#! /bin/bash

pwd=$(cd "$(dirname "$0")" && pwd) || { echo "cd failed"; exit 1; }
if [ -f "$pwd/setenv.sh" ];then
    . "$pwd/setenv.sh"
fi
if [ -f "$pwd/bin/setenv.sh" ];then
    . "$pwd/bin/setenv.sh"
fi

java_commond=$(which java 2>/dev/null)
if [ -n "$java_home" ];then
    java_commond=$java_home/bin/java
fi

jps_commond=$(which jps 2>/dev/null)
if [ -n "$java_home" ];then
    jps_commond=$java_home/bin/jps
fi

install(){
    [ -n "${app_name}" ] || { LogError "app_name not set"; exit 1; }
    [ -n "${app_home}" ] || { LogError "app_home not set"; exit 1; }
    [ -n "${app_version}" ] || { LogError "app_version not set"; exit 1; }
    $java_commond -version 2>&1 || { LogError "java not installed"; exit 1; }

    echo "prepare to install $app_name, version: $app_version"
    mkdir -p "$app_home/log"
    install_time=$(date "+%Y-%m-%d %H:%M:%S")

    ## 已经安装
    if [ -d "$app_home/lib" ];then
        v_old=$(sed -n '/^export app_version=.*/p' "$app_home/bin/setenv.sh" 2>/dev/null | sed 's/export app_version=//;s/^"//;s/"$//' 2>/dev/null)
        ## 确认继续安装
        read -p "$app_name($v_old) was already installed, continue to install [y/n](default: y)? " input
        input=${input:-"yes"}
        case $input in
            [Yy][Ee][Ss]|[Yy])
                bak_time=$(date "+%Y%m%d_%H%M%S")
                ## 确认保留配置
                read -p "reuse the existing config and scripts [y/n](default: y)? " input2
                input2=${input2:-"yes"}
                ## 确认注册系统服务
                read -p "register as a system service [y/n](default: n)? " input5
                input5=${input5:-"no"}

                install_copy || {
                    LogError "install terminated unexpectedly: copy failed."
                    exit 1
                }

                ## 备份lib
                if [ -d "$app_home/lib" ];then
                    mv $app_home/lib/${app_name}-${v_old}.jar $app_home/lib/${app_name}-${v_old}.jar_${bak_time} 2>/dev/null
                fi
                cp -rf lib/* $app_home/lib

                ## 备份config、bin
                case $input2 in
                    [Yy][Ee][Ss]|[Yy])
                        ;;
                    *)
                        if [ -d "$app_home/config" ];then
                            mv $app_home/config $app_home/config-${v_old}_${bak_time}
                        fi
                        cp -rf config $app_home

                        if [ -d "$app_home/bin" ];then
                            mv $app_home/bin $app_home/bin-${v_old}_${bak_time}
                        fi
                        cp -rf bin $app_home
                        sed -i 's/export install_time=.*/export install_time="'"$install_time"'"/' "$app_home/bin/setenv.sh"
                        ;;
                esac

                ## systemctl注册
                case $input5 in
                    [Yy][Ee][Ss]|[Yy])
                        input5="yes"
                        cp -f ${app_name}.service /etc/systemd/system 2>/dev/null
                        sed -i "/^\[Service\]/a Environment=java_commond=$java_commond" /etc/systemd/system/${app_name}.service 2>/dev/null
                        sed -i "/^\[Service\]/a Environment=jps_commond=$jps_commond" /etc/systemd/system/${app_name}.service 2>/dev/null
                        systemctl daemon-reload 2>/dev/null
                        systemctl enable ${app_name} 2>/dev/null
                        ;;
                    *)
                        ;;
                esac
                LogSuccess "$app_name $app_version install complete"

                ## 重启进程
                pid=$($jps_commond -l | grep "$app_home/lib/$app_name-$v_old.jar" | awk '{print $1}')
                if [ -n "$pid" ];then
                    read -p "$app_name is running, sure to restart [y/n](default: y)? " input3
                    input3=${input3:-"yes"}
                    case $input3 in
                        [Yy][Ee][Ss]|[Yy])
                            if [ "$input5" = "yes" ]; then
                                ## 脚本停止，systemctl重启
                                bash "$app_home/bin/run.sh" stop
                                systemctl restart ${app_name} 2>/dev/null
                                LogSuccess "$app_name is starting, see details with \"journalctl -u ${app_name}\""
                            else
                                ## 取消系统守护，脚本重启
                                rm -f /etc/systemd/system/${app_name}.service
                                systemctl disable ${app_name} 2>/dev/null
                                systemctl daemon-reload 2>/dev/null
                                bash "$app_home/bin/run.sh" restart
                            fi
                            ;;
                        *)
                            exit 0
                            ;;
                    esac
                else
                    if [ "$input5" = "yes" ]; then
                        ## systemctl启动
                        systemctl restart $app_name 2>/dev/null
                        LogSuccess "$app_name is starting, see details with \"journalctl -u ${app_name}\""
                    else
                        ## 取消系统守护，脚本启动
                        rm -f /etc/systemd/system/${app_name}.service
                        systemctl disable ${app_name} 2>/dev/null
                        systemctl daemon-reload 2>/dev/null
                        bash "$app_home/bin/run.sh" start
                    fi
                fi
                ;;
            *)
                exit 0
                ;;
        esac
    else
        install_copy || {
            LogError "install terminated unexpectedly: copy failed."
            if [ -n "$app_home" ] && [ "$app_home" != "/" ]; then
                level_count=$(echo "$app_home" | tr -cd '/' | wc -c)
                if [ "$level_count" -ge 2 ]; then
                    ## 风险操作，只允许删除二级以下目录
                    rm -rf "$app_home"
                else
                    LogError "$app_home is top dir, which is not allowed to be deleted"
                fi
            fi
            exit 1
        }
        cp -rf bin lib config $app_home

        sed -i 's/export install_time=.*/export install_time="'"$install_time"'"/' "$app_home/bin/setenv.sh"
        find "$app_home" -type f -name "*.sh" -exec chmod 744 {} \;
        find "$app_home" -type f -name "*.sh" -exec dos2unix {} \; 2>/dev/null
        find "$app_home" -type f -name "*.xml" -exec dos2unix {} \; 2>/dev/null
        find "$app_home" -type f -name "*.properties" -exec dos2unix {} \; 2>/dev/null
        LogSuccess "$app_name $app_version install complete"

        ## 注册系统服务
        read -p "register as a system service [y/n](default: n)? " input4
        input4=${input4:-"no"}
        case $input4 in
            [Yy][Ee][Ss]|[Yy])
                ## systemctl启动
                cp -f ${app_name}.service /etc/systemd/system
                sed -i "/^\[Service\]/a Environment=java_commond=$java_commond" /etc/systemd/system/${app_name}.service
                sed -i "/^\[Service\]/a Environment=jps_commond=$jps_commond" /etc/systemd/system/${app_name}.service
                systemctl daemon-reload 2>/dev/null
                systemctl enable ${app_name} 2>/dev/null
                systemctl restart ${app_name} 2>/dev/null
                LogSuccess "$app_name is starting, see details with \"journalctl -u ${app_name}\""
                ;;
            *)
                ## 脚本启动
                bash "$app_home/bin/run.sh" start
                ;;
        esac
    fi
}

uninstall(){
    [ -n "${app_name}" ] || { LogError "app_name not set"; exit 1; }
    [ -n "${app_home}" ] || { LogError "app_home not set"; exit 1; }
    if [ -d "$app_home" ];then
        if [ -n "$app_home" ] && [ ! "$app_home" = "/" ];then
            ## 移除守护
            rm -f /etc/systemd/system/${app_name}.service ${app_name} 2>/dev/null
            systemctl disable ${app_name} 2>/dev/null
            systemctl daemon-reload 2>/dev/null
            ## 停止服务
            bash "$app_home/bin/run.sh" stop
            level_count=$(echo "$app_home" | tr -cd '/' | wc -c)
            if [ "$level_count" -ge 2 ]; then
                ## 风险操作，只允许删除二级以下目录
                rm -rf "$app_home"
                LogSuccess "$app_name cleared[home=$app_home]"
            else
                LogError "$app_home is top dir, which is not allowed to be deleted"
            fi
        fi
    fi
    LogSuccess "$app_name uninstall complete."
}

case "$1" in
    install)
        install
        ;;
    uninstall)
        uninstall
        ;;
    reinstall)
        uninstall
        install
        ;;
    *)
    LogError $"usage: $0 {install|uninstall|reinstall}"
    exit 1
esac
exit 0
