[![Build Status](https://github.com/eozoo/zoo/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/eozoo/zoo/actions)
![Static Badge](https://img.shields.io/badge/Java-17-brightgreen)
![Static Badge](https://img.shields.io/badge/spring--boot-2.7.0-brightgreen)
![Maven central](https://img.shields.io/badge/maven--central-2.7.6-brightgreen)
[![License](https://img.shields.io/badge/license-Apache--2.0-brightgreen)](http://www.apache.org/licenses/LICENSE-2.0.txt)

## zoo

springboot工程框架

- 文档说明：[wiki](https://github.com/eozoo/zoo/wiki)

## 工程模板

> Windows环境如果cmd执行报错，可以用git bash执行

- 参数说明

```text
-DinteractiveMode       ## 是否交互式创建
-DarchetypeGroupId      ## 固定参数：模板的groupId
-DarchetypeArtifactId   ## 固定参数：模板的artifactId
-DarchetypeVersion      ## 固定参数：模板的version
-DgroupId      ## 必填参数：创建项目的groupId
-DartifactId   ## 必填参数：创建项目的artifactId
-Dversion      ## 必填参数：创建项目的version
-Dpackage      ## 必填参数：创建项目的代码package
-DserverPath   ## 选填参数：应用根路径，默认：/
-DserverPort   ## 选填参数：应用端口，默认：8080
-Dauthor       ## 选填参数：注释作者，默认：zoo
```

### Mvc工程

- 创建工程

```text
mvn archetype:generate                \
-DinteractiveMode=false               \
-DarchetypeGroupId=com.cowave.zoo     \
-DarchetypeArtifactId=archetype-mvc   \
-DarchetypeVersion=2.7.6              \
-DgroupId=com.cowave.demo      \
-DartifactId=zoo-mvc           \
-Dversion=1.0.0                \
-Dpackage=com.cowave.zoo.mvc
```

- 目录结构

> 按照Mvc的结构，代码package一般分成controller/service/model/mapper，或者可以再加一层api

```text
{项目名}-{应用名}
   ├─bin
   │  ├─env.properties                         ## 环境变量定义                （默认，可覆盖）
   │  ├─setenv.sh                              ## 运行前设置                  （默认，可覆盖）
   │  ├─run.sh                                 ## 运行脚本                   （默认，可覆盖）
   │  └─install.sh                             ## Tar包安装脚本               （默认，可覆盖）
   ├─src    
   │  └─main    
   │     ├─java    
   │     │  └─{package}
   │     │     ├─...
   │     │     └─Application.java
   │     │
   │     └─resources
   │        ├─smart-doc.json                   ## smart-doc接口文档描述
   │        ├─logback-spring.xml               ## logback日志配置            （默认，可覆盖）
   │        ├─sql                              ## liquibase数据库版本管理
   │        │  ├─changelog.yml
   │        │  ├─...
   │        ├─config                           ## 应用配置（约定放在config目录中，使用yml格式）
   │        │  ├─application.yml    
   │        │  ├─...    
   │        └─META-INF    
   │           ├─zoo.yml                       ## 默认配置
   │           └─i18n                          ## 国际化资源
   │              ├─...    
   │    
   ├─tar.sh                                    ## Tar构建                   （默认，可覆盖）
   ├─deb.sh                                    ## Deb构建                   （默认，可覆盖）
   ├─rpm.sh                                    ## Rpm构建                   （默认，可覆盖）
   ├─docker.sh                                 ## Docker构建                （默认，可覆盖）
   ├─pom.xml    
   ├─favicon.ico                               ## 网页图标                   （默认，可覆盖）
   └─README.md   
```

### DDD工程

- 创建工程

```text
mvn archetype:generate                \
-DinteractiveMode=false               \
-DarchetypeGroupId=com.cowave.zoo     \
-DarchetypeArtifactId=archetype-ddd   \
-DarchetypeVersion=2.7.6              \
-DgroupId=com.cowave.demo      \
-DartifactId=zoo-ddd           \
-Dversion=1.0.0                \
-Dpackage=com.cowave.zoo.ddd
```

- 目录结构

> 这里是按照自己理解进行的工程目录划分，考虑了一些对以往代码风格习惯的兼容。如果不能满足，可以自己根据实际情况调整，另外关于DDD工程可以参考: https://github.com/alibaba/COLA     

```text
{artifactId}
   ├─{artifactId}-domain    ## 领域模型，定义服务的各种模型属性和基本行为；
   ├─{artifactId}-infra     ## 基础组件，对基础组件调用的封装，比如数据库、缓存、消息中间件等处理，依赖${rootArtifactId}-domain；
   ├─{artifactId}-service   ## 业务处理，定义具体业务的实现逻辑，依赖${rootArtifactId}-infra；
   ├─{artifactId}-client    ## 远程调用，提供给外部调用的客户端，比如Rpc调用，依赖${rootArtifactId}-domain；
   ├─{artifactId}-app       ## 服务入口，定义启动类、controller接口、定时任务触发等，依赖${rootArtifactId}-service；
   │   ├─bin
   │   │  ├─env.properties                         ## 环境变量定义            （默认，可覆盖）
   │   │  ├─setenv.sh                              ## 运行前设置              （默认，可覆盖）
   │   │  ├─run.sh                                 ## 运行脚本               （默认，可覆盖）
   │   │  └─install.sh                             ## Tar包安装脚本           （默认，可覆盖）
   │   ├─src    
   │   │  └─main    
   │   │     ├─java    
   │   │     │  └─{package}
   │   │     │     ├─...
   │   │     │     └─Application.java
   │   │     │
   │   │     └─resources
   │   │        ├─smart-doc.json                   ## smart-doc接口文档描述
   │   │        ├─logback-spring.xml               ## logback日志配置         （默认，可覆盖）
   │   │        ├─sql                              ## liquibase数据库版本管理
   │   │        │  ├─changelog.yml
   │   │        │  ├─...
   │   │        ├─config                           ## 应用配置（约定放在config目录中，使用yml格式）
   │   │        │  ├─application.yml    
   │   │        │  ├─...    
   │   │        └─META-INF    
   │   │           ├─zoo.yml                       ## 默认配置
   │   │           └─i18n                          ## 国际化资源
   │   │              ├─...    
   │   │    
   │   ├─pom.xml  
   │   ├─favicon.ico                               ## 网页图标                （默认，可覆盖）
   │   ├─tar.sh                                    ## Tar构建                （默认，可覆盖）
   │   ├─deb.sh                                    ## Deb构建                （默认，可覆盖）
   │   ├─rpm.sh                                    ## Rpm构建                （默认，可覆盖）
   │   └─docker.sh                                 ## Docker构建             （默认，可覆盖）
   ├─pom.xml    
   └─README.md   
```
