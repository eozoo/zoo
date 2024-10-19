#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${symbol_pound}${symbol_pound} ${artifactId}

${symbol_pound}${symbol_pound}${symbol_pound} 工程目录

<pre>
{项目}-{应用}
   ├─${rootArtifactId}-api       ${symbol_pound}${symbol_pound} 应用接口，比如controller接口定义，service层业务处理，依赖${rootArtifactId}-core；
   ├─${rootArtifactId}-core      ${symbol_pound}${symbol_pound} 核心处理，比如持久层、缓存、消息中间件等处理，依赖${rootArtifactId}-model；
   ├─${rootArtifactId}-model     ${symbol_pound}${symbol_pound} 模型定义，各个领域的模型，定义模型的属性和行为；
   ├─${rootArtifactId}-remote    ${symbol_pound}${symbol_pound} 远程调用，提供给外部调用的客户端，比如Rpc接口，依赖${rootArtifactId}-model；
   ├─${rootArtifactId}-starter   ${symbol_pound}${symbol_pound} 启动入口，定义启动类及一些配置，依赖${rootArtifactId}-api；
   │   ├─bin
   │   │  ├─env.properties                         ${symbol_pound}${symbol_pound}（默认）环境变量定义
   │   │  ├─setenv.sh                              ${symbol_pound}${symbol_pound}（默认）运行前设置
   │   │  ├─run.sh                                 ${symbol_pound}${symbol_pound}（默认）运行脚本
   │   │  └─install.sh                             ${symbol_pound}${symbol_pound}（默认）Tar包安装脚本
   │   ├─src    
   │   │  └─main    
   │   │     ├─java    
   │   │     │  └─${package}
   │   │     │     ├─...
   │   │     │     └─Application.java
   │   │     │
   │   │     └─resources
   │   │        ├─smart-doc.json                   ${symbol_pound}${symbol_pound} smart-doc接口文档描述
   │   │        ├─logback-spring.xml               ${symbol_pound}${symbol_pound}（默认）logback日志配置
   │   │        ├─sql                              ${symbol_pound}${symbol_pound} liquibase数据库版本管理
   │   │        │  ├─changelog.yml
   │   │        │  ├─...
   │   │        ├─config                           ${symbol_pound}${symbol_pound} 应用配置（约定使用yml文件，并且放在config目录中）
   │   │        │  ├─application.yml    
   │   │        │  ├─...    
   │   │        └─META-INF    
   │   │           ├─common.yml                    ${symbol_pound}${symbol_pound} 默认配置
   │   │           └─i18n                          ${symbol_pound}${symbol_pound} 国际化资源
   │   │              ├─...    
   │   │    
   │   ├─pom.xml  
   │   ├─tar.sh                                    ${symbol_pound}${symbol_pound}（默认）Tar构建
   │   ├─deb.sh                                    ${symbol_pound}${symbol_pound}（默认）Deb构建脚本
   │   └─docker.sh                                 ${symbol_pound}${symbol_pound}（默认）Docker构建脚本
   ├─pom.xml    
   └─README.md   
</pre>

- 目录中（默认）的文件，表示已提供了默认的文件定义，如果需要也可以进行修改和覆盖；

> mvn compile 后，能够在target中目录中找到默认的文件，可以拷贝出来进行修改（拷贝到约定的路径）；

- 应用打包

打包需要切换到模块${rootArtifactId}-starter目录下，然后可以执行以下命令：

`maven clean install -Dbuild=jar` springboot默认的打包方式，整个打成一个jar包，使用命令行自行启动；    
`maven clean install -Dbuild=tar` 打成Tar包，针对Linux环境下的裸机安装；    
`maven clean install -Dbuild=deb` 打成Deb包，针对ubuntu环境下的裸机安装；    
`maven clean install -Dbuild=docker` 打成Docker镜像

