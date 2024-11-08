#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${symbol_pound}${symbol_pound} ${artifactId}

${symbol_pound}${symbol_pound}${symbol_pound} 工程目录

> 目录中标记了（默认）的文件，在mvn compile后，可以在target中目录中找到；

<pre>
${rootArtifactId}
   ├─${rootArtifactId}-api       ${symbol_pound}${symbol_pound} 应用接口，比如controller接口定义，service层业务处理，依赖${rootArtifactId}-core；
   ├─${rootArtifactId}-core      ${symbol_pound}${symbol_pound} 核心处理，比如持久层、缓存、消息中间件等处理，依赖${rootArtifactId}-model；
   ├─${rootArtifactId}-model     ${symbol_pound}${symbol_pound} 模型定义，各个领域的模型，定义模型的属性和行为；
   ├─${rootArtifactId}-remote    ${symbol_pound}${symbol_pound} 远程调用，提供给外部调用的客户端，比如Rpc接口，依赖${rootArtifactId}-model；
   ├─${rootArtifactId}-starter   ${symbol_pound}${symbol_pound} 启动入口，定义启动类及一些配置，依赖${rootArtifactId}-api；
   │   ├─bin
   │   │  ├─env.properties                         ${symbol_pound}${symbol_pound} 环境变量定义               （默认，可覆盖）
   │   │  ├─setenv.sh                              ${symbol_pound}${symbol_pound} 运行前设置                （默认，可覆盖）
   │   │  ├─run.sh                                 ${symbol_pound}${symbol_pound} 运行脚本                  （默认，可覆盖）
   │   │  └─install.sh                             ${symbol_pound}${symbol_pound} Tar包安装脚本             （默认，可覆盖）
   │   ├─src    
   │   │  └─main    
   │   │     ├─java    
   │   │     │  └─${package}
   │   │     │     ├─...
   │   │     │     └─Application.java
   │   │     │
   │   │     └─resources
   │   │        ├─smart-doc.json                   ${symbol_pound}${symbol_pound} smart-doc接口文档描述
   │   │        ├─logback-spring.xml               ${symbol_pound}${symbol_pound} logback日志配置           （默认，可覆盖）
   │   │        ├─sql                              ${symbol_pound}${symbol_pound} liquibase数据库版本管理
   │   │        │  ├─changelog.yml
   │   │        │  ├─...
   │   │        ├─config                           ${symbol_pound}${symbol_pound} 应用配置（约定放在config目录中，使用yml格式）
   │   │        │  ├─application.yml    
   │   │        │  ├─...    
   │   │        └─META-INF    
   │   │           ├─common.yml                    ${symbol_pound}${symbol_pound} 默认配置
   │   │           └─i18n                          ${symbol_pound}${symbol_pound} 国际化资源
   │   │              ├─...    
   │   │    
   │   ├─pom.xml
   │   ├─favicon.ico                               ${symbol_pound}${symbol_pound} 网页图标                  （默认，可覆盖）
   │   ├─tar.sh                                    ${symbol_pound}${symbol_pound} Tar构建                  （默认，可覆盖）
   │   ├─deb.sh                                    ${symbol_pound}${symbol_pound} Deb构建                  （默认，可覆盖）
   │   ├─rpm.sh                                    ${symbol_pound}${symbol_pound} Rpm构建                  （默认，可覆盖）
   │   └─docker.sh                                 ${symbol_pound}${symbol_pound} Docker构建               （默认，可覆盖）
   ├─pom.xml
   └─README.md   
</pre>

- 应用打包

打包需要切换到模块${rootArtifactId}-starter目录下，然后可以执行以下命令：

`maven clean install -Dbuild=jar` 打成springboot jar，使用java -jar启动       
`maven clean install -Dbuild=tar` 打成Tar包      
`maven clean install -Dbuild=deb` 打成Deb包       
`maven clean install -Dbuild=rpm` 打成Rpm包       
`maven clean install -Dbuild=docker` 打成Docker镜像

