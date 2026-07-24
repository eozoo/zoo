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

### Simple工程

- 创建工程

```text
mvn archetype:generate                  \
-DinteractiveMode=false                 \
-DarchetypeGroupId=com.cowave.zoo       \
-DarchetypeArtifactId=archetype-simple  \
-DarchetypeVersion=2.7.6       \
-DgroupId=org.zoo              \
-DartifactId=demo-simple       \
-Dversion=1.0.0                \
-Dpackage=org.zoo.demo.simple
```

- 目录结构

> 简化版的ddd工程目录，domain层主要只负责定义领域模型和对象

```text
{artifactId}
   ├─{artifactId}-domain    ## 领域模型，定义服务的各种领域模型和对象；
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

### DDD工程

- 创建工程

```text
mvn archetype:generate                \
-DinteractiveMode=false               \
-DarchetypeGroupId=com.cowave.zoo     \
-DarchetypeArtifactId=archetype-ddd   \
-DarchetypeVersion=2.7.6      \
-DgroupId=org.zoo             \
-DartifactId=demo-ddd         \
-Dversion=1.0.0               \
-Dpackage=org.zoo.demo.ddd
```

- 目录结构

> 相比Simple工程，DDD工程中的domain除了定义领域模型和对象，还要定义infra层的适配接口，以及负责领域核心业务biz操作的沉淀；然后service和infra完全解耦，通过domain层的接口进行对接；

```text
{artifactId}
   ├─{artifactId}-domain    ## 领域模型，定义服务的各种领域模型和对象，infra层的适配接口，以及沉淀核心的biz业务逻辑；
   ├─{artifactId}-infra     ## 基础组件，负责领域仓储接口的具体技术实现，比如数据库、缓存、消息中间件等处理，依赖${rootArtifactId}-domain；
   ├─{artifactId}-service   ## 业务处理，负责业务编排和领域能力调用，与${rootArtifactId}-infra完全解耦，只依赖${rootArtifactId}-domain；
   ├─{artifactId}-client    ## 远程调用，提供给外部调用的客户端，比如Rpc调用，依赖${rootArtifactId}-domain；
   ├─{artifactId}-app       ## 服务入口，定义启动类、controller接口、定时任务触发等，依赖${rootArtifactId}-service 和 ${rootArtifactId}-infra；
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

### DDD实体对象约定（仅供参考）

  | 对象后缀命名        | 说明                   | 定义&引用模块                    |
  |---------------|----------------------|----------------------------|
  | PO/DO         | 领域对象，与持久层库表一致（可省略后缀） | domain定义，所有模块引用（除了client）  |
  | PTO           | 持久层传输对象，多表联合查询结果集    | domain定义，所有模块引用（除了client）  |
  | Command/Query | 业务入参封装               | domain定义，所有模块引用（除了client）  |
  | VO            | 返回客户端数据对象            | domain定义，仅app/service引用    |
  | BO            | 业务过程对象               | domain定义，仅domain/service引用 |
  | DTO           | 服务间调用返回数据对象          | client定义，由引用方的infra适配      |
  | Req/Request   | 服务间调用请求参数对象          | client定义，由引用方的infra适配      |

```text
定义PTO是考虑到多表联合查询的场景（也可以拆分成简单查询然后在代码中组装数据，方式各有优劣），
这样在一些场景中可以一步到位，与PO/DO对象一样直接从infra层查出来交给上层app模块，不用中间再组装数据对象；
如果需要确实中间处理再出去的，可以在service层转换成VO再交给app模块；

定义Command/Query，也是想省掉中间不必要的数据对象转换，如果用app层的Request直接透传到infra层不是太合适，
所以从domain层的biz角度出发来定义Command/Query，让app和service层使用，这样透传到infra会比较能接受；

其中PTO/Command/VO允许继承PO/DO，如果字段区别不大的话，这样可以省掉大量重复定义；
除了DTO/Req/Request放在client模块，其它统一放在domain中也是为尽量简单，不特别违背DDD设计理念的情况下；
```
