#! /bin/bash

copy_exist(){
  if [ -f ../bin/env.properties ];then
      cp -f ../bin/env.properties bin/env.properties
  fi
  if [ -f ../bin/setenv.sh ];then
      cp -f ../bin/setenv.sh bin/setenv.sh
  fi
  if [ -f ../bin/run.sh ];then
      cp -f ../bin/run.sh bin/run.sh
  fi
  if [ -f ../bin/install.sh ];then
      cp -f ../bin/install.sh bin/install.sh
  fi
  if [ -f ../build.sh ];then
      cp -f ../build.sh ./build.sh
  fi
  find . -type f -name "*.sh" -exec chmod 744 {} \;
  find . -type f -name "*.sh" -exec dos2unix {} \;
}

prepare_build(){
  copy_exist
  source "./build.sh"
  build_prepare
}

tar_build(){
  source "./build.sh"
  build_tar
}

docker_build(){
  source "./build.sh"
  build_tar
  build_docker
}

deb_build(){
  source "./build.sh"
  build_tar
  build_deb
}

case "$1" in
    prepare)
        prepare_build
        ;;
    tar)
        tar_build
        ;;
    docker)
        docker_build
        ;;
    deb)
        deb_build
        ;;
    *)
esac
