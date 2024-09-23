#! /bin/bash

pwd=$(cd "$(dirname "$0")" && pwd) || { echo "cd failed"; exit 1; }
if [ -f "$pwd/setenv.sh" ];then
    source "$pwd/setenv.sh"
fi
if [ -f "$pwd/bin/setenv.sh" ];then
    source "$pwd/bin/setenv.sh"
fi

install(){
    [ -n "${app_name}" ] || { LogError "app_name not set"; exit 1; }
    [ -n "${app_home}" ] || { LogError "app_home not set"; exit 1; }
    [ -n "${app_version}" ] || { LogError "app_version not set"; exit 1; }
    if [ -d "$app_home" ];then
        v_old=$(sed -n '/^app_version=.*/p' "$app_home/bin/setenv.sh" 2>/dev/null | sed 's/app_version=//g' 2>/dev/null)
        LogError "install terminated: $app_name was already installed, version=$v_old"
        exit 1
        #compare=`awk -v a=$v_old -v b=$v_new 'BEGIN{print(a>=b)?"0":"1"}'`
        #if [ $compare = "0" ]; then
        #    LogWarn "$app_name version $v_old was already installed, install cancelled."
        #    exit 1
        #fi
    fi

    LogInfo "prepare to install $app_name, version: $app_version"
    mkdir -p "$app_home/log"
    install_copy || {
        LogError "install terminated unexpectedly: copy failed."
        if [ -n "$app_home" ] && [ "$app_home" != "/" ]; then
            rm -rf "$app_home"
        fi
        exit 1
    }

    install_time=$(date "+%Y-%m-%d %H:%M:%S")
    sed -i 's/export install_time=.*/export install_time="'"$install_time"'"/' "$app_home/bin/setenv.sh"

    cd "$app_home" || { LogError "cd failed: $app_home"; exit 1; }
    find "$app_home" -type f -name "*.sh" -exec chmod 744 {} \;
    find "$app_home" -type f -name "*.sh" -exec dos2unix {} \; 2>/dev/null
    find "$app_home" -type f -name "*.xml" -exec dos2unix {} \; 2>/dev/null
    find "$app_home" -type f -name "*.properties" -exec dos2unix {} \; 2>/dev/null
    LogSuccess "$app_name install success."
    sh "$app_home/bin/run.sh" start
}

uninstall(){
    [ -n "${app_name}" ] || { LogError "app_name not set"; exit 1; }
    [ -n "${app_home}" ] || { LogError "app_home not set"; exit 1; }
    if [ -d "$app_home" ];then
        if [ -n "$app_home" ] && [ ! "$app_home" = "/" ];then
            sh "$app_home/bin/run.sh" stop
            uninstall_bak
            rm -rf "$app_home"
            LogSuccess "$app_name cleared[home=$app_home]."
        fi
    fi
    LogSuccess "$app_name uninstall success."
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
